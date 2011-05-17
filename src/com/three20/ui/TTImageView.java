package com.three20.ui;

import com.three20.network.*;
import com.three20.network.response.*;
import com.three20.network.TTURLRequest.TTURLRequestDelegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TTImageView extends ImageView {

	private String 			mImageURL;
	private Bitmap 			mImage;
	private TTURLRequest	mRequest;
	private Handler			mHandler;
	
	private class TTURLRequestDelegateImpl extends TTURLRequestDelegate {
		@Override
		public void requestDidStartLoad(TTURLRequest request) {
		}
		
		@Override
		public void requestDidFinishLoad(TTURLRequest request) {
			TTURLImageResponse response = (TTURLImageResponse)request.getResponse();
			mImage = response.image;
			mRequest = null;
			mHandler.post(new Runnable(){
				public void run() {
					TTImageView.this.setImageBitmap(mImage);	
				}
			});
		}
	    
		@Override
		public void requestDidFailWithError(TTURLRequest request, Throwable error) {
			error.printStackTrace();
			mRequest = null;
		}

		@Override
		public void requestDidCancelLoad(TTURLRequest request) {
			mRequest = null;
		}
	}
	
	public TTImageView(Context context) {
		super(context);
		
		mImageURL = null;
		mImage = null;
		mHandler = new Handler();
	}
	
	public TTImageView(Context context, AttributeSet attrset){
		super(context, attrset);
		
		mImageURL = null;
		mImage = null;
		mHandler = new Handler();
	}
	
	private void stopLoading() {
		if (mRequest != null){
			mRequest.cancel();
		}
	}
	
	private void reload() {
		if (null == mRequest && null != mImageURL) {
			Bitmap image = TTURLCache.getSharedCache().imageForURL(mImageURL);
			
		    if (null != image ) {
		    	mImage = image;
		    	TTImageView.this.setImageBitmap(mImage);
		    } else {		    	
				TTURLRequest request = new TTURLRequest(mImageURL, new TTURLRequestDelegateImpl());
				request.setResponse(new TTURLImageResponse());
				if (!request.send()){
					
				}
		    }
		}
	}
	
	public void setImageURL(String imageURL) {
		if (null != mImage && null != imageURL && mImageURL == imageURL) {
		    return;
		}
		stopLoading();
		mImageURL = imageURL;
		if (null == mImageURL || 0 == mImageURL.length()) {
			return;
		} else {
		   reload();
		}
	}

	public String getImageURL() {
		return mImageURL;
	}
}
