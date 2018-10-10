package io.underdark.app.model;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.os.IBinder;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import org.slf4j.impl.StaticLoggerBinder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import io.underdark.Underdark;
import io.underdark.app.MainActivity;
import io.underdark.app.R;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;
import io.underdark.util.nslogger.NSLogger;
import io.underdark.util.nslogger.NSLoggerAdapter;

import static android.support.constraint.Constraints.TAG;
import static io.underdark.app.dialogs.NodeServiceNotification.CHANNEL_ID;


public class Node extends Service implements TransportListener
{


	static EventBus eventBus;
	SharedPreferences preferences;
	private boolean running;
	public static long nodeId;
	private Transport transport;
	public static String username;

	public static Set<Channel> channelsListeningTo;
	public static Set<Channel> channelsVisible;
	public static Set<Channel> channelsBroadcasting;
	public static Set<Link> links;
	public static int textColor = Color.WHITE;


	@Override
	public boolean equals(Object obj) {
		if(obj.getClass()!=Node.class)return false;
		Node n = (Node)obj;
		boolean isEqual = false;
		if(n.nodeId == this.nodeId &&
				n.username.contentEquals(this.username)) isEqual = true;

		return isEqual;
	}

	//-----------------------------------------------
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {
		//set up cache for messages
		//sets up the channels from saved channels
		eventBus = EventBus.getDefault();

		getCachedData();

		channelsVisible = new HashSet<>();
		links = new HashSet<>();


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
				getApplicationContext(),
				kinds
		);

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentIntent(pendingIntent)
				.setContent( new RemoteViews(this.getPackageName(), R.layout.service_notification))
				.build();

		startForeground(1, notification);

		//do heavy work on a background thread
		//stopSelf();



