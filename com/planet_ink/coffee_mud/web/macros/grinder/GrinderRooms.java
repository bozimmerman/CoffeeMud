package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.web.macros.RoomData;

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
	
	public static String editRoom(ExternalHTTPRequests httpReq, Hashtable parms, Room R)
	{
		if(R==null) return "Old Room not defined!";
		boolean redoAllMyDamnRooms=false;
		Room oldR=R;
		
		// class!
		String className=(String)httpReq.getRequestParameters().get("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this room.";
		
		ExternalPlay.resetRoom(R);
		
		if(!className.equalsIgnoreCase(CMClass.className(R)))
		{
			R=CMClass.getLocale(className);
			if(R==null)
				return "The class you chose does not exist.  Choose another.";
			for(int a=oldR.numAffects()-1;a>=0;a--)
			{
				Ability A=oldR.fetchAffect(a);
				if(A!=null)
				{
					A.unInvoke();
					oldR.delAffect(A);
				}
			}
			ExternalPlay.deleteTick(oldR,-1);
			CMMap.delRoom(oldR);
			CMMap.addRoom(R);
			R.setArea(oldR.getArea());
			R.setID(oldR.ID());
			for(int d=0;d<R.rawDoors().length;d++)
				R.rawDoors()[d]=oldR.rawDoors()[d];
			for(int d=0;d<R.rawExits().length;d++)
				R.rawExits()[d]=oldR.rawExits()[d];
			redoAllMyDamnRooms=true;
		}
		
		// name
		String name=(String)httpReq.getRequestParameters().get("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this room.";
		R.setDisplayText(name);
		
		
		// description
		String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
		if(desc==null)desc="";
		R.setDescription(desc);
		
		String err=GrinderAreas.doAffectsNBehavs(R,httpReq,parms);
		if(err.length()>0) return err;
		
		// here's where you resolve items and mobs
		Vector allmobs=new Vector();
		int skip=0;
		while(oldR.numInhabitants()>(skip))
		{
			MOB M=oldR.fetchInhabitant(skip);
			if(M.isEligibleMonster())
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
		
		if(httpReq.getRequestParameters().containsKey("MOB1"))
		{
			for(int i=1;;i++)
			{
				String MATCHING=(String)httpReq.getRequestParameters().get("MOB"+i);
				if(MATCHING==null)
					break;
				else
				if(RoomData.isAllNum(MATCHING))
				{
					MOB M=RoomData.getMOBFromCode(allmobs,MATCHING);
					if(M!=null)	happilyAddMob(M,R);
					else
					{
						StringBuffer str=new StringBuffer("!!!No MOB?!!!!");
						str.append(" Got: "+MATCHING);
					}
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
				for(int m=0;m<CMClass.MOBs.size();m++)
				{
					MOB M2=(MOB)CMClass.MOBs.elementAt(m);
					if((CMClass.className(M2).equals(MATCHING)))
					{	
						happilyAddMob((MOB)M2.copyOf(),R);
						break;	
					}
				}
			}
		}
		else
			return "No MOB Data!";
			

		if(httpReq.getRequestParameters().containsKey("ITEM1"))
		{
			for(int i=1;;i++)
			{
				String MATCHING=(String)httpReq.getRequestParameters().get("ITEM"+i);
				if(MATCHING==null)
					break;
				else
				{
					Item I2=RoomData.getItemFromAnywhere(allitems,MATCHING);
					if(I2!=null)
					{
						if(RoomData.isAllNum(MATCHING))
							happilyAddItem(I2,R);
						else
							happilyAddItem((Item)I2.copyOf(),R);
					}
				}
			}
		}
		else
			return "No Item Data!";
		
		
		for(int i=0;i<allitems.size();i++)
		{
			Item I=(Item)allitems.elementAt(i);
			if(!R.isContent(I))
				I.destroyThis();
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
			for(int r=0;r<CMMap.numRooms();r++)
			{
				Room R2=CMMap.getRoom(r);
				for(int d=0;d<R2.rawDoors().length;d++)
					if(R2.rawDoors()[d]==oldR)
					{
						R2.rawDoors()[d]=R;
						if(R2 instanceof GridLocale)
							((GridLocale)R2).buildGrid();
					}
			}
			R.getArea().clearMap();
		}
		R.getArea().fillInAreaRoom(R);
		ExternalPlay.DBUpdateRoom(R);
		ExternalPlay.DBUpdateMOBs(R);
		ExternalPlay.DBUpdateItems(R);
		R.startItemRejuv();
		return "";
	}
	
	
	
	public static String delRoom(Room R)
	{
		ExternalPlay.obliterateRoom(R);
		return "";
	}
	
	public static Room createLonelyRoom(Area A, Room linkTo, int dir, boolean copyThisOne)
	{
		Room newRoom=null;
		if((copyThisOne)&&(linkTo!=null))
		{
			ExternalPlay.resetRoom(linkTo);
			newRoom=(Room)linkTo.copyOf();
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				newRoom.rawDoors()[d]=null;
				newRoom.rawExits()[d]=null;
			}
		}
		else
		{
			newRoom=CMClass.getLocale("StdRoom");
			newRoom.setDisplayText("Title of "+newRoom.ID());
			newRoom.setDescription("Description of "+newRoom.ID());
		}
		newRoom.setID(ExternalPlay.getOpenRoomID(A.name()));
		newRoom.setArea(A);
		if(linkTo!=null)
		{
			newRoom.rawDoors()[Directions.getOpDirectionCode(dir)]=linkTo;
			newRoom.rawExits()[Directions.getOpDirectionCode(dir)]=CMClass.getExit("StdOpenDoorway");
		}
		ExternalPlay.DBCreateRoom(newRoom,CMClass.className(newRoom));
		ExternalPlay.DBUpdateExits(newRoom);
		if(newRoom.numInhabitants()>0)
			ExternalPlay.DBUpdateMOBs(newRoom);
		if(newRoom.numItems()>0)
			ExternalPlay.DBUpdateItems(newRoom);
		CMMap.addRoom(newRoom);
		newRoom.getArea().fillInAreaRoom(newRoom);
		return newRoom;
	}
	
	public static String createRoom(Room R, int dir, boolean copyThisOne)
	{
		R.clearSky();
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid();
		Room newRoom=createLonelyRoom(R.getArea(),R,dir,copyThisOne);
		R.rawDoors()[dir]=newRoom;
		if(R.rawExits()[dir]==null)
			R.rawExits()[dir]=CMClass.getExit("StdOpenDoorway");
		ExternalPlay.DBUpdateExits(R);
		R.getArea().fillInAreaRoom(R);
		return "";
	}
}