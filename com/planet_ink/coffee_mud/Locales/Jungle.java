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
public class Jungle extends StdRoom
{
	public String ID(){return "Jungle";}
	public Jungle()
	{
		super();
		name="the jungle";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_JUNGLE;
		domainCondition=Room.CONDITION_HOT;
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
				A=CMClass.getAbility("Disease_Gonorrhea");
			else
				A=CMClass.getAbility("Disease_Malaria");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_PLUMS),
		new Integer(EnvResource.RESOURCE_PINEAPPLES),
		new Integer(EnvResource.RESOURCE_COCONUTS),
		new Integer(EnvResource.RESOURCE_BANANAS),
		new Integer(EnvResource.RESOURCE_LIMES),
		new Integer(EnvResource.RESOURCE_JADE),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_HEMP),
		new Integer(EnvResource.RESOURCE_SILK),
		new Integer(EnvResource.RESOURCE_FRUIT),
		new Integer(EnvResource.RESOURCE_APPLES),
		new Integer(EnvResource.RESOURCE_BERRIES),
		new Integer(EnvResource.RESOURCE_ORANGES),
		new Integer(EnvResource.RESOURCE_COFFEEBEANS),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_VINE),
		new Integer(EnvResource.RESOURCE_LEMONS),
		new Integer(EnvResource.RESOURCE_FUR),
		new Integer(EnvResource.RESOURCE_FEATHERS)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Jungle.roomResources;}
}
