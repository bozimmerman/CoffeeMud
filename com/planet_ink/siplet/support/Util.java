package com.planet_ink.siplet.support;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Util
{
	public final static String	SPACES	= "                                                                     ";

	public static String toSemicolonList(byte[] bytes)
	{
		final StringBuffer str = new StringBuffer("");
		for (int b = 0; b < bytes.length; b++)
			str.append(Byte.toString(bytes[b]) + (b < (bytes.length - 1) ? ";" : ""));
		return str.toString();
	}

	public static String toSemicolonList(String[] bytes)
	{
		final StringBuffer str = new StringBuffer("");
		for (int b = 0; b < bytes.length; b++)
			str.append(bytes[b] + (b < (bytes.length - 1) ? ";" : ""));
		return str.toString();
	}

	public static String toSemicolonList(Vector<String> bytes)
	{
		final StringBuffer str = new StringBuffer("");
		for (int b = 0; b < bytes.size(); b++)
			str.append(bytes.elementAt(b) + (b < (bytes.size() - 1) ? ";" : ""));
		return str.toString();
	}

	public static byte[] fromByteList(String str)
	{
		final Vector<String> V = parseSemicolons(str, true);
		if (V.size() > 0)
		{
			final byte[] bytes = new byte[V.size()];
			for (int b = 0; b < V.size(); b++)
				bytes[b] = Byte.parseByte(V.elementAt(b));
			return bytes;
		}
		return new byte[0];
	}

	public static long absDiff(long x, long y)
	{
		final long d = x - y;
		if (d < 0)
			return d * -1;
		return d;
	}

	public static String repeat(String str1, int times)
	{
		if (times <= 0)
			return "";
		final StringBuffer str = new StringBuffer("");
		for (int i = 0; i < times; i++)
			str.append(str1);
		return str.toString();
	}

	public static String endWithAPeriod(String str)
	{
		if (str.length() == 0)
			return str;
		int x = str.length() - 1;
		while ((x >= 0) && ((Character.isWhitespace(str.charAt(x))) || ((x > 0) && ((str.charAt(x) != '^') && (str.charAt(x - 1) == '^') && ((--x) >= 0)))))
			x--;
		if (x < 0)
			return str;
		if ((str.charAt(x) == '.') || (str.charAt(x) == '!') || (str.charAt(x) == '?'))
			return str.trim() + " ";
		return str.substring(0, x + 1) + ". " + str.substring(x + 1).trim();
	}

	public static String startWithAorAn(String str)
	{
		if (str.length() == 0)
			return str;
		if ((!str.toUpperCase().startsWith("A ")) && (!str.toUpperCase().startsWith("AN ")) && (!str.toUpperCase().startsWith("THE ")) && (!str.toUpperCase().startsWith("SOME ")))
		{
			if ("aeiouAEIOU".indexOf(str.charAt(0)) >= 0)
				return "an " + str;
			return "a " + str;
		}
		return str;
	}

	public static int getParmInt(String text, String key, int defaultValue)
	{
		int x = text.toUpperCase().indexOf(key.toUpperCase());
		while (x >= 0)
		{
			if ((x == 0) || (!Character.isLetter(text.charAt(x - 1))))
			{
				while ((x < text.length()) && (text.charAt(x) != '=') && (!Character.isDigit(text.charAt(x))))
				{
					if ((text.charAt(x) == '+') || (text.charAt(x) == '-'))
						return defaultValue;
					x++;
				}
				if ((x < text.length()) && (text.charAt(x) == '='))
				{
					while ((x < text.length()) && (!Character.isDigit(text.charAt(x))))
						x++;
					if (x < text.length())
					{
						text = text.substring(x);
						x = 0;
						while ((x < text.length()) && (Character.isDigit(text.charAt(x))))
							x++;
						return Util.s_int(text.substring(0, x));
					}
				}
				x = -1;
			}
			else
				x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
		}
		return defaultValue;
	}

	public static boolean isVowel(char c)
	{
		return (("aeiou").indexOf(Character.toLowerCase(c)) >= 0);
	}

	public static int getParmPlus(String text, String key)
	{
		int x = text.toUpperCase().indexOf(key.toUpperCase());
		while (x >= 0)
		{
			if ((x == 0) || (!Character.isLetter(text.charAt(x - 1))))
			{
				while ((x < text.length()) && (text.charAt(x) != '+') && (text.charAt(x) != '-'))
				{
					if (text.charAt(x) == '=')
						return 0;
					x++;
				}
				if (x < text.length())
				{
					final char pm = text.charAt(x);
					while ((x < text.length()) && (!Character.isDigit(text.charAt(x))))
						x++;
					if (x < text.length())
					{
						text = text.substring(x);
						x = 0;
						while ((x < text.length()) && (Character.isDigit(text.charAt(x))))
							x++;
						if (pm == '+')
							return Util.s_int(text.substring(0, x));
						return -Util.s_int(text.substring(0, x));
					}
				}
				x = -1;
			}
			else
				x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
		}
		return 0;
	}

	public static double getParmDoublePlus(String text, String key)
	{
		int x = text.toUpperCase().indexOf(key.toUpperCase());
		while (x >= 0)
		{
			if ((x == 0) || (!Character.isLetter(text.charAt(x - 1))))
			{
				while ((x < text.length()) && (text.charAt(x) != '+') && (text.charAt(x) != '-'))
				{
					if (text.charAt(x) == '=')
						return 0.0;
					x++;
				}
				if (x < text.length())
				{
					final char pm = text.charAt(x);
					while ((x < text.length()) && (!Character.isDigit(text.charAt(x))) && (text.charAt(x) != '.'))
						x++;
					if (x < text.length())
					{
						text = text.substring(x);
						x = 0;
						while ((x < text.length()) && ((Character.isDigit(text.charAt(x))) || (text.charAt(x) == '.')))
							x++;
						if (text.substring(0, x).indexOf('.') < 0)
						{
							if (pm == '+')
								return Util.s_int(text.substring(0, x));
							return -Util.s_int(text.substring(0, x));
						}
						if (pm == '+')
							return Util.s_double(text.substring(0, x));
						return -Util.s_double(text.substring(0, x));
					}
				}
				x = -1;
			}
			else
				x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
		}
		return 0.0;
	}

	public static double getParmDouble(String text, String key, double defaultValue)
	{
		int x = text.toUpperCase().indexOf(key.toUpperCase());
		while (x >= 0)
		{
			if ((x == 0) || (!Character.isLetter(text.charAt(x - 1))))
			{
				while ((x < text.length()) && (text.charAt(x) != '='))
					x++;
				if (x < text.length())
				{
					while ((x < text.length()) && (!Character.isDigit(text.charAt(x))) && (text.charAt(x) != '.'))
						x++;
					if (x < text.length())
					{
						text = text.substring(x);
						x = 0;
						while ((x < text.length()) && ((Character.isDigit(text.charAt(x))) || (text.charAt(x) == '.')))
							x++;
						if (text.substring(0, x).indexOf('.') < 0)
							return Util.s_long(text.substring(0, x));
						return Util.s_double(text.substring(0, x));
					}
				}
				x = -1;
			}
			else
				x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
		}
		return defaultValue;
	}

	public static String getParmStr(String text, String key, String defaultVal)
	{
		int x = text.toUpperCase().indexOf(key.toUpperCase());
		while (x >= 0)
		{
			if ((x == 0) || (!Character.isLetter(text.charAt(x - 1))))
			{
				while ((x < text.length()) && (text.charAt(x) != '='))
				{
					if ((text.charAt(x) == '+') || (text.charAt(x) == '-'))
						return defaultVal;
					x++;
				}
				if (x < text.length())
				{
					boolean endWithQuote = false;
					while ((x < text.length()) && (!Character.isLetterOrDigit(text.charAt(x))))
					{
						if (text.charAt(x) == '\"')
						{
							endWithQuote = true;
							x++;
							break;
						}
						x++;
					}
					if (x < text.length())
					{
						text = text.substring(x);
						x = 0;
						while ((x < text.length()) && ((Character.isLetterOrDigit(text.charAt(x))) || ((endWithQuote) && (text.charAt(x) != '\"'))))
							x++;
						return text.substring(0, x).trim();
					}

				}
				x = -1;
			}
			else
				x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
		}
		return defaultVal;
	}

	public static String[] toStringArray(Vector<String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			final String[] s = new String[0];
			return s;
		}
		final String[] s = new String[V.size()];
		for (int v = 0; v < V.size(); v++)
			s[v] = V.elementAt(v).toString();
		return s;
	}

	public static long[] toLongArray(Vector<String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			final long[] s = new long[0];
			return s;
		}
		final long[] s = new long[V.size()];
		for (int v = 0; v < V.size(); v++)
			s[v] = Util.s_long(V.elementAt(v).toString());
		return s;
	}

	public static int[] toIntArray(Vector<String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			final int[] s = new int[0];
			return s;
		}
		final int[] s = new int[V.size()];
		for (int v = 0; v < V.size(); v++)
			s[v] = Util.s_int(V.elementAt(v).toString());
		return s;
	}

	public static String[] toStringArray(HashSet<String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			final String[] s = new String[0];
			return s;
		}
		final String[] s = new String[V.size()];
		int v = 0;
		for (final Iterator i = V.iterator(); i.hasNext();)
			s[v++] = (i.next()).toString();
		return s;
	}

	public static String toStringList(String[] V)
	{
		if ((V == null) || (V.length == 0))
		{
			return "";
		}
		final StringBuffer s = new StringBuffer("");
		for (final String element : V)
			s.append(", " + element);
		if (s.length() == 0)
			return "";
		return s.toString().substring(2);
	}

	public static String toStringList(long[] V)
	{
		if ((V == null) || (V.length == 0))
		{
			return "";
		}
		final StringBuffer s = new StringBuffer("");
		for (final long element : V)
			s.append(", " + element);
		if (s.length() == 0)
			return "";
		return s.toString().substring(2);
	}

	public static String toStringList(int[] V)
	{
		if ((V == null) || (V.length == 0))
		{
			return "";
		}
		final StringBuffer s = new StringBuffer("");
		for (final int element : V)
			s.append(", " + element);
		if (s.length() == 0)
			return "";
		return s.toString().substring(2);
	}

	public static String toStringList(Vector<String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			return "";
		}
		final StringBuffer s = new StringBuffer("");
		for (int v = 0; v < V.size(); v++)
			s.append(", " + V.elementAt(v).toString());
		if (s.length() == 0)
			return "";
		return s.toString().substring(2);
	}

	public static String toStringList(HashSet<String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			return "";
		}
		final StringBuffer s = new StringBuffer("");
		for (final Iterator i = V.iterator(); i.hasNext();)
			s.append(", " + i.next().toString());
		if (s.length() == 0)
			return "";
		return s.toString().substring(2);
	}

	public static Vector<String> makeVector(String[] O)
	{
		final Vector<String> V = new Vector<String>();
		if (O != null)
			for (final String element : O)
				V.addElement(element);
		return V;
	}

	public static Vector<String> makeVector()
	{
		return new Vector<String>();
	}

	public static Vector<Object> makeVector(Object O)
	{
		final Vector<Object> V = new Vector<Object>();
		V.addElement(O);
		return V;
	}

	public static Vector<Object> makeVector(Object O, Object O2)
	{
		final Vector<Object> V = new Vector<Object>();
		V.addElement(O);
		V.addElement(O2);
		return V;
	}

	public static Vector<Object> makeVector(Object O, Object O2, Object O3)
	{
		final Vector<Object> V = new Vector<Object>();
		V.addElement(O);
		V.addElement(O2);
		V.addElement(O3);
		return V;
	}

	public static Vector<Object> makeVector(Object O, Object O2, Object O3, Object O4)
	{
		final Vector<Object> V = new Vector<Object>();
		V.addElement(O);
		V.addElement(O2);
		V.addElement(O3);
		V.addElement(O4);
		return V;
	}

	public static HashSet<Object> makeHashSet()
	{
		return new HashSet<Object>();
	}

	public static HashSet<Object> makeHashSet(Object O)
	{
		final HashSet<Object> H = new HashSet<Object>();
		H.add(O);
		return H;
	}

	public static HashSet<Object> makeHashSet(Object O, Object O2)
	{
		final HashSet<Object> H = new HashSet<Object>();
		H.add(O);
		H.add(O2);
		return H;
	}

	public static HashSet<Object> makeHashSet(Object O, Object O2, Object O3)
	{
		final HashSet<Object> H = new HashSet<Object>();
		H.add(O);
		H.add(O2);
		H.add(O3);
		return H;
	}

	public static HashSet<Object> makeHashSet(Object O, Object O2, Object O3, Object O4)
	{
		final HashSet<Object> H = new HashSet<Object>();
		H.add(O);
		H.add(O2);
		H.add(O3);
		H.add(O4);
		return H;
	}

	public static String[] toStringArray(Hashtable<String,String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			final String[] s = new String[0];
			return s;
		}
		final String[] s = new String[V.size()];
		int v = 0;
		for (final Enumeration e = V.keys(); e.hasMoreElements();)
		{
			final String KEY = (String) e.nextElement();
			s[v] = V.get(KEY);
			v++;
		}
		return s;
	}

	public static void addToVector(Vector<Object> from, Vector<Object> to)
	{
		if (from != null)
		{
			for (int i = 0; i < from.size(); i++)
				to.addElement(from.elementAt(i));
		}
	}

	public static String toStringList(Hashtable<String,String> V)
	{
		if ((V == null) || (V.size() == 0))
		{
			return "";
		}
		final StringBuffer s = new StringBuffer("");
		for (final Enumeration e = V.keys(); e.hasMoreElements();)
		{
			final String KEY = (String) e.nextElement();
			s.append(KEY + "=" + (V.get(KEY)) + "/");
		}
		return s.toString();
	}

	public static String replaceAll(String str, String thisStr, String withThisStr)
	{
		if ((str == null) || (thisStr == null) || (withThisStr == null) || (str.length() == 0) || (thisStr.length() == 0))
			return str;
		for (int i = str.length() - 1; i >= 0; i--)
		{
			if (str.charAt(i) == thisStr.charAt(0))
			{
				if (str.substring(i).startsWith(thisStr))
					str = str.substring(0, i) + withThisStr + str.substring(i + thisStr.length());
			}
		}
		return str;
	}

	public static String replaceAllIgnoreCase(String str, String thisStr, String withThisStr)
	{
		if ((str == null) || (thisStr == null) || (withThisStr == null) || (str.length() == 0) || (thisStr.length() == 0))
			return str;
		for (int i = str.length() - 1; i >= 0; i--)
		{
			if (Character.toUpperCase(str.charAt(i)) == Character.toUpperCase(thisStr.charAt(0)))
			{
				if (str.substring(i).toUpperCase().startsWith(thisStr.toUpperCase()))
				{
					final boolean isUpperCase = Character.isUpperCase(str.charAt(i));
					if (withThisStr.length() > 0)
						withThisStr = (isUpperCase ? Character.toUpperCase(withThisStr.charAt(0)) : Character.toLowerCase(withThisStr.charAt(0))) + withThisStr.substring(1);
					str = str.substring(0, i) + withThisStr + str.substring(i + thisStr.length());
				}
			}
		}
		return str;
	}

	public static String replaceFirst(String str, String thisStr, String withThisStr)
	{
		if ((str == null) || (thisStr == null) || (withThisStr == null) || (str.length() == 0) || (thisStr.length() == 0))
			return str;
		for (int i = str.length() - 1; i >= 0; i--)
		{
			if (str.charAt(i) == thisStr.charAt(0))
			{
				if (str.substring(i).startsWith(thisStr))
				{
					str = str.substring(0, i) + withThisStr + str.substring(i + thisStr.length());
					return str;
				}
			}
		}
		return str;
	}

	public static boolean isInteger(String INT)
	{
		if (INT.length() == 0)
			return false;
		if (INT.startsWith("-") && (INT.length() > 1))
			INT = INT.substring(1);
		for (int i = 0; i < INT.length(); i++)
		{
			if (!Character.isDigit(INT.charAt(i)))
				return false;
		}
		return true;
	}

	public static boolean isDouble(String DBL)
	{
		if (DBL.length() == 0)
			return false;
		if (DBL.startsWith("-") && (DBL.length() > 1))
			DBL = DBL.substring(1);
		boolean alreadyDot = false;
		for (int i = 0; i < DBL.length(); i++)
		{
			if (!Character.isDigit(DBL.charAt(i)))
			{
				if (DBL.charAt(i) == '.')
				{
					if (alreadyDot)
						return false;
					alreadyDot = true;
				}
				else
					return false;
			}
		}
		return alreadyDot;
	}

	public static String capitalizeAndLower(String name)
	{
		return (Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase()).trim();
	}

	public static String capitalizeFirstLetter(String name)
	{
		return (Character.toUpperCase(name.charAt(0)) + name.substring(1)).trim();
	}

	/**
	 * Returns the boolean value of a string without crashing
	 * 
	 * 
	 * 
	 * Usage: int num=s_bool(CMD.substring(14));
	 * 
	 * @param INT
	 *            Boolean value of string
	 * @return int Boolean value of the string
	 */
	public static boolean s_bool(String BOOL)
	{
		return Boolean.valueOf(BOOL).booleanValue();
	}

	/**
	 * Returns the integer value of a string without crashing
	 * 
	 * 
	 * 
	 * Usage: int num=s_int(CMD.substring(14));
	 * 
	 * @param INT
	 *            Integer value of string
	 * @return int Integer value of the string
	 */
	public static int s_int(String INT)
	{
		int sint = 0;
		try
		{
			sint = Integer.parseInt(INT);
		}
		catch (final Exception e)
		{
			return 0;
		}
		return sint;
	}

	/**
	 * Returns the integer value of a string without crashing
	 * 
	 * 
	 * 
	 * Usage: int num=s0_int(CMD.substring(14));
	 * 
	 * @param INT
	 *            Integer value of string
	 * @return int Integer value of the string
	 */
	public static int s0_int(String INT)
	{
		int sint = 0;
		while ((INT.length() > 0) && (INT.startsWith("0")))
			INT = INT.substring(1);
		try
		{
			sint = Integer.parseInt(INT);
		}
		catch (final Exception e)
		{
			return 0;
		}
		return sint;
	}

	public static String lastWordIn(String thisStr)
	{
		final int x = thisStr.lastIndexOf(' ');
		if (x >= 0)
			return thisStr.substring(x + 1);
		return thisStr;
	}

	public static String removeColors(String s)
	{
		final StringBuffer str = new StringBuffer(s);
		int colorStart = -1;
		for (int i = 0; i < str.length(); i++)
		{
			switch (str.charAt(i))
			{
			case 'm':
				if (colorStart >= 0)
				{
					str.delete(colorStart, i + 1);
					colorStart = -1;
				}
				break;
			case (char) 27:
				colorStart = i;
				break;
			case '^':
				if ((i + 1) < str.length())
				{
					final int tagStart = i;
					final char c = str.charAt(i + 1);
					if ((c == '<') || (c == '&'))
					{
						i += 2;
						while (i < (str.length() - 1))
						{
							if (((c == '<') && ((str.charAt(i) != '^') || (str.charAt(i + 1) != '>'))) || ((c == '&') && (str.charAt(i) != ';')))
							{
								i++;
								if (i >= (str.length() - 1))
								{
									i = tagStart;
									str.delete(i, i + 2);
									i--;
									break;
								}
							}
							else
							{
								if (c == '<')
									str.delete(tagStart, i + 2);
								else
									str.delete(tagStart, i + 1);
								i = tagStart - 1;
								break;
							}
						}
					}
					else
					{
						str.delete(i, i + 2);
						i--;
					}
				}
				else
				{
					str.delete(i, i + 2);
					i--;
				}
				break;
			}
		}
		return str.toString();
	}

	public static String returnTime(long millis, long ticks)
	{
		String avg = "";
		if (ticks > 0)
			avg = ", Average=" + (millis / ticks) + "ms";
		if (millis < 1000)
			return millis + "ms" + avg;
		long seconds = millis / 1000;
		millis -= (seconds * 1000);
		if (seconds < 60)
			return seconds + "s " + millis + "ms" + avg;
		long minutes = seconds / 60;
		seconds -= (minutes * 60);
		if (minutes < 60)
			return minutes + "m " + seconds + "s " + millis + "ms" + avg;
		long hours = minutes / 60;
		minutes -= (hours * 60);
		if (hours < 24)
			return hours + "h " + minutes + "m " + seconds + "s " + millis + "ms" + avg;
		final long days = hours / 24;
		hours -= (days * 24);
		return days + "d " + hours + "h " + minutes + "m " + seconds + "s " + millis + "ms" + avg;

	}

	public static Vector<Object> copyVector(Vector<Object> V)
	{
		final Vector<Object> V2 = new Vector<Object>();
		for (int v = 0; v < V.size(); v++)
		{
			final Object h = V.elementAt(v);
			if (h instanceof Vector)
				V2.addElement(copyVector((Vector) h));
			else
				V2.addElement(h);
		}
		return V2;
	}

	public static int numBits(String s)
	{
		int i = 0;
		int num = 0;
		boolean in = false;
		char c = (char) 0;
		char fc = (char) 0;
		char lc = (char) 0;
		s = s.trim();
		while (i < s.length())
		{
			c = s.charAt(i);
			boolean white = (Character.isWhitespace(c) || (c == ' ') || (c == '	') || (c == '\t'));
			if (white && in && (((fc == '\'') && (lc != '\'')) || ((fc == '`') && (lc != '`'))))
				white = false;
			if (white && in)
			{
				num++;
				c = (char) 0;
				lc = (char) 0;
				fc = (char) 0;
				in = false;
			}
			else 
			if (!white)
			{
				if (!in)
				{
					in = true;
					fc = c;
					lc = (char) 0;
				}
				else
					lc = c;
			}
			i++;
		}
		if (in)
			return num + 1;
		return num;
	}

	public static String cleanBit(String s)
	{
		while (s.startsWith(" "))
			s = s.substring(1);
		while (s.endsWith(" "))
			s = s.substring(0, s.length() - 1);
		if ((s.startsWith("'")) || (s.startsWith("`")))
		{
			s = s.substring(1);
			if ((s.endsWith("'")) || (s.endsWith("`")))
				s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public static String getCleanBit(String s, int which)
	{
		return cleanBit(getBit(s, which));
	}

	public static String getPastBitClean(String s, int which)
	{
		return cleanBit(getPastBit(s, which));
	}

	public static String getPastBit(String s, int which)
	{
		int i = 0;
		int w = 0;
		boolean in = false;
		s = s.trim();
		char c = (char) 0;
		char lc = (char) 0;
		char fc = (char) 0;
		while (i < s.length())
		{
			c = s.charAt(i);
			boolean white = (Character.isWhitespace(c) || (c == ' ') || (c == '	') || (c == '\t'));
			if (white && in && (((fc == '\'') && (lc != '\'')) || ((fc == '`') && (lc != '`'))))
				white = false;
			if (white && in)
			{
				if (w == which)
				{
					s = s.substring(i + 1);
					if (((s.trim().startsWith("'")) || (s.trim().startsWith("`"))) && ((s.trim().startsWith("'")) || (s.trim().startsWith("`"))))
						s = s.trim().substring(1, s.length() - 1);
					return s;
				}
				w++;
				in = false;
				c = (char) 0;
				lc = (char) 0;
				fc = (char) 0;
			}
			else 
			if (!white)
			{
				if (!in)
				{
					fc = c;
					lc = (char) 0;
					in = true;
				}
				else
					lc = c;
			}
			i++;
		}
		return "";
	}

	public static String getBit(String s, int which)
	{
		int i = 0;
		int w = 0;
		boolean in = false;
		s = s.trim();
		String t = "";
		char c = (char) 0;
		char lc = (char) 0;
		char fc = (char) 0;
		while (i < s.length())
		{
			c = s.charAt(i);
			boolean white = (Character.isWhitespace(c) || (c == ' ') || (c == '	') || (c == '\t'));
			if (white && in && (((fc == '\'') && (lc != '\'')) || ((fc == '`') && (lc != '`'))))
				white = false;
			if (white && in)
			{
				if (w == which)
					return t;
				w++;
				in = false;
				c = (char) 0;
				lc = (char) 0;
				fc = (char) 0;
			}
			else 
			if (!white)
			{
				if (!in)
				{
					t = "";
					fc = c;
					lc = (char) 0;
					in = true;
				}
				else
					lc = c;
				t += c;
			}
			i++;
		}
		if (in)
			return t;
		return "";
	}

	/**
	 * Returns the long value of a string without crashing
	 * 
	 * 
	 * 
	 * Usage: lSize =
	 * WebIQBase.s_long(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * 
	 * @param long String to convert
	 * @return long Long value of the string
	 */
	public static long s_long(String LONG)
	{
		long slong = 0;
		try
		{
			slong = Long.parseLong(LONG);
		}
		catch (final Exception e)
		{
			return 0;
		}
		return slong;
	}

	/**
	 * Returns the double value of a string without crashing
	 * 
	 * 
	 * 
	 * Usage: dSize =
	 * WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * 
	 * @param double String to convert
	 * @return double Double value of the string
	 */
	public static double s_double(String DOUBLE)
	{
		double sdouble = 0;
		try
		{
			sdouble = Double.parseDouble(DOUBLE);
		}
		catch (final Exception e)
		{
			return 0;
		}
		return sdouble;
	}

	public static String combine(List<String> commands, int startAt, int endAt)
	{
		final StringBuffer Combined = new StringBuffer("");
		if (commands != null)
			for (int commandIndex = startAt; commandIndex < endAt; commandIndex++)
				Combined.append(commands.get(commandIndex) + " ");
		return Combined.toString().trim();
	}

	public static String combineWithQuotes(List<String> commands, int startAt, int endAt)
	{
		final StringBuffer Combined = new StringBuffer("");
		if (commands != null)
			for (int commandIndex = startAt; commandIndex < endAt; commandIndex++)
			{
				String s = commands.get(commandIndex);
				if (s.indexOf(' ') >= 0)
					s = "\"" + s + "\"";
				Combined.append(s + " ");
			}
		return Combined.toString().trim();
	}

	public static String combineAfterIndexWithQuotes(List<String> commands, String match)
	{
		final StringBuffer Combined = new StringBuffer("");
		if (commands != null)
			for (int commandIndex = 0; commandIndex < 0; commandIndex++)
			{
				String s = commands.get(commandIndex);
				if (s.indexOf(' ') >= 0)
					s = "\"" + s + "\"";
				Combined.append(s + " ");
			}
		return Combined.toString().trim();
	}

	public static String combineWithQuotes(List<String> commands, int startAt)
	{
		final StringBuffer Combined = new StringBuffer("");
		if (commands != null)
			for (int commandIndex = startAt; commandIndex < commands.size(); commandIndex++)
			{
				String s = commands.get(commandIndex);
				if (s.indexOf(' ') >= 0)
					s = "\"" + s + "\"";
				Combined.append(s + " ");
			}
		return Combined.toString().trim();
	}

	public static String combine(List<String> commands, int startAt)
	{
		final StringBuffer Combined = new StringBuffer("");
		if (commands != null)
			for (int commandIndex = startAt; commandIndex < commands.size(); commandIndex++)
				Combined.append(commands.get(commandIndex) + " ");
		return Combined.toString().trim();
	}

	public static List<String> parse(String str)
	{
		return parse(str, -1);
	}

	public static List<String> paramParse(String str)
	{
		final List<String> commands = parse(str);
		for (int i = 0; i < commands.size(); i++)
		{
			final String s = commands.get(i);
			if (s.startsWith("=") && (s.length() > 1) && (i > 0))
			{
				final String prev = commands.get(i - 1);
				commands.set(i-1,prev + s);
				commands.remove(i);
				i--;
			}
			else 
			if (s.endsWith("=") && (s.length() > 1) && (i < (commands.size() - 1)))
			{
				final String next = commands.get(i + 1);
				commands.set(i, s + next);
				commands.remove(i + 1);
			}
			else 
			if (s.equals("=") && ((i > 0) && (i < (commands.size() - 1))))
			{
				final String prev = commands.get(i - 1);
				final String next = commands.get(i + 1);
				commands.set(i-1, prev + "=" + next);
				commands.remove(i);
				commands.remove(i + 1);
				i--;
			}
		}
		return commands;
	}

	public static List<String> parse(String str, int upTo)
	{
		final List<String> commands = new Vector<String>();
		if (str == null)
			return commands;
		str = str.trim();
		while (!str.equals(""))
		{
			final int spaceIndex = str.indexOf(' ');
			final int strIndex = str.indexOf("\"");
			String CMD = "";
			if ((strIndex >= 0) && ((strIndex < spaceIndex) || (spaceIndex < 0)))
			{
				final int endStrIndex = str.indexOf("\"", strIndex + 1);
				if (endStrIndex > strIndex)
				{
					CMD = str.substring(strIndex + 1, endStrIndex).trim();
					str = str.substring(endStrIndex + 1).trim();
				}
				else
				{
					CMD = str.substring(strIndex + 1).trim();
					str = "";
				}
			}
			else 
			if (spaceIndex >= 0)
			{
				CMD = str.substring(0, spaceIndex).trim();
				str = str.substring(spaceIndex + 1).trim();
			}
			else
			{
				CMD = str.trim();
				str = "";
			}
			if (!CMD.equals(""))
			{
				commands.add(CMD);
				if ((upTo >= 0) && (commands.size() >= upTo))
				{
					if (str.length() > 0)
						commands.add(str);
					break;
				}

			}
		}
		return commands;
	}

	public static String stripBadHTMLTags(String s)
	{
		final StringBuffer buf = new StringBuffer(s);
		final Vector<Character> quotes = new Vector<Character>();
		int i = -1;
		int start = -1;
		StringBuffer bit = null;
		String lastTag = null;
		while ((++i) < buf.length())
		{
			switch (buf.charAt(i))
			{
			case '<':
				if (quotes.size() > 0)
					break;
				bit = new StringBuffer("");
				lastTag = null;
				start = i;
				break;
			case '>':
				if (bit != null)
					lastTag = bit.toString();
				if ((quotes.size() == 0) && (start >= 0) && (i - start > 0) && (lastTag != null) && (lastTag.trim().equalsIgnoreCase("FONT")))
				{
					final int distance = (i - start) + 1;
					buf.delete(start, i + 1);
					i = i - distance;
				}
				bit = null;
				lastTag = null;
				start = -1;
				break;
			case ' ':
				if (bit != null)
				{
					lastTag = bit.toString();
					bit = null;
				}
				break;
			case '"':
			case '\'':
				if (start < 0)
					break;
				if ((quotes.size() > 0) && (quotes.lastElement().charValue() == buf.charAt(i)))
					quotes.removeElementAt(quotes.size() - 1);
				else
					quotes.addElement(new Character(buf.charAt(i)));
				break;
			default:
				if (bit != null)
					bit.append(buf.charAt(i));
				break;
			}
		}
		return buf.toString();
	}

	public static Vector<String> parseCommas(String s, boolean ignoreNulls)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf(',');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x).trim();
			s = s.substring(x + 1).trim();
			if ((!ignoreNulls) || (s2.length() > 0))
				V.addElement(s2);
			x = s.indexOf(',');
		}
		if ((!ignoreNulls) || (s.trim().length() > 0))
			V.addElement(s.trim());
		return V;
	}

	public static Vector<String> parsePipes(String s, boolean ignoreNulls)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf('|');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x).trim();
			s = s.substring(x + 1).trim();
			if ((!ignoreNulls) || (s2.length() > 0))
				V.addElement(s2);
			x = s.indexOf('|');
		}
		if ((!ignoreNulls) || (s.trim().length() > 0))
			V.addElement(s.trim());
		return V;
	}

	public static Vector<String> parseSquiggles(String s)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf('~');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x).trim();
			s = s.substring(x + 1).trim();
			V.addElement(s2);
			x = s.indexOf('~');
		}
		return V;
	}

	public static Vector<String> parseSentences(String s)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf('.');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x + 1);
			s = s.substring(x + 1);
			V.addElement(s2);
			x = s.indexOf('.');
		}
		return V;
	}

	public static Vector<String> parseSquiggleDelimited(String s, boolean ignoreNulls)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf('~');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x).trim();
			s = s.substring(x + 1).trim();
			if ((s2.length() > 0) || (!ignoreNulls))
				V.addElement(s2);
			x = s.indexOf('~');
		}
		if ((s.length() > 0) || (!ignoreNulls))
			V.addElement(s);
		return V;
	}

	public static Vector<String> parseSemicolons(String s, boolean ignoreNulls)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf(';');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x).trim();
			s = s.substring(x + 1).trim();
			if ((!ignoreNulls) || (s2.length() > 0))
				V.addElement(s2);
			x = s.indexOf(';');
		}
		if ((!ignoreNulls) || (s.trim().length() > 0))
			V.addElement(s.trim());
		return V;
	}

	public static int abs(int val)
	{
		if (val >= 0)
			return val;
		return val * -1;
	}

	public static long abs(long val)
	{
		if (val >= 0)
			return val;
		return val * -1;
	}

	public static Vector<String> parseSpaces(String s, boolean ignoreNulls)
	{
		final Vector<String> V = new Vector<String>();
		if ((s == null) || (s.length() == 0))
			return V;
		int x = s.indexOf(' ');
		while (x >= 0)
		{
			final String s2 = s.substring(0, x).trim();
			s = s.substring(x + 1).trim();
			if ((!ignoreNulls) || (s2.length() > 0))
				V.addElement(s2);
			x = s.indexOf(' ');
		}
		if ((!ignoreNulls) || (s.trim().length() > 0))
			V.addElement(s.trim());
		return V;
	}

	public static int lengthMinusColors(String thisStr)
	{
		int size = 0;
		for (int i = 0; i < thisStr.length(); i++)
		{
			if (thisStr.charAt(i) == '^')
			{
				i++;
				if ((i + 1) < thisStr.length())
				{
					final int tagStart = i;
					final char c = thisStr.charAt(i);
					if ((c == '<') || (c == '&'))
					{
						while (i < (thisStr.length() - 1))
						{
							if (((c == '<') && ((thisStr.charAt(i) != '^') || (thisStr.charAt(i + 1) != '>'))) || ((c == '&') && (thisStr.charAt(i) != ';')))
							{
								i++;
								if (i >= (thisStr.length() - 1))
								{
									i = tagStart + 1;
									break;
								}
							}
							else
							{
								i++;
								break;
							}
						}
					}
				}
			}
			else
				size++;
		}
		return size;
	}

	/**
	 * Convert an integer to its Roman Numeral equivalent
	 * 
	 * 
	 * 
	 * Usage: Return=MiscFunc.convertToRoman(Number)+".";
	 * 
	 * @param i
	 *            Integer to convert
	 * 
	 * @return String Converted integer
	 */
	public static String convertToRoman(int i)
	{
		String Roman = "";
		final String Hundreds[] = { "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM", "P" };
		final String Tens[] = { "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC", "C" };
		final String Ones[] = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
		if (i > 1000)
		{
			Roman = "Y";
			i = i % 1000;
		}
		if (i >= 100)
		{
			final int x = i % 100;
			final int y = (i - x) / 100;
			if (y > 0)
				Roman += Hundreds[y - 1];
			i = x;
		}
		if (i >= 10)
		{
			final int x = i % 10;
			final int y = (i - x) / 10;
			if (y > 0)
				Roman += Tens[y - 1];
		}
		i = i % 10;
		if (i > 0)
			Roman += Ones[i - 1];
		return Roman;
	}

	public static String padLeft(String thisStr, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr).substring(0, thisMuch);
		return SPACES.substring(0, thisMuch - lenMinusColors) + thisStr;
	}

	public static String padLeft(String thisStr, String colorPrefix, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return colorPrefix + removeColors(thisStr).substring(0, thisMuch);
		return SPACES.substring(0, thisMuch - lenMinusColors) + colorPrefix + thisStr;
	}

	public static String padRight(String thisStr, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr).substring(0, thisMuch);
		return thisStr + SPACES.substring(0, thisMuch - lenMinusColors);
	}

	public static String limit(String thisStr, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr).substring(0, thisMuch);
		return thisStr;
	}

	public static String padRight(String thisStr, String colorSuffix, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr).substring(0, thisMuch) + colorSuffix;
		return thisStr + colorSuffix + SPACES.substring(0, thisMuch - lenMinusColors);
	}

	public static String padRightPreserve(String thisStr, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr);
		return thisStr + SPACES.substring(0, thisMuch - lenMinusColors);
	}

	public static String centerPreserve(String thisStr, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr);
		final int left = (thisMuch - lenMinusColors) / 2;
		final int right = ((left + left + lenMinusColors) < thisMuch) ? left + 1 : left;
		return SPACES.substring(0, left) + thisStr + SPACES.substring(0, right);
	}

	public static String padLeftPreserve(String thisStr, int thisMuch)
	{
		final int lenMinusColors = lengthMinusColors(thisStr);
		if (lenMinusColors > thisMuch)
			return removeColors(thisStr);
		return SPACES.substring(0, thisMuch - lenMinusColors) + thisStr;
	}

	public static boolean isNumber(String s)
	{
		if (s == null)
			return false;
		s = s.trim();
		if (s.length() == 0)
			return false;
		if ((s.length() > 1) && (s.startsWith("-")))
			s = s.substring(1);
		for (int i = 0; i < s.length(); i++)
			if ("0123456789.,".indexOf(s.charAt(i)) < 0)
				return false;
		return true;
	}

	public static double div(double a, double b)
	{
		return a / b;
	}

	public static double div(double a, int b)
	{
		return a / b;
	}

	public static double div(int a, double b)
	{
		return (a) / b;
	}

	public static double div(double a, long b)
	{
		return a / b;
	}

	public static double div(long a, double b)
	{
		return (a) / b;
	}

	public static double mul(double a, double b)
	{
		return a * b;
	}

	public static double mul(double a, int b)
	{
		return a * (b);
	}

	public static double mul(int a, double b)
	{
		return (a) * b;
	}

	public static double mul(double a, long b)
	{
		return a * (b);
	}

	public static double mul(long a, double b)
	{
		return (a) * b;
	}

	public static long mul(long a, long b)
	{
		return a * b;
	}

	public static int mul(int a, int b)
	{
		return a * b;
	}

	public static double div(long a, long b)
	{
		return ((double) a) / ((double) b);
	}

	public static double div(int a, int b)
	{
		return ((double) a) / ((double) b);
	}

	public static int pow(int x, int y)
	{
		return (int) Math.round(Math.pow(x, y));
	}

	public static int squared(int x)
	{
		return (int) Math.round(Math.pow(x, x));
	}

	public static boolean bset(int num, int bitmask)
	{
		return ((num & bitmask) == bitmask);
	}

	public static boolean bset(long num, long bitmask)
	{
		return ((num & bitmask) == bitmask);
	}

	public static boolean bset(long num, int bitmask)
	{
		return ((num & bitmask) == bitmask);
	}

	public static int setb(int num, int bitmask)
	{
		return num | bitmask;
	}

	public static boolean banyset(int num, int bitmask)
	{
		return ((num & bitmask) > 0);
	}

	public static boolean banyset(long num, long bitmask)
	{
		return ((num & bitmask) > 0);
	}

	public static boolean banyset(long num, int bitmask)
	{
		return ((num & bitmask) > 0);
	}

	public static long setb(long num, int bitmask)
	{
		return num | bitmask;
	}

	public static long setb(long num, long bitmask)
	{
		return num | bitmask;
	}

	public static int unsetb(int num, int bitmask)
	{
		if (bset(num, bitmask))
			num -= bitmask;
		return num;
	}

	public static long unsetb(long num, long bitmask)
	{
		if (bset(num, bitmask))
			num -= bitmask;
		return num;
	}

	public static long unsetb(long num, int bitmask)
	{
		if (bset(num, bitmask))
			num -= bitmask;
		return num;
	}

	public static boolean isSet(int number, int bitnumber)
	{
		if ((number & (pow(2, bitnumber))) == (pow(2, bitnumber)))
			return true;
		return false;
	}

	public static boolean isSet(long number, int bitnumber)
	{
		if ((number & (pow(2, bitnumber))) == (pow(2, bitnumber)))
			return true;
		return false;
	}

	public static String sameCase(String str, char c)
	{
		if (Character.isUpperCase(c))
			return str.toUpperCase();
		return str.toLowerCase();
	}

	public static Vector<Object> denumerate(Enumeration<Object> e)
	{
		final Vector<Object> V = new Vector<Object>();
		for (; e.hasMoreElements();)
			V.addElement(e.nextElement());
		return V;
	}
}
