[![Build Status](https://travis-ci.org/escidoc-ng/escidoc-ng.svg?branch=master)](https://travis-ci.org/escidoc-ng/escidoc-ng)

# Larch repository

This project integrates ElasticSearch and WeedFs into a large scale distributed repository system modeled after Fedora 3

## Links
 * [Spring Boot](http://projects.spring.io/spring-boot/)
 * [Spring MVC](http://projects.spring.io/spring-framework/)
 * [ElasticSearch](http://www.elasticsearch.org/)
 * [WeedFs](https://code.google.com/p/weed-fs/)
 * [Facebook's Haystack paper](http://www.usenix.org/event/osdi10/tech/full_papers/Beaver.pdf)


## Build Larch

```
#> mvn clean package
```

## Run the application using the WeedFs adapter.
This will start a WeedFs master and volume server

```
#> java -jar larch-server/target/larch-server-VERSION.jar
```

## Run the application using the plain filesystem adapter

```
#> java -jar larch-server/target/larch-server-1.0-SNAPSHOT.jar --spring.profiles.active=fs
```

## Accessing the repository

Various endpoints are exposed via a REST API (see package `net.objecthunter.larch.controller` in `larch-server` for now).
A Dashboard view is exposed at the webserver root `http://localhost:8080/`

## Settings
Settings can be found in
 * `src/main/resources/application.properties`
 * `src/main/resources/application-fs.properties`
 * `src/main/resources/application-weedfs.properties`

All settings can be overwritten on the command line using spring-boot's property substitution e.g.:
The property `elasticsearch.http.enabled` can be passed on the commandline in the following way:
```
#> java -jar larch-server/target/larch-server-VERSION.jar --elasticsearch.http.enabled=false
```

## Developer information

 * Code style is copied from the [Fedora 4 project](https://wiki.duraspace.org/display/FF/Code+Style+Guide)
 * [Eclipse Formatter XML](https://github.com/fasseg/larch/tree/master/doc/developer/eclipse-larch-formatter.xml)
 * [IntelliJ Eclipse Formatter plugin](http://plugins.jetbrains.com/plugin/6546?pr=)
 * A Service interaction diagram can be opened using the [draw.io website](https://www.draw.io/) can be found in the `doc/developer` directory
 
