package serve;

import java.util.ArrayList;

import profile.profiler;

import Common.FileF;
import Common.reporter;
import Common.dbConnection;


public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numthreads = 8; //default of 8
		if (args.length >= 1 ) {
		    try {
		    	numthreads = Integer.parseInt(args[0]);
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + args[0] + " must be an integer.");
		        System.exit(1);
		    }
		}
		
		
		Boolean live = true;
		reporter mReporter;
		
		String  FilePrefix = "restServer";
        
        String dirName = FilePrefix + "/";
        String fileName = dirName + "/" + "report.txt";

        if (FileF.Exists(fileName))
        {
            // remove
            FileF.Delete(fileName);
        }



        //create report file
        mReporter = new reporter(fileName);
        mReporter.setUsername("admin");
        mReporter.setType("restServer");
		
		ArrayList<Runnable> Tasks = new ArrayList<Runnable>();
        
		final profiler Profiler = new profiler();
		
		final dbConnection db_connection = new dbConnection(live,mReporter,Profiler);
		
		final Serve ServeInstance = new Serve(live,db_connection,mReporter);
		Tasks.add(ServeInstance);

		final WebServer webServer = new WebServer(live,numthreads,ServeInstance,db_connection,mReporter,Profiler);
		Tasks.add(webServer);
        
        ArrayList<Thread> threadpool = new ArrayList<Thread>();


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() 
            {    
                 System.out.println("Inside Add Shutdown Hook : " + Thread.currentThread().getName()) ;
                 
                 
            	 
            	//stop webServer
            	 webServer.ShutDown();
                	 
            	 while(!webServer.ShutDownFinished())
            	 {
            		 try {
     					Thread.sleep(1000);
     					System.out.print(".");
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
            	 }
            	 System.out.println("Finished Shutting down webServer");
            	 
            	//stop Search
            	 ServeInstance.ShutDown();
                	 
            	 while(!ServeInstance.ShutDownFinished())
            	 {
            		 try {
     					Thread.sleep(1000);
     					System.out.print(".");
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
            	 }
            	 System.out.println("Finished Shutting down Server");
            	 
            	//stop database
                 db_connection.ShutDown();
                	 
            	 while(!db_connection.ShutDownFinished())
            	 {
            		 try {
     					Thread.sleep(1000);
     					System.out.print(".");
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
            	 }
            	 System.out.println("Finished Shutting down database");
                 
            	System.out.println("Tasks exit");
             }
           });

        System.out.println("Added Shutting down hook");
        
        // create all threads
        for (Runnable task : Tasks)
        {
            Thread thread = new Thread(task);
            threadpool.add(thread);
            thread.start();
            

        }

        // join all threads
        for (Thread thread : threadpool)
        {
            try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        threadpool.clear();

	}

}
