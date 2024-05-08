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
import com.planet_ink.coffee_mud.Items.CompTech.StdCompFuelConsumer;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2024 Bo Zimmerman

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
public class GenStove extends GenContainer implements Light, FuelConsumer
{
	@Override
	public String ID()
	{
		return "GenStove";
	}

	protected String		readableText			= "20";
	protected boolean		lit						= false;
	protected int			durationTicks			= 20;
	protected boolean		destroyedWhenBurnedOut	= true;
	protected boolean		goesOutInTheRain		= true;
	protected int[]			generatedFuelTypes;
	protected int			ticksPerFuelConsume		= 10;
	protected volatile int	fuelTickDown			= 0;



	public GenStove()
	{
		super();
		setName("a generic stove");
		setDisplayText("a generic stove is here.");
		setDescription("");
		setMaterial(RawMaterial.RESOURCE_IRON);
		destroyedWhenBurnedOut	= false;
		goesOutInTheRain		= false;
		generatedFuelTypes = new int[] { RawMaterial.RESOURCE_WOOD, RawMaterial.RESOURCE_COAL };
	}

	@Override
	public void setDuration(final int duration)
	{
		readableText=""+duration;	// required because lazy way back when
		durationTicks = duration;
	}

	@Override
	public int getDuration()
	{
		durationTicks = CMath.s_int(readableText);	// required because lazy way back when
		return durationTicks;
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,false);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		if(CMath.isInteger(text)) // required because lazy way back when
			setDuration(CMath.s_int(text));
		else
			readableText=text;
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public int getTicksPerFuelConsume()
	{
		return ticksPerFuelConsume;
	}

	@Override
	public void setTicksPerFuelConsume(final int tick)
	{
		ticksPerFuelConsume = tick;
	}

	@Override
	public int[] getConsumedFuelTypes()
	{
		return generatedFuelTypes;
	}

	@Override
	public void setConsumedFuelType(final int[] resources)
	{
		generatedFuelTypes = resources;
	}

	protected boolean isFuel(final Item I)
	{
		if(!(I instanceof RawMaterial))
			return false;
		final int[] types = this.getConsumedFuelTypes();
		if(CMParms.contains(types, I.material()))
			return true;
		if(CMParms.contains(types, RawMaterial.RESOURCE_WOOD)
		&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN))
			return true;
		return false;
	}

	@Override
	public int getFuelRemaining()
	{
		int amt=0;
		for(final Item I : getFuel())
		{
			if(isFuel(I))
				amt+=I.phyStats().weight();
		}
		return amt;
	}

	protected synchronized List<Item> getFuel()
	{
		final List<Item> fuel = new ArrayList<Item>();
		for(final Item I : getContents())
		{
			if(isFuel(I))
				fuel.add(I);
		}
		return fuel;
	}

	@Override
	public void setOwner(final ItemPossessor newOwner)
	{
		super.setOwner(newOwner);
	}

	@Override
	public boolean consumeFuel(final int amount)
	{
		return false;
	}

	@Override
	public int getTotalFuelCapacity()
	{
		return capacity();
	}


	@Override
	public boolean destroyedWhenBurnedOut()
	{
		return this.destroyedWhenBurnedOut;
	}

	@Override
	public void setDestroyedWhenBurntOut(final boolean truefalse)
	{
		this.destroyedWhenBurnedOut = truefalse;
	}

	@Override
	public boolean goesOutInTheRain()
	{
		return this.goesOutInTheRain;
	}

	@Override
	public boolean isLit()
	{
		return this.lit;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_EXTINGUISH:
			if(!isLit())
			{
				mob.tell(L("@x1 is not lit!",name()));
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
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
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EXTINGUISH:
				if(isLit())
				{
					light(false);
					recoverPhyStats();
					room.recoverRoomStats();
				}
				break;
			}
		}
	}

	@Override
	public void light(final boolean isLit)
	{
		lit = isLit;
		if((owner() instanceof Room)||(owner() instanceof MOB))
		{
			final Ability bA = fetchEffect("Burning");
			final List<Item> fuel = getFuel();
			if(lit && (fuel.size()>0))
			{
				CMLib.threads().startTickDown(this, Tickable.TICKID_LIGHT_FLICKERS, 1);
				if(bA==null)
				{
					Ability B=CMClass.getAbility("Burning");
					B.setAbilityCode(1024|2048|4096); // not destroyed, no spread, not extinguished by rain
					final MOB mob=CMClass.getFactoryMOB();
					B.invoke(mob,this,true, Integer.MAX_VALUE/2);
					mob.destroy();
					B=fetchEffect("Burning");
					if(B!=null)
						B.makeLongLasting();
				}
			}
			else
			{
				CMLib.threads().deleteTick(me, Tickable.TICKID_LIGHT_FLICKERS);
				if(bA!=null)
				{
					bA.unInvoke();
					delEffect(bA);
				}
			}
			for(final Item fI : fuel)
			{
				final Ability A = fI.fetchEffect("Burning");
				if(A!=null)
				{
					if(isLit)
						return; // nothing to do!
					A.setAbilityCode(1024); // not destroyed, no spread, not extinguished by rain
					A.unInvoke();
					fI.delEffect(A);
				}
			}
			if(isLit && (fuel.size()>0))
			{
				final Item I = fuel.get(0);
				final Ability B=CMClass.getAbility("Burning");
				B.setAbilityCode(512); // item destroyed on burn end
				final MOB mob=CMClass.getFactoryMOB();
				B.invoke(mob,I,true, getDuration());
				mob.destroy();
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_LIGHT_FLICKERS)
		{
			if((owner()!=null)
			&&(isLit()))
			{
				if(this.getFuel().size()==0)
				{
					this.light(false);
					return false;
				}
				return true;
			}
			return false;
		}
		else
			return super.tick(ticking, tickID);
	}

	private final static String[] MYCODES={"CONSUMEDTYPES"};

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		final int internalNum = getInternalCodeNum(code);
		if(internalNum<0)
			return super.getStat(code);
		else
		switch(internalNum)
		{
		case 0:
		{
			final StringBuilder str=new StringBuilder("");
			for(int i=0;i<getConsumedFuelTypes().length;i++)
			{
				if(i>0)
					str.append(", ");
				str.append(RawMaterial.CODES.NAME(getConsumedFuelTypes()[i]));
			}
			return str.toString();
		}
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		final int internalNum = getInternalCodeNum(code);
		if(internalNum<0)
			super.setStat(code, val);
		else
		switch(internalNum)
		{
		case 0:
		{
			final List<String> mats = CMParms.parseCommas(val,true);
			final int[] newMats = new int[mats.size()];
			for(int x=0;x<mats.size();x++)
			{
				final int rsccode = RawMaterial.CODES.FIND_CaseSensitive(mats.get(x).trim());
				if(rsccode > 0)
					newMats[x] = rsccode;
			}
			setConsumedFuelType(newMats);
			break;
		}
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes==null)
		{
			if(codes!=null)
				return codes;
			final String[] MYCODES=CMProps.getStatCodesList(GenStove.MYCODES,this);
			final String[] superCodes=super.getStatCodes();
			codes=new String[superCodes.length+MYCODES.length];
			int i=0;
			for(;i<superCodes.length;i++)
				codes[i]=superCodes[i];
			for(int x=0;x<MYCODES.length;i++,x++)
				codes[i]=MYCODES[x];
		}
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenStove))
			return false;
		for(int i=0;i<getStatCodes().length;i++)
		{
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		}
		return true;
	}
}
