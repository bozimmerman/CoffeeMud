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
   Copyright 2016-2018 Bo Zimmerman

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

public class Skill_DevourCorpse extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_DevourCorpse";
	}

	private final static String	localizedName	= CMLib.lang().L("Devour Corpse");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target() instanceof DeadBody))
		{
			if(proficiencyCheck(mob,0,false))
			{
				final int targetWeight = ((Physical)msg.target()).phyStats().weight();
				if((targetWeight<(mob.phyStats().weight()*3))
				&&(mob.location()!=null))
				{
					if(msg.target() instanceof Food)
						return super.okMessage(myHost,msg);
					else
					if(!(msg.target() instanceof Item))
						return super.okMessage(myHost,msg);
					else
					if((!CMLib.flags().isGettable((Item)msg.target()))
					||(msg.target().displayText().length()==0)
					||(!CMLib.utensils().canBePlayerDestroyed(mob, (Item)msg.target(), false)))
					{
						mob.tell(L("You can not eat @x1.",((Item)msg.target()).name(mob)));
						return false;
					}
					if((msg.target() instanceof Container)
					&&(((Container)msg.target()).getContents().size()>0))
					{
						mob.tell(L("You need to get all the equipment out of @x1 first.",((Item)msg.target()).name(mob)));
						return false;
					}
	
					msg.modify(msg.source(),msg.target(),msg.tool(),
							   msg.sourceCode()|CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS,msg.sourceMessage(),
							   CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_MALICIOUS,msg.targetMessage(),
							   msg.othersCode()|CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS,msg.othersMessage());
				}
				else
				{
					mob.tell(L("@x1 is just too large for you to eat!",((Physical)msg.target()).name(mob)));
					return false;
				}
			}
			else
			{
				mob.tell(L("You failed to eat @x1.",((Physical)msg.target()).name(mob)));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
		{
			super.executeMsg(myHost,msg);
			return;
		}

		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_EAT)
		&&(msg.target() instanceof DeadBody))
		{
			final boolean hungry=mob.curState().getHunger()<=0;
			if((!hungry)
			&&(mob.curState().getHunger()>=mob.maxState().maxHunger(mob.baseWeight()))
			&&(CMLib.dice().roll(1,100,0)==1)
			&&(!CMLib.flags().isGolem(msg.source()))
			&&(msg.source().fetchEffect("Disease_Obesity")==null)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
			{
				final Ability A=CMClass.getAbility("Disease_Obesity");
				if ((A != null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
				{
					A.invoke(mob, mob, true, 0);
				}
			}
			final boolean full=!mob.curState().adjHunger(CMProps.getIntVar(CMProps.Int.HUNGER_FULL),mob.maxState().maxHunger(mob.baseWeight()));
			if((hungry)&&(mob.curState().getHunger()>0))
				mob.tell(L("You are no longer hungry."));
			else
			if(full)
				mob.tell(L("You are full."));
			msg.target().destroy();
			if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				mob.location().recoverRoomStats();
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		return true;
	}
}
