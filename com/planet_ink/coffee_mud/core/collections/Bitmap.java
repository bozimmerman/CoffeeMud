package com.planet_ink.coffee_mud.core.collections;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/*
   Copyright 2024-2025 Bo Zimmerman

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

/**
 * A simple bitmap implementation that can grow as needed. Bits are numbered
 * starting from 0. Internally, bits are packed into longs, 63 bits per long.
 * The 64th bit of each long is unused. The bitmap can be constructed from a hex
 * string, and can be converted back to a hex string.
 *
 * @author Bo Zimmerman
 */
public class Bitmap
{
	private long[]			bs		= new long[0];
	private volatile int	onBits	= 0;

	/**
	 * Default constructor. Creates an empty bitmap.
	 */
	public Bitmap()
	{
		super();
	}

	/**
	 * Construct a bitmap from a hex string. The string must be a multiple of 16
	 * characters long, and contain only valid hex characters (0-9, a-f).
	 *
	 * @param s the hex string
	 * @throws IllegalArgumentException if the string is not a valid hex string
	 */
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

	/**
	 * Set the bit at the given index to the given value. The bitmap will grow
	 * as needed to accommodate the index.
	 *
	 * @param byteNum the index of the bit to set
	 * @param tf true to set the bit, false to clear it
	 * @throws IllegalArgumentException if the index is negative
	 */
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

	/**
	 * Get the value of the bit at the given index. If the index is beyond the
	 * current size of the bitmap, false is returned.
	 *
	 * @param byteNum the index of the bit to get
	 * @return true if the bit is set, false if it is clear or beyond the
	 *         current size of the bitmap
	 * @throws IllegalArgumentException if the index is negative
	 */
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

	/**
	 * Convert the bitmap to a hex string. The string will be a multiple of 16
	 * characters long, with leading zeros as needed.
	 * @return the hex string representation of the bitmap
	 */
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

	/**
	 * Converts the given array of booleans to a bitmap.
	 * @param bits the array of booleans
	 * @return the bitmap
	 */
	public static Bitmap fromBoolArray(final boolean[] bits)
	{
		final Bitmap b = new Bitmap();
		for(int i=0;i<bits.length;i++)
			b.set(i, bits[i]);
		return b;
	}

	/**
	 * Converts this bitmap to an array of booleans. The array will be the same
	 * length as the number of bits in the bitmap.
	 * @param bits the array of booleans to fill
	 */
	public void toBoolArray(final boolean[] bits)
	{
		for(int i=0;i<bits.length;i++)
			bits[i] = get(i);
	}

	/**
	 * Returns the number of bits that are set to true in this bitmap.
	 *
	 * @return the number of bits that are set to true
	 */
	public int onCount()
	{
		return onBits;
	}
}
