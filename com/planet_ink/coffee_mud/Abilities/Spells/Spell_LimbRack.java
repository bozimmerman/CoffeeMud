package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
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
public class Spell_LimbRack extends Spell
{
	public String ID() { return "Spell_LimbRack"; }
	public String name(){return "Limb Rack";}
	public String displayText(){return "(Being pulled apart)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	public Vector limbsToRemove=new Vector();
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if(!super.tick(ticking,tickID))
	        return false;
	    if(invoker==null) return false;
	    MOB mob=(MOB)affected;
	    if(mob.location()!=null)
	    {
	        String str=(text().equalsIgnoreCase("ARMSONLY"))?
		        "<S-NAME> <S-IS-ARE> having <S-HIS-HER> arms pulled from <S-HIS-HER> body!"
		        :"<S-NAME> <S-IS-ARE> having <S-HIS-HER> arms and legs pulled from <S-HIS-HER> body!";
	        MUDFight.postDamage(invoker,mob,this,mob.maxState().getHitPoints()/10,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_BURSTING,str);
	    }
	    
	    return true;
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
	    if((msg.sourceMinor()==CMMsg.TYP_DEATH)
        &&(affected instanceof MOB)
        &&(msg.amISource((MOB)affected))
        &&(msg.source().location()!=null))
	    {
	        MOB mob=msg.source();
	        if(text().equalsIgnoreCase("ARMSONLY"))
		        mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> has <S-HIS-HER> arms TORN OFF!");
	        else
		        mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> has <S-HIS-HER> arms and legs TORN OFF!");
			Amputation A=(Amputation)mob.fetchEffect("Amputation");
			boolean newOne=false;
			if(A==null)
			{
				A=new Amputation();
				newOne=true;
			}
			for(int i=0;i<limbsToRemove.size();i++)
				Amputation.amputate(mob,A,(String)limbsToRemove.elementAt(i));
			if(newOne==true)
			{
				mob.addAbility(A);
				A.autoInvocation(mob);
			}
			else
			{
				Ability A2=mob.fetchAbility(A.ID());
				if(A2!=null) A2.setMiscText(A.text());
			}
			mob.confirmWearability();
	    }
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Amputation A=(Amputation)target.fetchEffect("Amputation");
		if(A==null)	A=new Amputation();
		Vector remainingLimbList=A.remainingLimbNameSet(target);
		for(int i=remainingLimbList.size()-1;i>=0;i--)
		{
		    String gone=(String)remainingLimbList.elementAt(i);
		    if((!gone.toUpperCase().endsWith(" ARM"))
		    &&(!gone.toUpperCase().endsWith(" LEG")))
		        remainingLimbList.removeElementAt(i);
		}
		if(remainingLimbList.size()==0)
		{
			if(!auto)
				mob.tell("There is nothing left on "+target.name()+" to rack off!");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"!":"^S<S-NAME> invoke(s) a stretching spell upon <T-NAMESELF>"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				super.maliciousAffect(mob,target,asLevel,12,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) stretchingly at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}
