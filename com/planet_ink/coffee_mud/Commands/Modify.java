package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Modify extends BaseGenerics
{
	public Modify(){}

	private String[] access={"MODIFY","MOD"};
	public String[] getAccessWords(){return access;}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME](@ room/[MOB NAME]) [LEVEL, ABILITY, REJUV, USES, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		MOB srchMob=mob;
		Room srchRoom=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell("MOB '"+rest+"' not found.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return;
				}
				srchMob=M;
				srchRoom=null;
			}
		}
		String command="";
		if(commands.size()>3)
			command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=Util.combine(commands,4);

		Item modItem=null;
		if((srchMob!=null)&&(srchRoom!=null))
			modItem=(Item)srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,itemID,Item.WORN_REQ_ANY);
		else
		if(srchMob!=null)
			modItem=srchMob.fetchInventory(itemID);
		else
		if(srchRoom!=null)
			modItem=srchRoom.fetchAnyItem(itemID);
		if(modItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if(command.equals("LEVEL"))
		{
			int newLevel=Util.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.baseEnvStats().setLevel(newLevel);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=Util.s_int(restStr);
			modItem.baseEnvStats().setAbility(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("HEIGHT"))
		{
			int newAbility=Util.s_int(restStr);
			modItem.baseEnvStats().setHeight(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=Util.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.baseEnvStats().setRejuv(newRejuv);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
			else
			{
				modItem.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modItem.recoverEnvStats();
				mob.tell(modItem.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("USES"))
		{
			int newUses=Util.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modItem.isGeneric())
				genMiscSet(mob,modItem);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if((command.length()==0)&&(modItem.isGeneric()))
		{
			genMiscSet(mob,modItem);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, HEIGHT, REJUV, USES, or MISC.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		Log.sysOut("Items",mob.Name()+" modified item "+modItem.ID()+".");
	}

	private void flunkCmd1(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, AFFECTS, BEHAVIORS, CLASS, XGRID, YGRID] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

	private void flunkCmd2(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, FILE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}
	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			Room oldRoom=(Room)mob.location().copyOf();
			while(!ok)
			{
				int showNumber=0;
				genRoomType(mob,mob.location(),++showNumber,showFlag);
				genDisplayText(mob,mob.location(),++showNumber,showFlag);
				genDescription(mob,mob.location(),++showNumber,showFlag);
				if(mob.location() instanceof GridLocale)
				{
					genGridLocaleX(mob,(GridLocale)mob.location(),++showNumber,showFlag);
					genGridLocaleY(mob,(GridLocale)mob.location(),++showNumber,showFlag);
					((GridLocale)mob.location()).buildGrid();
				}
				genBehaviors(mob,mob.location(),++showNumber,showFlag);
				genAffects(mob,mob.location(),++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
			if(!oldRoom.sameAs(mob.location()))
			{
				CMClass.DBEngine().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
				Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
			}
			return;
		}
		if(commands.size()<3) { flunkCmd1(mob); return;}

		String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=Util.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Area A=CMMap.getArea(restStr);
			String checkID=CMMap.getOpenRoomID(restStr);
			boolean reid=false;
			if(A==null)
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '"+restStr+"'.  Are you SURE (y/N)?","N"))
					{
						String areaType="";
						int tries=0;
						while((areaType.length()==0)&&((++tries)<10))
						{
							areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
							if(CMClass.getAreaType(areaType)==null)
							{
								mob.session().println("Invalid area type! Valid ones are:");
								mob.session().println(CMLister.reallyList(CMClass.areaTypes(),-1,null).toString());
								areaType="";
							}
						}
						if(areaType.length()==0) areaType="StdArea";
						A=CMClass.DBEngine().DBCreateArea(restStr,areaType);
						mob.location().setArea(A);
						reid=true;
					}
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"This entire area twitches.\n\r");
				}
				else
				{
					mob.tell("Sorry Charlie!");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
			}
			else
			{
				mob.location().setArea(A);
				if(!A.getProperMap().hasMoreElements())
					reid=true;
				else
					CMClass.DBEngine().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"This area twitches.\n\r");
			}
			
			if(reid)
			{
				CMMap.delRoom(mob.location());
				String oldID=mob.location().roomID();
				mob.location().setRoomID(checkID);
				CMClass.DBEngine().DBReCreate(mob.location(),oldID);
				CMMap.addRoom(mob.location());
				try
				{
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
						{
							Room thatRoom=R.rawDoors()[dir];
							if(thatRoom==mob.location())
							{
								CMClass.DBEngine().DBUpdateExits(R);
								break;
							}
						}
					}
			    }catch(NoSuchElementException e){}
			}
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDisplayText(restStr);
			CMClass.DBEngine().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Room newRoom=CMClass.getLocale(restStr);
			if(newRoom==null)
			{
				mob.tell("'"+restStr+"' is not a valid room locale.");
				return;
			}
			changeRoomType(mob.location(),newRoom);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("XGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setXSize(Util.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMClass.DBEngine().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("YGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setYSize(Util.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMClass.DBEngine().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDescription(restStr);
			CMClass.DBEngine().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
			genAffects(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMClass.DBEngine().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
			genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMClass.DBEngine().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		{
			flunkCmd1(mob);
			return;
		}
		mob.location().recoverRoomStats();
		Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.Name();
		Vector allMyDamnRooms=new Vector();
		for(Enumeration e=myArea.getProperMap();e.hasMoreElements();)
			allMyDamnRooms.addElement(e.nextElement());

		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			while(!ok)
			{
				int showNumber=0;
				genName(mob,myArea,++showNumber,showFlag);
				genDescription(mob,myArea,++showNumber,showFlag);
				genAuthor(mob,myArea,++showNumber,showFlag);
				genTechLevel(mob,myArea,++showNumber,showFlag);
				genClimateType(mob,myArea,++showNumber,showFlag);
				genTimeClock(mob,myArea,++showNumber,showFlag);
				genCurrency(mob,myArea,++showNumber,showFlag);
				genArchivePath(mob,myArea,++showNumber,showFlag);
                genParentAreas(mob,myArea,++showNumber,showFlag);
                genChildAreas(mob,myArea,++showNumber,showFlag);
				genSubOps(mob,myArea,++showNumber,showFlag);
				genBehaviors(mob,myArea,++showNumber,showFlag);
				genAffects(mob,myArea,++showNumber,showFlag);
				genImage(mob,myArea,++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
		}
		else
		{
			if(commands.size()<3) { flunkCmd1(mob); return;}

			String command=((String)commands.elementAt(2)).toUpperCase();
			String restStr="";
			if(commands.size()>=3)
				restStr=Util.combine(commands,3);

			if(command.equalsIgnoreCase("NAME"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setName(restStr);
			}
			else
			if(command.equalsIgnoreCase("DESC"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("FILE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setArchivePath(restStr);
			}
			else
			if(command.equalsIgnoreCase("CLIMATE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				int newClimate=0;
				for(int i=0;i<restStr.length();i++)
					switch(Character.toUpperCase(restStr.charAt(i)))
					{
					case 'R':
						newClimate=newClimate|Area.CLIMASK_WET;
						break;
					case 'H':
						newClimate=newClimate|Area.CLIMASK_HOT;
						break;
					case 'C':
						newClimate=newClimate|Area.CLIMASK_COLD;
						break;
					case 'W':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'D':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'N':
						// do nothing
						break;
					default:
						mob.tell("Invalid CLIMATE code: '"+restStr.charAt(i)+"'.  Valid codes include: R)AINY, H)OT, C)OLD, D)RY, W)INDY, N)ORMAL.\n\r");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
						return;
					}
				myArea.setClimateType(newClimate);
			}
			else
			if(command.equalsIgnoreCase("ADDSUB"))
			{
				if((commands.size()<4)||(!CMClass.DBEngine().DBUserSearch(null,restStr)))
				{
					mob.tell("Unknown or invalid username given.\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.addSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("DELSUB"))
			{
				if((commands.size()<4)||(!myArea.amISubOp(restStr)))
				{
					mob.tell("Unknown or invalid staff name given.  Valid names are: "+myArea.getSubOpList()+".\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.delSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("AFFECTS"))
			{
				genAffects(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			if(command.equalsIgnoreCase("BEHAVIORS"))
			{
				genBehaviors(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			{
				flunkCmd2(mob);
				return;
			}
		}

		if((!myArea.Name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				for(Enumeration r=myArea.getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R.roomID().startsWith(oldName+"#"))
					&&(CMMap.getRoom(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1))==null))
					{
						String oldID=R.roomID();
						R.setRoomID(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1));
						CMClass.DBEngine().DBReCreate(R,oldID);
					}
					else
						CMClass.DBEngine().DBUpdateRoom(R);
				}
				myArea.clearMaps();
				try
				{
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						boolean doIt=false;
						for(int d=0;d<R.rawDoors().length;d++)
						{
							Room R2=R.rawDoors()[d];
							if((R2!=null)&&(R2.getArea()==myArea))
							{ doIt=true; break;}
						}
						if(doIt)
							CMClass.DBEngine().DBUpdateExits(R);
					}
			    }catch(NoSuchElementException e){}
			}
			else
				myArea.setName(oldName);
		}
		else
			myArea.setName(oldName);
		myArea.recoverEnvStats();
		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		if(myArea.name().equals(oldName))
			CMClass.DBEngine().DBUpdateArea(myArea.Name(),myArea);
		else
		{
			CMClass.DBEngine().DBUpdateArea(oldName,myArea);
			CMMap.renameRooms(myArea,oldName,allMyDamnRooms);
		}
		Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
	}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		
		Exit thisExit=mob.location().rawExits()[direction];
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit '"+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if(thisExit.isGeneric())
		{
			modifyGenExit(mob,thisExit);
			return;
		}
		
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		//String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=Util.combine(commands,3);

		if(thisExit.isGeneric())
			modifyGenExit(mob,thisExit);
		else
		if(restStr.length()>0)
			thisExit.setMiscText(restStr);
		else
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		
		try
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room room=(Room)r.nextElement();
				for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
				{
					Exit exit=room.rawExits()[e2];
					if((exit!=null)&&(exit==thisExit))
					{
						CMClass.DBEngine().DBUpdateExits(room);
						room.getArea().fillInAreaRoom(room);
						break;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,thisExit.name()+" shake(s) under the transforming power.");
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
	}

	public boolean races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		modifyGenRace(mob,R);
		CMClass.DBEngine().DBDeleteRace(R.ID());
		CMClass.DBEngine().DBCreateRace(R.ID(),R.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,R.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public boolean classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY CLASS [CLASS ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=Util.combine(commands,2);
		CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell("'"+classID+"' is an invalid class id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell("'"+C.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		modifyGenClass(mob,C);
		CMClass.DBEngine().DBDeleteClass(C.ID());
		CMClass.DBEngine().DBCreateClass(C.ID(),C.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,C.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is MODIFY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=Util.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")||therest.equalsIgnoreCase("SELF"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is MODIFY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		String stuff=Util.combine(commands,2).toUpperCase();
		Social soc2=Socials.FetchSocial(stuff,true);
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(Socials.modifySocialInterface(mob,soc2))
		{
			soc2.setName(soc2.name().toUpperCase());
			if(Socials.FetchSocial(soc2.name(),true)!=soc2)
			{
				mob.session().rawPrintln("That social already exists in another form (<T-NAME>, or SELF).  Try deleting the other one first!");
				return;
			}
			else
			{
				Resources.removeResource("SOCIALS LIST");
				Socials.save();
			}
			Log.sysOut("SysopSocials",mob.Name()+" modified social "+soc2.name()+".");
		}
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just increased!");
	}

	public void players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=Util.combine(commands,2);
		MOB M=CMMap.getPlayer(mobID);
		if(M==null)
			for(Enumeration p=CMMap.players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(mobID))
				{ M=mob2; break;}
			}
		MOB TM=CMClass.getMOB("StdMOB");
		if((M==null)&&(CMClass.DBEngine().DBUserSearch(TM,mobID)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			CMClass.DBEngine().DBReadMOB(M);
			CMClass.DBEngine().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
		if(M==null)
		{
			mob.tell("There is no such player as '"+mobID+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		modifyPlayer(mob,M);
		Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
	}
	public void mobs(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MOB [MOB NAME] [LEVEL, ABILITY, REJUV, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=Util.combine(commands,4);


		MOB modMOB=mob.location().fetchInhabitant(mobID);
		if(modMOB==null)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(!modMOB.isMonster())
		{
			mob.tell(modMOB.Name()+" is a player! Try MODIFY USER!");
			return;
		}

		if(command.equals("LEVEL"))
		{
			int newLevel=Util.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.baseEnvStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=Util.s_int(restStr);
			modMOB.baseEnvStats().setAbility(newAbility);
			modMOB.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=Util.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.baseEnvStats().setRejuv(newRejuv);
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
			else
			{
				modMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modMOB.recoverEnvStats();
				mob.tell(modMOB.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modMOB.isGeneric())
				genMiscSet(mob,modMOB);
			else
				modMOB.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, REJUV, or MISC.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
		}
		Log.sysOut("Mobs",mob.Name()+" modified mob "+modMOB.Name()+".");
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			rooms(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			classes(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Modify the status of which clan?  Use clanlist.");
			else
			{
				String name=Util.combine(commands,2);
				Clan C=Clans.findClan(name);
				if(C==null)
					mob.tell("Clan '"+name+"' is unknown.  Try clanlist.");
				else
				{
					switch(C.getStatus())
					{
					case Clan.CLANSTATUS_ACTIVE:
						C.setStatus(Clan.CLANSTATUS_PENDING);
						mob.tell("Clan '"+C.name()+"' has been changed from active to pending!");
						C.update();
						break;
					case Clan.CLANSTATUS_PENDING:
						C.setStatus(Clan.CLANSTATUS_ACTIVE);
						mob.tell("Clan '"+C.name()+"' has been changed from pending to active!");
						C.update();
						break;
					case Clan.CLANSTATUS_FADING:
						C.setStatus(Clan.CLANSTATUS_ACTIVE);
						mob.tell("Clan '"+C.name()+"' has been changed from fading to active!");
						C.update();
						break;
					default:
						mob.tell("Clan '"+C.name()+"' has not been changed!");
						break;
					}
				}
			}
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Start/Stop which quest?  Use list quests.");
			else
			{
				String name=Util.combine(commands,2);
                if(Util.isInteger(name))
                {
                    Quest Q=Quests.fetchQuest(Util.s_int(name));
                    if(Q!=null) name=Q.name();
                }
				Quest Q=Quests.fetchQuest(name);
				if(Q==null)
					mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				else
				if(!mob.isMonster())
				{
					if((Q.running())&&(mob.session().confirm("Stop quest '"+Q.name()+"' (y/N)?","N")))
					{
						Q.stopQuest();
						mob.tell("Quest '"+Q.name()+"' stopped.");
					}
					else
					if((!Q.running())&&(mob.session().confirm("Start quest '"+Q.name()+"' (Y/n)?","Y")))
					{
						Q.startQuest();
						mob.tell("Quest '"+Q.name()+"' started.");
					}
				}
			}
		}
        else
        if(commandType.equals("FACTION"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
            if(commands.size()<3)
                mob.tell("Modify which faction?  Use list factions.");
            else
            {
                String name=Util.combine(commands,2);
                Faction F=Factions.getFaction(name);
                if(F==null) F=Factions.getFactionByName(name);
                if(F==null)
                    mob.tell("Faction '"+name+"' is unknown.  Try list factions.");
                else
                if(!mob.isMonster())
                    modifyFaction(mob,F);
            }
        }
		else
		{
			String allWord=Util.combine(commands,1);
			int x=allWord.indexOf("@");
			MOB srchMob=mob;
			Room srchRoom=mob.location();
			if(x>0)
			{
				String rest=allWord.substring(x+1).trim();
				allWord=allWord.substring(0,x).trim();
				if(rest.equalsIgnoreCase("room"))
					srchMob=null;
				else
				if(rest.length()>0)
				{
					MOB M=srchRoom.fetchInhabitant(rest);
					if(M==null)
					{
						mob.tell("MOB '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
					srchMob=M;
					srchRoom=null;
				}
			}
			Environmental thang=null;
			if((srchMob!=null)&&(srchRoom!=null))
				thang=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,allWord,Item.WORN_REQ_ANY);
			else
			if(srchMob!=null)
				thang=srchMob.fetchInventory(allWord);
			else
			if(srchRoom!=null)
				thang=srchRoom.fetchFromRoomFavorItems(null,allWord,Item.WORN_REQ_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) 
                    return errorOut(mob);
				if(!thang.isGeneric())
				{
					int showFlag=-1;
					if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
						showFlag=-999;
					boolean ok=false;
					while(!ok)
					{
						int showNumber=0;
						genLevel(mob,thang,++showNumber,showFlag);
						genAbility(mob,thang,++showNumber,showFlag);
						genRejuv(mob,thang,++showNumber,showFlag);
						genUses(mob,(Item)thang,++showNumber,showFlag);
						genMiscText(mob,thang,++showNumber,showFlag);
						if(showFlag<-900){ ok=true; break;}
						if(showFlag>0){ showFlag=-1; continue;}
						showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
						if(showFlag<=0)
						{
							showFlag=-1;
							ok=true;
						}
					}
				}
				else
					genMiscSet(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
                Log.sysOut("CreateEdit",mob.Name()+" modified item "+thang.Name()+" ("+thang.ID()+") in "+CMMap.getExtendedRoomID(mob.location())+".");
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) 
                    return errorOut(mob);
				if((!thang.isGeneric())&&(((MOB)thang).isMonster()))
				{
					int showFlag=-1;
					if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
						showFlag=-999;
					boolean ok=false;
					while(!ok)
					{
						int showNumber=0;
						genLevel(mob,thang,++showNumber,showFlag);
						genAbility(mob,thang,++showNumber,showFlag);
						genRejuv(mob,thang,++showNumber,showFlag);
						genMiscText(mob,thang,++showNumber,showFlag);
						if(showFlag<-900){ ok=true; break;}
						if(showFlag>0){ showFlag=-1; continue;}
						showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
						if(showFlag<=0)
						{
							showFlag=-1;
							ok=true;
						}
					}
                    Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMMap.getExtendedRoomID(((MOB)thang).location())+".");
				}
				else
				if(!((MOB)thang).isMonster())
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
					players(mob,Util.parse("MODIFY USER \""+thang.Name()+"\""));
				}
				else
                {
					genMiscSet(mob,thang);
                    Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMMap.getExtendedRoomID(((MOB)thang).location())+".");
                }
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().rawExits()[Directions.getGoodDirectionCode(allWord)];

				if(thang!=null)
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
					genMiscText(mob,thang,1,1);
					thang.recoverEnvStats();
					try
					{
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
							for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
							{
								Exit exit=room.rawExits()[e2];
								if((exit!=null)&&(exit==thang))
								{
									CMClass.DBEngine().DBUpdateExits(room);
									break;
								}
							}
						}
				    }catch(NoSuchElementException e){}
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
					Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thang.ID()+".");
				}
				else
				{
					commands.insertElementAt("EXIT",1);
					execute(mob,commands);
				}
			}
			else
			if(Socials.FetchSocial(allWord,true)!=null)
			{
				commands.insertElementAt("SOCIAL",1);
				execute(mob,commands);
			}
			else
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, EXIT, QUEST, MOB, USER, SOCIAL, CLAN, or ROOM.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
