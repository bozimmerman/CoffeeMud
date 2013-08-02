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
   Mostly Copyright 2000-2013 Bo Zimmerman

   Functions for diff, match and patch (C) 2006 Google
   Computes the difference between two texts to create a patch.
   Applies the patch onto another text, allowing for errors.
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
public class CMStrings
{
	private CMStrings(){super();}
	private static CMStrings inst=new CMStrings();
	public final static CMStrings instance(){return inst;}
	
	public final static String SPACES=repeat(" ",150);
	
	public final static String repeat(final String str1, final int times)
	{
		if(times<=0) return "";
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<times;i++)
			str.append(str1);
		return str.toString();
	}
	
	public final static boolean isUpperCase(final String str) 
	{
		if(str==null) return false;
		for(int c=0;c<str.length();c++)
			if(!Character.isUpperCase(str.charAt(c)))
				return false;
		return true;
	}
	
	public final static boolean isLowerCase(final String str) 
	{
		if(str==null) return false;
		for(int c=0;c<str.length();c++)
			if(!Character.isLowerCase(str.charAt(c)))
				return false;
		return true;
	}
	
	public final static String s_uppercase(final String str)
	{
		if(str==null) return "";
		return str.toUpperCase();
	}
	
	public final static String s_lowercase(final String str)
	{
		if(str==null) return "";
		return str.toLowerCase();
	}
	
	public final static String endWithAPeriod(final String str)
	{
		if((str==null)||(str.length()==0)) return str;
		int x=str.length()-1;
		while((x>=0)
		&&((Character.isWhitespace(str.charAt(x))) // possible #~ color concerns, but normally catches ^? at the end.
			||((x>0)&&((str.charAt(x)!='^')&&(str.charAt(x-1)=='^')&&((--x)>=0))))) 
				x--;
		if(x<0) return str;
		if((str.charAt(x)=='.')||(str.charAt(x)=='!')||(str.charAt(x)=='?')) 
			return str.trim()+" ";
		return str.substring(0,x+1)+". "+str.substring(x+1).trim();
	}
	
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

	public final static String bytesToStr(final byte[] b)
	{ 
		if(b==null) 
			return "";
		try
		{
			return new String(b,CMProps.getVar(CMProps.Str.CHARSETINPUT));
		}
		catch(Exception e)
		{
			return new String(b);
		}
	}
	
	public final static byte[] strToBytes(final String str)
	{ 
		try
		{ 
			return str.getBytes(CMProps.getVar(CMProps.Str.CHARSETINPUT));
		}
		catch(Exception e)
		{
			return str.getBytes();
		}
	}
	
	public final static boolean isVowel(final char c)
	{ 
		return (("aeiou").indexOf(Character.toLowerCase(c))>=0);
	}
	
	public final static int indexOfLastVowel(final String s)
	{ 
		if(s==null) return -1;
		for(int i=s.length()-1;i>=0;i--)
		{
			if(isVowel(s.charAt(i)))
				return i;
		}
		return -1;
	}
	
	public final static String scrunchWord(String s, final int len)
	{
		if(s.length()<=len) return s;
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
	
	public final static int finalDigits(String s)
	{
		if((s==null)||(s.length()==0)) return -1;
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

	public final static boolean containsWordIgnoreCase(final String thisStr, final String word)
	{
		if((thisStr==null)
		||(word==null)
		||(thisStr.length()==0)
		||(word.length()==0))
			return false;
		return containsWord(thisStr.toLowerCase(),word.toLowerCase());
	}

	public final static boolean containsWord(final String thisStr, final String word)
	{
		if((thisStr==null)
		||(word==null)
		||(thisStr.length()==0)
		||(word.length()==0))
			return false;
		for(int i=thisStr.length()-1;i>=0;i--)
		{
			if((thisStr.charAt(i)==word.charAt(0))
			&&((i==0)||(!Character.isLetter(thisStr.charAt(i-1)))))
				if((thisStr.substring(i).startsWith(word.toLowerCase()))
				&&((thisStr.length()==i+word.length())||(!Character.isLetter(thisStr.charAt(i+word.length())))))
				{
					return true;
				}
		}
		return false;
	}

	public final static String replaceAllofAny(final String str, final char[] theseChars, final char with)
	{
		if((str==null)
		||(theseChars==null)
		||(str.length()==0)
		||(!containsAny(str,theseChars)))
			return str;
		final char[] newChars = str.toCharArray();
		for(int i=str.length()-1;i>=0;i--)
			if(contains(theseChars,str.charAt(i)))
			{
				newChars[i]=with;
			}
		return new String(newChars);
	}
	
	public final static String deleteAllofAny(final String str, final char[] theseChars)
	{
		if((str==null)
		||(theseChars==null)
		||(str.length()==0)
		||(!containsAny(str,theseChars)))
			return str;
		final StringBuilder buf=new StringBuilder(str);
		for(int i=buf.length()-1;i>=0;i--)
			if(contains(theseChars,buf.charAt(i)))
			{
				buf.deleteCharAt(i);
			}
		return buf.toString();
	}

	public final static String replaceAll(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=str.length()-1;i>=0;i--)
			if(str.charAt(i)==thisStr.charAt(0))
				if(str.substring(i).startsWith(thisStr))
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
		return str;
	}
	
	public final static String replaceAlls(String str, final String pairs[][])
	{
		if((str==null)
		||(pairs==null)
		||(str.length()==0)
		||(pairs.length==0))
			return str;
		StringBuilder newStr=new StringBuilder("");
		boolean changed=false;
		for(int i=0;i<str.length();i++)
		{
			changed=false;
			for(int t=0;t<pairs.length;t++)
				if((str.charAt(i)==pairs[t][0].charAt(0))
				&&(str.substring(i).startsWith(pairs[t][0])))
				{
					newStr.append(pairs[t][1]);
					changed=true;
					i=i+pairs[t][0].length()-1;
					break;
				}
			if(!changed)
				newStr.append(str.charAt(i));
		}
		return newStr.toString();
	}
	
	public final static String replaceWord(String str, final String thisStr, final String withThisStr)
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
				if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
				&&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
				{
					String oldWord=str.substring(i,i+thisStr.length());
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
		return str;
	}
	
	public final static String replaceFirstWord(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		String uppercaseWithThisStr=withThisStr.toUpperCase();
		for(int i=str.length()-1;i>=0;i--)
		{
			if((str.charAt(i)==thisStr.charAt(0))
			&&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
				if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
				&&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
				{
					String oldWord=str.substring(i,i+thisStr.length());
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
		return str;
	}
	
	public final static String replaceFirst(String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=str.length()-1;i>=0;i--)
			if(str.charAt(i)==thisStr.charAt(0))
				if(str.substring(i).startsWith(thisStr))
				{
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
					return str;
				}
		return str;
	}
	
	public final static String capitalizeAndLower(final String name)
	{
		if((name==null)||(name.length()==0)) return "";
		char[] c=name.toCharArray();
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
				  case ColorLibrary.COLORCODE_FANSI256: i+=3; break;
				  case ColorLibrary.COLORCODE_BANSI256: i+=3; break;
				  case ColorLibrary.COLORCODE_BACKGROUND: i++; break;
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
			if(!Character.isLowerCase(c[i]))
				c[i]=Character.toLowerCase(c[i]);
		return new String(c).trim();
	}
	
	public final static String capitalizeFirstLetter(final String name)
	{
		if((name==null)||(name.length()==0)) 
			return "";
		char[] c=name.toCharArray();
		int i=0;
		for(;i<c.length;i++)
			if(c[i]=='^')
			{
				i++;
				if(i<c.length)
				{
					switch(c[i])
					{
					case ColorLibrary.COLORCODE_FANSI256: i+=3; break;
					case ColorLibrary.COLORCODE_BANSI256: i+=3; break;
					case ColorLibrary.COLORCODE_BACKGROUND: i++; break;
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
		if(i<c.length)
			c[i]=Character.toUpperCase(c[i]);
		return new String(c).trim();
	}
	
	public final static String lastWordIn(final String thisStr)
	{
		int x=thisStr.lastIndexOf(' ');
		if(x>=0)
			return thisStr.substring(x+1);
		return thisStr;
	}
	
	public final static String getSayFromMessage(final String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf('\'');
		int end=msg.lastIndexOf('\'');
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
		return null;
	}
	public final static String substituteSayInMessage(final String affmsg, final String msg)
	{
		if(affmsg==null) return null;
		final int start=affmsg.indexOf('\'');
		final int end=affmsg.lastIndexOf('\'');
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}

	public final static boolean containsIgnoreCase(final String[] strs, final String str)
	{
		if((str==null)||(strs==null)) return false;
		for(int s=0;s<strs.length;s++)
			if(strs[s].equalsIgnoreCase(str))
				return true;
		return false;
	}
	
	public final static boolean compareStringArrays(final String[] A1, final String[] A2)
	{
		if(((A1==null)||(A1.length==0))
		&&((A2==null)||(A2.length==0)))
			return true;
		if((A1==null)||(A2==null)) return false;
		if(A1.length!=A2.length) return false;
		for(int i=0;i<A1.length;i++)
		{
			boolean found=false;
			for(int i2=0;i2<A2.length;i2++)
				if(A1[i].equalsIgnoreCase(A2[i]))
				{ found=true; break;}
			if(!found) return false;
		}
		return true;
	}
	
	public final static boolean contains(final String[] strs, final String str)
	{
		if((str==null)||(strs==null)) return false;
		for(int s=0;s<strs.length;s++)
			if(strs[s].equals(str))
				return true;
		return false;
	}
	
	public final static boolean contains(final char[] anycs, final char c)
	{
		for(char c1 : anycs)
			if(c1==c)
				return true;
		return false;
	}
	
	public final static boolean containsAny(final String str, final char[] anycs)
	{
		if((str==null)||(anycs==null)) return false;
		for(int i=0;i<str.length();i++)
			if(contains(anycs,str.charAt(i)))
				return true;
		return false;
	}
	
	/**
	 * Replaces @x1 type variables inside a stringbuffer with an actual value
	 * Not used in the main expression system, this is a stand alone function
	 * Also uniquely, supports @x numbers above 10.  Values are *1* indexed!!
	 * @param str the stringbuffer to assess
	 * @param values values to replace each variable with
	 */
	public final static void replaceVariables(final StringBuffer str, final String values[])
	{
		final int valueLen=(values.length<=10)?1:Integer.toString(values.length).length();
		for(int i=0;i<str.length()-(1+valueLen);i++)
			if((str.charAt(i)=='@') && (str.charAt(i+1)=='x') && (Character.isDigit(str.charAt(i+2))))
			{
				int endDex=1;
				while((endDex < valueLen) && (Character.isDigit(str.charAt(i+2+endDex))))
					endDex++;
				final int valueDex = Integer.valueOf(str.substring(i+2,i+2+endDex)).intValue();
				final String newValue = (valueDex >0 && valueDex <= values.length)?values[valueDex-1]:"";
				str.delete(i, i+2+endDex);
				str.insert(i, newValue);
				i--;
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
	public final static String replaceVariables(final String str, final String values[])
	{
		final StringBuffer buf = new StringBuffer(str);
		replaceVariables(buf,values);
		return buf.toString();
	}
	
	/**
	 * Strips colors, of both the ansi, and cm code variety
	 * @param s the string to strip
	 * @return the stripped string
	 */
	public final static String removeColors(final String s)
	{
		if(s==null) return "";
		if(s.indexOf('^')<0) return s;
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
					colorStart=-1;
				}
				break;
			case (char)27: colorStart=i; break;
			case '^':
				if((i+1)<str.length())
				{
					int tagStart=i;
					char c=str.charAt(i+1);
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
	 * Returns the length of the string as if it has neither
	 * ansi nor cm color codes.
	 * @param thisStr the string to get the length of
	 * @return the length of thisStr w/o colors
	 */
	public final static int lengthMinusColors(final String thisStr)
	{
		if(thisStr==null) 
			return 0;
		if(thisStr.indexOf('^')<0) 
			return thisStr.length();
		int size=0;
		for(int i=0;i<thisStr.length();i++)
		{
			if(thisStr.charAt(i)=='^')
			{
				i++;
				if((i+1)<thisStr.length())
				{
					int tagStart=i;
					switch(thisStr.charAt(i))
					{
					case ColorLibrary.COLORCODE_BACKGROUND: i++; break;
					case ColorLibrary.COLORCODE_FANSI256: i+=3; break;
					case ColorLibrary.COLORCODE_BANSI256: i+=3; break;
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
			else
				size++;
		}
		return size;
	}
	
	public static void convertHtmlToText(final StringBuilder finalData)
	{
		final class TagStacker 
		{
			private Stack<Object[]> tagStack=new Stack<Object[]>();
			public void push(String tag, int index)
			{
				tagStack.push(new Object[]{tag,Integer.valueOf(index)});
			}
			@SuppressWarnings("unchecked")
			public int pop(String tag)
			{
				if(tagStack.size()==0) return -1;
				Stack<Object[]> backup=(Stack<Object[]>)tagStack.clone();
				Object[] top;
				do
				{
					top=tagStack.pop();
				}
				while((!((String)top[0]).equals(tag))&&(!tagStack.isEmpty()));
				if(!((String)top[0]).equals(tag))
				{
					tagStack=backup;
					return -1;
				}
				return ((Integer)top[1]).intValue();
			}
		}
		TagStacker stack=new TagStacker();
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
					int x=stack.pop("<!--");
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
						char p=finalData.charAt(i-1);
						if((n=='\n')||(n=='\r')&&(n!=c))
						{
							finalData.deleteCharAt(i+1);
							n=finalData.charAt(i+1);
						}
						if((Character.isLetterOrDigit(n)||((".?,;\"'!@#$%^*()_-+={}[]:").indexOf(n)>=0))
						&&(Character.isLetterOrDigit(p)||((".?,;\"'!@#$%^*()_-+={}[]:".indexOf(p)>=0))))
							finalData.setCharAt(i,' ');
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
				switch(c) {
				case ' ': case '\t': case '<': case '>': start=0; break;
				case '/': state=2; break;
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
				} break;
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
					for(String[] xset : xlateTags)
						if(xset[0].equals(tag))
						{
							finalData.insert(start, xset[1]);
							start=start+xset[1].length()-1;
							break;
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
						int x=stack.pop(tag);
						if(x>=0) start=x;
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
					if(code.equals("nbsp")) finalData.insert(start,' ');
					else if(code.equals("amp")) finalData.insert(start,'&');
					else if(code.equals("lt")) finalData.insert(start,'<');
					else if(code.equals("gt")) finalData.insert(start,'>');
					else if(code.equals("quot")) finalData.insert(start,'"');
					i=start-1;
					start=-1;
				}
				else
				if((!Character.isLetter(c))||((i-start)>10))
					start=-1;
				break;
			}
		}
	}
	
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
				switch(c) {
				case '/': state=1; closeFlag=true; break;
				case 'H': state=2; break;
				case 'B': state=3; break;
				default: start=-1; break;
				} break;
			case 1:
				switch(c) {
				case 'H': state=2; break;
				case 'B': state=3; break;
				default: start=-1; break;
				} break;
			case 2:
				switch(c) {
				case 'E': if(lastC!='H') state=-1; else state=5; break;
				case 'T': if(lastC!='H') state=-1; break;
				case 'M': if(lastC!='T') state=-1; break;
				case 'L': if(lastC!='M') state=-1; else state=4; break;
				default: start=-1; break;
				} break;
			case 3:
				switch(c) {
				case 'O': if(lastC!='B') state=-1; break;
				case 'D': if(lastC!='O') state=-1; break;
				case 'Y': if(lastC!='D') state=-1; else state=4; break;
				default: start=-1; break;
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
				switch(c) {
				case 'A': if(lastC!='E') state=-1; break;
				case 'D': if(lastC!='A') state=-1; else state=6; break;
				default: start=-1; break;
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

	public final static Hashtable<Object,Integer> makeNumericHash(final Object[] obj)
	{
		Hashtable<Object,Integer> H=new Hashtable<Object,Integer>();
		for(int i=0;i<obj.length;i++)
			H.put(obj[i],Integer.valueOf(i));
		return H;
	}
	
	public final static String padCenter(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		final int size=(thisMuch-lenMinusColors)/2;
		int rest=thisMuch-lenMinusColors-size;
		if(rest<0) rest=0;
		return SPACES.substring(0,size)+thisStr+SPACES.substring(0,rest);
	}
	public final static String padLeft(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}
	public final static String padLeftWith(final String thisStr, final char c, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		StringBuilder str=new StringBuilder("");
		for(int i=0;i<thisMuch-lenMinusColors;i++)
			str.append(c);
		str.append(thisStr);
		return str.toString();
	}
	public final static String padLeft(final String thisStr, final String colorPrefix, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return colorPrefix+removeColors(thisStr).substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lenMinusColors)+colorPrefix+thisStr;
	}
	public final static String safeLeft(final String thisStr, final int thisMuch)
	{
		if(thisStr.length()<=thisMuch)
			return thisStr;
		return thisStr.substring(0,thisMuch);
	}
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
	public final static String padRightWith(final String thisStr, final char c, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors==thisMuch)
			return thisStr;
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		StringBuilder str=new StringBuilder(thisStr);
		for(int i=0;i<thisMuch-lenMinusColors;i++)
			str.append(c);
		return str.toString();
	}
	public final static String limit(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		return thisStr;
	}
	public final static String ellipse(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch)+"...";
		return thisStr;
	}
	public final static String padRight(final String thisStr, final String colorSuffix, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch)+colorSuffix;
		if(thisMuch-lenMinusColors >= SPACES.length())
			return thisStr+colorSuffix+SPACES;
		return thisStr+colorSuffix+SPACES.substring(0,thisMuch-lenMinusColors);
	}
	public final static String padRightPreserve(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		if(thisMuch-lenMinusColors >= SPACES.length())
			return thisStr+SPACES;
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}
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
	public final static String padLeftPreserve(final String thisStr, final int thisMuch)
	{
		final int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}
	
	public final static String sameCase(final String str, final char c)
	{
		if(Character.isUpperCase(c))
			return str.toUpperCase();
		return str.toLowerCase();
	}

	// states: 0 = done after this one,-1 = done a char ago,-2 = eat & same state,-99 = error,
	// chars: 254 = digit, 253 = letter, 252 = digitNO0, 255=eof
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

	private static class StringExpToken
	{
		public int  	  type    = -1;
		public String    value    = "";
		public double     numValue  = 0.0;

		public final static StringExpToken token(final int type, final String value) throws Exception
		{
			final StringExpToken token = new StringExpToken();
			token.type = type;
			token.value = value;
			if((value.length()>0)&&(Character.isDigit(value.charAt(0))))
				token.numValue = Double.parseDouble(value);
			return token;
		}
		private StringExpToken() { }
	}

	private static StringExpToken nextToken(final List<StringExpToken> tokens, final int[] index) {
		if(index[0]>=tokens.size()) return null;
		return tokens.get(index[0]++);
	}
	
	private static final int	STRING_EXP_TOKEN_EVALUATOR    = 1;
	private static final int	STRING_EXP_TOKEN_OPENPAREN    = 2;
	private static final int	STRING_EXP_TOKEN_CLOSEPAREN    = 3;
	private static final int	STRING_EXP_TOKEN_WORD   	 = 4;
	private static final int	STRING_EXP_TOKEN_STRCONST    = 5;
	private static final int	STRING_EXP_TOKEN_COMBINER    = 6;
	private static final int	STRING_EXP_TOKEN_NOT		= 7;
	private static final int	STRING_EXP_TOKEN_NUMCONST    = 8;
	private static final int	STRING_EXP_TOKEN_UKNCONST    = 9;

	private static StringExpToken makeTokenType(String token, final Map<String,Object> variables, final boolean emptyVars) throws Exception
	{
		if ((token == null)||(token.length()==0))
			return null;
		if (token.startsWith("\""))
			return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, token.substring(1, token.length() - 1));
		if (token.startsWith("\'"))
			return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, token.substring(1, token.length() - 1));
		if (token.startsWith("`"))
			return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, token.substring(1, token.length() - 1));
		if (token.equals("("))
			return StringExpToken.token(STRING_EXP_TOKEN_OPENPAREN, token);
		if (token.equals(")"))
			return StringExpToken.token(STRING_EXP_TOKEN_CLOSEPAREN, token);
		if (token.equalsIgnoreCase("IN"))
			return StringExpToken.token(STRING_EXP_TOKEN_EVALUATOR, token);
		if (token.equals("+")||token.equals("-")||token.equals("*")||token.equals("/")||token.equals("?"))
			return StringExpToken.token(STRING_EXP_TOKEN_COMBINER, token);
		if (token.equals("!")||token.equalsIgnoreCase("NOT"))
			return StringExpToken.token(STRING_EXP_TOKEN_NOT, token);
		if(Character.isDigit(token.charAt(0)))
			return StringExpToken.token(STRING_EXP_TOKEN_NUMCONST, token);
		if (token.startsWith("$"))
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
				return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, value.toString());
			return StringExpToken.token(STRING_EXP_TOKEN_UKNCONST, value.toString());
		}
		if ((token.charAt(0) == '_') || (Character.isLetterOrDigit(token.charAt(0))) || (token.charAt(0) == '|') || (token.charAt(0) == '&'))
			return StringExpToken.token(STRING_EXP_TOKEN_WORD, token);
		return StringExpToken.token(STRING_EXP_TOKEN_EVALUATOR, token);
	}

	private static StringExpToken nextStringToken(final String expression, final int[] index, final Map<String,Object> variables, final boolean emptyVars) throws Exception
	{
		int[] stateBlock = STRING_EXP_SM[1];
		final StringBuffer token = new StringBuffer("");
		while (index[0] < expression.length())
		{
			char c = expression.charAt(index[0]);
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
			if (stateBlock[x] == 255)
			{
				finalState = stateBlock[x + 1];
				break;
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

	/*
	 * case STRING_EXP_TOKEN_EVALUATOR: case STRING_EXP_TOKEN_OPENPAREN: case STRING_EXP_TOKEN_CLOSEPAREN: case STRING_EXP_TOKEN_WORD: case
	 * STRING_EXP_TOKEN_CONST: case STRING_EXP_TOKEN_COMBINER:
	 */
	public final static String matchSimpleConst(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		final int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if((token.type != STRING_EXP_TOKEN_STRCONST)
		&& (token.type != STRING_EXP_TOKEN_UKNCONST))
			return null;
		index[0] = i[0];
		return token.value;
	}

	public final static Double matchSimpleNumber(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		final int[] i = index.clone();
		final StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if((token.type != STRING_EXP_TOKEN_NUMCONST)
		&& (token.type != STRING_EXP_TOKEN_UKNCONST))
			return null;
		index[0] = i[0];
		return Double.valueOf(token.numValue);
	}
	
	public final static String matchCombinedString(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			final String testInside = matchCombinedString(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = index.clone();
		final String leftValue = matchSimpleConst(tokens, i, variables);
		if (leftValue == null)
			return null;
		final int[] i2 = i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != STRING_EXP_TOKEN_COMBINER))
		{
			index[0] = i[0];
			return leftValue;
		}
		if(!token.value.equals("+")) 
			throw new Exception("Can't combine a string using '"+token.value+"'");
		i[0] = i2[0];
		final String rightValue = matchCombinedString(tokens, i, variables);
		if (rightValue == null)
			return null;
		index[0] = i[0];
		return leftValue + rightValue;
	}

	public final static Double matchCombinedNum(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			final Double testInside = matchCombinedNum(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = index.clone();
		final Double leftValue = matchSimpleNumber(tokens, i, variables);
		if (leftValue == null)
			return null;
		final int[] i2 = i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != STRING_EXP_TOKEN_COMBINER))
		{
			index[0] = i[0];
			return leftValue;
		}
		i[0] = i2[0];
		final Double rightValue = matchCombinedNum(tokens, i, variables);
		if (rightValue == null)
			return null;
		index[0] = i[0];
		if(token.value.equals("+"))
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() + rightValue.doubleValue());
		}
		else
		if(token.value.equals("-")) 
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() - rightValue.doubleValue());
		}
		else
		if(token.value.equals("*")) 
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() * rightValue.doubleValue());
		}
		else
		if(token.value.equals("/")) 
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() / rightValue.doubleValue());
		}
		else
		if(token.value.equals("?")) 
		{
			index[0] = i[0];
			return Double.valueOf(Math.round((Math.random() * (rightValue.doubleValue()-leftValue.doubleValue())) + leftValue.doubleValue())) ;
		}
		else
			throw new Exception("Unknown math combiner "+token.value);
	}
	
	public final static Boolean matchStringEvaluation(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if(token.type == STRING_EXP_TOKEN_NOT)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				return new Boolean(!testInside.booleanValue());
			}
		}
		else
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			final Boolean testInside = matchStringEvaluation(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = index.clone();
		final String leftValue = matchCombinedString(tokens, i, variables);
		if (leftValue == null)
			return null;
		token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type != STRING_EXP_TOKEN_EVALUATOR)
			return null;
		final String rightValue = matchCombinedString(tokens, i, variables);
		if (rightValue == null)
			return null;
		final int compare = leftValue.compareToIgnoreCase(rightValue);
		final Boolean result;
		if (token.value.equals(">"))
			result = new Boolean(compare > 0);
		else if (token.value.equals(">="))
			result = new Boolean(compare >= 0);
		else if (token.value.equals("<"))
			result = new Boolean(compare < 0);
		else if (token.value.equals("<="))
			result = new Boolean(compare <= 0);
		else if (token.value.equals("="))
			result = new Boolean(compare == 0);
		else if (token.value.equals("!="))
			result = new Boolean(compare != 0);
		else if (token.value.equals("<>"))
			result = new Boolean(compare != 0);
		else if (token.value.equals("><"))
			result = new Boolean(compare != 0);
		else
		if (token.value.equalsIgnoreCase("IN"))
			result = new Boolean(rightValue.toUpperCase().indexOf(leftValue.toUpperCase())>=0);
		else
			return null;
		index[0] = i[0];
		return result;
	}

	public final static Boolean matchNumEvaluation(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if(token.type == STRING_EXP_TOKEN_NOT)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				return new Boolean(!testInside.booleanValue());
			}
		}
		else
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			final Boolean testInside = matchNumEvaluation(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = index.clone();
		final Double leftValue = matchCombinedNum(tokens, i, variables);
		if (leftValue == null)
			return null;
		token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type != STRING_EXP_TOKEN_EVALUATOR)
			return null;
		final Double rightValue = matchCombinedNum(tokens, i, variables);
		if (rightValue == null)
			return null;
		final Boolean result;
		if (token.value.equals(">"))
			result = new Boolean(leftValue.doubleValue() > rightValue.doubleValue());
		else if (token.value.equals(">="))
			result = new Boolean(leftValue.doubleValue() >= rightValue.doubleValue());
		else if (token.value.equals("<"))
			result = new Boolean(leftValue.doubleValue() < rightValue.doubleValue());
		else if (token.value.equals("<="))
			result = new Boolean(leftValue.doubleValue() <= rightValue.doubleValue());
		else if (token.value.equals("="))
			result = new Boolean(leftValue.doubleValue() == rightValue.doubleValue());
		else if (token.value.equals("!="))
			result = new Boolean(leftValue.doubleValue() != rightValue.doubleValue());
		else if (token.value.equals("<>"))
			result = new Boolean(leftValue.doubleValue() != rightValue.doubleValue());
		else if (token.value.equals("><"))
			result = new Boolean(leftValue.doubleValue() != rightValue.doubleValue());
		else
		if (token.value.equalsIgnoreCase("IN"))
			throw new Exception("Can't use IN operator on numbers.");
		else
			return null;
		index[0] = i[0];
		return result;
	}
	
	public final static Boolean matchExpression(final List<StringExpToken> tokens, final int[] index, final Map<String,Object> variables) throws Exception
	{
		int[] i = index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		Boolean leftExpression = null;
		if(token.type == STRING_EXP_TOKEN_NOT)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				leftExpression = new Boolean(!testInside.booleanValue());
			}
		}
		else
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			final Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				leftExpression = testInside;
			}
		}
		if(leftExpression == null)
		{
			i = index.clone();
			leftExpression = matchStringEvaluation(tokens, i, variables);
			if(leftExpression == null) leftExpression = matchNumEvaluation(tokens, i, variables);
		}
		if (leftExpression == null) return null;
		final int[] i2 = i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != STRING_EXP_TOKEN_WORD))
		{
			index[0] = i[0];
			return leftExpression;
		}
		i[0] = i2[0];
		final Boolean rightExpression = matchExpression(tokens, i, variables);
		if (rightExpression == null)
			return null;
		final Boolean result;
		if (token.value.equalsIgnoreCase("AND"))
			result = new Boolean(leftExpression.booleanValue() && rightExpression.booleanValue());
		else if (token.value.startsWith("&"))
			result = new Boolean(leftExpression.booleanValue() && rightExpression.booleanValue());
		else if (token.value.startsWith("|"))
			result = new Boolean(leftExpression.booleanValue() || rightExpression.booleanValue());
		else if (token.value.equalsIgnoreCase("OR"))
			result = new Boolean(leftExpression.booleanValue() || rightExpression.booleanValue());
		else if (token.value.equalsIgnoreCase("XOR"))
			result = new Boolean(leftExpression.booleanValue() != rightExpression.booleanValue());
		else
			throw new Exception("Parse Exception: Illegal expression evaluation combiner: " + token.value);
		index[0] = i[0];
		return result;
	}

	public final static boolean parseStringExpression(final String expression, final Map<String,Object> variables, final boolean emptyVarsOK) throws Exception
	{
		final Vector<StringExpToken> tokens = new Vector<StringExpToken>();
		int[] i = { 0 };
		StringExpToken token = nextStringToken(expression,i,variables, emptyVarsOK);
		while(token != null) {
			tokens.addElement(token);
			token = nextStringToken(expression,i,variables, emptyVarsOK);
		}
		if(tokens.size()==0) return true;
		i = new int[]{ 0 };
		final Boolean value = matchExpression(tokens, i, variables);
		if (value == null) throw new Exception("Parse error on following statement: " + expression);
		return value.booleanValue();
	}

	public final static int countSubstrings(final String[] set, final String[] things)
	{
		if(set==null) return 0;
		if(things==null) return 0;
		int total=0;
		for(String longString : set)
			for(String subString : things)
			{
				int x=0;
				while((x=longString.indexOf( subString, x ))>=x)
					total++;
			}
		return total;
	}

	public final static String determineEOLN(final CharSequence str)
	{
		if(str!=null) 
		for(int i=0;i<str.length();i++)
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
	private static float Diff_Timeout = 1.0f;

	/**
	 * Internal class for returning results from diff_linesToChars().
	 * Other less paranoid languages just use a three-element array.
	 * @author fraser@google.com (Neil Fraser)
	 */
	private static class LinesToCharsResult {
		protected String chars1;
		protected String chars2;
		protected List<String> lineArray;

		protected LinesToCharsResult(String chars1, String chars2,
				List<String> lineArray) {
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
	 * This method allows the 'checklines' of diff_main() to be optional.
	 * Most of the time checklines is wanted, so default to true.
	 * @author fraser@google.com (Neil Fraser)
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @return Linked List of Diff objects.
	 */
	public static LinkedList<Diff> diff_main(String text1, String text2) {
		return diff_main(text1, text2, true);
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
	public static LinkedList<Diff> diff_main(String text1, String text2, boolean checklines) {
		// Set a deadline by which time the diff must be complete.
		long deadline;
		if (Diff_Timeout <= 0) {
			deadline = Long.MAX_VALUE;
		} else {
			deadline = System.currentTimeMillis() + (long) (Diff_Timeout * 1000);
		}
		return diff_main(text1, text2, checklines, deadline);
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
	private static LinkedList<Diff> diff_main(String text1, String text2, boolean checklines, long deadline) {
		// Check for null inputs.
		if (text1 == null || text2 == null) {
			throw new IllegalArgumentException("Null inputs. (diff_main)");
		}

		// Check for equality (speedup).
		LinkedList<Diff> diffs;
		if (text1.equals(text2)) {
			diffs = new LinkedList<Diff>();
			if (text1.length() != 0) {
				diffs.add(new Diff(DiffOperation.EQUAL, text1));
			}
			return diffs;
		}

		// Trim off common prefix (speedup).
		int commonlength = diff_commonPrefix(text1, text2);
		String commonprefix = text1.substring(0, commonlength);
		text1 = text1.substring(commonlength);
		text2 = text2.substring(commonlength);

		// Trim off common suffix (speedup).
		commonlength = diff_commonSuffix(text1, text2);
		String commonsuffix = text1.substring(text1.length() - commonlength);
		text1 = text1.substring(0, text1.length() - commonlength);
		text2 = text2.substring(0, text2.length() - commonlength);

		// Compute the diff on the middle block.
		diffs = diff_compute(text1, text2, checklines, deadline);

		// Restore the prefix and suffix.
		if (commonprefix.length() != 0) {
			diffs.addFirst(new Diff(DiffOperation.EQUAL, commonprefix));
		}
		if (commonsuffix.length() != 0) {
			diffs.addLast(new Diff(DiffOperation.EQUAL, commonsuffix));
		}

		diff_cleanupMerge(diffs);
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
	private static LinkedList<Diff> diff_compute(String text1, String text2, boolean checklines, long deadline) {
		LinkedList<Diff> diffs = new LinkedList<Diff>();

		if (text1.length() == 0) {
			// Just add some text (speedup).
			diffs.add(new Diff(DiffOperation.INSERT, text2));
			return diffs;
		}

		if (text2.length() == 0) {
			// Just delete some text (speedup).
			diffs.add(new Diff(DiffOperation.DELETE, text1));
			return diffs;
		}

		String longtext = text1.length() > text2.length() ? text1 : text2;
		String shorttext = text1.length() > text2.length() ? text2 : text1;
		int i = longtext.indexOf(shorttext);
		if (i != -1) {
			// Shorter text is inside the longer text (speedup).
			DiffOperation op = (text1.length() > text2.length()) ?
										 DiffOperation.DELETE : DiffOperation.INSERT;
			diffs.add(new Diff(op, longtext.substring(0, i)));
			diffs.add(new Diff(DiffOperation.EQUAL, shorttext));
			diffs.add(new Diff(op, longtext.substring(i + shorttext.length())));
			return diffs;
		}

		if (shorttext.length() == 1) {
			// Single character string.
			// After the previous speedup, the character can't be an equality.
			diffs.add(new Diff(DiffOperation.DELETE, text1));
			diffs.add(new Diff(DiffOperation.INSERT, text2));
			return diffs;
		}

		// Check to see if the problem can be split in two.
		String[] hm = diff_halfMatch(text1, text2);
		if (hm != null) {
			// A half-match was found, sort out the return data.
			String text1_a = hm[0];
			String text1_b = hm[1];
			String text2_a = hm[2];
			String text2_b = hm[3];
			String mid_common = hm[4];
			// Send both pairs off for separate processing.
			LinkedList<Diff> diffs_a = diff_main(text1_a, text2_a,
																					 checklines, deadline);
			LinkedList<Diff> diffs_b = diff_main(text1_b, text2_b,
																					 checklines, deadline);
			// Merge the results.
			diffs = diffs_a;
			diffs.add(new Diff(DiffOperation.EQUAL, mid_common));
			diffs.addAll(diffs_b);
			return diffs;
		}

		if (checklines && text1.length() > 100 && text2.length() > 100) {
			return diff_lineMode(text1, text2, deadline);
		}

		return diff_bisect(text1, text2, deadline);
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
	private static LinkedList<Diff> diff_lineMode(String text1, String text2, long deadline) {
		// Scan the text on a line-by-line basis first.
		LinesToCharsResult b = diff_linesToChars(text1, text2);
		text1 = b.chars1;
		text2 = b.chars2;
		List<String> linearray = b.lineArray;

		LinkedList<Diff> diffs = diff_main(text1, text2, false, deadline);

		// Convert the diff back to original text.
		diff_charsToLines(diffs, linearray);
		// Eliminate freak matches (e.g. blank lines)
		diff_cleanupSemantic(diffs);

		// Rediff any replacement blocks, this time character-by-character.
		// Add a dummy entry at the end.
		diffs.add(new Diff(DiffOperation.EQUAL, ""));
		int count_delete = 0;
		int count_insert = 0;
		String text_delete = "";
		String text_insert = "";
		ListIterator<Diff> pointer = diffs.listIterator();
		Diff thisDiff = pointer.next();
		while (thisDiff != null) {
			switch (thisDiff.operation) {
			case INSERT:
				count_insert++;
				text_insert += thisDiff.text;
				break;
			case DELETE:
				count_delete++;
				text_delete += thisDiff.text;
				break;
			case EQUAL:
				// Upon reaching an equality, check for prior redundancies.
				if (count_delete >= 1 && count_insert >= 1) {
					// Delete the offending records and add the merged ones.
					pointer.previous();
					for (int j = 0; j < count_delete + count_insert; j++) {
						pointer.previous();
						pointer.remove();
					}
					for (Diff newDiff : diff_main(text_delete, text_insert, false,
							deadline)) {
						pointer.add(newDiff);
					}
				}
				count_insert = 0;
				count_delete = 0;
				text_delete = "";
				text_insert = "";
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
	private static LinkedList<Diff> diff_bisect(String text1, String text2,
			long deadline) {
		// Cache the text lengths to prevent multiple calls.
		int text1_length = text1.length();
		int text2_length = text2.length();
		int max_d = (text1_length + text2_length + 1) / 2;
		int v_offset = max_d;
		int v_length = 2 * max_d;
		int[] v1 = new int[v_length];
		int[] v2 = new int[v_length];
		for (int x = 0; x < v_length; x++) {
			v1[x] = -1;
			v2[x] = -1;
		}
		v1[v_offset + 1] = 0;
		v2[v_offset + 1] = 0;
		int delta = text1_length - text2_length;
		// If the total number of characters is odd, then the front path will
		// collide with the reverse path.
		boolean front = (delta % 2 != 0);
		// Offsets for start and end of k loop.
		// Prevents mapping of space beyond the grid.
		int k1start = 0;
		int k1end = 0;
		int k2start = 0;
		int k2end = 0;
		for (int d = 0; d < max_d; d++) {
			// Bail out if deadline is reached.
			if (System.currentTimeMillis() > deadline) {
				break;
			}

			// Walk the front path one step.
			for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2) {
				int k1_offset = v_offset + k1;
				int x1;
				if (k1 == -d || (k1 != d && v1[k1_offset - 1] < v1[k1_offset + 1])) {
					x1 = v1[k1_offset + 1];
				} else {
					x1 = v1[k1_offset - 1] + 1;
				}
				int y1 = x1 - k1;
				while (x1 < text1_length && y1 < text2_length
							 && text1.charAt(x1) == text2.charAt(y1)) {
					x1++;
					y1++;
				}
				v1[k1_offset] = x1;
				if (x1 > text1_length) {
					// Ran off the right of the graph.
					k1end += 2;
				} else if (y1 > text2_length) {
					// Ran off the bottom of the graph.
					k1start += 2;
				} else if (front) {
					int k2_offset = v_offset + delta - k1;
					if (k2_offset >= 0 && k2_offset < v_length && v2[k2_offset] != -1) {
						// Mirror x2 onto top-left coordinate system.
						int x2 = text1_length - v2[k2_offset];
						if (x1 >= x2) {
							// Overlap detected.
							return diff_bisectSplit(text1, text2, x1, y1, deadline);
						}
					}
				}
			}

			// Walk the reverse path one step.
			for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2) {
				int k2_offset = v_offset + k2;
				int x2;
				if (k2 == -d || (k2 != d && v2[k2_offset - 1] < v2[k2_offset + 1])) {
					x2 = v2[k2_offset + 1];
				} else {
					x2 = v2[k2_offset - 1] + 1;
				}
				int y2 = x2 - k2;
				while (x2 < text1_length && y2 < text2_length
							 && text1.charAt(text1_length - x2 - 1)
							 == text2.charAt(text2_length - y2 - 1)) {
					x2++;
					y2++;
				}
				v2[k2_offset] = x2;
				if (x2 > text1_length) {
					// Ran off the left of the graph.
					k2end += 2;
				} else if (y2 > text2_length) {
					// Ran off the top of the graph.
					k2start += 2;
				} else if (!front) {
					int k1_offset = v_offset + delta - k2;
					if (k1_offset >= 0 && k1_offset < v_length && v1[k1_offset] != -1) {
						int x1 = v1[k1_offset];
						int y1 = v_offset + x1 - k1_offset;
						// Mirror x2 onto top-left coordinate system.
						x2 = text1_length - x2;
						if (x1 >= x2) {
							// Overlap detected.
							return diff_bisectSplit(text1, text2, x1, y1, deadline);
						}
					}
				}
			}
		}
		// Diff took too long and hit the deadline or
		// number of diffs equals number of characters, no commonality at all.
		LinkedList<Diff> diffs = new LinkedList<Diff>();
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
	private static LinkedList<Diff> diff_bisectSplit(String text1, String text2, int x, int y, long deadline) {
		String text1a = text1.substring(0, x);
		String text2a = text2.substring(0, y);
		String text1b = text1.substring(x);
		String text2b = text2.substring(y);

		// Compute both diffs serially.
		LinkedList<Diff> diffs = diff_main(text1a, text2a, false, deadline);
		LinkedList<Diff> diffsb = diff_main(text1b, text2b, false, deadline);

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
	private static LinesToCharsResult diff_linesToChars(String text1, String text2) {
		List<String> lineArray = new ArrayList<String>();
		Map<String, Integer> lineHash = new HashMap<String, Integer>();
		// e.g. linearray[4] == "Hello\n"
		// e.g. linehash.get("Hello\n") == 4

		// "\x00" is a valid character, but various debuggers don't like it.
		// So we'll insert a junk entry to avoid generating a null character.
		lineArray.add("");

		String chars1 = diff_linesToCharsMunge(text1, lineArray, lineHash);
		String chars2 = diff_linesToCharsMunge(text2, lineArray, lineHash);
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
	private static String diff_linesToCharsMunge(String text, List<String> lineArray, Map<String, Integer> lineHash) {
		int lineStart = 0;
		int lineEnd = -1;
		String line;
		StringBuilder chars = new StringBuilder();
		// Walk the text, pulling out a substring for each line.
		// text.split('\n') would would temporarily double our memory footprint.
		// Modifying text would create many large strings to garbage collect.
		while (lineEnd < text.length() - 1) {
			lineEnd = text.indexOf('\n', lineStart);
			if (lineEnd == -1) {
				lineEnd = text.length() - 1;
			}
			line = text.substring(lineStart, lineEnd + 1);
			lineStart = lineEnd + 1;

			if (lineHash.containsKey(line)) {
				chars.append(String.valueOf((char) lineHash.get(line).intValue()));
			} else {
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
	private static void diff_charsToLines(LinkedList<Diff> diffs, List<String> lineArray) {
		StringBuilder text;
		for (Diff diff : diffs) {
			text = new StringBuilder();
			for (int y = 0; y < diff.text.length(); y++) {
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
	private static int diff_commonPrefix(String text1, String text2) {
		// Performance analysis: http://neil.fraser.name/news/2007/10/09/
		int n = Math.min(text1.length(), text2.length());
		for (int i = 0; i < n; i++) {
			if (text1.charAt(i) != text2.charAt(i)) {
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
	private static int diff_commonSuffix(String text1, String text2) {
		// Performance analysis: http://neil.fraser.name/news/2007/10/09/
		int text1_length = text1.length();
		int text2_length = text2.length();
		int n = Math.min(text1_length, text2_length);
		for (int i = 1; i <= n; i++) {
			if (text1.charAt(text1_length - i) != text2.charAt(text2_length - i)) {
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
	private static int diff_commonOverlap(String text1, String text2) {
		// Cache the text lengths to prevent multiple calls.
		int text1_length = text1.length();
		int text2_length = text2.length();
		// Eliminate the null case.
		if (text1_length == 0 || text2_length == 0) {
			return 0;
		}
		// Truncate the longer string.
		if (text1_length > text2_length) {
			text1 = text1.substring(text1_length - text2_length);
		} else if (text1_length < text2_length) {
			text2 = text2.substring(0, text1_length);
		}
		int text_length = Math.min(text1_length, text2_length);
		// Quick check for the worst case.
		if (text1.equals(text2)) {
			return text_length;
		}

		// Start by looking for a single character match
		// and increase length until no match is found.
		// Performance analysis: http://neil.fraser.name/news/2010/11/04/
		int best = 0;
		int length = 1;
		while (true) {
			String pattern = text1.substring(text_length - length);
			int found = text2.indexOf(pattern);
			if (found == -1) {
				return best;
			}
			length += found;
			if (found == 0 || text1.substring(text_length - length).equals(
					text2.substring(0, length))) {
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
	private static String[] diff_halfMatch(String text1, String text2) {
		if (Diff_Timeout <= 0) {
			// Don't risk returning a non-optimal diff if we have unlimited time.
			return null;
		}
		String longtext = text1.length() > text2.length() ? text1 : text2;
		String shorttext = text1.length() > text2.length() ? text2 : text1;
		if (longtext.length() < 4 || shorttext.length() * 2 < longtext.length()) {
			return null;	// Pointless.
		}

		// First check if the second quarter is the seed for a half-match.
		String[] hm1 = diff_halfMatchI(longtext, shorttext,
																	 (longtext.length() + 3) / 4);
		// Check again based on the third quarter.
		String[] hm2 = diff_halfMatchI(longtext, shorttext,
																	 (longtext.length() + 1) / 2);
		String[] hm;
		if (hm1 == null && hm2 == null) {
			return null;
		} else if (hm2 == null) {
			hm = hm1;
		} else if (hm1 == null) {
			hm = hm2;
		} else {
			// Both matched.	Select the longest.
			hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
		}

		// A half-match was found, sort out the return data.
		if (text1.length() > text2.length()) {
			return hm;
			//return new String[]{hm[0], hm[1], hm[2], hm[3], hm[4]};
		} else {
			return new String[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
		}
	}

	/**
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
	private static String[] diff_halfMatchI(String longtext, String shorttext, int i) {
		// Start with a 1/4 length substring at position i as a seed.
		String seed = longtext.substring(i, i + longtext.length() / 4);
		int j = -1;
		String best_common = "";
		String best_longtext_a = "", best_longtext_b = "";
		String best_shorttext_a = "", best_shorttext_b = "";
		while ((j = shorttext.indexOf(seed, j + 1)) != -1) {
			int prefixLength = diff_commonPrefix(longtext.substring(i),
																					 shorttext.substring(j));
			int suffixLength = diff_commonSuffix(longtext.substring(0, i),
																					 shorttext.substring(0, j));
			if (best_common.length() < suffixLength + prefixLength) {
				best_common = shorttext.substring(j - suffixLength, j)
						+ shorttext.substring(j, j + prefixLength);
				best_longtext_a = longtext.substring(0, i - suffixLength);
				best_longtext_b = longtext.substring(i + prefixLength);
				best_shorttext_a = shorttext.substring(0, j - suffixLength);
				best_shorttext_b = shorttext.substring(j + prefixLength);
			}
		}
		if (best_common.length() * 2 >= longtext.length()) {
			return new String[]{best_longtext_a, best_longtext_b,
													best_shorttext_a, best_shorttext_b, best_common};
		} else {
			return null;
		}
	}

	/**
	 * Reduce the number of edits by eliminating semantically trivial equalities.
	 * @author fraser@google.com (Neil Fraser)
	 * @param diffs LinkedList of Diff objects.
	 */
	private static void diff_cleanupSemantic(LinkedList<Diff> diffs) {
		if (diffs.isEmpty()) {
			return;
		}
		boolean changes = false;
		Stack<Diff> equalities = new Stack<Diff>();	// Stack of qualities.
		String lastequality = null; // Always equal to equalities.lastElement().text
		ListIterator<Diff> pointer = diffs.listIterator();
		// Number of characters that changed prior to the equality.
		int length_insertions1 = 0;
		int length_deletions1 = 0;
		// Number of characters that changed after the equality.
		int length_insertions2 = 0;
		int length_deletions2 = 0;
		Diff thisDiff = pointer.next();
		while (thisDiff != null) {
			if (thisDiff.operation == DiffOperation.EQUAL) {
				// Equality found.
				equalities.push(thisDiff);
				length_insertions1 = length_insertions2;
				length_deletions1 = length_deletions2;
				length_insertions2 = 0;
				length_deletions2 = 0;
				lastequality = thisDiff.text;
			} else {
				// An insertion or deletion.
				if (thisDiff.operation == DiffOperation.INSERT) {
					length_insertions2 += thisDiff.text.length();
				} else {
					length_deletions2 += thisDiff.text.length();
				}
				// Eliminate an equality that is smaller or equal to the edits on both
				// sides of it.
				if (lastequality != null && (lastequality.length()
						<= Math.max(length_insertions1, length_deletions1))
						&& (lastequality.length()
								<= Math.max(length_insertions2, length_deletions2))) {
					//System.out.println("Splitting: '" + lastequality + "'");
					// Walk back to offending equality.
					while (thisDiff != equalities.lastElement()) {
						thisDiff = pointer.previous();
					}
					pointer.next();

					// Replace equality with a delete.
					pointer.set(new Diff(DiffOperation.DELETE, lastequality));
					// Insert a corresponding an insert.
					pointer.add(new Diff(DiffOperation.INSERT, lastequality));

					equalities.pop();	// Throw away the equality we just deleted.
					if (!equalities.empty()) {
						// Throw away the previous equality (it needs to be reevaluated).
						equalities.pop();
					}
					if (equalities.empty()) {
						// There are no previous equalities, walk back to the start.
						while (pointer.hasPrevious()) {
							pointer.previous();
						}
					} else {
						// There is a safe equality we can fall back to.
						thisDiff = equalities.lastElement();
						while (thisDiff != pointer.previous()) {
							// Intentionally empty loop.
						}
					}

					length_insertions1 = 0;	// Reset the counters.
					length_insertions2 = 0;
					length_deletions1 = 0;
					length_deletions2 = 0;
					lastequality = null;
					changes = true;
				}
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}

		// Normalize the diff.
		if (changes) {
			diff_cleanupMerge(diffs);
		}
		diff_cleanupSemanticLossless(diffs);

		// Find any overlaps between deletions and insertions.
		// e.g: <del>abcxxx</del><ins>xxxdef</ins>
		//	 -> <del>abc</del>xxx<ins>def</ins>
		// e.g: <del>xxxabc</del><ins>defxxx</ins>
		//	 -> <ins>def</ins>xxx<del>abc</del>
		// Only extract an overlap if it is as big as the edit ahead or behind it.
		pointer = diffs.listIterator();
		Diff prevDiff = null;
		//thisDiff = null; // BZ eclipse said this was redundant?!
		if (pointer.hasNext()) {
			prevDiff = pointer.next();
			if (pointer.hasNext()) {
				thisDiff = pointer.next();
			}
		}
		while (thisDiff != null) {
			if (prevDiff.operation == DiffOperation.DELETE &&
					thisDiff.operation == DiffOperation.INSERT) {
				String deletion = prevDiff.text;
				String insertion = thisDiff.text;
				int overlap_length1 = diff_commonOverlap(deletion, insertion);
				int overlap_length2 = diff_commonOverlap(insertion, deletion);
				if (overlap_length1 >= overlap_length2) {
					if (overlap_length1 >= deletion.length() / 2.0 ||
							overlap_length1 >= insertion.length() / 2.0) {
						// Overlap found. Insert an equality and trim the surrounding edits.
						pointer.previous();
						pointer.add(new Diff(DiffOperation.EQUAL,
																 insertion.substring(0, overlap_length1)));
						prevDiff.text =
								deletion.substring(0, deletion.length() - overlap_length1);
						thisDiff.text = insertion.substring(overlap_length1);
						// pointer.add inserts the element before the cursor, so there is
						// no need to step past the new element.
					}
				} else {
					if (overlap_length2 >= deletion.length() / 2.0 ||
							overlap_length2 >= insertion.length() / 2.0) {
						// Reverse overlap found.
						// Insert an equality and swap and trim the surrounding edits.
						pointer.previous();
						pointer.add(new Diff(DiffOperation.EQUAL,
																 deletion.substring(0, overlap_length2)));
						prevDiff.operation = DiffOperation.INSERT;
						prevDiff.text =
							insertion.substring(0, insertion.length() - overlap_length2);
						thisDiff.operation = DiffOperation.DELETE;
						thisDiff.text = deletion.substring(overlap_length2);
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
	 * e.g: The c<ins>at c</ins>ame. -> The <ins>cat </ins>came.
	 * @author fraser@google.com (Neil Fraser)
	 * @param diffs LinkedList of Diff objects.
	 */
	private static void diff_cleanupSemanticLossless(LinkedList<Diff> diffs) {
		String equality1, edit, equality2;
		String commonString;
		int commonOffset;
		int score, bestScore;
		String bestEquality1, bestEdit, bestEquality2;
		// Create a new iterator at the start.
		ListIterator<Diff> pointer = diffs.listIterator();
		Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
		Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
		Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
		// Intentionally ignore the first and last element (don't need checking).
		while (nextDiff != null) {
			if (prevDiff.operation == DiffOperation.EQUAL &&
					nextDiff.operation == DiffOperation.EQUAL) {
				// This is a single edit surrounded by equalities.
				equality1 = prevDiff.text;
				edit = thisDiff.text;
				equality2 = nextDiff.text;

				// First, shift the edit as far left as possible.
				commonOffset = diff_commonSuffix(equality1, edit);
				if (commonOffset != 0) {
					commonString = edit.substring(edit.length() - commonOffset);
					equality1 = equality1.substring(0, equality1.length() - commonOffset);
					edit = commonString + edit.substring(0, edit.length() - commonOffset);
					equality2 = commonString + equality2;
				}

				// Second, step character by character right, looking for the best fit.
				bestEquality1 = equality1;
				bestEdit = edit;
				bestEquality2 = equality2;
				bestScore = diff_cleanupSemanticScore(equality1, edit)
						+ diff_cleanupSemanticScore(edit, equality2);
				while (edit.length() != 0 && equality2.length() != 0
						&& edit.charAt(0) == equality2.charAt(0)) {
					equality1 += edit.charAt(0);
					edit = edit.substring(1) + equality2.charAt(0);
					equality2 = equality2.substring(1);
					score = diff_cleanupSemanticScore(equality1, edit)
							+ diff_cleanupSemanticScore(edit, equality2);
					// The >= encourages trailing rather than leading whitespace on edits.
					if (score >= bestScore) {
						bestScore = score;
						bestEquality1 = equality1;
						bestEdit = edit;
						bestEquality2 = equality2;
					}
				}

				if (!prevDiff.text.equals(bestEquality1)) {
					// We have an improvement, save it back to the diff.
					if (bestEquality1.length() != 0) {
						prevDiff.text = bestEquality1;
					} else {
						pointer.previous(); // Walk past nextDiff.
						pointer.previous(); // Walk past thisDiff.
						pointer.previous(); // Walk past prevDiff.
						pointer.remove(); // Delete prevDiff.
						pointer.next(); // Walk past thisDiff.
						pointer.next(); // Walk past nextDiff.
					}
					thisDiff.text = bestEdit;
					if (bestEquality2.length() != 0) {
						nextDiff.text = bestEquality2;
					} else {
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
	private static int diff_cleanupSemanticScore(String one, String two) {
		if (one.length() == 0 || two.length() == 0) {
			// Edges are the best.
			return 6;
		}

		// Each port of this function behaves slightly differently due to
		// subtle differences in each language's definition of things like
		// 'whitespace'.	Since this function's purpose is largely cosmetic,
		// the choice has been made to use each language's native features
		// rather than force total conformity.
		char char1 = one.charAt(one.length() - 1);
		char char2 = two.charAt(0);
		boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
		boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
		boolean whitespace1 = nonAlphaNumeric1 && Character.isWhitespace(char1);
		boolean whitespace2 = nonAlphaNumeric2 && Character.isWhitespace(char2);
		boolean lineBreak1 = whitespace1
				&& Character.getType(char1) == Character.CONTROL;
		boolean lineBreak2 = whitespace2
				&& Character.getType(char2) == Character.CONTROL;
		boolean blankLine1 = lineBreak1 && BLANKLINEEND.matcher(one).find();
		boolean blankLine2 = lineBreak2 && BLANKLINESTART.matcher(two).find();

		if (blankLine1 || blankLine2) {
			// Five points for blank lines.
			return 5;
		} else if (lineBreak1 || lineBreak2) {
			// Four points for line breaks.
			return 4;
		} else if (nonAlphaNumeric1 && !whitespace1 && whitespace2) {
			// Three points for end of sentences.
			return 3;
		} else if (whitespace1 || whitespace2) {
			// Two points for whitespace.
			return 2;
		} else if (nonAlphaNumeric1 || nonAlphaNumeric2) {
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
	private static void diff_cleanupMerge(LinkedList<Diff> diffs) {
		diffs.add(new Diff(DiffOperation.EQUAL, ""));	// Add a dummy entry at the end.
		ListIterator<Diff> pointer = diffs.listIterator();
		int count_delete = 0;
		int count_insert = 0;
		String text_delete = "";
		String text_insert = "";
		Diff thisDiff = pointer.next();
		Diff prevEqual = null;
		int commonlength;
		while (thisDiff != null) {
			switch (thisDiff.operation) {
			case INSERT:
				count_insert++;
				text_insert += thisDiff.text;
				prevEqual = null;
				break;
			case DELETE:
				count_delete++;
				text_delete += thisDiff.text;
				prevEqual = null;
				break;
			case EQUAL:
				if (count_delete + count_insert > 1) {
					boolean both_types = count_delete != 0 && count_insert != 0;
					// Delete the offending records.
					pointer.previous();	// Reverse direction.
					while (count_delete-- > 0) {
						pointer.previous();
						pointer.remove();
					}
					while (count_insert-- > 0) {
						pointer.previous();
						pointer.remove();
					}
					if (both_types) {
						// Factor out any common prefixies.
						commonlength = diff_commonPrefix(text_insert, text_delete);
						if (commonlength != 0) {
							if (pointer.hasPrevious()) {
								thisDiff = pointer.previous();
								assert thisDiff.operation == DiffOperation.EQUAL
											 : "Previous diff should have been an equality.";
								thisDiff.text += text_insert.substring(0, commonlength);
								pointer.next();
							} else {
								pointer.add(new Diff(DiffOperation.EQUAL,
										text_insert.substring(0, commonlength)));
							}
							text_insert = text_insert.substring(commonlength);
							text_delete = text_delete.substring(commonlength);
						}
						// Factor out any common suffixies.
						commonlength = diff_commonSuffix(text_insert, text_delete);
						if (commonlength != 0) {
							thisDiff = pointer.next();
							thisDiff.text = text_insert.substring(text_insert.length()
									- commonlength) + thisDiff.text;
							text_insert = text_insert.substring(0, text_insert.length()
									- commonlength);
							text_delete = text_delete.substring(0, text_delete.length()
									- commonlength);
							pointer.previous();
						}
					}
					// Insert the merged records.
					if (text_delete.length() != 0) {
						pointer.add(new Diff(DiffOperation.DELETE, text_delete));
					}
					if (text_insert.length() != 0) {
						pointer.add(new Diff(DiffOperation.INSERT, text_insert));
					}
					// Step forward to the equality.
					thisDiff = pointer.hasNext() ? pointer.next() : null;
				} else if (prevEqual != null) {
					// Merge this equality with the previous one.
					prevEqual.text += thisDiff.text;
					pointer.remove();
					thisDiff = pointer.previous();
					pointer.next();	// Forward direction
				}
				count_insert = 0;
				count_delete = 0;
				text_delete = "";
				text_insert = "";
				prevEqual = thisDiff;
				break;
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}
		if (diffs.getLast().text.length() == 0) {
			diffs.removeLast();	// Remove the dummy entry at the end.
		}

		/*
		 * Second pass: look for single edits surrounded on both sides by equalities
		 * which can be shifted sideways to eliminate an equality.
		 * e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
		 */
		boolean changes = false;
		// Create a new iterator at the start.
		// (As opposed to walking the current one back.)
		pointer = diffs.listIterator();
		Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
		thisDiff = pointer.hasNext() ? pointer.next() : null;
		Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
		// Intentionally ignore the first and last element (don't need checking).
		while (nextDiff != null) {
			if (prevDiff.operation == DiffOperation.EQUAL &&
					nextDiff.operation == DiffOperation.EQUAL) {
				// This is a single edit surrounded by equalities.
				if (thisDiff.text.endsWith(prevDiff.text)) {
					// Shift the edit over the previous equality.
					thisDiff.text = prevDiff.text
							+ thisDiff.text.substring(0, thisDiff.text.length()
																					 - prevDiff.text.length());
					nextDiff.text = prevDiff.text + nextDiff.text;
					pointer.previous(); // Walk past nextDiff.
					pointer.previous(); // Walk past thisDiff.
					pointer.previous(); // Walk past prevDiff.
					pointer.remove(); // Delete prevDiff.
					pointer.next(); // Walk past thisDiff.
					thisDiff = pointer.next(); // Walk past nextDiff.
					nextDiff = pointer.hasNext() ? pointer.next() : null;
					changes = true;
				} else if (thisDiff.text.startsWith(nextDiff.text)) {
					// Shift the edit over the next equality.
					prevDiff.text += nextDiff.text;
					thisDiff.text = thisDiff.text.substring(nextDiff.text.length())
							+ nextDiff.text;
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
		if (changes) {
			diff_cleanupMerge(diffs);
		}
	}

	/**
	 * Class representing one diff operation.
	 */
	public static class Diff {
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
		public Diff(DiffOperation operation, String text) {
			// Construct a diff with the specified operation and text.
			this.operation = operation;
			this.text = text;
		}

		/**
		 * Display a human-readable version of this Diff.
		 * @return text version.
		 */
		public String toString() {
			String prettyText = this.text.replace('\n', '\u00b6');
			return "Diff(" + this.operation + ",\"" + prettyText + "\")";
		}

		/**
		 * Create a numeric hash value for a Diff.
		 * This function is not used by DMP.
		 * @return Hash value.
		 */
		@Override
		public int hashCode() {
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
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Diff other = (Diff) obj;
			if (operation != other.operation) {
				return false;
			}
			if (text == null) {
				if (other.text != null) {
					return false;
				}
			} else if (!text.equals(other.text)) {
				return false;
			}
			return true;
		}
	}
}
