package edu.upenn.cis.stormlite.bolt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis.stormlite.DocumentContent;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.crawler.StormliteCrawler;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.spout.SpoutsQueue;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.helper.ChannelWrapper;
//import edu.upenn.cis455.storage.DBStorageInterface;
//import edu.upenn.cis455.xpathengine.XPathEngine;
//import edu.upenn.cis455.xpathengine.XPathEngineFactory;
//import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import jdk.internal.jline.internal.Log;

public class ParserBolt implements IRichBolt {
	
	Fields schema = new Fields("documentUrl","documentContent","documentType");
	String executorIdStr = UUID.randomUUID().toString();
	//TODO private DBStorageInterface db;
	private OutputCollector outCollector;
	// TODO: not sure if this the right implementation of the factory design pattern
	//private XPathEngine xpathEngine;

	

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
		
		
	}

	@Override
	public void execute(Tuple input) {
		// TODO Auto-generated method stub
		StormliteCrawler.decBusy();
		try {
			String url = input.getStringByField("documentUrl");
//			System.out.println("69 file content:"+ input.getObjectByField("documentContent"));
//			System.out.println("70 file type:"+ input.getObjectByField("documentType"));
			DocumentContent file =  (DocumentContent) input.getObjectByField("documentContent");
//			System.out.println("70 file\' url: "+file.getUrl());
			//System.out.println("70 file:"+ input.getObjectByField("documentContent"));
			String fileType = input.getStringByField("documentType");
			Map<String,ChannelWrapper> channels = db.getStoredChannels();
			System.out.println("72 Channels: " + channels);
			ArrayList<String> xPaths = new ArrayList<>();
			// xpath -> channelName
			HashMap<String,String> pathChannelMap = new HashMap<>();
			
			for(ChannelWrapper channel: channels.values()){
				xPaths.add(channel.getXPath());
				System.out.println("79 xpaths: " + channel.getXPath());
				System.out.println("80 channel name: "+channel.getChannelName());
				pathChannelMap.put(channel.getXPath(), channel.getChannelName());
			}
			
			if(xPaths!=null && !xPaths.isEmpty()){
			    System.out.println("85 xpath: "+xPaths);    			
    			// boolean arr for match
				
    			boolean[] matchedChannel = new boolean[xPaths.size()];
        		for(int i = 0; i < matchedChannel.length; i++) { 
        			matchedChannel[i] = false;
        		}
        		// arr for xpaths
        		String[] xpathArr = new String[xPaths.size()];
        		
        		for(int i = 0 ; i< xPaths.size() ; i++){
        		    xpathArr[i] = xPaths.get(i);
        		}
        		
        		//xpathEngine.setXPaths(xpathArr);
        		
        		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        		DocumentBuilder builtDocument = dbFactory.newDocumentBuilder();
        		
        		Document document = null;
        		if(fileType.contains("text/html")){
        			Tidy tidy = new Tidy();
        		    tidy.setInputEncoding("UTF-8");
        		    tidy.setOutputEncoding("UTF-8");
        		    tidy.setWraplen(Integer.MAX_VALUE);
        		    tidy.setXmlOut(true);
        		    tidy.setSmartIndent(true);
        		    tidy.setXmlTags(true);
        		    tidy.setMakeClean(true);
        		    tidy.setForceOutput(true);
        		    tidy.setQuiet(true);
        		    tidy.setShowWarnings(false);
        		    ByteArrayInputStream inputs = new ByteArrayInputStream(file.getContent().getBytes());      			
        		    ByteArrayOutputStream outputs = new ByteArrayOutputStream();
        		    tidy.parse(inputs, outputs);
        		    document = builtDocument.parse(new ByteArrayInputStream(outputs.toString("UTF-8").getBytes()));
//        			matchedChannel = xpathEngine.evaluate(document);
        		    
        			
//        		    System.out.println("110 html file url: "+file.getUrl());
//        			Tidy tidy = new Tidy();
//        			tidy.setXHTML(true);
//        			tidy.setXmlTags(false);
//        			tidy.setDocType("omit");
//        			tidy.setEncloseText(true);
//        			ByteArrayInputStream inputs = new ByteArrayInputStream(file.getContent().getBytes());
//        			ByteArrayOutputStream outputs = new ByteArrayOutputStream();
//        			tidy.parseDOM(inputs, outputs);
//        			document = builtDocument.parse(new ByteArrayInputStream(outputs.toString("UTF-8").getBytes()));
//        			matchedChannel = xpathEngine.evaluate(document);
        		}
        		else{
        		    System.out.println("119 html file url: "+file.getUrl());
        			document = builtDocument.parse(new ByteArrayInputStream(file.getContent().getBytes()));
        			//matchedChannel = xpathEngine.evaluate(document);
        		}
    			// if match channel and doc match, store the url for the doc
    			for(int i = 0; i < matchedChannel.length;i++){
    				if(matchedChannel[i] == true){
    					String channelname = pathChannelMap.get(xPaths.get(i));
    					System.out.println("add url "+url+" channel to "+(channelname));
    					db.addUrlToChannel(channelname,url);
    				}
    			}    
			}
			//StormliteCrawler.incBusy();
			
//			System.out.println(outCollector);
//			System.out.println("url: "+ url);
//			if(file.getUrl() != null) {
//				System.out.println("file url is not null");
//				System.out.println("------"+file.getUrl());
//			}
//			else {
//				System.out.println("file url is null");
//			}
//			System.out.println("file: "+ file);
//			System.out.println("fileType: "+ fileType);
//			System.out.println(new Values(url,file,fileType));
//			
//			try {
//			outCollector.emit(new Values<Object>(url,file,fileType));			
//			}
//			catch(NullPointerException e){
//				e.printStackTrace();
//				System.out.println(e.getMessage());
//			}
			UrlExtraction urlEx = new UrlExtraction(url,file,fileType);
			urlEx.urlExtract();
			
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.out.println("One of the exception happened. 134");
			e.printStackTrace();
		}
		
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// TODO: assign collector, DB, and engine 
		this.outCollector = collector;
		this.db = CrawlerBolt.getDB();
		this.xpathEngine = XPathEngineFactory.getXPathEngine();
		
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

	
	
}
