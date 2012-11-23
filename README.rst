Elasticsearch Langdetect Plugin
===============================

This is an implementation of a plugin for `Elasticsearch <http://github.com/elasticsearch/elasticsearch>`_ using the 
implementation of Nakatani Shuyo's `language detector <http://code.google.com/p/language-detection/>`_.

It uses 3-gram character and a Bayesian filter with various normalizations and feature sampling. The precision is over 99% for 53 languages.

The plugin offers a REST endpoint where a short text can be posted in UTF-8, and Elasticsearch responds with a list of recognized languages.

Currently, it does not provide automatic language-aware indexing. It is just `"sugar coating" <https://github.com/elasticsearch/elasticsearch/issues/489>`_. You have to evaluate the response by yourself and take the appropriate language-dependent action.

Installation
------------

The current version of the plugin is **1.0.0**

In order to install the plugin, please run

``bin/plugin -install jprante/elasticsearch-langdetect/1.0.0``.

Be aware, in case the version number is omitted, you will have the source code installed for manual compilation.

================= ================
Langdetect Plugin ElasticSearch
================= ================
master            0.20.x -> master
1.0.0             0.20.x           
================= ================


Example
=======

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