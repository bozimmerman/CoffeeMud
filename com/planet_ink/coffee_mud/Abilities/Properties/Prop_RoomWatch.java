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

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
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
		&&(affect.othersCode()!=Affect.NO_EFFECT)
		&&(affect.othersMessage()!=null)
		&&(affect.othersMessage().length()>0))
		{
			for(int r=0;r<newRooms.size();r++)
			{
				Room R=(Room)newRooms.elementAt(r);
				if((R!=null)&&(R.fetchAffect(ID())==null))
				{
					Affect msg=new FullMsg(affect.source(),affect.target(),affect.tool(),
								  Affect.NO_EFFECT,null,
								  Affect.NO_EFFECT,null,
								  affect.othersCode(),affect.othersMessage());
					if(R.okAffect(affect.source(),msg))
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(Sense.canSee(M))
						&&(Sense.canBeSeenBy(R,M))
						&&(Sense.canBeSeenBy(affect.source(),M)))
							M.affect(M,msg);
					}
				}
			}
		}
	}
}
