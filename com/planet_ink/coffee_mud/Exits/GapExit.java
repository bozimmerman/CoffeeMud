package com.planet_ink.coffee_mud.Exits;
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
public class GapExit extends StdExit
{
	public String ID(){	return "GapExit";}
	public String Name(){ return "a crevasse";}
	public String description(){return "Looks like you'll have to jump it.";}

	public int mobWeight(MOB mob)
	{
		int weight=mob.baseEnvStats().weight();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(!I.amWearingAt(Item.FLOATING_NEARBY)))
				weight+=I.envStats().weight();
		}
		return weight;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		MOB mob=msg.source();
		if(((msg.amITarget(this))||(msg.tool()==this))
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!Sense.isInFlight(mob))
		&&(!Sense.isFalling(mob)))
		{
			int chance=(int)Math.round(Util.div(mobWeight(mob),mob.maxCarry())*(100.0-new Integer(3*mob.charStats().getStat(CharStats.STRENGTH)).doubleValue()));
			if(Dice.rollPercentage()<chance)
			{
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to jump the crevasse, but miss(es) the far ledge!");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s)!!!!");
				MUDFight.postDeath(null,mob,null);
				return false;
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		MOB mob=msg.source();
		if(((msg.amITarget(this))||(msg.tool()==this))
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!Sense.isInFlight(mob))
		&&(!Sense.isFalling(mob)))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> jump(s) the crevasse!");
	}
}
