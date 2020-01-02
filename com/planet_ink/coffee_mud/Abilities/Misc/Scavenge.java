package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Scavenge extends StdAbility
{
	@Override
	public String ID()
	{
		return "Scavenge";
	}

	private final static String	localizedName	= CMLib.lang().L("Scavenge");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SCAVENGE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	public static Item getBody(final Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I instanceof DeadBody)
			&&(!((DeadBody)I).isPlayerCorpse())
			&&(((DeadBody)I).getMobName().length()>0))
				return I;
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=getBody(mob.location());
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof DeadBody))
		   ||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell(L("You may only scavenge from the dead."));
			return false;
		}

		if((((DeadBody)target).isPlayerCorpse())
		&&(!((DeadBody)target).getMobName().equals(mob.Name()))
		&&(((DeadBody)target).hasContent()))
		{
			mob.tell(L("You are not allowed to scavenge from that corpse."));
			return false;
		}

		final DeadBody body = (DeadBody)target;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,auto?L("<T-NAME> breaks down."):L("^S<S-NAME> scavenge(s) from <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final List<RawMaterial> resources=body.charStats().getMyRace().myResources();
				final List<RawMaterial> finalResources = new ArrayList<RawMaterial>(resources.size());
				for(int i=0;i<resources.size();i++)
				{
					final Item newFound=resources.get(i);
					if((newFound.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
						finalResources.add((RawMaterial)newFound.copyOf());
				}
				for(int i=0;i<resources.size();i++)
				{
					final Item newFound=resources.get(i);
					if((newFound.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
					{
						for(int x=0;x<(adjustedLevel(mob,asLevel)/8);x++)
							if(i<finalResources.size()-1)
								finalResources.add(i, (RawMaterial)newFound.copyOf());
							else
								finalResources.add((RawMaterial)newFound.copyOf());
						break;
					}
				}
				final List<Ability> diseases=new ArrayList<Ability>();
				for(int i=0;i<body.numEffects();i++)
				{
					final Ability A=body.fetchEffect(i);
					if((A!=null)&&(A instanceof DiseaseAffect))
					{
						if((CMath.bset(((DiseaseAffect)A).spreadBitmap(),DiseaseAffect.SPREAD_CONSUMPTION))
						||(CMath.bset(((DiseaseAffect)A).spreadBitmap(),DiseaseAffect.SPREAD_CONTACT)))
							diseases.add(A);
					}
				}

				for(int i=0;i<finalResources.size();i++)
				{
					final Item newFound=finalResources.get(i);
					if((newFound instanceof Food)||(newFound instanceof Drink))
					{
						for(int d=0;d<diseases.size();d++)
							newFound.addNonUninvokableEffect((Ability)diseases.get(d).copyOf());
					}
					newFound.recoverPhyStats();
					mob.location().addItem(newFound, Expire.Monster_EQ);
				}
				target.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialVisualFizzle(mob,target,auto?"":L("<S-NAME> attempt(s) to scavenge from <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
