package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class Prop_RoomWatch extends Property
{
	public String ID() { return "Prop_RoomWatch"; }
	public String name(){ return "Different Room Can Watch";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){return new Prop_RoomWatch();}
	private Vector newRooms=null;

	public String accountForYourself()
	{ return "Different View of "+text();	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(newRooms==null)
		{
			Vector V=Util.parseSemicolons(text(),true);
			newRooms=new Vector();
			for(int v=0;v<V.size();v++)
			{
				Room R=CMMap.getRoom((String)V.elementAt(v));
				if(R!=null) newRooms.addElement(R);
			}
		}

		if((affected!=null)
		&&(msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0))
		{
			for(int r=0;r<newRooms.size();r++)
			{
				Room R=(Room)newRooms.elementAt(r);
				if((R!=null)&&(R.fetchEffect(ID())==null))
				{
					FullMsg msg2=new FullMsg(msg.source(),msg.target(),msg.tool(),
								  CMMsg.NO_EFFECT,null,
								  CMMsg.NO_EFFECT,null,
								  msg.othersCode(),msg.othersMessage());
					if(R.okMessage(msg.source(),msg2))
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(Sense.canSee(M))
						&&(Sense.canBeSeenBy(R,M))
						&&(Sense.canBeSeenBy(msg2.source(),M)))
							M.executeMsg(M,msg2);
					}
				}
			}
		}
	}
}
