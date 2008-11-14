package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

@SuppressWarnings("unchecked")
public class MOBHunter extends ActiveTicker
{
	public String ID(){return "MOBHunter";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY|Behavior.FLAG_POTENTIALLYAGGRESSIVE;}
	protected boolean debug=false;
	int radius=20;

	public MOBHunter()
	{
		super();
		minTicks=600; maxTicks=1200; chance=100; radius=15;
		tickReset();
	}


	protected boolean isHunting(MOB mob)
	{
		Ability A=mob.fetchEffect("Thief_Assasinate");
		if(A!=null) return true;
		return false;
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		radius=CMParms.getParmInt(newParms,"radius",radius);
	}
	protected MOB findPrey(MOB mob)
	{
		MOB prey=null;
		Vector rooms=new Vector();
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.OPENONLY)
				.add(TrackingLibrary.TrackingFlag.AREAONLY)
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.add(TrackingLibrary.TrackingFlag.NOAIR)
				.add(TrackingLibrary.TrackingFlag.NOWATER);
		CMLib.tracking().getRadiantRooms(mob.location(),rooms,flags,null,radius,null);
		for(int r=0;r<rooms.size();r++)
		{
			Room R=(Room)rooms.elementAt(r);
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(CMLib.masking().maskCheck(getParms(),M,false))
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
					A.setProficiency(100);
					mob.curState().setMana(mob.maxState().getMana());
					mob.curState().setMovement(mob.maxState().getMovement());
					A.invoke(mob, new Vector(), prey, false,0);
				}
			}
	    }
	    return true;
	}
}
