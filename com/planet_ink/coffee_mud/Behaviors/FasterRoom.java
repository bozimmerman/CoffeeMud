package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class FasterRoom extends StdBehavior
{
	public String ID(){return "FasterRoom";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
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
						return Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}
	public void doBe(Room room, int burst, int health, int hits, int mana, int move)
	{
		if(room==null) return;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if(M!=null)
			{
				for(int i2=0;i2<burst;i2++)
					M.tick(M,MudHost.TICK_MOB);
				for(int i2=0;i2<health;i2++)
					M.curState().recoverTick(M,M.maxState());
				if(hits!=0)
				{
					int oldMana=M.curState().getMana();
					int oldMove=M.curState().getMovement();
					for(int i2=0;i2<mana;i2++)
						M.curState().recoverTick(M,M.maxState());
					M.curState().setMana(oldMana);
					M.curState().setMovement(oldMove);
				}
				if(mana!=0)
				{
					int oldHP=M.curState().getHitPoints();
					int oldMove=M.curState().getMovement();
					for(int i2=0;i2<mana;i2++)
						M.curState().recoverTick(M,M.maxState());
					M.curState().setHitPoints(oldHP);
					M.curState().setMovement(oldMove);
				}
				if(move!=0)
				{
					int oldMana=M.curState().getMana();
					int oldHP=M.curState().getHitPoints();
					for(int i2=0;i2<mana;i2++)
						M.curState().recoverTick(M,M.maxState());
					M.curState().setMana(oldMana);
					M.curState().setHitPoints(oldHP);
				}
			}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(((tickID==MudHost.TICK_AREA)||(tickID==MudHost.TICK_ROOM_BEHAVIOR)))
		{
			int burst=getVal(getParms(),"BURST",0)-1;
			int health=getVal(getParms(),"HEALTH",0)-1;
			int hits=getVal(getParms(),"HITS",0)-1;
			int mana=getVal(getParms(),"MANA",0)-1;
			int move=getVal(getParms(),"MOVE",0)-1;
			if(ticking instanceof Room)
				doBe((Room)ticking,burst,health,hits,mana,move);
			else
			if(ticking instanceof Area)
			{
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					doBe(R,burst,health,hits,mana,move);
				}
			}
		}
		return true;
	}
}
