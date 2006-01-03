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
public class Woods extends StdRoom
{
	public String ID(){return "Woods";}
	public Woods()
	{
		super();
		name="the woods";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_WOODS;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(CMLib.dice().rollPercentage()==1)
		   &&(CMLib.dice().rollPercentage()==1))
		{
			Ability A=CMClass.getAbility("Disease_PoisonIvy");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		new Integer(RawMaterial.RESOURCE_WOOD),
		new Integer(RawMaterial.RESOURCE_PINE),
		new Integer(RawMaterial.RESOURCE_BALSA),
		new Integer(RawMaterial.RESOURCE_OAK),
		new Integer(RawMaterial.RESOURCE_MAPLE),
		new Integer(RawMaterial.RESOURCE_REDWOOD),
		new Integer(RawMaterial.RESOURCE_IRONWOOD),
		new Integer(RawMaterial.RESOURCE_SAP),
		new Integer(RawMaterial.RESOURCE_YEW),
		new Integer(RawMaterial.RESOURCE_HICKORY),
		new Integer(RawMaterial.RESOURCE_TEAK),
		new Integer(RawMaterial.RESOURCE_CEDAR),
		new Integer(RawMaterial.RESOURCE_ELM),
		new Integer(RawMaterial.RESOURCE_CHERRYWOOD),
		new Integer(RawMaterial.RESOURCE_BEECHWOOD),
		new Integer(RawMaterial.RESOURCE_WILLOW),
		new Integer(RawMaterial.RESOURCE_SYCAMORE),
		new Integer(RawMaterial.RESOURCE_SPRUCE),
		new Integer(RawMaterial.RESOURCE_FRUIT),
		new Integer(RawMaterial.RESOURCE_APPLES),
		new Integer(RawMaterial.RESOURCE_BERRIES),
		new Integer(RawMaterial.RESOURCE_PEACHES),
		new Integer(RawMaterial.RESOURCE_CHERRIES),
		new Integer(RawMaterial.RESOURCE_ORANGES),
		new Integer(RawMaterial.RESOURCE_LEMONS),
		new Integer(RawMaterial.RESOURCE_FUR),
		new Integer(RawMaterial.RESOURCE_NUTS),
		new Integer(RawMaterial.RESOURCE_HERBS),
		new Integer(RawMaterial.RESOURCE_HONEY),
		new Integer(RawMaterial.RESOURCE_VINE),
		new Integer(RawMaterial.RESOURCE_HIDE),
		new Integer(RawMaterial.RESOURCE_FEATHERS),
		new Integer(RawMaterial.RESOURCE_LEATHER)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Woods.roomResources;}
}
