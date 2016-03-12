package serve;

import java.io.IOException;
import java.net.InetSocketAddress;

import profile.profiler;

import Common.reporter;
import Common.dbConnection;

import com.sun.net.httpserver.HttpServer;

public class WebServer implements Runnable {
	public WebServer(Boolean live, int numthreads,Serve ServeInstance, dbConnection db_connection,reporter _mReporter, profiler _Profiler)
	{
		shutdownFinished = false;
		mReporter = _mReporter;
        try {
        	if(live)
        	{
        		mReporter.report("LIVE Webserver running on port 8085", true);
        		server = HttpServer.create(new InetSocketAddress(8085), 0);
        	}
        	else
        	{
        		mReporter.report("DEBUG Webserver running on port 8084", true);
        		server = HttpServer.create(new InetSocketAddress(8084), 0);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        server.createContext("/", new ServeMessageHandler(numthreads,ServeInstance,db_connection,mReporter,_Profiler));
        server.setExecutor(null); // creates a default executor
        
    }
	
	
	public void run()
    {
		server.start();
		mReporter.report("Webserver running", true);
    }
	
	Boolean shuttingDown;
	Boolean shutdownFinished;
	
	public void ShutDown()
    {
    	mReporter.report("stopping WebServer!",true);
    	
    	try
    	{
    		server.stop(5);	//allow 5 seconds to stop
    		
    		mReporter.report("stopped WebServer!",true);
    		shutdownFinished = true;
    	}
    	catch (Exception ex)
    	{
    		mReporter.report("failed to stop WebServer!",true);
    	}
    	shuttingDown = true;
    	
    }
    
    public Boolean ShutDownFinished()
    {
    	return shutdownFinished;
    }
	
	HttpServer server;

	reporter mReporter;
}
