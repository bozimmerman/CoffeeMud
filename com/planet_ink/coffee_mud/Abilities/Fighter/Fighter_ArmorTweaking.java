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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Fighter_ArmorTweaking extends FighterSkill
{
	public String ID() { return "Fighter_ArmorTweaking"; }
	public String name(){ return "Armor Tweaking";}
	private static final String[] triggerStrings = {"ARMORTWEAK","TWEAK"};
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int maxRange(){return adjustedMaxInvokerRange(0);}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_ARMORUSE;}
	public int usageType(){return USAGE_MANA;}
	private int armorBonus = 1;
    
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(affected instanceof Item)
		{
			if(((Item)affected).amWearingAt(Wearable.IN_INVENTORY)
			||((invoker()!=null)&&(CMLib.flags().isInTheGame(invoker(),false)&&(((Item)affected).owner()!=invoker()))))
				unInvoke();
		}
	}

	public void setMiscText(String newMiscText) {
	    super.setMiscText(newMiscText);
	    if(newMiscText.length()>0)
	        armorBonus=CMath.s_int(newMiscText);
	}
	
	public void unInvoke()
	{
		if((affected instanceof Item)
		&&(!((Item)affected).amDestroyed())
		&&(((Item)affected).owner() instanceof MOB))
		{
			MOB M=(MOB)((Item)affected).owner();
			if((!M.amDead())&&(CMLib.flags().isInTheGame(M,true))&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY)))
				M.tell(M,affected,null,"<T-NAME> no longer feel(s) quite as snuggly tweaked.");
		}
		super.unInvoke();
	}
	
	public void affectEnvStats(Environmental affected, EnvStats stats)
	{
		if((affected instanceof Item)&&(armorBonus>0)&&(((Item)affected).owner() instanceof MOB))
		{
			stats.setArmor(stats.armor()+armorBonus);
			stats.addAmbiance("^w*^N");
		}
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		Item armor=super.getTarget(mob,null,givenTarget,null,commands,Wearable.FILTER_WORNONLY);
		if(armor==null) return false;
		if((!armor.amWearingAt(Wearable.WORN_ABOUT_BODY))
		&&(!armor.amWearingAt(Wearable.WORN_ARMS))
		&&(!armor.amWearingAt(Wearable.WORN_BACK))
		&&(!armor.amWearingAt(Wearable.WORN_HANDS))
		&&(!armor.amWearingAt(Wearable.WORN_HEAD))
		&&(!armor.amWearingAt(Wearable.WORN_LEGS))
		&&(!armor.amWearingAt(Wearable.WORN_NECK))
		&&(!armor.amWearingAt(Wearable.WORN_TORSO))
		&&(!armor.amWearingAt(Wearable.WORN_WAIST)))
		{
			mob.tell(armor.name()+" can not be tweaked to provide any more benefit.");
			return false;
		}
		if((!auto)&&(mob.isInCombat()))
		{
		    mob.tell("You are a bit too busy to do that right now.");
		    return false;
		}
		int bonus=(int)Math.round(CMath.mul(0.10+(0.10*getXLEVELLevel(mob)),armor.envStats().armor()));
		if(bonus<1)
		{
			mob.tell(armor.name()+" is too weak of an armor to provide any more benefit from tweaking.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String str=auto?armor.name()+" snuggly covers <S-NAME>!":"<S-NAME> tweak(s) <T-NAMESELF> until it is as snuggly protective as possible.";
			CMMsg msg=CMClass.getMsg(mob,armor,this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,armor,asLevel,0);
				Ability A=armor.fetchEffect(ID());
				if(A!=null){ A.setMiscText(""+bonus); A.makeLongLasting();}
				armor.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,armor,"<S-NAME> attempt(s) to tweak <T-NAME>, but just can't get it quite right.");
		return success;
	}

}