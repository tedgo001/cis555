package edu.upenn.cis.stormlite.bolt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.distributed.WorkerHelper;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;

public class PrintBoltHelper implements IRichBolt {
	
	static Logger log = LogManager.getLogger(PrintBoltHelper.class);
	
	Fields FieldsForPrint = new Fields();
	private FileWriter filewriter;
	private String outputFileName = "output.txt";
	private String outputDire;
	private File directory;
	private File file;
	private final String endline = "\r\n"; 
	private int numsCompleted = 0;
	
	// use the same method as the previous homework assignment
	String IdInPBH = UUID.randomUUID().toString();

	@Override
	public String getExecutorId() {
		// TODO Auto-generated method stub
		return IdInPBH;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(FieldsForPrint);
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		//shouldn't do cleanup in printBOltHelp class
		
	}

	@Override
	public void execute(Tuple input) {
		if (!input.isEndOfStream()){
		    System.out.println("executor id: "+getExecutorId() + "; Now input check (string) " + input.toString()); 
		    try{
				filewriter = new FileWriter(outputDire + "/output.txt",true);
				String key = input.getStringByField("key");
				String value = input.getStringByField("value");
				filewriter.write(key + "," + value + endline);
				filewriter.flush();
				filewriter.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		} else{
		    numsCompleted--;
		    if(numsCompleted == 0) {
				WorkerServer.setStatus("idle");
				WorkerServer.ifReady = false;
			}
		}
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		String dir = "";
		if(stormConf.get("outputDir")!=null){
		    dir = "/" + stormConf.get("outputDir");
		}
		
		outputDire = WorkerServer.getStoredDir() + dir;
		directory = new File(outputDire);
		
		
		if(!directory.exists()){
			directory.mkdir();
		}
		
		file = new File(outputDire + "/output.txt");
		
		if(file.exists()){
			file.delete();
		}
		
		int numOfReducers = Integer.parseInt(stormConf.get("reduceExecutors"));
        String[] workers = WorkerHelper.getWorkers(stormConf);
        int numOfWorker = workers.length;
		numsCompleted = numOfReducers + numOfReducers * (numOfWorker - 1);
		
	}

	@Override
	public void setRouter(StreamRouter router) {
		// TODO Auto-generated method stub
		// should not set router in printBolt, I think 
	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return FieldsForPrint;
	}

}
