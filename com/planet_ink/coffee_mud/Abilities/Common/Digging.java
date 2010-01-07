package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Digging extends GatheringSkill
{
	public String ID() { return "Digging"; }
	public String name(){ return "Gem Digging";}
	private static final String[] triggerStrings = {"DIG","DIGGING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;}
	protected boolean allowedWhileMounted(){return false;}
	public String supportedResourceString(){return "GLASS|PRECIOUS|SAND|STONE";}

	protected Item found=null;
	protected String foundShortName="";
	public Digging()
	{
		super();
		displayText="You are digging...";
		verb="digging";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==3)
			{
				if(found!=null)
				{
					commonTell(mob,"You have found some "+foundShortName+"!");
					displayText="You are digging out "+foundShortName;
					verb="digging out "+foundShortName;
					if((found.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
						tickDown=tickDown*3;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth digging up here.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted))
				{
					int amount=1;
					if(CMLib.dice().rollPercentage()>90)
						amount++;
					if((found.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
						amount=CMLib.dice().roll(1,55,0);
					amount=amount*abilityCode();
					String s="s";
					if((amount==1)||(foundShortName.endsWith("s"))) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to dig out "+amount+" "+foundShortName+s+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_RESOURCE));
						//CMLib.commands().postGet(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
            bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
			    return super.bundle(mob,commands);
		    return false;
		}
		
		verb="digging";
        playSound="dig.wav";
		found=null;
		if((!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_PRECIOUS,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_GLASS,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_SAND,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_STONE,mob.location())))
		{
			commonTell(mob,"You don't think this is a good place to dig.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		   &&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
		   ||((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
		   ||(resourceType==RawMaterial.RESOURCE_SAND)
		   ||(resourceType==RawMaterial.RESOURCE_STONE)))
		{
			found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		
		int duration=getDuration(60,mob,1,15);
		CMMsg msg=CMClass.getMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) digging.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
