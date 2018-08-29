package io.underdark.app;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Vector;

import io.underdark.app.model.Node;


public class MainActivity extends AppCompatActivity
{
    SharedPreferences preferences;
	public static String appId = "DewDrop";
	public static int appVersion = 1;
	public static Node node;
	public static boolean active = false;
	private TextView peersTextView;
	DrawerLayout mDrawerLayout;
	BroadcastReceiver mMessageReceiver;


	public tabPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
		peersTextView = findViewById(R.id.peersTextView);
		mDrawerLayout = findViewById(R.id.drawer_layout);

		//handle the toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


		//VIEW PAGER-------

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		//the pagerAdapter is the class that controls the view pager
		mSectionsPagerAdapter = new tabPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		//View pager is the ui element
		mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		//the specific layout for the viewpager pages
		TabLayout tabLayout = findViewById(R.id.tabs);
		//set up viewpager to be controlled by the tabs and vice versa
		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));



		//create singleton node
		node = Node.getInstance(this);
		if(node!=null){
			//set up node from shared preferences
		    node.username = preferences.getString("user_name","null");
        }


        //request list of broadcasting channels from every connected link
		try {
			node.requestChannelLists();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents
		// with actions named "custom-event-name".

		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter("mesh_irc_transmission"));

	}




	@Override
	protected void onStart()
	{
		super.onStart();
		this.active = true;
		node.start();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		this.active = false;
	}

	public void refreshPeers()
	{
		peersTextView.setText(node.getLinks().size() + " connected");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch(id){
			case R.id.action_settings:
				Intent intent = new Intent(getApplicationContext() , SettingsActivity.class);
				startActivity(intent);
				break;

			case R.id.refresh:
				try {
					node.requestChannelLists();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mSectionsPagerAdapter.notifyDataSetChanged();
				break;

			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.stop:
				node.stop();
				break;
			case R.id.start:
				node.start();
		}
		//noinspection SimplifiableIfStatement


		return super.onOptionsItemSelected(item);
	}

	public void addChannel(View view) {
		NewChannelDialog newChannelDialogue = new NewChannelDialog(this);
		newChannelDialogue.show();
	}






	public static class tabFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

		public static final String ARG_OBJECT = "object";
		public int tabPosition=0;
		RecyclerView mRecyclerView;
		RecyclerView.LayoutManager mLayoutManager;
		ChannelAdapter mAdapter;
		SwipeRefreshLayout mSwipeRefreshLayout;



		public void updateData(){
			mAdapter.retrieveData();
			mAdapter.notifyDataSetChanged();
			mSwipeRefreshLayout.setRefreshing(false);
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			savedInstanceState = getArguments();
			tabPosition = savedInstanceState.getInt(ARG_OBJECT);

		}


		@Nullable
		@Override
		public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

			final View v = inflater.inflate(R.layout.page_main, container, false);
			mSwipeRefreshLayout = v.findViewById(R.id.SwipeRefresher);
			mSwipeRefreshLayout.setOnRefreshListener(this);
			mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_accent));


			mRecyclerView = v.findViewById(R.id.channel_list);
			mRecyclerView.setHasFixedSize(true);
			mLayoutManager = new LinearLayoutManager(this.getContext());
			mRecyclerView.setLayoutManager(mLayoutManager);
			mAdapter = new ChannelAdapter(tabPosition, new ChannelAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(String channel, int tabPosition) {
					switch (tabPosition) {
						case 0:
							Intent intent = new Intent(getActivity(), Messenger.class);
							intent.putExtra("currentChannel", channel);
							getActivity().startActivity(intent);
							break;
						case 1:
							//todo: display channel information probably
							break;
						case 2:
							//request a persons
							//open information about person
							//and give the option to request private message
							//recieving person is given the option to start a conversation, deny, or block sender.
							break;
					}

				}
			});

			mRecyclerView.setAdapter(mAdapter);
			return v;
		}


		@Override
		public void onRefresh() {
			updateData();
		}
	}






	public static class tabPagerAdapter extends FragmentPagerAdapter {

		private Vector<tabFragment> pages;


		public tabPagerAdapter(FragmentManager fm) {
			super(fm);
			pages = new Vector<>();
		}


		@Override
		public Fragment getItem(int position) {
			Fragment fragment = new tabFragment();
			Bundle args = new Bundle();
			args.putInt(tabFragment.ARG_OBJECT, position);
			fragment.setArguments(args);
			pages.add((tabFragment) fragment);
			return fragment;
		}


		@Override
		public int getCount() {
			return 3;
		}

		public void updateLists() {
			for (tabFragment f : pages) {
				f.updateData();
			}
		}
	}


} // MainActivity

