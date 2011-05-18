package com.three20.ui;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TTLoadingView extends LinearLayout {
	
	protected TextView mTextView = null;
	protected ProgressBar mProgressBar = null;

	public TTLoadingView(Context context, int layoutId) {
		super(context);
		LayoutInflater.from(context).inflate(layoutId, this);
	}
	
	public TTLoadingView(Context context) {
		super(context);
		{
			this.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			this.setOrientation(LinearLayout.VERTICAL);
			this.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);

			mProgressBar = new ProgressBar(context);
			mProgressBar.setLayoutParams(new LayoutParams(
					24, 24));
			mProgressBar.setIndeterminate(true);
			this.addView(mProgressBar);	
			
			mTextView = new TextView(context);
			mTextView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mTextView.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);
			mTextView.setTextSize(18.0f);
			this.addView(mTextView);
			
		}
	}

	public TextView getTextView() {
		return this.mTextView;
	}
	
	public ProgressBar getProgressBar() {
		return this.mProgressBar;
	}
	
}
