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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2025 Bo Zimmerman

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
public class Fighter_RopeTrip extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_RopeTrip";
	}

	private final static String	localizedName	= CMLib.lang().L("Rope Trip");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Tripped)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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

	private static final String[]	triggerStrings	= I(new String[] { "ROPETRIP", "RTRIP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_BINDING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	protected int		enhancement	= 0;
	protected boolean	doneTicking	= false;

	@Override
	public int abilityCode()
	{
		return enhancement;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		enhancement = newCode;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		else
		if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
			return false;
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked() && (mob!=null))
		{
			final Room R=mob.location();
			if((R!=null)&&(!mob.amDead()))
			{
				final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> regain(s) <S-HIS-HER> feet."));
				if(R.okMessage(mob,msg)&&(!mob.amDead()))
				{
					R.send(mob,msg);
					CMLib.commands().postStand(mob,true, false);
				}
			}
			else
				mob.tell(L("You regain your feet."));
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
				return Ability.QUALITY_INDIFFERENT;
			if((!CMLib.flags().isAliveAwakeMobile(mob,true)||(CMLib.flags().isSitting(mob))))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>2))
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(((MOB)target).riding()!=null))
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isInFlight(target))
				return Ability.QUALITY_INDIFFERENT;
			if(getRope(mob,false)==null)
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				final Rideable targetR = ((MOB)target).riding();
				if((targetR != null)
				&&(mob.riding() != targetR))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	protected Item getRope(final MOB mob, final boolean auto)
	{
		Item lasso = mob.fetchHeldItem();
		if(CMLib.flags().isARope(lasso))
			return lasso;
		lasso = mob.fetchWieldedItem();
		if(CMLib.flags().isARope(lasso))
			return lasso;
		if(auto)
		{
			lasso = CMClass.getBasicItem("GenRideable");
			lasso.setMaterial(RawMaterial.RESOURCE_HEMP);
			((Rideable)lasso).setRideBasis(Rideable.Basis.LADDER);
			lasso.setName("a lasso");
			lasso.setDisplayText("a lasso is here");
			return lasso;
		}
		return null;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(2);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
		{
			failureTell(mob,target,auto,L("<S-NAME> is already on the floor!"));
			return false;
		}

		if((!CMLib.flags().isAliveAwakeMobile(mob,true)||(!CMLib.flags().isStanding(mob))))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		final Rideable targetR = target.riding();
		if((targetR != null)
		&&(mob.riding() != targetR))
		{
			mob.tell(L("You can't trip someone @x1 @x2!",target.riding().stateString(target),target.riding().name()));
			return false;
		}

		final Item lasso = getRope(mob, auto);
		if(lasso == null)
		{
			mob.tell(L("You don't seem to have a suitable rope in hand."));
			return false;
		}

		if(mob.isInCombat()&&(mob.rangeToTarget()>2))
		{
			mob.tell(L("You are too far away to trip!"));
			return false;
		}
		if(target.riding()!=null)
		{
			mob.tell(L("You can't trip someone @x1 @x2!",target.riding().stateString(target),target.riding().name()));
			return false;
		}
		if(CMLib.flags().isInFlight(target))
		{
			mob.tell(L("@x1 is flying and can't be tripped!",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STAT_DEXTERITY));
		final int adjustment=(-levelDiff)+(-(35+((int)Math.round((target.charStats().getStat(CharStats.STAT_DEXTERITY)-9.0)*3.0))));
		boolean success=proficiencyCheck(mob,adjustment,auto);
		success=success&&(target.charStats().getBodyPart(Race.BODY_LEG)>0);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
					auto?L("<T-NAME> trip(s)!"):L("^F^<FIGHT^><S-NAME> trip(s) <T-NAMESELF> with @x1!^</FIGHT^>^?",lasso.name(mob)));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					maliciousAffect(mob,target,asLevel,2,-1);
					target.tell(L("You hit the floor!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to trip <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
