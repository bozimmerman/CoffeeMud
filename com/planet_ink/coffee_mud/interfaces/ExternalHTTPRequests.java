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
	public Hashtable getRequestParameters();
	public void addRequestParameters(String key, String value);
	public void resetRequestEncodedParameters();
	public String getRequestEncodedParameters();
	public InetAddress getServerAddress();
	public String getPageContent(String filename);
	public Hashtable getRequestObjects();
	public Vector cache();
}
