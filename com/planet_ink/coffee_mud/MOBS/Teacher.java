package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Teacher extends StdMOB
{

	public Teacher()
	{
		super();
		Username="Cornelius, Knower of All Things";
		setDescription("He looks wise beyond his years.");
		setDisplayText("Cornelius is standing here contemplating your ignorance.");
		setAlignment(1000);
		setMoney(100);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(200);

		addBehavior(CMClass.getBehavior("MudChat"));
		addBehavior(CMClass.getBehavior("CombatAbilities"));

		baseCharStats().setStat(CharStats.INTELLIGENCE,25);
		baseCharStats().setStat(CharStats.WISDOM,25);
		baseCharStats().setStat(CharStats.CHARISMA,25);
		baseCharStats().setStat(CharStats.DEXTERITY,25);
		baseCharStats().setStat(CharStats.STRENGTH,25);
		baseCharStats().setStat(CharStats.CONSTITUTION,25);
		baseCharStats().setMyRace(CMClass.getRace("Human"));
		baseCharStats().getMyRace().setHeightWeight(baseEnvStats(),(char)baseCharStats().getStat(CharStats.GENDER));

		baseEnvStats().setAbility(10);
		baseEnvStats().setLevel(25);
		baseEnvStats().setArmor(-500);

		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			A=(Ability)A.copyOf();
			A.setProfficiency(100);
			A.setBorrowed(this,true);
			this.addAbility(A);
		}




		baseState.setHitPoints(4999);
		baseState.setMana(4999);
		baseState.setMovement(4999);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Teacher();
	}



}
