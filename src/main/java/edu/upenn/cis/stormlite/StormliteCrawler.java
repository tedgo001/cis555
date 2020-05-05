//package edu.upenn.cis.stormlite;
//
//import java.util.concurrent.LinkedBlockingQueue;
//
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import edu.upenn.cis.stormlite.bolt.CrawlerBolt;
//import edu.upenn.cis.stormlite.bolt.ParserBolt;
//import edu.upenn.cis.stormlite.bolt.UrlExtractorBolt;
//import edu.upenn.cis.stormlite.spout.SpoutsQueue;
//
//public class StormliteCrawler {
//	
//	private static String beginUrl;
//	private static String fileDirectory;
//    private static int maxSize;
//    private static int numOfFiles = 10000;
////    private static String hostName;
//    public static int numOfCrawled;
//    
//    private static int numsInBusy;
//    private static LocalCluster cluster;
//    
//    private static final String CRAWLER_QUEUE_SPOUT = "CRAWLER_QUEUE_SPOUT";
//    private static final String CRAWLER_BOLT = "CRAWLER_BOLT";
//    private static final String PARSER_BOLT = "PARSER_BOLT";
//    private static final String URL_EXTRACTOR_BOLT = "URL_EXTRACTOR_BOLT";
//	
//    public StormliteCrawler(String beginUrl, String fileDirectory, int maxSize){
//        this.beginUrl = beginUrl;
//        this.fileDirectory = fileDirectory;
//        this.maxSize = maxSize;
//        numOfCrawled = 0;
//    }
//    
//    public StormliteCrawler(String beginUrl, String fileDirectory, int maxSize, int numOfFiles){
//        this.beginUrl = beginUrl;
//        this.fileDirectory = fileDirectory;
//        this.maxSize = maxSize;
//        this.numOfFiles = numOfFiles;
//        numOfCrawled = 0;
//    }
//    
////    public StormliteCrawler(String beginUrl, String fileDirectory, int maxSize, int numOfFiles, String hostName ){
////        this.beginUrl = beginUrl;
////        this.fileDirectory = fileDirectory;
////        this.maxSize = maxSize;
////        this.numOfFiles = numOfFiles;
////        this.hostName = hostName;
////        numOfCrawled = 0;
////    }
//    
//    public static void main(String args[]) throws InterruptedException{
//
//        if(args.length < 3){
//		    System.out.println("At least three arguments are required");
//		    System.exit(1);
//		}
//        Config configure = new Config();
//        
//        StormliteCrawler crawler;// = new StormliteCrawler(args[0],args[1],Integer.parseInt(args[2]));;
//        
//        if(args.length == 3) {
//        	 crawler = new StormliteCrawler(args[0],args[1],Integer.parseInt(args[2]));
//        	 configure.put("seedUrl", args[0]);
//      		configure.put("dbDir", args[1]);
//      		configure.put("maxSize",args[2]);
//      		configure.put("UserAgent", "cis455crawler");
//        }
//        
//        else {
//         crawler = new StormliteCrawler(args[0], args[1], Integer.parseInt(args[2]),Integer.parseInt(args[3]));
//        configure.put("seedUrl", args[0]);
// 		configure.put("dbDir", args[1]);
// 		configure.put("maxSize",args[2]);
// 		configure.put("numOfFiles", String.valueOf(Integer.parseInt(args[3])));
// 		configure.put("UserAgent", "cis455crawler");
//        }
//        
////        if(args.length == 5) {
////        	 crawler = new StormliteCrawler(args[0], args[1], Integer.parseInt(args[2]),Integer.parseInt(args[3]),args[4]);
////        }
//               
//        
//        
//        
//        LinkedBlockingQueue<String> urlqueue = new LinkedBlockingQueue<String>();
//        AWSCredentials credentials = new BasicAWSCredentials(
//      		  "<AWS accesskey>", 
//      		  "<AWS secretkey>"
//      		);
//        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
//				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
//				.build();
//
//        //DBStorageInterface db = BDBStorageFactory.getDBInstance(fileDirectory);
//        
//        SpoutsQueue queueSpout = new SpoutsQueue();
//        CrawlerBolt crawlerBolt = new CrawlerBolt();
//        ParserBolt parsematchBolt = new ParserBolt();
//        UrlExtractorBolt linkextractorBolt = new UrlExtractorBolt();
//        
//        SpoutsQueue.setQueue(urlqueue);
//        SpoutsQueue.setSeed(beginUrl);
//        CrawlerBolt.preConfigure(db,maxSize,numOfFiles);
//        
//        TopologyBuilder tpBuilder = new TopologyBuilder();
//        
//        tpBuilder.setSpout(CRAWLER_QUEUE_SPOUT, queueSpout,1);
//        tpBuilder.setBolt(CRAWLER_BOLT, crawlerBolt, 10).shuffleGrouping(CRAWLER_QUEUE_SPOUT);
//        tpBuilder.setBolt(PARSER_BOLT, parsematchBolt, 10).shuffleGrouping(CRAWLER_BOLT);
//        tpBuilder.setBolt(URL_EXTRACTOR_BOLT, linkextractorBolt, 10).shuffleGrouping(CRAWLER_BOLT);
//        
//        cluster = new LocalCluster();
//	    Topology topology = tpBuilder.createTopology();
//	    
//	    ObjectMapper objMapper = new ObjectMapper();
//	    
//		try {
//			String tpStr = objMapper.writeValueAsString(topology);
//			System.out.println("The StormLite topology is:\n" + tpStr);
//		} 
//		catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        cluster.submitTopology("RunningCrawler", configure, topology);
//        Thread.sleep(100000);
//		while (!crawler.isFinished()){
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//            }
//        }
//        
//        cluster.killTopology("RunningCrawler");
//        cluster.shutdown();
//        //db.close();
//        // TODO: close amazon s3
//        
//        System.out.println(numOfCrawled +" urls crawled. Exit");
//        return;
//        
//    }
//    
//	
//    public synchronized static void incBusy(){
//        numsInBusy++;
//    }
//    
//    public synchronized static void decBusy(){
//        numsInBusy--;
//    }
//	
//	public boolean isFinished() {
//        if(numOfCrawled >= numOfFiles){
//        	System.out.println("numOfCrawled"+numOfCrawled);
//        	System.out.println("numOfFiles"+numOfFiles);
//            return true;
//        } 
//        ///&& numsInBusy <= 0 && cluster.taskQueue.isEmpty()==true
//        else if(SpoutsQueue.urlQueueisEmpty()&& numsInBusy <= 0 && cluster.taskQueue.isEmpty()==true){
//        	System.out.println("crawler is finished because no task in queue");
//            return true; 
//        }
//        return false; 
//        }
//
//}
