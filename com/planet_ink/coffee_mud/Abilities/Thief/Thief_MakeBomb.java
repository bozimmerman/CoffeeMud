package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_MakeBomb extends ThiefSkill
{
	public String ID() { return "Thief_MakeBomb"; }
	public String name(){ return "Make Bombs";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"BOMB"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_MakeBomb();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Trap theTrap=null;
		Vector traps=new Vector();
		int qualifyingClassLevel=CMAble.qualifyingClassLevel(mob,this);
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((A instanceof Trap)
			   &&(((Trap)A).isABomb())
			   &&(((Trap)A).maySetTrap(mob,qualifyingClassLevel)))
				traps.addElement(A);
		}
		Environmental trapThis=givenTarget;
		if(trapThis!=null)
			theTrap=(Trap)traps.elementAt(Dice.roll(1,traps.size(),-1));
		else
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Bomb Name",15)+" Requires\n\r");
			for(int r=0;r<traps.size();r++)
			{
				Trap T=(Trap)traps.elementAt(r);
				buf.append(Util.padRight(T.name(),15)+" ");
				buf.append(T.requiresToSet()+"\n\r");
			}
			if(mob.session()!=null) mob.session().rawPrintln(buf.toString());
			return true;
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Make a bomb from what, with what kind of bomb? Use bomb list for a list.");
				return false;
			}
			String name=(String)commands.lastElement();
			commands.removeElementAt(commands.size()-1);
			for(int r=0;r<traps.size();r++)
			{
				Trap T=(Trap)traps.elementAt(r);
				if(EnglishParser.containsString(T.name(),name))
					theTrap=T;
			}
			if(theTrap==null)
			{
				mob.tell("'"+name+"' is not a valid bomb name.  Try BOMB LIST.");
				return false;
			}

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

		FullMsg msg=new FullMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_GENERAL|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to make a bomb out of <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
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
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> set(s) the bomb off on accident!");
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