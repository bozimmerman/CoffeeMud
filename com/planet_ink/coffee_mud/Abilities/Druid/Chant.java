package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid extends StdAbility
{
	protected int affectType=Affect.MSG_CAST_VERBAL_SPELL;
	public Druid()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Druidic Chant";
		displayText="(in the natural order)";
		miscText="";
		triggerStrings.addElement("CHANT");
		triggerStrings.addElement("CH");
		quality=Ability.INDIFFERENT;
		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=0;
		maxRange=0;
	}

	public int classificationCode()
	{
		return Ability.PRAYER;
	}

	public Environmental newInstance()
	{
		return new Druid();
	}

	public boolean appropriateToMyAlignment(int alignment)
	{
		if((alignment>650)||(alignment<350))
			return false;
		return true;
	}
	public void helpProfficiency(MOB mob)
	{

		Ability A=(Ability)mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.appropriateToMyAlignment(mob.getAlignment()))
		{
			super.helpProfficiency(mob);
			return;
		}
		return;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.ACT_GENERAL;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((appropriateToMyAlignment(mob.getAlignment()))||(auto))
			return true;
		
		if((Dice.rollPercentage()<50))
			return true;
		mob.tell("Extreme emotions disrupt your chant.");
		return false;
	}
}
