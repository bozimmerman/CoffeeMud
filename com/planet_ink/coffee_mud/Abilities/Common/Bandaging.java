package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Bandaging extends CommonSkill implements MendingSkill
{
	public String ID() { return "Bandaging"; }
	public String name(){ return "Bandaging";}
	private static final String[] triggerStrings = {"BANDAGE","BANDAGING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;}

	protected Environmental bandaging=null;
	protected boolean messedUp=false;
	public Bandaging()
	{
		super();
		displayText="You are bandaging...";
		verb="bandaging";
	}
	public boolean supportsMending(Environmental E)
	{ 
		if(!(E instanceof MOB)) return false;
		return (E.fetchEffect("Bleeding")!=null)||(E.fetchEffect("Injury")!=null);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((bandaging==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((bandaging instanceof MOB)&&(!mob.location().isInhabitant((MOB)bandaging)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((bandaging!=null)&&(!aborted))
				{
					if((messedUp)||(bandaging==null))
						commonTell(mob,"You've failed to bandage "+bandaging.name()+"!");
					else
					{
						Ability A=bandaging.fetchEffect("Bleeding");
						if(A!=null) A.unInvoke();
						A=bandaging.fetchEffect("Injury");
						if(A!=null) A.unInvoke();
					}
				}
			}
		}
		super.unInvoke();
	}

    public double healthPct(MOB mob){ return CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints());}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb="taming";
		bandaging=null;
		MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target.fetchEffect("Bleeding")==null)
		&&(target.fetchEffect("Injury")==null))
		{
			super.commonTell(mob,target,null,"<T-NAME> <T-IS-ARE> not bleeding or injured!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,0,auto);
		int duration=3+(int)Math.round(10*(1.0-healthPct(target)))-getXLEVELLevel(mob);
		if(duration<3) duration=3;
		verb="bandaging "+target.name();
		bandaging=target;
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> begin(s) bandaging up <T-YOUPOSS> wounds.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
