package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Lee H. Fox

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
public class DrowWarrior extends DrowElf
{
	public String ID(){return "DrowWarrior";}
	public int darkDown=4;
	public int fightDown=2;
	public int statCheck=3;

	public static final int CAST_DARKNESS = 1;
	public static final int FIGHTER_SKILL = 128;
    public static final int CHECK_STATUS = 129;
    private int magicResistance = 50;

	public DrowWarrior()
	{
		super();

		baseEnvStats().setLevel(Dice.roll(4,6,1));

        magicResistance = 50 + baseEnvStats().level() * 2;

		// ===== set the basics
		Username="a Drow male";
		setDescription("a Drow warrior");
		setDisplayText("A Drow warrior considers you carefully.");

        equipDrow();

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));
		setMoney(Dice.roll(4,10,0) * 25);
		baseEnvStats.setWeight(70 + Dice.roll(3,6,2));
		baseCharStats.setStat(CharStats.GENDER,'M');

		setWimpHitPoint(1);

		baseCharStats().setStat(CharStats.STRENGTH,12 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.INTELLIGENCE,14 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.WISDOM,13 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.DEXTERITY,15 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.CONSTITUTION,12 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.CHARISMA,13 + Dice.roll(1,6,0));
		baseCharStats().setCurrentClass(CMClass.getCharClass("Fighter"));
		baseCharStats().setMyRace(CMClass.getRace("Elf"));
		baseCharStats().getMyRace().startRacing(this,false);


        addNaturalAbilities();

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

    public void equipDrow()
    {
        Armor chainMail = CMClass.getArmor("DrowChainMailArmor");
		if(chainMail!=null)
		{
			chainMail.wearAt(Item.ON_TORSO);
			this.addInventory(chainMail);
		}

        Weapon mainWeapon = null;
        Weapon secondWeapon = null;

        int weaponry = Dice.roll(1,4,0);
        switch(weaponry)
        {
            case 1:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        secondWeapon = CMClass.getWeapon("DrowSword");
		        baseEnvStats().setSpeed(2.0);
                break;
            case 2:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        baseEnvStats().setSpeed(1.0);
                break;
            case 3:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        secondWeapon = CMClass.getWeapon("DrowDagger");
		        baseEnvStats().setSpeed(2.0);
                break;
            case 4:
		        mainWeapon = CMClass.getWeapon("Scimitar");
		        secondWeapon = CMClass.getWeapon("Scimitar");
		        baseEnvStats().setSpeed(2.0);
                break;
            default:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        secondWeapon = CMClass.getWeapon("DrowSword");
		        baseEnvStats().setSpeed(2.0);
                break;
        }
		if(mainWeapon!=null)
		{
		    mainWeapon.wearAt(Item.WIELD);
		    this.addInventory(mainWeapon);
			if(secondWeapon!=null)
			{
				secondWeapon.wearAt(Item.HELD);
				this.addInventory(secondWeapon);
			}
		}

    }

    public void addNaturalAbilities()
    {
        Ability dark=CMClass.getAbility("Spell_Darkness");
		if(dark==null) return;


		dark.setProfficiency(100);
		dark.setBorrowed(this,true);
        this.addAbility(dark);

        Ability p1 =CMClass.getAbility("Prayer_ProtGood");
        p1.setProfficiency(Dice.roll(5, 10, 50));
		p1.setBorrowed(this,true);
        this.addAbility(p1);

        Ability p2 =CMClass.getAbility("Prayer_CauseLight");
        p2.setProfficiency(Dice.roll(5, 10, 50));
		p2.setBorrowed(this,true);
        this.addAbility(p2);

        Ability p3 =CMClass.getAbility("Prayer_CauseSerious");
        p3.setProfficiency(Dice.roll(5, 10, 50));
		p3.setBorrowed(this,true);
        this.addAbility(p3);

        Ability p4 =CMClass.getAbility("Prayer_Curse");
        p4.setProfficiency(Dice.roll(5, 10, 50));
		p4.setBorrowed(this,true);
        this.addAbility(p4);

        Ability p5 =CMClass.getAbility("Prayer_Paralyze");
        p5.setProfficiency(Dice.roll(5, 10, 50));
		p5.setBorrowed(this,true);
        this.addAbility(p5);

        Ability p6 =CMClass.getAbility("Prayer_DispelGood");
        p6.setProfficiency(Dice.roll(5, 10, 50));
		p6.setBorrowed(this,true);
        this.addAbility(p6);

        Ability p7 =CMClass.getAbility("Prayer_Plague");
        p7.setProfficiency(Dice.roll(5, 10, 50));
		p7.setBorrowed(this,true);
        this.addAbility(p7);

        Ability p8 =CMClass.getAbility("Prayer_CauseCritical");
        p8.setProfficiency(Dice.roll(5, 10, 50));
		p8.setBorrowed(this,true);
        this.addAbility(p8);

        Ability p9 =CMClass.getAbility("Prayer_Blindness");
        p9.setProfficiency(Dice.roll(5, 10, 50));
		p9.setBorrowed(this,true);
        this.addAbility(p9);

        Ability p10 =CMClass.getAbility("Prayer_BladeBarrier");
        p10.setProfficiency(Dice.roll(5, 10, 50));
		p10.setBorrowed(this,true);
        this.addAbility(p10);

        Ability p11 =CMClass.getAbility("Prayer_Hellfire");
        p11.setProfficiency(Dice.roll(5, 10, 50));
		p11.setBorrowed(this,true);
        this.addAbility(p11);

        Ability p12 =CMClass.getAbility("Prayer_UnholyWord");
        p12.setProfficiency(Dice.roll(5, 10, 50));
		p12.setBorrowed(this,true);
        this.addAbility(p12);

        Ability p13 =CMClass.getAbility("Prayer_Deathfinger");
        p13.setProfficiency(Dice.roll(5, 10, 50));
		p13.setBorrowed(this,true);
        this.addAbility(p13);

        Ability p14 =CMClass.getAbility("Prayer_Harm");
        p14.setProfficiency(Dice.roll(5, 10, 50));
		p14.setBorrowed(this,true);
        this.addAbility(p14);

    }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		boolean retval = super.okMessage(myHost,msg);

		if((msg.amITarget(this))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
		{
            if(Dice.rollPercentage() <= magicResistance)
            {
	            msg.source().tell("The drow warrior resisted your spell!");
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
				if((--fightDown)<=0)
				{
					fightDown=2;
					useSkill();
				}
				if((--statCheck)<=0)
				{
					statCheck=3;
					checkStatus();
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

    public boolean checkStatus()
    {
        if(envStats().disposition() == EnvStats.IS_SITTING)
            envStats().setDisposition(envStats().disposition() - EnvStats.IS_SITTING);
        this.location().show(this, null, CMMsg.MSG_NOISYMOVEMENT, "<S-NAME> stand(s) up, ready for more combat.");

        return true;
    }

    public boolean useSkill()
    {
        Ability prayer = null;
        if(Dice.rollPercentage() < 70)
        {
            prayer =  this.fetchAbility(Dice.roll(1,numLearnedAbilities(),-1));
            while((prayer==null)||(this.baseEnvStats().level() < CMAble.lowestQualifyingLevel(prayer.ID())))
                prayer = this.fetchAbility(Dice.roll(1,numLearnedAbilities(),-1));
        }
        else
            prayer = CMClass.getAbility("Prayer_CureSerious");
		if(prayer!=null)
	        return prayer.invoke(this,null,false,0);
		return false;
    }

	protected boolean castDarkness()
	{
		if(this.location()==null)
			return true;
		if(Sense.isInDark(this.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setBorrowed(this,true);
		dark.setProfficiency(100);
		if(this.fetchAbility(dark.ID())==null)
		   this.addAbility(dark);
		else
			dark=this.fetchAbility(dark.ID());

		if(dark!=null) dark.invoke(this,null,false,0);
		return true;
	}




}
