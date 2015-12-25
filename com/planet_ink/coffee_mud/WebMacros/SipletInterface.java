package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWDataBuffers;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.siplet.applet.*;

/*
   Copyright 2011-2015 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "SipletInterface";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	public static final LinkedList<String> removables		 = new LinkedList<String>();
	public static final Object 			   sipletConnectSync = new Object();
	public static volatile boolean 		   initialized		 = false;
	public static final SHashtable<String,SipletSession> 	 siplets 	= new SHashtable<String,SipletSession>();

	protected class SipletSession
	{
		public long 		lastTouched = System.currentTimeMillis();
		public Siplet 		siplet		= null;
		public String   	response	= "";
		public SipletSession(Siplet sip) { siplet=sip;}
	}

	protected class PipeSocket extends Socket
	{
		private boolean					isClosed	= false;
		private final PipedInputStream	inStream	= new PipedInputStream();
		private final PipedOutputStream	outStream	= new PipedOutputStream();
		private InetAddress				addr		= null;
		private PipeSocket				friendPipe	= null;

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

		@Override
		public void shutdownInput() throws IOException
		{
			inStream.close();
			isClosed = true;
		}

		@Override
		public void shutdownOutput() throws IOException
		{
			outStream.close();
			isClosed = true;
		}

		@Override
		public boolean isConnected()
		{
			return !isClosed;
		}

		@Override
		public boolean isClosed()
		{
			return isClosed;
		}

		@Override
		public synchronized void close() throws IOException
		{
			inStream.close();
			outStream.close();
			if (friendPipe != null)
			{
				friendPipe.shutdownInput();
				friendPipe.shutdownOutput();
			}
			isClosed = true;
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			return inStream;
		}

		@Override
		public OutputStream getOutputStream() throws IOException
		{
			return outStream;
		}

		@Override
		public InetAddress getInetAddress()
		{
			return addr;
		}
	}
	
	protected void initialize()
	{
		initialized = true;
		CMLib.threads().startTickDown(new Tickable()
		{
			private int	tickStatus	= Tickable.STATUS_NOT;

			@Override
			public int getTickStatus()
			{
				return tickStatus;
			}

			@Override
			public String name()
			{
				return "SipletInterface";
			}

			@Override
			public boolean tick(Tickable ticking, int tickID)
			{
				tickStatus = Tickable.STATUS_ALIVE;
				synchronized (siplets)
				{
					for (final String key : siplets.keySet())
					{
						final SipletSession p = siplets.get(key);
						if ((p != null) && ((System.currentTimeMillis() - p.lastTouched) > (2 * 60 * 1000)))
						{
							p.siplet.disconnectFromURL();
							removables.addLast(key);
						}
					}
					if (removables.size() > 0)
					{
						for (final String remme : removables)
							siplets.remove(remme);
						removables.clear();
					}
				}
				tickStatus = Tickable.STATUS_NOT;
				return true;
			}

			@Override
			public String ID()
			{
				return "SipletInterface";
			}

			@Override
			public CMObject copyOf()
			{
				return this;
			}

			@Override
			public void initializeClass()
			{
			}

			@Override
			public CMObject newInstance()
			{
				return this;
			}

			@Override
			public int compareTo(CMObject o)
			{
				return o == this ? 0 : 1;
			}
		}, Tickable.TICKID_MISCELLANEOUS, 10);
	}

	public String processRequest(final HTTPRequest httpReq)
	{
		if(httpReq.isUrlParameter("CONNECT"))
		{
			final String url=httpReq.getUrlParameter("URL");
			final int port=CMath.s_int(httpReq.getUrlParameter("PORT"));
			String hex="";
			final Siplet sip = new Siplet();
			boolean success=false;
			if(url!=null)
			{
				sip.init();
				synchronized(sipletConnectSync)
				{
					for(final MudHost h : CMLib.hosts())
					{
						if(h.getPort()==port)
						{
							try
							{
								final PipeSocket lsock=new PipeSocket(httpReq.getClientAddress(),null);
								final PipeSocket rsock=new PipeSocket(httpReq.getClientAddress(),lsock);
								success=sip.connectToURL(url, port,lsock);
								sip.setFeatures(true, Siplet.MSPStatus.External, false);
								h.acceptConnection(rsock);
							}
							catch(final IOException e)
							{
								success=false;
							}
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
							if(tokenNum<0)
								tokenNum = tokenNum * -1;
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
			final String token=httpReq.getUrlParameter("TOKEN");
			boolean success = false;
			if(token != null)
			{
				final SipletSession p = siplets.get(token);
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
			final String token=httpReq.getUrlParameter("TOKEN");
			boolean success = false;
			if(token != null)
			{
				final SipletSession p = siplets.get(token);
				if(p!=null)
				{
					String data=httpReq.getUrlParameter("DATA");
					if(data!=null)
					{
						p.lastTouched=System.currentTimeMillis();
						p.siplet.sendData(data);
						if(p.siplet.isConnectedToURL())
						{
							CMLib.s_sleep(10);
							if(p.siplet.isConnectedToURL())
							{
								p.lastTouched=System.currentTimeMillis();
								p.siplet.readURLData();
								data = p.siplet.getURLData();
								final String jscript = p.siplet.getJScriptCommands();
								success=p.siplet.isConnectedToURL();
								p.response=Boolean.toString(success)+';'+data+token+';'+jscript+token+';';
								return p.response;
							}
						}
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
	
	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) throws HTTPServerException
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return "false;";
		if(!initialized)
		{
			initialize();
		}
		
		if((httpReq.getHeader("upgrade")!=null)
		&&("websocket".equalsIgnoreCase(httpReq.getHeader("upgrade")))
		&&(httpReq.getHeader("connection")!=null)
		&&(httpReq.getHeader("connection").toLowerCase().indexOf("upgrade")>=0))
		{
			final HTTPException exception = new HTTPException(HTTPStatus.S101_SWITCHING_PROTOCOLS);
			try
			{
				final String key = httpReq.getHeader("sec-websocket-key");
				final String tokenStr = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
				final MessageDigest cript = MessageDigest.getInstance("SHA-1");
				cript.reset();
				cript.update(tokenStr.getBytes("utf8"));
				final String token = B64Encoder.B64encodeBytes(cript.digest());
				httpResp.setStatusCode(101);
				exception.getErrorHeaders().put(HTTPHeader.Common.CONNECTION, HTTPHeader.Common.UPGRADE.toString());
				exception.getErrorHeaders().put(HTTPHeader.Common.UPGRADE, "websocket");
				exception.getErrorHeaders().put(HTTPHeader.Common.SEC_WEBSOCKET_ACCEPT, token);
				final SipletProtocolHander newHandler = new SipletProtocolHander();
				exception.setNewProtocolHandler(newHandler);
			}
			catch (Exception e)
			{
				Log.errOut(e);
				throw new HTTPServerException(e.getMessage());
			}
			throw new HTTPServerException(exception);
		}
		
		return processRequest(httpReq);
	}

	private enum WSState
	{
		S0,P1,PX,M1,M2,M3,M4,PAYLOAD
	}

	private class SipletProtocolHander implements ProtocolHandler
	{
		private volatile WSState	state		= WSState.S0;
		private volatile int		subState	= 0;
		private volatile long		dataLen		= 0;
		private volatile byte		opCode		= 0;
		private final byte[]		mask		= new byte[4];
		private volatile boolean	finished	= false;
		private volatile int		maskPos		= 0;

		private final ByteArrayOutputStream payload = new ByteArrayOutputStream();
		private final ByteArrayOutputStream msg		= new ByteArrayOutputStream();

		public SipletProtocolHander()
		{
			
		}

		private byte[] encodeTextResponse(String resp)
		{
			return encodeResponse(resp.getBytes(Charset.forName("UTF8")),1);
		}

		private byte[] encodeResponse(byte[] resp, int type)
		{
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			bout.write(0x80 + (byte)type); // output
			if(resp.length < 126)
			{
				bout.write(resp.length & 0x7f);
			}
			else
			if(resp.length < 65535)
			{
				bout.write(126 & 0x7f);
				int byte1=resp.length / 256;
				int byte2 = resp.length - (byte1 * 256);
				bout.write(byte1);
				bout.write(byte2);
			}
			else
			{
				bout.write(127 & 0x7f);
				long len = resp.length;
				int byte1=(int)(len / 16777216);
				len = len - (byte1 * 16777216);
				int byte2=(int)(len / 65536);
				len = len - (byte2 * 65536);
				int byte3=(int)(len / 256);
				len = len - (byte3 * 256);
				bout.write(byte1 & 0xff);
				bout.write(byte2 & 0xff);
				bout.write(byte3 & 0xff);
				bout.write((byte)(len & 0xff));
			}
			try
			{
				bout.write(resp);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return bout.toByteArray();
		}

		/**
		 * When url-encoded data is received, this method is called to parse out
		 * the key-pairs and put their decoded keys and values into the urlParameters
		 * list.
		 * @param httpReq the request to populate with stuff
		 * @param parts the raw undecoded urlencoded line of data
		 * @throws HTTPException
		 */
		private void parseUrlEncodedKeypairs(HTTPRequest httpReq, String parts) throws HTTPException
		{
			try
			{
				httpReq.getUrlParameters().clear();
				final String[] urlParmArray = parts.split("&");
				final Map<String,String> urlParmsFound=new HashMap<String,String>();
				for(final String urlParm : urlParmArray)
				{
					final int equalDex = urlParm.indexOf('=');
					final String key;
					final String value;
					if(equalDex < 0)
					{
						key=URLDecoder.decode(urlParm,"UTF-8");
						value="";
					}
					else
					{
						key=URLDecoder.decode(urlParm.substring(0,equalDex),"UTF-8");
						value=URLDecoder.decode(urlParm.substring(equalDex+1),"UTF-8");
					}
					urlParmsFound.put(key, value);
				}
				for(final String key : urlParmsFound.keySet())
					httpReq.addFakeUrlParameter(key,urlParmsFound.get(key));
			}
			catch(final UnsupportedEncodingException ex)
			{
				throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
			}
		}
		
		private void reset(ByteBuffer buffer)
		{
			msg.reset();
			payload.reset();
			state = WSState.S0;
			subState	= 0;
			dataLen		= 0;
			opCode		= 0;
			finished	= false;
			maskPos		= 0;
		}

		private DataBuffers getOutput(DataBuffers outBuffer)
		{
			if(outBuffer == null)
				return new CWDataBuffers();
			return outBuffer;
		}
		
		private DataBuffers done(HTTPRequest request, ByteBuffer buffer, DataBuffers outBuffer) throws HTTPException
		{
			switch(opCode)
			{
			case 1:
			{
				final String cmd = new String(payload.toByteArray());
				parseUrlEncodedKeypairs(request,cmd);
				final String resp = processRequest(request);
				final byte[] encodedResp = this.encodeTextResponse(resp);
				reset(buffer);
				outBuffer = getOutput(outBuffer);
				outBuffer.add(encodedResp,System.currentTimeMillis(),true);
				break;
			}
			case 9:
			{
				final byte[] newPayload = msg.toByteArray();
				newPayload[0] = (byte)((newPayload[0] & 0xf0) + 10); // 10=pong
				reset(buffer);
				outBuffer = getOutput(outBuffer);
				outBuffer.add(newPayload,System.currentTimeMillis(),true);
				break;
			}
			case 10:
				// ignore pong
				break;
			default:
				break;
			}
			return outBuffer;
		}
		
		@Override
		public DataBuffers processBuffer(HTTPRequest request, ByteBuffer buffer) throws HTTPException
		{
			DataBuffers outBuffers = null;
			buffer.flip(); // turn the writeable buffer into a "readable" one
			while(buffer.position() < buffer.limit())
			{
				byte b=buffer.get(); // keep this here .. it ensures the while loop ends
				msg.write(b);
				switch(state)
				{
				case M1:
					mask[0]=b;
					state=WSState.M2;
					break;
				case M2:
					mask[1]=b;
					state=WSState.M3;
					break;
				case M3:
					mask[2]=b;
					state=WSState.M4;
					break;
				case M4:
					maskPos=0;
					mask[3]=b;
					if(dataLen > 0)
						state=WSState.PAYLOAD;
					else
					{
						if(finished)
							outBuffers = done(request,buffer,outBuffers);
						state=WSState.S0;
					}
					break;
				case P1:
				{
					if((b & 0x80) == 0)
					{
						throw HTTPException.standardException(HTTPStatus.S403_FORBIDDEN);
					}
					this.dataLen = (b & 0x7F);
					if(this.dataLen < 126 )
						state = WSState.M1;
					else
					if(this.dataLen == 126)
					{
						this.dataLen = 0;
						this.subState = 2;
						state = WSState.PX;
					}
					else
					{
						this.dataLen = 0;
						this.subState = 4;
						state = WSState.PX;
					}
					break;
				}
				case PAYLOAD:
					b = (byte)((b & 0xff) ^ mask[maskPos]);
					maskPos = (maskPos+1) % 4;
					payload.write(b);
					if(--dataLen <= 0)
					{
						if(finished)
							outBuffers = done(request,buffer,outBuffers);
						state=WSState.S0;
					}
					break;
				case PX:
					if(subState > 0)
					{
						subState--;
						dataLen = (dataLen << 8) + (b & 0xff);
						if(subState == 0)
							state = WSState.M1;
					}
					else
						state = WSState.M1;
					break;
				case S0:
				{
					opCode = (byte)(b & 0x0f);
					finished = (b & 0x80) == 0x80; 
					state = WSState.P1;
					break;
				}
				}
				//System.out.print(Integer.toHexString(buffer.get() & 0xff)+" ");
			}
			buffer.clear();
			return outBuffers;
		}
	}
	
}
