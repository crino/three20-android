package com.three20.network.response;

import com.three20.network.TTURLCache;
import com.three20.network.TTURLRequest;
import com.three20.ns.NSData;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TTURLImageResponse implements ITTURLResponse {

	public Bitmap image;

	public Exception processResponse(NSData data, TTURLRequest request) {
		try {
			if (data == null) {
				return new Exception("No data to process");
			} else {
				image = BitmapFactory.decodeByteArray(data.getBytes(), 0, data
						.length());
				if (image != null) {
					TTURLCache.getSharedCache().storeImage(image,
							request.getUrlPath());
				}
			}
		} catch (Exception ex) {
			return ex;
		}
		return null;
	}
}
