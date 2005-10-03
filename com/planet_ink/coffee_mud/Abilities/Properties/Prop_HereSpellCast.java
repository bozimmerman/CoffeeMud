package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_HereSpellCast extends Prop_HaveSpellCast
{
	public String ID() { return "Prop_HereSpellCast"; }
	public String name(){ return "Casting spells when here";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public boolean bubbleAffect(){return true;}
	protected int lastNum=-1;
    private Vector lastMOBs=new Vector();

    public String accountForYourself()
    { return spellAccountingsWithMask("Casts "," on those here.");}

    public void setMiscText(String newText)
    { 
        super.setMiscText(newText);
        lastMOBs=new Vector();
    }
    
	public void process(MOB mob, Room room, int code) // code=0 add/sub, 1=addon, 2=subon
	{
		if((code==2)||((code==0)&&(lastNum!=room.numInhabitants())))
		{
			for(int v=lastMOBs.size()-1;v>=0;v--)
			{
				MOB lastMOB=(MOB)lastMOBs.elementAt(v);
				if((lastMOB.location()!=room)
				||((mob==lastMOB)&&(code==2)))
				{
                    removeMyAffectsFrom(lastMOB);
					lastMOBs.removeElementAt(v);
				}
			}
			lastNum=room.numInhabitants();
		}
		if((!lastMOBs.contains(mob))
		&&((code==1)||((code==0)&&(room.isInhabitant(mob)))))
		{
			if(addMeIfNeccessary(mob,mob))
    			lastMOBs.addElement(mob);
		}
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(processing) return;
		if((((msg.targetMinor()==CMMsg.TYP_ENTER)&&(msg.target()==affected))
			||((msg.targetMinor()==CMMsg.TYP_RECALL)&&(msg.target()==affected)))
		&&(affected instanceof Room))
			process(msg.source(),(Room)affected,1);
		else
		if((((msg.targetMinor()==CMMsg.TYP_LEAVE)&&(msg.target()==affected))
			||((msg.targetMinor()==CMMsg.TYP_RECALL)&&(msg.target()!=affected)))
		&&(affected instanceof Room))
			process(msg.source(),(Room)affected,2);
	}
	
	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((host instanceof MOB)&&(affected instanceof Room))
			process((MOB)host, (Room)affected,0);
		processing=false;
	}
}
