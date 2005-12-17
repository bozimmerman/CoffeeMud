package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2005 Lee H. Fox

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
	public String ID(){return "DrowPriestess";}
	private int spellDown=3;
	private int darkDown=4;
	private int magicResistance = 50;

	public DrowPriestess()
	{
		super();

		baseEnvStats().setLevel(CMLib.dice().roll(4,6,1));

        magicResistance = 50 + baseEnvStats().level() * 2;

		// ===== set the basics
		Username="a Drow priestess";
		setDescription("a Drow priestess");
		setDisplayText("A Drow priestess wants to see you dead.");

		Weapon w=CMClass.getWeapon("Mace");
		if(w!=null)
		{
			w.wearAt(Item.WIELD);
			this.addInventory(w);
		}

		baseEnvStats().setArmor(40);

		baseState.setHitPoints(CMLib.dice().roll(baseEnvStats().level(),20,baseEnvStats().level()));
		setMoney(CMLib.dice().roll(4,10,0) * 25);
		baseEnvStats.setWeight(70 + CMLib.dice().roll(3,6,2));
		baseCharStats.setStat(CharStats.GENDER,'F');

		setWimpHitPoint(1);

		baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK | EnvStats.CAN_SEE_INFRARED);
		baseEnvStats().setSpeed(1.0);

		baseCharStats().setStat(CharStats.STRENGTH,12 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.INTELLIGENCE,14 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.WISDOM,13 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.DEXTERITY,15 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.CONSTITUTION,12 + CMLib.dice().roll(1,6,0));
		baseCharStats().setStat(CharStats.CHARISMA,13 + CMLib.dice().roll(1,6,0));
		baseCharStats().setCurrentClass(CMClass.getCharClass("Cleric"));
	    baseCharStats().setMyRace(CMClass.getRace("Elf"));
		baseCharStats().getMyRace().startRacing(this,false);

        addNaturalAbilities();

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

    public void addNaturalAbilities()
    {
        Ability dark=CMClass.getAbility("Spell_Darkness");
		if(dark==null) return;


        dark.setProfficiency(100);
		dark.setBorrowed(this,true);
        this.addAbility(dark);

        Ability p1 =CMClass.getAbility("Prayer_ProtGood");
        p1.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p1.setBorrowed(this,true);
        this.addAbility(p1);

        Ability p2 =CMClass.getAbility("Prayer_CauseLight");
        p2.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p2.setBorrowed(this,true);
        this.addAbility(p2);

        Ability p3 =CMClass.getAbility("Prayer_CauseSerious");
        p3.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p3.setBorrowed(this,true);
        this.addAbility(p3);

        Ability p4 =CMClass.getAbility("Prayer_Curse");
        p4.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p4.setBorrowed(this,true);
        this.addAbility(p4);

        Ability p5 =CMClass.getAbility("Prayer_Paralyze");
        p5.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p5.setBorrowed(this,true);
        this.addAbility(p5);

        Ability p6 =CMClass.getAbility("Prayer_DispelGood");
        p6.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p6.setBorrowed(this,true);
        this.addAbility(p6);

        Ability p7 =CMClass.getAbility("Prayer_Plague");
        p7.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p7.setBorrowed(this,true);
        this.addAbility(p7);

        Ability p8 =CMClass.getAbility("Prayer_CauseCritical");
        p8.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p8.setBorrowed(this,true);
        this.addAbility(p8);

        Ability p9 =CMClass.getAbility("Prayer_Blindness");
        p9.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p9.setBorrowed(this,true);
        this.addAbility(p9);

        Ability p10 =CMClass.getAbility("Prayer_BladeBarrier");
        p10.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p10.setBorrowed(this,true);
        this.addAbility(p10);

        Ability p11 =CMClass.getAbility("Prayer_Hellfire");
        p11.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p11.setBorrowed(this,true);
        this.addAbility(p11);

        Ability p12 =CMClass.getAbility("Prayer_UnholyWord");
        p12.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p12.setBorrowed(this,true);
        this.addAbility(p12);

        Ability p13 =CMClass.getAbility("Prayer_Deathfinger");
        p13.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p13.setBorrowed(this,true);
        this.addAbility(p13);

        Ability p14 =CMClass.getAbility("Prayer_Harm");
        p14.setProfficiency(CMLib.dice().roll(5, 10, 50));
		p14.setBorrowed(this,true);
        this.addAbility(p14);

    }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		boolean retval = super.okMessage(myHost,msg);

		if((msg.amITarget(this))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
		{
            if(CMLib.dice().rollPercentage() <= magicResistance)
            {
	            msg.source().tell("The drow priestess resisted your spell!");
	            return false;
            }
        }
        return retval;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==MudHost.TICK_MOB))
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
        if(CMLib.dice().rollPercentage() < 70)
        {
            prayer = fetchAbility(CMLib.dice().roll(1,numLearnedAbilities(),-1));
            while((prayer==null)||(this.baseEnvStats().level() < CMLib.ableMapper().lowestQualifyingLevel(prayer.ID())))
				prayer = fetchAbility(CMLib.dice().roll(1,numLearnedAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureSerious");
            prayer.setProfficiency(CMLib.dice().roll(5, 10, 50));
        }

		if(prayer!=null)
		    return prayer.invoke(this,null,false,0);
        return false;

    }

	protected boolean castDarkness()
	{
		if(this.location()==null)
			return true;
		if(CMLib.flags().isInDark(this.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setProfficiency(100);
		if(this.fetchAbility(dark.ID())==null)
			this.addAbility(dark);
		else
			dark =this.fetchAbility(dark.ID());

		if(dark!=null) dark.invoke(this,null,false,0);
		return true;
	}




}
