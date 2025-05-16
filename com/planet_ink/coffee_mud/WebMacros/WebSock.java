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
import com.planet_ink.coffee_mud.WebMacros.WebSock.WSPType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
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
   Copyright 2011-2025 Bo Zimmerman

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
public class WebSock extends StdWebMacro implements ProtocolHandler, Tickable
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

	protected volatile WSState	state		= WSState.S0;
	protected volatile int		subState	= 0;
	protected volatile long		dataLen		= 0;
	protected volatile byte		opCode		= 0;
	protected final byte[]		mask		= new byte[4];
	protected volatile boolean	finished	= false;
	protected volatile int		maskPos		= 0;
	protected volatile long		lastPing	= System.currentTimeMillis();
	protected HTTPIOHandler		ioHandler	= null;

	protected final CoffeePipeSocket lsock;
	protected final CoffeePipeSocket rsock;
	protected final Session[]	sess;
	protected final MudHost host;
	protected final OutputStream mudOut;
	protected final InputStream mudIn;
	protected final ByteArrayOutputStream payload = new ByteArrayOutputStream();
	protected final ByteArrayOutputStream msg		= new ByteArrayOutputStream();

	private static final byte[] pingFrame = new byte[] {(byte)0x89, 0x00};
	private static final byte[] emptyBytes = new byte[0];

	protected static enum WSPType
	{
		CONTINUE,
		TEXT,
		BINARY
	}

	public WebSock()
	{
		super();
		lsock=null;
		rsock=null;
		mudOut=null;
		mudIn=null;
		sess=null;
		host=null;
	}

	public WebSock(final HTTPRequest httpReq) throws IOException
	{
		synchronized(this)
		{
			MudHost foundH = null;
			if((httpReq != null)
			&&(httpReq.getUrlParameter("port")!=null))
			{
				final int portNum = CMath.s_int(httpReq.getUrlParameter("port"));
				for(final MudHost mudhost : CMLib.hosts())
				{
					if(mudhost.getPort() == portNum)
						foundH = mudhost;
				}
			}
			host = (foundH != null) ? foundH : CMLib.host();
			final CoffeeIOPipes pipes = new CoffeeIOPipes(65536);
			lsock=new CoffeePipeSocket(httpReq.getClientAddress(),pipes.getLeftPipe(),pipes.getRightPipe());
			rsock=new CoffeePipeSocket(httpReq.getClientAddress(),pipes.getRightPipe(),pipes.getLeftPipe());
			mudOut=lsock.getOutputStream();
			mudIn=lsock.getInputStream();
			pipes.getRightPipe().setWriteCallback(new Runnable() {
				@Override
				public void run()
				{
					try
					{
						if(ioHandler != null)
							poll();
					}
					catch(final Throwable t)
					{
						closeAndWait();
					}
				}
			});
			sess = host.acceptConnection(rsock);
		}
	}


	private synchronized void reset()
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

	private int	tickStatus	= Tickable.STATUS_NOT;

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus = Tickable.STATUS_ALIVE;
		try
		{
			if(isTimedOut())
				return false;
			if(ioHandler != null)
			{
				try
				{
					ping();
					poll();
				}
				catch(final Throwable t)
				{
					closeAndWait();
					return false;
				}
			}
		}
		finally
		{
			tickStatus = Tickable.STATUS_NOT;
		}
		return true;
	}

	private byte[] readBuffer(final int avail) throws IOException
	{
		byte[] buf=new byte[avail];
		final int num=mudIn.read(buf);
		if (num > 0)
		{
			if(num != avail)
				buf = Arrays.copyOf(buf, num);
			return buf;
		}
		return emptyBytes;
	}

	protected void ping()
	{
		try
		{
			final long idle = System.currentTimeMillis() - lastPing;
			if ((idle > (5 * 1000)))
			{
				final DataBuffers outBuffer = new CWDataBuffers();
				outBuffer.add(pingFrame, System.currentTimeMillis(), true);
				ioHandler.writeBytesToChannel(outBuffer);
			}
		}
		catch (final IOException e)
		{
			closeAndWait();
		}
	}

	protected Pair<byte[], WSPType> processPolledBytes(final byte[] data)
	{
		return new Pair<byte[], WSPType>(data, WSPType.BINARY);
	}

	protected void sendPacket(final byte[] buf, final WSPType type) throws IOException
	{
		if(buf.length>0)
		{
			final byte[] outBuf = encodeResponse(buf,type.ordinal());
			final DataBuffers outBuffer = new CWDataBuffers();
			outBuffer.add(outBuf, System.currentTimeMillis(), true);
			ioHandler.writeBytesToChannel(outBuffer);
			ioHandler.scheduleProcessing();
		}
	}

	protected void poll()
	{
		try
		{
			int avail = mudIn.available();
			while(avail>0)
			{
				final Pair<byte[], WSPType> buf = processPolledBytes(readBuffer(avail));
				if(buf != null)
					sendPacket(buf.first, buf.second);
				avail = mudIn.available();
			}
		}
		catch (final IOException e)
		{
			closeAndWait();
		}
	}

	private DataBuffers processInput(final int opCode, final HTTPRequest request, final byte[] payload , final DataBuffers outBuffer) throws HTTPException
	{
		switch(opCode)
		{
		case 0: // continue frame
		case 1: // text frame
		case 2: // binary frame
		{
			this.lastPing=System.currentTimeMillis();
			reset();
			try
			{
				if(payload.length>0)
				{
					Pair<byte[],WSPType> response;
					if (opCode == 2)
						response=processBinaryInput(payload);
					else
						response=processTextInput(new String(payload, CMProps.getVar(CMProps.Str.CHARSETINPUT)));
					if(response != null)
						sendPacket(response.first, response.second);
				}
			}
			catch (final IOException e)
			{
				this.closeAndWait();
				break;
			}
			break;
		}
		case 8: //connection close?!
			this.closeAndWait();
			break;
		case 9: // ping
		case 10: // pong -- ignore
		{
			this.lastPing=System.currentTimeMillis();
			break;
		}
		case 11: // close
			break;
		default:
			break;
		}
		return outBuffer;
	}

	@Override
	public DataBuffers processBuffer(final HTTPIOHandler handler, final HTTPRequest request, final ByteBuffer buffer) throws HTTPException
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
						outBuffers = processInput(opCode,request,payload.toByteArray(),outBuffers);
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
						outBuffers = processInput(opCode,request,payload.toByteArray(),outBuffers);
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
			if (lsock != null)
				lsock.close();
			if (rsock != null)
				rsock.close();
		}
		catch (final IOException e)
		{
		}
		HTTPIOHandler handler;
		try
		{
			handler = this.ioHandler;
		}
		finally
		{}
		CMLib.threads().deleteTick(this, -1);
		try
		{
			if(handler != null)
				handler.closeAndWait();
		}
		catch(final Exception e)
		{
		}
		finally
		{
			this.ioHandler = null;
		}
	}

	protected Pair<byte[], WSPType> processTextInput(final String input) throws IOException
	{
		if (mudOut != null)
		{
			mudOut.write(input.getBytes());
			mudOut.flush();
		}
		return null;
	}

	protected Pair<byte[], WSPType> processBinaryInput(final byte[] input) throws IOException
	{
		if (mudOut != null)
		{
			mudOut.write(input);
			mudOut.flush();
		}
		return null;
	}

	protected byte[] encodeResponse(final byte[] resp, final int type)
	{
		final ByteArrayOutputStream bout=new ByteArrayOutputStream();
		bout.write(0x80 + (byte)type); // output
		if(resp.length < 126)
		{
			bout.write(resp.length & 0x7f);
		}
		else
		if(resp.length < 65535)
		{
			bout.write(126 & 0x7f);
			final int byte1=resp.length / 256;
			final int byte2 = resp.length - (byte1 * 256);
			bout.write(byte1);
			bout.write(byte2);
		}
		else
		{
			bout.write(127 & 0x7f);
			long len = resp.length;
			final int byte1=(int)(len / 16777216);
			len = len - (byte1 * 16777216);
			final int byte2=(int)(len / 65536);
			len = len - (byte2 * 65536);
			final int byte3=(int)(len / 256);
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
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return bout.toByteArray();
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return "false;";

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
				final Constructor<?> constructor = this.getClass().getConstructor(HTTPRequest.class);
				final WebSock handler = (WebSock)constructor.newInstance(httpReq);
				exception.setNewProtocolHandler(handler);
				CMLib.threads().startTickDown(handler, Tickable.TICKID_MISCELLANEOUS, 250, 1);
			}
			catch (final Exception e)
			{
				Log.errOut(e);
				throw new HTTPServerException(e.getMessage());
			}
			throw new HTTPServerException(exception);
		}
		return "";
	}

}