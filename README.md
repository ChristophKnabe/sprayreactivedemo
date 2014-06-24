sprayreactivedemo
=================

Goal is developing a demonstration of reactive capabilities of *spray.io* using the *spray-can* client API.

I try to write a little web crawler, which should parallely access many web servers.
 
The start is the application *crawl.FutureCrawlMain*. It tries to get parallely pages from several URIs in the web.

When trying only with 2 URIs it usually succeeds well.
But it really does not scale as expected.
When trying with 3 URIs, some of them fail.
When trying with 4 or more URIs usually all of them fail.

When trying sequentially by application *crawl.SequentialCrawlMain*, all runs well.

The applications in package *crawl* are derived from the *spray-can* demo application from
    https://github.com/spray/spray/tree/master/examples/spray-can/simple-http-client/src/main/scala/spray/examples

The applications in package *demo* are direct clones of the given source.

CK 2014-06-24
