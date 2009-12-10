package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Skill_SlowFall extends BardSkill
{
	public String ID() { return "Skill_SlowFall"; }
	public String name(){return "Slow Fall";}
	public String displayText(){return activated?"(Slow Fall)":"";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_FITNESS; }
	public boolean activated=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(activated) affectableStats.setWeight(0);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected!=null)
		{
			if((affected.fetchEffect("Falling")!=null)
			   &&((!(affected instanceof MOB))
				  ||(((MOB)affected).fetchAbility(ID())==null)
				  ||proficiencyCheck((MOB)affected,0,false)))
			{
				activated=true;
				affected.recoverEnvStats();
				if(affected instanceof MOB)
					helpProficiency((MOB)affected);
			}
			else
			if(activated)
			{
				activated=false;
				affected.recoverEnvStats();
			}
		}
		return super.tick(ticking,tickID);
	}
}
