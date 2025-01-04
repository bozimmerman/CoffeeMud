package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
/**
* Base class for a property test, typically a unit test of some sort.
*
* @see com.planet_ink.coffee_mud.core.CMClass
*
* @author Bo Zimmerman
*/
public class PropTest extends StdTest
{
	@Override
	public String ID()
	{
		return "PropTest";
	}

	protected static final String[]	spells			= { "Spell_Blur", "Spell_ResistMagicMissiles" };
	protected static String			semiSpellList	= null;

	protected Area		tempA	= null;
	protected Room		R1		= null;
	protected Room		R2		= null;
	protected Ability	A2		= null;
	protected Item		I		= null;
	protected CMMsg		msg		= null;
	protected Command	C		= null;
	protected Item		IS[]	= new Item[2];
	final MOB[]			mobs	= new MOB[2];
	final MOB[]			backups	= new MOB[2];


	protected final Comparator<Object> comp=new Comparator<Object>()
	{
		@Override
		public int compare(final Object o1, final Object o2)
		{
			if(o1 == null)
				return (o2==null)?0:-1;
			if(o2==null)
				return 1;
			if(o1 instanceof Ability)
			{
				if(!(o2 instanceof Ability))
					return -1;
				if((((CMObject)o1).ID().equals(((CMObject)o2).ID()))
				&&(((Ability)o1).text().equals(((Ability)o2).text())))
					return 0;
			}
			else
			if(o1 instanceof Behavior)
			{
				if(!(o2 instanceof Behavior))
					return -1;
				if((((CMObject)o1).ID().equals(((CMObject)o2).ID()))
				&&(((Behavior)o1).getParms().equals(((Behavior)o2).getParms())))
					return 0;
			}
			else
			if(o1 instanceof CMObject)
			{
				if(!(o2 instanceof CMObject))
					return -1;
				if(((CMObject)o1).ID().compareTo(((CMObject)o2).ID())==0)
					return 0;
			}
			else
			if(o1.equals(o2))
				return 0;
			return -1;
		}
	};

	protected static String semiSpellList()
	{
		if(semiSpellList!=null)
			return semiSpellList;
		final StringBuffer str=new StringBuffer("");
		for (final String spell : spells)
			str.append(spell+";");
		semiSpellList=str.toString();
		return semiSpellList;
	}

	protected static final String[] maliciousspells={"Spell_Blindness","Spell_Mute"};
	protected static String maliciousSemiSpellList=null;

	protected static String maliciousSemiSpellList()
	{
		if(maliciousSemiSpellList!=null)
			return maliciousSemiSpellList;
		final StringBuffer str=new StringBuffer("");
		for (final String maliciousspell : maliciousspells)
			str.append(maliciousspell+";");
		maliciousSemiSpellList=str.toString();
		return maliciousSemiSpellList;
	}

	protected boolean isAllAdjusted(final MOB mob)
	{
		if(mob.phyStats().ability()<10)
			return false;
		if(mob.charStats().getStat(CharStats.STAT_GENDER)!='F')
			return false;
		if(!mob.charStats().getCurrentClass().ID().equals("Fighter"))
			return false;
		if(mob.charStats().getStat(CharStats.STAT_CHARISMA)<18)
			return false;
		if(mob.maxState().getMana()<1000)
			return false;
		return true;
	}

	protected boolean isAnyAdjusted(final MOB mob)
	{
		if(mob.phyStats().ability()>11)
			return true;
		if(mob.charStats().getStat(CharStats.STAT_GENDER)=='F')
			return true;
		if(mob.charStats().getCurrentClass().ID().equals("Fighter"))
			return true;
		if(mob.charStats().getStat(CharStats.STAT_CHARISMA)>=18)
			return true;
		if(mob.maxState().getMana()>=1000)
			return true;
		return false;
	}

	protected void giveAbility(final Physical P, final Ability A)
	{
		final Ability A2=((Ability)A.copyOf());
		A2.setMiscText(A.text());
		P.addNonUninvokableEffect(A2);
	}

	protected boolean testResistance(final MOB mob)
	{
		final Item I=CMClass.getWeapon("Dagger");
		mob.curState().setHitPoints(mob.maxState().getHitPoints());
		int curHitPoints=mob.curState().getHitPoints();
		CMLib.combat().postDamage(mob,mob,I,5,CMMsg.MSG_WEAPONATTACK,Weapon.TYPE_PIERCING,"<S-NAME> <DAMAGE> <T-NAME>.");
		if(mob.curState().getHitPoints()<curHitPoints-3)
			return false;
		curHitPoints=mob.curState().getHitPoints();
		CMLib.factions().setAlignmentOldRange(mob,0);
		final Ability A=CMClass.getAbility("Prayer_DispelEvil");
		A.invoke(mob,mob,true,1);
		if(mob.curState().getHitPoints()<curHitPoints)
			return false;
		curHitPoints=mob.curState().getHitPoints();
		if(mob.charStats().getSave(CharStats.STAT_SAVE_ACID)<30)
			return false;
		return true;
	}

