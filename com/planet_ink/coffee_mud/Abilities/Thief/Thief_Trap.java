package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Thief_Trap extends ThiefSkill
{
	@Override public String ID() { return "Thief_Trap"; }
	@Override public String name(){ return "Lay Traps";}
	@Override protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	@Override protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;}
	private static final String[] triggerStrings = {"TRAP"};
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	protected int maxLevel(){return Integer.MAX_VALUE;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Trap theTrap=null;
		final Vector traps=new Vector();
		int qualifyingClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(getXLEVELLevel(mob))-CMLib.ableMapper().qualifyingLevel(mob,this)+1;
		if(qualifyingClassLevel>maxLevel()) qualifyingClassLevel=maxLevel();
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A instanceof Trap)
			   &&(!((Trap)A).isABomb())
			   &&(((Trap)A).maySetTrap(mob,qualifyingClassLevel)))
				traps.addElement(A);
		}
		Physical trapThis=givenTarget;
		if(trapThis!=null)
		{
			int cuts=0;
			while(((++cuts)<100)&&(theTrap==null))
			{
				theTrap=(Trap)traps.elementAt(CMLib.dice().roll(1,traps.size(),-1));
				if(!theTrap.canSetTrapOn(mob,trapThis))
					theTrap=null;
			}
		}
		else
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			final StringBuffer buf=new StringBuffer(CMStrings.padRight(_("Trap Name"),15)+" "+CMStrings.padRight(_("Affects"),17)+" Requires\n\r");
			for(int r=0;r<traps.size();r++)
			{
				final Trap T=(Trap)traps.elementAt(r);
				buf.append(CMStrings.padRight(T.name(),15)+" ");
				if(T.canAffect(Ability.CAN_ROOMS))
					buf.append(CMStrings.padRight(_("Rooms"),17)+" ");
				else
				if(T.canAffect(Ability.CAN_EXITS))
					buf.append(CMStrings.padRight(_("Exits, Containers"),17)+" ");
				else
				if(T.canAffect(Ability.CAN_ITEMS))
					buf.append(CMStrings.padRight(_("Items"),17)+" ");
				else
					buf.append(CMStrings.padRight(_("Unknown"),17)+" ");
				buf.append(T.requiresToSet()+"\n\r");
			}
			if(mob.session()!=null) mob.session().rawPrintln(buf.toString());
			return true;
		}
		else
		{
			if(mob.isInCombat())
			{
				mob.tell(_("You are too busy to be laying traps at the moment!"));
				return false;
			}

			final String cmdWord=triggerStrings()[0].toLowerCase();
			if(commands.size()<2)
			{
				mob.tell(_("Trap what, with what kind of trap? Use @x1 list for a list.",cmdWord));
				return false;
			}
			String name;
			if(commands.get(0).toString().equalsIgnoreCase("room")||commands.get(0).toString().equalsIgnoreCase("here"))
			{
				name=CMParms.combine(commands,1);
				while(commands.size()>1)
					commands.removeElementAt(commands.size()-1);
			}
			else
			{
				name=(String)commands.lastElement();
				commands.removeElementAt(commands.size()-1);
			}
			for(int r=0;r<traps.size();r++)
			{
				final Trap T=(Trap)traps.elementAt(r);
				if(T.name().equalsIgnoreCase(name))
					theTrap=T;
			}
			if(theTrap==null)
			for(int r=0;r<traps.size();r++)
			{
				final Trap T=(Trap)traps.elementAt(r);
				if(CMLib.english().containsString(T.name(),name))
					theTrap=T;
			}
			if(theTrap==null)
			{
				mob.tell(_("'@x1' is not a valid trap name.  Try @x2 LIST.",name,cmdWord.toUpperCase()));
				return false;
			}

			final String whatToTrap=CMParms.combine(commands,0);
			final int dirCode=Directions.getGoodDirectionCode(whatToTrap);
			if(whatToTrap.equalsIgnoreCase("room")||whatToTrap.equalsIgnoreCase("here"))
				trapThis=mob.location();
			if((dirCode>=0)&&(trapThis==null))
				trapThis=mob.location().getExitInDir(dirCode);
			if(trapThis==null)
				trapThis=this.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
			if(trapThis==null) return false;
			if((!auto)&&(!theTrap.canSetTrapOn(mob,trapThis)))
				return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,+((mob.phyStats().level()+(getXLEVELLevel(mob)*2)
											 -trapThis.phyStats().level())*3),auto);
		final Trap theOldTrap=CMLib.utensils().fetchMyTrap(trapThis);
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

		final CMMsg msg=CMClass.getMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell(_("You have completed your task."));
				boolean permanent=false;
				if((trapThis instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,((Room)trapThis))))
					permanent=true;
				else
				if(trapThis instanceof Exit)
				{
					final Room R=mob.location();
					Room R2=null;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						if(R.getExitInDir(d)==trapThis)
						{ R2=R.getRoomInDir(d); break;}
					if((CMLib.law().doesOwnThisProperty(mob,R))
					||((R2!=null)&&(CMLib.law().doesOwnThisProperty(mob,R2))))
						permanent=true;
				}
				if(theTrap!=null)
				{
					theTrap.setTrap(mob,trapThis,getXLEVELLevel(mob),adjustedLevel(mob,asLevel),permanent);
					if(permanent)
						CMLib.database().DBUpdateRoom(mob.location());
				}
			}
			else
			{
				if((CMLib.dice().rollPercentage()>50)&&(theTrap!=null))
				{
					final Trap T=theTrap.setTrap(mob,trapThis,getXLEVELLevel(mob),adjustedLevel(mob,asLevel),false);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> trigger(s) the trap on accident!"));
					T.spring(mob);
				}
				else
				{
					mob.tell(_("You fail in your attempt."));
				}
			}
		}
		return success;
	}
}
