package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_Haunted extends Prayer
{
	public String ID() { return "Prayer_Haunted"; }
	public String name(){ return "Haunted";}
	public String displayText(){ return "(Haunted)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int level=14;
	protected int numDone=0;
	protected int numMax=Integer.MAX_VALUE;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
		{
			super.unInvoke();
			return;
		}
		Room  R=(Room)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(R!=null))
		   R.showHappens(CMMsg.MSG_OK_VISUAL,"The haunt over you fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room)&&(numDone<numMax))
		{
			Room R=(Room)affected;
			DeadBody B=null;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&(I instanceof DeadBody)
				&&(I.container()==null)
				&&(!((DeadBody)I).playerCorpse())
				&&(((DeadBody)I).mobName().length()>0))
				{
					B=(DeadBody)I;
					break;
				}
			}
			if(B!=null)
			{
				Prayer_AnimateGhost.makeGhostFrom(R,B,null,level);
				B.destroy();
				level+=5;
				numDone++;
			}
		}
		return super.tick(ticking,tickID);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already haunted.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" to haunt this place.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				level=14;
				numDone=0;
				numMax=mob.envStats().level()/8;
				if(CoffeeUtensils.doesOwnThisProperty(mob,((Room)target)))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMClass.DBEngine().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,(int)(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*5));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for a haunting, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}