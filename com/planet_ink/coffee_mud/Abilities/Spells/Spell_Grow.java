package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_Grow extends Spell
{
	public String ID() { return "Spell_Grow"; }
	public String name(){return "Grow";}
	public String displayText(){return "(Grow)";}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}
	private int oldWeight=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			double aff=1.0 + Util.mul(0.1,(invoker().envStats().level()));
			affectableStats.setHeight((int)Math.round(Util.mul(affectableStats.height(),aff)));
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)/2);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+(invoker().envStats().level()/5));
	}

	public void unInvoke()
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(oldWeight<1)
				mob.baseCharStats().getMyRace().setHeightWeight(mob.baseEnvStats(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			else
				mob.baseEnvStats().setWeight(oldWeight);
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shrink(s) back down to size.");
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already HUGE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> grow(s) to an enormous size!");
				oldWeight=target.baseEnvStats().weight();
				double aff=1.0 + Util.mul(0.1,(target.envStats().level()));
				aff=aff*aff;
				target.baseEnvStats().setWeight((int)Math.round(Util.mul(target.baseEnvStats().weight(),aff)));
				beneficialAffect(mob,target,asLevel,0);
				target.confirmWearability();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting but nothing happens.");


		// return whether it worked
		return success;
	}
}
