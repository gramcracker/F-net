package io.underdark.app;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Set;

import io.underdark.app.dialogs.PasswordPrompt;
import io.underdark.app.model.Channel;
import io.underdark.app.model.Node;
import io.underdark.transport.Link;

/**
 * Created by josh on 8/11/18.
 */


public class ChannelAdapter extends RecyclerView.Adapter <ChannelAdapter.ViewHolder> {

    //3 instances of this class will be made (one for each tab)

    //holds the strings for the cards in one tab
    private ArrayList<Channel> myChannels;
    private ArrayList<Channel> allChannels;
    private ArrayList<String> people;
    int tabPosition;
    private Context context;


    public void eventUpdate(Node node ){

        switch(tabPosition){
            case 0:
                myChannels = new ArrayList<>(node.channelsBroadcasting);
                int i = 0;
                for (Channel c : node.channelsListeningTo){
                    if(!myChannels.contains(c)){
                        myChannels.add(c);
                    }
                }
                break;
            case 1:
                people = new ArrayList<>();
                for (Link l : node.links){
                    people.add(String.valueOf(l));
                }
                break;
            case 2:
                people = new ArrayList<>();
                for (Link l : node.links){
                    people.add(String.valueOf(l));
                }
                break;
        }
        allChannels = new ArrayList<>(node.channelsVisible);



    }





    //the interface for clicking a card
    public interface OnItemClickListener {

        void onItemClick(Channel channel, int tabPosition);
    }

    public void setChannelsVisible(Set<Channel> channels){
        allChannels = new ArrayList<>(channels);
    }

    public void updateChannelsVisible(){

        setChannelsVisible(Node.channelsVisible);
    }

    public void setChannelsListening(Set<Channel> channels){
        myChannels = new ArrayList<>(channels);
//        int i = 0;
//        for (Channel c : Node.channelsListeningTo){
//            if(!myChannels.contains(c)){
//                myChannels.add(c);
//            }
//        }
    }

    public void updateChannelsListening(){
        setChannelsListening(Node.channelsListeningTo);
    }


    public void setPeople(Set<Link> links){
        people = new ArrayList<>();
        for (Link l : links){
            people.add(String.valueOf(l));
        }
    }

    public void updatePeople(){
        setPeople(Node.links);
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

    public ChannelAdapter( int tabPosition, OnItemClickListener listener) {

        myChannels = new ArrayList<>();
        allChannels = new ArrayList<>();
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





    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        TextView numListeningView;
        TextView posterView;
        CardView mCardView;
        ImageButton mImageButton;


        ViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.list_card);
            mTextView = itemView.findViewById(R.id.list_card_title);
            posterView = itemView.findViewById(R.id.poster_text_view);
            numListeningView = itemView.findViewById(R.id.listening_count_number);
            mImageButton = itemView.findViewById(R.id.list_image_button);


        }


        void bind(final Channel channel, final int tabPosition, final OnItemClickListener listener) {

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

        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false);

        return new ViewHolder(v);


    }





    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {

        holder.mTextView.setText(String.valueOf(tabPosition));
        final Channel channel;

        switch (tabPosition) {
            case 0:
                channel = myChannels.get(listPosition);
                holder.mTextView.setText(channel.title);
                holder.numListeningView.setText(String.valueOf(channel.usersListening.size()));
                holder.posterView.setText(channel.poster);
                holder.bind(channel, tabPosition, listener);

                holder.mImageButton.setImageResource(R.drawable.ic_remove);
                holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Node.removeChannel(channel);

                    }
                });
                break;

            case 1:
                channel = allChannels.get(listPosition);
                holder.mTextView.setText(channel.title);
                holder.numListeningView.setText(String.valueOf(channel.usersListening.size()));
                holder.posterView.setText(channel.poster);
                holder.bind(channel, tabPosition, listener);
                if(channel.keyRequired){
                    holder.mImageButton.setImageResource(R.drawable.ic_lock);
                    holder.mImageButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {
                           PasswordPrompt prompt = new PasswordPrompt(context, channel);
                           if(!prompt.isShowing()){
                               prompt.show();
                           }

                        }
                    });
                }else{
                    holder.mImageButton.setImageResource(R.drawable.ic_add);
                    holder.mImageButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        Node.addListening(channel);
                    }
                });
                }

                break;
            case 2:
                holder.mTextView.setText(people.get(listPosition));
                //holder.bind(people.get(listPosition), tabPosition, listener);
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

