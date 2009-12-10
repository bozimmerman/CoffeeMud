package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
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
public class GrinderRooms
{
	public static void happilyAddMob(MOB M, Room R)
	{
		M.setStartRoom(R);
		M.setLocation(R);
		M.envStats().setRejuv(5000);
		M.recoverCharStats();
		M.recoverEnvStats();
		M.recoverMaxState();
		M.resetToMaxState();
		M.bringToLife(R,true);
		R.recoverRoomStats();
	}
	public static void happilyAddItem(Item I, Room R)
	{
		if(I.subjectToWearAndTear())
			I.setUsesRemaining(100);
		I.recoverEnvStats();
		R.addItem(I);
		R.recoverRoomStats();
	}

	public static String editRoom(ExternalHTTPRequests httpReq, Hashtable parms, MOB whom, Room R)
	{
		if(R==null) return "Old Room not defined!";
		boolean redoAllMyDamnRooms=false;
		Room oldR=R;

		// class!
		String className=httpReq.getRequestParameter("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this room.";
    	synchronized(("SYNC"+R.roomID()).intern())
    	{
    		R=CMLib.map().getRoom(R);
	
            boolean singleMobMode=CMath.s_bool(httpReq.getRequestParameter("SINGLEMOB"));
            String delMOB=singleMobMode?httpReq.getRequestParameter("DELMOB"):null;
            
			CMLib.map().resetRoom(R);
			Room copyRoom=(Room)R.copyOf();
            boolean skipImage=false;
	
			if(!className.equalsIgnoreCase(CMClass.classID(R)))
			{
				R=CMClass.getLocale(className);
				if(R==null)
					return "The class you chose does not exist.  Choose another.";
				for(int a=oldR.numEffects()-1;a>=0;a--)
				{
					Ability A=oldR.fetchEffect(a);
					if(A!=null)
					{
						A.unInvoke();
						oldR.delEffect(A);
					}
				}
				CMLib.threads().deleteTick(oldR,-1);
				R.setRoomID(oldR.roomID());
				Area area=oldR.getArea();
				if(area!=null) area.delProperRoom(oldR);
				R.setArea(area);
				for(int d=0;d<R.rawDoors().length;d++)
					R.rawDoors()[d]=oldR.rawDoors()[d];
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
				    Exit E=oldR.getRawExit(d);
				    if(E!=null)
				        R.setRawExit(d,(Exit)E.copyOf());
				}
				redoAllMyDamnRooms=true;
                if(R.image().equalsIgnoreCase(CMProps.getDefaultMXPImage(oldR)))
                {
                    R.setImage(null);
                    skipImage=true;
                }
			}
	
			// name
			String name=httpReq.getRequestParameter("NAME");
			if((name==null)||(name.length()==0))
				return "Please enter a name for this room.";
			R.setDisplayText(name);
	
	
			// description
			String desc=httpReq.getRequestParameter("DESCRIPTION");
			if(desc==null)desc="";
			R.setDescription(desc);
	
			// image
            if(!skipImage)
            {
    			String img=httpReq.getRequestParameter("IMAGE");
    			if(img==null)img="";
    			R.setImage(img);
            }
	
			if(R instanceof GridLocale)
			{
				String x=httpReq.getRequestParameter("XGRID");
				if(x==null)x="";
				((GridLocale)R).setXGridSize(CMath.s_int(x));
				String y=httpReq.getRequestParameter("YGRID");
				if(y==null)y="";
				((GridLocale)R).setYGridSize(CMath.s_int(y));
				((GridLocale)R).clearGrid(null);
			}
	
			String err=GrinderAreas.doAffectsNBehavs(R,httpReq,parms);
			if(err.length()>0) return err;
	
			// here's where you resolve items and mobs
			Vector allmobs=new Vector();
			int skip=0;
			while(oldR.numInhabitants()>(skip))
			{
				MOB M=oldR.fetchInhabitant(skip);
				if(M.savable())
				{
					if(!allmobs.contains(M))
						allmobs.addElement(M);
					oldR.delInhabitant(M);
				}
				else
				if(oldR!=R)
				{
					oldR.delInhabitant(M);
					R.bringMobHere(M,true);
				}
				else
					skip++;
			}
			Vector allitems=new Vector();
			while(oldR.numItems()>0)
			{
				Item I=oldR.fetchItem(0);
				if(!allitems.contains(I))
					allitems.addElement(I);
				oldR.delItem(I);
			}
	
			if(httpReq.isRequestParameter("MOB1"))
			{
				for(int i=1;;i++)
				{
					String MATCHING=httpReq.getRequestParameter("MOB"+i);
					if(MATCHING==null)
						break;
					else
					if(RoomData.isAllNum(MATCHING))
					{
						MOB M=RoomData.getMOBFromCode(allmobs,MATCHING);
						if(M!=null)	
                        {
                            if(MATCHING.equalsIgnoreCase(delMOB))
                                continue;
                            happilyAddMob(M,R);
                        }
						else
						{
							StringBuffer str=new StringBuffer("!!!No MOB?!!!!");
							str.append(" Got: "+MATCHING);
						}
					}
					else
					if(MATCHING.startsWith("CATALOG-"))
					{
					    MOB M=RoomData.getMOBFromCatalog(MATCHING);
					    if(M!=null)
                            happilyAddMob((MOB)M.copyOf(),R);
					}
					else
					if(MATCHING.indexOf("@")>0)
					{
						for(int m=0;m<RoomData.mobs.size();m++)
						{
							MOB M2=(MOB)RoomData.mobs.elementAt(m);
							if(MATCHING.equals(""+M2))
							{
								happilyAddMob((MOB)M2.copyOf(),R);
								break;
							}
						}
					}
					else
					for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
					{
						MOB M2=(MOB)m.nextElement();
						if((CMClass.classID(M2).equals(MATCHING)))
						{
							happilyAddMob((MOB)M2.copyOf(),R);
							break;
						}
					}
				}
			}
			else
				return "No MOB Data!";
	
	
			if(httpReq.isRequestParameter("ITEM1"))
			{
			    Vector items=new Vector();
			    Vector cstrings=new Vector();
				for(int i=1;;i++)
				{
					String MATCHING=httpReq.getRequestParameter("ITEM"+i);
                    if(MATCHING==null) break;
					Item I2=RoomData.getItemFromAnywhere(allitems,MATCHING);
					if(I2!=null)
					{
	                    if(!RoomData.isAllNum(MATCHING))
	                        I2=(Item)I2.copyOf();
                        I2.unWear();
	                    //if(worn) I2.wearEvenIfImpossible(M);
						happilyAddItem(I2,R);
						items.addElement(I2);
						I2.setContainer(null);
                        String CONTAINER=httpReq.getRequestParameter("ITEMCONT"+i);
                        cstrings.addElement((CONTAINER==null)?"":CONTAINER);
					}
				}
	            for(int i=0;i<cstrings.size();i++)
	            {
	                String CONTAINER=(String)cstrings.elementAt(i);
	                if(CONTAINER.length()==0) continue;
	                Item I2=(Item)items.elementAt(i);
	                Item C2=(Item)CMLib.english().fetchEnvironmental(items,CONTAINER,true);
	                I2.setContainer(C2);
	            }
			}
			else
				return "No Item Data!";
	
	
			for(int i=0;i<allitems.size();i++)
			{
				Item I=(Item)allitems.elementAt(i);
				if(!R.isContent(I))
					I.destroy();
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I.container()!=null)&&(!R.isContent(I.container())))
					I.setContainer(null);
			}
			for(int m=0;m<allmobs.size();m++)
			{
				MOB M=(MOB)allmobs.elementAt(m);
				if(!R.isInhabitant(M))
					M.destroy();
			}
	
