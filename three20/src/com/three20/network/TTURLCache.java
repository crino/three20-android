package com.three20.network;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.util.Log;

import com.three20.ns.NSData;
import com.three20.ns.NSDate;
import com.three20.util.MD5;

/**
 * TTURLCache is the class to handle cache of url requests
 * 
 * @author cseverini
 */
public class TTURLCache {
	private static final String LOG = TTURLCache.class.getSimpleName();

	private static TTURLCache sharedCacheInstance;

	public static final long TT_DEFAULT_CACHE_EXPIRATION_AGE = (60 * 60 * 24 * 7);
	public static final long TT_DEFAULT_CACHE_INVALIDATION_AGE = (60 * 60 * 24);
	public static final long TT_CACHE_EXPIRATION_AGE_NEVER = (long) (1.0 / 0.0);

	private static final long kLargeImageSize = 600 * 400;

	private static final String kDefaultBaseExtDirCachePath = "/sdcard/cache";
	private static final String kDefaultBaseIntDirCachePath = "/data/data/%s/cache";
	private static final String kDefaultCacheName = "Three20";

	private String mCachePath;
	private HashMap<String, Bitmap> mImageCache = new HashMap<String, Bitmap>();
	private ArrayList<String> mImageSortedList = new ArrayList<String>();
	private int mTotalPixelCount = 0;
	private int mMaxPixelCount = 0;
	private boolean mDisableDiskCache = false;//
	private boolean mDisableImageCache = false;//
	private static boolean mExternalStorage = false;
	
	private long mInvalidationAge = TT_DEFAULT_CACHE_INVALIDATION_AGE;//

	private static String cachePathWithName(String name, boolean externalStorage, String packageName) {
		File newFile = null;
		
		if (!externalStorage) {
			String tempPath = String.format(kDefaultBaseIntDirCachePath, packageName);
			newFile = new File(tempPath);	
			if (!newFile.getParentFile().exists()) {
				externalStorage = true;
			} else {
				newFile = new File(newFile, name);
			}
		}
		if (externalStorage) {
			newFile = new File(kDefaultBaseExtDirCachePath, name);
		}
		if (!newFile.exists()) {
			newFile.mkdirs();
		}
		String p = newFile.getPath();
		mExternalStorage = externalStorage;
		return p;
	}
	
	/**
	 * @return the shared cache
	 */
	public static TTURLCache getSharedCache() {
		if (sharedCacheInstance == null) {
			sharedCacheInstance = new TTURLCache();
		}
		return sharedCacheInstance;
	}

	/**
	 * Sets the shared cache
	 * @param cache the TTURLCache to set
	 */
	public static void setSharedCache(TTURLCache cache) {
		sharedCacheInstance = cache;
	}

	/**
	 * Creates the cache with default name in external storage
	 */
	public TTURLCache() {
		this(kDefaultCacheName, true, null);
	}
	
	/**
	 * Creates the named cache in external storage
	 * @param cacheName the name of cache
	 */
	public TTURLCache(String cacheName) {
		this(cacheName, true, null);
	}
	/**
	 * Creates the named cache in internal storage
	 * @param cacheName the name of cache
	 * @param packageName the name of application package
	 */
	public TTURLCache(String cacheName, String packageName) {
		this(cacheName, false, packageName);
	}
	/**
	 * Creates the named cache in external or internal storage
	 * @param cacheName the name of cache
	 * @param externalStorage true if external
	 * @param packageName the name of application package for internal storage
	 */
	private TTURLCache(String cacheName, boolean externalStorage, String packageName) {
		mCachePath = TTURLCache.cachePathWithName(cacheName, externalStorage, packageName);
	}

	public boolean isOnExternalStorage() {
		return mExternalStorage;
	}
	
	/**
	 * @param mCachePath
	 *            the mCachePath to set
	 */
	public void setCachePath(String mCachePath) {
		this.mCachePath = mCachePath;
	}

	/**
	 * @return the mCachePath
	 */
	public String getCachePath() {
		return mCachePath;
	}

	public void setMaxPixelCount(int mMaxPixelCount) {
		this.mMaxPixelCount = mMaxPixelCount;
	}

	public int getMaxPixelCount() {
		return mMaxPixelCount;
	}

	public void setDisableDiskCache(boolean mDisableDiskCache) {
		this.mDisableDiskCache = mDisableDiskCache;
	}

	public boolean isDisableDiskCache() {
		return mDisableDiskCache;
	}

	public void setDisableImageCache(boolean mDisableImageCache) {
		this.mDisableImageCache = mDisableImageCache;
	}

	public boolean isDisableImageCache() {
		return mDisableImageCache;
	}

	public void setInvalidationAge(long mInvalidationAge) {
		this.mInvalidationAge = mInvalidationAge;
	}

	public long getInvalidationAge() {
		return mInvalidationAge;
	}

	private void expireImagesFromMemory() {
		while (mImageSortedList.size() > 0) {
			String key = mImageSortedList.get(0);
			Bitmap image = mImageCache.get(key);

			Log.d(LOG, "EXPIRING " + key);

			mTotalPixelCount -= image.getWidth() * image.getHeight();
			mImageCache.remove(key);
			mImageSortedList.remove(0);

			if (mTotalPixelCount <= mMaxPixelCount) {
				break;
			}
		}
	}

	public String keyForURL(String url) {
		return MD5.generateMD5(url);
	}

	public String cachePathForURL(String url) {
		String key = keyForURL(url);
		return cachePathForKey(key);
	}

	public String cachePathForKey(String key) {
		return mCachePath + File.separator + key;
	}

