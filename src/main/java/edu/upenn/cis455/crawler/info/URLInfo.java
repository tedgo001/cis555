package edu.upenn.cis455.crawler.info;

/** (MS1, MS2) Holds information about a URL.
  */
public class URLInfo {
	private String hostName;
	private int portNo;
	private String filePath;
	private boolean isEncrpted = false; // use to check http/https
	private String urlString;
	
	/**
	 * Constructor called with raw URL as input - parses URL to obtain host name and file path
	 */
	public URLInfo(String docURL){
		urlString = docURL;
		if(docURL == null || docURL.equals("")) {
			System.out.println("docURL is empty");
			return;
		}
		docURL = docURL.trim();
		if(!docURL.startsWith("http://") || docURL.length() < 8) {
			return;
		}
		if(docURL.startsWith("https://")) {
			this.isEncrpted = true;
			docURL = docURL.replaceFirst("https:", "http");
		}
		// Stripping off 'http://'
		docURL = docURL.substring(7);
		/*If starting with 'www.' , stripping that off too
		 * shouldn't start www, based on the instruction
		if(docURL.startsWith("www."))
			docURL = docURL.substring(4);*/
		int i = 0;
		while(i < docURL.length()){
			char c = docURL.charAt(i);
			if(c == '/')
				break;
			i++;
		}
		String address = docURL.substring(0,i);
		if(i == docURL.length()) {
			filePath = "/";
		}
		else {
			filePath = docURL.substring(i); //starts with '/'
		}
		if(address.equals("/") || address.equals("")) {
			return;
		}
		if(address.indexOf(':') != -1){
			String[] comp = address.split(":",2);

			//System.out.println("comp: "+comp);
			hostName = comp[0].trim();
			try{
				portNo = Integer.parseInt(comp[1].trim());
			}catch(NumberFormatException nfe){
				portNo = 8000;
			}
		}else{
			hostName = address;
			portNo = 8000;
		}
	}
	
	public URLInfo(String hostName, String filePath){
		this.hostName = hostName;
		this.filePath = filePath;
		this.portNo = 8000;
	}
	
	public URLInfo(String hostName,int portNo,String filePath){
		this.hostName = hostName;
		this.portNo = portNo;
		this.filePath = filePath;
	}
	public URLInfo(String hostName,int portNo,boolean isEncrypted){
		this.hostName = hostName;
		this.portNo = portNo;
		this.isEncrpted = isEncrypted;
	}
	
	public String getHostName(){ 
		return hostName;
	}
	public String getSite() { //		http://127.0.0.1
		return hostName.split("://")[1]; // 127.0.0.1
	}
	
	public void setHostName(String s){
		hostName = s;
	}
	
	public int getPortNo(){
		return portNo;
	}
	
	public void setPortNo(int p){
		portNo = p;
	}
	
	public String getFilePath(){
		return filePath;
	}
	
	public void setFilePath(String fp){
		filePath = fp;
	}

	public boolean isEncrypted() {
		// TODO Auto-generated method stub
		return this.isEncrpted;
	}
	public void setEncrypted(boolean ifEncrypted) {
		this.isEncrpted = ifEncrypted;
	}
	public String urlString() {
		return urlString;
	}
	public String getDomain(){
        if(getPortNo()!= 8000 && getPortNo()!= 443){
            return getHostName() + ":" + getPortNo();
        }
        if(isEncrypted()){
            return getHostName() + ":" + 443;
        }else{
            return getHostName() + ":" + 8000;   
        }
    }
	
	public int getConnectPortNum(){
		if(getPortNo()!= 8000 && getPortNo()!= 443){
            return getPortNo();
        }
        if(isEncrypted()){
            return 443;
        }else{
            return 8000;   
        }
	} 
	
}
