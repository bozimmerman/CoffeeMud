package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2011-2025 Bo Zimmerman

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
public class Skill_Subdue extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Subdue";
	}

	private final static String localizedName = CMLib.lang().L("Subdue");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		final MOB whom=this.whom;
		if(whom == null)
			return L("(Subdueing)");
		else
		if(affected instanceof MOB)
			return L("(Subdueing @x1)",whom.name((MOB)affected));
		else
			return L("(Subdueing @x1)",whom.name());
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

	private static final String[] triggerStrings =I(new String[] {"SUBDUE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_EVASIVE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected MOB whom=null;
	protected int whomDamage=0;
	protected int asLevel=0;
	protected final Set<MOB> lastKills = new LimitedTreeSet<MOB>(TimeManager.MILI_DAY,250,false);

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.attackAdjustment() - 10 + super.getXLEVELLevel(invoker()));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			final MOB whom = (this.whom==null)?M.getVictim():this.whom;
			if(canBeUninvoked()&&
			(M.amDead()||(!CMLib.flags().isInTheGame(M, false))||(!M.amActive())||M.amDestroyed()||(M.getVictim()!=whom)))
			{
				unInvoke();
				return true;
			}
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(affected !=null)
			&&(msg.source()==affected)
			&&(msg.target()==whom))
			{
				whomDamage+=msg.value();
				msg.setValue(1);
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB whom = (this.whom==null)?((MOB)affected).getVictim():this.whom;
			if((msg.source()==affected)
			&&(msg.target()==whom)
			&&(msg.targetMinor()==CMMsg.TYP_EXAMINE)
			&&(CMLib.flags().canBeSeenBy(whom, msg.source())))
			{
				final double actualHitPct = CMath.div(whom.curState().getHitPoints()-whomDamage,whom.baseState().getHitPoints());
				msg.source().tell(msg.source(),whom,null,L("<T-NAME> is @x1 health away from being overcome.",CMath.toPct(actualHitPct)));
			}

			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(affected !=null)
			&&(msg.source()==affected)
			&&(msg.target()==whom)
			&&(whom.curState().getHitPoints() - whomDamage)<=0)
			{
				final Ability sap=CMClass.getAbility("Skill_ArrestingSap");
				if(sap!=null)
					sap.invoke(whom,new XVector<String>("SAFELY",Integer.toString(adjustedLevel(msg.source(),asLevel))),whom,true,0);
				whom.makePeace(true);
				msg.source().makePeace(true);
				final Skill_Subdue skillA = (Skill_Subdue)msg.source().fetchAbility(ID());
				if((skillA!=null)
				&&(!skillA.lastKills.contains(whom)))
				{
					skillA.lastKills.add(whom);
				}
				unInvoke();
			}
		}
	}

	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		if((canBeUninvoked())
		&&(P instanceof MOB)
		&&(whom!=null))
			((MOB)P).tell(L("You are no longer trying to subdue @x1",whom.name()));
		super.unInvoke();
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Ability A=mob.fetchEffect(ID());
		if(A!=null)
			A.unInvoke();
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("^F^<FIGHT^><S-NAME> attempt(s) to subdue <T-NAMESELF>!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,0);
				final Skill_Subdue SK=(Skill_Subdue)mob.fetchEffect(ID());
				if(SK!=null)
				{
					SK.whom=target;
					SK.asLevel=asLevel;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to subdue <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
