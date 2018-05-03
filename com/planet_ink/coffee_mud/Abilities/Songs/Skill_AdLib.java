package com.planet_ink.coffee_mud.Abilities.Songs;
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

public class Skill_AdLib extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_AdLib";
	}

	private final static String localizedName = CMLib.lang().L("Ad Lib");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Waiting to Ad Lib @x1)",adLibbingM==null?"no one":adLibbingM.name(invoker()));
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
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"ADLIB"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_THEATRE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}
	
	protected MOB adLibbingM = null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if(R==null)
			{
				unInvoke();
				return false;
			}
			if((!R.isInhabitant(adLibbingM))||(!CMLib.flags().canBeSeenBy(adLibbingM,mob)))
			{
				unInvoke();
				return false;
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if((msg.source()==adLibbingM)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(msg.tool() instanceof Ability)
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(CMLib.flags().canBeSeenBy(msg.source(),mob))
		&&(msg.source().fetchAbility(msg.tool().ID())!=null))
		{
			final boolean hasAble=(mob.fetchAbility(msg.tool().ID())!=null);
			final int lowestLevel=CMLib.ableMapper().lowestQualifyingLevel(msg.tool().ID());
			int myLevel=0;
			if(hasAble)
				myLevel=adjustedLevel(mob,0)-lowestLevel+1;
			final int lvl=(mob.phyStats().level()/3)+getXLEVELLevel(mob);
			if(myLevel<lvl)
				myLevel=lvl;
			if((!hasAble)&&(lowestLevel<=myLevel))
			{
				final Ability A=(Ability)msg.tool().copyOf();
				if(msg.target()  instanceof Physical)
				{
					mob.location().show(mob,msg.target(),A,CMMsg.MSG_OK_VISUAL,L("<S-NAME> attempt(s) to ad-lib <O-NAME> at <T-NAME>!"));
					A.invoke(mob, new XVector<String>(msg.target().Name()), (Physical)msg.target(), false, 0);
				}
				else
				{
					mob.location().show(mob,msg.target(),A,CMMsg.MSG_OK_VISUAL,L("<S-NAME> attempt(s) to ad-lib <O-NAME>!"));
					A.invoke(mob, new XVector<String>(), null, false, 0);
				}
				unInvoke();
			}
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob, commands, givenTarget);
		if((target==null)||(target==mob))
			return false;
		
		final Room R=mob.location();
		if(R==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> watch(es) <T-NAME> and prepares to ad-lib."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				Skill_AdLib A=(Skill_AdLib)beneficialAffect(mob,mob,asLevel,3);
				if(A!=null)
					A.adLibbingM=target;
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> point(s) around, confusing <S-HIM-HERSELF>."));

		return success;
	}

}
