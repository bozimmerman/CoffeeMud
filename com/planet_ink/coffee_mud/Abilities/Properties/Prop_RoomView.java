package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomView extends Property
{
	public String ID() { return "Prop_RoomView"; }
	public String name(){ return "Different Room View";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Prop_RoomView();}
	private Room newRoom=null;

	public String accountForYourself()
	{ return "Different View of "+text();	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((newRoom==null)||(!newRoom.ID().equalsIgnoreCase(text().trim())))
			newRoom=CMMap.getRoom(text());
		if(newRoom==null) return super.okAffect(myHost,affect);
		
		if((affected!=null)
		&&((affected instanceof Room)||(affected instanceof Exit))
		&&(affect.amITarget(affected))
		&&(newRoom.fetchAffect(ID())==null)
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			Affect msg=new FullMsg(affect.source(),newRoom,affect.tool(),
						  affect.sourceCode(),affect.sourceMessage(),
						  affect.targetCode(),affect.targetMessage(),
						  affect.othersCode(),affect.othersMessage());
			if(newRoom.okAffect(affect.source(),msg))
			{
				newRoom.affect(affect.source(),msg);
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

}
