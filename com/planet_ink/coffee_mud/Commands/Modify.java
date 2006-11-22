package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2006 Bo Zimmerman

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

	private String[] access={getScr("Modify","cmd1"),getScr("Modify","cmd2")};
	public String[] getAccessWords(){return access;}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Modify","badmod"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
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
			if(rest.equalsIgnoreCase(getScr("Modify","cmdroom")))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell(getScr("Modify","nomob",rest));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
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
			restStr=CMParms.combine(commands,4);

		Item modItem=null;
		if((srchMob!=null)&&(srchRoom!=null))
			modItem=(Item)srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,itemID,Item.WORNREQ_ANY);
		else
		if(srchMob!=null)
			modItem=srchMob.fetchInventory(itemID);
		else
		if(srchRoom!=null)
			modItem=srchRoom.fetchAnyItem(itemID);
		if(modItem==null)
		{
			mob.tell(getScr("Modify","nohere",itemID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return;
		}
		mob.location().showOthers(mob,modItem,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));

		Item copyItem=(Item)modItem.copyOf();
		if(command.equals(getScr("Modify","cmdlevel")))
		{
			int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.baseEnvStats().setLevel(newLevel);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
			}
		}
		else
		if(command.equals(getScr("Modify","cmdable")))
		{
			int newAbility=CMath.s_int(restStr);
			modItem.baseEnvStats().setAbility(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
		}
		else
		if(command.equals(getScr("Modify","cmdheight")))
		{
			int newAbility=CMath.s_int(restStr);
			modItem.baseEnvStats().setHeight(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
		}
		else
		if(command.equals(getScr("Modify","cmdrejuv")))
		{
			int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.baseEnvStats().setRejuv(newRejuv);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
			}
			else
			{
				modItem.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modItem.recoverEnvStats();
				mob.tell(modItem.name()+getScr("Modify","neverrejuv"));
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
			}
		}
		else
		if(command.equals(getScr("Modify","cmduses")))
		{
			int newUses=CMath.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
			}
		}
		else
		if(command.equals(getScr("Modify","cmdmisc")))
		{
			if(modItem.isGeneric())
				genMiscSet(mob,modItem);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
		}
		else
		if((command.length()==0)&&(modItem.isGeneric()))
		{
			genMiscSet(mob,modItem);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+getScr("Modify","shakes"));
		}
		else
		{
			mob.tell(getScr("Modify","badmodmob"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
		}
		if(!copyItem.sameAs(modItem))
			Log.sysOut("Items",mob.Name()+getScr("Modify","modifieditem")+modItem.ID()+".");
	}

    protected void flunkCmd1(MOB mob)
	{
		mob.tell(getScr("Modify","badmodroom"));
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
	}

    protected void flunkCmd2(MOB mob)
	{
		mob.tell(getScr("Modify","badmodarea"));
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
	}
	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(getScr("Modify","nogridlocalechild"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshandsroom"));
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			Room oldRoom=(Room)mob.location().copyOf();
			while(!ok)
			{
				int showNumber=0;
                Room R=mob.location();
				genRoomType(mob,R,++showNumber,showFlag);
				genDisplayText(mob,R,++showNumber,showFlag);
				genDescription(mob,R,++showNumber,showFlag);
				if(mob.location() instanceof GridZones)
				{
					genGridLocaleX(mob,(GridZones)R,++showNumber,showFlag);
					genGridLocaleY(mob,(GridZones)R,++showNumber,showFlag);
					//((GridLocale)mob.location()).buildGrid();
				}
				genBehaviors(mob,R,++showNumber,showFlag);
				genAffects(mob,R,++showNumber,showFlag);
                for(int x=R.getSaveStatIndex();x<R.getStatCodes().length;x++)
                    R.setStat(R.getStatCodes()[x],CMLib.english().promptText(mob,R.getStat(R.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(R.getStatCodes()[x])));
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=CMath.s_int(mob.session().prompt(getScr("Modify","which"),""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
			if((!oldRoom.sameAs(mob.location()))&&(!mob.location().amDestroyed()))
			{
				CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","diff"));
				Log.sysOut(getScr("Modify","modifiedroom"),mob.Name()+getScr("Modify","modifiedroomreally")+mob.location().roomID()+".");
			}
			return;
		}
		if(commands.size()<3) { flunkCmd1(mob); return;}

		String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=CMParms.combine(commands,3);

		if(command.equalsIgnoreCase(getScr("Modify","cmdarea")))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Area A=CMLib.map().getArea(restStr);
			boolean reid=false;
			if(A==null)
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm(getScr("Modify","newarea",restStr),"N"))
					{
						String areaType="";
						int tries=0;
						while((areaType.length()==0)&&((++tries)<10))
						{
							areaType=mob.session().prompt(getScr("Modify","areatype"),"StdArea");
							if(CMClass.getAreaType(areaType)==null)
							{
								mob.session().println(getScr("Modify","badareatype"));
								mob.session().println(CMLib.lister().reallyList(CMClass.areaTypes(),-1,null).toString());
								areaType="";
							}
						}
						if(areaType.length()==0) areaType="StdArea";
						A=CMLib.database().DBCreateArea(restStr,areaType);
						mob.location().setArea(A);
                        CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
						reid=true;
					}
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","areatwitch"));
				}
				else
				{
					mob.tell(getScr("Modify","sorry"));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
				}
			}
			else
			{
				mob.location().setArea(A);
				if(A.getRandomProperRoom()!=null)
					reid=true;
				else
					CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","areatwitch2"));
			}
			
			if(reid)
			{
				Room R=mob.location();
				String oldID=R.roomID();
	    		synchronized(("SYNC"+R.roomID()).intern())
	    		{
	    			R=CMLib.map().getRoom(R);
					Room reference=CMLib.map().findConnectingRoom(R);
					String checkID=null;
					if(reference!=null)
						checkID=A.getNewRoomID(reference,CMLib.map().getRoomDir(reference,R));
					else
						checkID=A.getNewRoomID(R,-1);
					mob.location().setRoomID(checkID);
					CMLib.database().DBReCreate(R,oldID);
	    		}
			}
		}
		else
		if(command.equalsIgnoreCase(getScr("Modify","cmdname")))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDisplayText(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","diff"));
		}
		else
		if(command.equalsIgnoreCase(getScr("Modify","cmdclass")))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Room newRoom=CMClass.getLocale(restStr);
			if(newRoom==null)
			{
				mob.tell("'"+restStr+getScr("Modify","nolocale"));
				return;
			}
			changeRoomType(mob.location(),newRoom);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","diff"));
		}
		else
		if((command.equalsIgnoreCase(getScr("Modify","cmdxgrid")))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setXGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","diff"));
		}
		else
		if((command.equalsIgnoreCase(getScr("Modify","cmdygrid")))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setYGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","diff"));
		}
		else
		if(command.equalsIgnoreCase(getScr("Modify","cmddesc")))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDescription(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","realityup"));
		}
		else
		if(command.equalsIgnoreCase(getScr("Modify","cmdaffects")))
		{
			genAffects(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","realityup"));
		}
		else
		if(command.equalsIgnoreCase(getScr("Modify","cmdbehavs")))
		{
			genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","realityup"));
		}
		else
		{
			flunkCmd1(mob);
			return;
		}
		mob.location().recoverRoomStats();
		Log.sysOut(getScr("Modify","modifiedroom"),mob.Name()+getScr("Modify","modifiedroomreally")+mob.location().roomID()+".");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.Name();
		Vector allMyDamnRooms=new Vector();
		for(Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
			allMyDamnRooms.addElement(e.nextElement());

		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","waveswild"));
		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
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
				if(myArea instanceof GridZones)
				{
					genGridLocaleX(mob,(GridZones)myArea,++showNumber,showFlag);
					genGridLocaleY(mob,(GridZones)myArea,++showNumber,showFlag);
				}
				genBehaviors(mob,myArea,++showNumber,showFlag);
				genAffects(mob,myArea,++showNumber,showFlag);
				genImage(mob,myArea,++showNumber,showFlag);
                for(int x=myArea.getSaveStatIndex();x<myArea.getStatCodes().length;x++)
                    myArea.setStat(myArea.getStatCodes()[x],CMLib.english().promptText(mob,myArea.getStat(myArea.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(myArea.getStatCodes()[x])));
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=CMath.s_int(mob.session().prompt(getScr("Modify","which"),""));
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
				restStr=CMParms.combine(commands,3);

			if(command.equalsIgnoreCase(getScr("Modify","cmdname")))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setName(restStr);
			}
			else
			if(command.equalsIgnoreCase(getScr("Modify","cmddesconly")))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase(getScr("Modify","cmdfile")))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setArchivePath(restStr);
			}
			else
			if((command.equalsIgnoreCase(getScr("Modify","cmdxgrid")))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				((GridZones)myArea).setXGridSize(CMath.s_int(restStr));
			}
			else
			if((command.equalsIgnoreCase(getScr("Modify","cmdygrid")))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				((GridZones)myArea).setYGridSize(CMath.s_int(restStr));
			}
			if(command.equalsIgnoreCase(getScr("Modify","cmdclimate")))
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
						mob.tell(getScr("Modify","badclimate",""+restStr.charAt(i)));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
						return;
					}
				myArea.setClimateType(newClimate);
			}
			else
			if(command.equalsIgnoreCase(getScr("Modify","cmdaddsub")))
			{
				if((commands.size()<4)||(!CMLib.database().DBUserSearch(null,restStr)))
				{
					mob.tell(getScr("Modify","badusername"));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
				}
				myArea.addSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase(getScr("Modify","cmddelsub")))
			{
				if((commands.size()<4)||(!myArea.amISubOp(restStr)))
				{
					mob.tell(getScr("Modify","badstaffname")+myArea.getSubOpList()+".\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
				}
				myArea.delSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase(getScr("Modify","cmdaffects")))
			{
				genAffects(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			if(command.equalsIgnoreCase(getScr("Modify","cmdbehavs")))
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
			if(mob.session().confirm(getScr("Modify","isareanamenecc"),"N"))
			{
				for(Enumeration r=myArea.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
		    		synchronized(("SYNC"+R.roomID()).intern())
		    		{
		    			R=CMLib.map().getRoom(R);
						if((R.roomID().startsWith(oldName+"#"))
						&&(CMLib.map().getRoom(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1))==null))
						{
			    			R=CMLib.map().getRoom(R);
							String oldID=R.roomID();
							R.setRoomID(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							CMLib.database().DBUpdateRoom(R);
		    		}
				}
			}
			else
				myArea.setName(oldName);
		}
		else
			myArea.setName(oldName);
		myArea.recoverEnvStats();
		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","diff"));
		if(myArea.name().equals(oldName))
			CMLib.database().DBUpdateArea(myArea.Name(),myArea);
		else
		{
			CMLib.database().DBUpdateArea(oldName,myArea);
			CMLib.map().renameRooms(myArea,oldName,allMyDamnRooms);
		}
		Log.sysOut(getScr("Modify","modifiedroom"),mob.Name()+getScr("Modify","modifiedarea")+myArea.Name()+".");
	}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(getScr("Modify","nogridlocalechild"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}
		if(commands.size()<3)
		{
			mob.tell(getScr("Modify","badexitmod"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell(getScr("Modify","nodir")+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return;
		}
		
		Exit thisExit=mob.location().rawExits()[direction];
		if(thisExit==null)
		{
			mob.tell(getScr("Modify","noexit")+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshandsto")+Directions.getInDirectionName(direction)+".");

		if(thisExit.isGeneric())
		{
			modifyGenExit(mob,thisExit);
			return;
		}
		
		if(commands.size()<4)
		{
			mob.tell(getScr("Modify","badexitmod"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return;
		}

		//String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=CMParms.combine(commands,3);

		if(thisExit.isGeneric())
			modifyGenExit(mob,thisExit);
		else
		if(restStr.length()>0)
			thisExit.setMiscText(restStr);
		else
		{
			mob.tell(getScr("Modify","badexitmod"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return;
		}
		
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room room=(Room)r.nextElement();
				for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
				{
					Exit exit=room.rawExits()[e2];
					if((exit!=null)&&(exit==thisExit))
					{
						CMLib.database().DBUpdateExits(room);
						room.getArea().fillInAreaRoom(room);
						break;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,thisExit.name()+getScr("Modify","shakes"));
		Log.sysOut("Exits",mob.location().roomID()+getScr("Modify","exitschanged")+mob.Name()+".");
	}

	public boolean races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Modify","badmodrace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}

		String raceID=CMParms.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+getScr("Modify","noraceid"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+getScr("Modify","noracegen"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesaroundall",R.name()));
		modifyGenRace(mob,R);
		CMLib.database().DBDeleteRace(R.ID());
		CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,R.name()+getScr("Modify","everywhereshakes"));
		return true;
	}

	public boolean classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Modify","badmodclass"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}

		String classID=CMParms.combine(commands,2);
		CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell("'"+classID+getScr("Modify","badclassid"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell("'"+C.ID()+getScr("Modify","noracegen"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesaroundall",C.name()));
		modifyGenClass(mob,C);
		CMLib.database().DBDeleteClass(C.ID());
		CMLib.database().DBCreateClass(C.ID(),C.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,C.name()+getScr("Modify","everywhereshakes"));
		return true;
	}

	public boolean abilities(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Modify","badmodable"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
	
		String classID=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell("'"+classID+getScr("Modify","invalidable"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell("'"+A.ID()+getScr("Modify","noracegen"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesaroundall",A.name()));
		modifyGenAbility(mob,A);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,A.name()+getScr("Modify","everywhereshakes"));
		return true;
	}
	
    public void components(MOB mob, Vector commands)
    throws IOException
    {
        if(commands.size()<3)
        {
            mob.tell(getScr("Modify","badmodcomp"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
            return;
        }
        String skillID=CMParms.combine(commands,2);
        Ability A=CMClass.getAbility(skillID);
        if(A==null)
        {
            mob.tell("'"+skillID+getScr("Modify","badskillid"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
            return;
        }
        skillID=A.ID();
        if(CMLib.ableMapper().getAbilityComponentMap().get(A.ID().toUpperCase())==null)
        {
            mob.tell(getScr("Modify","nocompexists",A.ID()));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
            return;
        }
        super.modifyComponents(mob,skillID);
        String parms=CMLib.ableMapper().getAbilityComponentCodedString(skillID);
        String error=CMLib.ableMapper().addAbilityComponent(parms,CMLib.ableMapper().getAbilityComponentMap());
        if(error!=null)
        {
            mob.tell(error);
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
            return;
        }
        CMFile F=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true);
        StringBuffer text=F.textUnformatted();
        boolean lastWasCR=true;
        int delFromHere=-1;
        String upID=skillID.toUpperCase();
        for(int t=0;t<text.length();t++)
        {
            if(text.charAt(t)=='\n')
                lastWasCR=true;
            else
            if(text.charAt(t)=='\r')
                lastWasCR=true;
            else
            if(Character.isWhitespace(text.charAt(t)))
                continue;
            else
            if((lastWasCR)&&(delFromHere>=0))
            {
                text.delete(delFromHere,t);
                text.insert(delFromHere,parms+'\n');
                delFromHere=-1;
                break;
            }
            else
            if((lastWasCR)&&(Character.toUpperCase(text.charAt(t))==upID.charAt(0)))
            {
                if((text.substring(t).toUpperCase().startsWith(upID))
                &&(text.substring(t+upID.length()).trim().startsWith("=")))
                    delFromHere=t;
                lastWasCR=false;
            }
            else
                lastWasCR=false;
        }
        if(delFromHere>0)
        {
            text.delete(delFromHere,text.length());
            text.append(parms+'\n');
        }
        F.saveText(text.toString(),false);
        mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","complicup"));
    }
    
    
    
	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().rawPrintln(getScr("Modify","badmodsoc"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}
        String name=((String)commands.elementAt(2)).toUpperCase();
        String stuff="";
        if(commands.size()>3)
            stuff=CMParms.combine(commands,3).toUpperCase().trim();
        if(stuff.startsWith("<")||stuff.startsWith(">")||(stuff.startsWith("T-")))
            stuff="TNAME";
        if(stuff.equals("TNAME")) 
            stuff="<T-NAME>";
        String oldStuff=stuff;
        if(stuff.equals("NONE")) 
            stuff="";
        Social S=CMLib.socials().FetchSocial((name+" "+stuff).trim(),false);
        if(S==null)
        {
            mob.tell(getScr("Modify","nosocexists",stuff));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
            return;
        }
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesaroundidea",S.name()));
		CMLib.socials().modifySocialInterface(mob,(name+" "+oldStuff).trim());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Modify","happyup"));
	}

	public void players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Modify","badmoduser"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.map().getPlayer(mobID);
		if(M==null)
			for(Enumeration p=CMLib.map().players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(mobID))
				{ M=mob2; break;}
			}
		MOB TM=CMClass.getMOB("StdMOB");
		if((M==null)&&(CMLib.database().DBUserSearch(TM,mobID)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			CMLib.database().DBReadPlayer(M);
			CMLib.database().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
        TM.destroy();
		if(M==null)
		{
			mob.tell(getScr("Modify","noplayer")+mobID+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}
		mob.location().showOthers(mob,M,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));
		MOB copyMOB=(MOB)M.copyOf();
		modifyPlayer(mob,M);
		if(!copyMOB.sameAs(M))
			Log.sysOut("Mobs",mob.Name()+getScr("Modify","modifiedplayer")+M.Name()+".");
	}
	
	public void mobs(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell(getScr("Modify","badmodmobreally"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}

		String mobID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);


		MOB modMOB=mob.location().fetchInhabitant(mobID);
		if(modMOB==null)
		{
			mob.tell(getScr("Modify","nohere",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
			return;
		}

		if(!modMOB.isMonster())
		{
			mob.tell(modMOB.Name()+getScr("Modify","isplayer"));
			return;
		}
		MOB copyMOB=(MOB)modMOB.copyOf();
		mob.location().showOthers(mob,modMOB,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));
		if(command.equals(getScr("Modify","cmdlevel")))
		{
			int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.baseEnvStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+getScr("Modify","shakespower"));
			}
		}
		else
		if(command.equals(getScr("Modify","cmdable")))
		{
			int newAbility=CMath.s_int(restStr);
			modMOB.baseEnvStats().setAbility(newAbility);
			modMOB.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+getScr("Modify","shakespower"));
		}
		else
		if(command.equals(getScr("Modify","cmdrejuv")))
		{
			int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.baseEnvStats().setRejuv(newRejuv);
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+getScr("Modify","shakespower"));
			}
			else
			{
				modMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modMOB.recoverEnvStats();
				mob.tell(modMOB.name()+getScr("Modify","neverrejuv"));
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+getScr("Modify","shakespower"));
			}
		}
		else
		if(command.equals(getScr("Modify","cmdmisc")))
		{
			if(modMOB.isGeneric())
				genMiscSet(mob,modMOB);
			else
				modMOB.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+getScr("Modify","shakespower"));
		}
		else
		{
			mob.tell(getScr("Modify","badmodmob2"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubspowspell"));
		}
		if(!modMOB.sameAs(copyMOB))
			Log.sysOut("Mobs",mob.Name()+getScr("Modify","modifiedmob")+modMOB.Name()+".");
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell(getScr("Modify","nodothathere"));
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals(getScr("Modify","cmditem")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			items(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdroom2")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdrace")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			races(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdclass")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			classes(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdable")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDABILITIES")) return errorOut(mob);
			abilities(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdarea")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdexit")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdcomponent")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"COMPONENTS")) return errorOut(mob);
            components(mob,commands);
			return false;
		}
        else
        if(commandType.equals(getScr("Modify","cmdexpertise")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"EXPERTISES")) return errorOut(mob);
            mob.tell(getScr("Modify","nomodcomp"));
            return false;
        }
        else
        if(commandType.equals(getScr("Modify","cmdtitle")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"TITLES")) return errorOut(mob);
            mob.tell(getScr("Modify","nomodcomp"));
            return false;
        }
		else
		if(commandType.equals(getScr("Modify","cmdsoc")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			socials(mob,commands);
		}
		else
		if(commandType.equals(getScr("Modify","cmdmob")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mobs(mob,commands);
		}
		else
        if(commandType.startsWith(getScr("Modify","cmdjscript")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"JSCRIPTS")) 
                return errorOut(mob);
            if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)!=1)
            {
                mob.tell(getScr("Modify","reqapproval"));
                return true;
            }
            Long L=null;
            Object O=null;
            Hashtable j=CMSecurity.getApprovedJScriptTable();
            boolean somethingFound=false;
            for(Enumeration e=j.keys();e.hasMoreElements();)
            {
                L=(Long)e.nextElement();
                O=j.get(L);
                if(O instanceof StringBuffer)
                {
                    somethingFound=true;
                    mob.tell(getScr("Modify","unapproved")+((StringBuffer)O).toString()+"\n\r");
                    if((!mob.isMonster())
                    &&(mob.session().confirm(getScr("Modify","approve"),"Y")))
                        CMSecurity.approveJScript(mob.Name(),L.longValue());
                    else
                        j.remove(L);
                }
            }
            if(!somethingFound)
                mob.tell(getScr("Modify","noapproves"));
        }
        else
		if(commandType.equals(getScr("Modify","cmduser")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			players(mob,commands);
		}
		else
        if(commandType.equals(getScr("Modify","cmdpoll")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"POLLS")) return errorOut(mob);
            String name=CMParms.combine(commands,2);
            Poll P=null;
            if(CMath.isInteger(name))
                P=CMLib.polls().getPoll(CMath.s_int(name)-1);
            else
            if(name.length()>0)
                P=CMLib.polls().getPoll(name);
            if(P==null)
            {
                mob.tell(getScr("Modify","nopoll",name));
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
                return false;
            }
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Modify","wavesidea")+P.getSubject()+".^?");
            P.modifyVote(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Modify","uncertaintyup"));
            Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifiedpoll")+P.getName()+".");
        }
        else
		if(commandType.equals(getScr("Modify","cmdquest")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
			if(commands.size()<3)
				mob.tell(getScr("Modify","stopwhich"));
			else
			{
				String name=CMParms.combine(commands,2);
                Quest Q=null;
                if(CMath.isInteger(name))
                {
                    Q=CMLib.quests().fetchQuest(CMath.s_int(name)-1);
                    if(Q!=null) name=Q.name();
                }
                if(Q==null) Q=CMLib.quests().fetchQuest(name);
				if(Q==null)
					mob.tell(getScr("Modify","unknownquest",name));
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesaround")+Q.name()+".");
					if((Q.running())&&(mob.session().confirm(getScr("Modify","stopquest",Q.name()),"N")))
					{
						Q.stopQuest();
						mob.tell(getScr("Modify","queststopped",Q.name()));
					}
					else
					if((!Q.running())&&(mob.session().confirm(getScr("Modify","startquest",Q.name()),"Y")))
					{
						Q.startQuest();
                        if(!Q.running())
    						mob.tell(getScr("Modify","questnotstarted",Q.name()));
                        else
                            mob.tell(getScr("Modify","queststarted",Q.name()));
					}
				}
			}
		}
        else
        if(commandType.equals(getScr("Modify","cmdfaction")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            if(commands.size()<3)
                mob.tell(getScr("Modify","whichfaction"));
            else
            {
                String name=CMParms.combine(commands,2);
                Faction F=CMLib.factions().getFaction(name);
                if(F==null) F=CMLib.factions().getFactionByName(name);
                if(F==null)
                    mob.tell(getScr("Modify","unknownfaction",name));
                else
                if(!mob.isMonster())
                {
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesidea2")+F.name()+".");
                    modifyFaction(mob,F);
                    Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifiedfaction")+F.name()+" ("+F.factionID()+").");
                }
            }
        }
        else
        if(commandType.equals(getScr("Modify","cmdclan")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			if(commands.size()<3)
				mob.tell(getScr("Modify","whichclan"));
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell(getScr("Modify","unknownclan",name));
				else
                if(!mob.isMonster())
                {
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","wavesaround")+C.name()+".");
                    modifyClan(mob,C);
                    Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifiedclan")+C.name()+".");
                }
            }
        }
		else
		{
			String allWord=CMParms.combine(commands,1);
			int x=allWord.indexOf("@");
			MOB srchMob=mob;
			Room srchRoom=mob.location();
			if(x>0)
			{
				String rest=allWord.substring(x+1).trim();
				allWord=allWord.substring(0,x).trim();
				if(rest.equalsIgnoreCase(getScr("Modify","cmdroom")))
					srchMob=null;
				else
				if(rest.length()>0)
				{
					MOB M=srchRoom.fetchInhabitant(rest);
					if(M==null)
					{
						mob.tell(getScr("Modify","nomob",rest));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Modify","flubsspell"));
						return false;
					}
					srchMob=M;
					srchRoom=null;
				}
			}
			Environmental thang=null;
			if((srchMob!=null)&&(srchRoom!=null))
				thang=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,allWord,Item.WORNREQ_ANY);
			else
			if(srchMob!=null)
				thang=srchMob.fetchInventory(allWord);
			else
			if(srchRoom!=null)
				thang=srchRoom.fetchFromRoomFavorItems(null,allWord,Item.WORNREQ_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) 
                    return errorOut(mob);
				Item copyItem=(Item)thang.copyOf();
				mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));
				if(!thang.isGeneric())
				{
					int showFlag=-1;
					if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
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
						showFlag=CMath.s_int(mob.session().prompt(getScr("Modify","which"),""));
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
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+getScr("Modify","shakes"));
				if(!copyItem.sameAs(thang))
	                Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifieditemin",thang.Name(),thang.ID())+CMLib.map().getExtendedRoomID(mob.location())+".");
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) 
                    return errorOut(mob);
				MOB copyMOB=(MOB)thang.copyOf();
				if((!thang.isGeneric())&&(((MOB)thang).isMonster()))
				{
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));
					int showFlag=-1;
					if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
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
						showFlag=CMath.s_int(mob.session().prompt(getScr("Modify","which"),""));
						if(showFlag<=0)
						{
							showFlag=-1;
							ok=true;
						}
					}
					if(!copyMOB.sameAs(thang))
	                    Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifiedmobin",thang.Name(),thang.ID())+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
				}
				else
				if(!((MOB)thang).isMonster())
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
					players(mob,CMParms.parse(getScr("Modify","cmdmoduser")+thang.Name()+"\""));
				}
				else
                {
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));
					genMiscSet(mob,thang);
					if(!copyMOB.sameAs(thang))
	                    Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifiedmobin",thang.Name(),thang.ID())+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
                }
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+getScr("Modify","shakes"));
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().rawExits()[Directions.getGoodDirectionCode(allWord)];

				if(thang!=null)
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,getScr("Modify","waveshands"));
					Exit copyExit=(Exit)thang.copyOf();
					genMiscText(mob,thang,1,1);
					thang.recoverEnvStats();
					try
					{
						for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
				    		synchronized(("SYNC"+room.roomID()).intern())
				    		{
				    			room=CMLib.map().getRoom(room);
								for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
								{
									Exit exit=room.rawExits()[e2];
									if((exit!=null)&&(exit==thang))
									{
										CMLib.database().DBUpdateExits(room);
										break;
									}
								}
				    		}
						}
				    }catch(NoSuchElementException e){}
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+getScr("Modify","shakes"));
					if(!copyExit.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+getScr("Modify","modifiedexit")+thang.ID()+".");
				}
				else
				{
					commands.insertElementAt(getScr("Modify","cmdexit"),1);
					execute(mob,commands);
				}
			}
			else
			if(CMLib.socials().FetchSocial(allWord,true)!=null)
			{
				commands.insertElementAt(getScr("Modify","cmdsoc"),1);
				execute(mob,commands);
			}
			else
				mob.tell(getScr("Modify","modifyinst",commandType));
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	
}
