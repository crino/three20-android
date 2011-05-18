package com.three20.network;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.impl.client.DefaultHttpClient;
import com.three20.ns.NSData;
import com.three20.ns.NSDate;

import android.graphics.Bitmap;
import android.util.Log;

public class TTURLRequestQueue {

	private static final String LOG_TAG = TTURLRequestQueue.class.getSimpleName();

	private static final long FUSHDELAY = 500;
	private static final int MAX_CONCURRENT_LOADS = 5;
	private static final String DEFAULT_AGENT = "Android device";

	private static TTURLRequestQueue mainQueueInstance = null;

	private HashMap<String, TTURLRequestLoader> mLoaders = new HashMap<String, TTURLRequestLoader>();
	private ArrayList<TTURLRequestLoader> mLoaderQueue = new ArrayList<TTURLRequestLoader>();
	private int mTotalLoading = 0;
	private String mUserAgent = DEFAULT_AGENT;
	private boolean mSuspended = false;

	private Timer mLoaderQueueTimer;

	public static TTURLRequestQueue mainQueue() {
		if (mainQueueInstance == null)
			mainQueueInstance = new TTURLRequestQueue();
		return mainQueueInstance;
	}

	private TTURLRequestQueue() {
	}

	/*
	 * public ~URLRequestQueue() { _loaderQueueTimer.cancel(); }
	 */
	@SuppressWarnings("unused")
	private void /* NSData */loadFromBundle(String url, Exception error) {
		// NSString* path = TTPathForBundleResource([URL substringFromIndex:9]);
		// NSFileManager* fm = [NSFileManager defaultManager];
		// if ([fm fileExistsAtPath:path]) {
		// return [NSData dataWithContentsOfFile:path];
		// } else if (error) {
		// *error = [NSError errorWithDomain:NSCocoaErrorDomain
		// code:NSFileReadNoSuchFileError userInfo:nil];
		// }
		// return null;
	}

	@SuppressWarnings("unused")
	private void /* NSData */loadFromDocuments(String url, Exception error) {
		/*
		 * - (NSData*)loadFromDocuments:(NSString*)URL error:(NSError**)error {
		 * NSString* path = TTPathForDocumentsResource([URL
		 * substringFromIndex:12]); NSFileManager* fm = [NSFileManager
		 * defaultManager]; if ([fm fileExistsAtPath:path]) { return [NSData
		 * dataWithContentsOfFile:path]; } else if (error) {error = [NSError
		 * errorWithDomain:NSCocoaErrorDomain code:NSFileReadNoSuchFileError
		 * userInfo:nil]; } return nil;
		 */
	}

	public boolean loadFromCache(String url, String cacheKey,
			long expirationAge, boolean fromDisk, NSData data, Exception error,
			NSDate timestamp) {

		// TTDASSERT(nil != data);

		Bitmap image = TTURLCache.getSharedCache().imageForURL(url, fromDisk);

		if (null != image) {
			// data = image;
			return true;
		} else if (fromDisk) {
			// if (TTIsBundleURL(URL)) {
			// *data = [self loadFromBundle:URL error:error];
			// return true;
			// } else if (TTIsDocumentsURL(URL)) {
			// *data = [self loadFromDocuments:URL error:error];
			// return true;
			// } else {
			// *data = [[TTURLCache sharedCache] dataForKey:cacheKey
			// expires:expirationAge
			// timestamp:timestamp];
			if (data != null) {
				NSData cachedData = TTURLCache.getSharedCache().dataForKey(cacheKey,
						expirationAge, timestamp);
				if (cachedData != null) {
					data.setBytes(cachedData.getBytes());
					return true;
				}
				return false;
			}
		}
		return false;
	}

	private boolean loadRequestFromCache(TTURLRequest request) {
		if (request.getCacheKey() == null) {
			request.setCacheKey(TTURLCache.getSharedCache().keyForURL(
					request.getUrlPath()));
		}

		if ((request.getCachePolicy() & (TTURLRequestCachePolicy.DISK | TTURLRequestCachePolicy.MEMORY)) != 0) {
			NSDate timestamp = new NSDate();
			Exception error = null;
			NSData data = new NSData();
			if (loadFromCache(
					request.getUrlPath(),
					request.getCacheKey(),
					request.getCacheExpirationAge(),
					(!mSuspended && (request.getCachePolicy() & TTURLRequestCachePolicy.DISK) != 0),
					data, error, timestamp)) {

				request.setLoading(false);

				if (error == null) {
					error = request.getResponse()
							.processResponse(data, request);
				}
				if (error != null) {
					for (ITTURLRequestDelegate delegate : request.delegates) {
						delegate.requestDidFailWithError(request, error);
					}
				} else {
					request.setTimestamp((timestamp != null ? timestamp
							: new NSDate()));

					request.setRespondedFromCache(true);

					for (ITTURLRequestDelegate delegate : request.delegates) {
						delegate.requestDidFinishLoad(request);
					}
				}
				return true;
			}
		}
		return false;
	}

