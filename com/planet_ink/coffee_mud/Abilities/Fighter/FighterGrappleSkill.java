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
   Copyright 2022-2022 Bo Zimmerman

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

public class FighterGrappleSkill extends FighterSkill
{

	@Override
	public String ID()
	{
		return "FighterGrappleSkill";
	}

	@Override
	public String name()
	{
		return "FighterGrappleSkill";
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_GRAPPLING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_BINDING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}


	protected MOB			pairedWith		= null;
	protected volatile int	proficiencyDiff	= 0;
	protected volatile int	tickUp			= 0;
	protected boolean		broken			= false;

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	protected boolean isImmobilizing()
	{
		return false;
	}

	protected boolean hasWeightLimit()
	{
		return true;
	}

	protected boolean makeBreakAttempt()
	{
		final MOB targetM=(MOB)affected;
		final MOB invoker=invoker();
		if((invoker==null)||(targetM==null))
			return false;
		final int wbest =  Math.max(invoker.charStats().getStat(CharStats.STAT_DEXTERITY),
									invoker.charStats().getStat(CharStats.STAT_STRENGTH))
							+ super.getXLEVELLevel(invoker);
		final int mbest =  Math.min(targetM.charStats().getStat(CharStats.STAT_DEXTERITY),
									targetM.charStats().getStat(CharStats.STAT_STRENGTH))
							+ super.getXLEVELLevel(targetM)
							+ (tickUp * 2);
		if(mbest >= wbest)
		{
			broken=true;
			unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if (affected instanceof MOB)
		{
			final MOB mob = (MOB)affected;
			if((pairedWith == null)
			||(pairedWith.location() != mob.location())
			||(pairedWith.fetchEffect(ID())==null))
			{
				unInvoke();
				return false;
			}
			else
			if(invoker()==mob)
			{
				final Room R=mob.location();
				final int[] consumed = usageCost(mob, false);
				if(!testUsageCost(mob,true,consumed,false))
					return false;
				mob.curState().adjMana(-consumed[0],mob.maxState());
				mob.curState().adjMovement(-consumed[1],mob.maxState());
				mob.curState().adjHitPoints(-consumed[2],mob.maxState());
				if(R!=null)
					R.show(mob,pairedWith,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_VISUAL,null);
				if(!CMLib.combat().rollToHit(mob,pairedWith))
				{
					if(R!=null)
						R.show(mob,pairedWith,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_VISUAL,L("<S-NAME> fail(s) to maintain the @x1.",name().toLowerCase()));
					broken=true;
					unInvoke();
				}
				else
				if(R!=null)
					R.show(mob,pairedWith,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_VISUAL,null);
			}
			else
			if(invoker()==pairedWith)
			{
				final Room R=mob.location();
				if(R!=null)
					R.show(mob,pairedWith,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_VISUAL,null);
				tickUp++;
				if(!makeBreakAttempt())
					return false;
			}
		}
		return true;
	}

	private static final int[] exemptDomains = new int[] {
		Ability.DOMAIN_GRAPPLING, Ability.DOMAIN_DIRTYFIGHTING
	};

	protected int[] getExemptDomains()
	{
		return exemptDomains;
	}

	public boolean isMonkish(final MOB mob)
	{
		return false;
		/*
		if((mob.charStats().getStat(CharStats.STAT_STRENGTH)>=10)
		&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)>=10)
		&&(mob.charStats().getMaxStat(CharStats.STAT_STRENGTH)>=20))
		{
			final CharClass C = CMLib.ableMapper().qualifyingCharClassByLevel(mob, this);
			if((C!=null)
			&&(C.allowedArmorLevel() == CharClass.ARMOR_CLOTH))
				return true;
		}
		return false;
		*/
	}

	protected boolean isHandsFree()
	{
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.sourceMinor() == CMMsg.TYP_DEATH)
		&&(pairedWith != null)
		&&(msg.amISource(pairedWith)))
		{
			unInvoke();
			return super.okMessage(myHost, msg);
		}