	// - (NSString*)etagCachePathForKey:(NSString*)key;??

	public boolean hasDataForURL(String url) {
		String filePath = cachePathForURL(url);
		File fm = new File(filePath);
		return fm.exists();
	}

	public boolean hasDataForKey(String key, long expirationAge) {
		String filePath = cachePathForKey(key);
		File fm = new File(filePath);
		if (fm.exists()) {
			long modified = fm.lastModified();
			long timeIntervalSinceNow = System.currentTimeMillis() - modified;
			if (timeIntervalSinceNow < expirationAge) {
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean hasImageForURL(String url, boolean fromDisk) {
		boolean hasImage = (null != mImageCache.get(url));
		// if (!hasImage && fromDisk) {
		// if (TTIsBundleURL(URL)) {
		// hasImage = [self imageExistsFromBundle:URL];
		// } else if (TTIsDocumentsURL(URL)) {
		// hasImage = [self imageExistsFromDocuments:URL];
		// }
		// }
		// "com/codecarpet/fbconnect/resources/logout.png");
		// getClass().getClassLoader().getResourceAsStream(path);
		return hasImage;
	}

	public NSData dataForURL(String url) {
		return dataForURL(url, TTURLCache.TT_CACHE_EXPIRATION_AGE_NEVER, null);
	}

	public NSData dataForURL(String url, long expirationAge, NSDate timestamp) {
		String key = keyForURL(url);
		return dataForKey(key, expirationAge, timestamp);
	}

	public NSData dataForKey(String key, long expirationAge, NSDate timestamp) {
		String filePath = cachePathForKey(key);
		File fm = new File(filePath);
		if (fm.exists()) {
			NSDate dateModified = new NSDate();
			dateModified.setTime(fm.lastModified());
			long timeIntervalSinceNow = (long) dateModified
					.timeIntervalSinceNow();
			if (timeIntervalSinceNow < -expirationAge) {
				return null;
			}
			if (timestamp != null) {
				timestamp.setTime(dateModified.getTime());
			}
			return new NSData(fm);
		}
		return null;
	}

	public Bitmap imageForURL(String url) {
		return imageForURL(url, true);
	}

	public Bitmap imageForURL(String url, boolean fromDisk) {
		Bitmap image = mImageCache.get(url);
//		if (image == null && fromDisk) {
//			String key = keyForURL(url);
//			String filePath = cachePathForKey(key);
//			image = BitmapFactory.decodeFile(filePath);
//			storeImage(image, url);
//		}
		return image;
	}

	// - (NSString*)etagForKey:(NSString*)key;??

	public void storeDataURL(NSData data, String url) {
		String key = keyForURL(url);
		storeDataKey(data, key);
	}

	public void storeDataKey(NSData data, String key) {
		if (!mDisableDiskCache) {
			String filePath = cachePathForKey(key);
			File fm = new File(filePath);
			data.writeToFile(fm);
		}
	}

	public void storeImage(Bitmap image, String url) {
		storeImage(image, url, false);
	}

	private void storeImage(Bitmap image, String url, boolean force) {
		if (null != image && (force || !mDisableImageCache)) {
			int pixelCount = image.getWidth() * image.getHeight();

			if (force || (pixelCount < TTURLCache.kLargeImageSize)) {
				mTotalPixelCount += pixelCount;

				if ((mTotalPixelCount > mMaxPixelCount) && (mMaxPixelCount > 0)) {
					expireImagesFromMemory();
				}

				mImageSortedList.add(url);
				mImageCache.put(url, image);
			}
		}
	}
	
	// - (void)storeEtag:(NSString*)etag forKey:(NSString*)key;
	// - (NSString*)storeTemporaryImage:(UIImage*)image toDisk:(BOOL)toDisk;
	// - (NSString*)storeTemporaryData:(NSData*)data;
	// - (NSString*)storeTemporaryFile:(NSURL*)fileURL;
	// - (void)moveDataForURL:(NSString*)oldURL toURL:(NSString*)newURL;
	// - (void)moveDataFromPath:(NSString*)path toURL:(NSString*)newURL;
	// - (NSString*)moveDataFromPathToTemporaryURL:(NSString*)path;

	public void removeURL(String url, boolean fromDisk) {
		String key = keyForURL(url);
		mImageCache.remove(key);
		mImageSortedList.remove(key);

		if (fromDisk) {
			String filePath = cachePathForKey(key);
			File fm = new File(filePath);
			if (filePath != null && fm.exists()) {
				fm.delete();
			}
		}
	}

	public void removeKey(String key) {
		String filePath = cachePathForKey(key);
		File fm = new File(filePath);
		if (filePath != null && fm.exists()) {
			fm.delete();
		}
	}

	public void removeAll(boolean fromDisk) {
		mImageCache.clear();
		mImageSortedList.clear();
		mTotalPixelCount = 0;

		if (fromDisk) {
			File fm = new File(mCachePath);
			fm.delete();
			fm.mkdirs();
		}
	}

	public void invalidateURL(String url) {
		String key = keyForURL(url);
		invalidateKey(key);
	}

	public void invalidateKey(String key) {
		String filePath = cachePathForKey(key);
		File fm = new File(filePath);
		if (filePath != null && fm.exists()) {
			NSDate invalidDate = new NSDate(-mInvalidationAge);
			fm.setLastModified(invalidDate.getTime());
		}
	}

	public void invalidateAll() {
		NSDate invalidDate = new NSDate(-mInvalidationAge);
		File fm = new File(mCachePath);
		File[] files = fm.listFiles();
		for (File currentFile : files) {
			currentFile.setLastModified(invalidDate.getTime());
		}
	}
}