	public void cancelRequest(TTURLRequest request) {
		if (request != null) {
			TTURLRequestLoader loader = mLoaders.get(request.getCacheKey());
			if (loader != null) {
				if (!loader.cancel(request)) {
					mLoaderQueue.remove(loader);
				}
			}
		}
	}

	public void cancelRequestsWithDelegate(ITTURLRequestDelegate delegate) {
		ArrayList<TTURLRequest> requestsToCancel = null;
	
		for (TTURLRequestLoader loader : mLoaders.values()) {
			for (TTURLRequest request : loader.getRequests() ) {
				for (ITTURLRequestDelegate requestDelegate : request.delegates) {
					if (delegate == requestDelegate) {
						if (requestsToCancel == null) {
							requestsToCancel = new ArrayList<TTURLRequest>();
						}
						requestsToCancel.add(request);
						break;
					}
				}
				
//				if (request.userInfo isKindOfClass:[TTUserInfo class]]) {
//		        	TTUserInfo* userInfo = request.userInfo;
//		        	if (userInfo.weakRef && userInfo.weakRef == delegate) {
//		        		if (!requestsToCancel) {
//		        			requestsToCancel = [NSMutableArray array];
//		        		}
//		        		[requestsToCancel addObject:request];
//		        	}
//		     	}
			}
		}

		for (TTURLRequest request : requestsToCancel) {
		    cancelRequest(request);
		}
	}

	public void cancelAllRequests() {
		for (TTURLRequestLoader loader : mLoaders.values()) {
			loader.cancel();
		}
	}

	private void removeLoader(TTURLRequestLoader loader) {
		mTotalLoading--;
		mLoaders.remove(loader.getCacheKey());
	}

	public void loadNextInQueueDelayed() {
		if (mLoaderQueueTimer == null) {
			TimerTask tt = new TimerTask() {
				public void run() {
					loadNextInQueue();
				}
			};
			mLoaderQueueTimer = new Timer();
			mLoaderQueueTimer.schedule(tt, FUSHDELAY);
		}
	}

	private void loadNextInQueue() {
		mLoaderQueueTimer = null;
		for (int i = 0; i < MAX_CONCURRENT_LOADS
				&& mTotalLoading < MAX_CONCURRENT_LOADS
				&& mLoaderQueue.size() > 0; i++) {
			TTURLRequestLoader loader = mLoaderQueue.get(0);
			executeLoader(loader);
			if (mLoaderQueue.size() > 0) {
				mLoaderQueue.remove(0);
			}
		}

		if (mLoaderQueue.size() > 0 && !getSuspended()) {
			loadNextInQueueDelayed();
		}
	}

	public void loaderDidLoadResponse(TTURLRequestLoader loader, NSData data) {

		removeLoader(loader);

		Exception error = loader.processResponse(data);
		if (error != null) {
			loader.dispatchError(error);
		} else {
			int i = (loader.getCachePolicy() & TTURLRequestCachePolicy.NOCACHE);
			if (i == 0) {
				TTURLCache.getSharedCache().storeDataKey(data,
						loader.getCacheKey());
			}
			loader.dispatchLoaded(new NSDate());
		}
		loadNextInQueue();
	}

	public void loaderDidFailLoadWithError(TTURLRequestLoader loader,
			Exception error) {
		Log.e(this.getClass().toString(), "ERROR: "
				+ error.getLocalizedMessage());
		removeLoader(loader);
		loader.dispatchError(error);
		loadNextInQueue();
	}

	public void loaderDidCancel(TTURLRequestLoader loader, boolean wasLoading) {
		if (wasLoading) {
			removeLoader(loader);
			loadNextInQueue();
		} else {
			mLoaders.remove(loader.getCacheKey());
		}
	}

