from collections import defaultdict
from fractions import Fraction
from io import BytesIO
import json
import math
import os
import re
import shutil
from zipfile import ZipFile

import baker
import ftfy
import requests
import xmltodict

_THIS_PATH = os.path.dirname(os.path.abspath(__file__))
_TEST_RESOURCES_PATH = os.path.join(_THIS_PATH, '../src/test/resources/org/xbib/elasticsearch/index/mapper/langdetect/')
_MAIN_RESOURCES_PATH = os.path.join(_THIS_PATH, '../src/main/resources/langdetect/')

# Supported languages according to https://github.com/shuyo/language-detection/blob/wiki/LanguageList.md
_SUPPORTED_LANGUAGES = {
    'af': 'Afrikaans',
    'ar': 'Arabic',
    'bg': 'Bulgarian',
    'bn': 'Bengali',
    'ca': 'Catalan',  # Short profile only
    'cs': 'Czech',
    'da': 'Danish',
    'de': 'German',
    'el': 'Greek',
    'en': 'English',
    'es': 'Spanish',
    'et': 'Estonian',
    'fa': 'Persian',
    'fi': 'Finnish',
    'fr': 'French',
    'gu': 'Gujarati',
    'he': 'Hebrew',
    'hi': 'Hindi',
    'hr': 'Croatian',
    'hu': 'Hungarian',
    'id': 'Indonesian',
    'it': 'Italian',
    'ja': 'Japanese',
    'kn': 'Kannada',
    'ko': 'Korean',
    'lt': 'Lithuanian',
    'lv': 'Latvian',
    'mk': 'Macedonian',
    'ml': 'Malayalam',
    'mr': 'Marathi',
    'ne': 'Nepali',
    'nl': 'Dutch',
    'no': 'Norwegian',
    'pa': 'Punjabi',
    'pl': 'Polish',
    'pt': 'Portuguese',
    'ro': 'Romanian',
    'ru': 'Russian',
    'si': 'Sinhalese',  # Short profile only
    'sk': 'Slovak',
    'sl': 'Slovene',
    'so': 'Somali',
    'sq': 'Albanian',
    'sv': 'Swedish',
    'sw': 'Swahili',
    'ta': 'Tamil',
    'te': 'Telugu',
    'th': 'Thai',
    'tl': 'Tagalog',
    'tr': 'Turkish',
    'uk': 'Ukrainian',
    'ur': 'Urdu',
    'vi': 'Vietnamese',
    'zh-cn': 'SimplifiedChinese',
    'zh-tw': 'TraditionalChinese'
}


@baker.command
def generate_udhr_dataset(out_path=os.path.join(_TEST_RESOURCES_PATH, 'udhr.tsv')):
    """
    Download and generate the Universal Declaration of Human Rights (UDHR) dataset.

    The generated dataset consists of translations of UDHR for all the languages supported by the plugin. This command
    applies minimal preprocessing to create the dataset, including: matching the downloaded dataset's language
    codes with those used by the plugin, removing each file's English intro, and dropping redundant whitespace.

    :param out_path: output path for the dataset file, which will be written in tab-separated format with two
                     columns: language code and text.
    """
    # Download and extract the translations
    input_zip = ZipFile(BytesIO(requests.get('http://unicode.org/udhr/assemblies/udhr_txt.zip').content))
    filename_to_content = {name: input_zip.read(name).decode('utf-8') for name in input_zip.namelist()}

    # Map the supported language codes to the translations. Generally, supported language codes (which come from old
    # Wikipedia dumps) match the UDHR BCP47 codes, but there are some exceptions.
    bcp47_code_to_supported_code = {
        # Monotonic Greek (apparently more modern with fewer diacritics than the polytonic system)
        'el-monoton': 'el',
        # German (the other option is de-1901, which seems too old)
        'de-1996': 'de',
        # Bokmål Norwegian (spoken by 85% of Norwegians -- the original Norwegian Wikipedia language)
        # See https://en.wikipedia.org/wiki/Norwegian_Wikipedia
        'nb': 'no',
        # There's only one Portuguese Wikipedia, so go with Portugal's Portuguese translation
        # See https://en.wikipedia.org/wiki/Portuguese_Wikipedia
        'pt-PT': 'pt',
        # Simplified and Traditional Chinese
        # The supported codes are a relic from old Chinese Wikipedia. Nowadays localisation is done on the fly.
        # See https://en.wikipedia.org/wiki/Chinese_Wikipedia
        'zh-Hans': 'zh-cn',
        'zh-Hant': 'zh-tw'
    }
    supported_code_to_filename = {}
    for file_info in xmltodict.parse(filename_to_content['index.xml'])['udhrs']['udhr']:
        supported_code = bcp47_code_to_supported_code.get(file_info['@bcp47'], file_info['@bcp47'])
        if supported_code in _SUPPORTED_LANGUAGES:
            # Some languages have multiple translations, so we just use the last one (which seems to be more recent)
            supported_code_to_filename[supported_code] = 'udhr_{}.txt'.format(file_info['@f'])
    assert len(_SUPPORTED_LANGUAGES) == len(supported_code_to_filename)

    # Write the selected translations to the output file
    whitespace_pattern = re.compile(r'\s+')
    with open(out_path, 'w', encoding='utf-8') as out_file:
        for supported_code, filename in sorted(supported_code_to_filename.items()):
            # Remove the first 6 lines (English header) and clean up whitespace
            clean_text = whitespace_pattern.sub(' ', ' '.join(filename_to_content[filename].split('\n')[6:])).strip()
            out_file.write('{}\t{}\n'.format(supported_code, clean_text))


