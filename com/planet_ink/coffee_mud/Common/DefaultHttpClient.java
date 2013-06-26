package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.CoffeeMudException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.io.*;
import java.util.*;

/* 
   Copyright 2013-2013 Bo Zimmerman

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

public class DefaultHttpClient implements HttpClient, Cloneable
{
	public String ID(){return "DefaultHttpClient";}
	public String name() { return ID();}
	private volatile long tickStatus=Tickable.STATUS_NOT;
	
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(Exception e)
		{
			return new DefaultCharStats();
		}
	}
	public void initializeClass(){}

	public byte[] getRawUrl(final String urlStr, String cookieStr)
	{
		return getRawUrl(urlStr, cookieStr, 1024*1024*10, 10000);
	}
	public byte[] getRawUrl(final String urlStr)
	{
		return getRawUrl(urlStr, null, 1024*1024*10, 10000);
	}
	public byte[] getRawUrl(final String urlStr, final int maxLength, final int readTimeout)
	{
		return getRawUrl(urlStr, null, maxLength, readTimeout);
	}
	
	public byte[] getRawUrl(final String urlStr, String cookieStr, final int maxLength, final int readTimeout)
	{
		HttpURLConnection connection=null;
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(readTimeout);
			connection.setInstanceFollowRedirects(true);
			connection.setConnectTimeout(readTimeout);
			connection.setRequestMethod("GET");
			if((cookieStr!=null)&&(cookieStr.length()>0))
				connection.setRequestProperty("Cookie", cookieStr);
			connection.connect();
			if (connection.getResponseCode() == 302) {
				for(String key : connection.getHeaderFields().keySet())
				{
					System.out.println(key+"="+connection.getHeaderField(key));
				}
				InputStream in=connection.getInputStream();
				int len=connection.getContentLength();
				if((len > 0)&&((maxLength==0)||(len<=maxLength)))
				{
					byte[] buf=new byte[len];
					int read=in.read(buf);
					int readTotal=read;
					while(readTotal < len)
					{
						if(read<0)
							return null;
						read=in.read(buf, readTotal, len-readTotal);
						if(read>0)
							readTotal+=read;
					}
					return buf;
				}
			}
				
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream in=connection.getInputStream();
				int len=connection.getContentLength();
				if((len > 0)&&((maxLength==0)||(len<=maxLength)))
				{
					byte[] buf=new byte[len];
					int read=in.read(buf);
					int readTotal=read;
					while(readTotal < len)
					{
						if(read<0)
							return null;
						read=in.read(buf, readTotal, len-readTotal);
						if(read>0)
							readTotal+=read;
					}
					return buf;
				}
			}
		} catch (Exception e) {
			Log.errOut("HttpClient",e);
			return null;
		}
		finally {
			if(connection!=null)
				connection.disconnect();
		}
		return null;
	}

	public Map<String,List<String>> getHeaders(final String urlStr)
	{
		HttpURLConnection connection=null;
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(3000);
			connection.setInstanceFollowRedirects(true);
			connection.setConnectTimeout(3000);
			connection.setRequestMethod("GET");
			//connection.setDoInput(false);
			//connection.setDoOutput(false);
			connection.connect();
			return connection.getHeaderFields();
		} catch (Exception e) {
			Log.errOut("HttpClient",e);
			return null;
		}
		finally {
			if(connection!=null)
				connection.disconnect();
		}
	}

	@Override
    public long getTickStatus() { return tickStatus; }

	@Override
	public boolean tick(Tickable ticking, int tickID) {
		return false;
	}

	@Override
	public CMObject copyOf() { try { return (CMObject)this.clone(); } catch (CloneNotSupportedException e) { return this; } }

	public int compareTo(CMObject o)
	{
		final int comp=CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
		return (comp==0)?((this==o)?0:1):comp;
	}
}
