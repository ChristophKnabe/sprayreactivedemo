Parallel Reactive Client Scaling Demo with Spray
===============================
                                           Christoph Knabe, 2014-11-13

I want to create a demonstration for the parallel scalability
of Reactive Programming for my students and other interested people.

The goals are:

1. It should demonstrate massive parallelization, which could not be achieved in a threaded manner.
2. It should be so simple, that it is understandable for students. I hope to rest under 200 LoC for the functionality.
3. I think it should prefer actors over futures or RxScala streams, as they are the most basic mean of parallelization in Scala.

As my students do not dispose of a server, and cannot produce massive amounts of parallel requests, I think of a client application. My plan is to write a web crawler, which tries to get hold of as many different web addresses as possible and logs them with their link distance from the start page.

I made a prototype retrieving parallely 60 enumerated web pages using the spray-can client API.
In the Hackaton I want to implement parsing of the web pages in order to extract further links and crawl them recursively.
Also the duplicate-avoiding logic needs to be implemented.

Ideally in the end we could compare the results with a multithreaded Java web crawler.

Prototype: https://github.com/ChristophKnabe/sprayreactivedemo
Background: There is an interesting article of Yevgeniy Brikman explaining the advantages of event-based parallelization. See http://engineering.linkedin.com/play/play-framework-async-io-without-thread-pool-and-callback-hell
