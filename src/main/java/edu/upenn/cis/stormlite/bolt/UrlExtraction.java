//package edu.upenn.cis.stormlite.bolt;
//
//import java.io.IOException;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import edu.upenn.cis.stormlite.DocumentContent;
//import edu.upenn.cis.stormlite.StormliteCrawler;
//import edu.upenn.cis.stormlite.spout.SpoutsQueue;
//import jdk.internal.jline.internal.Log;
//
//public class UrlExtraction {
//	private String documentUrl;
//	private DocumentContent documentContent;
//	private String documentType;
//	
//	public UrlExtraction(String documentUrl,DocumentContent documentContent,String documentType) {
//		this.documentUrl = documentUrl;
//		this.documentContent = documentContent;
//		this.documentType = documentType;
//		
//		
//	}
//	
//	public void urlExtract() {
//		 StormliteCrawler.decBusy();
//		  //  documentUrl = input.getStringByField("documentUrl");
//		   // System.out.println("58:" + documentUrl);
//			//documentContent = (DocumentContent) input.getObjectByField("documentContent");
//			//System.out.println("60:" + documentContent);
//			//documentType = input.getStringByField("documentType");
//			//System.out.println("62:" + documentType);
//			//System.out.println("63: " + StormliteCrawler.numOfCrawled);
//		    if(documentType.contains("text/html")){//what type do we need to handle
//		        try {
//	    	        Document doc = Jsoup.connect(documentUrl).userAgent("cis455crawler").get();
//	                Elements links = doc.select("a[href]");
//	                
//	                for (Element link : links) {
//	                    //System.out.println("70 link : " + link.attr("abs:href"));
//	                    if(!CrawlerBolt.ifUrlSeenContains(link.attr("abs:href"))) {
//	                        SpoutsQueue.enque(link.attr("abs:href"));
//	                    }
//	                }
//	            } catch (IOException e) {
//	            	System.out.println("Exception");
//	            	Log.warn(e.getStackTrace());
//			    }
//		    }
//	}
//
//}
