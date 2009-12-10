package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_SneakAttack extends ThiefSkill
{
	public String ID() { return "Thief_SneakAttack"; }
	public String name(){ return "Sneak Attack";}
	public String displayText(){return "";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected boolean activated=false;
	protected boolean oncePerRound=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(activated)
		{
			int xlvl=super.getXLEVELLevel(invoker());
			affectableStats.setDamage(affectableStats.damage()+5+xlvl);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+50+(10*xlvl));
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((affected==null)||((!(affected instanceof MOB)))) return true;
		if(activated
		   &&(!oncePerRound)
		   &&msg.amISource((MOB)affected)
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			oncePerRound=true;
			helpProficiency((MOB)affected);
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(CMLib.flags().isHidden(affected))
		{
			if(!activated)
			{
				activated=true;
				affected.recoverEnvStats();
			}
		}
		else
		if(activated)
		{
			activated=false;
			affected.recoverEnvStats();
		}
		if(oncePerRound) oncePerRound=false;
		return super.tick(ticking,tickID);
	}

}
