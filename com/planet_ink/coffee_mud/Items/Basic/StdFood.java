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
public class StdFood extends StdItem implements Food
{
	@Override
	public String ID()
	{
		return "StdFood";
	}

	protected int amountOfNourishment=500;
	protected int nourishmentPerBite=0;
	protected long decayTime=0;

	public StdFood()
	{
		super();
		setName("a bit of food");
		basePhyStats.setWeight(2);
		setDisplayText("a bit of food is here.");
		setDescription("Looks like some mystery meat");
		baseGoldValue=5;
		material=RawMaterial.RESOURCE_MEAT;
		recoverPhyStats();
	}

	@Override
	public int nourishment()
	{
		return amountOfNourishment;
	}

	@Override
	public void setNourishment(int amount)
	{
		amountOfNourishment=amount;
	}

	@Override
	public int bite()
	{
		return nourishmentPerBite;
	}

	@Override
	public void setBite(int amount)
	{
		nourishmentPerBite=amount;
	}

	@Override
	public long decayTime()
	{
		return decayTime;
	}

	@Override
	public void setDecayTime(long time)
	{
		decayTime=time;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				if((!msg.targetMajor(CMMsg.MASK_HANDS))
				||(mob.isMine(this))
				||(!CMLib.flags().isGettable(this)))
				{
					int amountEaten=nourishmentPerBite;
					if((amountEaten<1)||(amountEaten>amountOfNourishment))
						amountEaten=amountOfNourishment;
					msg.setValue((amountEaten<amountOfNourishment)?amountEaten:0);
					return true;
				}
				mob.tell(L("You don't have that."));
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				final boolean hungry=mob.curState().getHunger()<=0;
				if((!hungry)
				&&(mob.curState().getHunger()>=mob.maxState().maxHunger(mob.baseWeight()))
				&&(CMLib.dice().roll(1,100,0)==1)
				&&(!CMLib.flags().isGolem(msg.source()))
				&&(msg.source().fetchEffect("Disease_Obesity")==null)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A=CMClass.getAbility("Disease_Obesity");
					if ((A != null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
					{
						A.invoke(mob, mob, true, 0);
					}
				}
				int amountEaten=nourishmentPerBite;
				if((amountEaten<1)||(amountEaten>amountOfNourishment))
					amountEaten=amountOfNourishment;
				amountOfNourishment-=amountEaten;
				final boolean full=!mob.curState().adjHunger(amountEaten,mob.maxState().maxHunger(mob.baseWeight()));
				if((hungry)&&(mob.curState().getHunger()>0))
					mob.tell(L("You are no longer hungry."));
				else
				if(full)
					mob.tell(L("You are full."));
				if(amountOfNourishment<=0)
					this.destroy();
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
				break;
			default:
				break;
			}
		}
	}
}
