package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_IllusoryDisease extends Spell
{
	public String ID() { return "Spell_IllusoryDisease"; }
	public String name(){return "Illusory Disease";}
	public String displayText(){return "(Diseased)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_IllusoryDisease();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ILLUSION;}
	private int diseaseTick=5;
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,(int)Math.round(Util.div(affectableStats.getStat(CharStats.STRENGTH),2.0)));
	}

	public String text(){return "DISEASE";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=5;
			String str=null;
			switch(Dice.roll(1,5,0))
			{
			case 1:
				str="<S-NAME> double(s) over and dry heaves.";
				break;
			case 2:
				str="<S-NAME> sneeze(s). AAAAAAAAAAAAAACHOOO!!!!";
				break;
			case 3:
				str="<S-NAME> shake(s) feverishly.";
				break;
			case 4:
				str="<S-NAME> look(s) around weakly.";
				break;
			case 5:
				str="<S-NAME> cough(s) and shudder(s) feverishly.";
				break;
			}
			if(str!=null)
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,str);
			return true;
		}
		return true;
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You begin to feel better.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) at <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> get(s) sick!");
					success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}