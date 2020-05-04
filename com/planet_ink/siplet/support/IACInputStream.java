package com.planet_ink.siplet.support;

import java.io.*;
import java.util.*;

import com.jcraft.jzlib.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Common.interfaces.Session.TickingCallback;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.siplet.applet.Siplet;
import com.planet_ink.siplet.applet.Siplet.MSPStatus;
import com.planet_ink.siplet.support.MiniJSON.JSONObject;
import com.planet_ink.siplet.support.MiniJSON.MJSONException;

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
public class IACInputStream extends InputStream
{
	private final InputStream in;
	final ByteArrayOutputStream iacBuf=new ByteArrayOutputStream();
	final byte[] empty=new byte[0];
	final int[] peekBuffer = new int[256];
	int peekRead = 0;
	int peekWrite = 0;
	
	public IACInputStream(final InputStream in)
	{
		this.in=in;
	}
	
	@Override
	public void close() throws IOException
	{
		super.close();
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
	
	private synchronized void checkIAC() throws IOException
	{
		synchronized(iacBuf)
		{
			if(iacBuf.size()>0)
				return;
			if(peekRead != peekWrite)
				return;
		}
		if(in.available()<=0)
			return;
		int c=this.peek();
		if((c != TELNET_IAC)&&((c & 0xff)!=TELNET_IAC))
			return;
		iacBuf.write(TELNET_IAC);
		if(!waitAvailable())
		{
			iacBuf.reset();
			peekWrite=peekRead;
			return;
		}
		c=this.peek();
		if(c>255)
			c=c&0xff;
		iacBuf.write(c);
		switch(c)
		{
		case TELNET_IAC:
			peekWrite=peekRead;
			return;
		case TELNET_SB:
		{
			if(!waitAvailable())
			{
				iacBuf.reset();
				peekWrite=peekRead;
				return;
			}
			final int subOptionCode = peek();
			iacBuf.write(subOptionCode);
			int last = 0;
			final long expire=System.currentTimeMillis() + 200;
			while(System.currentTimeMillis()<expire)
			{
				try
				{
					if(in.available()>0)
					{
						if(iacBuf.size()>250)
							return;
						last = peek();
						if (last == TELNET_IAC)
						{
							iacBuf.write(TELNET_IAC);
							last = peek();
							if (last == TELNET_IAC)
								iacBuf.write(last);
							else
							if (last == TELNET_SE)
							{
								iacBuf.write(last);
								break;
							}
						}
						else
							iacBuf.write(last);
					}
				}
				catch(final IOException e)
				{
				}
			}
			peekWrite=peekRead;
			return;
		}
		case TELNET_DO:
		case TELNET_DONT:
		case TELNET_WILL:
		case TELNET_WONT:
		{
			if(!waitAvailable())
			{
				iacBuf.reset();
				peekWrite=peekRead;
				return;
			}
			final int last=peek();
			iacBuf.write(last);
			peekWrite=peekRead;
			return;
		}
		case TELNET_AYT:
			peekWrite=peekRead;
			return;
		default:
			peekWrite=peekRead;
			return;
		}
	}

	public boolean isIACAvailable() throws IOException
	{
		checkIAC();
		synchronized(iacBuf)
		{
			return iacBuf.size()>0;
		}
	}
	
	public byte[] readIAC() throws IOException
	{
		checkIAC();
		synchronized(iacBuf)
		{
			if(iacBuf.size()==0)
				return empty;
			final byte[] bytes = iacBuf.toByteArray();
			iacBuf.reset();
			return bytes;
		}
	}
	
	@Override
	public int available() throws IOException
	{
		checkIAC();
		int ct=iacBuf.size();
		if(peekRead != peekWrite)
		{
			if(peekWrite > peekRead)
				ct += (peekWrite-peekRead);
			else
				ct += (255-peekRead)+peekWrite+1;
		}
		return ct + in.available();
	}
	
	private int peek() throws IOException
	{
		synchronized(peekBuffer)
		{
			if((peekWrite == peekRead-1)
			||((peekRead == 0)&&(peekWrite==255)))
				return -1;
			final int c=in.read();
			if(c==-1)
				return c;
			peekBuffer[peekWrite]=c;
			if(++peekWrite>255)
				peekWrite=0;
			return c;
		}
	}
	
	@Override
	public int read() throws IOException
	{
		checkIAC();
		if(peekWrite != peekRead)
		{
			synchronized(peekBuffer)
			{
				if(peekWrite != peekRead)
				{
					final int peekByte=peekBuffer[peekRead];
					if(++peekRead>255)
						peekRead=0;
					return peekByte;
				}
			}
		}
		return in.read();
	}
	
	

}
