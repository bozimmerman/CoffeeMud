package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Thief_UsePoison extends ThiefSkill
{
	public String ID() { return "Thief_UsePoison"; }
	public String name(){ return "Use Poison";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"POISON","USEPOISON"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("What would you like to poison, and which poison would you use?");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.elementAt(0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if((!(target instanceof Food))
		&&(!(target instanceof Drink))
		&&(!(target instanceof Weapon)))
		{
			mob.tell("You don't know how to poison "+target.name()+".");
			return false;
		}
		Item poison=mob.fetchCarried(null,Util.combine(commands,1));
		if((poison==null)||((poison!=null)&&(!Sense.canBeSeenBy(poison,mob))))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		Vector V=returnOffensiveAffects(poison);
		if((V.size()==0)||(!(poison instanceof Drink)))
		{
			mob.tell(poison.name()+" is not a poison!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,"<S-NAME> attempt(s) to poison <T-NAMESELF>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				Ability A=(Ability)V.firstElement();
				if(A!=null)
					if(target instanceof Weapon)
						A.invoke(mob,target,true,asLevel);
					else
						target.addNonUninvokableEffect(A);
			}
		}
		return success;
	}

}
