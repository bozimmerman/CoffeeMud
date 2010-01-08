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
public class Butchering extends GatheringSkill
{
	public String ID() { return "Butchering"; }
	public String name(){ return "Butchering";}
	private static final String[] triggerStrings = {"BUTCHER","BUTCHERING","SKIN"};
	public String[] triggerStrings(){return triggerStrings;}

	public String supportedResourceString(){return "FLESH|LEATHER|BLOOD|BONE|MILK|EGGS|WOOL";}
    protected DeadBody body=null;
	protected boolean failed=false;
	public Butchering()
	{
		super();
		displayText="You are skinning and butchering something...";
		verb="skinning and butchering";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((body!=null)&&(!aborted))
				{
					if(failed)
						commonTell(mob,"You messed up your butchering completely.");
					else
					{
						mob.location().show(mob,null,body,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to skin and chop up <O-NAME>.");
						Vector resources=body.charStats().getMyRace().myResources();
						Vector diseases=new Vector();
						for(int i=0;i<body.numEffects();i++)
						{
							Ability A=body.fetchEffect(i);
							if((A!=null)&&(A instanceof DiseaseAffect))
							{
								if((CMath.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
								||(CMath.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
									diseases.addElement(A);
							}
						}
						for(int y=0;y<abilityCode();y++)
						{
							for(int i=0;i<resources.size();i++)
							{
								Item newFound=(Item)((Item)resources.elementAt(i)).copyOf();
								if((newFound instanceof Food)||(newFound instanceof Drink))
								for(int d=0;d<diseases.size();d++)
									newFound.addNonUninvokableEffect((Ability)((Ability)diseases.elementAt(d)).copyOf());
								newFound.recoverEnvStats();
								mob.location().addItemRefuse(newFound,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_RESOURCE));
								mob.location().recoverRoomStats();
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		body=null;
		Item I=null;
		
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
		
		
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I2=mob.location().fetchItem(i);
				if((I2!=null)
				&&(I2 instanceof DeadBody)
				&&(CMLib.flags().canBeSeenBy(I2,mob))
				&&(I2.container()==null))
				{
					I=I2;
					break;
				}
			}
		}
		else
			I=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);

		if(I==null) return false;
		if((!(I instanceof DeadBody))
		   ||(((DeadBody)I).charStats()==null)
           ||((DeadBody)I).playerCorpse()
		   ||(((DeadBody)I).charStats().getMyRace()==null))
		{
			commonTell(mob,"You can't butcher "+I.name()+".");
			return false;
		}
		Vector resources=((DeadBody)I).charStats().getMyRace().myResources();
		if((resources==null)||(resources.size()==0))
		{
			commonTell(mob,"There doesn't appear to be any good parts on "+I.name()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,I,this,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) butchering <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			body=(DeadBody)I;
			verb="skinning and butchering "+I.name();
            playSound="ripping.wav";
			int duration=((I.envStats().weight()/(10+getXLEVELLevel(mob))));
			if(duration<3) duration=3;
			if(duration>40) duration=40;
			beneficialAffect(mob,mob,asLevel,duration);
			body.emptyPlease();
			body.destroy();
		}
		return true;
	}
}
