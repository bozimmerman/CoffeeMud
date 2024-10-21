package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Bitmap
{
	private long[]			bs		= new long[0];
	private volatile int	onBits	= 0;

	public Bitmap()
	{
		super();
	}

	public Bitmap(final String s)
	{
		if(s.equals("0"))
			return;
		if((s.length()%16)!=0)
			throw new IllegalArgumentException(s);
		bs = new long[s.length()/16];
		for(int i=0;i<bs.length;i++)
			bs[i] = Long.parseLong(s.substring(i*16,(i*16)+16), 16);
		for(int i=0;i<bs.length;i++)
		{
			long mask = 1;
			for(int b=0;b<63;b++)
			{
				if((bs[i]&mask)==mask)
					onBits++;
				mask = mask << 1;
			}
		}

	}

	public synchronized void set(final int byteNum, final boolean tf)
	{
		if(byteNum<0)
			throw new IllegalArgumentException(""+byteNum);
		synchronized(bs)
		{
			final int bitIndex = byteNum % 63;
			final int byteIndex = (int)Math.floor(byteNum / 63);
			if(byteIndex >= bs.length)
				bs = Arrays.copyOf(bs, byteIndex+1);
			final long mask=(long)Math.pow(2,bitIndex);
			if(tf)
			{
				if((bs[byteIndex] & mask) == 0)
					onBits++;
				bs[byteIndex] = bs[byteIndex] | mask;
			}
			else
			{
				if((bs[byteIndex] & mask) == mask)
					onBits--;
				bs[byteIndex] = bs[byteIndex] & ~mask;
			}
		}
	}

	public synchronized boolean get(final int byteNum)
	{
		if(byteNum<0)
			throw new IllegalArgumentException(""+byteNum);
		synchronized(bs)
		{
			final int bitIndex = byteNum % 63;
			final int byteIndex = (int)Math.floor(byteNum / 63);
			if(byteIndex >= bs.length)
				bs = Arrays.copyOf(bs, byteIndex+1);
			final long mask=(long)Math.pow(2,bitIndex);
			return (bs[byteIndex] & mask) == mask;
		}
	}

	public String toHexString()
	{
		final StringBuilder str = new StringBuilder("");
		synchronized(bs)
		{
			for(final long b : bs)
			{
				final String s = "0000000000000000"+Long.toHexString(b);
				str.append(s.substring(s.length()-16));
			}
		}
		return str.toString();
	}

	public int onCount()
	{
		return onBits;
	}
}
