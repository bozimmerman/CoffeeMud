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
public class Fighter_BodyToss extends FighterSkill
{
	public String ID() { return "Fighter_BodyToss"; }
	public String name(){ return "Body Toss";}
	private static final String[] triggerStrings = {"BODYTOSS"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_GRAPPLING;}
	public int usageType(){return USAGE_MOVEMENT;}

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
			if(anyWeapons(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.rangeToTarget()>0)
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_ARM)<=1)
				return Ability.QUALITY_INDIFFERENT;
			if(target.baseEnvStats().weight()>(mob.baseEnvStats().weight()*2))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob.getVictim();
		if(target==null)
		{
			mob.tell("You can only do this in combat!");
			return false;
		}
		if(anyWeapons(mob))
		{
			mob.tell("You must be unarmed to use this skill.");
			return false;
		}
		if(mob.rangeToTarget()>0)
		{
			mob.tell("You must get closer to "+target.charStats().himher()+" first!");
			return false;
		}
		if(CMLib.flags().isSitting(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_ARM)<=1)
		{
			mob.tell("You need arms to do this.");
			return false;
		}
		if(target.baseEnvStats().weight()>(mob.baseEnvStats().weight()*2))
		{
			mob.tell(target.name()+" is too big for you to toss!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),"^F^<FIGHT^><S-NAME> pick(s) up <T-NAMESELF> and toss(es) <T-HIM-HER> into the air!^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int dist=2+getXLEVELLevel(mob);
				if(mob.location().maxRange()<2) dist=mob.location().maxRange();
				mob.setAtRange(dist);
				target.setAtRange(dist);
				CMLib.combat().postDamage(mob,target,this,CMLib.dice().roll(1,12,0),CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"The hard landing <DAMAGE> <T-NAME>!");
				if(mob.getVictim()==null) mob.setVictim(null); // correct range
				if(target.getVictim()==null) target.setVictim(null); // correct range
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to pick up <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
