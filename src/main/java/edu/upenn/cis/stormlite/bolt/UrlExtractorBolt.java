package edu.upenn.cis.stormlite.bolt;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

//import org.apache.commons.logging.Log;
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
import jdk.internal.jline.internal.Log;

public class UrlExtractorBolt implements IRichBolt{
	
	Fields schema = new Fields();
	String executorIdStr = UUID.randomUUID().toString();
	private OutputCollector outCollector;
	
	private String documentUrl;
	private DocumentContent documentContent;
	private String documentType;
	
	
	

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
	    documentUrl = input.getStringByField("documentUrl");
	    System.out.println("58:" + documentUrl);
		documentContent = (DocumentContent) input.getObjectByField("documentContent");
		System.out.println("60:" + documentContent);
		documentType = input.getStringByField("documentType");
		System.out.println("62:" + documentType);
		//System.out.println("63: " + StormliteCrawler.numOfCrawled);
	    if(documentType.contains("text/html")){//what type do we need to handle
	        try {
    	        Document doc = Jsoup.connect(documentUrl).userAgent("cis455crawler").get();
                Elements links = doc.select("a[href]");
                
                for (Element link : links) {
                    //System.out.println("70 link : " + link.attr("abs:href"));
                    if(!CrawlerBolt.ifUrlSeenContains(link.attr("abs:href"))) {
                        SpoutsQueue.enque(link.attr("abs:href"));
                    }
                }
            } catch (IOException e) {
            	System.out.println("Exception");
            	Log.warn(e.getStackTrace());
		    }
	    }
		
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.outCollector = collector;
		
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
