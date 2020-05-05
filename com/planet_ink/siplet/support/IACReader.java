package com.planet_ink.siplet.support;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
   Copyright 2005-2020 Bo Zimmerman

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
public class IACReader extends Reader
{
	private final PeekInputStream	in;
	private final CharsetDecoder	decoder;
	private final QInputStream		que		= new QInputStream(1024);

	public IACReader(final InputStream in, final String inCharSet) throws UnsupportedEncodingException
	{
		this.in=new PeekInputStream(in,256);
		final Charset charSet = Charset.forName(inCharSet);
		decoder = charSet.newDecoder();
	}
	
	@Override
	public void close() throws IOException
	{
		in.close();
	}
	
	/** TELNET CODE: Indicates that what follows is subnegotiation of the indicated option*/
	private static final int TELNET_SB=250;
	/** TELNET CODE: Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option*/
	private static final int TELNET_WILL=251;
	/** TELNET CODE: Indicates the refusal to perform, or continue performing, the indicated option*/
	private static final int TELNET_WONT=252;
	/** TELNET CODE: 252 doubles as fake ansi 16 telnet code*/
	private static final int TELNET_DO=253;
	/** TELNET CODE: Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.*/
	private static final int TELNET_DONT=254;
	/** TELNET CODE: IAC*/
	private static final int TELNET_IAC=255;
	/** TELNET CODE: end of subnegotiation*/
	private static final int TELNET_SE=240;
	/** TELNET CODE: Are You There*/
	private static final int TELNET_AYT=246;

	private boolean waitAvailable() throws IOException
	{
		int tm=0;
		while(in.available()==0)
		{
			try
			{
				Thread.sleep(1);
			}
			catch(Exception e)
			{
				return false;
			}
			if(++tm>200)
				return false;
		}
		return true;
	}
	
	public int available() throws IOException
	{
		return que.available() + in.available();
	}
	
	public char readChar() throws IOException
	{
		if(que.available()>0)
		{
			char c=(char)this.que.read();
			return c;
		}
		if(in.available()==0)
			return (char)-1;
		int c=in.read();
		if(c>255)
			c=(c&0xff);
		if(((c != TELNET_IAC)&&((c & 0xff)!=TELNET_IAC)))
		{
			final ByteArrayOutputStream	bout= new ByteArrayOutputStream();
			while(bout.size()<10)
			{
				final byte b=(byte)(c & 0xff);
				bout.write(b);
				try
				{
					final byte[] byt=bout.toByteArray();
					CharBuffer buf = decoder.decode(ByteBuffer.wrap(byt));
					if(buf.hasRemaining())
					{
						bout.reset();
						char fc=buf.get();
						while(buf.hasRemaining())
							que.queue(buf.get());
						return fc;
					}
				}
				catch(MalformedInputException e)
				{
				}
				if(!waitAvailable())
					break;
				c=in.read();
			}
			for(final byte b1 : bout.toByteArray())
				que.queue(b1);
			return (char)que.read();
		}
		if(!waitAvailable())
			return (char)c;
		c=in.read();
		if(c>255)
			c=(c&0xff);
		que.queue(c); // NOT for the charset reader
		switch(c)
		{
		case TELNET_IAC:
			break;
		case TELNET_SB:
		{
			if(!waitAvailable())
				return (char)TELNET_IAC;
			final int subOptionCode = in.read();
			que.queue(subOptionCode);
			int last = 0;
			final long expire=System.currentTimeMillis() + 200;
			while(System.currentTimeMillis()<expire)
			{
				try
				{
					if(in.available()>0)
					{
						last = in.read();
						if (last == TELNET_IAC)
						{
							que.queue(TELNET_IAC);  // NOT for the charset reader
							last = in.read();
							if (last == TELNET_IAC)
								que.queue(last);  // NOT for the charset reader
							else
							if (last == TELNET_SE)
							{
								que.queue(last);  // NOT for the charset reader
								break;
							}
						}
						else
							que.queue(last);  // NOT for the charset reader
					}
				}
				catch(final IOException e)
				{
				}
			}
			break;
		}
		case TELNET_DO:
		case TELNET_DONT:
		case TELNET_WILL:
		case TELNET_WONT:
		{
			if(!waitAvailable())
				return (char)TELNET_IAC;
			final int last=in.read();
			que.queue(last); // NOT for the charset reader
			break;
		}
		case TELNET_AYT:
			break;
		default:
			break;
		}
		return (char)TELNET_IAC;
	}

	@Override
    public boolean ready() throws IOException 
    {
    	return available() > 0;
    }
    
    /**
     * Reads characters into a portion of an array.  This method will block
     * until some input is available, an I/O error occurs, or the end of the
     * stream is reached.
     *
     * @param      cbuf  Destination buffer
     * @param      off   Offset at which to start storing characters
     * @param      len   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		if((len==0)
		||(cbuf==null)
		||(cbuf.length==0)
		||(off >= cbuf.length))
			return 0;
		int ct=0;
		while(ct < len)
		{
			char c=readChar();
			if(c != -1)
			{
				cbuf[off+ct]=c;
				if((++ct + off >= cbuf.length)
				||(ct>=len))
					return ct;
			}
			else
			{
				try
				{
					Thread.sleep(1);
				}
				catch(Exception e)
				{
				}
			}
		}
		return ct;
	}
	
	

}
