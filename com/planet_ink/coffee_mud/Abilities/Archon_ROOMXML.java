package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.commands.sysop.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;


public class Archon_ROOMXML extends ArchonSkill
{
	public Archon_ROOMXML()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="RoomXML";

		triggerStrings.addElement("ROOMXML");

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Archon_ROOMXML();
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
			StringBuffer roomXML=new StringBuffer("");
			roomXML.append("<ROOM>");
			roomXML.append(XMLManager.convertXMLtoTag("ROOMID",room.ID()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMCLASS",INI.className(room)));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMAREA",room.getAreaID()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMDISPLAYTEXT",room.displayText()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMDESCRIPTION",room.description()));
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room door=room.doors()[d];
				Exit exit=room.exits()[d];
				Exit opExit=null;
				roomXML.append("<ROOM"+Directions.getDirectionName(d).toUpperCase()+">");
				if((door!=null)&&(door.ID().length()>0))
				{
					roomXML.append(XMLManager.convertXMLtoTag("DOOR",door.ID()));
					opExit=door.exits()[Directions.getOpDirectionCode(d)];
				}
				else
					roomXML.append("<DOOR></DOOR>");
				roomXML.append("<EXIT>");
				if((exit!=null)&&(door!=null)&&(door.ID().length()>0))
				{
					roomXML.append(XMLManager.convertXMLtoTag("EXITCLASS",INI.className(exit)));
					roomXML.append(XMLManager.convertXMLtoTag("EXITTEXT",exit.text()));
					roomXML.append(XMLManager.convertXMLtoTag("EXITSAME",""+(exit==opExit)));
					roomXML.append(XMLManager.convertXMLtoTag("EXITDOOR",""+exit.hasADoor()));
				}
				roomXML.append("</EXIT>");
				roomXML.append("</ROOM"+Directions.getDirectionName(d).toUpperCase()+">");
			}
			roomXML.append("</ROOM>");
			mob.session().rawPrintln(roomXML.toString());	
			return true;
		}
		else
		{
			String response="Done.";
			String roomBlock=XMLManager.returnXMLBlock(CommandProcessor.combine(commands,0),"ROOM");
			if(roomBlock.length()<10) return false;
			String newID=XMLManager.returnXMLValue(roomBlock,"ROOMID");
			String newRoomClass=XMLManager.returnXMLValue(roomBlock,"ROOMCLASS");
			String newArea=XMLManager.returnXMLValue(roomBlock,"ROOMAREA");
			String newDisplay=XMLManager.returnXMLValue(roomBlock,"ROOMDISPLAYTEXT");
			String newDescription=XMLManager.returnXMLValue(roomBlock,"ROOMDESCRIPTION");
			
			boolean isNewRoom=false;
			if(newID.equalsIgnoreCase("NEW"))
			{
				Room newRoom=MUD.getLocale(newRoomClass);
				if(newRoom==null) return false;
				isNewRoom=true;
				room=(Room)newRoom.newInstance();
				room.setID(Rooms.getOpenRoomID(newArea));
				room.setAreaID(newArea);
				newID=room.ID();
				RoomLoader.DBCreate(room,newRoomClass);
				MUD.map.addElement(room);
				response=newID;
				Log.sysOut("ROOMXML",mob.name()+" created room "+room.ID()+".");
			}
			else
			{
				if((!room.ID().equalsIgnoreCase(newID))&&(newID.length()>0))
				{
					for(int m=0;m<MUD.map.size();m++)
					{
						Room thisRoom=(Room)MUD.map.elementAt(m);
						if(thisRoom.ID().equalsIgnoreCase(newID))
						{
						   room=thisRoom;
						   break;
						}
					}
					if(room!=mob.location())
						room.bringMobHere(mob,true);
				}
				Log.sysOut("ROOMXML",mob.name()+" updated room "+room.ID()+".");
			}
			
			room.setID(newID);
			room.setDisplayText(newDisplay);
			room.setDescription(newDescription);
			room.setAreaID(newArea);
			
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				String block=XMLManager.returnXMLBlock(roomBlock,"ROOM"+Directions.getDirectionName(d).toUpperCase());
				String newDoor=null;
				String newClass=null;
				String newText=null;
				boolean exitSame=true;
				Room door=room.doors()[d];
				Exit exit=room.exits()[d];
				Exit opExit=null;
				if(block.length()>10)
				{
					newDoor=XMLManager.returnXMLValue(block,"DOOR");
					if(((newDoor.length()==0)&&(door==null))
					   ||(newDoor.length()>0)&&(door!=null)&&(door.ID().equals(newDoor)))
					{
						// its alllll good
					}
					else
					if(newDoor.length()==0)
						room.doors()[d]=null;
					else
					{
						Room newRoom=MUD.getRoom(newDoor);
						if(newRoom!=null)
						{
							room.doors()[d]=newRoom;
							door=newRoom;
						}
					}
					
					String exblock=XMLManager.returnXMLBlock(block,"EXIT");
					if(exblock.length()>10)
					{
						newClass=XMLManager.returnXMLValue(exblock,"EXITCLASS");
						if(((exit!=null)&&(!exit.ID().equalsIgnoreCase(newClass))||(exit==null)))
						{
							Exit newExit=MUD.getExit(newClass);
							if(newExit!=null)
							{
								newExit=(Exit)newExit.newInstance();
								if(door!=null)
								{
									opExit=(Exit)door.exits()[Directions.getOpDirectionCode(d)];
									if(opExit==exit)
									{
										door.exits()[Directions.getOpDirectionCode(d)]=newExit;
										RoomLoader.DBUpdateExits(door);
									}
								}
								room.exits()[d]=newExit;
								exit=newExit;
							}
						}
						newText=XMLManager.returnXMLValue(exblock,"EXITTEXT");
						if(exit!=null)
							exit.setMiscText(newText);
						//exitSame=XMLManager.returnXMLBoolean(exblock,"EXITSAME");
					}
					else
					{
						room.exits()[d]=null;
						exit=null;
					}
					
					
				}
			}
			RoomLoader.DBUpdateRoom(room);
			RoomLoader.DBUpdateExits(room);
			if(room instanceof GridLocale)
				((GridLocale)room).buildGrid();
			if(room!=mob.location())
				room.bringMobHere(mob,true);
			mob.session().rawPrintln("<RESPONSE>"+response+"</RESPONSE>");
			for(int m=0;m<MUD.map.size();m++)
			{
				Room R=(Room)MUD.map.elementAt(m);
				for(int m1=0;m1<MUD.map.size();m1++)
					if((((Room)MUD.map.elementAt(m1))==R)&&(m1!=m))
						Log.errOut("ROOMXML",R.ID()+"="+((Room)MUD.map.elementAt(m1)).ID());
					
			}
			return true;
		}
	}
}