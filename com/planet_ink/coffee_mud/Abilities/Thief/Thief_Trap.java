package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Trap extends ThiefSkill
{
	public String ID() { return "Thief_Trap"; }
	public String name(){ return "Lay Traps";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"TRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Trap();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Trap theTrap=null;
		Vector traps=new Vector();
		int qualifyingClassLevel=CMAble.qualifyingClassLevel(mob,this);
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((A instanceof Trap)
			   &&(!((Trap)A).isABomb())
			   &&(((Trap)A).maySetTrap(mob,qualifyingClassLevel)))
				traps.addElement(A);
		}
		Environmental trapThis=givenTarget;
		if(trapThis!=null)
			theTrap=(Trap)traps.elementAt(Dice.roll(1,traps.size(),-1));
		else
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			Exit E=CMClass.getExit("StdExit");
			Item I=CMClass.getItem("StdItem");
			StringBuffer buf=new StringBuffer(Util.padRight("Trap Name",15)+" "+Util.padRight("Affects",17)+" Requires\n\r");
			for(int r=0;r<traps.size();r++)
			{
				Trap T=(Trap)traps.elementAt(r);
				buf.append(Util.padRight(T.name(),15)+" ");
				if(T.canAffect(mob.location()))
					buf.append(Util.padRight("Rooms",17)+" ");
				else
				if(T.canAffect(E))
					buf.append(Util.padRight("Exits, Containers",17)+" ");
				else
				if(T.canAffect(I))
					buf.append(Util.padRight("Items",17)+" ");
				else
					buf.append(Util.padRight("Unknown",17)+" ");
				buf.append(T.requiresToSet()+"\n\r");
			}
			if(mob.session()!=null) mob.session().rawPrintln(buf.toString());
			return true;
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Trap what, with what kind of trap? Use trap list for a list.");
				return false;
			}
			String name=(String)commands.lastElement();
			commands.removeElementAt(commands.size()-1);
			for(int r=0;r<traps.size();r++)
			{
				Trap T=(Trap)traps.elementAt(r);
				if(CoffeeUtensils.containsString(T.name(),name))
					theTrap=T;
			}
			if(theTrap==null)
			{
				mob.tell("'"+name+"' is not a valid trap name.  Try TRAP LIST.");
				return false;
			}

			String whatToTrap=Util.combine(commands,0);
			int dirCode=Directions.getGoodDirectionCode(whatToTrap);
			if((trapThis==null)&&(whatToTrap.equalsIgnoreCase("room")||whatToTrap.equalsIgnoreCase("here")))
				trapThis=mob.location();
			if((dirCode>=0)&&(trapThis==null))
				trapThis=mob.location().getExitInDir(dirCode);
			if(trapThis==null)
				trapThis=this.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
			if(trapThis==null) return false;
			if((!auto)&&(!theTrap.canSetTrapOn(mob,trapThis)))
				return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()
											 -trapThis.envStats().level())*3),auto);
		Trap theOldTrap=CoffeeUtensils.fetchMyTrap(trapThis);
		if(theOldTrap!=null)
		{
			if(theOldTrap.disabled())
				success=false;
			else
			{
				theOldTrap.spring(mob);
				return false;
			}
		}

		FullMsg msg=new FullMsg(mob,trapThis,this,auto?Affect.MSG_OK_ACTION:Affect.MSG_THIEF_ACT,Affect.MASK_GENERAL|Affect.MSG_THIEF_ACT,Affect.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on <T-NAMESELF>."));
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell("You have completed your task.");
				theTrap.setTrap(mob,trapThis,CMAble.qualifyingClassLevel(mob,this),adjustedLevel(mob));
			}
			else
			{
				if(Dice.rollPercentage()>50)
				{
					Trap T=theTrap.setTrap(mob,trapThis,CMAble.qualifyingClassLevel(mob,this),adjustedLevel(mob));
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) the trap on accident!");
					T.spring(mob);
				}
				else
				{
					mob.tell("You fail in your attempt.");
				}
			}
		}
		return success;
	}
}