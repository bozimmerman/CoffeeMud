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
   Copyright 2000-2018 Lee H. Fox

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
public class DrowPriestess extends DrowElf
{
	@Override
	public String ID()
	{
		return "DrowPriestess";
	}

	protected int spellDown=3;
	protected int magicResistance = 50;

	public DrowPriestess()
	{
		super();

		darkDown = 4;

		basePhyStats().setLevel(CMLib.dice().roll(4,6,1));

		magicResistance = 50 + basePhyStats().level() * 2;

		// ===== set the basics
		username="a Drow priestess";
		setDescription("a Drow priestess");
		setDisplayText("A Drow priestess wants to see you dead.");

		final Weapon w=CMClass.getWeapon("Mace");
		if(w!=null)
		{
			w.wearAt(Wearable.WORN_WIELD);
			this.addItem(w);
		}

		basePhyStats().setArmor(40);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));
		setMoney(CMLib.dice().roll(4,10,0) * 25);
		basePhyStats.setWeight(70 + CMLib.dice().roll(3,6,2));
		baseCharStats.setStat(CharStats.STAT_GENDER,'F');

		setWimpHitPoint(1);

		basePhyStats().setSensesMask(PhyStats.CAN_SEE_DARK | PhyStats.CAN_SEE_INFRARED);
		basePhyStats().setSpeed(1.0);

		baseCharStats().setStat(CharStats.STAT_STRENGTH,12 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,14 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.STAT_WISDOM,13 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.STAT_DEXTERITY,15 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.STAT_CONSTITUTION,12 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.STAT_CHARISMA,13 + CMLib.dice().roll(1,6,0));
		baseCharStats().setCurrentClass(CMClass.getCharClass("Cleric"));
		baseCharStats().setMyRace(CMClass.getRace("Elf"));
		baseCharStats().getMyRace().startRacing(this,false);

		addNaturalAbilities();

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	public void addNaturalAbilities()
	{
		final Ability dark=CMClass.getAbility("Spell_Darkness");
		if(dark==null)
			return;

		dark.setProficiency(100);
		dark.setSavable(false);
		this.addAbility(dark);

		final Ability p1 =CMClass.getAbility("Prayer_ProtGood");
		p1.setProficiency(CMLib.dice().roll(5, 10, 50));
		p1.setSavable(false);
		this.addAbility(p1);

		final Ability p2 =CMClass.getAbility("Prayer_CauseLight");
		p2.setProficiency(CMLib.dice().roll(5, 10, 50));
		p2.setSavable(false);
		this.addAbility(p2);

		final Ability p3 =CMClass.getAbility("Prayer_CauseSerious");
		p3.setProficiency(CMLib.dice().roll(5, 10, 50));
		p3.setSavable(false);
		this.addAbility(p3);

		final Ability p4 =CMClass.getAbility("Prayer_Curse");
		p4.setProficiency(CMLib.dice().roll(5, 10, 50));
		p4.setSavable(false);
		this.addAbility(p4);

		final Ability p5 =CMClass.getAbility("Prayer_Paralyze");
		p5.setProficiency(CMLib.dice().roll(5, 10, 50));
		p5.setSavable(false);
		this.addAbility(p5);

		final Ability p6 =CMClass.getAbility("Prayer_DispelGood");
		p6.setProficiency(CMLib.dice().roll(5, 10, 50));
		p6.setSavable(false);
		this.addAbility(p6);

		final Ability p7 =CMClass.getAbility("Prayer_Plague");
		p7.setProficiency(CMLib.dice().roll(5, 10, 50));
		p7.setSavable(false);
		this.addAbility(p7);

		final Ability p8 =CMClass.getAbility("Prayer_CauseCritical");
		p8.setProficiency(CMLib.dice().roll(5, 10, 50));
		p8.setSavable(false);
		this.addAbility(p8);

		final Ability p9 =CMClass.getAbility("Prayer_Blindness");
		p9.setProficiency(CMLib.dice().roll(5, 10, 50));
		p9.setSavable(false);
		this.addAbility(p9);

		final Ability p10 =CMClass.getAbility("Prayer_BladeBarrier");
		p10.setProficiency(CMLib.dice().roll(5, 10, 50));
		p10.setSavable(false);
		this.addAbility(p10);

		final Ability p11 =CMClass.getAbility("Prayer_Hellfire");
		p11.setProficiency(CMLib.dice().roll(5, 10, 50));
		p11.setSavable(false);
		this.addAbility(p11);

		final Ability p12 =CMClass.getAbility("Prayer_UnholyWord");
		p12.setProficiency(CMLib.dice().roll(5, 10, 50));
		p12.setSavable(false);
		this.addAbility(p12);

		final Ability p13 =CMClass.getAbility("Prayer_Deathfinger");
		p13.setProficiency(CMLib.dice().roll(5, 10, 50));
		p13.setSavable(false);
		this.addAbility(p13);

		final Ability p14 =CMClass.getAbility("Prayer_Harm");
		p14.setProficiency(CMLib.dice().roll(5, 10, 50));
		p14.setSavable(false);
		this.addAbility(p14);

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final boolean retval = super.okMessage(myHost,msg);

		if((msg.amITarget(this))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
		{
			if(CMLib.dice().rollPercentage() <= magicResistance)
			{
				msg.source().tell(L("The drow priestess resisted your spell!"));
				return false;
			}
		}
		return retval;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==Tickable.TICKID_MOB))
		{
			if (isInCombat())
			{
				if((--spellDown)<=0)
				{
					spellDown=3;
					castSpell();
				}
				if((--darkDown)<=0)
				{
					darkDown=4;
					castDarkness();
				}
			}

		}
		return super.tick(ticking,tickID);
	}

	public boolean castSpell()
	{
		Ability prayer = null;
		int tries = 10;
		if(CMLib.dice().rollPercentage() < 70)
		{
			prayer = fetchRandomAbility();
			while(((--tries)>0)&&((prayer==null)||(this.basePhyStats().level() < CMLib.ableMapper().lowestQualifyingLevel(prayer.ID()))))
				prayer = fetchRandomAbility();
		}
		else
		{
			prayer = CMClass.getAbility("Prayer_CureSerious");
			prayer.setProficiency(CMLib.dice().roll(5, 10, 50));
		}

		if(prayer!=null)
			return prayer.invoke(this,null,false,0);
		return false;

	}

	@Override
	protected boolean castDarkness()
	{
		if(this.location()==null)
			return true;
		if(CMLib.flags().isInDark(this.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setProficiency(100);
		if(this.fetchAbility(dark.ID())==null)
			this.addAbility(dark);
		else
			dark =this.fetchAbility(dark.ID());

		if(dark!=null)
			dark.invoke(this,null,false,0);
		return true;
	}

}
