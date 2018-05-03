package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class Skill_Stonecunning extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Stonecunning";
	}

	private final static String	localizedName	= CMLib.lang().L("Stonecunning");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ALERT;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected LinkedList<Pair<Room,Boolean>> lastFiveRooms = new LinkedList<Pair<Room,Boolean>>();
	protected volatile boolean doThisRoom	= false;
	protected volatile Room thisRoom = null;

	protected boolean appliesToRoom(Room R)
	{
		return
			(R.domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)
			||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
			||(R.domainType()==Room.DOMAIN_INDOORS_CAVE);
	}

	public String trapCheck(Physical P)
	{
		if(P!=null)
		if(CMLib.utensils().fetchMyTrap(P)!=null)
			return L("@x1 is trapped.\n\r",P.name());
		return "";
	}

	public String trapHere(MOB mob, Room R)
	{
		final StringBuffer msg=new StringBuffer("");
		if(CMLib.flags().canBeSeenBy(R,mob))
			msg.append(trapCheck(R));
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Exit E=R.getExitInDir(d);
			final Room R2=R.getRoomInDir(d);
			if((R2!=null)
			&&(E!=null)
			&&(CMLib.flags().canBeSeenBy(E,mob)))
			{
				final Exit E2=R.getReverseExit(d);
				msg.append(trapCheck(R));
				msg.append(trapCheck(E2));
				msg.append(trapCheck(R2));
			}
		}
		for(Enumeration<Item> i=R.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I!=null)
			&&(I.container()==null)
			&&(CMLib.flags().canBeSeenBy(I, mob)))
				msg.append(trapCheck(I));
		}
		return msg.toString();
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(doThisRoom)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_HIDDEN_ITEMS);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob)
		&&(msg.targetMinor()==CMMsg.TYP_LOOK)
		&&(msg.target() instanceof Room)
		&&(msg.target() == mob.location())
		&&(CMLib.flags().isAliveAwakeMobile(mob,true))
		&&(CMLib.flags().canBeSeenBy(msg.target(), mob))
		&&(thisRoom!=msg.target()))
		{
			final Room R=(Room)msg.target();
			thisRoom = R;
			if(!appliesToRoom(R))
			{
				doThisRoom = false;
				mob.recoverPhyStats();
			}
			else
			{
				synchronized(lastFiveRooms)
				{
					boolean found=false;
					for(Pair<Room,Boolean> p : lastFiveRooms)
					{
						if(p.first == msg.target())
						{
							found=true;
							doThisRoom=p.second.booleanValue();
						}
					}
					if(!found)
					{
						if(lastFiveRooms.size()>5)
							lastFiveRooms.removeFirst();
						doThisRoom = this.proficiencyCheck(mob, 2*getXLEVELLevel(mob), false);
						lastFiveRooms.add(new Pair<Room,Boolean>(R,Boolean.valueOf(doThisRoom)));
					}
				}
			}
			if(doThisRoom)
			{
				msg.addTrailerRunnable(new Runnable()
				{
					final MOB mob=(MOB)affected;
					final Room R=(Room)msg.target();
					
					@Override
					public void run()
					{
						if((R!=null)
						&&(R==thisRoom)
						&&(mob.location()==R))
						{
							final String msg=trapHere(mob, R);
							if(msg.length()>0)
								mob.tell(msg);
						}
					}
				});
			}
		}
		super.executeMsg(myHost, msg);
	}
}
