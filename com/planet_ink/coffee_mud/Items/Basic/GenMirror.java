package com.planet_ink.coffee_mud.Items.Basic;

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
public class GenMirror extends GenItem
{
	public String ID(){	return "GenMirror";}
	private boolean oncePerRound=false;
	public GenMirror()
	{
		super();
		setName("a generic mirror");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic mirror sits here.");
		setDescription("You see yourself in it!");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_GLASS);
	}
	public String description()
	{
		return "You see yourself in it!";
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((owner==null)||(!(owner instanceof MOB))||(amWearingAt(Item.INVENTORY)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)owner;
		if((msg.amITarget(mob))
		&&(!oncePerRound)
		&&(msg.tool() instanceof Ability)
		&&((msg.tool().ID().equals("Spell_FleshStone"))
			||(msg.tool().ID().equals("Prayer_FleshRock")))
		&&(!mob.amDead())
		&&(mob!=msg.source()))
		{
			oncePerRound=true;
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,name()+" reflects the vicious magic!");
			Ability A=(Ability)msg.tool();
			A.invoke(mob,msg.source(),true,envStats().level());
			return false;
		}
		else
			oncePerRound=false;
		return super.okMessage(myHost,msg);
	}

}
