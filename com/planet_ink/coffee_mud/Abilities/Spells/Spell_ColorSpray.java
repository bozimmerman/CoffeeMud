package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_ColorSpray extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ColorSpray";
	}

	private final static String localizedName = CMLib.lang().L("Color Spray");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override public String displayText() { 
		if(text().equalsIgnoreCase("UNCONSCIOUS"))
			return L("(Dazed into unconsciousness)");
		else
			return L("(Dazed and blind)");
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(text().equalsIgnoreCase("UNCONSCIOUS"))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SEE);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			if(text().equalsIgnoreCase("UNCONSCIOUS"))
			{
				mob.tell(L("You regain consciousness."));
				CMLib.commands().postStand(mob, true);
			}
			else
			if(text().equalsIgnoreCase("STUNNED"))
				mob.tell(L("You are no longer stunned and your vision returns."));
			else
				mob.tell(L("You are no longer dazed and your vision returns."));
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if((((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0)||(!CMLib.flags().canSee((MOB)target)))
					return Ability.QUALITY_INDIFFERENT;
				if(!CMLib.flags().canSee((MOB)target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			if((msg.sourceMajor(CMMsg.MASK_EYES))
			||(msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH))
			||(msg.sourceMajor(CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
				{
					if(CMLib.flags().isSleeping(mob))
						mob.tell(L("You are unconscious."));
					else
					if(text().equalsIgnoreCase("STUNNED"))
						mob.tell(L("You are stunned and dazed."));
					else
						mob.tell(L("You are dazed."));
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(L("There doesn't appear to be anyone here worth color spraying."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> wiggle(s) <S-HIS-HER> fingers, spraying a blast of colors!^?")))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// if they can't hear the sleep spell, it
					// won't happen
					if((target.charStats().getBodyPart(Race.BODY_EYE)>0) && CMLib.flags().canSee(target))
					{
						final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),null);
						if((mob.location().okMessage(mob,msg)) && (target.fetchEffect(this.ID())==null))
						{
							mob.location().send(mob,msg);
							if(msg.value()<=0)
							{
								int levelDiff=(mob.phyStats().level()+(super.getXLEVELLevel(mob)/3)) - target.phyStats().level();
								if(levelDiff<0)
									levelDiff=0;
								int ticks=0;
								String text="";
								if(levelDiff>(CMProps.getIntVar(CMProps.Int.EXPRATE)*2))
								{
									text="UNCONSCIOUS";
								}
								else
								if(levelDiff>CMProps.getIntVar(CMProps.Int.EXPRATE))
								{
									text="STUNNED";
									ticks=4;
								}
								else
								{
									ticks=2;
								}
								final Ability A=maliciousAffect(mob,target,asLevel,ticks,CMMsg.TYP_MIND);
								if(A!=null)
								{
									if(levelDiff>(CMProps.getIntVar(CMProps.Int.EXPRATE)*2))
									{
										mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> fall(s) unconscious!"));
									}
									else
									if(levelDiff>CMProps.getIntVar(CMProps.Int.EXPRATE))
									{
										mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> <T-IS-ARE> stunned!"));
									}
									else
									{
										mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> <T-IS-ARE> momentarily dazed!"));
									}
									A.setMiscText(text);
									target.recoverPhyStats();
								}
							}
						}
					}
					else
						maliciousFizzle(mob,target,L("<T-NAME> seem(s) unaffected by the spell from <S-NAME>."));
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> wiggle(s) fingers around, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
