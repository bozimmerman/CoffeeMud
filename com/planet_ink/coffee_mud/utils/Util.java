package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.io.*;
public class Util
{
	public final static String SPACES="                                                                     ";
	
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
		catch(java.lang.NumberFormatException e){ return 0;}
		return sint;
	}
	
	public static String lastWordIn(String thisStr)
	{
		int x=thisStr.lastIndexOf(' ');
		if(x>=0)
			return thisStr.substring(x+1);
		return thisStr;
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
		catch(java.lang.NumberFormatException e){ return 0;}
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
		catch(java.lang.NumberFormatException e){ return 0;}
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
	
	public static String padRight(String thisStr, int thisMuch)
	{
		if(thisStr.length()>thisMuch)
			return thisStr.substring(0,thisMuch);
		return thisStr+SPACES.substring(0,thisMuch-thisStr.length());
	}
	public static String padLeft(String thisStr, int thisMuch)
	{
		if(thisStr.length()>thisMuch)
			return thisStr.substring(0,thisMuch);
		return SPACES.substring(0,thisMuch-thisStr.length())+thisStr;
	}
	public static String padRightPreserve(String thisStr, int thisMuch)
	{
		if(thisStr.length()>=thisMuch)
			return thisStr;
		return thisStr+SPACES.substring(0,thisMuch-thisStr.length());
	}
	public static String padLeftPreserve(String thisStr, int thisMuch)
	{
		if(thisStr.length()>=thisMuch)
			return thisStr;
		return SPACES.substring(0,thisMuch-thisStr.length())+thisStr;
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
	public static boolean bset(int num, int bit)
	{
		return ((num&bit)==bit);
	}
	public static boolean isSet(int number, int bitnumber)
	{
		if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
			return true;
		return false;
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
