package edu.upenn.cis455.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


public class DBConfiguration {
	
	
	
	public void configure() {
		
		// connecting to the S3
		AWSCredentials credentials = new BasicAWSCredentials(
				  "<AWS accesskey>", 
				  "<AWS secretkey>"
				);
		
		
	AmazonS3 s3client = AmazonS3ClientBuilder
			  .standard()
			  .withCredentials(new AWSStaticCredentialsProvider(credentials))
			  .withRegion(Regions.US_EAST_2)
			  .build();
	}
	
	public void addInfo() {
		// add everthing to the bucket in S3
	}
	

}
