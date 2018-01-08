package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Ranger_Camouflage extends StdAbility
{
	@Override
	public String ID()
	{
		return "Ranger_Camouflage";
	}

	private final static String localizedName = CMLib.lang().L("Camouflage");

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
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_NATURELORE;
	}

	private static final String[] triggerStrings =I(new String[] {"CAMOUFLAGE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=0;
	private int bonus=0;
	private int prof=0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
				||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_FLEE)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMajor()>0))
			{
				unInvoke();
				mob.recoverPhyStats();
			}
			else
			if((abilityCode()==0)
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
			&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
			&&(msg.othersMajor()>0))
			{
				if(msg.othersMajor(CMMsg.MASK_SOUND))
				{
					unInvoke();
					mob.recoverPhyStats();
				}
				else
				switch(msg.othersMinor())
				{
				case CMMsg.TYP_SPEAK:
				case CMMsg.TYP_CAST_SPELL:
					{
						unInvoke();
						mob.recoverPhyStats();
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					if(((msg.target() instanceof Exit)
						||((msg.target() instanceof Item)
						   &&(!msg.source().isMine(msg.target())))))
					{
						unInvoke();
						mob.recoverPhyStats();
					}
					break;
				}
			}
		}
		return;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,prof+bonus+affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
		if(CMLib.flags().isSneaking(affected))
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_SNEAKING);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=super.getAnyTarget(mob, commands, givenTarget, new Filterer(){
			@Override
			public boolean passesFilter(Object obj) {
				if((obj instanceof MOB)
				||(obj instanceof Exit))
					return true;
				if(!(obj instanceof Item))
					return false;
				return ((Item)obj).owner() instanceof Room;
			}
			
		});
		if(target==null)
			return false;
		if((target == mob) && (givenTarget != mob))
		{
			mob.tell(L("You can not camouflage yourself!"));
			return false;
		}
		if((mob.isInCombat())
		||((target instanceof MOB)&&((MOB)target).isInCombat()))
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}
		if(target instanceof MOB)
		{
			final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
			if(!H.contains(target))
			{
				mob.tell(L("You can only camouflage a group member."));
				return false;
			}
		}

		if((!CMLib.flags().isInWilderness(mob))&&(!auto))
		{
			mob.tell(L("You only know how to camouflage things outdoors."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Collection<MOB> notList= (target instanceof MOB) ? new XVector<MOB>((MOB)target) : new XVector<MOB>();
		final MOB highestMOB=Ranger_Hide.getHighestLevelMOB(mob,notList);
		final int levelDiff=(mob.phyStats().level()+(2*getXLEVELLevel(mob)))-Ranger_Hide.getMOBLevel(highestMOB);

		final String str;
		if(target instanceof MOB)
			str=L("You carefully camoflauge <T-NAMESELF> and direct <T-HIM-HER> to hold still.");
		else
			str=L("You carefully camoflauge <T-NAMESELF>.");

		boolean success=proficiencyCheck(mob,levelDiff*10,auto);

		if(!success)
		{
			if(highestMOB!=null)
				beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to camouflage <T-NAMESELF> from @x1 and fail(s).",highestMOB.name(mob)));
			else
				beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to camouflage <T-NAMESELF> and fail(s)."));
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),str,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				super.beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
				final Ranger_Camouflage newOne=(Ranger_Camouflage)target.fetchEffect(ID());
				if(newOne!=null)
				{
					newOne.bonus=getXLEVELLevel(mob)*2;
					newOne.prof=proficiency();
				}
				mob.recoverPhyStats();
			}
			else
				success=false;
		}
		return success;
	}
}
