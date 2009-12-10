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
public class Prayer_MassFreedom extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_MassFreedom"; }
	public String name(){ return "Mass Freedom";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean supportsMending(Environmental E)
	{ 
		if(!(E instanceof MOB)) return false;
		MOB caster=CMClass.getMOB("StdMOB");
		caster.baseEnvStats().setLevel(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL));
		caster.envStats().setLevel(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL));
		boolean canMend=returnOffensiveAffects(caster,E).size()>0;
		caster.destroy();
		return canMend;
	}
	
	public Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=CMClass.getMOB("StdMOB");
		Vector offenders=new Vector();

		CMMsg msg=CMClass.getMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				try
				{
					newMOB.recoverEnvStats();
					A.affectEnvStats(newMOB,newMOB.envStats());
					int clas=A.classificationCode()&Ability.ALL_ACODES;
					if((!CMLib.flags().aliveAwakeMobileUnbound(newMOB,true))
					   ||(CMath.bset(A.flags(),Ability.FLAG_BINDING))
					   ||(!A.okMessage(newMOB,msg)))
					if((A.invoker()==null)
					||((clas!=Ability.ACODE_SPELL)&&(clas!=Ability.ACODE_CHANT)&&(clas!=Ability.ACODE_PRAYER)&&(clas!=Ability.ACODE_SONG))
					||((A.invoker()!=null)
					   &&(A.invoker().envStats().level()<=(caster.envStats().level()+1+(2*super.getXLEVELLevel(caster))))))
					 	offenders.addElement(A);
				}
				catch(Exception e)
				{}
			}
		}
        newMOB.destroy();
		return offenders;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(supportsMending((MOB)target))
                    return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"A feeling of freedom flows through the air":"^S<S-NAME> "+prayWord(mob)+" for freedom, and the area begins to fill with divine glory.^?");
			Room room=mob.location();
			if((room!=null)&&(room.okMessage(mob,msg)))
			{
				room.send(mob,msg);
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB target=room.fetchInhabitant(i);
					if(target==null) break;

					Vector offensiveAffects=returnOffensiveAffects(mob,target);

					if(offensiveAffects.size()>0)
					{
						// it worked, so build a copy of this ability,
						// and add it to the affects list of the
						// affected MOB.  Then tell everyone else
						// what happened.
						for(int a=offensiveAffects.size()-1;a>=0;a--)
							((Ability)offensiveAffects.elementAt(a)).unInvoke();
						if((!CMLib.flags().stillAffectedBy(target,offensiveAffects,false))&&(target.location()!=null))
							target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) less constricted.");
					}
				}
			}
		}
		else
			this.beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for freedom, but nothing happens.");

		// return whether it worked
		return success;
	}
}
