package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.exceptions.*;
import java.net.InetAddress;
public interface ExternalHTTPRequests
{
	public byte [] doVirtualPage(byte [] data)
		throws HTTPRedirectException;
	public String ServerVersionString();
	public String getWebServerPortStr();
	public String getHTTPstatus();
	public String getHTTPstatusInfo();
	public String getHTTPclientIP();
	public String getWebServerPartialName();
	public File grabFile(String filename);
	public MudHost getMUD();
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
