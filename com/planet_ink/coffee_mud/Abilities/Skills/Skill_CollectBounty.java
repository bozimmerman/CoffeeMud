package com.planet_ink.coffee_mud.Abilities.Skills;
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
	
	public Vector getWarrantsOf(MOB target, Room R)
	{
	    return getWarrantsOf(target,CoffeeUtensils.getLegalObject(R));
	}
	public Vector getWarrantsOf(MOB target, Area legalA)
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
	
	public MOB findElligibleOfficer(Area myArea, Area legalA)
	{
		Behavior B=null;
		if(legalA!=null) B=CoffeeUtensils.getLegalBehavior(legalA);
		if((B!=null)&&(myArea!=null))
		{
		    for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
		    {
		        Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
				    MOB M=R.fetchInhabitant(i);
				    if(M!=null)
				    {
				        Vector V=new Vector();
				        V.addElement(new Integer(Law.MOD_ISELLIGOFFICER));
				        if(B.modifyBehavior(legalA,M,V))
				            return M;
				    }
				}
		    }
		    if(legalA!=myArea)
		    for(Enumeration e=legalA.getMetroMap();e.hasMoreElements();)
		    {
		        Room R=(Room)e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
				    MOB M=R.fetchInhabitant(i);
				    if(M!=null)
				    {
				        Vector V=new Vector();
				        V.addElement(new Integer(Law.MOD_ISELLIGOFFICER));
				        if(B.modifyBehavior(legalA,M,V))
				            return M;
				    }
				}
		    }
		}
		return null;
	}
	
	public MOB getJudgeIfHere(MOB mob, MOB target, Room R)
	{
		Behavior B=null;
		if(R!=null) B=CoffeeUtensils.getLegalBehavior(R);
		Area legalA=CoffeeUtensils.getLegalObject(R);
		if((B!=null)&&(R!=null))
			for(int i=0;i<R.numInhabitants();i++)
			{
			    MOB M=R.fetchInhabitant(i);
			    if((M!=null)&&(M!=mob)&&(M!=target))
			    {
			        Vector V=new Vector();
			        V.addElement(new Integer(Law.MOD_ISJUDGE));
			        if(B.modifyBehavior(legalA,M,V))
			            return M;
			    }
			}
		return null;
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		Room R=mob.location();
		if(mob.fetchEffect(ID())!=null)
		{
		    mob.tell("You are already collecting a bounty.  Be patient.");
		    return false;
		}
		
		MOB judge=getJudgeIfHere(mob,target,R);
			
		if(judge==null)
		{
		    mob.tell("You must present "+target.name()+" to the judge.");
		    return false;
		}
			
		Vector warrants=getWarrantsOf(target,R);
		if(warrants.size()==0)
		{
		    mob.tell(target.name()+" is not wanted for anything here.");
		    return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		Area legalA=CoffeeUtensils.getLegalObject(R);
		if((success)&&(legalA!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MOUTH|CMMsg.MASK_SOUND|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> turn(s) <T-NAMESELF> in to "+judge.name()+" for the bounty.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB officer=findElligibleOfficer(mob.location().getArea(),legalA);
				if((officer!=null)&&(!mob.location().isInhabitant(officer)))
				    MUDTracker.wanderFromTo(officer,mob.location(),true);
				if((officer==null)||(!mob.location().isInhabitant(officer)))
				{
				    CommonMsgs.say(judge,mob,"I'm sorry, there are no free officers to take care of this one right now.",false,false);
				    return false;
				}
				int gold=0;
				Ability A=mob.fetchEffect("Skill_HandCuff");
				if((A!=null)||(mob.fetchEffect("Thief_Bind")!=null)&&(target.amFollowing()==mob))
			    {
				    A.setInvoker(officer);
				    target.setFollowing(officer);
			    }
			    LegalWarrant W=(LegalWarrant)warrants.firstElement();
		        W.setArrestingOfficer(legalA,officer);
		        W.setState(Law.STATE_REPORTING);
				for(int i=0;i<warrants.size();i++)
				{
				    W=(LegalWarrant)warrants.elementAt(i);
			        gold+=(W.actionCode()*5);
				}
				mob.location().show(judge,mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> pay(s) <T-NAMESELF> the bounty of "+gold+" gold on "+target.Name()+".");
				MoneyUtils.giveMoney(judge,mob,gold);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to turn in <T-NAMESELF> to "+judge.name()+" for the bounty, but can't get "+judge.charStats().hisher()+" attention.");

		return success;
	}

}

