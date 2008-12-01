package com.planet_ink.coffee_mud.Areas;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.Areas.interfaces.Area;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.CompleteRoomEnumerator;
import com.planet_ink.coffee_mud.Libraries.interfaces.WorldMap;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.DVector;

@SuppressWarnings("unchecked")
public class StdThinInstance extends StdThinArea
{
	public String ID(){	return "StdThinArea";}
	private long flags=Area.FLAG_THIN|Area.FLAG_INSTANCE_PARENT;
	public long flags(){return flags;}
	
    public void addProperRoom(Room R)
    {
    	if(R!=null) R.setExpirationDate(WorldMap.ROOM_EXPIRATION_MILLIS);
    	super.addProperRoom(R);
    }
	public Room getRoom(String roomID)
    {
    	if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
    		return super.getRoom(roomID);
    	
    	if(!isRoom(roomID)) return null;
    	Room R=super.getRoom(roomID);
    	if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
    	{
    		if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
	    		roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			R=CMLib.database().DBReadRoomObject(roomID,false);
			if(R!=null)
			{
				R.setArea(this);
				Vector V=CMParms.makeVector(R);
				CMLib.database().DBReadRoomExits(roomID,V,false);
				CMLib.database().DBReadContent(R,V);
				fillInAreaRoom(R);
				R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
			}
    	}
    	return R;
    }
}
