package com.planet_ink.coffee_mud.utils;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
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
	
	public static String id(Object e)
	{
		if(e!=null)
			if(e instanceof Environmental)
				return ((Environmental)e).ID();
			else
			if(e instanceof Race)
				return ((Race)e).ID();
			else
			if(e instanceof CharClass)
				return ((CharClass)e).ID();
			else
			if(e instanceof Behavior)
				return ((Behavior)e).ID();
		return "";
	}
	
	public static boolean containsString(String toSrchStr, String srchStr)
	{
		if(srchStr.equalsIgnoreCase("all")) return true;
		int x=toSrchStr.toUpperCase().indexOf(srchStr.toUpperCase());
		if(x<0) return false;
		
		if(x==0)
		{
			if(toSrchStr.length()<=srchStr.length())
				return true;
			//if(Character.isLetter(toSrchStr.charAt(x+srchStr.length())))
			//   return false;
			return true;
		}
		else
		{
			if(Character.isLetter(toSrchStr.charAt(x-1)))
			   return false;
			if(toSrchStr.length()<=x+srchStr.length())
				return true;
			//if(Character.isLetter(toSrchStr.charAt(x+srchStr.length())))
			  // return false;
			return true;
		}
	}
	
	public static Environmental fetchEnvironmental(Vector list, String srchStr)
	{
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("the")))
		   return null;
		if(srchStr.startsWith("all "))
			srchStr=srchStr.substring(4);
		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}

							   
		int myOccurrance=occurrance;
		for(int i=0;i<list.size();i++)
		{
			Environmental thisThang=(Environmental)list.elementAt(i);
			if(thisThang.ID().equalsIgnoreCase(srchStr)||containsString(thisThang.name(),srchStr))
				if((--myOccurrance)<=0)
					return thisThang;
		}
		myOccurrance=occurrance;
		for(int i=0;i<list.size();i++)
		{
			Environmental thisThang=(Environmental)list.elementAt(i);
			if((!(thisThang instanceof Ability))&&(containsString(thisThang.displayText(),srchStr)))
				if((--myOccurrance)<=0)
					return thisThang;
		}
		return null;
	}
	
	public static Environmental fetchEnvironmental(Hashtable list, String srchStr)
	{
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("the")))
		   return null;
		if(srchStr.startsWith("all "))
			srchStr=srchStr.substring(4);
		
		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}
		if(list.get(srchStr)!=null) 
			return (Environmental)list.get(srchStr);
		int myOccurrance=occurrance;
		for(Enumeration e=list.elements();e.hasMoreElements();)
		{
			Environmental thisThang=(Environmental)e.nextElement();
			if(thisThang.ID().equalsIgnoreCase(srchStr)||containsString(thisThang.name(),srchStr))
				if((--myOccurrance)<=0)
					return thisThang;
		}
		myOccurrance=occurrance;
		for(Enumeration e=list.elements();e.hasMoreElements();)
		{
			Environmental thisThang=(Environmental)e.nextElement();
			if(containsString(thisThang.displayText(),srchStr))
				if((--myOccurrance)<=0)
					return thisThang;
		}
		return null;
	}
	
	public static Environmental fetchEnvironmental(Environmental[] list, String srchStr)
	{
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("the")))
		   return null;
		if(srchStr.startsWith("all "))
			srchStr=srchStr.substring(4);
		
		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}
		int myOccurrance=occurrance;
		for(int i=0;i<list.length;i++)
		{
			Environmental thisThang=(Environmental)list[i];
			if(thisThang!=null)
				if(thisThang.ID().equalsIgnoreCase(srchStr)||containsString(thisThang.name(),srchStr))
					if((--myOccurrance)<=0)
						return thisThang;
		}
		myOccurrance=occurrance;
		for(int i=0;i<list.length;i++)
		{
			Environmental thisThang=(Environmental)list[i];
			if(thisThang!=null)
				if(containsString(thisThang.displayText(),srchStr))
					if((--myOccurrance)<=0)
						return thisThang;
		}
		return null;
	}
	
	public static Item fetchAvailableItem(Vector list, String srchStr, Item goodLocation, boolean wornOnly, boolean unwornOnly)
	{
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("the")))
		   return null;
		if(srchStr.startsWith("all "))
			srchStr=srchStr.substring(4);
		
		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}
		int myOccurrance=occurrance;
		for(int i=0;i<list.size();i++)
		{
			Item thisThang=(Item)list.elementAt(i);
			boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
			
			if((thisThang.location()==goodLocation)
			&&((beingWorn&wornOnly)||((!beingWorn)&&(unwornOnly))||((!wornOnly)&&(!unwornOnly)))
			&&(thisThang.ID().equalsIgnoreCase(srchStr)||containsString(thisThang.name(),srchStr)))
			{
				if((--myOccurrance)<=0)
					return thisThang;
			}
		}
		myOccurrance=occurrance;
		for(int i=0;i<list.size();i++)
		{
			Item thisThang=(Item)list.elementAt(i);
			boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
			if((thisThang.location()==goodLocation)
			&&((beingWorn&wornOnly)||((!beingWorn)&&(unwornOnly))||((!wornOnly)&&(!unwornOnly)))
			&&(containsString(thisThang.displayText(),srchStr)))
				if((--myOccurrance)<=0)
					return thisThang;
		}
		return null;
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
	
}
