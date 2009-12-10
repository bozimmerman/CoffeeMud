package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Fighter_CalledStrike extends FighterSkill
{
	public String ID() { return "Fighter_CalledStrike"; }
	public String name(){ return "Called Strike";}
	private static final String[] triggerStrings = {"CALLEDSTRIKE"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String displayText(){return "";}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public int usageType(){return USAGE_MOVEMENT;}

	protected String gone="";
	protected MOB target=null;
	protected int hpReq=9;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-100);
	}

	protected boolean amputate()
	{
		MOB mob=target;
		if(mob==null) return false;
		Amputator A=(Amputator)mob.fetchEffect("Amputation");
		if(A==null)	A=(Amputator)CMClass.getAbility("Amputation");
		A.amputate(mob,A,gone);
        if(mob.fetchEffect(A.ID())==null)
			mob.addNonUninvokableEffect(A);
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(target==null))
		   return super.okMessage(myHost,msg);
		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.amITarget(target))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			int hurtAmount=msg.value();
			if(hurtAmount>=(target.baseState().getHitPoints()/(hpReq-getXLEVELLevel(invoker()))))
			{
				hurtAmount=(target.baseState().getHitPoints()/(hpReq-getXLEVELLevel(invoker())));
				msg.setValue(msg.value()+hurtAmount);
				amputate();
			}
			else
				mob.tell(mob,target,null,"You failed to cut off <T-YOUPOSS> '"+gone+"'.");
			unInvoke();
		}
		return super.okMessage(myHost,msg);
	}

	protected boolean prereqs(MOB mob, boolean quiet)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
		    if(!quiet)
			mob.tell("You are too far away to perform a called strike!");
			return false;
		}

		Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon)))
		{
            if(!quiet)
			mob.tell("You need a weapon to perform a called strike!");
			return false;
		}
		Weapon wp=(Weapon)w;
		if(wp.weaponType()!=Weapon.TYPE_SLASHING)
		{
            if(!quiet)
			mob.tell("You cannot amputate with "+wp.name()+"!");
			return false;
		}
		return true;
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!prereqs(mob,true))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!prereqs(mob,false)) return false;

		gone="";
		hpReq=9;
		target=null;

		if(commands.size()>0)
		{
			String s=(String)commands.firstElement();
			if(mob.location().fetchInhabitant(s)!=null)
				target=mob.location().fetchInhabitant(s);
			if((target!=null)&&(!CMLib.flags().canBeSeenBy(target,mob)))
			{
				mob.tell("You can't see '"+s+"' here.");
				return false;
			}
			if(target!=null)
				commands.removeElementAt(0);
		}
		if(target==null)
			target=mob.getVictim();
		if((target==null)||(target==mob))
		{
			mob.tell("Do this to whom?");
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target.name()+" already has a call against one of "+target.charStats().hisher()+" limbs.");
			return false;
		}

		Amputator A=(Amputator)target.fetchEffect("Amputation");
		if(A==null)	A=(Amputator)CMClass.getAbility("Amputation");

		Vector remainingLimbList=A.remainingLimbNameSet(target);
		if(remainingLimbList.size()==0)
		{
			if(!auto)
				mob.tell("There is nothing left on "+target.name()+" to cut off!");
			return false;
		}
		if(mob.isMonster())
			gone=(String)remainingLimbList.elementAt(CMLib.dice().roll(1,remainingLimbList.size(),-1));
		else
		if(commands.size()<=0)
		{
			mob.tell("You must specify a body part to cut off.");
			StringBuffer str=new StringBuffer("Parts include: ");
			for(int i=0;i<remainingLimbList.size();i++)
				str.append(((String)remainingLimbList.elementAt(i))+", ");
			mob.tell(str.toString().substring(0,str.length()-2)+".");
			return false;
		}
		else
		{
			String off=CMParms.combine(commands,0);
			if((off.equalsIgnoreCase("head"))
			&&(target.charStats().getBodyPart(Race.BODY_HEAD)>=0))
		    {
				gone=Race.BODYPARTSTR[Race.BODY_HEAD].toLowerCase();
				hpReq=3;
			}
			else
			for(int i=0;i<remainingLimbList.size();i++)
				if(((String)remainingLimbList.elementAt(i)).toUpperCase().startsWith(off.toUpperCase()))
				{
					gone=(String)remainingLimbList.elementAt(i);
					break;
				}
			if(gone.length()==0)
			{
				mob.tell("'"+off+"' is not a valid body part.");
				StringBuffer str=new StringBuffer("Parts include: ");
				for(int i=0;i<remainingLimbList.size();i++)
					str.append(((String)remainingLimbList.elementAt(i))+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&(gone.length()>0))
		{
            CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,"^F^<FIGHT^><S-NAME> call(s) '"+gone+"'!^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
				invoker=mob;
				beneficialAffect(mob,mob,asLevel,2);
				Ability A2=target.fetchEffect("Injury");
				if(A2!=null) A2.setMiscText(mob.Name()+"/"+gone);
				mob.recoverEnvStats();
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> call(s) '"+gone+"', but fail(s) <S-HIS-HER> attack.");

		// return whether it worked
		return success;
	}
}
