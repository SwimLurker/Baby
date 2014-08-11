package org.slstudio.baby.ui;

import org.slstudio.baby.R;

import android.content.Context;

public class SplashImageProviderManager {
	public static String RANDOM_PROVIDER = "random";
	
	private static SplashImageProviderManager _instance = null;
	private Context context = null;
	
	private SplashImageProviderManager(Context context){
		this.context = context;
	}

	public static SplashImageProviderManager getInstance(Context context){
		if(_instance == null){
			_instance = new SplashImageProviderManager(context);
		}
		return _instance;
	}
	
	public ISplashImageProvider getSplashImageProvider(String providerName){
		if(providerName.equalsIgnoreCase(RANDOM_PROVIDER)){
			return new RandomSplashImageProvider(context.getResources(), 
					new int[]{R.drawable.splash});
		}else{
			return new DefaultSplashImageProvider(context.getResources());
		}
	}
}
