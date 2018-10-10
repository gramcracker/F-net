package io.underdark.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.underdark.transport.Link;

public class Channel implements Serializable {
    public String title = "";
    public String info = "";
    public String poster = "";
    long posterId = 0;
    public ArrayList<Transmission> recentMessages;
    public Set<String> usersListening;
    public boolean keyRequired = false;
    public String key;
    int id = 0;

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        Channel testCase = (Channel) obj;
        if(testCase.title.contentEquals(this.title) &&
                testCase.info.contentEquals(this.info) &&
                testCase.posterId == this.posterId){
            //title, info, and poster must match
            isEqual = true;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public Channel(String title, String poster){
        this.title = title;
        this.poster = poster;
        recentMessages = new ArrayList<>();
        usersListening = new HashSet<>();
    }

}
