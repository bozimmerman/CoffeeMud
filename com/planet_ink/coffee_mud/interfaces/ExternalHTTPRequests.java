package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface ExternalHTTPRequests
{
	public String ServerVersionString();
	public String getWebServerPortStr();
	//public HTTPserver getWebServer();
	public String getHTTPstatus();
	public String getHTTPstatusInfo();
	public String getHTTPclientIP();
	public String getWebServerPartialName();
	public Host getMUD();
	public String WebHelperhtmlPlayerList();
	public String WebHelperhtmlAreaTbl();
	public Hashtable getRequestParameters();
}
