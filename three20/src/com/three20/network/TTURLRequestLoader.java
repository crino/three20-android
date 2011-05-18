package com.three20.network;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.three20.ns.NSData;
import com.three20.ns.NSDate;


import android.util.Log;

public class TTURLRequestLoader {
	
	private static final String LOG_TAG = TTURLRequestLoader.class.getSimpleName();
	
	private static final int LOAD_MAX_RETRIES = 2;

	private ArrayList<TTURLRequest> mRequests = new ArrayList<TTURLRequest>();
	private String mCacheKey = null;
	private String mUrlPath = null;
	private int mCachePolicy = TTURLRequestCachePolicy.DEFAULT;
	private long mCacheExpirationAge = 0;
	
	private int mRetriesLeft = LOAD_MAX_RETRIES;
	private TTURLRequestQueue mQueue;
	private Thread mThreadConnection;
	
	public TTURLRequestLoader(TTURLRequest request, TTURLRequestQueue queue) {
		mUrlPath = request.getUrlPath();
		mQueue = queue;
		mCacheKey = request.getCacheKey();
		mCachePolicy = request.getCachePolicy();
		mCacheExpirationAge = request.getCacheExpirationAge();
		mThreadConnection = new Thread();

		addRequest(request);
	}
	
	public void setUrlPath(String urlPath) {
		this.mUrlPath = urlPath;
	}

	public String getUrlPath() {
		return mUrlPath;
	}

	public void setCacheKey(String cacheKey) {
		this.mCacheKey = cacheKey;
	}

	public String getCacheKey() {
		return mCacheKey;
	}
	
	public void setCachePolicy(int cachePolicy) {
		this.mCachePolicy = cachePolicy;
	}

	public int getCachePolicy() {
		return mCachePolicy;
	}

	public void setCacheExpirationAge(long cacheExpirationAge) {
		mCacheExpirationAge = cacheExpirationAge;
	}

	public long getCacheExpirationAge() {
		return mCacheExpirationAge;
	}

	public void setRequests(ArrayList<TTURLRequest> mRequests) {
		this.mRequests = mRequests;
	}

	public ArrayList<TTURLRequest> getRequests() {
		return mRequests;
	}

