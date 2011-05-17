package com.three20.ui;


import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

public class TTErrorView extends LinearLayout {

	protected TextView mTextView = null;
	protected TextView mSubtextView = null;

	public TTErrorView(Context context, int layoutId) {
		super(context);
		LayoutInflater.from(context).inflate(layoutId, this);
	}
	
	public TTErrorView(Context context) {
		super(context);
		{
			this.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			this.setOrientation(LinearLayout.VERTICAL);
			this.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);

			mTextView = new TextView(context);
			mTextView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mTextView.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);
			mTextView.setTextSize(18.0f);
			this.addView(mTextView);

			mSubtextView = new TextView(context);
			mSubtextView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mSubtextView.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);
			mSubtextView.setTextSize(10.0f);
			this.addView(mSubtextView);		
		}
	}

	public TextView getTextView() {
		return this.mTextView;
	}
	
	public TextView getSubtextView() {
		return this.mSubtextView;
	}
}
