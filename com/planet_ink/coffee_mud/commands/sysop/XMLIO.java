package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;

public class XMLIO
{
	
	Rooms myRooms=new Rooms();
							
	public void xml(MOB mob,Vector commands)
	{
		if(mob.isMonster()) return;
		if(commands.size()<2) return;
		commands.removeElementAt(0);
		String s=(String)commands.elementAt(0);
		commands.removeElementAt(0);
		if(s.equalsIgnoreCase("INFO"))
			infoxml(mob,commands);
		else
		if(s.equalsIgnoreCase("CONTENT"))
			contentxml(mob,commands);
		else
		if(s.equalsIgnoreCase("ROOM"))
			roomxml(mob,commands);
	}
	
	public void infoxml(MOB mob, Vector commands)
	{

		if(commands.size()<1) return;

		StringBuffer roomXML=new StringBuffer("");
		String newID=XMLManager.returnXMLValue(Util.combine(commands,0),"ID");
		if(newID.length()==0)
		{
			String newList=XMLManager.returnXMLValue(Util.combine(commands,0),"LIST");
			if(newList.length()>0)
			{
				roomXML.append("<LIST>");
				if(newList.equalsIgnoreCase("ROOM"))
				{
					for(int m=0;m<CMMap.map.size();m++)
					{
						Environmental E=(Environmental)CMMap.map.elementAt(m);
						if(E.ID().length()>0)
							roomXML.append(E.ID()+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("AREA"))
				{
					Hashtable h=new Hashtable();
					for(int m=0;m<CMMap.map.size();m++)
					{
						Room R=(Room)CMMap.map.elementAt(m);
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
					for(int m=0;m<CMClass.locales.size();m++)
					{
						Environmental E=(Environmental)CMClass.locales.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("CLASS"))
				{
					for(int m=0;m<CMClass.charClasses.size();m++)
					{
						CharClass C=(CharClass)CMClass.charClasses.elementAt(m);
						roomXML.append(C.ID()+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("EXIT"))
				{
					for(int m=0;m<CMClass.exits.size();m++)
					{
						Environmental E=(Environmental)CMClass.exits.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("ITEM"))
				{
					for(int m=0;m<CMClass.items.size();m++)
					{
						Environmental E=(Environmental)CMClass.items.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
					for(int m=0;m<CMClass.weapons.size();m++)
					{
						Environmental E=(Environmental)CMClass.weapons.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
					for(int m=0;m<CMClass.armor.size();m++)
					{
						Environmental E=(Environmental)CMClass.armor.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
					for(int m=0;m<CMClass.miscMagic.size();m++)
					{
						Environmental E=(Environmental)CMClass.miscMagic.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("MOB"))
				{
					for(int m=0;m<CMClass.MOBs.size();m++)
					{
						Environmental E=(Environmental)CMClass.MOBs.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("ABILITY"))
				{
					for(int m=0;m<CMClass.abilities.size();m++)
					{
						Environmental E=(Environmental)CMClass.abilities.elementAt(m);
						roomXML.append(CMClass.className(E)+";");
					}
				}
				else
				if(newList.equalsIgnoreCase("BEHAVIOR"))
				{
					for(int m=0;m<CMClass.behaviors.size();m++)
					{
						Behavior B=(Behavior)CMClass.behaviors.elementAt(m);
						roomXML.append(B.ID()+";");
					}
				}
				else
				{
					for(int m=0;m<CMMap.map.size();m++)
					{
						Room R=(Room)CMMap.map.elementAt(m);
						if((R.ID().length()>0)&&(R.getAreaID().equalsIgnoreCase(newList)))
							roomXML.append(R.ID()+";");
					}
				}
				roomXML.append("</LIST>");
			}

			mob.session().rawPrintln(roomXML.toString());
			if(roomXML.length()>30)
				return;
			else
				return;
		}

		int level=0;
		int sp=newID.indexOf(" ");
		if(sp>0)
		{
			level=Util.s_int(newID.substring(sp+1));
			newID=newID.substring(0,sp);
		}

		Room room2=CMClass.getLocale(newID);
		if(room2!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","ROOM"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",CMClass.className(room2)));
			roomXML.append("</OBJECT>");
		}

		Exit exit=CMClass.getExit(newID);
		if(exit!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","EXIT"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",CMClass.className(exit)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTDOOR",""+exit.hasADoor()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLOCK",""+exit.hasALock()));
			roomXML.append("</OBJECT>");
		}

		Item item=CMClass.getItem(newID);
		if(item!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","ITEM"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",CMClass.className(item)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTEXT",""+item.text()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLEVEL",""+item.baseEnvStats().level()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTABILITY",""+item.baseEnvStats().ability()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTREJUV",""+item.baseEnvStats().rejuv()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTUSES",""+item.usesRemaining()));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCONTAINER",""+item.isAContainer()));
			roomXML.append("</OBJECT>");
		}

		MOB mob2=CMClass.getMOB(newID);
		if(mob2!=null)
		{
			if(level>0)
			{
				mob2=(MOB)mob2.newInstance();
				mob2.baseCharStats().getMyClass().buildMOB(mob2,level,0,150,0,'M');
			}
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","MOB"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",CMClass.className(mob2)));
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

		Behavior behave=CMClass.getBehavior(newID);
		if(behave!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","BEHAVIOR"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",CMClass.className(behave)));
			roomXML.append("</OBJECT>");
		}

		Ability able=CMClass.getAbility(newID);
		if(able!=null)
		{
			roomXML.append("<OBJECT>");
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTTYPE","ABILITY"));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTCLASS",CMClass.className(able)));
			roomXML.append(XMLManager.convertXMLtoTag("OBJECTLEVEL",""+item.baseEnvStats().level()));
			roomXML.append("</OBJECT>");
		}

		if(roomXML.length()==0)
			roomXML.append("<OBJECT></OBJECT>");

		mob.session().rawPrintln(roomXML.toString());
		return;
	}
	
	public void contentxml(MOB mob, Vector commands)
	{
		if(commands.size()>=1)
		{
			String possID=Util.combine(commands,0);
			Room room=null;
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room thisRoom=(Room)CMMap.map.elementAt(m);
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
				commands.removeAllElements();
			}
		}

		Room room=mob.location();
		if(commands.size()<1)
		{
			myRooms.clearTheRoom(room);
			ExternalPlay.DBReadContent(room);
			StringBuffer roomXML=new StringBuffer("");
			roomXML.append("<ROOMID>"+room.ID()+"</ROOMID>");
			roomXML.append("<ROOMCONTENTS>");
			roomXML.append("<ROOMMOBS>");
			int num=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB mob2=room.fetchInhabitant(i);
				if(CoffeeUtensils.isEligibleMonster(mob2))
				{
					num++;
					roomXML.append("<ROOMMOB"+num+">");
					roomXML.append(XMLManager.convertXMLtoTag("MOBCLASS",CMClass.className(mob2)));
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
				roomXML.append(XMLManager.convertXMLtoTag("ITEMCLASS",CMClass.className(item)));
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
			return;
		}
		else
		{
			String roomID=XMLManager.returnXMLValue(Util.combine(commands,0),"ROOMID");
			if((roomID.length()>0)&&(!roomID.equalsIgnoreCase(room.ID())))
			{
				for(int m=0;m<CMMap.map.size();m++)
				{
					Room thisRoom=(Room)CMMap.map.elementAt(m);
					if(thisRoom.ID().equalsIgnoreCase(roomID))
					{
					   room=thisRoom;
					   break;
					}
				}
				if(room!=mob.location())
					room.bringMobHere(mob,true);
			}
			String roomBlock=XMLManager.returnXMLBlock(Util.combine(commands,0),"ROOMCONTENTS");
			if(roomBlock.length()<10) return;

			// if necessary, remove it all
			// and start over.. yes, even for an edit!
			myRooms.clearTheRoom(room);

			/// do mobs..
			int num=1;
			String mobBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMMOBS");
			String mBlock="";
			if(mobBlock.length()>10)
				mBlock=XMLManager.returnXMLBlock(mobBlock,"ROOMMOB1");
			while(mBlock.length()>10)
			{
				String newClass=XMLManager.returnXMLValue(mBlock,"MOBCLASS");
				String newText=XMLManager.returnXMLValue(mBlock,"MOBTEXT");
				int newLevel=Util.s_int(XMLManager.returnXMLValue(mBlock,"MOBLEVEL"));
				int newAbility=Util.s_int(XMLManager.returnXMLValue(mBlock,"MOBABILITY"));
				int newRejuv=Util.s_int(XMLManager.returnXMLValue(mBlock,"MOBREJUV"));
				if(newRejuv<=0) newRejuv=Integer.MAX_VALUE;
				MOB newMOB=CMClass.getMOB(newClass);
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
					newMOB.resetToMaxState();
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
				iBlock=XMLManager.returnXMLBlock(itemBlock,"ROOMITEM1");
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

				Item newItem=CMClass.getItem(newClass);
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

			myRooms.clearDebriAndRestart(room,0);
			Log.sysOut("CNTNTXML",mob.name()+" updated content of room "+room.ID()+".");
			mob.session().rawPrintln("<RESPONSE>Done.</RESPONSE>");
			return;
		}
	}
	
	public void roomxml(MOB mob, Vector commands)
	{
		if(commands.size()>=1)
		{
			String possID=Util.combine(commands,0);
			Room room=null;
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room thisRoom=(Room)CMMap.map.elementAt(m);
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
				commands.removeAllElements();
			}
		}

		Room room=mob.location();
		if(commands.size()<1)
		{
			StringBuffer roomXML=new StringBuffer("");
			roomXML.append("<ROOM>");
			roomXML.append(XMLManager.convertXMLtoTag("ROOMID",room.ID()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMCLASS",CMClass.className(room)));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMAREA",room.getAreaID()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMDISPLAYTEXT",room.displayText()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMDESCRIPTION",room.description()));
			roomXML.append(XMLManager.convertXMLtoTag("ROOMTEXT",room.text()));
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
					roomXML.append(XMLManager.convertXMLtoTag("EXITCLASS",CMClass.className(exit)));
					roomXML.append(XMLManager.convertXMLtoTag("EXITTEXT",exit.text()));
					roomXML.append(XMLManager.convertXMLtoTag("EXITSAME",""+(exit==opExit)));
					roomXML.append(XMLManager.convertXMLtoTag("EXITDOOR",""+exit.hasADoor()));
				}
				roomXML.append("</EXIT>");
				roomXML.append("</ROOM"+Directions.getDirectionName(d).toUpperCase()+">");
			}
			roomXML.append("</ROOM>");
			mob.session().rawPrintln(roomXML.toString());
			return;
		}
		else
		{
			String response="Done.";
			String roomBlock=XMLManager.returnXMLBlock(Util.combine(commands,0),"ROOM");
			if(roomBlock.length()<10) return;
			String newID=XMLManager.returnXMLValue(roomBlock,"ROOMID");
			String newRoomClass=XMLManager.returnXMLValue(roomBlock,"ROOMCLASS");
			String newArea=XMLManager.returnXMLValue(roomBlock,"ROOMAREA");
			String newDisplay=XMLManager.returnXMLValue(roomBlock,"ROOMDISPLAYTEXT");
			String newDescription=XMLManager.returnXMLValue(roomBlock,"ROOMDESCRIPTION");

			boolean isNewRoom=false;
			if(newID.equalsIgnoreCase("NEW"))
			{
				Room newRoom=CMClass.getLocale(newRoomClass);
				if(newRoom==null) return;
				isNewRoom=true;
				room=(Room)newRoom.newInstance();
				room.setID(myRooms.getOpenRoomID(newArea));
				room.setAreaID(newArea);
				newID=room.ID();
				ExternalPlay.DBCreate(room,newRoomClass);
				CMMap.map.addElement(room);
				response=newID;
				Log.sysOut("ROOMXML",mob.name()+" created room "+room.ID()+".");
			}
			else
			{
				if((!room.ID().equalsIgnoreCase(newID))&&(newID.length()>0))
				{
					for(int m=0;m<CMMap.map.size();m++)
					{
						Room thisRoom=(Room)CMMap.map.elementAt(m);
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
						Room newRoom=CMMap.getRoom(newDoor);
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
							Exit newExit=CMClass.getExit(newClass);
							if(newExit!=null)
							{
								newExit=(Exit)newExit.newInstance();
								if(door!=null)
								{
									opExit=(Exit)door.exits()[Directions.getOpDirectionCode(d)];
									if(opExit==exit)
									{
										door.exits()[Directions.getOpDirectionCode(d)]=newExit;
										ExternalPlay.DBUpdateExits(door);
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
			ExternalPlay.DBUpdateRoom(room);
			ExternalPlay.DBUpdateExits(room);
			if(room instanceof GridLocale)
				((GridLocale)room).buildGrid();
			if(room!=mob.location())
				room.bringMobHere(mob,true);
			mob.session().rawPrintln("<RESPONSE>"+response+"</RESPONSE>");
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room R=(Room)CMMap.map.elementAt(m);
				for(int m1=0;m1<CMMap.map.size();m1++)
					if((((Room)CMMap.map.elementAt(m1))==R)&&(m1!=m))
						Log.errOut("ROOMXML",R.ID()+"="+((Room)CMMap.map.elementAt(m1)).ID());

			}
			return;
		}
	}
}
