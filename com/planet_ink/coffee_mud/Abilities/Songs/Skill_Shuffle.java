package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Skill_Shuffle extends BardSkill
{
	public String ID() { return "Skill_Shuffle"; }
	public String name(){ return "Shuffle";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SHUFFLE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((Sense.isSitting(mob)||Sense.isSleeping(mob)))
		{
			mob.tell(mob.name()+" must stand up first!");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		if(mob.location().numInhabitants()==1)
		{
			mob.tell("You are the only one here!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> shuffle(s) around, bumping into everyone.");
			FullMsg msg2=new FullMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				Vector V=new Vector();
				Room R=mob.location();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					V.addElement(M);
				}
				while(R.numInhabitants()>0)
				{
					MOB M=R.fetchInhabitant(0);
					R.delInhabitant(M);
				}
				while(V.size()>0)
				{
					MOB M=(MOB)V.elementAt(Dice.roll(1,V.size(),-1));
					if(M.location()==R) R.addInhabitant(M);
					V.removeElement(M);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> shuffle(s) around, confusing <S-HIM-HERSELF>.");

		return success;
	}

}
