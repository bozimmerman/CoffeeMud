package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Util
{
	public final static String SPACES="                                                                     ";
	private static byte[] encodeBuffer = new byte[65536];
	private static Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
	private static Inflater decompresser = new Inflater();
	
	public static String startWithAorAn(String str)
	{
		if(str.length()==0) 
			return str;
		if((!str.toUpperCase().startsWith("A "))
		&&(!str.toUpperCase().startsWith("AN "))
		&&(!str.toUpperCase().startsWith("THE "))
		&&(!str.toUpperCase().startsWith("SOME ")))
			if("aeiouAEIOU".indexOf(str.charAt(0))>=0) 
				return "an "+str;
			else
				return "a "+str;
		return str;
	}
	
	public static String[] toStringArray(Vector V)
	{
		if((V==null)||(V.size()==0)){
			String[] s=new String[0];
			return s;
		}
		String[] s=new String[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=(String)V.elementAt(v);
		return s;
	}
	
	public static String toStringList(Vector V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.size();v++)
			s.append(((String)V.elementAt(v))+"/");
		return s.toString();
	}
	
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
	
	public static String decompressString(byte[] b)
	{
		try
		{
			if ((b == null)||(b.length==0)) return "";

			decompresser.reset();
			decompresser.setInput(b);

			synchronized (encodeBuffer)
			{
				int len = decompresser.inflate(encodeBuffer);
				return new String(encodeBuffer, 0, len, "UTF-8");
			}
		}
		catch (Exception ex)
		{
			Log.errOut("MUD", "Error occur during decompression.  Buffer was: "+new String(b));
			return "";
		}
	}

	public static byte[] compressString(String s)
	{
		byte[] result = null;

		try
		{
			compresser.reset();
			compresser.setInput(s.getBytes("UTF-8"));
			compresser.finish();
			
			if(s.length()>encodeBuffer.length)
				encodeBuffer=new byte[s.length()];

			synchronized (encodeBuffer)
			{
				int len = compresser.deflate(encodeBuffer);
				result = new byte[len];
				System.arraycopy(encodeBuffer, 0, result, 0, len);
			}
		}
		catch (Exception ex)
		{
			Log.errOut("MUD", "Error occur during compression");
		}

	    return result;
	}

	public static String capitalize(String name)
	{
		return (Character.toUpperCase(name.charAt(0))+name.substring(1).toLowerCase()).trim();
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
			case '^': str.delete(i,i+2); break;
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
		else
			return num;
	}
	
	public static String getCleanBit(String s, int which)
	{
		s=getBit(s,which);
		if(s.startsWith("'"))
			s=s.substring(1);
		while(s.endsWith(" "))
			s=s.substring(0,s.length()-1);
		if(s.endsWith("'"))
			s=s.substring(0,s.length()-1);
		return s;
	}
	
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
					return s.substring(i+1);
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
		else
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
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			Combined.append((String)commands.elementAt(commandIndex)+" ");
		return Combined.toString().trim();
	}
	
	public static String combine(Vector commands, int startAt)
	{
		StringBuffer Combined=new StringBuffer("");
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			Combined.append((String)commands.elementAt(commandIndex)+" ");
		return Combined.toString().trim();
	}
	
	public static Vector parse(String str)
	{	return parse(str,-1);	}
	
	public static Vector parse(String str, int upTo)
	{
		Vector commands=new Vector();
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

	public static Vector parseCommas(String s)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(",");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if(s2.length()>0) V.addElement(s2);
			x=s.indexOf(",");
		}
		if(s.trim().length()>0)
			V.addElement(s.trim());
		return V;
	}
	
	public static Vector parseSemicolons(String s)
	{
		Vector V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(";");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if(s2.length()>0) V.addElement(s2);
			x=s.indexOf(";");
		}
		if(s.trim().length()>0)
			V.addElement(s.trim());
		return V;
	}
	
	
	public static int lengthMinusColors(String thisStr)
	{
		int size=0;
		for(int i=0;i<thisStr.length();i++)
		{
			if(thisStr.charAt(i)=='^')
				i++;
			else
				size++;
		}
		return size;
	}
	public static String padLeft(String thisStr, int thisMuch)
	{
		if(thisStr.length()>thisMuch)
			return thisStr.substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-lengthMinusColors(thisStr))+thisStr;
	}
	public static String padRight(String thisStr, int thisMuch)
	{
		if(thisStr.length()>thisMuch)
			return thisStr.substring(0,thisMuch);
		return thisStr+SPACES.substring(0,thisMuch-lengthMinusColors(thisStr));
	}
	public static String padRightPreserve(String thisStr, int thisMuch)
	{
		if(thisStr.length()>=thisMuch)
			return thisStr;
		return thisStr+SPACES.substring(0,thisMuch-lengthMinusColors(thisStr));
	}
	public static String padLeftPreserve(String thisStr, int thisMuch)
	{
		if(thisStr.length()>=thisMuch)
			return thisStr;
		return SPACES.substring(0,thisMuch-lengthMinusColors(thisStr))+thisStr;
	}
	
	public static boolean isNumber(String s)
	{
		if(s==null) return false;
		if(s.length()==0) return false;
		s=s.trim();
		for(int i=0;i<s.length();i++)
			if("0123456789-.,".indexOf(s.charAt(i))<0)
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
		else
			return str.toLowerCase();
	}
	
	public static String safetyFilter(String s)
	{
		StringBuffer s1=new StringBuffer(s);
		
		int x=-1;
		while((++x)<s1.length())
		{
			if(s1.charAt(x)=='\r')
			{
				s1.deleteCharAt(x);
				x--;
			}
			else
			if(s1.charAt(x)=='\n')
			{
				s1.setCharAt(x,'\\');
				s1.insert(x+1,'n');
				x++;
			}
			else
			if(s1.charAt(x)=='\'')
				s1.setCharAt(x,'`');
		}
		return s1.toString();
	}
	
}