@baker.command
def generate_wordpress_translations_dataset(out_path=os.path.join(_TEST_RESOURCES_PATH, 'wordpress-translations.tsv'),
                                            texts_per_language=50):
    """
    Download and generate the WordPress interface translations dataset.

    The generated dataset consists of translations for WordPress 4.6.x versions. This command applies minimal processing
    to create the dataset, including: matching the dataset's language codes with those used by the plugin, unescaping
    HTML entities, and stripping variable placeholders, HTML tags, and redundant whitespace.

    :param out_path: output path for the dataset file, which will be written in tab-separated format with two
                     columns: language code and text.
    :param texts_per_language: number of texts to retain per langauge. The output file will contain up to this number of
                               texts per language, excluding URL translations and word lists. The longest texts for
                               each language are retained.
    """
    url_template = 'https://translate.wordpress.org/projects/wp/4.6.x/{}/default/export-translations?format=json'
    requests_session = requests.Session()
    wp_placeholder_pattern = re.compile(r'(%\d*\$?[sd])|(###[A-Z_]+###)')
    html_tag_pattern = re.compile(r'<[^>]+>')
    whitespace_pattern = re.compile(r'\s+')
    with open(out_path, 'w', encoding='utf-8') as out_file:
        for supported_code in sorted(_SUPPORTED_LANGUAGES):
            # Use Australian and Bokmål Norwegian as the representative English and Norwegian variants, respectively
            if supported_code == 'en':
                wp_code = 'en-au'
            elif supported_code == 'no':
                wp_code = 'nb'
            else:
                wp_code = supported_code
            # Clean and retain the longest texts
            clean_texts_with_len = []
            for original_text, translations in requests_session.get(url_template.format(wp_code)).json().items():
                # Skip links and simple lists (e.g., stopwords aren't translated to Chinese)
                if original_text.startswith('http') or original_text.startswith('Comma-separated'):
                    continue
                for translation in translations:
                    # Skip texts that haven't been translated
                    if supported_code != 'en' and original_text == translation:
                        continue
                    clean_text = wp_placeholder_pattern.sub('', translation)
                    clean_text = ftfy.fixes.unescape_html(clean_text)
                    clean_text = html_tag_pattern.sub('', clean_text)
                    clean_text = whitespace_pattern.sub(' ', clean_text).strip()
                    clean_texts_with_len.append((len(clean_text), clean_text))
            clean_texts_with_len.sort(reverse=True)
            for _, clean_text in clean_texts_with_len[:texts_per_language]:
                out_file.write('{}\t{}\n'.format(supported_code, clean_text))


@baker.command
def merge_language_profiles(output_profile_dir='merged-average'):
    """
    Preprocess the original default and short-text profile files, averaging the normalized n-gram frequencies from the
    two profiles to create a merged profile.

    For example, if the trigram "xyz" appears 1 time out of 1000 trigrams in a default profile file and 1 out of 100
    in a short profile file, its merged-average frequency would be (0.001 + 0.01) / 2 = 0.0055. This frequency is then
    converted back to an integer, as the plugin's Java code assumes the frequencies are integers.

    :param output_profile_dir: directory name under the main resource path where the merged profile will be saved,
                               overwriting any existing files in the directory
    """
    merged_dir = os.path.join(_MAIN_RESOURCES_PATH, output_profile_dir)
    shutil.rmtree(merged_dir, ignore_errors=True)
    os.mkdir(merged_dir)
    for lang in _SUPPORTED_LANGUAGES:
        merged_profile = dict(name=lang, freq=defaultdict(Fraction), n_words=[1, 1, 1])
        profile_paths = []
        for profile_name in ['default', 'short']:
            profile_path = os.path.join(_MAIN_RESOURCES_PATH, '' if profile_name == 'default' else 'short-text', lang)
            if os.path.exists(profile_path):
                profile_paths.append(profile_path)
        # Copy the original profile without any processing if only one exists
        for profile_path in profile_paths:
            with open(profile_path, encoding='utf-8') as profile_file:
                profile = json.load(profile_file)
            # The n_words sums of some profiles are wrong so we fix them here
            profile['n_words'] = [0, 0, 0]
            for ngram, count in profile['freq'].items():
                profile['n_words'][len(ngram) - 1] += count
            for ngram, count in profile['freq'].items():
                merged_profile['freq'][ngram] += Fraction(count,
                                                          profile['n_words'][len(ngram) - 1] * len(profile_paths))
        # The least common multiplier of the frequency denominators for each n-gram length is the new n_words
        merged_n_words = merged_profile['n_words']
        for ngram, freq in merged_profile['freq'].items():
            n_words_index = len(ngram) - 1
            merged_n_words[n_words_index] = int(merged_n_words[n_words_index] * freq.denominator /
                                                math.gcd(merged_n_words[n_words_index], freq.denominator))
        # Ensure we don't exceed the maximum long value in Java
        for n_words in merged_n_words:
            assert n_words < 2 ** 63
        n_words_check = list(merged_n_words)
        for ngram, freq in merged_profile['freq'].items():
            merged_profile['freq'][ngram] = int(merged_n_words[len(ngram) - 1] * freq)
            n_words_check[len(ngram) - 1] -= merged_profile['freq'][ngram]
        assert not sum(n_words_check)
        with open(os.path.join(merged_dir, lang), 'w', encoding='utf-8') as out_file:
            json.dump(merged_profile, out_file, ensure_ascii=False, separators=',:')

if __name__ == '__main__':
    baker.run()
