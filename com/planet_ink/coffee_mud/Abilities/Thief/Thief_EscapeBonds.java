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
public class Thief_EscapeBonds extends ThiefSkill
{
	public String ID() { return "Thief_EscapeBonds"; }
	public String name(){ return "Escape Bonds";}
	public String displayText(){return "(Slipping from your bonds)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_BINDING;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"ESCAPEBONDS","ESCAPE"};
	public String[] triggerStrings(){return triggerStrings;}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((!CMLib.flags().aliveAwakeMobile(mob,true))
			||(!CMLib.flags().isBound(mob)))
			{ unInvoke(); return false;}
			Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_BINDING);
			if(V.size()==0)
			{ unInvoke(); return false;}
			int newStrength=mob.charStats().getStat(CharStats.STAT_STRENGTH)
                           +getXLEVELLevel(mob)
						   +(mob.charStats().getStat(CharStats.STAT_DEXTERITY)*2);
			CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HANDS,"<S-NAME> slip(s) and wiggle(s) in <S-HIS-HER> bonds.");
			for(int v=0;v<V.size();v++)
			{
				mob.charStats().setStat(CharStats.STAT_STRENGTH,newStrength);
				Ability A=(Ability)V.elementAt(v);
				if(A.okMessage(mob,msg)) A.executeMsg(mob,msg);
			}
			mob.recoverCharStats();
		}
		return true;
	}

	public void unInvoke()
	{
		MOB M=(MOB)affected;
		super.unInvoke();
		if((M!=null)&&(!M.amDead()))
		{
			if(!CMLib.flags().isBound(M))
				M.tell("You slip free of your bonds.");
			else
				M.tell("You stop trying to slip free of your bonds.");
		}
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.fetchEffect(this.ID())!=null)
                return Ability.QUALITY_INDIFFERENT;
            if((!CMLib.flags().aliveAwakeMobile(mob,true))||(!CMLib.flags().isBound(mob)))
                return Ability.QUALITY_INDIFFERENT;
            Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_BINDING);
            if(V.size()==0)
                return Ability.QUALITY_INDIFFERENT;
            return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
        }
        return super.castingQuality(mob,target);
    }

    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already trying to slip free of <S-HIS-HER> bonds.");
			return false;
		}
		if((!CMLib.flags().aliveAwakeMobile(mob,true))||(!CMLib.flags().isBound(mob)))
		{
			mob.tell(target,null,null,"<T-NAME> <T-IS-ARE> not bound!");
			return false;
		}
		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_BINDING);
		if(V.size()==0)
		{
			mob.tell(target,null,null,"<T-NAME> <T-IS-ARE> not bound by anything which can be slipped free of.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT),auto?"<T-NAME> start(s) slipping from <T-HIS-HER> bonds.":"<S-NAME> attempt(s) to slip free of <S-HIS-HER> bonds.");
		if(!success)
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to slip free of <S-HIS-HER> bonds, but can't seem to concentrate.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}