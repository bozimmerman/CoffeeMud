package com.planet_ink.coffee_mud.web.macros.grinder;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderRoom
{
	public int x=0;
	public int y=0;
    public int z=0;
	public String roomID="";
	public Room room=null;
    public GrinderDir[] doors=new GrinderDir[Directions.NUM_DIRECTIONS];
	public GrinderRoom(Room R)
	{
		room=R;
		roomID=R.roomID();
        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
        {
            GrinderDir D=new GrinderDir();
			Room R2=R.rawDoors()[d];
            if(R2!=null)
            {
                D.room=R2.roomID();
				Exit E2=R.rawExits()[d];
                if(E2!=null)
					D.exit=E2;
            }
            doors[d]=D;
        }
	}
}
