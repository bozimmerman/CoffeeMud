package com.planet_ink.coffee_mud.Races;
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

import java.util.List;
import java.util.Vector;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Lich extends Undead
{
	@Override
	public String ID()
	{
		return "Lich";
	}

	private final static String localizedStaticName = CMLib.lang().L("Lich");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_PIERCE, affectableStats.getStat(CharStats.STAT_SAVE_PIERCE)+50);
		affectableStats.setStat(CharStats.STAT_SAVE_SLASH, affectableStats.getStat(CharStats.STAT_SAVE_SLASH)+50);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,-4);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,6);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_PIERCE, affectableStats.getStat(CharStats.STAT_SAVE_PIERCE)-50);
		affectableStats.setStat(CharStats.STAT_SAVE_SLASH, affectableStats.getStat(CharStats.STAT_SAVE_SLASH)-50);
		affectableStats.adjStat(CharStats.STAT_CONSTITUTION,+4);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,-6);
	}

	@Override
	public DeadBody getCorpseContainer(final MOB mob, final Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_BONE);
		}
		return body;
	}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		return resources;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!(ticking instanceof MOB))
			return super.tick(ticking,tickID);
		final MOB myChar=(MOB)ticking;
		if((tickID==Tickable.TICKID_MOB)&&(CMLib.dice().rollPercentage()<10))
		{
			final Ability A=CMClass.getAbility("Spell_Fear");
			if(A!=null)
			{
				A.setMiscText("WEAK");
				A.invoke(myChar,null,true,0);
			}
		}
		return super.tick(myChar,tickID);
	}
}
