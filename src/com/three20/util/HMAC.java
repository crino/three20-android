package com.three20.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC {
	public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	/**
	 * Computes RFC 2104-compliant HMAC signature. 
	 * @param data The data to be signed.
	 * @param key The signing key.
	 * @return The byte array of HMAC signature.
	 */
	public static byte[] getSignatureBytes(String data, String key) {
		byte[] result = null;
		try {

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
					HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing
			// key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			result = mac.doFinal(data.getBytes());
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * Computes RFC 2104-compliant HMAC signature.
	 * @param data The data to be signed.
	 * @param key The signing key.
	 * @return The Base64 encoded string of HMAC signature.
	 */
	public static String getSignatureBase64String(String data, String key) {
		byte[] hmac = HMAC.getSignatureBytes(data, key);
		// base64-encode the hmac
		String result = new String(Base64Coder.encode(hmac));
		return result;
	}
}
