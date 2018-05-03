package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Butchering extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Butchering";
	}

	private final static String	localizedName	= CMLib.lang().L("Butchering");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BUTCHER", "BUTCHERING", "SKIN" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	public String supportedResourceString()
	{
		return "FLESH|LEATHER|BLOOD|BONE|MILK|EGGS|WOOL";
	}

	protected DeadBody	body	= null;
	protected boolean	failed	= false;
	
	public Butchering()
	{
		super();
		displayText=L("You are skinning and butchering something...");
		verb=L("skinning and butchering");
	}

	protected int getDuration(MOB mob, int weight)
	{
		int duration=((weight/(10+getXLEVELLevel(mob))));
		duration = super.getDuration(duration, mob, 1, 3);
		if(duration>40)
			duration=40;
		return duration;
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((body!=null)&&(!aborted))
				{
					if(failed)
						commonTell(mob,L("You messed up your butchering completely."));
					else
					if(mob.location()!=null)
					{
						final CMMsg msg=CMClass.getMsg(mob,body,this,getCompletedActivityMessageType(),
								L("<S-NAME> manage(s) to skin and chop up <T-NAME>."));
						msg.setValue(baseYield()+abilityCode());
						if(mob.location().okMessage(mob, msg))
						{
							mob.location().send(mob, msg);
							final List<RawMaterial> resources=body.charStats().getMyRace().myResources();
							final Vector<Ability> diseases=new Vector<Ability>();
							for(int i=0;i<body.numEffects();i++)
							{
								final Ability A=body.fetchEffect(i);
								if((A!=null)&&(A instanceof DiseaseAffect))
								{
									if((CMath.bset(((DiseaseAffect)A).spreadBitmap(),DiseaseAffect.SPREAD_CONSUMPTION))
									||(CMath.bset(((DiseaseAffect)A).spreadBitmap(),DiseaseAffect.SPREAD_CONTACT)))
										diseases.addElement(A);
								}
							}
							for(int y=0;y<msg.value();y++)
							{
								for(int i=0;i<resources.size();i++)
								{
									final Item newFound=(Item)((Item)resources.get(i)).copyOf();
									if((newFound instanceof Food)||(newFound instanceof Drink))
									{
										for(int d=0;d<diseases.size();d++)
											newFound.addNonUninvokableEffect((Ability)diseases.elementAt(d).copyOf());
									}
									newFound.recoverPhyStats();
									if(!dropAWinner(mob,newFound))
									{
										y=9999;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		body=null;
		Item I=null;

		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
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
				final Item I2=mob.location().getItem(i);
				if((I2 instanceof DeadBody)
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

		if(I==null) 
			return false;
		if((!(I instanceof DeadBody))
		   ||(((DeadBody)I).charStats()==null)
		   ||((DeadBody)I).isPlayerCorpse()
		   ||(((DeadBody)I).charStats().getMyRace()==null))
		{
			commonTell(mob,L("You can't butcher @x1.",I.name(mob)));
			return false;
		}
		final List<RawMaterial> resources=((DeadBody)I).charStats().getMyRace().myResources();
		if((resources==null)||(resources.size()==0))
		{
			commonTell(mob,L("There doesn't appear to be any good parts on @x1.",I.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),getActivityMessageType(),getActivityMessageType(),L("<S-NAME> start(s) butchering <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			body=(DeadBody)I;
			verb=L("skinning and butchering @x1",I.name());
			playSound="ripping.wav";
			final int duration=getDuration(mob,I.phyStats().weight());
			beneficialAffect(mob,mob,asLevel,duration);
			body.emptyPlease(false);
			body.destroy();
		}
		return true;
	}
}
