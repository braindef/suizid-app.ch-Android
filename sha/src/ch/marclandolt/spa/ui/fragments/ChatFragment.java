package ch.marclandolt.spa.ui.fragments;

import org.jivesoftware.smack.XMPPException;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.marclandolt.spa.R;
import ch.marclandolt.spa.backend.XmppService;
import ch.marclandolt.spa.config.Config;

public class ChatFragment extends Fragment implements OnClickListener,
		OnKeyListener {

	TextView chatTextView;
	Button sendButton;
	EditText chatEditText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_chat, container, false);

		chatTextView = (TextView) view.findViewById(R.id.chatTextView);
		chatTextView.setOnClickListener(this);
		chatTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

		chatEditText = (EditText) view.findViewById(R.id.chatEditText);
		chatEditText.setOnKeyListener(this);

		sendButton = (Button) view.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(this);

		IntentFilter filter = new IntentFilter(XmppService.SEND_TO_ACTIVITY);
		MyRxReceiver receiver = new MyRxReceiver();
		getActivity().registerReceiver(receiver, filter);

		return view;
	}

	@Override
	public void onClick(View view) {

		chatTextView.append(Html.fromHtml("<font color=#3333FF>me:</font> "
				+ chatEditText.getText().toString() + "<br>"));

		// Intent that sends the typed message to the xmpp service
		Intent intent = new Intent();
		intent.setAction(XmppService.SEND_TO_SERVICE);
		intent.putExtra("MESSAGE_FROM_FRAGMENT_CHAT", chatEditText.getText().toString());
		getActivity().sendBroadcast(intent);
		chatEditText.setText("");
	}

	// Keylistener that listens on Enter key of the editText of the typed
	// messages
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		{
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					sendButton.performClick();
					return true;
				default:
					break;
				}
			}
			return false;
		}

	}

	// Receiver that listens to the messages from the xmpp-service
	public class MyRxReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra(XmppService.MESSAGE_TO_FRAGMENT_CHAT))
			{
			String message = intent.getStringExtra("message");
			String sender = intent.getStringExtra("sender");
			
			chatTextView.append(Html.fromHtml("<font color=#FF3333>"
					+ sender.split("@")[0] + ":</font> " + message + "<br>"));
			}
		}
	}
}