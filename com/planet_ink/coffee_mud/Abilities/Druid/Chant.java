package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant extends StdAbility
{
	public String ID() { return "Chant"; }
	public String name(){ return "a Druidic Chant";}
	public String displayText(){return "(in the natural order)";}
	protected int affectType(boolean auto){
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		return affectType;
	}
	private static final String[] triggerStrings = {"CHANT","CH"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public Environmental newInstance(){	return new Chant();	}
	public int classificationCode()	{ return Ability.CHANT;	}

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
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)
		&&(CMAble.getQualifyingLevel(mob.charStats().getCurrentClass().ID(),ID())<0)
		&&(!appropriateToMyAlignment(mob.getAlignment()))
		&&(mob.isMine(this))
		&&(Dice.rollPercentage()<50))
		{
			mob.tell("Extreme emotions disrupt your chant.");
			return false;
		}
		return true;
	}
}
