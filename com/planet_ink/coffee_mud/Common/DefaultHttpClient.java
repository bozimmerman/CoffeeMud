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
	protected Map<String,String> reqHeaders=new CaselessTreeMap<String>();
	protected Map<String,List<String>> respHeaders=new CaselessTreeMap<List<String>>();
	protected Socket sock = null;
	protected OutputStream out = null;
	protected InputStream in = null;
	protected Method meth = Method.GET;
	protected int connectTimeout=10000;
	protected int readTimeout=10000;
	protected int maxReadBytes=0;
	protected byte[] outBody=null;
	protected Integer respStatus=null;
	
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(Exception e)
		{
			return new DefaultHttpClient();
		}
	}
	public void initializeClass(){}

	public HttpClient header(String key, String value)
	{
		reqHeaders.put(key, value);
		return this;
	}
	
	public HttpClient method(Method meth)
	{
		if(meth!=null)
		{
			this.meth=meth;
		}
		return this;
	}
	
	public HttpClient body(String body)
	{
		if(body!=null)
		{
			this.outBody=body.getBytes();
		}
		return this;
	}
	public HttpClient body(byte[] body)
	{
		if(body!=null)
		{
			this.outBody=body;
		}
		return this;
	}
	
	public HttpClient reset()
	{
		reqHeaders.clear();
		respHeaders.clear();
		respStatus=null;
		return this;
	}
	
	public HttpClient connectTimeout(int ms) {
		this.connectTimeout=ms;
		return this;
	}
	public HttpClient readTimeout(int ms) {
		this.readTimeout=ms;
		return this;
	}
	public HttpClient maxReadBytes(int bytes) {
		this.maxReadBytes=bytes;
		return this;
	}
	
	protected void conditionalHeader(String key, String value, List<String> clearSet)
	{
		if(!reqHeaders.containsKey(key))
		{
			reqHeaders.put(key, value);
			clearSet.add(key);
		}
	}
	
	public int getResponseCode()
	{
		if(this.respStatus!=null)
			return this.respStatus.intValue();
		return -1;
	}
	
	public Map<String,List<String>> getResponseHeaders()
	{
		return this.respHeaders;
	}
	
	public HttpClient doRequest(String url) throws IOException
	{
		respHeaders.clear();
		respStatus=null;
		outBody=null;
		if(url == null) throw new IOException("Bad url");
		final boolean ssl=url.toLowerCase().startsWith("https");
		if(ssl) throw new IOException("Unsupported: ssl");
		int x=url.indexOf("://");
		if(x>=0) url=url.substring(x+3);
		String host;
		String rest="/";
		x=url.indexOf('/');
		if(x<0) x=url.indexOf('?');
		if(x>=0)
		{
			host=url.substring(0,x);
			rest=url.substring(x);
		}
		else
			host=url;
		int port=ssl?443:80;
		x=host.indexOf(':');
		if(x>=0)
		{
			port=CMath.s_int(host.substring(x+1),80);
			host=host.substring(0,x);
		}
		List<String> onesToClear=new Vector<String>();
		conditionalHeader("Host",host,onesToClear);
		conditionalHeader("Connection","Keep-Alive",onesToClear);
		conditionalHeader("Accept","*/*",onesToClear);
		int len=(outBody!=null)?outBody.length:0;
		conditionalHeader("Content-Length",""+len,onesToClear);
		if(sock == null)
		{
			sock=new Socket();
			sock.connect(new InetSocketAddress(host,port), this.connectTimeout);
			in=sock.getInputStream();
			sock.setSoTimeout(10);
			out=sock.getOutputStream();
		}
		final IOException cleanException=new IOException("Connection closed by remote host");
		try { 
			while(in.read()!=-1); /* clear the stream */
			throw cleanException;
		} catch(IOException e) { if(e==cleanException) throw e; }
		out.write((meth.toString()+" "+rest+" HTTP/1.1\n").getBytes());
		for(String key : reqHeaders.keySet())
			out.write((key+": "+reqHeaders.get(key)+"\n").getBytes());
		for(String key : onesToClear)
			reqHeaders.remove(key);
		out.write("\n".getBytes());
		if(outBody!=null)
			out.write(outBody);
		long nextReadTimeout=(this.readTimeout>0)?(System.currentTimeMillis()+this.readTimeout):Long.MAX_VALUE;
		int lastC=-1;
		boolean postHeader=false;
		boolean firstLineReceived=false;
		StringBuilder lineBuilder=new StringBuilder("");
		ByteArrayOutputStream bodyBuilder=new ByteArrayOutputStream();
		int c=0;
		int maxBytes=this.maxReadBytes;
		while(c!=-1)
		{
			try
			{
				lastC=c;
				c=in.read();
				if(this.readTimeout>0)
					nextReadTimeout=(System.currentTimeMillis()+this.readTimeout);
			}
			catch(IOException e)
			{
				if(e instanceof java.net.SocketTimeoutException)
				{
					if(System.currentTimeMillis()>nextReadTimeout)
						throw e;
					continue;
				}
				else 
					throw e;
			}
			if(postHeader)
			{
				bodyBuilder.write(c);
				if((maxBytes==0)||(bodyBuilder.size()>=maxBytes))
					break;
			}
			else
			if((c=='\n')&&(lastC=='\n'))
			{
				postHeader=true;
				if(maxBytes==0)
					break;
			}
			else
			if(c=='\n')
			{
				String s=lineBuilder.toString();
				lineBuilder.setLength(0);
				if(!firstLineReceived)
				{
					firstLineReceived=true;
					String[] parts=s.split(" ", 3);
					if(CMath.isInteger(parts[1]))
						respStatus=Integer.valueOf(CMath.s_int(s.trim()));
					else
						respStatus=Integer.valueOf(-1);
				}
				else
				{
					x=s.indexOf(':');
					if(x>0)
					{
						String key=s.substring(0,x).trim();
						String value=s.substring(x+1).trim();
						if((key.equalsIgnoreCase("Content-Length"))
						&&(CMath.isInteger(value)))
						{
							int possMax=CMath.s_int(value);
							if((maxBytes==0)||(possMax<maxBytes))
								maxBytes=possMax;
						}
						if((key.equalsIgnoreCase("Transer-Encoding"))
						&&(value.equalsIgnoreCase("chunked")))
						{
							if(!respHeaders.containsKey("Content-Length"))
								maxBytes=Integer.MAX_VALUE;
						}
						List<String> list;
						if(respHeaders.containsKey(key))
							list=respHeaders.get(key);
						else
						{
							list=new ArrayList<String>();
							respHeaders.put(key, list);
						}
						list.add(value);
					}
				}
			}
			else
				lineBuilder.append((char)c);
		}
		this.outBody=bodyBuilder.toByteArray();
		return this;
	}
	
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
	
	public int getResponseContentLength()
	{
		if(this.outBody!=null)
			return this.outBody.length;
		return 0;
	}
	
	public InputStream getResponseBody()
	{
		if(this.outBody!=null)
			return new ByteArrayInputStream(this.outBody);
		return new ByteArrayInputStream(new byte[0]);
		
	}
	
	public HttpClient doGet(String url) throws IOException
	{
		return this.method(Method.GET).doRequest(url);
	}
	
	public HttpClient doHead(String url) throws IOException
	{
		return this.method(Method.HEAD).doRequest(url);
	}

	
	public byte[] getRawUrl(final String urlStr, String cookieStr, final int maxLength, final int readTimeout)
	{
		HttpClient h=null;
		try {
			h=this.readTimeout(readTimeout).connectTimeout(readTimeout).method(Method.GET);
			if((cookieStr!=null)&&(cookieStr.length()>0))
				h=h.header("Cookie", cookieStr);
			h.doRequest(urlStr);
			if (h.getResponseCode() == 302) {
				for(String key : h.getResponseHeaders().keySet())
				{
					System.out.println(key+"="+h.getResponseHeaders().get(key));
				}
				InputStream in=h.getResponseBody();
				int len=h.getResponseContentLength();
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
				
			if (h.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream in=h.getResponseBody();
				int len=h.getResponseContentLength();
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
			if(h!=null)
				h.finished();
		}
		return null;
	}

	public void finished()
	{
		if(sock!=null)
		{
			try
			{
				sock.shutdownInput();
				sock.shutdownOutput();
				sock.close();
			}
			catch(Exception e) {}
			finally
			{
				sock=null;
				in=null;
				out=null;
			}
		}
	}

	
	public Map<String,List<String>> getHeaders(final String urlStr)
	{
		HttpClient h=null;
		try {
			h=this.readTimeout(3000).connectTimeout(3000).method(Method.GET);
			h.doRequest(urlStr);
			return h.getResponseHeaders();
		} catch (Exception e) {
			Log.errOut("HttpClient",e);
			return null;
		}
		finally {
			if(h!=null)
				h.finished();
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
