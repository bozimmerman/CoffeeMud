package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant extends StdAbility
{
	public String ID() { return "Chant"; }
	public String name(){ return "a Druidic Chant";}
	public String displayText(){return "(in the natural order)";}
    protected boolean renderedMundane=false;
	protected int affectType(boolean auto)
    {
        int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
        if(renderedMundane)
        {
            affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH;
            if(quality()==Ability.MALICIOUS)
                affectType=affectType|CMMsg.MASK_MALICIOUS;
        }
        else
        if(quality()==Ability.MALICIOUS)
            affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
            
        if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
        return affectType;
	}
    protected int verbalCastMask(boolean auto)
    {
        int affectType=CMMsg.MSK_CAST_MALICIOUS_VERBAL;
        if(renderedMundane)
        {
            affectType=CMMsg.MASK_SOUND|CMMsg.MASK_MOUTH;
            if(quality()==Ability.MALICIOUS)
                affectType=affectType|CMMsg.MASK_MALICIOUS;
        }
        if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
        return affectType;
    }
	private static final String[] triggerStrings = {"CHANT","CH"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

    public void setMiscText(String newText)
    {
        if(newText.equalsIgnoreCase("render mundane"))
            renderedMundane=true;
        else
            super.setMiscText(newText);
    }
	public int classificationCode()	{ return Ability.CHANT;	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(mob.isMine(this))
		&&(Dice.rollPercentage()<50))
		{
			if(!appropriateToMyFactions(mob))
			{
				mob.tell("Extreme emotions disrupt your chant.");
				return false;
			}
			else
			if(!CoffeeUtensils.armorCheck(mob,CharClass.ARMOR_LEATHER))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
				return false;
			}
		}
		return true;
	}
}
