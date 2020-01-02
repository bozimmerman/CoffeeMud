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
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
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
public class PaintingSkill extends CommonSkill
{
	@Override
	public String ID()
	{
		return "PaintingSkill";
	}

	private final static String localizedName = CMLib.lang().L("Painting Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	// common recipe definition indexes
	protected static final int	RCP_FINALNAME	= 0;
	protected static final int	RCP_LEVEL		= 1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_COLOR		= 3;
	protected static final int	RCP_MASK		= 4;
	protected static final int	RCP_EXPERTISE	= 5;
	protected static final int	RCP_MISC		= 6;

	protected Item found=null;
	protected String writing="";

	protected String fixColor(final String name, final String colorWord)
	{
		final Vector<String> V=CMParms.parse(name);
		for(int v=0;v<V.size();v++)
		{
			final String word=V.elementAt(v);
			if((word.equalsIgnoreCase("an")) || (word.equalsIgnoreCase("a")))
			{
				final String properPrefix=CMLib.english().properIndefiniteArticle(colorWord);
				V.insertElementAt(colorWord,v+1);
				if(word.toLowerCase().equals(word))
					V.set(v,properPrefix.toLowerCase());
				else
					V.set(v,CMStrings.capitalizeAndLower(properPrefix));
				return CMParms.combine(V,0);
			}
			else
			if((word.equalsIgnoreCase("of"))
			||(word.equalsIgnoreCase("some"))
			||(word.equalsIgnoreCase("the")))
			{
				V.insertElementAt(colorWord,v+1);
				return CMParms.combine(V,0);
			}
		}
		V.insertElementAt(colorWord,0);
		return CMParms.combine(V,0);
	}

	public List<String> getAllPaintingPhrases()
	{
		@SuppressWarnings("unchecked")
		List<String> colors = (List<String>)Resources.getResource("SYSTEM_PAINTING_PHRASES");
		if(colors == null)
		{
			colors = new LinkedList<String>();
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof PaintingSkill)
				{
					final PaintingSkill P=(PaintingSkill)A;
					if(!P.ID().endsWith("PaintingSkill"))
					{
						for(final List<String> recipe : super.loadRecipes(P.getRecipeFile()))
						{
							if(recipe.size()>RCP_COLOR)
							{
								final String colorPhrase = recipe.get(RCP_COLOR).toLowerCase();
								if(!colors.contains(colorPhrase))
									colors.add(colorPhrase);
							}
						}
					}
				}
			}
			Resources.submitResource("SYSTEM_PAINTING_PHRASES", colors);
		}
		return colors;
	}

	public List<String> getAllColors256NamesLowercased()
	{
		@SuppressWarnings("unchecked")
		List<String> colors = (List<String>)Resources.getResource("SYSTEM_COLOR256_PHRASES");
		if(colors == null)
		{
			colors = new LinkedList<String>();
			for(final Enumeration<Color256> c=CMLib.color().getColors256();c.hasMoreElements(); )
			{
				final Color256 C=c.nextElement();
				if(!colors.contains(C.getName1().toLowerCase()))
					colors.add(C.getName1());
				if(!colors.contains(C.getName2()))
					colors.add(C.getName2().toLowerCase());
			}
			
			Collections.sort(colors,new Comparator<String>()
			{
				@Override
				public int compare(final String o1, final String o2)
				{
					if(o1.length()==o2.length())
						return 0;
					if(o1.length()>o2.length())
						return -1;
					return 1;
				}
			});
			Resources.submitResource("SYSTEM_COLOR256_PHRASES", colors);
		}
		return colors;
	}

	public Map<String, Color256> getAllColors256NamesMap()
	{
		@SuppressWarnings("unchecked")
		Map<String, Color256> colors = (Map<String, Color256>)Resources.getResource("SYSTEM_COLOR256_NAME_MAP");
		if(colors == null)
		{
			colors = new TreeMap<String, Color256>();
			for(final Enumeration<Color256> c=CMLib.color().getColors256();c.hasMoreElements(); )
			{
				final Color256 C=c.nextElement();
				if(!colors.containsKey(C.getName1().toLowerCase()))
					colors.put(C.getName1().toLowerCase(), C);
				if(!colors.containsKey(C.getName2().toLowerCase()))
					colors.put(C.getName2().toLowerCase(), C);
			}
			Resources.submitResource("SYSTEM_COLOR256_NAME_MAP", colors);
		}
		return colors;
	}

	protected String removeRemainingColor(final String str)
	{
		final int end=str.indexOf("^?");
		if((end>0)&&(end<=str.length()-3))
		{
			final int start=str.substring(0,end).indexOf('^');
			if((start>=0)&&(start<(end-3)))
				return str.substring(0,start)+str.substring(end+3);
		}
		return str;
	}

	protected void removePaintJob(final Physical foundP)
	{
		if(foundP != null)
		{
			if(!restoreOldName(foundP))
			{
				final List<String> colors = this.getAllPaintingPhrases();
				String name=foundP.Name();
				String disp=foundP.displayText();
				for(final String color : colors)
				{
					int x=name.toLowerCase().indexOf(color);
					if(x>=0)
					{
						name = removeRemainingColor(name.substring(0, x).trim()+" "+name.substring(x+color.length()).trim());
						x=disp.toLowerCase().indexOf(color);
						if(x>=0)
							disp = removeRemainingColor(disp.substring(0, x).trim()+" "+disp.substring(x+color.length()).trim());
						break;
					}
				}
				if(name.toLowerCase().startsWith("a "))
					name=CMLib.english().startWithAorAn(name.substring(2));
				else
				if(name.toLowerCase().startsWith("an "))
					name=CMLib.english().startWithAorAn(name.substring(3));
				foundP.setName(name);
				foundP.setDisplayText(disp);
				foundP.setDescription(CMStrings.removeColors(foundP.description()));
			}
			foundP.text();
		}
	}

	protected String getRecipeFile()
	{
		return "paintingskill.txt";
	}

	protected String getColorCode(final String writing)
	{
		String colorCode="^";
		for(int i=0;i<writing.length();i++)
		{
			if((writing.charAt(i)=='^')
			&&(i<writing.length()-1)
			&&(writing.charAt(i+1)!='?'))
			{
				if((writing.charAt(i+1)==ColorLibrary.COLORCODE_FANSI256)
				&&(i<writing.length()-1))
				{
					colorCode=writing.substring(i+1, i+5);
					break;
				}
				else
				if((writing.charAt(i+1)!=ColorLibrary.COLORCODE_BACKGROUND)
				&&(writing.charAt(i+1)!=ColorLibrary.COLORCODE_BANSI256))
				{
					colorCode=""+writing.charAt(i+1);
					break;
				}
			}
		}
		return colorCode;
	}

	protected String getDescriptionColor(final Physical found, final String colorCode)
	{
		final StringBuffer desc=new StringBuffer(found.description());
		for(int x=0;x<(desc.length()-1);x++)
		{
			if((desc.charAt(x)=='^')
			&&(desc.charAt(x+1)!='?'))
			{
				if((desc.charAt(x+1)==ColorLibrary.COLORCODE_FANSI256)
				&&(x<desc.length()-4))
				{
					desc.delete(x+1, x+5);
					desc.insert(x+1, colorCode);
				}
				else
				if((desc.charAt(x+1)!=ColorLibrary.COLORCODE_BACKGROUND)
				&&(desc.charAt(x+1)!=ColorLibrary.COLORCODE_BANSI256))
				{
					desc.delete(x+1, x+2);
					desc.insert(x+1, colorCode);
				}
			}
		}
		final String d=desc.toString();
		if(!d.endsWith("^?"))
			desc.append("^?");
		if(!d.startsWith("^"+colorCode))
			desc.insert(0,"^"+colorCode);
		return desc.toString();
	}

	protected boolean restoreOldName(final Physical found)
	{
		final Ability A=found.fetchEffect("ExtraData");
		if((A != null)
		&&(A.isStat("PRE_PAINT_JOB_NAME"))
		&&(A.isStat("PRE_PAINT_JOB_DISPLAY")))
		{
			found.setName(A.getStat("PRE_PAINT_JOB_NAME"));
			found.setDisplayText(A.getStat("PRE_PAINT_JOB_DISPLAY"));
			if(found.isStat("PRE_PAINT_JOB_DESCRIPTION"))
				found.setDescription(A.getStat("PRE_PAINT_JOB_DESCRIPTION"));
			else
				found.setDescription(CMStrings.removeColors(found.description()));
			return true;
		}
		return false;
	}

	protected boolean saveOldName(final Physical found)
	{
		if(found.displayText().indexOf("^?")<0) //don't save the colored things
		{
			Ability A=found.fetchEffect("ExtraData");
			if(A==null)
			{
				A=CMClass.getAbility("ExtraData");
				found.addNonUninvokableEffect(A);
			}
			if(!A.isStat("PRE_PAINT_JOB_NAME"))
				A.setStat("PRE_PAINT_JOB_NAME", found.Name());
			if(!A.isStat("PRE_PAINT_JOB_DISPLAY"))
				A.setStat("PRE_PAINT_JOB_DISPLAY", found.displayText());
			//if(!A.isStat("PRE_PAINT_JOB_DESCRIPTION"))
			//	A.setStat("PRE_PAINT_JOB_DESCRIPTION", found.description());
			return true;
		}
		return false;
	}

	protected void addPaintJob(final Physical found, final String writing)
	{
		saveOldName(found);
		final String colorCode = getColorCode(writing);
		found.setDescription(getDescriptionColor(found, colorCode));
		found.setName(fixColor(found.Name(), writing));
		found.setDisplayText(fixColor(found.displayText(), writing));
		found.text();
	}
}
