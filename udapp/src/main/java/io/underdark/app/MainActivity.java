package io.underdark.app;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentPagerAdapter;
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

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Set;
import java.util.Vector;

import io.underdark.app.dialogs.NewChannelDialog;
import io.underdark.app.model.Channel;
import io.underdark.app.model.Node;
import io.underdark.app.dialogs.UsernameDialog;
import io.underdark.transport.Link;


public class MainActivity extends AppCompatActivity
{
    public SharedPreferences preferences;
	//public static Node node;
	Intent nodeIntent;

	private TextView peersTextView;
	DrawerLayout mDrawerLayout;

	public tabPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	NavigationView navigationView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//create singleton node

        preferences = getSharedPreferences("MAIN_PREFERENCES", MODE_PRIVATE);
        //start the node service

		peersTextView = findViewById(R.id.peersTextView);
		mDrawerLayout = findViewById(R.id.drawer_layout);

		//handle the toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

		EventBus.getDefault().register(this);

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

		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				int id = item.getItemId();
				switch (id){
					case R.id.username:
						UsernameDialog usernameDialog = UsernameDialog.getInstance(MainActivity.this);
						if(!usernameDialog.isShowing()){
							usernameDialog.show();
						}
						break;
					case R.id.textColor:
						pickColor();

				}
				return false;
			}
		});


        nodeIntent = new Intent(this, Node.class);
        startService(nodeIntent);

	}




	@Subscribe(threadMode = ThreadMode.MAIN)
	public void refreshPeers(Set<Link> links)
	{
		peersTextView.setText(links.size() + " connected");
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

			    Node.requestChannelLists();

				mSectionsPagerAdapter.notifyDataSetChanged();
				break;

			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.stop:
				stopService(nodeIntent);
				break;
			case R.id.start:
				startService(nodeIntent);
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

		@Override
		public void onDestroy() {
			super.onDestroy();

			EventBus.getDefault().unregister(this);
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
			mAdapter = new ChannelAdapter( tabPosition, new ChannelAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(Channel channel, int tabPosition) {
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


		@Subscribe
		public void onEventChannels(Set<Channel> channels) {
			Toast.makeText(this.getContext(),"new chnnels!", Toast.LENGTH_LONG).show();
			if (channels.equals(Node.channelsListeningTo)) {
				mAdapter.setChannelsListening(channels);
			}else if(channels.equals(Node.channelsVisible)){
				mAdapter.setChannelsVisible(channels);
			}
			mAdapter.notifyDataSetChanged();
		}

		@Subscribe
		public void onEventPeople(Set<Link> links){
			mAdapter.setPeople(links);
			mAdapter.notifyDataSetChanged();

		}
	}






	public static class tabPagerAdapter extends FragmentPagerAdapter {

		private Vector<tabFragment> pages;



		private tabPagerAdapter(FragmentManager fm) {
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

		public void eventUpdate(Node node){
			for(tabFragment p : pages){
				p.mAdapter.eventUpdate(node);
			}
		}

	}

	public void pickColor(){
		ColorPickerDialogBuilder
				.with(this)
				.setTitle("Choose color")
				.initialColor(Node.textColor)
				.wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
				.density(12)
				.setOnColorSelectedListener(new OnColorSelectedListener() {
					@Override
					public void onColorSelected(int selectedColor) {

					}
				})
				.setPositiveButton("ok", new ColorPickerClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
						Node.textColor = selectedColor;
					}
				})
				.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.build()
				.show();
	}


} // MainActivity

