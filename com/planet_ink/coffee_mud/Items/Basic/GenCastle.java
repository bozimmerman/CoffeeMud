package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;

/*
   Copyright 2021-2021 Bo Zimmerman

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
public class GenCastle extends GenSiegableBoardable
{
	@Override
	public String ID()
	{
		return "GenCastle";
	}

	public GenCastle()
	{
		super();
		setName("the castle [NEWNAME]");
		setDisplayText("the castle [NEWNAME] is here.");
		setMaterial(RawMaterial.RESOURCE_STONE);
		this.doorName="portcullis";
	}

	@Override
	protected Room createFirstRoom()
	{
		final Room R=CMClass.getLocale("StoneRoom");
		R.setDisplayText(L("The Base"));
		return R;
	}


	@Override
	public int getMaxHullPoints()
	{
		return 50 * getArea().numberOfProperIDedRooms();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_SIT)
		&&(msg.target()==this)
		&&(msg.source().location()==owner())
		&&(!CMLib.flags().isFlying(msg.source()))
		&&(!CMLib.flags().isFalling((Physical)msg.target()))
		&&(!CMLib.law().doesHavePriviledgesHere(msg.source(), super.getDestinationRoom(msg.source().location()))))
		{
			final Rideable ride=msg.source().riding();
			if(ride == null)
			{
				msg.source().tell(CMLib.lang().L("You'll need permission to get inside this castle."));
				return false;
			}
			else
			if(!CMLib.flags().isClimbing(msg.source()))
			{
				msg.source().tell(CMLib.lang().L("You'll need permission to get inside this castle from @x1, such as some means to climb up.",ride.name(msg.source())));
				return false;
			}
			else
				msg.source().setRiding(null); // if you're climbing, you're not riding any more
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
		}
	}

	protected boolean isSuitableRoomToBuildIn(final Room R)
	{
		if(R==null)
			return false;
		if((R.domainType()&Room.INDOORS)==Room.INDOORS)
			return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_OUTDOORS_AIR:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return false;
		}
		return true;
	}

	@Override
	protected Room findNearestDocks(final Room R)
	{
		if(R!=null)
		{
			if(isSuitableRoomToBuildIn(R))
				return R;
			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.AREAONLY)
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY);
			final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(final Room R2 : rooms)
			{
				if(isSuitableRoomToBuildIn(R2))
					return R2;
			}
		}
		return null;
	}

	@Override
	protected void doCombatDefeat(final MOB victorM)
	{
		final Room baseR=CMLib.map().roomLocation(this);
		if(baseR!=null)
		{
			final String crumbleString = L("<T-NAME> start(s) crumbling!");
			baseR.show(victorM, this, CMMsg.MSG_OK_ACTION, crumbleString);
			this.announceToNonOuterViewers(victorM, crumbleString);
			//TODO: make it crumble
		}
		if((victorM.riding() instanceof BoardableItem)
		&&((victorM.Name().equals(victorM.riding().Name()))))
		{
			final Area A=((BoardableItem)victorM.riding()).getArea();
			if(A!=null)
			{
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null)
					&&(R.numInhabitants()>0))
					{
						for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							final CMMsg msg2=CMClass.getMsg(M, this, CMMsg.MSG_CAUSESINK, null);
							this.sendAreaMessage(msg2, false);
							R.showSource(M, this, CMMsg.MSG_CAUSESINK, null);
							CMLib.achievements().possiblyBumpAchievement(M, Event.SHIPSSUNK, 1, this);
						}
					}
				}
			}
		}
		if(!CMLib.leveler().postExperienceToAllAboard(victorM.riding(), 500, this))
			CMLib.leveler().postExperience(victorM, null, null, 500, false);
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenCastle))
			return false;
		return super.sameAs(E);
	}
}
