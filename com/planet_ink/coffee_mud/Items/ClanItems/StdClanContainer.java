package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
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
public class StdClanContainer extends StdContainer implements ClanItem
{
	public String ID(){	return "StdClanContainer";}
	protected String myClan="";
	protected int ciType=0;
	private long lastClanCheck=0;
	public int ciType(){return ciType;}
	public void setCIType(int type){ ciType=type;}
	public StdClanContainer()
	{
		super();

		setName("a clan container");
		baseEnvStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		capacity=100;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}

	public String clanID(){return myClan;}
	public void setClanID(String ID){myClan=ID;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
	    if((System.currentTimeMillis()-lastClanCheck)>IQCalendar.MILI_HOUR)
	    {
		    if((clanID().length()>0)&&(Clans.getClan(clanID())==null))
		        destroy();
		    lastClanCheck=System.currentTimeMillis();
	    }
		if(StdClanItem.stdExecuteMsg(this,msg))
			super.executeMsg(myHost,msg);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(StdClanItem.stdOkMessage(this,msg))
			return super.okMessage(myHost,msg);
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!StdClanItem.standardTick(this,tickID))
			return false;
		return super.tick(ticking,tickID);
	}
}
