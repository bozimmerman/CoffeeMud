package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Ranger_Enemy1 extends StdAbility
{
	@Override
	public String ID()
	{
		return "Ranger_Enemy1";
	}

	private final static String localizedName = CMLib.lang().L("Favored Enemy 1");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Enemy of the "+text()+")");
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
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public String text()
	{
		if(miscText.length()==0)
		{
			if(!(affected instanceof MOB))
				return super.text();
			final MOB mob=(MOB)affected;
			final Vector<String> choices=new Vector<String>();
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				final Race R=r.nextElement();
				if((!choices.contains(R.racialCategory()))
				&&(CMath.bset(R.availabilityCode(),Area.THEME_FANTASY)))
					choices.addElement(R.racialCategory());
			}
			for(int a=0;a<mob.numAbilities();a++)
			{
				final Ability A=mob.fetchAbility(a);
				if((A instanceof Ranger_Enemy1)
				   &&(((Ranger_Enemy1)A).miscText.length()>0))
					choices.remove(((Ranger_Enemy1)A).miscText);
			}
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof Ranger_Enemy1)
				   &&(((Ranger_Enemy1)A).miscText.length()>0))
					choices.remove(((Ranger_Enemy1)A).miscText);
			}
			choices.remove("Unique");
			choices.remove("Unknown");
			choices.remove(mob.charStats().getMyRace().racialCategory());
			miscText=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			for(int a=0;a<mob.numAbilities();a++)
			{
				final Ability A=mob.fetchAbility(a);
				if((A!=null)&&(A.ID().equals(ID())))
					((Ranger_Enemy1)A).miscText=miscText;
			}
			for(int a=0;a<mob.numEffects();a++) // personal
			{
				final Ability A=mob.fetchEffect(a);
				if((A!=null)&&(A.ID().equals(ID())))
					((Ranger_Enemy1)A).miscText=miscText;
			}
		}
		return super.text();
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		final MOB victim=mob.getVictim();
		if((victim!=null)&&(victim.charStats().getMyRace().racialCategory().equals(text())))
		{
			final int level=1+adjustedLevel(mob,0);
			final double damBonus=CMath.mul(CMath.div(proficiency(),100.0),level);
			final double attBonus=CMath.mul(CMath.div(proficiency(),100.0),3*level);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(attBonus));
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(damBonus));
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).charStats().getMyRace().racialCategory().equals(text()))
		&&(CMLib.dice().roll(1, 10, 0)==1))
			helpProficiency(msg.source(), 0);
		return super.okMessage(myHost, msg);
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
