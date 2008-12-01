package com.planet_ink.coffee_mud.Areas;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.Areas.interfaces.Area;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.CompleteRoomEnumerator;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Libraries.interfaces.WorldMap;
import com.planet_ink.coffee_mud.Locales.interfaces.GridLocale;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.DVector;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.MsgListener;

public class StdThinInstance extends StdThinArea
{
	public String ID(){	return "StdThinArea";}
	private long flags=Area.FLAG_THIN|Area.FLAG_INSTANCE_PARENT;
	public long flags(){return flags;}
	
	public Vector children = null;
	
	protected String getStrippedRoomID(String roomID)
	{
		int x=roomID.indexOf("#");
		if(x<0) return null;
		return roomID.substring(x);
	}
	
	protected String convertToMyArea(String roomID)
	{
		String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null) return null;
		return Name()+strippedID;
	}
	
	protected Area getParentArea()
	{
		int x=Name().lastIndexOf("_");
		if(x<0) return null;
		if(!CMath.isNumber(Name().substring(x+1))) return null;
		Area parentA = CMLib.map().getArea(Name().substring(0,x));
		if((parentA==null)
		||(!CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_PARENT))
		||(CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_CHILD)))
			return null;
		return parentA;
	}
	
	public Room getRoom(String roomID)
    {
    	if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
    		return super.getRoom(roomID);
    	
    	if(!isRoom(roomID)) return null;
    	Room R=super.getRoom(roomID);
    	if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
    	{
    		Area parentA=getParentArea();
    		if(parentA==null) return null;
    		
    		if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
	    		roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
    		R=parentA.getRoom(parentA.Name()+getStrippedRoomID(roomID));
    		if(R==null) return null;
    		
    		Room origRoom=R;
    		R=(Room)R.copyOf();
			R.clearSky();
			if(R instanceof GridLocale)
				((GridLocale)R).clearGrid(null);
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				R.rawDoors()[d]=null;
    		R.setRoomID(roomID);
			R.setArea(this);
			addProperRoom(R);
			
			synchronized(("SYNC"+roomID).intern())
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room dirR=origRoom.rawDoors()[d];
					if(dirR!=null)
					{
						String myRID=dirR.roomID();
						if(dirR.getArea()==parentA)
						{
							String localDirRID=convertToMyArea(myRID);
							Room localDirR=getProperRoom(localDirRID);
							if(localDirR!=null)
								R.rawDoors()[d]=localDirR;
							else
							{
	            				R.rawDoors()[d]=CMClass.getLocale("ThinRoom");
	            				R.rawDoors()[d].setRoomID(localDirRID);
	            				R.rawDoors()[d].setArea(this);
							}
						}
						else
						{
							Room localDirR=CMLib.map().getRoom(dirR);
							if(localDirR!=null)
								R.rawDoors()[d]=localDirR;
						}
					}
				}
			}
			fillInAreaRoom(R);
			R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
    	}
    	return R;
    }
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
		
        if((msg.sourceMinor()==CMMsg.TYP_ENTER)
        &&(msg.target() instanceof Room)
        &&(CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT))
        &&(isRoom((Room)msg.target())))
        {
        	
        }
        return true;
	}
}
