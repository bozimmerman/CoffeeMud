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
public class Prayer_Stoning extends Prayer
{
	public String ID() { return "Prayer_Stoning"; }
	public String name(){ return "Stoning";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected Vector cits=new Vector();

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		MOB mob=(MOB)affected;
	    Room R=mob.location();
		if(R!=null)
		{
			for(int i=0;i<cits.size();i++)
			{
			    MOB M=(MOB)cits.elementAt(i);
			    if((M.location()!=mob.location())||(mob.amDead()))
			    {
			        MUDTracker.wanderAway(M,true,false);
			        M.destroy();
			        M.setLocation(null);
			    }
			    else
			    {
			        if(Dice.rollPercentage()>=50)
			        {
				        int dmg=mob.maxState().getHitPoints()/20;
				        if(dmg<1) dmg=1;
				        Item W=mob.fetchWieldedItem();
				        if(W!=null)
				        {
				            W.baseEnvStats().setDamage(dmg);
				            W.envStats().setDamage(dmg);
				        }
				        MUDFight.postDamage(M,mob,W,dmg,CMMsg.MSG_WEAPONATTACK|CMMsg.TYP_GENERAL,Weapon.TYPE_BASHING,"<S-NAME> stone(s) <T-NAMESELF>!");
			        }
			        else
			            R.show(M,mob,null,CMMsg.TYP_SPEAK,"<S-NAME> shout(s) obscenities at <T-NAMESELF>.");
			    }
			}
		    while(cits.size()<10)
			{
		        MOB M=CMClass.getMOB("AngryCitizen");
		        if(M==null)
		        {
		            unInvoke();
		            break;
		        }
		        else
		        {
		            Room R2=CMClass.getLocale("StdRoom");
		            cits.addElement(M);
		            M.bringToLife(R2,true);
		            MUDTracker.wanderIn(M,R);
		        }
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		Behavior B=null;
		if(mob.location()!=null) B=CoffeeUtensils.getLegalBehavior(mob.location());
		Vector warrants=new Vector();
		if(B!=null)
		{
			warrants.addElement(new Integer(Law.MOD_GETWARRANTSOF));
			warrants.addElement(target.Name());
			if(!B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),target,warrants))
				warrants.clear();
		}
		if(warrants.size()==0)
		{
		    mob.tell("You are not allowed to stone "+target.Name()+" at this time.");
		    return false;
		}
		
		if((!auto)&&(!Sense.isBoundOrHeld(target)))
		{
			mob.tell(target.name()+" must be bound first.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,-target.envStats().level(),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> call(s) for the stoning of <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE);
					for(int i=0;i<warrants.size();i++)
					{
						LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
						W.setCrime("pardoned");
						W.setOffenses(0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> call(s) for the stoning of <T-NAMESELF>.");


		// return whether it worked
		return success;
	}
}
