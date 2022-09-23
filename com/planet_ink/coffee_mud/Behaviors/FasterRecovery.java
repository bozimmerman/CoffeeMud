package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2005-2022 Bo Zimmerman

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
public class FasterRecovery extends StdBehavior
{
	@Override
	public String ID()
	{
		return "FasterRecovery";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS|Behavior.CAN_ITEMS;
	}

	protected static enum RecType
	{
		BURST,
		HEALTH,
		HITS,
		MANA,
		MOVE
	}

	@SuppressWarnings("unchecked")
	protected Triad<RecType,Integer,int[]>[] changes = new Triad[0];

	@Override
	public String accountForYourself()
	{
		return "faster/slower recovering";
	}

	@Override
	public void setParms(final String parameters)
	{
		super.setParms(parameters);
		final List<Triad<RecType,Integer,int[]>> lst = new ArrayList<Triad<RecType,Integer,int[]>>();
		for(final RecType r : RecType.values())
		{
			final int val  = CMParms.getParmInt(parameters,r.name(),1)-1;
			if(val != 0)
				lst.add(new Triad<RecType,Integer,int[]>(r,Integer.valueOf(val),new int[] {0}));
		}
		@SuppressWarnings("unchecked")
		final Triad<RecType,Integer,int[]>[] ch = new Triad[lst.size()];
		changes = lst.toArray(ch);
	}

	public void doBe(final MOB M)
	{
		if((M==null)||(changes.length==0))
			return;
		for(final Triad<RecType,Integer,int[]> typ : changes)
		{
			switch(typ.first)
			{
			case BURST:
				for(int i2=0;i2<typ.second.intValue();i2++)
					M.tick(M,Tickable.TICKID_MOB);
				break;
			case HEALTH:
				for(int i2=0;i2<typ.second.intValue();i2++)
					CMLib.combat().recoverTick(M);
				break;
			case HITS:
			{
				final int oldMana=M.curState().getMana();
				final int oldMove=M.curState().getMovement();
				for(int i2=0;i2<RecType.HITS.ordinal();i2++)
					CMLib.combat().recoverTick(M);
				M.curState().setMana(oldMana);
				M.curState().setMovement(oldMove);
				break;
			}
			case MANA:
			{
				final int oldHP=M.curState().getHitPoints();
				final int oldMove=M.curState().getMovement();
				for(int i2=0;i2<RecType.MANA.ordinal();i2++)
					CMLib.combat().recoverTick(M);
				M.curState().setHitPoints(oldHP);
				M.curState().setMovement(oldMove);
				break;
			}
			case MOVE:
			{
				final int oldMana=M.curState().getMana();
				final int oldHP=M.curState().getHitPoints();
				for(int i2=0;i2<RecType.MOVE.ordinal();i2++)
					CMLib.combat().recoverTick(M);
				M.curState().setMana(oldMana);
				M.curState().setHitPoints(oldHP);
				break;
			}
			default:
				break;
			}
		}
	}

	public void doBe(final Room room)
	{
		if(room==null)
			return;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB M=room.fetchInhabitant(i);
			if(M!=null)
				doBe(M);
		}
	}

	public void doBe(final Area area)
	{
		if(area==null)
			return;
		for(final Enumeration<Room> r=area.getMetroMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			doBe(R);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(ticking instanceof Room)
			doBe((Room)ticking);
		else
		if(ticking instanceof Area)
			doBe((Area)ticking);
		else
		if(ticking instanceof Rideable)
		{
			Rider R=null;
			for(int r=0;r<((Rideable)ticking).numRiders();r++)
			{
				R=((Rideable)ticking).fetchRider(r);
				if(R instanceof MOB)
					doBe((MOB)R);
			}
		}
		else
		if(ticking instanceof MOB)
			doBe((MOB)ticking);
		else
		if(ticking instanceof Item)
		{
			if(CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof MOB)
			&&(((Item)ticking).amBeingWornProperly()))
				doBe((MOB)((Item)ticking).owner());
			else
			if(!CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof Room))
				doBe((Room)((Item)ticking).owner());
		}
		return true;
	}
}
