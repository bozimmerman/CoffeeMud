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
public class Skill_CollectBounty extends StdAbility
{
	public String ID() { return "Skill_CollectBounty"; }
	public String name(){ return "Collect Bounty";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"COLLECTBOUNTY","BOUNTY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MANA;}
	public Room dontleaveR=null;
	public int payOff=0;
	public Vector thewarrants=null;
	

	public boolean tick(Tickable ticking, int tickID)
	{
	    if(!super.tick(ticking,tickID)) return false;
	    if((affected instanceof MOB)&&(dontleaveR!=null)&&(thewarrants!=null))
	    {
	        MOB mob=(MOB)affected;
	        if(!dontleaveR.isInhabitant(mob))
	            unInvoke();
	        else
	        {
	            
	        }
	    }
	    else
	    if(canBeUninvoked()) 
	        unInvoke();
	    return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(mob.fetchEffect(ID())!=null)
		{
		    mob.tell("You are already collecting a bounty.  Be patient.");
		    return false;
		}
		
		Behavior B=null;
		Room R=mob.location();
		if(R!=null) B=CoffeeUtensils.getLegalBehavior(R);
		
		MOB judge=null;
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
		    MOB M=R.fetchInhabitant(i);
		    if((M!=null)&&(M!=mob)&&(M!=target))
		    {
		        Vector V=new Vector();
		        V.addElement(new Integer(Law.MOD_ISJUDGE));
		        if(B.modifyBehavior(CoffeeUtensils.getLegalObject(R),M,V))
		        {
		            judge=M; break;
		        }
		    }
		}
			
		if(judge==null)
		{
		    mob.tell("You must present "+target.name()+" to the judge.");
		    return false;
		}
			
		Vector warrants=new Vector();
		Area legalA=CoffeeUtensils.getLegalObject(R);
		if(B!=null)
		{
			warrants.addElement(new Integer(Law.MOD_GETWARRANTSOF));
			warrants.addElement(target.Name());
			if(!B.modifyBehavior(legalA,mob,warrants))
				warrants.clear();
			else
			for(int i=warrants.size()-1;i>=0;i--)
			{
			    LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
			    if(W.crime().equalsIgnoreCase("pardoned"))
			        warrants.removeElementAt(i);
			}
		}
		if(warrants.size()==0)
		{
		    mob.tell(target.name()+" is not wanted for anything here.");
		    return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> turn(s) <T-NAMESELF> in to "+judge.name()+" for the bounty.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int gold=0;
				for(int i=0;i<warrants.size();i++)
				{
				    LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
				    if(!W.crime().equalsIgnoreCase("pardoned"))
				    {
				        W.setArrestingOfficer(legalA,mob);
				        W.setState(Law.STATE_REPORTING);
				        gold+=(W.actionCode()*5);
				    }
				}
				super.beneficialAffect(mob,mob,asLevel,0);
				Skill_CollectBounty A=(Skill_CollectBounty)mob.fetchEffect(ID());
				if(A!=null){ A.dontleaveR=R; A.payOff=gold; A.thewarrants=warrants;}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to turn in <T-NAMESELF> to "+judge.name()+" for the bounty, but can't get "+judge.charStats().hisher()+" attention.");

		return success;
	}

}

