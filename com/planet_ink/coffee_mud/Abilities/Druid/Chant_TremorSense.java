package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Chant_TremorSense extends Chant
{
	public String ID() { return "Chant_TremorSense"; }
	public String name(){return "Tremor Sense";}
	public String displayText(){return "(Tremor Sense)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	protected Vector rooms=new Vector();

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> tremor sense fades.");
		for(int r=0;r<rooms.size();r++)
		{
			Room R=(Room)rooms.elementAt(r);
			Ability A=R.fetchEffect(ID());
			if((A!=null)&&(A.invoker()==mob))
				A.unInvoke();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected==null) return;
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
					invoker.tell("You feel footsteps around you.");
				else
				{
					int dir=CMLib.tracking().radiatesFromDir((Room)affected,rooms);
					if(dir>=0)
						invoker.tell("You feel footsteps "+Directions.getInDirectionName(dir));
				}
			}
			else
			if((msg.tool() instanceof Ability)
			&&((msg.tool().ID().equals("Prayer_Tremor"))
				||(msg.tool().ID().endsWith("_Earthquake"))))
			{
				if(invoker.location()==affected)
					invoker.tell("You feel a ferocious rumble.");
				else
				{
					int dir=CMLib.tracking().radiatesFromDir((Room)affected,rooms);
					if(dir>=0)
						invoker.tell("You feel a ferocious rumble "+Directions.getInDirectionName(dir));
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already sensing tremors.");
			return false;
		}

		if((!CMLib.flags().isSitting(mob))||(mob.riding()!=null))
		{
			mob.tell("You must be sitting on the ground for this chant to work.");
			return false;
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"":"^S<S-NAME> chant(s) to <S-HIM-HERSELF>.  ")+"<T-NAME> gain(s) a sense of the earth!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				rooms=new Vector();
				TrackingLibrary.TrackingFlags flags;
				flags = new TrackingLibrary.TrackingFlags()
						.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
						.add(TrackingLibrary.TrackingFlag.NOAIR)
						.add(TrackingLibrary.TrackingFlag.NOWATER);
				CMLib.tracking().getRadiantRooms(mob.location(),rooms,flags,null,5,null);
				for(int r=0;r<rooms.size();r++)
				{
					Room R=(Room)rooms.elementAt(r);
					if((R!=mob.location())
					&&(R.domainType()!=Room.DOMAIN_INDOORS_AIR)
					&&(R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
					&&(R.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
						beneficialAffect(mob,R,asLevel,0);
				}
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing happens.");


		// return whether it worked
		return success;
	}
}
