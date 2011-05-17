package com.three20.network;

public class TTURLRequestCachePolicy {
	public static final int NONE    = 0;
	public static final int MEMORY  = 1;
	public static final int DISK    = 2;
	public static final int NETWORK = 4;
	public static final int NOCACHE = 8;    
	public static final int LOCAL = (MEMORY | DISK);
	public static final int DEFAULT = (MEMORY | DISK | NETWORK);
}
