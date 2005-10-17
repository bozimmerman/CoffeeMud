package com.planet_ink.siplet.support;

import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Util
{
	public final static String SPACES="                                                                     ";
	
	public static String toSemicolonList(byte[] bytes)
	{
		StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(Byte.toString(bytes[b])+(b<(bytes.length-1)?";":""));
		return str.toString();
	}
	
    public static String toSemicolonList(String[] bytes)
    {
        StringBuffer str=new StringBuffer("");
        for(int b=0;b<bytes.length;b++)
            str.append(bytes[b]+(b<(bytes.length-1)?";":""));
        return str.toString();
    }
    
    public static String toSemicolonList(Vector bytes)
    {
        StringBuffer str=new StringBuffer("");
        for(int b=0;b<bytes.size();b++)
            str.append(bytes.elementAt(b)+(b<(bytes.size()-1)?";":""));
        return str.toString();
    }
    
	public static byte[] fromByteList(String str)
	{
		Vector V=parseSemicolons(str,true);
		if(V.size()>0)
		{
			byte[] bytes=new byte[V.size()];
			for(int b=0;b<V.size();b++)
				bytes[b]=Byte.parseByte((String)V.elementAt(b));
			return bytes;
		}
		return new byte[0];
	}
	
	public static long absDiff(long x, long y)
	{
		long d=x-y;
		if(d<0) return d*-1;
		return d;
	}
	
	public static String repeat(String str1, int times)
	{
		if(times<=0) return "";
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<times;i++)
			str.append(str1);
		return str.toString();
	}
	
    public static String endWithAPeriod(String str)
    {
        if(str.length()==0) return str;
        int x=str.length()-1;
        while((x>=0)
        &&((Character.isWhitespace(str.charAt(x)))
            ||((x>0)&&((str.charAt(x)!='^')&&(str.charAt(x-1)=='^')&&((--x)>=0))))) 
                x--;
        if(x<0) return str;
        if(str.charAt(x)=='.') return str.trim()+" ";
        return str.substring(0,x+1)+". "+str.substring(x+1).trim();
    }
    
	public static String startWithAorAn(String str)
	{
		if(str.length()==0) 
			return str;
		if((!str.toUpperCase().startsWith("A "))
		&&(!str.toUpperCase().startsWith("AN "))
		&&(!str.toUpperCase().startsWith("THE "))
		&&(!str.toUpperCase().startsWith("SOME ")))
        {
			if("aeiouAEIOU".indexOf(str.charAt(0))>=0) 
				return "an "+str;
			return "a "+str;
        }
		return str;
	}
	
	
	public static int getParmInt(String text, String key, int defaultValue)
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
						return Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}
	
	public static boolean isVowel(char c)
	{ return (("aeiou").indexOf(Character.toLowerCase(c))>=0);}
	
	public static int getParmPlus(String text, String key)
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
					char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return Util.s_int(text.substring(0,x));
						return -Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}

	public static double getParmDoublePlus(String text, String key)
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
					char pm=text.charAt(x);
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
						if(text.substring(0,x).indexOf(".")<0)
						{
							if(pm=='+')
								return new Integer(Util.s_int(text.substring(0,x))).doubleValue();
							return new Integer(-Util.s_int(text.substring(0,x))).doubleValue();
						}
						if(pm=='+')
							return Util.s_double(text.substring(0,x));
						return -Util.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0.0;
	}
	
	public static double getParmDouble(String text, String key, double defaultValue)
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
						if(text.substring(0,x).indexOf(".")<0)
							return new Long(Util.s_long(text.substring(0,x))).doubleValue();
						return Util.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}
	
	public static String getParmStr(String text, String key, String defaultVal)
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
							&&((Character.isLetterOrDigit(text.charAt(x)))
							||((endWithQuote)&&(text.charAt(x)!='\"'))))
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

	public static String[] toStringArray(Vector V)
	{
		if((V==null)||(V.size()==0)){
			String[] s=new String[0];
			return s;
		}
		String[] s=new String[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=V.elementAt(v).toString();
		return s;
	}
	
	public static long[] toLongArray(Vector V)
	{
		if((V==null)||(V.size()==0)){
			long[] s=new long[0];
			return s;
		}
		long[] s=new long[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=Util.s_long(V.elementAt(v).toString());
		return s;
	}
	public static int[] toIntArray(Vector V)
	{
		if((V==null)||(V.size()==0)){
			int[] s=new int[0];
			return s;
		}
		int[] s=new int[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=Util.s_int(V.elementAt(v).toString());
		return s;
	}
	
	public static String[] toStringArray(HashSet V)
	{
		if((V==null)||(V.size()==0)){
			String[] s=new String[0];
			return s;
		}
		String[] s=new String[V.size()];
		int v=0;
		for(Iterator i=V.iterator();i.hasNext();)
			s[v++]=(i.next()).toString();
		return s;
	}
	
	public static String toStringList(String[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	
	public static String toStringList(long[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	
	public static String toStringList(int[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	
	
	public static String toStringList(Vector V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.size();v++)
			s.append(", "+V.elementAt(v).toString());
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	
	public static String toStringList(HashSet V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(Iterator i=V.iterator();i.hasNext();)
			s.append(", "+i.next().toString());
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	
	public static Vector makeVector(String[] O)
	{ 
		Vector V=new Vector();
		if(O!=null)
		for(int s=0;s<O.length;s++)
			V.addElement(O[s]);
		return V;
	}
	public static Vector makeVector()
	{ return new Vector();}
	public static Vector makeVector(Object O)
	{ Vector V=new Vector(); V.addElement(O); return V;}
	public static Vector makeVector(Object O, Object O2)
	{ Vector V=new Vector(); V.addElement(O); V.addElement(O2); return V;}
	public static Vector makeVector(Object O, Object O2, Object O3)
	{ Vector V=new Vector(); V.addElement(O); V.addElement(O2); V.addElement(O3); return V;}
	public static Vector makeVector(Object O, Object O2, Object O3, Object O4)
	{ Vector V=new Vector(); V.addElement(O); V.addElement(O2); V.addElement(O3); V.addElement(O4); return V;}
		
	public static HashSet makeHashSet(){return new HashSet();}
	public static HashSet makeHashSet(Object O)
	{HashSet H=new HashSet(); H.add(O); return H;}
	public static HashSet makeHashSet(Object O, Object O2)
	{HashSet H=new HashSet(); H.add(O); H.add(O2); return H;}
	public static HashSet makeHashSet(Object O, Object O2, Object O3)
	{HashSet H=new HashSet(); H.add(O); H.add(O2); H.add(O3); return H;}
	public static HashSet makeHashSet(Object O, Object O2, Object O3, Object O4)
	{HashSet H=new HashSet(); H.add(O); H.add(O2); H.add(O3); H.add(O4); return H;}
	
	public static String[] toStringArray(Hashtable V)
	{
		if((V==null)||(V.size()==0)){
			String[] s=new String[0];
			return s;
		}
		String[] s=new String[V.size()];
		int v=0;
		for(Enumeration e=V.keys();e.hasMoreElements();)
		{
			String KEY=(String)e.nextElement();
			s[v]=(String)V.get(KEY);
			v++;
		}
		return s;
	}
	
	public static void addToVector(Vector from, Vector to)
	{
		if(from!=null)
		for(int i=0;i<from.size();i++)
			to.addElement(from.elementAt(i));
	}
	
	public static String toStringList(Hashtable V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(Enumeration e=V.keys();e.hasMoreElements();)
		{
			String KEY=(String)e.nextElement();
			s.append(KEY+"="+((String)V.get(KEY))+"/");
		}
		return s.toString();
	}
	
	public static String replaceAll(String str, String thisStr, String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)==thisStr.charAt(0))
				if(str.substring(i).startsWith(thisStr))
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
		}
		return str;
	}
	public static String replaceFirst(String str, String thisStr, String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)==thisStr.charAt(0))
				if(str.substring(i).startsWith(thisStr))
				{
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
					return str;
				}
		}
		return str;
	}
	
	public static boolean isInteger(String INT)
	{
		if(INT.length()==0) return false;
		if(INT.startsWith("-")&&(INT.length()>1))
		    INT=INT.substring(1);
		for(int i=0;i<INT.length();i++)
			if(!Character.isDigit(INT.charAt(i)))
				return false;
		return true;
	}
	
	public static boolean isDouble(String DBL)
	{
		if(DBL.length()==0) return false;
		if(DBL.startsWith("-")&&(DBL.length()>1))
		    DBL=DBL.substring(1);
		boolean alreadyDot=false;
		for(int i=0;i<DBL.length();i++)
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
		return alreadyDot;
	}
	
    public static String capitalizeAndLower(String name)
    {
        return (Character.toUpperCase(name.charAt(0))+name.substring(1).toLowerCase()).trim();
    }
	public static String capitalizeFirstLetter(String name)
	{
		return (Character.toUpperCase(name.charAt(0))+name.substring(1)).trim();
	}
	
	/**
	 * Returns the boolean value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> int num=s_bool(CMD.substring(14));
	 * @param INT Boolean value of string
	 * @return int Boolean value of the string
	 */
	public static boolean s_bool(String BOOL)
	{
		return Boolean.valueOf(BOOL).booleanValue(); 
	}
    
	/**
	 * Returns the integer value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	public static int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
		catch(Exception e){ return 0;}
		return sint;
	}
	
    /**
     * Returns the integer value of a string without crashing
     * 
     * <br><br><b>Usage:</b> int num=s0_int(CMD.substring(14));
     * @param INT Integer value of string
     * @return int Integer value of the string
     */
    public static int s0_int(String INT)
    {
        int sint=0;
        while((INT.length()>0)&&(INT.startsWith("0")))
                INT=INT.substring(1);
        try{ sint=Integer.parseInt(INT); }
        catch(Exception e){ return 0;}
        return sint;
    }
    
	public static String lastWordIn(String thisStr)
	{
		int x=thisStr.lastIndexOf(' ');
		if(x>=0)
			return thisStr.substring(x+1);
		return thisStr;
	}
	
	public static String removeColors(String s)
	{
		StringBuffer str=new StringBuffer(s);
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
				    if((c=='<')||(c=='&'))
				    {
					    i+=2;
					    while(i<(str.length()-1))
				        {
				            if(((c=='<')&&((str.charAt(i)!='^')||(str.charAt(i+1)!='>')))
				            ||((c=='&')&&(str.charAt(i)!=';')))
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
				                if(c=='<')
					                str.delete(tagStart,i+2);
				                else
					                str.delete(tagStart,i+1);
				                i=tagStart-1;
				                break;
				            }
				        }
				    }
					else
					{
					    str.delete(i,i+2); 
						i--;
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
	
	public static String returnTime(long millis, long ticks)
	{
		String avg="";
		if(ticks>0)
			avg=", Average="+(millis/ticks)+"ms";
		if(millis<1000) return millis+"ms"+avg;
		long seconds=millis/1000;
		millis-=(seconds*1000);
		if(seconds<60) return seconds+"s "+millis+"ms"+avg;
		long minutes=seconds/60;
		seconds-=(minutes*60);
		if(minutes<60) return minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long hours=minutes/60;
		minutes-=(hours*60);
		if(hours<24) return hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long days=hours/24;
		hours-=(days*24);
		return days+"d "+hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
		
	}
	
	public static Vector copyVector(Vector V)
	{
		Vector V2=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Object h=V.elementAt(v);
			if(h instanceof Vector)
				V2.addElement(copyVector((Vector)h));
			else
				V2.addElement(h);
		}
		return V2;
	}
	
	public static int numBits(String s)
	{
		int i=0;
		int num=0;
		boolean in=false;
		char c=(char)0;
		char fc=(char)0;
		char lc=(char)0;
		s=s.trim();
		while(i<s.length())
		{
			c=s.charAt(i);
			boolean white=(Character.isWhitespace(c)||(c==' ')||(c=='	')||(c=='\t'));
			if(white&&in&&(((fc=='\'')&&(lc!='\''))||((fc=='`')&&(lc!='`'))))
				white=false;
			if(white&&in)
			{
				num++;
				c=(char)0;
				lc=(char)0;
				fc=(char)0;
				in=false;
			}
			else
			if(!white)
			{
				if(!in)
				{
					in=true;
					fc=c;
					lc=(char)0;
				}
				else
					lc=c;
			}
			i++;
		}
		if(in)
			return num+1;
		return num;
	}
	
	public static String cleanBit(String s)
	{ 
		while(s.startsWith(" "))
			s=s.substring(1);
		while(s.endsWith(" "))
			s=s.substring(0,s.length()-1);
		if((s.startsWith("'"))||(s.startsWith("`")))
		{
			s=s.substring(1);
			if((s.endsWith("'"))||(s.endsWith("`")))
				s=s.substring(0,s.length()-1);
		}
		return s;
	}
	public static String getCleanBit(String s, int which)
	{ return cleanBit(getBit(s,which));}
	
	public static String getPastBitClean(String s, int which)
	{ return cleanBit(getPastBit(s,which));}
	
	public static String getPastBit(String s, int which)
	{
		int i=0;
		int w=0;
		boolean in=false;
		s=s.trim();
		String t="";
		char c=(char)0;
		char lc=(char)0;
		char fc=(char)0;
		while(i<s.length())
		{
			c=s.charAt(i);
			boolean white=(Character.isWhitespace(c)||(c==' ')||(c=='	')||(c=='\t'));
			if(white&&in&&(((fc=='\'')&&(lc!='\''))||((fc=='`')&&(lc!='`'))))
				white=false;
			if(white&&in)
			{
				if(w==which)
				{
					s=s.substring(i+1);
					if(((s.trim().startsWith("'"))||(s.trim().startsWith("`")))
					&&((s.trim().startsWith("'"))||(s.trim().startsWith("`"))))
						s=s.trim().substring(1,s.length()-1);
					return s;
				}
				w++;
				in=false;
				c=(char)0;
				lc=(char)0;
				fc=(char)0;
			}
			else
			if(!white)
			{
				if(!in)
				{
					t="";
					fc=c;
					lc=(char)0;
					in=true;
				}
				else
					lc=c;
				t+=c;
			}
			i++;
		}
		return "";
	}
	
	public static String getBit(String s, int which)
	{
		int i=0;
		int w=0;
		boolean in=false;
		s=s.trim();
		String t="";
		char c=(char)0;
		char lc=(char)0;
		char fc=(char)0;
		while(i<s.length())
		{
			c=s.charAt(i);
			boolean white=(Character.isWhitespace(c)||(c==' ')||(c=='	')||(c=='\t'));
			if(white&&in&&(((fc=='\'')&&(lc!='\''))||((fc=='`')&&(lc!='`'))))
				white=false;
			if(white&&in)
			{
				if(w==which)
					return t;
				w++;
				in=false;
				c=(char)0;
				lc=(char)0;
				fc=(char)0;
			}
			else
			if(!white)
			{
				if(!in)
				{
					t="";
					fc=c;
					lc=(char)0;
					in=true;
				}
				else
					lc=c;
				t+=c;
			}
			i++;
		}
		if(in)
			return t;
		return "";
	}
	
	/**
	 * Returns the long value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> lSize = WebIQBase.s_long(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param long String to convert
	 * @return long Long value of the string
	 */
	public static long s_long(String LONG)
	{
		long slong=0;
		try{ slong=Long.parseLong(LONG); }
		catch(Exception e){ return 0;}
		return slong;
	}
	
	/**
	 * Returns the double value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param double String to convert
	 * @return double Double value of the string
	 */
	public static double s_double(String DOUBLE)
	{
		double sdouble=0;
		try{ sdouble=Double.parseDouble(DOUBLE); }
		catch(Exception e){ return 0;}
		return sdouble;
	}
	
	public static String combine(Vector commands, int startAt, int endAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			Combined.append((String)commands.elementAt(commandIndex)+" ");
		return Combined.toString().trim();
	}
	
	public static String combineWithQuotes(Vector commands, int startAt, int endAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
		{
			String s=(String)commands.elementAt(commandIndex);
			if(s.indexOf(" ")>=0) s="\""+s+"\"";
			Combined.append(s+" ");
		}
		return Combined.toString().trim();
	}
	
    public static String combineAfterIndexWithQuotes(Vector commands, String match)
    {
        StringBuffer Combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=0;commandIndex<0;commandIndex++)
        {
            String s=(String)commands.elementAt(commandIndex);
            if(s.indexOf(" ")>=0) s="\""+s+"\"";
            Combined.append(s+" ");
        }
        return Combined.toString().trim();
    }
    
	public static String combineWithQuotes(Vector commands, int startAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
		{
			String s=(String)commands.elementAt(commandIndex);
			if(s.indexOf(" ")>=0) s="\""+s+"\"";
			Combined.append(s+" ");
		}
		return Combined.toString().trim();
	}
	
	public static String combine(Vector commands, int startAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			Combined.append((String)commands.elementAt(commandIndex)+" ");
		return Combined.toString().trim();
	}
	
	public static Vector parse(String str)
	{	return parse(str,-1);	}
	
	
	public static Vector paramParse(String str)
	{
		Vector commands=parse(str);
		for(int i=0;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.startsWith("=")&&(s.length()>1)&&(i>0))
			{
				String prev=(String)commands.elementAt(i-1);
				commands.setElementAt(prev+s,i-1);
				commands.removeElementAt(i);
				i--;
			}
			else
			if(s.endsWith("=")&&(s.length()>1)&&(i<(commands.size()-1)))
			{
				String next=(String)commands.elementAt(i+1);
				commands.setElementAt(s+next,i);
				commands.removeElementAt(i+1);
			}
			else
			if(s.equals("=")&&((i>0)&&(i<(commands.size()-1))))
			{
				String prev=(String)commands.elementAt(i-1);
				String next=(String)commands.elementAt(i+1);
				commands.setElementAt(prev+"="+next,i-1);
				commands.removeElementAt(i);
				commands.removeElementAt(i+1);
				i--;
			}
		}
		return commands;
	}
	
	public static Vector parse(String str, int upTo)
	{
		Vector commands=new Vector();
		if(str==null) return commands;
		str=str.trim();
		while(!str.equals(""))
		{
			int spaceIndex=str.indexOf(" ");
			int strIndex=str.indexOf("\"");
			String CMD="";
			if((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
			{
				int endStrIndex=str.indexOf("\"",strIndex+1);
				if(endStrIndex>strIndex)
				{
					CMD=str.substring(strIndex+1,endStrIndex).trim();
					str=str.substring(endStrIndex+1).trim();
				}
				else
				{
					CMD=str.substring(strIndex+1).trim();
					str="";
				}
			}
			else
			if(spaceIndex>=0)
			{
				CMD=str.substring(0,spaceIndex).trim();
				str=str.substring(spaceIndex+1).trim();
			}
			else
			{
				CMD=str.trim();
				str="";
			}
			if(!CMD.equals(""))
			{
				commands.addElement(CMD);
				if((upTo>=0)&&(commands.size()>=upTo))
				{
					if(str.length()>0)
						commands.addElement(str);
					break;
				}
					
			}
		}
		return commands;
	}

	public static Vector parseCommas(String s, boolean ignoreNulls)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(",");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(",");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	
	public static Vector parseSquiggles(String s)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf("~");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			V.addElement(s2);
			x=s.indexOf("~");
		}
		return V;
	}
	
	public static Vector parseSentences(String s)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(".");
		while(x>=0)
		{
			String s2=s.substring(0,x+1);
			s=s.substring(x+1);
			V.addElement(s2);
			x=s.indexOf(".");
		}
		return V;
	}
	
	public static Vector parseSquiggleDelimited(String s, boolean ignoreNulls)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf("~");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((s2.length()>0)||(!ignoreNulls))
				V.addElement(s2);
			x=s.indexOf("~");
		}
		if((s.length()>0)||(!ignoreNulls))
			V.addElement(s);
		return V;
	}
	
	public static Vector parseSemicolons(String s, boolean ignoreNulls)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(";");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(";");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	
	public static int abs(int val)
	{
	    if(val>=0) return val;
	    return val*-1;
	}
	
	public static long abs(long val)
	{
	    if(val>=0) return val;
	    return val*-1;
	}
	
	public static Vector parseSpaces(String s, boolean ignoreNulls)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(" ");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(" ");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	
	public static int lengthMinusColors(String thisStr)
	{
		int size=0;
		for(int i=0;i<thisStr.length();i++)
		{
			if(thisStr.charAt(i)=='^')
			{
				i++;
				if((i+1)<thisStr.length())
				{
				    int tagStart=i;
				    char c=thisStr.charAt(i);
				    if((c=='<')||(c=='&'))
				    while(i<(thisStr.length()-1))
			        {
			            if(((c=='<')&&((thisStr.charAt(i)!='^')||(thisStr.charAt(i+1)!='>')))
			            ||((c=='&')&&(thisStr.charAt(i)!=';')))
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
				}
			}
			else
				size++;
		}
		return size;
	}
	
	/** Convert an integer to its Roman Numeral equivalent
	 * 
	 * <br><br><b>Usage:</b> Return=MiscFunc.convertToRoman(Number)+".";
	 * @param i Integer to convert
	 * 
	 * @return String Converted integer
	 */
	public static String convertToRoman(int i)
	{
		String Roman="";
		String Hundreds[]={"C","CC","CCC","CD","D","DC","DCC","DCCC","CM","P"};
		String Tens[]={"X","XX","XXX","XL","L","LX","LXX","LXXX","XC","C"};
		String Ones[]={"I","II","III","IV","V","VI","VII","VIII","IX","X"};
		if(i>1000)
		{
			Roman="Y";
			i=i%1000;
		}
		if(i>=100)
		{
			int x=i%100;
			int y=Math.round((i-x)/100);
			if(y>0)
				Roman+=Hundreds[y-1];
			i=x;
		}
		if(i>=10)
		{
			int x=i%10;
			int y=Math.round((i-x)/10);
			if(y>0)
				Roman+=Tens[y-1];
		}
		i=i%10;
		if(i>0)
			Roman+=Ones[i-1];
		return Roman;
	}
	
	public static String padLeft(String thisStr, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}
	public static String padLeft(String thisStr, String colorPrefix, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return colorPrefix+removeColors(thisStr).substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lenMinusColors)+colorPrefix+thisStr;
	}
	public static String padRight(String thisStr, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch);
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}
    public static String limit(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr).substring(0,thisMuch);
        return thisStr;
    }
	public static String padRight(String thisStr, String colorSuffix, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr).substring(0,thisMuch)+colorSuffix;
		return thisStr+colorSuffix+SPACES.substring(0,thisMuch-lenMinusColors);
	}
	public static String padRightPreserve(String thisStr, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}
	public static String centerPreserve(String thisStr, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		int left=(thisMuch-lenMinusColors)/2;
		int right=((left+left+lenMinusColors)<thisMuch)?left+1:left;
		return SPACES.substring(0,left)+thisStr+SPACES.substring(0,right);
	}
	public static String padLeftPreserve(String thisStr, int thisMuch)
	{
	    int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}
	
	public static boolean isNumber(String s)
	{
		if(s==null) return false;
		s=s.trim();
		if(s.length()==0) return false;
		if((s.length()>1)&&(s.startsWith("-")))
		    s=s.substring(1);
		for(int i=0;i<s.length();i++)
			if("0123456789.,".indexOf(s.charAt(i))<0)
				return false;
		return true;
	}
	
	public static double div(double a, double b)
	{
		return a/b;
	}
	public static double div(double a, int b)
	{
		return a/new Integer(b).doubleValue();
	}
	public static double div(int a, double b)
	{
		return new Integer(a).doubleValue()/b;
	}
	public static double div(double a, long b)
	{
		return a/new Long(b).doubleValue();
	}
	public static double div(long a, double b)
	{
		return new Long(a).doubleValue()/b;
	}
	
	public static double mul(double a, double b)
	{
		return a*b;
	}
	public static double mul(double a, int b)
	{
		return a*new Integer(b).doubleValue();
	}
	public static double mul(int a, double b)
	{
		return new Integer(a).doubleValue()*b;
	}
	public static double mul(double a, long b)
	{
		return a*new Long(b).doubleValue();
	}
	public static double mul(long a, double b)
	{
		return new Long(a).doubleValue()*b;
	}
	public static long mul(long a, long b)
	{
		return a*b;
	}
	public static int mul(int a, int b)
	{
		return a*b;
	}
	public static double div(long a, long b)
	{
		return new Long(a).doubleValue()/new Long(b).doubleValue();
	}
	public static double div(int a, int b)
	{
		return new Integer(a).doubleValue()/new Integer(b).doubleValue();
	}
	public static int pow(int x, int y)
	{
		return (int)Math.round(Math.pow(new Integer(x).doubleValue(),new Integer(y).doubleValue()));
	}
	public static int squared(int x)
	{
		return (int)Math.round(Math.pow(new Integer(x).doubleValue(),new Integer(x).doubleValue()));
	}
	public static boolean bset(int num, int bitmask)
	{
		return ((num&bitmask)==bitmask);
	}
	public static boolean bset(long num, long bitmask)
	{
		return ((num&bitmask)==bitmask);
	}
	public static boolean bset(long num, int bitmask)
	{
		return ((num&bitmask)==bitmask);
	}
	public static int setb(int num, int bitmask)
	{
		return num|bitmask;
	}
	public static boolean banyset(int num, int bitmask)
	{
		return ((num&bitmask)>0);
	}
	public static boolean banyset(long num, long bitmask)
	{
		return ((num&bitmask)>0);
	}
	public static boolean banyset(long num, int bitmask)
	{
		return ((num&bitmask)>0);
	}
	public static long setb(long num, int bitmask)
	{
		return num|bitmask;
	}
	public static long setb(long num, long bitmask)
	{
		return num|bitmask;
	}
	public static int unsetb(int num, int bitmask)
	{
		if(bset(num,bitmask))
			num-=bitmask;
		return num;
	}
	public static long unsetb(long num, long bitmask)
	{
		if(bset(num,bitmask))
			num-=bitmask;
		return num;
	}
	public static long unsetb(long num, int bitmask)
	{
		if(bset(num,bitmask))
			num-=bitmask;
		return num;
	}
	public static boolean isSet(int number, int bitnumber)
	{
		if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
			return true;
		return false;
	}
	public static boolean isSet(long number, int bitnumber)
	{
		if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
			return true;
		return false;
	}
	
	public static String sameCase(String str, char c)
	{
		if(Character.isUpperCase(c))
			return str.toUpperCase();
		return str.toLowerCase();
	}
	
	public static Vector denumerate(Enumeration e)
	{
		Vector V=new Vector();
		for(;e.hasMoreElements();)
			V.addElement(e.nextElement());
		return V;
	}
}
