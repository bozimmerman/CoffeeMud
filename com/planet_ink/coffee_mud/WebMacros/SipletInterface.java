package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.Pipe;
import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.siplet.applet.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class SipletInterface extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public boolean isAWebPath(){return true;}
	
	public static final LinkedList<String> removables		 = new LinkedList<String>();
	public static final Object 			   sipletConnectSync = new Object();
	public static volatile boolean 		   initialized		 = false;
	public static final SHashtable<String,SipletSession> 	 siplets 	= new SHashtable<String,SipletSession>(); 
	
	private class SipletSession
	{
		public long 		lastTouched = System.currentTimeMillis();
		public Siplet 		siplet		= null;
		public String   	response	= "";
		public SipletSession(Siplet sip) { siplet=sip;}
	}
	
	private class PipeSocket extends Socket
	{
		private boolean 			isClosed = false;
		private PipedInputStream 	inStream = new PipedInputStream();
		private PipedOutputStream 	outStream= new PipedOutputStream();
		private InetAddress			addr=null;
		private PipeSocket 			friendPipe=null;
		public PipeSocket(InetAddress addr, PipeSocket pipeLocal) throws IOException
		{
			this.addr=addr;
			if(pipeLocal!=null)
			{
				pipeLocal.inStream.connect(outStream);
				pipeLocal.outStream.connect(inStream);
				friendPipe=pipeLocal;
				pipeLocal=friendPipe;
			}
		}
		public void shutdownInput() throws IOException  
		{ 
			inStream.close(); 
			isClosed=true; 
		}
		public void shutdownOutput() throws IOException 
		{ 
			outStream.close(); 
			isClosed=true; 
		}
		public boolean isConnected() { return !isClosed; }
		public boolean isClosed() { return isClosed; }
		public synchronized void close() throws IOException 
		{
			inStream.close();
			outStream.close();
			if(friendPipe!=null)
			{
				friendPipe.shutdownInput();
				friendPipe.shutdownOutput();
			}
			isClosed = true;
		}
		public InputStream getInputStream() throws IOException { return inStream; }
		public OutputStream getOutputStream() throws IOException { return outStream; }
		public InetAddress getInetAddress() { return addr; }
	}
	
	public String runMacro(HTTPRequest httpReq, String parm) throws HTTPServerException
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return "false;";
		if(!initialized)
		{
			initialized=true;
			CMLib.threads().startTickDown(new Tickable(){
				private long tickStatus=Tickable.STATUS_NOT;
				public long getTickStatus() { return tickStatus;}
				public String name() { return "SipletInterface";}
				public boolean tick(Tickable ticking, int tickID) 
				{
					tickStatus=Tickable.STATUS_ALIVE;
					synchronized(siplets)
					{
						for(final String key : siplets.keySet())
						{
							SipletSession p = siplets.get(key);
							if((p!=null)&&((System.currentTimeMillis()-p.lastTouched)>(2 * 60 * 1000)))
							{
								p.siplet.disconnectFromURL();
								removables.addLast(key);
							}
						}
						if(removables.size()>0)
						{
							for(final String remme : removables)
								siplets.remove(remme);
							removables.clear();
						}
					}
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
				public String ID() { return "SipletInterface";}
				public CMObject copyOf() { return this;}
				public void initializeClass() {}
				public CMObject newInstance() { return this;}
				public int compareTo(CMObject o) { return o==this?0:1;}
			}, Tickable.TICKID_MISCELLANEOUS, 10);
		}
		
		if(httpReq.isUrlParameter("CONNECT"))
		{
			String url=httpReq.getUrlParameter("URL");
			int port=CMath.s_int(httpReq.getUrlParameter("PORT"));
			String hex="";
			Siplet sip = new Siplet();
			boolean success=false;
			if(url!=null)
			{
				sip.init();
				synchronized(sipletConnectSync)
				{
					for(MudHost h : CMLib.hosts())
						if(h.getPort()==port)
						{
							try
							{
								PipeSocket lsock=new PipeSocket(httpReq.getClientAddress(),null);
								PipeSocket rsock=new PipeSocket(httpReq.getClientAddress(),lsock);
								success=sip.connectToURL(url, port,lsock);
								sip.setFeatures(true, Siplet.MSPStatus.External, false);
								h.acceptConnection(rsock);
							}
							catch(IOException e)
							{
								success=false;
							}
						}
				}
				if(success)
				{
					synchronized(siplets)
					{
						int tokenNum=0;
						int tries=1000;
						while((tokenNum==0)&&((--tries)>0))
						{
							tokenNum = new Random().nextInt();
							if(tokenNum<0) tokenNum = tokenNum * -1;
							hex=Integer.toHexString(tokenNum);
							if(httpReq.isUrlParameter(hex))
								tokenNum=0;
						}
						siplets.put(hex, new SipletSession(sip));
					}
				}
			}
			return Boolean.toString(success)+';'+hex+';'+sip.info()+hex+';';
		}
		else
		if(httpReq.isUrlParameter("DISCONNECT"))
		{
			String token=httpReq.getUrlParameter("TOKEN");
			boolean success = false;
			if(token != null)
			{
				SipletSession p = siplets.get(token);
				if(p!=null)
				{
					siplets.remove(token);
					p.siplet.disconnectFromURL();
					success=true;
				}
			}
			return Boolean.toString(success)+';';
		}
		else
		if(httpReq.isUrlParameter("SENDDATA"))
		{
			String token=httpReq.getUrlParameter("TOKEN");
			boolean success = false;
			if(token != null)
			{
				SipletSession p = siplets.get(token);
				if(p!=null)
				{
					String data=httpReq.getUrlParameter("DATA");
					if(data!=null)
					{
						p.lastTouched=System.currentTimeMillis();
						p.siplet.sendData(data);
						success=p.siplet.isConnectedToURL();
					}
				}
			}
			return Boolean.toString(success)+';';
		}
		else
		if(httpReq.isUrlParameter("POLL"))
		{
			final String token=httpReq.getUrlParameter("TOKEN");
			if(token != null)
			{
				final SipletSession p = siplets.get(token);
				if(p!=null)
				{
					if(p.siplet.isConnectedToURL())
					{
						if(httpReq.isUrlParameter("LAST"))
							return p.response;
						else
						{
							p.lastTouched=System.currentTimeMillis();
							p.siplet.readURLData();
							final String data = p.siplet.getURLData();
							final String jscript = p.siplet.getJScriptCommands();
							final boolean success=p.siplet.isConnectedToURL();
							p.response=Boolean.toString(success)+';'+data+token+';'+jscript+token+';';
							return p.response;
						}
					}
				}
			}
			return "false;"+token+";"+token+";";
		}
		return "false;";
	}
}
