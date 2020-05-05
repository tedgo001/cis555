package edu.upenn.cis455.mapreduce.job;

import java.util.Iterator;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class MyJob implements Job {

	@Override
	public void map(String key, String value, Context context) {
		// TODO Auto-generated method stub
		String[] allWords = value.split("\n");
		for(String word : allWords){
		    word = word.replaceAll(" ","");
		    if(!word.equals("")){
		        context.write(word,"1");
		    }
		}
	}

	@Override
	public void reduce(String key, Iterator<String> values, Context context) {
		// TODO Auto-generated method stub
		int cnt = 0;
		while(values.hasNext()){
		    String word = values.next();
		    cnt += Integer.valueOf(word);
		}
		context.write(key, Integer.toString(cnt));
	}

}
