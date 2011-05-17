package com.three20.network;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;

import android.graphics.Bitmap;
import android.util.Log;

import com.three20.ns.NSDate;
import com.three20.network.response.ITTURLResponse;
import com.three20.util.MD5;

public class TTURLRequest {
	private static final String LOG_TAG = TTURLRequest.class.getSimpleName();

	private static final String kStringBoundary = "3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f";

	public static final int HTTP_METHOD_GET = 0;
	public static final int HTTP_METHOD_POST = 1;
	public static final int HTTP_METHOD_PUT = 2;
	public static final int HTTP_METHOD_DELETE = 3;
	public static final int HTTP_METHOD_BITMAP = 4;

	protected boolean mIsLoading = false;
	protected String mCacheKey = null;

	protected int mHttpMethod = HTTP_METHOD_GET;
	private String mHttpBody = null;
	private String mContentType = null;
	protected int mCachePolicy = TTURLRequestCachePolicy.DEFAULT;
	protected long mCacheExpirationAge = TTURLCache.TT_DEFAULT_CACHE_EXPIRATION_AGE;
	protected boolean mRespondedFromCache = false;
	protected String mUrlPath = null;
	protected NSDate mTimestamp;
	protected ITTURLResponse mResponse;

	private Object mUserInfo = null;
	
	protected ArrayList<Header> mHeaders = new ArrayList<Header>();

	public HashMap<String, Object> parameters = new HashMap<String, Object>();
	public ArrayList<TTURLRequestDelegate> delegates = new ArrayList<TTURLRequestDelegate>();

	public TTURLRequest(String url, TTURLRequestDelegate delegate) {
		mUrlPath = url;
		if (delegate != null) {
			this.delegates.add(delegate);
		}
	}

	public String toString() {
		return "<TTURLRequest " + mUrlPath + ">";
	}

	/**
	 * @param respondedFromCache
	 *            the respondedFromCache to set
	 */
	public void setRespondedFromCache(boolean respondedFromCache) {
		this.mRespondedFromCache = respondedFromCache;
	}

	/**
	 * @return the respondedFromCache
	 */
	public boolean isRespondedFromCache() {
		return mRespondedFromCache;
	}

	/**
	 * @param httpMethod
	 *            the httpMethod to set
	 */
	public void setHttpMethod(int httpMethod) {
		this.mHttpMethod = httpMethod;
	}

	/**
	 * @param cachePolicy
	 *            the cachePolicy to set
	 */
	public void setCachePolicy(int cachePolicy) {
		this.mCachePolicy = cachePolicy;
	}

	/**
	 * @param cacheExpirationAge
	 *            the cacheExpirationAge to set
	 */
	public void setCacheExpirationAge(long cacheExpirationAge) {
		this.mCacheExpirationAge = cacheExpirationAge;
	}

	/**
	 * @return the cacheExpirationAge
	 */
	public long getCacheExpirationAge() {
		return mCacheExpirationAge;
	}

	/**
	 * @return the cachePolicy
	 */
	public int getCachePolicy() {
		return mCachePolicy;
	}

	/**
	 * @return the httpMethod
	 */
	public int getHttpMethod() {
		return mHttpMethod;
	}

	public void setHttpBody(String mHttpBody) {
		this.mHttpBody = mHttpBody;
	}

	public String getHttpBody() {
		if (mHttpBody != null) {
			return mHttpBody;
		} else if (getHttpMethod() == TTURLRequest.HTTP_METHOD_POST
				|| getHttpMethod() == TTURLRequest.HTTP_METHOD_PUT) {
			return generatePostBody();
		} else {
			return null;
		}
	}
	
	public void setContentType(String mContentType) {
		this.mContentType = mContentType;
	}
	
	public String getContentType() {
		if (mContentType != null) {
			return mContentType;
		} else if (getHttpMethod() == TTURLRequest.HTTP_METHOD_POST
				|| getHttpMethod() == TTURLRequest.HTTP_METHOD_PUT) {
			return "multipart/form-data; boundary=" + kStringBoundary;
		} else {
			return null;
		}
	}

