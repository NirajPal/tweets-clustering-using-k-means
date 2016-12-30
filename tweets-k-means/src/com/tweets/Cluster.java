package com.tweets;

import java.util.ArrayList;

public class Cluster {
	
	
	
	
	ArrayList<TweetData> tweetList;
	TweetData centroid;
	
	Cluster()
	{
		tweetList=new ArrayList<TweetData>();
	}
	
	public ArrayList<TweetData> getTweetList() {
		return tweetList;
	}
	public void setTweetList(ArrayList<TweetData> tweetList) {
		this.tweetList = tweetList;
	}
	public TweetData getCentroid() {
		return centroid;
	}
	public void setCentroid(TweetData centroid) {
		this.centroid = centroid;
	}
	
}
