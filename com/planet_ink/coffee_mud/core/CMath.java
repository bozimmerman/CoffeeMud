package com.planet_ink.coffee_mud.core;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.math.BigInteger;

/*
   Copyright 2005-2018 Bo Zimmerman

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
 * A core singleton class handling various mathematical operations and
 * functions, especially dealing with explicit type conversions, and
 * special string conversions and functions.
 */
public class CMath
{
	private CMath()
	{
		super();
	}

	private static final CMath	inst	= new CMath();

	public static final CMath instance()
	{
		return inst;
	}

	private static final String[]		ROMAN_HUNDREDS	= {"C","CC","CCC","CD","D","DC","DCC","DCCC","CM","P"};
	private static final String[]		ROMAN_TENS		= {"X","XX","XXX","XL","L","LX","LXX","LXXX","XC","C"};
	private static final String[]		ROMAN_ONES		= {"I","II","III","IV","V","VI","VII","VIII","IX","X"};
	private static final String			ROMAN_ALL		= "CDMPXLIV";
	private static final String[]		LONG_ABBR		= {"","k", "M", "G", "T", "P","E"};
	private static final DecimalFormat	TWO_PLACES		= new DecimalFormat("0.#####%");
	private static final int[]			INTEGER_BITMASKS= new int[31];
	private static final long[]			LONG_BITMASKS	= new long[63];
	private static Random 				rand			= new Random(System.currentTimeMillis());
	
	static
	{
		for(int l=0;l<63;l++)
		{
			if(l<INTEGER_BITMASKS.length)
				INTEGER_BITMASKS[l]=1<<l;
			if(l<LONG_BITMASKS.length)
				LONG_BITMASKS[l]=1L<<l;
		}
	}

	/**
	 * Returns an abbreviation of the given long, giving 2 significant digits
	 * after the decimal, and returning k, M, G, T, P, or E for the power base 1000.
	 * @param l the long to abbreviate
	 * @return the abbreviated long
	 */
	public static String abbreviateLong(final long l)
	{
		final String lStr = Long.toString(l);
		if(lStr.length() < 4)
			return lStr;
		int llen = (lStr.length()-1) / 3;
		if(llen >= LONG_ABBR.length)
			llen = LONG_ABBR.length-1;
		return Double.toString(Math.round(Math.pow(1000, llen) * 100.0)/100.0) + LONG_ABBR[llen];
	}
	
	/** Convert an integer to its Roman Numeral equivalent
	 *
	 * Usage: Return=convertToRoman(Number)+".";
	 * @param i Integer to convert
	 *
	 * @return String Converted integer
	 */
	public final static String convertToRoman(int i)
	{
		final StringBuffer roman=new StringBuffer("");
		if(i>1000)
		{
			roman.append("Y");
			i=i%1000;
		}
		if(i>=100)
		{
			final int x=i%100;
			final int y=(i-x)/100;
			if(y>0)
				roman.append(ROMAN_HUNDREDS[y-1]);
			i=x;
		}
		if(i>=10)
		{
			final int x=i%10;
			final int y=(i-x)/10;
			if(y>0)
				roman.append(ROMAN_TENS[y-1]);
		}
		i=i%10;
		if(i>0)
			roman.append(ROMAN_ONES[i-1]);
		return roman.toString();
	}

	/**
	 * Convert a number from roman numeral to integer.
	 * @param s the roman numeral string
	 * @return the int
	 */
	public final static int convertFromRoman(final String s)
	{
		int x=0;
		while(s.startsWith("Y"))
			x+=1000;
		for(int i=ROMAN_HUNDREDS.length-1;i>=0;i--)
		{
			if(s.startsWith(ROMAN_HUNDREDS[i]))
			{
				x+=(100*(i+1));
				break;
			}
		}
		for(int i=ROMAN_TENS.length-1;i>=0;i--)
		{
			if(s.startsWith(ROMAN_TENS[i]))
			{
				x+=(10*(i+1));
				break;
			}
		}
		for(int i=ROMAN_ONES.length-1;i>=0;i--)
		{
			if(s.startsWith(ROMAN_ONES[i]))
			{
				x+=i+1;
				break;
			}
		}
		return x;
	}

	/**
	 * Return st,nd,rd for a number
	 * @param num the number
	 * @return the st,nd,rd appendage only
	 */
	public final static String numAppendage(final int num)
	{
		if((num<11)||(num>13))
		{
			final String strn=""+num;
			switch(strn.charAt(strn.length()-1))
			{
			case '1':
				return "st";
			case '2':
				return "nd";
			case '3':
				return "rd";
			}
		}
		return "th";
	}

	/**
	 * Append st,nd,rd for a number
	 * @param num the number
	 * @return the number with st,nd,rd appendage only
	 */
	public final static String appendNumAppendage(final int num)
	{
		return num+numAppendage(num);
	}

	/**
	 * Return true if the char is a roman numeral digit
	 * @param c the char
	 * @return true if is roman
	 */
	public final static boolean isRomanDigit(final char c)
	{
		return ROMAN_ALL.indexOf(c) >= 0;
	}

	/**
	 * Returns true if the string is a roman numeral
	 * @param s the string to test
	 * @return true if a roman numeral, false otherwise
	 */
	public final static boolean isRomanNumeral(final String s)
	{
		if(s==null)
			return false;
		final String ups=s.toUpperCase().trim();
		if(ups.length()==0)
			return false;
		for(int c=0;c<ups.length();c++)
		{
			if(!isRomanDigit(ups.charAt(c)))
				return false;
		}
		return true;
	}

	/**
	 * Returns the absolute difference between two numbers
	 * @param x the first number
	 * @param y the second number
	 * @return the absolute difference (x-y)*(-1 if less than 0)
	 */
	public final static long absDiff(final long x, final long y)
	{
		final long d=x-y;
		if(d<0)
			return d*-1;
		return d;
	}

	/**
	 * Returns which object in the object array is same as the
	 * string, when cast to a string.
	 * @param o array of objects
	 * @param s the string to look
	 * @return the object or null
	 */
	public final static Object s_valueOf(Object[] o, String s)
	{
		if(s==null)
			return null;
		for(final Object a : o)
		{
			if(a.toString().equalsIgnoreCase(s.trim()))
				return a;
		}
		return null;
	}

	/**
	 * Returns how many bits are set in the given 64 bit long
	 * @param i the long to count bits in
	 * @return the number of bits set
	 */
	public static int numberOfSetBits(long i)
	{
		i = i - ((i >> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >> 2) & 0x3333333333333333L);
		return (int)((((i + (i >> 4)) & 0xF0F0F0F0F0F0F0FL) * 0x101010101010101L) >> 56);
	}

