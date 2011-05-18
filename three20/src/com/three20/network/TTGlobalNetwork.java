package com.three20.network;

public class TTGlobalNetwork {
	private static int mNetworkTaskCount = 0;
	
	public synchronized static void TTNetworkRequestStarted() {
		if (mNetworkTaskCount == 0) {

		}
		mNetworkTaskCount++;
	}

	public synchronized static void TTNetworkRequestStopped() {
		mNetworkTaskCount--;
		mNetworkTaskCount = Math.max(0, mNetworkTaskCount);
		if (mNetworkTaskCount == 0) {

		}
	}
}
