package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ExtAbility;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_TendMount extends FighterSkill implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Fighter_TendMount";
	}

	private final static String	localizedName	= CMLib.lang().L("Tend Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TENDMOUNT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANATOMY;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			final MOB tmob = (MOB)target;
			final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(mob);
			if(((!choices.containsSecond(tmob.baseCharStats().getMyRace()))
				&&(!choices.containsFirst(tmob.baseCharStats().getMyRace().racialCategory())))
			||(!CMLib.flags().isAnimalIntelligence(tmob)))
				return Ability.QUALITY_INDIFFERENT;
			if(tmob.curState().getHitPoints()<tmob.maxState().getHitPoints())
				return Ability.QUALITY_BENEFICIAL_OTHERS;
			if(returnOffensiveAffects(tmob).size()>0)
				return Ability.QUALITY_BENEFICIAL_OTHERS;
			return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob, target);
	}

	@Override
	public boolean supportsMending(final Physical item)
	{
		if(!(item instanceof MOB))
			return false;
		final MOB tmob = (MOB)item;
		if(!CMLib.flags().isAnimalIntelligence(tmob))
			return false;
		if(tmob.curState().getHitPoints()<tmob.maxState().getHitPoints())
			return true;
		return returnOffensiveAffects(item).size()>0;
	}

	public List<Ability> returnOffensiveAffects(final Physical fromMe)
	{
		final Vector<Ability> offenders=new Vector<Ability>();
		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			final Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				if((A instanceof DiseaseAffect)
				||(A.ID().equalsIgnoreCase("Bleeding"))
				||(A.ID().equalsIgnoreCase("Injury"))
				||(A.ID().equalsIgnoreCase("Hamstring"))
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE))
					offenders.addElement(A);
			}
		}
		return offenders;
	}

	protected volatile int maxTick = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(affected instanceof MOB)
		{
			if(this.maxTick < this.tickDown)
				this.maxTick = this.tickDown;

			final MOB mob=(MOB)affected;
			if((invoker()==null)
			||(invoker().isInCombat())
			||(!CMLib.flags().isAliveAwakeMobileUnbound(invoker(), true))
			||(mob.location() != CMLib.map().roomLocation(invoker())))
				unInvoke();
			else
			if(castingQuality(invoker(),mob)==Ability.QUALITY_INDIFFERENT)
				unInvoke();
			else
			if((this.tickDown % 2)==0)
			{
				mob.location().show(invoker(), mob, CMMsg.MSG_HANDS,
									L("<S-NAME> continue(s) tending to <T-NAME> (@x1).",
									CMath.toWholePct(CMath.div((this.maxTick-tickDown+1),this.maxTick+1))));
			}
			final int xlevel = super.getXLEVELLevel(invoker());
			if((mob.curState().getHitPoints()<mob.maxState().getHitPoints())
			&&(this.maxTick != this.tickDown))
			{
				final double pct = 0.02 + CMath.mul(xlevel,0.005);
				int amtToHeal = (int)Math.round(CMath.mul(mob.maxState().getHitPoints(), pct));
				final int amtNeed = mob.maxState().getHitPoints() - mob.curState().getHitPoints();
				if(amtToHeal > amtNeed)
					amtToHeal = amtNeed;
				if(amtToHeal > 0)
					CMLib.combat().postHealing(invoker(), mob, this, amtToHeal,  CMMsg.MSG_HANDS, null);
			}
			if(CMLib.dice().rollPercentage()<=(5+xlevel))
			{
				final List<Ability> V = this.returnOffensiveAffects(mob);
				if(V.size()>0)
					V.get(CMLib.dice().roll(1, V.size(), -1)).unInvoke();
			}
		}
		return super.tick(ticking, tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((invoker()!=null)
			&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				invoker().tell(invoker(),mob,null,L("You have finished tending to <T-NAME>."));
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)
		&&(givenTarget ==null))
		{
			mob.tell(L("You must specify a target to tend to!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target==mob)
		{
			mob.tell(L("You can't tend yourself."));
			return false;
		}

		final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(mob);
		if(((!choices.containsSecond(target.baseCharStats().getMyRace()))
			&&(!choices.containsFirst(target.baseCharStats().getMyRace().racialCategory())))
		||(!CMLib.flags().isAnimalIntelligence(target)))
		{
			mob.tell(L("@x1 is not the sort you know how to tend to.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),
											L("<S-NAME> begin(s) tending to <T-NAME>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				this.maxTick = 0;
				super.beneficialAffect(mob, target, asLevel, 0);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to tend to <T-NAMESELF>, but fail(s)."));

		return success;
	}

}
