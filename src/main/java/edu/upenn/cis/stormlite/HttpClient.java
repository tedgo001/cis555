package edu.upenn.cis.stormlite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.upenn.cis.stormlite.bolt.CrawlerBolt;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.crawler.info.URLInfo;

public class HttpClient {
	
	private String beginUrl;
	private String host;
	private String fileDir;
	private int portNum;
	private boolean isEncrypted;
	private List<String> UrlForCrawling = new ArrayList<>();
	private String document = null;
	private URLInfo urlInfo;
	private String contentType;
	private int contentLength;
	private Long lastUrlModifiedTime; // the lastmodifed for a url
	private String lastDbModifiedString; // the lastmodified get from db
		
	static final Logger log = LogManager.getLogger(HttpClient.class);
	    
	public HttpClient(String beginUrl){
		this.beginUrl = beginUrl;
		this.urlInfo = new URLInfo(beginUrl);
		this.host = this.urlInfo.getHostName();
		this.fileDir = this.urlInfo.getFilePath();
		this.portNum = this.urlInfo.getPortNo();
		this.contentLength = -1;
		this.lastUrlModifiedTime = null;
		this.contentType = "";
		this.isEncrypted = this.urlInfo.isEncrypted();
	}
	
	
	

	public boolean deferToCrawl() {
		// TODO Auto-generated method stub
		Integer delayedTime;
	    Long lastAccessedSiteTime;
	    if(CrawlerBolt.getLastAccessTime().get(this.host) == null) {
	    	return false;
	    }
	    if((lastAccessedSiteTime = CrawlerBolt.getLastAccessTime().get(this.host)) != null && CrawlerBolt.getUrlRobots().get(this.host) != null){
	    	delayedTime = CrawlerBolt.getUrlRobots().get(this.host).getCrawlDelay("cis455crawler");
	        if(delayedTime != null){
	        	long currTime = lastAccessedSiteTime + delayedTime;
	        	boolean needToWait = currTime < System.currentTimeMillis() ? false : true;
	            return needToWait;
	            }
	        }
	        return false; 
	}

