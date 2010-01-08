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
public class Spell_Shrink extends Spell
{
	public String ID() { return "Spell_Shrink"; }
	public String name(){return "Shrink";}
	public String displayText(){return "(Shrunk)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_ITEMS|CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((canBeUninvoked())&&(affected!=null))
		{
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				if((mob.location()!=null)&&(!mob.amDead()))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> return(s) to <S-HIS-HER> normal size.");
			}
			else
			if(affected instanceof Item)
			{
				Item item=(Item)affected;
				if(item.owner()!=null)
				{
					if(item.owner() instanceof Room)
						((Room)item.owner()).showHappens(CMMsg.MSG_OK_VISUAL,item.name()+" returns to its proper size.");
					else
					if(item.owner() instanceof MOB)
						((MOB)item.owner()).tell(item.name()+" returns to its proper size.");
				}
			}
		}
		super.unInvoke();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int str=affectableStats.getStat(CharStats.STAT_STRENGTH);
		int dex=affectableStats.getStat(CharStats.STAT_DEXTERITY);
		affectableStats.setStat(CharStats.STAT_STRENGTH,(str/10)+1);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,(dex*2)+1);
	}

	public void affectEnvStats(Environmental host, EnvStats affectedStats)
	{
		super.affectEnvStats(host,affectedStats);
		int height=(int)Math.round(affectedStats.height()*0.10);
		if(height==0) height=1;
		affectedStats.setHeight(height);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&((target instanceof MOB)||(target instanceof Item)))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> feel(s) somewhat smaller.":"^S<S-NAME> cast(s) a small spell on <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
                boolean isJustUnInvoking=false;
                if(target instanceof Item)
                {
                    Ability A=target.fetchEffect("Spell_Shrink");
                    if((A!=null)&&(A.canBeUninvoked()))
                    {
                        A.unInvoke();
                        isJustUnInvoking=true;
                    }
                }
                else
                if(target instanceof MOB)
                {
                    Ability A=target.fetchEffect("Spell_Grow");
                    if((A!=null)&&(A.canBeUninvoked()))
                    {
                        A.unInvoke();
                        isJustUnInvoking=true;
                    }
                }

                if((!isJustUnInvoking)&&(msg.value()<=0))
                {
    				beneficialAffect(mob,target,asLevel,0);
    				if(target instanceof MOB)
    					CMLib.utensils().confirmWearability((MOB)target);
                }
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to cast a small spell, but fail(s).");

		return success;
	}
}