	private void executeLoader(TTURLRequestLoader loader) {
		NSData data = new NSData();
		Exception error = null;
		NSDate timestamp = new NSDate();

		if ((loader.getCachePolicy() & (TTURLRequestCachePolicy.DISK | TTURLRequestCachePolicy.MEMORY)) > 0
				&& loadFromCache(
						loader.getUrlPath(),
						loader.getCacheKey(),
						loader.getCacheExpirationAge(),
						((loader.getCachePolicy() & TTURLRequestCachePolicy.DISK) > 0),
						data, error, timestamp)) {
			mLoaders.remove(loader.getCacheKey());

			if (error == null) {
				error = loader.processResponse(data);
			}
			if (error != null) {
				loader.dispatchError(error);
			} else {
				loader.dispatchLoaded(timestamp);
			}
		} else {
			++mTotalLoading;
			try {
				URI uri = new URI(loader.getUrlPath());
				loader.load(uri);
			} catch (URISyntaxException e) {
			}
		}
	}

	public boolean sendRequest(TTURLRequest request) {
		if (this.loadRequestFromCache(request)) {
			return true;
		}
		for (ITTURLRequestDelegate delegate : request.delegates) {
			delegate.requestDidStartLoad(request);
		}

		/*
		 * if (!request.urlPath.length) { NSError* error = [NSError
		 * errorWithDomain:NSURLErrorDomain code:NSURLErrorBadURL userInfo:nil];
		 * for (id<TTURLRequestDelegate> delegate in request.delegates) { if
		 * ([delegate
		 * respondsToSelector:@selector(request:didFailLoadWithError:)]) {
		 * [delegate request:request didFailLoadWithError:error]; } } return NO;
		 * }
		 */
		request.setLoading(true);

		TTURLRequestLoader loader = null;

		if (request.getHttpMethod() != TTURLRequest.HTTP_METHOD_POST
				&& request.getHttpMethod() != TTURLRequest.HTTP_METHOD_PUT) {
			if (mLoaders.containsKey(request.getCacheKey())) {
				loader = mLoaders.get(request.getCacheKey());
				loader.addRequest(request);
				return false;
			}
		}

		loader = new TTURLRequestLoader(request, this);
		mLoaders.put(request.getCacheKey(), loader);
		if (getSuspended() || mTotalLoading == MAX_CONCURRENT_LOADS) {
			mLoaderQueue.add(loader);
		} else {
			mTotalLoading++;
			try {
				URI uri = new URI(request.getUrlPath());
				loader.load(uri);
			} catch (URISyntaxException e) {
			}
		}
		return false;
	}

	public DefaultHttpClient createDefaultHttpClient(TTURLRequest request, URI uri) {
		if (uri == null) {
			try {
				uri = new URI(request.getUrlPath());
			} catch (URISyntaxException e) {
			}
		}	
		
		//httpClient.execute(new HttpGet(uri));
		

		/*
		 * NSMutableURLRequest* URLRequest = [NSMutableURLRequest
		 * requestWithURL:URL
		 * cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
		 * timeoutInterval:kTimeout];
		 * 
		 * if (self.userAgent) { [URLRequest setValue:self.userAgent
		 * forHTTPHeaderField:@"User-Agent"]; }
		 * 
		 * if (request) { [URLRequest
		 * setHTTPShouldHandleCookies:request.shouldHandleCookies];
		 * 
		 * NSString* method = request.httpMethod; if (method) { [URLRequest
		 * setHTTPMethod:method]; }
		 * 
		 * NSString* contentType = request.contentType; if (contentType) {
		 * [URLRequest setValue:contentType forHTTPHeaderField:@"Content-Type"];
		 * }
		 * 
		 * NSData* body = request.httpBody; if (body) { [URLRequest
		 * setHTTPBody:body]; }
		 * 
		 * NSDictionary* headers = request.headers; for (NSString *key in
		 * [headers keyEnumerator]) { [URLRequest setValue:[headers
		 * objectForKey:key] forHTTPHeaderField:key]; } }
		 * 
		 * return URLRequest;
		 */
		return null;
	}

	public void setSuspended(boolean isSuspended) {
		Log.i(LOG_TAG, "SUSPEND LOADING " + isSuspended);

		this.mSuspended = isSuspended;

		if (!mSuspended) {
			loadNextInQueue();
		} else if (mLoaderQueueTimer != null) {
			mLoaderQueueTimer.cancel();
			mLoaderQueueTimer = null;
		}
	}

	public boolean getSuspended() {
		return mSuspended;
	}

	public void setUserAgent(String _userAgent) {
		this.mUserAgent = _userAgent;
	}

	public String getUserAgent() {
		return mUserAgent;
	}
}
