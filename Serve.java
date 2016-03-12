package search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.io.StringReader;


import Common.FileF;
import Common.reporter;


import org.json.JSONArray;

public class Serve implements Runnable {

    public void InitUser()
    {
    	Boolean altport = false;
    }
    
    dbConnection db_connection;
    
    public Serve(Boolean _live,dbConnection _db_connection,reporter _mReporter)
    {
    	shutdownFinished = false;
    	mReporter = _mReporter;
    	FilePrefix = "searchServer";
    	
    	db_connection = _db_connection;
    	
    	shutdown = false;
    	
        Boolean debug =!_live;
        live = !debug;
 
        if(live)
        {
        	mReporter.report("LIVE MODE", true);
        	Site = "http://www.livesite.com/";  
        	
        }
        else
        {
        	mReporter.report("DEBUG MODE", true);
        	Site = "http://www.testsite.com/";
        }
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        InitUser();

    }

    
    
	Boolean shutdownFinished;
	
	public void ShutDown()
    {
		mReporter.report("stopping Server!",true);
		shutdown = true;
    
    }
	

    
    public Boolean ShutDownFinished()
    {
    	return shutdownFinished;
    }
    

    // the program essentially serves up json formatted content
    
    public void run()
    {

    	int round = 0;

    	while(true)	
	    {

	    	round++;
	    	mReporter.report("Round : " + round, true);

	    	mReporter.report("Serve READY", true);

    	
	    	int ms = 1000;
	    	int seconds = 1*ms;
	    	int min = 60*seconds;
	    	int hours = 60*min;
	    	
	    	int sleepTime = 10*seconds; 
	    	int roundTime = 4*hours;
	    	//roundTime = min;
	    	int sleeprounds = roundTime/sleepTime;
	    	
	    	for(int sleeploop =0; sleeploop < sleeprounds; sleeploop++)
	    	{
		    	
		    	
		    	 try {
		 			Thread.sleep(sleepTime);
		 		} catch (InterruptedException e) {
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 		}
		 		
		 		if(shutdown)
		 		{
		 			shutdownFinished = true;
		 			 mReporter.report(" Serve exit", true);
		 			break;
		 		}
	    	}
	    }
	    	
	    
	    	

    }
    

    reporter mReporter;
    String FilePrefix;
    Boolean live;

    String Site;

    Boolean shutdown;

}



