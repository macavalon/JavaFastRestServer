package serve;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import profile.profiler;

import Common.reporter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServeMessageHandler implements HttpHandler
{
	ServeMessageHandler(int numthreads,
						 Serve _searchInstance,
						 dbConnection _db_connection,
						 reporter _mReporter,
						 profiler _Profiler)
	{
		db_connection = _db_connection;
		searchInstance = _searchInstance;  
		threadpool = new ArrayList<Thread>();
		mReporter = _mReporter;
		
		//message queue
		queue = new ConcurrentLinkedQueue<HttpExchange>();
		
		
		//return message queue
		returnqueue = new ConcurrentLinkedQueue<ReturnMessage>();
		
		//create the set of search threads that will pick from queue
		numOfThreads = numthreads;
		mReporter.report("Starting www engine with threads : " + numOfThreads,true);
		for(int i=0 ; i < numOfThreads; i++)
		{
			Runnable rServeThread = new ServeThread(i,queue,returnqueue,searchInstance,db_connection,mReporter,_Profiler);
			
			Thread runAutoThread = new Thread(rServeThread);	

			threadpool.add(runAutoThread);
			runAutoThread.start();
		}
		
		//create the set of search threads that will return responses
		numOfThreads = numthreads;
		mReporter.report("Starting www engine with return response threads : " + numOfThreads,true);
		for(int i=0 ; i < numOfThreads; i++)
		{
			Runnable rReturnThread = new ReturnThread(i,returnqueue,searchInstance,db_connection,mReporter,_Profiler);
			
			Thread runAutoThread = new Thread(rReturnThread);	

			threadpool.add(runAutoThread);
			runAutoThread.start();
		}
	}
	
	reporter mReporter;
	
	ArrayList<Thread> threadpool;
	
	//howto be multithreaded and handle multiple requests..
	
	@Override
    public void handle(HttpExchange t) throws IOException {
		
		queue.add(t); //add message into queue
		
		synchronized (queue) {
			queue.notifyAll();
        }
    }
	
	Serve searchInstance;
	dbConnection db_connection;
	ConcurrentLinkedQueue<HttpExchange> queue;
	ConcurrentLinkedQueue<ReturnMessage> returnqueue;
	int numOfThreads;
}
