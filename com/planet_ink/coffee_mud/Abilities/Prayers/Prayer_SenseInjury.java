package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class Prayer_SenseInjury extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SenseInjury";
	}

	private final static String localizedName = CMLib.lang().L("Sense Injury");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Sense Injury)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	protected Room lastRoom=null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			lastRoom=null;
			mob.tell(L("Your injury sensors fade."));
		}
	}

	private static final List<MOB> empty=new ReadOnlyVector<MOB>(1);
	public List<MOB> getInjured(MOB mob, Room R)
	{
		if(R==null)
			return empty;
		List<MOB> V=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&((M.curState().getHitPoints() < M.maxState().getHitPoints())
				||(M.fetchEffect("BrokenLimbs")!=null)
				||(M.fetchEffect("Bleeding")!=null)
				||(M.fetchEffect("Amputation")!=null)
				||(M.fetchEffect("Injuries")!=null)))
			{
				if(V==null)
					V=new Vector<MOB>();
				V.add(M);
			}
		}
		if(V!=null)
			return V;
		return empty;
	}

	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room R=mob.location().getRoomInDir(d);
			final Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(getInjured(mob,R).size()>0))
			{
				if(last.length()>0)
					dirs+=", "+last;
				last=CMLib.directions().getInDirectionName(d);
			}
		}
		final List<MOB> V=getInjured(mob,mob.location());
		if(V.size()>0)
		{
			boolean didSomething=false;
			for(int v=0;v<V.size();v++)
			{
				final Environmental E=V.get(v);
				if(CMLib.flags().canBeSeenBy(E,mob))
				{
					didSomething=true;
					if(last.length()>0)
						dirs+=", "+last;
					last="in "+E.name();
				}
			}
			if(!didSomething)
			{
				if(last.length()>0)
					dirs+=", "+last;
				last=L("here");
			}
		}

		if((dirs.length()==0)&&(last.length()==0))
			mob.tell(L("You do not sense any injuries."));
		else
		if(dirs.length()==0)
			mob.tell(L("You sense injuries @x1.",last));
		else
			mob.tell(L("You sense injuries @x1, and @x2.",dirs.substring(2),last));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target=mob;
		if((auto)&&(givenTarget!=null))
			target=givenTarget;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) injury senses!"):L("^S<S-NAME> listen(s) for signs of injuries from @x1.^?",hisHerDiety(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> listen(s) to @x1 for a message, but there is no answer.",hisHerDiety(mob)));

		// return whether it worked
		return success;
	}
}
