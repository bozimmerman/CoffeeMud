package com.planet_ink.coffee_mud.Locales;

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
public class Swamp extends StdRoom
{
	public String ID(){return "Swamp";}
	public Swamp()
	{
		super();
		name="the swamp";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_SWAMP;
		domainCondition=Room.CONDITION_WET;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		   &&(Dice.rollPercentage()==1)
		   &&(Dice.rollPercentage()==1))
		{
			Ability A=null;
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Chlamydia");
			else
				A=CMClass.getAbility("Disease_Malaria");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_JADE),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_COCOA),
		new Integer(EnvResource.RESOURCE_COAL),
		new Integer(EnvResource.RESOURCE_PIPEWEED),
		new Integer(EnvResource.RESOURCE_BAMBOO),
		new Integer(EnvResource.RESOURCE_SUGAR),
		new Integer(EnvResource.RESOURCE_CLAY),
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Swamp.roomResources;}
}
