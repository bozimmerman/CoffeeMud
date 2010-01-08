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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Catalog extends StdCommand
{
	public Catalog(){}

	private String[] access={"CATALOG"};
	public String[] getAccessWords(){return access;}
	
	public boolean catalog(Room R, MOB mob, Environmental E)
	    throws java.io.IOException
	{
	    Environmental origE=E;
	    Environmental cataE=CMLib.catalog().getCatalogObj(E);
	    if((!(E instanceof DBIdentifiable))
	    ||(!((DBIdentifiable)E).canSaveDatabaseID()))
	    {
            mob.tell("The object '"+E.Name()+"' can not be cataloged.");
            return false;
	    }
        String msg="<S-NAME> catalog(s) <T-NAMESELF>.";
        if(CMLib.flags().isCataloged(E))
        {
            mob.tell("The object '"+E.Name()+"' is already cataloged.");
            return false;
        }
        if(cataE!=null)
        {
            CMLib.catalog().changeCatalogUsage(E,true);
            StringBuffer diffs=CMLib.catalog().checkCatalogIntegrity(E);
            if((diffs==null)||(diffs.length()==0))
            {
                mob.tell("The object '"+cataE.Name()+"' already exists in the catalog, exactly as it is.");
                return true;
            }
            if((mob.session()==null)
            ||(!mob.session().confirm("Cataloging that object will change the existing cataloged '"+E.Name()+"' by altering the following properties: "+diffs.toString()+".  Please confirm (y/N)?","Y")))
            {
                CMLib.catalog().changeCatalogUsage(origE,false);
                return false;
            }
            msg="<S-NAME> modif(ys) the cataloged version of <T-NAMESELF>.";
            CMLib.catalog().updateCatalog(E);
        }
        else
        {
            CMLib.catalog().addCatalog(E);
        }
        R.show(mob,E,CMMsg.MSG_OK_VISUAL,msg);
        return true;
	}
	
	
	public Environmental findCatalog(int whatKind, String ID, boolean exactOnly)
	{
		Object[] data=new Object[]{null,null};
		if((data[0]==null)&&((whatKind==0)||(whatKind==1)))
		{ data[0]=CMLib.catalog().getCatalogMob(ID); if(data[0]!=null) data[1]=Integer.valueOf(1);}
		if((data[0]==null)&&((whatKind==0)||(whatKind==2)))
		{ data[0]=CMLib.catalog().getCatalogItem(ID); if(data[0]!=null) data[1]=Integer.valueOf(2);}
		if(exactOnly) return (Environmental)data[0];
		if((data[0]==null)&&((whatKind==0)||(whatKind==1)))
		{
			String[] names=(String[])CMLib.catalog().getCatalogMobNames().clone();
			for(int x=0;x<names.length;x++)
				if(CMLib.english().containsString(names[x], ID))
				{	data[0]=CMLib.catalog().getCatalogMob(names[x]); data[1]=Integer.valueOf(1); break;}
			if(data[0] == null)
			{
				for(int s=0;s<names.length;s++)
					names[s] = CMStrings.removeColors(names[s]);
				for(int x=0;x<names.length;x++)
					if(ID.equalsIgnoreCase(names[x]))
					{	data[0]=CMLib.catalog().getCatalogMob(names[x]); data[1]=Integer.valueOf(1); break;}
				if(data[0] == null)
				for(int x=0;x<names.length;x++)
					if(CMLib.english().containsString(names[x], ID))
					{	data[0]=CMLib.catalog().getCatalogMob(names[x]); data[1]=Integer.valueOf(1); break;}
			}
		}
		if((data[0]==null)&&((whatKind==0)||(whatKind==2)))
		{
			String[] names=(String[])CMLib.catalog().getCatalogItemNames().clone();
			for(int x=0;x<names.length;x++)
				if(CMLib.english().containsString(names[x], ID))
				{	data[0]=CMLib.catalog().getCatalogItem(names[x]); data[1]=Integer.valueOf(2); break;}
			if(data[0] == null)
			{
				for(int s=0;s<names.length;s++)
					names[s] = CMStrings.removeColors(names[s]);
				for(int x=0;x<names.length;x++)
					if(ID.equalsIgnoreCase(names[x]))
					{	data[0]=CMLib.catalog().getCatalogItem(names[x]); data[1]=Integer.valueOf(2); break;}
				if(data[0] == null)
				for(int x=0;x<names.length;x++)
					if(CMLib.english().containsString(names[x], ID))
					{	data[0]=CMLib.catalog().getCatalogItem(names[x]); data[1]=Integer.valueOf(2); break;}
			}
		}
		return (Environmental)data[0];
	}

	public Enumeration getRoomSet(MOB mob, String which)
	{
		Enumeration rooms=null;
		if(which.equalsIgnoreCase("ROOM"))
			rooms=CMParms.makeVector(CMLib.map().getExtendedRoomID(mob.location())).elements();
		else
		if(which.equalsIgnoreCase("AREA"))
			rooms=mob.location().getArea().getProperRoomnumbers().getRoomIDs();
		else
		if(which.equalsIgnoreCase("WORLD"))
			rooms=CMLib.map().roomIDs();
		return rooms;
	}

	public int getObjectType(Vector commands)
	{
		int whatKind=0;
		if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
		{ commands.removeElementAt(0); whatKind=1; }
		if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
		{ commands.removeElementAt(0); whatKind=2;}
		return whatKind;
	}

	public boolean checkUserRoomSetEntry(Vector commands)
	{
		if(commands.size()==0) return false;
		if((((String)commands.firstElement()).equalsIgnoreCase("ROOM"))
		||(((String)commands.firstElement()).equalsIgnoreCase("AREA"))
		||(((String)commands.firstElement()).equalsIgnoreCase("WORLD")))
			return true;
		return false;
		
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room R=mob.location();
		if(R==null) return false;
		if((commands!=null)&&(commands.size()>1))
		{
			commands.removeElementAt(0);
			final String[] types={"object","mobs","items"};
			
			if(checkUserRoomSetEntry(commands))
			{
				String which=((String)commands.firstElement()).toLowerCase();
				Enumeration rooms=getRoomSet(mob,which);
				commands.removeElementAt(0);
				int whatKind=getObjectType(commands);
				String type=types[whatKind];
				
				if((mob.session()!=null)
				&&(mob.session().confirm("You are about to auto-catalog (room-reset and save) all "+which+" "+type+".\n\r"
			        +"This command, if used improperly, may alter "+type+" in this "+which+".\n\rAre you absolutely sure (y/N)?","N"))
                &&((which.equalsIgnoreCase("ROOM"))||mob.session().confirm("I'm serious now.  You can't abort this, and it WILL modify stuff.\n\r"
                    +"Have you tested this command on small areas and know what you're doing?.\n\rAre you absolutely POSITIVELY sure (y/N)?","N")))
				{
					Environmental E=null;
					String roomID=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						R=CMLib.coffeeMaker().makeNewRoomContent(CMLib.map().getRoom(roomID));
						Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
						boolean dirty=false;
						for(CatalogLibrary.RoomContent content : contents)
						{
							E=content.E();
							if(E instanceof Coins) continue;
                            if((E instanceof MOB)&&(whatKind!=2))
                            {
                                if(catalog(R,mob,E)){ 
                                	content.flagDirty(); 
                                	dirty=true;
                                }
                            }
                            else
                            if((E instanceof Item)&&(whatKind!=1))
                            {
                                if(catalog(R,mob,E)){ 
                                	content.flagDirty(); 
                                	dirty=true;
                                }
                            }
						}
						if(dirty)
						{
							CMLib.catalog().updateRoomContent(roomID, contents);
							R.destroy();
							CMLib.map().resetRoom(CMLib.map().getRoom(roomID), true);
						}
					}
				}
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("LIST"))
			{
				commands.removeElementAt(0);
				int whatKind=getObjectType(commands);
				String ID=CMParms.combine(commands,0);
				StringBuffer list=new StringBuffer("");
				int col=0;
				MOB M=null;
				Item I=null;
				CatalogLibrary.CataData data;
				if((whatKind==0)||(whatKind==1)) 
				{
					list.append("^HMobs\n\r^N");
					list.append(CMStrings.padRight("Name",34)+" ");
					list.append(CMStrings.padRight("#",3));
					list.append(CMStrings.padRight("Name",34)+" ");
					list.append(CMStrings.padRight("#",3));
					list.append("\n\r"+CMStrings.repeat("-",78)+"\n\r");
					String[] names=CMLib.catalog().getCatalogMobNames();
					for(int i=0;i<names.length;i++)
					{
						M=CMLib.catalog().getCatalogMob(names[i]);
						data=CMLib.catalog().getCatalogMobData(names[i]);
						if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(M.Name(),ID)))
						{
							list.append(CMStrings.padRight(M.Name(),34)).append(" ");
							list.append(CMStrings.padRight(Integer.toString(data.numReferences()),3));
							if(col==0)
							{
								col++;
							}
							else
							{
								col++;
								list.append("\n\r");
							}
						}
					}
					list.append("\n\r\n\r");
				}
				if((whatKind==0)||(whatKind==2)) 
				{
					list.append("^HItems\n\r^N");
					list.append(CMStrings.padRight("Name",34)+" ");
                    list.append(CMStrings.padRight("#",3)+" ");
                    list.append(CMStrings.padRight("Rate",6)+" ");
					list.append(CMStrings.padRight("Mask",31)+" ");
                    list.append("\n\r"+CMStrings.repeat("-",78)+"\n\r");
                    String[] names=CMLib.catalog().getCatalogItemNames();
					for(int i=0;i<names.length;i++)
					{
						I=CMLib.catalog().getCatalogItem(names[i]);
                        data=CMLib.catalog().getCatalogItemData(names[i]);
						if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(I.Name(),ID)))
						{
							list.append(CMStrings.padRight(I.Name(),34)+" ");
							list.append(CMStrings.padRight(Integer.toBinaryString(data.numReferences()),3)+" ");
							if(data.getRate()<=0.0)
							{
							    list.append("N/A   ");
			                    list.append(CMStrings.padRight(" ",31));
							}
							else
							{
							    list.append(CMStrings.padRight(CMath.toPct(data.getRate()),6)+" ");
                                list.append(data.getWhenLive()?"L ":"D ");
                                list.append(CMStrings.padRight(data.getMaskStr(),29));
							}
							list.append("\n\r");
						}
					}
					list.append("\n\r");
				}
				if(mob.session()!=null)
					mob.session().wraplessPrintln(list.toString());
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("DELETE"))
			{
				commands.removeElementAt(0);
				int whatKind=getObjectType(commands);
				String ID=CMParms.combine(commands,0);
				Environmental[] del=null;
				if(ID.equalsIgnoreCase("everydamnmob"))
					del=CMLib.catalog().getCatalogMobs();
				else
				if(ID.equalsIgnoreCase("everydamnitem"))
					del=CMLib.catalog().getCatalogItems();
				else
				if(ID.equalsIgnoreCase("everydamnthing"))
				{
					Vector V=CMParms.makeVector(CMLib.catalog().getCatalogItems());
					V.addAll(CMParms.makeVector(CMLib.catalog().getCatalogMobs()));
					del=new Environmental[V.size()];
					for(int i=0;i<V.size();i++)
						del[i]=(Environmental)V.elementAt(i);
				}
				else
				{
					Environmental E=findCatalog(whatKind,ID,false);
					if(E==null)
					{
						mob.tell("'"+ID+"' not found in catalog! Try CATALOG LIST");
						return false;
					}
					del=new Environmental[]{E};
				}
				for(int d=0;d<del.length;d++)
				{
					Environmental E=del[d];
					CatalogLibrary.CataData data=CMLib.catalog().getCatalogData(E);
					if(E instanceof MOB)
					{
						String prefix="";
						if((data!=null)&&(data.numReferences()>0))
							prefix="Catalog MOB '"+((MOB)E).Name()+"' is currently listed as being in use '"+data.numReferences()+" times.  ";
						if((mob.session()!=null)
						&&((del.length>10)||(mob.session().confirm(prefix+"This will permanently delete mob '"+((MOB)E).Name()+"' from the catalog.  Are you sure (y/N)?","N"))))
						{
							CMLib.catalog().delCatalog((MOB)E);
							mob.tell("MOB '"+((MOB)E).Name()+" has been permanently removed from the catalog.");
						}
					}
					else
					if(E instanceof Item)
					{
						String prefix="";
						if((data!=null)&&(data.numReferences()>0))
							prefix="Catalog Item '"+((Item)E).Name()+"' is currently listed as being in use '"+data.numReferences()+" times.  ";
						if((mob.session()!=null)
						&&((del.length>10)||(mob.session().confirm(prefix+"This will permanently delete item '"+((Item)E).Name()+"' from the catalog.  Are you sure (y/N)?","N"))))
						{
							CMLib.catalog().delCatalog((Item)E);
							mob.tell("Item '"+E.Name()+" has been permanently removed from the catalog.");
						}
					}
				}
			}
            else
            if(((String)commands.firstElement()).equalsIgnoreCase("EDIT"))
            {
                commands.removeElementAt(0);
				int whatKind=getObjectType(commands);
                String ID=CMParms.combine(commands,0);
                Environmental E=findCatalog(whatKind,ID,false);
                if(E==null)
                {
                    mob.tell("'"+ID+"' not found in catalog! Try CATALOG LIST");
                    return false;
                }
                CatalogLibrary.CataData data=CMLib.catalog().getCatalogData(E);
                if(E instanceof MOB)
                {
                    mob.tell("There is no extra mob data to edit. See help on CATALOG.");
                }
                else
                if(E instanceof Item)
                {
                    if(mob.session()!=null)
                    {
                    	Double newPct=null;
                    	while(newPct == null)
                    	{
	                        String newRate=mob.session().prompt("Enter a new Drop Rate or 0% to disable ("+CMath.toPct(data.getRate())+"): ", CMath.toPct(data.getRate()));
	                        if(newRate.trim().length()==0)
	                        	return false;
	                        else
	                        if(CMath.isPct(newRate))
	                        	newPct=Double.valueOf(CMath.s_pct(newRate));
	                        else
	                        	mob.tell("'"+newRate+"' is not a valid percentage value.  Try something like 10%");
                    	}
                        data.setRate(newPct.doubleValue());
                        if(data.getRate()<=0.0)
                        {
                            data.setMaskStr("");
                            data.setRate(0.0);
                            CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)E);
                            Log.sysOut("Catalog",mob.Name()+" modified catalog item "+E.Name());
                            mob.tell("No drop item.");
                            return false;
                        }
                        String choice=mob.session().choose("Is this for L)ive mobs or D)ead ones ("+(data.getWhenLive()?"L":"D")+"): ","LD", (data.getWhenLive()?"L":"D"));
                        data.setWhenLive(choice.equalsIgnoreCase("L"));
                        String newMask="?";
                        while(newMask.equalsIgnoreCase("?"))
                        {
                            newMask=mob.session().prompt("Enter new MOB selection mask, or NULL ("+data.getMaskStr()+")\n\r: ",data.getMaskStr());
                            if(newMask.equalsIgnoreCase("?"))
                                mob.tell(CMLib.masking().maskHelp("\n","disallow"));
                        }
                        if((newMask.length()==0)||(newMask.equalsIgnoreCase("null")))
                        {
                            data.setMaskStr("");
                            data.setRate(0.0);
                            mob.tell("Mask removed.");
                        }
                        else
                        {
                            data.setMaskStr(newMask);
                        }
                        CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)E);
                        Log.sysOut("Catalog",mob.Name()+" modified catalog item "+E.Name());
                        mob.tell("Item '"+E.Name()+" has been updated.");
                    }
                }
            }
            else
            if((((String)commands.firstElement()).equalsIgnoreCase("SCAN"))
            ||(((String)commands.firstElement()).equalsIgnoreCase("DBSCAN")))
            {
            	boolean db=((String)commands.firstElement()).toUpperCase().startsWith("DB");
            	commands.removeElementAt(0);
    			if(checkUserRoomSetEntry(commands))
				{
					String which=((String)commands.firstElement()).toLowerCase();
					Enumeration rooms=getRoomSet(mob,which);
					commands.removeElementAt(0);
					
					int whatKind=getObjectType(commands);
					Environmental E=null;
					String roomID=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						R=CMLib.map().getRoom(roomID);
						if(db) R=CMLib.coffeeMaker().makeNewRoomContent(R);
						Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
						for(CatalogLibrary.RoomContent content : contents)
						{
							E=content.E();
							if(E instanceof Coins) continue;
                            if((E instanceof MOB)&&(whatKind!=2)&&(CMLib.flags().isCataloged(E)))
                            	if(CMLib.catalog().getCatalogObj(E)!=null)
	                            	mob.tell("Check: MOB "+E.Name()+" in "+roomID+" is cataloged.");
                            	else
	                            	mob.tell("Error: MOB "+E.Name()+" in "+roomID+" is falsely cataloged.");
                            
                            if((E instanceof Item)&&(whatKind!=1)&&(CMLib.flags().isCataloged(E)))
                            	if(CMLib.catalog().getCatalogObj(E)!=null)
	                            	mob.tell("Check: Item "+E.Name()+" in "+roomID+" is cataloged.");
                            	else
	                            	mob.tell("Error: Item "+E.Name()+" in "+roomID+" is falsely cataloged.");
						}
						if(db) R.destroy();
					}
				}
    			else
    				mob.tell("Scan what?");
            }
            else
            if((((String)commands.firstElement()).equalsIgnoreCase("OVERLOOK"))
            ||(((String)commands.firstElement()).equalsIgnoreCase("DBOVERLOOK")))
            {
            	boolean db=((String)commands.firstElement()).toUpperCase().startsWith("DB");
            	commands.removeElementAt(0);
    			if(checkUserRoomSetEntry(commands))
				{
					String which=((String)commands.firstElement()).toLowerCase();
					Enumeration rooms=getRoomSet(mob,which);
					commands.removeElementAt(0);
					
					int whatKind=getObjectType(commands);
					Environmental E=null;
					String roomID=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						R=CMLib.map().getRoom(roomID);
						if(db) R=CMLib.coffeeMaker().makeNewRoomContent(R);
						Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
						for(CatalogLibrary.RoomContent content : contents)
						{
							E=content.E();
							if(E instanceof Coins) continue;
                            if((E instanceof MOB)&&(whatKind!=2)&&(!CMLib.flags().isCataloged(E)))
                            	if(CMLib.catalog().getCatalogMob(E.Name())!=null)
	                            	mob.tell("MOB "+E.Name()+" in "+roomID+" should be tied to the catalog.");
                            	else
	                            	mob.tell("MOB "+E.Name()+" in "+roomID+" is not cataloged.");
                            
                            if((E instanceof Item)&&(whatKind!=1)&&(!CMLib.flags().isCataloged(E)))
                            	if(CMLib.catalog().getCatalogItem(E.Name())!=null)
	                            	mob.tell("Item "+E.Name()+" in "+roomID+" should be tied to the catalog.");
                            	else
	                            	mob.tell("Item "+E.Name()+" in "+roomID+" is not cataloged.");
						}
						if(db) R.destroy();
					}
				}
    			else
    				mob.tell("Scan what?");
            }
			else
            if(((String)commands.firstElement()).equalsIgnoreCase("CLEAN"))
            {
				commands.removeElementAt(0);
				if(checkUserRoomSetEntry(commands))
				{
					String which=((String)commands.firstElement()).toLowerCase();
					Enumeration rooms=getRoomSet(mob,which);
					commands.removeElementAt(0);
					int whatKind=getObjectType(commands);
					String type=types[whatKind];
					if((mob.session()!=null)
					&&(mob.session().confirm("You are about to auto-clean (and auto-save) all "+which+" "+type+".\n\r"
				        +"This command, if used improperly, may alter "+type+" in this "+which+".\n\rAre you absolutely sure (y/N)?","N")))
					{
						Environmental E=null;
						String roomID=null;
						for(;rooms.hasMoreElements();)
						{
							roomID=(String)rooms.nextElement();
							R=CMLib.coffeeMaker().makeNewRoomContent(CMLib.map().getRoom(roomID));
							Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
							boolean dirty=false;
							for(CatalogLibrary.RoomContent content : contents)
							{
								E=content.E();
								if(E instanceof Coins) continue;
	                            if((E instanceof MOB)&&(whatKind!=2))
	                            {
                                    if((CMLib.flags().isCataloged(E))
                                    &&(CMLib.catalog().getCatalogObj(E)==null))
                                    { 
                                    	CMLib.catalog().changeCatalogUsage(E,false);
                                    	content.flagDirty();
                                    	dirty=true;
                                    	mob.tell("MOB "+E.Name()+" in "+roomID+" was cleaned.");
                                    }
	                            }
	                            else
	                            if((E instanceof Item)&&(whatKind!=1))
	                            {
                                    if((CMLib.flags().isCataloged(E))
                                    &&(CMLib.catalog().getCatalogObj(E)==null))
                                    { 
                                    	CMLib.catalog().changeCatalogUsage(E,false);
                                    	content.flagDirty();
                                    	dirty=true;
                                    	mob.tell("Item "+E.Name()+" in "+roomID+" was cleaned.");
                                    }
	                            }
							}
							if(dirty)
							{
								CMLib.catalog().updateRoomContent(roomID, contents);
								CMLib.map().resetRoom(CMLib.map().getRoom(roomID), true);
								R.destroy();
							}
						}
					}
				}
    			else
    				mob.tell("Clean what?");
			}
			else
			{
				Environmental thisThang=null;
				String ID=CMParms.combine(commands,0);
				if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
					thisThang=mob;
				if(thisThang==null)
					thisThang=R.fetchFromRoomFavorMOBs(null,ID,Wearable.FILTER_ANY);
				if(thisThang!=null)
				{
				    if(!catalog(R,mob,thisThang))
				        return false;
				    if((thisThang instanceof DBIdentifiable)
				    &&(((DBIdentifiable)thisThang).canSaveDatabaseID())
				    &&(((DBIdentifiable)thisThang).databaseID().length()>0))
				    {
					    Room startRoom=CMLib.map().getStartRoom(thisThang);
					    if(startRoom !=null)
					    {
					    	if(thisThang instanceof MOB)
					    		CMLib.database().DBUpdateMOB(startRoom.roomID(),(MOB)thisThang);
					    	else
					    		CMLib.database().DBUpdateItem(startRoom.roomID(),(Item)thisThang);
					    }
				    }
				}
				else
					mob.tell("You don't see '"+ID+"' here!");
			}
		}
		else
			mob.tell("Catalog huh? Try CATALOG LIST (MOBS/ITEMS) (MASK), CATALOG [mob/item name], CATALOG DELETE [mob/item name], CATALOG EDIT [item name].");
		return false;
	}
	
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CATALOG");}
}
