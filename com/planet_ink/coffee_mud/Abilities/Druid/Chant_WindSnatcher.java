package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_WindSnatcher extends Chant
{
	public String ID() { return "Chant_WindSnatcher"; }
	public String name(){ return "Wind Snatcher";}
	public String displayText(){ return "(Wind Snatcher)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public Environmental newInstance(){	return new Chant_WindSnatcher();}


	public String[] windSpells={
		"Chant_WindGust",
		"Chant_SummonWind",
		"Prayer_HolyWind",
		"Spell_GustOfWind"
		};

	public boolean isSpell(String ID)
	{
		for(int i=0;i<windSpells.length;i++)
			if(windSpells[i].equalsIgnoreCase(ID))
				return true;
		return false;
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your wind snatcher fades away.");
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((affect.tool()!=null)&&(affect.tool() instanceof Ability)
		   &&(isSpell(affect.tool().ID())))
		{
			affect.source().location().show(invoker,null,affect.MSG_OK_VISUAL,"A form around <S-NAME> snatches "+affect.tool().name()+".");
			return false;
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchAffect(ID())!=null)
		{
			target.tell("You are already snatching the wind.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) for <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The wind snatcher surrounds <S-NAME>");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for the wind snatcher, but nothing happens.");


		// return whether it worked
		return success;
	}
}