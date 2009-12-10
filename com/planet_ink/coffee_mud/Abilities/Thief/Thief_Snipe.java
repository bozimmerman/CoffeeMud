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
public class Thief_Snipe extends ThiefSkill
{
	public String ID() { return "Thief_Snipe"; }
	public String name(){ return "Snipe";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"SNIPE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 100;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}

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
            if(target instanceof MOB)
            {
                if(CMLib.flags().canBeSeenBy(mob,(MOB)target))
                    return Ability.QUALITY_INDIFFERENT;
                Item w=mob.fetchWieldedItem();
                if((w==null)
                ||(!(w instanceof Weapon)))
                    return Ability.QUALITY_INDIFFERENT;
                Weapon ww=(Weapon)w;
                if(((ww.weaponClassification()!=Weapon.CLASS_RANGED)&&(ww.weaponClassification()!=Weapon.CLASS_THROWN))
                ||(w.maxRange()<=0))
                    return Ability.QUALITY_INDIFFERENT;
                return Ability.QUALITY_MALICIOUS;
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
		if(CMLib.flags().canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely.");
			return false;
		}
		Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to snipe.");
			return false;
		}
		Weapon ww=(Weapon)w;
		if(((ww.weaponClassification()!=Weapon.CLASS_RANGED)&&(ww.weaponClassification()!=Weapon.CLASS_THROWN))
		||(w.maxRange()<=0))
		{
			mob.tell("You need a ranged weapon to snipe.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		int code=CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT;
		String str=auto?"":"<S-NAME> strike(s) <T-NAMESELF> from the shadows!";
		int otherCode=success?code:CMMsg.NO_EFFECT;
		String otherStr=success?str:null;
		CMMsg msg=CMClass.getMsg(mob,target,this,code,str,otherCode,otherStr,otherCode,otherStr);
		if(mob.location().okMessage(mob,msg))
		{
		    boolean alwaysInvis=CMath.bset(mob.baseEnvStats().disposition(),EnvStats.IS_INVISIBLE);
		    if(!alwaysInvis) mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);
		    mob.recoverEnvStats();
		    mob.location().send(mob,msg);
		    CMLib.combat().postAttack(mob,target,w);
		    if(!alwaysInvis) mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()-EnvStats.IS_INVISIBLE);
		    mob.recoverEnvStats();
			if(success)
			{
				MOB oldVictim=target.getVictim();
				MOB oldVictim2=mob.getVictim();
				if(oldVictim==mob) target.makePeace();
				if(oldVictim2==target) mob.makePeace();
				if(mob.fetchEffect("Thief_Hide")==null)
				{
					Ability hide=mob.fetchAbility("Thief_Hide");
					if(hide!=null) hide.invoke(mob,null,false,asLevel);

					mob.location().recoverRoomStats();
					if(CMLib.flags().canBeSeenBy(mob,target))
					{
						target.setVictim(oldVictim);
						mob.setVictim(oldVictim2);
					}
				}
			}
		}
		return success;
	}
}
