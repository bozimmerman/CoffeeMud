package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Thief_MakeBomb extends ThiefSkill
{
	public String ID() { return "Thief_MakeBomb"; }
	public String name(){ return "Make Bombs";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"BOMB"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Trap theTrap=null;
		Vector traps=new Vector();
		int qualifyingClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(getXLEVELLevel(mob));
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
			theTrap=(Trap)traps.elementAt(CMLib.dice().roll(1,traps.size(),-1));
		else
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Bomb Name",15)+" Requires\n\r");
			for(int r=0;r<traps.size();r++)
			{
				Trap T=(Trap)traps.elementAt(r);
				buf.append(CMStrings.padRight(T.name(),15)+" ");
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
				if(CMLib.english().containsString(T.name(),name))
					theTrap=T;
			}
			if(theTrap==null)
			{
				mob.tell("'"+name+"' is not a valid bomb name.  Try BOMB LIST.");
				return false;
			}

			trapThis=this.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
			if(trapThis==null) return false;
			if((!auto)&&(!theTrap.canSetTrapOn(mob,trapThis)))
				return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,+((mob.envStats().level()+(getXLEVELLevel(mob)*3)
											 -trapThis.envStats().level())*3),auto);
		Trap theOldTrap=CMLib.utensils().fetchMyTrap(trapThis);
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

		CMMsg msg=CMClass.getMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to make a bomb out of <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell("You have completed your task.");
				theTrap.setTrap(mob,trapThis,getXLEVELLevel(mob),adjustedLevel(mob,asLevel),false);
			}
			else
			{
				if(CMLib.dice().rollPercentage()>50)
				{
					Trap T=theTrap.setTrap(mob,trapThis,getXLEVELLevel(mob),adjustedLevel(mob,asLevel),false);
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
