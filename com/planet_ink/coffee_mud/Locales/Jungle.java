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
public class Jungle extends StdRoom
{
	public String ID(){return "Jungle";}
	public Jungle()
	{
		super();
		name="the jungle";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_JUNGLE;}
	public int domainConditions(){return Room.CONDITION_HOT;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		   &&(!msg.source().isMonster())
		   &&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		   &&(CMLib.dice().rollPercentage()==1)
		   &&(CMLib.dice().rollPercentage()==1)
		   &&(!CMSecurity.isDisabled("AUTODISEASE")))
		{
			Ability A=null;
			if(CMLib.dice().rollPercentage()>50)
				A=CMClass.getAbility("Disease_Gonorrhea");
			else
				A=CMClass.getAbility("Disease_Malaria");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		new Integer(RawMaterial.RESOURCE_PLUMS),
		new Integer(RawMaterial.RESOURCE_PINEAPPLES),
		new Integer(RawMaterial.RESOURCE_COCONUTS),
		new Integer(RawMaterial.RESOURCE_BANANAS),
		new Integer(RawMaterial.RESOURCE_LIMES),
		new Integer(RawMaterial.RESOURCE_JADE),
		new Integer(RawMaterial.RESOURCE_SCALES),
		new Integer(RawMaterial.RESOURCE_HEMP),
		new Integer(RawMaterial.RESOURCE_SILK),
		new Integer(RawMaterial.RESOURCE_FRUIT),
		new Integer(RawMaterial.RESOURCE_APPLES),
		new Integer(RawMaterial.RESOURCE_BERRIES),
		new Integer(RawMaterial.RESOURCE_ORANGES),
		new Integer(RawMaterial.RESOURCE_COFFEEBEANS),
		new Integer(RawMaterial.RESOURCE_HERBS),
		new Integer(RawMaterial.RESOURCE_VINE),
		new Integer(RawMaterial.RESOURCE_LEMONS),
		new Integer(RawMaterial.RESOURCE_FUR),
		new Integer(RawMaterial.RESOURCE_FEATHERS)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Jungle.roomResources;}
}
