package com.three20.ui;

import android.graphics.Bitmap;

public abstract class TTImageViewDelegate {
	/**
	 * Called when the image begins loading asynchronously.
	 */
	public abstract void imageViewDidStartLoad(TTImageView imageView);

	/**
	 * Called when the image finishes loading asynchronously.
	 */
	public abstract void imageViewDidLoadImage(TTImageView imageView, Bitmap image);

	/**
	 * Called when the image failed to load asynchronously.
	 * If error is nil then the request was cancelled.
	 */
	public abstract void imageViewDidFailLoadWithError(TTImageView imageView, Throwable error);
}