	/**
	 * Returns how many bits are set in the given 32 bit int
	 * @param i the int to count bits in
	 * @return the number of bits set
	 */
	public static int numberOfSetBits(int i)
	{
		i = i - ((i >> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
		return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
	}

	/**
	 * Returns the matching enum.  Case Sensitive!
	 * @param c the enum class to look in
	 * @param s the string to look
	 * @return the enum or null
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final static Enum<? extends Enum> s_valueOf(Class<? extends Enum> c, String s)
	{
		if((c==null)||(s==null))
			return null;
		try
		{
			return Enum.valueOf(c, s);
		}
		catch(final Exception e)
		{
			return null;
		}
	}

	/**
	 * Returns the matching enum.  Case Sensitive!
	 * @param c the enum class to look in
	 * @param s the string to look
	 * @param def the value to use when null
	 * @return the enum
	 */
	@SuppressWarnings("rawtypes")
	public final static Enum<? extends Enum> s_valueOf(Class<? extends Enum> c, String s, Enum<? extends Enum> def)
	{
		final Enum<? extends Enum> obj = s_valueOf(c,s);
		if(obj == null)
			return def;
		return obj;
	}

	/**
	 * Returns true if the string is a number (float or int)
	 * @param s the string to test
	 * @return true if a number, false otherwise
	 */
	public final static boolean isNumber(final String s)
	{
		if(s==null)
			return false;
		final String ups=s.trim();
		if(ups.length()==0)
			return false;
		int start=0;
		if(ups.startsWith("-"))
			start=1;
		for(int i=start;i<ups.length();i++)
		{
			if("0123456789.,".indexOf(ups.charAt(i))<0)
				return false;
		}
		return true;
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final double a, final double b)
	{
		return a/b;
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final double a, final int b)
	{
		return a/(b);
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final int a, final double b)
	{
		return (a)/b;
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final double a, final long b)
	{
		return a/(b);
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final long a, final double b)
	{
		return (a)/b;
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static double mul(final double a, final double b)
	{
		return a*b;
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static double mul(final double a, final int b)
	{
		return a*(b);
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static double mul(final int a, final double b)
	{
		return (a)*b;
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static double mul(final double a, final long b)
	{
		return a*(b);
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static double mul(final long a, final double b)
	{
		return (a)*b;
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static long mul(final long a, final long b)
	{
		return a*b;
	}

	/**
	 * Multiply a and b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the first number
	 * @param b the second number
	 * @return the retult of multiplying a and b
	 */
	public final static int mul(final int a, final int b)
	{
		return a*b;
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final long a, final long b)
	{
		return ((double)a)/((double)b);
	}

	/**
	 * Divide a by b, making sure both are cast to doubles
	 * and that the return is precisely double.
	 * @param a the dividend
	 * @param b the divisor
	 * @return the quotient
	 */
	public final static double div(final int a, final int b)
	{
		return ((double)a)/((double)b);
	}

	/**
	 * Raises x to the y power, making sure both are cast to doubles
	 * and that the return is rounded off.
	 * @param x the base number
	 * @param y the power
	 * @return x to the y power, rounded off
	 */
	public final static long pow(final long x, final long y)
	{
		return Math.round(Math.pow((x),(y)));
	}

	/**
	 * Returns x, squared, after being case to a double
	 * @param x the number to square
	 * @return x, squared, and rounded off.
	 */
	public final static int squared(final int x)
	{
		return (int)Math.round(Math.pow((x),2.0));
	}

	/**
	 * Returns true if the given number has the bits
	 * represented by the given bitmask set.
	 * @param num the number
	 * @param bitmask the bit mask
	 * @return true if the bits are set, false otherwise
	 */
	public final static boolean bset(final short num, final short bitmask)
	{
		return ((num&bitmask)==bitmask);
	}

	/**
	 * Returns true if the given number has the bits
	 * represented by the given bitmask set.
	 * @param num the number
	 * @param bitmask the bit mask
	 * @return true if the bits are set, false otherwise
	 */
	public final static boolean bset(final int num, final int bitmask)
	{
		return ((num&bitmask)==bitmask);
	}

	/**
	 * Returns true if the given number has the bits
	 * represented by the given bitmask set.
	 * @param num the number
	 * @param bitmask the bit mask
	 * @return true if the bits are set, false otherwise
	 */
	public final static boolean bset(final long num, final long bitmask)
	{
		return ((num&bitmask)==bitmask);
	}

	/**
	 * Returns true if the given number has the bits
	 * represented by the given bitmask set.
	 * @param num the number
	 * @param bitmask the bit mask
	 * @return true if the bits are set, false otherwise
	 */
	public final static boolean bset(final long num, final int bitmask)
	{
		return ((num&bitmask)==bitmask);
	}

	/**
	 * Returns the given number, after having set the
	 * bits represented by the given bit mask.
	 * @param num the number
	 * @param bitmask the bitmask
	 * @return the number | the bitmask
	 */
	public final static int setb(final int num, final int bitmask)
	{
		return num|bitmask;
	}

	/**
	 * Returns true if any of the bits represented
	 * by the given bitmask are set in the given
	 * number.
	 * @param num the given number
	 * @param bitmask the bitmask of bits to check
	 * @return true if any bits from the mask are set
	 */
	public final static boolean banyset(final int num, final int bitmask)
	{
		return ((num&bitmask)>0);
	}

	/**
	 * Returns true if any of the bits represented
	 * by the given bitmask are set in the given
	 * number.
	 * @param num the given number
	 * @param bitmask the bitmask of bits to check
	 * @return true if any bits from the mask are set
	 */
	public final static boolean banyset(final long num, final long bitmask)
	{
		return ((num&bitmask)>0);
	}

	/**
	 * Returns true if any of the bits represented
	 * by the given bitmask are set in the given
	 * number.
	 * @param num the given number
	 * @param bitmask the bitmask of bits to check
	 * @return true if any bits from the mask are set
	 */
	public final static boolean banyset(final long num, final int bitmask)
	{
		return ((num&bitmask)>0);
	}

	/**
	 * Returns the given number, after having set the
	 * bits represented by the given bit mask.
	 * @param num the number
	 * @param bitmask the bitmask
	 * @return the number | the bitmask
	 */
	public final static long setb(final long num, final int bitmask)
	{
		return num|bitmask;
	}

	/**
	 * Returns the given number, after having set the
	 * bits represented by the given bit mask.
	 * @param num the number
	 * @param bitmask the bitmask
	 * @return the number | the bitmask
	 */
	public final static long setb(final long num, final long bitmask)
	{
		return num|bitmask;
	}

	/**
	 * Unsets those bits in the given number which are
	 * turned ON in the given bitmask.
	 * @param num the given number
	 * @param bitmask the given bitmask
	 * @return the number without the bitmasks bits turned on.
	 */
	public final static int unsetb(final int num, final int bitmask)
	{
		return num & (~bitmask);
	}

	/**
	 * Sets or Unsets those bits in the given number which are
	 * turned ON or OFF in the given bitmask.
	 * @param num the given number
	 * @param bitmask the given bitmask
	 * @param setOrUnSet true to set the bit, false otherwise
	 * @return the number with or without the bitmasks bits turned on.
	 */
	public final static int dobit(final int num, final int bitmask, boolean setOrUnSet)
	{
		return setOrUnSet ? (num | bitmask) : (num & (~bitmask));
	}

	/**
	 * Unsets those bits in the given number which are
	 * turned ON in the given bitmask.
	 * @param num the given number
	 * @param bitmask the given bitmask
	 * @return the number without the bitmasks bits turned on.
	 */
	public final static long unsetb(final long num, final long bitmask)
	{
		return num & (~bitmask);
	}

	/**
	 * Unsets those bits in the given number which are
	 * turned ON in the given bitmask.
	 * @param num the given number
	 * @param bitmask the given bitmask
	 * @return the number without the bitmasks bits turned on.
	 */
	public final static long unsetb(final long num, final int bitmask)
	{
		return num & (~bitmask);
	}

	/**
	 * Returns the bit index (0 based) of the first bit set in the given mask.
	 * @param bits the bits to check
	 * @return the first bit set, as an index (1=0, 2=1, 4=2, 8=3, etc..)
	 */
	public final static int firstBitSetIndex(int bits)
	{
		return ((bits & 0x80000000)!=0) ? 31 : firstBitSetIndex((bits << 1) | 1) - 1;
	}

	/**
	 * Returns the bit index (0 based) of the first bit set in the given mask.
	 * @param bits the bits to check
	 * @return the first bit set, as an index (1=0, 2=1, 4=2, 8=3, etc..)
	 */
	public final static int[] getAllBitsSet(int bits)
	{
		final List<Integer> bitsSet=new ArrayList<Integer>();
		for(int i=0;i<32;i++)
		{
			if(isSet(bits,i))
				bitsSet.add(Integer.valueOf(i));
		}
		final int[] ret=new int[bitsSet.size()];
		for(int i=0;i<bitsSet.size();i++)
			ret[i]=bitsSet.get(i).intValue();
		return ret;
	}

	/**
	 * Given a bitmask, seperates the mask according to which
	 * bits are set and returns those original values in an
	 * array where each entry is the value of each bit
	 * @param mask the mask to seperate
	 * @return an entry for every set bit
	 */
	public final static int[] getSeperateBitMasks(int mask)
	{
		if(mask==0)
			return new int[0];
		int ct=0;
		for(int i=0;i<INTEGER_BITMASKS.length;i++)
		{
			if((mask & INTEGER_BITMASKS[i])!=0)
				ct++;
		}
		final int[] masks=new int[ct];
		ct=0;
		for(int i=0;i<INTEGER_BITMASKS.length;i++)
		{
			if((mask & INTEGER_BITMASKS[i])!=0)
				masks[ct++] = (mask & INTEGER_BITMASKS[i]);
		}
		return masks;
	}
	
	/**
	 * Given a bitmask, seperates the mask according to which
	 * bits are set and returns those original values in an
	 * array where each entry is the value of each bit
	 * @param mask the mask to seperate
	 * @return an entry for every set bit
	 */
	public final static long[] getSeperateBitMasks(long mask)
	{
		if(mask==0)
			return new long[0];
		int ct=0;
		for(int i=0;i<LONG_BITMASKS.length;i++)
		{
			if((mask & LONG_BITMASKS[i])!=0)
				ct++;
		}
		final long[] masks=new long[ct];
		ct=0;
		for(int i=0;i<LONG_BITMASKS.length;i++)
		{
			if((mask & LONG_BITMASKS[i])!=0)
				masks[ct++] = (mask & LONG_BITMASKS[i]);
		}
		return masks;
	}
	
	/**
	 * Returns true if the bitnumberth bit (0...) is set
	 * in the given number
	 * @param number the given number
	 * @param bitnumber the bit to check (0,1,2...)
	 * @return true if the given bitnumberth bit is set
	 */
	public final static boolean isSet(final int number, final int bitnumber)
	{
		final int mask=(int)pow(2,bitnumber);
		return ((number&mask)==mask);
	}
	/**
	 * Returns true if the given string represents a
	 * percentage in the form X% where X is any real
	 * number.
	 * @param s the string to check
	 * @return true if it is a percentage, false otherwise
	 */
	public final static boolean isPct(final String s)
	{
		if(s==null)
			return false;
		final String ts=s.trim();
		if(!ts.endsWith("%"))
			return false;
		return isNumber(ts.substring(0,ts.length()-1));
	}

	/**
	 * Replaces @x1 type variables inside a stringbuffer with an actual value
	 * Not used in the main expression system, this is a stand alone function
	 * Also uniquely, supports @x numbers above 10.  Values are *1* indexed!!
	 * @param str the stringbuffer to assess
	 * @param values values to replace each variable with
	 */
	public final static void replaceVariables(final StringBuffer str, final double values[])
	{
		final int valueLen=(values.length<=10)?1:Integer.toString(values.length).length();
		for(int i=0;i<str.length()-(1+valueLen);i++)
		{
			if((str.charAt(i)=='@') && (str.charAt(i+1)=='x') && (Character.isDigit(str.charAt(i+2))))
			{
				int endDex=1;
				while((endDex < valueLen) && (Character.isDigit(str.charAt(i+2+endDex))))
					endDex++;
				final int valueDex = Integer.valueOf(str.substring(i+2,i+2+endDex)).intValue();
				final double newNumValue = (valueDex >0 && valueDex <= values.length)?values[valueDex-1]:0.0;
				final String newValue = ( Math.round(newNumValue) == newNumValue) ? Long.toString(Math.round(newNumValue)) : Double.toString(newNumValue);
				str.delete(i, i+2+endDex);
				str.insert(i, newValue);
				i--;
			}
		}
	}

	/**
	 * Replaces @x1 type variables inside a stringbuffer with an actual value
	 * Not used in the main expression system, this is a stand alone function
	 * Also uniquely, supports @x numbers above 10.  Values are *1* indexed!!
	 * @param str the stringbuffer to assess
	 * @param values values to replace each variable with
	 * @return the string with values replaced.
	 */
	public final static String replaceVariables(final String str, final double values[])
	{
		final StringBuffer buf = new StringBuffer(str);
		replaceVariables(buf,values);
		return buf.toString();
	}

	/**
	 * Converts a single hex digit to an int
	 * @param c the hex digit, maybe
	 * @return the int representation
	 */
	public final static int hexDigit(final char c)
	{
		if(c<'0')
			return -1;
		if(c<'9')
			return c-'0';
		if(c<'A')
			return -1;
		if(c<'G')
			return (c-'A')+10;
		if(c<'a')
			return -1;
		if(c<'f')
			return (c-'a')+10;
		return -1;
	}

	/**
	 * Converts the given string to a floating
	 * point number, 1&gt;=N&gt;=0, representing
	 * the whole percentage of the string.  The
	 * string format is either X or X%, where 100&gt;=X&gt;=0
	 * If the format is bad, 0.0 is returned.
	 * @param s the string to convert
	 * @return the string converted to a real number
	 */
	public final static double s_pct(String s)
	{
		if(s==null)
			return 0.0;
		while(s.trim().endsWith("%"))
			s=s.trim().substring(0,s.length()-1).trim();
		return s_double(s)/100.0;
	}

	/**
	 * Converts a percentage 1&gt;d&gt;0 to a string.
	 * @param d the number to convert
	 * @return the percentage string.
	 */
	public final static String toPct(final double d)
	{
		final String s=TWO_PLACES.format(d);
		if(s.endsWith("%%"))
			return s.substring(0,s.length()-1);
		return s;
	}

	/**
	 * Converts the string to a double percentage and then
	 * converts that back to a percentage.
	 * @param s the string number
	 * @return the percentage %
	 */
	public final static String toPct(final String s) { return toPct(s_pct(s)); }

	/**
	 * Returns true if the bitnumberth bit (0...) is set
	 * in the given number
	 * @param number the given number
	 * @param bitnumber the bit to check (0,1,2...)
	 * @return true if the given bitnumberth bit is set
	 */
	public final static boolean isSet(final long number, final int bitnumber)
	{
		if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
			return true;
		return false;
	}

	/**
	 * Returns whether the given string is a valid
	 * math expression (5 + 7)/2, etc. Does this
	 * by evaluating the expression and returning
	 * false if an error is found.  No variables
	 * are allowed.
	 * @param st the possible math expression
	 * @return true if it is a math expression
	 */
	public final static boolean isMathExpression(final String st)
	{
		if((st==null)||(st.length()==0))
			return false;
		try
		{
			parseMathExpression(st);
		}
		catch (final Exception e)
		{
			return false;
		}
		return true;
	}
	/**
	 * Returns whether the given string is a valid
	 * math expression (@x1 + 7)/2, etc. Does this
	 * by evaluating the expression and returning
	 * false if an error is found.  All necessary
	 * variables MUST be included (@x1=vars[0])
	 * @param st the possible math expression
	 * @param vars the 0 based variables
	 * @return true if it is a math expression
	 */
	public final static boolean isMathExpression(final String st, final double[] vars)
	{
		if((st==null)||(st.length()==0))
			return false;
		try
		{
			parseMathExpression(st, vars);
		}
		catch (final Exception e)
		{
			return false;
		}
		return true;
	}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variable @xx will refer to current computed value.
	 * Returns 0.0 on any parsing error
	 * @param st a full math expression string
	 * @return the result of the expression
	 */
	public final static double s_parseMathExpression(final String st)
	{
		try
		{
			return parseMathExpression(st);
		}
		catch (final Exception e)
		{
			return 0.0;
		}
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Returns 0.0 on any parsing error
	 * @param st a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public final static double s_parseMathExpression(final String st, final double[] vars)
	{
		try
		{
			return parseMathExpression(st, vars);
		}
		catch (final Exception e)
		{
			return 0.0;
		}
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @return the result of the expression
	 */
	public final static long s_parseLongExpression(final String st)
	{
		try
		{
			return parseLongExpression(st);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public final static long s_parseLongExpression(final String st, final double[] vars)
	{
		try
		{
			return parseLongExpression(st, vars);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}
	
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variable @xx will refer to current computed value.
	 * Round the result to an integer.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @return the result of the expression
	 */
	public final static int s_parseIntExpression(final String st)
	{
		try
		{
			return parseIntExpression(st);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}
	
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to an integer.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public final static int s_parseIntExpression(final String st, final double[] vars)
	{
		try
		{
			return parseIntExpression(st, vars);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * Parse a pre-compiled expression.  Requires a vars variable of at least 10 entries
	 * to ensure NO exceptions (other than /0).
	 * @see CMath#compileMathExpression(StreamTokenizer, boolean)
	 * @param st the tokenizer
	 * @param inParen true if the parse is in the middle of the parenthesis
	 * @param vars the variable values
	 * @param previous the previous value, for operators that require it
	 * @return the final value
	 * @throws ArithmeticException  a parse error, typically arithmetic
	 */
	private final static double parseMathExpression(final StreamTokenizer st, final Random rand, final boolean inParen, final double[] vars, final double previous)
		throws ArithmeticException
	{
		if(!inParen)
		{
			st.ordinaryChar('/');
			st.ordinaryChar('x');
			st.ordinaryChar('X');
		}
		double finalValue=0;
		try
		{
			int c=st.nextToken();
			char lastOperation='+';
			while(c!=StreamTokenizer.TT_EOF)
			{
				double curValue=0.0;
				switch(c)
				{
				case StreamTokenizer.TT_NUMBER:
					curValue=st.nval;
					break;
				case '(':
					curValue=parseMathExpression(st,rand,true,vars,finalValue);
					break;
				case ')':
					if(!inParen)
						throw new ArithmeticException("')' is an unexpected token.");
					return finalValue;
				case '@':
				{
					c=st.nextToken();
					if((c!='x')&&(c!='X'))
						throw new ArithmeticException("'"+c+"' is an unexpected token after @.");
					c=st.nextToken();
					if((c=='x')||(c=='X'))
						curValue=previous;
					else
					{
						if(c!=StreamTokenizer.TT_NUMBER)
							throw new ArithmeticException("'"+c+"' is an unexpected token after @x.");
						if(vars==null)
							throw new ArithmeticException("vars have not been defined for @x"+st.nval);
						if((st.nval>vars.length)||(st.nval<1.0))
							throw new ArithmeticException("'"+st.nval+"/"+vars.length+"' is an illegal variable reference.");
						curValue=vars[((int)st.nval)-1];
					}
					break;
				}
				case '+':
				case '<':
				case '>':
				case '-':
				case '%':
				case '*':
				case '\\':
				case '/':
				case '?':
				case '^':
				{
					lastOperation=(char)c;
					c=st.nextToken();
					continue;
				}
				default:
					throw new ArithmeticException("'"+c+"' is an illegal expression.");
				}
				switch(lastOperation)
				{
				case '<':
					finalValue = finalValue < curValue ? finalValue : curValue;
					break;
				case '>':
					finalValue = finalValue > curValue ? finalValue : curValue;
					break;
				case '+':
					finalValue += curValue;
					break;
				case '-':
					finalValue -= curValue;
					break;
				case '*':
					finalValue *= curValue;
					break;
				case '%':
					finalValue %= curValue;
					break;
				case '/':
				case '\\':
					finalValue /= curValue;
					break;
				case '^':
					finalValue = Math.pow(finalValue, curValue);
					break;
				case '?':
					finalValue = ((curValue - finalValue + 0.5) * rand.nextDouble()) + finalValue;
					break;
				}
				c=st.nextToken();
			}
		}
		catch(final IOException e)
		{
		}
		if(inParen)
			throw new ArithmeticException("')' was missing from this expression");
		return finalValue;
	}

	/**
	 * A class that extends Random, only it always returns the highest possible
	 * values for each method.
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public static final Random NotRandomHigh = new Random(System.currentTimeMillis())
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 8563826975901541973L;

		@Override
		protected int next(int bits)
		{
			return (int)(0xFFFFFFFFFFFFFFFFL >>> (48 - bits));
		}
		
		@Override
		public int nextInt()
		{
			return Integer.MAX_VALUE;
		}
		
		@Override
		public int nextInt(int bound)
		{
			return bound-1;
		}
		
		@Override
		public double nextDouble()
		{
			return 1.0;
		}
		
		@Override
		public float nextFloat()
		{
			return 1.0f;
		}
		
		@Override
		public synchronized double nextGaussian()
		{
			return 1.0;
		}
		
		@Override
		public boolean nextBoolean()
		{
			return true;
		}
		
		@Override
		public void nextBytes(byte[] bytes)
		{
			for(int i=0;i<bytes.length;i++)
				bytes[i]=(byte)255;
		}
	};
	
	/**
	 * A class representing a a list of compiled operation in a complete formula.  
	 * Optomized for speed of execution rather than the obvious wastefulness of storage.
	 */
	public static final class CompiledFormula extends LinkedList<CompiledOperation> implements Cloneable
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= -846934171010829515L;
	}
	
	/**
	 * A class representing a single piece of a compiled operation.  Optomized for
	 * speed of execution rather than the obvious wastefulness of storage.
	 */
	public static final class CompiledOperation
	{
		public static final int		OPERATION_VARIABLE		= 0;
		public static final int		OPERATION_VALUE			= 1;
		public static final int		OPERATION_OPERATION		= 2;
		public static final int		OPERATION_LIST			= 3;
		public static final int		OPERATION_PREVIOUSVALUE	= 4;
		
		public final int	type;
		public int			variableIndex	= 0;
		public double		value			= 0.0;
		public char			operation		= ' ';
		
		public CompiledFormula	list	= null;

		public CompiledOperation(int variableIndex)
		{
			type = OPERATION_VARIABLE;
			this.variableIndex = variableIndex;
		}

		public CompiledOperation(double value)
		{
			type = OPERATION_VALUE;
			this.value = value;
		}

		public CompiledOperation(CompiledFormula list)
		{
			type = OPERATION_LIST;
			this.list = list;
		}

		public CompiledOperation(char operation)
		{
			type = OPERATION_OPERATION;
			this.operation = operation;
		}

		public CompiledOperation()
		{
			type = OPERATION_PREVIOUSVALUE;
		}
	}

	/**
	 * Pre-compiles an expression for faster evaluation later on.
	 * @see CMath#parseMathExpression(CompiledFormula, double[], double)
	 * @param formula the math expression as a string
	 * @return the pre-compiled expression
	 * @throws ArithmeticException a parse error, typically arithmetic
	 */
	public final static CompiledFormula compileMathExpression(final String formula)
	{
		if(formula != null)
			return compileMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false);
		else
			return new CompiledFormula();
	}

	/**
	 * Pre-compiles an expression for faster evaluation later on.
	 * @see CMath#parseMathExpression(String, double[])
	 * @param st the tokenized expression
	 * @param inParen whether or not you are in parenthesis mode
	 * @return the pre-compiled expression
	 * @throws ArithmeticException a parse error, typically arithmetic
	 */
	private final static CompiledFormula compileMathExpression(final StreamTokenizer st, final boolean inParen)
		throws ArithmeticException
	{
		if(!inParen)
		{
			st.ordinaryChar('/');
			st.ordinaryChar('x');
			st.ordinaryChar('X');
		}
		final CompiledFormula list = new CompiledFormula();

		try
		{
			int c=st.nextToken();
			char lastOperation='+';
			while(c!=StreamTokenizer.TT_EOF)
			{
				switch(c)
				{
				case StreamTokenizer.TT_NUMBER:
					list.add(new CompiledOperation(st.nval));
					break;
				case '(':
					list.add(new CompiledOperation(compileMathExpression(st,true)));
					break;
				case ')':
					if(!inParen)
						throw new ArithmeticException("')' is an unexpected token.");
					return list;
				case '@':
				{
					c=st.nextToken();
					if((c!='x')&&(c!='X'))
						throw new ArithmeticException("'"+c+"' is an unexpected token after @.");
					c=st.nextToken();
					if((c=='x')||(c=='X'))
						list.add(new CompiledOperation());
					else
					{
						if(c!=StreamTokenizer.TT_NUMBER)
							throw new ArithmeticException("'"+c+"' is an unexpected token after @x.");
						if((st.nval>11)||(st.nval<1.0))
							throw new ArithmeticException("'"+st.nval+"/11' is an illegal variable reference.");
						list.add(new CompiledOperation(((int)st.nval)-1));
					}
					break;
				}
				case '+':
				case '-':
				case '%':
				case '*':
				case '\\':
				case '/':
				case '?':
				case '<':
				case '>':
				case '^':
				{
					lastOperation=(char)c;
					c=st.nextToken();
					continue;
				}
				default:
					throw new ArithmeticException("'"+(char)c+"' ("+c+") is an illegal expression.");
				}
				switch(lastOperation)
				{
				case '+':
				case '-':
				case '%':
				case '*':
				case '?':
				case '<':
				case '>':
				case '^':
					list.add(new CompiledOperation(lastOperation));
					break;
				case '/':
				case '\\':
					list.add(new CompiledOperation('/'));
					break;
				}
				c=st.nextToken();
			}
		}
		catch(final IOException e)
		{
		}
		if(inParen)
			throw new ArithmeticException("')' was missing from this expression");
		return list;
	}

	/**
	 * Parse a pre-compiled expression.  Requires a vars variable of at least 10 entries
	 * to ensure NO exceptions (other than /0).
	 * @see CMath#compileMathExpression(StreamTokenizer, boolean)
	 * @param list the pre-compiled expression
	 * @param vars the variable values
	 * @param previous the previous value, for operators that require it
	 * @return the final value
	 */
	public final static double parseMathExpression(final CompiledFormula list, final double[] vars, final double previous)
	{
		return parseMathExpression(list, rand, vars, previous);
	}
	
	/**
	 * Parse a pre-compiled expression.  Requires a vars variable of at least 10 entries
	 * to ensure NO exceptions (other than /0).
	 * @see CMath#compileMathExpression(StreamTokenizer, boolean)
	 * @param list the pre-compiled expression
	 * @param rand the random number generator to use
	 * @param vars the variable values
	 * @param previous the previous value, for operators that require it
	 * @return the final value
	 */
	public final static double parseMathExpression(final CompiledFormula list, final Random rand, final double[] vars, final double previous)
	{
		double finalValue=0.0;
		double curValue=0.0;
		for (final CompiledOperation o : list)
		{
			switch(o.type)
			{
			case CompiledOperation.OPERATION_VALUE:
				curValue = o.value;
				break;
			case CompiledOperation.OPERATION_VARIABLE:
				curValue = vars[o.variableIndex];
				break;
			case CompiledOperation.OPERATION_LIST:
				curValue = parseMathExpression(o.list, rand, vars, finalValue);
				break;
			case CompiledOperation.OPERATION_PREVIOUSVALUE:
				curValue = previous;
				break;
			case CompiledOperation.OPERATION_OPERATION:
				switch(o.operation)
				{
				case '+':
					finalValue += curValue;
					break;
				case '-':
					finalValue -= curValue;
					break;
				case '%':
					finalValue %= curValue;
					break;
				case '*':
					finalValue *= curValue;
					break;
				case '/':
					finalValue /= curValue;
					break;
				case '?':
					finalValue = ((curValue - finalValue + 0.5) * rand.nextDouble()) + finalValue;
					break;
				case '<':
					finalValue = finalValue < curValue ? finalValue : curValue;
					break;
				case '>':
					finalValue = finalValue > curValue ? finalValue : curValue;
					break;
				case '^':
					finalValue = Math.pow(finalValue, curValue);
					break;
				}
				break;
			}
		}
		return finalValue;
	}

	/**
	 * Performs the operation between the finalValue and the curValue.
	 * 
	 * @param operation +, -, etc..
	 * @param finalValue the left hand number
	 * @param curValue the right hand number
	 * @return the result of the operation
	 */
	protected double doOperation(final char operation, double finalValue, final double curValue)
	{
		switch(operation)
		{
		case '+':
			finalValue += curValue;
			break;
		case '-':
			finalValue -= curValue;
			break;
		case '%':
			finalValue %= curValue;
			break;
		case '*':
			finalValue *= curValue;
			break;
		case '/':
			finalValue /= curValue;
			break;
		case '?':
			finalValue = ((curValue - finalValue + 0.5) * rand.nextDouble()) + finalValue;
			break;
		case '<':
			finalValue = finalValue < curValue ? finalValue : curValue;
			break;
		case '>':
			finalValue = finalValue > curValue ? finalValue : curValue;
			break;
		case '^':
			finalValue = Math.pow(finalValue, curValue);
			break;
		}
		return finalValue;
	}
	
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @return the result of the expression
	 */
	public final static long parseLongExpression(final String formula)
	{
		return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))), rand, false, null, 0));
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public final static long parseLongExpression(final String formula, final double[] vars)
	{
		return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))), rand, false, vars, 0));
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to an integer.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @return the result of the expression
	 * @throws ArithmeticException  a parse error, typically arithmetic
	 */
	public final static int parseIntExpression(final String formula) throws ArithmeticException
	{
		return (int)Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),rand, false,null,0));
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to an integer.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 * @throws ArithmeticException  a parse error, typically arithmetic
	 */
	public final static int parseIntExpression(final String formula, final double[] vars) throws ArithmeticException
	{
		return (int) Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))), rand, false, vars, 0));
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variable @xx will refer to current computed value.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @return the result of the expression
	 * @throws ArithmeticException a parsing error
	 */
	public final static double parseMathExpression(String formula) throws ArithmeticException
	{
		return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))), rand, false, null, 0);
	}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?&lt;&gt;.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 * @throws ArithmeticException a parsing error
	 */
	public final static double parseMathExpression(final String formula, final double[] vars) throws ArithmeticException
	{
		return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))), rand, false, vars, 0);
	}

	/**
	 * Returns the long value of a string without crashing
	 *
	 * Usage: lSize = WebIQBase.s_long(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param LONG String to convert
	 * @return long Long value of the string
	 */
	public final static long s_long(final String LONG)
	{
		try
		{
			return Long.parseLong(LONG);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * Returns the floating point value of a string without crashing
	 *
	 * Usage: lSize = WebIQBase.s_float(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param FLOAT String to convert
	 * @return Float value of the string
	 */
	public final static float s_float(final String FLOAT)
	{
		try
		{
			return Float.parseFloat(FLOAT);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * Returns the double value of a string without crashing
	 *
	 * Usage: dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param DOUBLE String to convert
	 * @return double Double value of the string
	 */
	public final static double s_double(final String DOUBLE)
	{
		try
		{
			return Double.parseDouble(DOUBLE);
		}
		catch (final Exception e)
		{
			return 0.0;
		}
	}

	/**
	 * Returns the absolute value (X&gt;=0) of the
	 * given number
	 * @param val the number
	 * @return the absolute value of the number
	 */
	public final static int abs(final int val)
	{
		if(val>=0)
			return val;
		return val*-1;
	}

	/**
	 * Returns the first set bit number of the bitmask given
	 * @param mask the bit mask given.
	 * @return the first set bit number of the bitmask given
	 */
	public final static int bitNumber(final long mask)
	{
		if(mask<=0)
			return 0;
		for(int i=0;i<64;i++)
		{
			if((mask&pow(2,i))>0)
				return i+1;
		}
		return 0;
	}

	/**
	 * Returns the absolute value (X&gt;=0) of the
	 * given number
	 * @param val the number
	 * @return the absolute value of the number
	 */
	public final static long abs(final long val)
	{
		if(val>=0)
			return val;
		return val*-1;
	}

	/**
	 * Returns the boolean value of a string without crashing
	 *
	 * Usage: int num=s_bool(CMD.substring(14));
	 * @param BOOL Boolean value of string
	 * @return int Boolean value of the string
	 */
	public final static boolean s_bool(final String BOOL)
	{
		if(BOOL==null)
			return false;
		return Boolean.valueOf(BOOL).booleanValue();
	}

	/**
	 * Returns whether the given string is a boolean value
	 *
	 * Usage: if(isBool(CMD.substring(14)));
	 * @param BOOL Boolean value of string
	 * @return whether it is a boolean
	 */
	public final static boolean isBool(final String BOOL)
	{
		return "true".equalsIgnoreCase(BOOL)||"false".equalsIgnoreCase(BOOL);
	}

	/**
	 * Returns the integer value of a string without crashing
	 *
	 * Usage: int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	public final static int s_int(final String INT)
	{
		try
		{
			return Integer.parseInt(INT);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * Converts the given object into an iteger, if it can
	 * @param O the object to try and convert
	 * @return the int, if possible
	 */
	public final static int s_intOf(final Object O)
	{
		if(O instanceof Integer)
			return ((Integer)O).intValue();
		if(O instanceof Long)
			return ((Long)O).intValue();
		if(O instanceof Double)
			return ((Double)O).intValue();
		return s_int(String.valueOf(O));
	}

	/**
	 * Returns the integer value of a string without crashing
	 *
	 * Usage: int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @param def default value if the given string is not an int
	 * @return int Integer value of the string
	 */
	public final static int s_int(final String INT, final int def)
	{
		try
		{
			return Integer.parseInt(INT);
		}
		catch (final Exception e)
		{
			return def;
		}
	}

	/**
	 * Returns the short value of a string without crashing
	 *
	 * Usage: int num=s_short(CMD.substring(14));
	 * @param SHORT Short value of string
	 * @return short Short value of the string
	 */
	public final static short s_short(final String SHORT)
	{
		try
		{
			return Short.parseShort(SHORT);
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * Returns whether the given string is a long value
	 *
	 * Usage: if(isLong(CMD.substring(14)));
	 * @param LONG Long value of string
	 * @return whether it is a long
	 */
	public final static boolean isLong(final String LONG)
	{
		return isInteger(LONG);
	}

	/**
	 * Returns whether the given string is a int value
	 *
	 * Usage: if(isInteger(CMD.substring(14)));
	 * @param INT Integer value of string
	 * @return whether it is a int
	 */
	public final static boolean isInteger(final String INT)
	{
		if(INT==null)
			return false;
		if(INT.length()==0)
			return false;
		int i=0;
		if(INT.charAt(0)=='-')
		{
			if(INT.length()>1)
				i++;
			else
				return false;
		}
		for(;i<INT.length();i++)
		{
			if(!Character.isDigit(INT.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Returns whether the given string is a float value
	 *
	 * Usage: if(isFloat(CMD.substring(14)));
	 * @param DBL float value of string
	 * @return whether it is a float
	 */
	public final static boolean isFloat(final String DBL)
	{
		return isDouble(DBL);
	}

	/**
	 * Returns a int representing either the given value, or
	 * the 2^ power of the comma separated values in the order
	 * they appear in the given string list.
	 *
	 * Usage: if(s_parseBitIntExpression(CMDS,CMD.substring(14)));
	 * @param bits the ordered string values from 0-whatever.
	 * @param val the expression, or list of string values
	 * @return the int value, or 0
	 */
	public final static int s_parseBitIntExpression(final String[] bits, final String val)
	{
		return (int)s_parseBitLongExpression(bits,val);
	}

	/**
	 * Returns a long representing either the given value, or
	 * the 2^ power of the comma separated values in the order
	 * they appear in the given string list.
	 *
	 * Usage: if(s_parseBitLongExpression(CMDS,CMD.substring(14)));
	 * @param bits the ordered string values from 0-whatever.
	 * @param val the expression, or list of string values
	 * @return the long value, or 0
	 */
	public final static long s_parseBitLongExpression(final String[] bits, String val)
	{
		if((val==null)||(val.trim().length()==0)||(isMathExpression(val)))
			return s_parseLongExpression(val);
		final StringTokenizer tokens=new StringTokenizer(val,",");
		long l=0;
		while(tokens.hasMoreElements())
		{
			val=tokens.nextToken().trim();
			if((val.length()==0)||(isMathExpression(val)))
				l|=s_parseLongExpression(val);
			else
			for(int x=0;x<bits.length;x++)
			{
				if(bits[x].equalsIgnoreCase(val))
				{
					l+=pow(2,x-1);
					break;
				}
			}
		}
		return l;
	}

	/**
	 * Replaces the internal Random object with the one
	 * passed in.  Intended to be used for debugging purposes
	 * only.
	 * @param rand the random object to use
	 */
	public final static void setRand(final Random rand)
	{
		CMath.rand = rand;
	}

	/**
	 * Returns a long representing either the given value, or
	 * the index of the value in the order
	 * they appear in the given string list.
	 *
	 * Usage: if(s_parseListLongExpression(CMDS,CMD.substring(14)));
	 * @param descs the ordered string values from 0-whatever.
	 * @param val the expression, or list of string values
	 * @return the long value, or 0
	 */
	public final static long s_parseListLongExpression(final String[] descs, final String val)
	{
		if((val==null)||(val.trim().length()==0)||(isMathExpression(val)))
			return s_parseLongExpression(val);
		for(int x=0;x<descs.length;x++)
		{
			if(descs[x].equalsIgnoreCase(val))
				return x;
		}
		return 0;
	}

	/**
	 * Returns a int representing either the given value, or
	 * the index of the value in the order
	 * they appear in the given string list.
	 *
	 * Usage: if(s_parseListIntExpression(CMDS,CMD.substring(14)));
	 * @param descs the ordered string values from 0-whatever.
	 * @param val the expression, or list of string values
	 * @return the int value, or 0
	 */
	public final static int s_parseListIntExpression(final String[] descs, final String val)
	{
		return (int) s_parseListLongExpression(descs, val);
	}

	/**
	 * Returns whether the given string is a double value
	 *
	 * Usage: if(isDouble(CMD.substring(14)));
	 * @param DBL double value of string
	 * @return whether it is a double
	 */
	public final static boolean isDouble(final String DBL)
	{
		if(DBL==null)
			return false;
		if(DBL.length()==0)
			return false;
		int i=0;
		if(DBL.charAt(0)=='-')
		{
			if(DBL.length()>1)
				i++;
			else
				return false;
		}
		boolean alreadyDot=false;
		for(;i<DBL.length();i++)
		{
			if(!Character.isDigit(DBL.charAt(i)))
			{
				if(DBL.charAt(i)=='.')
				{
					if(alreadyDot)
						return false;
					alreadyDot=true;
				}
				else
					return false;
			}
		}
		return alreadyDot;
	}

	/**
	 * Returns whether the given string is a number followed
	 * by 1 or more characters.
	 * @param str the string to check
	 * @return true if its a numstring.
	 */
	public final static boolean isNumberFollowedByString(final String str)
	{
		if((str==null)||(str.length()<2))
			return false;
		if(!Character.isDigit(str.charAt(0)))
			return false;
		int dex=1;
		for(;dex<str.length();dex++)
		{
			if(Character.isLetter(str.charAt(dex)))
				break;
			else
			if(!Character.isDigit(str.charAt(dex)))
				return false;
		}
		if(dex>=str.length())
			return false;
		for(;dex<str.length();dex++)
		{
			if(!Character.isLetter(str.charAt(dex)))
				return false;
		}
		return true;
	}

	/**
	 * Returns a number/string pair built from a given string, if it is a string with
	 * a number followed by one or more characters.  Returns null if the given string
	 * does not match that criteria
	 * @param str the string to check
	 * @return a Math.Entry pair of the separated number/string.
	 */
	public final static Entry<Integer,String> getNumberFollowedByString(final String str)
	{
		if((str==null)||(str.length()<2))
			return null;
		if(!Character.isDigit(str.charAt(0)))
			return null;
		int dex=1;
		for(;dex<str.length();dex++)
		{
			if(Character.isLetter(str.charAt(dex)))
				break;
			else
			if(!Character.isDigit(str.charAt(dex)))
				return null;
		}
		if(dex>=str.length())
			return null;
		final int endNumber=dex;
		for(;dex<str.length();dex++)
		{
			if(!Character.isLetter(str.charAt(dex)))
				return null;
		}
		final Integer num=Integer.valueOf(s_int(str.substring(0,endNumber)));
		final String rest=str.substring(endNumber);
		return new Entry<Integer,String>()
		{
			@Override
			public Integer getKey()
			{
				return num;
			}

			@Override
			public String getValue()
			{
				return rest;
			}

			@Override
			public String setValue(String value)
			{
				return value;
			}
		};
	}

	/**
	 * If the given string is 1 or more characters followed by decimal digits, this will return a Map.Entry
	 * with those parts separated.  If the string is characters followed by a roman numeral digits, it will
	 * return a Map.Entry with those parts separated.  Otherwise, it will return a Map.Entry with the string
	 * and a null integer.
	 * @param str the string to check
	 * @param romanOK true to check for roman numerals, false to just check for decimal
	 * @return the Map.Entry
	 */
	public final static Entry<String,Integer> getStringFollowedByNumber(final String str, final boolean romanOK)
	{
		final String codeStr;
		final Integer number;
		if((str==null)||(str.length()<1))
		{
			codeStr=str;
			number=null;
		}
		else
		{
			int dex=str.length()-1;
			if(!Character.isDigit(str.charAt(dex)))
			{
				if((!romanOK)||(!isRomanDigit(str.charAt(dex))))
				{
					codeStr=str;
					number=null;
				}
				else
				{
					dex--;
					while((dex>0)&&(isRomanDigit(str.charAt(dex))))
						dex--;
					codeStr=str.substring(0,dex+1);
					number=Integer.valueOf(convertFromRoman(str.substring(dex+1)));
				}
			}
			else
			{
				dex--;
				while((dex>0)&&(Character.isDigit(str.charAt(dex))))
					dex--;
				codeStr=str.substring(0,dex+1);
				number=Integer.valueOf(s_int(str.substring(dex+1)));
			}
		}
		return new Entry<String,Integer>()
		{
			@Override
			public String getKey()
			{
				return codeStr;
			}

			@Override
			public Integer getValue()
			{
				return number;
			}

			@Override
			public Integer setValue(Integer value)
			{
				return value;
			}
		};
	}

	/**
	 * @see java.lang.Math#round(double)
	 * @param d the real number
	 * @return the rounded number as a long
	 */
	public final static long round(final double d)
	{
		return Math.round(d);
	}

	/**
	 * @see java.lang.Math#round(float)
	 * @param d the real number
	 * @return the rounded number as a long
	 */
	public final static long round(final float d)
	{
		return Math.round(d);
	}

	/**
	 * @see java.lang.Math#abs(double)
	 * @param d the real number
	 * @return the absolute value of the number
	 */
	public final static double abs(final double d)
	{
		return Math.abs(d);
	}

	/**
	 * @see java.lang.Math#abs(float)
	 * @param d the real number
	 * @return the absolute value of the number
	 */
	public final static float abs(final float d)
	{
		return Math.abs(d);
	}

	/**
	 * @see java.lang.Math#random()
	 * @return a random number
	 */
	public final static double random()
	{
		return rand.nextDouble();
	}

	/**
	 * @see java.lang.Math#floor(double)
	 * @see CMath#ceiling(double)
	 * @param d the number to get the floor of
	 * @return the floor of the given number
	 */
	public final static double floor(final double d)
	{
		return Math.floor(d);
	}

	/**
	 * @see java.lang.Math#floor(double)
	 * @see CMath#ceiling(double)
	 * @param d the number to get the floor of
	 * @return the floor of the given number
	 */
	public final static float floor(final float d)
	{
		return (float) Math.floor(d);
	}

	/**
	 * @see java.lang.Math#ceil(double)
	 * @see CMath#floor(double)
	 * @param d the number to get the ceiling of
	 * @return the ceiling of the given number
	 */
	public final static double ceiling(final double d)
	{
		return Math.ceil(d);
	}

	/**
	 * @see java.lang.Math#ceil(double)
	 * @see CMath#floor(float)
	 * @param d the number to get the ceiling of
	 * @return the ceiling of the given number
	 */
	public final static float ceiling(final float d)
	{
		return (float) Math.ceil(d);
	}

	/**
	 * @see java.lang.Math#sqrt(double)
	 * @param d the number to get the square root of
	 * @return the square root of the given number
	 */
	public final static double sqrt(final double d)
	{
		return Math.sqrt(d);
	}

	/**
	 * @see java.lang.Math#sqrt(double)
	 * @param d the number to get the square root of
	 * @return the square root of the given number
	 */
	public final static float sqrt(final float d)
	{
		return (float) Math.sqrt(d);
	}

	/**
	 * Returns greater of two numbers
	 * @param a first number
	 * @param b second number
	 * @return greater of the two
	 */
	public final static double greater(final double a, final double b)
	{
		return a<b?b:a;
	}
	
	/**
	 * Generates a big integer from multiply two longs
	 * @param l1 the first long
	 * @param l2 the second long
	 * @return the big big integer
	 */
	public final static BigInteger bigMultiply(long l1, long l2)
	{
		return BigInteger.valueOf(l1).multiply(BigInteger.valueOf(l2));
	}
	
	/**
	 * Generates a big integer from multiply two numbers, rounding when necessary
	 * @param l1 the first number
	 * @param l2 the second number
	 * @return the big big integer
	 */
	public final static BigInteger bigMultiply(double l1, long l2)
	{
		return BigInteger.valueOf(Math.round(l1)).multiply(BigInteger.valueOf(l2));
	}
}
