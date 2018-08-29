package io.underdark.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import io.underdark.transport.Link;

import static io.underdark.app.MainActivity.node;

/**
 * Created by josh on 8/11/18.
 */

public class ChannelAdapter extends RecyclerView.Adapter <ChannelAdapter.ViewHolder> {


    //3 instances of this class will be made (one for each tab)

    //holds the strings for the cards in one tab
    private ArrayList<String> myChannels;
    private ArrayList<String> allChannels;
    private ArrayList<String> people;
    int tabPosition;

    //the interface for clicking a card
    public interface OnItemClickListener {
        void onItemClick(String channel, int tabPosition);
    }

    public void updateChannelsVisible(){
        allChannels = new ArrayList<>(node.channelsVisible);
    }

    public void updateChannelsListening(){
        myChannels = new ArrayList<>(node.channelsListeningTo);
    }

    public void updatePeople(){
        people = new ArrayList<>();
        for (Link l : node.links){
            people.add(String.valueOf(l));
        }
    }

    public void retrieveData(){
        switch(tabPosition){
            case 0:
                updateChannelsListening();
                break;
            case 1:
                updateChannelsVisible();
                break;
            case 2:
                updatePeople();
                break;
        }
    }

    private final OnItemClickListener listener;

    public ChannelAdapter(int tabPosition, OnItemClickListener listener) {

        allChannels = new ArrayList<>();
        myChannels = new ArrayList<>();
        people = new ArrayList<>();

        retrieveData();

        this.tabPosition = tabPosition;

        switch (this.tabPosition) {
            case 0:

                break;
            case 1:
                break;
            case 2:
                break;
        }

        this.listener = listener;


    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public CardView mCardview;
        public ImageButton mImageButton;


        public ViewHolder(View itemView) {
            super(itemView);
            mCardview = itemView.findViewById(R.id.list_card);
            mTextView = itemView.findViewById(R.id.list_card_title);
            mImageButton = itemView.findViewById(R.id.list_image_button);


        }


        public void bind(final String channel, final int tabPosition, final OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(channel, tabPosition);

                }
            });




        }

    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ChannelAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {


        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list_item, parent, false);

        return new ViewHolder(v);


    }




    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {

        holder.mTextView.setText(String.valueOf(tabPosition));

        switch (tabPosition) {
            case 0:
                holder.mTextView.setText(myChannels.get(listPosition));
                holder.bind(myChannels.get(listPosition), tabPosition, listener);

                holder.mImageButton.setImageResource(R.drawable.ic_remove);
                holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        node.removeChannel(myChannels.get(listPosition));
                        notifyItemRemoved(listPosition);
                        //notifyItemRangeChanged(0, myChannels.size());
                        notifyDataSetChanged();

                    }
                });
                break;

            case 1:
                holder.mTextView.setText(allChannels.get(listPosition));
                holder.bind(allChannels.get(listPosition), tabPosition, listener);
                holder.mImageButton.setImageResource(R.drawable.ic_add);
                holder.mImageButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        node.setListeningto(allChannels.get(listPosition));
                        notifyItemInserted(myChannels.size());
                        notifyDataSetChanged();
                    }
                });
                break;
            case 2:
                holder.mTextView.setText(people.get(listPosition));
                holder.bind(people.get(listPosition), tabPosition, listener);
                holder.mImageButton.setImageResource(R.drawable.ic_remove);
                break;
        }


    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        int count = 0;
        switch (tabPosition) {
            case 0:
                count = myChannels.size();
                break;
            case 1:
                count = allChannels.size();
                break;
            case 2:
                count= people.size();
                break;
        }
        return count;
    }
}

