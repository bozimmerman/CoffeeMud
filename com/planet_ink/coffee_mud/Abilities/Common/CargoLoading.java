package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class CargoLoading extends CommonSkill
{
	@Override
	public String ID()
	{
		return "CargoLoading";
	}

	private final static String localizedName = CMLib.lang().L("Cargo Loading");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "CARGO"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_BINDING;
	}

	@Override
	protected boolean allowedWhileMounted()
	{
		return false;
	}

	protected Item		loadingI		= null;
	protected Room		cargoR			= null;
	protected Room		fromR			= null;
	protected boolean	loading			= true;

	public CargoLoading()
	{
		super();
		displayText=L("You are loading cargo...");
		verb=L("loading");
	}

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(25,mob,level,5);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return super.tick(ticking,tickID);
	}

	final TrackingLibrary.TrackingFlags rflags = CMLib.tracking().newFlags()
												.plus(TrackingLibrary.TrackingFlag.NOAIR)
												.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
												.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY);
	public boolean canLoadCargoHere(final MOB mob, final Item I, final Room R)
	{
		if((R.domainType()&Room.INDOORS)==0)
			return false;
		final MOB dropM=CMClass.getFactoryMOB("cargo loader",I.phyStats().level(),R);
		try
		{
			final CMMsg msg=CMClass.getMsg(dropM, I, null, CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP, CMMsg.MSG_DROP, CMMsg.NO_EFFECT, null);
			if( R.okMessage(dropM, msg) )
			{
				final List<Room> radiusRooms=CMLib.tracking().getRadiantRooms(R, rflags, 5+super.getXLEVELLevel(mob));
				return radiusRooms.contains(mob.location());
			}
			return false;
		}
		finally
		{
			dropM.destroy();
		}
	}

	public boolean canUnloadCargoFromHere(final MOB mob, final Item I, final Room R)
	{
		if((R.domainType()&Room.INDOORS)==0)
			return false;
		final MOB dropM=CMClass.getFactoryMOB("cargo loader",I.phyStats().level(),R);
		try
		{
			final List<Room> radiusRooms=CMLib.tracking().getRadiantRooms(R, rflags, 5+super.getXLEVELLevel(mob));
			return radiusRooms.contains(mob.location());
		}
		finally
		{
			dropM.destroy();
		}
	}

	@Override
	public void unInvoke()
	{
		final boolean isaborted=aborted;
		final Environmental aff=affected;
		super.unInvoke();
		if((canBeUninvoked)
		&&(aff instanceof MOB)
		&&(cargoR!=null))
		{
			final MOB mob=(MOB)aff;
			if(loading)
			{
				if((loadingI == null)
				||(cargoR==null)
				||(!this.canLoadCargoHere(mob,loadingI, cargoR)))
					commonTell((MOB)aff,L("Your cargo loading has failed.\n\r"));
				else
				{
					cargoR.moveItemTo(loadingI, Expire.Never);
					cargoR.showOthers(mob,loadingI,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> store(s) <T-NAME> here."));
					if(mob.location()!=cargoR)
						mob.location().show(mob,loadingI,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> finish(es) cargo-loading <T-NAME>."));
				}
			}
			else
			if(loadingI == null)
			{
				commonTell((MOB)aff,L("Your cargo unloading has failed.\n\r"));
			}
			else
			if(!isaborted)
			{
				mob.location().moveItemTo(loadingI,Expire.Player_Drop);
				cargoR.showOthers(mob,loadingI,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> finish(es) unloading <T-NAME>."));
				if(mob.location()!=cargoR)
					mob.location().show(mob,loadingI,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> unload(s) <T-NAME> cargo here."));
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(super.checkStop(mob, commands) || (R==null))
			return true;

		verb=L("loading");
		cargoR=null;
		String shipName="the ship";
		if(commands.size()<3)
		{
			commonTell(mob,null,null,L("Load or Unload? What cargo into or from which ship? Try CARGO LOAD [ITEM] [SHIP NAME] to load cargo, or CARGO UNLOAD [ITEM] [SHIP NAME] to unload."));
			return false;
		}
		this.fromR=R;
		this.loading=true;
		final String typ=commands.remove(0).toUpperCase().trim();
		if("LOAD".startsWith(typ))
		{
		}
		else
		if("UNLOAD".startsWith(typ))
		{
			loading=false;
		}
		else
		{
			commonTell(mob,null,null,L("Load or Unload? What cargo into or from which ship? Try CARGO LOAD [ITEM] [SHIP NAME] to load cargo, or CARGO UNLOAD [ITEM] [SHIP NAME] to unload."));
			return false;
		}

		final List<String> cargoV=new XVector<String>(commands.remove(0));
		if(loading)
		{
			final Item I=super.getTarget(mob, R, givenTarget, cargoV, Wearable.FILTER_ROOMONLY);
			if(I==null)
				return false;
			final Item possShipI=super.getTarget(mob, R, givenTarget, commands, Wearable.FILTER_ROOMONLY);
			if(possShipI==null)
				return false;
			shipName=possShipI.Name();
			if(!(possShipI instanceof BoardableShip))
			{
				commonTell(mob, L("@x1 is not a cargo ship.",possShipI.name()));
				return false;
			}
			if((I instanceof BoardableShip)
			||(!CMLib.flags().isGettable(I)))
			{
				commonTell(mob, L("You can't load @x1 as cargo!",I.name()));
				return false;
			}
			final PrivateProperty propRecord = CMLib.law().getPropertyRecord(possShipI);
			if(propRecord != null)
			{
				if(!CMLib.law().doesHaveWeakPrivilegesWith(mob, propRecord))
				{
					commonTell(mob,L("You aren't permitted to load cargo onto @x1,",possShipI.Name()));
					return false;
				}
			}
			this.cargoR=null;
			final Area shipA=((BoardableShip)possShipI).getShipArea();
			for(final Enumeration<Room> r=shipA.getProperMap();r.hasMoreElements();)
			{
				final Room R1=r.nextElement();
				if(R1==null)
					continue;
				if(this.canLoadCargoHere(mob, I, R1))
				{
					cargoR=R1;
					break;
				}
			}
			if(cargoR == null)
			{
				commonTell(mob,L("There appears to be no space on board @x1 for @x2.",possShipI.Name(),I.Name()));
				return false;
			}
			this.loadingI=I;
		}
		else
		{
			final Item possShipI=super.getTarget(mob, R, givenTarget, commands, Wearable.FILTER_ROOMONLY);
			if(possShipI==null)
				return false;
			shipName=possShipI.Name();
			if(!(possShipI instanceof BoardableShip))
			{
				commonTell(mob, L("@x1 is not a cargo ship.",possShipI.name()));
				return false;
			}
			final PrivateProperty propRecord = CMLib.law().getPropertyRecord(possShipI);
			if(propRecord != null)
			{
				if(!CMLib.law().doesHaveWeakPrivilegesWith(mob, propRecord))
				{
					commonTell(mob,L("You aren't permitted to unload cargo off of @x1,",possShipI.Name()));
					return false;
				}
			}
			this.cargoR=null;
			final Area shipA=((BoardableShip)possShipI).getShipArea();
			for(final Enumeration<Room> r=shipA.getProperMap();r.hasMoreElements();)
			{
				final Room R1=r.nextElement();
				if(R1==null)
					continue;
				final Item I=R1.findItem(null, cargoV.get(0));
				if((I!=null)
				&&(CMLib.flags().canBeSeenBy(I, mob))
				&&(CMLib.flags().isGettable(I))
				&&(this.canUnloadCargoFromHere(mob, I, R1)))
				{
					this.cargoR=R1;
					this.loadingI=I;
				}
			}
			if(cargoR==null)
			{
				commonTell(mob,L("You couldn't find any reachable cargo called '@x1' on board @x2.",cargoV.get(0),shipName));
				return false;
			}
		}

		if((this.loadingI != null)&&(this.loadingI.phyStats().weight() > (1000 + (1000*super.getXLEVELLevel(mob)))))
		{
			commonTell(mob,L("You just don't have the expertise to move that much weight"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int duration=getDuration(mob,1);
		final String cargoMsgStr;
		if(loading)
		{
			cargoMsgStr=L("<S-NAME> start(s) loading <T-NAME> cargo into @x1.",shipName);
			verb=L("loading @x1 onto @x2",loadingI.Name(),shipName);
			displayText=L("You are loading @x1 onto @x2",loadingI.Name(),shipName);
		}
		else
		{
			cargoMsgStr=L("<S-NAME> start(s) unloading <T-NAME> cargo from @x1.",shipName);
			verb=L("unloading @x1 from @x2",loadingI.Name(),shipName);
			displayText=L("You are unloading @x1 from @x2",loadingI.Name(),shipName);
		}
		final CMMsg msg=CMClass.getMsg(mob,loadingI,this,getActivityMessageType(),cargoMsgStr);
		if(R.okMessage(mob,msg) && cargoR.okMessage(mob, msg))
		{
			R.send(mob,msg);
			cargoR.sendOthers(mob,msg);
			if(!super.proficiencyCheck(mob,0,auto))
				loadingI=null;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
