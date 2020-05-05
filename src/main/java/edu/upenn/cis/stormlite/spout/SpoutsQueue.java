package edu.upenn.cis.stormlite.spout;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.crawler.StormliteCrawler;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;

public class SpoutsQueue implements IRichSpout {

	//log to record 
	static final Logger log = LogManager.getLogger(SpoutsQueue.class);
	
    String executorIdStr = UUID.randomUUID().toString();
    private static Map<String, String> configuration;
    SpoutOutputCollector spoutOutCollector;
    private static LinkedBlockingQueue<String> urlQueue;
    public static String seedUrl;
	@Override
	public String getExecutorId() {
		// TODO Auto-generated method stub
		return executorIdStr;
	}
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("url"));
		
	}
	@Override
	public void open(Map<String, String> configuration, TopologyContext topo, SpoutOutputCollector spoutOutcollector) {
		// TODO Auto-generated method stub
		this.spoutOutCollector = spoutOutcollector;
        this.configuration = configuration;
		
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		  urlQueue.clear();
		
	}
	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		 // System.out.println("Next one------!");
        if(!urlQueue.isEmpty()){
            String nextUrl =  urlQueue.poll();
            if(nextUrl != null){
                System.out.println(nextUrl + " is dequeued from urlQueue in SpoutsQueue");
                StormliteCrawler.incBusy();
                this.spoutOutCollector.emit(new Values<Object>(nextUrl));
            }
        }
        //System.out.println("should yield");
        Thread.yield();
		
	}
	
	
	public static void enque(String url){
        urlQueue.add(url);
    }
    
    public static boolean urlQueueisEmpty(){
        return urlQueue.isEmpty();
    }
    
    public static void setQueue(LinkedBlockingQueue<String> newUrlQueue){ 
    	urlQueue = newUrlQueue; 
	}
    public static void setSeed(String url){
        seedUrl = url;
        urlQueue.add(seedUrl);
    }
//	@Override
//	public void setRouter(StreamRouter router) {
//		// TODO Auto-generated method stub
//		
//	}
	@Override
	public void setRouter(StreamRouter router) {
		// TODO Auto-generated method stub
		
	}
	
	
}
