package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;

public class Lister
{
	
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
		return reallyList(these,ofType,null);
	}
	public static StringBuffer reallyList(Vector these)
	{
		return reallyList(these,-1,null);
	}
	public static StringBuffer reallyList(Vector these, Room likeRoom)
	{
		return reallyList(these,-1,likeRoom);
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
			boolean ok=true;
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if(((Ability)thisThang).classificationCode()!=ofType)
						ok=false;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).ID().length()>0)&&(!((Room)thisThang).getAreaID().equals(likeRoom.getAreaID())))
				   ok=false;
			}
			if(ok)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(INI.className(thisThang),24));
			}
		}
		lines.append("\n\r");
		return lines;
	}
	
	public static StringBuffer reallyList(Vector these, int ofType, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(these.size()==0) return lines;
		int column=0;
		for(int i=0;i<these.size();i++)
		{
			Object thisThang=these.elementAt(i);
			String thisOne=Util.id(thisThang);
			boolean ok=true;
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if(((Ability)thisThang).classificationCode()!=ofType)
						ok=false;
				}
			}
			if(likeRoom!=null)
			{
				if((((Room)thisThang).ID().length()>0)&&(!((Room)thisThang).getAreaID().equals(likeRoom.getAreaID())))
				   ok=false;
			}
			if(ok)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(INI.className(thisThang),24));
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public static StringBuffer roomDetails(Vector these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(these.size()==0) return lines;
		if(likeRoom==null) return lines;
		int column=0;
		for(int m=0;m<these.size();m++)
		{
			Room thisThang=(Room)these.elementAt(m);
			String thisOne=(String)thisThang.ID();
			if((thisOne.length()>0)&&(thisThang.getAreaID().equals(likeRoom.getAreaID())))
				lines.append(Util.padRight(thisOne,24)+": "+thisThang.displayText()+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}
	public static void list(MOB mob, Vector commands)
	{
		if(commands.size()==0)
		{
			mob.tell("List what?");
			return;
		}
		
		String listThis=CommandProcessor.combine(commands,0).toUpperCase();
		
		if("ITEMS".startsWith(listThis))
			mob.tell(reallyList(MUD.items).toString());
		else
		if("ARMOR".startsWith(listThis))
			mob.tell(reallyList(MUD.armor).toString());
		else
		if("WEAPONS".startsWith(listThis))
			mob.tell(reallyList(MUD.weapons).toString());
		else
		if("MOBS".startsWith(listThis))
			mob.tell(reallyList(MUD.MOBs).toString());
		else
		if("ROOMS".startsWith(listThis))
			mob.tell(roomDetails(MUD.map,mob.location()).toString());
		else
		if("AREA".startsWith(listThis))
			mob.tell(reallyList(MUD.map,mob.location()).toString());
		else
		if("LOCALES".startsWith(listThis))
			mob.tell(reallyList(MUD.locales).toString());
		else
		if("BEHAVIORS".startsWith(listThis))
			mob.tell(reallyList(MUD.behaviors).toString());
		else
		if("EXITS".startsWith(listThis))
			mob.tell(reallyList(MUD.exits).toString());
		else
		if("RACES".startsWith(listThis))
			mob.tell(reallyList(MUD.races).toString());
		else
		if("CLASSES".startsWith(listThis))
			mob.tell(reallyList(MUD.charClasses).toString());
		else
		if("SPELLS".startsWith(listThis))
			mob.tell(reallyList(MUD.abilities,Ability.SPELL).toString());
		else
		if("SKILLS".startsWith(listThis))
			mob.tell(reallyList(MUD.abilities,Ability.SKILL).toString());
		else
		if("TICKS".startsWith(listThis))
			mob.tell(ServiceEngine.listTicks().toString());
		else
		if("MAGIC".startsWith(listThis))
			mob.tell(reallyList(MUD.miscMagic).toString());
		else
		if("LOG".startsWith(listThis))
			mob.tell(Log.getLog().toString());
		else
			mob.tell("Can't list those, try ITEMS, ARMOR, WEAPONS, MOBS, ROOMS, LOCALES, EXITS, RACES, CLASSES, MAGIC, SPELLS, BEHAVIORS, SKILLS, TICKS, LOG, or AREA.");
	}
}
