package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ReverseGravity extends Spell
{
	private Vector childrenAffects=new Vector();
	
	public Spell_ReverseGravity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Reverse Gravity";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Gravity is Reversed)";

		canAffectCode=Ability.CAN_ROOMS;
		canTargetCode=0;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(18);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ReverseGravity();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		else
		if((affected!=null)&&(affected instanceof Room)&&(invoker!=null))
		{
			Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if(!Sense.isInFlight(inhab))
				{
					inhab.makePeace();
					Ability A=CMClass.getAbility("Falling");
					A.setAffectedOne(null);
					A.setProfficiency(100);
					A.invoke(null,null,inhab,true);
					A=inhab.fetchAffect("Falling");
					if(A!=null)
						childrenAffects.addElement(A);
				}
			}
			for(int i=0;i<room.numItems();i++)
			{
				Item inhab=room.fetchItem(i);
				if(!Sense.isFlying(inhab.ultimateContainer()))
				{
					Ability A=CMClass.getAbility("Falling");
					A.setAffectedOne(room);
					A.setProfficiency(100);
					A.invoke(null,null,inhab,true);
					A=inhab.fetchAffect("Falling");
					if(A!=null)
						childrenAffects.addElement(A);
				}
			}
		}
		return true;
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked)
		{
			if(affected instanceof Room)
			{
				Room room=(Room)affected;
				room.showHappens(Affect.MSG_OK_VISUAL, "Gravity returns to normal...");
				if(invoker!=null)
				{
					Ability me=invoker.fetchAffect(ID());
					if(me!=null) me.setProfficiency(0);
				}
			}
			else
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				if(mob.location()!=null)
				{
					mob.location().show(mob, null, Affect.MSG_OK_VISUAL, "Gravity returns to normal..");
					Ability me=mob.location().fetchAffect(ID());
					if(me!=null) me.setProfficiency(0);
				}
			}
			while(childrenAffects.size()>0)
			{
				Ability A=(Ability)childrenAffects.elementAt(0);
				A.setProfficiency(0);
				childrenAffects.removeElement(A);
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"Gravity has already been reversed here!");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType, (auto?"G":"<S-NAME> chant(s) and wave(s) and g")+"ravity begins to reverse!");
			if(mob.location().okAffect(msg))
			{
				childrenAffects=new Vector();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),7);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) in reverse, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
