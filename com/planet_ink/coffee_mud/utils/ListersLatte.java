package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;


public class ListersLatte
{
	private ListersLatte(){}
	public static StringBuffer reallyList(Hashtable these, int ofType)
	{
		return reallyList(these,ofType,null);
	}
	public static StringBuffer reallyList(Hashtable these)
	{
		return reallyList(these,-1,null);
	}
	public static StringBuffer reallyList(Hashtable these, Room likeRoom)
	{
		return reallyList(these,-1,likeRoom);
	}
	public static StringBuffer reallyList(Vector these, int ofType)
	{
		return reallyList(these.elements(),ofType,null);
	}
	public static StringBuffer reallyList(Enumeration these, int ofType)
	{
		return reallyList(these,ofType,null);
	}
	public static StringBuffer reallyList(Vector these)
	{
		return reallyList(these.elements(),-1,null);
	}
	public static StringBuffer reallyList(Enumeration these)
	{
		return reallyList(these,-1,null);
	}
	public static StringBuffer reallyList(Vector these, Room likeRoom)
	{
		return reallyList(these.elements(),-1,likeRoom);
	}
	public static StringBuffer reallyList(Hashtable these, int ofType, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(these.size()==0) return lines;
		int column=0;
		for(Enumeration e=these.keys();e.hasMoreElements();)
		{
			String thisOne=(String)e.nextElement();
			Object thisThang=these.get(thisOne);
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
				list=CMClass.className(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_CODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).roomID().length()>0)&&(!((Room)thisThang).getArea().Name().equals(likeRoom.getArea().Name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(list,24)+" ");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public static StringBuffer reallyList(Vector these, int ofType, Room likeRoom)
	{ return reallyList(these.elements(),ofType,likeRoom);}
	public static StringBuffer reallyList(Enumeration these, Room likeRoom)
	{ return reallyList(these,-1,likeRoom);}
	public static StringBuffer reallyList(Enumeration these, int ofType, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
				list=CMClass.className(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_CODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).roomID().length()>0)&&(!((Room)thisThang).getArea().Name().equals(likeRoom.getArea().Name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(list,24)+" ");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public static StringBuffer reallyList2Cols(Enumeration these, int ofType, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
				list=CMClass.className(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_CODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).roomID().length()>0)&&(!((Room)thisThang).getArea().Name().equals(likeRoom.getArea().Name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>2)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(list,37)+" ");
			}
		}
		lines.append("\n\r");
		return lines;
	}
}
