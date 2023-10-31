package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Skills.StdSkill;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class Skill_RopeDismount extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_RopeDismount";
	}

	private final static String	localizedName	= CMLib.lang().L("Rope Dismount");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ROPEDISMOUNT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int minRange()
	{
		return 0;
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

	protected static Item getTargetRope(final MOB mob, final MOB targetM)
	{
		if(targetM == null)
			return null;
		for(final Enumeration<Item> i = targetM.items();i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if(I instanceof Rideable)
			{
				final Ability A = ((Rideable)I).fetchEffect("Skill_Lassoing");
				if((A != null)
				&&(A.invoker() == mob))
					return I;
			}
		}
		return null;
	}

	protected static boolean isRopedTarget(final MOB mob, final MOB targetM)
	{
		return getTargetRope(mob, targetM) != null;
	}

	protected static boolean isMountedTarget(final MOB targetM)
	{
		if(targetM == null)
			return false;
		final Room R = targetM.location();
		if(R==null)
			return false;
		final Rideable ride = targetM.riding();
		if(ride != null)
		{
			if(!(ride instanceof Container))
				return true;
			if(((Container)ride).hasADoor())
				return false;
			return true;
		}
		if((R.getArea() instanceof Boardable)
		&&((R.domainType()&Room.INDOORS)==0)
		&&(((Boardable)R.getArea()).getBoardableItem() instanceof NavigableItem))
		{
			final Rideable.Basis rb = ((NavigableItem)((Boardable)R.getArea()).getBoardableItem()).navBasis();
			if((rb == Rideable.Basis.LAND_BASED)
			||(rb == Rideable.Basis.WAGON)
			||(rb == Rideable.Basis.WATER_BASED))
				return true;
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:null;
		if(target == null)
		{
			if(commands.size()>0)
			{
				target = Skill_Lassoing.fetchExposedInhabitant(mob,commands,new Filterer<MOB>() {
					final MOB M = mob;
					@Override
					public boolean passesFilter(final MOB obj)
					{
						return (obj != null)
								&&(isRopedTarget(M,obj))
								&&(isMountedTarget(obj));
					}
				});
				if(target == null)
					target=this.getTarget(mob,commands,givenTarget);
				if(target==null)
					return false;
				if(!isRopedTarget(mob,target))
				{
					mob.tell(L("@x1 was not lassoed by you!",target.name(mob)));
					return false;
				}
				if(!isMountedTarget(target))
				{
					mob.tell(L("@x1 is not mounted!",target.name(mob)));
					return false;
				}
			}
			else
			{
				target = mob.getVictim();
				if((target == null)
				||(!isRopedTarget(mob,target))
				||(!isMountedTarget(target)))
				{
					target = Skill_Lassoing.fetchExposedInhabitant(mob,new XVector<String>("ALL"),new Filterer<MOB>() {
						final MOB M = mob;
						@Override
						public boolean passesFilter(final MOB obj)
						{
							return (obj != null)
									&&(isRopedTarget(M,obj))
									&&(isMountedTarget(obj));
						}
					});
				}
				if(target == null)
				{
					mob.tell(L("You don't see anyone here who is mounted and whom you've lassoed."));
					return false;
				}
			}
		}


		if((!auto)
		&&(!CMLib.flags().isAliveAwakeMobile(mob,true)))
		{
			mob.tell(L("You need to be able to move!"));
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
		final boolean success=proficiencyCheck(mob,adjustment,auto);
		if(success)
		{
			final String msgStr = L("<T-NAME> <T-IS-ARE> dismounted!");
			final Room srcR = mob.location();
			final Room tgtR = target.location();
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0), msgStr);
			final CMMsg tmsg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0), null);
			final CMMsg dismountMsg;
			final boolean riding = target.riding() != null;
			if(riding)
				dismountMsg = CMClass.getMsg(target,target.riding(),null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DISMOUNT,null);
			else
				dismountMsg = CMClass.getMsg(target,tgtR,null,CMMsg.MSG_LEAVE,null);
			CMLib.color().fixSourceFightColor(msg);
			msg.setValue(0);
			tmsg.setValue(0);
			if(srcR.okMessage(mob,msg)
			&&((srcR==tgtR)||(tgtR.okMessage(mob, tmsg)))
			&&tgtR.okMessage(target,dismountMsg))
			{
				srcR.send(mob,msg);
				if(srcR!=tgtR)
					tgtR.send(mob,msg);
				if((msg.value()<=0)&&(tmsg.value()<=0))
				{
					tgtR.send(target,dismountMsg);
					if(!riding)
					{
						srcR.show(target,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> fall(s) into @x1.",srcR.displayText(target)));
						srcR.bringMobHere(target,false);
						target.setRiding(null);
					}
					if(target.riding()==null)
					{
						maliciousAffect(mob,target,asLevel,2,-1);
						target.tell(L("You hit the ground!"));
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to dismount <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
