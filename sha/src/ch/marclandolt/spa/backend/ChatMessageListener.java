package ch.marclandolt.spa.backend;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import ch.marclandolt.spa.config.Config;

import android.content.Intent;
import android.util.Log;

public class ChatMessageListener implements MessageListener {

	private static final String TAG = ChatMessageListener.class.getSimpleName();
	public SmackAsyncTask parent;

	@Override
	public void processMessage(Chat chat, Message message) {

		Log.d(TAG, message.getBody().toString());
		Log.d(TAG, message.getFrom().toString());
		
		Intent intent = new Intent();
		intent.setAction(XmppService.SEND_TO_ACTIVITY);
		intent.putExtra(XmppService.MESSAGE_TO_FRAGMENT_CHAT, "true");
		intent.putExtra("message", message.getBody().toString());
		intent.putExtra("sender", message.getFrom().toString());
		parent.parent.sendBroadcast(intent);

		//Intent intent2 = new Intent();
		//intent2.setAction(XmppService.SEND_TO_ACTIVITY);
		//intent2.putExtra(XmppService.OPEN_CHAT, "true");
		//parent.parent.sendBroadcast(intent);
		 
	}
}
