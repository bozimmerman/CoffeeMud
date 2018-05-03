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
   Copyright 2017-2018 Bo Zimmerman

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
public class Skill_Skillcraft extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Skillcraft";
	}

	private final static String	localizedName	= CMLib.lang().L("Skillcraft");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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
		return Ability.ACODE_SKILL | Ability.DOMAIN_STREETSMARTS;
	}

	public String	lastID	= "";

	private final int[] localTypes = new int[] { Ability.ACODE_SKILL };
	
	public int[] craftTypes()
	{
		return localTypes;
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		if(!super.autoInvocation(mob, force))
			return false;
		if(text().length()>0)
		{
			final List<String> abilities=CMParms.parseCommas(text(), true);
			setMiscText("");
			final MOB casterM=CMClass.getFactoryMOB();
			final Ability A=(Ability)copyOf();
			for(final String ID : abilities)
			{
				A.setMiscText(ID);
				lastID=ID;
				final Ability castA=CMClass.getAbility(ID);
				if(castA!=null)
					executeMsg(mob, CMClass.getMsg(mob,casterM,castA,CMMsg.MSG_OK_VISUAL,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(!msg.amISource(mob))
		&&(msg.tool() instanceof Ability)
		&&(CMParms.contains(this.craftTypes(), ((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES))
		&&(!lastID.equalsIgnoreCase(msg.tool().ID()))
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(CMLib.flags().canBeSeenBy(msg.source(),mob))
		&&(msg.source().fetchAbility(msg.tool().ID())!=null))
		{
			final boolean hasAble=(mob.fetchAbility(ID())!=null);
			final int lowestLevel=CMLib.ableMapper().lowestQualifyingLevel(msg.tool().ID());
			int myLevel=0;
			if(hasAble)
				myLevel=adjustedLevel(mob,0)-lowestLevel+1;
			final int lvl=(mob.phyStats().level()/3)+getXLEVELLevel(mob);
			if(myLevel<lvl)
				myLevel=lvl;
			if(((!hasAble)||proficiencyCheck(mob,0,false))
			&&(lowestLevel<=myLevel))
			{
				final Ability A=(Ability)copyOf();
				A.setMiscText(msg.tool().ID());
				lastID=msg.tool().ID();
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),A,CMMsg.MSG_OK_VISUAL,L("<T-NAME> do(es) '@x1'.",msg.tool().name()),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				helpProficiency(mob, 0);
			}
		}
	}
}
