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
public class Prop_Resistance extends Prop_HaveResister
{
	public String ID() { return "Prop_Resistance"; }
	public String name(){ return "Resistance to Stuff";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
    public boolean bubbleAffect(){return false;}
    
    public String accountForYourself()
    { return "Have resistances: "+describeResistance(text());}
    
    public boolean canResist(Environmental E)
    {
        return ((E instanceof MOB)
                &&(E==affected));
    }
}
