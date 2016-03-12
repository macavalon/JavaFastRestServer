JavaFastRestServer
==================

A simple java rest server
Uses sun.com HttpServer
Messages are handled by ServerMessageHandler
- initially pushed to a ConcurrentLinkedQueue
- process by "ServeThreads" (number determined by constructor)
 * Has a connection MySQL
 * Performs initial query decode by type, query arguments
 * Supports path for /stats, /loader-io-key
 * setup for jsonp callback
 * write to report file for SLOW queries (> 1s)
- returned messages processed by "ReturnThreads
- well defined shutdown call, from shutdownhook


Example use
============================

See Program.java for recommended implementation

```
import serve.WebServer;
import Common.reporter;
import profile.Profiler;

profiler Profiler = new profiler();

String fileName = "report.txt";

if (FileF.Exists(fileName))
{
    // remove
    FileF.Delete(fileName);
}

//create report file
reporter mReporter = new reporter(fileName);
mReporter.setUsername("admin");
mReporter.setType("restServer");

Boolean live = true;

int numthreads = 8; 

dbConnection db_connection = new dbConnection(live,mReporter,Profiler);

Serve ServeInstance = new Serve(live,db_connection,mReporter);

WebServer webServer = new WebServer(live,numthreads,ServeInstance,db_connection,mReporter,Profiler);
                                        
webServer.run();
```


