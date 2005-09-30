package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_Shrink extends Spell
{
	public String ID() { return "Spell_Shrink"; }
	public String name(){return "Shrink";}
	public String displayText(){return "(Shrunk)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_ITEMS|CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

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
		int str=affectableStats.getStat(CharStats.STRENGTH);
		int dex=affectableStats.getStat(CharStats.DEXTERITY);
		affectableStats.setStat(CharStats.STRENGTH,(str/10)+1);
		affectableStats.setStat(CharStats.DEXTERITY,(dex*2)+1);
	}

	public void affectEnvStats(Environmental host, EnvStats affectedStats)
	{
		super.affectEnvStats(host,affectedStats);
		int height=(int)Math.round(new Integer(affectedStats.height()).doubleValue()*0.10);
		if(height==0) height=1;
		affectedStats.setHeight(height);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental E=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(E==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if((success)&&((E instanceof MOB)||(E instanceof Item)))
		{
			FullMsg msg=new FullMsg(mob,E,this,affectType(auto),auto?"<T-NAME> feel(s) somewhat smaller.":"^S<S-NAME> cast(s) a small spell on <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
                boolean isJustUnInvoking=false;
                if(E instanceof Item)
                {
                    Ability A=E.fetchEffect("Spell_Shrink");
                    if((A!=null)&&(A.canBeUninvoked()))
                    {
                        A.unInvoke();
                        isJustUnInvoking=true;
                    }
                }
                else
                if(E instanceof MOB)
                {
                    Ability A=E.fetchEffect("Spell_Grow");
                    if((A!=null)&&(A.canBeUninvoked()))
                    {
                        A.unInvoke();
                        isJustUnInvoking=true;
                    }
                }
                
                if((!isJustUnInvoking)&&(msg.value()<=0))
                {
    				beneficialAffect(mob,E,asLevel,0);
    				if(E instanceof MOB)
    					((MOB)E).confirmWearability();
                }
			}
		}
		else
			beneficialWordsFizzle(mob,E,"<S-NAME> attempt(s) to cast a small spell, but fail(s).");

		return success;
	}
}
