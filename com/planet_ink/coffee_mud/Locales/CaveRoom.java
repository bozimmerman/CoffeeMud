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
public class CaveRoom extends StdRoom
{
	public String ID(){return "CaveRoom";}
	public CaveRoom()
	{
		super();
		name="the cave";
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;

		domainCondition=Room.CONDITION_NORMAL;
	}

	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		   &&(Dice.rollPercentage()==1)
		   &&(Dice.rollPercentage()==1))
		{
			Ability A=CMClass.getAbility("Disease_Syphilis");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_GRANITE),
		new Integer(EnvResource.RESOURCE_OBSIDIAN),
		new Integer(EnvResource.RESOURCE_MARBLE),
		new Integer(EnvResource.RESOURCE_STONE),
		new Integer(EnvResource.RESOURCE_IRON),
		new Integer(EnvResource.RESOURCE_LEAD),
		new Integer(EnvResource.RESOURCE_GOLD),
		new Integer(EnvResource.RESOURCE_SILVER),
		new Integer(EnvResource.RESOURCE_ZINC),
		new Integer(EnvResource.RESOURCE_COPPER),
		new Integer(EnvResource.RESOURCE_TIN),
		new Integer(EnvResource.RESOURCE_MITHRIL),
		new Integer(EnvResource.RESOURCE_MUSHROOMS),
		new Integer(EnvResource.RESOURCE_GEM),
		new Integer(EnvResource.RESOURCE_PERIDOT),
		new Integer(EnvResource.RESOURCE_DIAMOND),
		new Integer(EnvResource.RESOURCE_LAPIS),
		new Integer(EnvResource.RESOURCE_BLOODSTONE),
		new Integer(EnvResource.RESOURCE_MOONSTONE),
		new Integer(EnvResource.RESOURCE_ALEXANDRITE),
		new Integer(EnvResource.RESOURCE_GEM),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_CRYSTAL),
		new Integer(EnvResource.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
