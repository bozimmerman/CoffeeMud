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

		baseCharStats().setIntelligence(25);
		baseCharStats().setWisdom(25);
		baseCharStats().setCharisma(25);
		baseCharStats().setDexterity(25);
		baseCharStats().setStrength(25);
		baseCharStats().setConstitution(25);
		baseCharStats().setMyRace(CMClass.getRace("Human"));

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
