package com.planet_ink.coffee_mud.CharClasses;
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
public class SpecialistMage extends Mage
{
	@Override
	public String ID()
	{
		return "SpecialistMage";
	}

	private final static String localizedStaticName = CMLib.lang().L("Specialist Mage");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Mage";
	}

	public int domain()
	{
		return Ability.DOMAIN_ABJURATION;
	}

	public int opposed()
	{
		return Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				final int level=CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID());
				if((!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID()))
				&&(level>0)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
				{
					final boolean secret=CMLib.ableMapper().getSecretSkill(ID(),true,A.ID());
					if((A.classificationCode()&Ability.ALL_DOMAINS)==opposed())
					{
						if(CMLib.ableMapper().getDefaultGain(ID(),true,A.ID()))
							CMLib.ableMapper().addCharAbilityMapping(ID(),level,A.ID(),0,"",false,secret);
						else
							CMLib.ableMapper().delCharAbilityMapping(ID(),A.ID());
					}
					else
					if((A.classificationCode()&Ability.ALL_DOMAINS)==domain()&&(!secret))
						CMLib.ableMapper().addCharAbilityMapping(ID(),level,A.ID(),25,true);
					else
						CMLib.ableMapper().addCharAbilityMapping(ID(),level,A.ID(),0,"",false,secret);
				}
			}
		}
	}

	@Override
	public int availabilityCode()
	{
		return 0;
	}

	@Override
	public String getOtherBonusDesc()
	{
		final String chosen = CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[domain() >> 5].replace('_', ' '));
		return L("At 5th level, receives bonus damage from @x1 as levels advance.  "
				+ "At 10th level, receives double duration on your @x1 magic, "
				+ "and half duration from malicious @x1 magic.",chosen);
	}

	@Override
	public String getOtherLimitsDesc()
	{
		final String opposed = CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[opposed() >> 5].replace('_', ' '));
		return L("Unable to cast @x1 spells.  Receives penalty damage from @x1 as levels advance.  "
				+ "Receives double duration from malicious @x1 magic, half duration on other @x1"
				+ " effects.",opposed);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB myChar=(MOB)myHost;
		if((msg.tool()==null)||(!(msg.tool() instanceof Ability)))
			return super.okMessage(myChar,msg);
		final int domain=((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS;
		if(msg.amISource(myChar)
		&&(myChar.isMine(msg.tool())))
		{
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(domain==opposed())
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,msg.tool().ID())))
			{
				if(CMLib.dice().rollPercentage()>
					(myChar.charStats().getStat(CharStats.STAT_INTELLIGENCE)*((myChar.charStats().getCurrentClass().ID().equals(ID()))?1:2)))
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fizzle(s) a spell."));
					return false;
				}
			}
			if((myChar.charStats().getClassLevel(this)>=5)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==domain()))
			{
				int classLevel=myChar.charStats().getClassLevel(this);
				if(classLevel>30)
					classLevel=30;
				msg.setValue((int)Math.round(CMath.mul(msg.value(),1.0+CMath.mul(0.01,classLevel))));
			}
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool() instanceof Ability))
		{
			int classLevel=myChar.charStats().getClassLevel(this);
			if(classLevel>30)
				classLevel=30;
			if((domain==domain())
			&&(classLevel>=5))
				msg.setValue((int)Math.round(CMath.div((msg.value()),1.0+CMath.mul(0.01,classLevel))));
			else
			if(domain==opposed())
				msg.setValue((int)Math.round(CMath.mul((msg.value()),1.0+CMath.mul(0.01,classLevel))));
		}

		return super.okMessage(myChar,msg);
	}

	@Override
	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null)
			return duration;
		final boolean lessTen=myChar.charStats().getClassLevel(this)<10;

		final int domain=skill.classificationCode()&Ability.ALL_DOMAINS;
		if((skill.invoker()==myChar)
		||(skill.abstractQuality()!=Ability.QUALITY_MALICIOUS))
		{
			if(domain==opposed())
				return duration/2;
			else
			if(domain==domain())
			{
				if(!lessTen)
				{
					if(duration>=(Integer.MAX_VALUE/2))
						return duration;
					return duration*2;
				}
			}
		}
		else
		{
			if(domain==opposed())
			{
				if(duration>=(Integer.MAX_VALUE/2))
					return duration;
				return duration*2;
			}
			else
			if(domain==domain())
			{
				if(!lessTen)
					return duration/2;
			}
		}
		return duration;
	}
}