	protected Item[] giveTo(final Item I, final Ability A, final MOB mob1, final MOB mob2, final int code)
	{
		final Item[] IS=new Item[2];
		final Item I1=(Item)I.copyOf();
		if(A!=null)
			giveAbility(I1,A);
		if(code<2)
		{
			mob1.addItem(I1);
			if(code==1)
				I1.wearEvenIfImpossible(mob1);
		}
		else
		{
			mob1.location().addItem(I1);
			if((I1 instanceof Rideable)&&(code==2))
				mob1.setRiding((Rideable)I1);
		}

		IS[0]=I1;

		final Item I2=(Item)I.copyOf();
		if(A!=null)
			giveAbility(I2,A);
		if(mob2!=null)
		{
			if(code<2)
			{
				mob2.addItem(I2);
				if(code==1)
					I2.wearEvenIfImpossible(mob2);
			}
			else
			{
				mob2.location().addItem(I2);
				if((I2 instanceof Rideable)&&(code==2))
					mob2.setRiding((Rideable)I2);
			}
		}
		IS[1]=I2;
		mob1.location().recoverRoomStats();
		return IS;
	}

	protected boolean spellCheck(final String[] spells, final MOB mob)
	{
		for (final String spell : spells)
		{
			if(mob.fetchAbility(spell)==null)
				return false;
		}
		return true;
	}

	protected boolean effectCheck(final String[] spells, final MOB mob)
	{
		for (final String spell : spells)
		{
			if(mob.fetchEffect(spell)==null)
				return false;
		}
		return true;
	}

	@Override
	public void cleanupTest()
	{
		super.cleanupTest();
		if(R2 != null)
			R2.delAllEffects(true);
		if(R1 != null)
			R1.delAllEffects(true);
		if(IS!=null)
		{
			if(IS[0]!=null)
				IS[0].destroy();
			if(IS[1]!=null)
				IS[1].destroy();
		}
		if(mobs[0]!=null)
			mobs[0].destroy();
		if(mobs[1]!=null)
			mobs[1].destroy();
		if(backups[0]!=null)
			backups[0].destroy();
		if(backups[1]!=null)
			backups[1].destroy();

		if(R2 != null)
			R2.destroy();
		if(R1 != null)
			R1.destroy();
		if(tempA != null)
			tempA.destroy();
	}

	protected void resetTest()
	{
		cleanupTest();
		tempA=CMClass.getAreaType("StdArea");
		tempA.setName("TempArea");
		R1=CMClass.getLocale("WoodRoom");
		R1.setRoomID("TempArea#1");
		R1.setArea(tempA);
		R2=CMClass.getLocale("StoneRoom");
		R2.setRoomID("TempArea#2");
		R2.setArea(tempA);
		R1.setRawExit(Directions.UP,CMClass.getExit("Open"));
		R2.setRawExit(Directions.DOWN,CMClass.getExit("Open"));
		R1.rawDoors()[Directions.UP]=R2;
		R2.rawDoors()[Directions.DOWN]=R1;
		mobs[0]=CMClass.getMOB("StdMOB");
		mobs[0].baseCharStats().setMyRace(CMClass.getRace("Dwarf"));
		mobs[0].setName(L("A Dwarf"));
		mobs[0].baseCharStats().setCurrentClass(CMClass.getCharClass("Gaian"));
		mobs[0].baseCharStats().setCurrentClassLevel(30);
		mobs[0].basePhyStats().setLevel(30);
		mobs[0].recoverCharStats();
		mobs[0].recoverPhyStats();
		backups[0]=(MOB)mobs[0].copyOf();
		mobs[1]=CMClass.getMOB("StdMOB");
		mobs[1].setName(L("A Human"));
		mobs[1].baseCharStats().setMyRace(CMClass.getRace("Human"));
		mobs[1].baseCharStats().setCurrentClass(CMClass.getCharClass("Druid"));
		mobs[1].baseCharStats().setCurrentClassLevel(30);
		mobs[1].basePhyStats().setLevel(30);
		mobs[1].recoverCharStats();
		mobs[1].recoverPhyStats();
		backups[1]=(MOB)mobs[1].copyOf();
		mobs[0].bringToLife(R1,true);
		mobs[1].bringToLife(R1,true);
	}
}
