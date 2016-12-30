package com.tweets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class TweetsTest {
	
	
	static ArrayList<String> seeds=new ArrayList<String>();
	static ArrayList<TweetData> tweetList=new ArrayList<TweetData>();
	static Map<String,Cluster> clusters=new HashMap<String,Cluster>();
	
	public static void main(String args[]) {
		
		if(args.length != 4){
			System.out.println("Invalid Arguments");
			System.exit(0);
		}
		
		int numberOfClusters=Integer.parseInt(args[0]);
		String initialSeedsFile =args[1];
		String TweetsDataFile=args[2];
		String outputFile=args[3];
		
		File file=new File("data/input");
		file.mkdirs();
		
		if(numberOfClusters>25)
			numberOfClusters=25;
		
		init(TweetsDataFile);
		initializeCentroids(initialSeedsFile,numberOfClusters);
		clusterTweets();
		computeCluster();
		printResults(outputFile);
	}
	
	
	public static void init(String fileName)
	{
		JSONParser parser = new JSONParser();
		try {

			FileReader fr=new FileReader("data/input/"+fileName);
			
			BufferedReader br=new BufferedReader(fr);
			String sCurrentLine="";
			String tweets="[";
			
			while ((sCurrentLine = br.readLine()) != null) {
				tweets += sCurrentLine + "," + "\n";
			}
			tweets+="]";
			
			JSONArray jsonArray = (JSONArray) parser.parse(tweets);
			for(Object obx : jsonArray)
			{
				JSONObject tweet = (JSONObject) obx;
				TweetData tweetData=new TweetData();
				tweetData.setText(String.valueOf(tweet.get("text")));
				tweetData.setId(String.valueOf(tweet.get("id")));
				tweetList.add(tweetData);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	
	
	public static void initializeCentroids(String fileName,int k)
	{
		try {
			
			FileReader fr=new FileReader("data/input/"+fileName);
			BufferedReader br=new BufferedReader(fr);
			int count=0;
			String sCurrentLine="";
			while ((sCurrentLine = br.readLine()) != null) {
				
				String seed=sCurrentLine.replaceAll(",", "");
				seeds.add(seed);
			}
			
			for(TweetData tweet : tweetList)
			{
				
				if(seeds.contains(tweet.getId()))
				{
					if(count<=k){
						Cluster tweetCluster=new Cluster();
						tweetCluster.setCentroid(tweet);
						clusters.put(tweet.getText(),tweetCluster);
						count++;
					}
				}
					
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void clusterTweets()
	{
		
		for(TweetData tweet: tweetList)
		{
			//System.out.println();
			Cluster cluster = null;
			double MAXDISTANCE = Double.MAX_VALUE;
			
			for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {
				
				Cluster currentCluster=entry.getValue();
				TweetData centroid=currentCluster.getCentroid();
				double distance=computeJaccardDistance(tweet,centroid);	
				if(distance<MAXDISTANCE)
				{
					MAXDISTANCE=distance;
					cluster=currentCluster;
				}
			}
			ArrayList<TweetData> tweetList=cluster.getTweetList();
			tweetList.add(tweet);
		}
	}


	private static double computeJaccardDistance(TweetData tweet, TweetData centroid) {
		
		String[] tweetText=processTweet(tweet.getText());
		String[] centroidText=processTweet(centroid.getText());
		
		HashSet<String> tweetSet=new HashSet<String>();
		HashSet<String> intersectionSet=new HashSet<String>();
		HashSet<String> unionSet=new HashSet<String>();
		
		for(String t: tweetText)
		{
			tweetSet.add(t);
		}
		
		for(String c: centroidText)
		{
			if(tweetSet.contains(c))
			{
				intersectionSet.add(c);
			}
		}
		
		unionSet.addAll(Arrays.asList(tweetText));
		unionSet.addAll(Arrays.asList(centroidText));
		
		
		double ratio=(double)intersectionSet.size()/(double)unionSet.size();
		
		return 1.0-ratio;
	}


	private static String[] processTweet(String text) {
		
		String[] processedTweet=text.split(" ");
		
		for(int i=0;i<processedTweet.length;i++){
			processedTweet[i]=processedTweet[i].replaceAll("[^\\w]","").trim();
			processedTweet[i]=processedTweet[i].replaceAll("\\n"," ").trim();
			processedTweet[i]=processedTweet[i].replaceAll("[^a-zA-Z0-9 -]", "");
		}
		
		return processedTweet;
	}
	
    public static void computeCluster(){
		
		boolean flag = false;
		
		do {
			
			flag = false;
			for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {

				Cluster cluster = entry.getValue();
				if (recomputeCentroid(cluster)) {
					flag = true;
				}
			}
			if (flag) {

				for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {

					entry.getValue().getTweetList().clear();
				}
				clusterTweets();
			}
		} while (flag);
		
	}
	
	
	public static boolean recomputeCentroid(Cluster cluster){
		TweetData newCentroid=null;
		double MAXDISTANCE=Double.MAX_VALUE;		
		for(TweetData tweet: cluster.getTweetList())
		{
			double distance=0.0;
			for(TweetData tweetTmp: cluster.getTweetList())
			{
				distance += computeJaccardDistance(tweet, tweetTmp);	
			}
			if(distance < MAXDISTANCE)
			{	
				MAXDISTANCE = distance;
				newCentroid = tweet;
			}
		}
		if(!cluster.getCentroid().getId().equalsIgnoreCase(newCentroid.getId()))
		{
			cluster.setCentroid(newCentroid);
			return true;
		}		
		return false;
	}
	
	public static double calculateSSE(){
		
		double sse = 0.0;
		for (Map.Entry<String, Cluster> entry : clusters.entrySet()){
	    	
			
			TweetData centroid = entry.getValue().getCentroid();  
	    	for(TweetData tweet: entry.getValue().getTweetList()){
	    		
	    		double distance = computeJaccardDistance(tweet, centroid);
	    		sse+= Math.pow(distance, 2);
	    	    	
	    	}
	    	
	    }
		System.out.println("SSE :: "+sse);
		return sse;
		
	}
	
	
	public static void printResults(String fileName)
	{
		
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			//String content = "This is the content to write into file\n";

			fw = new FileWriter("data/input/"+fileName);
			bw = new BufferedWriter(fw);
			String content="";
			for (Map.Entry<String, Cluster> entry : clusters.entrySet()){
				
				Cluster cluster=entry.getValue();
				content=content+cluster.getCentroid().getId()+" "+" ";
				for (TweetData tweet: cluster.getTweetList()){
					
					content=content+tweet.getId()+",";
				}
				content=content+"\n";
				
			}
			
			content=content+"\nSSE="+calculateSSE();
			
			bw.write(content);
			bw.close();
			fw.close();
			
		} catch (IOException e) {

			e.printStackTrace();

		}
		
		
		
		
	}
	
	
	
		
}
