package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WardArea extends Spell implements Trap
{
	public String ID() { return "Spell_WardArea"; }
	public String name(){return "Ward Area";}
	public String displayText(){return "(Ward Area spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	private Ability shooter=null;
	private Vector parameters=null;
	public Environmental newInstance(){	return new Spell_WardArea();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	private boolean sprung=false;
	
	public boolean disabled(){return sprung;}
	public void disable(){unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{beneficialAffect(mob,E,0);return (Trap)E.fetchAffect(ID());}

	public boolean sprung(){return sprung;}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(sprung) return super.okAffect(myHost,affect);
		if(!super.okAffect(myHost,affect))
			return false;

		if((affect.amITarget(affected))
		&&(!affect.amISource(invoker())))
		{
			if((affect.targetMinor()==Affect.TYP_ENTER)
			||(affect.targetMinor()==Affect.TYP_LEAVE)
			||(affect.targetMinor()==Affect.TYP_FLEE))
			{
				if(affect.targetMinor()==Affect.TYP_LEAVE)
					return true;
				else
				{
					spring(affect.source());
					return false;
				}
			}
		}
		return true;
	}

	
	public void spring(MOB mob)
	{
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooter==null)||(parameters==null))
			return;
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,Affect.MSG_OK_ACTION,"<S-NAME> avoid(s) a magical ward trap.");
		else
		{
			MOB newCaster=CMClass.getMOB("StdMOB");
			newCaster.setName("the thin air");
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			newCaster.baseEnvStats().setLevel(invoker.envStats().level());
			newCaster.recoverEnvStats();
			newCaster.recoverCharStats();
			if(invoker()!=null)
				newCaster.setLeigeID(invoker().name());
			newCaster.setLocation((Room)affected);
			try
			{
				shooter.invoke(newCaster,parameters,mob,true);
			}
			catch(Exception e){Log.errOut("WARD/"+Util.combine(parameters,0),e);}
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		unInvoke();
		sprung=true;
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if(sprung)
			return;

		if((affect.amITarget(affected))
		&&(!affect.amISource(invoker())))
		{
			if(affect.targetMinor()==Affect.TYP_LEAVE)
			{
				spring(affect.source());
			}
		}
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		super.unInvoke();
		if(canBeUninvoked())
		{
			shooter=null;
			parameters=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify what arcane spell to set, and any necessary parameters.");
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
				mob.tell("You cannot set that spell here -- there are other players present!");
				return false;
			}
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		Environmental target = mob.location();
		if((target.fetchAffect(this.ID())!=null)||(givenTarget!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"A ward trap has already been set here!");
			if(mob.location().okAffect(mob,msg))
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

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), auto?"":"^S<S-NAME> set(s) a magical trap.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),9999);
				shooter=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to set a magic trap, but fail(s).");

		// return whether it worked
		return success;
	}
}