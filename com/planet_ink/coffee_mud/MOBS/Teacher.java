package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Behaviors.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
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
		
		addBehavior(new MudChat());
		
		GenWeapon d=new GenWeapon("a wooden ruler",
								  "a wooden ruler lies sits here",
								  "It\\`s long and wooden, with little tick marks.",
								  null,
								  1000,
								  4,
								  30,
								  20,
								  Weapon.TYPE_BASHING,
								  Weapon.CLASS_BLUNT,
								  true);
		d.baseEnvStats().setAbility(2);
		d.baseEnvStats().setLevel(25);
		d.recoverEnvStats();
		d.wear(Item.WIELD);
		addInventory(d);
		
		addBehavior(new CombatAbilities());
		
		baseCharStats().setIntelligence(25);
		baseCharStats().setWisdom(25);
		baseCharStats().setCharisma(25);
		baseCharStats().setDexterity(25);
		baseCharStats().setStrength(25);
		baseCharStats().setConstitution(25);
		
		baseEnvStats().setAbility(10);
		baseEnvStats().setLevel(25);
		baseEnvStats().setArmor(-500);
		
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			A=(Ability)A.copyOf();
			A.setProfficiency(100);
			this.addAbility(A);
		}
			
		
		
		
		maxState().setHitPoints(999);
		maxState().setMana(999);
		maxState().setMovement(999);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Teacher();
	}



}
