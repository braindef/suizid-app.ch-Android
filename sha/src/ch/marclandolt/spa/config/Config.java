package ch.marclandolt.spa.config;

import ch.marclandolt.spa.backend.SmackAsyncTask;
import ch.marclandolt.spa.ui.activities.MainActivity;


public class Config {

	public static String servername = "ns3.ignored.ch";
	public static String serverBotJid = "server@ns3.ignored.ch";

	public static String managementUser = "management";
	public static String managementPassword = "password";

	public static String chatPartner = null;

	public static String username = null;
	public static String password = null;

	public static String supporter = null;
	public static String helpSeeker = null;

	public static boolean initialMessageSent = false;
	public static boolean isSupporter = false;
	public static boolean isHelpSeeker = false;

	public static boolean isLoggedIn = false;
	public static boolean Available = true;						//also used when in chat session with a help seeking person
	
	protected static boolean isConnected = false;
	public static boolean free = true;
	public static boolean ringing = false;

}
