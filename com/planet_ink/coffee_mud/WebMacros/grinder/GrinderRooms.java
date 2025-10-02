package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class GrinderRooms
{
	public static void happilyAddMob(final MOB M, final Room R)
	{
		M.setStartRoom(R);
		M.setLocation(R);
		M.phyStats().setRejuv(5000);
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		M.resetToMaxState();
		M.bringToLife(R,true);
		R.recoverRoomStats();
	}

	public static void happilyAddItem(final Item I, final Room R)
	{
		if(I.subjectToWearAndTear())
			I.setUsesRemaining(100);
		I.recoverPhyStats();
		R.addItem(I);
		R.recoverRoomStats();
	}

	public static void editTags(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final CMObject o)
	{
		if(parms.containsKey("TAGS") && (o instanceof Taggable) && (httpReq.isUrlParameter("TAGS")))
		{
			final Taggable T=(Taggable)o;
			for (final Enumeration<String> t = T.tags(); t.hasMoreElements();)
				T.delTag(t.nextElement());
			for(final String tag : CMParms.parseSemicolons(httpReq.getUrlParameter("TAGS"), true))
				T.addTag(tag);
		}
	}

	public static String editRoom(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final MOB whom, Room R)
	{
		if(R==null)
			return "Old Room not defined!";
		boolean redoAllMyDamnRooms=false;
		final Room oldR=R;

		// class!
		final String className=httpReq.getUrlParameter("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this room.";
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);

			final boolean singleMobMode=CMath.s_bool(httpReq.getUrlParameter("SINGLEMOB"));
			final String delMOB=singleMobMode?httpReq.getUrlParameter("DELMOB"):null;

			CMLib.map().resetRoom(R);
			final Room copyRoom=(Room)R.copyOf();
			boolean skipImage=false;

			if(!className.equalsIgnoreCase(CMClass.classID(R)))
			{
				R=CMClass.getLocale(className);
				if(R==null)
					return "The class you chose does not exist.  Choose another.";
				oldR.delAllEffects(true);
				CMLib.threads().deleteTick(oldR,-1);
				R.setRoomID(oldR.roomID());
				final Area area=oldR.getArea();
				if(area!=null)
					area.delProperRoom(oldR);
				R.setArea(area);
				for(int d=0;d<R.rawDoors().length;d++)
					R.rawDoors()[d]=oldR.rawDoors()[d];
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Exit E=oldR.getRawExit(d);
					if(E!=null)
						R.setRawExit(d,(Exit)E.copyOf());
				}
				redoAllMyDamnRooms=true;
				if(R.image().equalsIgnoreCase(CMLib.protocol().getDefaultMXPImage(oldR)))
				{
					R.setImage(null);
					skipImage=true;
				}
			}

			// name
			final String name=httpReq.getUrlParameter("NAME");
			if((name==null)||(name.length()==0))
				return "Please enter a name for this room.";
			if(name.length()>250)
				return "The name you entered is too long.";
			R.setDisplayText(name);

			editTags(httpReq,parms,R);

			// description
			String desc=CMStrings.fixMudCRLF(httpReq.getUrlParameter("DESCRIPTION"));
			if(desc==null)
				desc="";
			R.setDescription(desc);

			// climate
			/*
			if(httpReq.isUrlParameter("CLIMATE"))
			{
				int climate=CMath.s_int(httpReq.getUrlParameter("CLIMATE"));
				if(climate>=0)
				{
					for(int i=1;;i++)
					{
						if(httpReq.isUrlParameter("CLIMATE"+(Integer.toString(i))))
						{
							int newVal=CMath.s_int(httpReq.getUrlParameter("CLIMATE"+(Integer.toString(i))));
							if(newVal<0)
							{
								climate=-1;
								break;
							}
							climate=climate|newVal;
						}
						else
							break;
					}
				}
				R.setClimateType(climate);
			}
			else
				R.setClimateType(-1);
			*/
			// atmosphere
			//if(httpReq.isUrlParameter("ATMOSPHERE"))
			//	R.setAtmosphere(CMath.s_int(httpReq.getUrlParameter("ATMOSPHERE")));

			// image
			if(!skipImage)
			{
				String img=httpReq.getUrlParameter("IMAGE");
				if(img==null)
					img="";
				R.setImage(img);
			}

			if(R instanceof GridLocale)
			{
				String x=httpReq.getUrlParameter("XGRID");
				if(x==null)
					x="";
				((GridLocale)R).setXGridSize(CMath.s_int(x));
				String y=httpReq.getUrlParameter("YGRID");
				if(y==null)
					y="";
				((GridLocale)R).setYGridSize(CMath.s_int(y));
				((GridLocale)R).clearGrid(null);
			}
			if(R instanceof AutoGenArea)
			{
				String AGXMLPATH=httpReq.getUrlParameter("AGXMLPATH");
				if(AGXMLPATH==null)
					AGXMLPATH="";
				((AutoGenArea) R).setGeneratorXmlPath(CMLib.coffeeFilter().safetyInFilter(AGXMLPATH));

				String AGAUTOVAR=httpReq.getUrlParameter("AGAUTOVAR");
				if(AGAUTOVAR==null)
					AGAUTOVAR="";
				((AutoGenArea) R).setAutoGenVariables(CMLib.coffeeFilter().safetyInFilter(AGAUTOVAR));
			}

			String error=GrinderAreas.doAffects(R,httpReq,parms);
			if(error.length()>0)
				return error;
			error=GrinderAreas.doBehavs(R,httpReq,parms);
			if(error.length()>0)
				return error;

			// here's where you resolve items and mobs
			final List<MOB> allmobs=new ArrayList<MOB>();
			int skip=0;
			while(oldR.numInhabitants()>(skip))
			{
				final MOB M=oldR.fetchInhabitant(skip);
				if(M.isSavable())
				{
					if(!allmobs.contains(M))
						allmobs.add(M);
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
			final List<Item> allitems=new ArrayList<Item>();
			while(oldR.numItems()>0)
			{
				final Item I=oldR.getItem(0);
				if(!allitems.contains(I))
					allitems.add(I);
				oldR.delItem(I);
			}

			if(httpReq.isUrlParameter("MOB1"))
			{
				for(int i=1;;i++)
				{
					final String MATCHING=httpReq.getUrlParameter("MOB"+i);
					if(MATCHING==null)
						break;
					else
					if(CMLib.webMacroFilter().isAllNum(MATCHING))
					{
						final MOB M=CMLib.webMacroFilter().getMOBFromWebCache(allmobs,MATCHING);
						if(M!=null)
						{
							if(MATCHING.equalsIgnoreCase(delMOB))
								continue;
							happilyAddMob(M,R);
						}
						else
						{
							final StringBuffer str=new StringBuffer("!!!No MOB?!!!!");
							str.append(" Got: "+MATCHING);
						}
					}
					else
					if(MATCHING.startsWith("CATALOG-"))
					{
						final MOB M=CMLib.webMacroFilter().getMOBFromCatalog(MATCHING);
						if(M!=null)
							happilyAddMob((MOB)M.copyOf(),R);
					}
					else
					if(MATCHING.indexOf('@')>0)
					{
						final MOB M2=CMLib.webMacroFilter().getMOBFromAnywhere(MATCHING);
						if(M2 != null)
							happilyAddMob((MOB)M2.copyOf(),R);
					}
					else
					for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
					{
						final MOB M2=m.nextElement();
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

			if(httpReq.isUrlParameter("ITEM1"))
			{
				final List<Item> items=new ArrayList<Item>();
				final List<String> cstrings=new ArrayList<String>();
				for(int i=1;;i++)
				{
					final String MATCHING=httpReq.getUrlParameter("ITEM"+i);
					if(MATCHING==null)
						break;
					Item I2=CMLib.webMacroFilter().findItemInAnything(allitems,MATCHING);
					if(I2!=null)
					{
						if(!CMLib.webMacroFilter().isAllNum(MATCHING))
							I2=(Item)I2.copyOf();
						if(I2!=null)
						{
							I2.unWear();
							//if(worn) I2.wearEvenIfImpossible(M);
							happilyAddItem(I2,R);
							items.add(I2);
							I2.setContainer(null);
							final String CONTAINER=httpReq.getUrlParameter("ITEMCONT"+i);
							cstrings.add((CONTAINER==null)?"":CONTAINER);
						}
					}
				}
				for(int i=0;i<cstrings.size();i++)
				{
					final String CONTAINER=cstrings.get(i);
					if(CONTAINER.length()==0)
						continue;
					final Item I2=items.get(i);
					final Item C2=(Item)CMLib.english().fetchEnvironmental(items,CONTAINER,true);
					if(C2 instanceof Container)
						I2.setContainer((Container)C2);
				}
			}
			else
				return "No Item Data!";

			for(int i=0;i<allitems.size();i++)
			{
				final Item I=allitems.get(i);
				if(!R.isContent(I))
					I.destroy();
			}
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I.container()!=null)&&(!R.isContent(I.container())))
					I.setContainer(null);
			}
			for(int m=0;m<allmobs.size();m++)
			{
				final MOB M=allmobs.get(m);
				if(!R.isInhabitant(M))
					M.destroy();
			}

			if(redoAllMyDamnRooms)
			{
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R2=r.nextElement();
						for(int d=0;d<R2.rawDoors().length;d++)
						{
							if(R2.rawDoors()[d]==oldR)
							{
								R2.rawDoors()[d]=R;
								if(R2 instanceof GridLocale)
									((GridLocale)R2).buildGrid();
							}
						}
					}
				}
				catch(final NoSuchElementException e)
				{
				}
				try
				{
					for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
					{
						final MOB M=e.nextElement();
						if(M.getStartRoom()==oldR)
							M.setStartRoom(R);
						else
						if(M.location()==oldR)
							M.setLocation(R);
					}
				}
				catch(final NoSuchElementException e)
				{
				}
			}
			R.getArea().fillInAreaRoom(R);
			CMLib.database().DBUpdateRoom(R);
			CMLib.database().DBUpdateMOBs(R);
			CMLib.database().DBUpdateItems(R);
			CMLib.threads().rejuv(R, Tickable.TICKID_ROOM_ITEM_REJUV);
			R.startItemRejuv();
			if(oldR!=R)
			{
				oldR.destroy();
				R.getArea().addProperRoom(R);
			}
			if(!copyRoom.sameAs(R))
				Log.sysOut("Grinder",whom.Name()+" modified room "+R.roomID()+".");
			copyRoom.destroy();
			fixDeities(R);
		}
		return "";
	}

	protected static void fixDeities(final Room R)
	{
		if(R==null)
			return;
		//OK! Keep this weirdness here!  It's necessary because oldRoom will have blown
		//away any real deities with copy deities in the CMMap, and this will restore
		//the real ones.
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M instanceof Deity)
			&&(M.isMonster())
			&&(M.isSavable())
			&&(M.getStartRoom()==R))
				CMLib.map().registerWorldObjectLoaded(R.getArea(), R, M);
		}
	}

	public static String delRoom(final Room R)
	{
		CMLib.map().obliterateMapRoom(R);
		return "";
	}

	public static Room createLonelyRoom(final Area A, final Room linkTo, final int dir, final boolean copyThisOne)
	{
		Room newRoom=null;
		final String newRoomID=A.getNewRoomID(linkTo,dir);
		if(newRoomID.length()==0)
			return null;
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
			newRoom.setDisplayText(CMLib.lang().L("Title of @x1",newRoomID));
			newRoom.setDescription(CMLib.lang().L("Description of @x1",newRoomID));
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

	public static String createRoom(final Room R, final int dir, final boolean copyThisOne)
	{
		if(dir>=R.rawDoors().length)
			return "";
		R.clearSky();
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		final Room newRoom=createLonelyRoom(R.getArea(),R,dir,copyThisOne);
		R.rawDoors()[dir]=newRoom;
		if(R.getRawExit(dir)==null)
			R.setRawExit(dir,CMClass.getExit("StdOpenDoorway"));
		CMLib.database().DBUpdateExits(R);
		R.getArea().fillInAreaRoom(R);
		return "";
	}

	public static Room createGridRoom(final Area A, final String roomID, final Room copyThisOne, final RoomnumberSet deferredExitSaves, final boolean autoLink)
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
			R.setDisplayText(CMLib.lang().L("Title of @x1",R.roomID()));
			R.setDescription(CMLib.lang().L("Description of @x1",R.roomID()));
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
			final GridZones GZ=(GridZones)R.getArea();
			final GridZones.XYVector Rxy=GZ.getRoomXY(R);
			boolean resaveMyExits=false;
			if((Rxy.x>=0)&&(Rxy.y>=0))
			{
				Room R2=null;

				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final int[] xy=Directions.adjustXYByDirections(Rxy.x,Rxy.y,d);
					R2=GZ.getGridChild(xy[0],xy[1]);
					if((R2!=null)&&(R!=R2))
					{
						final int opD=Directions.getOpDirectionCode(d);
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
