package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2016-2025 Bo Zimmerman

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
public class Chant_Whirlpool extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Whirlpool";
	}

	private final static String	localizedName	= CMLib.lang().L("Whirlpool");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL - 99;
	}

	private volatile Room theWhirlpool = null;

	@Override
	public void unInvoke()
	{
		Room R=null;
		if(affected instanceof Room)
			R=(Room)affected;
		super.unInvoke();
		if((R!=null) && canBeUninvoked())
		{
			if(theWhirlpool instanceof GridLocale)
			{
				((GridLocale)theWhirlpool).clearGrid(R);
				theWhirlpool.getArea().destroy();
				theWhirlpool = null;
			}
		}
	}

	public boolean canEnterTheWhirlpool(final MOB M)
	{
		if(invoker()!=null)
			return invoker().mayIFight(M);
		return true;
	}

	public boolean canEnterTheWhirlpool(final Rideable R)
	{
		for(final Enumeration<Rider> r2 = R.riders();r2.hasMoreElements();)
		{
			final Rider R2=r2.nextElement();
			if((R2 instanceof MOB)&&(!canEnterTheWhirlpool((MOB)R2)))
				return false;
		}
		return true;
	}

	public boolean canEnterTheWhirlpool(final Boardable S)
	{
		if(S instanceof PrivateProperty)
		{
			if(invoker()!=null)
			{
				if(!CMLib.law().canAttackThisProperty(invoker(), (PrivateProperty)S))
					return false;

			}
		}
		return true;
	}

	public boolean canEverEnterThePool(final MOB M)
	{
		if((invoker!=null)
		&&((invoker==M)
			||(!invoker.mayIFight(M))
			||(invoker.getGroupMembers(new HashSet<MOB>()).contains(M))))
			return false;
		if((!this.canBeUninvoked())
		&&(affected instanceof Room))
		{
			if(CMLib.law().doesHavePriviledgesHere(M, (Room)affected))
				return false;
		}
		boolean enterThePool = false;
		if((M.riding()!=null)&&(!CMLib.flags().isFlying(M.riding())))
		{
			if(M.riding() instanceof Boardable)
				enterThePool = canEnterTheWhirlpool((Boardable)M.riding());
			else
			if(M.riding().rideBasis()==Rideable.Basis.WATER_BASED)
				enterThePool = canEnterTheWhirlpool(M.riding());
		}
		if(canEnterTheWhirlpool(M))
		{
			enterThePool = true;
		}
		return enterThePool;
	}

	public boolean canEverEnterThePool(final Item I)
	{
		if(I instanceof Boardable)
			return canEnterTheWhirlpool((Boardable)I);
		else
		if((I instanceof Rideable)
		&&(((Rideable)I).rideBasis()==Rideable.Basis.WATER_BASED))
			return canEnterTheWhirlpool((Rideable)I);
		if(I.container()!=null)
			return false;
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			for(final Enumeration<Item> r=R.items();r.hasMoreElements();)
			{
				final Item I=r.nextElement();
				if((I!=null)&&(canEverEnterThePool(I)))
				{
					if(I instanceof Boardable)
					{
						final Area A=((Boardable)I).getArea();
						if(A!=null)
						{
							for(final Enumeration<Room> r2=A.getProperMap();r2.hasMoreElements();)
							{
								final Room R2=r2.nextElement();
								if((R2!=null)&&((R2.domainType()&Room.INDOORS)==0))
									R2.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 is swept into a massive whirlpool!",I.Name()));
							}
						}
					}
					R.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 is swept into a massive whirlpool!",I.Name()));
					final GridLocale grid = (GridLocale)theWhirlpool;
					final Room targetRoom = grid.getGridChild(grid.xGridSize()-1, 0);
					if(targetRoom != null)
						targetRoom.moveItemTo(I);
				}
			}
			for(final Enumeration<MOB> r=R.inhabitants();r.hasMoreElements();)
			{
				final MOB M=r.nextElement();
				if((M!=null)&&(canEverEnterThePool(M)))
				{
					if(!M.isMonster())
						M.tell(L("^XYou're swept into a terrible whirlpool!"));
					final GridLocale grid = (GridLocale)theWhirlpool;
					final Room targetRoom = grid.getGridChild(grid.xGridSize()-1, 0);
					if(targetRoom != null)
						targetRoom.bringMobHere(M, false);
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==affected)
		&&(msg.tool() instanceof Exit)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!CMLib.flags().isFlying(msg.source())))
		{
			if(this.canEverEnterThePool(msg.source()))
			{
				final MOB M=msg.source();
				if(theWhirlpool instanceof GridLocale)
				{
					if(!M.isMonster())
						M.tell(L("^XYou've stumbled into a terrible whirlpool!"));
					final GridLocale grid = (GridLocale)theWhirlpool;
					final Room targetRoom = grid.getGridChild(grid.xGridSize()-1, 0);
					if(targetRoom != null)
						msg.setTarget(targetRoom);
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,L("A MASSIVE WHIRLPOOL IS HERE!!!"),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room target=mob.location();
		if(!CMLib.flags().isWaterySurfaceRoom(target))
		{
			mob.tell(L("This magic only works in water surfaces."));
			return false;
		}

		final Room waterBelow = target.getRoomInDir(Directions.DOWN);
		if(!CMLib.flags().isUnderWateryRoom(waterBelow))
		{
			mob.tell(L("This magic only works above the watery deeps."));
			return false;
		}

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("There is already a whirlpool here! Geesh, how did you miss it?!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s) treacherously to the waters.^?"));
			if(target.okMessage(mob,msg))
			{
				target.send(mob,msg);
				target.showHappens(CMMsg.MSG_OK_ACTION, L("A MASSIVE WHIRLPOOL FORMS HERE!"));
				final Chant_Whirlpool W=(Chant_Whirlpool)beneficialAffect(mob,target,asLevel,0);
				if(W!=null)
				{
					W.theWhirlpool = CMClass.getLocale("Whirlpool");
					W.theWhirlpool.setDisplayText(L("You are caught in a massive whirlpool"));
					W.theWhirlpool.setDescription(L("The only way out appear to be to fight against the currents."));
					final Area tempArea=CMClass.getAreaType("StdArea");
					tempArea.setName("A Whirlpool");
					W.theWhirlpool.setSavable(false);
					W.theWhirlpool.setRoomID("");
					W.theWhirlpool.setArea(tempArea);
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						if(d != Directions.DOWN)
						{
							W.theWhirlpool.rawDoors()[d] = target.rawDoors()[d];
							W.theWhirlpool.setRawExit(d, target.getRawExit(d));
						}
					}
					((GridLocale)W.theWhirlpool).buildGrid();
				}
				target.recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) treacherously to the waters, but nothing happens."));

		// return whether it worked
		return success;
	}
}
