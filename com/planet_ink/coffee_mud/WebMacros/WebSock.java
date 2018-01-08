package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CoffeeIOPipe.CoffeeIOPipes;
import com.planet_ink.coffee_mud.core.CoffeeIOPipe.CoffeePipeSocket;
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
import com.planet_ink.coffee_mud.WebMacros.SipletInterface.WSState;

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
   Copyright 2011-2018 Bo Zimmerman

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
public class WebSock extends StdWebMacro
{
	@Override
	public String name()
	{
		return "WebSock";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}
	
	protected boolean initialized = false;
	public static final List<WebSockHandler>	handlers				= new LinkedList<WebSockHandler>();

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
				return "WebSock";
			}

			@Override
			public boolean tick(Tickable ticking, int tickID)
			{
				tickStatus = Tickable.STATUS_ALIVE;
				synchronized (handlers)
				{
					for (Iterator<WebSockHandler> h=handlers.iterator();h.hasNext();)
					{
						WebSockHandler W=h.next();
						if (W == null)
							continue;
						final long idle = System.currentTimeMillis() - W.lastPing;
						if ((idle > (30 * 1000)))
						{
							try
							{
								W.lsock.close();
								W.rsock.close();
							}
							catch (IOException e)
							{
								Log.errOut(e);
							}
							h.remove();
						}
						else
						if(W.ioHandler != null)
						{
							try
							{
								if(W.in.available()>0)
									W.ioHandler.scheduleProcessing();
							}
							catch (IOException e)
							{
								Log.errOut(e);
							}
						}
					}
				}
				tickStatus = Tickable.STATUS_NOT;
				return true;
			}

			@Override
			public String ID()
			{
				return "WebSock";
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
		}, Tickable.TICKID_MISCELLANEOUS, 250, 1);
	}

	private class WebSockHandler implements ProtocolHandler
	{
		private volatile WSState	state		= WSState.S0;
		private volatile int		subState	= 0;
		private volatile long		dataLen		= 0;
		private volatile byte		opCode		= 0;
		private final byte[]		mask		= new byte[4];
		private volatile boolean	finished	= false;
		private volatile int		maskPos		= 0;
		private volatile long		lastPing	= System.currentTimeMillis();
		private HTTPIOHandler		ioHandler	= null;

		private final CoffeePipeSocket lsock;
		private final CoffeePipeSocket rsock;
		private final OutputStream out;
		private final InputStream in;
		private final ByteArrayOutputStream payload = new ByteArrayOutputStream();
		private final ByteArrayOutputStream msg		= new ByteArrayOutputStream();

		public WebSockHandler(HTTPRequest httpReq) throws IOException
		{
			synchronized(this)
			{
				for(final MudHost h : CMLib.hosts())
				{
					try
					{
						final CoffeeIOPipes pipes = new CoffeeIOPipes(65536);
						lsock=new CoffeePipeSocket(httpReq.getClientAddress(),pipes.getLeftPipe(),pipes.getRightPipe());
						rsock=new CoffeePipeSocket(httpReq.getClientAddress(),pipes.getRightPipe(),pipes.getLeftPipe());
						h.acceptConnection(rsock);
						out=lsock.getOutputStream();
						in=lsock.getInputStream();
						return;
					}
					catch(final IOException e)
					{
						throw e;
					}
				}
			}
			throw new IOException("No host found.");
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
		
		private void reset(ByteBuffer buffer)
		{
			msg.reset();
			payload.reset();
			state 		= WSState.S0;
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
			case 1: // text data
			{
				this.lastPing=System.currentTimeMillis();
				final byte[] newPayload = msg.toByteArray();
				if(newPayload.length>0)
				{
					try
					{
						out.write(newPayload);
					}
					catch (IOException e)
					{
						Log.errOut(e);
					}
				}
				reset(buffer);
				outBuffer = getOutput(outBuffer);
				try
				{
					final int avail=in.available();
					final byte[] buf=new byte[avail];
					final int num=in.read(buf);
					if(num>0)
					{
						final byte[] outBuf = encodeResponse((num==avail)?buf:Arrays.copyOf(buf, num),1);
						outBuffer.add(outBuf, System.currentTimeMillis(), true);
					}
				}
				catch (IOException e)
				{
					Log.errOut(e);
				}
				break;
			}
			case 9: // ping
			{
				this.lastPing=System.currentTimeMillis();
				final byte[] newPayload = msg.toByteArray();
				newPayload[0] = (byte)((newPayload[0] & 0xf0) + 10); // 10=pong
				reset(buffer);
				outBuffer = getOutput(outBuffer);
				outBuffer.add(newPayload,System.currentTimeMillis(),true);
				break;
			}
			case 10: // pong -- ignore
				break;
			case 11: // close
				break;
			default:
				break;
			}
			return outBuffer;
		}
		
		@Override
		public DataBuffers processBuffer(HTTPIOHandler handler, HTTPRequest request, ByteBuffer buffer) throws HTTPException
		{
			if(ioHandler==null)
				ioHandler = handler;
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

		@Override
		public boolean isTimedOut()
		{
			final long idle = System.currentTimeMillis() - lastPing;
			if ((idle > (30 * 1000)))
			{
				this.closeAndWait();
			}
			return false;
		}

		@Override
		public void closeAndWait()
		{
			try
			{
				lsock.close();
				rsock.close();
			}
			catch (IOException e)
			{
				Log.errOut(e);
			}
			synchronized(handlers)
			{
				handlers.remove(this);
			}
		}
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
				exception.getErrorHeaders().put(HTTPHeader.Common.UPGRADE, httpReq.getHeader("upgrade"));
				exception.getErrorHeaders().put(HTTPHeader.Common.SEC_WEBSOCKET_ACCEPT, token);
				if(httpReq.getHeader("origin")!=null)
					exception.getErrorHeaders().put(HTTPHeader.Common.ORIGIN, httpReq.getHeader("origin"));
				final StringBuilder locationStr;
				if(httpReq.getFullHost().startsWith("https:"))
					locationStr = new StringBuilder("wss://"+httpReq.getHost());
				else
					locationStr = new StringBuilder("ws://"+httpReq.getHost());
				if(httpReq.getClientPort() != 80)
					locationStr.append(":").append(httpReq.getClientPort());
				locationStr.append(httpReq.getUrlPath());
				exception.getErrorHeaders().put(HTTPHeader.Common.SEC_WEBSOCKET_LOCATION, locationStr.toString());
				final WebSockHandler newHandler = new WebSockHandler(httpReq);
				synchronized(handlers)
				{
					handlers.add(newHandler);
				}
				exception.setNewProtocolHandler(newHandler);
				httpReq.getRequestObjects().put("___SIPLETHANDLER", newHandler);
			}
			catch (Exception e)
			{
				Log.errOut(e);
				throw new HTTPServerException(e.getMessage());
			}
			throw new HTTPServerException(exception);
		}
		return "";
	}

}