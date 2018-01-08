package com.planet_ink.coffee_mud.Items.Basic;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class LightSource extends StdItem implements Light
{
	@Override
	public String ID()
	{
		return "LightSource";
	}

	protected boolean	lit						= false;
	protected int		durationTicks			= 200;
	protected boolean	destroyedWhenBurnedOut	= true;
	protected boolean	goesOutInTheRain		= true;

	public LightSource()
	{
		super();
		setName("a light source");
		setDisplayText("an ordinary light source sits here doing nothing.");
		setDescription("It looks like a light source of some sort.  I`ll bet it would help you see in the dark.");

		properWornBitmap=Wearable.WORN_HELD;
		setMaterial(RawMaterial.RESOURCE_OAK);
		wornLogicalAnd=false;
		baseGoldValue=5;
		recoverPhyStats();
	}

	@Override
	public void setDuration(int duration)
	{
		durationTicks = duration;
	}

	@Override
	public int getDuration()
	{
		return durationTicks;
	}

	@Override
	public boolean destroyedWhenBurnedOut()
	{
		return destroyedWhenBurnedOut;
	}

	@Override
	public void setDestroyedWhenBurntOut(boolean truefalse)
	{
		destroyedWhenBurnedOut = truefalse;
	}

	@Override
	public boolean goesOutInTheRain()
	{
		return this.goesOutInTheRain;
	}

	@Override
	public boolean isLit()
	{
		return lit;
	}

	@Override
	public void light(boolean isLit)
	{
		lit = isLit;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			if(getDuration()==0)
			{
				mob.tell(L("@x1 looks used up.",name()));
				return false;
			}
			final Room room=mob.location();
			if(room!=null)
			{
				if(((LightSource.inTheRain(room)&&(goesOutInTheRain()))
					||(LightSource.inTheWater(msg.source(),room)))
				   &&(getDuration()>0)
				   &&(mob.isMine(this)))
				{
					mob.tell(L("It's too wet to light @x1 here.",name()));
					return false;
				}
			}
			return super.okMessage(myHost,msg);
		case CMMsg.TYP_EXTINGUISH:
			if((getDuration()==0)||(!isLit()))
			{
				mob.tell(L("@x1 is not lit!",name()));
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void recoverPhyStats()
	{
		if((getDuration()>0)&&(isLit()))
			basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_LIGHTSOURCE);
		else
		if((basePhyStats().disposition()&PhyStats.IS_LIGHTSOURCE)==PhyStats.IS_LIGHTSOURCE)
			basePhyStats().setDisposition(basePhyStats().disposition()-PhyStats.IS_LIGHTSOURCE);
		super.recoverPhyStats();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_LIGHT_FLICKERS)
		{
			if((owner()!=null)
			&&(isLit())
			&&(getDuration()>0))
			{
				if(owner() instanceof Room)
				{
					final Room R=(Room)owner();
					if(R.numInhabitants()>0)
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 flickers and burns out.",name()));
					if(destroyedWhenBurnedOut())
						destroy();
					R.recoverRoomStats();
				}
				else
				if(owner() instanceof MOB)
				{
					final MOB M=(MOB)owner();
					M.tell(M,null,this,L("<O-NAME> flickers and burns out."));
					setDuration(0);
					if(destroyedWhenBurnedOut())
						destroy();
					M.recoverPhyStats();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					if(M.location()!=null)
						M.location().recoverRoomStats();
				}
			}
			light(false);
			setDuration(0);
			setDescription("It looks all used up.");
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public static boolean inTheRain(Room room)
	{
		if((room==null)||(room.getArea()==null))
			return false;
		return (((room.domainType()&Room.INDOORS)==0)
				&&((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_RAIN)
				   ||(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_THUNDERSTORM)));
	}

	public static boolean inTheWater(MOB mob, Room room)
	{
		if((room==null)||(mob==null))
			return false;
		if(CMLib.flags().isUnderWateryRoom(room))
			return true;
		if((!CMLib.flags().isFlying(mob))
		&&(mob.riding()==null)
		&&(CMLib.flags().isSwimmingInWater(mob)))
			return true;
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		final MOB mob=msg.source();
		if(mob==null)
			return;
		final Room room=mob.location();
		if(room==null)
			return;
		if(((LightSource.inTheRain(room)&&goesOutInTheRain())
				   ||(LightSource.inTheWater(msg.source(),room)))
		&&(isLit())
		&&(getDuration()>0)
		&&(mob.isMine(this))
		&&((!CMLib.flags().isInFlight(mob))
		   ||(LightSource.inTheRain(room))
		   ||((room.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(room.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))))
		{
			if(LightSource.inTheWater(msg.source(),room))
				mob.tell(L("The water makes @x1 go out.",name()));
			else
				mob.tell(L("The rain makes @x1 go out.",name()));
			durationTicks=1;
			tick(this,Tickable.TICKID_LIGHT_FLICKERS);
		}

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EXTINGUISH:
				if(isLit())
				{
					light(false);
					CMLib.threads().deleteTick(this,Tickable.TICKID_LIGHT_FLICKERS);
					recoverPhyStats();
					room.recoverRoomStats();
				}
				break;
			case CMMsg.TYP_HOLD:
				if(getDuration()>0)
				{
					if(!isLit())
						msg.addTrailerMsg(CMClass.getMsg(mob,this,CMMsg.MASK_SOUND|CMMsg.MASK_HANDS|CMMsg.MASK_MOVE|CMMsg.TYP_FIRE,L("<S-NAME> light(s) up @x1.",name())));
					else
						mob.tell(L("@x1 is already lit.",name()));
					light(true);
					CMLib.threads().startTickDown(this,Tickable.TICKID_LIGHT_FLICKERS,getDuration());
					recoverPhyStats();
					msg.source().recoverPhyStats();
					room.recoverRoomStats();
				}
				break;
			}
			if((msg.tool()==this)
			&&(msg.sourceMinor()==CMMsg.TYP_THROW))
			{
				msg.source().recoverPhyStats();
				if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_OPTIMIZE))
				{
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
					final Room R=CMLib.map().roomLocation(msg.target());
					if((R!=null)&&(R!=msg.source().location()))
						R.recoverRoomStats();
				}
			}
			else
			if(msg.amITarget(this))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_DROP:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
				case CMMsg.TYP_REMOVE:
					if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					{
						msg.source().recoverPhyStats();
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
						final Room R=CMLib.map().roomLocation(msg.tool());
						if((R!=null)&&(R!=msg.source().location()))
							R.recoverRoomStats();
					}
					break;
				}
			}
		}
	}
}
