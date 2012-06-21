package pl.blastteam.iceblast;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends Activity {

	MediaPlayer introSound;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splashscreen);
		introSound = MediaPlayer.create(SplashScreen.this, R.raw.intro2 );
		Thread splashWait = new Thread(){
        	public void run(){
        		try {
        			// FIXME: tymczasowo zakomentowano
        			introSound.start();
        			sleep(1200);
        			Intent menuIntent = new Intent("pl.blastteam.iceblast.MAINMENU");
        			startActivity(menuIntent);
        		} catch (InterruptedException e) {
					e.printStackTrace();
				}
        		finally {
        			finish();
        		}
        	}
        };
        splashWait.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		introSound.release();
	}

}
