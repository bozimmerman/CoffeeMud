package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class DrowWizard extends DrowElf
{
	public int darkDown=4;
	public int spellDown=2;
    private int magicResistance = 50;

	public DrowWizard()
	{
		super();

		baseEnvStats().setLevel(Dice.roll(4,6,1));

        magicResistance = 50 + baseEnvStats().level() * 2;

		String sex = "male";

		// ===== set the basics
		Username="a Drow male";
		setDescription("a Drow wizard");
		setDisplayText("He\\`s wearing a finely crafted black cloak.");
		
		Quarterstaff mainWeapon = new Quarterstaff();
		mainWeapon.wear(Item.WIELD);
		this.addInventory(mainWeapon);
		
		baseEnvStats().setArmor(40);
		
		maxState.setHitPoints(Dice.roll(baseEnvStats().level(), 8, 2));
		setMoney(Dice.roll(4,10,0) * 25);
		baseEnvStats.setWeight(70 + Dice.roll(3,6,2));
		
		setWimpHitPoint(1);
		
		baseEnvStats().setSpeed(1.0);
		
		baseCharStats().setStrength(12 + Dice.roll(1,6,0));
		baseCharStats().setIntelligence(14 + Dice.roll(1,6,0));
		baseCharStats().setWisdom(13 + Dice.roll(1,6,0));
		baseCharStats().setDexterity(15 + Dice.roll(1,6,0));
		baseCharStats().setConstitution(12 + Dice.roll(1,6,0));
		baseCharStats().setCharisma(13 + Dice.roll(1,6,0));
		baseCharStats().setMyClass(new Mage());
        baseCharStats().setMyRace(new Elf());

        addNaturalAbilities();

		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
    public void addNaturalAbilities()
    {
        Spell_Darkness dark=new Spell_Darkness();
        dark.setProfficiency(100);
        this.addAbility(dark);

        Prayer_ProtGood p1 = new Prayer_ProtGood();
        p1.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p1);

        Prayer_CauseLight p2 = new Prayer_CauseLight();
        p2.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p2);

        Prayer_CauseSerious p3 = new Prayer_CauseSerious();
        p3.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p3);

        Prayer_Curse p4 = new Prayer_Curse();
        p4.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p4);

        Prayer_Paralyze p5 = new Prayer_Paralyze();
        p5.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p5);

        Prayer_DispelGood p6 = new Prayer_DispelGood();
        p6.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p6);

        Prayer_Plague p7 = new Prayer_Plague();
        p7.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p7);

        Prayer_CauseCritical p8 = new Prayer_CauseCritical();
        p8.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p8);

        Prayer_Blindness p9 = new Prayer_Blindness();
        p9.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p9);

        Prayer_BladeBarrier p10 = new Prayer_BladeBarrier();
        p10.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p10);

        Prayer_Hellfire p11 = new Prayer_Hellfire();
        p11.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p11);

        Prayer_UnholyWord p12 = new Prayer_UnholyWord();
        p12.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p12);

        Prayer_Deathfinger p13 = new Prayer_Deathfinger();
        p13.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p13);

        Prayer_Harm p14 = new Prayer_Harm();
        p14.setProfficiency(Dice.roll(5, 10, 50));
        this.addAbility(p14);

    }

	public boolean okAffect(Affect affect)
	{
		boolean retval = super.okAffect(affect);
		MOB SourceMOB = affect.source();
		
		if(affect.amITarget(this))
		{
            if((affect.targetCode()==Affect.STRIKE_MAGIC) || (affect.targetCode()==Affect.SOUND_MAGIC))
            {
                if(Dice.rollPercentage() <= magicResistance)
                {
	                affect.source().tell("The drow warrior resisted your spell!");
	                return false;
                }
            }
        }
        return true;
    }

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==ServiceEngine.MOB_TICK))
		{
			if (isInCombat())
			{
				if((--spellDown)<=0)
				{
					spellDown=2;
					castSpell();
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

    public boolean castSpell()
    {
        Prayer prayer = null;
        if(Dice.rollPercentage() < 70)
        {
            prayer = (Prayer) this.fetchAbility(Dice.roll(1,14,1));
            while(this.baseEnvStats().level() < prayer.baseEnvStats().level())
                prayer = (Prayer) this.fetchAbility(Dice.roll(1,14,1));
        }
        else
        {
            prayer = (Prayer) new Prayer_CureSerious();
        }
        Vector commands = new Vector();
//        commands.addElement();
        return prayer.invoke(this, commands);;      
    }
	
	protected boolean castDarkness()
	{
		if(this.location()==null) 
			return true;
		if(Sense.isInDark(this.location()))
			return true;
		
		Spell_Darkness dark=new Spell_Darkness();
		dark.setProfficiency(100);
		if(this.fetchAbility(dark.ID())==null)
		   this.addAbility(dark);
		else
			dark = (Spell_Darkness) this.fetchAbility(dark.ID());
		
		dark.invoke(this,new Vector());
		return true;
	}


	public Environmental newInstance()
	{
		return new DrowWizard();
	}
	
}
