package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Druid_RecoverVoice extends StdAbility
{
	public String ID() { return "Druid_RecoverVoice"; }
	public String name(){ return "Recover Voice";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	private static final String[] triggerStrings = {"VRECOVER","RECOVERVOICE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}


	public static Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=CMClass.getMOB("StdMOB");
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				newMOB.recoverEnvStats();
				A.affectEnvStats(newMOB,newMOB.envStats());
				if((!Sense.canSpeak(newMOB))
				&&((A.invoker()==null)
				   ||((A.invoker()!=null)
					  &&(A.invoker().envStats().level()<=caster.envStats().level()+10))))
						offenders.addElement(A);
			}
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		Vector offensiveAffects=returnOffensiveAffects(mob,mob);
		if((!success)||(offensiveAffects.size()==0))
			mob.tell("You failed in your vocal meditation.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.TYP_GENERAL|CMMsg.MASK_GENERAL|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
			}
		}
		return success;
	}
}

