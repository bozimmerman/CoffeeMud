package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Prop_PracticeDummy extends Property
{
	boolean disabled=false;
	public String ID() { return "Prop_PracticeDummy"; }
	public String name(){ return "Practice Dummy";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected boolean unkillable=true;

	public String accountForYourself()
	{ return "Undefeatable";	}
	
	public void setMiscText(String newMiscText)
	{
	    super.setMiscText(newMiscText);
	    unkillable=newMiscText.toUpperCase().indexOf("KILL")<0;
	}

	public void affectCharState(MOB mob, CharState affectableMaxState)
	{
		super.affectCharState(mob,affectableMaxState);
		if(unkillable)
			affectableMaxState.setHitPoints(99999);
	}

	public void affectEnvStats(Environmental E, EnvStats affectableStats)
	{
		super.affectEnvStats(E,affectableStats);
		if(unkillable)
			affectableStats.setArmor(100);
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) 
		    return false;
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(unkillable))
			{
			    msg.source().tell("You are not allowed to die.");
			    return false;
			}
			else
			if(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			{
				if(unkillable)
					msg.source().curState().setHitPoints(99999);
				((MOB)affected).makePeace();
				Room room=((MOB)affected).location();
				if(room!=null)
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB mob=room.fetchInhabitant(i);
					if((mob.getVictim()!=null)&&(mob.getVictim()==affected))
						mob.makePeace();
				}
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item))
			{
				msg.source().tell("Dummys cant get anything.");
				return false;
			}
		}
		return true;
	}
}
