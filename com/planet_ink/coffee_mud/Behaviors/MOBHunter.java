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

public class MOBHunter extends ActiveTicker 
{
	public String ID(){return "MOBHunter";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY|Behavior.FLAG_POTENTIALLYAGGRESSIVE;}
	private boolean debug=false;
	int radius=20;

	public MOBHunter() 
	{
		super();
		minTicks=600; maxTicks=1200; chance=100; radius=20;
		tickReset();
	}
	public Behavior newInstance()
	{
	    return new MOBHunter();
	}

	private boolean isHunting(MOB mob)
	{
		Ability A=mob.fetchAffect("Thief_Assasinate");
		if(A!=null) return true;
		return false;
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		radius=getParmVal(newParms,"radius",radius);
	}
	private MOB findPrey(MOB mob)
	{
		MOB prey=null;
		Vector rooms=new Vector();
		SaucerSupport.getRadiantRooms(mob.location(),rooms,true,true,true,null,radius);
		for(int r=0;r<rooms.size();r++)
		{
			Room R=(Room)rooms.elementAt(r);
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