package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

import java.io.IOException;
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
public class Milkable extends StdAbility implements Drink
{
	@Override
	public String ID()
	{
		return "Milkable";
	}

	private final static String	localizedName	= CMLib.lang().L("Milkable");

	@Override
	public String name()
	{
		if(affected != null)
			return affected.Name();
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Milkable)");
	private final static String	localizedStaticDisplay2	= CMLib.lang().L("(Refusing Milk)");

	@Override
	public String displayText()
	{
		if(isMilkingOK(null))
			return localizedStaticDisplay;
		else
			return localizedStaticDisplay2;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
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

	protected int 		liquidType				= RawMaterial.RESOURCE_MILK;
	protected boolean	drinkableMilkable		= false;
	protected boolean	milkingOK				= false;
	protected int		milkPerDay				= 1000;
	protected int		milkRemain				= 1000;
	protected int		amountOfThirstQuenched	= 100;

	protected volatile double refill			= 0;

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		drinkableMilkable=CMParms.getParmBool(newMiscText, "DRINK", false);
		milkingOK = CMParms.getParmBool(newMiscText, "ON", milkingOK);
		milkPerDay = CMParms.getParmInt(newMiscText, "PERDAY", 1000);
		final String liquidType = CMParms.getParmStr(newMiscText, "TYPE", "MILK");
		final int resourceCode = RawMaterial.CODES.FIND_IgnoreCase(liquidType);
		if(resourceCode > 0)
			this.liquidType = resourceCode;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		final boolean worked = super.autoInvocation(mob, force);
		if(mob.isPlayer() || (!mob.isMonster()))
			milkingOK=true;
		return worked;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((liquidRemaining() < liquidHeld())
		&&(liquidHeld()<Integer.MAX_VALUE/2)
		&&(affected != null))
		{
			final Physical affected=this.affected;
			final Room R=CMLib.map().roomLocation(affected);
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

	protected boolean isMilkingOK(final MOB milkingMOB)
	{
		final Physical affected=this.affected;
		if(affected instanceof MOB)
		{
			if(milkingOK
			||(CMLib.flags().isBoundOrHeld(affected))
			||((((MOB)affected).isMonster())
				&&(((MOB)affected).getStartRoom()!=null)
				&&(milkingMOB!=null)
				&&(CMLib.law().doesHavePriviledgesHere(milkingMOB, ((MOB)affected).getStartRoom())))
			||((((MOB)affected).isMonster())
				&&(milkingMOB!=null)
				&&(((MOB)affected).amUltimatelyFollowing()==milkingMOB)))
					return true;
		}
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_COMMANDREJECT:
			if((msg.targetMessage()!=null)
			&& this.drinkableMilkable
			&& (msg.tool()==affected))
			{
				final MOB mob=msg.source();
				final List<String> cmds=CMParms.parse(msg.targetMessage());
				if(cmds.size()==0)
					return true;
				final String word=cmds.get(0).toUpperCase();
				if("FILL".startsWith(word)
				&& (isMilkingOK(msg.source())))
				{
					final CMMsg fillMsg=CMClass.getMsg(mob,msg.target(),this,CMMsg.MSG_FILL,L("<S-NAME> milk(s) <O-NAME>, filling <T-NAME>."));
					if(mob.location().okMessage(mob,fillMsg))
						mob.location().send(mob,fillMsg);
					return false;
				}
			}
			break;
		case CMMsg.TYP_HUH:
		{
			if(msg.targetMessage()!=null)
			{
				final MOB mob=msg.source();
				final List<String> commands=CMParms.parse(msg.targetMessage());
				if(commands.size()==0)
					return true;
				final String word=commands.get(0).toUpperCase();
				if(word.equals("MILK"))
				{
					if((msg.source() == affected)
					&&(commands.size()<3))
					{
						boolean newMilk=!this.milkingOK;
						if(commands.size()>1)
						{
							if(commands.get(1).equalsIgnoreCase("ok")
							||commands.get(1).equalsIgnoreCase("on"))
								newMilk=true;
							else
							if(commands.get(1).equalsIgnoreCase("no")
							||commands.get(1).equalsIgnoreCase("off"))
								newMilk=false;
							else
							{
								mob.tell(L("'@x1' is an illegal argument.  Try OK/NO.",commands.get(1)));
								return false;
							}
						}
						this.milkingOK=newMilk;
						if(this.milkingOK)
							mob.location().show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> show(s) interest in being milked."));
						else
							mob.location().show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> <S-IS-ARE> not showing any interest in being milked."));
						return false;
					}
					final List<String> origCmds=new XVector<String>(commands);
					if(commands.size()<3)
					{
						CMLib.commands().postCommandFail(mob,commands,L("Milk whom, into what?"));
						return false;
					}
					commands.remove(0);
					int fromDex=commands.size()-1;
					for(int i=commands.size()-2;i>=1;i--)
					{
						if(commands.get(i).equalsIgnoreCase("into"))
						{
							fromDex=i;
							commands.remove(i);
						}
					}
					final String thingToFill=CMParms.combine(commands,fromDex);
					while(commands.size()>=(fromDex+1))
						commands.remove(commands.size()-1);
					final String thingToFillFrom=CMParms.combine(commands,0);
					final Environmental fillFromThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFillFrom,Wearable.FILTER_ANY);
					if((fillFromThis==null)||(!CMLib.flags().canBeSeenBy(fillFromThis,mob)))
					{
						CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",thingToFillFrom));
						return false;
					}
					if(fillFromThis != affected)
						return true;
					final Item fillThis=mob.findItem(null,thingToFill);
					if((fillThis==null)||(!CMLib.flags().canBeSeenBy(fillThis,mob)))
					{
						CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",thingToFill));
						return false;
					}
					if(isMilkingOK(mob))
					{
						final CMMsg fillMsg=CMClass.getMsg(mob,fillThis,this,CMMsg.MSG_FILL,L("<S-NAME> milk(s) <O-NAME> into <T-NAME>."));
						if(!mob.isMine(fillThis))
						{
							if(CMLib.commands().postGet(mob,null,fillThis,false))
							{
								if(mob.location().okMessage(mob,fillMsg))
									mob.location().send(mob,fillMsg);
							}
						}
						else
						if(mob.location().okMessage(mob,fillMsg))
							mob.location().send(mob,fillMsg);
					}
					else
					{
						mob.tell(mob,affected,null,L("<T-NAME> won't seem to hold still for you."));
					}
					return false;
				}
			}
			break;
		}
		case CMMsg.TYP_DRINK:
			if((msg.target()==affected)
			&& this.drinkableMilkable
			&& (isMilkingOK(msg.source())))
				msg.setTarget(this);
			break;
		}
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
	public int thirstQuenched()
	{
		return amountOfThirstQuenched;
	}

	@Override
	public int liquidHeld()
	{
		return milkPerDay;
	}

	@Override
	public int liquidRemaining()
	{
		return milkRemain;
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
	}

	@Override
	public void setLiquidRemaining(final int amount)
	{
		milkRemain=amount;
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

	@Override
	public long decayTime()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public void setDecayTime(final long time)
	{
	}
}
