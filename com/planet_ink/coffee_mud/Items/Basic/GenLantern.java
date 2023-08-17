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
   Copyright 2001-2023 Bo Zimmerman

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
public class GenLantern extends GenLightSource implements LiquidHolder
{
	@Override
	public String ID()
	{
		return "GenLantern";
	}

	public static final int DURATION_TICKS	= 800;

	protected int	amountOfLiquidHeld		= 80;
	protected int	amountOfLiquidRemaining	= 80;
	protected int	liquidType				= RawMaterial.RESOURCE_LAMPOIL;

	public GenLantern()
	{
		super();
		setName("a hooded lantern");
		setDisplayText("a hooded lantern sits here.");
		setDescription("");

		basePhyStats().setWeight(5);
		setDuration(DURATION_TICKS);
		destroyedWhenBurnedOut=false;
		goesOutInTheRain=false;
		baseGoldValue=60;
		amountOfLiquidHeld = 80;
		amountOfLiquidRemaining = 80;
		liquidType = RawMaterial.RESOURCE_LAMPOIL;
		setMaterial(RawMaterial.RESOURCE_STEEL);
		recoverPhyStats();
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_LIGHT_FLICKERS)
		{
			if((owner()!=null)
			&&(isLit())
			&&(getDuration()>0))
			{
				final double pctLeft = CMath.div(super.durationTickDown, getDuration());
				final int rem = (int)Math.round(Math.ceil(CMath.mul(pctLeft, liquidHeld())));
				if(rem < liquidRemaining())
					setLiquidRemaining(rem);
			}
		}
		return super.tick(ticking, tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
				case CMMsg.TYP_FILL:
					if((msg.tool()!=msg.target())
					&&(msg.tool() instanceof Drink))
					{
						if(((Drink)msg.tool()).liquidType()!=liquidType())
						{
							mob.tell(L("You can only fill @x1 with @x1!",name(),RawMaterial.CODES.NAME(liquidType()).toLowerCase()));
							return false;
						}
						final Drink thePuddle=(Drink)msg.tool();
						if(!thePuddle.containsLiquid())
						{
							mob.tell(L("@x1 is empty.",thePuddle.name()));
							return false;
						}
						if(liquidRemaining() >= liquidHeld())
						{
							mob.tell(L("@x1 is full.",name()));
							return false;
						}
						return true;
					}
					mob.tell(L("You can't fill @x1 from that.",name()));
					return false;
				default:
					break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_FILL:
				if((msg.tool() instanceof Drink))
				{
					final Drink thePuddle=(Drink)msg.tool();
					int amountToTake=liquidHeld()-liquidRemaining();
					if(!thePuddle.containsLiquid())
						amountToTake=0;
					else
					if(amountToTake > thePuddle.liquidRemaining())
						amountToTake = thePuddle.liquidRemaining();
					setLiquidRemaining(liquidRemaining()+amountToTake);
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
					if(durationTickDown < 0)
						durationTickDown = 0;
					durationTickDown += (int)Math.round(CMath.mul(CMath.div(getDuration(),liquidHeld()),amountToTake));
					if(liquidRemaining()>=liquidHeld())
						durationTickDown = getDuration();
					if(!isGeneric())
						setDescription(L("@x1 still looks like it has some life left in it.",name()));
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public int liquidHeld()
	{
		return amountOfLiquidHeld;
	}

	@Override
	public int liquidRemaining()
	{
		return amountOfLiquidRemaining;
	}

	@Override
	public int liquidType()
	{
		return liquidType;
	}

	@Override
	public void setLiquidType(int newLiquidType)
	{
		if(newLiquidType == 0)
			newLiquidType = RawMaterial.RESOURCE_LAMPOIL;
		liquidType = newLiquidType;
	}

	@Override
	public void setLiquidHeld(final int amount)
	{
		amountOfLiquidHeld = amount;
	}

	@Override
	public void setLiquidRemaining(final int amount)
	{
		amountOfLiquidRemaining = amount;
	}

	@Override
	public boolean containsLiquid()
	{
		return amountOfLiquidRemaining > 0;
	}

	@Override
	public int amountTakenToFillMe(final LiquidHolder theSource)
	{
		return amountOfLiquidHeld - amountOfLiquidRemaining;
	}

	private final static String[] MYCODES={"LIQUIDHELD","LIQUIDTYPE"};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
		{
		case 0:
			return "" + liquidHeld();
		case 1:
			return RawMaterial.CODES.NAME(liquidType());
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getInternalCodeNum(code))
		{
		case 0:
			setLiquidHeld(CMath.s_parseIntExpression(val));
			break;
		case 1:
		{
			int x = CMath.s_parseListIntExpression(RawMaterial.CODES.NAMES(), val);
			x = ((x >= 0) && (x < RawMaterial.RESOURCE_MASK)) ? RawMaterial.CODES.GET(x) : x;
			setLiquidType(x);
			break;
		}
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenLantern.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenLantern))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
