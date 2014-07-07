sprayreactivedemo
=================

Goal is developing a demonstration of reactive capabilities of *spray.io* using the *spray-can* client API.

I try to write a little web crawler, which should parallely access many web servers.       
 
Start with the application *crawl.SequentialCrawlMain*. It tries to get web pages one after another from many URIs by awaiting the asynchronous execution for each web request.   
This application crawls about 60 URIs sequentially in about 60 seconds.
 
Then test the application *crawl.FutureCrawlMain*. It tries to get in parallel pages from many URIs in the web by using a Future for each web request.
This application crawls about 60 URIs in parallel in about 8 seconds.
 
Then test the application *crawl.ActorPerRequestCrawlMain*. It tries to get in parallel pages from many URIs in the web by using a new Actor for each web request.
This application crawls about 60 URIs in parallel in about 10 seconds.

Unfortunately the application does not scale as expected. If you compare the elapsed time of each web request in the parallel applications, 
you can see, that the Actors/Futures started later are lasting much longer than the Actors/Futures started earlier.
So the sum of the elapsed times of all web requests in the parallel scenario is about 210 seconds and much greater than the total elapsed time in the sequential scenario.

All runs were done directly in IntelliJ IDEA 13.1.3.

You can test the Future's performance outside IntelliJ by the following commands 

    sbt package
    java -jar target/scala-2.10/sprayreactivedemo-snapshot_2.10-1.0-one-jar.jar -f    

If you give instead of the last option -s the crawling will occur sequentially, if you give -a by Actors.

If you have problems using sbt, you can call ./ppsbt instead, which is more robust in relation to sbt versions.

The applications in package *crawl* are derived from the *spray-can* demo application from
    https://github.com/spray/spray/tree/master/examples/spray-can/simple-http-client/src/main/scala/spray/examples
The applications in package *demo* are direct clones of the indicated source.

Christoph Knabe, 2014-07-07
