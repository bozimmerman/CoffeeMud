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
   Copyright 2005-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
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

	protected int burst=0;
	protected int health=0;
	protected int hits=0;
	protected int mana=0;
	protected int move=0;

	@Override
	public String accountForYourself()
	{
		return "faster recovering";
	}

	@Override
	public void setParms(String parameters)
	{
		super.setParms(parameters);
		burst=getVal(parameters,"BURST",0)-1;
		health=getVal(parameters,"HEALTH",0)-1;
		hits=getVal(parameters,"HITS",0)-1;
		mana=getVal(parameters,"MANA",0)-1;
		move=getVal(parameters,"MOVE",0)-1;
	}

	public static int getVal(String text, String key, int defaultValue)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	public void doBe(MOB M, int burst, int health, int hits, int mana, int move)
	{
		if(M==null)
			return;
		for(int i2=0;i2<burst;i2++)
			M.tick(M,Tickable.TICKID_MOB);
		for(int i2=0;i2<health;i2++)
			CMLib.combat().recoverTick(M);
		if(hits!=0)
		{
			final int oldMana=M.curState().getMana();
			final int oldMove=M.curState().getMovement();
			for(int i2=0;i2<hits;i2++)
				CMLib.combat().recoverTick(M);
			M.curState().setMana(oldMana);
			M.curState().setMovement(oldMove);
		}
		if(mana!=0)
		{
			final int oldHP=M.curState().getHitPoints();
			final int oldMove=M.curState().getMovement();
			for(int i2=0;i2<mana;i2++)
				CMLib.combat().recoverTick(M);
			M.curState().setHitPoints(oldHP);
			M.curState().setMovement(oldMove);
		}
		if(move!=0)
		{
			final int oldMana=M.curState().getMana();
			final int oldHP=M.curState().getHitPoints();
			for(int i2=0;i2<mana;i2++)
				CMLib.combat().recoverTick(M);
			M.curState().setMana(oldMana);
			M.curState().setHitPoints(oldHP);
		}
	}

	public void doBe(Room room, int burst, int health, int hits, int mana, int move)
	{
		if(room==null)
			return;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB M=room.fetchInhabitant(i);
			if(M!=null)
				doBe(M,burst,health,hits,mana,move);
		}
	}

	public void doBe(Area area, int burst, int health, int hits, int mana, int move)
	{
		if(area==null)
			return;
		for(final Enumeration r=area.getMetroMap();r.hasMoreElements();)
		{
			final Room R=(Room)r.nextElement();
			doBe(R,burst,health,hits,mana,move);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(ticking instanceof Room)
			doBe((Room)ticking,burst,health,hits,mana,move);
		else
		if(ticking instanceof Area)
			doBe((Area)ticking,burst,health,hits,mana,move);
		else
		if(ticking instanceof Rideable)
		{
			Rider R=null;
			for(int r=0;r<((Rideable)ticking).numRiders();r++)
			{
				R=((Rideable)ticking).fetchRider(r);
				if(R instanceof MOB)
					doBe((MOB)R,burst,health,hits,mana,move);
			}
		}
		else
		if(ticking instanceof MOB)
			doBe((MOB)ticking,burst,health,hits,mana,move);
		else
		if(ticking instanceof Item)
		{
			if(CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof MOB)
			&&(!((Item)ticking).amWearingAt(Wearable.IN_INVENTORY)))
				doBe((MOB)((Item)ticking).owner(),burst,health,hits,mana,move);
			else
			if(!CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof Room))
				doBe((Room)((Item)ticking).owner(),burst,health,hits,mana,move);
		}
		return true;
	}
}
