package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2000-2006 Bo Zimmerman

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
	}
	public int domainType(){return Room.DOMAIN_INDOORS_CAVE;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}

	public int maxRange(){return 5;}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		   &&(CMLib.dice().rollPercentage()==1)
		   &&(CMLib.dice().rollPercentage()==1))
		{
			Ability A=CMClass.getAbility("Disease_Syphilis");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}
	public static final Integer[] resourceList={
		new Integer(RawMaterial.RESOURCE_GRANITE),
		new Integer(RawMaterial.RESOURCE_OBSIDIAN),
		new Integer(RawMaterial.RESOURCE_MARBLE),
		new Integer(RawMaterial.RESOURCE_STONE),
		new Integer(RawMaterial.RESOURCE_IRON),
		new Integer(RawMaterial.RESOURCE_LEAD),
		new Integer(RawMaterial.RESOURCE_GOLD),
		new Integer(RawMaterial.RESOURCE_SILVER),
		new Integer(RawMaterial.RESOURCE_ZINC),
		new Integer(RawMaterial.RESOURCE_COPPER),
		new Integer(RawMaterial.RESOURCE_TIN),
		new Integer(RawMaterial.RESOURCE_MITHRIL),
		new Integer(RawMaterial.RESOURCE_MUSHROOMS),
		new Integer(RawMaterial.RESOURCE_GEM),
		new Integer(RawMaterial.RESOURCE_PERIDOT),
		new Integer(RawMaterial.RESOURCE_DIAMOND),
		new Integer(RawMaterial.RESOURCE_LAPIS),
		new Integer(RawMaterial.RESOURCE_BLOODSTONE),
		new Integer(RawMaterial.RESOURCE_MOONSTONE),
		new Integer(RawMaterial.RESOURCE_ALEXANDRITE),
		new Integer(RawMaterial.RESOURCE_GEM),
		new Integer(RawMaterial.RESOURCE_SCALES),
		new Integer(RawMaterial.RESOURCE_CRYSTAL),
		new Integer(RawMaterial.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return CaveRoom.roomResources;}
}