	private void connectToURL(final URI uri) {
		
		TTGlobalNetwork.TTNetworkRequestStarted();
		
		Log.i(LOG_TAG, "Connecting to: " + uri.toString());
		
		final TTURLRequest request = mRequests.size() == 1 ? mRequests.get(0)
				: null;

		//NSURLRequest* URLRequest = [_queue createNSURLRequest:request URL:URL];
		  
		Runnable connection = new Runnable() {
			public void run() {
				//sets up parameters 
		        HttpParams params = new BasicHttpParams(); 
		        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
		        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 
		        HttpProtocolParams.setUserAgent(params, mQueue.getUserAgent());
		        HttpProtocolParams.setUseExpectContinue(params, false);
		        //params.setParameter("http.socket.timeout", new Integer(1000));
		      
		        //registers schemes for both http and https 
		        SchemeRegistry registry = new SchemeRegistry(); 
		        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
		        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory(); 
		        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 
		        registry.register(new Scheme("https", sslSocketFactory, 443)); 

		        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry); 		
				DefaultHttpClient httpClient = new DefaultHttpClient(manager, params);
				try {
					HttpResponse response = null;
					switch (request.getHttpMethod()) {
					case TTURLRequest.HTTP_METHOD_GET:
						HttpGet httpGet = new HttpGet(uri);
						for (Header header : request.getHeaders()) {
							httpGet.addHeader(header);
						}
						response = httpClient.execute(httpGet);
						break;
					case TTURLRequest.HTTP_METHOD_POST:
						HttpPost httpPost = new HttpPost(uri);
						for (Header header : request.getHeaders()) {
							httpPost.addHeader(header);
						}
						String contentType = request.getContentType();
						if (contentType != null) {
							httpPost.addHeader("Content-Type", contentType);
						}
						String body = request.getHttpBody();
						if (body != null) {
							httpPost.setEntity(new StringEntity(body));
						}
						response = httpClient.execute(httpPost);
						break;
					case TTURLRequest.HTTP_METHOD_PUT:
						// HttpPut httpPut = new HttpPut(uri);
						// httpPut.setEntity(new StringEntity(data));
						// response = httpClient.execute(httpPut);
						break;
					case TTURLRequest.HTTP_METHOD_DELETE:
						// response = httpClient.execute(new HttpDelete(uri));
						break;
					}
					
					TTGlobalNetwork.TTNetworkRequestStopped();
					
					if (response.getStatusLine().getStatusCode() == 200 ) {
						HttpEntity entity = response.getEntity();
						NSData data = new NSData( EntityUtils.toByteArray(entity) );
						mQueue.loaderDidLoadResponse(TTURLRequestLoader.this, data);
					} else {
						HttpEntity entity = response.getEntity();
						String data = new String( EntityUtils.toByteArray(entity) );
						Log.e(LOG_TAG, "Error - response: " + data);
						Exception error = new Exception("URLError code:" + response.getStatusLine().getStatusCode());
						mQueue.loaderDidFailLoadWithError(TTURLRequestLoader.this, error);
					}
				} catch (ClientProtocolException cpe) {
					if (mRetriesLeft != 0){
						--mRetriesLeft;
						try {
							URI uri = new URI(getUrlPath());
							load(uri);
						} catch (URISyntaxException e) {
						}
					}
				} catch (Exception e) {
					TTGlobalNetwork.TTNetworkRequestStopped();
					dispatchError(e);
				}
			}
		};
		mThreadConnection = new Thread(connection);
		mThreadConnection.start();
	}

	public void removeRequest(TTURLRequest request) {
		mRequests.remove(request);
	}

	public void addRequest(TTURLRequest request) {
		/*
		 * TTDASSERT([_urlPath isEqualToString:request.urlPath]);
		 * TTDASSERT(_cacheKey == request.cacheKey); TTDASSERT(_cachePolicy ==
		 * request.cachePolicy); TTDASSERT(_cacheExpirationAge ==
		 * request.cacheExpirationAge);
		 */
		mRequests.add(request);
	}

	public void load(URI uri) {
		if (!mThreadConnection.isAlive()) {
			connectToURL(uri);
		}
	}

	public boolean cancel(TTURLRequest request) {
		int index = mRequests.indexOf(request);
		if (index == -1) {
			request.setLoading(false);

			for (ITTURLRequestDelegate delegate : request.delegates) {
				delegate.requestDidCancelLoad(request);
			}
			mRequests.remove(index);
		}
		if (mRequests.size() == 0) {
			mQueue.loaderDidCancel(this, request.isLoading());
			TTGlobalNetwork.TTNetworkRequestStopped();
			return false;
		} else {
			return true;
		}
	}

	public Exception processResponse(NSData data) {
		for (TTURLRequest request : mRequests) {
			if (request.getResponse() != null){
				Exception error = request.getResponse().processResponse(data, request);
				if (error != null) {
					return error;
				}
			}
		}
		return null;
	}

	public void dispatchError(Exception error) {
		for (TTURLRequest request : mRequests) {
			request.setLoading(false);

			for (ITTURLRequestDelegate delegate : request.delegates) {
				delegate.requestDidFailWithError(request, error);
			}
		}
	}

	public void dispatchLoaded(NSDate timestamp) {
		for (TTURLRequest request : mRequests) {
			request.setTimestamp(timestamp);
			request.setLoading(false);

			for (ITTURLRequestDelegate delegate : request.delegates) {
				delegate.requestDidFinishLoad(request);
			}
		}
	}

	public void cancel() {
		for (TTURLRequest request : mRequests) {
			cancel(request);
		}
	}
}
