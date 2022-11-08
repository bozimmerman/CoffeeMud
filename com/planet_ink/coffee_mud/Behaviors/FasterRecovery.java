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
	protected Triad<RecType,Integer,int[]>[] tickChanges = new Triad[0];

	@Override
	public String accountForYourself()
	{
		return "faster recovering";
	}

	@Override
	public void setParms(final String parameters)
	{
		super.setParms(parameters);
		final List<Triad<RecType,Integer,int[]>> lst = new ArrayList<Triad<RecType,Integer,int[]>>();
		for(final RecType r : RecType.values())
		{
			String val = CMParms.getParmStr(parameters, r.name(), "").trim();
			if(val.length()>0)
			{
				boolean abs = false;
				if(((val.charAt(0)=='+') || (val.charAt(0)=='-'))
				&&(val.length()>1))
				{
					val=val.substring(1);
					abs=true;
				}
				if(Character.isDigit(val.charAt(0)))
				{
					int valn;
					int ticks = 1;
					final int x=val.indexOf('/');
					if(x>0)
					{
						valn = CMath.s_int(val.substring(0,x).trim());
						ticks = CMath.s_int(val.substring(x+1).trim());
					}
					else
						valn  = CMath.s_int(val);
					if(valn != 0)
					{
						if(abs)
							lst.add(new Triad<RecType,Integer,int[]>(r,Integer.valueOf(valn),new int[] {ticks, ticks, 0}));
						else
							lst.add(new Triad<RecType,Integer,int[]>(r,Integer.valueOf(valn),new int[] {ticks, ticks}));
					}
				}
				else
					Log.errOut("Unknown val '"+val+"' on FasterRecovery");
			}
		}
		@SuppressWarnings("unchecked")
		final Triad<RecType,Integer,int[]>[] ch = new Triad[lst.size()];
		tickChanges = lst.toArray(ch);
	}

	public void recoverTick(final MOB M)
	{
		if((M==null)||(tickChanges.length==0))
			return;
		for(final Triad<RecType,Integer,int[]> typ : tickChanges)
		{
			final int[] td;
			synchronized(typ.third)
			{
				td = typ.third;
			}
			if(--td[0] > 0)
				continue;
			td[0] = td[1];
			if((td.length==3) && (td[2] == 1))
			{
				final int val = typ.second.intValue();
				switch(typ.first)
				{
				case BURST:
				case HEALTH:
					M.curState().adjHitPoints(val, M.maxState());
					M.curState().adjMana(val, M.maxState());
					M.curState().adjMovement(val, M.maxState());
					break;
				case HITS:
					M.curState().adjHitPoints(val, M.maxState());
					break;
				case MANA:
					M.curState().adjMana(val, M.maxState());
					break;
				case MOVE:
					M.curState().adjMovement(val, M.maxState());
					break;
				default:
					break;
				}
			}
			else
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
	}

	public void recoverTick(final Room room)
	{
		if(room==null)
			return;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB M=room.fetchInhabitant(i);
			if(M!=null)
				recoverTick(M);
		}
	}

	public void recoverTick(final Area area)
	{
		if(area==null)
			return;
		for(final Enumeration<Room> r=area.getMetroMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			recoverTick(R);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickChanges.length==0)
			return super.tick(ticking, tickID);
		if(ticking instanceof Room)
			recoverTick((Room)ticking);
		else
		if(ticking instanceof Area)
			recoverTick((Area)ticking);
		else
		if(ticking instanceof Rideable)
		{
			Rider R=null;
			for(int r=0;r<((Rideable)ticking).numRiders();r++)
			{
				R=((Rideable)ticking).fetchRider(r);
				if(R instanceof MOB)
					recoverTick((MOB)R);
			}
		}
		else
		if(ticking instanceof MOB)
			recoverTick((MOB)ticking);
		else
		if(ticking instanceof Item)
		{
			if(CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof MOB)
			&&(((Item)ticking).amBeingWornProperly()))
				recoverTick((MOB)((Item)ticking).owner());
			else
			if(!CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof Room))
				recoverTick((Room)((Item)ticking).owner());
		}
		return super.tick(ticking, tickID);
	}
}
