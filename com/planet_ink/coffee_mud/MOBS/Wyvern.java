package com.planet_ink.coffee_mud.MOBS;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class Wyvern extends StdMOB
{
	@Override
	public String ID()
	{
		return "Wyvern";
	}

	protected int stingDown=5;

	public Wyvern()
	{
		super();
		final Random randomizer = new Random(System.currentTimeMillis());

		username="a wyvern";
		setDescription("A distant cousin to the dragon, a wyvern is 35-foot-long dark brown to gray body of the wyvern is half tail. Its leathery batlike wings are over 50 feet from tip to tip..");
		setDisplayText("A mean looking wyvern is here.");
		CMLib.factions().setAlignment(this,Faction.Align.NEUTRAL);
		setMoney(0);
		setWimpHitPoint(2);

		basePhyStats().setWeight(2000 + Math.abs(randomizer.nextInt() % 550));

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STAT_STRENGTH,18);
		baseCharStats().setStat(CharStats.STAT_DEXTERITY,13);
		baseCharStats().setMyRace(CMClass.getRace("Wyvern"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setDamage(16);
		basePhyStats().setSpeed(2.0);
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(7);
		basePhyStats().setArmor(70);
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_FLYING);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==Tickable.TICKID_MOB))
		{
			if((--stingDown)<=0)
			{
				stingDown=5;
				if (isInCombat())
					sting();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void recoverCharStats()
	{
		super.recoverCharStats();
		charStats().setStat(CharStats.STAT_SAVE_POISON,charStats().getStat(CharStats.STAT_SAVE_POISON)+100);
	}

	protected boolean sting()
	{
		if (CMLib.flags().isAliveAwakeMobileUnbound(this,true)&&
			(CMLib.flags().canHear(this)||CMLib.flags().canSee(this)||CMLib.flags().canSmell(this)))
		{
			final MOB target = getVictim();
			// ===== if it is less than three so roll for it
			final int roll = (int)Math.round(Math.random()*99);

			// ===== check the result
			if (roll<20)
			{
				// Sting was successful
 				final CMMsg msg=CMClass.getMsg(this, target, null, CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_POISON, L("^F^<FIGHT^><S-NAME> sting(s) <T-NAMESELF>!^</FIGHT^>^?"));
				CMLib.color().fixSourceFightColor(msg);
				if(location().okMessage(target,msg))
				{
					this.location().send(target,msg);
					if(msg.value()<=0)
					{
						final Ability poison = CMClass.getAbility("Poison");
						if(poison!=null)
							poison.invoke(this, target, true,0);
					}
				}
			}
		}
		return true;
	}

}
