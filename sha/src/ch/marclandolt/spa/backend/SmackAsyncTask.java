package ch.marclandolt.spa.backend;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import ch.marclandolt.spa.config.Config;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class SmackAsyncTask extends AsyncTask<Void, Void, Void> implements
		ChatManagerListener {

	private static final String TAG = SmackAsyncTask.class.getSimpleName();

	public XmppService parent;
	ConnectionConfiguration conConfig;
	XMPPTCPConnection connection;

	public Chat managementChat;
	public ManagementChatMessageListener mcml;

	public Chat chat;
	public ChatMessageListener cml;

	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "doInBackground()");

		// init Smack xmpp Library
		SmackAndroid.init(parent.getApplicationContext());

		// create Connection Configurateion
		conConfig = new ConnectionConfiguration(Config.servername, 5222);

		// using encryption does not yet work so we disable the encryption
		// functions
		conConfig
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

		login(Config.username, Config.password, Config.serverBotJid);

		return null;
	}

	public void login(String username, String password, String serverBotJid) {
		Log.d(TAG, "login()");
		try {

			connection = new XMPPTCPConnection(conConfig);
			connection.connect();
			ChatManager.getInstanceFor(connection).addChatListener(this);
			Log.d(TAG, "connected()");
		} catch (SmackException e) {
			Log.d(TAG, "smackException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "ioException");
			e.printStackTrace();
		} catch (XMPPException e) {
			Log.d(TAG, "XmppException");
			e.printStackTrace();
		}

		if (connection.isConnected() == false) {
			Intent intent = new Intent();
			intent.setAction(XmppService.SEND_TO_ACTIVITY);
			intent.putExtra(XmppService.CONNECT_FAILED, "true");
			parent.sendBroadcast(intent);

		} else {
			Intent intent = new Intent();
			intent.setAction(XmppService.SEND_TO_ACTIVITY);
			intent.putExtra(XmppService.CONNECTED, "true");
			parent.sendBroadcast(intent);
		}

		if (mcml == null) {
			mcml = new ManagementChatMessageListener();
			mcml.parent = this;
		}

		if (cml == null) {
			cml = new ChatMessageListener();
			cml.parent = this;
		}

		Log.d(TAG, "login()");
		// if there is no username set, because it's a help seeker that has no
		// login it gets a login with the management user
		if (username == null) {
			username = Config.managementUser;
			password = Config.managementPassword;
			Log.d(TAG, "getLogin");
		}
		try {
			Log.d(username, password);
			connection.login(username, password);
			Log.d(TAG, "logged in");
		} catch (SaslException e) {
			Log.d(TAG, "saslException");
			e.printStackTrace();
		} catch (XMPPException e) {
			Log.d(TAG, "XMPPException");
			e.printStackTrace();
		} catch (SmackException e) {
			Log.d(TAG, "IOExecption");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "IOExecption");
			e.printStackTrace();
		}

		// If we logged in with the management user we request some login Data
		// to log in again with this data
		if (username.startsWith(Config.managementUser)) {
			Log.d(TAG, "getLogin");
			this.managementChat = ChatManager.getInstanceFor(connection)
					.createChat(serverBotJid, this.mcml);
			try {
				this.managementChat
						.sendMessage("SuicidePreventionAppServerLoginRequest");
			} catch (NotConnectedException e) {
				Log.d("myApp", "notconnected");
				e.printStackTrace();
			} catch (XMPPException e) {
				Log.d("myApp", "XMPPException");
				e.printStackTrace();
			}
		}

		// if it is a help seeker with correct login it requests a supporter to chat with
		if (Config.isHelpSeeker && ! username.startsWith(Config.managementUser)) {
			Log.d("isHelpSeeker", "isHelpSeeker");
			this.managementChat = ChatManager.getInstanceFor(connection)
					.createChat(serverBotJid, mcml);
			try {
				this.managementChat
						.sendMessage("SuicidePreventionAppServerSupporterRequest");
			} catch (NotConnectedException e) {
				Log.d("myApp", "notconnected");
				e.printStackTrace();
			} catch (XMPPException e) {
				Log.d("myApp", "XMPPException");
				e.printStackTrace();
			}

		}

		// if it is a supporter it tells the sleekxmpp jabber Bot service it's
		// presence
		if (Config.isSupporter) {
			Log.d("isSupporter", "isSupporter");
			this.managementChat = ChatManager.getInstanceFor(connection)
					.createChat(serverBotJid, mcml);
			try {

				this.managementChat
						.sendMessage("SuicidePreventionAppServerSupporterLoggedIn");

			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void chatCreated(Chat chat, boolean arg1) {

		Log.d(TAG, "chat created");

		if (chat.getParticipant().startsWith(Config.serverBotJid)) {
			Log.d(TAG, "chat starts with serverBotJid");
			return;
		}

		chat.addMessageListener(cml);
		this.chat = chat;

	}


}
