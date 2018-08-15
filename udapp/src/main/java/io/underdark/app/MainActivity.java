package io.underdark.app;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.IOException;

import io.underdark.app.model.Node;


public class MainActivity extends AppCompatActivity
{

	public static String appId = "DewDrop";
	public static int appVersion = 1;
	public static Node node;
	public static boolean active = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		ViewPager mViewPager = findViewById(R.id.container);

		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		try {
			node = new Node(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
				break;
			case R.id.exit:
				node.stop();
				break;
		}
		//noinspection SimplifiableIfStatement


		return super.onOptionsItemSelected(item);
	}


	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private RecyclerView mRecyclerView;
		private RecyclerView.Adapter mAdapter;
		private RecyclerView.LayoutManager mLayoutManager;
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";


		public PlaceholderFragment() {

		}

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {

			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			/**
			 * Add the channel list to each tabs fragment
			 */

			mRecyclerView = rootView.findViewById(R.id.channel_list);
			mRecyclerView.setHasFixedSize(true);
			mLayoutManager = new LinearLayoutManager(getContext());
			mRecyclerView.setLayoutManager(mLayoutManager);

			// specify an adapter (see also next example)
			mAdapter = new ChannelAdapter();
			mRecyclerView.setAdapter(mAdapter);
			return rootView;

		}
	}


	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}
	}


} // MainActivity

