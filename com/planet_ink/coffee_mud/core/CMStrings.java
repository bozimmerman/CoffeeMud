package com.planet_ink.coffee_mud.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;

/*
   Mostly Copyright 2000-2025 Bo Zimmerman

   Functions for diff (C) 2006 Google
   Author: fraser@google.com (Neil Fraser)

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
 * A singleton of String utilities, string searchers, string builders,
 * comparers, and alterers.
 * Also includes my String comparison expression parser and evaluator.
 * Also includes an adaptation of google's string differencer.
 * @author Bo Zimmerman
 *
 */
public class CMStrings
{
	private CMStrings()
	{
		super();
	}

	/**
	 * A string array with 0 entries
	 */
	public final static String[] emptyStringArray=new String[0];

	private static CMStrings inst=new CMStrings();

	/**
	 * Returns a static instance of this singleton
	 * @return a static instance of this singleton
	 */
	public final static CMStrings instance()
	{
		return inst;
	}

	/**
	 * 1024 spaces
	 */
	public final static String SPACES=repeat(' ',1024);

	/**
	 * Builds a string consisting entirely of the given String,
	 * the given number of times in a row.
	 * @param str1 the String to repeat
	 * @param times the size of the string
	 * @return a string of those Strings
	 */
	public final static String repeat(final String str1, final int times)
	{
		if(times<=0)
			return "";
		final StringBuffer str=new StringBuffer("");
		for(int i=0;i<times;i++)
			str.append(str1);
		return str.toString();
	}

	/**
	 * Trims the given string, but only from the end (right side).
	 * Otherwise, acts just like String.trim()
	 * @param str the string to trim
	 * @return the right-trimmed string
	 */
	public final static String rtrim(final String str)
	{
		if(str == null)
			return null;
		if(str.length()==0)
			return str;
		int x=str.length();
		while((x>0) && Character.isWhitespace(str.charAt(x-1)))
			x--;
		return str.substring(0,x);
	}

	/**
	 * Trims the given string, but only from the front (left side).
	 * Otherwise, acts just like String.trim()
	 * @param str the string to trim
	 * @return the left-trimmed string
	 */
	public final static String ltrim(final String str)
	{
		if(str == null)
			return null;
		if(str.length()==0)
			return str;
		int x=-1;
		while((x<str.length()) && Character.isWhitespace(str.charAt(x+1)))
			x++;
		return str.substring(x);
	}

	/**
	 * Splits a string into single spaceless word lengths where each
	 * entry starts with a capital letter of the alphabet.
	 *
	 * @param str the string to split
	 * @return the capitalized word-lengths, minus spaces
	 */
	public final static String[] splitByCapitals(final String str)
	{
		if((str == null)||(str.length()==0))
			return new String[0];
		final List<String> caps = new ArrayList<String>();
		final StringBuilder word = new StringBuilder(str.charAt(0));
		for(int i=1;i<str.length();i++)
		{
			final char c = str.charAt(i);
			if(Character.isLetter(c) && Character.isUpperCase(c))
			{
				if(word.length()>0)
					caps.add(word.toString());
				word.setLength(0);
			}
			if(!Character.isWhitespace(c))
				word.append(c);
		}
		if(word.length()>0)
			caps.add(word.toString());
		return caps.toArray(new String[caps.size()]);
	}

	/**
	 * Builds a string consisting entirely of the given character,
	 * the given number of times in a row.
	 * @param chr1 the character to repeat
	 * @param times the size of the string
	 * @return a string of those characters
	 */
	public final static String repeat(final char chr1, final int times)
	{
		if(times<=0)
			return "";
		final byte[] buf=new byte[times];
		if(Character.charCount(chr1)>1)
			return repeat(Character.toString(chr1), times);
		Arrays.fill(buf, (byte)chr1);
		return new String(buf);
	}

	/**
	 * Builds a string consisting entirely of the given character,
	 * the given number of times in a row.  If the number of times
	 * is higher than a given limit, then an "x" and a number of extra
	 * times over the limit is added.
	 * @param chr1 the character to repeat
	 * @param times the size of the string
	 * @param limit the maximum number of repeats before xNN
	 * @return a string of those characters
	 */
	public final static String repeatWithLimit(final char chr1, final int times, final int limit)
	{
		if(times<=0)
			return "";
		if(times > limit)
			return repeat(chr1,limit)+"x"+(times-limit);
		final byte[] buf=new byte[times];
		if(Character.charCount(chr1)>1)
			return repeat(Character.toString(chr1), times);
		Arrays.fill(buf, (byte)chr1);
		return new String(buf);
	}

	/**
	 * Returns true if the given string is in uppercase, meaning it
	 * has no lowercase characters.
	 * @param str the string to check
	 * @return true if the string is in uppercase, false otherwise
	 */
	public final static boolean isUpperCase(final String str)
	{
		if(str==null)
			return false;
		for(int c=0;c<str.length();c++)
		{
			final char ch=str.charAt(c);
			if((!Character.isUpperCase(ch))
			&&(ch!='_')
			&&(ch!=' ')
			&&(!Character.isDigit(ch)))
				return false;
		}
		return true;
	}

	/**
	 * Returns true if the given string is in lowercase, meaning it
	 * has no uppercase characters.
	 * @param str the string to check
	 * @return true if the string is in lowercase, false otherwise
	 */
	public final static boolean isLowerCase(final String str)
	{
		if(str==null)
			return false;
		for(int c=0;c<str.length();c++)
		{
			final char ch=str.charAt(c);
			if((!Character.isLowerCase(ch))
			&&(ch!='_')
			&&(ch!=' ')
			&&(!Character.isDigit(ch)))
				return false;
		}
		return true;
	}

	/**
	 * Returns the given string in uppercase, or "" if the string
	 * was null.
	 * @param str the string, or null
	 * @return the string in uppercase, or "" if null
	 */
	public final static String s_uppercase(final String str)
	{
		if(str==null)
			return "";
		return str.toUpperCase();
	}

	/**
	 * Returns the given string in lowercase, or "" if the string
	 * was null.
	 * @param str the string, or null
	 * @return the string in lowercase, or "" if null
	 */
	public final static String s_lowercase(final String str)
	{
		if(str==null)
			return "";
		return str.toLowerCase();
	}

	/**
	 * Returns the number of letters in the second word that are
	 * also found in the first word.
	 * @param word the word to look in
	 * @param str the word the count is based on
	 * @return the final count
	 */
	public static final int sameLetterCount(final String word, final String str)
	{
		if((word==null)||(str==null))
			return 0;
		if((word.length()==0)||(str.length()==0))
			return 0;
		int i=0;
		for(final char c : str.toCharArray())
		{
			if(word.indexOf(c)>=0)
				i++;
		}
		return i;
	}

	/**
	 * Puts a period at the end of the last viewable character in this string,
	 * assuming there isn't already punctuation at the end.  Preserves any
	 * trailing special color codes.
	 * @param str the string to end with a period.
	 * @return the string, with a period at the end.
	 */
	public final static String endWithAPeriod(final String str)
	{
		if((str==null)||(str.length()==0))
			return str;
		int x=str.length()-1;
		while((x>=0)
		&&((Character.isWhitespace(str.charAt(x))) // possible #~ color concerns, but normally catches ^? at the end.
			||((x>0)&&((str.charAt(x)!='^')&&(str.charAt(x-1)=='^')&&((--x)>=0)))))
		{
			x--;
		}
		if(x<0)
			return str;
		if((str.charAt(x)=='.')||(str.charAt(x)=='!')||(str.charAt(x)=='?'))
			return str.trim()+" ";
		return str.substring(0,x+1)+". "+str.substring(x+1).trim();
	}

	/**
	 * Converts the given object to a string by the following method:
	 * 1. If it's a string, returns the string
	 * 2. If it's a byte array, returns it as a string decoded using
	 *    the current threads CHARSETINPUT from the system properties
	 * 3. If it's non-null, it calls toString()
	 * 4. Returns ""
	 * @param b the object to inspect
	 * @return the string
	 */
	public final static String bytesToStr(final Object b)
	{
		if(b instanceof String)
			return (String)b;
		else
		if(b instanceof byte[])
			return bytesToStr((byte[])b);
		else
		if(b!=null)
			return b.toString();
		return "";
	}

	/**
	 * Finds any commas in the string and ensures there is at least one space
	 * immediately after them.
	 * @param str the string to check
	 * @return the fixed string.
	 */
	public final static String addCommaSpacing(final String str)
	{
		if(str==null)
			return null;
		if((str.length()==0) || (str.indexOf(',')<0))
			return str;
		final StringBuilder s=new StringBuilder("");
		for(int i=0;i<str.length();i++)
		{
			if((str.charAt(i)==',')
			&&((i==str.length()-1)||(str.charAt(i+1)!=' ')))
				s.append(", ");
			else
				s.append(str.charAt(i));
		}
		return s.toString();
	}

	/**
	 * Converts the given byte array back into a string using the current
	 * threads CHARSETINPUT string encoding from the system properties.
	 * @param b the byte array to decode
	 * @return the string representation of the byte array
	 */
	public final static String bytesToStr(final byte[] b)
	{
		if(b==null)
			return "";
		try
		{
			return new String(b,CMProps.getVar(CMProps.Str.CHARSETINPUT));
		}
		catch(final Exception e)
		{
			return new String(b);
		}
	}

	/**
	 * Converts the given string to bytes using the current threads CHARSETINPUT
	 * string encoding from the system properties.
	 * @param str the string to encode
	 * @return the string, encoded into bytes from the
	 */
	public final static byte[] strToBytes(final String str)
	{
		try
		{
			return str.getBytes(CMProps.getVar(CMProps.Str.CHARSETINPUT));
		}
		catch(final Exception e)
		{
			return str.getBytes();
		}
	}

	/**
	 * Returns true if the given character is a vowel AEIOU, or false otherwise.
	 * This check is case-insensitive
	 * @param c the character to look at
	 * @return true if the character is a vowel, false otherwise
	 */
	public final static boolean isVowel(final char c)
	{
		return (("aeiouAEIOU").indexOf(c)>=0);
	}

	public final static String getFirstWord(final String s)
	{
		if(s==null)
			return "";
		return s.substring(0,indexOfEndOfWord(s,0));
	}


	/**
	 * Returns the next index in the given string of an end-of-word character,
	 * such as space,.;? or !.  It returns that index.
	 * @param s the string to look inside of
	 * @param startWith the starting index for the search
	 * @return the index of the end-of-word char, or string length for no more
	 */
	public final static int indexOfEndOfWord(final String s, final int startWith)
	{
		if((s==null)||(startWith>=s.length())||(startWith<0))
			return -1;
		for(int x=startWith;x<s.length();x++)
		{
			switch(s.charAt(x))
			{
			case ' ':
			case '.':
			case ',':
			case ';':
			case '?':
			case '!':
				return x;
			default:
				break;
			}
		}
		return s.length();
	}

	/**
	 * Returns the character index of the last vowel in this string
	 * @param s the string to look in
	 * @return the index of the last vowel
	 */
	public final static int indexOfLastVowel(final String s)
	{
		if(s==null)
			return -1;
		for(int i=s.length()-1;i>=0;i--)
		{
			if(isVowel(s.charAt(i)))
				return i;
		}
		return -1;
	}

	/**
	 * Attempts to make the given string only as long as the given length
	 * by first removing all spaces, and then removing all vowels, and
	 * finally just truncating it at the end.  No special accomodations
	 * are made about these strings -- they are assumed to be uncoded ascii.
	 * @param s the string to scrunch
	 * @param len the maximum length of the string
	 * @return the scrunches string, or the whole string, if &lt;= len
	 */

	public final static String scrunchWord(String s, final int len)
	{
		if((s.length()<=len)||(len<0))
			return s;
		s=s.trim();
		int x=s.lastIndexOf(' ');
		while((s.length()>len)&&(x>0))
		{
			s=s.substring(0,x)+s.substring(x+1);
			x=s.lastIndexOf(' ');
		}
		x=indexOfLastVowel(s);
		while((s.length()>len)&&(x>0))
		{
			s=s.substring(0,x)+s.substring(x+1);
			x=indexOfLastVowel(s);
		}
		if(s.length()>len)
			return s.substring(0,len);
		return s;
	}

	/**
	 * Returns whether the given string contains any of the given strings in the
	 * array, and if so, which one.  Case sensitive.
	 *
	 * @param text the text to search
	 * @param any the list of things to search for in the text
	 * @return index into the any array that was found.
	 */
	public final static int indexOfAny(final String text, final String[] any)
	{
		if((text==null)||(any==null)||(any.length==0))
			return -1;
		for(int a=0;a<any.length;a++)
			if(text.indexOf(any[a])>=0)
				return a;
		return -1;
	}

	/**
	 * Returns the value of any digits at the end of the given string.
	 * If no digits are found, returns -1.
	 * @param s the string to look for digits at the end of
	 * @return the value of any trailing digits, or -1
	 */
	public final static int finalDigits(final String s)
	{
		if((s==null)||(s.length()==0))
			return -1;
		int x=s.length();
		while((x>0)&&(Character.isDigit(s.charAt(x-1))))
			x--;
		if(x>=s.length())
			return -1;
		if(x==0)
			return Integer.parseInt(s);
		else
			return Integer.parseInt(s.substring(x));
	}

	/**
	 * This strange method parses the given string for one of the characters in the given
	 * array.  Whenever one of the characters is encountered, a Map Entry where the splitter
	 * is the key and the preceding characters are the split string is added to a final array
	 * of map entries.
	 * @param str the string to parse
	 * @param splitters the delimiters, and also the keys to the map.entry
	 * @return an array of all key/delimiters and the preceiding characters.
	 */
	@SuppressWarnings("unchecked")
	public final static Map.Entry<Character,String>[] splitMulti(final String str, final char[] splitters)
	{
		final List<Map.Entry<Character,String>> list=new ArrayList<Map.Entry<Character,String>>();
		char curC='\0';
		final StringBuilder curStr=new StringBuilder("");
		for(int i=0;i<str.length();i++)
		{
			if(contains(splitters,str.charAt(i)))
			{
				final Character finalC=Character.valueOf(curC);
				final String finalStr=curStr.toString();
				list.add(new Map.Entry<Character,String>()
				{
					@Override
					public Character getKey()
					{
						return finalC;
					}

					@Override
					public String getValue()
					{
						return finalStr;
					}

					@Override
					public String setValue(final String value)
					{
						return finalStr;
					}
				});
				curStr.setLength(0);
				curC=str.charAt(i);
			}
			else
				curStr.append(str.charAt(i));
		}
		final Character finalC=Character.valueOf(curC);
		final String finalStr=curStr.toString();
		list.add(new Map.Entry<Character,String>()
		{
			@Override
			public Character getKey()
			{
				return finalC;
			}

			@Override
			public String getValue()
			{
				return finalStr;
			}

			@Override
			public String setValue(final String value)
			{
				return finalStr;
			}
		});
		return list.toArray(new Map.Entry[0]);
	}

	/**
	 * Returns whether the given string contains the second string, without any following
	 * letter, which is the CMStrings definition of a "word".  This check is case
	 * in-sensitive.
	 * @param thisStr the string to look in
	 * @param word the string/word to look for
	 * @return true if the word is in the string, and false otherwise
	 */
	public final static boolean containsWordIgnoreCase(final String thisStr, final String word)
	{
		if((thisStr==null)
		||(word==null)
		||(thisStr.length()==0)
		||(word.length()==0))
			return false;
		return containsWord(thisStr.toLowerCase(),word.toLowerCase());
	}

	/**
	 * Returns whether the first string starts with the second string, case insensitive.
	 *
	 * @param thisStr the string to search
	 * @param startStr the substring to search for
	 * @return true if its true
	 */
	public final static boolean startsWithIgnoreCase(final String thisStr, final String startStr)
	{
		if((thisStr == null)
		||(startStr==null)
		||(thisStr.length()<startStr.length()))
			return false;
		if(startStr.length()==0)
			return true;
		return thisStr.substring(0,startStr.length()).toLowerCase().startsWith(startStr.toLowerCase());
	}

	/**
	 * Returns whether the first string ends with the second string, case insensitive.
	 *
	 * @param thisStr the string to search
	 * @param startStr the substring to search for
	 * @return true if its true
	 */
	public final static boolean endsWithIgnoreCase(final String thisStr, final String startStr)
	{
		if((thisStr == null)
		||(startStr==null)
		||(thisStr.length()<startStr.length()))
			return false;
		if(startStr.length()==0)
			return true;
		return thisStr.substring(0,startStr.length()).toLowerCase().endsWith(startStr.toLowerCase());
	}

	/**
	 * Returns whether the given string contains the second string, without any following
	 * letter, which is the CMStrings definition of a "word".  This check is case
	 * sensitive.  It returns the index of the word
	 * @param thisStr the string to look in
	 * @param word the string/word to look for
	 * @return -1, or the index of the found word in the string
	 */
	public final static int indexOfWord(final String thisStr, final String word)
	{
		if((thisStr==null)
		||(word==null)
		||(thisStr.length()==0)
		||(word.length()==0))
			return -1;
		for(int i=thisStr.length()-1;i>=0;i--)
		{
			if((thisStr.charAt(i)==word.charAt(0))
			&&((i==0)||(!Character.isLetter(thisStr.charAt(i-1)))))
			{
				if((thisStr.substring(i).startsWith(word))
				&&((thisStr.length()==i+word.length())||(!Character.isLetter(thisStr.charAt(i+word.length())))))
					return i;
			}
		}
		return -1;
	}

	/**
	 * Returns whether the given string contains the second string, without any following
	 * letter, which is the CMStrings definition of a "word".  This check is case
	 * sensitive.
	 * @param thisStr the string to look in
	 * @param word the string/word to look for
	 * @return true if the word is in the string, and false otherwise
	 */
	public final static boolean containsWord(final String thisStr, final String word)
	{
		return indexOfWord(thisStr,word) >=0;
	}

	/**
	 * Rebuilds the given string by replacing any instances of any of the characters
	 * in the given array with the given character.  The search is case-sensitive.
	 * @param str the string to rebuild without those characters
	 * @param theseChars the characters to remove from the string
	 * @param with the character to replace all the array characters with.
	 * @return the rebuilt string, with all those characters replaced
	 */
	public final static String replaceAllofAny(final String str, final char[] theseChars, final char with)
	{
		if((str==null)
		||(theseChars==null)
		||(str.length()==0)
		||(!containsAny(str,theseChars)))
			return str;
		if(with=='\0')
		{
			final StringBuilder s=new StringBuilder(str);
			for(int i=str.length()-1;i>=0;i--)
			{
				if(contains(theseChars,str.charAt(i)))
					s.deleteCharAt(i);
			}
			return s.toString();
		}
		else
		{
			final char[] newChars = str.toCharArray();
			for(int i=str.length()-1;i>=0;i--)
			{
				if(contains(theseChars,str.charAt(i)))
					newChars[i]=with;
			}
			return new String(newChars);
		}
	}

	/**
	 * Rebuilds the given string by replacing any instances of any of the characters
	 * in the given array with the given character.  The search is case-sensitive.
	 * @param str the string to rebuild without those characters
	 * @param theseChars the characters to remove from the string
	 * @param withThese the character to replace all the array characters with.
	 * @return the rebuilt string, with all those characters replaced
	 */
	public final static String replaceAllofAny(final String str, final char[] theseChars, final char[] withThese)
	{
		if((str==null)
		||(theseChars==null)
		||(str.length()==0)
		||(!containsAny(str,theseChars)))
			return str;
		final char[] newChars = str.toCharArray();
		for(int i=str.length()-1;i>=0;i--)
		{
			final int x=indexOf(theseChars,str.charAt(i));
			if((x>=0)&&(x<withThese.length))
			{
				newChars[i]=withThese[x];
			}
		}
		return new String(newChars);
	}

