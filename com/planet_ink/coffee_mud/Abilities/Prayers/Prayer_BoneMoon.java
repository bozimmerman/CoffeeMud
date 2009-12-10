package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_BoneMoon extends Prayer
{
	public String ID() { return "Prayer_BoneMoon"; }
	public String name(){ return "Bone Moon";}
	public String displayText(){ return "(Bone Moon)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_MOONALTERING;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int level=1;

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null) return;
		if(canBeUninvoked())
		{
			Room R=CMLib.map().roomLocation(affected);
			if((R!=null)&&(CMLib.flags().isInTheGame(affected,true)))
				R.showHappens(CMMsg.MSG_OK_VISUAL,"The bone moon fades.");
		}
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			DeadBody B=null;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&(I instanceof DeadBody)
				&&(I.container()==null)
				&&(!((DeadBody)I).playerCorpse())
				&&(((DeadBody)I).mobName().length()>0))
				{
					B=(DeadBody)I;
					break;
				}
			}
			if(B!=null)
			{
				new Prayer_AnimateSkeleton().makeSkeletonFrom(R,B,null,level);
				B.destroy();
				level+=3;
			}
		}
		return super.tick(ticking,tickID);
	}

   public int castingQuality(MOB mob, Environmental target)
   {
        if(mob!=null)
        {
            if((mob.isMonster())&&(mob.isInCombat()))
                return Ability.QUALITY_INDIFFERENT;
            Room R=mob.location();
            if(R!=null)
            {
                for(int a=0;a<R.numEffects();a++)
                {
                    Ability A=R.fetchEffect(a);
                    if((A!=null)
                    &&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
                        return Ability.QUALITY_INDIFFERENT;
                }
            }
        }
        return super.castingQuality(mob,target);
    }
   
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already under a bone moon.");
			return false;
		}
		for(int a=0;a<target.numEffects();a++)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
			{
				mob.tell("The moon is already under "+A.name()+", and can not be changed until this magic is gone.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The Bone Moon rises over <S-NAME>.");
				level=1;
				if(CMLib.law().doesOwnThisProperty(mob,target))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom(target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for the Bone Moon, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
