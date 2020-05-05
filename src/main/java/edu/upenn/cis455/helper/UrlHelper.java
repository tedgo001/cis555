package edu.upenn.cis455.helper;
import java.io.Serializable;

public class UrlHelper implements Serializable {

	private String url;
	private Integer docId;
	private String lastModifiedTime;
	
	public UrlHelper(Integer docId, String url, String lastModifiedTime) {
		this.url = url;
		this.docId = docId;
		this.lastModifiedTime = lastModifiedTime;
	}
	
	public final String getUrl() {
		return this.url;
	}
	public final Integer getDocId() {
		return this.docId;
		
	}
	public final String getLastModifiedTime() {
		return this.lastModifiedTime;
	}
	
	public String toString() {
		return "[Url: url is: " + this.url + ", docId = " + this.docId + ", and lastModifiedTime = " + this.lastModifiedTime + " ]";
		
	}
	public int hashCode() {
		return this.docId != null ? this.docId.hashCode() : 0;
		
	}
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		}
		if(obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		if(!(obj instanceof UrlHelper)) {
			return false;
		}
		UrlHelper anotherUrl = (UrlHelper) obj;
		if(this.docId != null ? !this.docId.equals(anotherUrl.docId) : anotherUrl.docId != null) {
			return false;
		}
		return true;
	}
	
}
