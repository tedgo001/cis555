package edu.upenn.cis455.mapreduce.worker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class WorkWrapper extends Thread {
	
	  	static int myPortNum;
	    private static String masterAddr;
		private static boolean running = true;
	    private static String currState = workerStatus.IDLE.getState();//status: mapping,waiting,reducing,idle
		private static String job = "";
		private static int keysRead = 0;
		private static int keysWritten = 0;
		private static List<String> results = new ArrayList<String>();
		
		public enum workerStatus{
	        MAPPING("mapping"), WAITING("waiting"), REDUCING("reducing"), IDLE("idle");
	        private String name;
	        private workerStatus(String name){
	            this.name = name;
	        }
	        public String getState(){
	            return this.name;
	        }
		}
		
		public WorkWrapper(int portNum, String addr){
		    myPortNum = portNum;
		    masterAddr = addr;
		}
	    
	    public void run(){
		    while(running){
		        //synchronize
		        currState = WorkerServer.getWorkerStatus();//status: mapping,waiting,reducing,idle
	            job = WorkerServer.getCurrJob();
	            keysRead = WorkerServer.getKeysRead();
	            keysWritten = WorkerServer.getKeysWritten();
	            results = WorkerServer.getResults();
		        
		       
		        String urlString;
		        if(masterAddr.startsWith("http")){
		            urlString = masterAddr + "/workerstatus";
		        } 
		        else{
		            urlString = "http://" + masterAddr + "/workerstatus";   
		        }
		        // hardcode queryParams (maybe not a elegant idea?)
		        String queryParams = "port=" + myPortNum +"&status="+ currState
								+ "&job=" + job + "&read=" + keysRead +"&written=" 
								+ keysWritten;
								//+ "&results=" + getResults(results);
				try{
				    URL url = new URL(urlString + "?" + queryParams);
					HttpURLConnection httpconnection = (HttpURLConnection)url.openConnection();
					httpconnection.setRequestMethod("GET");
					httpconnection.setDoOutput(true);
					httpconnection.getResponseCode();
					//System.out.println("[port:" + myPort + "] Response:" + httpconnection.getResponseCode());
				} catch(IOException e){
					e.getStackTrace();
					System.out.println(e.getMessage());
				}
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		}
		
//		public static String getResults(List<String> results){
//			StringBuilder sb = new StringBuilder();
//			int count = 0;
//			for(String str : results) {
//			    if(count >= 100) break;
//				if(sb.length() != 0) sb.append(",");
//				sb.append(str);
//				count++;
//			}
//			return "[" + sb.toString() + "]";
//		}

}
