package edu.upenn.cis455.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChannelWrapper implements Serializable {

	private String channelName;
	private String userName;
	private String XPath;
	private List<String> wantedUrls;
	private String creatorName;
	
	public ChannelWrapper(){
		
	}
	
	public ChannelWrapper(String channelName,String userName,String XPath){
		this.channelName = channelName;
		this.userName = userName;
		this.XPath = XPath;
		this.wantedUrls = new ArrayList<String>();
	}

	public String getChannelName() {
		return channelName;
	}

	public String getUserName() {
		return userName;
	}

	public String getXPath() {
		return XPath;
	}

	public List<String> getWantedUrls() {
		return wantedUrls;
	}
	
	public void setWantedUrls(String url){
		if(!wantedUrls.contains(url)){
			wantedUrls.add(url);
		}
	}
	
	public int hashCode() {
		return this.channelName != null ? this.channelName.hashCode() : 0;
	}
	
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj==null || this.getClass() != obj.getClass()) {
			return false;
		}
		if(!(obj instanceof ChannelWrapper)) {
			return false;
		}
		ChannelWrapper anotherDoc = (ChannelWrapper) obj;
		if(this.channelName != null ? !this.channelName.equals(anotherDoc.channelName):anotherDoc.channelName != null) {
			return false;
		}
		return true;
	}

	public String getCreatorName() {
		// TODO Auto-generated method stub
		return creatorName;
	}
	
	
	
}
