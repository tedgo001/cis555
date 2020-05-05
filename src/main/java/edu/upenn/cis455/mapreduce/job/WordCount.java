package edu.upenn.cis455.mapreduce.job;

import java.util.Iterator;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {

  public void map(String key, String value, Context context)
  {
    // Your map function for WordCount goes here
		String[] allWords = value.split("\n");
		for(String word : allWords){
		    word = word.replaceAll(" ","");
		    if(!word.equals("")){
		        context.write(word,"1");
		    }
		}

  }
  
  public void reduce(String key, Iterator<String> values, Context context)
  {
    // Your reduce function for WordCount goes here
		int cnt = 0;
		while(values.hasNext()){
		    String word = values.next();
		    cnt += Integer.valueOf(word);
		}
		context.write(key, Integer.toString(cnt));

  }
  
}
