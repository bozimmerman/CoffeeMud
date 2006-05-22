package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

/* 
   Copyright 2000-2006 Bo Zimmerman

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
	public String ID() { return "EnhancedCraftingSkill"; }
	public String name(){ return "Enhanced Crafting Skill";}
	
	protected int materialAdjustments=0;
	
	protected final static String[] STAGES={"I","II","III"};
	protected final static String[] STAGESTAT={"14","18","22"};
	protected final static int TYPE_LITECRAFT=0;
	protected final static int TYPE_DURACRAFT=1;
	protected final static int TYPE_QUALCRAFT=2;
	protected final static int TYPE_LTHLCRAFT=3;
	protected final static String[] TYPES_CODES={"LITECRAFT",
											   "DURACRAFT",
											   "QUALCRAFT",
											   "LTHLCRAFT",
	};
	protected final static String[] TYPES_STATS={"DEX",
											     "STR",
											     "CHA",
											     "CON",
	};
	protected final static String[] TYPES_NAMES={"Light Crafting",
											   "Durable Crafting",
											   "Quality Crafting",
											   "Lethal Crafting",
	};
	
	protected final static String[][] STAGE_TYPES={
		{"Light","Supple","Agile"},
		{"Strong","Reinforced","Fortified"},
		{"Fine","Beautiful","Exquisite"},
		{"Damaging","Brutal","Lethal"},
	};
	
	protected final static String[] ALL_CODES={"LITECRAFTI","LITECRAFTII","LITECRAFTIII",
											 "DURACRAFTI","DURACRAFTII","DURACRAFTIII",
											 "QUALCRAFTI","QUALCRAFTII","QUALCRAFTIII",
											 "LTHLCRAFTI","LTHLCRAFTII","LTHLCRAFTIII"};
	
	protected String[] supportedEnhancements(){ return ALL_CODES;}
	
	private final static HashSet doneSkills=new HashSet();
	static
	{
		for(int t=0;t<TYPES_CODES.length;t++)
		{
			for(int s=0;s<STAGES.length;s++)
			{
				String finalReq="+"+TYPES_STATS[t]+" "+STAGESTAT[s];
				CMLib.edu().addDefinition(TYPES_CODES[t]+STAGES[s],TYPES_NAMES[t]+" "+STAGES[s],"",finalReq,0,1,0,0,0);
			}
		}
	}
	protected int[][] fetchFoundResourceData(MOB mob,
											 int req1Required,
											 String req1Desc, int[] req1,
											 int req2Required,
											 String req2Desc, int[] req2,
											 boolean bundle,
											 int autoGeneration,
											 DVector eduMods)
	{
		if(eduMods!=null)
		for(int t=0;t<eduMods.size();t++)
		{
			int type=((Integer)eduMods.elementAt(t,1)).intValue();
			int stage=((Integer)eduMods.elementAt(t,2)).intValue();
			switch(type)
			{
			case TYPE_LITECRAFT:
				switch(stage)
				{
				case 0:
					if(req1Required>0) req1Required=atLeast1(req1Required,-0.1);
					if(req2Required>0) req2Required=atLeast1(req2Required,-0.1);
					break;
				case 1:
					if(req1Required>0) req1Required=atLeast1(req1Required,-0.25);
					if(req2Required>0) req2Required=atLeast1(req2Required,-0.25);
					break;
				case 2:
					if(req1Required>0) req1Required=atLeast1(req1Required,-0.5);
					if(req2Required>0) req2Required=atLeast1(req2Required,-0.5);
					break;
				}
				break;
			case TYPE_DURACRAFT:
				switch(stage)
				{
				case 0:
					if(req1Required>0) req1Required=atLeast1(req1Required,0.1);
					if(req2Required>0) req2Required=atLeast1(req2Required,0.1);
					break;
				case 1:
					if(req1Required>0) req1Required=atLeast1(req1Required,0.2);
					if(req2Required>0) req2Required=atLeast1(req2Required,0.2);
					break;
				case 2:
					if(req1Required>0) req1Required=atLeast1(req1Required,0.25);
					if(req2Required>0) req2Required=atLeast1(req2Required,0.25);
					break;
				}
				break;
			case TYPE_QUALCRAFT:
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
			case TYPE_LTHLCRAFT:
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
			}
		}
		return super.fetchFoundResourceData(mob,
				req1Required,req1Desc,req1,
				req2Required,req2Desc,req2,
				bundle,autoGeneration,eduMods);
	}
	
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		if((!ID().equalsIgnoreCase("EnhancedCraftingSkill"))
		&&(!doneSkills.contains(ID()))
		&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)))
		{
			doneSkills.add(ID());
			EducationLibrary.EducationDefinition def=null;
			for(int t=0;t<TYPES_CODES.length;t++)
			{
				for(int s=0;s<STAGES.length;s++)
				if(supported().contains(TYPES_CODES[t]+STAGES[s]))
				{
					def=CMLib.edu().getDefinition(TYPES_CODES[t]+STAGES[s]);
					if(def!=null)
					{
						if((def.uncompiledListMask==null)||(def.uncompiledListMask.length()==0))
						{
							def.uncompiledListMask="";
							if(s>0)
								def.uncompiledListMask+=" -EDUCATIONS +"+TYPES_CODES[t]+STAGES[s-1];
							def.uncompiledListMask+=" -SKILLS";
						}
						def.uncompiledListMask+=" +"+ID();
						def.compiledListMask=CMLib.masking().maskCompile(def.uncompiledListMask);
					}
				}
			}
		}
	}
	protected Vector supportedV=null;
	protected final Vector supported(){
		if(supportedV==null)
		{
			supportedV=new Vector();
			for(int s=0;s<supportedEnhancements().length;s++)
				supportedV.addElement(supportedEnhancements()[s]);
		}
		return supportedV;
	}

	private final static int atLeast1(int value, double pct)
	{
		int change=(int)Math.round(CMath.mul(value,pct));
		if(pct<0.0)
		{
			if((change==0)&&(value>1)) change-=1;
			value+=change;
		}
		else
		{
			if(change==0) change+=1;
			value+=change;
		}
		return value;
	}
	
	protected String applyName(String name, String word)
	{
		Vector V=CMParms.parse(name);
		int insertHere=0;
		if((V.size()>0)
		&&(CMLib.english().isAnArticle((String)V.firstElement())))
			insertHere++;
		V.insertElementAt(word.toLowerCase(),insertHere);
		if((insertHere>0)
		&&(((String)V.firstElement()).equalsIgnoreCase("A"))
		&&(((String)V.firstElement()).equalsIgnoreCase("AN")))
		{
			V.removeElementAt(0);
			return CMStrings.startWithAorAn(CMParms.combineWithQuotes(V,0));
		}
		return CMParms.combineWithQuotes(V,0);
	}
	
	protected void applyName(Item item, String word)
	{
		String oldName=item.Name();
		item.setName(applyName(item.Name(),word));
		item.setDisplayText(applyName(item.displayText(),word));
		item.setDescription(applyName(item.description(),word));
		verb=CMStrings.replaceAll(verb,oldName,item.Name());
		displayText=CMStrings.replaceAll(displayText,oldName,item.Name());
		//startStr=CMStrings.replaceAll(startStr,oldName,item.Name());
	}
	
	public void enhanceList(MOB mob)
	{
		StringBuffer extras=new StringBuffer("");
		for(int t=0;t<TYPES_CODES.length;t++)
		{
			for(int s=STAGES.length-1;s>=0;s--)
				if((supported().contains(TYPES_CODES[t]+STAGES[s]))
				&&(mob.fetchEducation(TYPES_CODES[t]+STAGES[s])!=null))
					extras.append(STAGE_TYPES[t][s]+", ");
		}
		if(extras.length()>0)
			commonTell(mob,"You can use your educations to enhance this skill by " +
					 "prepending one or more of the following words to the name " +
					 "of the item you wish to craft: "+extras.substring(0,extras.length()-2)+".");
	}

	public DVector enhancedTypes(MOB mob, Vector commands)
	{
		String cmd=null;
		DVector types=null;
		materialAdjustments=0;
		if((commands!=null)&&(commands.size()>0)&&(commands.firstElement() instanceof String))
		{
			cmd=(String)commands.firstElement();
			if((!cmd.equalsIgnoreCase("list"))
			&&(!cmd.equalsIgnoreCase("mend"))
			&&(!cmd.equalsIgnoreCase("scan")))
			{
				boolean foundSomething=true;
				while(foundSomething)
				{
					foundSomething=false;
					for(int t=0;t<TYPES_CODES.length;t++)
					{
						for(int s=STAGES.length-1;s>=0;s--)
							if((supported().contains(TYPES_CODES[t]+STAGES[s]))
							&&(mob.fetchEducation(TYPES_CODES[t]+STAGES[s])!=null)
							&&(cmd.equalsIgnoreCase(STAGE_TYPES[t][s])))
							{
								commands.removeElementAt(0);
								if(types==null) types=new DVector(2);
								types.addElement(new Integer(t),new Integer(s));
								if(commands.size()>0)
									cmd=(String)commands.firstElement();
								else
									cmd="";
								foundSomething=true;
								break; // you can do any from a stage, but only 1 per stage!
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
	
	public void enhanceItem(MOB mob, Item item, DVector types)
	{
		if(types==null) return;
		EnhancedCraftingSkill affect=(EnhancedCraftingSkill)mob.fetchEffect(ID());
		if((affect!=null)
		&&(!affect.aborted)
		&&(!affect.messedUp)
		&&(!affect.mending)
		&&(item!=null))
		{
			for(int t=0;t<types.size();t++)
			{
				int type=((Integer)types.elementAt(t,1)).intValue();
				int stage=((Integer)types.elementAt(t,2)).intValue();
				switch(type)
				{
				case TYPE_LITECRAFT:
					switch(stage)
					{
					case 0:
						applyName(item,STAGE_TYPES[type][stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.2));
						break;
					case 1:
						applyName(item,STAGE_TYPES[type][stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),1.0));
						affect.tickDown*=2;
						break;
					case 2:
						applyName(item,STAGE_TYPES[type][stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),2.5));
						//addStatAdjustment(item,"DEX","+1");
						affect.tickDown*=4;
						break;
					}
					break;
				case TYPE_DURACRAFT:
					if(!(item instanceof Armor))
						commonTell(mob,STAGE_TYPES[type][stage]+" only applies to armor.");
					else
					switch(stage)
					{
					case 0:
						applyName(item,STAGE_TYPES[type][stage]);
						item.baseEnvStats().setArmor(item.baseEnvStats().armor()+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.1));
						break;
					case 1:
						applyName(item,STAGE_TYPES[type][stage]);
						item.baseEnvStats().setArmor(atLeast1(item.baseEnvStats().armor(),0.1)+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),1.5));
						affect.tickDown*=2;
						break;
					case 2:
						applyName(item,STAGE_TYPES[type][stage]);
						item.baseEnvStats().setArmor(atLeast1(item.baseEnvStats().armor(),0.25)+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),2.5));
						//addStatAdjustment(item,"CON","+1");
						affect.tickDown*=4;
						break;
					}
					break;
				case TYPE_QUALCRAFT:
					switch(stage)
					{
					case 0:
						applyName(item,STAGE_TYPES[type][stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.5));
						affect.tickDown*=2;
						break;
					case 1:
						applyName(item,STAGE_TYPES[type][stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),1.0));
						affect.tickDown*=3;
						break;
					case 2:
						applyName(item,STAGE_TYPES[type][stage]);
						item.setBaseValue(atLeast1(item.baseGoldValue(),2.5));
						addStatAdjustment(item,"CHA","+1");
						affect.tickDown*=4;
						break;
					}
					break;
				case TYPE_LTHLCRAFT:
					if(!(item instanceof Weapon))
						commonTell(mob,STAGE_TYPES[type][stage]+" only applies to weapons.");
					else
					switch(stage)
					{
					case 0:
						applyName(item,STAGE_TYPES[type][stage]);
						item.baseEnvStats().setDamage(atLeast1(item.baseEnvStats().damage(),0.05));
						item.setBaseValue(atLeast1(item.baseGoldValue(),0.5));
						affect.tickDown*=2;
						break;
					case 1:
						applyName(item,STAGE_TYPES[type][stage]);
						item.baseEnvStats().setDamage(atLeast1(item.baseEnvStats().damage(),0.1));
						item.setBaseValue(atLeast1(item.baseGoldValue(),2.0));
						item.baseEnvStats().setWeight(atLeast1(item.baseEnvStats().weight(),0.1));
						affect.tickDown*=3;
						break;
					case 2:
						applyName(item,STAGE_TYPES[type][stage]);
						item.baseEnvStats().setDamage(atLeast1(item.baseEnvStats().damage(),0.15)+1);
						item.setBaseValue(atLeast1(item.baseGoldValue(),4.0));
						item.baseEnvStats().setWeight(atLeast1(item.baseEnvStats().weight(),0.1));
						affect.tickDown*=5;
						break;
					}
					break;
				}
			}
			item.recoverEnvStats();
		}
	}
	
}
