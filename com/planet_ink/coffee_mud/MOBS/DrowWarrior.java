package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class DrowWarrior extends DrowElf
{
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

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(), 8, 2));
		setMoney(Dice.roll(4,10,0) * 25);
		baseEnvStats.setWeight(70 + Dice.roll(3,6,2));
		baseCharStats.setGender('M');

		setWimpHitPoint(1);

		baseCharStats().setStrength(12 + Dice.roll(1,6,0));
		baseCharStats().setIntelligence(14 + Dice.roll(1,6,0));
		baseCharStats().setWisdom(13 + Dice.roll(1,6,0));
		baseCharStats().setDexterity(15 + Dice.roll(1,6,0));
		baseCharStats().setConstitution(12 + Dice.roll(1,6,0));
		baseCharStats().setCharisma(13 + Dice.roll(1,6,0));
		baseCharStats().setMyClass(CMClass.getCharClass("Fighter"));
		baseCharStats().setMyRace(CMClass.getRace("Elf"));


        addNaturalAbilities();

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

    public void equipDrow()
    {
        Armor chainMail = (Armor)CMClass.getArmor("DrowChainMailArmor");
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
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        secondWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        baseEnvStats().setSpeed(2.0);
                break;
            case 2:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        baseEnvStats().setSpeed(1.0);
                break;
            case 3:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        secondWeapon = (Weapon)CMClass.getWeapon("DrowDagger");
		        baseEnvStats().setSpeed(2.0);
                break;
            case 4:
		        mainWeapon = (Weapon)CMClass.getWeapon("Scimitar");
		        secondWeapon = (Weapon)CMClass.getWeapon("Scimitar");
		        baseEnvStats().setSpeed(2.0);
                break;
            default:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        secondWeapon = (Weapon)CMClass.getWeapon("DrowSword");
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

	public boolean okAffect(Affect affect)
	{
		boolean retval = super.okAffect(affect);

		if((affect.amITarget(this))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_CAST_SPELL))
		{
            if(Dice.rollPercentage() <= magicResistance)
            {
	            affect.source().tell("The drow warrior resisted your spell!");
	            return false;
            }
        }
        return retval;
    }

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==Host.MOB_TICK))
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
		return super.tick(tickID);
	}

    public boolean checkStatus()
    {
        if(envStats().disposition() == Sense.IS_SITTING)
            envStats().setDisposition(envStats().disposition() - Sense.IS_SITTING);
        this.location().show(this, null, Affect.MSG_NOISYMOVEMENT, "<S-NAME> stand(s) up, ready for more combat.");

        return true;
    }

    public boolean useSkill()
    {
        Ability prayer = null;
        if(Dice.rollPercentage() < 70)
        {
            prayer =  this.fetchAbility(Dice.roll(1,14,1));
            while(this.baseEnvStats().level() < prayer.baseEnvStats().level())
                prayer = this.fetchAbility(Dice.roll(1,14,1));
        }
        else
            prayer = CMClass.getAbility("Prayer_CureSerious");
        return prayer.invoke(this,null,false);
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

		dark.invoke(this,null,false);
		return true;
	}


	public Environmental newInstance()
	{
		return new DrowWarrior();
	}

}
