package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://falserealities.game-host.org</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class ZapperHunter extends ActiveTicker 
{
	public String ID(){return "ZapperHunter";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY|Behavior.FLAG_POTENTIALLYAGGRESSIVE;}
	private boolean debug=false;

	public ZapperHunter() 
	{
		super();
		minTicks=600; maxTicks=1200; chance=100;
		tickReset();
	}
	public Behavior newInstance()
	{
	    return new ZapperHunter();
	}

	private boolean isHunting(MOB mob)
	{
		Ability A=mob.fetchAbility("Thief_Assasinate");
		if(A!=null) return true;
		return false;
	}

	private MOB findPrey(MOB mob)
	{
		MOB prey=null;
		Area a=mob.location().getArea();
		for(int r=0;r<a.mapSize()*2;r++)
		{
			Room R=(Room)a.getRandomRoom();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(SaucerSupport.zapperCheck(getParms(),M)) 
				{	
					prey=M;
					break;
				}
			}
		}
		return prey;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
	    super.tick(ticking,tickID);
	    if((canAct(ticking,tickID))&&(ticking instanceof MOB))
	    {
			MOB mob=(MOB)ticking;
			if(debug) Log.sysOut("ZAPHUNT", "Tick starting");
			if(!isHunting(mob))
			{
				if(debug) Log.sysOut("ZAPHUNT", "'"+mob.Name()+"' not hunting.");
				MOB prey=findPrey(mob);
				if(prey!=null)
				{
					if(debug) Log.sysOut("ZAPHUNT", "'"+mob.Name()+"' found prey: '"+prey.Name()+"'");
					Ability A=CMClass.getAbility("Thief_Assassinate");
					mob.addAbility(A);
					A.setProfficiency(100);
					mob.curState().setMana(mob.maxState().getMana());
					mob.curState().setMovement(mob.maxState().getMovement());
					A.invoke(mob, null, prey, false);
				}
			}
	    }
	    return true;
	}
}