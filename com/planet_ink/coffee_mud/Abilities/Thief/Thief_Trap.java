package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Thief_Trap extends ThiefSkill
{
	public String ID() { return "Thief_Trap"; }
	public String name(){ return "Lay Traps";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	public int quality(){ return MALICIOUS;}
	private static final String[] triggerStrings = {"TRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	protected int maxLevel(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Trap theTrap=null;
		Vector traps=new Vector();
		int qualifyingClassLevel=CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this)+1;
		if(qualifyingClassLevel>maxLevel()) qualifyingClassLevel=maxLevel();
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
			String cmdWord=triggerStrings()[0].toLowerCase();
			if(commands.size()<2)
			{
				mob.tell("Trap what, with what kind of trap? Use "+cmdWord+" list for a list.");
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
				mob.tell("'"+name+"' is not a valid trap name.  Try "+cmdWord.toUpperCase()+" LIST.");
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

		boolean success=profficiencyCheck(mob,+((mob.envStats().level()
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

		FullMsg msg=new FullMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_GENERAL|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell("You have completed your task.");
				boolean permanent=false;
				if((trapThis instanceof Room)
				&&(CoffeeUtensils.doesOwnThisProperty(mob,((Room)trapThis))))
					permanent=true;
				else
				if(trapThis instanceof Exit)
				{
					Room R=mob.location();
					Room R2=null;
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
						if(R.getExitInDir(d)==trapThis)
						{ R2=R.getRoomInDir(d); break;}
					if((CoffeeUtensils.doesOwnThisProperty(mob,R))
					||((R2!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob,R2))))
						permanent=true;
				}
				if(permanent)
				{
					Ability newTrap=(Ability)theTrap.copyOf();
					newTrap.setInvoker(mob);
					trapThis.addNonUninvokableEffect(newTrap);
					CMClass.DBEngine().DBUpdateRoom(mob.location());
				}
				else
					theTrap.setTrap(mob,trapThis,CMAble.qualifyingClassLevel(mob,this),adjustedLevel(mob));
			}
			else
			{
				if(Dice.rollPercentage()>50)
				{
					Trap T=theTrap.setTrap(mob,trapThis,CMAble.qualifyingClassLevel(mob,this),adjustedLevel(mob));
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) the trap on accident!");
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
