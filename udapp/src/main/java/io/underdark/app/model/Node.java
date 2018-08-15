package io.underdark.app.model;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.jakewharton.disklrucache.DiskLruCache;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Random;

import io.underdark.Underdark;
import io.underdark.app.MainActivity;
import io.underdark.app.Messenger;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;
import io.underdark.util.nslogger.NSLogger;
import io.underdark.util.nslogger.NSLoggerAdapter;


public class Node implements TransportListener
{
	SharedPreferences preferences;
	private boolean running;
	private MainActivity activity;
	private long nodeId;
	private Transport transport;
	String receivedData = "";
	private Date currentTime;
	SimpleDateFormat dateFormat;
	public String username;
	DiskLruCache messageCache;
	File messageCacheFile;
	String messageCacheFileName = "message_cache";
	int textColor = Color.WHITE;

	public String getMessage() {
		return  receivedData;
	}

	public static class Transmission{
		int color = Color.WHITE;
		String time = "";
		public String channel = "";
		public String message = "";
		public String originName  = "";
		public long originId;
		public String destination = "";
		public enum Type { none, messagesSync, channelsSync, channelList, messageList, channelMessage, privateMessage  }
		Type type = Type.none;
		
		Transmission(Type _type){
			type = _type;
			
		}
	}
	
	private ArrayList<Link> links = new ArrayList<>();
	private int framesCount = 0;

	public Node(MainActivity activity) throws IOException {
		//date to tag messages
		getSettings();
		dateFormat= new SimpleDateFormat("HH:mm");
		//set up cache for messages
		messageCacheFile = new File(messageCacheFileName);
		messageCache = DiskLruCache.open(messageCacheFile, MainActivity.appVersion, 1, 38400);
		
		this.activity = activity;

		do
		{
			nodeId = new Random().nextLong();
		} while (nodeId == 0);

		if(nodeId < 0)
			nodeId = -nodeId;

		configureLogging();

		EnumSet<TransportKind> kinds = EnumSet.of(TransportKind.BLUETOOTH, TransportKind.WIFI);
		//kinds = EnumSet.of(TransportKind.WIFI);
		//kinds = EnumSet.of(TransportKind.BLUETOOTH);

		this.transport = Underdark.configureTransport(
				234235,
				nodeId,
				this,
				null,
				activity.getApplicationContext(),
				kinds
		);


	}

	private void getSettings() {
		preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		username = preferences.getString("user_name","null");

	}

	private void configureLogging()
	{
		NSLoggerAdapter adapter = (NSLoggerAdapter)
				StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger(Node.class.getName());
		adapter.logger = new NSLogger(activity.getApplicationContext());
		adapter.logger.connect("192.168.5.203", 50000);

		Underdark.configureLogging(true);
	}

	/**
	 * Start listening for bytes
	 */
	public void start()
	{
		if(running)
			return;

		running = true;
		transport.start();
	}

	/**
	 * Stop listening for bytes
	 */
	public void stop()
	{
		if(!running)
			return;

		running = false;
		transport.stop();
	}

	
	public ArrayList<Link> getLinks()
	{
		return links;
	}


	//region TransportListener
	@Override
	public void transportNeedsActivity(Transport transport, ActivityCallback callback)
	{
		callback.accept(activity);
	}

	@Override
	public void transportLinkConnected(Transport transport, Link link)
	{
		links.add(link);
		activity.refreshPeers();
	}

	@Override
	public void transportLinkDisconnected(Transport transport, Link link)
	{
		links.remove(link);
		activity.refreshPeers();

		if(links.isEmpty())
		{
			framesCount = 0;
			if (Messenger.active){
			Messenger.refreshFrames();
		}

		}
	}

	
	@Override
	public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData)
	{
		Transmission transmission = SerializationUtils.deserialize(frameData);

		/**
		 * When transmission object bytes is recieved, convert it from bytes to 
		 * a transmission object then check its type and handle each type accordingly
		 */
		
		switch(transmission.type){
			case none:
				break;
			case channelMessage:
				//if channel exists in channels listening to:
				//check for @message and private message
				//add message object to cache and message text view if open
				receivedData = formatMessage(transmission.message, transmission.originName);

				break;
			case messagesSync:
				//get origin
				//check current channel matches channel from
				
				break;
			case messageList:
				//if transmission.origin = this && waiting for message list:
				//set waiting for message list = false
				//clear message list
				//add messages to view
				break;
			case channelsSync:
				//check if conditions for each channel to be active
				//send channel list
				break;
			case channelList:
				//if transmission.origin = this && waiting for channel list:
				//set waiting for message list = false
				//clear message list
				//add messages to view
				break;

		}



		activity.refreshFrames();
	}

	public String formatMessage(String message, String origin){

		//todo: change to get date from transmission object
		Date currentTime = Calendar.getInstance().getTime();
		String m = dateFormat.format(currentTime)+" "
				+origin+": "
				+message+"\n";
		return m;
	}


	public void broadcastBytes(byte[] frameData)
	{
		if(links.isEmpty())
			return;

		activity.refreshFrames();

		for(Link link : links){
			link.sendFrame(frameData);
		}


	}

	public void broadcastString(String message) throws UnsupportedEncodingException {

		if(message.isEmpty()) return;

		byte[] message_bytes = message.getBytes("UTF-8");

		broadcastBytes(message_bytes);

	}

	public void sendPrivateMessage(long user, String message){
		Transmission t = new Transmission(Transmission.Type.privateMessage);
	}

	public void sendChannelMessage(String channel, String message){
		Transmission t = new Transmission(Transmission.Type.channelMessage);
		t.color = textColor;
		t.message = message;
		t.originId = nodeId;
		t.channel = channel;
		t.time = dateFormat.format(currentTime);
		broadcastBytes(SerializationUtils.serialize(t.getClass()));
	}



	public void requestChannelLists(){
		byte [] channelListRequest = (MainActivity.appId + "_request_channels").getBytes();
	}
	//endregion
} // Node
