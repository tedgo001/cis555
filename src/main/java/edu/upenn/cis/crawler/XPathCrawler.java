//package edu.upenn.cis.crawler;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.HttpURLConnection;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.URL;
//import java.net.UnknownHostException;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Queue;
//import java.util.Set;
//import java.util.TimeZone;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingQueue;
////import org.apache.logging.log4j.Level;
//import javax.net.ssl.HttpsURLConnection;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.jsoup.nodes.Element;
//
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//
//import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
//import edu.upenn.cis455.crawler.info.URLInfo;
////import edu.upenn.cis455.storage.BDBStorageFactory;
////import edu.upenn.cis455.storage.DBStorageInterface;
//import jdk.internal.jline.internal.Log;
//import edu.upenn.cis.crawler.CrawlHelper;
//
///** (MS1, MS2) The main class of the crawler.
//  */
//public class XPathCrawler implements CrawlHelper {
//	
//	static final Logger report = LogManager.getLogger(XPathCrawler.class);
//    int NUM_WORKERS = 10;
//    
//    private final String seedUrl;
//    AWSCredentials credentials = new BasicAWSCredentials(
//    		  "<AWS accesskey>", 
//    		  "<AWS secretkey>"
//    		);
//      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
//				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
//				.build();
//    private final DBStorageInterface db;
//    private final int maxSize;
//    private int numOfFiles = 50;
//    private String hostName;
//    
//    public int numsInBusy = 0;
//    public int numsInCrawl = 0;
//    public int numsInEnd = 0;
//    
//    Map<ThreadWorker,Boolean> tw = new HashMap<>();
//    BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
//    
//    Set<String> urlSeen = new HashSet<>();
//    Set<String> contentSeen = new HashSet<>();
//    
//    Map<String,RobotsTxtInfo> robots = new HashMap<>();
//    Map<String,List<String>> urlMap = new HashMap<>();
//    
//    Map<String,Long> lastAccessTime = new HashMap<>();
// 
//    public XPathCrawler(String beginUrl, DBStorageInterface db, int maxSize, int numOfFiles) {
//        this.seedUrl= beginUrl;
//        this.db = db;
//        this.maxSize = maxSize;
//        this.numOfFiles = numOfFiles;
//    }
//    public XPathCrawler(String beginUrl, DBStorageInterface db, int maxSize) {
//        this.seedUrl= beginUrl;
//        this.db = db;
//        this.maxSize = maxSize;
//        
//    }
//    public XPathCrawler(String beginUrl, DBStorageInterface db, int maxSize, int numOfFiles,String hostName) {
//        this.seedUrl= beginUrl;
//        this.db = db;
//        this.maxSize = maxSize;
//        this.numOfFiles = numOfFiles;
//        this.hostName = hostName;
//    }
//	
//    public void start() {
//        //logger.info("Crawl Main Start");
//   
//        URLInfo url = new URLInfo(seedUrl);
//        if(urlMap.get(url.getDomain())==null){
//            
//            urlMap.put(url.getDomain(),new ArrayList<String>());
//        }
//        urlMap.get(url.getDomain()).add(seedUrl);
//        urlQueue.add(url.getDomain());
//        
//        //logger.info("Workers has been set up");
//        for(int i = 0; i< NUM_WORKERS; i++){
//        	//System.out.println("109");
//            ThreadWorker worker = new ThreadWorker(this.db, this.urlQueue, this.urlMap, i, this);
//           // System.out.println("work built");
//            tw.put(worker,false);
//            worker.start();
//           // System.out.println("worker is running");
//        }
//    }
//    
//   
//    public boolean allowedToCrawl(String site, int port, boolean isEncypted) {
//        String host;
//        if(site.contains(":")){
//            int last = site.indexOf(":");
//            host = site.substring(0,last);
//        }
//        else{
//            host = site;
//        }
//        URLInfo urlInfo = new URLInfo(host, port,isEncypted);
//        report.info("urlInfo: "+urlInfo.toString());
//		if (!this.robots.containsKey(site)) {
//            try {
//        		if(!getRobotContent(urlInfo)) {
//        			report.warn("could not parse for robots file in: " + constructUrl(site,port,isEncypted));
//        		}
//            } catch (Exception e) {
//                report.error("Exception: could not parse");
//            }
//        }
//        //logger.info("host robot is: " + this.hostRobots);
//        if(this.robots.get(site)!=null){
//            return this.robots.get(site).crawlForHost("/");
//        }
//        return true;
//    }
// 
//
//	public boolean allowedToCrawl(URLInfo urlInfo) {
//        
//		report.info("passed in url: "+urlInfo);
//        String host = urlInfo.getDomain();
//        report.info("host: "+ host);
//		if (!this.robots.containsKey(host)) {
//            try {
//        		if(!getRobotContent(urlInfo)) {
//        			report.warn("cannot parse for robots file in: " + urlInfo);
//        		}
//            } catch (Exception e) {
//                report.error("Exception: fail to parse");
//            }
//        }
//        //report.info("urlInfo: "+urlInfo);
//        String []paths = urlInfo.getFilePath().split("/");
//        report.info(paths.toString());
//        String currPath = "";
//        RobotsTxtInfo robot = this.robots.get(host);
//        if(robot!=null){
//            if(robot.getDisallowedLinks("cis455crawler") != null) {
//                boolean goAhead = true;
//                for(int i = 1; i < paths.length; i++) {
//                    currPath = currPath + "/" + paths[i];
//                    System.out.println("line 166"+robot.getDisallowedLinks("cis455crawler"));
//                    if(robot.getDisallowedLinks("cis455crawler") != null && robot.getDisallowedLinks("cis455crawler").contains(currPath)) {
//                    	report.info("171");
//                        goAhead = false;
//                    }
//                    if(robot.getDisallowedLinks("cis455crawler") != null && robot.getDisallowedLinks("cis455crawler").contains(currPath + "/")) {
//                    	report.info("175");
//                        goAhead = false;
//                    }
//                    if(goAhead == false) {
//                    	report.info("179");
//                        return false;
//                    } 
//                }
//                return true;
//            }
//            if(robot.getDisallowedLinks("*") != null) {
//                boolean goAhead = true;
//                for(int i=1; i<paths.length; i++) {
//                    if(robot.getDisallowedLinks("*") != null) {
//                        if(robot.getDisallowedLinks("*") != null && robot.getDisallowedLinks("*").contains(currPath)) {
//                        	report.info("190");
//                            goAhead = false;
//                        }
//                        if(robot.getDisallowedLinks("*") != null && robot.getDisallowedLinks("*").contains(currPath + "/")) {
//                        	report.info("194");
//                            goAhead = false;
//                        }
//                        if(goAhead==false) {
//                        	report.info(198);
//                            return false;
//                        }
//                    }
//                }
//                return true;
//            }
//        }
//        return true;
//    }
//
//	public void setAccessTime(String hostSite) {
//		lastAccessTime.put(hostSite, System.currentTimeMillis());
//		
//	}
//
//	public void addCount() {
//		numsInCrawl++;
//		
//	}
//
//	public void addUrls(URLInfo url) {
//		
//		this.urlSeen.add(url.urlString());
//		
//	}
//
//	public Set<String> getUrls() {
//		
//		return this.urlSeen;
//	}
//
//	public boolean needToDownload(String content) {
//		synchronized (contentSeen) {
//            if (contentSeen.contains(content)){
//                return false;    
//            }
//            return true;
//        }
//	}
//
//	public boolean allowedToFetch(URLInfo url) {
//		
//		  	String lastModifiedMoment = db.getLastModifiedMoment(url.urlString());
//	        HttpsURLConnection httpsConnection = null;
//	        HttpURLConnection httpConnection = null;
//	        boolean encrypted = url.isEncrypted();
//	        
//	        try{
//	            //System.out.println("url info:  "+ url.toString());
//	        	URL url_link = new URL(buildAUrl(url));
//	            //URL url_link = new URL(constructUrl(url));
//	            report.info("url_link: "+url_link.toString());
//	            if(encrypted){
//	                httpsConnection = (HttpsURLConnection)url_link.openConnection();
//	                httpsConnection.setRequestMethod("HEAD");
//	                httpsConnection.setRequestProperty("User-Agent","cis455crawler");
//	                httpsConnection.setInstanceFollowRedirects(true);
//	                if (lastModifiedMoment != null) {
//	                    //System.out.println("time: "+lastModifiedMoment);
//	                    httpsConnection.setRequestProperty("If-Modified-Since", lastModifiedMoment);
//	                }
//	            } else{
//	                httpConnection = (HttpURLConnection)url_link.openConnection();
//	                httpConnection.setRequestMethod("HEAD");
//	                httpConnection.setRequestProperty("User-Agent","cis455crawler");
//	                httpConnection.setInstanceFollowRedirects(true);
//	                if (lastModifiedMoment != null) {
//	                    //System.out.println("time: "+lastModifiedMoment);
//	                    httpConnection.setRequestProperty("If-Modified-Since", lastModifiedMoment);
//	                }
//	      
//	            }   
//	            try {
//	                if(encrypted){
//	                	
//	                    if (httpsConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
//	                    	
//	                        Long tempTime = httpsConnection.getLastModified();
//	                        if(tempTime!=null && lastModifiedMoment !=null){
//	                            Date lastModifiedWeb = new Date(tempTime);
//	                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
//	    		                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//	    		                Date lastModifiedTime  = dateFormat.parse(lastModifiedMoment);
//	    		                if(lastModifiedWeb.before(lastModifiedTime)){
//	                                System.out.println(url.urlString()+": Not Modified");
//	                                httpsConnection.disconnect();
//	                                return false;	                    
//	    		                }
//			                }
//			                return true;
//			                
//	                    } 
//	                    else if(httpsConnection.getResponseCode() == 304){
//	                        System.out.println(url.urlString()+": Not Modified");
//	                        httpsConnection.disconnect();
//	                        return false;
//	                    }
//	                } 
//	                else{
//	                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//	                        Long tempTime = httpConnection.getLastModified();
//	                        
//	                        if(tempTime!=null && lastModifiedMoment !=null){
//	                            Date lastModifiedWeb = new Date(tempTime);
//	                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
//	    		                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//	    		                Date lastModifiedTime  = dateFormat.parse(lastModifiedMoment);
//	    		                
//	    		                if(lastModifiedWeb.before(lastModifiedTime)){
//	                                System.out.println(url.urlString()+": Not Modified");
//	                                httpConnection.disconnect();
//	                                return false;	                    
//	    		                }
//			                }
//			                return true;
//			                
//	                    }
//	                    else if(httpConnection.getResponseCode() == 304){
//	                        System.out.println(url.urlString()+": Not Modified");
//	                        httpConnection.disconnect();
//	                        return false;
//	                    }
//	                }                
//	            } catch (IOException e) {
//	                report.warn("Exception: IOException happened");
//	            }     
//	        } catch(Exception e){
//	            report.warn("Some unknown exception");
//	        }
//	        if(encrypted){
//	            httpsConnection.disconnect();
//	        } else{
//	            httpConnection.disconnect();
//	        }
//	        return true;
//		
//		
//	}
//
//	public void addContent(String content) {
//		this.contentSeen.add(content);
//	}
//
//	public boolean deferToCrawl(String site) {
//		
//		    Integer delayedTime;
//	        Long lastAccessTimeForThisSite;
//	        if (lastAccessTime.get(site) == null) {
//	            return false;
//	        }
//	        if((lastAccessTimeForThisSite = lastAccessTime.get(site)) != null && robots.get(site) != null){
//	            delayedTime = robots.get(site).getCrawlDelay("cis455crawler");
//	            if(delayedTime != null){
//	                boolean ifWait = lastAccessTimeForThisSite + delayedTime < System.currentTimeMillis() ? false : true;
//	                return ifWait;
//	            }
//	        }
//	        return false; 
//		
//	}
//
//
//	public boolean allowedToParse(URLInfo url) {
//		report.info("passed in Url: "+url);
//		if(this.urlSeen.contains(url.urlString())){
//			 System.out.println("352");
//            return false;
//        }
//        int contentLength = -1;
//        String contentType = "";
//            
//        HttpsURLConnection httpsConnection = null;
//        HttpURLConnection httpConnection = null;
//        BufferedReader inputReader = null;
//        boolean encrypted = url.isEncrypted();
//        
//        String nextLine = null;
//        StringBuffer response = new StringBuffer();
//        
//        try{
//            
//        	report.info(url.urlString());
//        	//report.info(constructUrl(url.getHostName(),url.getConnectPortNum(),url.isEncrypted()));
//           // URL urllink = new URL(constructUrl(url.getHostName(),url.getConnectPortNum(),url.isEncrypted()));
//            //report.info(urllink); //http://127.0.0.1/
//            URL urllink = new URL(url.urlString());
//            report.info(urllink);
//            if(encrypted){
//                httpsConnection = (HttpsURLConnection)urllink.openConnection();
//                httpsConnection.setRequestMethod("HEAD");
//                httpsConnection.setRequestProperty("User-Agent","cis455crawler");
//                httpsConnection.setInstanceFollowRedirects(true);
//            } else{
//            	report.info("http connection is being established!");
//                httpConnection = (HttpURLConnection)urllink.openConnection();
//                httpConnection.setRequestMethod("HEAD");
//                httpConnection.setRequestProperty("User-Agent","cis455crawler");
//                httpConnection.setInstanceFollowRedirects(true);
//            }   
//            try {
//                if(encrypted){
//                    if (httpsConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
//                        contentType = httpsConnection.getContentType();
//                        contentLength = httpsConnection.getContentLength();
//            //ifValidType(contentType) &&
//                        if(ifValidType(contentType) && (contentLength>=0 && contentLength< getSize()* 1024*1024 || contentLength == -1)){
//                          
//                            httpsConnection.disconnect();
//                            return true;   
//                        } else{
//                           
//                            httpsConnection.disconnect();
//                            //System.out.println("394");
//                            return false;
//                        }
//                    }else{
//                        httpsConnection.disconnect();
//                        return false;
//                    }
//                } else{
//                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                        try{
//                            contentType = httpConnection.getContentType();
//                            report.info("get contentType, and it is: "+contentType);
//                        }
//                        catch(Exception e){
//                        	 System.out.println("407");
//                            return false;
//                        }
//                    	//ifValidType(contentType) &&
//                        contentLength = httpConnection.getContentLength();
//                        report.info("we get content length, it is: "+contentLength);
//                        if(ifValidType(contentType) && (contentLength>=0 && contentLength< getSize()* 1024*1024 || contentLength == -1)){
//                            System.out.println("427");
//                            httpConnection.disconnect();
//                            return true;   
//                        } else{
//                            
//                            httpConnection.disconnect();
//                            System.out.println("416");
//                            return false;
//                        }
//                    }else{
//                        
//                        httpConnection.disconnect();
//                        System.out.println("421");
//                        return false;
//                    }
//                }                
//            } catch (IOException e) {
//               report.warn("Exception: IO exception");
//            }     
//        } catch(Exception e){
//            report.warn("Unknown Exception");
//        }
//        if(encrypted){
//            httpsConnection.disconnect();
//        } else{
//            httpConnection.disconnect();
//        }
//     System.out.println("435");
//        return false;
//	}
//
//	public boolean ifValidType(String contentType) {
//		if(contentType.contains("text/html")){
//            return true;
//        } else if(contentType.contains("text/xml")){
//            return true;
//        } else if(contentType.contains("application/xml")){
//            return true;
//        } else if(contentType.contains("+xml")){
//            return true;
//        } else{
//            return false;
//        }
//	}
//
//	public boolean isFinished() {
//		
//		if(this.numsInCrawl >= numOfFiles){
//			System.out.println("crawler num:"+numsInCrawl);
//			System.out.println("numofFiles:"+numOfFiles);
//            return true;
//        } 
////		else if(this.urlQueue.isEmpty() && this.numsInBusy <= 0){
////			System.out.println("458");
////            return true; 
////        }
//        return false; 
//	}
//
//	public void startWorking(boolean inWork) {
//		
//		if(inWork){
//            this.numsInBusy++;
//        } 
//		else {
//            this.numsInBusy--;
//        }
//		
//	}
//
//	public void readyToExit() {
//		
//		  numsInEnd++;
//	}
//
//	public int getSize() {
//		return this.maxSize;
//	}
//
//	public String constructUrl(URLInfo url) {
//		
//		StringBuilder newUrl = new StringBuilder();
//		
//        if(url.isEncrypted()){
//            newUrl.append("https://");
//        } 
//        else{
//            newUrl.append("http://");
//        }
//        
//        newUrl.append(url.getHostName());
//        
//        newUrl.append(url.getFilePath());
//        return newUrl.toString();
//	}
//
//	   
//    public String constructUrl(String site, int port, boolean isEncypted) {
//    	StringBuilder newUrl = new StringBuilder();
//    	
//        if(isEncypted){
//            newUrl.append("https://");
//        } 
//        else{
//            newUrl.append("http://");
//        }
//        
//        newUrl.append(site);
//        
//        if(port!=8000){
//       // report.info(port);
//            newUrl.append(":" + port);
//        }
//        else {
//        	//newUrl.append(":" + 8000);
//        }
//        
//        
//        return newUrl.toString();
//	}
//
//	
//	public boolean getRobotContent(URLInfo url) throws Exception {
//		
//		BufferedReader inputReader;
//        HttpsURLConnection httpsConnection = null;
//        HttpURLConnection httpConnection = null;
//        
//        URL fetch = new URL(buildAUrl(url));
//        //report.info("fetch: " + fetch);
//        boolean encrypted = url.isEncrypted();
//        
//        try{
//            if(encrypted){
//                httpsConnection = (HttpsURLConnection)fetch.openConnection();
//                httpsConnection.setRequestMethod("GET");
//                httpsConnection.setRequestProperty("User-Agent","cis455crawler");
//                httpsConnection.setInstanceFollowRedirects(true);
//                inputReader = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream(),StandardCharsets.UTF_8));
//            } 
//            else{
//            	report.info("running");
//                httpConnection = (HttpURLConnection)fetch.openConnection();
//                httpConnection.setRequestMethod("GET");
//                httpConnection.setRequestProperty("User-Agent","cis455crawler");
//                httpConnection.setInstanceFollowRedirects(true);
//                inputReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(),StandardCharsets.UTF_8));
//            }   
//            
//            String newline;
//            
//            RobotsTxtInfo newRobot = new RobotsTxtInfo();
//            
//            String userAgent = "";
//            while ((newline = inputReader.readLine()) != null) {
//                if(newline.equals("") || newline.equals(" ")) {
//                    userAgent = "";
//                    continue;
//                }
//                report.info("content: " + newline);
//                if(newline.contains(":")){
//                    String[] word = newline.split(":");
//                    if (word[0].toLowerCase().contains("user-agent")) {
//                        userAgent = word[1].trim();
//                        if (userAgent.toLowerCase().contains("*") || userAgent.toLowerCase().contains("cis455crawler")){
//                            newRobot.addUserAgent(userAgent);
//                            report.info("userAgent: " + word[1].trim());
//                        }
//                    } 
//                    else if (userAgent.equalsIgnoreCase("*") || userAgent.equalsIgnoreCase("cis455crawler")) {
//                        if (word[0].toLowerCase().contains("disallow")) {
//                            newRobot.addDisallowedLink(userAgent, word[1].trim());
//                        } 
//                        else if (word[0].toLowerCase().contains("allow")) {
//                            newRobot.addAllowedLink(userAgent, word[1].trim());
//                        } 
//                        else if (word[0].toLowerCase().contains("crawl-delay")) {
//                            newRobot.addCrawlDelay(userAgent, Integer.valueOf(word[1].trim()));
//                        } 
//                        else if (word[0].toLowerCase().contains("sitemap")) {
//                            newRobot.addSitemapLink(word[1].trim());
//                            //report.info("Crawl delay: " + item[1].trim() + ", agent: "+ userAgent);
//                        }
//                    }   
//                }
//            }
//            this.robots.put(url.getDomain(),newRobot); 
//            report.info("Put robot " + url.getDomain());
//            this.lastAccessTime.put(url.getDomain(), new Long(System.currentTimeMillis()));
//            inputReader.close();
//            if(encrypted){
//                httpsConnection.disconnect();
//            } else{
//            	report.info("httpConnection closed");
//                httpConnection.disconnect();
//            }
//           
//            return true;
//        } catch(Exception e){
//        	report.info(e.getStackTrace());
//            report.warn("catch some exceptions in parsing robots");
//            return false;
//        }
//	}
//
//	public String buildAUrl(URLInfo url) {
//		
//		StringBuilder newUrl = new StringBuilder();
//		
//        if(url.isEncrypted()){
//            newUrl.append("https://");
//        } 
//        else{
//            newUrl.append("http://");
//        }
//        
//        newUrl.append(url.getHostName());
//        //report.info(url.getDomain());
//        if(newUrl.toString().endsWith("/")){
//            newUrl.append("robots.txt");
//        } 
//        else{
//            newUrl.append("/robots.txt");
//        }
//        String newUrlResult = newUrl.toString();
//        if(!newUrlResult.contains(String.valueOf(url.getPortNo()))) {
//        	newUrlResult = newUrlResult.replace("localhost", "localhost:"+url.getPortNo());
//        }
//        return newUrl.toString();
//	}
//	  
//	public void close() {
//	        db.close();
//	    }
//	
//	
//	public static void main(String args[]){
//		
//        if (args.length < 3 || args.length > 5) {
//        	System.out.println("args length: "+args.length);
//            System.out.println("make sure you give the right inputs");
//            System.exit(1);
//        }
//        
//        //System.out.println("Crawler starting");
//        String beginUrl = args[0];
//        String dir = args[1];
//        Integer maxSize = Integer.valueOf(args[2]);
//        Integer fileNum = args.length >= 4 ? Integer.valueOf(args[3]) : 1000;
//        String hostName = args.length == 5 ? args[4] : "";
//        DBStorageInterface db = BDBStorageFactory.getDBInstance(dir);
//     if(args.length==5) {
//    	 
//    	 InetAddress host = null;
//    	 DatagramSocket s = null;
//    	 
//        if(!hostName.equals("")) {
//        	try {
//				 host = InetAddress.getByName(hostName);
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        	try {
//			 s = new DatagramSocket();
//			} catch (SocketException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        	
//        }
//        XPathCrawler crawler = new XPathCrawler(beginUrl, db, maxSize, fileNum,hostName);
//        crawler.start();
//        if(host!= null && s != null) {
//        byte[] data = ("timqi;" + crawler.getUrls()).getBytes();
//        DatagramPacket packet = new DatagramPacket(data,data.length,host,10455);
//        try {
//			s.send(packet);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//        while (!crawler.isFinished())
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        crawler.close();
//     }
//     }
//     else if(args.length==4) {
//    	 XPathCrawler crawler = new XPathCrawler(beginUrl, db, maxSize, fileNum);
//    	 crawler.start();
//    	 while (!crawler.isFinished())
//             try {
//                 Thread.sleep(10);
//             } catch (InterruptedException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             }
//    	 crawler.close();
//     }
//     else {
//    	 //System.out.println("3 arguments, running! ");
//    	 XPathCrawler crawler = new XPathCrawler(beginUrl, db, maxSize);
//    	 System.out.println("crawler built");
//    	 crawler.start();
//    	 System.out.println("crawler start");
//    	 while (!crawler.isFinished())
//    		 
//             try {
//                 Thread.sleep(10);
//                 //System.out.println("thread here");
//             } catch (InterruptedException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             }
//        
//         crawler.close();
//     }
//        
//      
////        
////        XPathCrawler crawler = new XPathCrawler(beginUrl, db, maxSize, fileNum);
////        
////        //System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
////        crawler.start();
////        
////        while (!crawler.isFinished())
////            try {
////                Thread.sleep(10);
////            } catch (InterruptedException e) {
////                // TODO Auto-generated catch block
////                e.printStackTrace();
////            }
////       
////        crawler.close();
//  }
//	
//	
//	
//	
//	
//	
//	
//}
