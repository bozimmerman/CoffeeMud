package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Chant_TremorSense extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_TremorSense";
	}

	private final static String	localizedName	= CMLib.lang().L("Tremor Sense");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Tremor Sense)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS | CAN_ROOMS;
	}

	protected Vector<Room>	rooms	= new Vector<Room>();

	@Override
	public CMObject copyOf()
	{
		final Chant_TremorSense obj=(Chant_TremorSense)super.copyOf();
		obj.rooms=new Vector<Room>();
		obj.rooms.addAll(rooms);
		return obj;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> tremor sense fades."));
		}
		for(int r=0;r<rooms.size();r++)
		{
			final Room R=rooms.elementAt(r);
			final Ability A=R.fetchEffect(ID());
			if((A!=null)&&(A.invoker()==mob))
				A.unInvoke();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected==null)
			return;
		if(affected instanceof MOB)
		{
			if(msg.amISource((MOB)affected)
			&&((msg.sourceMinor()==CMMsg.TYP_STAND)
			   ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)))
				unInvoke();
		}
		else
		if(affected instanceof Room)
		{
			if((msg.target()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!CMLib.flags().isInFlight(msg.source()))
			&&(invoker!=null)
			&&(invoker.location()!=null))
			{
				if(invoker.location()==affected)
					invoker.tell(L("You feel footsteps around you."));
				else
				{
					final int dir=CMLib.tracking().radiatesFromDir((Room)affected,rooms);
					if(dir>=0)
						invoker.tell(L("You feel footsteps @x1",CMLib.directions().getInDirectionName(dir)));
				}
			}
			else
			if((msg.tool() instanceof Ability)
			&&((msg.tool().ID().equals("Prayer_Tremor"))
				||(msg.tool().ID().endsWith("_Earthquake"))))
			{
				if(invoker.location()==affected)
					invoker.tell(L("You feel a ferocious rumble."));
				else
				{
					final int dir=CMLib.tracking().radiatesFromDir((Room)affected,rooms);
					if(dir>=0)
						invoker.tell(L("You feel a ferocious rumble @x1",CMLib.directions().getInDirectionName(dir)));
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already sensing tremors."));
			return false;
		}

		if((!CMLib.flags().isSitting(mob))||(mob.riding()!=null))
		{
			mob.tell(L("You must be sitting on the ground for this chant to work."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("@x1<T-NAME> gain(s) a sense of the earth!^?",L(auto?"":"^S<S-NAME> chant(s) to <S-HIM-HERSELF>.  ")));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				rooms=new Vector<Room>();
				TrackingLibrary.TrackingFlags flags;
				flags = CMLib.tracking().newFlags()
						.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
						.plus(TrackingLibrary.TrackingFlag.NOAIR)
						.plus(TrackingLibrary.TrackingFlag.NOWATER);
				int range=5 +(super.getXMAXRANGELevel(mob)/2);
				CMLib.tracking().getRadiantRooms(mob.location(),rooms,flags,null,range,null);
				for(int r=0;r<rooms.size();r++)
				{
					final Room R=rooms.elementAt(r);
					if((R!=mob.location())
					&&(R.domainType()!=Room.DOMAIN_INDOORS_AIR)
					&&(!CMLib.flags().isWateryRoom(R))
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
						beneficialAffect(mob,R,asLevel,0);
				}
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s), but nothing happens."));

		// return whether it worked
		return success;
	}
}
