package com.planet_ink.siplet.applet;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.siplet.support.*;
import com.jcraft.jzlib.*;

import java.applet.Applet;
import java.awt.*;
import java.net.*;
import java.io.*;

/*
   Copyright 2000-2020 Bo Zimmerman

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
public class Siplet
{
	public final static boolean debugDataOut=false;

	public final static long serialVersionUID=7;
	public static final float VERSION_MAJOR=(float)2.3;
	public static final long  VERSION_MINOR=0;

	protected StringBuffer		buf			= new StringBuffer("");
	protected String			lastURL		= "coffeemud.net";
	protected int				lastPort	= 23;
	protected Socket			sock		= null;
	protected InputStream		rawin		= null;
	protected Reader[]			in			= new Reader[1];
	protected DataOutputStream	out;
	protected boolean			connected	= false;
	protected TelnetFilter		telnetFilter		= new TelnetFilter(this);

	protected StringBuffer		buffer;
	protected int				sillyCounter= 0;

	public enum MSPStatus
	{
		Disabled,
		Internal,
		External
	}

	public void setFeatures(final boolean mxp, final MSPStatus msp, final boolean mccp)
	{
		telnetFilter.setNeverMCCPSupport(!mccp);
		telnetFilter.setNeverMXPSupport(!mxp);
		telnetFilter.setNeverMSPSupport(msp);
	}

	public void init()
	{
		buffer = new StringBuffer();
	}

	public String info()
	{
		return "Siplet V"+VERSION_MAJOR+"."+VERSION_MINOR+" (C)2005-2020 Bo Zimmerman";
	}

	public void start()
	{
		if (debugDataOut)
			System.out.println("starting siplet " + VERSION_MAJOR + "." + VERSION_MINOR + " ");
	}

	public void stop()
	{
		if (debugDataOut)
			System.out.println("!stopped siplet!");
	}

	public void destroy()
	{
	}

	public Siplet create()
	{
		return new Siplet();
	}

	public void addItem(final String newWord)
	{
		if(debugDataOut) System.out.println(newWord);
		buffer.append(newWord);
		//repaint();
	}

	public void paint(final Graphics g)
	{
		// uncomment if we go back to being an applet
		//g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		//g.drawString(buffer.toString(), 5, 15);
	}

	public boolean connectToURL()
	{
		return connectToURL(lastURL, lastPort);
	}

	public boolean connectToURL(final String url, final int port)
	{
		connected=false;
		if(sock!=null)
		{
			disconnectFromURL();
		}
		try
		{
			lastURL=url;
			lastPort=port;
			if (debugDataOut)
				System.out.println("connecting to " + url + ":" + port + " ");
			sock = new Socket(InetAddress.getByName(url), port);
			Thread.sleep(100);
			rawin=sock.getInputStream();
			in[0] = new IACReader(rawin, CMProps.getVar(CMProps.Str.CHARSETINPUT));
			out = new DataOutputStream(sock.getOutputStream());
			telnetFilter = new TelnetFilter(this);
			connected=true;
		}
		catch(final Exception e)
		{
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	public boolean hasWaitingData()
	{
		try
		{
			return this.in[0] != null && (this.in[0].ready());
		}
		catch (final Exception e)
		{
			this.disconnectFromURL();
			return false;
		}
	}

	public boolean connectToURL(final String url, final int port, final Socket sock)
	{
		connected=false;
		if(this.sock!=null)
		{
			disconnectFromURL();
		}
		try
		{
			lastURL=url;
			lastPort=port;
			if (debugDataOut)
				System.out.println("internal connect to " + url + ":" + port + " ");
			this.sock=sock;
			rawin=sock.getInputStream();
			in[0] = new IACReader(rawin, CMProps.getVar(CMProps.Str.CHARSETINPUT));
			out = new DataOutputStream(sock.getOutputStream());
			telnetFilter=new TelnetFilter(this);
			connected=true;
		}
		catch(final Exception e)
		{
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	public void disconnectFromURL()
	{
		connected=false;
		try
		{
			if(out!=null)
			{
				out.write(new byte[]{(byte)255,(byte)253,18}); //iac, iacdo, logout
				out.flush();
			}
		}
		catch (final Exception e)
		{
		}
		try
		{
			if ((in != null) && (in[0] != null))
				in[0].close();
		}
		catch (final Exception e)
		{
		}
		try
		{
			if (out != null)
				out.close();
		}
		catch (final Exception e)
		{
		}
		try
		{
			if (sock != null)
				sock.close();
		}
		catch (final Exception e)
		{
		}
		in=null;
		out=null;
		sock=null;
	}

	public void sendData(final String data)
	{
		if(connected)
		{
			try
			{
				if(sock.isClosed())
				{
					disconnectFromURL();
				}
				else
				if(!sock.isConnected())
				{
					disconnectFromURL();
				}
				else
				{
					final byte[] bytes=telnetFilter.peruseInput(data);
					if(bytes!=null)
					{
						boolean success = false;
						int attempts=10000;
						while(connected && !success && (--attempts>0))
						{
							try
							{
								out.write(bytes);
								if((bytes.length==0)||((bytes[bytes.length-1]!=13)&&(bytes[bytes.length-1]!=10)))
									out.writeBytes("\n");
								out.flush();
								success=true;
							}
							catch(final IOException e)
							{
								try
								{
									Thread.sleep(1);
								}
								catch(final Exception e2)
								{
								}
							}
						}
						if(!success)
							throw new IOException("Failed to read.");
					}
				}
			}
			catch(final IOException e)
			{
				disconnectFromURL();
			}
		}
	}
	public String getJScriptCommands()
	{
		return telnetFilter.getEnquedJScript();
	}

	public String getURLData()
	{
		synchronized(buf)
		{
			final String s=telnetFilter.getEnquedResponses();
			if (s.length() > 0)
				sendData(s);
			final StringBuilder data=new StringBuilder("");
			if(telnetFilter.MSDPsupport())
				data.append(telnetFilter.getMsdpHtml());
			if(telnetFilter.GMCPsupport())
				data.append(telnetFilter.getGmcpHtml());
			int endAt=telnetFilter.HTMLFilter(buf);
			if (buf.length() == 0)
				return data.toString();
			if (endAt < 0)
				endAt = buf.length();
			if (endAt == 0)
				return data.toString();
			if (telnetFilter.isUIonHold())
				return data.toString();
			if(endAt<buf.length())
			{
				data.append(buf.substring(0,endAt));
				buf.delete(0,endAt);
			}
			else
			{
				data.append(buf.toString());
				buf.setLength(0);
			}
			if (debugDataOut)
			{
				if (data.length() > 0)
					System.out.println("/DATA=" + data.toString());
			}
			return data.toString();
		}
	}

	public boolean isConnectedToURL()
	{
		try
		{
			if(connected
			&&(!sock.isClosed())
			&&(sock.isConnected()))
				return true;
		}
		catch(final Exception e)
		{
			connected=false;
		}
		return false;
	}

	public void readURLData()
	{
		try
		{
			if(connected
			&&in[0].ready()
			&&(!sock.isClosed())
			&&(sock.isConnected()))
			{
				long last=System.currentTimeMillis();
				while(connected
				&&(!sock.isClosed())
				&&(sock.isConnected())
				&&(System.currentTimeMillis()-last)<250)
				{
					if(in[0].ready())
					{
						try
						{
							telnetFilter.TelnetRead(buf,rawin,in);
							last=System.currentTimeMillis();
						}
						catch(final java.io.InterruptedIOException e)
						{
							disconnectFromURL();
							return;
						}
						catch(final Exception e)
						{
							if(e instanceof com.jcraft.jzlib.ZStreamException)
							{
								disconnectFromURL();
								CMLib.s_sleep(100);
								connectToURL();
							}
							else
							{
								disconnectFromURL();
								return;
							}
						}
					}
					else
					{
						CMLib.s_sleep(1);
					}
				}
			}
			if(sock.isClosed())
			{
				disconnectFromURL();
			}
			else
			if(!sock.isConnected())
			{
				disconnectFromURL();
			}
			else
			if(buf.length()>0)
				telnetFilter.TelenetFilter(buf,out,rawin,in);

		}
		catch(final Exception e)
		{
			disconnectFromURL();
			return;
		}
	}
}
