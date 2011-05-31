package com.three20.example;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.three20.ui.TTImageView;
import com.three20.ui.TTImageViewDelegate;

public class MainActivity extends Activity {
	
	private TTImageView imgView;
	private TextView txtView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        txtView = (TextView)findViewById(R.id.textview1);
        
        imgView = (TTImageView)findViewById(R.id.imageView1);
        imgView.setDelegate(new TTImageViewDelegate() {
        	@Override
    		public void imageViewDidStartLoad(TTImageView imageView) {
    			txtView.setText("Starting loading: " + imgView.getImageURL());
    		}
        	@Override
    		public void imageViewDidLoadImage(TTImageView imageView, Bitmap image) {
    			txtView.setText("Image loaded from: " + imgView.getImageURL());
    		}
        	@Override
    		public void imageViewDidFailLoadWithError(TTImageView imageView, Throwable error) {
    			txtView.setText("Error loading image from: " + imgView.getImageURL() + "\n" + error.getLocalizedMessage());
    		}
		});
        
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
		        imgView.setImageURL("http://farm4.static.flickr.com/3163/3110335722_7a906f9d8b_m.jpg");
			}
		});
    }
}