package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.net.InetAddress;
public interface ExternalHTTPRequests
{
	public String ServerVersionString();
	public String getWebServerPortStr();
	public String getHTTPstatus();
	public String getHTTPstatusInfo();
	public String getHTTPclientIP();
	public String getWebServerPartialName();
	public Host getMUD();
	public String WebHelperhtmlPlayerList();
	public String WebHelperhtmlAreaTbl();
	public boolean isRequestParameter(String key);
	public String getRequestParameter(String key);
	public void removeRequestParameter(String key);
	public void addRequestParameters(String key, String value);
	public Hashtable getVirtualDirectories();
	public String getRequestEncodedParameters();
	public InetAddress getServerAddress();
	public String getPageContent(String filename);
	public Hashtable getRequestObjects();
}
