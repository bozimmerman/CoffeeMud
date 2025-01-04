package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class Druid_KnowPlants extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_KnowPlants";
	}

	private final static String localizedName = CMLib.lang().L("Know Plants");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	private static final String[] triggerStrings =I(new String[] {"KNOWPLANT"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_NATURELORE;
	}

	public static boolean isPlant(final Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A.invoker()!=null)&&(A instanceof Chant_SummonPlants))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item I=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(I==null)
			return false;
		if(((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)
		&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN))
		{
			commonTelL(mob,"Your plant knowledge can tell you nothing about @x1.",I.name(mob));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			commonTelL(mob,"Your plant senses fail you.");
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,I,null,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final StringBuffer str=new StringBuffer("");
				final String rscName = RawMaterial.CODES.NAME(I.material()).toLowerCase();
				final String name = CMStrings.capitalizeAndLower(I.name(mob));
				if(I instanceof RawMaterial)
				{
					final RawMaterial rI=(RawMaterial)I;
					if(!CMStrings.containsWord(name.toLowerCase(), rscName.toLowerCase()))
						str.append(L("@x1 is a kind of @x2.  ",name,rscName));
					if((rI.getSubType()!=null)&&(rI.getSubType().length()>0)
					&&(!CMStrings.containsWord(name.toLowerCase(), rI.getSubType().toLowerCase())))
						str.append(L("@x1 is made of @x2.  ",name,rI.getSubType().toLowerCase()));
				}
				else
					str.append(L("@x1 is made of @x2.  ",name,RawMaterial.CODES.NAME(I.material()).toLowerCase()));
				final String matName = RawMaterial.CODES.MAT_DESC(I.material());
				if(!CMStrings.containsWord(name.toLowerCase(), matName.toLowerCase()))
					str.append(L("@x1 is a type of @x2.  ",CMStrings.capitalizeAndLower(rscName),matName.toLowerCase()));
				if(isPlant(I))
					str.append(L("It was summoned by @x1.  ",I.rawSecretIdentity()));
				else
				if(!(I instanceof RawMaterial))
					str.append(L("It is either processed by hand, or found in the wild.  "));
				commonTell(mob,str.toString().trim());
				final Integer matI = Integer.valueOf(I.material());
				final List<String> foundIn=new ArrayList<String>();
				for(final Enumeration<Room> r=CMClass.locales();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R.resourceChoices()!=null)
					&&(R.resourceChoices().contains(matI))
					&&(!R.name().toLowerCase().endsWith(" room"))
					&&(!foundIn.contains(R.name())))
						foundIn.add(R.name());
				}
				if(foundIn.size()>0)
				{
					if(I instanceof RawMaterial)
					{
						if(((RawMaterial)I).getSubType().equalsIgnoreCase(RawMaterial.ResourceSubType.SEED.name()))
							commonTelL(mob,"It can be grown in @x1.",CMLib.english().toEnglishStringList(foundIn));
						else
						if(CMParms.contains(RawMaterial.CODES.WOODIES(), I.material()))
							commonTelL(mob,"It can be found in @x1.",CMLib.english().toEnglishStringList(foundIn));
						else
							commonTelL(mob,"It can be foraged in @x1.",CMLib.english().toEnglishStringList(foundIn));
					}
					else
					{
						commonTelL(mob,"@x1 can be found in @x2.",
								CMStrings.capitalizeAndLower(rscName),
								CMLib.english().toEnglishStringList(foundIn));
					}
				}
				commonTelL(mob,"@x1 has a hardness of @x2 and a buoyancy of @x3.",
						CMStrings.capitalizeAndLower(rscName),
						""+RawMaterial.CODES.HARDNESS(matI.intValue()),
						""+RawMaterial.CODES.BOUYANCY(matI.intValue()));
				if(I instanceof Food)
				{
					if(I.fetchEffect("Poison_Rotten")!=null)
						commonTelL(mob,"It was edible, before it went rotten.");
					else
					if(I.fetchEffect("Prayer_Rot")!=null)
						commonTelL(mob,"It is edible, but will eventually go bad.");
					else
						commonTelL(mob,"It is edible.");

				}
			}
		}
		return success;
	}
}

