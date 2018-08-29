package io.underdark.app.model;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
	private static Node instance = null;
	public static Date currentTime;
	public String username;
	DiskLruCache myMessageCache;
	DiskLruCache myChannelCache;
	File messageCacheFile;
	File channelCacheFile;
	String messageCacheFileName = "message_cache";
	String channelCacheFileName = "channel_cache";


	public Set<String> channelsListeningTo;
	public Set<String> channelsVisible;
	public Set<String> channelsBroadcasting;
	int textColor = Color.WHITE;

	public static class Transmission implements Serializable {
		int color = Color.WHITE;
		String time = "";
		public String channelTo = "";
		public String message = "";
		public String originName  = "";
		public String newChannel = "";
		public long nodeFrom;
		public long nodeTo;

		public enum Type { none, messagesSync, channelsListRequest, channelList, messageList, channelMessage, newChannel, privateMessage  }
		Type type = Type.none;

		ArrayList<String> listData;
		
		Transmission(Type _type){
			type = _type;
			listData = new ArrayList<>();
		}
	}

	
	public Set<Link> links;

	private Node(MainActivity activity) throws IOException {
		//date to tag messages
		//set up cache for messages
//		messageCacheFile = new File(messageCacheFileName);
//		myMessageCache = DiskLruCache.open(messageCacheFile, MainActivity.appVersion, 1, 38400);
//		myChannelCache = DiskLruCache.open(channelCacheFile, MainActivity.appVersion, 1, 38400);
		links = new HashSet<>();
		channelsListeningTo = new HashSet<>();
		channelsVisible= new HashSet<>();
		channelsBroadcasting = new HashSet<>();
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


	public static Node getInstance(MainActivity activity)
	{
		if (instance == null) {
			try {
				instance = new Node(activity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return instance;
	}


	public Set<String> getChannelsListeningTo() {
		return channelsListeningTo;
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

	
	public Set<Link> getLinks()
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
		if (!links.contains(link)){
			for (Link l : links){
				if(l.getNodeId() == link.getNodeId()) return;
			}
			links.add(link);
		}
		activity.refreshPeers();
		try {
			requestChannelLists();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void transportLinkDisconnected(Transport transport, Link link)
	{
		if (links.contains(link)){
			links.remove(link);
		}

		activity.refreshPeers();

		if(links.isEmpty())
		{

			if (Messenger.active){
			refreshFrames();
		}

		}
	}

	private byte [] serialize(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(o);
		return bos.toByteArray();
	}
//
//	private Object deserialize( byte [] frameData ){
//
//	}
	@Override
	public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData)
	{
		Transmission transmission;
		ObjectInput in = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(frameData);
			in = new ObjectInputStream(bis);
			transmission = (Transmission) in.readObject();

			Intent intent = new Intent("mesh_irc_transmission");

			switch(transmission.type){

				case none:
					break;
				case channelMessage:
					//if channel exists in channels listening to:
					//check for @message and private message
					//add message object to cache and message text view if open

					if(channelsListeningTo.contains(transmission.channelTo)){
						intent.putExtra("channelTo", transmission.channelTo);
						intent.putExtra("message", transmission.message);
						intent.putExtra("user", transmission.originName);
						intent.putExtra("time", transmission.time);
						LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
						Log.d("sender", "Broadcasting message");
					}

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
				case channelsListRequest:
					//check if conditions for each channel to be active(if only uses a specific router)
					//send channel list
					long nodeTo = transmission.nodeFrom;
					sendChannelList(nodeTo);


					break;
				case channelList:
					//if transmission.origin = this && waiting for channel list:
					//set waiting for message list = false
					//clear message list
					//add messages to view
					if(transmission.nodeTo == this.nodeId){
						channelsVisible.addAll(transmission.listData);
						Toast.makeText(activity, channelsVisible.toString(), Toast.LENGTH_SHORT).show();
					}

					refreshUI();


					break;

			}



		} catch (IOException e) {
			e.printStackTrace();


		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		/**
		 * When transmission object bytes is recieved, convert it from bytes to 
		 * a transmission object then check its type and handle each type accordingly
		 */
	}

	private void refreshFrames() {


	}



	public void broadcastBytes(byte[] frameData)
	{
		if(links.isEmpty())
			return;

		refreshFrames();

		for(Link link : links){
			link.sendFrame(frameData);
		}


	}

	public static String getTime(){
		SimpleDateFormat dateFormat;
		dateFormat= new SimpleDateFormat("HH:mm");
		return dateFormat.format(Calendar.getInstance().getTime());

	}

	public void broadcastString(String message) throws UnsupportedEncodingException {

		if(message.isEmpty()) return;

		byte[] message_bytes = message.getBytes("UTF-8");

		broadcastBytes(message_bytes);

	}

	public void sendPrivateMessage(long user, String message){
		Transmission t = new Transmission(Transmission.Type.privateMessage);
	}

	public void sendChannelMessage(String channel_to, String message) throws IOException {

		if(message.contentEquals("")) return;

		Transmission t = new Transmission(Transmission.Type.channelMessage);
		t.color = textColor;
		t.message = message;
		t.nodeFrom = nodeId;
		t.originName = username;
		t.channelTo = channel_to;
		t.time =getTime();
		broadcastBytes(serialize(t));
	}


	public void requestChannelLists() throws IOException {
		Transmission t = new Transmission(Transmission.Type.channelsListRequest);
		t.nodeFrom = nodeId;
		broadcastBytes(serialize(t));
	}


	public void sendChannelList(long nodeTo) throws IOException {
		if(!channelsBroadcasting.isEmpty()){
			Transmission t = new Transmission(Transmission.Type.channelList);
			t.nodeTo = nodeTo;
			t.listData.addAll(channelsBroadcasting);
			for(Link l: links){
				if (l.getNodeId() == nodeTo){
					l.sendFrame(serialize(t));
					}
			}
		}


	}

	public void broadcastNewChannel(String channel) throws IOException {
		 if( channelsVisible.contains(channel) ||
				 channelsBroadcasting.contains(channel) || channel == "") return;
		channelsBroadcasting.add(channel);
		Transmission t = new Transmission(Transmission.Type.newChannel);
		t.newChannel = channel;
		broadcastBytes(serialize(t));
	}

	public void newChannel(String channel) throws IOException {
		//add channel to channels listening to.
		//send channel list
		if(channelsListeningTo.contains(channel)||channel == "") return;
		channelsListeningTo.add(channel);
		broadcastNewChannel(channel);
		refreshUI();

	}

	public void setListeningto(String channel){
		channelsListeningTo.add(channel);
		refreshUI();
	}

	public void removeChannel(String channel){
		if(channelsListeningTo.contains(channel)) channelsListeningTo.remove(channel);

		if(channelsBroadcasting.contains((channel))) channelsBroadcasting.remove((channel));

	}

	public void refreshUI(){
		activity.mSectionsPagerAdapter.updateLists();
	}

	//endregion
} // Node
