
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
