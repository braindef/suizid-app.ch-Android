package ch.marclandolt.spa.ui.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ch.marclandolt.spa.R;
import ch.marclandolt.spa.backend.XmppService;
import ch.marclandolt.spa.config.Config;
import ch.marclandolt.spa.ui.activities.MainActivity;

public class LoginFragment extends Fragment implements OnClickListener,
		OnKeyListener {

	private static final String TAG = LoginFragment.class.getSimpleName();

	EditText loginName;
	EditText password;
	TextView login_message;
	Button loginButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_login, container, false);

		Context context = getActivity().getApplicationContext();
		
		SharedPreferences preferences = context.getSharedPreferences("suicideApp", Context.MODE_PRIVATE);  
		
		loginName = (EditText) view.findViewById(R.id.login_name);
		loginName.setText(preferences.getString("suicideApp-username", "username"));

		password = (EditText) view.findViewById(R.id.password);
		password.setText(preferences.getString("suicideApp-password", "password"));
		password.setOnKeyListener(this);

		loginButton = (Button) view.findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);

		login_message = (TextView) view.findViewById(R.id.login_message);

		TextView website = (TextView) view.findViewById(R.id.website);
		website.setOnClickListener(this);

		IntentFilter filter = new IntentFilter(XmppService.SEND_TO_ACTIVITY);
		MyLoggedInReciever receiver = new MyLoggedInReciever();
		getActivity().registerReceiver(receiver, filter);

		return view;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.login_button:

			login(loginName.getText().toString(), password.getText().toString());
			login_message.setText("login attempt");

			break;
		case R.id.website:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://ns3.ignored.ch"));
			startActivity(browserIntent);
			break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				loginButton.performClick();
				return true;
			default:
				break;
			}
		}
		return false;
	}

	public void login(String username, String password) {
		Config.username = username;
		Config.password = password;
		Config.isSupporter = true;
		Config.isHelpSeeker = false;
		Config.supporter = null;
		Config.helpSeeker = null;

		Context context = getActivity().getApplicationContext();
		
		SharedPreferences preferences = context.getSharedPreferences("suicideApp", Context.MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("suicideApp-username", username);
		editor.putString("suicideApp-password", password);
		editor.commit();
		
		Intent intent = new Intent(XmppService.SEND_TO_SERVICE);		
		intent.putExtra(XmppService.CONNECT,"connect");
		intent.putExtra(XmppService.OFFER_HELP, "true");
		getActivity().sendBroadcast(intent);
	}

	public class MyLoggedInReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra(XmppService.LOGGED_IN)) {
				Log.d(TAG, "Received Loggedin");

				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.fragment_place, new HelpFragment());
				ft.commit();
				getActivity().moveTaskToBack(true);


			}
		}
	}

}