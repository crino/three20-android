package com.three20.network.response;

import com.three20.ns.NSData;
import com.three20.network.TTURLRequest;

public interface ITTURLResponse {

	public Exception processResponse(NSData data, TTURLRequest request);

}
