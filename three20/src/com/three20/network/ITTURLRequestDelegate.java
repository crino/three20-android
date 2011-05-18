package com.three20.network;

public interface ITTURLRequestDelegate {
	/**
	 * The request has begun loading.
	 *
	 * This method will not be called if the data is loaded immediately from the cache.
	 * @see requestDidFinishLoad:
	 */
    public void requestDidStartLoad(TTURLRequest request);

    /**
     * The request has loaded data and been processed into a response.
     *
     * If the request is served from the cache, this is the only delegate method that will be called.
     */
    public void requestDidFinishLoad(TTURLRequest request);
    
    /**
     * Called when an error prevents the request from completing successfully.
     */
    public void requestDidFailWithError(TTURLRequest request, Throwable error);

    /**
     * Called when the request was cancelled.
     */
    public void requestDidCancelLoad(TTURLRequest request);
}
