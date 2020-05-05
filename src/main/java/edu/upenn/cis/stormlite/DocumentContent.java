package edu.upenn.cis.stormlite;

public class DocumentContent {
	private String url;
	private String contentType;
	private String content;
	private long lastcrawlTime;

	public DocumentContent(){
		
	}

	public DocumentContent(String url, String contentType, String content, long lastCrawlTime)
	{
		this.url = url;
		this.contentType = contentType;
		this.content = content;
		this.lastcrawlTime = lastCrawlTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getLastcrawlTime() {
		return lastcrawlTime;
	}

	public void setLastcrawlTime(long lastcrawlTime) {
		this.lastcrawlTime = lastcrawlTime;
	}


	
	
	

}