	public boolean allowedToCrawl() {
		// TODO Auto-generated method stub
		if (!CrawlerBolt.getUrlRobots().containsKey(host)) {
            try {
        		if(!parseRobotFileContent(this.urlInfo)) {
        			log.warn("cannot parse this url: " + this.beginUrl);
        			return false;
        		}
            } catch (Exception e) {
                log.error("Line 70 Exception. Cannot parse");
            }
        }
        
        String []entirePath = this.fileDir.split("/");
        String currPath = "";
        RobotsTxtInfo robot = CrawlerBolt.getUrlRobots().get(host);
        if(robot != null){
            if(robot.getDisallowedLinks("cis455crawler") != null) {
                boolean goCode = true;
                for(int i=1; i < entirePath.length; i++) {
                    currPath = currPath + "/" + entirePath[i];
                    //System.out.println("disallowed url: "+robot.getDisallowedLinks("cis455crawler"));
                    if(robot.getDisallowedLinks("cis455crawler") != null && robot.getDisallowedLinks("cis455crawler").contains(currPath))
                        goCode = false;
                    if(robot.getDisallowedLinks("cis455crawler") != null && robot.getDisallowedLinks("cis455crawler").contains(currPath + "/"))
                        goCode = false;
                        
                    if(!goCode) {
                        return false;
                    } 
                }
                return true;
            }
            if(robot.getDisallowedLinks("*") != null) {
                boolean proceed = true;
                for(int i=1; i<entirePath.length; i++) {
                    if(robot.getDisallowedLinks("*") != null) {
                        //System.out.println("disallowed url: "+robot.getDisallowedLinks("*"));
                        if(robot.getDisallowedLinks("*") != null && robot.getDisallowedLinks("*").contains(currPath))
                            proceed = false;
                        if(robot.getDisallowedLinks("*") != null && robot.getDisallowedLinks("*").contains(currPath + "/"))
                            proceed = false;
                        if(!proceed) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return true;
	}

	public boolean parseRobotFileContent(URLInfo urlInfo) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader inputReader;
        HttpsURLConnection connectionHttps = null;
        HttpURLConnection connectionHttp = null;
        
        URL fetchedUrl = new URL(urlBuilder(urlInfo));
        
        try{
            if(this.isEncrypted){
                connectionHttps = (HttpsURLConnection)fetchedUrl.openConnection();
                connectionHttps.setRequestMethod("GET");
                connectionHttps.setRequestProperty("User-Agent","cis455crawler");
                connectionHttps.setInstanceFollowRedirects(true);
                inputReader = new BufferedReader(new InputStreamReader(connectionHttps.getInputStream(),StandardCharsets.UTF_8));
            } 
            else{
                connectionHttp = (HttpURLConnection)fetchedUrl.openConnection();
                connectionHttp.setRequestMethod("GET");
                connectionHttp.setRequestProperty("User-Agent","cis455crawler");
                connectionHttp.setInstanceFollowRedirects(true);
                inputReader = new BufferedReader(new InputStreamReader(connectionHttp.getInputStream(),StandardCharsets.UTF_8));
            }   
            
            String newline;
            
            RobotsTxtInfo newRobot = new RobotsTxtInfo();
            String userAgent = "";
            while ((newline = inputReader.readLine()) != null) {
            	
                if(newline.equals("") || newline.equals(" ")) {
                    userAgent = "";
                    continue;
                }
                String[] robotFileItems = newline.split(":");
                if (robotFileItems[0].toLowerCase().contains("user-agent")) {
                    userAgent = robotFileItems[1].trim();
                    if (userAgent.toLowerCase().contains("*") || userAgent.toLowerCase().contains("cis455crawler")){
                        newRobot.addUserAgent(userAgent);
                        //System.out.println("userAgent: " + robotFileItems[1].trim());
                    }
                } 
                else if (userAgent.equalsIgnoreCase("*") || userAgent.equalsIgnoreCase("cis455crawler")) {
                    if (robotFileItems[0].toLowerCase().contains("disallow")) {
                        newRobot.addDisallowedLink(userAgent, robotFileItems[1].trim());
                    } else if (robotFileItems[0].toLowerCase().contains("allow")) {
                        newRobot.addAllowedLink(userAgent, robotFileItems[1].trim());
                    } else if (robotFileItems[0].toLowerCase().contains("crawl-delay")) {
                        newRobot.addCrawlDelay(userAgent, Integer.valueOf(robotFileItems[1].trim()));
                    } else if (robotFileItems[0].toLowerCase().contains("sitemap")) {
                        newRobot.addSitemapLink(robotFileItems[1].trim());
                    }
                }
            }
            CrawlerBolt.addRobot(this.host,newRobot);            
            CrawlerBolt.addLastAccessTime(this.host, new Long(System.currentTimeMillis()));
            inputReader.close();
            if(this.isEncrypted){
                connectionHttps.disconnect();
            } 
            else{
                connectionHttp.disconnect();
            }
            return true;
            
        } catch(Exception e){
            System.out.println("Exception(s) happened when parsing robots file");
            log.warn("EXCEPTION PARSING ROBOTSFILE");
            return false;
        }
	}



	public String urlBuilder(URLInfo url) {
		// TODO Auto-generated method stub
		StringBuilder newUrl = new StringBuilder();
		
        if(url.isEncrypted()){
            newUrl.append("https://");
        } 
        else{
            newUrl.append("http://");
        }
        
        newUrl.append(url.getHostName());
        
        if(newUrl.toString().endsWith("/")){
            newUrl.append("robots.txt");
        } 
        else{
            newUrl.append("/robots.txt");
        }
        return newUrl.toString();
	}




	public boolean allowedToParse() {
		// TODO Auto-generated method stub
		if(CrawlerBolt.ifUrlSeenContains(this.beginUrl)){
            return false;
        }
        HttpsURLConnection httpsConnection = null;
        HttpURLConnection httpConnection = null;
        try{
            //System.out.println("url: "+ url);
            URL urlToConn = new URL(this.beginUrl);
            if(this.isEncrypted){
                httpsConnection = (HttpsURLConnection)urlToConn.openConnection();
                httpsConnection.setRequestMethod("HEAD");
                httpsConnection.setRequestProperty("User-Agent","cis455crawler");
                httpsConnection.setInstanceFollowRedirects(true);
            } 
            else{
                httpConnection = (HttpURLConnection)urlToConn.openConnection();
                httpConnection.setRequestMethod("HEAD");
                httpConnection.setRequestProperty("User-Agent","cis455crawler");
                httpConnection.setInstanceFollowRedirects(true);
            }   
            try {
                if(this.isEncrypted){
                    if (httpsConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        contentType = httpsConnection.getContentType();
                        contentLength = httpsConnection.getContentLength();
                        if(checkContentType(contentType) && contentLength>=0 && contentLength < CrawlerBolt.getMaxSize()* 1024*1024){
                            //log.info("250");
                            httpsConnection.disconnect();
                            return true;   
                        } else{
                            //System.out.println(this.url+ "exceeds input max size");
                            httpsConnection.disconnect();
                            return false;
                        }
                    }else{
                        System.out.println(this.beginUrl+ " is not downloading(check head request again)");
                        httpsConnection.disconnect();
                        return false;
                    }
                } 
                else{
                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        contentType = httpConnection.getContentType();
                        contentLength = httpConnection.getContentLength();
                        if(checkContentType(contentType) && contentLength>=0 && contentLength< CrawlerBolt.getMaxSize() * 1024*1024){
                            //logger.info("269");
                            httpConnection.disconnect();
                            return true;   
                        } 
                        else{
                            System.out.println(this.beginUrl+ ":Not downloading(content exedes max size or false type)");
                            httpConnection.disconnect();
                            return false;
                        }
                    }else{
                        System.out.println(this.beginUrl+ "is not downloading(check head request again)");
                        httpConnection.disconnect();
                        return false;
                    }
                }                
            } catch (IOException e) {
                log.warn("catch an IOException 286");
            }     
        } catch(Exception e){
            log.warn("catch unknown exception 289");
        }
        if(this.isEncrypted){
            httpsConnection.disconnect();
        } else{
            httpConnection.disconnect();
        }
        System.out.println(this.beginUrl+ "is not downloading(check head request again)");
        return false;
	}

	public void parseUrlInfo() {
		// TODO Auto-generated method stub
		try {
            URL urlInfo = new URL(this.beginUrl);
            
            HttpsURLConnection httpsConnection = null;
            HttpURLConnection httpConnection = null;
            BufferedReader inputReader;
            
            if(this.isEncrypted){
                httpsConnection = (HttpsURLConnection)urlInfo.openConnection();
                httpsConnection.setRequestMethod("GET");
                httpsConnection.setRequestProperty("User-Agent","cis455crawler");
                httpsConnection.setInstanceFollowRedirects(true);
                inputReader = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
                contentType = httpsConnection.getContentType();
            } else{
                httpConnection = (HttpURLConnection)urlInfo.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("User-Agent","cis455crawler");
                httpConnection.setInstanceFollowRedirects(true);
                inputReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                contentType = httpConnection.getContentType();
            }   
            
            this.document = parseContent(inputReader);
            
            if(this.isEncrypted){
                httpsConnection.disconnect();
            } else{
                httpConnection.disconnect();
            }
        } catch (Exception e) {
            log.warn("Exception!!!!!! 333");
        } 
		
	}

	public String parseContent(BufferedReader inputReader) {
		// TODO Auto-generated method stub
		String newline = null;
	    StringBuffer response = new StringBuffer();
	    try {
	    	while ((newline = inputReader.readLine()) != null) {
	    		response.append(newline + '\n');
	        }
	    	
	        inputReader.close();
	        return response.toString();
	        
	        } 
	    catch (IOException e) {
	            log.warn("IOException 350");
	            return null;
	        } 
	}




	public String getDocContent() {
		// TODO Auto-generated method stub
		return this.document;
	}

	public boolean allowedToModified() {
		// TODO Auto-generated method stub
		System.out.println("this begins url: "+this.beginUrl);
		System.out.println("cralwerbolt.getDB()"+ CrawlerBolt.getDB());
		  this.lastDbModifiedString = CrawlerBolt.getDB().getLastModifiedMoment(this.beginUrl);
	        HttpsURLConnection httpsConn = null;
	        HttpURLConnection httpConn = null;
	        System.out.println("last modified time in db: " + this.lastDbModifiedString);
	        try{
	            //System.out.println("372 url: "+ beginUrl);
	            URL urllink = new URL(this.beginUrl);
	            if(this.isEncrypted){
	                httpsConn = (HttpsURLConnection)urllink.openConnection();
	                httpsConn.setRequestMethod("HEAD");
	                httpsConn.setRequestProperty("User-Agent","cis455crawler");
	                httpsConn.setInstanceFollowRedirects(true);
	            } else{
	                httpConn = (HttpURLConnection)urllink.openConnection();
	                httpConn.setRequestMethod("HEAD");
	                httpConn.setRequestProperty("User-Agent","cis455crawler");
	                httpConn.setInstanceFollowRedirects(true);
	            }   
	            try {
	                if(this.isEncrypted){
	                    if (httpsConn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
	                        this.lastUrlModifiedTime = httpsConn.getLastModified();
	                        if(this.lastUrlModifiedTime != null && this.lastDbModifiedString !=null){
	                            Date lastModifiedWeb = new Date(this.lastUrlModifiedTime);
	                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    		                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    		                Date lastModifiedTime  = dateFormat.parse(this.lastDbModifiedString);
	    		                if(lastModifiedWeb.before(lastModifiedTime)){
	                                System.out.println(beginUrl+": Not Modified");
	                                httpsConn.disconnect();
	                                return false;	                    
	    		                }
			                }
			                return true;
	                    } else if(httpsConn.getResponseCode() == 304){
	                        System.out.println(beginUrl+": Not Modified");
	                        httpsConn.disconnect();
	                        return false;
	                    }
	                } else{
	                    if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	                        this.lastUrlModifiedTime = httpConn.getLastModified();
	                        if(this.lastUrlModifiedTime!=null && this.lastDbModifiedString !=null){
	                            Date lastModifiedWeb = new Date(this.lastUrlModifiedTime);
	                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    		                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    		                Date lastModifiedTime  = dateFormat.parse(this.lastDbModifiedString);
	    		                if(lastModifiedWeb.before(lastModifiedTime)){
	                                System.out.println(beginUrl+": has not been modified");
	                                httpConn.disconnect();
	                                return false;	                    
	    		                }
			                }
			                return true;
	                    }else if(httpConn.getResponseCode() == 304){
	                        System.out.println(beginUrl+": Not Modified");
	                        httpConn.disconnect();
	                        return false;
	                    }
	                }                
	            } catch (IOException e) {
	                log.warn("catch IOException");
	            }     
	        } catch(Exception e){
	            log.warn("catch an unknown exception");
	        }
	        if(this.isEncrypted){
	            httpsConn.disconnect();
	        } else{
	            httpConn.disconnect();
	        }
	        return true;
			
		
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		return contentType;
	}

	public Long getLastModifiedTime() {
		// TODO Auto-generated method stub
		return lastUrlModifiedTime;
	}

	public String getHostUrl() {
		// TODO Auto-generated method stub
		return this.host;
	}
	
	public List<String> getNewUrls(){
	    return UrlForCrawling;
	}
	
    public boolean checkContentType(String contentType){
        if(contentType.contains("text/html")){
            return true;
        } 
        else if(contentType.contains("text/xml")){
            return true;
        } 
        else if(contentType.contains("application/xml")){
            return true;
        } 
        else if(contentType.contains("+xml")){
            return true;
        } 
        else{
            return false;
        }
    }

}
