package ch.marclandolt.spa.backend;

import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;

import ch.marclandolt.spa.config.Config;
import ch.marclandolt.spa.ui.activities.MainActivity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class XmppService extends Service {
	private static final String TAG = XmppService.class.getSimpleName();

	public static final String SERVICE_INPUT = "SERVICE_INPUT";
	public static final String SERVICE_OUTPUT = "SERVICE_OUTPUT";

	public static final String CONNECT = "CONNECT";
	public static final String CONNECTED = "CONNECTED";
	public static final String CONNECT_FAILED = "CONNECT_FAILED";
	public static final String DISCONNECT = "DISCONNECT";
	public static final String NEED_HELP = "NEED_HELP";
	public static final String OFFER_HELP = "OFFER_HELP";
	public static final String LOGGED_IN = "LOGGED_IN";
	public static final String CALL = "CALL";
	public static final String OPEN_CHAT = "OPEN_CHAT";
	public static final String OPEN_CALL = "OPEN_CALL";
	public static final String OPEN_HELP = "OPEN_HELP";
	public static final String OPEN_LOGIN = "OPEN_LOGIN";
	public static final String OPEN_SEARCH = "OPEN_SEARCH";
	public static final String OPEN_EVALUATE = "OPEN_EVALUATE";

	public static final String ACCEPT = "ACCEPT";
	public static final String DECLINE = "DECLINE";
	
	public static final String RESCUED = "RESCUED";
	public static final String HELPED = "HELPED";
	public static final String MADEWORSE = "MADEWORSE";

	public static final String MESSAGE_FROM_FRAGMENT_CHAT = "MESSAGE_FROM_FRAGMENT_CHAT";
	public static final String MESSAGE_TO_FRAGMENT_CHAT = "MESSAGE_TO_FRAGMENT_CHAT";

	public final static String SEND_TO_ACTIVITY = "ch.marclandolt.spa.backend.SEND_TO_ACTIVITY";
	public final static String SEND_TO_SERVICE = "ch.marclandolt.spa.backend.SEND_TO_SERVICE";

	SmackAsyncTask smackAsyncTask = null;
	
	Timer timeout=null;

	Ringtone r;

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(XmppService.SEND_TO_SERVICE);
		registerReceiver(receiver, filter);

	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra(XmppService.DISCONNECT)) {
				Log.d(TAG, "disconnecting");

				Log.d(TAG, "deleting user credentials");
				Config.username = null;
				Config.password = null;

				if(smackAsyncTask!=null)
				try {
					smackAsyncTask.connection.disconnect();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.d(TAG, "disconnected");
				smackAsyncTask = null;

				if (Config.isSupporter) {
					Intent intent2 = new Intent();
					intent2.setAction(XmppService.SEND_TO_ACTIVITY);
					intent2.putExtra(XmppService.OPEN_LOGIN, "true");
					sendBroadcast(intent2);
				}
			}
			
			if (intent.hasExtra(XmppService.CONNECT)) {
				Log.d(TAG, "connecting");
				Log.d(TAG, this.toString());

				// Create the AsyncTask that automatically connects to the
				// server
				// if (smackAsyncTask == null) {
				Log.d(TAG, "created smackAsyncTask");
				smackAsyncTask = new SmackAsyncTask();
				smackAsyncTask.parent = XmppService.this;
				smackAsyncTask.execute();
				// } else {
				Log.d(TAG, smackAsyncTask.toString());
				// }

			}


			if (intent.hasExtra(XmppService.NEED_HELP)) {
				Log.d(TAG, "need help");
				Config.isHelpSeeker = true;
				Config.isSupporter = false;
				Config.username = null;
				Config.password = null;

			}

			if (intent.hasExtra(XmppService.OFFER_HELP)) {
				Log.d(TAG, "need help");
				Config.isSupporter = true;

			}

			if (intent.hasExtra(MESSAGE_FROM_FRAGMENT_CHAT)) {
				try {
					smackAsyncTask.chat.sendMessage(intent
							.getStringExtra(MESSAGE_FROM_FRAGMENT_CHAT));
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (intent.hasExtra(ACCEPT)) {
				onSupporterAccept();
			}

			if (intent.hasExtra(DECLINE)) {
				onSupporterDecline();
			}
			
			if (intent.hasExtra(RESCUED)) {
				onRescued();
			}
			
			if (intent.hasExtra(HELPED)) {
				onHelped();
			}
			
			if (intent.hasExtra(MADEWORSE)) {
				onMadeWorse();
			}
		}
	};

	// brings Activity to front and opens dialog for supporter so that he can
	// choose if he want to take the call or not
	public void openAlert() {

		Log.d(TAG, "openAlert()");
		
		Config.ringing = true;
		
		// Bring Activity to Front
		Intent i = new Intent(getBaseContext(), MainActivity.class);
		i.setAction(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getBaseContext().startActivity(i);

		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		r.play();

		// open chat Fragment on UI
		Intent intent = new Intent();
		intent.setAction(XmppService.SEND_TO_ACTIVITY);
		intent.putExtra(XmppService.OPEN_CALL, "true");
		sendBroadcast(intent);

		Log.d(TAG, "bringing activty to front");
		
		timeout = new Timer();
		timeout.schedule(new TimerTask() {
			@Override
			public void run() {
				onSupporterDecline();
			}
		}, 45000);

	}

	// if supporter accepts the call in the dialog box, the app sands an Ack to
	// the sleek-xmpp server bot
	public void onSupporterAccept() {
		Log.d(TAG, "assigning seeker: " + Config.helpSeeker);
		if (Config.helpSeeker != null) {
			Log.d(TAG, "in class registerd");

			smackAsyncTask.chat = ChatManager.getInstanceFor(
					smackAsyncTask.connection).createChat(Config.helpSeeker,
					smackAsyncTask.cml);
			


			r.stop();

			// set the supporter to unavailable
			
			
			timeout.cancel();
			timeout.purge();
			timeout=null;

		}

		try {
			smackAsyncTask.managementChat
					.sendMessage("SuicidePreventionAppServerSupporterRequestCallingAccept;"
							+ Config.helpSeeker);
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// open chat Fragment on UI
		Intent intent = new Intent();
		intent.setAction(XmppService.SEND_TO_ACTIVITY);
		intent.putExtra(XmppService.OPEN_CHAT, "true");
		sendBroadcast(intent);
		
		try {
			smackAsyncTask.chat.sendMessage("Connected");
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// if supporter declines the call in the dialog box, the app sands an
	// decline message to the sleek-xmpp server bot, the bot should search the
	// next supporter
	public TimerTask onSupporterDecline() {
		// Log.d(TAG, Config.helpSeeker);

		r.stop();

		try {
			smackAsyncTask.managementChat
					.sendMessage("SuicidePreventionAppServerSupporterRequestCallingDecline;"
							+ Config.helpSeeker);
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Config.inSession = false;
		
		Intent intent = new Intent();
		intent.setAction(XmppService.SEND_TO_ACTIVITY);
		intent.putExtra(XmppService.OPEN_HELP, "true");
		sendBroadcast(intent);
		
		timeout.cancel();
		timeout.purge();
		timeout=null;
		
		return null;
	}

	public void onRescued()
	{
		endEvaluate("rescued");
	}
	
	public void onHelped()
	{
		endEvaluate("helped");
	}
	
	public void onMadeWorse()
	{
		endEvaluate("madeworse");
	}
	
	public void endEvaluate(String points)
	{
		try {
			smackAsyncTask.managementChat
					.sendMessage("SuicidePreventionAppServerHelpSeekerEndSession;"
							+ points + ";" + Config.supporter);
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Intent intent3 = new Intent();
		intent3.setAction(XmppService.SEND_TO_SERVICE);
		intent3.putExtra(XmppService.DISCONNECT, "true");
		sendBroadcast(intent3);
		
		Intent intent = new Intent();
		intent.setAction(XmppService.SEND_TO_ACTIVITY);
		intent.putExtra(XmppService.OPEN_HELP, "true");
		sendBroadcast(intent);
		
		Config.supporter = null;
		Config.helpSeeker = null;
		Config.isHelpSeeker = false;
		Config.inSession = false;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}