package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Delay extends Spell
{
	public String ID() { return "Spell_Delay"; }
	public String name(){return "Delay";}
	public String displayText(){return "(Delay spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	private Ability shooter=null;
	private Vector parameters=null;
	public Environmental newInstance(){	return new Spell_Delay();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooter==null)||(parameters==null))
			return;
		if(canBeUninvoked)
		{
			MOB newCaster=CMClass.getMOB("StdMOB");
			newCaster.setName("the thin air");
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			newCaster.baseEnvStats().setLevel(invoker.envStats().level());
			newCaster.recoverEnvStats();
			newCaster.recoverCharStats();
			newCaster.setLocation((Room)affected);
			try
			{
				shooter.invoke(newCaster,parameters,null,true);
			}
			catch(Exception e){Log.errOut("DELAY/"+Util.combine(parameters,0),e);}
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		super.unInvoke();
		if(canBeUninvoked)
		{
			shooter=null;
			parameters=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell("You must specify what arcane spell to delay, and any necessary parameters.");
			return false;
		}
		commands.insertElementAt("CAST",0);
		shooter=ExternalPlay.getToEvoke(mob,commands);
		parameters=commands;
		if((shooter==null)||((shooter.classificationCode()&Ability.ALL_CODES)!=Ability.SPELL))
		{
			parameters=null;
			shooter=null;
			mob.tell("You don't know any arcane spell by that name.");
			return false;
		}
		
		if(shooter.quality()==Ability.MALICIOUS)
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M!=mob)&&(!M.mayIFight(mob)))
			{
				mob.tell("You cannot delay that spell here -- there are other players present!");
				return false;
			}
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		mob.curState().setMana(0);
		Environmental target = mob.location();
		if((target.fetchAffect(this.ID())!=null)||(givenTarget!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"A delay has already been cast here!");
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

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), auto?"":"^S<S-NAME> point(s) and shout(s) 'NOW!'.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.tell("You hear a clock start ticking down in your head...20...19...");
				beneficialAffect(mob,mob.location(),5);
				shooter=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> point(s) and shout(s) 'NOW', but then look(s) frustrated.");

		// return whether it worked
		return success;
	}
}