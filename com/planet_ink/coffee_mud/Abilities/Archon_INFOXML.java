package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;


public class Archon_INFOXML extends ArchonSkill
{
	public Archon_INFOXML()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="InfoXML";

		triggerStrings.addElement("INFOXML");

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Archon_INFOXML();
	}
	
	public boolean invoke(MOB mob, Vector commands)
	{

		if(mob.isMonster()) return false;
		
		Room room=mob.location();
		if(commands.size()<1) return false;
		
		StringBuffer roomXML=new StringBuffer("");
		String newID=XMLManager.returnXMLValue(CommandProcessor.combine(commands,0),"ID");
		if(newID.length()==0)
		{
			String newList=XMLManager.returnXMLValue(CommandProcessor.combine(commands,0),"LIST");
			if(newList.length()>0)
			{
				roomXML.append("<LIST>");
				if(newList.equalsIgnoreCase("ROOM"))
				{
					for(int m=0;m<MUD.map.size();m++)
					{
						Environmental E=(Environmental)MUD.map.elementAt(m);
						if(E.ID().length()>0)
							roomXML.append(E.ID()+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("AREA"))
				{
					Hashtable h=new Hashtable();
					for(int m=0;m<MUD.map.size();m++)
					{
						Room R=(Room)MUD.map.elementAt(m);
						if(h.get(R.getAreaID())==null)
						{
							roomXML.append(R.getAreaID()+";");
							h.put(R.getAreaID(),R.getAreaID());
						}
					}
				}
				else
				if(newList.equalsIgnoreCase("LOCALE"))
				{
					for(int m=0;m<MUD.locales.size();m++)
					{
						Environmental E=(Environmental)MUD.locales.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("CLASS"))
				{
					for(int m=0;m<MUD.charClasses.size();m++)
					{
						CharClass C=(CharClass)MUD.charClasses.elementAt(m);
						roomXML.append(C.ID()+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("EXIT"))
				{
					for(int m=0;m<MUD.exits.size();m++)
					{
						Environmental E=(Environmental)MUD.exits.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("ITEM"))
				{
					for(int m=0;m<MUD.items.size();m++)
					{
						Environmental E=(Environmental)MUD.items.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
					for(int m=0;m<MUD.weapons.size();m++)
					{
						Environmental E=(Environmental)MUD.weapons.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
					for(int m=0;m<MUD.armor.size();m++)
					{
						Environmental E=(Environmental)MUD.armor.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
					for(int m=0;m<MUD.miscMagic.size();m++)
					{
						Environmental E=(Environmental)MUD.miscMagic.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("MOB"))
				{
					for(int m=0;m<MUD.MOBs.size();m++)
					{
						Environmental E=(Environmental)MUD.MOBs.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("ABILITY"))
				{
					for(int m=0;m<MUD.abilities.size();m++)
					{
						Environmental E=(Environmental)MUD.abilities.elementAt(m);
						roomXML.append(INI.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("BEHAVIOR"))
				{
					for(int m=0;m<MUD.behaviors.size();m++)
					{
						Behavior B=(Behavior)MUD.behaviors.elementAt(m);
						roomXML.append(B.ID()+";");
					}
				}
				else
				{
					for(int m=0;m<MUD.map.size();m++)
					{
						Room R=(Room)MUD.map.elementAt(m);
						if((R.ID().length()>0)&&(R.getAreaID().equalsIgnoreCase(newList)))
							roomXML.append(R.ID()+";");
					}
				}
				roomXML.append("</LIST>");
			}
			
			mob.session().rawPrintln(roomXML.toString());
			if(roomXML.length()>30)
				return true;
			else
				return false;
		}
		
		int level=0;
		int sp=newID.indexOf(" ");
		if(sp>0)
		{
			level=Util.s_int(newID.substring(sp+1));
			newID=newID.substring(0,sp);
		}
		
		Room room2=MUD.getLocale(newID);
		if(room2!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","ROOM"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",INI.className(room2)));
			roomXML.append("</OBJECT>");
		}
		
		Exit exit=MUD.getExit(newID);
		if(exit!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","EXIT"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",INI.className(exit)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTDOOR",""+exit.hasADoor()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLOCK",""+exit.hasALock()));
			roomXML.append("</OBJECT>");
		}
		
		Item item=MUD.getItem(newID);
		if(item!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","ITEM"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",INI.className(item)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTEXT",""+item.text()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLEVEL",""+item.baseEnvStats().level()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTABILITY",""+item.baseEnvStats().ability()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTREJUV",""+item.baseEnvStats().rejuv()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTUSES",""+item.usesRemaining()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCONTAINER",""+item.isAContainer()));
			roomXML.append("</OBJECT>");
		}
		
		MOB mob2=MUD.getMOB(newID);
		if(mob2!=null)
		{
			if(level>0)
			{
				mob2=(MOB)mob2.newInstance();
				mob2.baseCharStats().getMyClass().buildMOB(mob2,level,0,150,0,'M');
			}
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","MOB"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",INI.className(mob2)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTEXT",""+mob2.text()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLEVEL",""+mob2.baseEnvStats().level()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTABILITY",""+mob2.baseEnvStats().ability()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTREJUV",""+mob2.baseEnvStats().rejuv()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTARMOR",""+mob2.baseEnvStats().armor()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTATTACK",""+mob2.baseEnvStats().attackAdjustment()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTDAMAGE",""+mob2.baseEnvStats().damage()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTMONEY",""+mob2.getMoney()));
			roomXML.append("</OBJECT>");
		}

		Behavior behave=MUD.getBehavior(newID);
		if(behave!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","BEHAVIOR"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",INI.className(behave)));
			roomXML.append("</OBJECT>");
		}
		
		Ability able=MUD.getAbility(newID);
		if(able!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","ABILITY"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",INI.className(able)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLEVEL",""+item.baseEnvStats().level()));
			roomXML.append("</OBJECT>");
		}
		
		if(roomXML.length()==0)
			roomXML.append("<OBJECT></OBJECT>");
			
		mob.session().rawPrintln(roomXML.toString());
		return true;
	}
}