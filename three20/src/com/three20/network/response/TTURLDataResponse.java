package com.three20.network.response;

import com.three20.ns.NSData;
import com.three20.network.TTURLRequest;

public class TTURLDataResponse implements ITTURLResponse {

	public NSData data;

	public Exception processResponse(NSData data, TTURLRequest request) {
		try {
			if (data == null) {
				return new Exception("No response");
			} else {
				this.data = data;
			}
		} catch (Exception ex) {
			return ex;
		}
		return null;
	}
}
