package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Root extends Chant
{
	public String ID() { return "Chant_Root"; }
	public String name(){return "Root";}
	public String displayText(){return "(Rooted)";}
	public int quality(){ return BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){ return new Chant_Root();}
	private boolean uprooted=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You pull up your roots.");
	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			if(affect.amISource((MOB)affected))
			{
				if((affect.targetMinor()==Affect.TYP_LEAVE)
				||(affect.sourceMinor()==Affect.TYP_ADVANCE)
				||(affect.sourceMinor()==Affect.TYP_RETREAT))
				{
					if(!uprooted)
					{
						affect.source().tell("You can't really go anywhere -- you are rooted!");
						return false;
					}
					uprooted=false;
				}
			}
			else
			if(affect.amITarget(affected))
			{
				if((affect.tool()!=null)
				&&(("spell_dismissal;spell_levitate;spell_gustofwind;spell_repulsion;chant_windgust;prayer_holywind;spell_shove;fighter_bullrush;").indexOf(affect.tool().ID().toLowerCase())>=0))
				{
					if(!uprooted)
					{
						affect.source().tell((MOB)affected,null,null,"<S-NAME> <S-IS-ARE> rooted and can't go anywhere.");
						return false;
					}
					uprooted=false;
				}
			}
		}
		return super.okAffect(myHost,affect);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		
		if(target.fetchAffect(ID())!=null)
		{
			target.tell("You are already rooted.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<S-NAME> become(s) rooted to the ground!":"^S<S-NAME> chant(s) as <S-HIS-HER> feet become rooted in the ground!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing more happens.");

		// return whether it worked
		return success;
	}
}
