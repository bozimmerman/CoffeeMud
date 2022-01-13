package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2022 Bo Zimmerman

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
public class Thief_WhipStrip extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_WhipStrip";
	}

	private final static String localizedName = CMLib.lang().L("Whip Strip");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;
	}

	private static final String[] triggerStrings =I(new String[] {"WHIPSTRIP"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	public int code=0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code=newCode;
	}

	protected Item stolenI=null;
	protected MOB target=null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(target==null))
			return super.okMessage(myHost,msg);
		final MOB mob=(MOB)affected;
		MOB target=this.target;
		if(target==null)
			target=mob.getVictim();
		if(msg.amISource(mob)
		&&(msg.amITarget(target))
		&&(target instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==mob.fetchWieldedItem())
		&&(msg.tool()!=null)
		&&(msg.value()>0)
		&&(this.prereqs(msg.source(),true)))
		{
			final Room R=target.location();
			if(R==null)
				return super.okMessage(myHost, msg);
			Item stolenI=this.stolenI;
			if((stolenI==null)||(!stolenI.amBeingWornProperly()))
				stolenI=target.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
			if((stolenI==null)||(!stolenI.amBeingWornProperly()))
				return super.okMessage(myHost, msg);
			final String str=L("<S-NAME> whip strip(s) @x1 off <T-NAMESELF>.",stolenI.name());

			final String hisStr=str;
			final int hisCode=CMMsg.MSG_THIEF_ACT | ((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);

			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(R.okMessage(mob,msg2))
			{
				R.send(mob,msg2);
				msg2=CMClass.getMsg(target,stolenI,null,CMMsg.MSG_REMOVE,CMMsg.MSG_REMOVE,CMMsg.MSG_NOISE,null);
				if(R.okMessage(target,msg2))
				{
					R.send(mob,msg2);
					msg2=CMClass.getMsg(target,stolenI,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
					if(R.okMessage(target,msg2))
						R.send(mob,msg2);
				}
			}
			unInvoke();
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(((MOB)target).amDead()||(!CMLib.flags().canBeSeenBy(target,mob)))
				return Ability.QUALITY_INDIFFERENT;
			if(!((MOB)target).mayIFight(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(!prereqs(mob,true))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	protected boolean prereqs(final MOB mob, final boolean quiet)
	{
		final Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon))
		||(((Weapon)w).weaponClassification()!=Weapon.CLASS_FLAILED)
		||((((Weapon)w).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		{
			if(!quiet)
				mob.tell(L("You need a leather flailed weapon to perform a whipstrip!"));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String itemToSteal="all";
		if(!auto)
		{
			if(commands.size()<2)
			{
				mob.tell(L("Whip Strip what off of whom?"));
				return false;
			}
			itemToSteal=commands.get(0);
		}

		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands,1));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",CMParms.combine(commands,1)));
			return false;
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));
		if((!target.mayIFight(mob))||(levelDiff>15))
		{
			mob.tell(L("You cannot strip anything off of @x1.",target.charStats().himher()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Item stolenI=target.fetchItem(null,Wearable.FILTER_WORNONLY,itemToSteal);
		if((stolenI==null)||(!CMLib.flags().canBeSeenBy(stolenI,mob)))
		{
			mob.tell(L("@x1 doesn't seem to be wearing '@x2'.",target.name(mob),itemToSteal));
			return false;
		}

		if(levelDiff>0)
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((CMLib.flags().canBeSeenBy(mob,target))?1:2));

		final boolean success=proficiencyCheck(mob,levelDiff,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,
					L("^F^<FIGHT^><S-NAME> wind(s) up to whipstrip <T-NAME>!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				final Thief_WhipStrip A=(Thief_WhipStrip)beneficialAffect(mob,mob,asLevel,2);
				A.target=target;
				A.stolenI=stolenI;
				mob.recoverPhyStats();
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> wind(s) up to whipstrip <T-NAME>, but fail(s)."));
		return success;
	}
}
