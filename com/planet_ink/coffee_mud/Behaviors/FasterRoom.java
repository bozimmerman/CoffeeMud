package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
	public void doBe(Room room, int burst, int health)
	{
		if(room==null) return;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if(M!=null)
			{
				for(int i2=0;i2<burst;i2++)
					M.tick(Host.MOB_TICK);
				for(int i2=0;i2<health;i2++)
					M.curState().recoverTick(M,M.maxState());
			}
		}
	}
	public void tick(Environmental ticking, int tickID)
	{
		if(((tickID==Host.AREA_TICK)||(tickID==Host.ROOM_BEHAVIOR_TICK)))
		{
			int burst=getVal(getParms(),"BURST",0)-1;
			int health=getVal(getParms(),"HEALTH",0)-1;
			if(ticking instanceof Room)
				doBe((Room)ticking,burst,health);
			else
			if(ticking instanceof Area)
			{
				Area area=(Area)ticking;
				Vector V=area.getMyMap();
				for(int v=0;v<V.size();v++)
					doBe((Room)V.elementAt(v),burst,health);
			}
		}
	}
}
