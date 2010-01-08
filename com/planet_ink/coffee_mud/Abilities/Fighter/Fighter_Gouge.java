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
public class Fighter_Gouge extends FighterSkill
{
	boolean doneTicking=false;
	public String ID() { return "Fighter_Gouge"; }
	public String name(){ return "Gouge";}
	public String displayText(){ return "(Gouged Eyes)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"GOUGE"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	protected int overrideMana(){return 100;}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your eyes feel better.");
	}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			   &&((I.amWearingAt(Wearable.WORN_WIELD))
			      ||(I.amWearingAt(Wearable.WORN_HELD))))
				return true;
		}
		return false;
	}

	public int castingQuality(MOB mob, Environmental target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(((mob.charStats().getBodyPart(Race.BODY_HAND)<=0))
			||((mob.charStats().getMyRace().bodyMask()[Race.BODY_HAND]<=0)
			   &&(mob.charStats().getBodyPart(Race.BODY_FOOT)<=0)))
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)<=0))
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(!CMLib.flags().canSee((MOB)target)))
                return Ability.QUALITY_INDIFFERENT;
			if(anyWeapons(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!auto)
		&&((mob.charStats().getBodyPart(Race.BODY_HAND)<=0))
		||((mob.charStats().getMyRace().bodyMask()[Race.BODY_HAND]<=0)
		   &&(mob.charStats().getBodyPart(Race.BODY_FOOT)<=0)))
		{
			mob.tell("You need hands to gouge.");
			return false;
		}

		if((!auto)&&(target.charStats().getBodyPart(Race.BODY_EYE)<=0))
		{
			mob.tell(target.name()+" has no eyes!");
			return false;
		}

		if((!auto)&&(anyWeapons(mob)))
		{
			mob.tell("Your hands must be free to gouge.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		if((success)&&(hit))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":"^F^<FIGHT^><S-NAME> gouge(s) at <T-YOUPOSS> eyes!^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> blinded!");
				maliciousAffect(mob,target,asLevel,5,-1);
                Amputator A=(Amputator)target.fetchEffect("Amputation");
                if(A==null) A=(Amputator)CMClass.getAbility("Amputation");
                Vector remainingLimbList=A.remainingLimbNameSet(target);
                String gone=null;
                for(int i=0;i<remainingLimbList.size();i++)
                    if(((String)remainingLimbList.elementAt(i)).toUpperCase().endsWith("EYE"))
                    {
                        gone=(String)remainingLimbList.elementAt(i);
                        break;
                    }
                if(gone!=null)
                {
                    Ability A2=CMClass.getAbility("Injury");
                    if(A2!=null)
                    {
                        A2.setMiscText(mob.Name()+"/"+gone);
                        CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSG_DAMAGE,"<DAMAGE> <T-NAME>.");
                        msg2.setValue(target.maxState().getHitPoints()/(20-getXLEVELLevel(mob)));
                        if(!A2.invoke(mob,CMParms.makeVector(msg2),target,true,0))
                        {
                            A2=target.fetchEffect("Injury");
                            if( A2 != null )
                            {
                              A2.setMiscText(mob.Name()+"/"+gone);
                              A2.okMessage(target,msg2);
                            }
                        }
                    }
                }
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to gouge <T-YOUPOSS> eyes, but fail(s).");
		return success;
	}
}
