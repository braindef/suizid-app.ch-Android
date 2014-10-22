package ch.marclandolt.spa.backend;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import ch.marclandolt.spa.config.Config;

import android.content.Intent;
import android.util.Log;

public class ManagementChatMessageListener implements MessageListener {

	private static final String TAG = ManagementChatMessageListener.class
			.getSimpleName();
	public SmackAsyncTask parent;

	@Override
	public void processMessage(Chat chat, Message message) {

		Log.d(TAG, message.getBody().toString());
		Log.d(TAG, message.getFrom().toString());

		// Getting login Data for anonymous help seekers
		if (message.getBody().toString()
				.contains("SuicidePreventionAppServerLoginRequestAnswer")) {

			Config.username = message.getBody().toString().split(";")[1]
					.split("@")[0];
			Config.password = message.getBody().toString().split(";")[2];
			Log.d(TAG, Config.username + " " + Config.password);

			try {
				Log.d(TAG, "disconnect");
				parent.connection.disconnect();
			} catch (SmackException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// when the credentials are entered in the static fields of Config
			// we start again with that credentials
			parent.login(Config.username, Config.password, Config.serverBotJid);
			return;
		}

		// Processing the register message from the sleek-xmpp server bot
		if (message.getBody().toString()
				.contains("SuicidePreventionAppServerSupporterLoggedInAck")) {

			// Intent to login fragment that causes the login window to close
			Intent intent = new Intent();
			intent.setAction(XmppService.SEND_TO_ACTIVITY);
			intent.putExtra(XmppService.LOGGED_IN, "true");
			parent.parent.sendBroadcast(intent);
		}

		// assigning help seeking person to the supporter
		if (message.getBody().toString()
				.contains("SuicidePreventionAppServerSupporterRequestCalling;")) {

			if (Config.Available && Config.free) {
				Config.free=false;
				Log.d(TAG, "selected helpSeeker: "
						+ message.getBody().toString().split(";")[1]);
				Config.helpSeeker = message.getBody().toString().split(";")[1];

				parent.parent.openAlert();

				
			} else {
				String tempHelpSeeker = message.getBody().toString().split(";")[1];

				try {
					parent.managementChat
							.sendMessage("SuicidePreventionAppServerSupporterRequestCallingDecline;"
									+ tempHelpSeeker);
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			return;

		}

		
		  // Getting supporter name if it is a help seeker if (message
		  if (message.getBody().toString().contains("SuicidePreventionAppServerSupporterRequestCallingAccept")) {
		  Log.d(TAG, "selected supporter: " + message.getBody().toString().split(";")[1]);
		  Config.supporter = message.getBody().toString().split(";")[1] .split("/")[0];
		  
		  Log.d(TAG, Config.supporter); 
		  
		  if (Config.supporter !=  null)
		  { 
			  Log.d("supporter", "in class registred");
			  parent.chat = ChatManager.getInstanceFor(parent.connection).createChat( Config.supporter, parent.cml);
			  
				Intent intent = new Intent();
				intent.setAction(XmppService.SEND_TO_ACTIVITY);
				intent.putExtra(XmppService.OPEN_CHAT, "true");
				parent.parent.sendBroadcast(intent);
				
				
				//Send initial message
				try {
					parent.chat.sendMessage("Connected");
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  
		  }
		  
		  // open chat Fragment on UI parent.openChatWindow();
		  
		  return;
		  
		  }
		 
	}
}
