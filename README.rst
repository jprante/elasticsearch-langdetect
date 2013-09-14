Elasticsearch Langdetect Plugin
===============================

This is an implementation of a plugin for `Elasticsearch <http://github.com/elasticsearch/elasticsearch>`_ using the 
implementation of Nakatani Shuyo's `language detector <http://code.google.com/p/language-detection/>`_.

It uses 3-gram character and a Bayesian filter with various normalizations and feature sampling. The precision is over 99% for 53 languages.

The plugin offers a REST endpoint where a short text can be posted in UTF-8, and Elasticsearch responds with a list of recognized languages.

Currently, it does not provide automatic language-aware indexing. It is just `"sugar coating" <https://github.com/elasticsearch/elasticsearch/issues/489>`_. You have to evaluate the response by yourself and take the appropriate language-dependent action.

Installation
------------

The current version of the plugin is **1.1.0** (Sep 13, 2013)

Prerequisites::

  Elasticsearch 0.90.3+

Bintray:

https://bintray.com/pkg/show/general/jprante/elasticsearch-plugins/elasticsearch-langdetect

`Direct download <http://dl.bintray.com/jprante/elasticsearch-plugins/org/xbib/elasticsearch/plugin/elasticsearch-langdetect/1.1.0/elasticsearch-knapsack-2.0.1.zip>`_

Command::

  ./bin/plugin -url ... -install langdetect

================= ================
Langdetect Plugin ElasticSearch
================= ================
master            0.90.x -> master
1.1.0             0.90.x
1.0.0             0.20.x           
================= ================

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

Language detection API example
==============================

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