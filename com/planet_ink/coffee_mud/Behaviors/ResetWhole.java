package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ResetWhole extends ActiveTicker
{
	public String ID(){return "ResetWhole";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

	protected long lastAccess=-1;
	
	public ResetWhole()
	{
		tickReset();
	}
	public Behavior newInstance()
	{
		return new ResetWhole();
	}

	public void affect(Environmental E, Affect msg)
	{
		super.affect(E,msg);
		if(!msg.source().isMonster())
		{
			if((E instanceof Area)
			&&(msg.source().location().getArea()==E))
				lastAccess=System.currentTimeMillis();
			else
			if((E instanceof Room)
			&&(msg.source().location()==E))
				lastAccess=System.currentTimeMillis();
		}
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(lastAccess<0) return;
		
		long time=1800000;
		try
		{
			time=Long.parseLong(getParms());
			time=time*Host.TICK_TIME;
		}
		catch(Exception e){}
		if((lastAccess+time)>System.currentTimeMillis())
		{
			if(ticking instanceof Area)
			{
				Vector rooms=((Area)ticking).getMyMap();
				for(int r=0;r<rooms.size();r++)
				{
					Room R=(Room)rooms.elementAt(r);
					for(int b=0;b<R.numBehaviors();b++)
					{
						Behavior B=R.fetchBehavior(b);
						if((B!=null)&&(B.ID().equals(ID())))
						{ R=null; break;}
					}
					if(R!=null)	ExternalPlay.resetRoom(R);
				}
			}
			else
			if(ticking instanceof Room)
				ExternalPlay.resetRoom((Room)ticking);
			else
			{
				Room room=super.getBehaversRoom(ticking);
				if(room!=null)
					ExternalPlay.resetRoom(room);
			}
			lastAccess=-1;
		}
	}
}