	/**
	 * Rebuilds the given string by deleting any instances of any of the characters
	 * in the given array.  The search is case-sensitive.
	 * @param str the string to rebuild without those characters
	 * @param theseChars the characters to remove from the string
	 * @return the rebuilt string, without the characters
	 */
	public final static String deleteAllofAny(final String str, final char[] theseChars)
	{
		if((str==null)
		||(theseChars==null)
		||(str.length()==0)
		||(!containsAny(str,theseChars)))
			return str;
		final StringBuilder buf=new StringBuilder(str);
		for(int i=buf.length()-1;i>=0;i--)
		{
			if(contains(theseChars,buf.charAt(i)))
			{
				buf.deleteCharAt(i);
			}
		}
		return buf.toString();
	}

	/**
	 * Rebuilds the given string by deleting any instances of \r, \n, \t, and multi-spaces
	 * in the given array.  The search is case-sensitive.
	 * @param str the string to rebuild without those characters
	 * @return the rebuilt string, without the characters
	 */
	public final static String deleteCRLFTAB(final String str)
	{
		if((str==null)
		||(str.length()==0))
			return str;
		final StringBuilder buf=new StringBuilder(str);
		boolean spacer=false;
		for(int i=buf.length()-1;i>=0;i--)
		{
			switch(buf.charAt(i))
			{
			case '\r':
			case '\n':
			case '\t':
			{
				if((i==0)
				||(i==buf.length()-1)
				||(buf.charAt(i-1)==' ')
				||(buf.charAt(i+1)==' '))
					buf.deleteCharAt(i);
				else
					buf.setCharAt(i, ' ');
				break;
			}
			case ' ':
				if(spacer)
					buf.deleteCharAt(i);
				else
					spacer=true;
				break;
			default:
				spacer=false;
				break;
			}
		}
		return buf.toString();
	}

	/**
	 * Rebuilds the given string by deleting any instances of a given character
	 * The search is case-sensitive.
	 * @param str the string to rebuild without those characters
	 * @param thisChar the character to remove from the string
	 * @return the rebuilt string, without the character
	 */
	public final static String deleteAllofChar(final String str, final char thisChar)
	{
		if((str==null)
		||(str.length()==0)
		||(str.indexOf(thisChar)<0))
			return str;
		final StringBuilder buf=new StringBuilder(str);
		for(int i=buf.length()-1;i>=0;i--)
		{
			if(buf.charAt(i)==thisChar)
			{
				buf.deleteCharAt(i);
			}
		}
		return buf.toString();
	}

