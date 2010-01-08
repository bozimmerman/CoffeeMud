package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Shatter extends Spell
{
	public String ID() { return "Spell_Shatter"; }
	public String name(){return "Shatter";}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
    public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
    
    public Item getItem(MOB mobTarget)
    {
        Vector goodPossibilities=new Vector();
        Vector possibilities=new Vector();
        for(int i=0;i<mobTarget.inventorySize();i++)
        {
            Item item=mobTarget.fetchInventory(i);
            if((item!=null)
               &&(item.subjectToWearAndTear()))
            {
                if(item.amWearingAt(Wearable.IN_INVENTORY))
                    possibilities.addElement(item);
                else
                    goodPossibilities.addElement(item);
            }
        }
        if(goodPossibilities.size()>0)
            return (Item)goodPossibilities.elementAt(CMLib.dice().roll(1,goodPossibilities.size(),-1));
        else
        if(possibilities.size()>0)
            return (Item)possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1));
        return null;
    }

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((target instanceof MOB)&&(mob!=target))
            {
                Item I=getItem((MOB)target);
                if(I==null)
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=null;
		if(mobTarget!=null)
		{
            target=getItem(mob);
			if(target==null)
				return maliciousFizzle(mob,mobTarget,"<S-NAME> attempt(s) a shattering spell at <T-NAMESELF>, but nothing happens.");
		}

		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);

		if(target==null) return false;
        Room R=CMLib.map().roomLocation(target);
        if(R==null) R=mob.location();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> starts vibrating!":"^S<S-NAME> utter(s) a shattering spell, causing <T-NAMESELF> to vibrate and resonate.^?");
			CMMsg msg2=CMClass.getMsg(mob,mobTarget,this,verbalCastCode(mob,target,auto),null);
			if((R.okMessage(mob,msg))&&((mobTarget==null)||(R.okMessage(mob,msg2))))
			{
				R.send(mob,msg);
				if(mobTarget!=null)
					R.send(mob,msg2);
				if(msg.value()<=0)
				{
					int damage=100+adjustedLevel(mob,asLevel)-target.envStats().level();
					if(CMLib.flags().isABonusItems(target))
						damage=(int)Math.round(CMath.div(damage,2.0));
					switch(target.material()&RawMaterial.MATERIAL_MASK)
					{
					case RawMaterial.MATERIAL_PAPER:
					case RawMaterial.MATERIAL_CLOTH:
					case RawMaterial.MATERIAL_VEGETATION:
					case RawMaterial.MATERIAL_PLASTIC:
					case RawMaterial.MATERIAL_LEATHER:
					case RawMaterial.MATERIAL_FLESH:
						damage=(int)Math.round(CMath.div(damage,3.0));
						break;
					case RawMaterial.MATERIAL_WOODEN:
						damage=(int)Math.round(CMath.div(damage,1.5));
						break;
					case RawMaterial.MATERIAL_GLASS:
					case RawMaterial.MATERIAL_ROCK:
						damage=(int)Math.round(CMath.mul(damage,2.0));
						break;
					case RawMaterial.MATERIAL_PRECIOUS:
						break;
					case RawMaterial.MATERIAL_ENERGY:
						damage=0;
						break;
					}
					target.setUsesRemaining(target.usesRemaining()-damage);
					if(target.usesRemaining()>0)
						target.recoverEnvStats();
					else
					{
						target.setUsesRemaining(100);
						if(mobTarget==null)
							R.show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> is destroyed!");
						else
							R.show(mobTarget,target,CMMsg.MSG_OK_VISUAL,"<T-NAME>, possessed by <S-NAME>, is destroyed!");
						target.unWear();
						target.destroy();
						R.recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) a shattering spell, but nothing happens.");


		// return whether it worked
		return success;
	}
}

