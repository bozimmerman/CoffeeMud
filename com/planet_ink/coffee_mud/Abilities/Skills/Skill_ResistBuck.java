package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Properties.Prop_RideResister;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
public class Skill_ResistBuck extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ResistBuck";
	}

	private final static String localizedName = CMLib.lang().L("Bronco Busting");

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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FITNESS;
	}

	protected volatile MOB curMount = null;
	protected volatile int mountTicks = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((curMount != null)
		&&(affected instanceof MOB))
		{
			final MOB mountM = curMount;
			final MOB mob=(MOB)affected;
			if((mob.riding() == mountM)
			&&(mountTicks>0))
			{
				if(--mountTicks <= 0)
				{
					if(!mountM.basePhyStats().isAmbiance("@NOAUTOBUCK"))
						mountM.basePhyStats().addAmbiance("@NOAUTOBUCK");
					final Behavior B = mountM.fetchBehavior("Skill_Buck");
					if(B != null)
						mountM.delBehavior(B);
					mountM.recoverPhyStats();
				}
			}
			else
			{
				curMount = null;
				mountTicks = 0;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==affected)
		&&(msg.tool() instanceof Ability)
		&&(((Ability)msg.tool()).ID().equals("Skill_Buck"))
		&&(affected instanceof MOB))
		{
			final MOB mob = (MOB)affected;
			final int chance = super.getXLEVELLevel(mob) + (int)Math.round(100.0*CMath.mul(proficiency(), 0.9));
			if(CMLib.dice().rollPercentage()<=chance)
			{
				final Room R =(mob).location();
				if(R != null)
				{
					if((this.curMount != msg.source())
					&&(msg.source() instanceof Rideable)
					&&(((Rideable)msg.source()).amRiding(mob)))
					{
						this.curMount = msg.source();
						this.mountTicks = 30 - super.getXLEVELLevel(mob);
					}
					super.helpProficiency(mob, 0);
					if(R.show(mob, msg.source(), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> resist(s) <T-YOUPOSS> attempt to buck <S-HIM-HER>.")))
						return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

}
