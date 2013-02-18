package com.planet_ink.coffee_mud.core;

import java.util.*;
import java.util.Map.Entry;

import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
	
	public final static String SPACES="                                                                                                                                          ";
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
		for(int c=0;c<str.length();c++)
			if(!Character.isUpperCase(str.charAt(c)))
				return false;
		return true;
	}
	
	public final static boolean isLowerCase(final String str) 
	{
		for(int c=0;c<str.length();c++)
			if(!Character.isLowerCase(str.charAt(c)))
				return false;
		return true;
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
			return new String(b,CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT));
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
			return str.getBytes(CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT));
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
	
	public static void convertHtmlToText(final StringBuffer finalData)
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
	
	public static void stripHeadHtmlTags(final StringBuffer finalData)
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
		return (StringExpToken)tokens.get(index[0]++);
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
		final int[] i = (int[]) index.clone();
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
		final int[] i = (int[]) index.clone();
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
		int[] i = (int[]) index.clone();
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
		i = (int[]) index.clone();
		final String leftValue = matchSimpleConst(tokens, i, variables);
		if (leftValue == null)
			return null;
		final int[] i2 = (int[]) i.clone();
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
		int[] i = (int[]) index.clone();
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
		i = (int[]) index.clone();
		final Double leftValue = matchSimpleNumber(tokens, i, variables);
		if (leftValue == null)
			return null;
		final int[] i2 = (int[]) i.clone();
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
		int[] i = (int[]) index.clone();
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
		i = (int[]) index.clone();
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
		int[] i = (int[]) index.clone();
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
		i = (int[]) index.clone();
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
		int[] i = (int[]) index.clone();
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
			i = (int[]) index.clone();
			leftExpression = matchStringEvaluation(tokens, i, variables);
			if(leftExpression == null) leftExpression = matchNumEvaluation(tokens, i, variables);
		}
		if (leftExpression == null) return null;
		final int[] i2 = (int[]) i.clone();
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
}
