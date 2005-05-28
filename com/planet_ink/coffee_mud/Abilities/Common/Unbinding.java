package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Unbinding extends CommonSkill
{
	public String ID() { return "Unbinding"; }
	public String name(){ return "Unbinding";}
	private static final String[] triggerStrings = {"UNBIND","UNTIE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	MOB found=null;
	Ability removing=null;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==3)
			{
			    Vector affects=null;
			    if(found!=null)
				   affects=Sense.flaggedAffects(found,Ability.FLAG_BINDING);
				if((affects!=null)&&(affects.size()>0))
				{
				    removing=(Ability)affects.firstElement();
					displayText="You are removing "+removing.name()+" from "+found.name();
					verb="removing "+removing.name()+" from "+found.name();
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to remove any of the bindings.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}
			}
			else
			if((found!=null)&&(mob!=null))
			{
			    if(found.location()!=mob.location())
                {
                    aborted=true;
				    unInvoke();
                }
			    if(!Sense.canBeSeenBy(found,mob))
                {
                    aborted=true;
                    unInvoke();
                }
			    if(!Sense.aliveAwakeMobile(mob,false))
                {
                    aborted=true;
                    unInvoke();
                }
			    if((removing!=null)&&(found.fetchEffect(removing.ID())!=removing))
                {
                    aborted=true;
                    unInvoke();
                }
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((found!=null)&&(removing!=null)&&(!aborted))
				{
				    removing.unInvoke();
				    if(found.fetchEffect(removing.ID())==null)
						mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to remove "+removing.name()+" from "+found.name()+".");
				    else
						mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> fail(s) to remove "+removing.name()+" from "+found.name()+".");
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

	    MOB target=getTarget(mob,commands,givenTarget);
	    if(target==null) return false;
	    if((!auto)&&(target==mob))
	    {
	        mob.tell("You can't unbind yourself!");
	        return false;
	    }
	    if((!auto)&&mob.isInCombat())
	    {
	        mob.tell("Not while you are fighting!");
	        return false;
	    }
	    Vector affects=Sense.flaggedAffects(target,Ability.FLAG_BINDING);
	    if(affects.size()==0)
	    {
	        mob.tell(target.name()+" does not have any bindings you can remove.");
	        return false;
	    }
	    Ability A=(Ability)affects.firstElement();
	    
		verb="unbinding";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		int duration=CMAble.lowestQualifyingLevel(A.ID())-CMAble.qualifyingLevel(mob,A);
		if(duration<5) duration=3;
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> begin(s) to unbind <T-NAMESELF>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=target;
			if(profficiencyCheck(mob,0,auto))
			    found=null;
		}
		return true;
		
	}
}