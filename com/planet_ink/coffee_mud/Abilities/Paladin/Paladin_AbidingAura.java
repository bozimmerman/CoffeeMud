package com.planet_ink.coffee_mud.Abilities.Paladin;
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

public class Paladin_AbidingAura extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_AbidingAura";
	}

	private final static String localizedName = CMLib.lang().L("Abiding Aura");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_HOLYPROTECTION;
	}
	
	protected Map<MOB,Runnable> abiding=new SHashtable<MOB,Runnable>();
	
	public Paladin_AbidingAura()
	{
		super();
		paladinsGroup=new HashSet<MOB>();
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(invoker!=null)
		&&(CMLib.flags().isGood(invoker))
		&&(super.paladinsGroup.contains(msg.source())))
		{
			final MOB mob=msg.source();
			final Room startRoom=mob.getStartRoom();
			if((startRoom!=null)&&(super.proficiencyCheck(mob, 0, false)))
			{
				if(mob.fetchAbility("Dueling")!=null)
					return super.okMessage(host,msg);
				final Room oldRoom=mob.location();
				mob.curState().setHitPoints(1);
				oldRoom.show(invoker,mob,CMMsg.MSG_OK_VISUAL,L("<T-YOUPOSS> death is prevented by <S-YOUPOSS> abiding aura!"));
				if(!abiding.containsKey(msg.source()))
				{
					final Set<MOB> paladinsGroup=super.paladinsGroup;
					final MOB killerM=(msg.tool() instanceof MOB)?(MOB)msg.tool():null;
					final Runnable runnable=new Runnable()
					{
						@Override
						public void run()
						{
							abiding.remove(mob);
							if(!mob.amDead())
							{
								if(mob.curState().getHitPoints() < CMath.mul(mob.maxState().getHitPoints(), 0.10))
								{
									paladinsGroup.remove(mob);
									CMLib.combat().postDeath(killerM, mob, null);
								}
							}
						}
					};
					super.helpProficiency(mob, 0);
					abiding.put(mob, runnable);
					CMLib.threads().scheduleRunnable(runnable, 10000);
					mob.tell(L("^xThe aura will protect you for only 10 seconds, after which you must be above 10% hit points to survive.^?^."));
				}
				return false;
			}
		}
		return super.okMessage(host,msg);
	}
}
