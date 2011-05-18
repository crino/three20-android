package com.three20.ns;

public class NSError {

	private int mCode;
	private String mDomain;
	private String mHelpAnchor;
	
	public NSError(int code, String domain/*, HashMap<?,?> userInfo*/){
		mCode = code;
		mDomain = domain;
	}
	
	public void setCode(int mCode) {
		this.mCode = mCode;
	}
	public int getCode() {
		return mCode;
	}
	public void setDomain(String mDomain) {
		this.mDomain = mDomain;
	}
	public String getDomain() {
		return mDomain;
	}
	public void setHelpAnchor(String mHelpAnchor) {
		this.mHelpAnchor = mHelpAnchor;
	}
	public String getHelpAnchor() {
		return mHelpAnchor;
	}
	
	
}
