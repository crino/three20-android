package com.three20.ns;

import java.io.File;

public class NSData {

	public static final NSData EmptyData = new NSData();

	protected byte[] bytes;

	/**
	 * Default constructor creates a zero-data object.
	 */
	public NSData() {
		bytes = new byte[0];
	}

	/**
	 * Creates an object containing a copy of the contents of the specified
	 * NSData object.
	 */
	public NSData(NSData aData) {
		this(aData.getBytes());
	}

	/**
	 * Creates an object containing a copy of the specified bytes.
	 */
	public NSData(byte[] data) {
		this(data, 0, data.length);
	}

	/**
	 * Creates an object containing a copy of the bytes from the specified array
	 * within the specified range.
	 */
	public NSData(byte[] data, int start, int length) {
		bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = data[start + i];
		}
	}

	/**
	 * Creates an object containing the contents of the specified file. Errors
	 * reading the file will produce an empty or partially blank array.
	 */
	public NSData(File aFile) {
		int len = (int) aFile.length();
		byte[] data = new byte[len];
		try {
			new java.io.FileInputStream(aFile).read(data);
		} catch (Exception exc) {
			// produce an empty or partially blank array
		}
		bytes = data;
	}

	/**
	 * Returns the length of the contained data.
	 */
	public int length() {
		return bytes.length;
	}

	/**
	 * Returns the contained data.
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Sets the contained data.
	 */
	public void setBytes(byte[] data) {
		bytes = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			bytes[i] = data[i];
		}
	}

	
	public String toString() { 
		String hex = "0123456789ABCDEF"; 
		StringBuffer buf = new StringBuffer();
		buf.append('<');
		for (int i = 0; i < bytes.length; i++) { 
			byte b = bytes[i]; 
			buf.append(hex.charAt((b & 0xf0) >> 4)); 
			buf.append(hex.charAt(b & 0x0f)); 
			if (i % 5 == 4){
				buf.append(' '); 
			}
		}
		buf.append('>'); 
		return buf.toString(); 
	}

	/**
	 * Returns whether the specified data is equivalent to these data.
	 */
	public boolean isEqualToData(NSData aData) {
		if (length() != aData.length())
			return false;
		byte[] a = getBytes();
		byte[] b = aData.getBytes();

		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	public boolean isEqual(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof NSData)
			return isEqualToData((NSData) obj);
		return false;
	}
	
	public boolean writeToFile(File aFile){
		try {
			new java.io.FileOutputStream(aFile).write(bytes);
		} catch (Exception exc) {
			return false;
		}
		return true;
	}
}
