package edu.upenn.cis.stormlite.spout;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;

import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
public class FileSpoutImplementation implements IRichSpout {
	
	static Logger log = Logger.getLogger(FileSpout.class);

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordSpout, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    /**
	 * The collector is the destination for tuples; you "emit" tuples there
	 */
	SpoutOutputCollector collector;
	
	Queue<File> fileQueue = new LinkedList<File>();
	
	/**
	 * This is a simple file reader
	 */
	File file;
	String fileDirectory;
    BufferedReader reader;
	Random r = new Random();
		
	int inx = 0;
	boolean sentEof = false;
	
    public FileSpoutImplementation() {
    	
    }
    @SuppressWarnings("rawtypes")
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        log.info("worker index:"+conf.get("workerIndex"));
        if (conf.containsKey("workerIndex")){
            fileDirectory = WorkerServer.getStoredDir() + "/" + conf.get("inputDir"); 
            //+ "/" + conf.get("workerIndex");   
        } else{
            fileDirectory = WorkerServer.getStoredDir() + "/" + conf.get("inputDir");
        }
		File fileDir = new File(fileDirectory);
    	log.info("[spout" + conf.get("workerIndex") +"]: " + fileDirectory);
    	for(File file : fileDir.listFiles()){
			if(!file.isDirectory()){
				fileQueue.add(file);
			}
		}	
    }
    @Override
    public void close() {
    	if (reader != null)
	    	try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    @Override
    public synchronized void nextTuple() {
        //log.info("[SPOUT] new: " + fileDirectory);
        //log.info("[QUeue] fileQueue " + fileQueue);
        if(!fileQueue.isEmpty()){
            file = fileQueue.poll();
            String line;
            //System.out.println("[QUeue] file = " + file);
            try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
            
            if (reader != null && !sentEof) {
                try{
                    while((line = reader.readLine()) != null){
						String[] words = line.split("[ \\t\\,.]");
						
						for(String word : words){
						    //System.out.println("[words] getting " + word);
							this.collector.emit(new Values<Object>(String.valueOf(inx++), word) );
						}
					}
                } catch (IOException e) {
	    		    e.printStackTrace();
	    	    }
            }    
        }else if(!sentEof){
            this.collector.emitEndOfStream();
	        sentEof = true;
        } 
        Thread.yield();
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("key", "value"));
    }


	@Override
	public String getExecutorId() {
		
		return executorId;
	}


	@Override
	public void setRouter(StreamRouter router) {
		this.collector.setRouter(router);
	}
	
	


}
