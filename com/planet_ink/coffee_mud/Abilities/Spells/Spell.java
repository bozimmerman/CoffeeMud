package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell extends StdAbility
{
	protected int affectType=Affect.MSG_CAST_VERBAL_SPELL;

	public Spell()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Spell";
		displayText="(in a magical land of dreams)";
		miscText="";
		triggerStrings.addElement("CAST");
		triggerStrings.addElement("CA");
		triggerStrings.addElement("C");
		
		canAffectCode=0;
		canTargetCode=Ability.CAN_MOBS;
		
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.ACT_GENERAL;

		return super.invoke(mob,commands,givenTarget,auto);
	}
}
