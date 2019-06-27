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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2019 Bo Zimmerman

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

	protected String fixColor(final String name, final String colorChar, final String colorWord)
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

	public List<String> getAllColorPhrases()
	{
		@SuppressWarnings("unchecked")
		List<String> colors = (List<String>)Resources.getResource("SYSTEM_COLOR256_PHRASES");
		if(colors == null)
		{
			colors = new LinkedList<String>();
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof PaintingSkill)
				{
					final PaintingSkill P=(PaintingSkill)A;
					if(!P.ID().equals("PaintingSkill"))
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
			Resources.submitResource("SYSTEM_COLOR256_PHRASES", colors);
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

	protected void removePaintJob(final Physical P)
	{
		if(P != null)
		{
			final Ability A=P.fetchEffect("UnderThePaintJob");
			if(A != null)
			{
				final XMLLibrary xml=CMLib.xml();
				final List<XMLLibrary.XMLTag> tags=xml.parseAllXML(A.text());
				if(xml.isTagInPieces(tags, "NAME"))
					P.setName(xml.restoreAngleBrackets(xml.getValFromPieces(tags, "NAME")));
				if(xml.isTagInPieces(tags, "DISPLAY"))
					P.setDisplayText(xml.restoreAngleBrackets(xml.getValFromPieces(tags, "DISPLAY")));
				if(xml.isTagInPieces(tags, "DESCRIPTION"))
					P.setDescription(xml.restoreAngleBrackets(xml.getValFromPieces(tags, "DESCRIPTION")));
				P.delEffect(A);
			}
			final List<String> colors = this.getAllColorPhrases();
			String name=found.Name();
			String disp=found.displayText();
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
			found.setName(name);
			found.setDisplayText(disp);
			found.setDescription(CMStrings.removeColors(found.description()));
			found.text();
		}
	}

	protected String getRecipeFile()
	{
		return "paintingskill.txt";
	}

	protected void addPaintJob(final Physical found, final String writing)
	{
		/*
		if(P.fetchEffect("UnderThePaintJob") == null)
		{
			final ThinAbility A=new ThinAbility()
			{
				@Override
				public String ID()
				{
					return "UnderThePaintJob";
				}
			};
			final XMLLibrary xml=CMLib.xml();
			A.setMiscText(xml.convertXMLtoTag("NAME", xml.parseOutAngleBrackets(P.Name()))+
							xml.convertXMLtoTag("DISPLAY", xml.parseOutAngleBrackets(P.displayText()))+
							xml.convertXMLtoTag("DESCRIPTION", xml.parseOutAngleBrackets(P.description())));
			P.addNonUninvokableEffect(A);
		}
		*/
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
		found.setDescription(desc.toString());
		found.setName(fixColor(found.Name(), colorCode, writing));
		found.setDisplayText(fixColor(found.displayText(), colorCode, writing));
		found.text();
	}

}