		if(msg.amISource(mob))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_HUH:
				if((msg.targetMessage()!=null)
				&&(mob==invoker()))
				{
					final int x=msg.targetMessage().indexOf(' ');
					final String cmd;
					if(x>0)
						cmd=msg.targetMessage().substring(0, x).toUpperCase().trim();
					else
						cmd=msg.targetMessage().toUpperCase().trim();
					if("RELEASE".startsWith(cmd))
					{
						unInvoke();
						return false;
					}
				}
				break;
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_PUSH:
				if((mob == invoker())
				&&(msg.target() == pairedWith)
				&&(CMLib.flags().isStanding(mob))
				&&((msg.targetMinor()==CMMsg.TYP_PULL)||(msg.targetMinor()==CMMsg.TYP_PUSH)))
					return super.okMessage(myHost,msg);
			//$FALL-THROUGH$
			default:
				if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&&(msg.sourceMajor(CMMsg.MASK_HANDS)||msg.sourceMajor(CMMsg.MASK_MOVE)))
				{
					if(mob != invoker())
						makeBreakAttempt();
					if((((!msg.sourceMajor(CMMsg.MASK_MOVE))&&(isHandsFree())))
					||(msg.sourceMinor()==CMMsg.TYP_DELICATE_HANDS_ACT))
						break;
					if(isImmobilizing())
					{
						if(msg.sourceMessage()!=null)
						{
							if(mob==invoker())
								mob.tell(L("You are holding a "+name().toLowerCase()+"!"));
							else
								mob.tell(L("You are in a "+name().toLowerCase()+"!"));
						}
						return false;
					}
					if((!(msg.tool() instanceof Ability))
					||(!CMParms.contains(getExemptDomains(), (((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS))))
					{
						if(msg.sourceMessage()!=null)
						{
							if(mob==invoker())
								mob.tell(L("You are holding a "+name().toLowerCase()+"!"));
							else
								mob.tell(L("You are in a "+name().toLowerCase()+"!"));
						}
						return false;
					}
					else
					if((mob == invoker())
					&&((msg.sourceMinor()!=CMMsg.TYP_WEAPONATTACK)))
						unInvoke();
				}
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target instanceof MOB))
		{
			final MOB tmob=(MOB)target;
			if((hasWeightLimit())
			&&(mob.baseWeight()<tmob.baseWeight()-(mob.baseWeight()*2)))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(tmob.riding()!=mob.riding())
				return Ability.QUALITY_INDIFFERENT;
			if((!isImmobilizing())&&(!CMLib.flags().isStanding(mob)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob, target);
	}

	@Override
	public boolean proficiencyCheck(final MOB mob, int adjustment, final boolean auto)
	{
		adjustment +=proficiencyDiff;
		return super.proficiencyCheck(mob, adjustment, auto);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(isImmobilizing())
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
		final String d = displayText();
		if(d.length()>0)
			affectableStats.addAmbiance(d.toLowerCase());
	}

	public boolean finishGrapple(final MOB mob, final int baseTicks, final MOB target, final int asLevel)
	{
		tickUp=0;
		final int duration = baseTicks + (super.getXLEVELLevel(mob) / 2);
		final FighterGrappleSkill targetGrappleA = (FighterGrappleSkill)maliciousAffect(mob,target,asLevel,duration,-1);
		final FighterGrappleSkill sourceGrappleA = (FighterGrappleSkill)maliciousAffect(mob,mob,asLevel,duration,-1);
		final boolean success = (sourceGrappleA != null) && (targetGrappleA != null);
		if(!success)
		{
			if(targetGrappleA!=null)
				targetGrappleA.unInvoke();
			if(sourceGrappleA!=null)
				sourceGrappleA.unInvoke();
		}
		else
		{
			sourceGrappleA.pairedWith = target;
			targetGrappleA.pairedWith = mob;
		}
		return success;
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
			if((!mob.amDead())
			&&(CMLib.flags().isInTheGame(mob,false)))
			{
				final String actName = name().toLowerCase();
				final Room R=mob.location();
				final MOB P = pairedWith;
				if((mob==invoker) && (P != null))
				{
					if(broken)
					{
						if(R!=null)
							R.show(P,mob,CMMsg.MSG_OK_ACTION,L("<S-NAME> break(s) out of <T-YOUPOSS> "+actName+"."));
						else
							mob.tell(L("Your "+actName+" is broken."));
					}
					else
					if(R!=null)
						R.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> release(s) <S-HIS-HER> "+actName+"."));
					else
						mob.tell(L("You release your "+actName+"."));
				}
				else
				if(broken)
				{
					if(R!=null)
						R.show(mob,P,CMMsg.MSG_OK_ACTION,L("<S-NAME> break(s) out of <T-YOUPOSS> "+actName+"."));
					else
						mob.tell(L("You break out of the "+actName+"."));
				}
				else
				{
					if(R!=null)
						R.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> <S-IS-ARE> released from the "+actName+""));
					else
						mob.tell(L("You are released from the "+actName+"."));
				}
				CMLib.commands().postStand(mob,true, false);
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		proficiencyDiff = 0;
		tickUp = 0;

		if(!(givenTarget instanceof MOB))
			return false;
		final MOB target = (MOB)givenTarget;

		/*if(isGrappled(mob)!=null)
		{
			mob.tell(L("You are already in a grapple."));
			return false;
		}*/

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away from your target to "+name().toLowerCase()+" them!"));
			return false;
		}

		if((!auto)
		&&(hasWeightLimit())
		&&(mob.baseWeight()<target.baseWeight()-(mob.baseWeight()*2)))
		{
			mob.tell(L("@x1 is too big to "+name().toLowerCase()+"!",target.name(mob)));
			return false;
		}

		if(!CMLib.flags().canMove(mob))
		{
			mob.tell(L("You can't move!"));
			return false;
		}

		if(target.riding()!=mob.riding())
		{
			if(target.riding()!=null)
				mob.tell(L("You can't do that to someone @x1 @x2!",target.riding().stateString(target),target.riding().name()));
			else
				mob.tell(L("You can't do that to someone while @x1 @x2!",mob.riding().stateString(mob),mob.riding().name()));
			return false;
		}

		if((!isImmobilizing())&&(!CMLib.flags().isStanding(mob)))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		proficiencyDiff = 0;
		if((givenTarget instanceof MOB)
		&&(!isMonkish(mob))
		&&(mob.isInCombat() || ((MOB)givenTarget).isInCombat())
		&&(getGrappleA((MOB)givenTarget)==null)
		&&((!((MOB)givenTarget).isInCombat())
			||((((MOB)givenTarget).getVictim()==mob)&&(((MOB)givenTarget).rangeToTarget()==0))))
		{
			final MOB victimMOB = (MOB)givenTarget;
			final int oldHP = mob.curState().getHitPoints();
			CMLib.combat().postAttack(victimMOB, mob, victimMOB.fetchWieldedItem());
			final int newHP = mob.curState().getHitPoints();
			final int hpLost = oldHP - newHP;
			if(hpLost > 0)
			{
				final int xdmg = super.getXLEVELLevel(mob) * 5;
				final double pct = (CMath.div(hpLost,mob.maxState().getHitPoints()) * 100.0) - xdmg;
				if(pct > 0)
					proficiencyDiff = -(int)Math.round(pct);
			}
		}
		int levelDiff=givenTarget.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*10;
		else
			levelDiff=0;
		proficiencyDiff -= levelDiff;
		return true;
	}
}
