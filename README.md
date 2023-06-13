# Demo virtual threads

Based on [Virtual threads in Spring Boot with Java 19](https://medium.com/@egorponomarev/virtual-threads-in-spring-boot-with-java-19-ea98e1725058).
This blog post demonstrates the difference between using traditional threads and virtual threads (a Java 19+ feature).
It demonstrates this by issuing 1000 concurrent requests with the Apache benchmark tool (ab).
Since Tomcat is configured with max 200 threads this effectively causes "batches" of 200 requests to be processed.

The first problem I ran into when trying to replicate this demo was the following error:

    Benchmarking localhost (be patient)
    socket: Too many open files (24)

This was easily solved with some Googling and this [Stack Overflow post](https://stackoverflow.com/a/1504603/17542):

    ulimit -n 10000

The next problem was a bit more tricky:

    Benchmarking localhost (be patient)
    apr_socket_recv: Connection reset by peer (54)
    Total of 1 requests completed

Instead of figuring this out I decided to just scale down the demo by a factor of ten.
So instead of running a 1000 concurrent requests on a 200 thread pool,
I'll run 100 concurrent requests on a 20 thread pool.
This is set in `application.properties`:

    server.tomcat.threads.max=20

To run the application with virtual threads `--enable-preview` must be passed as an argument to the java compiler,
as well as the Java command.
I've configured this in `pom.xml` for the `maven-compiler-plugin` and `spring-boot` plugin,
so virtual threads are always available.
To actually take advantage of them Tomcat must be customized like this in `Demo1Application`:

```java
    @Bean
    @Profile("virtual-threads")
    public TomcatProtocolHandlerCustomizer<?> virtualThreads() {
        return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
```

I've added `@Profile` here so usage of virtual threads can be toggled via a command line parameter - 
simply pass `-Dspring-boot.run.profiles=virtual-threads` to `mvn spring-boot:run` to enable them.

Running `ab -n 100 -c 1000 http://localhost:8080/hello` yields the following results (Time taken for tests):

Without virtual threads: 6.161 seconds  
With virtual threads: 2.163 seconds

Clearly virtual threads is the winner in this case.

What if we increase the `server.tomcat.threads.max` to 200 and run the same tests?
There is practically no difference. Virtual threads are great for **scalability** - when you have saturated the thread pool, 
before that it makes little difference.

