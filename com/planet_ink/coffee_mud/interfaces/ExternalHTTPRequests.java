package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.exceptions.*;
import java.net.InetAddress;
/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
