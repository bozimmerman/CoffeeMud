package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Drowness extends StdBehavior
{
	public String ID(){return "Drowness";}


	boolean confirmedSetup=false;
	public int darkDown=4;
	public int fightDown=2;
	public int statCheck=3;
	private int spellDown=3;
	private int magicResistance = 50;

	public static final int CAST_DARKNESS = 1;
	public static final int FIGHTER_SKILL = 128;
    public static final int CHECK_STATUS = 129;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;

		mob.baseCharStats().setStat(CharStats.STRENGTH,12 + Dice.roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,14 + Dice.roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.WISDOM,13 + Dice.roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.DEXTERITY,15 + Dice.roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,12 + Dice.roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.CHARISMA,13 + Dice.roll(1,6,0));
		if(mob.baseCharStats().getStat(CharStats.GENDER)=='M')
		{
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass("Fighter"));
			mob.recoverCharStats();
			addMaleNaturalAbilities(mob);
		}
		else
		{
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass("Cleric"));
			mob.recoverCharStats();
			addFemaleNaturalAbilities(mob);
		}
		mob.baseCharStats().setMyRace(CMClass.getRace("Elf"));
		mob.baseCharStats().getMyRace().startRacing(mob,false);

		mob.recoverMaxState();
		mob.recoverEnvStats();
		mob.recoverCharStats();
	}

    public void addFemaleNaturalAbilities(MOB mob)
    {
		Weapon mainWeapon = (Weapon)CMClass.getWeapon("Mace");
		mainWeapon.wearAt(Item.WIELD);
		mob.addInventory(mainWeapon);

        Ability dark=CMClass.getAbility("Spell_Darkness");
        dark.setProfficiency(100);
		dark.setBorrowed(mob,true);
        mob.addAbility(dark);

        Ability p1 = CMClass.getAbility("Prayer_ProtGood");
        p1.setProfficiency(Dice.roll(5, 10, 50));
		p1.setBorrowed(mob,true);
        mob.addAbility(p1);

        Ability p2 = CMClass.getAbility("Prayer_CauseLight");
        p2.setProfficiency(Dice.roll(5, 10, 50));
		p2.setBorrowed(mob,true);
        mob.addAbility(p2);

        Ability p3 = CMClass.getAbility("Prayer_CauseSerious");
        p3.setProfficiency(Dice.roll(5, 10, 50));
		p3.setBorrowed(mob,true);
        mob.addAbility(p3);

        Ability p4 = CMClass.getAbility("Prayer_Curse");
        p4.setProfficiency(Dice.roll(5, 10, 50));
		p4.setBorrowed(mob,true);
        mob.addAbility(p4);

        Ability p5 = CMClass.getAbility("Prayer_Paralyze");
        p5.setProfficiency(Dice.roll(5, 10, 50));
		p5.setBorrowed(mob,true);
        mob.addAbility(p5);

        Ability p6 = CMClass.getAbility("Prayer_DispelGood");
        p6.setProfficiency(Dice.roll(5, 10, 50));
		p6.setBorrowed(mob,true);
        mob.addAbility(p6);

        Ability p7 = CMClass.getAbility("Prayer_Plague");
        p7.setProfficiency(Dice.roll(5, 10, 50));
		p7.setBorrowed(mob,true);
        mob.addAbility(p7);

        Ability p8 = CMClass.getAbility("Prayer_CauseCritical");
        p8.setProfficiency(Dice.roll(5, 10, 50));
		p8.setBorrowed(mob,true);
        mob.addAbility(p8);

        Ability p9 = CMClass.getAbility("Prayer_Blindness");
        p9.setProfficiency(Dice.roll(5, 10, 50));
		p9.setBorrowed(mob,true);
        mob.addAbility(p9);

        Ability p10 = CMClass.getAbility("Prayer_BladeBarrier");
        p10.setProfficiency(Dice.roll(5, 10, 50));
		p10.setBorrowed(mob,true);
        mob.addAbility(p10);

        Ability p11 = CMClass.getAbility("Prayer_Hellfire");
        p11.setProfficiency(Dice.roll(5, 10, 50));
		p11.setBorrowed(mob,true);
        mob.addAbility(p11);

        Ability p12 = CMClass.getAbility("Prayer_UnholyWord");
        p12.setProfficiency(Dice.roll(5, 10, 50));
		p12.setBorrowed(mob,true);
        mob.addAbility(p12);

        Ability p13 = CMClass.getAbility("Prayer_Deathfinger");
        p13.setProfficiency(Dice.roll(5, 10, 50));
		p13.setBorrowed(mob,true);
        mob.addAbility(p13);

        Ability p14 = CMClass.getAbility("Prayer_Harm");
        p14.setProfficiency(Dice.roll(5, 10, 50));
		p14.setBorrowed(mob,true);
        mob.addAbility(p14);

    }

    public void addMaleNaturalAbilities(MOB mob)
    {
        Armor chainMail = (Armor)CMClass.getArmor("DrowChainMailArmor");
        chainMail.wearAt(Item.ON_TORSO);
        mob.addInventory(chainMail);

        Weapon mainWeapon = null;
        Weapon secondWeapon = null;

        int weaponry = Dice.roll(1,4,0);
		if(mob.inventorySize()==0)
        switch(weaponry)
        {
            case 1:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        secondWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        mainWeapon.wearAt(Item.WIELD);
		        secondWeapon.wearAt(Item.HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
            case 2:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
//		        Shield secondWeapon = new Shield();
		        mainWeapon.wearAt(Item.WIELD);
//		        secondWeapon.wear(Item.SHIELD);
		        mob.addInventory(mainWeapon);
//              mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(1.0);
                break;
            case 3:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        secondWeapon = (Weapon)CMClass.getWeapon("DrowDagger");
		        mainWeapon.wearAt(Item.WIELD);
		        secondWeapon.wearAt(Item.HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
            case 4:
		        mainWeapon = (Weapon)CMClass.getWeapon("Scimitar");
		        secondWeapon = (Weapon)CMClass.getWeapon("Scimitar");
		        mainWeapon.wearAt(Item.WIELD);
		        secondWeapon.wearAt(Item.HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
            default:
		        mainWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        secondWeapon = (Weapon)CMClass.getWeapon("DrowSword");
		        mainWeapon.wearAt(Item.WIELD);
		        secondWeapon.wearAt(Item.HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
        }

        Ability dark=CMClass.getAbility("Spell_Darkness");
        dark.setProfficiency(100);
		dark.setBorrowed(mob,true);
        mob.addAbility(dark);

        Ability p1 = CMClass.getAbility("Prayer_ProtGood");
        p1.setProfficiency(Dice.roll(5, 10, 50));
		p1.setBorrowed(mob,true);
        mob.addAbility(p1);

        Ability p2 = CMClass.getAbility("Prayer_CauseLight");
        p2.setProfficiency(Dice.roll(5, 10, 50));
		p2.setBorrowed(mob,true);
        mob.addAbility(p2);

        Ability p3 = CMClass.getAbility("Prayer_CauseSerious");
        p3.setProfficiency(Dice.roll(5, 10, 50));
		p3.setBorrowed(mob,true);
        mob.addAbility(p3);

        Ability p4 = CMClass.getAbility("Prayer_Curse");
        p4.setProfficiency(Dice.roll(5, 10, 50));
		p4.setBorrowed(mob,true);
        mob.addAbility(p4);

        Ability p5 = CMClass.getAbility("Prayer_Paralyze");
        p5.setProfficiency(Dice.roll(5, 10, 50));
		p5.setBorrowed(mob,true);
        mob.addAbility(p5);

        Ability p6 = CMClass.getAbility("Prayer_DispelGood");
        p6.setProfficiency(Dice.roll(5, 10, 50));
		p6.setBorrowed(mob,true);
        mob.addAbility(p6);

        Ability p7 = CMClass.getAbility("Prayer_Plague");
        p7.setProfficiency(Dice.roll(5, 10, 50));
		p7.setBorrowed(mob,true);
        mob.addAbility(p7);

        Ability p8 = CMClass.getAbility("Prayer_CauseCritical");
        p8.setProfficiency(Dice.roll(5, 10, 50));
		p8.setBorrowed(mob,true);
        mob.addAbility(p8);

        Ability p9 = CMClass.getAbility("Prayer_Blindness");
        p9.setProfficiency(Dice.roll(5, 10, 50));
		p9.setBorrowed(mob,true);
        mob.addAbility(p9);

        Ability p10 = CMClass.getAbility("Prayer_BladeBarrier");
        p10.setProfficiency(Dice.roll(5, 10, 50));
		p10.setBorrowed(mob,true);
        mob.addAbility(p10);

        Ability p11 = CMClass.getAbility("Prayer_Hellfire");
        p11.setProfficiency(Dice.roll(5, 10, 50));
		p11.setBorrowed(mob,true);
        mob.addAbility(p11);

        Ability p12 = CMClass.getAbility("Prayer_UnholyWord");
        p12.setProfficiency(Dice.roll(5, 10, 50));
		p12.setBorrowed(mob,true);
        mob.addAbility(p12);

        Ability p13 = CMClass.getAbility("Prayer_Deathfinger");
        p13.setProfficiency(Dice.roll(5, 10, 50));
		p13.setBorrowed(mob,true);
        mob.addAbility(p13);

        Ability p14 = CMClass.getAbility("Prayer_Harm");
        p14.setProfficiency(Dice.roll(5, 10, 50));
		p14.setBorrowed(mob,true);
        mob.addAbility(p14);

    }


    public boolean checkStatus(MOB mob)
    {
        if(Sense.isSitting(mob))
            mob.envStats().setDisposition(mob.envStats().disposition() - EnvStats.IS_SITTING);
        mob.location().show(mob, null, CMMsg.MSG_QUIETMOVEMENT, "<S-NAME> stand(s) up, ready for more combat.");

        return true;
    }

    public boolean useSkill(MOB mob)
    {
        Ability prayer = null;
        if(Dice.rollPercentage() < 70)
        {
            prayer = mob.fetchAbility(Dice.roll(1,mob.numLearnedAbilities(),-1));
            while((prayer==null)||(mob.baseEnvStats().level() < CMAble.lowestQualifyingLevel(prayer.ID())))
                prayer = mob.fetchAbility(Dice.roll(1,mob.numLearnedAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureSerious");
        }
		if(prayer!=null)
	        return prayer.invoke(mob,null,false);
		else
			return false;
    }

	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if(oking==null) return super.okMessage(oking,msg);
		if(!(oking instanceof MOB)) return super.okMessage(oking,msg);

		boolean retval = super.okMessage(oking, msg);
		MOB mob=(MOB)oking;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
		{
            if(Dice.rollPercentage() <= magicResistance)
            {
	            msg.source().tell("The drow resisted your spell!");
	            return false;
            }
        }
        return retval;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		if(ticking!=null)
		if(ticking instanceof MOB)
		{
			MOB mob=(MOB)ticking;
			if((!mob.amDead())&&(tickID==MudHost.TICK_MOB))
			{
				if(mob.baseCharStats().getStat(CharStats.GENDER)=='F')
				{
					if (mob.isInCombat())
					{
						if((--spellDown)<=0)
						{
							spellDown=3;
							castFemaleSpell(mob);
						}
						if((--darkDown)<=0)
						{
							darkDown=4;
							castDarkness(mob);
						}
					}
				}else
				{
					if (mob.isInCombat())
					{
						if((--fightDown)<=0)
						{
							fightDown=2;
							useSkill(mob);
						}
						if((--statCheck)<=0)
						{
							statCheck=3;
							checkStatus(mob);
						}
						if((--darkDown)<=0)
						{
							darkDown=4;
							castDarkness(mob);
						}
					}
				}

			}
		}
		super.tick(ticking,tickID);
		return true;
	}

    public boolean castFemaleSpell(MOB mob)
    {
	    Ability prayer = null;
        if(Dice.rollPercentage() < 70)
        {
            prayer = mob.fetchAbility(Dice.roll(1,mob.numLearnedAbilities(),-1));
            while((prayer==null)||(mob.baseEnvStats().level() < CMAble.lowestQualifyingLevel(prayer.ID())))
                prayer = mob.fetchAbility(Dice.roll(1,mob.numLearnedAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureSerious");
            prayer.setProfficiency(Dice.roll(5, 10, 50));
        }
		if(prayer!=null)
	        return prayer.invoke(mob,null,false);
		else
			return false;
    }

	protected boolean castDarkness(MOB mob)
	{
		if(mob.location()==null)
			return true;
		if(Sense.isInDark(mob.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setProfficiency(100);
		dark.setBorrowed(mob,true);
		if(mob.fetchAbility(dark.ID())==null)
			mob.addAbility(dark);
		else
			dark = mob.fetchAbility(dark.ID());

		if(dark!=null) dark.invoke(mob,null,false);
		return true;
	}
}