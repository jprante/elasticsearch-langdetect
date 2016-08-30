![Image 'Tower of Babel'](https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_%28Rotterdam%29_-_Google_Art_Project.jpg/299px-Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_%28Rotterdam%29_-_Google_Art_Project.jpg)

# Elasticsearch Langdetect Plugin

This is an implementation of a plugin for [Elasticsearch](http://github.com/elasticsearch/elasticsearch) using the 
implementation of Nakatani Shuyo's [language detector](http://code.google.com/p/language-detection/).

It uses 3-gram character and a Bayesian filter with various normalizations and feature sampling.
The precision is over 99% for 53 languages.

The plugin offers a mapping type to specify fields where you want to enable language detection.
Detected languages are indexed into a subfield of the field named 'lang', as you can see in the example.
The field can be queried for language codes.

You can use the multi_field mapping type to combine this plugin with the attachment mapper plugin, to
enable language detection in base64-encoded binary data. Currently, UTF-8 texts are supported only.

The plugin offers also a REST endpoint, where a short text can be posted to in UTF-8, and the plugin responds
with a list of recognized languages.

Here is a list of languages code recognized:

af
ar
bg
bn
cs
da
de
el
en
es
et
fa
fi
fr
gu
he
hi
hr
hu
id
it
ja
kn
ko
lt
lv
mk
ml
mr
ne
nl
no
pa
pl
pt
ro
ru
sk
sl
so
sq
sv
sw
ta
te
th
tl
tr
uk
ur
vi
zh-cn
zh-tw


## Versions

![Travis](https://travis-ci.org/jprante/elasticsearch-langdetect.png)

| Elasticsearch  | Plugin         | Release date |
| -------------- | -------------- | ------------ |
| 2.3.3          | 2.3.3.0        | Jun 11, 2016 |
| 2.3.2          | 2.3.2.0        | Jun 11, 2016 |
| 2.3.1          | 2.3.1.0        | Apr 11, 2016 |
| 2.2.1          | 2.2.1.0        | Apr 11, 2016 |
| 2.2.0          | 2.2.0.2        | Mar 25, 2016 |
| 2.2.0          | 2.2.0.1        | Mar  6, 2016 |
| 2.1.1          | 2.1.1.0        | Dec 20, 2015 |
| 2.1.0          | 2.1.0.0        | Dec 15, 2015 |
| 2.0.1          | 2.0.1.0        | Dec 15, 2015 |
| 2.0.0          | 2.0.0.0        | Nov 12, 2015 |
| 2.0.0-beta2    | 2.0.0-beta2.0  | Sep 19, 2015 |
| 1.6.0          | 1.6.0.0        | Jul  1, 2015 |
| 1.4.0          | 1.4.4.2        | Apr  3, 2015 |
| 1.4.0          | 1.4.4.1        | Mar  4, 2015 |
| 1.4.0          | 1.4.0.2        | Nov 26, 2014 |
| 1.4.0          | 1.4.0.1        | Nov 20, 2014 |
| 1.4.0          | 1.4.0.0        | Nov 14, 2014 |
| 1.3.1          | 1.3.0.0        | Jul 30, 2014 |
| 1.2.1          | 1.2.1.1        | Jun 18, 2014 |


## Installation Elasticsearch 2.x

    ./bin/plugin install https://github.com/jprante/elasticsearch-langdetect/archive/2.3.3.0.zip

## Installation Elasticsearch 1.x

    ./bin/plugin -install langdetect -url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-langdetect/1.6.0.0/elasticsearch-langdetect-1.6.0.0-plugin.zip

Do not forget to restart the node after installing.

## Project docs

The Maven project site is available at [Github](http://jprante.github.io/elasticsearch-langdetect)

## Issues

All feedback is welcome! If you find issues, please post them at [Github](https://github.com/jprante/elasticsearch-langdetect/issues)

# Examples

## A simple language detection example

In this example, we create a simple detector field, and write text to it for detection.

    curl -XDELETE 'localhost:9200/test'

    curl -XPUT 'localhost:9200/test'

    curl -XPOST 'localhost:9200/test/article/_mapping' -d '
    {
      "article" : {
        "properties" : {
           "content" : { "type" : "langdetect" }
        }
      }
    }
    '

    curl -XPUT 'localhost:9200/test/article/1' -d '
    {
      "title" : "Some title",
      "content" : "Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?"
    }
    '

    curl -XPUT 'localhost:9200/test/article/2' -d '
    {
      "title" : "Ein Titel",
      "content" : "Einigkeit und Recht und Freiheit für das deutsche Vaterland!"
    }
    '

    curl -XPUT 'localhost:9200/test/article/3' -d '
    {
      "title" : "Un titre",
      "content" : "Allons enfants de la Patrie, Le jour de gloire est arrivé!"
    }
    '

A search for the detected language codes is a simple term query, like this:

    curl -XGET 'localhost:9200/test/_refresh'

    curl -XPOST 'localhost:9200/test/_search' -d '
    {
       "query" : {
           "term" : {
                "content" : "en"
           }
       }
    }
    '
    curl -XPOST 'localhost:9200/test/_search' -d '
    {
       "query" : {
           "term" : {
                "content" : "de"
           }
       }
    }
    '

    curl -XPOST 'localhost:9200/test/_search' -d '
    {
       "query" : {
           "term" : {
                "content" : "fr"
           }
       }
    }
    '

## Show stored language codes 
 
Using multifields, it is possible to store the text alongside with the detected language(s).
Here, we use another (short nonsense) example text for demonstration,
which has more than one detected language code.

    curl -XDELETE 'localhost:9200/test'

    curl -XPUT 'localhost:9200/test'

    curl -XPOST 'localhost:9200/test/article/_mapping' -d '
    {
      "article" : {
        "properties" : {
           "content" : { 
             "type" : "multi_field",
               "fields" : {
                   "content" : {
                       "type" : "string"
                   },
                   "language" : {
                         "type": "langdetect",
                         "store" : true
                   }
               }
           }
        }
      }
    }
    '

    curl -XPUT 'localhost:9200/test/article/1' -d '
    {
      "content" : "watt datt"
    }
    '

    curl -XGET 'localhost:9200/test/_refresh'

    curl -XPOST 'localhost:9200/test/_search?pretty' -d '
    {
       "fields" : "content.language",
       "query" : {
           "match" : {
                "content" : "watt datt"
           }
       }
    }
    '

The result is 

    {
     "took" : 2,
     "timed_out" : false,
     "_shards" : {
       "total" : 5,
       "successful" : 5,
       "failed" : 0
     },
     "hits" : {
       "total" : 1,
       "max_score" : 0.51623213,
       "hits" : [ {
         "_index" : "test",
         "_type" : "article",
         "_id" : "1",
         "_score" : 0.51623213,
         "fields" : {
           "content.language" : [ "sv", "it", "nl" ]
         }
       } ]
     }
    }


## Language detection with attachment mapper plugin

	curl -XDELETE 'localhost:9200/test'

	curl -XPUT 'localhost:9200/test'  -d '
	{
	  "mappings" : {
		"_default_" : {
		  "properties" : {
			"content" : {
			  "type" : "attachment",
			  "fields" : {
				"content" : {
				  "type" : "multi_field",
				  "fields" : {
					"content" : { 
					  "type" : "string" 
					},
					"language" : { 
					  "type" : "langdetect",
					  "binary" : true
					}
				  }
				}
			  }
			}
		  }
		}
	  }
	}
	'

	rm index.tmp
	echo -n '{"content":"' >> index.tmp
	echo "This is a very simple text in plain english" | base64  >> index.tmp
	echo -n '"}' >> index.tmp
	curl -XPOST --data-binary "@index.tmp" 'localhost:9200/test/docs/1'
	rm index.tmp

	curl -XPOST 'localhost:9200/test/_refresh'

	curl -XGET 'localhost:9200/test/docs/_mapping?pretty'

	curl -XPOST 'localhost:9200/test/docs/_search?pretty' -d '
	{
	 "query" : {
		  "match" : {
			 "content" : "very simple"
		  }
	   }
	}
	'

	curl -XPOST 'localhost:9200/test/docs/_search?pretty' -d '
	{
	 "query" : {
		  "term" : {
			 "content.language" : "en"
		  }
	   }
	}
	'

## Language detection REST API Example

    curl -XPOST 'localhost:9200/_langdetect?pretty' -d 'This is a test'
	{
	  "profile" : "/langdetect/",
	  "languages" : [ {
	    "language" : "en",
	    "probability" : 0.9999971603535163
	  } ]
	}

    curl -XPOST 'localhost:9200/_langdetect?pretty' -d 'Das ist ein Test'
	{
	  "profile" : "/langdetect/",
      "languages" : [ {
        "language" : "de",
        "probability" : 0.9999993070517024
      } ]
    }

    curl -XPOST 'localhost:9200/_langdetect?pretty' -d 'Datt isse ne test'
	{
	  "profile" : "/langdetect/",
      "languages" : [ {
        "language" : "no",
        "probability" : 0.5714251911820175
      }, {
        "language" : "de",
        "probability" : 0.14285762298521493
      }, {
        "language" : "it",
        "probability" : 0.14285706984044144
      } ]
    }

## Use _langdetect endpoint from Sense

    GET _langdetect
    {
       "text": "das ist ein test"
    }
	
	
## Change profile of language detection to "short text" profile

    curl -XPOST 'localhost:9200/_langdetect?pretty&profile=short-text' -d 'Das ist ein Test'
	{
	  "profile" : "/langdetect/short-text/",
      "languages" : [ {
        "language" : "de",
        "probability" : 0.9999993070517024
      } ]
    }


# Settings

These settings can be used in `elasticsearch.yml` to modify language detection.

Use with caution. You don't need to modify settings. This list is just for the sake of completeness.
For successful modification of the model parameters, you should study the source code
and be familiar with probabilistic matching using naive bayes with character n-gram. 
See also Ted Dunning,
[Statistical Identification of Language](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.48.1958), 1994.

`languages` - a comma-separated list of language codes such as (de,en,fr...) used to restrict (and speed up) the detection process

`map.<code>` - a substitution code for a language code

`number_of_trials` - number of trials, affects CPU usage (default: 7)

`alpha` - additional smoothing parameter, default: 0.5

`alpha_width` - the width of smoothing, default: 0.05

`iteration_limit` - safeguard to break loop, default: 10000

`prob_threshold` - default: 0.1

`conv_threshold` - detection is terminated when normalized probability exceeds 
this threshold, default: 0.99999

`base_freq` - default 10000

# Credits

Thanks to Alexander Reelsen for his OpenNLP plugin, from where I have copied and 
adapted the mapping type analysis code.

# License

Elasticsearch Langdetect Plugin

Derived work of language-detection by Nakatani Shuyo http://code.google.com/p/language-detection/

Copyright (C) 2012 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
you may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
