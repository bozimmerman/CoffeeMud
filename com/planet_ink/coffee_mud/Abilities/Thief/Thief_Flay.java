package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Flay extends ThiefSkill
{
	public String ID() { return "Thief_Flay"; }
	public String name(){ return "Flay";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_LEGAL;}
	private static final String[] triggerStrings = {"FLAY"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 100;}
	public int usageType(){return USAGE_MOVEMENT;}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
	    if(!super.okMessage(host,msg)) return false;
	    if((affected instanceof MOB)
	    &&msg.amISource((MOB)affected)
	    &&(msg.targetMinor()==CMMsg.TYP_WEAR)
	    &&(msg.target() instanceof Item)
	    &&((CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_BACK))
        ||(CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_TORSO))))
	    {
	        msg.source().tell("The flayed marks on your back make wearing that too painful.");
	        return false;
	    }
	    return true;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(CMLib.flags().isSitting(mob))
                return Ability.QUALITY_INDIFFERENT;
            if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
                return Ability.QUALITY_INDIFFERENT;
            if(target != null)
            {
                if((!CMLib.flags().isBoundOrHeld(target))&&(!CMLib.flags().isSleeping(target)))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

        if(CMLib.flags().isSitting(mob))
        {
            mob.tell("You need to stand up!");
            return false;
        }
        if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
            return false;
		if((!auto)&&(!CMLib.flags().isBoundOrHeld(target))&&(!CMLib.flags().isSleeping(target)))
		{
			mob.tell(target.name()+" must be prone or bound first.");
			return false;
		}
		for(int i=0;i<target.inventorySize();i++)
		{
		    Item I=target.fetchInventory(i);
		    if((I!=null)&&((I.amWearingAt(Wearable.WORN_BACK))||(I.amWearingAt(Wearable.WORN_TORSO))))
		    {
			    mob.tell(target.name()+" must be remove items worn on the torso or back first.");
			    return false;
		    }
		}
		
		Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if(!auto)
		{
			if((w==null)||(!(w instanceof Weapon)))
			{
				mob.tell("You cannot flay without a weapon!");
				return false;
			}
			ww=(Weapon)w;
			if(ww.weaponClassification()!=Weapon.CLASS_FLAILED)
			{
				mob.tell("You cannot flay with a "+ww.name()+", you need a flailing weapon!");
				return false;
			}
			if(w.material()!=RawMaterial.RESOURCE_LEATHER)
			{
				mob.tell("You cannot flay with a "+ww.name()+", you need a weapon made of leather!");
				return false;
			}
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
			{
				mob.tell("You are too far away to try that!");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,"<S-NAME> flay(s) the bare back of <T-NAMESELF>!");
		boolean makePeace = CMLib.flags().isBound(target) && (mob.getVictim() == null) && (target.getVictim() == null);
		
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			maliciousAffect(mob,target,asLevel,0,-1);
			if(target.fetchEffect(ID())!=null)
			{
				Ability A=CMClass.getAbility("Bleeding");
				if(A!=null) A.invoke(mob,target,true,asLevel);
				if(makePeace)
				{
					mob.makePeace();
					target.makePeace();
				}
			}
		}
		else
		    maliciousFizzle(mob,target,"<S-NAME> attempt(s) flay <T-NAMESELF>, but fail(s).");
		return success;
	}
}

