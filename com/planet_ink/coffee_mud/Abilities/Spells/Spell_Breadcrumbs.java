package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_Breadcrumbs extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Breadcrumbs";
	}

	private final static String localizedName = CMLib.lang().L("Breadcrumbs");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	public Vector<Room> trail=null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your breadcrumbs fade away."));
		trail=null;
	}

	@Override
	public String displayText()
	{
		final StringBuffer str=new StringBuffer(L("(Breadcrumb Trail: "));
		if(trail!=null)
		{
			synchronized(trail)
			{
				Room lastRoom=null;
				for(int v=trail.size()-1;v>=0;v--)
				{
					final Room R=trail.elementAt(v);
					if(lastRoom!=null)
					{
						int dir=-1;
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							if(lastRoom.getRoomInDir(d)==R)
							{
								dir=d;
								break;
							}
						}
						if(dir>=0)
							str.append(CMLib.directions().getDirectionName(dir)+" ");
						else
							str.append(L("Unknown "));
					}
					lastRoom=R;
				}
			}
		}
		return str.toString()+")";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(trail!=null)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room))
		{
			final Room newRoom=(Room)msg.target();
			boolean kill=false;
			int t=0;
			while(t<trail.size())
			{
				if(kill)
					trail.removeElement(trail.elementAt(t));
				else
				{
					final Room R=trail.elementAt(t);
					if(R==newRoom)
						kill=true;
					t++;
				}
			}
			if(kill)
				return;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room adjacentRoom=newRoom.getRoomInDir(d);
				if((adjacentRoom!=null)
				   &&(newRoom.getExitInDir(d)!=null))
				{
					kill=false;
					t=0;
					while(t<trail.size())
					{
						if(kill)
							trail.removeElement(trail.elementAt(t));
						else
						{
							final Room R=trail.elementAt(t);
							if(R==adjacentRoom)
								kill=true;
							t++;
						}
					}
				}
			}
			trail.addElement(newRoom);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if(target==null)
			return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already dropping breadcrumbs."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) mysterious breadcrumbs."):L("^S<S-NAME> invoke(s) the mystical breadcrumbs.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				trail=new Vector<Room>();
				trail.addElement(mob.location());
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke breadcrumbs, but fail(s)."));

		// return whether it worked
		return success;
	}
}
