package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Bandaging extends CommonSkill implements MendingSkill
{
	@Override public String ID() { return "Bandaging"; }
	private final static String localizedName = CMLib.lang()._("Bandaging");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"BANDAGE","BANDAGING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}
	@Override public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;}

	protected Physical bandaging=null;
	protected boolean messedUp=false;
	public Bandaging()
	{
		super();
		displayText=_("You are bandaging...");
		verb=_("bandaging");
	}
	@Override
	public boolean supportsMending(Physical item)
	{
		if(!(item instanceof MOB)) return false;
		return (item.fetchEffect("Bleeding")!=null)||(item.fetchEffect("Injury")!=null);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
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
			if(mob.curState().adjHitPoints(super.getXLEVELLevel(invoker())+(int)Math.round(CMath.div(mob.phyStats().level(),2.0)),mob.maxState()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> mend(s) and heal(s)."));
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if((bandaging!=null)&&(!aborted))
				{
					if((messedUp)||(bandaging==null))
						commonTell(mob,_("You've failed to bandage @x1!",bandaging.name()));
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

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=_("bandaging");
		bandaging=null;
		final MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target.fetchEffect("Bleeding")==null)
		&&(target.fetchEffect("Injury")==null))
		{
			super.commonTell(mob,target,null,_("<T-NAME> <T-IS-ARE> not bleeding or injured!"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,0,auto);
		int duration=3+(int)Math.round(10*(1.0-healthPct(target)))-getXLEVELLevel(mob);
		if(duration<3) duration=3;
		verb=_("bandaging @x1",target.name());
		bandaging=target;
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,_("<S-NAME> begin(s) bandaging up <T-YOUPOSS> wounds."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
