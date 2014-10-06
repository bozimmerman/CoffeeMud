package com.planet_ink.coffee_mud.core;
import java.nio.ByteBuffer;
import java.util.*;

import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

/*
   Copyright 2005-2014 Bo Zimmerman

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
public class CMParms
{
	private CMParms(){super();}
	private static CMParms inst=new CMParms();
	public final static CMParms instance(){return inst;}
	
	public final static CMParms createDelimiter(final char[] chars)
	{
		final boolean[] delims=new boolean[256];
		for(char c : chars)
			delims[(byte)c]=true;
		return new CMParms()
		{
			@Override
			public boolean isDelimiter(final char c)
			{
				return delims[(byte)c];
			}
		};
	}
	
	public boolean isDelimiter(final char c)
	{
		return Character.isWhitespace(c);
	}

	public final static String combine(final List<?> commands)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		for(final Object o : commands)
			combined.append(o.toString()+" ");
		return combined.toString().trim();
	}

	public final static String combine(final List<?> commands, final int startAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			combined.append(commands.get(commandIndex).toString()+" ");
		return combined.toString().trim();
	}

	public final static String combine(final List<?> commands, final int startAt, final int endAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			combined.append(commands.get(commandIndex).toString()+" ");
		return combined.toString().trim();
	}

	public final static String combineWith(final List<?> commands, final char withChar, final int startAt, final int endAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			combined.append(commands.get(commandIndex).toString()+withChar);
		return combined.toString().trim();
	}

	public final static String combineWith(final List<?> commands, final char withChar)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		for(int commandIndex=0;commandIndex<commands.size();commandIndex++)
			combined.append(commands.get(commandIndex).toString()+withChar);
		return combined.toString().trim();
	}

	public final static String combineWithQuotes(final List<?> commands, final int startAt, final int endAt)
	{
		final StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				if(s.indexOf(' ')>=0) s="\""+s+"\"";
				Combined.append(s+" ");
			}
		}
		return Combined.toString().trim();
	}

	public final static String combineWith(final List<?> commands, final String withSeparator)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		for(int commandIndex=0;commandIndex<commands.size();commandIndex++)
			combined.append(commands.get(commandIndex).toString()+withSeparator);
		return combined.toString().trim();
	}

	public final static String[] combine(final String[] strs1, final String[] strs2)
	{
		final int strs1Len = strs1.length;
		final int strs2Len = strs2.length;
		final String[] array= new String[strs1Len+strs2Len];
		System.arraycopy(strs1, 0, array, 0, strs1Len);
		System.arraycopy(strs2, 0, array, strs1Len, strs2Len);
		return array;
	}

	public final static String quoteIfNecessary(final String str)
	{
		if(str==null) return str;
		if(str.indexOf(' ')>=0)
			return "\""+str+"\"";
		return str;
	}

	public final static String combineAfterIndexWithQuotes(final Vector<?> commands, final String match)
	{
		final StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=0;commandIndex<0;commandIndex++)
			{
				s=(String)commands.elementAt(commandIndex);
				if(s.indexOf(' ')>=0) s="\""+s+"\"";
				Combined.append(s+" ");
			}
		}
		return Combined.toString().trim();
	}

	public final static String combineWithQuotes(final List<?> commands, final int startAt)
	{
		final StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				if(s.indexOf(' ')>=0) s="\""+s+"\"";
				Combined.append(s+" ");
			}
		}
		return Combined.toString().trim();
	}

	public final static String combineWithTabs(final List<?> commands, final int startAt)
	{
		return combineWithX(commands,"\t",startAt);
	}

	public final static String combineWithX(final List<?> commands, final String X, final int startAt)
	{
		final StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				Combined.append(s+X);
			}
		}
		return Combined.toString().trim();
	}

	public final static String combine(final Set<?> flags)
	{
		final StringBuffer combined=new StringBuffer("");
		if(flags!=null)
			for (final Object name : flags)
				combined.append(name.toString()+" ");
		return combined.toString().trim();
	}

	public final static List<String> paramParse(final String str)
	{
		final List<String> commands=parse(str);
		String s;
		for(int i=0;i<commands.size();i++)
		{
			s=commands.get(i);
			if(s.startsWith("=")&&(s.length()>1)&&(i>0))
			{
				final String prev=commands.get(i-1);
				commands.set(i-1,prev+s);
				commands.remove(i);
				i--;
			}
			else
			if(s.endsWith("=")&&(s.length()>1)&&(i<(commands.size()-1)))
			{
				final String next=commands.get(i+1);
				commands.set(i,s+next);
				commands.remove(i+1);
			}
			else
			if(s.equals("=")&&((i>0)&&(i<(commands.size()-1))))
			{
				final String prev=commands.get(i-1);
				final String next=commands.get(i+1);
				commands.set(i-1,prev+"="+next);
				commands.remove(i);
				commands.remove(i+1);
				i--;
			}
		}
		return commands;
	}

	public final static Vector<String> parse(final String str)
	{
		return parse(str,-1);
	}

	public final static Vector<String> parse(final String str, final int upTo)
	{
		final Vector<String> commands=new Vector<String>();
		if(str==null) return commands;
		final StringBuilder s=new StringBuilder();
		final char[] cs=str.toCharArray();
		int state=0;
		for(final char c : cs)
			switch(state)
			{
			case 0:
			{
				if(c=='\"')
					state=2;
				else
				if(!Character.isWhitespace(c))
				{
					s.append(c);
					state=1;
				}
				break;
			}
			case 1:
			{
				if(Character.isWhitespace(c))
				{
					if(s.length()>0)
						commands.add(s.toString());
					s.setLength(0);
					state=0;
				}
				else
					s.append(c);
				break;
			}
			case 2:
			{
				if(c=='\"')
				{
					commands.add(s.toString());
					s.setLength(0);
					state=0;
				}
				else
					s.append(c);
				break;
			}
			}
		if(s.length()>0)
			commands.add(s.toString());
		return commands;
	}

	public final static List<String> parseCommas(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,',',ignoreNulls);
	}

	public final static List<String> parseCommandFlags(final String s, final String[] flags)
	{
		if((s==null)||(s.length()==0)) return new Vector<String>(1);
		final List<String> V=parseCommas(s,true);
		final Vector<String> finalV=new Vector<String>(V.size());
		int index;
		for(final String flag : V)
		{
			index=CMParms.indexOfIgnoreCase(flags, flag);
			if(index>=0)
				finalV.addElement(flags[index]);
		}
		return V;
	}

	public final static List<String> parseTabs(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,'\t',ignoreNulls);
	}

	public final static List<String> parseAny(final String s, final String delimeter, final boolean ignoreNulls)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.length()==0)) return V;
		if((delimeter==null)||(delimeter.length()==0))
		{
			V.add(s);
			return V;
		}
		int last=0;
		final char firstChar=delimeter.charAt(0);
		for(int i=0;i<s.length()-delimeter.length()+1;i++)
			if((s.charAt(i)==firstChar)
			&&(s.substring(i,i+delimeter.length()).equals(delimeter)))
			{
				final String sub=s.substring(last,i).trim();
				last=i+delimeter.length();
				i+=delimeter.length()-1;
				if(!ignoreNulls||(sub.length()>0))
					V.add(sub);
			}
		final String sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(!ignoreNulls||(sub.length()>0))
			V.add(sub);
		return V;
	}

	public final static List<String> parseAnyWords(final String s, final String delimeter, final boolean ignoreNulls)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.length()==0)) return V;
		if((delimeter==null)||(delimeter.length()==0))
		{
			V.add(s);
			return V;
		}
		int last=0;
		final String upDelimeter=delimeter.toUpperCase();
		final char firstChar=upDelimeter.charAt(0);
		final String tests=s.toUpperCase();
		for(int i=0;i<tests.length()-upDelimeter.length()+1;i++)
			if((tests.charAt(i)==firstChar)
			&&(tests.substring(i,i+upDelimeter.length()).equals(upDelimeter)))
			{
				final String sub=s.substring(last,i).trim();
				last=i+upDelimeter.length();
				i+=upDelimeter.length()-1;
				if(!ignoreNulls||(sub.length()>0))
					V.add(sub);
			}
		final String sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(!ignoreNulls||(sub.length()>0))
			V.add(sub);
		return V;
	}

	public final static List<String> parseAny(final String s, final char delimiter, final boolean ignoreNulls)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.length()==0)) return V;
		int last=0;
		String sub;
		for(int i=0;i<s.length();i++)
			if(s.charAt(i)==delimiter)
			{
				sub=s.substring(last,i).trim();
				last=i+1;
				if(!ignoreNulls||(sub.length()>0))
					V.add(sub);
			}
		sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(!ignoreNulls||(sub.length()>0))
			V.add(sub);
		return V;
	}

	public final static List<String> parseSquiggles(final String s)
	{
		return parseAny(s,'~',false);
	}

	public final static List<String> parseSentences(final String s)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.length()==0)) return V;
		int last=0;
		String sub;
		for(int i=0;i<s.length();i++)
			if(s.charAt(i)=='.')
			{
				sub=s.substring(last,i+1).trim();
				last=i+1;
				V.add(sub);
			}
		sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(sub.length()>0)
			V.add(sub);
		return V;
	}

	public final static List<String> parseSquiggleDelimited(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,'~',ignoreNulls);
	}

	public final static List<String> parseSemicolons(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,';',ignoreNulls);
	}

	public final static List<String> parseSpaces(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,' ',ignoreNulls);
	}

	public final static int numBits(final String s)
	{
		return ((Integer)getBitWork(s,Integer.MAX_VALUE,2)).intValue();
	}

	public final static String cleanBit(String s)
	{
		if(s.length()==0)
			return s;
		if((s.charAt(0)==' ')||(s.charAt(s.length()-1)==' '))
			s=s.trim();
		if(s.length()<2)
			return s.replace('\'','`');
		if(s.charAt(0)=='\'')
		{
			if(s.charAt(s.length()-1)=='\'')
				return s.substring(1,s.length()-1).replace('\'','`');
			return s.substring(1).replace('\'','`');
		}
		if(s.charAt(0)=='`')
		{
			if(s.charAt(s.length()-1)=='`')
				return s.substring(1,s.length()-1).replace('\'','`');
			return s.substring(1).replace('\'','`');
		}
		return s.replace('\'','`');
	}

	public final static String getCleanBit(final String s, final int which)
	{
		return cleanBit(getBit(s,which));
	}

	public final static String getPastBitClean(final String s, final int which)
	{
		return cleanBit(getPastBit(s,which));
	}

	public final static String getPastBit(final String s, final int which)
	{
		return (String)getBitWork(s,which,1);
	}

	public final static String getBit(final String s, final int which)
	{
		return (String)getBitWork(s,which,0);
	}

	public final static Object getBitWork(final String s, final int which, final int op)
	{
		int currOne=0;
		int start=-1;
		char q=' ';
		final char[] cs=s.toCharArray();
		for(int c=0;c<cs.length;c++)
			switch(start)
			{
			case -1:
				switch(cs[c])
				{
				case ' ': case '\t': case '\n': case '\r':
					break;
				case '\'': case '`':
					q=cs[c];
					start=c;
					break;
				default:
					q=' ';
					start=c;
					break;
				}
				break;
			default:
				if((cs[c]==q)||(q==' ' && cs[c]=='\t'))
				{
					if((q!=' ')
					&&(c<cs.length-1)
					&&(!Character.isWhitespace(cs[c+1])))
						break;
					if(which==currOne)
					{
						switch(op)
						{
						case 0:
							if(q==' ')
								return new String(cs,start,c-start);
							return new String(cs,start,c-start+1);
						case 1:
							return new String(cs,c+1,cs.length-c-1).trim();
						}
					}
					currOne++;
					start=-1;
				}
				break;
			}
		switch(op)
		{
		case 0:
			if(start<0)
				return "";
			return new String(cs,start,cs.length-start);
		case 1:
			return "";
		default:
			if(start<0)
				return Integer.valueOf(currOne);
			return Integer.valueOf(currOne+1);
		}
	}

	public final static String getParmStr(String text, final String key, final String defaultVal)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
				{
					if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
						return defaultVal;
					x++;
				}
				if(x<text.length())
				{
					boolean endWithQuote=false;
					while((x<text.length())&&(!Character.isLetterOrDigit(text.charAt(x))))
					{
						if(text.charAt(x)=='\"')
						{
							endWithQuote=true;
							x++;
							break;
						}
						x++;
					}
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
							&&((!endWithQuote)&&(!Character.isWhitespace(text.charAt(x)))&&(text.charAt(x)!=';')&&(text.charAt(x)!=','))
							||((endWithQuote)&&(text.charAt(x)!='\"')))
								x++;
						return text.substring(0,x).trim();
					}

				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultVal;
	}

	private static int[] makeIntArray(final int x, final int y)
	{
		final int[] xy=new int[2];
		xy[0]=x;
		xy[1]=y;
		return xy;
	}

	public final static int[] getParmCompare(String text, final String key, final int value)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())
					&&(text.charAt(x)!='>')
					&&(text.charAt(x)!='<')
					&&(text.charAt(x)!='!')
					&&(text.charAt(x)!='='))
					x++;
				if(x<text.length()-1)
				{
					final char comp=text.charAt(x);
					boolean andEqual=(text.charAt(x)=='=');
					if(text.charAt(x+1)=='='){ x++; andEqual=true;}
					if(x<text.length()-1)
					{
						while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
							x++;
						if(x<text.length())
						{
							text=text.substring(x);
							x=0;
							while((x<text.length())&&(Character.isDigit(text.charAt(x))))
								x++;
							final int found=CMath.s_int(text.substring(0,x));
							if(andEqual&&(found==value))
								return makeIntArray(comp,(comp=='!')?-1:1);
							switch(comp)
							{
								case '>': return makeIntArray(comp,(value>found)?1:-1);
								case '<': return makeIntArray(comp,(value<found)?1:-1);
								case '!': makeIntArray(comp,1);
							}
							return makeIntArray(comp,0);
						}
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return makeIntArray('\0',0);
	}

	private static int strIndex(final Vector<String> V, final String str, final int start)
	{
		if(str.indexOf(' ')<0) return V.indexOf(str,start);
		final List<String> V2=CMParms.parse(str);
		if(V2.size()==0) return -1;
		int x=V.indexOf(V2.get(0),start);
		boolean found=false;
		while((x>=0)&&((x+V2.size())<=V.size())&&(!found))
		{
			found=true;
			for(int v2=1;v2<V2.size();v2++)
				if(!V.get(x+v2).equals(V2.get(v2)))
				{ found=false; break;}
			if(!found) x=V.indexOf(V2.get(0),x+1);
		}
		if(found) return x;
		return -1;
	}

	private static int stringContains(final Vector<String> V, final char combiner, final StringBuffer buf, int lastIndex)
	{
		final String str=buf.toString().trim();
		if(str.length()==0) return lastIndex;
		buf.setLength(0);
		switch(combiner)
		{
		case '&':
			lastIndex=strIndex(V,str,0);
			return lastIndex;
		case '|':
			if(lastIndex>=0) return lastIndex;
			return strIndex(V,str,0);
		case '>':
			if(lastIndex<0) return lastIndex;
			return strIndex(V,str,lastIndex<0?0:lastIndex+1);
		case '<':
		{
			if(lastIndex<0) return lastIndex;
			final int newIndex=strIndex(V,str,0);
			if(newIndex<lastIndex) return newIndex;
			return -1;
		}
		}
		return -1;
	}
	private static int stringContains(final Vector<String> V, final char[] str, final int[] index, final int depth)
	{
		final StringBuffer buf=new StringBuffer("");
		int lastIndex=0;
		boolean quoteMode=false;
		char combiner='&';
		for(int i=index[0];i<str.length;i++)
		{
			switch(str[i])
			{
			case ')':
				if((depth>0)&&(!quoteMode))
				{
					index[0]=i;
					return stringContains(V,combiner,buf,lastIndex);
				}
				buf.append(str[i]);
				break;
			case ' ':
				buf.append(str[i]);
				break;
			case '&':
			case '|':
			case '>':
			case '<':
				if(quoteMode)
					buf.append(str[i]);
				else
				{
					lastIndex=stringContains(V,combiner,buf,lastIndex);
					combiner=str[i];
				}
				break;
			case '(':
				if(!quoteMode)
				{
					lastIndex=stringContains(V,combiner,buf,lastIndex);
					index[0]=i+1;
					final int newIndex=stringContains(V,str,index,depth+1);
					i=index[0];
					switch(combiner)
					{
					case '&':
						if((lastIndex<0)||(newIndex<0))
							lastIndex=-1;
						break;
					case '|':
						if(newIndex>=0)
							lastIndex=newIndex;
						break;
					case '>':
						if(newIndex<=lastIndex)
							lastIndex=-1;
						else
							lastIndex=newIndex;
						break;
					case '<':
						if((newIndex<0)||(newIndex>=lastIndex))
							lastIndex=-1;
						else
							lastIndex=newIndex;
						break;
					}
				}
				else
					buf.append(str[i]);
				break;
			case '\"':
				quoteMode=(!quoteMode);
				break;
			case '\\':
				if(i<str.length-1)
				{
					buf.append(str[i+1]);
					i++;
				}
				break;
			default:
				if(Character.isLetter(str[i]))
					buf.append(Character.toLowerCase(str[i]));
				else
					buf.append(str[i]);
				break;
			}
		}
		return stringContains(V,combiner,buf,lastIndex);
	}

	public final static Map<String,String> parseEQParms(final String str, final String[] parmList)
	{
		final Hashtable<String,String> h=new Hashtable<String,String>();
		int lastEQ=-1;
		String lastParm=null;
		char c;
		for(int x=0;x<str.length();x++)
		{
			c=Character.toUpperCase(str.charAt(x));
			if(Character.isLetter(c))
			{
				for (final String element : parmList)
				{
					if((Character.toUpperCase(element.charAt(0)) == c)
					&&((str.length()-x) >= element.length())
					&&(str.substring(x,x+element.length()).equalsIgnoreCase(element)))
					{
						int chkX=x+element.length();
						while((chkX<str.length())&&(Character.isWhitespace(str.charAt(chkX))))
							chkX++;
						if((chkX<str.length())&&(str.charAt(chkX)=='='))
						{
							chkX++;
							if((lastParm!=null)&&(lastEQ>0))
							{
								String val=str.substring(lastEQ,x).trim();
								if(val.startsWith("\"")&&(val.endsWith("\"")))
									val=val.substring(1,val.length()-1).trim();
								h.put(lastParm,val);
							}
							lastParm=element;
							x=chkX;
							lastEQ=chkX;
						}
					}
				}
			}
		}
		if((lastParm!=null)&&(lastEQ>0))
		{
			String val=str.substring(lastEQ).trim();
			if(val.startsWith("\"")&&(val.endsWith("\"")))
				val=val.substring(1,val.length()-1).trim();
			h.put(lastParm,val);
		}
		return h;
	}

	public final static String combineEQParms(final Map<String,String> parms, final char delimiter)
	{
		final StringBuilder str=new StringBuilder("");
		for(final String key : parms.keySet())
		{
			final String val=parms.get(key);
			if(val.indexOf(' ')>0)
				str.append(key).append("=\"").append(parms.get(key)).append('\"').append(delimiter);
			else
				str.append(key).append('=').append(parms.get(key)).append(delimiter);
		}
		if(str.length()==0)
			return "";
		return str.substring(0,str.length()-1);
	}
	
	public final static Map<String,String> parseEQParms(final String parms)
	{
		return parseEQParms(parms,instance());
	}
	
	public final static Map<String,String> parseEQParms(final String parms, final CMParms delimiterCheck)
	{
		final Map<String,String> h=new Hashtable<String,String>();
		int state=0;
		int start=-1;
		String parmName=null;
		int lastPossibleStart=-1;
		boolean lastWasWhitespace=false;
		final StringBuffer str=new StringBuffer(parms);
		for(int x=0;x<=str.length();x++)
		{
			final char c=(x==str.length())?'\n':str.charAt(x);
			switch(state)
			{
			case 0:
				if((c=='_')||(Character.isLetter(c)))
				{
					start=x;
					state=1;
					parmName=null;
				}
				break;
			case 1:
				if(c=='=')
				{
					parmName=str.substring(start,x).toUpperCase().trim();
					state=2;
				}
				else
				if(delimiterCheck.isDelimiter(c))
				{
					if((!Character.isWhitespace(c))&&(x<str.length()))
						str.setCharAt(x,' '); // has to be trimmable
					parmName=str.substring(start,x).toUpperCase().trim();
					start=x;
				}
				break;
			case 2:
				if((c=='\"')||(c=='\n'))
				{
					state=3;
					start=x+1;
					lastPossibleStart=start;
				}
				else
				if(c=='=')
				{ // do nothing, this is a do-over
				}
				else
				if(!delimiterCheck.isDelimiter(c))
				{
					lastWasWhitespace=false;
					state=4;
					start=x;
					lastPossibleStart=start;
				}
				else
				if(!Character.isWhitespace(c))
					str.setCharAt(x,' '); // has to be trimmable
				break;
			case 3:
				if(c=='\\')
					str.deleteCharAt(x);
				else
				if(c=='\"')
				{
					state=0;
					h.put(parmName,str.substring(start,x));
					parmName=null;
				}
				break;
			case 4:
				if(c=='\\')
					str.deleteCharAt(x);
				else
				if(c=='=')
				{
					final String value=str.substring(start,x).trim();
					if(value.length()==0)
						state=2;
					else
					{
						h.put(parmName,str.substring(start,lastPossibleStart).trim());
						parmName=str.substring(lastPossibleStart,x).toUpperCase().trim();
						state=2;
					}
				}
				else
				if(c=='\n')
				{
					state=0;
					h.put(parmName,str.substring(start,x));
					parmName=null;
				}
				else
				if(delimiterCheck.isDelimiter(c))
				{
					if(!Character.isWhitespace(c))
						str.setCharAt(x,' '); // has to be trimmable
					lastWasWhitespace=true;
				}
				else
				if(lastWasWhitespace)
				{
					lastWasWhitespace=false;
					lastPossibleStart=x;
				}
				break;
			}
		}
		return h;
	}

	public final static Map<String,String> parseEQParms(final List<String> parms, final int start, final int end)
	{
		final Map<String,String> h=new Hashtable<String,String>();
		for(final Object O : parms)
			h.putAll(parseEQParms((String)O));
		return h;
	}

	public final static List<List<String>> parseDoubleDelimited(String text, final char delim1, final char delim2)
	{
		final List<String> preparseV=new Vector<String>();
		int y=0;
		while((text!=null)&&(text.length()>0))
		{
			y=text.indexOf(delim1);
			while((y>0)&&(text.charAt(y-1)=='\\'))
				y=text.indexOf(delim1,y+1);
			String script="";
			if(y<0)
			{
				script=text.trim();
				text="";
			}
			else
			{
				script=text.substring(0,y).trim();
				text=text.substring(y+1).trim();
			}
			if(script.length()>0)
				preparseV.add(script);
		}
		final List<List<String>> parsedV=new Vector<List<String>>();
		for(String s : preparseV)
		{
			final List<String> groupV=new Vector<String>();
			while(s.length()>0)
			{
				y=-1;
				int yy=0;
				while(yy<s.length())
					if((s.charAt(yy)==delim2)&&((yy<=0)||(s.charAt(yy-1)!='\\'))) {y=yy;break;}
					else
					if(s.charAt(yy)=='\n'){y=yy;break;}
					else
					if(s.charAt(yy)=='\r'){y=yy;break;}
					else yy++;
				String cmd="";
				if(y<0)
				{
					cmd=s.trim();
					s="";
				}
				else
				{
					cmd=s.substring(0,y).trim();
					s=s.substring(y+1).trim();
				}
				if((cmd.length()>0)&&(!cmd.startsWith("#")))
				{
					cmd=CMStrings.replaceAll(cmd,"\\"+delim1,""+delim1);
					cmd=CMStrings.replaceAll(cmd,"\\=","=");
					groupV.add(CMStrings.replaceAll(cmd,"\\"+delim2,""+delim2));
				}
			}
			if(groupV.size()>0)
				parsedV.add(groupV);
		}
		return parsedV;
	}

	public final static int stringContains(final String str1, final String str2)
	{
		final StringBuffer buf1=new StringBuffer(str1.toLowerCase());
		for(int i=buf1.length()-1;i>=0;i--)
		{
			if((buf1.charAt(i)!=' ')
			&&(buf1.charAt(i)!='\'')
			&&(buf1.charAt(i)!='\"')
			&&(buf1.charAt(i)!='`')
			&&(!Character.isLetterOrDigit(buf1.charAt(i))))
				buf1.setCharAt(i,' ');
		}
		final Vector<String> V=CMParms.parse(buf1.toString());
		return stringContains(V,str2.toCharArray(),new int[]{0},0);
	}

	public final static int getParmPlus(String text, final String key)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
				{
					if(text.charAt(x)=='=')
						return 0;
					x++;
				}
				if(x<text.length())
				{
					final char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return CMath.s_int(text.substring(0,x));
						return -CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}

	public final static double getParmDoublePlus(String text, final String key)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
				{
					if(text.charAt(x)=='=')
						return 0.0;
					x++;
				}
				if(x<text.length())
				{
					final char pm=text.charAt(x);
					while((x<text.length())
					&&(!Character.isDigit(text.charAt(x)))
					&&(text.charAt(x)!='.'))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
						&&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
							x++;
						if(text.substring(0,x).indexOf('.')<0)
						{
							if(pm=='+')
								return CMath.s_int(text.substring(0,x));
							return (-CMath.s_int(text.substring(0,x)));
						}
						if(pm=='+')
							return CMath.s_double(text.substring(0,x));
						return -CMath.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0.0;
	}

	public final static double getParmDouble(String text, final String key, final double defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if(x<text.length())
				{
					while((x<text.length())
					&&(!Character.isDigit(text.charAt(x)))
					&&(text.charAt(x)!='.'))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
						&&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
							x++;
						if(text.substring(0,x).indexOf('.')<0)
							return CMath.s_long(text.substring(0,x));
						return CMath.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}


	public final static int getParmInt(String text, final String key, final int defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
				{
					if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
						return defaultValue;
					x++;
				}
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	public final static boolean getParmBool(String text, final String key, final boolean defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		String s;
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					s=text.substring(x+1).trim();
					if(Character.toUpperCase(s.charAt(0))=='T') return true;
					if(Character.toUpperCase(s.charAt(0))=='T') return false;
				}
			}
			x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	public final static String[] toStringArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final String[] s=new String[0];
			return s;
		}
		final String[] s=new String[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=V.get(v).toString();
		return s;
	}

	public final static Set<String> toStringSet(final List<?> V, final Set<String> S)
	{
		if((V==null)||(V.size()==0))
		{
			return S;
		}
		for(int v=0;v<V.size();v++)
			S.add(V.get(v).toString());
		return S;
	}

	public final static String[] toStringArray(final Object[] O)
	{
		if(O==null) return new String[0];
		final String[] s=new String[O.length];
		for(int o=0;o<O.length;o++)
			s[o]=(O[o]!=null)?O[o].toString():"";
		return s;
	}

	public final static long[] toLongArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final long[] s=new long[0];
			return s;
		}
		final long[] s=new long[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_long(V.get(v).toString());
		return s;
	}

	public final static double[] toDoubleArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final double[] s=new double[0];
			return s;
		}
		final double[] s=new double[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_double(V.get(v).toString());
		return s;
	}

	public final static int[] toIntArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final int[] s=new int[0];
			return s;
		}
		final int[] s=new int[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_int(V.get(v).toString());
		return s;
	}

	public final static String toSemicolonList(final byte[] bytes)
	{
		final StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(Byte.toString(bytes[b])+(b<(bytes.length-1)?";":""));
		return str.toString();
	}

	public final static String toSemicolonList(final String[] bytes)
	{
		final StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(bytes[b]+(b<(bytes.length-1)?";":""));
		return str.toString();
	}

	public final static String toSemicolonList(final Object[] bytes)
	{
		final StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(bytes[b]+(b<(bytes.length-1)?";":""));
		return str.toString();
	}

	public final static String toSemicolonList(final Enumeration<?> bytes)
	{
		final StringBuffer str=new StringBuffer("");
		Object o;
		for(;bytes.hasMoreElements();)
		{
			o=bytes.nextElement();
			str.append(o.toString()+(bytes.hasMoreElements()?";":""));
		}
		return str.toString();
	}

	public final static String toSemicolonList(final List<?> bytes)
	{
		if((bytes==null)||(bytes.size()==0)) return "";
		final StringBuffer str=new StringBuffer(""+bytes.get(0));
		for(int b=1;b<bytes.size();b++)
			str.append(';').append(""+bytes.get(b));
		return str.toString();
	}

	public final static String toSafeSemicolonList(final List<?> list)
	{
		return toSafeSemicolonList(list.toArray());
	}

	public final static String toSafeSemicolonList(final Object[] list)
	{
		final StringBuffer buf1=new StringBuffer("");
		StringBuffer s=null;
		for(int l=0;l<list.length;l++)
		{
			s=new StringBuffer(list[l].toString());
			for(int i=0;i<s.length();i++)
				switch(s.charAt(i))
				{
				case '\\':
				case ';':
					s.insert(i,'\\');
					i++;
					break;
				}
			buf1.append(s.toString());
			if(l<list.length-1)
				buf1.append(';');
		}
		return buf1.toString();
	}

	public final static List<String> parseSafeSemicolonList(final String list, final boolean ignoreNulls)
	{
		if(list==null) return new Vector<String>();
		final StringBuffer buf1=new StringBuffer(list);
		int lastDex=0;
		final Vector<String> V=new Vector<String>();
		for(int l=0;l<buf1.length();l++)
			switch(buf1.charAt(l))
			{
			case '\\':
				buf1.delete(l,l+1);
				break;
			case ';':
				if((!ignoreNulls)||(lastDex<l))
					V.addElement(buf1.substring(lastDex,l));
				lastDex=l+1;
				break;
			}
		if((!ignoreNulls)||(lastDex<buf1.length()))
			V.addElement(buf1.substring(lastDex,buf1.length()));
		return V;
	}

	public final static byte[] fromByteList(final String str)
	{
		final List<String> V=CMParms.parseSemicolons(str,true);
		if(V.size()>0)
		{
			final byte[] bytes=new byte[V.size()];
			for(int b=0;b<V.size();b++)
				bytes[b]=Byte.parseByte(V.get(b));
			return bytes;
		}
		return new byte[0];
	}

	public final static String[] toStringArray(final Set<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final String[] s=new String[0];
			return s;
		}
		final String[] s=new String[V.size()];
		int v=0;
		for (final Object name : V)
			s[v++]=(name).toString();
		return s;
	}

	public final static List<String> toNameVector(final Set<? extends Environmental> V)
	{
		final Vector<String> s=new Vector<String>();
		if((V==null)||(V.size()==0))
			return s;
		for (final Environmental environmental : V)
			s.add(environmental.name());
		return s;
	}

	public final static List<String> toNameVector(final Enumeration<? extends Environmental> V)
	{
		final Vector<String> s=new Vector<String>();
		if(V==null) return s;
		for(;V.hasMoreElements();)
			s.add(V.nextElement().name());
		return s;
	}

	@SuppressWarnings("rawtypes")
	public final static String toString(final Object o)
	{
		if(o==null) return "null";
		if(o instanceof String) return (String)o;
		if(o instanceof List) return toStringList((List)o);
		if(o instanceof String[]) return toStringList((String[])o);
		if(o instanceof Enumeration) return toStringList((Enumeration)o);
		return o.toString();
	}

	public final static String toStringList(final String[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	public final static String toStringList(final Object[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	public final static String toStringList(final Enumeration<?> e)
	{
		if(!e.hasMoreElements()) return "";
		final Object o=e.nextElement();
		final StringBuilder s=new StringBuilder(""+o);
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement());
		return s.toString();
	}

	public final static String toEnvironmentalStringList(final Enumeration<? extends Environmental> e)
	{
		if(!e.hasMoreElements()) return "";
		final Environmental o=e.nextElement();
		final StringBuilder s=new StringBuilder(o.name());
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement().name());
		return s.toString();
	}

	public final static String toCMObjectStringList(final Enumeration<? extends CMObject> e)
	{
		if(!e.hasMoreElements()) return "";
		final CMObject o=e.nextElement();
		final StringBuilder s=new StringBuilder(o.ID());
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement().ID());
		return s.toString();
	}

	public final static String toCMObjectStringList(final CMObject[] e)
	{
		if((e==null)||(e.length==0)) return "";
		final StringBuilder s=new StringBuilder();
		for(CMObject o : e)
			s.append(", "+o.ID());
		return s.substring(2);
	}

	public final static String toCMObjectStringList(final Iterator<? extends CMObject> e)
	{
		if(!e.hasNext()) return "";
		final CMObject o=e.next();
		final StringBuilder s=new StringBuilder(o.ID());
		for(;e.hasNext();)
			s.append(", "+e.next().ID());
		return s.toString();
	}

	public final static String toStringList(final long[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	public final static String toStringList(final short[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Short.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	public final static String toStringList(final boolean[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	public final static String toStringList(final byte[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+((int)V[v]));
		return s.toString();
	}

	public final static String toStringList(final char[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+((long)V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+((long)V[v]));
		return s.toString();
	}

	public final static String toStringList(final int[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	public final static String toStringList(final double[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}


	public final static String toStringList(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(V.get(0).toString());
		for(int v=1;v<V.size();v++)
			s.append(", "+V.get(v).toString());
		return s.toString();
	}

	public final static String toStringList(final Set<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final Iterator<?> i=V.iterator();
		final StringBuilder s=new StringBuilder(i.next().toString());
		for(;i.hasNext();)
			s.append(", "+i.next().toString());
		return s.toString();
	}

	public final static String toTightStringList(final long[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	public final static String toTightStringList(final short[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Short.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	public final static String toTightStringList(final boolean[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	public final static String toTightStringList(final byte[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	public final static String toTightStringList(final char[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+((long)V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append((long)V[v]);
		return s.toString();
	}

	public final static String toTightStringList(final int[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	public final static String toTightStringList(final double[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}


	public final static String toTightStringList(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(V.get(0).toString());
		for(int v=1;v<V.size();v++)
			s.append(',').append(V.get(v).toString());
		return s.toString();
	}

	public final static String toTightStringList(final Set<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final Iterator<?> i=V.iterator();
		final StringBuilder s=new StringBuilder(i.next().toString());
		for(;i.hasNext();)
			s.append(',').append(i.next().toString());
		return s.toString();
	}

	public final static String toStringList(final Map<String,?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuffer s=new StringBuffer("");
		for(final String KEY : V.keySet())
			s.append(KEY+"="+(V.get(KEY).toString())+"/");
		return s.toString();
	}

	public final static String[] appendToArray(final String[] front, final String[] back)
	{
		if(back==null) return front;
		if(front==null) return back;
		if(back.length==0) return front;
		final String[] newa = Arrays.copyOf(front, front.length + back.length);
		for(int i=0;i<back.length;i++)
			newa[newa.length-1-i]=back[back.length-1-i];
		return newa;
	}

	public final static Map<String,String> parseEQStringList(final String s)
	{
		final Hashtable<String,String> h=new Hashtable<String,String>();
		final String[] allWords = s.split("/");
		for(final String word : allWords)
		{
			final String[] set=word.split("=");
			if(set.length==2)
				h.put(set[0].toUpperCase().trim(), set[1]);
		}
		return h;
	}

	public final static String toStringEqList(final Map<String,?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuffer s=new StringBuffer("");
		for(final String KEY : V.keySet())
		{
			String val = V.get(KEY).toString();
			if(val.indexOf(' ')>0)
				val="\""+val+"\"";
			s.append(KEY+"="+val+" ");
		}
		return s.toString().trim();
	}


	public final static List<Object> copyFlattenVector(final List<?> V)
	{
		final Vector<Object> V2=new Vector<Object>();
		for(int v=0;v<V.size();v++)
		{
			final Object h=V.get(v);
			if(h instanceof List<?>)
				V2.addElement(copyFlattenVector((List<?>)h));
			else
				V2.addElement(h);
		}
		return V2;
	}

	public final static int indexOf(final String[] supported, final String expertise)
	{
		if(supported==null) return -1;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].equals(expertise))
				return i;
		return -1;
	}

	public final static int indexOfIgnoreCase(final Enumeration<?> supported, final String key)
	{
		if(supported==null) return -1;
		int index = -1;
		for(;supported.hasMoreElements();)
		{
			if(supported.nextElement().toString().equalsIgnoreCase(key))
				return index;
			index++;
		}
		return -1;
	}

	public final static int indexOf(final int[] supported, final int x)
	{
		if(supported==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i]==x)
				return i;
		return -1;
	}

	public final static int indexOf(final long[] supported, final long x)
	{
		if(supported==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i]==x)
				return i;
		return -1;
	}

	public final static int indexOf(final Enumeration<?> supported, final Object key)
	{
		if(supported==null) return -1;
		int index = -1;
		for(;supported.hasMoreElements();)
		{
			if(supported.nextElement().equals(key))
				return index;
			index++;
		}
		return -1;
	}

	public final static int indexOfIgnoreCase(final Iterator<?> supported, final String key)
	{
		if(supported==null) return -1;
		int index = -1;
		for(;supported.hasNext();)
		{
			if(supported.next().toString().equalsIgnoreCase(key))
				return index;
			index++;
		}
		return -1;
	}

	public final static int indexOf(final Iterator<?> supported, final Object key)
	{
		if(supported==null) return -1;
		int index = -1;
		for(;supported.hasNext();)
		{
			if(supported.next().equals(key))
				return index;
			index++;
		}
		return -1;
	}

	public final static int indexOfIgnoreCase(final String[] supported, final String expertise)
	{
		if(supported==null) return -1;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].equalsIgnoreCase(expertise))
				return i;
		return -1;
	}

	public final static int indexOfIgnoreCase(final List<?> supported, final String str)
	{
		if(supported==null) return -1;
		if(str==null) return -1;
		for(int i=0;i<supported.size();i++)
			if(supported.get(i).toString().equalsIgnoreCase(str))
				return i;
		return -1;
	}

	public final static boolean contains(final String[] supported, final String expertise)
	{
		return indexOf(supported,expertise)>=0;
	}

	public final static boolean contains(final Enumeration<String> supported, final String expertise)
	{
		for(;supported.hasMoreElements();)
			if(supported.nextElement().equalsIgnoreCase(expertise))
				return true;
		return false;
	}

	public final static boolean contains(final char[] supported, final char c)
	{
		for(final char c2 : supported)
			if(c2==c)
				return true;
		return false;
	}

	public final static boolean contains(final byte[] supported, final byte b)
	{
		for(final byte b2 : supported)
			if(b2==b)
				return true;
		return false;
	}

	public final static boolean containsIgnoreCase(final String[] supported, final String expertise)
	{
		return indexOfIgnoreCase(supported,expertise)>=0;
	}

	public final static boolean containsIgnoreCase(final List<?> supported, final String str)
	{
		return indexOfIgnoreCase(supported,str)>=0;
	}

	public final static int indexOf(final Object[] supported, final Object expertise)
	{
		if(supported==null) return -1;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].equals(expertise))
				return i;
		return -1;
	}

	public final static boolean contains(final Object[] supported, final Object expertise)
	{
		return indexOf(supported,expertise)>=0;
	}

	public final static boolean contains(final int[] supported, final int x)
	{
		return indexOf(supported,x)>=0;
	}

	public final static boolean contains(final ByteBuffer buf, final byte[] bytes, final int pos)
	{
		for(int i=0;i<bytes.length && (i+pos)<buf.limit();i++)
			if(buf.get(pos+i)!=bytes[i])
				return false;
		return true;
	}

	public final static int containIndex(final ByteBuffer buf, final byte[][] bytes, final int pos)
	{
		for(int x=0;x<bytes.length;x++)
			if(contains(buf,bytes[x],pos))
				return x;
		return -1;
	}

	public final static int startsWith(final String[] supported, final String expertise)
	{
		if(supported==null) return 0;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].startsWith(expertise))
				return i;
		return -1;
	}

	public final static int startsWithIgnoreCase(final String[] supported, final String expertise)
	{
		if(supported==null) return 0;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].toUpperCase().startsWith(expertise.toUpperCase()))
				return i;
		return -1;
	}

	public final static boolean startsAnyWith(final String[] supported, final String expertise)
	{
		return startsWith(supported,expertise)>=0;
	}

	public final static boolean startsAnyWithIgnoreCase(final String[] supported, final String expertise)
	{
		return startsWithIgnoreCase(supported,expertise)>=0;
	}

	public final static int endsWith(final String[] supported, final String expertise)
	{
		if(supported==null) return 0;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].endsWith(expertise))
				return i;
		return -1;
	}

	public final static int endsWithIgnoreCase(final String[] supported, final String expertise)
	{
		if(supported==null) return 0;
		if(expertise==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].toUpperCase().endsWith(expertise.toUpperCase()))
				return i;
		return -1;
	}

	public final static boolean endsAnyWith(final String[] supported, final String expertise)
	{
		return endsWith(supported,expertise)>=0;
	}

	public final static boolean endsAnyWithIgnoreCase(final String[] supported, final String expertise)
	{
		return endsWithIgnoreCase(supported,expertise)>=0;
	}

	/** constant value representing an undefined/unimplemented miscText/parms format.*/
	public static final String FORMAT_UNDEFINED="{UNDEFINED}";
	/** constant value representing an always empty miscText/parms format.*/
	public static final String FORMAT_EMPTY="{EMPTY}";
}