		requestChannelLists();
	}

	@Override
	public void onDestroy() {
		cacheData();
		super.onDestroy();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		start();
		//return super.onStartCommand(intent, flags, startId);
		return START_NOT_STICKY;
	}





	private void configureLogging()
	{
		NSLoggerAdapter adapter = (NSLoggerAdapter)
				StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger(Node.class.getName());
		adapter.logger = new NSLogger(getApplicationContext());
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


    @Override
    public void transportNeedsActivity(Transport transport, ActivityCallback activityCallback) {

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

		eventBus.post(links);

		requestChannelLists(link);

	}

	@Override
	public void transportLinkDisconnected(Transport transport, Link link)
	{
		if (links.contains(link)){
			links.remove(link);
		}

		eventBus.post(links);

	}

	private static byte [] serialize(Object o) throws IOException {
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
		ObjectInput in;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(frameData);
			in = new ObjectInputStream(bis);
			transmission = (Transmission) in.readObject();

			switch(transmission.type){

				case none:
					break;
				case channelMessage:
					//todo change this to add transmission to channelListeningtTO's transmission arraylist then broadcast the last message to messenger
					//if channel exists in channels listening to:
					//check for @message and private message
					//add message object to cache and message text view if open
					for (Channel c : channelsListeningTo){
						if (c.equals(transmission.channelTo)){
							c.recentMessages.add(transmission);
							eventBus.post(transmission);
						}
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
					if(transmission.nodeTo == nodeId){
						channelsVisible.addAll(transmission.channelList);
						eventBus.post(channelsVisible);
					}
					break;
				case requestUnlock:
                    for(Channel c : channelsBroadcasting){
						if(transmission.channelTo.key.contentEquals(c.key)) {
							Transmission response = new Transmission(Transmission.Type.acceptUnlock);
							response.channelTo = c;
							response.nodeFrom = nodeId;
							response.nodeTo = transmission.nodeFrom;
							Link l = getLink(transmission.nodeFrom);
							if (l!=null){
								sendPrivateTransmission(response, l);
							}
							break;
						}

					}
				case acceptUnlock:
                    Toast.makeText(this.getApplicationContext(),"channel unlocked", Toast.LENGTH_LONG).show();

                    addListening(transmission.channelTo);

					break;

				case newChannel:
					channelsVisible.add(transmission.broadcastingChannel);
					eventBus.post(channelsVisible);

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




	public static void broadcastBytes(byte[] frameData)
	{
		if(links.isEmpty())
			return;

		for(Link link : links){
			link.sendFrame(frameData);
		}


	}

	public static String getTime(){
		SimpleDateFormat dateFormat;
		dateFormat= new SimpleDateFormat("hh:mm a");
		return dateFormat.format(Calendar.getInstance().getTime());

	}



	public void sendPrivateMessage(Link link, String message){
		Transmission t = new Transmission(Transmission.Type.privateMessage);
		t.message = message;
		t.nodeFrom = nodeId;
		t.nodeTo = link.getNodeId();
		sendPrivateTransmission( t, link);
	}

	private static void sendPrivateTransmission(Transmission t, Link link){
		try {
			link.sendFrame(serialize(t));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendChannelMessage(Channel channel_to, String message) throws IOException {

		if(message.contentEquals("")) return;

		Transmission t = new Transmission(Transmission.Type.channelMessage);
		t.color = textColor;
		t.message = message;
		t.nodeFrom = nodeId;
		t.originName = username;
		t.channelTo = channel_to;
		t.time =getTime();
		for(Channel c: channelsListeningTo){
			if (c.equals(channel_to)){
				c.recentMessages.add(t);
			}
		}
		broadcastBytes(serialize(t));
	}


	public static void requestChannelLists() {
		Transmission t = new Transmission(Transmission.Type.channelsListRequest);
		t.nodeFrom = nodeId;
		try {
			broadcastBytes(serialize(t));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void requestChannelLists(Link link){
		Transmission t = new Transmission(Transmission.Type.channelsListRequest);
		t.nodeFrom = nodeId;
		sendPrivateTransmission(t, link);

	}

	public static Link getLink(long node){
		for(Link l: links){
			if (l.getNodeId() == node){
				return l;
			}
		}
		return null;
	}


	public void sendChannelList(long nodeTo) {
		if(!channelsBroadcasting.isEmpty()) {
			Transmission t = new Transmission(Transmission.Type.channelList);
			t.nodeTo = nodeTo;
			t.channelList.addAll(channelsBroadcasting);
			Link l = getLink(nodeTo);
			if (l != null) {
				try {
					l.sendFrame(serialize(t));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void broadcastNewChannel(Channel channel) throws IOException {
		 if( channelsVisible.contains(channel) ||
				 channelsBroadcasting.contains(channel) || channel == null) return;
		channelsBroadcasting.add(channel);
		Transmission t = new Transmission(Transmission.Type.newChannel);
		t.broadcastingChannel = channel;
		broadcastBytes(serialize(t));
	}

	public static void setNewChannel(Channel channel) throws IOException {
		//add channel to channels listening to.
		//send channel list
		if(channelsListeningTo.contains(channel)||channel == null) return;
		channel.posterId = nodeId;
		channel.poster = username;
		addListening(channel);
		broadcastNewChannel(channel);


	}

	public static void addListening(Channel channel){
		channel.usersListening.add(username);
		channelsListeningTo.add(channel);
		eventBus.post(channelsListeningTo);
	}

	public static void removeChannel(Channel channel){
		if(channelsListeningTo.contains(channel)) channelsListeningTo.remove(channel);
		if(channelsBroadcasting.contains((channel))) channelsBroadcasting.remove((channel));
		eventBus.post(channelsVisible);

	}



	public void cacheData(){
        preferences = this.getSharedPreferences("NODE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		Gson gson = new Gson();
		String broadcasting = gson.toJson(channelsBroadcasting);
		editor.putString("CHANNELS_BROADCASTING", broadcasting);
		String listening = gson.toJson(channelsListeningTo);
		editor.putString("CHANNELS_LISTENING", listening);
        Log.e(TAG," from node.cacheData broadcasting-> "+channelsBroadcasting .toString());
        Log.e(TAG," from node.cacheData listening-> "+channelsListeningTo .toString());
        editor.apply();
	}

	public void getCachedData(){
		preferences = this.getSharedPreferences("NODE_PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
		username = preferences.getString("USERNAME","null");
		String broadcasting = preferences.getString("CHANNELS_BROADCASTING", null);
        String listening = preferences.getString("CHANNELS_LISTENING", null);
        Type type = new TypeToken<Set<Channel>>() {}.getType();
        channelsBroadcasting = gson.fromJson(broadcasting,type);
        channelsListeningTo = gson.fromJson(listening,type);
        if(channelsBroadcasting == null) channelsBroadcasting = new HashSet<>();
        if(channelsListeningTo == null ) channelsListeningTo = new HashSet<>();
        Log.e(TAG," from node.getCacheData broadcasting-> "+channelsBroadcasting .toString());
        Log.e(TAG," from node.getCacheData listening-> "+channelsListeningTo .toString());


    }

	public static void unlock(Channel channel) {
		Link link = getLink(channel.posterId);
		if(link!= null){
			Transmission t = new Transmission(Transmission.Type.requestUnlock);
			t.channelTo = channel;
			t.nodeFrom = nodeId;
			t.nodeTo = channel.posterId;
			sendPrivateTransmission(t, link);
		}

	}


	//endregion
} // Node
