//package edu.upenn.cis.stormlite.spout;
//
//import edu.upenn.cis.stormlite.spout.FileSpout;
//import edu.upenn.cis455.mapreduce.worker.WorkerServer;
//
//public class FileSpoutHelper extends FileSpout {
//
//	  private String fileName = "words.txt";
//
//		@Override
//		public String getFilename() {
//			//return fileName;
//		    String input = (String)this.confMap.get("inputDir");
//			return  WorkerServer.getStoredDir() + "/" + input + "/" + fileName;
//		}
//	
//}
