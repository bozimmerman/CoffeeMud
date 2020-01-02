package com.planet_ink.coffee_mud.Abilities.Diseases;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Disease_RadiationSickness extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_RadiationSickness";
	}

	private final static String localizedName = CMLib.lang().L("Radiation Sickness");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Radiation Sickness)");

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

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 6;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 999999;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 8;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return "";
	}

	@Override
	protected String DISEASE_START()
	{
		return "";
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int spreadBitmap()
	{
		return SPREAD_GET;
	}

	protected final long conTickBase = CMProps.getTicksPerMudHour() * 150;
	protected volatile int conTickUp = 0;
	protected volatile int conDown = 2;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;
		final MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		conTickUp++;

		if(!mob.amDead())
		{
			if(conTickUp > conTickBase)
			{
				conTickUp=0;
				conDown += 2;
				mob.recoverCharStats();
			}
			if(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<=0)
			{
				CMLib.combat().postDeath(invoker==null?mob:invoker, mob,null);
				return false;
			}
			if((--diseaseTick)<=0)
			{
				diseaseTick=DISEASE_DELAY();
				if(CMLib.dice().rollPercentage()<5)
				{
					MOB diseaser=invoker;
					if(diseaser==null)
						diseaser=mob;
					if(mob.fetchEffect("Disease_Migraines")==null)
					{
						final Ability A=CMClass.getAbility("Disease_Migraines");
						if(A!=null)
							A.invoke(diseaser, mob, false, 0);
					}
					else
					if(mob.fetchEffect("Disease_Diarrhea")==null)
					{
						final Ability A=CMClass.getAbility("Disease_Diarrhea");
						if(A!=null)
							A.invoke(diseaser, mob, false, 0);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-conDown);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((affected instanceof Item)
		&&(givenTarget instanceof MOB))
		{
			final Item I=(Item)affected;
			final MOB M=(MOB)givenTarget;
			Item checkI=I.container();
			while((checkI!=null)&&(checkI!=I))
			{
				if(checkI.material()==RawMaterial.RESOURCE_LEAD)
					return false;
				checkI=checkI.container();
			}
			final List<Item> worn=M.fetchWornItems(Wearable.WORN_HANDS,(short)-2048,(short)0);
			for(final Item checkI2 : worn)
			{
				if(checkI2.material()==RawMaterial.RESOURCE_LEAD)
					return false;
			}
		}
		return super.invoke(mob, commands, givenTarget, auto, asLevel);
	}
}
