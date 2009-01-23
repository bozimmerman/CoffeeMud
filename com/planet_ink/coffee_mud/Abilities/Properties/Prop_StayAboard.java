package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2008 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prop_StayAboard extends Property
{
	public String ID() { return "Prop_StayAboard"; }
	public String name(){ return "Stays on mounted thing";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
    protected Rideable rideable=null;
    public String accountForYourself() { return "Stays on anything mounted to.";}
    protected boolean noRepeat=false;
    
    public void setAffectedOne(Environmental E)
    {
    	super.setAffectedOne(E);
    	if(E instanceof Rider) {
    		rideable = ((Rider)E).riding();
    	}
    }
    
    public void affectEnvStats(Environmental E, EnvStats affectableStats)
    {
    	super.affectEnvStats(E, affectableStats);
    	synchronized(this)
    	{
    		if(noRepeat) return;
    		try {
	    		noRepeat=true;
		    	if(E instanceof Rider)
		    		if(rideable==null)
			    		rideable=((Rider)E).riding();
		    		else
		    		if(!CMLib.flags().isInTheGame(rideable,true))
		    			rideable=null;
		    		else
		    		{
		    			Room rideR=CMLib.map().roomLocation(rideable);
			    		if((CMLib.map().roomLocation(E)!=rideR)
			    		||(((Rider)E).riding()!=rideable))
			    		{
			    			if(((Rider)E).riding()!=null)
			    				((Rider)E).setRiding(null);
			    			if(CMLib.map().roomLocation(E)!=rideR)
			    				if(E instanceof Item)
			    					rideR.bringItemHere((Item)E,-1,true);
			    				else
			    				if(E instanceof MOB)
			    					rideR.bringMobHere((MOB)E,true);
			    			((Rider)E).setRiding(rideable);
			    		}
		    		}
    		} finally {
    			noRepeat=false;
    		}
    	}
    }
}
