package pl.blastteam.iceblast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class IceBlast extends Activity implements OnClickListener,
		IceBlastConstants {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		View przyciskTrening = findViewById(R.id.przycisk_trening);
		przyciskTrening.setOnClickListener(this);
		View przyciskWielu = findViewById(R.id.przycisk_wielu);
		przyciskWielu.setOnClickListener(this);
		View przyciskInfo = findViewById(R.id.przycisk_info);
		przyciskInfo.setOnClickListener(this);
		View przyciskWyjscie = findViewById(R.id.przycisk_wyjscie);
		przyciskWyjscie.setOnClickListener(this);
		View przyciskAbout = findViewById(R.id.przycisk_about);
		przyciskAbout.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.przycisk_trening:
			uruchomTrening();
			break;
		case R.id.przycisk_wielu:
			wyswietlDialogMulti();
			break;
		case R.id.przycisk_info:
			Intent i = new Intent(this, Info.class);
			startActivity(i);
			break;
		case R.id.przycisk_wyjscie:
			finish();
			break;
		case R.id.przycisk_about:
			Intent j = new Intent(this, About.class);
			startActivity(j);
			break;

		}
	}

	private void uruchomTrening() {
		Intent intencja = new Intent(IceBlast.this, Trening.class);
		startActivity(intencja);
	}

	private void wyswietlDialogMulti() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.tytul_multiplayer)
				.setItems(R.array.multi_entries,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								uruchomMulti(i);
							}
						}).show();
	}

	private void uruchomMulti(int i) {
		switch (i) {
		case 0:
			Intent intencja = new Intent(IceBlast.this, Multitouch.class);
			startActivity(intencja);
			break;
		case 1:
			Intent intentLan = new Intent(IceBlast.this, MultiLan2.class);
			startActivity(intentLan);
			break;
		}
	}

}