	/**
	 * Finds all instances of the second parameter string in the first string,
	 * replaces them with the third word.  Returns the string with or without changes.
	 * The search is case sensitive
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, where found.
	 * @return the string modified, or not modified if no replacements were made.
	 */
	public final static String replaceAll(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0)
		||(thisStr.equals(withThisStr)))
		{
			return str;
		}
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)==thisStr.charAt(0))
			{
				if(str.substring(i).startsWith(thisStr))
				{
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
				}
			}
		}
		return str;
	}

	/**
	 * Finds all instances of the second parameter string in the first string, ignoring case,
	 * replaces them with the third word.  Returns the string with or without changes.
	 * The search is case insensitive
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, where found.
	 * @return the string modified, or not modified if no replacements were made.
	 */
	public final static String replaceAllIgnoreCase(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0)
		||(thisStr.equalsIgnoreCase(withThisStr)))
		{
			return str;
		}
		final String lthisStr = thisStr.toLowerCase();
		final String lstr = str.toLowerCase();
		for(int i=str.length()-1;i>=0;i--)
		{
			if(lstr.charAt(i)==lthisStr.charAt(0))
			{
				if(lstr.substring(i).startsWith(lthisStr))
				{
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
				}
			}
		}
		return str;
	}

	/**
	 * Flattens a string by converting all carriage returns and linefeeds into spaces.
	 * @param str the string to flatten
	 * @return the flat string
	 */
	public final static String flatten(String str)
	{
		if(str==null)
			return null;
		str=replaceAll(str,"\n\r"," ");
		str=replaceAll(str,"\r\n"," ");
		return str.replace('\r', ' ').replace('\n', ' ');
	}

	/**
	 * This methods replaces any double-escapes to single escape characters, and any
	 * escaped double-quotes to double-quotes
	 * @param str the string to de-escape
	 * @return the string, de-escaped
	 */
	public final static String deEscape(final String str)
	{
		return deEscape(str,'\"');
	}

	/**
	 * This methods replaces any double-escapes to single escape characters, and any
	 * escaped double-chars to double-chars
	 * @param str the string to de-escape
	 * @param quot the char to de-escape
	 * @return the string, de-escaped
	 */
	public final static String deEscape(String str, final char quot)
	{
		if(str==null)
			return str;
		int i = str.indexOf('\\');
		while((i>-1)&&(i<str.length()-1))
		{
			final char c = str.charAt(i+1);
			if((quot == c)||(c=='\\'))
				str=str.substring(0,i)+str.substring(i+1);
			i = str.indexOf('\\',i+1);
		}
		return str;
	}

	/**
	 * This methods replaces any of the given escaped chars to being de-escaped.
	 * @param str the string to de-escape
	 * @param chars the chars to de-escape
	 * @return the string, de-escaped
	 */
	public final static String deEscape(String str, final String chars)
	{
		if(str==null)
			return str;
		int i = str.indexOf('\\');
		while((i>-1)&&(i<str.length()-1))
		{
			if(chars.indexOf(str.charAt(i+1))>=0)
				str=str.substring(0,i)+str.substring(i+1);
			i = str.indexOf('\\',i+1);
		}
		return str;
	}

	/**
	 * This methods replaces any escapes to double-escape characters, and any
	 *  double-quotes to escaped double-quotes
	 * @param str the string to escape
	 * @return the string, escaped
	 */
	public final static String escape(final String str)
	{
		if(str==null)
			return str;
		return replaceAll(replaceAll(str,"\\","\\\\"),"\"","\\\"");
	}

	/**
	 * Finds all instances of the second parameter string in the first StringBuffer,
	 * replaces them with the third word.  Returns the StringBuffer with or without changes.
	 * The search is case sensitive
	 * @param str the StringBuffer to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, where found.
	 * @return the StringBuffer modified, or not modified if no replacements were made.
	 */
	public final static StringBuffer replaceAll(final StringBuffer str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
		{
			return str;
		}
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)==thisStr.charAt(0))
			{
				if(str.substring(i).startsWith(thisStr))
				{
					str.delete(i, i+thisStr.length());
					str.insert(i, withThisStr);
				}
			}
		}
		return str;
	}

	/**
	 * Finds all instances of the second parameter string in each of the strings in the array,
	 * replaces them with the third word.  Returns the string array with or without changes.
	 * The search is case sensitive
	 * @param strs the string array to look inside of
	 * @param thisStr the string to look for inside the array strings
	 * @param withThisStr the string to replace the second string with, where found.
	 * @return the string array modified, or not modified if no replacements were made.
	 */
	public final static String[] replaceInAll(final String[] strs, final String thisStr, final String withThisStr)
	{
		if((strs==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(strs.length==0)
		||(thisStr.length()==0))
		{
			return strs;
		}
		for(int s=0;s<strs.length;s++)
		{
			final String str=strs[s];
			for(int i=str.length()-1;i>=0;i--)
			{
				if(str.charAt(i)==thisStr.charAt(0))
				{
					if(str.substring(i).startsWith(thisStr))
					{
						strs[s]=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
					}
				}
			}
		}
		return strs;
	}

	/**
	 * Builds a new version of the given string by replacing all instances of the first string
	 * in each pair with the second string.  The pairs should be allocated String[N][2] where
	 * N is any number of pairs, and 0 index string is the one to replace with the string in the
	 * 1 index.  This method is case-sensitive!
	 * @param str the string to rebuild
	 * @param pairs the string pairs for search/replace duties
	 * @return the rebuilt string
	 */
	public final static String replaceAlls(final String str, final String pairs[][])
	{
		if((str==null)
		||(pairs==null)
		||(str.length()==0)
		||(pairs.length==0))
			return str;
		final StringBuilder newStr=new StringBuilder("");
		boolean changed=false;
		for(int i=0;i<str.length();i++)
		{
			changed=false;
			for (final String[] pair : pairs)
			{
				if((str.charAt(i)==pair[0].charAt(0))
				&&(str.substring(i).startsWith(pair[0])))
				{
					newStr.append(pair[1]);
					changed=true;
					i=i+pair[0].length()-1;
					break;
				}
			}
			if(!changed)
				newStr.append(str.charAt(i));
		}
		return newStr.toString();
	}

	/**
	 * Finds all instances of the second parameter word in the first string,
	 * and replaces it with the third word.  Returns the first string with or without changes. The
	 * string is only altered where the second string appears as a inside the first string with no
	 * trailing letters to alter the word.  Preceding letters appear to be ok.  The search is case
	 * insensitive, but the replacement will be made intelligently to preserve the case of the word(s)
	 * being replaced.
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, where found.
	 * @return the string modified, or not modified if no replacements were made.
	 */
	public final static String replaceWord(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		final String uppercaseWithThisStr=withThisStr.toUpperCase();
		final char firstLetter = Character.toLowerCase(thisStr.charAt(0));
		for(int i=str.length()-1;i>=0;i--)
		{
			if((Character.toLowerCase(str.charAt(i))==firstLetter)
			&&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
			{
				if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
				&&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
				{
					final String oldWord=str.substring(i,i+thisStr.length());
					if(oldWord.toUpperCase().equals(oldWord))
						str=str.substring(0,i)+uppercaseWithThisStr+str.substring(i+thisStr.length());
					else
					if(oldWord.toLowerCase().equals(oldWord))
						str=str.substring(0,i)+uppercaseWithThisStr.toLowerCase()+str.substring(i+thisStr.length());
					else
					if((oldWord.length()>0)&&(Character.isUpperCase(oldWord.charAt(0))))
						str=str.substring(0,i)+uppercaseWithThisStr.charAt(0)+uppercaseWithThisStr.substring(1).toLowerCase()+str.substring(i+thisStr.length());
					else
						str=str.substring(0,i)+uppercaseWithThisStr.toLowerCase()+str.substring(i+thisStr.length());
				}
			}
		}
		return str;
	}

	/**
	 * Finds all instances of the second parameter string in the first string,
	 * and replaces it with the third String.  Returns the first string with or without changes. The
	 * string is only altered where the second string appears as a inside the first string with no
	 * trailing letters to alter the word.  Preceding letters are also not OK.
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, where found.
	 * @return the string modified, or not modified if no replacements were made.
	 */
	public final static String replaceWhole(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=str.length()-1;i>=0;i--)
		{
			if((str.charAt(i)==thisStr.charAt(0))
			&&((i==0)||(!Character.isLetterOrDigit(str.charAt(i-1)))))
			{
				if((str.substring(i).startsWith(thisStr))
				&&((str.length()==i+thisStr.length())||(!Character.isLetterOrDigit(str.charAt(i+thisStr.length())))))
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
			}
		}
		return str;
	}

	/**
	 * Finds the first and only the first instance of the second parameter word in the first string,
	 * and replaces it with the third word.  Returns the first string with or without changes. The
	 * string is only altered if the second string appears as a inside the first string with no
	 * trailing letters to alter the word.  Preceding letters appear to be ok.  The search is case
	 * insensitive, but the replacement will be made intelligently to preserve the case of the word
	 * being replaced.
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, if found.
	 * @return the string modified, or not modified if no replacement was made.
	 */
	public final static String replaceFirstWord(final String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		final String uppercaseWithThisStr=withThisStr.toUpperCase();
		for(int i=str.length()-1;i>=0;i--)
		{
			if((str.charAt(i)==thisStr.charAt(0))
			&&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
			{
				if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
				&&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
				{
					final String oldWord=str.substring(i,i+thisStr.length());
					if(oldWord.toUpperCase().equals(oldWord))
						return str.substring(0,i)+uppercaseWithThisStr+str.substring(i+thisStr.length());
					else
					if(oldWord.toLowerCase().equals(oldWord))
						return str.substring(0,i)+uppercaseWithThisStr.toLowerCase()+str.substring(i+thisStr.length());
					else
					if((oldWord.length()>0)&&(Character.isUpperCase(oldWord.charAt(0))))
						return str.substring(0,i)+uppercaseWithThisStr.charAt(0)+uppercaseWithThisStr.substring(1).toLowerCase()+str.substring(i+thisStr.length());
					else
						return str.substring(0,i)+uppercaseWithThisStr.toLowerCase()+str.substring(i+thisStr.length());
				}
			}
		}
		return str;
	}

	/**
	 * Finds the first and only the first instance of the second parameter string in the first string,
	 * and replaces it with the third string.  Returns the first string with or without changes.
	 * This method is case sensitive.
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, if found.
	 * @return the string modified, or not modified if no replacement was made.
	 */
	public final static String replaceFirst(final String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)==thisStr.charAt(0))
			{
				if(str.substring(i).startsWith(thisStr))
				{
					return str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
				}
			}
		}
		return str;
	}

	/**
	 * Returns the number of identifiable words in the string
	 * @param str the string to count the words in
	 * @return the number of words
	 */
	public final static int numWords(String str)
	{
		if(str==null)
			return 0;
		str=str.trim();
		if(str.length()==0)
			return 0;
		boolean flag=false;
		int ct=0;
		for(int i=0;i<str.length();i++)
		{
			final char c=str.charAt(i);
			if(Character.isLetter(c))
			{
				if(!flag)
				{
					flag=true;
					ct++;
				}
			}
			else
			if(Character.isWhitespace(c) && (flag))
				flag=false;
		}
		return ct;
	}

	/**
	 * Capitalizes the first letter in the given string, and forcibly lowercases
	 * the remaining letters in the string.
	 * This method respects special CoffeeMUD color codes, skipping over
	 * them to find that elusive first letter.
	 * @param name the string to capitalize and lowercase
	 * @return the string, capitalized, with other letters lowercased
	 */
	public final static String capitalizeAndLower(final String name)
	{
		if((name==null)||(name.length()==0))
			return "";
		final char[] c=name.toCharArray();
		int i=0;
		for(;i<c.length;i++)
		{
			if(c[i]=='^')
			{
				i++;
				if(i<c.length)
				{
					switch(c[i])
					{
					case ColorLibrary.COLORCODE_FANSI256:
						if((i<c.length-8)&&(c[i+1]==c[i]))
						{
							if(!CMath.isHexNumber(name.substring(i+2,i+8)))
								i += 3;
							else
								i += 7;
						}
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BANSI256:
						if((i<c.length-1)&&(c[i+1]==c[i]))
							i += 7;
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BACKGROUND:
						i++;
						break;
					case '<':
						while(i<c.length-1)
						{
							if((c[i]!='^')||(c[i+1]!='>'))
								i++;
							else
							{
								i++;
								break;
							}
						}
						break;
					case '&':
						while(i<c.length)
						{
							if(c[i]!=';')
								i++;
							else
								break;
						}
						break;
					}
				}
			}
			else
			if(Character.isLetter(c[i]))
				break;
		}
		if(i<c.length)
			c[i]=Character.toUpperCase(c[i]);
		i++;
		for(;i<c.length;i++)
		{
			if(!Character.isLowerCase(c[i]))
				c[i]=Character.toLowerCase(c[i]);
		}
		return new String(c).trim();
	}

	/**
	 * Capitalizes the first letter in every word in the given string, and lowercases any
	 * other letters in each word.
	 * This method respects special CoffeeMUD color codes, skipping over
	 * them to find that elusive first letters.
	 * @param name the string with words to capitalize and lowercase.
	 * @return the string, with all words capitalized and remaining word letters lowercased.
	 */
	public final static String capitalizeAllFirstLettersAndLower(final String name)
	{
		if((name==null)||(name.length()==0))
			return "";
		final char[] c=name.toCharArray();
		int i=0;
		boolean firstLetter=true;
		for(;i<c.length;i++)
		{
			if(c[i]=='^')
			{
				i++;
				if(i<c.length)
				{
					switch(c[i])
					{
					case ColorLibrary.COLORCODE_FANSI256:
						if((i<c.length-8)&&(c[i+1]==c[i]))
						{
							if(!CMath.isHexNumber(name.substring(i+2,i+8)))
								i += 3;
							else
								i += 7;
						}
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BANSI256:
						if((i<c.length-1)&&(c[i+1]==c[i]))
							i += 7;
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BACKGROUND:
						i++;
						break;
					case '<':
						while(i<c.length-1)
						{
							if((c[i]!='^')||(c[i+1]!='>'))
								i++;
							else
							{
								i++;
								break;
							}
						}
						break;
					case '&':
						while (i < c.length)
						{
							if (c[i] != ';')
								i++;
							else
								break;
						}
						break;
					}
				}
			}
			else
			if(Character.isLetter(c[i]))
			{
				if(firstLetter)
				{
					c[i]=Character.toUpperCase(c[i]);
					firstLetter=false;
				}
				else
				if(!Character.isLowerCase(c[i]))
					c[i]=Character.toLowerCase(c[i]);
			}
			else
			if(Character.isWhitespace(c[i])||(c[i]=='/'))
				firstLetter=true;
		}
		return new String(c).trim();
	}

	/**
	 * Capitalizes the first letter in the given string, if one is found.
	 * This method respects special CoffeeMUD color codes, skipping over
	 * them to find that elusive first letter.  This will also add a period
	 * at the end, if no other punctuation is found.
	 * @param name the string to capitalize and punctuationify
	 * @return the string, capitalized and punctuated
	 */
	public final static String capitalizeFirstLetterAndEndSentence(final String name)
	{
		final StringBuilder  c=new StringBuilder(name);
		int lastLetter = -1;
		int firstLetter = -1;
		int lastPunctuation = -1;
		int i=0;
		for(;i<c.length();i++)
		{
			if(c.charAt(i)=='^')
			{
				i++;
				if(i<c.length())
				{
					switch(c.charAt(i))
					{
					case ColorLibrary.COLORCODE_FANSI256:
						if((i<c.length()-8)&&(c.charAt(i+1)==c.charAt(i)))
						{
							if(!CMath.isHexNumber(name.substring(i+2,i+8)))
								i += 3;
							else
								i += 7;
						}
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BANSI256:
						if((i<c.length()-1)&&(c.charAt(i+1)==c.charAt(i)))
							i += 7;
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BACKGROUND:
						i++;
						break;
					case '<':
						while(i<c.length()-1)
						{
							if((c.charAt(i)!='^')||(c.charAt(i+1)!='>'))
								i++;
							else
							{
								i++;
								break;
							}
						}
						break;
					case '&':
						while(i<c.length())
						{
							if(c.charAt(i)!=';')
								i++;
							else
								break;
						}
						break;
					}
				}
			}
			else
			if(Character.isLetter(c.charAt(i))&&(firstLetter < 0))
				firstLetter = i;
			else
			if(Character.isLetterOrDigit(c.charAt(i)))
				lastLetter = i;
			else
			if("!?.*&$@)".indexOf(c.charAt(i))>=0)
				lastPunctuation = i;
		}
		if((firstLetter >= 0) && (firstLetter<c.length()))
			c.setCharAt(firstLetter, Character.toUpperCase(c.charAt(firstLetter)));
		if(lastLetter > 0)
		{
			if(lastLetter == c.length()-1)
				c.append(".");
			else
			if(lastPunctuation < lastLetter)
				c.insert(lastLetter+1, ".");
		}
		return c.toString().trim();
	}

	/**
	 * Capitalizes the first letter in the given string, if one is found.
	 * This method respects special CoffeeMUD color codes, skipping over
	 * them to find that elusive first letter.
	 * @param name the string to capitalize
	 * @return the string, capitalized
	 */
	public final static String capitalizeFirstLetter(final String name)
	{
		if((name==null)||(name.length()==0))
			return "";
		final char[] c=name.toCharArray();
		int i=0;
		for(;i<c.length;i++)
		{
			if(c[i]=='^')
			{
				i++;
				if(i<c.length)
				{
					switch(c[i])
					{
					case ColorLibrary.COLORCODE_FANSI256:
						if((i<c.length-8)&&(c[i+1]==c[i]))
						{
							if(!CMath.isHexNumber(name.substring(i+2,i+8)))
								i += 3;
							else
								i += 7;
						}
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BANSI256:
						if((i<c.length-1)&&(c[i+1]==c[i]))
							i += 7;
						else
							i += 3;
						break;
					case ColorLibrary.COLORCODE_BACKGROUND:
						i++;
						break;
					case '<':
						while(i<c.length-1)
						{
							if((c[i]!='^')||(c[i+1]!='>'))
								i++;
							else
							{
								i++;
								break;
							}
						}
						break;
					case '&':
						while(i<c.length)
						{
							if(c[i]!=';')
								i++;
							else
								break;
						}
						break;
					}
				}
			}
			else
			if(Character.isLetter(c[i]))
				break;
		}
		if(i<c.length)
			c[i]=Character.toUpperCase(c[i]);
		return new String(c).trim();
	}

	/**
	 * Returns all characters after the last space in the
	 * given string, or just returns the given string if no spaces
	 * were found.
	 * @param thisStr the string to look for spaces in
	 * @return the substring after the last space, or the whole string
	 */
	public final static String lastWordIn(final String thisStr)
	{
		final int x=thisStr.lastIndexOf(' ');
		if(x>=0)
			return thisStr.substring(x+1);
		return thisStr;
	}

	/**
	 * Returns the string between the first and last ' characters in the
	 * given string, or null if no such characters were found.
	 * @param msg the string to parse out from
	 * @return the string between the ' characters, or null
	 */
	public final static String getSayFromMessage(final String msg)
	{
		if(msg==null)
			return null;
		final int start=msg.indexOf('\'');
		final int end=msg.lastIndexOf('\'');
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
		return null;
	}

	/**
	 * Replaces the string between the first and last ' characters in the
	 * given string with the given string.
	 * @param msg the string to parse out from
	 * @param rep the string to replace
	 */
	public final static String replaceSayInMessage(final String msg, final String rep)
	{
		if(msg==null)
			return msg;
		final int start=msg.indexOf('\'');
		final int end=msg.lastIndexOf('\'');
		if((start>0)&&(end>start))
			return msg.substring(0,start+1)+rep+msg.substring(end);
		return msg;
	}

	/**
	 * This method replaces the string between the first and last ' characters in the
	 * first string with the second string.
	 * @param affmsg the string to replace a portion of
	 * @param msg the string to insert into the first string, between the ' chars
	 * @return the new string, or unchanged if there is nothing to do
	 */
	public final static String substituteSayInMessage(final String affmsg, final String msg)
	{
		if(affmsg==null)
			return null;
		final int start=affmsg.indexOf('\'');
		final int end=affmsg.lastIndexOf('\'');
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}

	/**
	 * Returns whether the given string array contains the given string.
	 * This check is case insensitive.
	 * @param strs the array to look in
	 * @param str the string to look for
	 * @return true if the array contained the string, and false otherwise
	 */
	public final static boolean containsIgnoreCase(final String[] strs, final String str)
	{
		if((str==null)||(strs==null))
			return false;
		for (final String str2 : strs)
		{
			if(str2.equalsIgnoreCase(str))
				return true;
		}
		return false;
	}

	/**
	 * Compares the two string arrays to see if they are the same, but
	 * in a case-insensitive way.
	 * @param A1 the first string array
	 * @param A2 the second string array
	 * @return true if the arrays are the same, and false otherwise.
	 */
	public final static boolean compareStringArraysIgnoreCase(final String[] A1, final String[] A2)
	{
		if(((A1==null)||(A1.length==0))
		&&((A2==null)||(A2.length==0)))
			return true;
		if((A1==null)||(A2==null))
			return false;
		if(A1.length!=A2.length)
			return false;
		for(int a1=0;a1<A1.length;a1++)
		{
			if(!A1[a1].equalsIgnoreCase(A2[a1]))
				return false;
		}
		return true;
	}

	/**
	 * Returns whether the given string array contains the given string.
	 * This check is Case Sensitive!
	 * @param strs the array to look in
	 * @param str the string to look for
	 * @return true if the array contained the string, and false otherwise
	 */
	public final static boolean contains(final String[] strs, final String str)
	{
		if((str==null)||(strs==null))
			return false;
		for (final String str2 : strs)
		{
			if(str2.equals(str))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the given character array contains the given character.
	 * This method is case-sensitive!
	 * @param anycs the character array to look in
	 * @param c the character to look for
	 * @return true if the array contained the character, false otherwise
	 */
	public final static boolean contains(final char[] anycs, final char c)
	{
		for(final char c1 : anycs)
		{
			if(c1==c)
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the given character array contains the given character and
	 * if so, returns the index in the array of the character
	 * This method is case-sensitive!
	 * @param anycs the character array to look in
	 * @param c the character to look for
	 * @return the index of the character in the array, or -1
	 */
	public final static int indexOf(final char[] anycs, final char c)
	{
		for(int i=0;i<anycs.length;i++)
		{
			if(anycs[i]==c)
				return i;
		}
		return -1;
	}

	/**
	 * Checks whether the given string contains any of the given characters.
	 * This method is case sensitive!
	 * @param str the string to check
	 * @param anycs the list of characters to look for
	 * @return true if any characters were found, false otherwise
	 */
	public final static boolean containsAny(final String str, final char[] anycs)
	{
		if((str==null)||(anycs==null))
			return false;
		for(int i=0;i<str.length();i++)
		{
			if(contains(anycs,str.charAt(i)))
				return true;
		}
		return false;
	}

	/**
	 * Returns whether the given string array contains a string that
	 * starts with the given string.
	 * This check is Case Sensitive!
	 * @param strs the array to look in
	 * @param str the string to look for
	 * @return true if the array contained the string beginning, and false otherwise
	 */
	public final static boolean containsStartsWith(final String[] strs, final String str)
	{
		if((str==null)||(strs==null))
			return false;
		for (final String str2 : strs)
		{
			if(str2.startsWith(str))
				return true;
		}
		return false;
	}

	/**
	 * Returns whether the given string array contains a string that
	 * start is the first part of the given string, such that:
	 * str.startsWith(strs[x])
	 * This check is Case Sensitive!
	 * @param strs the array to look in
	 * @param str the string to look for
	 * @return true if the array contained the string beginning, and false otherwise
	 */
	public final static boolean containsStarterWith(final String[] strs, final String str)
	{
		if((str==null)||(strs==null))
			return false;
		for (final String str2 : strs)
		{
			if(str.startsWith(str2))
				return true;
		}
		return false;
	}

	/**
	 * Replaces @x1 type variables inside a stringbuffer with an actual value
	 * Not used in the main expression system, this is a stand alone function
	 * Also uniquely, supports @x numbers above 10.  Values are *1* indexed!!
	 *
	 * @param str the stringbuffer to assess
	 * @param values values to replace each variable with
	 */
	public final static void replaceVariables(final StringBuffer str, final String values[])
	{
		replaceVariables(str, values, 0);
	}

	/**
	 * Replaces @x1 type variables inside a stringbuffer with an actual value
	 * Not used in the main expression system, this is a stand alone function
	 * Also uniquely, supports @x numbers above 10.  Values are *1* indexed!!
	 *
	 * @param str the stringbuffer to assess
	 * @param values values to replace each variable with
	 * @param highDex the index to use for @x0
	 * @return the highest variable index used
	 */
	public final static int replaceVariables(final StringBuffer str, final String values[], int highDex)
	{
		final int numValues=(values==null)?0:values.length;
		final int firstIndex=str.indexOf("@");
		if((numValues==0)||(firstIndex<0))
			return highDex;
		final int valueLen=(numValues<=10)?1:Integer.toString(numValues).length();
		short safety=100;
		for(int i=firstIndex;(i<str.length()-(1+valueLen));i++)
		{
			if((str.charAt(i)=='@')
			&& (Character.toLowerCase(str.charAt(i+1))=='x')
			&& (Character.isDigit(str.charAt(i+2)))
			&&((--safety)>0)
			&&(values != null))
			{
				int endDex=1;
				while((endDex < valueLen) && (Character.isDigit(str.charAt(i+2+endDex))))
					endDex++;
				final int variableIndex = Integer.valueOf(str.substring(i+2,i+2+endDex)).intValue();
				str.delete(i, i+2+endDex);
				if(variableIndex == 0)
				{
					if(highDex < values.length)
						str.insert(i, CMParms.combineQuoted(Arrays.asList(values),highDex));
				}
				else
				if(variableIndex <= numValues)
				{
					str.insert(i, values[variableIndex-1]);
					if(variableIndex > highDex)
						highDex = variableIndex;
				}
				i--;
			}
		}
		return highDex;
	}

	/**
	 * Replaces @x1 type variables inside a stringbuffer with an actual value
	 * Not used in the main expression system, this is a stand alone function
	 * Also uniquely, supports @x numbers above 10.  Values are *1* indexed!!
	 * @param str the stringbuffer to assess
	 * @param values values to replace each variable with
	 * @return the string with values replaced.
	 */
	public final static String replaceVariables(final String str, final String... values)
	{
		if(((values==null)||(values.length==0))&&(str.indexOf('@')<0))
			return str;
		final StringBuffer buf = new StringBuffer(str);
		replaceVariables(buf,values);
		return buf.toString();
	}

	/**
	 * Strips punctuation characters, leaving only letters and
	 * numbers and such.  Removes all this:
	 * !@#$%^&*()+&lt;>.,'\";:) {}[]|\\/?~
	 * Including spaces, for some reason.
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String removePunctuation(final String s)
	{
		final StringBuilder str=new StringBuilder(s);
		for(int i=str.length()-1;i>=0;i--)
		{
			if("!@#$%^&*()+<>.,'\";:) {}[]|\\/?~`".indexOf(str.charAt(i))>=0)
				str.deleteCharAt(i);
		}
		return str.toString();
	}

	/**
	 * Strips punctuation characters, leaving only letters and
	 * numbers and such.  Removes all this:
	 * !.,;:?
	 * Including spaces, for some reason.
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String removePunctuationStrict(final String s)
	{
		final StringBuilder str=new StringBuilder(s);
		for(int i=str.length()-1;i>=0;i--)
		{
			if("!.,;:?`".indexOf(str.charAt(i))>=0)
				str.deleteCharAt(i);
		}
		return str.toString();
	}

	/**
	 * Strips punctuation characters, leaving only letters and
	 * numbers and such.
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String removeAllButLettersAndDigits(final String s)
	{
		final StringBuilder str=new StringBuilder(s);
		for(int i=str.length()-1;i>=0;i--)
		{
			if(!Character.isLetterOrDigit(str.charAt(i)))
				str.deleteCharAt(i);
		}
		return str.toString();
	}

	/**
	 * Returns the given string, limited by the given length, with
	 * colors accounted for.
	 * @param s the string to limit
	 * @param thisMuch the length to limit it to
	 * @return the limited string
	 */
	private static String limitCut(final String s, final int thisMuch)
	{
		if(s==null)
			return "";
		final StringBuilder str=new StringBuilder(s);
		int count = 0;
		int colorStart=-1;
		for(int i=0;i<str.length();i++)
		{
			switch(str.charAt(i))
			{
			case 'm':
				if(colorStart>=0)
					colorStart=-1;
				else
				{
					count++;
					if(count >= thisMuch)
						return str.substring(0,i+1);
				}
				break;
			case (char) 27:
				colorStart = i;
				break;
			case '^':
			{
				if((i+1)<str.length())
				{
					final char c=str.charAt(i+1);
					switch(c)
					{
					case ColorLibrary.COLORCODE_BACKGROUND:
						if(i+3<=str.length())
							i+=2;
						else
							return str.toString();
						break;
					case ColorLibrary.COLORCODE_FANSI256:
					case ColorLibrary.COLORCODE_BANSI256:
						if((i+9<=str.length())&&(str.charAt(i+2)==c))
						{
							if(!CMath.isHexNumber(str.substring(i+3,i+9)))
								i+=4;
							else
								i += 8;
						}
						else
						if(i+5<=str.length())
							i+=4;
						else
							return str.toString();
						break;
					case '<':
					{
						i+=2;
						while(i<(str.length()-1))
						{
							if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
							{
								i++;
								if(i>=(str.length()-1))
									return str.toString();
							}
							else
							{
								i++;
								break;
							}
						}
						break;
					}
					case '&':
					{
						i+=2;
						while(i<(str.length()-1))
						{
							if(str.charAt(i)!=';')
							{
								i++;
								if(i>=(str.length()-1))
									return str.toString();
							}
							else
							{
								// probably viewable
								count++;
								if(count >= thisMuch)
									return str.substring(0,i+1);
							}
							break;
						}
						break;
					}
					case '^':
					{
						i++;
						count++;
						if(count >= thisMuch)
							return str.substring(0,i+1);
						break;
					}
					default:
						i++;
						break;
					}
				}
				else
					return str.toString();
				break;
			}
			default:
			{
				count++;
				if(count >= thisMuch)
					return str.substring(0,i+1);
				break;
			}
			}
		}
		return str.toString();
	}

	/**
	 * Strips colors, of both the ansi, and cm code variety
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String removeColors(final String s)
	{
		if(s==null)
			return "";
		if((s.indexOf('^')<0)&&(s.indexOf((char)27)<0))
			return s;
		final StringBuilder str=new StringBuilder(s);
		int colorStart=-1;
		for(int i=0;i<str.length();i++)
		{
			switch(str.charAt(i))
			{
			case 'm':
				if(colorStart>=0)
				{
					str.delete(colorStart,i+1);
					i=colorStart-1;
					colorStart=-1;
				}
				break;
			case (char) 27:
				colorStart = i;
				break;
			case '^':
				if((i+1)<str.length())
				{
					final int tagStart=i;
					final char c=str.charAt(i+1);
					switch(c)
					{
					case ColorLibrary.COLORCODE_BACKGROUND:
						if(i+3<=str.length())
						{
							str.delete(i,i+3);
							i--;
						}
						break;
					case ColorLibrary.COLORCODE_FANSI256:
					case ColorLibrary.COLORCODE_BANSI256:
						if((i+9<=str.length())&&(str.charAt(i+2)==c))
						{
							if(!CMath.isHexNumber(str.substring(i+3,i+9)))
								str.delete(i,i+5);
							else
								str.delete(i,i+9);
							i--;
						}
						else
						if(i+5<=str.length())
						{
							str.delete(i,i+5);
							i--;
						}
						break;
					case '<':
					{
						i+=2;
						while(i<(str.length()-1))
						{
							if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
							{
								i++;
								if(i>=(str.length()-1))
								{
									i=tagStart;
									str.delete(i,i+2);
									i--;
									break;
								}
							}
							else
							{
								str.delete(tagStart,i+2);
								i=tagStart-1;
								break;
							}
						}
						break;
					}
					case '&':
					{
						i+=2;
						while(i<(str.length()-1))
						{
							if(str.charAt(i)!=';')
							{
								i++;
								if(i>=(str.length()-1))
								{
									i=tagStart;
									str.delete(i,i+2);
									i--;
									break;
								}
							}
							else
							{
								str.delete(tagStart,i+1);
								i=tagStart-1;
								break;
							}
						}
						break;
					}
					default:
					{
						str.delete(i,i+2);
						i--;
					}
					break;
					}
				}
				else
				{
					str.delete(i,i+2);
					i--;
				}
				break;
			}
		}
		return str.toString();
	}

	/**
	 * Strips cr and lf
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String removeCRLF(final String s)
	{
		if(s==null)
			return "";
		if((s.indexOf('\n')<0)&&(s.indexOf('\r')<0))
			return s;
		final StringBuilder str=new StringBuilder(s);
		for(int i=0;i<str.length();i++)
		{
			switch(str.charAt(i))
			{
			case '\n':
			case '\r':
				str.delete(i, i+1);
				break;
			default:
				break;
			}
		}
		return str.toString();
	}

	/**
	 * Strips cr and lf, replacing them with a space when necc
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String unWWrap(final String s)
	{
		if(s==null)
			return "";
		if((s.indexOf('\n')<0)&&(s.indexOf('\r')<0))
			return s;
		final StringBuilder str=new StringBuilder(s);
		for(int i=0;i<str.length();i++)
		{
			final char c=str.charAt(i);
			switch(c)
			{
			case '\n':
			case '\r':
			{
				if((i==0)||(i==str.length()-1))
					str.delete(i, i+1);
				else
				{
					int len=1;
					final char nc = str.charAt(i+1);
					if((nc!=c)&&((nc=='\n')||(nc=='\r')))
						len++;
					if(i+len>=str.length())
						str.delete(i, i+len);
					else
					if((!Character.isWhitespace(str.charAt(i-1))&&(!Character.isWhitespace(str.charAt(i+len)))))
						str.replace(i, i+len, " ");
					else
						str.delete(i, i+len);
				}
				break;
			}
			default:
				break;
			}
		}
		return str.toString();
	}

	/**
	 * Returns a boolean array of the chars marking where
	 * colors (ansi and cm), and other markups chars are
	 * located in the string
	 * @param s the string to strip
	 * @return the markup char locations as true
	 */
	public final static boolean[] markMarkups(final String s)
	{
		if(s==null)
			return new boolean[0];
		final String HEX_DIGITS="0123456789ABCDEFabcdef";
		final StringBuilder str=new StringBuilder(s);
		final boolean[] nos=new boolean[s.length()];
		boolean colorStart=false;
		for(int i=0;i<str.length();i++)
		{
			switch(str.charAt(i))
			{
			case 'm':
				if(colorStart)
				{
					nos[i]=true;
					colorStart=false;
				}
				break;
			case (char) 27:
				nos[i]=true;
				colorStart=true;
				break;
			case '%':
				if(((i+2)<str.length())
				&&(HEX_DIGITS.indexOf(str.charAt(i+1))>=0)
				&&(HEX_DIGITS.indexOf(str.charAt(i+2))>=0))
				{
					nos[i]=true;
					nos[++i]=true;
					nos[++i]=true;
				}
				break;
			case '^':
				if((i+1)<str.length())
				{
					nos[i]=true;
					final char c=str.charAt(i+1);
					switch(c)
					{
					case ColorLibrary.COLORCODE_BACKGROUND:
						if(i+3<=str.length())
						{
							nos[++i]=true;
							nos[++i]=true;
						}
						break;
					case ColorLibrary.COLORCODE_FANSI256:
					case ColorLibrary.COLORCODE_BANSI256:
						if((i+9<str.length())&&(str.charAt(i+2)==c))
						{
							nos[++i]=true;
							nos[++i]=true;
							nos[++i]=true;
							nos[++i]=true;
							if(CMath.isHexNumber(str.substring(i+3,i+9)))
							{
								nos[++i]=true;
								nos[++i]=true;
								nos[++i]=true;
								nos[++i]=true;
							}
						}
						else
						if(i+5<=str.length())
						{
							nos[++i]=true;
							nos[++i]=true;
							nos[++i]=true;
							nos[++i]=true;
						}
						break;
					case '<':
					{
						nos[i]=true;
						nos[i+1]=true;
						i+=2;
						while(i<(str.length()-1))
						{
							nos[i]=true;
							if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
							{
								i++;
								if(i>=(str.length()-1))
									break;
							}
							else
							{
								nos[++i]=true;
								break;
							}
						}
						break;
					}
					case '&':
					{
						nos[i]=true;
						nos[i+1]=true;
						i+=2;
						while(i<(str.length()-1))
						{
							if(str.charAt(i)!=';')
							{
								nos[i++]=true;
								if(i>=(str.length()-1))
								{
									nos[i]=true;
									break;
								}
							}
							else
							{
								nos[i]=true;
								break;
							}
						}
						break;
					}
					default:
					{
						nos[++i]=true;
					}
					break;
					}
				}
				else
				{
					nos[i]=true;
				}
				break;
			default:
				if(colorStart)
					nos[i]=true;
				break;
			}
		}
		return nos;
	}

	/**
	 * Returns the length of the string as if it has neither
	 * ansi nor cm color codes.
	 * @param thisStr the string to get the length of
	 * @return the length of thisStr w/o colors
	 */
	public final static int lengthMinusColors(final String thisStr)
	{
		if(thisStr==null)
			return 0;
		if((thisStr.indexOf('^')<0)&&(thisStr.indexOf((char)27)<0))
			return thisStr.length();
		int size=0;
		boolean colorStart = false;
		for(int i=0;i<thisStr.length();i++)
		{
			switch(thisStr.charAt(i))
			{
			case 'm':
				if(colorStart)
					colorStart=false;
				else
					size++;
				break;
			case (char) 27:
				colorStart = true;
				break;
			case '^':
				if(!colorStart)
				{
					i++;
					if((i+1)<thisStr.length())
					{
						final int tagStart=i;
						switch(thisStr.charAt(i))
						{
						case ColorLibrary.COLORCODE_BACKGROUND:
							i++;
							break;
						case ColorLibrary.COLORCODE_FANSI256:
							if((i<thisStr.length()-8)
							&&(thisStr.charAt(i+1)==thisStr.charAt(i)))
							{
								if(!CMath.isHexNumber(thisStr.substring(i+2,i+8)))
									i+=3;
								else
									i += 7;
							}
							else
								i += 3;
							break;
						case ColorLibrary.COLORCODE_BANSI256:
							if((i<thisStr.length()-9)
							&&(thisStr.charAt(i+1)==thisStr.charAt(i)))
							{
								if(!CMath.isHexNumber(thisStr.substring(i+2,i+8)))
								{
									if(CMath.isHexNumber(thisStr.substring(i+2,i+4)))
										i+=3;
								}
								else
									i += 7;
							}
							else
								i += 3;
							break;
						case '<':
						{
							while(i<(thisStr.length()-1))
							{
								if((thisStr.charAt(i)!='^')||(thisStr.charAt(i+1)!='>'))
								{
									i++;
									if(i>=(thisStr.length()-1))
									{
										i=tagStart+1;
										break;
									}
								}
								else
								{
									i++;
									break;
								}
							}
							break;
						}
						case '&':
						{
							while(i<(thisStr.length()-1))
							{
								if(thisStr.charAt(i)!=';')
								{
									i++;
									if(i>=(thisStr.length()-1))
									{
										i=tagStart+1;
										break;
									}
								}
								else
								{
									i++;
									break;
								}
							}
							break;
						}
						}
					}
				}
				break;
			default:
				if(!colorStart)
					size++;
				break;
			}
		}
		return size;
	}

	/**
	 * This method returns the given string with any
	 * &amp;..; entities converted to their original self
	 * @param s the string to convert
	 * @return the new converted string
	 */
	public static String convertHtmlEntities(String s)
	{
		if(s==null)
			return null;
		int x=s.indexOf('&');
		while(x>=0)
		{
			final int y=s.indexOf(';',x+1);
			if(y>x)
			{
				final String code=s.substring(x+1,y).toLowerCase();
				if(code.equals("nbsp"))
					s=s.substring(0,x)+" "+s.substring(y+1);
				else
				if (code.equals("amp"))
				{
					s=s.substring(0,x)+"&"+s.substring(y+1);
					x++;
				}
				else
				if (code.equals("lt"))
					s=s.substring(0,x)+"<"+s.substring(y+1);
				else
				if (code.equals("gt"))
					s=s.substring(0,x)+">"+s.substring(y+1);
				else
				if (code.equals("quot"))
					s=s.substring(0,x)+"\""+s.substring(y+1);
				else
				if (code.startsWith("#")
				&&(CMath.isInteger(code.substring(1))))
				{
					final int num=CMath.s_int(code.substring(1));
					if(num<65536)
						s=s.substring(0,x)+Character.toString((char)num)+s.substring(y+1);
				}
			}
			x=s.indexOf('&',x+1);
		}
		return s;
	}

	/**
	 * This monstrous method converts an html document into a somewhat-readable text
	 * document for display in, for example, the text portion of an email, or in the
	 * mud command line.  It does things like remove scripts, convert &nbsp;-like tags
	 * to their ascii values, and converts &lt;P&gt;, &lt;BR&gt;, and &lt;DIV&gt; tags to CRLF.
	 * @param finalData the stringbuilder containing the html to convert.
	 * @return the converted string (finalData is also modified)
	 */
	public static String convertHtmlToText(final StringBuilder finalData)
	{
		final class TagStacker
		{
			private Stack<Object[]> tagStack=new Stack<Object[]>();
			public void push(final String tag, final int index)
			{
				tagStack.push(new Object[]{tag,Integer.valueOf(index)});
			}
			@SuppressWarnings("unchecked")
			public int pop(final String tag)
			{
				if(tagStack.size()==0)
					return -1;
				final Stack<Object[]> backup=(Stack<Object[]>)tagStack.clone();
				Object[] top;
				do
				{
					top=tagStack.pop();
				}
				while((!((String)top[0]).equals(tag))&&(!tagStack.isEmpty()))
					;
				if(!((String)top[0]).equals(tag))
				{
					tagStack=backup;
					return -1;
				}
				return ((Integer)top[1]).intValue();
			}
		}
		final TagStacker stack=new TagStacker();
		final String[] badBlockTags=new String[]{"STYLE","SCRIPT","HEAD"};
		final String[][] xlateTags=new String[][]{
				{"P","\n\r"}, {"BR","\n\r"}, {"DIV","\n\r"}, {"HR","\r\n-----------------------------------------------------------------------------\n\r"}
		};
		int start=-1;
		int state=0;
		char c;
		boolean incomment=false;
		String tag=null;
		for(int i=0;i<finalData.length();i++)
		{
			c=finalData.charAt(i);
			if(incomment)
			{
				if((c=='-')
				&&(i<finalData.length()-2)
				&&(finalData.charAt(i+1)=='-')
				&&(finalData.charAt(i+2)=='>'))
				{
					final int x=stack.pop("<!--");
					if(x>=0)
					{
						finalData.delete(x,i+3);
						i=x-1;
					}
					else
					{
						finalData.delete(i, i+3);
						i=i-3;
					}
					incomment=false;
				}
			}
			else
			if(c=='<')
			{
				start=i;
				state=0;
			}
			else
			if(c=='&')
			{
				start=i;
				state=5;
			}
			else
			if(start<0)
			{
				if((c=='\n')||(c=='\r'))
				{
					if((i>0)&&(i<finalData.length()-2))
					{
						char n=finalData.charAt(i+1);
						final char p=finalData.charAt(i-1);
						if((n=='\n')||(n=='\r')&&(n!=c))
						{
							finalData.deleteCharAt(i+1);
							n=finalData.charAt(i+1);
						}
						if((Character.isLetterOrDigit(n)||((".?,;\"'!@#$%^*()_-+={}[]:").indexOf(n)>=0))
						&&(Character.isLetterOrDigit(p)||((".?,;\"'!@#$%^*()_-+={}[]:".indexOf(p)>=0))))
						{
							finalData.setCharAt(i,' ');
						}
						else
						{
							finalData.delete(i,i+1);
							i--;
						}
					}
				}
				continue;
			}
			else
			switch(state)
			{
			case 0:
				switch(c)
				{
				case ' ':
				case '\t':
				case '<':
				case '>':
					start = 0;
					break;
				case '/':
					state = 2;
					break;
				case '!':
				{
					if((i<finalData.length()-2)
					&&(finalData.charAt(i+1)=='-')
					&&(finalData.charAt(i+2)=='-'))
					{
						stack.push("<!--",start);
						i+=2;
						incomment=true;
					}
					break;
				}
				default:
					if(Character.isLetter(c))
						state=1;
					else
						start=-1;
					break;
				}
				break;
			case 1: // eval start tag
				if(c=='>')
				{
					tag=finalData.substring(start+1,i).toUpperCase();
					stack.push(tag,start);
					state=3;
					i--;
				}
				else
				if(Character.isWhitespace(c)||(c=='/')||(c==':'))
				{
					if(start==i-1)
						start=-1;
					else
					{
						tag=finalData.substring(start+1,i).toUpperCase();
						state=3;
					}
				}
				else
				if((i-start)>20)
					start=-1;
				break;
			case 2: // eval end tag
				if(c=='>')
				{
					state=4;
					tag=finalData.substring(start+2,i).toUpperCase();
					i--;
				}
				else
				if(Character.isWhitespace(c))
				{
					if(start==i-1)
						start=-1;
					else
					if(state==2)
					{
						state=4;
						tag=finalData.substring(start+2,i).toUpperCase();
					}
				}
				else
				if((i-start)>20)
					start=-1;
				break;
			case 3: // end start tag
				if(tag==null)
					start=-1;
				else
				if(c=='>')
				{
					finalData.delete(start, i+1);
					for(final String[] xset : xlateTags)
					{
						if(xset[0].equals(tag))
						{
							finalData.insert(start, xset[1]);
							start=start+xset[1].length()-1;
							break;
						}
					}
					i=start-1;
					start=-1;
					tag=null;
				}
				break;
			case 4: // end end tag
				if(tag==null)
					start=-1;
				else
				if(c=='>')
				{
					if(CMStrings.contains(badBlockTags, tag))
					{
						final int x=stack.pop(tag);
						if(x>=0)
							start=x;
					}
					finalData.delete(start, i+1);
					i=start-1;
					start=-1;
					tag=null;
				}
				break;
			case 5: // during & thing
				if(c==';') // the end
				{
					final String code=finalData.substring(start+1,i).toLowerCase();
					finalData.delete(start, i+1);
					if(code.equals("nbsp"))
						finalData.insert(start,' ');
					else
					if (code.equals("amp"))
						finalData.insert(start, '&');
					else
					if (code.equals("lt"))
						finalData.insert(start, '<');
					else
					if (code.equals("gt"))
						finalData.insert(start, '>');
					else
					if (code.equals("quot"))
						finalData.insert(start, '"');
					i=start-1;
					start=-1;
				}
				else
				if((!Character.isLetter(c))||((i-start)>10))
					start=-1;
				break;
			}
		}
		return finalData.toString();
	}

	/**
	 * Returns the apparent end of line string used by the given
	 * string.  Returns the default if it can't be determined.
	 *
	 * @param str the string to check
	 * @param defaultEOL eol string to use if undetermined
	 * @return the end of line string
	 */
	public static String getEOL(final String str, final String defaultEOL)
	{
		if((str!=null)&&(str.length()>0))
		{
			int state=0;
			for(final char c : str.toCharArray())
			{
				switch(state)
				{
				case 0:
					if(c=='\n')
						state=1;
					else
					if(c=='\r')
						state=2;
					break;
				case 1:
					if(c=='\n')
						return "\n";
					else
					if(c=='\r')
						return "\n\r";
					else
						return "\n";
				case 2:
					if(c=='\r')
						return "\r";
					else
					if(c=='\n')
						return "\r\n";
					else
						return "\r";
				}
			}
		}
		return defaultEOL;
	}

	/**
	 * Strips the leading and trailing &lt;HTML&gt;, &lt;HEAD&gt;, and &lt;BODY&gt; tags from
	 * the given StringBuilder.
	 * @param finalData the StringBuilder to remove leading tags from.
	 */
	public static void stripHeadHtmlTags(final StringBuilder finalData)
	{
		int start=-1;
		int state=0;
		char c=' ';
		char lastC=' ';
		int headStart=-1;
		boolean closeFlag=false;
		for(int i=0;i<finalData.length();i++)
		{
			c=Character.toUpperCase(finalData.charAt(i));
			if(Character.isWhitespace(c))
				continue;
			else
			if(c=='<')
			{
				start=i;
				state=0;
				closeFlag=false;
			}
			else
			if(start<0)
				continue;
			else
			switch(state)
			{
			case 0:
				switch(c)
				{
				case '/':
					state = 1;
					closeFlag = true;
					break;
				case 'H':
					state = 2;
					break;
				case 'B':
					state = 3;
					break;
				default:
					start = -1;
					break;
				} break;
			case 1:
				switch(c)
				{
				case 'H':
					state = 2;
					break;
				case 'B':
					state = 3;
					break;
				default:
					start = -1;
					break;
				} break;
			case 2:
				switch(c)
				{
				case 'E':
					if (lastC != 'H')
						state = -1;
					else
						state = 5;
					break;
				case 'T':
					if (lastC != 'H')
						state = -1;
					break;
				case 'M':
					if (lastC != 'T')
						state = -1;
					break;
				case 'L':
					if (lastC != 'M')
						state = -1;
					else
						state = 4;
					break;
				default:
					start = -1;
					break;
				} break;
			case 3:
				switch(c)
				{
				case 'O':
					if (lastC != 'B')
						state = -1;
					break;
				case 'D':
					if (lastC != 'O')
						state = -1;
					break;
				case 'Y':
					if (lastC != 'D')
						state = -1;
					else
						state = 4;
					break;
				default:
					start = -1;
					break;
				} break;
			case 4:
				if(c=='>')
				{
					finalData.delete(start, i+1);
					i=start-1;
					start=-1;
				}
				break;
			case 5:
				switch(c)
				{
				case 'A':
					if (lastC != 'E')
						state = -1;
					break;
				case 'D':
					if (lastC != 'A')
						state = -1;
					else
						state = 6;
					break;
				default:
					start = -1;
					break;
				} break;
			case 6:
				if(c=='>')
				{
					if(!closeFlag)
					{
						finalData.delete(start, i+1);
						headStart=start;
					}
					else
					{
						finalData.delete(headStart, i+1);
						start=headStart;
					}
					i=start-1;
					start=-1;
				}
				break;
			}
			lastC=c;
		}
	}

	/**
	 * Given an array of objects, this method creates a map of those
	 * objects to their ordinal numeric values.
	 * @param obj the array of objects
	 * @return a map of the objects and their ordinal numeric values.
	 */
	public final static Map<Object,Integer> makeNumericHash(final Object[] obj)
	{
		final Hashtable<Object,Integer> H=new Hashtable<Object,Integer>();
		for(int i=0;i<obj.length;i++)
			H.put(obj[i],Integer.valueOf(i));
		return H;
	}

	/**
	 * Given an array of objects, this method creates a map of those
	 * objects to their ordinal numeric values, with a given opener.
	 * @param obj the array of objects
	 * @param firstInt the ordinal value to start with
	 * @return a map of the objects and their ordinal numeric values.
	 */
	public final static Map<Object,Integer> makeNumericHash(final Object[] obj, final int firstInt)
	{
		final Hashtable<Object,Integer> H=new Hashtable<Object,Integer>();
		for(int i=0;i<obj.length;i++)
			H.put(obj[i],Integer.valueOf(i+firstInt));
		return H;
	}

	/**
	 * Pads the string equally to the left and right with spaces until it is the
	 * length of the given number. If the string is already larger than the given
	 * number, no spaces are added, and the string is truncated.  Color codes are
	 * removed if truncation is necessary.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or truncated if already too long
	 */
	public final static String padCenter(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		final int size=(thisMuch-lenMinusColors)/2;
		int rest=thisMuch-lenMinusColors-size;
		if(rest<0)
			rest=0;
		return SPACES.substring(0,size)+thisStr+SPACES.substring(0,rest);
	}

	/**
	 * Pads the string to the left with spaces until it is the length
	 * of the given number. If the string is already larger than the given number, the
	 * string is truncated at the end until it is the given length.  If the string
	 * must be truncated, any color codes are also removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padLeft(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}

	/**
	 * Pads the string to the left with the given character until it is the length
	 * of the given number. If the string is already larger than the given number, the
	 * string is truncated at the end until it is the given length.  If the string
	 * must be truncated, any color codes are also removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param c the character to pad the string with
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padLeftWith(final String thisStr, final char c, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		final StringBuilder str=new StringBuilder("");
		for(int i=0;i<thisMuch-lenMinusColors;i++)
			str.append(c);
		str.append(thisStr);
		return str.toString();
	}

	/**
	 * Pads the string to the left with spaces until it is the length of the given
	 * number. If the string is already larger than the given number, the string is
	 * truncated at the end until it is the given length.
	 * This method always prepends the given colorSuffix string to the beginning of the string
	 * after any spaces are added, before the string is truncated.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param colorPrefix the string to always add to the beginning of the given string before spaces
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padLeft(final String thisStr, final String colorPrefix, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return colorPrefix+removeColors(thisStr).substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lenMinusColors)+colorPrefix+thisStr;
	}

	/**
	 * Truncates the given string if the string is larger than the given number,
	 * or returns it unchanged otherwise.
	 * NO special color codes are removed or evaluated -- this is simple string truncation.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final maximum length of the string.
	 * @return the string truncated, or unchanged if not long enough
	 */
	public final static String safeLeft(final String thisStr, final int thisMuch)
	{
		if(thisStr.length()<=thisMuch)
			return thisStr;
		return thisStr.substring(0,thisMuch);
	}

	/**
	 * Truncates the given string if the string is larger than the given number,
	 * or returns it unchanged otherwise.
	 * NO special color codes are removed or evaluated -- this is simple string truncation.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final maximum length of the string.
	 * @return the string truncated, or unchanged if not long enough
	 */
	public final static String truncate(final String thisStr, final int thisMuch)
	{
		if(thisStr.length()<=thisMuch)
			return thisStr;
		return thisStr.substring(0,thisMuch);
	}

	/**
	 * If the given string both begins and ends with double-quotes ",
	 * then this method removes them before returning the string, and
	 * does nothing otherwise.
	 * @param thisStr the string to remove quotes from
	 * @return the string without prefix and suffix quotes, or unchanged
	 */
	public final static String trimQuotes(final String thisStr)
	{
		if(thisStr.startsWith("\"")&&thisStr.endsWith("\""))
			return thisStr.substring(1,thisStr.length()-1);
		return thisStr;
	}

	/**
	 * If the given string ends or begins with cr or lf,
	 * then this method removes them before returning the string, and
	 * does nothing otherwise.
	 * @param thisStr the string to remove cr and lf from
	 * @return the string without cr and lf at the ends, or unchanged
	 */
	public final static String trimCRLF(final String thisStr)
	{
		while(thisStr.startsWith("\r")||thisStr.startsWith("\n"))
			return thisStr.substring(1);
		while(thisStr.endsWith("\r")||thisStr.endsWith("\n"))
			return thisStr.substring(0,thisStr.length()-1);
		return thisStr;
	}

	/**
	 * If the given string contains cr or lf, but not both,
	 * then this method will fix the string to use both as
	 * per mud standard.
	 * @param thisStr the string to fix cr and lf in
	 * @return the string fixed, or unchanged
	 */
	public final static String fixMudCRLF(final String thisStr)
	{
		if(thisStr==null)
			return null;
		if((thisStr.indexOf('\n')<0)&&(thisStr.indexOf('\r')<0))
			return thisStr;
		final StringBuilder seq = new StringBuilder(thisStr);
		for(int i=0;i<seq.length();i++)
		{
			if(seq.charAt(i)=='\n')
			{
				if(i==seq.length()-1)
					seq.append('\r');
				else
				if(seq.charAt(i+1) != '\r')
					seq.insert(i+1, '\r');
				i++;
			}
			else
			if(seq.charAt(i)=='\r')
			{
				if(i==0)
					seq.insert(0, '\n');
				else
					seq.insert(i, '\n');
				i++;
			}
		}
		return thisStr;
	}

	/**
	 * Pads the string to the right with spaces until it is the length
	 * of the given number. If the string is already larger than the given number, the
	 * string is truncated at the end until it is the given length.  If the string
	 * must be truncated, any color codes are also removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padRight(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors==thisMuch)
			return thisStr;
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		if(thisMuch-lenMinusColors >= SPACES.length())
			return thisStr+SPACES;
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}

	/**
	 * Pads the string to the right with spaces until it is the length
	 * of the given number. If the string is already larger than the given number, the
	 * string is truncated at the end until it is the given length.  If the string
	 * must be truncated, any color codes are also removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param prefixColorStr the string to prefix the padded string with
	 * @param thisStr the string to pad or truncate
	 * @param suffixColorStr the string to suffix the string before padding
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padRight(final String prefixColorStr, final String thisStr, final String suffixColorStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors==thisMuch)
			return prefixColorStr + thisStr+suffixColorStr;
		if(lenMinusColors>thisMuch)
			return prefixColorStr + removeColors(thisStr).substring(0,thisMuch)+suffixColorStr;
		if(thisMuch-lenMinusColors >= SPACES.length())
			return prefixColorStr + thisStr + suffixColorStr + SPACES;
		return prefixColorStr + thisStr + suffixColorStr + SPACES.substring(0,thisMuch-lenMinusColors);
	}

	/**
	 * Pads the string to the right with the given character until it is the length
	 * of the given number. If the string is already larger than the given number, the
	 * string is truncated at the end until it is the given length.  If the string
	 * must be truncated, any color codes are also removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param c the character to pad the string with
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padRightWith(final String thisStr, final char c, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors==thisMuch)
			return thisStr;
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		final StringBuilder str=new StringBuilder(thisStr);
		for(int i=0;i<thisMuch-lenMinusColors;i++)
			str.append(c);
		return str.toString();
	}

	/**
	 * Truncates the given string if the string is larger than the given number,
	 * or returns it unchanged otherwise. If the string is larger than the given
	 * number, color codes are removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param prefixColorStr the string to prefix the padded string with
	 * @param thisStr the string to pad or truncate
	 * @param suffixColorStr the string to suffix the string before padding
	 * @param thisMuch the final maximum length of the string.
	 * @return the string truncated, or unchanged if not long enough
	 */
	public final static String limit(final String prefixColorStr, final String thisStr, final String suffixColorStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return prefixColorStr+removeColors(thisStr).substring(0,thisMuch)+suffixColorStr;
		return prefixColorStr+thisStr+suffixColorStr;
	}

	/**
	 * Truncates the given string if the string is larger than the given number,
	 * or returns it unchanged otherwise. If the string is larger than the given
	 * number, color codes are removed.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final maximum length of the string.
	 * @return the string truncated, or unchanged if not long enough
	 */
	public final static String limit(final String thisStr, final int thisMuch)
	{
		if(thisMuch <= 0)
			return thisStr;
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		return thisStr;
	}

	/**
	 * Truncates the given string if the string is larger than the given number,
	 * or returns it unchanged otherwise. If the string is larger than the given
	 * number, color codes are preserved.
	 * This method preserves any special CoffeeMud/ANSI color codes before calculating
	 * length as if colors weren't there.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final maximum length of the string.
	 * @return the string truncated, or unchanged if not long enough
	 */
	public final static String limitColors(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return limitCut(thisStr, thisMuch);
		return thisStr;
	}

	/**
	 * Pads the string to the right with three dots if the string is larger than
	 * the given number, or returns it unchanged otherwise. If the string is
	 * larger than the given number, the string is truncated at the end, color codes
	 * removed, until it is the given length, and the ellipse added.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final maximum length of the string before ...
	 * @return the string padded, or unchanged if not long enough
	 */
	public final static String ellipse(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch)+"...";
		return thisStr;
	}

	/**
	 * Returns the given string, unless it is null, in which
	 * case it returns "".
	 * @param str the string
	 * @return the String, or ""
	 */
	public final static String emptyString(final String str)
	{
		return (str == null)?"":str;
	}

	/**
	 * Pads the string to the right with three dots if the string is larger than
	 * the given number, or returns it unchanged otherwise. If the string is
	 * larger than the given number, the string is truncated at the end, color codes
	 * preserved, until it is the given length, and the ellipse added.
	 * This method preserves any special CoffeeMud/ANSI color codes while calculating
	 * length as if they weren't there.
	 * @param thisStr the string to pad or truncate
	 * @param thisMuch the final maximum length of the string before ...
	 * @return the string padded, or unchanged if not long enough
	 */
	public final static String ellipseColored(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return limitCut(thisStr, thisMuch)+"^.^N...";
		return thisStr;
	}

	/**
	 * Pads the string to the right with spaces until it is the length of the given
	 * number. If the string is already larger than the given number, the string is
	 * truncated at the end until it is the given length.
	 * This method always adds the given colorSuffix string to the end of the string
	 * before any spaces are added, or after the string is truncated.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad or truncate
	 * @param colorSuffix the string to always add to the end of the given string before spaces
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padRight(final String thisStr, final String colorSuffix, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch)+colorSuffix;
		if(thisMuch-lenMinusColors >= SPACES.length())
			return thisStr+colorSuffix+SPACES;
		return thisStr+colorSuffix+SPACES.substring(0,thisMuch-lenMinusColors);
	}

	/**
	 * Pads the string to the right with spaces until it is the length of the given
	 * number. If the string is already larger than the given number, no spaces are
	 * added, and the string is returned unchanged.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padRightPreserve(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return thisStr;
		if(thisMuch-lenMinusColors >= SPACES.length())
			return thisStr+SPACES;
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}

	/**
	 * Pads the string equally to the left and right with spaces until it is the
	 * length of the given number. If the string is already larger than the given
	 * number, no spaces are added, and the string is returned unchanged.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String centerPreserve(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		final int left=(thisMuch-lenMinusColors)/2;
		final int right=((left+left+lenMinusColors)<thisMuch)?left+1:left;
		if(thisMuch-lenMinusColors >= SPACES.length())
			return thisStr+SPACES;
		return SPACES.substring(0,left)+thisStr+SPACES.substring(0,right);
	}

	/**
	 * Pads the string to the left with spaces until it is the length of the given
	 * number. If the string is already larger than the given number, no spaces are
	 * added, and the string is returned unchanged.
	 * This method removes any special CoffeeMud/ANSI color codes before calculating
	 * length.
	 * @param thisStr the string to pad
	 * @param thisMuch the final minimum length of the string.
	 * @return the string padded, or unchanged if already too long
	 */
	public final static String padLeftPreserve(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}

	/**
	 * Returns the given string in the same entire case as the character given.
	 * @param str the string to make upper or lowercase
	 * @param c if this character is uppercase, makes the whole string uppercase, etc.
	 * @return the given str, with the case changed.
	 */
	public final static String sameCase(final String str, final char c)
	{
		if(Character.isUpperCase(c))
			return str.toUpperCase();
		return str.toLowerCase();
	}

	/**
	 * Given a string array, will return the string at the given
	 * index, returning the def itself if out-of-range.
	 * @param strs the string array
	 * @param index the array index
	 * @param def what to return if out-of-range
	 */
	public final static String s_indexStr(final String[] strs, final int index, final String def)
	{
		if((index<0)||(index>=strs.length))
			return ""+index;
		return strs[index];
	}

	/**
	 * Tokenizer/Scanner table for the string expression parser and evaluator.
	 * Clearly this is a state machine, where the values a series of characters, followed by
	 * the next state that the character would put the tokenizer info.  The states are numbered
	 * according to the index of the outer int array.  Other states are below.
	 * States: 0 = done after this one,-1 = done a char ago,-2 = eat & same state,-99 = error,
	 * Special Chars: 254 = digit, 253 = letter, 252 = digitNO0, 255=eof
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @see CMStrings.StringExpToken
	 */
	private static final int[][]	STRING_EXP_SM    = { { -1 }, // 0 == done after this one, 1 == real first state
			{ ' ', -2, '=', 2, '>', 4, '<', 5, '!', 2, '(', 0, ')', 0, '\"', 3, '+', 0, '-', 0, '*', 0, '/', 0, '&', 6, '?',0, '|', 7, '\'', 8, '`', 9, '$', 10, 253, 12, 252, 13, '0', 15, 255, 255, -99 }, // 1
			{ '=', 0, -1 }, // 2 -- starts with =
			{ '\"', 0, 255, -99, 3 }, // 3 -- starts with "
			{ '=', 0, '>', 0, -1 }, // 4 -- starts with <
			{ '=', 0, '<', 0, -1 }, // 5 -- starts with >
			{ '&', 0, -1 }, // 6 -- starts with &
			{ '|', 0, -1 }, // 7 -- starts with |
			{ '\'', 0, 255, -99, 8 }, // 8 -- starts with '
			{ '`', 0, 255, -99, 9 }, // 9 -- starts with `
			{ 253, 11, '_', 11, -99 }, // 10 == starts with $
			{ 253, 11, 254, 11, '_', 11, 255, -1, -1 }, // 11=starts $Letter
			{ 253, 12, 255, -1, -1 },   			 // 12=starts with letter
			{ 254, 13, '.', 14, -1}, // 13=starts with a digit
			{ 254, 14, '.', -99, -1}, // 14=continues a digit
			{ 254, -99, '.', 14, -1} // 15=starts with a 0
	};

	/**
	 * Token type enumeration for the string expression parser and evaluator.
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @see CMStrings.StringExpToken
	 * @author Bo Zimmerman
	 */
	private enum StringExpTokenType
	{
		NOTHING,
		EVALUATOR,
		OPENPAREN,
		CLOSEPAREN,
		WORD,
		STRCONST,
		COMBINER,
		NOT,
		NUMCONST,
		UKNCONST
	}

	/**
	 * Main token class for the string expression parser and evaluator.
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @author Bo Zimmerman
	 */
	private static class StringExpToken
	{
		public StringExpTokenType	type;
		public String    			value;
		public double     			numValue  = 0.0;

		/**
		 * Construct a new token from valid data.  If the value given is numeric, a double
		 * value of it will be parsed, which may throw an exception if that parsing fails.
		 * @param type the type of token this is supposed to be
		 * @param value the string value of the token
		 * @return the valid token
		 * @throws Exception might throw an exception if the token starts with a digit, but is not numeric
		 */
		public final static StringExpToken token(final StringExpTokenType type, final String value) throws Exception
		{
			final StringExpToken token = new StringExpToken();
			token.type = type;
			token.value = value;
			if((value.length()>0)&&(CMath.isNumber(value)))
				token.numValue = Double.parseDouble(value);
			return token;
		}

		private StringExpToken()
		{
		}
	}

	/**
	 * Given a list of tokens and the current index into the list, this method returns
	 * the token at the current index, or null if end-of-list is reached.  If a token
	 * is returned, the index is incremented by one.
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param tokens the list of tokens
	 * @param index the index into the token list
	 * @return the current token
	 */
	private static StringExpToken nextToken(final List<StringExpToken> tokens, final int[] index)
	{
		if(index[0]>=tokens.size())
			return null;
		return tokens.get(index[0]++);
	}

	/**
	 * Creates a valid string parsing token object given the idenfied token substring from the
	 * expression, the variables to look up, if necessary, and whether missing variables are
	 * evaluated as "", or an exception is thrown.  Variable substitution happens in this
	 * method as $ prefixed variables are identified.
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param token the token substring to evaluate
	 * @param variables the string to string map of $variables for substitution
	 * @param emptyVars true if missing variables are evaluated as "", or false to throw an exception
	 * @return the valid token object
	 * @throws Exception thrown if emptyVars is false and a $variable is referenced but not found
	 */
	private static StringExpToken makeTokenType(String token, final Map<String,Object> variables, final boolean emptyVars) throws Exception
	{
		if ((token == null)||(token.length()==0))
			return null;
		switch(token.charAt(0))
		{
		case '\"':
			return StringExpToken.token(StringExpTokenType.STRCONST, token.substring(1, token.length() - 1));
		case '\'':
			return StringExpToken.token(StringExpTokenType.STRCONST, token.substring(1, token.length() - 1));
		case '`':
			return StringExpToken.token(StringExpTokenType.STRCONST, token.substring(1, token.length() - 1));
		case '(':
			if(token.length()==1)
				return StringExpToken.token(StringExpTokenType.OPENPAREN, token);
			break;
		case ')':
			if(token.length()==1)
				return StringExpToken.token(StringExpTokenType.CLOSEPAREN, token);
			break;
		case 'I':
			if (token.equalsIgnoreCase("IN"))
				return StringExpToken.token(StringExpTokenType.EVALUATOR, token);
			break;
		case '+':
		case '-':
		case '*':
		case '/':
		case '?':
			if(token.length()==1)
				return StringExpToken.token(StringExpTokenType.COMBINER, token);
			break;
		case '!':
			if(token.length()==1)
				return StringExpToken.token(StringExpTokenType.NOT, token);
			break;
		case 'N':
			if (token.equalsIgnoreCase("NOT"))
				return StringExpToken.token(StringExpTokenType.NOT, token);
			break;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return StringExpToken.token(StringExpTokenType.NUMCONST, token);
		case '$':
		{
			token = token.substring(1);
			Object value = variables.get(token);
			if(!(value instanceof String))
				value = variables.get(token.toUpperCase().trim());
			if((value == null)&&(emptyVars))
				value="";
			else
			if(!(value instanceof String))
				throw new Exception("Undefined variable found: $" + token);
			if((value.toString().length()>0)&&(!CMath.isNumber(value.toString())))
				return StringExpToken.token(StringExpTokenType.STRCONST, value.toString());
			return StringExpToken.token(StringExpTokenType.UKNCONST, value.toString());
		}
		case '_':
		case '|':
		case '&':
			return StringExpToken.token(StringExpTokenType.WORD, token);
		default:
			if (Character.isLetterOrDigit(token.charAt(0)))
				return StringExpToken.token(StringExpTokenType.WORD, token);
			break;
		}
		return StringExpToken.token(StringExpTokenType.EVALUATOR, token);
	}

	/**
	 * This is the main string expression evaluator Scanner/Tokenizer.
	 * Given a full string expression, this method returns the next valid token in the string from
	 * the given index.  Variables are evaluated at parse time, and missing variables are evaluated
	 * as "" if emptyVars is true, and throw and expression otherwise.  The index into the expression
	 * passed in will be returned modified after a token is identified.
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param expression the full string expression to tokenize
	 * @param index the index into the expression to begin tokenizing.  Is modified.
	 * @param variables the string to string map of $variables to substitute.
	 * @param emptyVars true if missing variables are "", and false to generate an exception
	 * @return the next token idenfied in the expression at the index
	 * @throws Exception a parsing error occurred, typically a missing variable
	 */
	private static StringExpToken nextStringToken(final String expression, final int[] index, final Map<String,Object> variables, final boolean emptyVars) throws Exception
	{
		int[] stateBlock = STRING_EXP_SM[1];
		final StringBuffer token = new StringBuffer("");
		while (index[0] < expression.length())
		{
			final char c = expression.charAt(index[0]);
			int nextState = stateBlock[stateBlock.length - 1];
			boolean match = false;
			for (int x = 0; x < stateBlock.length - 1; x += 2)
			{
				switch (stateBlock[x])
				{
					case 254:
						match = Character.isDigit(c);
						break;
					case 252:
						match = Character.isDigit(c)&&(c!='0');
						break;
					case 253:
						match = Character.isLetter(c);
						break;
					case 255:
						break; // nope, not yet
					default:
						match = (c == stateBlock[x]);
						break;
				}
				if (match)
				{
					nextState = stateBlock[x + 1];
					break;
				}
			}
			switch (nextState)
			{
				case 255:
					return null;
				case -99:
					throw new Exception("Illegal character in expression: " + c);
				case -2:
					index[0]++;
					break;
				case -1:
					return makeTokenType(token.toString(), variables, emptyVars);
				case 0:
				{
					token.append(c);
					index[0]++;
					return makeTokenType(token.toString(), variables, emptyVars);
				}
				default:
				{
					token.append(c);
					index[0]++;
					stateBlock = STRING_EXP_SM[nextState];
					break;
				}
			}
		}
		int finalState = stateBlock[stateBlock.length - 1];
		for (int x = 0; x < stateBlock.length - 1; x += 2)
		{
			if (stateBlock[x] == 255)
			{
				finalState = stateBlock[x + 1];
				break;
			}
		}
		switch (finalState)
		{
			case -99:
				throw new Exception("Expression ended prematurely");
			case -1:
			case 0:
				return makeTokenType(token.toString(), variables, emptyVars);
			default:
				return null;
		}
	}

	/**
	 * Given a set of pre-parsed tokens from an Expression, this method returns a literal value token
	 * begining with the token at the given index, which may be modified.  Variables are substituted
	 * at evaluation time.  Parsing errors throw an exception
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param tokens the operator expression tokens -- pre-parsed
	 * @param index the token list index, which may be incremented as parsing continues
	 * @param variables the variables to substitute
	 * @return the literal or evaluated variable token.
	 * @throws Exception a parsing error occurred
	 */
	private final static StringExpToken matchSimpleValue(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		final int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if((token.type==StringExpTokenType.COMBINER)
		&&(token.value.equals("-")))
		{
			tokens.remove(--i[0]);
			token = nextToken(tokens, i);
			token.value="-"+token.value;
			if(token.type==StringExpTokenType.NUMCONST)
			{
				try
				{
					token.numValue=Double.valueOf(token.value).doubleValue();
				}
				catch(final Exception e)
				{
				}
			}
		}
		if((token.type != StringExpTokenType.NUMCONST)
		&& (token.type != StringExpTokenType.STRCONST)
		&& (token.type != StringExpTokenType.UKNCONST))
			return null;
		index[0] = i[0];
		return token;
	}

	/**
	 * Given a set of pre-parsed tokens from an Expression, this method returns a literal or combined token
	 * begining with the token at the given index, which may be modified.  Variables are substituted
	 * at evaluation time.  Parsing errors throw an exception
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param tokens the operator expression tokens -- pre-parsed
	 * @param index the token list index, which may be incremented as parsing continues
	 * @param variables the variables to substitute
	 * @return the literal, evaluated variable, or combined value token.
	 * @throws Exception a parsing error occurred
	 */
	private static StringExpToken matchCombinedValue(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type == StringExpTokenType.OPENPAREN)
		{
			final StringExpToken testInside = matchCombinedValue(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != StringExpTokenType.CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = index.clone();
		final StringExpToken leftValue = matchSimpleValue(tokens, i, variables);
		if (leftValue == null)
			return null;
		final int[] i2 = i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != StringExpTokenType.COMBINER))
		{
			index[0] = i[0];
			return leftValue;
		}
		i[0] = i2[0];
		final StringExpToken rightValue = matchCombinedValue(tokens, i, variables);
		if (rightValue == null)
			return null;
		index[0] = i[0];
		if((leftValue.type==StringExpTokenType.STRCONST)||(rightValue.type==StringExpTokenType.STRCONST))
		{
			if(!token.value.equals("+"))
				throw new Exception("Can't combine a string using '"+token.value+"'");
			index[0] = i[0];
			final StringExpToken result=new StringExpToken();
			result.type=StringExpTokenType.STRCONST;
			result.value=leftValue.value + rightValue.value;
			return result;
		}
		final StringExpToken result=new StringExpToken();
		result.type=StringExpTokenType.NUMCONST;
		if(token.value.length()==1)
		{
			switch(token.value.charAt(0))
			{
			case '+':
			{
				index[0] = i[0];
				result.numValue=leftValue.numValue + rightValue.numValue;
				return result;
			}
			case '-':
			{
				index[0] = i[0];
				result.numValue=leftValue.numValue - rightValue.numValue;
				return result;
			}
			case '*':
			{
				index[0] = i[0];
				result.numValue=leftValue.numValue * rightValue.numValue;
				return result;
			}
			case '/':
			{
				index[0] = i[0];
				result.numValue=leftValue.numValue / rightValue.numValue;
				return result;
			}
			case '?':
			{
				index[0] = i[0];
				result.numValue=Math.round((Math.random() * (rightValue.numValue-leftValue.numValue)) + leftValue.numValue);
				return result;
			}
			default:
				throw new Exception("Unknown math combiner "+token.value);
			}
		}
		else
			throw new Exception("Unknown math combiner "+token.value);
	}

	/**
	 * Given a set of pre-parsed tokens from an Expression, this method evaluates the operator expression,
	 * begining with the token at the given index, which may be modified.  Variables are substituted
	 * at evaluation time.  Parsing errors throw an exception
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param tokens the operator expression tokens -- pre-parsed
	 * @param index the token list index, which may be incremented as parsing continues
	 * @param variables the variables to substitute
	 * @return true if the expression is true, and false otherwise
	 * @throws Exception a parsing error occurred
	 */
	private static Boolean matchValueEvaluation(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if(token.type == StringExpTokenType.NOT)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				return Boolean.valueOf(!testInside.booleanValue());
			}
		}
		else
		if (token.type == StringExpTokenType.OPENPAREN)
		{
			final Boolean testInside = matchValueEvaluation(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != StringExpTokenType.CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = index.clone();
		final StringExpToken leftValue = matchCombinedValue(tokens, i, variables);
		if (leftValue == null)
			return null;
		token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type != StringExpTokenType.EVALUATOR)
			return null;
		final StringExpToken rightValue = matchCombinedValue(tokens, i, variables);
		if (rightValue == null)
			return null;
		final int compare;
		if((leftValue.type==StringExpTokenType.STRCONST)||(rightValue.type==StringExpTokenType.STRCONST))
		{
			compare = leftValue.value.compareToIgnoreCase(rightValue.value);
		}
		else
		if((leftValue.type==StringExpTokenType.NUMCONST)||(rightValue.type==StringExpTokenType.NUMCONST))
		{
			compare = Double.valueOf(leftValue.numValue).compareTo(Double.valueOf(rightValue.numValue));
		}
		else
		if(token.value.equalsIgnoreCase("IN"))
		{
			compare = leftValue.value.compareToIgnoreCase(rightValue.value);
		}
		else
		{
			// the great assumption -- when both are unknown, assume they are numbers!
			compare = Double.valueOf(leftValue.value).compareTo(Double.valueOf(rightValue.value));
		}
		final Boolean result;
		switch(token.value.charAt(0))
		{
		case '>':
			if(token.value.length()==1)
				result = Boolean.valueOf(compare > 0);
			else
			switch(token.value.charAt(1))
			{
			case '=':
				result = Boolean.valueOf(compare >= 0);
				break;
			case '<':
				result = Boolean.valueOf(compare != 0);
				break;
			default:
				return null;
			}
			break;
		case '<':
			if(token.value.length()==1)
				result = Boolean.valueOf(compare < 0);
			else
			switch(token.value.charAt(1))
			{
			case '=':
				result = Boolean.valueOf(compare <= 0);
				break;
			case '>':
				result = Boolean.valueOf(compare != 0);
				break;
			default:
				return null;
			}
			break;
		case '=':
			if(token.value.length()==1)
				result = Boolean.valueOf(compare == 0);
			else
			switch(token.value.charAt(1))
			{
			case '=':
				result = Boolean.valueOf(compare == 0);
				break;
			case '<':
				result = Boolean.valueOf(compare <= 0);
				break;
			case '>':
				result = Boolean.valueOf(compare >= 0);
				break;
			default:
				return null;
			}
			break;
		case '!':
			if(token.value.length()==1)
				return null;
			else
			switch(token.value.charAt(1))
			{
			case '=':
				result = Boolean.valueOf(compare != 0);
				break;
			case '<':
				result = Boolean.valueOf(compare > 0);
				break;
			case '>':
				result = Boolean.valueOf(compare < 0);
				break;
			default:
				return null;
			}
			break;
		case 'I':
		case 'i':
			if (token.value.equalsIgnoreCase("IN"))
				result = Boolean.valueOf(rightValue.value.toUpperCase().indexOf(leftValue.value.toUpperCase())>=0);
			else
				return null;
			break;
		default:
			return null;
		}
		index[0] = i[0];
		return result;
	}

	/**
	 * Normalizes line endings in the given stringbuilder by replacing
	 * all carriage returns with only linefeeds.
	 *
	 * @param str the modified string
	 */
	public static void normalizeLineEndings(final StringBuffer str)
	{
		for(int i=0;i<str.length();i++)
		{
			// if contains even a single linefeed, destroy all crs
			if(str.charAt(i)=='\n')
			{
				for(i=str.length()-1;i>=0;i--)
				{
					if(str.charAt(i)=='\r')
						str.deleteCharAt(i);
				}
				return;
			}
		}
		// otherwise, convert all crs to lfs
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\r')
				str.setCharAt(i,'\n');
		}
	}


	/**
	 * Normalizes line endings in the given stringbuilder by replacing
	 * all linefeeds with linefeed + carriage return.
	 *
	 * @param str the modified string
	 */
	public static void dikufyLineEndings(final StringBuffer str)
	{
		for(int i=0;i<str.length();i++)
		{
			final char c = str.charAt(i);
			switch(c)
			{
			case '\n':
				if((i < str.length()-1)
				&&(str.charAt(i+1)=='\r')) // already normalized
					return;
				if(i == str.length()-1)
				{
					str.append('\r');
					return;
				}
				str.insert(++i, '\r');
				break;
			case '\r':
				if((i<str.length()-1)
				&&(str.charAt(i+1)=='\n'))
				{
					str.setCharAt(i, '\n');
					str.setCharAt(++i, '\r');
				}
				else
					str.insert(i++, '\n');
				break;
			}
		}
	}

	/**
	 * Converts various unicode look-alike or invalid characters
	 * to something more displayable.  Also does the normal conversion
	 * of coffeemud quotes to ` characters.
	 * @param str the stringbuffer to convert
	 */
	public static void normalizeCharacters(final StringBuffer str)
	{
		if(str == null)
			return;
		final char[] chars = str.toString().toCharArray();
		str.setLength(0);
		for(final char c : chars)
			switch(c)
			{
			case '\u2018': case '\u2019':
			case '\'':
				str.append('`');
				break;
			case '\u2212': case '\u2010':
			case '\u2013': case '\u2014':
			case '\u2015':
				str.append('-');
				break;
			case '\u200B': case '\u200C':
			case '\u200D': case '\uFFFD':
				break;
			case '\u00A0':
				str.append(' ');
				break;
			case '\u2026':
				str.append("...");
				break;
			case '\u201C': case '\u201D':
				str.append('"');
				break;
			default:
				str.append(c);
				break;
			}
	}

	/**
	 * Given a set of pre-parsed tokens from an Expression, this method evaluates the expression,
	 * begining with the token at the given index, which may be modified.  Variables are substituted
	 * at evaluation time.  Parsing errors throw an exception
	 * @see CMStrings#parseStringExpression(String, Map, boolean)
	 * @param tokens the expression tokens -- pre-parsed
	 * @param index the token list index, which may be incremented as parsing continues
	 * @param variables the variables to substitute
	 * @return true if the expression is true, and false otherwise
	 * @throws Exception a parsing error occurred
	 */
	private static Boolean matchExpression(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		Boolean leftExpression = null;
		if(token.type == StringExpTokenType.NOT)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				leftExpression = Boolean.valueOf(!testInside.booleanValue());
			}
		}
		else
		if (token.type == StringExpTokenType.OPENPAREN)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != StringExpTokenType.CLOSEPAREN)
					return null;
				index[0] = i[0];
				leftExpression = testInside;
			}
		}
		if(leftExpression == null)
		{
			i = index.clone();
			leftExpression = matchValueEvaluation(tokens, i, variables);
		}
		if (leftExpression == null)
			return null;
		final int[] i2 = i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != StringExpTokenType.WORD))
		{
			index[0] = i[0];
			return leftExpression;
		}
		i[0] = i2[0];
		final Boolean rightExpression = matchExpression(tokens, i, variables);
		if (rightExpression == null)
			return null;
		final Boolean result;
		switch(token.value.charAt(0))
		{
		case 'a':
		case 'A':
			if (token.value.equalsIgnoreCase("AND"))
				result = Boolean.valueOf(leftExpression.booleanValue() && rightExpression.booleanValue());
			else
				throw new Exception("Parse Exception: Illegal expression evaluation combiner: " + token.value);
			break;
		case '&':
			result = Boolean.valueOf(leftExpression.booleanValue() && rightExpression.booleanValue());
			break;
		case '|':
			result = Boolean.valueOf(leftExpression.booleanValue() || rightExpression.booleanValue());
			break;
		case 'O':
		case 'o':
			if (token.value.equalsIgnoreCase("OR"))
				result = Boolean.valueOf(leftExpression.booleanValue() || rightExpression.booleanValue());
			else
				throw new Exception("Parse Exception: Illegal expression evaluation combiner: " + token.value);
			break;
		case 'X':
		case 'x':
			if (token.value.equalsIgnoreCase("XOR"))
				result = Boolean.valueOf(leftExpression.booleanValue() != rightExpression.booleanValue());
			else
				throw new Exception("Parse Exception: Illegal expression evaluation combiner: " + token.value);
			break;
		default:
			throw new Exception("Parse Exception: Illegal expression evaluation combiner: " + token.value);
		}
		index[0] = i[0];
		return result;
	}

	/**
	 * Parses a string comparison expression and returns the result of the evaluation.
	 * Basic comparison operators include = &gt; &lt; &gt;= &lt;= != and IN.
	 * The IN operator is a substring check.
	 * Comparisons may be between string literals/variables, or numeric literals/variables.
	 * Numeric literals and variables may be combined using +, -, *, /, and ?, where ? is
	 * a special operator for random-number generation, which separates the lowest and highest
	 * number in a range, inclusive.
	 * Conjunctions include AND &amp; OR | and XOR. Parenthesis () may be used to group expressions.
	 * NOT is a valid prefix to negate an evaluation. All checks are always case-insensitive.
	 * Variables may be included, which are substituted at evaluation time. Variables are
	 * designated in the expression by prefix with $.  Variables are checked case-sensitive
	 * first, and then as uppercase.  If not found, an exception is thrown, unless the
	 * emptyVarsOk field was set to true. The value of variables should always be a String obj.
	 * @param expression the string expression to evaluate
	 * @param variables the map of variable names to String objects
	 * @param emptyVarsOK true if missing variables are "", and false otherwise.
	 * @return true if the expression evaluates to TRUE, and false otherwise
	 * @throws Exception an error occurred in parsing
	 */
	public final static boolean parseStringExpression(final String expression, final Map<String,Object> variables, final boolean emptyVarsOK) throws Exception
	{
		final Vector<StringExpToken> tokens = new Vector<StringExpToken>();
		int[] i = { 0 };
		StringExpToken token = nextStringToken(expression,i,variables, emptyVarsOK);
		while(token != null)
		{
			tokens.addElement(token);
			token = nextStringToken(expression,i,variables, emptyVarsOK);
		}
		if(tokens.size()==0)
			return true;
		i = new int[]{ 0 };
		final Boolean value = matchExpression(tokens, i, variables);
		return value.booleanValue();
	}

	/**
	 * Counts the number of times all of the strings in the second set appear in any
	 * of the strings in the first set.  The search is substring based, so a second
	 * set string may appear multiple times in a first set string.
	 * @param set the set of strings to search through
	 * @param things the set of strings to search FOR
	 * @return the number of times any of the things appears in any of the set strings
	 */
	public final static int countSubstrings(final String[] set, final String[] things)
	{
		if(set==null)
			return 0;
		if(things==null)
			return 0;
		int total=0;
		for(final String longString : set)
		{
			for(final String subString : things)
			{
				int x=0;
				while((x=longString.indexOf( subString, x ))>=x)
					total++;
			}
		}
		return total;
	}

	public final static boolean matches(final String str, final String mask, final boolean caseSensitive)
	{
		if(str==null)
			return false;
		if((mask==null)||(mask.length()==0))
			return true;
		final char[] cstr=caseSensitive?str.toCharArray():str.toLowerCase().toCharArray();
		final char[] mfix=caseSensitive?mask.toCharArray():mask.toLowerCase().toCharArray();
		// mstr needs 'fixing'
		final char[] macc=new char[mfix.length];
		int adex=0;
		for(int i=0;i<mfix.length;i++)
		{
			if((mfix[i]=='\\')
			&&(i<mfix.length))
				macc[adex++]=mfix[++i];
			else
			if(mfix[i]=='*')
				macc[adex++]=(char)0;
			else
			if(mfix[i]=='?')
				macc[adex++]=(char)1;
			else
				macc[adex++]=mfix[i];
		}
		final char[] mstr = Arrays.copyOf(macc, adex);

		int cpos = 0;
		int mpos = 0;
		int cend = cstr.length-1;
		int mend = mstr.length-1;
		while((mend>=mpos)||(cend>=cpos))
		{
			for(;mpos<=mend;mpos++)
			{
				final char c = mstr[mpos];
				if(c==(char)0)
					break;
				if(cpos>cend)
					return false;
				if((c!=(char)1)
				&&(c != cstr[cpos]))
					return false;
				cpos++;
			}
			for(;mend>=mpos;mend--)
			{
				final char c = mstr[mend];
				if(c==(char)0)
					break;
				if(cpos > cend)
					return false;
				if((c!=(char)1)
				&&(c != cstr[cend]))
					return false;
				cend--;
			}
			if(mend<mpos)
			{
				if(cpos>cend)
					return true;
				return false;
			}
			if(mend==mpos) // it must be a *
				return true;
			if(mend<mpos-1) // it must be **
				return true;
			while((mpos<=mend)&&(mstr[mpos]==(char)0))
				mpos++;
			if(mend<mpos)
				return true;
			while((mpos<=mend)&&(mstr[mend]==(char)0))
				mend--;
			if(mend<mpos)
				return true;
			char c=mstr[mpos];
			if(c!=(char)1)
				cpos = String.valueOf(cstr).indexOf(c,cpos);
			if((cpos<0)||(cpos>cend))
				return false;
			c=mstr[mend];
			if(c!=(char)1)
				cend = String.valueOf(cstr).lastIndexOf(c,cend);
			if((cend<0)||(cpos>cend))
				return false;
		}
		return true;
	}

	/**
	 * Counts the number of times the given character appears in the given string.
	 * @param str the string to search through
	 * @param c the character to count
	 * @return the number of times the character appears in the string
	 */
	public final static int countChars(final String str, final char c)
	{
		if(str==null)
			return 0;
		int total=0;
		int dex=str.indexOf(c);
		while(dex>=0)
		{
			total++;
			dex=str.indexOf(c,dex+1);
		}
		return total;
	}

	/**
	 * Returns the type of end-of-line used by the given charsequence.
	 * Considers \r, \n, \n\r, and \r\n as valid options.
	 * @param str the char sequence to check
	 * @return the first valid end-of-line found.
	 */
	public final static String determineEOLN(final CharSequence str)
	{
		if(str!=null)
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\n')
			{
				if((i<str.length()-1)&&(str.charAt(i+1)=='\r'))
					return "\n\r";
				return "\n";
			}
			else
			if(str.charAt(i)=='\r')
			{
				if((i<str.length()-1)&&(str.charAt(i+1)=='\n'))
					return "\r\n";
				return "\r";
			}
		}
		return ""+((char)0x0a);
	}

	/*
	 * Functions for diff, match and patch.
	 * Computes the difference between two texts to create a patch.
	 * Applies the patch onto another text, allowing for errors.
	 *
	 * @author fraser@google.com (Neil Fraser)
	 */

	/**
	 * Number of seconds to map a diff before giving up (0 for infinity).
	 */
	private static float DIFF_TIMEOUT = 1.0f;

	/**
	 * Internal class for returning results from diffLinesToChars().
	 * Other less paranoid languages just use a three-element array.
	 * @author fraser@google.com (Neil Fraser)
	 */
	private static class LinesToCharsResult
	{
		protected String chars1;
		protected String chars2;
		protected List<String> lineArray;

		protected LinesToCharsResult(final String chars1, final String chars2, final List<String> lineArray)
		{
			this.chars1 = chars1;
			this.chars2 = chars2;
			this.lineArray = lineArray;
		}
	}
	/**
	 * The data structure representing a diff is a Linked list of Diff objects:
	 * {Diff(Operation.DELETE, "Hello"), Diff(Operation.INSERT, "Goodbye"),
	 *	Diff(Operation.EQUAL, " world.")}
	 * which means: delete "Hello", add "Goodbye" and keep " world."
	 * @author fraser@google.com (Neil Fraser)
	 */
	public static enum DiffOperation {
		DELETE, INSERT, EQUAL
	}

	/**
	 * Find the differences between two texts.
	 * Run a faster, slightly less optimal diff.
	 * This method allows the 'checklines' of diffMain() to be optional.
	 * Most of the time checklines is wanted, so default to true.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @return Linked List of Diff objects.
	 */
	public static LinkedList<Diff> diffMain(final String text1, final String text2)
	{
		return diffMain(text1, text2, true);
	}

	/**
	 * Find the differences between two texts.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @param checklines Speedup flag.	If false, then don't run a
	 *		 line-level diff first to identify the changed areas.
	 *		 If true, then run a faster slightly less optimal diff.
	 * @return Linked List of Diff objects.
	 */
	public static LinkedList<Diff> diffMain(final String text1, final String text2, final boolean checklines)
	{
		// Set a deadline by which time the diff must be complete.
		long deadline;
		if (DIFF_TIMEOUT <= 0)
		{
			deadline = Long.MAX_VALUE;
		}
		else
		{
			deadline = System.currentTimeMillis() + (long) (DIFF_TIMEOUT * 1000);
		}
		return diffMain(text1, text2, checklines, deadline);
	}

	/**
	 * Find the differences between two texts.	Simplifies the problem by
	 * stripping any common prefix or suffix off the texts before diffing.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @param checklines Speedup flag.	If false, then don't run a
	 *		 line-level diff first to identify the changed areas.
	 *		 If true, then run a faster slightly less optimal diff.
	 * @param deadline Time when the diff should be complete by.	Used
	 *		 internally for recursive calls.	Users should set DiffTimeout instead.
	 * @return Linked List of Diff objects.
	 */
	private static LinkedList<Diff> diffMain(String text1, String text2, final boolean checklines, final long deadline)
	{
		// Check for null inputs.
		if (text1 == null || text2 == null)
		{
			throw new IllegalArgumentException("Null inputs. (diffMain)");
		}

		// Check for equality (speedup).
		LinkedList<Diff> diffs;
		if (text1.equals(text2))
		{
			diffs = new LinkedList<Diff>();
			if (text1.length() != 0)
			{
				diffs.add(new Diff(DiffOperation.EQUAL, text1));
			}
			return diffs;
		}

		// Trim off common prefix (speedup).
		int commonlength = diffCommonPrefix(text1, text2);
		final String commonprefix = text1.substring(0, commonlength);
		text1 = text1.substring(commonlength);
		text2 = text2.substring(commonlength);

		// Trim off common suffix (speedup).
		commonlength = diffCommonSuffix(text1, text2);
		final String commonsuffix = text1.substring(text1.length() - commonlength);
		text1 = text1.substring(0, text1.length() - commonlength);
		text2 = text2.substring(0, text2.length() - commonlength);

		// Compute the diff on the middle block.
		diffs = diffCompute(text1, text2, checklines, deadline);

		// Restore the prefix and suffix.
		if (commonprefix.length() != 0)
		{
			diffs.addFirst(new Diff(DiffOperation.EQUAL, commonprefix));
		}
		if (commonsuffix.length() != 0)
		{
			diffs.addLast(new Diff(DiffOperation.EQUAL, commonsuffix));
		}

		diffCleanupMerge(diffs);
		return diffs;
	}

	/**
	 * Find the differences between two texts.	Assumes that the texts do not
	 * have any common prefix or suffix.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @param checklines Speedup flag.	If false, then don't run a
	 *		 line-level diff first to identify the changed areas.
	 *		 If true, then run a faster slightly less optimal diff.
	 * @param deadline Time when the diff should be complete by.
	 * @return Linked List of Diff objects.
	 */
	private static LinkedList<Diff> diffCompute(final String text1, final String text2, final boolean checklines, final long deadline)
	{
		LinkedList<Diff> diffs = new LinkedList<Diff>();

		if (text1.length() == 0)
		{
			// Just add some text (speedup).
			diffs.add(new Diff(DiffOperation.INSERT, text2));
			return diffs;
		}

		if (text2.length() == 0)
		{
			// Just delete some text (speedup).
			diffs.add(new Diff(DiffOperation.DELETE, text1));
			return diffs;
		}

		final String longtext = text1.length() > text2.length() ? text1 : text2;
		final String shorttext = text1.length() > text2.length() ? text2 : text1;
		final int i = longtext.indexOf(shorttext);
		if (i != -1)
		{
			// Shorter text is inside the longer text (speedup).
			final DiffOperation op = (text1.length() > text2.length()) ? DiffOperation.DELETE : DiffOperation.INSERT;
			diffs.add(new Diff(op, longtext.substring(0, i)));
			diffs.add(new Diff(DiffOperation.EQUAL, shorttext));
			diffs.add(new Diff(op, longtext.substring(i + shorttext.length())));
			return diffs;
		}

		if (shorttext.length() == 1)
		{
			// Single character string.
			// After the previous speedup, the character can't be an equality.
			diffs.add(new Diff(DiffOperation.DELETE, text1));
			diffs.add(new Diff(DiffOperation.INSERT, text2));
			return diffs;
		}

		// Check to see if the problem can be split in two.
		final String[] hm = diffHalfMatch(text1, text2);
		if (hm != null)
		{
			// A half-match was found, sort out the return data.
			final String text1a = hm[0];
			final String text1b = hm[1];
			final String text2a = hm[2];
			final String text2b = hm[3];
			final String midcommon = hm[4];
			// Send both pairs off for separate processing.
			final LinkedList<Diff> diffsa = diffMain(text1a, text2a, checklines, deadline);
			final LinkedList<Diff> diffsb = diffMain(text1b, text2b, checklines, deadline);
			// Merge the results.
			diffs = diffsa;
			diffs.add(new Diff(DiffOperation.EQUAL, midcommon));
			diffs.addAll(diffsb);
			return diffs;
		}

		if (checklines && text1.length() > 100 && text2.length() > 100)
		{
			return diffLineMode(text1, text2, deadline);
		}

		return diffBisect(text1, text2, deadline);
	}

	/**
	 * Do a quick line-level diff on both strings, then rediff the parts for
	 * greater accuracy.
	 * This speedup can produce non-minimal diffs.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @param deadline Time when the diff should be complete by.
	 * @return Linked List of Diff objects.
	 */
	private static LinkedList<Diff> diffLineMode(String text1, String text2, final long deadline)
	{
		// Scan the text on a line-by-line basis first.
		final LinesToCharsResult b = diffLinesToChars(text1, text2);
		text1 = b.chars1;
		text2 = b.chars2;
		final List<String> linearray = b.lineArray;

		final LinkedList<Diff> diffs = diffMain(text1, text2, false, deadline);

		// Convert the diff back to original text.
		diffCharsToLines(diffs, linearray);
		// Eliminate freak matches (e.g. blank lines)
		diffCleanupSemantic(diffs);

		// Rediff any replacement blocks, this time character-by-character.
		// Add a dummy entry at the end.
		diffs.add(new Diff(DiffOperation.EQUAL, ""));
		int countdelete = 0;
		int countinsert = 0;
		String textdelete = "";
		String textinsert = "";
		final ListIterator<Diff> pointer = diffs.listIterator();
		Diff thisDiff = pointer.next();
		while (thisDiff != null)
		{
			switch (thisDiff.operation)
			{
			case INSERT:
				countinsert++;
				textinsert += thisDiff.text;
				break;
			case DELETE:
				countdelete++;
				textdelete += thisDiff.text;
				break;
			case EQUAL:
				// Upon reaching an equality, check for prior redundancies.
				if (countdelete >= 1 && countinsert >= 1)
				{
					// Delete the offending records and add the merged ones.
					pointer.previous();
					for (int j = 0; j < countdelete + countinsert; j++)
					{
						pointer.previous();
						pointer.remove();
					}
					for (final Diff newDiff : diffMain(textdelete, textinsert, false, deadline))
					{
						pointer.add(newDiff);
					}
				}
				countinsert = 0;
				countdelete = 0;
				textdelete = "";
				textinsert = "";
				break;
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}
		diffs.removeLast();	// Remove the dummy entry at the end.

		return diffs;
	}

	/**
	 * Find the 'middle snake' of a diff, split the problem in two
	 * and return the recursively constructed diff.
	 * See Myers 1986 paper: An O(ND) Difference Algorithm and Its Variations.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @param deadline Time at which to bail if not yet complete.
	 * @return LinkedList of Diff objects.
	 */
	private static LinkedList<Diff> diffBisect(final String text1, final String text2, final long deadline)
	{
		// Cache the text lengths to prevent multiple calls.
		final int text1length = text1.length();
		final int text2length = text2.length();
		final int maxd = (text1length + text2length + 1) / 2;
		final int voffset = maxd;
		final int vlength = 2 * maxd;
		final int[] v1 = new int[vlength];
		final int[] v2 = new int[vlength];
		for (int x = 0; x < vlength; x++)
		{
			v1[x] = -1;
			v2[x] = -1;
		}
		v1[voffset + 1] = 0;
		v2[voffset + 1] = 0;
		final int delta = text1length - text2length;
		// If the total number of characters is odd, then the front path will
		// collide with the reverse path.
		final boolean front = (delta % 2 != 0);
		// Offsets for start and end of k loop.
		// Prevents mapping of space beyond the grid.
		int k1start = 0;
		int k1end = 0;
		int k2start = 0;
		int k2end = 0;
		for (int d = 0; d < maxd; d++)
		{
			// Bail out if deadline is reached.
			if (System.currentTimeMillis() > deadline)
			{
				break;
			}

			// Walk the front path one step.
			for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2)
			{
				final int k1offset = voffset + k1;
				int x1;
				if (k1 == -d || (k1 != d && v1[k1offset - 1] < v1[k1offset + 1]))
				{
					x1 = v1[k1offset + 1];
				}
				else
				{
					x1 = v1[k1offset - 1] + 1;
				}
				int y1 = x1 - k1;
				while (x1 < text1length && y1 < text2length
				&& text1.charAt(x1) == text2.charAt(y1))
				{
					x1++;
					y1++;
				}
				v1[k1offset] = x1;
				if (x1 > text1length)
				{
					// Ran off the right of the graph.
					k1end += 2;
				}
				else
				if (y1 > text2length)
				{
					// Ran off the bottom of the graph.
					k1start += 2;
				}
				else
				if (front)
				{
					final int k2offset = voffset + delta - k1;
					if (k2offset >= 0 && k2offset < vlength && v2[k2offset] != -1)
					{
						// Mirror x2 onto top-left coordinate system.
						final int x2 = text1length - v2[k2offset];
						if (x1 >= x2)
						{
							// Overlap detected.
							return diffBisectSplit(text1, text2, x1, y1, deadline);
						}
					}
				}
			}

			// Walk the reverse path one step.
			for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2)
			{
				final int k2offset = voffset + k2;
				int x2;
				if (k2 == -d || (k2 != d && v2[k2offset - 1] < v2[k2offset + 1]))
				{
					x2 = v2[k2offset + 1];
				}
				else
				{
					x2 = v2[k2offset - 1] + 1;
				}
				int y2 = x2 - k2;
				while (x2 < text1length && y2 < text2length
				&& text1.charAt(text1length - x2 - 1) == text2.charAt(text2length - y2 - 1))
				{
					x2++;
					y2++;
				}
				v2[k2offset] = x2;
				if (x2 > text1length)
				{
					// Ran off the left of the graph.
					k2end += 2;
				}
				else
				if (y2 > text2length)
				{
					// Ran off the top of the graph.
					k2start += 2;
				}
				else
				if (!front)
				{
					final int k1offset = voffset + delta - k2;
					if (k1offset >= 0 && k1offset < vlength && v1[k1offset] != -1)
					{
						final int x1 = v1[k1offset];
						final int y1 = voffset + x1 - k1offset;
						// Mirror x2 onto top-left coordinate system.
						x2 = text1length - x2;
						if (x1 >= x2)
						{
							// Overlap detected.
							return diffBisectSplit(text1, text2, x1, y1, deadline);
						}
					}
				}
			}
		}
		// Diff took too long and hit the deadline or
		// number of diffs equals number of characters, no commonality at all.
		final LinkedList<Diff> diffs = new LinkedList<Diff>();
		diffs.add(new Diff(DiffOperation.DELETE, text1));
		diffs.add(new Diff(DiffOperation.INSERT, text2));
		return diffs;
	}

	/**
	 * Given the location of the 'middle snake', split the diff in two parts
	 * and recurse.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @param x Index of split point in text1.
	 * @param y Index of split point in text2.
	 * @param deadline Time at which to bail if not yet complete.
	 * @return LinkedList of Diff objects.
	 */
	private static LinkedList<Diff> diffBisectSplit(final String text1, final String text2, final int x, final int y, final long deadline)
	{
		final String text1a = text1.substring(0, x);
		final String text2a = text2.substring(0, y);
		final String text1b = text1.substring(x);
		final String text2b = text2.substring(y);

		// Compute both diffs serially.
		final LinkedList<Diff> diffs = diffMain(text1a, text2a, false, deadline);
		final LinkedList<Diff> diffsb = diffMain(text1b, text2b, false, deadline);

		diffs.addAll(diffsb);
		return diffs;
	}

	/**
	 * Split two texts into a list of strings.	Reduce the texts to a string of
	 * hashes where each Unicode character represents one line.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return An object containing the encoded text1, the encoded text2 and
	 *		 the List of unique strings.	The zeroth element of the List of
	 *		 unique strings is intentionally blank.
	 */
	private static LinesToCharsResult diffLinesToChars(final String text1, final String text2)
	{
		final List<String> lineArray = new ArrayList<String>();
		final Map<String, Integer> lineHash = new HashMap<String, Integer>();
		// e.g. linearray[4] == "Hello\n"
		// e.g. linehash.get("Hello\n") == 4

		// "\x00" is a valid character, but various debuggers don't like it.
		// So we'll insert a junk entry to avoid generating a null character.
		lineArray.add("");

		final String chars1 = diffLinesToCharsMunge(text1, lineArray, lineHash);
		final String chars2 = diffLinesToCharsMunge(text2, lineArray, lineHash);
		return new LinesToCharsResult(chars1, chars2, lineArray);
	}

	/**
	 * Split a text into a list of strings.	Reduce the texts to a string of
	 * hashes where each Unicode character represents one line.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text String to encode.
	 * @param lineArray List of unique strings.
	 * @param lineHash Map of strings to indices.
	 * @return Encoded string.
	 */
	private static String diffLinesToCharsMunge(final String text, final List<String> lineArray, final Map<String, Integer> lineHash)
	{
		int lineStart = 0;
		int lineEnd = -1;
		String line;
		final StringBuilder chars = new StringBuilder();
		// Walk the text, pulling out a substring for each line.
		// text.split('\n') would would temporarily double our memory footprint.
		// Modifying text would create many large strings to garbage collect.
		while (lineEnd < text.length() - 1)
		{
			lineEnd = text.indexOf('\n', lineStart);
			if (lineEnd == -1)
			{
				lineEnd = text.length() - 1;
			}
			line = text.substring(lineStart, lineEnd + 1);
			lineStart = lineEnd + 1;

			if (lineHash.containsKey(line))
			{
				chars.append(String.valueOf((char) lineHash.get(line).intValue()));
			}
			else
			{
				lineArray.add(line);
				lineHash.put(line, Integer.valueOf(lineArray.size() - 1));
				chars.append(String.valueOf((char) (lineArray.size() - 1)));
			}
		}
		return chars.toString();
	}

	/**
	 * Rehydrate the text in a diff from a string of line hashes to real lines of
	 * text.
	 * @author fraser@google.com (Neil Fraser)
	 * @param diffs LinkedList of Diff objects.
	 * @param lineArray List of unique strings.
	 */
	private static void diffCharsToLines(final LinkedList<Diff> diffs, final List<String> lineArray)
	{
		StringBuilder text;
		for (final Diff diff : diffs)
		{
			text = new StringBuilder();
			for (int y = 0; y < diff.text.length(); y++)
			{
				text.append(lineArray.get(diff.text.charAt(y)));
			}
			diff.text = text.toString();
		}
	}

	/**
	 * Determine the common prefix of two strings
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return The number of characters common to the start of each string.
	 */
	private static int diffCommonPrefix(final String text1, final String text2)
	{
		// Performance analysis: http://neil.fraser.name/news/2007/10/09/
		final int n = Math.min(text1.length(), text2.length());
		for (int i = 0; i < n; i++)
		{
			if (text1.charAt(i) != text2.charAt(i))
			{
				return i;
			}
		}
		return n;
	}

	/**
	 * Determine the common suffix of two strings
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return The number of characters common to the end of each string.
	 */
	private static int diffCommonSuffix(final String text1, final String text2)
	{
		// Performance analysis: http://neil.fraser.name/news/2007/10/09/
		final int text1length = text1.length();
		final int text2length = text2.length();
		final int n = Math.min(text1length, text2length);
		for (int i = 1; i <= n; i++)
		{
			if (text1.charAt(text1length - i) != text2.charAt(text2length - i))
			{
				return i - 1;
			}
		}
		return n;
	}

	/**
	 * Determine if the suffix of one string is the prefix of another.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return The number of characters common to the end of the first
	 *		 string and the start of the second string.
	 */
	private static int diffCommonOverlap(String text1, String text2)
	{
		// Cache the text lengths to prevent multiple calls.
		final int text1length = text1.length();
		final int text2length = text2.length();
		// Eliminate the null case.
		if (text1length == 0 || text2length == 0)
		{
			return 0;
		}
		// Truncate the longer string.
		if (text1length > text2length)
		{
			text1 = text1.substring(text1length - text2length);
		}
		else
		if (text1length < text2length)
		{
			text2 = text2.substring(0, text1length);
		}
		final int textlength = Math.min(text1length, text2length);
		// Quick check for the worst case.
		if (text1.equals(text2))
		{
			return textlength;
		}

		// Start by looking for a single character match
		// and increase length until no match is found.
		// Performance analysis: http://neil.fraser.name/news/2010/11/04/
		int best = 0;
		int length = 1;
		while (true)
		{
			final String pattern = text1.substring(textlength - length);
			final int found = text2.indexOf(pattern);
			if (found == -1)
			{
				return best;
			}
			length += found;
			if (found == 0 || text1.substring(textlength - length).equals(text2.substring(0, length)))
			{
				best = length;
				length++;
			}
		}
	}

	/**
	 * Do the two texts share a substring which is at least half the length of
	 * the longer text?
	 * This speedup can produce non-minimal diffs.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return Five element String array, containing the prefix of text1, the
	 *		 suffix of text1, the prefix of text2, the suffix of text2 and the
	 *		 common middle.	Or null if there was no match.
	 */
	private static String[] diffHalfMatch(final String text1, final String text2)
	{
		if (DIFF_TIMEOUT <= 0)
		{
			// Don't risk returning a non-optimal diff if we have unlimited time.
			return null;
		}
		final String longtext = text1.length() > text2.length() ? text1 : text2;
		final String shorttext = text1.length() > text2.length() ? text2 : text1;
		if (longtext.length() < 4 || shorttext.length() * 2 < longtext.length())
		{
			return null;	// Pointless.
		}

		// First check if the second quarter is the seed for a half-match.
		final String[] hm1 = diffHalfMatchI(longtext, shorttext, (longtext.length() + 3) / 4);
		// Check again based on the third quarter.
		final String[] hm2 = diffHalfMatchI(longtext, shorttext, (longtext.length() + 1) / 2);
		String[] hm;
		if (hm1 == null && hm2 == null)
		{
			return null;
		}
		else
		if (hm2 == null)
		{
			hm = hm1;
		}
		else
		if (hm1 == null)
		{
			hm = hm2;
		}
		else
		{
			// Both matched.	Select the longest.
			hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
		}

		// A half-match was found, sort out the return data.
		if (text1.length() > text2.length())
		{
			return hm;
			//return new String[]{hm[0], hm[1], hm[2], hm[3], hm[4]};
		}
		else
		if(hm!=null)
		{
			return new String[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
		}
		else
		{
			return new String[]{};
		}
	}

	/**f
	 * Does a substring of shorttext exist within longtext such that the
	 * substring is at least half the length of longtext?
	 * @author fraser@google.com (Neil Fraser)
	 * @param longtext Longer string.
	 * @param shorttext Shorter string.
	 * @param i Start index of quarter length substring within longtext.
	 * @return Five element String array, containing the prefix of longtext, the
	 *		 suffix of longtext, the prefix of shorttext, the suffix of shorttext
	 *		 and the common middle.	Or null if there was no match.
	 */
	private static String[] diffHalfMatchI(final String longtext, final String shorttext, final int i)
	{
		// Start with a 1/4 length substring at position i as a seed.
		final String seed = longtext.substring(i, i + longtext.length() / 4);
		int j = -1;
		String bestcommon = "";
		String bestlongtexta = "", bestlongtextb = "";
		String bestshorttexta = "", bestshorttextb = "";
		while ((j = shorttext.indexOf(seed, j + 1)) != -1)
		{
			final int prefixLength = diffCommonPrefix(longtext.substring(i), shorttext.substring(j));
			final int suffixLength = diffCommonSuffix(longtext.substring(0, i), shorttext.substring(0, j));
			if (bestcommon.length() < suffixLength + prefixLength)
			{
				bestcommon = shorttext.substring(j - suffixLength, j) + shorttext.substring(j, j + prefixLength);
				bestlongtexta = longtext.substring(0, i - suffixLength);
				bestlongtextb = longtext.substring(i + prefixLength);
				bestshorttexta = shorttext.substring(0, j - suffixLength);
				bestshorttextb = shorttext.substring(j + prefixLength);
			}
		}
		if (bestcommon.length() * 2 >= longtext.length())
		{
			return new String[]{bestlongtexta, bestlongtextb, bestshorttexta, bestshorttextb, bestcommon};
		}
		else
		{
			return null;
		}
	}

	/**
	 * Reduce the number of edits by eliminating semantically trivial equalities.
	 * @author fraser@google.com (Neil Fraser)
	 * @param diffs LinkedList of Diff objects.
	 */
	private static void diffCleanupSemantic(final LinkedList<Diff> diffs)
	{
		if (diffs.isEmpty())
		{
			return;
		}
		boolean changes = false;
		final Stack<Diff> equalities = new Stack<Diff>();	// Stack of qualities.
		String lastequality = null; // Always equal to equalities.lastElement().text
		ListIterator<Diff> pointer = diffs.listIterator();
		// Number of characters that changed prior to the equality.
		int lengthinsertions1 = 0;
		int lengthdeletions1 = 0;
		// Number of characters that changed after the equality.
		int lengthinsertions2 = 0;
		int lengthdeletions2 = 0;
		Diff thisDiff = pointer.next();
		while (thisDiff != null)
		{
			if (thisDiff.operation == DiffOperation.EQUAL)
			{
				// Equality found.
				equalities.push(thisDiff);
				lengthinsertions1 = lengthinsertions2;
				lengthdeletions1 = lengthdeletions2;
				lengthinsertions2 = 0;
				lengthdeletions2 = 0;
				lastequality = thisDiff.text;
			}
			else
			{
				// An insertion or deletion.
				if (thisDiff.operation == DiffOperation.INSERT)
				{
					lengthinsertions2 += thisDiff.text.length();
				}
				else
				{
					lengthdeletions2 += thisDiff.text.length();
				}
				// Eliminate an equality that is smaller or equal to the edits on both
				// sides of it.
				if (lastequality != null && (lastequality.length() <= Math.max(lengthinsertions1, lengthdeletions1))
				&& (lastequality.length() <= Math.max(lengthinsertions2, lengthdeletions2)))
				{
					// Walk back to offending equality.
					while (thisDiff != equalities.lastElement())
					{
						thisDiff = pointer.previous();
					}
					pointer.next();

					// Replace equality with a delete.
					pointer.set(new Diff(DiffOperation.DELETE, lastequality));
					// Insert a corresponding an insert.
					pointer.add(new Diff(DiffOperation.INSERT, lastequality));

					equalities.pop();	// Throw away the equality we just deleted.
					if (!equalities.empty())
					{
						// Throw away the previous equality (it needs to be reevaluated).
						equalities.pop();
					}
					if (equalities.empty())
					{
						// There are no previous equalities, walk back to the start.
						while (pointer.hasPrevious())
						{
							pointer.previous();
						}
					}
					else
					{
						// There is a safe equality we can fall back to.
						thisDiff = equalities.lastElement();
						while (thisDiff != pointer.previous())
						{
							// Intentionally empty loop.
						}
					}

					lengthinsertions1 = 0;	// Reset the counters.
					lengthinsertions2 = 0;
					lengthdeletions1 = 0;
					lengthdeletions2 = 0;
					lastequality = null;
					changes = true;
				}
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}

		// Normalize the diff.
		if (changes)
		{
			diffCleanupMerge(diffs);
		}
		diffCleanupSemantiCLossless(diffs);

		// Find any overlaps between deletions and insertions.
		// e.g: <del>abcxxx</del><ins>xxxdef</ins>
		//	 -> <del>abc</del>xxx<ins>def</ins>
		// e.g: <del>xxxabc</del><ins>defxxx</ins>
		//	 -> <ins>def</ins>xxx<del>abc</del>
		// Only extract an overlap if it is as big as the edit ahead or behind it.
		pointer = diffs.listIterator();
		Diff prevDiff = null;
		//thisDiff = null; // BZ eclipse said this was redundant?!
		if (pointer.hasNext())
		{
			prevDiff = pointer.next();
			if (pointer.hasNext())
			{
				thisDiff = pointer.next();
			}
		}
		while (thisDiff != null)
		{
			if ((prevDiff!=null)
			&&(prevDiff.operation == DiffOperation.DELETE)
			&&(thisDiff.operation == DiffOperation.INSERT))
			{
				final String deletion = prevDiff.text;
				final String insertion = thisDiff.text;
				final int overlaplength1 = diffCommonOverlap(deletion, insertion);
				final int overlaplength2 = diffCommonOverlap(insertion, deletion);
				if (overlaplength1 >= overlaplength2)
				{
					if (overlaplength1 >= deletion.length() / 2.0 || overlaplength1 >= insertion.length() / 2.0)
					{
						// Overlap found. Insert an equality and trim the surrounding edits.
						pointer.previous();
						pointer.add(new Diff(DiffOperation.EQUAL, insertion.substring(0, overlaplength1)));
						prevDiff.text = deletion.substring(0, deletion.length() - overlaplength1);
						thisDiff.text = insertion.substring(overlaplength1);
						// pointer.add inserts the element before the cursor, so there is
						// no need to step past the new element.
					}
				}
				else
				{
					if (overlaplength2 >= deletion.length() / 2.0 || overlaplength2 >= insertion.length() / 2.0)
					{
						// Reverse overlap found.
						// Insert an equality and swap and trim the surrounding edits.
						pointer.previous();
						pointer.add(new Diff(DiffOperation.EQUAL, deletion.substring(0, overlaplength2)));
						prevDiff.operation = DiffOperation.INSERT;
						prevDiff.text = insertion.substring(0, insertion.length() - overlaplength2);
						thisDiff.operation = DiffOperation.DELETE;
						thisDiff.text = deletion.substring(overlaplength2);
						// pointer.add inserts the element before the cursor, so there is
						// no need to step past the new element.
					}
				}
				thisDiff = pointer.hasNext() ? pointer.next() : null;
			}
			prevDiff = thisDiff;
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}
	}

	/**
	 * Look for single edits surrounded on both sides by equalities
	 * which can be shifted sideways to align the edit to a word boundary.
	 * e.g: The c at c ame. to The cat came.
	 * @author fraser@google.com (Neil Fraser)
	 * @param diffs LinkedList of Diff objects.
	 */
	private static void diffCleanupSemantiCLossless(final LinkedList<Diff> diffs)
	{
		String equality1, edit, equality2;
		String commonString;
		int commonOffset;
		int score, bestScore;
		String bestEquality1, bestEdit, bestEquality2;
		// Create a new iterator at the start.
		final ListIterator<Diff> pointer = diffs.listIterator();
		Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
		Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
		Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
		// Intentionally ignore the first and last element (don't need checking).
		while (nextDiff != null)
		{
			if ((prevDiff!=null)&&(thisDiff!=null)&&(prevDiff.operation == DiffOperation.EQUAL && nextDiff.operation == DiffOperation.EQUAL))
			{
				// This is a single edit surrounded by equalities.
				equality1 = prevDiff.text;
				edit = thisDiff.text;
				equality2 = nextDiff.text;

				// First, shift the edit as far left as possible.
				commonOffset = diffCommonSuffix(equality1, edit);
				if (commonOffset != 0)
				{
					commonString = edit.substring(edit.length() - commonOffset);
					equality1 = equality1.substring(0, equality1.length() - commonOffset);
					edit = commonString + edit.substring(0, edit.length() - commonOffset);
					equality2 = commonString + equality2;
				}

				// Second, step character by character right, looking for the best fit.
				bestEquality1 = equality1;
				bestEdit = edit;
				bestEquality2 = equality2;
				bestScore = diffCleanupSemanticScore(equality1, edit) + diffCleanupSemanticScore(edit, equality2);
				while (edit.length() != 0 && equality2.length() != 0
				&& edit.charAt(0) == equality2.charAt(0))
				{
					equality1 += edit.charAt(0);
					edit = edit.substring(1) + equality2.charAt(0);
					equality2 = equality2.substring(1);
					score = diffCleanupSemanticScore(equality1, edit) + diffCleanupSemanticScore(edit, equality2);
					// The >= encourages trailing rather than leading whitespace on edits.
					if (score >= bestScore)
					{
						bestScore = score;
						bestEquality1 = equality1;
						bestEdit = edit;
						bestEquality2 = equality2;
					}
				}

				if (!prevDiff.text.equals(bestEquality1))
				{
					// We have an improvement, save it back to the diff.
					if (bestEquality1.length() != 0)
					{
						prevDiff.text = bestEquality1;
					}
					else
					{
						pointer.previous(); // Walk past nextDiff.
						pointer.previous(); // Walk past thisDiff.
						pointer.previous(); // Walk past prevDiff.
						pointer.remove(); // Delete prevDiff.
						pointer.next(); // Walk past thisDiff.
						pointer.next(); // Walk past nextDiff.
					}
					thisDiff.text = bestEdit;
					if (bestEquality2.length() != 0)
					{
						nextDiff.text = bestEquality2;
					}
					else
					{
						pointer.remove(); // Delete nextDiff.
						nextDiff = thisDiff;
						thisDiff = prevDiff;
					}
				}
			}
			prevDiff = thisDiff;
			thisDiff = nextDiff;
			nextDiff = pointer.hasNext() ? pointer.next() : null;
		}
	}

	/**
	 * Given two strings, compute a score representing whether the internal
	 * boundary falls on logical boundaries.
	 * Scores range from 6 (best) to 0 (worst).
	 * @author fraser@google.com (Neil Fraser)
	 * @param one First string.
	 * @param two Second string.
	 * @return The score.
	 */
	private static int diffCleanupSemanticScore(final String one, final String two)
	{
		if (one.length() == 0 || two.length() == 0)
		{
			// Edges are the best.
			return 6;
		}

		// Each port of this function behaves slightly differently due to
		// subtle differences in each language's definition of things like
		// 'whitespace'.	Since this function's purpose is largely cosmetic,
		// the choice has been made to use each language's native features
		// rather than force total conformity.
		final char char1 = one.charAt(one.length() - 1);
		final char char2 = two.charAt(0);
		final boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
		final boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
		final boolean whitespace1 = nonAlphaNumeric1 && Character.isWhitespace(char1);
		final boolean whitespace2 = nonAlphaNumeric2 && Character.isWhitespace(char2);
		final boolean lineBreak1 = whitespace1 && Character.getType(char1) == Character.CONTROL;
		final boolean lineBreak2 = whitespace2 && Character.getType(char2) == Character.CONTROL;
		final boolean blankLine1 = lineBreak1 && BLANKLINEEND.matcher(one).find();
		final boolean blankLine2 = lineBreak2 && BLANKLINESTART.matcher(two).find();

		if (blankLine1 || blankLine2)
		{
			// Five points for blank lines.
			return 5;
		}
		else
		if (lineBreak1 || lineBreak2)
		{
			// Four points for line breaks.
			return 4;
		}
		else
		if (nonAlphaNumeric1 && !whitespace1 && whitespace2)
		{
			// Three points for end of sentences.
			return 3;
		}
		else
		if (whitespace1 || whitespace2)
		{
			// Two points for whitespace.
			return 2;
		}
		else
		if (nonAlphaNumeric1 || nonAlphaNumeric2)
		{
			// One point for non-alphanumeric.
			return 1;
		}
		return 0;
	}

	// Define some regex patterns for matching boundaries.
	private static Pattern BLANKLINEEND = Pattern.compile("\\n\\r?\\n\\Z", Pattern.DOTALL);
	private static Pattern BLANKLINESTART = Pattern.compile("\\A\\r?\\n\\r?\\n", Pattern.DOTALL);

	/**
	 * Reorder and merge like edit sections.	Merge equalities.
	 * Any edit section can move as long as it doesn't cross an equality.
	 * @param diffs LinkedList of Diff objects.
	 */
	private static void diffCleanupMerge(final LinkedList<Diff> diffs)
	{
		diffs.add(new Diff(DiffOperation.EQUAL, ""));	// Add a dummy entry at the end.
		ListIterator<Diff> pointer = diffs.listIterator();
		int countdelete = 0;
		int countinsert = 0;
		String textdelete = "";
		String textinsert = "";
		Diff thisDiff = pointer.next();
		Diff prevEqual = null;
		int commonlength;
		while (thisDiff != null)
		{
			switch (thisDiff.operation)
			{
			case INSERT:
				countinsert++;
				textinsert += thisDiff.text;
				prevEqual = null;
				break;
			case DELETE:
				countdelete++;
				textdelete += thisDiff.text;
				prevEqual = null;
				break;
			case EQUAL:
				if (countdelete + countinsert > 1)
				{
					final boolean bothtypes = countdelete != 0 && countinsert != 0;
					// Delete the offending records.
					pointer.previous();	// Reverse direction.
					while (countdelete-- > 0)
					{
						pointer.previous();
						pointer.remove();
					}
					while (countinsert-- > 0)
					{
						pointer.previous();
						pointer.remove();
					}
					if (bothtypes)
					{
						// Factor out any common prefixies.
						commonlength = diffCommonPrefix(textinsert, textdelete);
						if (commonlength != 0)
						{
							if (pointer.hasPrevious())
							{
								thisDiff = pointer.previous();
								assert thisDiff.operation == DiffOperation.EQUAL : "Previous diff should have been an equality.";
								thisDiff.text += textinsert.substring(0, commonlength);
								pointer.next();
							}
							else
							{
								pointer.add(new Diff(DiffOperation.EQUAL, textinsert.substring(0, commonlength)));
							}
							textinsert = textinsert.substring(commonlength);
							textdelete = textdelete.substring(commonlength);
						}
						// Factor out any common suffixies.
						commonlength = diffCommonSuffix(textinsert, textdelete);
						if (commonlength != 0)
						{
							thisDiff = pointer.next();
							thisDiff.text = textinsert.substring(textinsert.length() - commonlength) + thisDiff.text;
							textinsert = textinsert.substring(0, textinsert.length() - commonlength);
							textdelete = textdelete.substring(0, textdelete.length() - commonlength);
							pointer.previous();
						}
					}
					// Insert the merged records.
					if (textdelete.length() != 0)
					{
						pointer.add(new Diff(DiffOperation.DELETE, textdelete));
					}
					if (textinsert.length() != 0)
					{
						pointer.add(new Diff(DiffOperation.INSERT, textinsert));
					}
					// Step forward to the equality.
					thisDiff = pointer.hasNext() ? pointer.next() : null;
				}
				else
				if (prevEqual != null)
				{
					// Merge this equality with the previous one.
					prevEqual.text += thisDiff.text;
					pointer.remove();
					thisDiff = pointer.previous();
					pointer.next();	// Forward direction
				}
				countinsert = 0;
				countdelete = 0;
				textdelete = "";
				textinsert = "";
				prevEqual = thisDiff;
				break;
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}
		if (diffs.getLast().text.length() == 0)
		{
			diffs.removeLast();	// Remove the dummy entry at the end.
		}

		/*
		 * Second pass: look for single edits surrounded on both sides by equalities
		 * which can be shifted sideways to eliminate an equality.
		 * e.g: A<ins>BA</ins>C to <ins>AB</ins>AC
		 */
		boolean changes = false;
		// Create a new iterator at the start.
		// (As opposed to walking the current one back.)
		pointer = diffs.listIterator();
		Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
		thisDiff = pointer.hasNext() ? pointer.next() : null;
		Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
		// Intentionally ignore the first and last element (don't need checking).
		while (nextDiff != null)
		{
			if ((prevDiff!=null)&&(thisDiff!=null)&&(prevDiff.operation == DiffOperation.EQUAL && nextDiff.operation == DiffOperation.EQUAL))
			{
				// This is a single edit surrounded by equalities.
				if (thisDiff.text.endsWith(prevDiff.text))
				{
					// Shift the edit over the previous equality.
					thisDiff.text = prevDiff.text + thisDiff.text.substring(0, thisDiff.text.length() - prevDiff.text.length());
					nextDiff.text = prevDiff.text + nextDiff.text;
					pointer.previous(); // Walk past nextDiff.
					pointer.previous(); // Walk past thisDiff.
					pointer.previous(); // Walk past prevDiff.
					pointer.remove(); // Delete prevDiff.
					pointer.next(); // Walk past thisDiff.
					thisDiff = pointer.next(); // Walk past nextDiff.
					nextDiff = pointer.hasNext() ? pointer.next() : null;
					changes = true;
				}
				else
				if (thisDiff.text.startsWith(nextDiff.text))
				{
					// Shift the edit over the next equality.
					prevDiff.text += nextDiff.text;
					thisDiff.text = thisDiff.text.substring(nextDiff.text.length()) + nextDiff.text;
					pointer.remove(); // Delete nextDiff.
					nextDiff = pointer.hasNext() ? pointer.next() : null;
					changes = true;
				}
			}
			prevDiff = thisDiff;
			thisDiff = nextDiff;
			nextDiff = pointer.hasNext() ? pointer.next() : null;
		}
		// If shifts were made, the diff needs reordering and another shift sweep.
		if (changes)
		{
			diffCleanupMerge(diffs);
		}
	}

	/**
	 * Class representing one diff operation.
	 */
	public static class Diff
	{
		/**
		 * One of: INSERT, DELETE or EQUAL.
		 */
		public DiffOperation operation;
		/**
		 * The text associated with this diff operation.
		 */
		public String text;

		/**
		 * Constructor.	Initializes the diff with the provided values.
		 * @param operation One of INSERT, DELETE or EQUAL.
		 * @param text The text being applied.
		 */
		public Diff(final DiffOperation operation, final String text)
		{
			// Construct a diff with the specified operation and text.
			this.operation = operation;
			this.text = text;
		}

		/**
		 * Display a human-readable version of this Diff.
		 * @return text version.
		 */
		@Override
		public String toString()
		{
			final String prettyText = this.text.replace('\n', '\u00b6');
			return "Diff(" + this.operation + ",\"" + prettyText + "\")";
		}

		/**
		 * Create a numeric hash value for a Diff.
		 * This function is not used by DMP.
		 * @return Hash value.
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = (operation == null) ? 0 : operation.hashCode();
			result += prime * ((text == null) ? 0 : text.hashCode());
			return result;
		}

		/**
		 * Is this Diff equivalent to another Diff?
		 * @param obj Another Diff to compare against.
		 * @return true or false.
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final Diff other = (Diff) obj;
			if (operation != other.operation)
			{
				return false;
			}
			if (text == null)
			{
				if (other.text != null)
				{
					return false;
				}
			}
			else
			if (!text.equals(other.text))
			{
				return false;
			}
			return true;
		}
	}

	/**
	 * Does traditional filename type matching, where ? substitutes for a
	 * single character, and * for a bunch.  This check is case sensitive!
	 * You need to upper or lower both strings before calling to make
	 * it insensitive
	 * @param fileName the name to check for a match
	 * @param fileNameMask the mask to match the fileName against
	 * @return true if they match, false otherwise
	 */
	public final static boolean filenameMatcher(final String fileName, final String fileNameMask)
	{
		boolean ismatch=true;
		if((!fileName.equalsIgnoreCase(fileNameMask))&&(fileNameMask.length()>0))
		{
			for(int f=0,n=0;f<fileNameMask.length();f++,n++)
			{
				if(fileNameMask.charAt(f)=='?')
				{
					if (n >= fileName.length())
					{
						ismatch = false;
						break;
					}
				}
				else
				if(fileNameMask.charAt(f)=='*')
				{
					if(f==fileNameMask.length()-1)
						break;
					int endOfMatchStr=fileNameMask.indexOf('*',f+1);
					if(endOfMatchStr<0)
						endOfMatchStr=fileNameMask.indexOf('?',f+1);
					int mbEnd = fileNameMask.length();
					if(endOfMatchStr>f)
						mbEnd = endOfMatchStr;
					final String matchBuf = fileNameMask.substring(f+1,mbEnd);
					final int found = fileName.indexOf(matchBuf,n);
					if(found < 0)
					{
						ismatch=false;
						break;
					}
					else
					{
						n=found + matchBuf.length() - 1;
						f+=matchBuf.length();
					}
				}
				else
				if((n>=fileName.length())
				||(fileNameMask.charAt(f)!=fileName.charAt(n))
				||((f==fileNameMask.length()-1)&&(n<fileName.length()-1)))
				{
					ismatch=false;
					break;
				}
			}
		}
		return ismatch;
	}
}
