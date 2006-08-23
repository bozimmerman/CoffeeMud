package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2000-2006 Bo Zimmerman

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

public class Prayer_DivineLuck extends Prayer
{
	public String ID() { return "Prayer_DivineLuck"; }
	public String name(){ return "Divine Luck";}
	public String displayText(){ return "(Divine Luck)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	private static final int[] allSaves={
		CharStats.STAT_SAVE_ACID,
		CharStats.STAT_SAVE_COLD,
		CharStats.STAT_SAVE_DISEASE,
		CharStats.STAT_SAVE_ELECTRIC,
		CharStats.STAT_SAVE_FIRE,
		CharStats.STAT_SAVE_GAS,
		CharStats.STAT_SAVE_GENERAL,
		CharStats.STAT_SAVE_JUSTICE,
		CharStats.STAT_SAVE_MAGIC,
		CharStats.STAT_SAVE_MIND,
		CharStats.STAT_SAVE_PARALYSIS,
		CharStats.STAT_SAVE_POISON,
		CharStats.STAT_SAVE_UNDEAD,
		CharStats.STAT_SAVE_WATER,
		CharStats.STAT_SAVE_TRAPS};

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		for(int i=0;i<allSaves.length;i++)
			affectableStats.setStat(allSaves[i],
				affectableStats.getStat(allSaves[i])
					+1+(affectedMOB.envStats().level()/5));
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affected.envStats().level()/5)+1);
		affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()/5)+1);

		if(mob.isInCombat())
		{
			MOB victim=mob.getVictim();
			if(CMLib.flags().isEvil(victim))
				affectableStats.setArmor(affectableStats.armor()-10);
		}
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your divine luck is over.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        Environmental target=mob;
        if((auto)&&(givenTarget!=null)) target=givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
            mob.tell(mob,target,null,"<T-NAME> <T-IS-ARE> already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) divinely lucky.":"^S<S-NAME> "+prayWord(mob)+" for divine luck.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but as luck would have it, there's no answer.");


		// return whether it worked
		return success;
	}
}
