package serve;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;

import profile.profilepoint;
import profile.profiler;
import Common.reporter;

import com.sun.net.httpserver.HttpExchange;

public class ReturnThread implements Runnable {

	ReturnThread(int i, ConcurrentLinkedQueue<ReturnMessage> mq,
					Serve _ServeInstance,
					dbConnection _db_connection, 
					reporter _mReporter,
					profiler _Profiler)
	{
		db_connection = _db_connection;
		ServeInstance = _ServeInstance;
		returnqueue = mq;
		mReporter = _mReporter;
		threadNumber = i;
		Profiler = _Profiler;
	}
	
	reporter mReporter;
	
	dbConnection db_connection;
	Serve ServeInstance;
	ReturnMessage rm;
	ConcurrentLinkedQueue<ReturnMessage> returnqueue;
	private volatile boolean running = true;
	
	profiler Profiler;
	
	int threadNumber;
	
	
	
	public void terminate() {
        running = false;
    }
	
	public void run()
	{
		while (running) {
            try {
                
                synchronized (returnqueue) {
                	returnqueue.wait();
                }

                rm = returnqueue.poll();

                
                if (rm != null) {
                	mReporter.report("Return Thread " + threadNumber + " Processing",true);
                	mReporter.report("ReturnQueue size : " + returnqueue.size(),true);
                	processMessage();
                }
                
            } catch (InterruptedException e) {
            	mReporter.report("ReturnThread " + threadNumber + " Stopping...",true);
                running = false;
            }
        }
	}
	
	public void processMessage()
	{
		profilepoint _profilepoint = new profilepoint(Profiler,"processMessage");
		long startTime = System.nanoTime();
		//long sendTime = 0;
		long m = 1000;
	    long u = m*1000;
	    long n = u*1000;

		try {
			rm.t.sendResponseHeaders(200, rm.response.getBytes().length);
			
	        OutputStream os = rm.t.getResponseBody();
	        os.write(rm.response.getBytes());
	        os.close();
	        
	        mReporter.report(rm.t.getRemoteAddress().getHostString() + " : " + " completed ",true);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        rm.t = null;
        rm.response = null;
		rm = null;
		

        
        long totalTime = System.nanoTime();
        long totalTimeTaken = 	totalTime - startTime;

        double totalTimeTakenSecs = totalTimeTaken/n;
        if(totalTimeTakenSecs> 1)
        {
        	mReporter.report(" SLOW return took " + totalTimeTakenSecs + "s : ",true);
        }

        _profilepoint.measure();
	}
}

