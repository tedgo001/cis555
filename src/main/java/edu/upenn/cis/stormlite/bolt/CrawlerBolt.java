package edu.upenn.cis.stormlite.bolt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.crawler.StormliteCrawler;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.spout.SpoutsQueue;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis.stormlite.DocumentContent;
import edu.upenn.cis.stormlite.HttpClient;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
//import edu.upenn.cis455.storage.DBStorageInterface;

public class CrawlerBolt implements IRichBolt {
	
	    static final Logger log = LogManager.getLogger(CrawlerBolt.class);
	    
	    Fields schema = new Fields("documentUrl","documentContent","documentType");
	    
		String executorIdStr = UUID.randomUUID().toString();
		
		Map<String, String> configure;
		
		private OutputCollector outCollector;
		private static Map<String,RobotsTxtInfo> urlRobots;
	    private static Map<String,Long> lastAccessTime;
	    //private static DBStorageInterface db;
	    private static int maxSize;
	    private static int numOfFiles;
		private static Set<String> urlSeen;
		private static Set<String> contentSeen;
		
		public CrawlerBolt(){
		   // should be created automatically, but leave it here for safety    
		}
		
		public static synchronized Map<String,RobotsTxtInfo> getUrlRobots(){
		      return urlRobots;
		}
		    
	    public static synchronized void addRobot(String hostUrl,RobotsTxtInfo newRobot){
		      urlRobots.put(hostUrl,newRobot);   
		}
		    
		public static synchronized void addLastAccessTime(String hostUrl, Long time){
		      lastAccessTime.put(hostUrl,time);
		}
		    
		public static synchronized Map<String,Long> getLastAccessTime(){
		    return lastAccessTime;
		}
	

	public static void preConfigure(DBStorageInterface db_, int maxSize_, int numOfFiles_) {
		// TODO Auto-generated method stub
		db = db_;
	    maxSize = maxSize_;
	    numOfFiles = numOfFiles_;
		
	}

	@Override
	public String getExecutorId() {
		// TODO Auto-generated method stub
		return executorIdStr;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(schema);
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		urlRobots.clear();
		lastAccessTime.clear();
		urlSeen.clear();
		contentSeen.clear();
		
	}

	@Override
	public void execute(Tuple input) {
		// TODO Auto-generated method stub
		StormliteCrawler.decBusy();
	    String url = input.getStringByField("url");
	    
	    System.out.println(url + " gets into Bolt");
	    
	    if(urlSeen.size() > numOfFiles){
	        System.out.println("Cannot crawl more. Stop");
	        return;
	    }
	    
	    HttpClient helper = new HttpClient(url);
	    System.out.println("normal url: "+url);
	    if(helper.deferToCrawl()){
	        SpoutsQueue.enque(url);
	        return;
	    }
	    if(!helper.allowedToCrawl()){
	        System.out.println(url + "cannot be allowed to download");
			return;
	    }
	    System.out.println(url + "is permitted to crawl");
	    if(!helper.allowedToParse()){
	    	return;
	    }
	    System.out.println(url + " is allowed to parse");
	    helper.parseUrlInfo();
	    System.out.println(url + " has been parsed");
	    setUrlSeen(url);
	    StormliteCrawler.numOfCrawled++;
	    String content = helper.getDocContent();
	    System.out.println("content: " + content.substring(0,15));
        if(content!=null){
            if(shouldDownload(encryptContent(content))){
                if(helper.allowedToModified()){
                	System.out.println("url: "+url);
                	System.out.println("helper content type: "+helper.getContentType());
                	
                	System.out.println("help LMT"+helper.getLastModifiedTime());
                    DocumentContent file = new DocumentContent(url,helper.getContentType(),content,helper.getLastModifiedTime());
                    // start downloading and store to db
                  //TODO  db.addADocument(url,content);
                    System.out.println(url+" 149 is downloading");
                    addLastAccessTime(helper.getHostUrl(),System.currentTimeMillis());
                    setContentSeen(encryptContent(content));
                    StormliteCrawler.incBusy();
                   // System.out.println("158 file: "+file);
                    //System.out.println("159 file\'s url: "+ file.getUrl());
                    //System.out.println("70 file\'s content:"+ file.getContent());
                    outCollector.emit(new Values<Object>(url,file,helper.getContentType()));
                } else{
	                try{
	                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	                    Date lastModifiedTime  = dateFormat.parse(db.getLastModifiedMoment(url));
	                    Long milliseconds = lastModifiedTime.getTime();
                        DocumentContent file = new DocumentContent(url,helper.getContentType(),db.getDocumentFromUrl(url),milliseconds);
                        StormliteCrawler.incBusy();
                        System.out.println("158 file: "+file);
                        System.out.println("159 file\'s url: "+ file.getUrl());
                        System.out.println("70 file\'s content:"+ file.getContent());
                        
                        outCollector.emit(new Values<Object>(url,file,helper.getContentType()));
	                } catch(ParseException e){
	                	e.getStackTrace();
	                    System.out.println(e);

	                }
                }
            }
        }
		
	}
	
	public static synchronized void setUrlSeen(String url){
        urlSeen.add(url);
    }

	public static synchronized void setContentSeen(String content) {
		// TODO Auto-generated method stub
		contentSeen.add(content);
		
	}
	public static synchronized Set<String> getUrlSeen(){
        return urlSeen;
    }
	
	public static synchronized Set<String> getContentSeen(){
        return contentSeen;
    }
	
	public static synchronized boolean ifUrlSeenContains(String url){
        return urlSeen.contains(url);
    }

	public boolean shouldDownload(String content) {
		// TODO Auto-generated method stub
		synchronized (contentSeen) {
            if (contentSeen.contains(content)){
                return false;    
            }
            return true;
        }
	}

	public String encryptContent(String content) {
		// TODO Auto-generated method stub
		byte[] result = {};
        String hashedString = "hash";
        try {
            MessageDigest sha = MessageDigest.getInstance("MD5");
            result = sha.digest(content.getBytes(StandardCharsets.UTF_8));
            hashedString = DatatypeConverter.printHexBinary(result);
        }
        catch(java.security.NoSuchAlgorithmException e) {
            log.debug("Hashing algorithmn does not exist!!!");
        }
        return hashedString;
	}



	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.configure = stormConf;
	    this.outCollector = collector;
		urlSeen = new HashSet<>();
		contentSeen = new HashSet<>();
		urlRobots = new HashMap<>();
		lastAccessTime = new HashMap<>();
		
	}

	@Override
	public void setRouter(StreamRouter router) {
		// TODO Auto-generated method stub
		 this.outCollector.setRouter(router);
		
	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return schema;
	}
	
//	public static DBStorageInterface getDB(){
//	    return db;
//	}
	
	public static int getMaxSize(){
	    return maxSize;
	}

	

}
