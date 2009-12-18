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
   Copyright 2000-2010 Bo Zimmerman

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
public class Swamp extends StdRoom
{
	public String ID(){return "Swamp";}
	public Swamp()
	{
		super();
		name="the swamp";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_SWAMP;}
	public int domainConditions(){return Room.CONDITION_WET;}

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
				A=CMClass.getAbility("Disease_Chlamydia");
			else
				A=CMClass.getAbility("Disease_Malaria");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_JADE),
		Integer.valueOf(RawMaterial.RESOURCE_SCALES),
		Integer.valueOf(RawMaterial.RESOURCE_COCOA),
		Integer.valueOf(RawMaterial.RESOURCE_COAL),
		Integer.valueOf(RawMaterial.RESOURCE_PIPEWEED),
		Integer.valueOf(RawMaterial.RESOURCE_BAMBOO),
		Integer.valueOf(RawMaterial.RESOURCE_SUGAR),
		Integer.valueOf(RawMaterial.RESOURCE_CLAY),
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Swamp.roomResources;}
}
