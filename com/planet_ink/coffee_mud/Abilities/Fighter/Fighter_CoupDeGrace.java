package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Fighter_CoupDeGrace extends FighterSkill
{
	@Override public String ID() { return "Fighter_CoupDeGrace"; }
	@Override public String name(){ return "Coup de Grace";}
	private static final String[] triggerStrings = {"COUP","COUPDEGRACE"};
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(0);}
	@Override public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	@Override public int usageType(){return USAGE_MOVEMENT;}
	@Override protected int overrideMana(){return 150;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!mob.isInCombat()) return Ability.QUALITY_INDIFFERENT;
			if(mob.rangeToTarget()>0) return Ability.QUALITY_INDIFFERENT;
			final Item w=mob.fetchWieldedItem();
			Weapon ww=null;
			if((w==null)||(!(w instanceof Weapon))) return Ability.QUALITY_INDIFFERENT;
			ww=(Weapon)w;
			if((ww.weaponType()!=Weapon.TYPE_SLASHING)
			&&(ww.weaponType()!=Weapon.TYPE_PIERCING))
				 return Ability.QUALITY_INDIFFERENT;
			if(mob.curState().getMovement()<overrideMana()) return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isSleeping(mob.getVictim())) return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(_("You must be in combat to do this!"));
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(_("You are too far away to try that!"));
			return false;
		}
		final Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if(!auto)
		{
			if((w==null)||(!(w instanceof Weapon)))
			{
				mob.tell(_("You cannot coup de grace without a weapon!"));
				return false;
			}
			ww=(Weapon)w;
			if((ww.weaponType()!=Weapon.TYPE_SLASHING)
			&&(ww.weaponType()!=Weapon.TYPE_PIERCING))
			{
				mob.tell(_("You cannot coup de grace with a @x1!",ww.name()));
				return false;
			}
			if(mob.curState().getMovement()<150)
			{
				mob.tell(_("You don't have the energy to try it."));
				return false;
			}
			if(!CMLib.flags().isSleeping(mob.getVictim()))
			{
				if(CMLib.flags().isSitting(mob.getVictim()))
					mob.tell(_("@x1 is on the ground, but not prone!",mob.getVictim().charStats().HeShe()));
				else
					mob.tell(_("@x1 is not prone!",mob.getVictim().charStats().HeShe()));
				return false;
			}
		}

		final MOB target=mob.getVictim();
		final int dmg=target.curState().getHitPoints();
		if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(ww==null))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		final int chance=(-levelDiff)+(-(target.charStats().getStat(CharStats.STAT_CONSTITUTION)*2));
		final boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		final boolean success=proficiencyCheck(mob,chance,auto)&&(hit);
		if((success)&&((dmg<50)||(dmg<(target.maxState().getHitPoints()/4))))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(1);
				CMLib.combat().postDamage(mob,target,ww,dmg,CMMsg.MSG_WEAPONATTACK,ww.weaponClassification(),auto?"":"^F^<FIGHT^><S-NAME> rear(s) back and Coup-de-Grace(s) <T-NAME>!^</FIGHT^>^?"+CMLib.protocol().msp("decap.wav",30));
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			final String str=auto?"":"<S-NAME> attempt(s) a Coup-de-Grace and fail(s)!";
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_ACTION,str);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}
