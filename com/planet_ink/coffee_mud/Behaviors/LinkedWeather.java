package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class LinkedWeather extends StdBehavior
{
	public String ID(){return "LinkedWeather";}
	protected int canImproveCode(){return Behavior.CAN_AREAS;}


	protected long lastWeather=-1;
	protected long lastPending=-1;
	protected String areaName=null;
	protected boolean rolling=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=MudHost.TICK_AREA) return true;
		if(!(ticking instanceof Area)) return true;
		if(areaName==null){
			if(getParms().length()==0)
				return true;
			String s=getParms();
			int x=s.indexOf(";");
			rolling=false;
			if(x>=0)
			{
				if(s.indexOf("ROLL",x+1)>=0)
				   rolling=true;
				s=s.substring(0,x);
			}
			Area A=CMMap.getArea(s);
			if(A!=null) areaName=A.Name();
		}

		Area A=(Area)ticking;
		Area linkedA=CMMap.getArea(areaName);
		if((A!=null)&&(linkedA!=null))
		{
			if(rolling)
				A.getClimateObj().setNextWeatherType(linkedA.getClimateObj().weatherType(null));
			else
			{
				A.getClimateObj().setCurrentWeatherType(linkedA.getClimateObj().weatherType(null));
				A.getClimateObj().setNextWeatherType(linkedA.getClimateObj().nextWeatherType(null));
			}
		}
		return true;
	}
}
