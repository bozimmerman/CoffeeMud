package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2000-2010 Lee H. Fox

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
public class Drowness extends StdBehavior
{
	public String ID(){return "Drowness";}


	boolean confirmedSetup=false;
	public int darkDown=4;
	public int fightDown=2;
	public int statCheck=3;
	protected int spellDown=3;
	protected int magicResistance = 50;

	public static final int CAST_DARKNESS = 1;
	public static final int FIGHTER_SKILL = 128;
    public static final int CHECK_STATUS = 129;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;

		mob.baseCharStats().setStat(CharStats.STAT_STRENGTH,12 + CMLib.dice().roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,14 + CMLib.dice().roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.STAT_WISDOM,13 + CMLib.dice().roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,15 + CMLib.dice().roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,12 + CMLib.dice().roll(1,6,0));
		mob.baseCharStats().setStat(CharStats.STAT_CHARISMA,13 + CMLib.dice().roll(1,6,0));
		if(mob.baseCharStats().getStat(CharStats.STAT_GENDER)=='M')
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
		Weapon mainWeapon = CMClass.getWeapon("Mace");
		mainWeapon.wearAt(Wearable.WORN_WIELD);
		mob.addInventory(mainWeapon);

        Ability dark=CMClass.getAbility("Spell_Darkness");
        dark.setProficiency(100);
		dark.setSavable(false);
        mob.addAbility(dark);

        Ability p1 = CMClass.getAbility("Prayer_ProtGood");
        p1.setProficiency(CMLib.dice().roll(5, 10, 50));
		p1.setSavable(false);
        mob.addAbility(p1);

        Ability p2 = CMClass.getAbility("Prayer_CauseLight");
        p2.setProficiency(CMLib.dice().roll(5, 10, 50));
		p2.setSavable(false);
        mob.addAbility(p2);

        Ability p3 = CMClass.getAbility("Prayer_CauseSerious");
        p3.setProficiency(CMLib.dice().roll(5, 10, 50));
		p3.setSavable(false);
        mob.addAbility(p3);

        Ability p4 = CMClass.getAbility("Prayer_Curse");
        p4.setProficiency(CMLib.dice().roll(5, 10, 50));
		p4.setSavable(false);
        mob.addAbility(p4);

        Ability p5 = CMClass.getAbility("Prayer_Paralyze");
        p5.setProficiency(CMLib.dice().roll(5, 10, 50));
		p5.setSavable(false);
        mob.addAbility(p5);

        Ability p6 = CMClass.getAbility("Prayer_DispelGood");
        p6.setProficiency(CMLib.dice().roll(5, 10, 50));
		p6.setSavable(false);
        mob.addAbility(p6);

        Ability p7 = CMClass.getAbility("Prayer_Plague");
        p7.setProficiency(CMLib.dice().roll(5, 10, 50));
		p7.setSavable(false);
        mob.addAbility(p7);

        Ability p8 = CMClass.getAbility("Prayer_CauseCritical");
        p8.setProficiency(CMLib.dice().roll(5, 10, 50));
		p8.setSavable(false);
        mob.addAbility(p8);

        Ability p9 = CMClass.getAbility("Prayer_Blindness");
        p9.setProficiency(CMLib.dice().roll(5, 10, 50));
		p9.setSavable(false);
        mob.addAbility(p9);

        Ability p10 = CMClass.getAbility("Prayer_BladeBarrier");
        p10.setProficiency(CMLib.dice().roll(5, 10, 50));
		p10.setSavable(false);
        mob.addAbility(p10);

        Ability p11 = CMClass.getAbility("Prayer_Hellfire");
        p11.setProficiency(CMLib.dice().roll(5, 10, 50));
		p11.setSavable(false);
        mob.addAbility(p11);

        Ability p12 = CMClass.getAbility("Prayer_UnholyWord");
        p12.setProficiency(CMLib.dice().roll(5, 10, 50));
		p12.setSavable(false);
        mob.addAbility(p12);

