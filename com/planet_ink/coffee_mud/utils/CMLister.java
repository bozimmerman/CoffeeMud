package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;


public class CMLister
{
	private CMLister(){};
	
	public static StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		StringBuffer say=new StringBuffer("");
		while(items.size()>0)
		{
			Item item=(Item)items.elementAt(0);
			String str=null;
			if((!useName)&&(!item.name().equals(item.Name())))
				str=item.name()+" is here.";
			else
				str=(useName||(item.displayText().length()==0))?item.name():item.displayText();
			int reps=0;
			items.removeElement(item);
			int here=0;
			while(here<items.size())
			{
				Item item2=(Item)items.elementAt(here);
				if(item2==null)
					break;
				else
				{
					String str2=null;
					if((!useName)&&(!item2.name().equals(item2.Name())))
						str2=item2.name()+" is here.";
					else
						str2=(useName||(item2.displayText().length()==0))?item2.name():item2.displayText();
					if(str2.length()==0)
						items.removeElement(item2);
					else
					if((str.equals(str2))
					&&(Sense.seenTheSameWay(mob,item,item2)))
					{
						reps++;
						items.removeElement(item2);
					}
					else
						here++;
				}
			}
			if(Sense.canBeSeenBy(item,mob)
			&&((item.displayText().length()>0)
			    ||Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)
				||useName))
			{
				if(reps==0)	say.append("      ");
				else
				if(reps>=99)
					say.append("("+Util.padLeftPreserve(""+(reps+1),3)+") ");
				else
				if(reps>0)
					say.append(" ("+Util.padLeftPreserve(""+(reps+1),2)+") ");
				if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
					say.append("^H("+CMClass.className(item)+")^N ");
				say.append("^I");
				if(useName)
					say.append(item.name());
				else
				if(!item.name().equals(item.Name()))
					say.append(item.name()+" is here.");
				else
				if(item.displayText().length()>0)
					say.append(item.displayText());
				else
					say.append(item.name());
				say.append(" "+Sense.colorCodes(item,mob)+"^N\n\r");
			}
		}
		return say;
	}
	
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
	
	public static StringBuffer fourColumns(Vector reverseList)
	{
		StringBuffer topicBuffer=new StringBuffer("");
		int col=0;
		for(int i=0;i<reverseList.size();i++)
		{
			if((++col)>4)
			{
				topicBuffer.append("\n\r");
				col=1;
			}
			if(((String)reverseList.elementAt(i)).length()>18)
			{
				topicBuffer.append(Util.padRight((String)reverseList.elementAt(i),(18*2)+1)+" ");
				++col;
			}
			else
				topicBuffer.append(Util.padRight((String)reverseList.elementAt(i),18)+" ");
		}
		return topicBuffer;
	}
}
