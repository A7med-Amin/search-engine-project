# Search_Engine_Project
![web_interface](https://github.com/A7med-Amin/search-engine-project/assets/80707696/41deb998-c2df-4f79-a7a0-7c3f085a6af8)


## Team_Members
| Name    |
| ------  |
| Ahmed Amin  |
| Marwan Mostafa  |
| Yasmine Hashem |
| Anas Sherif|

## Project_Description
Based search engine that demonstrates the main features of a search engine (web crawling, indexing, ranking) and interaction between them.
project is mainly developed in java with react.js for web interface and spring boot.

## Web crawler
The crawler starts with a list of URL addresses (seed set). It downloads the documents identified by these URLs and extracts hyper-links from them. The extracted URLs are added to the list of URLs to be downloaded. Thus, web crawling is a recursive process.

## Indexer
The output of web crawling process is a set of downloaded HTML documents. To respond to user queries fast enough, the contents of these documents have to be indexed in a mongoDB cluster that stores the words contained in each document and their importance (e.g., whether they are in the title, in a header or in plain text).

## Query processor
This module receives search queries, performs necessary preprocessing and searches the index for relevant documents. Retrieve documents containing words that share the same stem with those in the search query. For example, the search query “travel” should match (with lower degree) the words “traveler”, “traveling” … etc.

## Phrase searching
Search engines will generally search for words as phrases when quotation marks are placed around the phrase. Results obtained when searching for a sentence with quotation marks around them should be a subset of the results obtained when searching for the same sentence without the quotation marks.

## Ranker
### A) Relevance
Relevance is a relation between the query words and the result page and calculated with tf-idf of the query word in the result page.

### B) Popularity
Popularity is a measure for the importance of any web page regardless the requested query.

## Web interface
### A) Front-end
Application is implemented based on react.js. It displays page links related to the searched query with page title, paragraph that the searched query appear in it where the searched words is in bold form.

### B) API
Spring boot is used to link the java code with user interface web application and mongoDB using apis.



