package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class EnhancedCraftingSkill extends CraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "EnhancedCraftingSkill";
	}

	private final static String localizedName = CMLib.lang().L("Enhanced Crafting Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Hashtable<String, String> parametersFields()
	{
		return new Hashtable<String, String>();
	}

	@Override
	public String parametersFormat()
	{
		return "";
	}

	protected int materialAdjustments = 0;

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	protected int[][] fetchFoundResourceData(MOB mob,
											 int req1Required,
											 String req1Desc, int[] req1,
											 int req2Required,
											 String req2Desc, int[] req2,
											 boolean bundle,
											 int autoGeneration,
											 PairVector<EnhancedExpertise,Integer> expMods)
	{
		if(expMods!=null)
		{
			for(int t=0;t<expMods.size();t++)
			{
				final EnhancedExpertise type=expMods.elementAt(t).first;
				final int stage=expMods.elementAt(t).second.intValue();
				switch(type)
				{
				case LITECRAFT:
					switch(stage)
					{
					case 0:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,-0.1);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,-0.1);
						break;
					case 1:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,-0.25);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,-0.25);
						break;
					case 2:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,-0.5);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,-0.5);
						break;
					}
					break;
				case DURACRAFT:
					switch(stage)
					{
					case 0:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,0.1);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,0.1);
						break;
					case 1:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,0.2);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,0.2);
						break;
					case 2:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,0.25);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,0.25);
						break;
					}
					break;
				case QUALCRAFT:
					switch(stage)
					{
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
					}
					break;
				case LTHLCRAFT:
					switch(stage)
					{
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
					}
					break;
				case CNTRCRAFT:
					switch(stage)
					{
					case 0:
						break;
					case 1:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,0.05);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,0.05);
						break;
					case 2:
						if(req1Required>0)
							req1Required=atLeast1(req1Required,0.10);
						if(req2Required>0)
							req2Required=atLeast1(req2Required,0.10);
						break;
					}
					break;
				}
			}
		}
		return super.fetchFoundResourceData(mob,
				req1Required,req1Desc,req1,
				req2Required,req2Desc,req2,
				bundle,autoGeneration,expMods);
	}

	public void fixDataForComponents(int[][] data, String woodRequiredStr, boolean autoGeneration, List<Object> componentsFoundList)
	{
		boolean emptyComponents=false;
		if((componentsFoundList==null)||(componentsFoundList.size()==0))
		{
			emptyComponents=true;
			if(componentsFoundList==null)
				componentsFoundList=new ArrayList<Object>();
			final List<AbilityComponent> componentsRequirements=getNonStandardComponentRequirements(woodRequiredStr);
			if(componentsRequirements!=null)
			{
				final List<Item> components=CMLib.ableComponents().componentsSample(componentsRequirements, true);
				if(components != null)
					componentsFoundList.addAll(components);
			}
		}

		if(autoGeneration)
		{
			List<Integer> compInts=new ArrayList<Integer>();
			for(final Object o : componentsFoundList)
			{
				if(o instanceof Item)
				{
					final Item I=(Item)o;
					compInts.add(Integer.valueOf(I.material()));
					data[0][FOUND_AMT] += I.phyStats().weight();
				}
			}
			if(compInts.size()>0)
			{
				Collections.sort(compInts);
				data[0][FOUND_CODE]=compInts.get((int)Math.round(Math.floor(compInts.size()/2))).intValue();
			}
		}
		else
		if(((data[0][FOUND_CODE]==0)&&(data[1][FOUND_CODE]==0)))
		{
			final List<Integer> rscs=myResources();
			for(final Object o : componentsFoundList)
			{
				if(o instanceof Item)
				{
					final Item I=(Item)o;
					if(rscs.contains(Integer.valueOf(I.material())))
					{
						if(data[0][FOUND_CODE]==0)
							data[0][FOUND_CODE]=I.material();
						data[0][FOUND_AMT] += I.phyStats().weight();
					}
				}
			}
			if(data[0][FOUND_CODE]==0)
			{
				List<Integer> compInts=new ArrayList<Integer>();
				for(final Object o : componentsFoundList)
				{
					if(o instanceof Item)
					{
						final Item I=(Item)o;
						compInts.add(Integer.valueOf(I.material()));
						data[0][FOUND_AMT] += I.phyStats().weight();
					}
				}
				if(compInts.size()>0)
				{
					Collections.sort(compInts);
					data[0][FOUND_CODE]=compInts.get((int)Math.round(Math.floor(compInts.size()/2))).intValue();
				}
			}
		}
		if(emptyComponents)
		{
			for(final Object o : componentsFoundList)
			{
				if(o instanceof Item)
				{
					final Item I=(Item)o;
					I.destroy();
				}
			}
			componentsFoundList.clear();
		}
	}

	private final static int atLeast1(int value, double pct)
	{
		int change=(int)Math.round(CMath.mul(value,pct));
		if(pct<0.0)
		{
			if((change==0)&&(value>1))
				change-=1;
			value+=change;
		}
		else
		{
			if(change==0)
				change+=1;
			value+=change;
		}
		return value;
	}

	protected EnhancedExpertise getLocalExpCode(String exp)
	{
		if(exp==null)
			return null;
		exp=exp.toUpperCase();
		for(final EnhancedExpertise key : EnhancedExpertise.values())
		{
			if(exp.startsWith(key.stageKey))
				return key;
		}
		return null;
	}

	protected String applyName(String name, String word)
	{
		final Vector<String> V=CMParms.parse(name);
		int insertHere=0;
		if((V.size()>0)
		&&(CMLib.english().isAnArticle(V.firstElement())))
			insertHere++;
		V.insertElementAt(word.toLowerCase(),insertHere);
		if((insertHere>0)
		&&((V.firstElement().equalsIgnoreCase("A"))
			||(V.firstElement().equalsIgnoreCase("AN"))))
		{
			V.removeElementAt(0);
			return CMLib.english().startWithAorAn(CMParms.combineQuoted(V,0));
		}
		return CMParms.combineQuoted(V,0);
	}

	protected void applyName(Item item, String word)
	{
		final String oldName=item.Name();
		item.setName(applyName(item.Name(),word));
		item.setDisplayText(applyName(item.displayText(),word));
		item.setDescription(applyName(item.description(),word));
		verb=CMStrings.replaceAll(verb,oldName,item.Name());
		displayText=CMStrings.replaceAll(displayText,oldName,item.Name());
		//startStr=CMStrings.replaceAll(startStr,oldName,item.Name());
	}

	public List<String> getThisSkillsExpertises()
	{
		final List<String> V=new Vector<String>();
		for(EnhancedExpertise expertise : EnhancedExpertise.values())
		{
			final String s=CMLib.expertises().getApplicableExpertise(ID(),expertise.flag);
			if(s!=null)
				V.add(s);
		}
		return V;
	}

	@Override
	protected List<List<String>> loadList(StringBuffer str)
	{
		final List<List<String>> lists=super.loadList(str);
		final List<String> parmNames=CMParms.parseTabs(parametersFormat(), true);
		int levelParmPos=-1;
		for(int p=0;p<parmNames.size();p++)
		{
			if(parmNames.get(p).endsWith("_LEVEL"))
			{
				levelParmPos=p;
				break;
			}
		}
		if(levelParmPos<0)
			return lists;
		final List<List<String>> sortedLists=new Vector<List<String>>();
		while(lists.size()>0)
		{
			int lowestLevelRecipeIndex=-1;
			int lowestLevel=Integer.MAX_VALUE;
			for(int index=0;index<lists.size();index++)
			{
				if((lists.get(index).size()>levelParmPos)&&(CMath.s_int(lists.get(index).get(levelParmPos))<lowestLevel))
				{
					lowestLevelRecipeIndex=index;
					lowestLevel=CMath.s_int(lists.get(index).get(levelParmPos));
				}
			}
			if(lowestLevelRecipeIndex<0)
				sortedLists.add(lists.remove(0));
			else
				sortedLists.add(lists.remove(lowestLevelRecipeIndex));
		}
		return sortedLists;
	}

	public void enhanceList(MOB mob)
	{
		final StringBuffer extras=new StringBuffer("");
		String stage=null;
		String key=null;
		final List<String> types=getThisSkillsExpertises();
		for(int t=0;t<types.size();t++)
		{
			key=types.get(t);
			final int stages=CMLib.expertises().getStages(key);
			final EnhancedExpertise code=getLocalExpCode(key);
			if(code != null)
			{
				final Pair<String,Integer> X=mob.fetchExpertise(key);
				for(int s=stages-1;s>=0;s--)
				{
					if((X!=null)&&(X.getValue().intValue()>=(s+1)))
					{
						stage=CMath.convertToRoman(s+1);
						ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(key+stage);
						if(def==null)
							def=CMLib.expertises().getDefinition(key+(s+1));
						if(def==null)
							def=CMLib.expertises().getDefinition(key);
						if(def!=null)
							extras.append(def.getData()[s]+", ");
					}
				}
			}
		}
		if(extras.length()>0)
			commonTell(mob,L("You can use your expertises to enhance this skill by prepending one or more of the following words to the name of the item you wish to craft: @x1.",extras.substring(0,extras.length()-2)));
	}

	public List<ExpertiseLibrary.ExpertiseDefinition> getAllThisSkillsDefinitions()
	{
		final List<ExpertiseLibrary.ExpertiseDefinition> defs = new ArrayList<ExpertiseLibrary.ExpertiseDefinition>();
		final List<String> experTypes=getThisSkillsExpertises();
		String key=null;
		String stage;
		for(int t=0;t<experTypes.size();t++)
		{
			key=experTypes.get(t);
			final int stages=CMLib.expertises().getStages(key);
			final EnhancedExpertise code=getLocalExpCode(key);
			if(code != null)
			{
				for(int s=stages-1;s>=0;s--)
				{
					stage=CMath.convertToRoman(s+1);
					ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(key+stage);
					if(def != null)
						defs.add(def);
					else
					{
						def = CMLib.expertises().getDefinition(key+(s+1));
						if(def != null)
							defs.add(def);
					}
				}
			}
		}
		return defs;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	public PairVector<EnhancedExpertise,Integer> enhancedTypes(MOB mob, List<String> commands)
	{
		String cmd=null;
		PairVector<EnhancedExpertise,Integer> types=null;
		materialAdjustments=0;
		if((commands!=null)&&(commands.size()>0))
		{
			cmd=commands.get(0);
			if((!cmd.equalsIgnoreCase("list"))
			&&(!cmd.equalsIgnoreCase("mend"))
			&&(!cmd.equalsIgnoreCase("scan")))
			{
				boolean foundSomething=true;
				String stage=null;
				final List<String> experTypes=getThisSkillsExpertises();
				while(foundSomething)
				{
					foundSomething=false;
					String key=null;
					for(int t=0;t<experTypes.size();t++)
					{
						key=experTypes.get(t);
						final int stages=CMLib.expertises().getStages(key);
						final EnhancedExpertise code=getLocalExpCode(key);
						if(code != null)
						{
							final Pair<String,Integer> X=mob.fetchExpertise(key);
							for(int s=stages-1;s>=0;s--)
							{
								if((X==null)||(X.getValue().intValue()<(s+1)))
									continue;
								stage=CMath.convertToRoman(s+1);
								ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(key+stage);
								if(def==null)
									def=CMLib.expertises().getDefinition(key+(s+1));
								if(def==null)
									def=CMLib.expertises().getDefinition(key);
								if(def!=null)
								{
									if(cmd.equalsIgnoreCase(def.getData()[s]))
									{
										commands.remove(0);
										if(types==null)
											types=new PairVector<EnhancedExpertise,Integer>();
										if(!types.containsFirst(code))
										{
											types.addElement(code,Integer.valueOf(s));
											if(commands.size()>0)
												cmd=commands.get(0);
											else
												cmd="";
											foundSomething=true;
											break; // you can do any from a stage, but only 1 per stage!
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return types;
	}

	public void addStatAdjustment(Item item, String stat, String adjustment)
	{
		stat=stat.toUpperCase().trim();
		Ability WA=item.fetchEffect("Prop_WearAdjuster");
		if(WA==null)
		{
			WA=CMClass.getAbility("Prop_WearAdjuster");
			item.addNonUninvokableEffect(WA);
		}
		else
		if((CMLib.english().containsString(WA.text().toUpperCase(),stat+"+"))
		||(CMLib.english().containsString(WA.text().toUpperCase(),stat+"-")))
			return;
		if(WA!=null)
			WA.setMiscText((WA.text()+" "+stat+adjustment).trim());
	}

	public void addSpellAdjustment(Item item, String spell, String parm)
	{
		Ability WA=item.fetchEffect("Prop_WearSpellCast");
		if(WA==null)
		{
			WA=CMClass.getAbility("Prop_WearSpellCast");
			item.addNonUninvokableEffect(WA);
		}
		else
		if(CMLib.english().containsString(WA.text().toUpperCase(),spell))
			return;
		if(WA!=null)
		{
			if(WA.text().length()>0)
				WA.setMiscText(WA.text()+";"+spell+"("+parm+")");
			else
				WA.setMiscText(spell+"("+parm+")");
		}
	}

	@Override
	protected String cleanBuildingNameForXP(MOB mob, String name)
	{
		name=" "+CMLib.english().cleanArticles(name)+" ";
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,CMParms.parse(name));
		if(enhancedTypes != null)
		{
			for(int t=0;t<enhancedTypes.size();t++)
			{
				final EnhancedExpertise type=enhancedTypes.elementAt(t).first;
				final int stage=enhancedTypes.elementAt(t).second.intValue();
				final String expertiseID=CMLib.expertises().getApplicableExpertise(ID(),type.flag);
				ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(expertiseID+CMath.convertToRoman(1));
				if(def==null)
					def = CMLib.expertises().getDefinition(expertiseID+1);
				if(def==null)
					def = CMLib.expertises().getDefinition(expertiseID);
				if(def==null)
					continue;
				name=CMStrings.replaceAll(name," "+def.getData()[stage].toUpperCase()+" ","");
			}
		}
		return name.trim();
	}

	public void enhanceItem(MOB mob, Item item, PairVector<EnhancedExpertise,Integer> types)
	{
		if(types==null)
			return;
		final EnhancedCraftingSkill affect=(EnhancedCraftingSkill)mob.fetchEffect(ID());
		if((affect!=null)
		&&(!affect.aborted)
		&&(activity == CraftingActivity.CRAFTING)
		&&(item!=null))
		{
			for(int t=0;t<types.size();t++)
			{
				final EnhancedExpertise type=types.elementAt(t).first;
				final int stage=types.elementAt(t).second.intValue();
				final String expertiseID=CMLib.expertises().getApplicableExpertise(ID(),type.flag);
				ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(expertiseID+CMath.convertToRoman(1));
				if(def==null)
					def = CMLib.expertises().getDefinition(expertiseID+1);
				if(def==null)
					def = CMLib.expertises().getDefinition(expertiseID);
				if(def==null)
					continue;
				switch(type)
				{
				case LITECRAFT:
				{
					switch(stage)
					{
					case 0:
						applyName(item,def.getData()[stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.1));
						break;
					case 1:
						applyName(item,def.getData()[stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.2));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getData()[stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.3));
						//addStatAdjustment(item,"DEX","+1");
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					}
					break;
				}
				case DURACRAFT:
				{
					if((!(item instanceof Armor))||(item.basePhyStats().armor()==0))
						commonTell(mob,L("@x1 only applies to protective armor.",def.getData()[stage]));
					else
					switch(stage)
					{
					case 0:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setArmor(item.basePhyStats().armor()+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.1));
						break;
					case 1:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setArmor(atLeast1(item.basePhyStats().armor(),0.1)+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.2));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setArmor(atLeast1(item.basePhyStats().armor(),0.25)+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.3));
						//addStatAdjustment(item,"CON","+1");
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					}
					break;
				}
				case QUALCRAFT:
				{
					switch(stage)
					{
					case 0:
						applyName(item,def.getData()[stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.5));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 1:
						applyName(item,def.getData()[stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),1.5));
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getData()[stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),2.5));
						if((item instanceof Armor)
						&&(!CMath.bset(((Armor)item).getLayerAttributes(),Armor.LAYERMASK_MULTIWEAR)))
							addSpellAdjustment(item,"Spell_WellDressed","1");
						affect.bumpTickDown(Math.round(0.75 * affect.tickDown));
						break;
					}
					break;
				}
				case LTHLCRAFT:
				{
					if(!(item instanceof Weapon))
						commonTell(mob,L("@x1 only applies to weapons.",def.getData()[stage]));
					else
					switch(stage)
					{
					case 0:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setDamage(atLeast1(item.basePhyStats().damage(),0.05));
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.1));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 1:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setDamage(atLeast1(item.basePhyStats().damage(),0.1));
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.2));
						item.basePhyStats().setWeight(atLeast1(item.basePhyStats().weight(),0.1));
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setDamage(atLeast1(item.basePhyStats().damage(),0.15)+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.3));
						item.basePhyStats().setWeight(atLeast1(item.basePhyStats().weight(),0.1));
						affect.bumpTickDown(Math.round(0.75 * affect.tickDown));
						break;
					}
					break;
				}
				case CNTRCRAFT:
				{
					if(!(item instanceof Weapon))
						commonTell(mob,L("@x1 only applies to weapons.",def.getData()[stage]));
					else
					switch(stage)
					{
					case 0:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setAttackAdjustment(item.basePhyStats().attackAdjustment()+3);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.1));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 1:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setAttackAdjustment(item.basePhyStats().attackAdjustment()+6);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.2));
						item.basePhyStats().setWeight(atLeast1(item.basePhyStats().weight(),0.05));
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getData()[stage]);
						item.basePhyStats().setAttackAdjustment(item.basePhyStats().attackAdjustment()+9);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.3));
						item.basePhyStats().setWeight(atLeast1(item.basePhyStats().weight(),0.1));
						affect.bumpTickDown(Math.round(1.25 * affect.tickDown));
						break;
					}
					break;
				}
				}
			}
			item.recoverPhyStats();
		}
	}
}
