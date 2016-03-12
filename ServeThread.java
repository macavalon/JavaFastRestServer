package search;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import profile.profilepoint;
import profile.profiler;

import Common.reporter;

import com.sun.net.httpserver.HttpExchange;

public class ServeThread implements Runnable {

	ServeThread(int i, 
					ConcurrentLinkedQueue<HttpExchange> mq,
					ConcurrentLinkedQueue<ReturnMessage> rmq,
					Serve _ServeInstance,
					dbConnection _db_connection, 
					reporter _mReporter,
					profiler _Profiler)
	{
		db_connection = _db_connection;
		ServeInstance = _ServeInstance;
		//t = _t;
		queue = mq;
		returnqueue = rmq;
		mReporter = _mReporter;
		threadNumber = i;
		Profiler = _Profiler;
	}
	
	reporter mReporter;
	
	dbConnection db_connection;
	Serve ServeInstance;
	HttpExchange t;
	ConcurrentLinkedQueue<HttpExchange> queue;
	ConcurrentLinkedQueue<ReturnMessage> returnqueue;
	private volatile boolean running = true;
	
	profiler Profiler;
	
	int threadNumber;
	
	public HashMap<String, String> queryToMap(String query){
		
		HashMap<String, String> result = new HashMap<String, String>();
		if(query!=null)
		{
		    for (String param : query.split("&")) {
		        String pair[] = param.split("=");
		        if (pair.length>1) {
		            result.put(pair[0], pair[1]);
		        }else{
		            result.put(pair[0], "");
		        }
		    }
		}
	    return result;
	}
	
	public void terminate() {
        running = false;
    }
	
	public void run()
	{
		while (running) {
            try {
                
                synchronized (queue) {
                    queue.wait();
                }

                t = queue.poll();

                
                if (t != null) {
                	mReporter.report("Thread " + threadNumber + " Processing",true);
                	mReporter.report("Queue size : " + queue.size(),true);
                	processMessage();
                }
                
            } catch (InterruptedException e) {
            	mReporter.report("Thread " + threadNumber + " Stopping...",true);
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

		String httpmethod = t.getRequestMethod();
		
		if(httpmethod.compareTo("GET")!=0)
		{
			return;
		}
		
		
		String query = t.getRequestURI().getQuery();
		String path = t.getRequestURI().getPath();
		
		HashMap<String, String> queryMap = queryToMap(query);
	
		String requestType = "";
		String searchTerm = "";
		String callback = "";
		int start = 0;
		
		Boolean returnResult = false;
		
		String response = "Unknown request";
		
        String loaderIoKey = "loaderio-nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn";
        
		if(	query==null 
		   && path.equals("/"+loaderIoKey+".txt"))
		{
            //maybe you're using loaderio for testing
            // and need the endpoint to retrurn
        
			response = loaderIoKey;
		
			ReturnMessage rm = new ReturnMessage(t,response);
			returnqueue.add(rm);
			synchronized (returnqueue) {
				returnqueue.notifyAll();
	        }
			
			mReporter.report("loader io return",true);
			
		}
		else if(	query==null 
				   && path.equals("/stats"))
				{
                    //maybe you've implemented statistics
                    // which you want retrieved from http://host/stats
                    
					/*response = someclass.reportStats();
				
					ReturnMessage rm = new ReturnMessage(t,response);
					returnqueue.add(rm);
					synchronized (returnqueue) {
						returnqueue.notifyAll();
			        }
					
					mReporter.report(response,true);*/
					
				}
		else
		{
			
			
			//get the query type
			if(queryMap.containsKey("type"))
			{
				requestType = queryMap.get("type");
			}
			
			
			//need to pull out all arguments...as required for each type of request
            // EXAMPLE arguments
			//get the accName
			String accName = "";
			if(queryMap.containsKey("accName"))
			{
				accName = queryMap.get("accName");
			}
			
			
			//get the message_id
			String message_id = "";
			if(queryMap.containsKey("message_id"))
			{
				message_id = queryMap.get("message_id");
			}
			
			//get the jsonp callback
			if(queryMap.containsKey("callback"))
			{
				callback = queryMap.get("callback");
			}
			
			//need to rate limit the requests
			InetSocketAddress recipient = t.getRemoteAddress();
			
			mReporter.report(recipient.getHostString() + " : " + callback + " processing ", true);
			
			
			ArrayList<String> results = new ArrayList<String> ();
			
			
			JSONArray resultArray = new JSONArray();
			
			if(requestType.compareTo("queryGetMessagesById")==0)
			{

                // get the messages from the db
                ArrayList<String> msgIdList = new ArrayList<String>();
                if(db_connection.queryGetMessagesById(msgIdList,resultArray))
                {
                    response = callback + "(" + resultArray.toString() + ")";
                    returnResult = true;
                }

			}
			
			else
			{
				mReporter.report(recipient.getHostString() + " unspported request: " +requestType ,true);
			}
		
		
			if(returnResult)
			{
				
	 
			}
			else
			{
				mReporter.report(recipient.getHostString() + " : "+ callback + " bad query " + query,true);
			}
			
			
			ReturnMessage rm = new ReturnMessage(t,response);
			returnqueue.add(rm);
			synchronized (returnqueue) {
				returnqueue.notifyAll();
	        }
			
	        resultArray = null;
	        recipient = null;
			results = null;

		}
		
        
		queryMap = null;

        
        long totalTime = System.nanoTime();
        long totalTimeTaken = 	totalTime - startTime;

        double totalTimeTakenSecs = totalTimeTaken/n;
        if(totalTimeTakenSecs> 1)
        {
        	mReporter.report(" SLOW query took " + totalTimeTakenSecs + "s : " + query,true);
        }

        _profilepoint.measure();
	}
}

