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
   Copyright 2006-2018 Bo Zimmerman

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

public class Thief_HideInPlainSight extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_HideInPlainSight";
	}

	private final static String localizedName = CMLib.lang().L("Hide In Plain Sight");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Hiding in plain sight)");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"HIDEINPLAINSITE","HIPS"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;
	}

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

	public Ability obscureAbility=null;

	public Ability makeObscurinator(MOB mob)
	{
		if(obscureAbility!=null)
			return obscureAbility;
		obscureAbility=CMClass.getAbility("Spell_ObscureSelf");
		if(obscureAbility==null)
			return null;
		obscureAbility.setAffectedOne(mob);
		return obscureAbility;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_EXAMINE)||(msg.targetMinor()==CMMsg.TYP_LOOK)))
			return true;
		else
		if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
		{
			if(msg.source()==affected)
			{
				if(!msg.othersMajor(CMMsg.MASK_SOUND))
					msg.setOthersMessage(null);
				else
				if((msg.sourceMinor()!=CMMsg.TYP_SPEAK)
				&&(makeObscurinator(msg.source())!=null))
					return makeObscurinator(msg.source()).okMessage(myHost,msg);
			}
			else
			if((msg.sourceMinor()!=CMMsg.TYP_SPEAK)
			&&(affected instanceof MOB)
			&&(makeObscurinator((MOB)affected)!=null))
				return makeObscurinator((MOB)affected).okMessage(myHost,msg);
		}
		return true;
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
	public void unInvoke()
	{
		final MOB M=(MOB)affected;
		super.unInvoke();
		if((M!=null)&&(!M.amDead())&&(super.canBeUninvoked()))
			M.tell(L("You are no longer hiding in plain site."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.fetchEffect(this.ID())!=null)
		{
			Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.unInvoke();
			A=mob.fetchEffect(ID());
			if(A!=null)
				mob.tell(L("You are already hiding in plain site."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final MOB highestMOB=getHighestLevelMOB(mob,null);
		final int levelDiff=mob.phyStats().level()-getMOBLevel(highestMOB)-(this.getXLEVELLevel(mob)*2);

		final String str=L("You step to the side and become totally inconspicuous.");

		boolean success=proficiencyCheck(mob,levelDiff*10,auto);

		if(!success)
		{
			if(highestMOB!=null)
				beneficialVisualFizzle(mob,highestMOB,L("<S-NAME> step(s) to the side of <T-NAMESELF>, but end(s) up looking like an idiot."));
			else
				beneficialVisualFizzle(mob,null,L("<S-NAME> step(s) to the side and look(s) like an idiot."));
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),str,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				beneficialAffect(mob,mob,asLevel,0);
				mob.recoverPhyStats();
			}
			else
				success=false;
		}
		return success;
	}
}
