package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class DrowPriestess extends DrowElf
{
	private int spellDown=3;
	private int darkDown=4;
	private int magicResistance = 50;

	public DrowPriestess()
	{
		super();

		baseEnvStats().setLevel(Dice.roll(4,6,1));

        magicResistance = 50 + baseEnvStats().level() * 2;

		// ===== set the basics
		Username="a Drow priestess";
		setDescription("a Drow priestess");
		setDisplayText("A Drow priestess wants to see you dead.");

		Weapon w=(Weapon)CMClass.getWeapon("Mace");
		if(w!=null)
		{
			w.wearAt(Item.WIELD);
			this.addInventory(w);
		}

		baseEnvStats().setArmor(40);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));
		setMoney(Dice.roll(4,10,0) * 25);
		baseEnvStats.setWeight(70 + Dice.roll(3,6,2));
		baseCharStats.setStat(CharStats.GENDER,(int)'F');

		setWimpHitPoint(1);

		baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK | EnvStats.CAN_SEE_INFRARED);
		baseEnvStats().setSpeed(1.0);

		baseCharStats().setStat(CharStats.STRENGTH,12 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.INTELLIGENCE,14 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.WISDOM,13 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.DEXTERITY,15 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.CONSTITUTION,12 + Dice.roll(1,6,0));
		baseCharStats().setStat(CharStats.CHARISMA,13 + Dice.roll(1,6,0));
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

	public boolean okAffect(Affect affect)
	{
		boolean retval = super.okAffect(affect);

		if((affect.amITarget(this))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_CAST_SPELL))
		{
            if(Dice.rollPercentage() <= magicResistance)
            {
	            affect.source().tell("The drow priestess resisted your spell!");
	            return false;
            }
        }
        return retval;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==Host.MOB_TICK))
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
        if(Dice.rollPercentage() < 70)
        {
            prayer = (Ability) this.fetchAbility(Dice.roll(1,numAbilities(),-1));
            while((prayer==null)||(this.baseEnvStats().level() < CMAble.lowestQualifyingLevel(prayer.ID())))
				prayer = (Ability) this.fetchAbility(Dice.roll(1,numAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureSerious");
            prayer.setProfficiency(Dice.roll(5, 10, 50));
        }

        boolean prayerSuccess = prayer.invoke(this,null,false);
        return prayerSuccess;

    }

	protected boolean castDarkness()
	{
		if(this.location()==null)
			return true;
		if(Sense.isInDark(this.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setProfficiency(100);
		if(this.fetchAbility(dark.ID())==null)
		   this.addAbility(dark);
		else
			dark =this.fetchAbility(dark.ID());

		dark.invoke(this,null,false);
		return true;
	}


	public Environmental newInstance()
	{
		return new DrowPriestess();
	}

}
