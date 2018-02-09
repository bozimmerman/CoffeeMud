package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class CraftingSkill extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "CraftingSkill";
	}

	private final static String localizedName = CMLib.lang().L("Crafting Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CRAFTINGSKILL;
	}

	@Override
	public String accountForYourself()
	{
		return name() + " requires: " + supportedResourceString();
	}

	protected Item		buildingI		= null;
	protected Recipe	recipeHolder	= null;
	protected boolean	fireRequired	= true;
	
	protected LinkedList<String> last4items = new LinkedList<String>();

	protected enum CraftingActivity
	{
		CRAFTING,
		MENDING,
		LEARNING,
		REFITTING,
		RETITLING,
		DOORING,
		DEMOLISH
	}

	protected CraftingActivity	activity		= CraftingActivity.CRAFTING;
	protected boolean			messedUp		= false;

	// common recipe definition indexes
	protected static final int	RCP_FINALNAME	= 0;
	protected static final int	RCP_LEVEL		= 1;
	protected static final int	RCP_TICKS		= 2;

	// for ability component style materials
	protected static final int	CF_AMOUNT		= 0;
	protected static final int	CF_HARDNESS		= 1;
	protected static final int	CF_MATERIAL		= 2;
	protected static final int	CF_TOTAL		= 3;
	
	protected static class CraftParms
	{
		public int autoGenerate=0;
		public Physical givenTarget=null;
		public boolean forceLevels=false;
		public List<Item> results=new Vector<Item>(1);
		
		public CraftParms(int autoGenerate, Physical givenTarget, boolean forceLevels)
		{
			this.autoGenerate=autoGenerate;
			this.givenTarget=givenTarget;
			this.forceLevels=forceLevels;
		}
	}

	public CraftingSkill()
	{
		super();
	}

	protected enum EnhancedExpertise
	{
		LITECRAFT("LITE",ExpertiseLibrary.Flag.X1),
		DURACRAFT("DURA",ExpertiseLibrary.Flag.X2),
		QUALCRAFT("QUAL",ExpertiseLibrary.Flag.X3),
		LTHLCRAFT("LTHL",ExpertiseLibrary.Flag.X4),
		CNTRCRAFT("CNTR",ExpertiseLibrary.Flag.X5)
		;
		
		public final String stageKey;
		public final ExpertiseLibrary.Flag flag;
		
		private EnhancedExpertise(String stageKey, ExpertiseLibrary.Flag flag)
		{
			this.stageKey = stageKey;
			this.flag = flag;
		}
		
	}

	public String parametersFile()
	{
		return "";
	}

	public double getItemWeightMultiplier(boolean bundling)
	{
		return 1.0;
	}

	public int getStandardWeight(int baseWoodRequired, boolean bundling)
	{
		final int newWeight=(int)Math.round( baseWoodRequired * this.getItemWeightMultiplier( bundling ));
		if((baseWoodRequired>0) && (newWeight<=0))
			return 1;
		return newWeight;
	}

	protected String replacePercent(String thisStr, String withThis)
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

	protected void messedUpCrafting(MOB mob)
	{
		if(buildingI!=null)
		{
			if(buildingI.usesRemaining()<100)
			{
				if(buildingI.usesRemaining()>90)
					buildingI.setUsesRemaining(buildingI.usesRemaining()+1);
				else
				if(buildingI.usesRemaining()>80)
					buildingI.setUsesRemaining(buildingI.usesRemaining()+3);
				else
				if(buildingI.usesRemaining()>70)
					buildingI.setUsesRemaining(buildingI.usesRemaining()+5);
				else
				if(buildingI.usesRemaining()>60)
					buildingI.setUsesRemaining(buildingI.usesRemaining()+7);
				else
					buildingI.setUsesRemaining(buildingI.usesRemaining()+10);
			}
			commonEmote(mob,L("<S-NAME> mess(es) up mending @x1.",buildingI.name()));
		}

	}

	protected long getContainerType(final String s)
	{
		if(s.length()==0)
			return 0;
		long ret=0;
		final String[] allTypes=CMParms.parseAny(s, "|", true).toArray(new String[0]);
		for(final String splitS : allTypes)
		{
			if(CMath.isInteger(splitS))
				ret = ret | CMath.s_int(splitS);
			else
			{
				final int bit=CMParms.indexOf(Container.CONTAIN_DESCS, splitS.toUpperCase().trim());
				if(bit>0)
					ret = ret | CMath.pow(2,(bit-1));
			}
		}
		return ret;
	}

	protected List<List<String>> addRecipes(MOB mob, List<List<String>> recipes)
	{
		if(mob==null)
			return recipes;
		Item I=null;
		List<List<String>> V=null;
		List<String> V2=null;
		List<String> lastRecipeV=null;
		boolean clonedYet=false;
		for(int i=0;i<mob.numItems();i++)
		{
			I=mob.getItem(i);
			if((I instanceof Recipe)
			&&(((Recipe)I).getCommonSkillID().equalsIgnoreCase(ID())))
			{
				if(!clonedYet)
				{
					recipes=new XVector<List<String>>(recipes);
					clonedYet=true;
				}
				final StringBuffer allRecipeLines=new StringBuffer("");
				if(((Recipe)I).getRecipeCodeLines().length>0)
				{
					for(final String recipeLine : ((Recipe)I).getRecipeCodeLines())
					{
						allRecipeLines.append(recipeLine);
						allRecipeLines.append( "\n" );
					}
				}
				V=loadList(allRecipeLines);
				for(int v=0;v<V.size();v++)
				{
					V2=V.get(v);
					if(recipes.size()==0)
						recipes.add(V2);
					else
					{
						lastRecipeV=recipes.get(recipes.size()-1);
						if((recipes.size()==0)||lastRecipeV.size()<=V2.size())
							recipes.add(V2);
						else
						{
							Log.errOut(ID(),"Not enough parms ("+lastRecipeV.size()+"<="+V2.size()+"): "+CMParms.combine(V2));
							while(V2.size()<lastRecipeV.size())
								V2.add("");
							while(V2.size()>lastRecipeV.size())
								V2.remove(V2.size()-1);
							recipes.add(V2);
						}
					}
					if(V2 instanceof Vector)
						((Vector)V2).trimToSize();
				}
			}
		}
		if(recipes instanceof Vector)
			((Vector)recipes).trimToSize();
		return recipes;
	}

	protected int getBuildingMaterial(int woodRequired, int[][] foundData, int[] compData)
	{
		if((woodRequired == 0) && (compData[CF_MATERIAL] > 0))
			return compData[CF_MATERIAL];
		else
		if((woodRequired==0)&&(foundData[1][FOUND_CODE]>0))
			return foundData[1][FOUND_CODE];
		else
		if((foundData[0][FOUND_CODE]==0)&&(foundData[1][FOUND_CODE]!=0))
			return foundData[1][FOUND_CODE];
		else
			return foundData[0][FOUND_CODE];
	}
	
	protected int adjustWoodRequired(int woodRequired, MOB mob)
	{
		int newWoodRequired=woodRequired-(int)Math.round((0.05*woodRequired*getXPCOSTLevel(mob)));
		if(newWoodRequired<=0)
		{
			if(woodRequired > 0)
				newWoodRequired=1;
			else
				newWoodRequired=0;
		}
		return newWoodRequired;
	}

	protected String cleanBuildingNameForXP(MOB mob, String name)
	{
		return name;
	}
	
	@Override
	protected boolean dropAWinner(MOB mob, Item buildingI)
	{
		final Room R=mob.location();
		if(R==null)
			commonTell(mob,L("You are NOWHERE?!"));
		else
		if(buildingI==null)
			commonTell(mob,L("You have built NOTHING?!!"));
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,buildingI,this,CMMsg.TYP_ITEMGENERATED|CMMsg.MASK_ALWAYS,null);
			
			final double levelLimit=CMProps.getIntVar(CMProps.Int.EXPRATE);
			final double levelDiff=buildingI.phyStats().level()-mob.phyStats().level();
			double levelXPFactor = 100.0;
			if(levelDiff<(-levelLimit) )
				levelXPFactor=0.0;
			else
			if(levelLimit>0)
			{
				double levelFactor=levelDiff / levelLimit;
				if( levelFactor > levelLimit )
					levelFactor = levelLimit;
				levelXPFactor+=(levelFactor *  levelXPFactor);
			}
			if((buildingI instanceof DoorKey) && (!ID().equalsIgnoreCase("LockSmith")))
				msg.setValue(0);
			else
			{
				final CraftingSkill mySkill = (CraftingSkill)mob.fetchAbility(ID());
				if(mySkill == null)
					msg.setValue(0);
				else
				{
					final LinkedList<String> localLast5Items = mySkill.last4items;
					String buildingIName = cleanBuildingNameForXP(mob,buildingI.Name().toUpperCase());
					int lastBaseDuration = this.lastBaseDuration;
					if(lastBaseDuration > 75)
						lastBaseDuration = 75;
					double baseXP = lastBaseDuration * levelXPFactor / 25.0;
					double xp = lastBaseDuration * levelXPFactor / 25.0;
					for(String s : localLast5Items)
					{
						if(s.equals(buildingIName))
							xp -= (baseXP * 0.25);
					}
					if(localLast5Items.size()==5)
						localLast5Items.removeFirst();
					localLast5Items.addLast(buildingIName);
					if(xp > 0.0)
						msg.setValue((int)Math.round(xp));
				}
			}
			if(mob.location().okMessage(mob,msg))
			{
				R.addItem(buildingI,ItemPossessor.Expire.Player_Drop);
				R.recoverRoomStats();
				mob.location().send(mob,msg);
				if(!R.isContent(buildingI))
				{
					commonTell(mob,L("You have won the common-skill-failure LOTTERY! Congratulations!"));
					CMLib.leveler().postExperience(mob, null, null,50,false);
				}
				else
					return true;
			}
		}
		return false;
	}

	protected void addSpells(Physical P, String spells)
	{
		if(spells.length()==0)
			return;
		if(spells.equalsIgnoreCase("bundle"))
			return;
		if(spells.startsWith("*") && spells.endsWith(";__DELETE__"))
		{
			String ableID=spells.substring(0, spells.indexOf(';'));
			Ability oldA=P.fetchEffect(ableID.substring(1));
			if(oldA!=null)
			{
				oldA.unInvoke();
				P.delEffect(oldA);
				spells="";
			}
		}
		final List<Ability> V=CMLib.ableParms().getCodedSpells(spells);
		for(int v=0;v<V.size();v++)
		{
			final Ability A=V.get(v);
			if(P instanceof Wand)
			{
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
				||(((Wand)P).getSpell()!=null))
					P.addNonUninvokableEffect(A);
				else
					((Wand)P).setSpell(A);
			}
			else
				P.addNonUninvokableEffect(A);
		}
	}

	protected void setWearLocation(Item I, String wearLocation, int hardnessMultiplier)
	{
		short[] layerAtt = null;
		short[] layers = null;
		if(I instanceof Armor)
		{
			layerAtt = new short[1];
			layers = new short[1];
			final long[] wornLoc = new long[1];
			final boolean[] logicalAnd = new boolean[1];
			final double[] hardBonus=new double[]{hardnessMultiplier};
			CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,wearLocation);
			if(I instanceof Armor)
			{
				final Armor armor = (Armor)I;
				armor.setClothingLayer(layers[0]);
				armor.setLayerAttributes(layerAtt[0]);
			}
			if(I.basePhyStats().armor()>0)
				I.basePhyStats().setArmor(I.basePhyStats().armor()+(int)Math.round(hardBonus[0]));
			I.setRawLogicalAnd(logicalAnd[0]);
			I.setRawProperLocationBitmap(wornLoc[0]);
		}
	}

	protected List<List<String>> loadList(StringBuffer str)
	{
		final List<List<String>> V=new Vector<List<String>>();
		if(str==null)
			return V;
		List<String> V2=new Vector<String>();
		boolean oneComma=false;
		int start=0;
		int longestList=0;
		boolean skipLine=(str.length()>0)&&(str.charAt(0)=='#');
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\t')
			{
				if(!skipLine)
				{
					V2.add(str.substring(start,i));
					start=i+1;
					oneComma=true;
				}
			}
			else
			if((str.charAt(i)=='\n')||(str.charAt(i)=='\r'))
			{
				if(skipLine)
					skipLine=false;
				else
				if(oneComma)
				{
					V2.add(str.substring(start,i));
					if(V2.size()>longestList)
						longestList=V2.size();
					if(V2 instanceof Vector)
						((Vector)V2).trimToSize();
					V.add(V2);
					V2=new Vector<String>();
				}
				start=i+1;
				oneComma=false;
				if((start<str.length())&&(str.charAt(start)=='#'))
					skipLine=true;
			}
		}
		if((oneComma)&&(str.substring(start).trim().length()>0)&&(!skipLine))
			V2.add(str.substring(start));
		if(V2.size()>1)
		{
			if(V2.size()>longestList)
				longestList=V2.size();
			V.add(V2);
		}
		for(int v=0;v<V.size();v++)
		{
			V2=V.get(v);
			while(V2.size()<longestList)
				V2.add("");
		}
		return V;
	}

	protected List<List<String>> loadRecipes(String filename)
	{
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS).text();
			V=loadList(str);
			if((V.size()==0)&&(!ID().equals("GenCraftSkill")))
				Log.errOut(ID(),"Recipes not found!");
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}

	protected static final int FOUND_CODE=0;
	protected static final int FOUND_AMT=1;
	
	public List<List<String>> fetchRecipes()
	{
		return loadRecipes();
	}

	protected List<List<String>> loadRecipes()
	{
		return new Vector<List<String>>();
	}

	protected int[][] fetchFoundResourceData(MOB mob,
											 int req1Required,
											 String req1Desc, int[] req1,
											 int req2Required,
											 String req2Desc, int[] req2,
											 boolean bundle,
											 int autoGeneration,
											 PairVector<EnhancedExpertise,Integer> eduMods)
	{
		final int[][] data=new int[2][2];
		if((req1Desc!=null)&&(req1Desc.length()==0))
			req1Desc=null;
		if((req2Desc!=null)&&(req2Desc.length()==0))
			req2Desc=null;

		// the fake resource generation:
		if(autoGeneration>0)
		{
			data[0][FOUND_AMT]=req1Required;
			data[1][FOUND_AMT]=req2Required;
			data[0][FOUND_CODE]=autoGeneration;
			data[1][FOUND_CODE]=autoGeneration;
			return data;
		}

		Item firstWood=null;
		Item firstOther=null;
		if(req1!=null)
		{
			for (final int element : req1)
			{
				if((element&RawMaterial.RESOURCE_MASK)==0)
					firstWood=CMLib.materials().findMostOfMaterial(mob.location(),element);
				else
					firstWood=CMLib.materials().findFirstResource(mob.location(),element);

				if(firstWood!=null)
					break;
			}
		}
		else
		if(req1Desc!=null)
			firstWood=CMLib.materials().fetchFoundOtherEncoded(mob.location(),req1Desc);
		data[0][FOUND_AMT]=0;
		if(firstWood!=null)
		{
			data[0][FOUND_AMT]=CMLib.materials().findNumberOfResource(mob.location(),firstWood.material());
			data[0][FOUND_CODE]=firstWood.material();
		}

		if(req2!=null)
		{
			for (final int element : req2)
			{
				if((element&RawMaterial.RESOURCE_MASK)==0)
					firstOther=CMLib.materials().findMostOfMaterial(mob.location(),element);
				else
					firstOther=CMLib.materials().findFirstResource(mob.location(),element);
				if(firstOther!=null)
					break;
			}
		}
		else
		if(req2Desc!=null)
			firstOther=CMLib.materials().fetchFoundOtherEncoded(mob.location(),req2Desc);
		data[1][FOUND_AMT]=0;
		if(firstOther!=null)
		{
			data[1][FOUND_AMT]=CMLib.materials().findNumberOfResource(mob.location(),firstOther.material());
			data[1][FOUND_CODE]=firstOther.material();
		}
		if(req1Required>0)
		{
			if(data[0][FOUND_AMT]==0)
			{
				if(req1Desc!=null)
					commonTell(mob,L("There is no @x1 here to make anything from!  It might need to be put down first.",req1Desc.toLowerCase()));
				return null;
			}
			if(!bundle)
				req1Required=fixResourceRequirement(data[0][FOUND_CODE],req1Required);
		}
		if(req2Required>0)
		{
			if(req2Desc != null)
			{
				if(((req2!=null)&&(data[1][FOUND_AMT]==0))
				||((req2==null)&&(req2Desc.length()>0)&&(data[1][FOUND_AMT]==0)))
				{
					if(req2Desc.equalsIgnoreCase("PRECIOUS"))
						commonTell(mob,L("You need some sort of precious stones to make that.  There is not enough here.  Are you sure you set it all on the ground first?"));
					else
					if(req2Desc.equalsIgnoreCase("WOODEN"))
						commonTell(mob,L("You need some wood to make that.  There is not enough here.  Are you sure you set it all on the ground first?"));
					else
						commonTell(mob,L("You need some @x1 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",req2Desc.toLowerCase()));
					return null;
				}
			}
			if(!bundle)
				req2Required=fixResourceRequirement(data[1][FOUND_CODE],req2Required);
		}

		if(req1Required>data[0][FOUND_AMT])
		{
			commonTell(mob,L("You need @x1 pounds of @x2 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",""+req1Required,RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase()));
			return null;
		}
		data[0][FOUND_AMT]=req1Required;
		if((req2Required>0)&&(req2Required>data[1][FOUND_AMT]))
		{
			commonTell(mob,L("You need @x1 pounds of @x2 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",""+req2Required,RawMaterial.CODES.NAME(data[1][FOUND_CODE]).toLowerCase()));
			return null;
		}
		data[1][FOUND_AMT]=req2Required;
		return data;
	}

	protected void randomRecipeFix(MOB mob, List<List<String>> recipes, List<String> commands, int autoGeneration)
	{
		if(((mob.isMonster()&&(!CMLib.flags().isAnimalIntelligence(mob)))||(autoGeneration>0))
		&&(commands.size()==0)
		&&(recipes!=null)
		&&(recipes.size()>0))
		{
			int tries=0;
			final int maxtries=100;
			while((++tries)<maxtries)
			{
				final List<String> randomRecipe=recipes.get(CMLib.dice().roll(1,recipes.size(),-1));
				boolean proceed=true;
				if((randomRecipe.size()>1))
				{
					int levelIndex=-1;
					for(int i=1;i<randomRecipe.size();i++)
					{
						if(CMath.isInteger(randomRecipe.get(i)))
						{
							levelIndex=i;
							break;
						}
					}
					if((levelIndex>0)
					&&(xlevel(mob)<CMath.s_int(randomRecipe.get(levelIndex))))
						proceed=false;
				}
				if((proceed)||(tries==(maxtries-1)))
				{
					commands.add(randomRecipe.get(RCP_FINALNAME));
					break;
				}
			}
		}
	}

	public ItemKeyPair craftAnyItem(int material)
	{
		return craftItem(null,material,false, false);
	}

	/**
	 * This method is called when a player or the system invokes this skill,
	 * especially when they intend to use the skill to auto-generate an item
	 * instead of following the more friendly user-crafting.
	 * Calls the more complete invoke method without an empty command strings vector
	 * unless target is non-null, in which case the vector will contain the name
	 * of the target.
	 * @param mob the player or mob invoking the skill
	 * @param commands the parameters entered for the skill (minus trigger word)
	 * @param givenTarget null, unless being auto-invoked. Represents an override target.
	 * @param auto false if player enters command, true if system invokes the command
	 * @param asLevel -1, unless being auto-invoked, when it is the level to invoke it at.
	 * @param autoGenerate 0, unless auto generation, in which case it's a RawMaterial Resource Code number
	 * @param forceLevels true to override other level modifiers on the items to force the Stock level.
	 * @param crafted when autoGenerate &gt; 0, this is where the auto generated crafted items are placed
	 * @return whether the skill successfully invoked.
	 */
	protected boolean autoGenInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		return false;
	}
	
	public ItemKeyPair craftItem(String recipeName, int material, boolean forceLevels, boolean noSafety)
	{
		MOB mob=null;
		try
		{
			mob=CMLib.map().getFactoryMOBInAnyRoom();
			if(noSafety)
				mob.setAttribute(Attrib.SYSOPMSGS, true);
			mob.basePhyStats().setLevel(Integer.MAX_VALUE/2);
			mob.basePhyStats().setSensesMask(mob.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
			mob.recoverPhyStats();
			return craftItem(mob,new XVector<String>(recipeName),material,forceLevels);
		}
		finally
		{
			if(mob!=null)
			{
				mob.setAttribute(Attrib.SYSOPMSGS, false);
				mob.destroy();
			}
		}
	}
	
	public ItemKeyPair craftItem(MOB mob, List<String> recipes, int material, boolean forceLevels)
	{
		Item building=null;
		DoorKey key=null;
		int tries=0;
		if(material<0)
		{
			List<Integer> rscs=myResources();
			if(rscs.size()==0)
				rscs=new XVector<Integer>(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
			material=rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue();
		}
		while(((building==null)||(building.name().endsWith(" bundle")))&&(((++tries)<100)))
		{
			List<Item> V=new ArrayList<Item>(1);
			autoGenInvoke(mob,recipes,null,true,-1,material,forceLevels,V);
			if(V.size()>0)
			{
				if((V.size()>1)&&((V.get(V.size()-2) instanceof DoorKey)))
					key=(DoorKey)V.get(V.size()-2);
				else
					key=null;
				building=V.get(V.size()-1);
			}
			else
				building=null;
		}
		if(building==null)
			return null;
		building.setSecretIdentity("");
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();
		if(key!=null)
		{
			key.setSecretIdentity("");
			key.recoverPhyStats();
			key.text();
			key.recoverPhyStats();
		}
		return new ItemKeyPair(building, key);
	}

	public List<ItemKeyPair> craftAllItemSets(int material, boolean forceLevels)
	{
		final List<ItemKeyPair> allItems=new Vector<ItemKeyPair>();
		final List<List<String>> recipes=fetchRecipes();
		Item built=null;
		final HashSet<String> usedNames=new HashSet<String>();
		ItemKeyPair pair=null;
		String s=null;
		for(int r=0;r<recipes.size();r++)
		{
			s=recipes.get(r).get(RCP_FINALNAME);
			s=replacePercent(s,"").trim();
			pair=craftItem(s,material,forceLevels, false);
			if(pair==null)
				continue;
			built=pair.item;
			if(!usedNames.contains(built.Name()))
			{
				usedNames.add(built.Name());
				allItems.add(pair);
			}
		}
		usedNames.clear();
		return allItems;
	}

	public boolean checkInfo(MOB mob, List<String> commands)
	{
		if((commands!=null)
		&&(commands.size()>1)
		&&(commands.get(0).equalsIgnoreCase("info")))
		{
			List<String> recipe = new XVector<String>(commands);
			recipe.remove(0);
			String recipeName = CMParms.combine(commands);
			List<Integer> rscs=myResources();
			if(rscs.size()==0)
				rscs=new XVector<Integer>(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
			final int material;
			switch(rscs.get(0).intValue()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_CLOTH:
				material = RawMaterial.RESOURCE_COTTON;
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
				material = RawMaterial.RESOURCE_IRON;
				break;
			case RawMaterial.MATERIAL_WOODEN:
				material = RawMaterial.RESOURCE_WOOD;
				break;
			case RawMaterial.MATERIAL_ROCK:
				material = RawMaterial.RESOURCE_STONE;
				break;
			default:
				material=RawMaterial.CODES.MOST_FREQUENT(rscs.get(0).intValue()&RawMaterial.MATERIAL_MASK);
				break;
			}
			ItemKeyPair pair = craftItem(mob,recipe,material,false);
			if(pair == null)
			{
				commonTell(mob,L("You don't know how to make '@x1'",recipeName));
			}
			else
			{
				final String viewDesc = CMLib.coffeeShops().getViewDescription(mob, pair.item);
				commonTell(mob,viewDesc);
				pair.item.destroy();
				if(pair.key!=null)
					pair.key.destroy();
			}
			return true;
		}
		return false;
	}

	public ItemKeyPair craftItem(String recipeName)
	{
		List<Integer> rscs=myResources();
		if(rscs.size()==0)
			rscs=new XVector<Integer>(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
		final int material=rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue();
		return craftItem(recipeName,material,false, false);
	}

	public List<ItemKeyPair> craftAllItemSets(boolean forceLevels)
	{
		List<Integer> rscs=myResources();
		final List<ItemKeyPair> allItems=new Vector<ItemKeyPair>();
		List<ItemKeyPair> pairs=null;
		if(rscs.size()==0)
			rscs=new XVector<Integer>(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
		for(int r=0;r<rscs.size();r++)
		{
			pairs=craftAllItemSets(rscs.get(r).intValue(), forceLevels);
			if((pairs==null)||(pairs.size()==0))
				continue;
			allItems.addAll(pairs);
		}
		return allItems;
	}

	public List<List<String>> matchingRecipeNames(String recipeName, boolean beLoose)
	{
		return matchingRecipeNames(fetchRecipes(),recipeName,beLoose);
	}

	protected List<List<String>> matchingRecipeNames(List<List<String>> recipes, String recipeName, boolean beLoose)
	{
		final List<List<String>> matches=new Vector<List<String>>();
		if(recipeName.length()==0)
			return matches;
		for(int r=0;r<recipes.size();r++)
		{
			final List<String> V=recipes.get(r);
			if(V.size()>0)
			{
				final String item=V.get(RCP_FINALNAME);
				if(replacePercent(item,"").equalsIgnoreCase(recipeName))
					matches.add(V);
			}
		}
		if(matches.size()>0)
			return matches;
		for(int r=0;r<recipes.size();r++)
		{
			final List<String> V=recipes.get(r);
			if(V.size()>0)
			{
				final String item=V.get(RCP_FINALNAME);
				if(((replacePercent(item,"").toUpperCase()+" ").startsWith(recipeName.toUpperCase())))
					matches.add(V);
			}
		}
		if(matches.size()>0)
			return matches;
		for(int r=0;r<recipes.size();r++)
		{
			final List<String> V=recipes.get(r);
			if(V.size()>0)
			{
				final String item=V.get(RCP_FINALNAME);
				if(((" "+replacePercent(item,"").toUpperCase()+" ").indexOf(" "+recipeName.toUpperCase()+" ")>=0))
					matches.add(V);
			}
		}
		if(matches.size()>0)
			return matches;
		if(beLoose)
		{
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=V.get(RCP_FINALNAME);
					if((recipeName.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
						matches.add(V);
				}
			}
			if(matches.size()>0)
				return matches;
			final String lastWord=CMParms.parse(recipeName).lastElement();
			if(lastWord.length()>1)
			{
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					if(V.size()>0)
					{
						final String item=V.get(RCP_FINALNAME);
						if((replacePercent(item,"").toUpperCase().indexOf(lastWord.toUpperCase())>=0)
						||(lastWord.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
							matches.add(V);
					}
				}
			}
		}
		return matches;
	}

	protected Vector<Item> getAllMendable(MOB mob, Environmental from, Item contained)
	{
		Vector<Item> V=new Vector<Item>();
		if(from==null)
			return V;
		if(from instanceof Room)
		{
			final Room R=(Room)from;
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I!=null)
				&&(I.container()==contained)
				&&(canMend(mob,I,true))
				&&(CMLib.flags().canBeSeenBy(I,mob)))
					V.addElement(I);
			}
		}
		else
		if(from instanceof MOB)
		{
			final MOB M=(MOB)from;
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				if((I!=null)
				&&(I.container()==contained)
				&&(canMend(mob,I,true))
				&&(CMLib.flags().canBeSeenBy(I,mob))
				&&((mob==from)||(!I.amWearingAt(Wearable.IN_INVENTORY))))
					V.addElement(I);
			}
		}
		else
		if(from instanceof Item)
		{
			if(from instanceof Container)
				V=getAllMendable(mob,((Item)from).owner(),(Item)from);
			if(canMend(mob,from,true))
				V.addElement((Item)from);
		}
		return V;
	}

	public boolean publicScan(MOB mob, List<String> commands)
	{
		final String rest=CMParms.combine(commands,1);
		Environmental scanning=null;
		if(rest.length()==0)
			scanning=mob;
		else
		if(rest.equalsIgnoreCase("room"))
			scanning=mob.location();
		else
		{
			scanning=mob.location().fetchInhabitant(rest);
			if((scanning==null)||(!CMLib.flags().canBeSeenBy(scanning,mob)))
			{
				commonTell(mob,L("You don't see anyone called '@x1' here.",rest));
				return false;
			}
		}
		final Vector<Item> allStuff=getAllMendable(mob,scanning,null);
		if(allStuff.size()==0)
		{
			if(mob==scanning)
				commonTell(mob,L("You don't seem to have anything that needs mending with @x1.",name()));
			else
				commonTell(mob,L("You don't see anything on @x1 that needs mending with @x2.",scanning.name(),name()));
			return false;
		}
		final StringBuffer buf=new StringBuffer("");
		if(scanning==mob)
			buf.append(L("The following items could use some @x1:\n\r",name()));
		else
			buf.append(L("The following items on @x1 could use some @x2:\n\r",scanning.name(),name()));
		for(int i=0;i<allStuff.size();i++)
		{
			final Item I=allStuff.elementAt(i);
			buf.append(CMStrings.padRight(I.usesRemaining()+"%",5)+I.name());
			if(!I.amWearingAt(Wearable.IN_INVENTORY))
				buf.append(" ("+Wearable.CODES.NAME(I.rawWornCode())+")");
			if(i<(allStuff.size()-1))
				buf.append("\n\r");
		}
		commonTell(mob,buf.toString());
		return true;
	}

	protected int getPercentChanceToDeconstruct(final MOB crafterM, final Item I)
	{
		return (int)Math.round(((1.0+crafterM.phyStats().level()-I.phyStats().level())
			   /crafterM.phyStats().level()/2.0+0.5)
			*(proficiency()/100.0)*(proficiency()/100.0)*100.0);
	}

	public boolean mayICraft(final Item I)
	{
		return false;
	}

	protected boolean deconstructRecipeInto(final Item I, final Recipe R)
	{

		if((I==null)||(R==null))
			return false;
		if(!(this instanceof ItemCraftor))
			return false;
		final ItemCraftor C=(ItemCraftor)this;
		if(!C.supportsDeconstruction())
			return false;
		if(!C.mayICraft(I))
			return false;
		final List<String> existingRecipes=new XVector<String>(R.getRecipeCodeLines());
		if(R.getTotalRecipePages() <=existingRecipes.size())
			return false;
		try
		{
			existingRecipes.add(CMLib.ableParms().makeRecipeFromItem(C, I));
			R.setRecipeCodeLines(existingRecipes.toArray(new String[0]));
			R.setCommonSkillID( ID() );
		}
		catch(final CMException cme)
		{
			Log.errOut("CraftingSkill",cme.getMessage());
			return false;
		}
		return true;
	}

	protected boolean mayBeCrafted(final Item I)
	{
		if(I==null)
			return false;
		if(I instanceof ArchonOnly)
			return false;
		//if(!(I.isGeneric())) return false;
		if(I instanceof Food)
			return false;
		if(I instanceof Scroll)
			return false;
		if(I instanceof ClanItem)
			return false;
		if(I instanceof DeadBody)
			return false;
		if((!CMLib.flags().isDroppable(I))
		||(!CMLib.flags().isGettable(I))
		||(!CMLib.flags().isRemovable(I))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_ITEMNORUIN))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_ITEMNOWISH))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_UNLOCATABLE)))
			return false;
		for(int i=0;i<I.numEffects();i++)
		{
			final Ability A=I.fetchEffect(i);
			if(A!=null)
			{
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
				&&(CMath.bset( A.flags(), Ability.FLAG_ZAPPER )))
					return false;
				if(CMath.bset( A.flags(), Ability.FLAG_UNCRAFTABLE ))
					return false;
			}
		}
		for(int i=0;i<I.numBehaviors();i++)
		{
			if(I.fetchBehavior( i ) instanceof ScriptingEngine)
				return false;
		}
		if(I.numScripts()>0)
			return false;
		if(CMLib.flags().flaggedBehaviors(I, Behavior.FLAG_POTENTIALLYAUTODEATHING).size()>0)
			return false;
		final Item I2=CMClass.getItem(I.ID());
		if(CMLib.flags().isGettable(I)!=CMLib.flags().isGettable(I2))
			return false;
		if(CMLib.flags().isRemovable(I)!=CMLib.flags().isRemovable(I2))
			return false;
		return true;
	}

	public boolean isANativeItem(String name)
	{
		name=CMLib.english().stripPunctuation(name);
		final List<String> nameV=CMParms.parse(name.toUpperCase());
		final List<List<String>> recipes = this.loadRecipes();
		if(nameV.size()==0)
			return false;
		TreeSet<String> allExpertiseWords=(TreeSet<String>)Resources.getResource("CRAFTING_SKILL_EXPERTISE_WORDS");
		if(allExpertiseWords == null)
		{
			if (this instanceof EnhancedCraftingSkill)
			{
				final List<ExpertiseLibrary.ExpertiseDefinition> V = ((EnhancedCraftingSkill)this).getAllThisSkillsDefinitions();
				allExpertiseWords = new TreeSet<String>();
				for(final ExpertiseLibrary.ExpertiseDefinition def : V )
				{
					if(def.getData() != null)
					{
						for(final String s : def.getData())
							allExpertiseWords.add(s.toUpperCase());
					}
				}
				Resources.submitResource("CRAFTING_SKILL_EXPERTISE_WORDS", allExpertiseWords);
			}
			else
				allExpertiseWords=new TreeSet<String>();
		}
		for(final List<String> recipe : recipes)
		{
			if(recipe.size() <= RCP_FINALNAME)
				continue;
			final String thisOnesName = recipe.get(RCP_FINALNAME);
			final List<String> thisOneNameV=CMParms.parse(thisOnesName.toUpperCase());
			boolean match=false;
			for(int n=0,o=0;;n++)
			{
				if((n==nameV.size())&&(o==thisOneNameV.size()))
					break;
				if((n==nameV.size())&&(o<thisOneNameV.size()))
				{
					match=false;
					break;
				}
				final String nw=nameV.get(n);
				if(CMLib.english().isAnArticle(nw))
				{
					// ignoring ALL articles!
				}
				else
				if(allExpertiseWords.contains(nw))
				{
					match=true;
					// wasted real word, wait to match recipe word
				}
				else
				if(o==thisOneNameV.size())
				{
					match=false;
					break;
				}
				else
				if(thisOneNameV.get(o).equals("%"))
				{
					if(RawMaterial.CODES.FIND_CaseSensitive(nw)>=0)
					{
						o++; // match!
						match=true;
					}
					else
					{
						match=false;
						break;
					}
				}
				else
				{
					final String ow=CMLib.english().stripPunctuation(thisOneNameV.get(o));
					if(CMLib.english().isAnArticle(ow))
					{
						// ignoring all articles
						o++;
						n--;
					}
					else
					if(nw.equals(ow))
					{
						o++; // match!
						match=true;
					}
					else
					{
						match=false;
						break;
					}
				}
			}
			if(match)
				return true;
		}
		return false;
	}

	public boolean mayICraft(final MOB crafterM, final Item I)
	{
		if(!mayICraft(I))
			return false;
		if((!crafterM.isMine(I))&&(!CMLib.law().doesHavePriviledgesHere(crafterM,crafterM.location())))
			return false;
		return true;
	}

	protected void setWeaponTypeClass(Weapon weapon, String weaponClass)
	{
		setWeaponTypeClass(weapon,weaponClass,Weapon.TYPE_BASHING,Weapon.TYPE_BASHING);
	}

	protected void setWeaponTypeClass(Weapon weapon, String weaponClass, int flailedType)
	{
		setWeaponTypeClass(weapon,weaponClass,flailedType,Weapon.TYPE_BASHING);
	}

	protected void setWeaponTypeClass(Weapon weapon, String weaponClass, int flailedType, int naturalType)
	{
		weapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		for(int cl=0;cl<Weapon.TYPE_DESCS.length;cl++)
		{
			if(weaponClass.equalsIgnoreCase(Weapon.TYPE_DESCS[cl]))
			{
				weapon.setWeaponDamageType(cl);
				return;
			}
		}
		for(int cl=0;cl<Weapon.CLASS_DESCS.length;cl++)
		{
			if(weaponClass.equalsIgnoreCase(Weapon.CLASS_DESCS[cl]))
				weapon.setWeaponClassification(cl);
		}
		switch(weapon.weaponClassification())
		{
		case Weapon.CLASS_AXE:
			weapon.setWeaponDamageType(Weapon.TYPE_SLASHING);
			break;
		case Weapon.CLASS_SWORD:
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED:
		case Weapon.CLASS_POLEARM:
		case Weapon.CLASS_RANGED:
		case Weapon.CLASS_THROWN:
			weapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
			break;
		case Weapon.CLASS_FLAILED:
			weapon.setWeaponDamageType(flailedType);
			break;
		case Weapon.CLASS_NATURAL:
			weapon.setWeaponDamageType(naturalType);
			break;
		case Weapon.CLASS_BLUNT:
		case Weapon.CLASS_HAMMER:
		case Weapon.CLASS_STAFF:
			weapon.setWeaponDamageType(Weapon.TYPE_BASHING);
			break;
		}
	}

	protected void setRideBasis(Rideable rideable, String type)
	{
		final List<String> basises=CMParms.parseAny(type.toUpperCase().trim(), '|', true);
		if(basises.indexOf("CHAIR")>=0)
			rideable.setRideBasis(Rideable.RIDEABLE_SIT);
		else
		if(basises.indexOf("TABLE")>=0)
			rideable.setRideBasis(Rideable.RIDEABLE_TABLE);
		else
		if(basises.indexOf("LADDER")>=0)
			rideable.setRideBasis(Rideable.RIDEABLE_LADDER);
		else
		if(basises.indexOf("ENTER")>=0)
			rideable.setRideBasis(Rideable.RIDEABLE_ENTERIN);
		else
		if(basises.indexOf("BED")>=0)
			rideable.setRideBasis(Rideable.RIDEABLE_SLEEP);
	}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(E==null)
			return false;
		if(!(E instanceof Item))
		{
			if(!quiet)
				commonTell(mob,L("You can't mend @x1.",E.name()));
			return false;
		}
		final Item IE=(Item)E;
		if(!IE.subjectToWearAndTear())
		{
			if(!quiet)
				commonTell(mob,L("You can't mend @x1.",IE.name()));
			return false;
		}
		if(IE.usesRemaining()>=100)
		{
			if(!quiet)
				commonTell(mob,L("@x1 is in good condition already.",IE.name()));
			return false;
		}
		return true;
	}

	protected List<AbilityComponent> getNonStandardComponentRequirements(String woodRequiredStr)
	{
		final List<AbilityComponent> componentsRequirements;
		if(woodRequiredStr==null)
			componentsRequirements=null;
		else
		if(woodRequiredStr.trim().startsWith("("))
		{
			final Map<String, List<AbilityComponent>> H=new TreeMap<String, List<AbilityComponent>>();
			final String error=CMLib.ableComponents().addAbilityComponent("ID="+woodRequiredStr, H);
			if(error!=null)
				Log.errOut(ID(),"Error parsing custom component: "+woodRequiredStr);
			componentsRequirements=H.get("ID");
		}
		else
			componentsRequirements=CMLib.ableComponents().getAbilityComponentMap().get(woodRequiredStr.toUpperCase());
		return componentsRequirements;
	}
	
	public List<Object> getAbilityComponents(MOB mob, String componentID, String doingWhat, int autoGenerate, int[] compData)
	{
		if(autoGenerate>0)
			return new LinkedList<Object>();

		final List<AbilityComponent> componentsRequirements=getNonStandardComponentRequirements(componentID);
		if(componentsRequirements!=null)
		{
			final List<Object> components=CMLib.ableComponents().componentCheck(mob,componentsRequirements, true);
			if(components!=null)
			{
				if(compData != null)
				{
					for(Object o : components)
					{
						if(o instanceof Physical)
							compData[CF_AMOUNT] += ((Physical)o).phyStats().weight();
						if((o instanceof Item)&&(compData[CF_MATERIAL]==0))
						{
							compData[CF_HARDNESS] = RawMaterial.CODES.HARDNESS(((Item)o).material());
							compData[CF_MATERIAL] = ((Item)o).material();
						}
					}
				}
				return components;
			}
			final StringBuffer buf=new StringBuffer("");
			for(int r=0;r<componentsRequirements.size();r++)
				buf.append(CMLib.ableComponents().getAbilityComponentDesc(mob,componentsRequirements.get(r),r>0));
			mob.tell(L("You lack the necessary materials to @x1, the requirements are: @x2.",doingWhat.toLowerCase(),buf.toString()));
			return null;
		}
		return new LinkedList<Object>();
	}

	public Pair<String,Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RCP_FINALNAME ), Integer.valueOf(CMath.s_int(recipe.get( RCP_LEVEL ))));
	}

	public String getComponentDescription(final MOB mob, final List<String> recipe, final int RCP_WOOD)
	{
		final String woodStr = recipe.get(RCP_WOOD);
		if(CMath.isInteger(woodStr))
		{
			int wood=CMath.s_int(woodStr);
			wood=adjustWoodRequired(wood,mob);
			return Integer.toString(wood);
		}
		else
		{
			final List<AbilityComponent> componentsRequirements;
			final String ID=woodStr.toUpperCase().trim();
			if(woodStr.trim().startsWith("("))
			{
				final Map<String, List<AbilityComponent>> H=new TreeMap<String, List<AbilityComponent>>();
				final String error=CMLib.ableComponents().addAbilityComponent("ID="+ID, H);
				if(error!=null)
					return "Error parsing custom component: "+woodStr;
				componentsRequirements=H.get("ID");
			}
			else
				componentsRequirements=CMLib.ableComponents().getAbilityComponentMap().get(ID);
			if(componentsRequirements!=null)
				return CMLib.ableComponents().getAbilityComponentDesc(mob, componentsRequirements);
		}
		return "?";
	}

	protected boolean doLearnRecipe(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		recipeHolder=null;
		if((!(this instanceof ItemCraftor))||(!((ItemCraftor)this).supportsDeconstruction()))
		{
			commonTell(mob,L("You don't know how to learn new recipes with this skill."));
			return false;
		}
		commands=new XVector<String>(commands);
		commands.remove(0);
		if(commands.size()<1)
		{
			commonTell(mob,L("You've failed to specify which item to deconstruct and learn."));
			return false;
		}
		buildingI=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(buildingI == null)
			return false;
		if(buildingI.owner() instanceof Room)
		{
			commonTell(mob,L("You need to pick that up first."));
			return false;
		}
		if(!mayICraft( mob, buildingI ))
		{
			commonTell(mob,L("You can't learn anything about @x1 with @x2.",buildingI.name(mob),name()));
			return false;
		}
		if(!buildingI.amWearingAt( Wearable.IN_INVENTORY ))
		{
			commonTell(mob,L("You need to remove @x1 first.",buildingI.name(mob)));
			return false;
		}
		if((buildingI instanceof Container)&&(((Container)buildingI).hasContent()))
		{
			commonTell(mob,L("You need to empty @x1 first.",buildingI.name(mob)));
			return false;
		}
		recipeHolder=null;
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem( i );
			if((I instanceof Recipe)&&(I.container()==null))
			{
				final Recipe R=(Recipe)I;
				if(((R.getCommonSkillID().length()==0)||(R.getCommonSkillID().equalsIgnoreCase( ID() )))
				&&(R.getTotalRecipePages() > R.getRecipeCodeLines().length))
				{
					recipeHolder=R;
					break;
				}
			}
		}
		if(recipeHolder==null)
		{
			commonTell(mob,L("You need to have either a blank recipe page or book, or one already containing recipes for @x1 that has blank pages.",name()));
			return false;
		}
		activity = CraftingActivity.LEARNING;
		// checking to see if there is enough space (prop_reqcapacity) to make an
		// item is unnecessary, because you must first drop the same weight in materials
		// before you can make the item!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		displayText=L("You are deconstructing @x1",buildingI.name());
		verb=L("deconstructing @x1",buildingI.name());
		messedUp=!proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> start(s) deconstructing and studying <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			final int duration = getDuration(10+buildingI.phyStats().level(),mob,buildingI.phyStats().level(),10);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
	
}
