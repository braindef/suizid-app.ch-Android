package ch.marclandolt.spa.ui.activities;

import ch.marclandolt.spa.R;
import ch.marclandolt.spa.backend.XmppService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import android.content.IntentFilter;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.marclandolt.spa.backend.*;
import ch.marclandolt.spa.config.Config;
import ch.marclandolt.spa.ui.fragments.CallFragment;
import ch.marclandolt.spa.ui.fragments.ChatFragment;
import ch.marclandolt.spa.ui.fragments.ConfigFragment;
import ch.marclandolt.spa.ui.fragments.HelpFragment;
import ch.marclandolt.spa.ui.fragments.LoginFragment;
import ch.marclandolt.spa.ui.fragments.SearchFragment;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	XmppBroadcastReceiver xmppBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent service = new Intent(this, XmppService.class);
		startService(service);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_place, new HelpFragment());
		ft.commit();

		Intent textCapitalizeIntent = new Intent(XmppService.SEND_TO_SERVICE);
		textCapitalizeIntent.putExtra(XmppService.SERVICE_INPUT, "test");
		sendBroadcast(textCapitalizeIntent);
		

		SharedPreferences preferences = getApplicationContext().getSharedPreferences("suicideApp", Context.MODE_PRIVATE); 
		
		Config.username = preferences.getString("suicideApp-username", "username");
		Config.password = preferences.getString("suicideApp-password", "password");
		
		Intent intent = new Intent(XmppService.SEND_TO_SERVICE);		
		intent.putExtra(XmppService.CONNECT,"connect");
		intent.putExtra(XmppService.OFFER_HELP, "true");
		getApplicationContext().sendBroadcast(intent);

	}

	/**
	 * registering BroadcastReceiver
	 */
	private void registerReceiver() {
		/* create filter for exact intent what we want from other intent */
		IntentFilter intentFilter = new IntentFilter(
				XmppService.SEND_TO_ACTIVITY);
		xmppBroadcastReceiver = new XmppBroadcastReceiver();
		registerReceiver(xmppBroadcastReceiver, intentFilter);
	}

	public class XmppBroadcastReceiver extends BroadcastReceiver {
		/**
		 * action string for our broadcast receiver to get notified
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra(XmppService.CONNECT_FAILED)) {
				String resultText = intent
						.getStringExtra(XmppService.CONNECT_FAILED);
				Toast.makeText(context, "Connect Failed", 2000).show();
			}
			if (intent.hasExtra(XmppService.CONNECTED)) {
				String resultText = intent
						.getStringExtra(XmppService.CONNECTED);
				Toast.makeText(context, "Connected, searching Supporter", 4000)
						.show();
			}
			if (intent.hasExtra(XmppService.OPEN_CHAT)) {
				String resultText = intent
						.getStringExtra(XmppService.OPEN_CHAT);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.fragment_place, new ChatFragment());
				ft.commit();
			}
			if (intent.hasExtra(XmppService.OPEN_CALL)) {
				String resultText = intent
						.getStringExtra(XmppService.OPEN_CALL);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.fragment_place, new CallFragment());
				ft.commit();
			}
			if (intent.hasExtra(XmppService.OPEN_HELP)) {
				String resultText = intent
						.getStringExtra(XmppService.OPEN_HELP);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.fragment_place, new HelpFragment());
				ft.commit();
			}
			if (intent.hasExtra(XmppService.OPEN_LOGIN)) {
				String resultText = intent
						.getStringExtra(XmppService.OPEN_LOGIN);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.fragment_place, new LoginFragment());
				ft.commit();
			}
		}
	};

	@Override
	public void onBackPressed() {
		endChat();
	}

	protected void endChat() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(R.string.end_chat_message);
		alertDialogBuilder.setPositiveButton(R.string.positive_button,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent();
						intent.setAction(XmppService.SEND_TO_ACTIVITY);
						intent.putExtra(XmppService.OPEN_HELP, "true");
						sendBroadcast(intent);
						/*
						Intent intent2 = new Intent();
						intent2.setAction(XmppService.SEND_TO_SERVICE);
						intent2.putExtra("MESSAGE_FROM_FRAGMENT_CHAT", "Disconnecting");
						sendBroadcast(intent2);
						*/
						if (Config.isHelpSeeker) {
							Intent intent3 = new Intent();
							intent3.setAction(XmppService.SEND_TO_SERVICE);
							intent3.putExtra(XmppService.DISCONNECT, "true");
							sendBroadcast(intent3);
						} 
						Config.supporter = null;
						Config.helpSeeker = null;
						Config.isHelpSeeker = false;
						Config.free = true;
					}
				});
		alertDialogBuilder.setNegativeButton(R.string.negative_button,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		/* we should unregister BroadcastReceiver here */
		unregisterReceiver(xmppBroadcastReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		/* we register BroadcastReceiver here */
		registerReceiver();
		if (Config.ringing) {
			Config.ringing = false;
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_place, new CallFragment());
			ft.commit();
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.action_exit:
			finish();
			// System.exit(0);
			return true;

		case R.id.end_chat:
			endChat();
			return true;
		case R.id.action_available:
			if (item.isChecked() == true) {
				item.setChecked(false);
				Config.Available = false;
			} else {
				item.setChecked(true);
				Config.Available = true;
			}
			return true;
		case R.id.action_settings:
			FragmentTransaction ft2 = getFragmentManager().beginTransaction();
			ft2.replace(R.id.fragment_place, new ConfigFragment());
			ft2.commit();
			return true;
		case R.id.supporter_login:
			FragmentTransaction ft3 = getFragmentManager().beginTransaction();
			ft3.replace(R.id.fragment_place, new LoginFragment());
			ft3.commit();
			return true;
		case R.id.action_transfer:
			return true;

		}
		return super.onOptionsItemSelected(item);
	}
}