        Ability p13 = CMClass.getAbility("Prayer_Deathfinger");
        p13.setProficiency(CMLib.dice().roll(5, 10, 50));
		p13.setSavable(false);
        mob.addAbility(p13);

        Ability p14 = CMClass.getAbility("Prayer_Harm");
        p14.setProficiency(CMLib.dice().roll(5, 10, 50));
		p14.setSavable(false);
        mob.addAbility(p14);

    }

    public void addMaleNaturalAbilities(MOB mob)
    {
        Armor chainMail = CMClass.getArmor("DrowChainMailArmor");
        chainMail.wearAt(Wearable.WORN_TORSO);
        mob.addInventory(chainMail);

        Weapon mainWeapon = null;
        Weapon secondWeapon = null;

        int weaponry = CMLib.dice().roll(1,4,0);
		if(mob.inventorySize()==0)
        switch(weaponry)
        {
            case 1:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        secondWeapon = CMClass.getWeapon("DrowSword");
		        mainWeapon.wearAt(Wearable.WORN_WIELD);
		        secondWeapon.wearAt(Wearable.WORN_HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
            case 2:
		        mainWeapon = CMClass.getWeapon("DrowSword");
//		        Shield secondWeapon = new Shield();
		        mainWeapon.wearAt(Wearable.WORN_WIELD);
//		        secondWeapon.wear(Item.SHIELD);
		        mob.addInventory(mainWeapon);
//              mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(1.0);
                break;
            case 3:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        secondWeapon = CMClass.getWeapon("DrowDagger");
		        mainWeapon.wearAt(Wearable.WORN_WIELD);
		        secondWeapon.wearAt(Wearable.WORN_HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
            case 4:
		        mainWeapon = CMClass.getWeapon("Scimitar");
		        secondWeapon = CMClass.getWeapon("Scimitar");
		        mainWeapon.wearAt(Wearable.WORN_WIELD);
		        secondWeapon.wearAt(Wearable.WORN_HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
            default:
		        mainWeapon = CMClass.getWeapon("DrowSword");
		        secondWeapon = CMClass.getWeapon("DrowSword");
		        mainWeapon.wearAt(Wearable.WORN_WIELD);
		        secondWeapon.wearAt(Wearable.WORN_HELD);
		        mob.addInventory(mainWeapon);
                mob.addInventory(secondWeapon);
		        mob.baseEnvStats().setSpeed(2.0);
                break;
        }

        Ability dark=CMClass.getAbility("Spell_Darkness");
        dark.setProficiency(100);
		dark.setSavable(false);
        mob.addAbility(dark);

        Ability p1 = CMClass.getAbility("Prayer_ProtGood");
        p1.setProficiency(CMLib.dice().roll(5, 10, 50));
		p1.setSavable(false);
        mob.addAbility(p1);

        Ability p2 = CMClass.getAbility("Prayer_CauseLight");
        p2.setProficiency(CMLib.dice().roll(5, 10, 50));
		p2.setSavable(false);
        mob.addAbility(p2);

        Ability p3 = CMClass.getAbility("Prayer_CauseSerious");
        p3.setProficiency(CMLib.dice().roll(5, 10, 50));
		p3.setSavable(false);
        mob.addAbility(p3);

        Ability p4 = CMClass.getAbility("Prayer_Curse");
        p4.setProficiency(CMLib.dice().roll(5, 10, 50));
		p4.setSavable(false);
        mob.addAbility(p4);

        Ability p5 = CMClass.getAbility("Prayer_Paralyze");
        p5.setProficiency(CMLib.dice().roll(5, 10, 50));
		p5.setSavable(false);
        mob.addAbility(p5);

        Ability p6 = CMClass.getAbility("Prayer_DispelGood");
        p6.setProficiency(CMLib.dice().roll(5, 10, 50));
		p6.setSavable(false);
        mob.addAbility(p6);

        Ability p7 = CMClass.getAbility("Prayer_Plague");
        p7.setProficiency(CMLib.dice().roll(5, 10, 50));
		p7.setSavable(false);
        mob.addAbility(p7);

        Ability p8 = CMClass.getAbility("Prayer_CauseCritical");
        p8.setProficiency(CMLib.dice().roll(5, 10, 50));
		p8.setSavable(false);
        mob.addAbility(p8);

        Ability p9 = CMClass.getAbility("Prayer_Blindness");
        p9.setProficiency(CMLib.dice().roll(5, 10, 50));
		p9.setSavable(false);
        mob.addAbility(p9);

        Ability p10 = CMClass.getAbility("Prayer_BladeBarrier");
        p10.setProficiency(CMLib.dice().roll(5, 10, 50));
		p10.setSavable(false);
        mob.addAbility(p10);

        Ability p11 = CMClass.getAbility("Prayer_Hellfire");
        p11.setProficiency(CMLib.dice().roll(5, 10, 50));
		p11.setSavable(false);
        mob.addAbility(p11);

        Ability p12 = CMClass.getAbility("Prayer_UnholyWord");
        p12.setProficiency(CMLib.dice().roll(5, 10, 50));
		p12.setSavable(false);
        mob.addAbility(p12);

        Ability p13 = CMClass.getAbility("Prayer_Deathfinger");
        p13.setProficiency(CMLib.dice().roll(5, 10, 50));
		p13.setSavable(false);
        mob.addAbility(p13);

        Ability p14 = CMClass.getAbility("Prayer_Harm");
        p14.setProficiency(CMLib.dice().roll(5, 10, 50));
		p14.setSavable(false);
        mob.addAbility(p14);

    }


    public boolean checkStatus(MOB mob)
    {
        if(CMLib.flags().isSitting(mob))
            mob.envStats().setDisposition(mob.envStats().disposition() - EnvStats.IS_SITTING);
        mob.location().show(mob, null, CMMsg.MSG_QUIETMOVEMENT, "<S-NAME> stand(s) up, ready for more combat.");

        return true;
    }

    public boolean useSkill(MOB mob)
    {
        Ability prayer = null;
        if(CMLib.dice().rollPercentage() < 70)
        {
            prayer = mob.fetchAbility(CMLib.dice().roll(1,mob.numLearnedAbilities(),-1));
            while((prayer==null)||(mob.baseEnvStats().level() < CMLib.ableMapper().lowestQualifyingLevel(prayer.ID())))
                prayer = mob.fetchAbility(CMLib.dice().roll(1,mob.numLearnedAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureSerious");
        }
		if(prayer!=null)
	        return prayer.invoke(mob,null,false,0);
		return false;
    }

	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if(oking==null) return super.okMessage(oking,msg);
		if(!(oking instanceof MOB)) return super.okMessage(oking,msg);

		boolean retval = super.okMessage(oking, msg);
		MOB mob=(MOB)oking;
		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
		{
            if(CMLib.dice().rollPercentage() <= magicResistance)
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
			if((!mob.amDead())&&(tickID==Tickable.TICKID_MOB))
			{
				if(mob.baseCharStats().getStat(CharStats.STAT_GENDER)=='F')
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
        if(CMLib.dice().rollPercentage() < 70)
        {
            prayer = mob.fetchAbility(CMLib.dice().roll(1,mob.numLearnedAbilities(),-1));
            while((prayer==null)||(mob.baseEnvStats().level() < CMLib.ableMapper().lowestQualifyingLevel(prayer.ID())))
                prayer = mob.fetchAbility(CMLib.dice().roll(1,mob.numLearnedAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureSerious");
            prayer.setProficiency(CMLib.dice().roll(5, 10, 50));
        }
		if(prayer!=null)
	        return prayer.invoke(mob,null,false,0);
		return false;
    }

	protected boolean castDarkness(MOB mob)
	{
		if(mob.location()==null)
			return true;
		if(CMLib.flags().isInDark(mob.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setProficiency(100);
		dark.setSavable(false);
		if(mob.fetchAbility(dark.ID())==null)
			mob.addAbility(dark);
		else
			dark = mob.fetchAbility(dark.ID());

		if(dark!=null) dark.invoke(mob,null,false,0);
		return true;
	}
}
