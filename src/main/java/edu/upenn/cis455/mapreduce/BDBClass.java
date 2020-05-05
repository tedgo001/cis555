package edu.upenn.cis455.mapreduce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.bind.serial.StoredClassCatalog;

import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.serial.SerialBinding;

import edu.upenn.cis455.mapreduce.WordHelper;

public class BDBClass {

	  private String homeDire = null;
		private Environment myEnv;
		
		private StoredClassCatalog javaCatalog;
		
		private static final String CLASS_CATALOG = "java_class_catalog";
		private static final String WORD_STORE = "word_store";
		
		private Database wordDb;
		
		private StoredSortedMap<String,WordHelper> words;
		
		public BDBClass(String homeDir){
			homeDire = homeDir;
			File file = new File(homeDir);
	        if (!file.exists()) {
	            file.mkdir();
	        }
			
			EnvironmentConfig myEnvConfig = new EnvironmentConfig();
			myEnvConfig.setAllowCreate(true);
			myEnvConfig.setTransactional(true);
			myEnv = new Environment(new File(homeDir), myEnvConfig);
			
			DatabaseConfig dbConfig = new DatabaseConfig();
	        dbConfig.setTransactional(true);
	        dbConfig.setAllowCreate(true);
	        Database catalogDb = myEnv.openDatabase(null, CLASS_CATALOG, dbConfig);
	        
	        javaCatalog = new StoredClassCatalog(catalogDb);
	        
			wordDb = myEnv.openDatabase(null, WORD_STORE, dbConfig);
			
			EntryBinding<String> stringBinding = new StringBinding();
			EntryBinding<WordHelper> dicBinding = new SerialBinding<WordHelper>(javaCatalog, WordHelper.class);
			
			words = new StoredSortedMap<String,WordHelper>(wordDb, stringBinding, dicBinding, true);
			//System.out.println("Berkeley DB now has been set up");
			
		}
		
		public void addWord(String word, String countNum){
		    if(words.keySet().contains(word)){
		        WordHelper wordHel = words.get(word);
	    		wordHel.addCount(countNum);
	    		words.put(word,wordHel);
		    } 
		    else{
		        WordHelper wordHel = new WordHelper(word);
	    		wordHel.addCount(countNum);
	    		words.put(word, wordHel);
		    }
		}
		
		public List<String> getWords(){
		    List<String> wordList = new ArrayList<>();
			for(String word : words.keySet()){
				wordList.add(word);
			}
			return wordList;
		}
		
		public List<String> getCount(String word){
			return words.get(word).getCount();
		}
		
		
		public void close() throws DatabaseException{
	        wordDb.close();
	        javaCatalog.close();
	        myEnv.close();
	    } 
	    
	    public void updateDB() {
			if(myEnv != null){
				myEnv.sync();
			}
		}
	
	
}
