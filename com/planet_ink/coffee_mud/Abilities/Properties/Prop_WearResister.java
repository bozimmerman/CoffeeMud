package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_WearResister extends Property
{
	public String ID() { return "Prop_WearResister"; }
	public String name(){ return "Resistance due to worn";}
	public boolean bubbleAffect(){return true;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	private CharStats adjCharStats=null;


	public String accountForYourself()
	{
		String id="The wearer gains resistances: "+text();
		return id;
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		Prop_HaveResister.setAdjustments(this,adjCharStats);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affected !=null)
		&&(affected instanceof Item)
		&&(!((Item)affected).amWearingAt(Item.INVENTORY)))
			Prop_HaveResister.adjCharStats(affectedStats,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected !=null)
		&&(affected instanceof Item)
		&&(!((Item)affected).amWearingAt(Item.INVENTORY))
		&&(((Item)affected).owner() instanceof MOB))
		{
			MOB mob=(MOB)((Item)affected).owner();
			if((msg.amITarget(mob))&&(msg.value()<=0)&&(mob.location()!=null))
			{
				if(!Prop_HaveResister.isOk(msg,this,mob))
					return false;
				Prop_HaveResister.resistAffect(msg,mob,this);
			}
		}
		return true;
	}

}
