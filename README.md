# wewe

A small weather application using [re-frame](https://github.com/day8/re-frame), see
it in action [here](https://wewe.azurewebsites.net/).

## Prerequisites 

A jdk (tested on 8 and 12 openjdk) and [leiningen](https://leiningen.org/)

## Building just the frontend

```
lein prod
```

grab and deploy the contents of `resources/public`.

## Development Mode

```
lein dev
```

## Building with a small clojure backend

```
lein clean
lein with-profile prod uberjar
```

Make sure you have a redis with redisearch running as explained
[here](https://oss.redislabs.com/redisearch/Quick_Start.html).

Then: 

```
java -jar target/wewe.jar  
```

