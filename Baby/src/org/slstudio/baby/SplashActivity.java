package org.slstudio.baby;


import org.slstudio.baby.ui.ISplashImageProvider;
import org.slstudio.baby.ui.SplashImageProviderManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGTH = 3000;
	private ISplashImageProvider splashImageProvider = null;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		
		splashImageProvider = SplashImageProviderManager.getInstance(this).getSplashImageProvider(SplashImageProviderManager.RANDOM_PROVIDER);
		
		
		final ImageView image = (ImageView)findViewById(R.id.splash_image);
		Drawable splashImage = splashImageProvider.getSplashImage();
		if(splashImage != null){
			image.setBackgroundDrawable(splashImage);
		}
		
		final AnimationSet as = new AnimationSet(false);
		
		final Animation ani1 = new ScaleAnimation(1f,1.1f, 1f, 1.1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.3f);
		final Animation ani2 = new AlphaAnimation(1.0f, 0.3f);
		
		as.addAnimation(ani1);
		as.addAnimation(ani2);
		
		ani1.setDuration(SPLASH_DISPLAY_LENGTH);
		ani1.setFillAfter(true);
		ani1.setStartOffset(500);
		
		ani2.setDuration(SPLASH_DISPLAY_LENGTH);
		ani2.setFillAfter(true);
		ani2.setStartOffset(2000);
		
		//as.setFillAfter(true);
		
		image.setAnimation(as);

		
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.finish();
			}
			
		}, SPLASH_DISPLAY_LENGTH);
		
		as.startNow();
	}
}
