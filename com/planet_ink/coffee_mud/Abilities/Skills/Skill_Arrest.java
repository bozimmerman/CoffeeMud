package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_Arrest extends StdAbility
{
	public String ID() { return "Skill_Arrest"; }
	public String name(){ return "Arrest";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"ARREST"};
	public String[] triggerStrings(){return triggerStrings;}
	private int enhancement=0;
	public int abilityCode(){return enhancement;}
	public void setAbilityCode(int newCode){enhancement=newCode;}
	public int usageType(){return USAGE_MOVEMENT;}

	public static Vector getWarrantsOf(MOB target, Area legalA)
	{
		Behavior B=null;
		if(legalA!=null) B=CoffeeUtensils.getLegalBehavior(legalA);
		Vector warrants=new Vector();
		if(B!=null)
		{
			warrants.addElement(new Integer(Law.MOD_GETWARRANTSOF));
			warrants.addElement(target.Name());
			if(!B.modifyBehavior(legalA,target,warrants))
				warrants.clear();
			else
			for(int i=warrants.size()-1;i>=0;i--)
			{
			    LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
			    if(W.crime().equalsIgnoreCase("pardoned"))
			        warrants.removeElementAt(i);
			}
		}
		return warrants;
	}
	
	public void makePeace(Room R, MOB mob, MOB target)
	{
		if(R==null) return;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB inhab=R.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
			    if(inhab.getVictim()==mob)
					inhab.makePeace();
			    if(inhab.getVictim()==target)
					inhab.makePeace();
			}
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((mob==target)&&(!auto))
		{
		    mob.tell("You can not arrest yourself.");
		    return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!auto)
		{
			if(mob.baseWeight()<(target.baseWeight()-450))
			{
				mob.tell(target.name()+" is way to big for you!");
				return false;
			}
		}
		int levelDiff=target.envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STRENGTH));

		// now see if it worked
		boolean success=profficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STRENGTH)-mob.charStats().getStat(CharStats.STRENGTH)))),auto);
		if(success)
		{
			Ability A=CMClass.getAbility("Skill_ArrestingSap");
			if(A!=null)
			{
				A.setProfficiency(100);
				A.setAbilityCode(10);
				A.invoke(mob,target,true,0);
			}
			if(Sense.isSleeping(target))
			{
			    makePeace(mob.location(),mob,target);
				A=target.fetchEffect("Skill_ArrestingSap");
				if(A!=null)A.unInvoke();
				A=CMClass.getAbility("Skill_HandCuff");
				if(A!=null)	A.invoke(mob,target,true,0);
			    makePeace(mob.location(),mob,target);
			    mob.tell("You'll have to PULL "+target.charStats().himher()+" to the judge now before he gets out of the cuffs.");
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> rear(s) back and attempt(s) to knock <T-NAMESELF> out, but fail(s).");

		// return whether it worked
		return success;
	}
}