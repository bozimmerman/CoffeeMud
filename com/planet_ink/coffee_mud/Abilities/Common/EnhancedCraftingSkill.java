package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
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
   Copyright 2006-2025 Bo Zimmerman

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
	public String getRecipeFormat()
	{
		return "";
	}

	protected final static int HIDE_MASK=1<<30;
	protected final static int STAGE_MASK=~(1<<30);

	protected int materialAdjustments = 0;

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.General;
	}

	@Override
	protected boolean supportsArmors()
	{
		return getRecipeFormat().indexOf("CODED_WEAR_LOCATION")>=0;
	}

	@Override
	protected boolean supportsWeapons()
	{
		return getRecipeFormat().indexOf("WEAPON_CLASS")>=0;
	}

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
	}

	@Override
	protected int[][] fetchFoundResourceData(final MOB mob,
											 int req1Required,
											 final String req1Desc, final int[] req1,
											 int req2Required,
											 final String req2Desc, final int[] req2,
											 final boolean bundle,
											 final int autoGeneration,
											 final PairVector<EnhancedExpertise,Integer> expMods)
	{
		if(expMods!=null)
		{
			for(int t=0;t<expMods.size();t++)
			{
				final EnhancedExpertise type=expMods.elementAt(t).first;
				final int stage=expMods.elementAt(t).second.intValue() & STAGE_MASK;
				switch(type)
				{
				case LITECRAFT:
					switch(stage)
					{
					case 0:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,-0.1);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,-0.1);
						break;
					case 1:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,-0.25);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,-0.25);
						break;
					case 2:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,-0.5);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,-0.5);
						break;
					}
					break;
				case DURACRAFT:
					switch(stage)
					{
					case 0:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,0.1);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,0.1);
						break;
					case 1:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,0.2);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,0.2);
						break;
					case 2:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,0.25);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,0.25);
						break;
					}
					break;
				case RUSHCRAFT:
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
							req1Required=multiplyMinResult1(req1Required,0.05);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,0.05);
						break;
					case 2:
						if(req1Required>0)
							req1Required=multiplyMinResult1(req1Required,0.10);
						if(req2Required>0)
							req2Required=multiplyMinResult1(req2Required,0.10);
						break;
					}
					break;
				case FORTCRAFT:
				case VIGOCRAFT:
				case IMBUCRAFT:
					break;
				case ADVNCRAFT:
					break;
				default:
					break;
				}
			}
		}
		return super.fetchFoundResourceData(mob,
				req1Required,req1Desc,req1,
				req2Required,req2Desc,req2,
				bundle,autoGeneration,expMods);
	}

	@Override
	public boolean checkInfo(final MOB mob, final List<String> commands)
	{
		final List<String> infoCmds = new ArrayList<String>(commands.size());
		infoCmds.addAll(commands);
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,infoCmds);
		return checkInfo(mob, infoCmds, enhancedTypes);
	}

	@Override
	public void fixInfoItem(final MOB mob, final Item I, final int lvl, final PairVector<EnhancedExpertise,Integer> enhancedTypes)
	{
		final EnhancedCraftingSkill affect=(EnhancedCraftingSkill)mob.fetchEffect(ID());
		Ability delEffectA=null;
		try
		{
			if(affect==null)
			{
				delEffectA=this;
				mob.addEffect(delEffectA);
			}
			enhanceItem(mob,I,lvl,enhancedTypes);
		}
		finally
		{
			if(delEffectA != null)
				mob.delEffect(delEffectA);
		}
	}


	private final static int multiplyMinResult1(int value, final double pct)
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

	protected String applyName(final String name, final String word)
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

	protected void applyName(final Item item, final String word, final boolean hide)
	{
		final String oldName=item.Name();
		if(!hide)
		{
			item.setName(applyName(item.Name(),word));
			item.setDisplayText(applyName(item.displayText(),word));
		}
		item.setDescription(applyName(item.description(),word));
		verb=CMStrings.replaceAll(verb,oldName,item.Name());
		displayText=CMStrings.replaceAll(displayText,oldName,item.Name());
		//startStr=CMStrings.replaceAll(startStr,oldName,item.Name());
	}

	public List<String> getThisSkillsExpertises()
	{
		final List<String> V=new ArrayList<String>(3);
		for(final EnhancedExpertise expertise : EnhancedExpertise.values())
		{
			final String[] ss=CMLib.expertises().getApplicableExpertises(ID(),expertise.flag);
			if(ss!=null)
			{
				for(final String sid : ss)
				{
					if(!V.contains(sid))
						V.add(sid);
				}
			}
		}
		return V;
	}

	@Override
	protected List<List<String>> loadList(final StringBuffer str)
	{
		final List<List<String>> lists=super.loadList(str);
		final List<String> parmNames=CMParms.parseTabs(getRecipeFormat(), true);
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

	public void enhanceList(final MOB mob)
	{
		final StringBuffer extras=new StringBuffer("");
		String stage=null;
		String key=null;
		final List<String> types=getThisSkillsExpertises();
		final Set<String> found = new TreeSet<String>();
		for(int t=0;t<types.size();t++)
		{
			key=types.get(t);
			final int stages=CMLib.expertises().numStages(key);
			final EnhancedExpertise code=getLocalExpCode(key);
			if(code != null)
			{
				Pair<String,Integer> mobExp=mob.fetchExpertise(key);
				if((mobExp == null)&&(code==EnhancedExpertise.RUSHCRAFT))
					mobExp=new Pair<String,Integer>(key,Integer.valueOf(CMLib.expertises().getStageCodes(key).size()));
				for(int s=stages-1;s>=0;s--)
				{
					if((mobExp!=null)&&(mobExp.getValue().intValue()>=(s+1)))
					{
						stage=CMath.convertToRoman(s+1);
						ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(key+stage);
						if(def==null)
							def=CMLib.expertises().getDefinition(key+(s+1));
						if(def==null)
							def=CMLib.expertises().getDefinition(key);
						if((def!=null)&&(!found.contains(def.getStageNames()[s])))
						{
							found.add(def.getStageNames()[s]);
							extras.append(def.getStageNames()[s]+", ");
						}
					}
				}
			}
		}
		if(extras.length()>0)
		{
			commonTelL(mob,  "You can use your expertises to enhance this skill by prepending one or more "
							+ "of the following words to the name of the item you wish to craft"
							+ ": @x1.",extras.substring(0,extras.length()-2));
			commonTelL(mob,"Put the word 'hide' before any enhancement you want hidden from the name.");
		}
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
			final int stages=CMLib.expertises().numStages(key);
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

	public PairVector<EnhancedExpertise,Integer> enhancedTypes(final MOB mob, final List<String> commands)
	{
		String cmd=null;
		PairVector<EnhancedExpertise,Integer> types=null;
		materialAdjustments=0;
		if((commands!=null)
		&&(commands.size()>0))
		{
			int cmdDex=0;
			cmd=commands.get(cmdDex);
			boolean hideNext = false;
			if((cmd.equalsIgnoreCase("hide"))
			&&(commands.size()>1))
			{
				hideNext = true;
				cmd=commands.get(1);
			}
			if((cmd.equalsIgnoreCase("info"))
			&&(commands.size()>1))
			{
				cmdDex++;
				cmd=commands.get(cmdDex);
			}
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
						final int stages=CMLib.expertises().numStages(key);
						final EnhancedExpertise code=getLocalExpCode(key);
						if(code != null)
						{
							Pair<String, Integer> mobExp=mob.fetchExpertise(key);
							if((mobExp == null)&&(code==EnhancedExpertise.RUSHCRAFT))
								mobExp=new Pair<String,Integer>(key,Integer.valueOf(CMLib.expertises().getStageCodes(key).size()));
							for(int s=stages-1;s>=0;s--)
							{
								if((mobExp==null)||(mobExp.getValue().intValue()<(s+1)))
									continue;
								stage=CMath.convertToRoman(s+1);
								ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(key+stage);
								if(def==null)
									def=CMLib.expertises().getDefinition(key+(s+1));
								if(def==null)
									def=CMLib.expertises().getDefinition(key);
								if(def!=null)
								{
									if(cmd.equalsIgnoreCase(def.getStageNames()[s]))
									{
										commands.remove(cmdDex);
										if(hideNext)
											commands.remove(cmdDex);
										if(types==null)
											types=new PairVector<EnhancedExpertise,Integer>();
										if(!types.containsFirst(code))
										{
											types.addElement(code,Integer.valueOf(s | (hideNext?HIDE_MASK:0)));
											if(commands.size()>0)
											{
												hideNext=false;
												cmdDex=0;
												cmd=commands.get(0);
												if(cmd.equalsIgnoreCase("hide")
												&& (commands.size()>1))
												{
													hideNext=true;
													cmd=commands.get(1);
												}
												if((cmd.equalsIgnoreCase("info"))
												&&(commands.size()>1))
												{
													cmdDex++;
													cmd=commands.get(cmdDex);
												}
											}
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

	public void addStatAdjustment(final Item item, String stat, final String adjustment)
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

	public void addSpellAdjustment(final Item item, final String spell, final String parm)
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
	protected String cleanBuildingNameForXP(final MOB mob, String name)
	{
		name=" "+CMLib.english().removeArticleLead(name)+" ";
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,CMParms.parse(name));
		if(enhancedTypes != null)
		{
			for(int t=0;t<enhancedTypes.size();t++)
			{
				final EnhancedExpertise type=enhancedTypes.elementAt(t).first;
				final int stage=enhancedTypes.elementAt(t).second.intValue( ) & STAGE_MASK;
				final String expertiseID=CMLib.expertises().getApplicableExpertise(ID(),type.flag);
				ExpertiseLibrary.ExpertiseDefinition def = CMLib.expertises().getDefinition(expertiseID+CMath.convertToRoman(1));
				if(def==null)
					def = CMLib.expertises().getDefinition(expertiseID+1);
				if(def==null)
					def = CMLib.expertises().getDefinition(expertiseID);
				if(def==null)
					continue;
				name=CMStrings.replaceAll(name," "+def.getStageNames()[stage].toUpperCase()+" ","");
			}
		}
		return name.trim();
	}

	protected void affectLevelBy(final Item item, final int stage)
	{
		item.basePhyStats().setLevel(item.basePhyStats().level()+stage+1);
		item.phyStats().setLevel(item.basePhyStats().level()+stage+1);
		if((item instanceof Weapon)
		&&(item.basePhyStats().damage()>0))
		{
			final Item itemCopy=(Item)item.copyOf();
			final int level=CMLib.itemBuilder().timsLevelCalculator(itemCopy);
			itemCopy.basePhyStats().setLevel(level);
			itemCopy.phyStats().setLevel(level);
			CMLib.itemBuilder().balanceItemByLevel(itemCopy);
			final int oldDamage=itemCopy.basePhyStats().damage();
			itemCopy.basePhyStats().setLevel(level+stage+1);
			itemCopy.phyStats().setLevel(level+stage+1);
			CMLib.itemBuilder().balanceItemByLevel(itemCopy);
			final int damDiff = itemCopy.basePhyStats().damage() - oldDamage;
			item.basePhyStats().setDamage(item.basePhyStats().damage() + damDiff);
			item.phyStats().setDamage(item.phyStats().damage() + damDiff);
			itemCopy.destroy();
		}
		else
		if((item instanceof Armor)
		&&(item.basePhyStats().armor()>0))
		{
			final Item itemCopy=(Item)item.copyOf();
			final int level=CMLib.itemBuilder().timsLevelCalculator(itemCopy);
			itemCopy.basePhyStats().setLevel(level);
			itemCopy.phyStats().setLevel(level);
			CMLib.itemBuilder().balanceItemByLevel(itemCopy);
			final int oldArmor=itemCopy.basePhyStats().armor();
			itemCopy.basePhyStats().setLevel(level+stage+1);
			itemCopy.phyStats().setLevel(level+stage+1);
			CMLib.itemBuilder().balanceItemByLevel(itemCopy);
			final int damDiff = itemCopy.basePhyStats().armor() - oldArmor;
			item.basePhyStats().setArmor(item.basePhyStats().armor() + damDiff);
			item.phyStats().setArmor(item.phyStats().armor() + damDiff);
			itemCopy.destroy();
		}
		else
		if((item instanceof Container)
		&&(((Container)item).capacity()>0)
		&&(((Container)item).capacity()<Integer.MAX_VALUE/2))
			((Container)item).setCapacity(multiplyMinResult1(((Container)item).capacity(),0.1*(stage+1)));
		else
		if((item instanceof Food)
		||((item instanceof Drink)&&(CMath.bset(item.material(), RawMaterial.MATERIAL_LIQUID))))
		{
			if(item instanceof Food)
				((Food)item).setNourishment((int)Math.round(((Food)item).nourishment()*(1+(.5*(stage+1)))));
			else
			{
				((Drink)item).setLiquidHeld((int)Math.round(((Drink)item).liquidHeld()*(1+(.5*(stage+1)))));
				((Drink)item).setLiquidRemaining((int)Math.round(((Drink)item).liquidRemaining()*(1+(.5*stage))));
			}
		}
	}

	public void enhanceItem(final MOB mob, final Item item, int recipeLevel, final PairVector<EnhancedExpertise,Integer> types)
	{
		if(types==null)
			return;
		final EnhancedCraftingSkill affect=(EnhancedCraftingSkill)mob.fetchEffect(ID());
		if((affect!=null)
		&&(!affect.aborted)
		&&(activity == CraftingActivity.CRAFTING)
		&&(item!=null))
		{
			final ExpertiseLibrary exLib = CMLib.expertises();
			for(int t=0;t<types.size();t++)
			{
				final EnhancedExpertise type=types.elementAt(t).first;
				final int typeStageCode = types.elementAt(t).second.intValue();
				final int stage=typeStageCode  & STAGE_MASK;
				final boolean hide=(typeStageCode  & HIDE_MASK) > 0;
				final String expertiseID=exLib.getApplicableExpertise(ID(),type.flag);
				ExpertiseLibrary.ExpertiseDefinition def = exLib.getDefinition(expertiseID+CMath.convertToRoman(1));
				if(def==null)
					def = exLib.getDefinition(expertiseID+1);
				if(def==null)
					def = exLib.getDefinition(expertiseID);
				if(def==null)
					continue;
				int addToStat = CharState.STAT_MOVE;
				switch(type)
				{
				case LITECRAFT:
				{
					switch(stage)
					{
					case 0:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.1));
						break;
					case 1:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.2));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.3));
						//addStatAdjustment(item,"DEX","+1");
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					}
					break;
				}
				case RUSHCRAFT:
				{
					switch(stage)
					{
					case 0:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),-0.1));
						affect.bumpTickDown(Math.round(-0.10 * affect.tickDown));
						affectLevelBy(item, -2);
						break;
					case 1:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),-0.2));
						affect.bumpTickDown(Math.round(-0.20 * affect.tickDown));
						affectLevelBy(item, -3);
						break;
					case 2:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),-0.3));
						affect.bumpTickDown(Math.round(-0.30 * affect.tickDown));
						affectLevelBy(item, -4);
						break;
					}
					break;
				}
				case ADVNCRAFT:
				{
					if(stage >= 0)
					{
						applyName(item,def.getStageNames()[stage], hide);
						affectLevelBy(item, stage);
					}
					break;
				}
				case DURACRAFT:
				{
					if((!(item instanceof Armor))||(item.basePhyStats().armor()==0))
						commonTelL(mob,"@x1 only applies to protective armor.",def.getStageNames()[stage]);
					else
					switch(stage)
					{
					case 0:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setArmor(item.basePhyStats().armor()+1);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.1));
						break;
					case 1:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setArmor(multiplyMinResult1(item.basePhyStats().armor(),0.1)+1);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.2));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setArmor(multiplyMinResult1(item.basePhyStats().armor(),0.25)+1);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.3));
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
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.5));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 1:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),1.5));
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getStageNames()[stage], hide);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),2.5));
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
						commonTelL(mob,"@x1 only applies to weapons.",def.getStageNames()[stage]);
					else
					switch(stage)
					{
					case 0:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setDamage(multiplyMinResult1(item.basePhyStats().damage(),0.05));
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.1));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 1:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setDamage(multiplyMinResult1(item.basePhyStats().damage(),0.1));
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.2));
						item.basePhyStats().setWeight(multiplyMinResult1(item.basePhyStats().weight(),0.1));
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setDamage(multiplyMinResult1(item.basePhyStats().damage(),0.15)+1);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.3));
						item.basePhyStats().setWeight(multiplyMinResult1(item.basePhyStats().weight(),0.1));
						affect.bumpTickDown(Math.round(0.75 * affect.tickDown));
						break;
					}
					break;
				}
				case CNTRCRAFT:
				{
					if(!(item instanceof Weapon))
						commonTelL(mob,"@x1 only applies to weapons.",def.getStageNames()[stage]);
					else
					switch(stage)
					{
					case 0:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setAttackAdjustment(item.basePhyStats().attackAdjustment()+3);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.1));
						affect.bumpTickDown(Math.round(0.25 * affect.tickDown));
						break;
					case 1:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setAttackAdjustment(item.basePhyStats().attackAdjustment()+6);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.2));
						item.basePhyStats().setWeight(multiplyMinResult1(item.basePhyStats().weight(),0.05));
						affect.bumpTickDown(Math.round(0.5 * affect.tickDown));
						break;
					case 2:
						applyName(item,def.getStageNames()[stage], hide);
						item.basePhyStats().setAttackAdjustment(item.basePhyStats().attackAdjustment()+9);
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.3));
						item.basePhyStats().setWeight(multiplyMinResult1(item.basePhyStats().weight(),0.1));
						affect.bumpTickDown(Math.round(1.25 * affect.tickDown));
						break;
					}
					break;
				}
				case FORTCRAFT:
				case VIGOCRAFT:
				case IMBUCRAFT:
				{
					applyName(item,def.getStageNames()[stage], hide);
					if (type == EnhancedExpertise.IMBUCRAFT)
					{
						addToStat=CharState.STAT_MANA;
						item.basePhyStats().setDisposition(item.basePhyStats().disposition()|PhyStats.IS_BONUS);
					}
					Ability propA = item.fetchEffect("Prop_UseAdjuster");
					if(propA == null)
					{
						final String statName=CharState.STAT_DESCS[addToStat];
						propA=CMClass.getAbility("Prop_UseAdjuster");
						if(recipeLevel == 0)
							recipeLevel = 1;
						propA.setMiscText(statName+"+"+((stage+1)*recipeLevel));
						affect.bumpTickDown(Math.round((1.1 + (0.1 * stage)) * affect.tickDown));
						item.setBaseValue(multiplyMinResult1(item.baseGoldValue(),0.25));
						item.addNonUninvokableEffect(propA);
					}
					break;
				}
				default:
					break;
				}
			}
			item.recoverPhyStats();
		}
	}
}
