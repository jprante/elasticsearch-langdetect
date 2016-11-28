from io import BytesIO
import re
from zipfile import ZipFile

import baker
import requests
import xmltodict

# Supported languages according to https://github.com/shuyo/language-detection/blob/wiki/LanguageList.md
_SUPPORTED_LANGUAGES = {
    'af': 'Afrikaans',
    'ar': 'Arabic',
    'bg': 'Bulgarian',
    'bn': 'Bengali',
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
def generate_udhr_dataset(out_path='../src/test/resources/org/xbib/elasticsearch/index/mapper/langdetect/udhr.tsv'):
    """
    Download and generate the Universal Declaration of Human Rights (UDHR) dataset.

    The generated dataset consists of translations of UDHR for all the languages supported by the plugin. This command
    applies minimal preprocessing to create the dataset, including: matching the downloaded dataset's language
    codes with those used by the plugin, removing each file's English intro, and dropping redundant whitespace.

    :param out_path: output path for the dataset file, which will be written in tab-separated format with two
                     columns: language code and text
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
        # Norwegian, Bokmål (spoken by 85% of Norwegians -- the original Norwegian Wikipedia language)
        # See https://en.wikipedia.org/wiki/Norwegian_Wikipedia
        'nb': 'no',
        # There's only one Portuguese Wikipedia, so go with Portugal's Portuguese translation
        # See https://en.wikipedia.org/wiki/Portuguese_Wikipedia
        'pt-PT': 'pt',
        # Mandarin Chinese (Simplified) and Mandarin Chinese (Traditional)
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
    whitespace_pattern = re.compile(r'\s+', flags=re.UNICODE | re.MULTILINE)
    with open(out_path, 'w', encoding='utf-8') as out_file:
        for supported_code, filename in supported_code_to_filename.items():
            out_file.write(supported_code)
            out_file.write('\t')
            # Remove the first 6 lines (English header) and clean up whitespace
            out_file.write(whitespace_pattern.sub(' ', ' '.join(filename_to_content[filename].split('\n')[6:])).strip())
            out_file.write('\n')


if __name__ == '__main__':
    baker.run()