			if(redoAllMyDamnRooms)
			{
			    try
			    {
					for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
					{
						Room R2=(Room)r.nextElement();
						for(int d=0;d<R2.rawDoors().length;d++)
							if(R2.rawDoors()[d]==oldR)
							{
								R2.rawDoors()[d]=R;
								if(R2 instanceof GridLocale)
									((GridLocale)R2).buildGrid();
							}
					}
			    }catch(NoSuchElementException e){}
			    try
			    {
					for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
					{
						MOB M=(MOB)e.nextElement();
						if(M.getStartRoom()==oldR)
							M.setStartRoom(R);
						else
						if(M.location()==oldR)
							M.setLocation(R);
					}
			    }catch(NoSuchElementException e){}
			}
			R.getArea().fillInAreaRoom(R);
			CMLib.database().DBUpdateRoom(R);
			CMLib.database().DBUpdateMOBs(R);
			CMLib.database().DBUpdateItems(R);
			R.startItemRejuv();
	        if(oldR!=R)
            { 
                oldR.destroy(); 
                R.getArea().addProperRoom(R);
            }
			if(!copyRoom.sameAs(R))
				Log.sysOut("Grinder",whom.Name()+" modified room "+R.roomID()+".");
			copyRoom.destroy();
    	}
		return "";
	}



	public static String delRoom(Room R)
	{
		CMLib.map().obliterateRoom(R);
		return "";
	}

	public static Room createLonelyRoom(Area A, Room linkTo, int dir, boolean copyThisOne)
	{
		Room newRoom=null;
		String newRoomID=A.getNewRoomID(linkTo,dir);
		if(newRoomID.length()==0) return null;
		if((copyThisOne)&&(linkTo!=null))
		{
			CMLib.map().resetRoom(linkTo);
			newRoom=(Room)linkTo.copyOf();
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				newRoom.rawDoors()[d]=null;
				newRoom.setRawExit(d,null);
			}
		}
		else
		{
			newRoom=CMClass.getLocale("StdRoom");
			newRoom.setDisplayText("Title of "+newRoomID);
			newRoom.setDescription("Description of "+newRoomID);
		}
		newRoom.setRoomID(newRoomID);
		newRoom.setArea(A);
		if(linkTo!=null)
		{
			newRoom.rawDoors()[Directions.getOpDirectionCode(dir)]=linkTo;
			newRoom.setRawExit(Directions.getOpDirectionCode(dir),CMClass.getExit("StdOpenDoorway"));
		}
		CMLib.database().DBCreateRoom(newRoom);
		CMLib.database().DBUpdateExits(newRoom);
		if(newRoom.numInhabitants()>0)
			CMLib.database().DBUpdateMOBs(newRoom);
		if(newRoom.numItems()>0)
			CMLib.database().DBUpdateItems(newRoom);
		newRoom.getArea().fillInAreaRoom(newRoom);
		return newRoom;
	}

	public static String createRoom(Room R, int dir, boolean copyThisOne)
	{
		R.clearSky();
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		Room newRoom=createLonelyRoom(R.getArea(),R,dir,copyThisOne);
		R.rawDoors()[dir]=newRoom;
		if(R.getRawExit(dir)==null)
			R.setRawExit(dir,CMClass.getExit("StdOpenDoorway"));
		CMLib.database().DBUpdateExits(R);
		R.getArea().fillInAreaRoom(R);
		return "";
	}
	public static Room createGridRoom(Area A, String roomID, Room copyThisOne, RoomnumberSet deferredExitSaves, boolean autoLink)
	{
		Room R=null;
		if(copyThisOne!=null)
		{
			R=(Room)copyThisOne.copyOf();
			R.setRoomID(roomID);
		}
		else
		{
			R=CMClass.getLocale("StdRoom");
			R.setRoomID(roomID);
			R.setDisplayText("Title of "+R.roomID());
			R.setDescription("Description of "+R.roomID());
		}
		R.setArea(A);
		CMLib.database().DBCreateRoom(R);
		if(R.numInhabitants()>0)
			CMLib.database().DBUpdateMOBs(R);
		if(R.numItems()>0)
			CMLib.database().DBUpdateItems(R);
		R.getArea().fillInAreaRoom(R);
		if((autoLink)&&(R.getArea() instanceof GridZones))
		{
			GridZones GZ=(GridZones)R.getArea();
			int x=GZ.getGridChildX(R);
			int y=GZ.getGridChildY(R);
			boolean resaveMyExits=false;
			if((x>=0)&&(y>=0))
			{
				Room R2=null;
				
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					int[] xy=Directions.adjustXYByDirections(x,y,d);
					R2=GZ.getGridChild(xy[0],xy[1]);
					if((R2!=null)&&(R!=R2))
					{
						int opD=Directions.getOpDirectionCode(d);
						if(R2.rawDoors()[opD]==null)
						{
							R2.rawDoors()[opD]=R;
							if(R2.getRawExit(opD)==null)
								R2.setRawExit(opD,CMClass.getExit("StdOpenDoorway"));
							if(deferredExitSaves!=null)
							{
								if(!deferredExitSaves.contains(R2.roomID()))
									deferredExitSaves.add(R2.roomID());
							}
							else
								CMLib.database().DBUpdateExits(R2);
						}
						if(R.rawDoors()[d]==null)
						{
							R.rawDoors()[d]=R2;
							if(R.getRawExit(d)==null)
								R.setRawExit(d,CMClass.getExit("StdOpenDoorway"));
							resaveMyExits=true;
						}
					}
				}
			}
			if(resaveMyExits)
			{
				if(deferredExitSaves!=null)
				{
					if(!deferredExitSaves.contains(R.roomID()))
						deferredExitSaves.add(R.roomID());
				}
				else
				{
					CMLib.database().DBUpdateExits(R);
					R.getArea().fillInAreaRoom(R);
				}
			}
		}
		return R;
	}
}
