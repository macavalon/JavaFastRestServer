package serve;

import com.sun.net.httpserver.HttpExchange;

public class ReturnMessage {

	ReturnMessage(HttpExchange _t, String _response)
	{
		t = _t;
		response = _response;
	}
	
	public HttpExchange t;
	public String response;
}
