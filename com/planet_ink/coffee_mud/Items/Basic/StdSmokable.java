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
public class StdSmokable extends StdContainer implements Light
{
	@Override
	public String ID()
	{
		return "StdSmokable";
	}

	protected boolean lit=false;
	protected long puffTicks=30000/CMProps.getTickMillis();
	protected int baseDuration=200;
	protected int durationTicks=200;
	protected boolean destroyedWhenBurnedOut=true;
	protected boolean goesOutInTheRain=true;

	public StdSmokable()
	{
		super();
		setName("a cigar");
		setDisplayText("a cigar has been left here.");
		setDescription("Woven of fine leaf, it looks like a fine smoke!");

		capacity=0;
		containType=Container.CONTAIN_SMOKEABLES;
		properWornBitmap=Wearable.WORN_MOUTH;
		setMaterial(RawMaterial.RESOURCE_PIPEWEED);
		wornLogicalAnd=false;
		baseGoldValue=5;
		recoverPhyStats();
	}

	@Override
	public void setDuration(int duration)
	{
		baseDuration=duration;
	}

	@Override
	public int getDuration()
	{
		return baseDuration;
	}

	@Override
	public boolean destroyedWhenBurnedOut()
	{
		return this.destroyedWhenBurnedOut;
	}

	@Override
	public void setDestroyedWhenBurntOut(boolean truefalse)
	{
		destroyedWhenBurnedOut=truefalse;
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
		lit=isLit;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();

		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WEAR:
			if(capacity>0)
			{
				if(hasContent())
					durationTicks=baseDuration;
				else
					durationTicks=0;
			}
			if(durationTicks==0)
			{
				mob.tell(L("@x1 looks empty.",name()));
				return false;
			}
			final Room room=mob.location();
			if(room!=null)
			{
				if(((LightSource.inTheRain(room)&&(goesOutInTheRain()))
					||(LightSource.inTheWater(msg.source(),room)))
				&&(durationTicks>0)
				&&(mob.isMine(this)))
				{
					mob.tell(L("It's too wet to light @x1 here.",name()));
					return false;
				}
			}
			msg.modify(msg.source(),msg.target(),msg.tool(),
						msg.sourceCode(),L("<S-NAME> light(s) up <T-NAME>."),
						msg.targetCode(),L("<S-NAME> light(s) up <T-NAME>."),
						msg.othersCode(),L("<S-NAME> light(s) up <T-NAME>."));
			return super.okMessage(myHost,msg);
		case CMMsg.TYP_EXTINGUISH:
			if((durationTicks==0)||(!isLit()))
			{
				mob.tell(L("@x1 is not lit!",name()));
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_LIGHT_FLICKERS)
		&&(isLit())
		&&(tickStatus==Tickable.STATUS_NOT)
		&&(owner()!=null))
		{
			tickStatus=Tickable.STATUS_ALIVE;
			if(((--durationTicks)>0)&&(!destroyed))
			{
				if(((durationTicks%puffTicks)==0)
				&&(owner() instanceof MOB)
				&&(!amWearingAt(Wearable.IN_INVENTORY)))
				{
					final MOB mob=(MOB)owner();
					if((mob.location()!=null)
					&&(CMLib.flags().isAliveAwakeMobile(mob,true)))
					{
						tickStatus=Tickable.STATUS_WEATHER;
						mob.location().show(mob,this,this,CMMsg.MSG_HANDS,L("<S-NAME> puff(s) on <T-NAME>."));
						if((CMLib.dice().roll(1,1000,0)==1)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
						{
							final Ability A=CMClass.getAbility("Disease_Cancer");
							if((A!=null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
								A.invoke(mob,mob,true,0);
						}
					}
				}
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}
			if(owner() instanceof Room)
			{
				tickStatus=Tickable.STATUS_CLASS;
				final Room R=(Room)owner;
				if(R.numInhabitants()>0)
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 burns out.",name()));
				if(destroyedWhenBurnedOut())
					destroy();
				R.recoverRoomStats();
			}
			else
			if(owner() instanceof MOB)
			{
				tickStatus=Tickable.STATUS_DEAD;
				final MOB M=(MOB)owner();
				M.tell(M,null,this,L("<O-NAME> burns out."));
				durationTicks=0;
				if(destroyedWhenBurnedOut())
					destroy();
				M.recoverPhyStats();
				M.recoverCharStats();
				M.recoverMaxState();
				M.recoverPhyStats();
				if(M.location()!=null)
					M.location().recoverRoomStats();
			}
			light(false);
			durationTicks=0;
		}
		tickStatus=Tickable.STATUS_NOT;
		return false;
	}

	public void getAddictedTo(MOB mob, Item item)
	{
		Ability A=mob.fetchEffect("Addictions");
		if(A==null)
		{
			A=CMClass.getAbility("Addictions");
			if(A!=null)
				A.invoke(mob,item,true,0);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(mob==null)
			return;
		final Room room=mob.location();
		if(room==null)
			return;
		if(((LightSource.inTheRain(room)&&goesOutInTheRain())
		   ||(LightSource.inTheWater(msg.source(),room)))
		&&(isLit())
		&&(durationTicks>0)
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
			case CMMsg.TYP_WEAR:
				if(durationTicks>0)
				{
					if(capacity>0)
					{
						final List<Item> V=getContents();
						Item I=null;
						for(int v=0;v<V.size();v++)
						{
							I=V.get(v);
							if(CMLib.dice().roll(1,100,0)==1)
								getAddictedTo(msg.source(),I);
							I.destroy();
						}
					}
					else
					if(CMLib.dice().roll(1,100,0)==1)
						getAddictedTo(msg.source(),this);

					light(true);
					CMLib.threads().startTickDown(this,Tickable.TICKID_LIGHT_FLICKERS,1);
					recoverPhyStats();
					room.recoverRoomStats();
				}
				break;
			}
		super.executeMsg(myHost,msg);
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
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_PUSH:
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
