### Simple NHS Conditions Search Engine

## Requirements

1. We would like you to write a service that scrape the nhsChoice website (http://www.nhs.uk/Conditions/Pages/hub.aspx) and cache the condition pages and their subpages content in a json file (contain at least url, page content and title).

2. As the second part, we need a rest enabled service that load this cache and provide endpoint to search them and point user to the most appropriate page for requests li

## PLEASE READ BEFORE PROCEEDING

Run on Linux/OSX os, because "nhs-cache" and "nhs-index" paths are hardcoded and unix based.
Also have to wait till scraping is over. Usually it takes 7-10 mins with localhost. (site has ddos protection) Can speedup if you have fast and reliable proxies! Searcher returns top 10 json results for the query!
If something doesn't work please do not hesitate to contact me!

## Prerequisites

1. Download & Install [Java] (http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
2. Download & Install [Maven] (https://maven.apache.org/install.html)

## Running

First you have to build and run "ScrapperAndIndexer" project to scrape nhs conditions website and index path.
Then to search through the data you have to build and run "Searcher" project

# ScrapperAndIndexer
Build:

1. ``` cd "ScrapperAndIndexer" ```
2. ``` mvn clean package ```

Run:

``` java -jar target/scrapperAndIndexer-jar-with-dependencies.jar ```

# Searcher
Build:

1. ``` cd "Searcher" ```
2. ``` mvn clean package ```

Run:

``` java -jar target/searcher-jar-with-dependencies.jar ```

## API Reference
Open browser and type in
http://localhost:8080/api/search/cancer
you can replace "cancer" with any search query



