package com.planet_ink.coffee_mud.Abilities.Archon;
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
   Copyright 2004-2024 Bo Zimmerman

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
public class Archon_Metacraft extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Metacraft";
	}

	private final static String localizedName = CMLib.lang().L("Metacrafting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "METACRAFT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	public static List<Ability> craftingSkills = new Vector<Ability>();

	protected String replacePercent(final String thisStr, final String withThis)
	{
		if(withThis.length()==0)
		{
			int x=thisStr.indexOf("% ");
			if(x>=0)
				return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf(" %");
			if(x>=0)
				return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf('%');
			if(x>=0)
				return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		else
		{
			final int x=thisStr.indexOf('%');
			if(x>=0)
				return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		return thisStr;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(craftingSkills.size()==0)
		{
			final ArrayList<Ability> V=new ArrayList<Ability>();
			for(final Enumeration<ItemCraftor> e=CMClass.craftorAbilities();e.hasMoreElements();)
				V.add((Ability)e.nextElement().copyOf());
			while(V.size()>0)
			{
				int lowest=Integer.MAX_VALUE;
				Ability lowestA=null;
				for(int i=0;i<V.size();i++)
				{
					final Ability A=V.get(i);
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
		}
		if(commands.size()<1)
		{
			mob.tell(L("Metacraft what ([recipe], everything, every [recipe], all [skill name]), (optionally) out of what material, and (optionally) to self, to here, or to file [FILENAME]?"));
			return false;
		}
		String mat=null;
		String toWHERE = "SELF";
		if(commands.size()>1)
		{
			for(int x=1;x<commands.size()-1;x++)
			{
				if(commands.get(x).equalsIgnoreCase("to"))
				{
					if(commands.get(x+1).equalsIgnoreCase("self"))
					{
						toWHERE="SELF";
						commands.remove(x);
						commands.remove(x);
						break;
					}
					if(commands.get(x+1).equalsIgnoreCase("here"))
					{
						toWHERE="HERE";
						commands.remove(x);
						commands.remove(x);
						break;
					}
					if(commands.get(x+1).equalsIgnoreCase("file")&&(x<commands.size()-2))
					{
						toWHERE=commands.get(x+2);
						commands.remove(x);
						commands.remove(x);
						commands.remove(x);
						break;
					}
				}
			}
			if((commands.size()>2)
			||(commands.size()>1)&&(RawMaterial.CODES.FIND_IgnoreCase(commands.get(commands.size()-1))>0))
			{
				mat=(commands.get(commands.size()-1)).toUpperCase();
				commands.remove(commands.size()-1);
			}
		}
		int material=-1;
		List<Ability> craftingSkills=Archon_Metacraft.craftingSkills;
		if(mat!=null)
			material=RawMaterial.CODES.FIND_StartsWith(mat);
		if((mat!=null)&&(material<0))
		{
			mob.tell(L("'@x1' is not a recognized material.",mat));
			return false;
		}
		else
		if(material>0)
		{
			craftingSkills = new ArrayList<Ability>();
			for(final Ability A : Archon_Metacraft.craftingSkills)
			{
				final ItemCraftor craft = (ItemCraftor)A;
				if(craft.myResources().contains(Integer.valueOf(material)))
					craftingSkills.add(A);
			}
			if(craftingSkills.size()==0)
				craftingSkills=Archon_Metacraft.craftingSkills;
		}

		ItemCraftor skill=null;
		List<Pair<Ability,String>> skillsToUse=new ArrayList<Pair<Ability,String>>();
		if(commands.size()>1)
		{

		}
		String recipe=CMParms.combine(commands,0);
		boolean everyFlag=false;
		if(recipe.equalsIgnoreCase("everything"))
		{
			skillsToUse=new XVector<Pair<Ability,String>>();
			for(final Ability A : craftingSkills)
				skillsToUse.add(new Pair<Ability,String>(A,"*"));
			everyFlag=true;
			recipe=null;
		}
		else
		if(recipe.toUpperCase().startsWith("EVERY "))
		{
			everyFlag=true;
			recipe=recipe.substring(6).trim();
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.get(i);
				final List<String> V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0))
				{
					for(final String s2 : V)
						skillsToUse.add(new Pair<Ability,String>(skill,replacePercent(s2,"")));
				}
			}
			if(skillsToUse.size()==0)
			{
				for(int i=0;i<craftingSkills.size();i++)
				{
					skill=(ItemCraftor)craftingSkills.get(i);
					final List<String> V=skill.matchingRecipeNames(recipe,true);
					if((V!=null)&&(V.size()>0))
					{
						for(final String s2 : V)
							skillsToUse.add(new Pair<Ability,String>(skill,replacePercent(s2,"")));
					}
				}
			}
		}
		else
		if(recipe.toUpperCase().startsWith("ALL "))
		{
			everyFlag=true;
			final String skillName=recipe.toUpperCase().substring(4);
			skill = (ItemCraftor)CMLib.english().fetchEnvironmental(craftingSkills, skillName, true);
			if(skill == null)
				skill = (ItemCraftor)CMLib.english().fetchEnvironmental(craftingSkills, skillName, false);
			if(skill == null)
			{
				mob.tell(L("'@x1' is not a known crafting skill.",skillName));
				return false;
			}
			skillsToUse=new XVector<Pair<Ability,String>>();
			skillsToUse.add(new Pair<Ability,String>(skill,"*"));
			recipe=null;
		}
		else
		{
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.get(i);
				final List<String> V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0))
					skillsToUse.add(new Pair<Ability,String>(skill,this.replacePercent(V.get(0), "")));
			}
			if(skillsToUse.size()==0)
			{
				for(int i=0;i<craftingSkills.size();i++)
				{
					skill=(ItemCraftor)craftingSkills.get(i);
					final List<String> V=skill.matchingRecipeNames(recipe,true);
					if((V!=null)&&(V.size()>0))
						skillsToUse.add(new Pair<Ability,String>(skill,this.replacePercent(V.get(0), "")));
				}
			}
		}
		if(skillsToUse.size()==0)
		{
			mob.tell(L("'@x1' can not be made with any of the known crafting skills.",recipe));
			return false;
		}

		boolean success=false;
		final StringBuffer xml = new StringBuffer("<ITEMS>");
		final HashSet<String> files = new HashSet<String>();
		for(int s=0;s<skillsToUse.size();s++)
		{
			skill=(ItemCraftor)skillsToUse.get(s).first;
			final String recipeName = skillsToUse.get(s).second;
			final List<Item> items=new ArrayList<Item>();
			if(everyFlag)
			{
				if(recipe==null)
				{
					List<ItemCraftor.CraftedItem> V=null;
					if(material>=0)
						V=skill.craftAllItemSets(material,false);
					else
						V=skill.craftAllItemSets(false);

					if(V!=null)
					{
						for(final ItemCraftor.CraftedItem L: V)
						{
							items.add(L.item);
							if(L.key!=null)
								items.add(L.key);
						}
					}
				}
				else
				if(material>=0)
				{
					final ItemCraftor.CraftedItem pair = skill.craftItem(recipeName,material,false, true);
					if(pair!=null)
						items.addAll(pair.asList());
				}
				else
				{
					final ItemCraftor.CraftedItem pair = skill.craftItem(recipeName,-1,false,true);
					if(pair!=null)
						items.addAll(pair.asList());
				}
			}
			else
			if(material>=0)
			{
				final ItemCraftor.CraftedItem pair = skill.craftItem(recipeName,material,false, true);
				if(pair!=null)
					items.addAll(pair.asList());
			}
			else
			{
				final ItemCraftor.CraftedItem pair = skill.craftItem(recipeName,-1,false,true);
				if(pair!=null)
					items.addAll(pair.asList());
			}
			if(items.size()==0)
				continue;
			success=true;
			if(toWHERE.equals("SELF")||toWHERE.equals("HERE"))
			{
				for(final Item building : items)
				{
					if(building instanceof ClanItem)
					{
						final Pair<Clan,Integer> p=CMLib.clans().findPrivilegedClan(mob, Clan.Function.ENCHANT);
						if(p!=null)
						{
							final Clan C=p.first;
							final String clanName=(" "+C.getGovernmentName()+" "+C.name());
							building.setName(CMStrings.replaceFirst(building.Name(), " Clan None", clanName));
							building.setDisplayText(CMStrings.replaceFirst(building.displayText(), " Clan None", clanName));
							building.setDescription(CMStrings.replaceFirst(building.description(), " Clan None", clanName));
							((ClanItem)building).setClanID(C.clanID());
						}
					}
					if(toWHERE.equals("HERE"))
					{
						mob.location().addItem(building,ItemPossessor.Expire.Player_Drop);
						mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,L("@x1 appears here.",building.name()));
					}
					else
					{
						mob.moveItemTo(building);
						mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,L("@x1 appears in <S-YOUPOSS> hands.",building.name()));
					}
				}
			}
			else
				xml.append(CMLib.coffeeMaker().getUniqueItemsXML(items,new Hashtable<String,List<Item>>(),files,null));
			mob.location().recoverPhyStats();
			if(!everyFlag)
				break;
		}
		if(success
		&&(!toWHERE.equals("SELF"))
		&&(!toWHERE.equals("HERE")))
		{
			final CMFile file = new CMFile(toWHERE,mob);
			if(!file.canWrite())
				mob.tell(L("Unable to open file '@x1' for writing.",toWHERE));
			else
			{
				xml.append("</ITEMS>");
				if(files.size()>0)
				{
					final StringBuffer str=new StringBuffer("<FILES>");
					for(final Iterator<String> i=files.iterator();i.hasNext();)
					{
						final String filename=i.next();
						final StringBuffer buf=new CMFile(Resources.makeFileResourceName(filename),null,CMFile.FLAG_LOGERRORS).text();
						if((buf!=null)&&(buf.length()>0))
						{
							str.append("<FILE NAME=\""+filename+"\">");
							str.append(buf);
							str.append("</FILE>");
						}
					}
					str.append("</FILES>");
					xml.append(str);
				}
				file.saveText(xml);
				mob.tell(L("File '@x1' written.",file.getAbsolutePath()));
			}
		}
		if(!success)
		{
			mob.tell(L("The metacraft failed."));
			return false;
		}
		return true;
	}
}
