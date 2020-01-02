package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Items.Basic.GenDrink;
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
public class GenCow extends GenRideable implements Drink
{
	@Override
	public String ID()
	{
		return "GenCow";
	}

	protected int		amountOfThirstQuenched	= 100;
	protected int		amountOfLiquidHeld		= 1000;
	protected int		amountOfLiquidRemaining	= 1000;
	protected int		liquidType				= RawMaterial.RESOURCE_MILK;
	protected volatile double refill			= 0;


	public GenCow()
	{
		super();
		username="a cow";
		setDescription("A large lumbering beast that looks too slow to get out of your way.");
		setDisplayText("A fat happy cow wanders around here.");
		CMLib.factions().setAlignment(this,Faction.Align.NEUTRAL);
		setMoney(0);
		setWimpHitPoint(0);
		super.riderCapacity=0;
		basePhyStats().setDamage(1);
		basePhyStats().setSpeed(1.0);
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(2);
		basePhyStats().setArmor(90);
		baseCharStats().setStat(CharStats.STAT_GENDER, 'F');
		baseCharStats().setMyRace(CMClass.getRace("Cow"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public long decayTime()
	{
		return 0;
	}

	@Override
	public void setDecayTime(final long time)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
			return true;
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			final MOB mob=msg.source();
			final boolean thirsty=mob.curState().getThirst()<=0;
			final boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell(L("You are no longer thirsty."));
			else
			if(full)
				mob.tell(L("You have drunk all you can."));
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_FILL)
		&&(msg.target() instanceof Container)
		&&(((Container)msg.target()).capacity()>0))
		{
			final Container container=(Container)msg.target();
			final Item I=CMLib.materials().makeItemResource(liquidType());
			I.setMaterial(liquidType());
			I.setBaseValue(RawMaterial.CODES.VALUE(liquidType()));
			I.basePhyStats().setWeight(1);
			CMLib.materials().addEffectsToResource(I);
			I.recoverPhyStats();
			I.setContainer(container);
			if(container.owner()!=null)
				if(container.owner() instanceof MOB)
					((MOB)container.owner()).addItem(I);
				else
				if(container.owner() instanceof Room)
					((Room)container.owner()).addItem(I,ItemPossessor.Expire.Resource);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((liquidRemaining() < liquidHeld())
		&&(liquidHeld()<Integer.MAX_VALUE/2))
		{
			final Room R=CMLib.map().roomLocation(this);
			if(R!=null)
			{
				final Area A=R.getArea();
				if(A!=null)
				{
					final TimeClock clock=A.getTimeObj();
					final int ticksPerMudday=(int)((clock.getHoursInDay() * CMProps.getMillisPerMudHour()) / CMProps.getTickMillis());
					final double amt=CMath.div(liquidHeld(),ticksPerMudday);
					refill += amt;
					final int amtNow=(int)CMath.round(Math.floor(refill));
					if(amtNow > 0)
					{
						setLiquidRemaining(liquidRemaining() + amtNow);
						if(liquidRemaining() > liquidHeld())
							setLiquidRemaining(liquidHeld());
						refill -= amtNow;
					}
				}
			}
		}
		return true;
	}

	@Override
	public int thirstQuenched()
	{
		return amountOfThirstQuenched;
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
	public boolean disappearsAfterDrinking()
	{
		return false;
	}

	@Override
	public int liquidType()
	{
		return liquidType;
	}

	@Override
	public void setLiquidType(final int newLiquidType)
	{
		liquidType = newLiquidType;
	}

	@Override
	public void setThirstQuenched(final int amount)
	{
		amountOfThirstQuenched = amount;
	}

	@Override
	public void setLiquidHeld(final int amount)
	{
		amountOfLiquidHeld=amount;
	}

	@Override
	public void setLiquidRemaining(final int amount)
	{
		amountOfLiquidRemaining=amount;
	}

	@Override
	public boolean containsDrink()
	{
		return this.liquidRemaining() > 0;
	}

	@Override
	public int amountTakenToFillMe(final Drink theSource)
	{
		return 0;
	}

	private final static String[] MYCODES={"QUENCHED","LIQUIDHELD","LIQUIDTYPE"};

	@Override
	public String getStat(final String code)
	{
		if(super.isStat(code))
			return super.getStat(code);
		else
		switch(getCodeNum(code))
		{
		case 0:
			return "" + thirstQuenched();
		case 1:
			return "" + liquidHeld();
		case 2:
			return "" + liquidType();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(super.isStat(code))
			super.setStat(code, val);
		else
		switch(getCodeNum(code))
		{
		case 0:
			setThirstQuenched(CMath.s_parseIntExpression(val));
			break;
		case 1:
			setLiquidHeld(CMath.s_parseIntExpression(val));
			break;
		case 2:
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

	@Override
	protected int getCodeNum(final String code)
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
		final String[] MYCODES=CMProps.getStatCodesList(GenCow.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(super.getStatCodes());
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
		if(!(E instanceof GenCow))
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
