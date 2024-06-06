package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2024 Bo Zimmerman

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
public class Timsvalue extends StdTest
{
	@Override
	public String ID()
	{
		return "Timsvalue";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final ArrayList<ItemCraftor> V=new ArrayList<ItemCraftor>();
		final Vector<ItemCraftor> craftingSkills=new Vector<ItemCraftor>();
		for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
		{
			final Ability A=e.nextElement();
			if(A instanceof ItemCraftor)
				V.add((ItemCraftor)A.copyOf());
		}
		while(V.size()>0)
		{
			int lowest=Integer.MAX_VALUE;
			ItemCraftor lowestA=null;
			for(int i=0;i<V.size();i++)
			{
				final ItemCraftor A=V.get(i);
				final int ii=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
				if(ii<lowest)
				{
					lowest=ii;
					lowestA=A;
				}
			}
			if(lowestA==null)
				lowestA=V.get(0);
			if(lowestA!=null)
			{
				V.remove(lowestA);
				craftingSkills.add(lowestA);
			}
			else
				break;
		}
		for(final ItemCraftor cA : craftingSkills)
		{
			if(cA.ID().toLowerCase().indexOf("jewel")>=0)
				continue;
			final List<Integer> rscs = cA.myResources();
			if(rscs.size()==0)
				continue;
			final Map<Integer,int[]> mats = new TreeMap<Integer,int[]>();
			for(final Integer mI : rscs)
			{
				final Integer matI=Integer.valueOf(mI.intValue()&RawMaterial.MATERIAL_MASK);
				if(!mats.containsKey(matI))
					mats.put(matI, new int[] {0});
				mats.get(matI)[0]++;
			}
			int mostCommonMat=-1;
			int mostCommonIs=0;
			for(final Integer matI : mats.keySet())
				if(mats.get(matI)[0]>mostCommonIs)
				{
					mostCommonMat=matI.intValue();
					mostCommonIs = mats.get(matI)[0];
				}
			int mostCommonRsc = -1;
			switch(mostCommonMat)
			{
			case RawMaterial.MATERIAL_PAPER:
				mostCommonRsc=RawMaterial.RESOURCE_PAPER;
				break;
			case RawMaterial.MATERIAL_CLOTH:
				mostCommonRsc=RawMaterial.RESOURCE_COTTON;
				break;
			case RawMaterial.MATERIAL_ROCK:
				mostCommonRsc=RawMaterial.RESOURCE_STONE;
				break;
			case RawMaterial.MATERIAL_PRECIOUS:
				mostCommonRsc=RawMaterial.RESOURCE_GEM;
				break;
			case RawMaterial.MATERIAL_SYNTHETIC:
				mostCommonRsc=RawMaterial.RESOURCE_PLASTIC;
				break;
			case RawMaterial.MATERIAL_GLASS:
				mostCommonRsc=RawMaterial.RESOURCE_GLASS;
				break;
			case RawMaterial.MATERIAL_FLESH:
				mostCommonRsc=RawMaterial.RESOURCE_BEEF;
				break;
			case RawMaterial.MATERIAL_LEATHER:
				mostCommonRsc=RawMaterial.RESOURCE_LEATHER;
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
				mostCommonRsc=RawMaterial.RESOURCE_IRON;
				break;
			case RawMaterial.MATERIAL_WOODEN:
				mostCommonRsc=RawMaterial.RESOURCE_OAK;
				break;
			default:
				mostCommonRsc=RawMaterial.RESOURCE_OAK;
				break;
			}
			final List<ItemCraftor.CraftedItem> l=cA.craftAllItemSets(mostCommonRsc,false);
			if(V!=null)
			{
				final String fileName = "skills/"+cA.getRecipeFilename();
				final StringBuffer recipeFile = Resources.getFileResource(fileName, true);
				boolean didSomething = false;
				for(final ItemCraftor.CraftedItem L: l)
				{
					final Item I = L.item;
					if(I.baseGoldValue()<=0)
						continue;
					if(I instanceof FalseLimb)
						continue;
					final String rscName = RawMaterial.CODES.NAME(I.material()).toLowerCase();
					final int oldValue = I.baseGoldValue();
					I.setBaseValue(CMLib.itemBuilder().calculateBaseValue(I));
					boolean modified = false;
					if(CMLib.itemBuilder().calculateBaseValue(I)*3<oldValue)
					{
						mob.tell(I.name()+" is "+oldValue+" which is > 3 * "+I.baseGoldValue());
					}

					if(oldValue > I.baseGoldValue() *2)
					{
						String findName = CMLib.english().removeArticleLead(
								CMStrings.replaceAll(I.name(), rscName+" ", "% "));
						if(findName.startsWith("designer "))
							findName=findName.substring(9);
						final int x = recipeFile.toString().toLowerCase().indexOf(findName.toLowerCase());
						if(x >= 0)
						{
							final int eol = recipeFile.indexOf("\n",x+1);
							final String lineStr = recipeFile.substring(x,eol);
							final String srchStr = ""+oldValue;
							final int valDex = lineStr.indexOf("\t"+srchStr+"\t");
							if(valDex > 0)
							{
								recipeFile.replace(x+valDex+1, x+valDex+srchStr.length()+1, ""+I.baseGoldValue());
								didSomething=true;
								modified = true;
							}

						}
						if(modified)
							mob.tell(I.name()+" value is "+oldValue+" but fixed to "+I.baseGoldValue()+": "+cA.ID()+": "+rscName);
						else
							mob.tell(findName+" ("+x+") value is "+oldValue+" but SHOULD be "+I.baseGoldValue()+": "+cA.ID()+": "+rscName);
					}
				}
				if(didSomething)
				{
					final StringBuffer finalData = new StringBuffer(
						CMStrings.replaceAll(recipeFile.toString(), "\n\r", "\n")
					);
					Resources.updateFileResource(fileName, finalData);
				}
			}
		}
		return null;
	}
}
