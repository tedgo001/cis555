//package edu.upenn.cis.crawler;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//
//import javax.net.ssl.HttpsURLConnection;
//import javax.xml.bind.DatatypeConverter;
//
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import edu.upenn.cis455.crawler.info.URLInfo;
//import edu.upenn.cis455.storage.DBStorageInterface;
//
//public class ThreadWorker extends Thread {
//	
//final static Logger logger = LogManager.getLogger(ThreadWorker.class);
//    
//	final XPathCrawler crawler;
//	final DBStorageInterface db;
//	final Integer Id;    
//	BlockingQueue<String> urlQueue;
//    Map<String,List<String>> urlMap;
//    
//
//    public ThreadWorker(DBStorageInterface db, BlockingQueue<String> urlQueue, Map<String,List<String>> urlMap, Integer IdNum,XPathCrawler crawler) {
//        this.Id = IdNum;
//        this.db = db;
//        this.urlQueue = urlQueue;
//        this.urlMap = urlMap;
//        this.crawler = crawler;
//    }
//    
//    public void run(){
//    	logger.info("crawler is finished"+crawler.isFinished());
//        while (!crawler.isFinished()){
//             //System.out.println("start running");
//             //logger.info("Worker " + this.ID + " is starting");
//             String host;
//             synchronized(urlQueue){
//                 host = urlQueue.poll();
//             }
//             if(host==null){
//                 continue;
//             }
//             if (!urlMap.isEmpty() && host != null) {
//                    crawler.startWorking(true);
//                    //logger.info("Worker " + this.ID + " is crawling");
//                    doCrawl(host, urlMap, urlQueue);
//                    //hostMap.remove(host);
//                    crawler.startWorking(false);
//             }  
//        }
//        crawler.readyToExit();
//    }
//    
//    public void doCrawl(String host, Map<String,List<String>> urlMap, BlockingQueue<String> urlQueue) {
//        boolean fetch = false;
//        boolean isdenied = false;
//        URLInfo info = null;
//        System.out.println("Worker: " + this.Id + " handles the host: " + host);
//        List<String> urlList = urlMap.get(host);
//        logger.info("Worker: " + this.Id + " got urlGroups: " + urlList);
//        
//        synchronized(this){
//            if (urlList != null && !urlList.isEmpty()) {
//                if(urlList.isEmpty()) {
//                    urlMap.remove(host);
//                    return;
//                } 
//                info = new URLInfo(urlList.remove(0));
//                logger.info("Worker " + this.Id + " get url: " + info.urlString());
//                
//                if (crawler.allowedToCrawl(host, info.getConnectPortNum(), info.isEncrypted())){
//                    logger.info("Worker " + this.Id + " get Ok to crawl host: " + host);
//                    if (crawler.deferToCrawl(host)) {
//                        logger.info("Crawl defered");
//                        urlList.add(0, info.urlString());
//                        synchronized(urlQueue){
//                            urlQueue.add(host);   
//                        }
//                    } else {
//                        if(crawler.allowedToCrawl(info)){
//                        System.out.println("we can crawl");
//                            if (crawler.allowedToParse(info)) {
//                            	System.out.println("we can parse");
//                                if(!urlList.isEmpty() && !urlQueue.contains(host)){
//                                    try{
//                                        urlQueue.put(host);
//                                    } catch(InterruptedException e){
//                                        logger.warn("catch interrupted exception in worker crawl");
//                                    }
//                                }
//                                fetch= true;
//                            }
//                        }else{
//                            //logger.warn("url path denied for privacy: "+info.toString());   
//                        }
//                    }
//                } else{
//                    isdenied = true;
//                }
//            } else {
//                logger.warn("The urlGroups is empty or null");
//                return;
//            } 
//            
//            if (info != null && fetch) {
//                System.out.println("Worker: " + this.Id + "crawed url: " + info.urlString());
//                crawler.setAccessTime(host);
//                crawler.addCount();
//                crawler.addUrls(info);
//                parseUrl(info);
//            }
//            //logger.info("worker " + this.ID + " deal with url " + info.toString() + " done");
//        }
//    }
//    
//    public void parseUrl(URLInfo urlInfo) {
//        //logger.info(info.toString() + ": Downloading");
//    	logger.info("parseUrl:---------------------");
//        //logger.info("Worker " + this.ID + " URL parse: " + master.urlComposer(info));
//        try {
//        	//logger.info("xpathcrawler debug in tw: "+crawler.constructUrl(urlInfo.getHostName(),urlInfo.getConnectPortNum(),urlInfo.isEncrypted()));
//        	logger.info("passed in urlInfo:"+urlInfo.urlString());
//        	//logger.info("passed in info"+crawler.constructUrl(urlInfo));
//        	//logger.info(crawler.constructUrl(urlInfo.getHostName(),urlInfo.getConnectPortNum(),urlInfo.isEncrypted()));
//            URL url = new URL(urlInfo.urlString());
//            
//            //logger.info("url:" + url);
//            HttpsURLConnection connectionHttps = null;
//            HttpURLConnection connectionHttp = null;
//            BufferedReader inputReader;
//            boolean encrypted = urlInfo.isEncrypted();
//            String contentType = "";
//            
//            if(encrypted){
//                connectionHttps = (HttpsURLConnection)url.openConnection();
//                connectionHttps.setRequestMethod("GET");
//                connectionHttps.setRequestProperty("User-Agent","cis455crawler");
//                connectionHttps.setInstanceFollowRedirects(true);
//                inputReader = new BufferedReader(new InputStreamReader(connectionHttps.getInputStream()));
//                contentType = connectionHttps.getContentType();
//            } else{
//                connectionHttp = (HttpURLConnection)url.openConnection();
//                connectionHttp.setRequestMethod("GET");
//                connectionHttp.setRequestProperty("User-Agent","cis455crawler");
//                //connectionHttp.setRequestProperty("Host",info.getHostName());
//                connectionHttp.setInstanceFollowRedirects(true);
//                inputReader = new BufferedReader(new InputStreamReader(connectionHttp.getInputStream()));
//                contentType = connectionHttp.getContentType();
//            }   
//            
//            String response = parseText(inputReader);
//            logger.info("Worker: " + this.Id + " finish parsing text with response");
//            
//            if(contentType.contains("text/html")){
//                System.out.println(urlInfo.toString() + "[is adding others]");
//                Document document = Jsoup.connect(crawler.constructUrl(urlInfo.getHostName(),urlInfo.getConnectPortNum(),urlInfo.isEncrypted())).userAgent("cis455crawler").get();
//                Elements links = document.select("a[href]");
//                
//                for (Element link : links) {
//                    if(!crawler.getUrls().contains(link.attr("abs:href"))) {
//                        EnqueueUrl(link.attr("abs:href"));
//                        logger.info(link.attr("abs:href"));
//                    }
//                }   
//            }
//            
//            synchronized(crawler){
//            	logger.info("inside the synchronized crawler");
//                if(crawler.needToDownload(doEncrypt(response))){
//                	logger.info("need to download");
//                    if(crawler.allowedToFetch(urlInfo)){
//                    	logger.info("allow to fetch");
//                        logger.info("Downloading: "+urlInfo.urlString());
//                        
//                        crawler.addContent(doEncrypt(response));
//                        System.out.println("191");
//                        db.addADocument(urlInfo.urlString(), response); 
//                        System.out.println("193");
//                    }
//                }
//            }
//            System.out.println("197");
//            if(encrypted){
//                connectionHttps.disconnect();
//            } else{
//                connectionHttp.disconnect();
//            }
//        } catch (Exception e) {
//            logger.warn("catch exception");
//            logger.warn(e.getMessage());
//        } 
//    }
//    
//    public String parseText(BufferedReader ir) {
//        String newline = null;
//        StringBuffer response = new StringBuffer();
//        long pos = 0;
//        try {
//            while ((newline = ir.readLine()) != null && pos<= crawler.getSize() * 1024 * 1024) {
//                response.append(newline + '\n');
//                pos += newline.length();
//            }
//            ir.close();
//            return response.toString();
//        } catch (IOException e) {
//            logger.warn("Here is an IO Exception");
//            return null;
//        } 
//    }
//    
//    public synchronized void EnqueueUrl(String nextUrl) {        
//        URLInfo info = new URLInfo(nextUrl);
//        
//        
//        if (!urlMap.containsKey(info.getDomain())){
//            urlMap.put(info.getDomain(), new ArrayList<>()); 
//            //System.out.println("Domain: " + info.getDomain());
//        } 
//        if(urlMap.get(info.getDomain()) != null) {
//            if(!urlMap.get(info.getDomain()).contains(nextUrl) && !crawler.getUrls().contains(nextUrl)) {
//                urlMap.get(info.getDomain()).add(nextUrl);
//            }
//        }
//        if(!urlQueue.contains(info.getDomain())){
//            try{
//                urlQueue.put(info.getDomain());
//            } catch(InterruptedException e){
//                logger.warn("catch interrupted exception when adding to the urlQueue");
//            }
//        }
//    }
//
//    public String doEncrypt(String str){
//        byte[] result = {};
//        String strHash = "hash";
//        try {
//            MessageDigest sha = MessageDigest.getInstance("MD5");
//            result = sha.digest(str.getBytes(StandardCharsets.UTF_8));
//            strHash = DatatypeConverter.printHexBinary(result);
//        }
//        catch(java.security.NoSuchAlgorithmException e) {
//            logger.debug("hash algorithm not found!");
//        }
//        return strHash;
//    }
//
//}
