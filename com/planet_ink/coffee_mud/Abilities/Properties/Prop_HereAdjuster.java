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
public class Prop_HereAdjuster extends Prop_HaveAdjuster
{
	public String ID() { return "Prop_HereAdjuster"; }
	public String name(){ return "Adjustments to stats when here";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
    
	public String accountForYourself()
	{
		return super.fixAccoutingsWithMask("Affects on those here: "+text());
	}

    
    public boolean canApply(MOB mob)
    {
        if((mob.location()!=affected)
        ||((mask.size()>0)&&(!MUDZapper.zapperCheckReal(mask,mob))))
            return false;
        return true;
    }
}
