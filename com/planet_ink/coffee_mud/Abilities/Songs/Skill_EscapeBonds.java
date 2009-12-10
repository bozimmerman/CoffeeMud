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
@SuppressWarnings("unchecked")
public class Skill_EscapeBonds extends BardSkill
{
	public String ID() { return "Skill_EscapeBonds"; }
	public String name(){ return "Escape Bonds";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"ESCAPEBONDS","ESCAPE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public int usageType(){return USAGE_MANA;}


	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(!CMLib.flags().isBound(mob))
			unInvoke();
		else
			stats.setStat(CharStats.STAT_STRENGTH,stats.getStat(CharStats.STAT_STRENGTH)+stats.getStat(CharStats.STAT_DEXTERITY)+mob.envStats().level());
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if((CMLib.flags().isBound((MOB)target))&&(target==mob))
                    return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().isBound(mob))
		{
			mob.tell("You don't seem to be bound by anything you can escape!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),"<S-NAME> attempt(s) to escape <S-HIS-HER> bonds.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.addEffect(this);
				mob.recoverCharStats();
				mob.location().send(mob,msg);
				mob.delEffect(this);
				mob.recoverCharStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> fumble(s) <S-HIS-HER> attempt to escape.");

		return success;
	}

}
