package com.planet_ink.coffee_mud.utils;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class CoffeeUtensils
{
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
		if(srchStr.endsWith("$")&&(toSrchStr.toUpperCase().endsWith(srchStr.toUpperCase().substring(0,srchStr.length()-1))))
		   return true;
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

	public static Environmental fetchEnvironmental(Vector list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=Util.s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}


		int myOccurrance=occurrance;
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr)||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=occurrance;
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if(containsString(thisThang.name(),srchStr)&&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=occurrance;
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((!(thisThang instanceof Ability))
					&&(thisThang.displayText().length()>0)
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static Environmental fetchEnvironmental(Hashtable list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=Util.s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}
		if(list.get(srchStr)!=null)
			return (Environmental)list.get(srchStr);
		int myOccurrance=occurrance;
		if(exactOnly)
		{
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if(thisThang.ID().equalsIgnoreCase(srchStr)||thisThang.name().equalsIgnoreCase(srchStr))
					if((!allFlag)||(thisThang.displayText().length()>0))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		else
		{
			myOccurrance=occurrance;
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if(containsString(thisThang.name(),srchStr)&&((!allFlag)||(thisThang.displayText().length()>0)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
			myOccurrance=occurrance;
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((thisThang.displayText().length()>0)&&(containsString(thisThang.displayText(),srchStr)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
		}
		return null;
	}

	public static Environmental fetchEnvironmental(Environmental[] list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("the")))
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=Util.s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}
		int myOccurrance=occurrance;
		if(exactOnly)
		{
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if(thisThang!=null)
					if(thisThang.ID().equalsIgnoreCase(srchStr)||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
			}
		}
		else
		{
			myOccurrance=occurrance;
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if(thisThang!=null)
					if(containsString(thisThang.name(),srchStr)&&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
			}
			myOccurrance=occurrance;
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if((thisThang!=null)&&(thisThang.displayText().length()>0))
					if(containsString(thisThang.displayText(),srchStr))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		return null;
	}

	public static Item fetchAvailableItem(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			occurrance=Util.s_int(srchStr.substring(dot+1));
			srchStr=srchStr.substring(0,dot);
		}
		int myOccurrance=occurrance;
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(thisThang.ID().equalsIgnoreCase(srchStr)||(thisThang.name().equalsIgnoreCase(srchStr))))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(containsString(thisThang.name(),srchStr)&&((!allFlag)||(thisThang.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=occurrance;
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
					if((thisThang.container()==goodLocation)
					&&(thisThang.displayText().length()>0)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static Room roomLocation(Environmental E)
	{
		if(E==null) return null;
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return ((MOB)((Item)E).owner()).location();
		return null;
	}
	
	//int attChance=(int)(adjArmor+adjAttack);
	public static boolean normalizeAndRollLess(int score)
	{
		return (Dice.rollPercentage()<normalizeBy5(score));
	}
	
	public static int normalizeBy5(int score)
	{
		if(score>95) 
			return 95;
		else
		if(score<5)
			return 5;
		return score;
	}
}

