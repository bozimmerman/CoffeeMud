package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ThinAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color256;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class MasterPaintingSkill extends PaintingSkill
{
	@Override
	public String ID()
	{
		return "MasterPaintingSkill";
	}

	private final static String localizedName = CMLib.lang().L("Master Painting Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected volatile List<Color256>	colors		= null;
	protected volatile List<String>		colorsNames	= null;
	protected volatile List<String>		recipe		= null;

	@Override
	protected String getRecipeFile()
	{
		return "masterpainting.txt";
	}

	protected String applyColorMask(final String basePhrase, final String colorPhrase, final String fullMask, final List<String> colorNames, final List<Color256> colors)
	{
		String finalColorPhrase = colorPhrase;
		for(int i=0;i<colorsNames.size();i++)
			finalColorPhrase=CMStrings.replaceAll(finalColorPhrase, "@x"+(i+1), colorsNames.get(i));
		final String finalUncoloredPhrase = super.fixColor(basePhrase, finalColorPhrase);
		final Integer ZERO=Integer.valueOf(0);
		final Map<Integer, List<String>> masks = new HashMap<Integer, List<String>>();
		masks.put(ZERO, new ArrayList<String>());
		final String[] maskParts=fullMask.split("\\|");
		for(final String part : maskParts)
		{
			final Integer VAL;
			if(part.endsWith("W"))
			{
				VAL=Integer.valueOf(part.length()-1);
				if(!masks.containsKey(VAL))
					masks.put(VAL, new ArrayList<String>());
			}
			else
				VAL=ZERO;
			masks.get(ZERO).add(part);
		}
		final boolean brokenWords = (masks.size()>1);
		List<String> wordPhraseParts;
		if(brokenWords) // this means there are fixed-word-size rules
			wordPhraseParts=CMParms.parseSpaces(finalUncoloredPhrase, true);
		else
		{
			wordPhraseParts=new ArrayList<String>(1);
			wordPhraseParts.add(finalUncoloredPhrase);
		}
		for(int w=0;w<wordPhraseParts.size();w++)
		{
			String wordPhrase = wordPhraseParts.get(w); // might be a word, might be the whole thing
			Integer maskSetKey;
			if(!brokenWords)
				maskSetKey=ZERO;
			else
			{
				maskSetKey=Integer.valueOf(wordPhrase.length());
				if(!masks.containsKey(maskSetKey))
					maskSetKey=ZERO;
			}
			final List<String> pickMaskFrom=masks.get(ZERO);
			if(pickMaskFrom.size()==0)
				continue;
			String mask=pickMaskFrom.get(CMLib.dice().roll(1, pickMaskFrom.size(), -1));
			int mindex=0;
			for(int ci=0;ci<wordPhrase.length();ci++)
			{
				final char mc=mask.charAt(mindex);
				while((wordPhrase.charAt(ci)==' ')&&(mc!='w')&&(mc!='W'))
					ci++;
				switch(mc)
				{
				case '0':
				{
					final String cStr="^?";
					wordPhrase=wordPhrase.substring(0, ci)+cStr+wordPhrase.substring(ci);
					ci+=cStr.length();
					wordPhraseParts.set(w, wordPhrase);
					break;
				}
				case '1':
				case '2':
				case '3':
				{
					final Color256 C=colors.get(mc-'1');
					final String cStr=C.getCmChars();
					wordPhrase=wordPhrase.substring(0, ci)+cStr+wordPhrase.substring(ci);
					ci+=cStr.length();
					wordPhraseParts.set(w, wordPhrase);
					break;
				}
				case 'w': // break to end of word
					if((!brokenWords)&&(wordPhrase.indexOf(' ',ci)>0))
						ci=wordPhrase.indexOf(' ',ci);
					else
						ci=wordPhrase.length()-1;
					break;
				case 'W': // anchored end of word (length is static, NOT LAST CHAR).
					if((!brokenWords)&&(wordPhrase.indexOf(' ',ci)>0))
						ci=wordPhrase.indexOf(' ',ci);
					else
						ci=wordPhrase.length()-1;
					break;
				case 'e': // skip to last letter of word, respect next w or W
				{
					int len=1;
					if(mask.endsWith("w")||mask.endsWith("W"))
						len=(mask.length()-1-mindex)-1;
					if(brokenWords)
						ci=(wordPhrase.length()-len)-1;
					else
					{
						final int k=wordPhrase.indexOf(' ',ci);
						if(k<0)
							ci=(wordPhrase.length()-len)-1;
						else
							ci=(k-len)-1;
					}
					break;
				}
				case 'E': // skip to beginning of last word
					ci=wordPhrase.length()-1;
					if(brokenWords)
						w=wordPhraseParts.size()-2;
					else
					if(wordPhrase.indexOf(' ')>0)
						ci=wordPhrase.lastIndexOf(' ');
					break;
				case 'F': // skip to next-to-last-word
					ci=wordPhrase.length()-1;
					if(brokenWords)
						w=wordPhraseParts.size()-3;
					else
					if(wordPhrase.indexOf(' ')>0)
						ci=wordPhrase.lastIndexOf(' ', wordPhrase.lastIndexOf(' ')-1);
					break;
				case 'c': // skip to before middle letter(s). Do next thing once for odd, twice for even
					if(brokenWords)
						ci=(wordPhrase.length()/2)-1;
					else
					{
						final int fc;
						if(ci==0)
							fc=0;
						else
						if(wordPhrase.charAt(ci)==' ')
							fc=ci+1;
						else
							fc=wordPhrase.lastIndexOf(' ',ci)+1;
						final int lc=wordPhrase.indexOf(' ',fc);
						ci=fc+(((lc-fc)/2)-1);
					}
					break;
				case 'C': // skip to before center word(s)
					if(brokenWords)
					{
						ci=wordPhrase.length()-1;
						w=(wordPhraseParts.size()/2)-1;
					}
					else
					{
						final List<Integer> spaces=new ArrayList<Integer>();
						for(int j=0;j<wordPhrase.length();j++)
						{
							if(wordPhrase.charAt(j)==' ')
								spaces.add(Integer.valueOf(j));
						}
						final int k=(spaces.size()/2)-1;
						if(k>=0)
							ci=spaces.get(k).intValue();
					}
					break;
				case 'm': // skip to middle of entire phrase
					if(brokenWords)
					{
						w=(wordPhraseParts.size()/2)-1;
						final String thatWord=wordPhraseParts.get(w);
						ci=(thatWord.length()/2)-1;
					}
					else
						ci=(wordPhrase.length()/2)-1;
					break;
				case '-': // go back 1 char
					ci--; // this would make us process this char again
					ci--; // but this will go back one char
					break;
				case '*': // break. finish entire phrase
					ci=wordPhrase.length()-1;
					w=wordPhraseParts.size()-1;
					break;
				}
				mindex++;
				if(mindex>=mask.length())
				{
					mask=pickMaskFrom.get(CMLib.dice().roll(1, pickMaskFrom.size(), -1));
					mindex=0;
				}
			}
		}
		return CMParms.combineWith(wordPhraseParts, ' ');
	}

	@Override
	protected void addPaintJob(final Physical found, String writing)
	{
		final List<Color256> colors;
		final List<String> recipe;
		final List<String> colorsNames;
		synchronized(this)
		{
			colors=this.colors;
			recipe=this.recipe;
			colorsNames=this.colorsNames;
		}

		if((colorsNames!=null)
		&&(colors!=null)
		&&(colors.size()==colorsNames.size()))
		{
			for(int i=0;i<colorsNames.size();i++)
			{
				writing=writing.substring(0, i)+colors.get(i).getCmChars()+writing.substring(i);
				i+=colors.get(i).getCmChars().length();
			}
			writing+="^?";
		}

		if(!saveOldName(found)
		||(colors==null)
		||(colorsNames==null)
		||(recipe==null))
			super.addPaintJob(found, writing);
		else
		{
			final String recipePhrase = recipe.get(RCP_COLOR);
			final String recipeMask = recipe.get(RCP_MASK);

			found.setName(applyColorMask(found.Name(), recipePhrase, recipeMask, colorsNames, colors));
			found.setDisplayText(applyColorMask(found.displayText(), recipePhrase, recipeMask, colorsNames, colors));
			final String colorCode = getColorCode(found.Name());
			found.setDescription(getDescriptionColor(found, colorCode));
			found.text();
		}
	}

	protected final int requiredColorsInRecipe(final String mask)
	{
		if(mask.indexOf('3')>=0)
			return 3;
		return 2;
	}

	protected void doPaintingTest(final MOB mob, final List<List<String>> recipes)
	{
		final List<Color256> allColors=new XVector<Color256>(CMLib.color().getColors256());
		for(int i=0;i<recipes.size();i++)
		{
			final List<String> recipe=recipes.get(i);
			final Item I=CMClass.getBasicItem("GenItem");
			I.setName("a most awesome item");
			I.setDisplayText("the most awesome item sits here.");
			I.text();
			final int numColors=this.requiredColorsInRecipe(recipe.get(RCP_MASK));
			this.found=I;
			this.colors=new ArrayList<Color256>(numColors);
			this.recipe=recipe;
			this.colorsNames=new ArrayList<String>(numColors);
			final int[] useThese = new int[] {9, 10, 11};
			for(int c=0;c<numColors;c++)
			{
				final Color256 C=allColors.get(useThese[c]);
				if(colors.contains(C))
				{
					c--;
					continue;
				}
				colors.add(C);
				if(CMLib.dice().rollPercentage()>50)
					colorsNames.add(C.getName1());
				else
					colorsNames.add(C.getName2());
			}
			this.addPaintJob(I, recipe.get(RCP_FINALNAME));
			mob.tell("^N^."+recipe.get(RCP_FINALNAME));
			mob.tell(I.Name());
			mob.tell(I.displayText()+"\n\r");
		}
	}
}
