package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.commands.sysop.Rooms;
import java.io.*;
import java.util.*;


public class Archon_CONTENTXML extends ArchonSkill
{
	public Archon_CONTENTXML()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="ContentXML";

		triggerStrings.addElement("CONTENTXML");

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Archon_CONTENTXML();
	}
	
	public boolean invoke(MOB mob, Vector commands)
	{

		if(mob.isMonster()) return false;
		if(commands.size()==1)
		{
			String possID=CommandProcessor.combine(commands,0);
			Room room=null;
			for(int m=0;m<MUD.map.size();m++)
			{
				Room thisRoom=(Room)MUD.map.elementAt(m);
				if(thisRoom.ID().equalsIgnoreCase(possID))
				{
				   room=thisRoom;
				   break;
				}
			}
			if(room!=null)
			{
				if(room!=mob.location())
					room.bringMobHere(mob,true);
				commands.removeElementAt(0);
			}
		}
		
		Room room=mob.location();
		if(commands.size()<1)
		{
			Rooms.clearTheRoom(room);
			RoomLoader.DBReadContent(room);
			StringBuffer roomXML=new StringBuffer("");
			roomXML.append("<ROOMID>"+room.ID()+"</ROOMID>");
			roomXML.append("<ROOMCONTENTS>");
			roomXML.append("<ROOMMOBS>");
			int num=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB mob2=room.fetchInhabitant(i);
				if(RoomLoader.isEligibleMonster(mob2))
				{
					num++;
					roomXML.append("<ROOMMOB"+num+">");
					roomXML.append(XMLManager.convertXMLtoTag("MOBCLASS",INI.className(mob2)));
					roomXML.append(XMLManager.convertXMLtoTag("MOBTEXT",""+mob2.text()));
					roomXML.append(XMLManager.convertXMLtoTag("MOBLEVEL",""+mob2.baseEnvStats().level()));
					roomXML.append(XMLManager.convertXMLtoTag("MOBABILITY",""+mob2.baseEnvStats().ability()));
					roomXML.append(XMLManager.convertXMLtoTag("MOBREJUV",""+mob2.baseEnvStats().rejuv()));
					roomXML.append("</ROOMMOB"+num+">");
					
				}
			}
			roomXML.append("</ROOMMOBS>");
			roomXML.append("<ROOMITEMS>");
			num=0;
			for(int i=0;i<room.numItems();i++)
			{
				Item item=room.fetchItem(i);
				num++;
				roomXML.append("<ROOMITEM"+num+">");
				roomXML.append(XMLManager.convertXMLtoTag("ITEMCLASS",INI.className(item)));
				roomXML.append(XMLManager.convertXMLtoTag("ITEMTEXT",""+item.text()));
				roomXML.append(XMLManager.convertXMLtoTag("ITEMLEVEL",""+item.baseEnvStats().level()));
				roomXML.append(XMLManager.convertXMLtoTag("ITEMABILITY",""+item.baseEnvStats().ability()));
				roomXML.append(XMLManager.convertXMLtoTag("ITEMREJUV",""+item.baseEnvStats().rejuv()));
				roomXML.append(XMLManager.convertXMLtoTag("ITEMUSES",""+item.usesRemaining()));
				int locationNum=0;
				if(item.location()!=null)
					for(int num2=0;num2<room.numItems();num2++)
					{
						if(room.fetchItem(num2)==item.location())
						{
							locationNum=num2+1;
							break;
						}
					}
				roomXML.append(XMLManager.convertXMLtoTag("ITEMLOCATION",""+locationNum));
				roomXML.append("</ROOMITEM"+num+">");
			}
			
			roomXML.append("</ROOMITEMS>");
			roomXML.append("</ROOMCONTENTS>");
			mob.session().rawPrintln(roomXML.toString());	
			return true;
		}
		else
		{
			String roomID=XMLManager.returnXMLValue(CommandProcessor.combine(commands,0),"ROOMID");
			if((roomID.length()>0)&&(!roomID.equalsIgnoreCase(room.ID())))
			{
				for(int m=0;m<MUD.map.size();m++)
				{
					Room thisRoom=(Room)MUD.map.elementAt(m);
					if(thisRoom.ID().equalsIgnoreCase(roomID))
					{
					   room=thisRoom;
					   break;
					}
				}
				if(room!=mob.location())
					room.bringMobHere(mob,true);
			}
			String roomBlock=XMLManager.returnXMLBlock(CommandProcessor.combine(commands,0),"ROOMCONTENTS");
			if(roomBlock.length()<10) return false;
			
			// if necessary, remove it all
			// and start over.. yes, even for an edit!
			Rooms.clearTheRoom(room);
			
			/// do mobs.. 
			int num=1;
			String mobBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMMOBS");
			String mBlock="";
			if(mobBlock.length()>10)
				mBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMMOB1");
			while(mBlock.length()>10)
			{
				String newClass=XMLManager.returnXMLValue(mBlock,"MOBCLASS");
				String newText=XMLManager.returnXMLValue(mBlock,"MOBTEXT");
				int newLevel=Util.s_int(XMLManager.returnXMLValue(mBlock,"MOBLEVEL"));
				int newAbility=Util.s_int(XMLManager.returnXMLValue(mBlock,"MOBABILITY"));
				int newRejuv=Util.s_int(XMLManager.returnXMLValue(mBlock,"MOBREJUV"));
				if(newRejuv<=0) newRejuv=Integer.MAX_VALUE;
				MOB newMOB=MUD.getMOB(newClass);
				if(newMOB!=null)
				{
					newMOB=(MOB)newMOB.newInstance();
					newMOB.setMiscText(newText);
					newMOB.baseEnvStats().setLevel(newLevel);
					newMOB.baseEnvStats().setAbility(newAbility);
					newMOB.baseEnvStats().setRejuv(newRejuv);
					
					newMOB.setStartRoom(room);
					newMOB.setLocation(room);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.bringToLife(room);
					
				}
				// now rebuild!
				num++;
				mBlock=XMLManager.returnXMLBlock(mobBlock,"ROOMMOB"+num);
			}
			
			
			
			/// do items.. 
			num=1;
			String itemBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMITEMS");
			String iBlock="";
			if(itemBlock.length()>10)
				iBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMITEM1");
			Vector itemVec=new Vector();
			Hashtable itemMap=new Hashtable();
			while(iBlock.length()>10)
			{
				
				String newClass=XMLManager.returnXMLValue(iBlock,"ITEMCLASS");
				String newText=XMLManager.returnXMLValue(iBlock,"ITEMTEXT");
				int newLevel=Util.s_int(XMLManager.returnXMLValue(iBlock,"ITEMLEVEL"));
				int newAbility=Util.s_int(XMLManager.returnXMLValue(iBlock,"ITEMABILITY"));
				int newRejuv=Util.s_int(XMLManager.returnXMLValue(iBlock,"ITEMREJUV"));
				if(newRejuv<=0) newRejuv=Integer.MAX_VALUE;
				int newUses=Util.s_int(XMLManager.returnXMLValue(iBlock,"ITEMUSES"));
				int newLocation=Util.s_int(XMLManager.returnXMLValue(iBlock,"ITEMLOCATION"));
				
				Item newItem=MUD.getItem(newClass);
				if(newItem!=null)
				{
					newItem=(Item)newItem.newInstance();
					newItem.setMiscText(newText);
					newItem.baseEnvStats().setLevel(newLevel);
					newItem.baseEnvStats().setAbility(newAbility);
					newItem.baseEnvStats().setRejuv(newRejuv);
					newItem.setUsesRemaining(newUses);
					newItem.setLocation(null);
					newItem.recoverEnvStats();
					
					room.addItem(newItem);
					itemVec.addElement(newItem);
					itemMap.put(newItem,new Integer(newLocation));
				}
				num++;
				iBlock=XMLManager.returnXMLBlock(itemBlock,"ROOMITEM"+num);
			}
			if(itemBlock.length()>10)
			{
				for(Enumeration e=itemMap.keys();e.hasMoreElements();)
				{
					Item item=(Item)e.nextElement();
					int loc=((Integer)itemMap.get(item)).intValue();
					if((loc>0)&&(loc<=itemVec.size()))
						item.setLocation((Item)itemVec.elementAt(loc-1));
				}
			}
			
			Rooms.clearDebriAndRestart(room,0);
			Log.sysOut("CNTNTXML",mob.name()+" updated content of room "+room.ID()+".");
			mob.session().rawPrintln("<RESPONSE>Done.</RESPONSE>");	
			return true;
		}
	}
}