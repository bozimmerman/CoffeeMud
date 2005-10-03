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
public class Prop_RideSpellCast extends Prop_HaveSpellCast
{
	public String ID() { return "Prop_RideSpellCast"; }
	public String name(){ return "Casting spells when ridden";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private Vector lastRiders=new Vector();
    public String accountForYourself()
    { return spellAccountingsWithMask("Casts "," on those mounted.");}

    public void setMiscText(String newText)
    { 
        super.setMiscText(newText);
        lastRiders=new Vector();
    }
    
	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if(affected instanceof Rideable)
		{
			Rideable RI=(Rideable)affected;
			for(int r=0;r<RI.numRiders();r++)
			{
				Rider R=RI.fetchRider(r);
				if(R instanceof MOB)
				{
					MOB M=(MOB)R;
					if((!lastRiders.contains(M))&&(RI.amRiding(M)))
                    {
						if(addMeIfNeccessary(M,M))
                            lastRiders.addElement(M);
                    }
				}
			}
			for(int i=lastRiders.size()-1;i>=0;i--)
			{
				MOB M=(MOB)lastRiders.elementAt(i);
				if(!RI.amRiding(M))
                {
					removeMyAffectsFrom(M);
                    while(lastRiders.contains(M))
                        lastRiders.removeElement(M);
                }
			}
		}
		processing=false;
	}
}
