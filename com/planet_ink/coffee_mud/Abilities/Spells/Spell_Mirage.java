package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Mirage extends Spell
{
	Room newRoom=null;
	public Spell_Mirage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mirage";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Mirage spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Mirage();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		room.show(invoker, null, Affect.MSG_OK_VISUAL, "The appearance of this place changes...");
		super.unInvoke();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		&&(affected instanceof Room)
		&&(affect.amITarget(affected))
		&&(newRoom.fetchAffect(ID())==null)
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			Affect msg=new FullMsg(affect.source(),newRoom,affect.tool(),
						  affect.sourceCode(),affect.sourceMessage(),
						  affect.targetCode(),affect.targetMessage(),
						  affect.othersCode(),affect.othersMessage());
			if(newRoom.okAffect(msg))
			{
				newRoom.affect(msg);
				return false;
			}
		}
		return super.okAffect(affect);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Vector V=mob.location().getArea().getMyMap();
		if(V.size()<2)
		{
			mob.tell("This area is too small to cast this spell.");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();
		boolean success=profficiencyCheck(0,auto);
		newRoom=mob.location();
		while(newRoom==mob.location())
			newRoom=(Room)V.elementAt(Dice.roll(1,V.size(),-1));
		
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType, auto?"":"<S-NAME> chant(s) and gesture(s) dramatically!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"The appearance of this place changes...");
				beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) dramatically, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
