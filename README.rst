.. image:: ../../../elasticsearch-langdetect/raw/master/speechbubble_green.png

Image by `CurtiveArticide <http://www.softicons.com/free-icons/designers/curtivearticide>`_ `CC Attribution-NonCommercial 3.0 Unported <http://creativecommons.org/licenses/by-nc/3.0/>`_

Elasticsearch Langdetect Plugin
===============================

This is an implementation of a plugin for `Elasticsearch <http://github.com/elasticsearch/elasticsearch>`_ using the 
implementation of Nakatani Shuyo's `language detector <http://code.google.com/p/language-detection/>`_.

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


Thanks to Alexander Reelsen for his OpenNLP plugin, from where I have copied and adapted the mapping type analysis code.


Installation
------------

Current version of the plugin is **2.0.1** (Oct 25, 2013)

Prerequisites::

  Java JDK 7
  Elasticsearch 0.90+

=============  =========  =================  =============================================================
ES version     Plugin     Release date       Command
-------------  ---------  -----------------  -------------------------------------------------------------
0.90.3         1.2.0      Sep 14, 2013       ./bin/plugin --install langdetect --url http://bit.ly/19RBF3b
0.90.5         2.0.0      Oct 22, 2013       ./bin/plugin --install langdetect --url http://bit.ly/1djTYCS
0.90.5         **2.0.1**  Oct 25, 2013       ./bin/plugin --install langdetect --url http://bit.ly/16861yL
=============  =========  =================  =============================================================

Bintray:

https://bintray.com/pkg/show/general/jprante/elasticsearch-plugins/elasticsearch-langdetect

Language detection mapping example
==================================

::

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

        curl -XGET 'localhost:9200/test/_refresh'

        curl -XPOST 'localhost:9200/test/_search' -d '
        {
           "query" : {
               "term" : {
                    "content.lang" : "en"
               }
           }
        }
        '
        curl -XPOST 'localhost:9200/test/_search' -d '
        {
           "query" : {
               "term" : {
                    "content.lang" : "de"
               }
           }
        }
        '

        curl -XPOST 'localhost:9200/test/_search' -d '
        {
           "query" : {
               "term" : {
                    "content.lang" : "fr"
               }
           }
        }
        '

Language detection with attachment mapper plugin
================================================

::

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
					"content" : { "type" : "string" },
					"language" : { "type" : "langdetect" }
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
			 "content.language.lang" : "en"
		  }
	   }
	}
	'



Language detection REST API example
===================================

::

    curl -XPOST 'localhost:9200/_langdetect?pretty' -d 'This is a test'
	{
	  "ok" : true,
	  "languages" : [ {
	    "language" : "en",
	    "probability" : 0.9999971603535163
	  } ]
	}

    curl -XPOST 'localhost:9200/_langdetect?pretty' -d 'Das ist ein Test'
	{
      "ok" : true,
      "languages" : [ {
        "language" : "de",
        "probability" : 0.9999993070517024
      } ]
    }

    curl -XPOST 'localhost:9200/_langdetect?pretty' -d 'Datt isse ne test'
	{
      "ok" : true,
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


License
=======

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