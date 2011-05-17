package com.three20.network.response;


import org.json.JSONObject;

import com.three20.network.TTURLRequest;
import com.three20.ns.NSData;

public class TTURLJSONResponse implements ITTURLResponse {
	
	public JSONObject jsonObj;
	
	public Exception processResponse(NSData data, TTURLRequest request) {
    	try {   	    	
	    	if (data == null) {
	    		return new Exception("No response");
	    	}
	    	else {
	    		String json = new String(data.getBytes());
	    		jsonObj = new JSONObject(json);
	    	}
		}
		catch (Exception ex) {
			return ex;
		}
		return null;
	}
}