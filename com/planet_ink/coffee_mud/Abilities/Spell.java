package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell extends StdAbility
{
	public Spell()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Spell";
		displayText="(in a magical land of dreams)";
		miscText="";
		triggerStrings.addElement("CAST");
		triggerStrings.addElement("CA");
		
		canBeUninvoked=true;
		isAutoinvoked=false;
	}
	
	public int classificationCode()
	{
		return Ability.SPELL;
	}
	
	public Environmental newInstance()
	{
		return new Spell();
	}
	
	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;
		
		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't speak!");
			return false;
		}
		
		return true;
	}
}
