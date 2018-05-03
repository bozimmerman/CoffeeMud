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
   Copyright 2003-2018 Bo Zimmerman

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

public class Skill_HandCuff extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_HandCuff";
	}

	private final static String localizedName = CMLib.lang().L("Handcuff");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Handcuffed)");

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

	private static final String[] triggerStrings = I(new String[] { "HANDCUFF", "CUFF" });

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
		return Ability.FLAG_BINDING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	public int amountRemaining=0;
	public boolean oldAssist=false;
	public boolean oldGuard=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BOUND);
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
		if(msg.amISource(mob))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> attempt(s) to recall, but the handcuffs prevent <S-HIM-HER>."));
				return false;
			}
			else
			if(((msg.sourceMinor()==CMMsg.TYP_FOLLOW)&&(msg.target()!=invoker()))
			||((msg.sourceMinor()==CMMsg.TYP_NOFOLLOW)&&(msg.source().amFollowing()==invoker())))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) against <S-HIS-HER> cuffs."));
				amountRemaining-=(mob.charStats().getStat(CharStats.STAT_STRENGTH)+mob.phyStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_SIT)
			||(msg.sourceMinor()==CMMsg.TYP_STAND))
				return true;
			else
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&(!((Room)msg.target()).isInhabitant(invoker))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) against <S-HIS-HER> cuffs."));
				amountRemaining-=(mob.charStats().getStat(CharStats.STAT_STRENGTH)+mob.phyStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_ENTER)
				return true;
			else
			if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(amountRemaining>0)
			&&((msg.sourceMajor(CMMsg.MASK_HANDS))
				||(msg.sourceMajor(CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) against <S-HIS-HER> cuffs."));
				amountRemaining-=mob.charStats().getStat(CharStats.STAT_STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		else
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(msg.amITarget(affected))
		&&(!mob.isInCombat())
		&&(mob.amFollowing()!=null)
		&&(msg.source().isMonster())
		&&(msg.source().getVictim()!=mob))
		{
			msg.source().tell(L("You may not assault this prisoner."));
			if(mob.getVictim()==msg.source())
			{
				mob.makePeace(true);
				mob.setVictim(null);
			}
			return false;
		}
		return super.okMessage(myHost,msg);
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
			mob.setFollowing(null);
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> <S-IS-ARE> released from the handcuffs."));
			if(!oldAssist)
				mob.setAttribute(MOB.Attrib.AUTOASSIST,false);
			if(oldGuard)
				mob.setAttribute(MOB.Attrib.AUTOGUARD,false);
			CMLib.commands().postStand(mob,true);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target instanceof MOB))
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(Skill_Arrest.getWarrantsOf((MOB)target, CMLib.law().getLegalObject(mob.location().getArea())).size()==0)
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isStanding((MOB)target))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		if((commands.size()>0)&&(commands.get(0)).equalsIgnoreCase("UNTIE"))
		{
			commands.remove(0);
			final MOB target=super.getTarget(mob,commands,givenTarget,false,true);
			if(target==null)
				return false;
			final Ability A=target.fetchEffect(ID());
			if(A!=null)
			{
				if(mob.location().show(mob,target,null,CMMsg.MSG_HANDS,L("<S-NAME> attempt(s) to unbind <T-NAMESELF>.")))
				{
					A.unInvoke();
					return true;
				}
				return false;
			}
			mob.tell(L("@x1 doesn't appear to be handcuffed.",target.name(mob)));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(Skill_Arrest.getWarrantsOf(target, CMLib.law().getLegalObject(mob.location().getArea())).size()==0)
		{
			mob.tell(L("@x1 has no warrants out here.",target.name(mob)));
			return false;
		}
		if((CMLib.flags().isStanding(target))&&(!auto))
		{
			mob.tell(L("@x1 doesn't look willing to cooperate.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:CMMsg.MASK_MALICIOUS),L("<S-NAME> handcuff(s) <T-NAME>."));
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final int amountToRemain=adjustedLevel(mob,asLevel)*300;
					amountRemaining=amountToRemain;
					if(target.location()==mob.location())
					{
						success=maliciousAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER,-1)!=null;
						if(success)
						{
							Skill_HandCuff A = (Skill_HandCuff)target.fetchEffect(ID());
							if(A!=null)
							{
								A.amountRemaining = amountToRemain;
								if(auto)
									A.makeLongLasting();
							}
							oldAssist=target.isAttributeSet(MOB.Attrib.AUTOASSIST);
							if(!oldAssist)
								target.setAttribute(MOB.Attrib.AUTOASSIST,true);
							oldGuard=target.isAttributeSet(MOB.Attrib.AUTOASSIST);
							if(oldGuard)
								target.setAttribute(MOB.Attrib.AUTOGUARD,false);
							final boolean oldNOFOL=target.isAttributeSet(MOB.Attrib.NOFOLLOW);
							if(target.numFollowers()>0)
								CMLib.commands().forceStandardCommand(target,"NoFollow",new XVector<String>("UNFOLLOW","QUIETLY"));
							target.setAttribute(MOB.Attrib.NOFOLLOW,false);
							CMLib.commands().postFollow(target,mob,true);
							if(oldNOFOL)
								target.setAttribute(MOB.Attrib.NOFOLLOW,true);
							else
								target.setAttribute(MOB.Attrib.NOFOLLOW,false);
							target.setFollowing(mob);
							A = (Skill_HandCuff)target.fetchEffect(ID());
							if(A!=null)
								A.amountRemaining = amountToRemain;
						}
					}
				}
				if(mob.getVictim()==target)
					mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to bind <T-NAME> and fail(s)."));

		// return whether it worked
		return success;
	}
}