	/**
	 * @param urlPath
	 *            the urlPath to set
	 */
	public void setUrlPath(String urlPath) {
		this.mUrlPath = urlPath;
	}

	/**
	 * @return the urlPath
	 */
	public String getUrlPath() {
		return mUrlPath;
	}

	public void setTimestamp(NSDate timestamp) {
		this.mTimestamp = timestamp;
	}

	public NSDate getTimestamp() {
		return mTimestamp;
	}

	public void setResponse(ITTURLResponse response) {
		this.mResponse = response;
	}

	public ITTURLResponse getResponse() {
		return mResponse;
	}

	public ArrayList<Header> getHeaders() {
		return mHeaders;
	}

	public void setUserInfo(Object mUserInfo) {
		this.mUserInfo = mUserInfo;
	}

	public Object getUserInfo() {
		return mUserInfo;
	}

	private String generateCacheKey() {
		if (getHttpMethod() == TTURLRequest.HTTP_METHOD_POST
				|| getHttpMethod() == TTURLRequest.HTTP_METHOD_PUT) {
			StringBuilder joined = new StringBuilder();
			joined.append(mUrlPath);
			for (String key : parameters.keySet()) {
				joined.append(key);
				joined.append("=");
				Object value = parameters.get(key);
				if (value instanceof String) {
					joined.append(String.valueOf(value));
				}
			}
			return MD5.generateMD5(joined.toString());
		} else {
			return MD5.generateMD5(mUrlPath);
		}
	}

	private String generatePostBody() {
		StringBuilder body = new StringBuilder();
		// TODO generatePostBody(): VERIFY
		for (String key : parameters.keySet()) {
			Object value = parameters.get(key);
			if (!(value instanceof Bitmap)) {
				body.append("--" + kStringBoundary + "\r\n");
				body.append("Content-Disposition: form-data; name=\"" + key
						+ "\"\r\n\r\n");
				body.append(String.valueOf(value));
				body.append("\r\n");
			}
		}
		// TODO generatePostBody(): add images to 
		body.append("--" + kStringBoundary + "--\r\n");
		Log.d(LOG_TAG, "Sending: " + body.toString());
		return body.toString();
	}

	/** Sends the request */
	public boolean send() {
		return TTURLRequestQueue.mainQueue().sendRequest(this);
	}

	/** Sets the request as loading */
	public void setLoading(boolean isLoading) {
		this.mIsLoading = isLoading;
	}

	/**  */
	public boolean isLoading() {
		return mIsLoading;
	}

	public void setCacheKey(String cacheKey) {
		this.mCacheKey = cacheKey;
	}

	public String getCacheKey() {
		if (mCacheKey == null || mCacheKey.length() == 0) {
			mCacheKey = generateCacheKey();
		}
		return mCacheKey;
	}

	/** Cancels the request */
	public void cancel() {
		TTURLRequestQueue.mainQueue().cancelRequest(this);
	}

	public static abstract class TTURLRequestDelegate implements
			ITTURLRequestDelegate {
		/**
		 * The request has begun loading.
		 * 
		 * This method will not be called if the data is loaded immediately from
		 * the cache.
		 * 
		 * @see requestDidFinishLoad:
		 */
		public void requestDidStartLoad(TTURLRequest request) {
		}

		/**
		 * The request has loaded data and been processed into a response.
		 * 
		 * If the request is served from the cache, this is the only delegate
		 * method that will be called.
		 */
		public void requestDidFinishLoad(TTURLRequest request) {
		}

		/**
		 * Called when an error prevents the request from completing
		 * successfully.
		 */
		public void requestDidFailWithError(TTURLRequest request,
				Throwable error) {
		}

		/**
		 * Called when the request was canceled.
		 */
		public void requestDidCancelLoad(TTURLRequest request) {
		}
	}
}
