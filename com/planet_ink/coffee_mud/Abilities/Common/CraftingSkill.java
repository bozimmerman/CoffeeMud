package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2020 Bo Zimmerman

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

	protected LinkedList<String> last25items = new LinkedList<String>();

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

	private static MOB factoryWorkerM = null;

	private static final Set<ViewType> viewFlags = new XHashSet<ViewType>(new ViewType[] {ViewType.BASIC,ViewType.IDENTIFY});

	protected static class CraftParms
	{
		public int autoGenerate=0;
		public Physical givenTarget=null;
		public boolean forceLevels=false;
		public List<Item> results=new Vector<Item>(1);

		public CraftParms(final int autoGenerate, final Physical givenTarget, final boolean forceLevels)
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
		ADVNCRAFT("ADVN",ExpertiseLibrary.Flag.LEVEL),
		LITECRAFT("LITE",ExpertiseLibrary.Flag.X1),
		DURACRAFT("DURA",ExpertiseLibrary.Flag.X2),
		QUALCRAFT("QUAL",ExpertiseLibrary.Flag.X3),
		LTHLCRAFT("LTHL",ExpertiseLibrary.Flag.X4),
		CNTRCRAFT("CNTR",ExpertiseLibrary.Flag.X5),
		FORTCRAFT("FORT",ExpertiseLibrary.Flag.X4),
		IMBUCRAFT("IMBU",ExpertiseLibrary.Flag.X4),
		VIGOCRAFT("VIGO",ExpertiseLibrary.Flag.X4),
		;

		public final String stageKey;
		public final ExpertiseLibrary.Flag flag;

		private EnhancedExpertise(final String stageKey, final ExpertiseLibrary.Flag flag)
		{
			this.stageKey = stageKey;
			this.flag = flag;
		}

	}

	public String parametersFile()
	{
		return "";
	}

	public double getItemWeightMultiplier(final boolean bundling)
	{
		return 1.0;
	}

	public int getStandardWeight(final int baseWoodRequired, final int otherResourceCd, final boolean bundling)
	{
		int newWeight=(int)Math.round( baseWoodRequired * this.getItemWeightMultiplier( bundling ));
		if(otherResourceCd > 0)
			newWeight += 1;
		if((baseWoodRequired>0) && (newWeight<=0))
			return 1;
		return newWeight;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<List<String>> loadRecipes(final String filename)
	{
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS).text();
			V=loadList(str);
			Collections.sort(V,new Comparator<List<String>>()
			{
				@Override
				public int compare(final List<String> o1, final List<String> o2)
				{
					if(o1.size()<=RCP_LEVEL)
						return -1;
					if(o2.size()<=RCP_LEVEL)
						return 1;
					final int level1=CMath.s_int(o1.get(RCP_LEVEL));
					final int level2=CMath.s_int(o2.get(RCP_LEVEL));
					return (level1>level2)?1:(level1<level2)?-1:0;
				}
			});
			if((V.size()==0)
			&&(!ID().equals("GenCraftSkill"))
			&&(!ID().endsWith("Costuming")))
				Log.errOut(ID(),"Recipes not found!");
			V=new ReadOnlyList<List<String>>(V);
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}

	protected String determineFinalResourceName(final int backupMaterial, final MaterialLibrary.DeadResourceRecord res1, final MaterialLibrary.DeadResourceRecord res2)
	{
		if((res1 != null)&&(res1.subType.length()>0))
			return res1.subType.toLowerCase();
		if((res2 != null)&&(res2.subType.length()>0))
			return res2.subType.toLowerCase();
		if((res1!=null)&&(res1.resCode>=0))
			return RawMaterial.CODES.NAME(res1.resCode).toLowerCase();
		if((res2!=null)&&(res2.resCode>=0))
			return RawMaterial.CODES.NAME(res2.resCode).toLowerCase();
		return RawMaterial.CODES.NAME(backupMaterial).toLowerCase();
	}

	protected String determineFinalName(final String thisStr, final int backupMaterial, final MaterialLibrary.DeadResourceRecord res1, final MaterialLibrary.DeadResourceRecord res2)
	{
		final String resourceName = this.determineFinalResourceName(backupMaterial, res1, res2);
		return replacePercent(thisStr, resourceName).toLowerCase();
	}

	protected String determineDescription(final String name, final int backupMaterial, final MaterialLibrary.DeadResourceRecord res1, final MaterialLibrary.DeadResourceRecord res2)
	{
		final String resourceName = this.determineFinalResourceName(backupMaterial, res1, res2);
		if(name.toLowerCase().indexOf(resourceName) >= 0)
			return name+".  ";
		return L("@x1 made from @x2. ", name, resourceName);
	}

	@Override
	protected List<List<String>> addRecipes(final MOB mob, final List<List<String>> recipes)
	{
		if(mob==null)
			return recipes;
		return super.addRecipes(mob, recipes);
	}

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

	protected void messedUpCrafting(final MOB mob)
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

	protected int getBuildingMaterial(final int woodRequired, final int[][] foundData, final int[] compData)
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

	protected int adjustWoodRequired(final int woodRequired, final MOB mob)
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

	protected String cleanBuildingNameForXP(final MOB mob, final String name)
	{
		return name;
	}

	protected void setMsgXPValue(final MOB mob, final CMMsg msg)
	{
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
				final LinkedList<String> localLast25Items = mySkill.last25items;
				final String buildingIName = cleanBuildingNameForXP(mob,buildingI.Name().toUpperCase());
				int lastBaseDuration = this.lastBaseDuration;
				if(lastBaseDuration > 75)
					lastBaseDuration = 75;
				final double baseXP = lastBaseDuration * levelXPFactor / 25.0;
				double xp = lastBaseDuration * levelXPFactor / 25.0;
				for(final String s : localLast25Items)
				{
					if(s.equals(buildingIName))
						xp -= (baseXP * 0.25);
				}
				if(localLast25Items.size()==5)
					localLast25Items.removeFirst();
				localLast25Items.addLast(buildingIName);
				if(xp > 0.0)
					msg.setValue((int)Math.round(xp));
			}
		}
	}

	@Override
	protected boolean dropAWinner(final MOB mob, final Item buildingI)
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
			setMsgXPValue(mob,msg);
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

	protected void addOtherThings(final PhysicalAgent P, final List<CMObject> otherThings)
	{
		if(otherThings == null)
			return;

		for(final CMObject O : otherThings)
		{
			if(O instanceof Ability)
			{
				final Ability A=(Ability)O;
				if((!A.canBeUninvoked())
				&&(P.fetchEffect(A.ID())==null))
				{
					final Ability A2=(Ability)A.copyOf();
					P.addNonUninvokableEffect(A2);
				}
			}
			else
			if(O instanceof Behavior)
			{
				final Behavior B=(Behavior)O;
				if(P.fetchBehavior(B.ID())==null)
				{
					final Behavior B2=(Behavior)B.copyOf();
					B2.setParms(B.getParms());
					P.addBehavior(B2);
				}
			}
		}
	}

	protected void addSpellsOrBehaviors(final PhysicalAgent P, String spells, final List<CMObject> otherSpells1, final List<CMObject> otherSpells2)
	{
		if(spells.equalsIgnoreCase("bundle"))
			return;
		if(otherSpells1 != null)
			addOtherThings(P,otherSpells1);
		if(otherSpells2 != null)
			addOtherThings(P,otherSpells2);
		if(spells.length()==0)
			return;
		if(spells.startsWith("*") && spells.endsWith(";__DELETE__"))
		{
			String ableID=spells.substring(1, spells.indexOf(';'));
			Ability oldA=P.fetchEffect(ableID);
			Behavior oldB=P.fetchBehavior(ableID);
			if(oldA!=null)
			{
				oldA.unInvoke();
				P.delEffect(oldA);
				spells="";
			}
			else
			if(oldB!=null)
			{
				P.delBehavior(oldB);
				spells="";
			}
			else
			{
				ableID=ableID.toLowerCase();
				for(final Enumeration<Ability> eA = P.effects();eA.hasMoreElements();)
				{
					oldA=eA.nextElement();
					if((oldA!=null) && (oldA.text().toLowerCase().indexOf(ableID)>=0))
					{
						oldA.unInvoke();
						P.delEffect(oldA);
						spells="";
						break;
					}
				}
				if(spells.length()>0)
				{
					for(final Enumeration<Behavior> eB = P.behaviors();eB.hasMoreElements();)
					{
						oldB=eB.nextElement();
						if((oldB!=null) && (oldB.getParms().toLowerCase().indexOf(ableID)>=0))
						{
							P.delBehavior(oldB);
							spells="";
							break;
						}
					}
				}
			}
		}
		else
		{
			final List<CMObject> V=CMLib.coffeeMaker().getCodedSpellsOrBehaviors(spells);
			for(final CMObject O : V)
			{
				if(O instanceof Ability)
				{
					final Ability A=(Ability)O;
					if(P instanceof Wand)
					{
						if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
						||(((Wand)P).getSpell()!=null))
							P.addNonUninvokableEffect(A);
						else
							((Wand)P).setSpell(A);
					}
					else
					if(P instanceof SpellHolder)
					{
						if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
							P.addNonUninvokableEffect(A);
						else
						if(((SpellHolder)P).getSpells().size()==0)
							((SpellHolder)P).setSpellList(A.ID()+((A.text().length()==0)?"":("("+A.text()+")")));
						else
							((SpellHolder)P).setSpellList(((SpellHolder)P).getSpellList()+";"+A.ID()+((A.text().length()==0)?"":("("+A.text()+")")));
					}
					else
						P.addNonUninvokableEffect(A);
				}
				else
				if(O instanceof Behavior)
				{
					final Behavior B=(Behavior)O;
					P.addBehavior(B);
				}
			}
		}
	}

	protected void setWearLocation(final Item I, final String wearLocation, final int hardnessMultiplier)
	{
		final short[] layerAtt = new short[1];
		final short[] layers = new short[1];
		if(I instanceof Armor)
		{
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
		else
		{
			final long[] wornLoc = new long[1];
			final boolean[] logicalAnd = new boolean[1];
			final double[] hardBonus=new double[]{1};
			CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,wearLocation);
			I.setRawLogicalAnd(logicalAnd[0]);
			I.setRawProperLocationBitmap(wornLoc[0]);
		}
	}

	protected static class FoundResourceData
	{
		public int resourceCode = -1;
		public int resourceAmt = 0;
		public RawMaterial resourceFound = null;
	}

	protected static class FoundResources
	{
		public FoundResourceData main = new FoundResourceData();
		public FoundResourceData other = new FoundResourceData();
	}

	protected static final int FOUND_CODE=0;
	protected static final int FOUND_AMT=1;
	protected static final int FOUND_SUB=2;

	public List<List<String>> fetchRecipes()
	{
		return loadRecipes();
	}

	protected List<List<String>> loadRecipes()
	{
		return new Vector<List<String>>();
	}

	protected int[][] fetchFoundResourceData(final MOB mob,
											 int req1Required,
											 String req1Desc, final int[] req1,
											 int req2Required,
											 String req2Desc, final int[] req2,
											 final boolean bundle,
											 final int autoGeneration,
											 final PairVector<EnhancedExpertise,Integer> eduMods)
	{
		final int[][] data=new int[2][3];
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
			data[0][FOUND_SUB]="".hashCode();
			data[1][FOUND_SUB]="".hashCode();
			return data;
		}

		RawMaterial firstWood=null;
		RawMaterial firstOther=null;
		if(req1!=null)
		{
			for (final int element : req1)
			{
				if((element&RawMaterial.RESOURCE_MASK)==0)
					firstWood=CMLib.materials().findMostOfMaterial(mob.location(),element);
				else
					firstWood=CMLib.materials().findFirstResource(mob.location(),element);
				if(firstWood!=null)
				{
					if(firstWood.getSubType().equals(RawMaterial.ResourceSubType.SEED.name()))
						firstWood=null;
					else
						break;
				}
			}
		}
		else
		if(req1Desc!=null)
			firstWood=CMLib.materials().fetchFoundOtherEncoded(mob.location(),req1Desc);
		data[0][FOUND_AMT]=0;
		if(firstWood!=null)
		{
			data[0][FOUND_AMT]=CMLib.materials().findNumberOfResource(mob.location(),firstWood);
			data[0][FOUND_CODE]=firstWood.material();
			data[0][FOUND_SUB]=firstWood.getSubType().hashCode();
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
				{
					if(firstOther.getSubType().equals(RawMaterial.ResourceSubType.SEED.name()))
						firstOther=null;
					else
						break;
				}
			}
		}
		else
		if(req2Desc!=null)
			firstOther=CMLib.materials().fetchFoundOtherEncoded(mob.location(),req2Desc);
		data[1][FOUND_AMT]=0;
		if(firstOther!=null)
		{
			data[1][FOUND_AMT]=CMLib.materials().findNumberOfResource(mob.location(),firstOther);
			data[1][FOUND_CODE]=firstOther.material();
			data[1][FOUND_SUB]=firstOther.getSubType().hashCode();
		}
		if(req1Required>0)
		{
			if(data[0][FOUND_AMT]==0)
			{
				if(req1Desc!=null)
				{
					final int x=req1Desc.indexOf('(');
					if((x>0)&&(req1Desc.endsWith(")")))
					{
						final String subType=req1Desc.substring(x+1,req1Desc.length()-1);
						final int rscCode=RawMaterial.CODES.FIND_IgnoreCase(req1Desc.substring(0,x));
						final String rscName=CMLib.materials().makeResourceSimpleName(rscCode, subType);
						if(rscName!=null)
						{
							commonTell(mob,L("There is no @x1 here to make anything from!  It might need to be put down first.",rscName));
							return null;
						}
					}
					commonTell(mob,L("There is no @x1 here to make anything from!  It might need to be put down first.",req1Desc.toLowerCase()));
				}
				else
				if((req1!=null)&&(req1.length>0))
				{
					final int rscCode=req1[0];
					final String rscName=CMLib.materials().makeResourceSimpleName(rscCode, "");
					if(rscName!=null)
					{
						commonTell(mob,L("There is no @x1 here to make anything from!  It might need to be put down first.",rscName));
						return null;
					}
				}
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
					{
						final int x=req2Desc.indexOf('(');
						if((x>0)&&(req2Desc.endsWith(")")))
						{
							final String subType=req2Desc.substring(x+1,req2Desc.length()-1);
							final int rscCode=RawMaterial.CODES.FIND_IgnoreCase(req2Desc.substring(0,x));
							final String rscName=CMLib.materials().makeResourceSimpleName(rscCode, subType);
							if(rscName!=null)
							{
								commonTell(mob,L("There is no @x1 here to make anything from!  It might need to be put down first.",rscName));
								return null;
							}
						}
						commonTell(mob,L("You need some @x1 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",req2Desc.toLowerCase()));
					}
					return null;
				}
			}
			if(!bundle)
				req2Required=fixResourceRequirement(data[1][FOUND_CODE],req2Required);
		}

		if(req1Required>data[0][FOUND_AMT])
		{
			String req1MatName=RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase();
			if((firstWood != null)&&(firstWood.getSubType().length()>0))
				req1MatName=firstWood.getSubType().toLowerCase();
			if(req1Required>1)
				commonTell(mob,L("You need a @x1 pound bundle of @x2 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",""+req1Required,req1MatName));
			else
				commonTell(mob,L("You need a pound of @x2 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",""+req1Required,req1MatName));
			return null;
		}
		data[0][FOUND_AMT]=req1Required;
		if((req2Required>0)&&(req2Required>data[1][FOUND_AMT]))
		{
			String req2MatName=RawMaterial.CODES.NAME(data[1][FOUND_CODE]).toLowerCase();
			if((firstOther != null)&&(firstOther.getSubType().length()>0))
				req2MatName=firstOther.getSubType().toLowerCase();
			if(req2Required>1)
				commonTell(mob,L("You need a @x1 pound bundle of @x2 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",""+req2Required,req2MatName));
			else
				commonTell(mob,L("You need a pound of @x2 to make that.  There is not enough here.  Are you sure you set it all on the ground first?",""+req2Required,req2MatName));
			return null;
		}
		data[1][FOUND_AMT]=req2Required;
		return data;
	}

	protected void randomRecipeFix(final MOB mob, final List<List<String>> recipes, final List<String> commands, final int autoGeneration)
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

	public ItemKeyPair craftAnyItem(final int material)
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
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int autoGenerate, final boolean forceLevels, final List<Item> crafted)
	{
		return false;
	}

	public ItemKeyPair craftItem(final String recipeName, final int material, final boolean forceLevels, final boolean noSafety)
	{
		if(factoryWorkerM==null)
		{
			factoryWorkerM=CMClass.getMOB("StdMOB");
			factoryWorkerM.setName(L("somebody"));
			factoryWorkerM.setLocation(CMLib.map().getRandomRoom());
			factoryWorkerM.basePhyStats().setLevel(Integer.MAX_VALUE/2);
			factoryWorkerM.basePhyStats().setSensesMask(factoryWorkerM.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
			factoryWorkerM.recoverPhyStats();
		}
		if(noSafety)
			factoryWorkerM.setAttribute(Attrib.SYSOPMSGS, true);
		factoryWorkerM.resetToMaxState();
		return craftItem(factoryWorkerM,new XVector<String>(recipeName),material,forceLevels);
	}

	protected boolean isThereANonBundleChoice(final List<String> recipes)
	{
		for(final String s : recipes)
		{
			if(s.toLowerCase().indexOf("bundle")<0)
				return true;
		}
		return false;
	}

	public ItemKeyPair craftItem(final MOB mob, final List<String> recipes, int material, final boolean forceLevels)
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
		while(((building==null)
			||(building.name().endsWith(" bundle")&&(!isThereANonBundleChoice(recipes))))
		&&(((++tries)<100)))
		{
			final List<Item> V=new ArrayList<Item>(1);
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
		if(!(building instanceof RawMaterial))
			building.setSecretIdentity("");
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();
		if(key!=null)
		{
			if(!(key instanceof RawMaterial))
				key.setSecretIdentity("");
			key.recoverPhyStats();
			key.text();
			key.recoverPhyStats();
		}
		return new ItemKeyPair(building, key);
	}

	public List<ItemKeyPair> craftAllItemSets(final int material, final boolean forceLevels)
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

	protected Set<ViewType> viewFlags()
	{
		return viewFlags;
	}

	public boolean checkInfo(final MOB mob, final List<String> commands)
	{
		if((commands!=null)
		&&(commands.size()>1)
		&&(commands.get(0).equalsIgnoreCase("info")))
		{
			final List<String> recipe = new XVector<String>(commands);
			recipe.remove(0);
			final String recipeName = CMParms.combine(recipe);
			List<Integer> rscs=myResources();
			if(rscs.size()==0)
				rscs=new XVector<Integer>(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
			final int material;
			if(((rscs.get(0).intValue()&RawMaterial.RESOURCE_MASK)>0)
			&&((rscs.get(0).intValue()&RawMaterial.MATERIAL_MASK)>0))
				material = rscs.get(0).intValue();
			else
			{
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
			}
			final List<List<String>> recipes=addRecipes(mob,loadRecipes());
			final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
			if(matches.size()>0)
			{
				for(int i=matches.size()-1;i>=0;i--)
				{
					final int level = CMath.s_int(matches.get(i).get(RCP_LEVEL));
					if(level>xlevel(mob))
						matches.remove(i);
				}
			}
			if(matches.size() == 0)
			{
				commonTell(mob,L("You don't know how to make anything called '@x1'",recipeName));
			}
			else
			{
				final ItemKeyPair pair = craftItem(mob,recipe,material,false);
				if(pair == null)
				{
					commonTell(mob,L("You don't know how to make anything called '@x1'",recipeName));
				}
				else
				{
					final String viewDesc = CMLib.coffeeShops().getViewDescription(mob, pair.item, viewFlags());
					commonTell(mob,viewDesc);
					if(viewDesc.length()>0)
						commonTell(mob,L("* The material type is an example only."));
					pair.item.destroy();
					if(pair.key!=null)
						pair.key.destroy();
				}
			}
			return true;
		}
		return false;
	}

	public ItemKeyPair craftItem(final String recipeName)
	{
		List<Integer> rscs=myResources();
		if(rscs.size()==0)
			rscs=new XVector<Integer>(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
		final int material=rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue();
		return craftItem(recipeName,material,false, false);
	}

	public List<ItemKeyPair> craftAllItemSets(final boolean forceLevels)
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

	public List<List<String>> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		return matchingRecipeNames(fetchRecipes(),recipeName,beLoose);
	}

	protected boolean supportsWeapons()
	{
		return false;
	}

	protected boolean supportsArmors()
	{
		return false;
	}

	protected List<List<String>> matchingRecipeNames(final List<List<String>> recipes, String recipeName, final boolean beLoose)
	{
		final List<List<String>> matches=new Vector<List<String>>();
		if(recipeName.length()==0)
			return matches;
		int selNum=-1;
		final int lastPeriodDex = recipeName.lastIndexOf('.');
		if(lastPeriodDex > 0)
		{
			if(CMath.isInteger(recipeName.substring(lastPeriodDex+1)))
			{
				selNum=CMath.s_int(recipeName.substring(lastPeriodDex+1));
				recipeName=recipeName.substring(0,lastPeriodDex);
			}
			else
			if(CMath.isInteger(recipeName.substring(0,lastPeriodDex))
			&&(lastPeriodDex<recipeName.length()-1))
			{
				selNum=CMath.s_int(recipeName.substring(0,lastPeriodDex));
				recipeName=recipeName.substring(lastPeriodDex+1);
			}
		}
		if(matches.size()==0)
		{
			if(CMath.isInteger(recipeName))
			{
				int level=CMath.s_int(recipeName);
				if(level < 0)
				{
					level *= -1;
					for(int r=0;r<recipes.size();r++)
					{
						final List<String> V=recipes.get(r);
						if(V.size()>0)
						{
							final int rcpLevel = CMath.s_int(V.get(RCP_LEVEL));
							if(rcpLevel <= level)
								matches.add(V);
						}
					}
				}
				else
				{
					for(int r=0;r<recipes.size();r++)
					{
						final List<String> V=recipes.get(r);
						if(V.size()>0)
						{
							final int rcpLevel = CMath.s_int(V.get(RCP_LEVEL));
							if(rcpLevel == level)
								matches.add(V);
						}
					}
				}
			}
			else
			if((recipeName.length()>1)
			&&(recipeName.endsWith("+")||recipeName.endsWith("-"))
			&&(CMath.isInteger(recipeName.substring(0,recipeName.length()-1).trim())))
			{
				final int level=CMath.s_int(recipeName.substring(0,recipeName.length()-1).trim());
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					if(V.size()>0)
					{
						final int rcpLevel = CMath.s_int(V.get(RCP_LEVEL));
						if(rcpLevel >= level)
							matches.add(V);
					}
				}
			}
			else
			if((recipeName.length()>2)
			&&(recipeName.indexOf('-')>0)
			&&(CMath.isInteger(recipeName.substring(0,recipeName.indexOf('-')).trim()))
			&&(CMath.isInteger(recipeName.substring(recipeName.indexOf('-')+1).trim())))
			{
				final int mlevel=CMath.s_int(recipeName.substring(0,recipeName.indexOf('-')).trim());
				final int xlevel=CMath.s_int(recipeName.substring(recipeName.indexOf('-')+1).trim());
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					if(V.size()>0)
					{
						final int rcpLevel = CMath.s_int(V.get(RCP_LEVEL));
						if((rcpLevel >= mlevel)
						&&(rcpLevel <= xlevel))
							matches.add(V);
					}
				}
			}
		}
		if(matches.size()==0)
		{
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
		}
		if(matches.size()==0)
		{
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=V.get(RCP_FINALNAME);
					if((((replacePercent(item,"").toUpperCase()+" ").startsWith(recipeName.toUpperCase())))
					||((" "+replacePercent(item,"").toUpperCase()+" ").indexOf(" "+recipeName.toUpperCase()+" ")>=0))
						matches.add(V);
				}
			}
		}
		if(beLoose && (matches.size()==0))
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
			if(matches.size()==0)
			{
				final Vector<String> rn=CMParms.parse(recipeName);
				final String lastWord=rn.lastElement();
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
				if((matches.size()>1)&&(rn.size()>1))
				{
					final String firstWord=rn.firstElement();
					final List<List<String>> otherMatches=new XVector<List<String>>();
					if(firstWord.length()>1)
					{
						for(int r=0;r<matches.size();r++)
						{
							final List<String> V=matches.get(r);
							if(V.size()>0)
							{
								final String item=V.get(RCP_FINALNAME);
								if((replacePercent(item,"").toUpperCase().indexOf(firstWord.toUpperCase())>=0)
								||(firstWord.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
									otherMatches.add(V);
							}
						}
						if(otherMatches.size()<matches.size())
						{
							matches.clear();
							matches.addAll(otherMatches);
						}
					}
				}
			}
		}

		if(supportsWeapons() && (matches.size()==0))
		{
			int x=CMParms.indexOf(Weapon.CLASS_DESCS,recipeName.toUpperCase().trim());
			if(x>=0)
			{
				final String weaponClass = Weapon.CLASS_DESCS[x];
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					if(V.contains(weaponClass))
						matches.add(V);
				}
				return matches;
			}
			x=CMParms.indexOf(Weapon.TYPE_DESCS,recipeName.toUpperCase().trim());
			if(x>=0)
			{
				final String weaponType = Weapon.TYPE_DESCS[x];
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					if(V.contains(weaponType))
						matches.add(V);
				}
			}
		}

		if(supportsArmors() && (matches.size()==0))
		{
			final long code=Wearable.CODES.FIND_ignoreCase(recipeName.toUpperCase().trim());
			if(code > 0)
			{
				final String wearLoc = Wearable.CODES.NAMEUP(code);
				for(int r=0;r<recipes.size();r++)
				{
					final List<String> V=recipes.get(r);
					for(int v=0;v<V.size();v++)
					{
						if(V.get(v).toUpperCase().indexOf(wearLoc)>=0)
						{
							matches.add(V);
							break;
						}
					}
				}
			}
		}

		if(selNum>0)
		{
			final List<List<String>> newMatches=new Vector<List<String>>();
			if(selNum<=matches.size())
				newMatches.add(matches.get(selNum-1));
			return newMatches;
		}
		return matches;
	}

	protected Vector<Item> getAllMendable(final MOB mob, final Environmental from, final Item contained)
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

	public boolean publicScan(final MOB mob, final List<String> commands)
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
		final List<Item> allStuff=getAllMendable(mob,scanning,null);
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
			final Item I=allStuff.get(i);
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

	protected boolean deconstructRecipeInto(final MOB mob, final Item I, final Recipe R)
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
			final CMMsg msg=CMClass.getMsg(mob,I,this,CMMsg.TYP_RECIPELEARNED|CMMsg.MASK_ALWAYS,null);
			setMsgXPValue(mob,msg);
			if((mob!=null)
			&&(mob.location()!=null)
			&&(mob.location().okMessage(mob, msg)))
			{
				mob.location().send(mob, msg);
				existingRecipes.add(CMLib.ableParms().makeRecipeFromItem(C, I));
				R.setRecipeCodeLines(existingRecipes.toArray(new String[0]));
				R.setCommonSkillID( ID() );
				return true;
			}
		}
		catch(final CMException cme)
		{
			Log.errOut("CraftingSkill",cme.getMessage());
		}
		return false;
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
		@SuppressWarnings("unchecked")
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

	protected void setWeaponTypeClass(final Weapon weapon, final String weaponClass)
	{
		setWeaponTypeClass(weapon,weaponClass,Weapon.TYPE_BASHING,Weapon.TYPE_BASHING);
	}

	protected void setWeaponTypeClass(final Weapon weapon, final String weaponClass, final int flailedType)
	{
		setWeaponTypeClass(weapon,weaponClass,flailedType,Weapon.TYPE_BASHING);
	}

	protected void setWeaponTypeClass(final Weapon weapon, final String weaponClass, final int flailedType, final int naturalType)
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

	protected void setRideBasis(final Rideable rideable, final String type)
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

	protected boolean canMend(final MOB mob, final Environmental E, final boolean quiet)
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

	protected List<AbilityComponent> getNonStandardComponentRequirements(final String woodRequiredStr, final int adjustAmounts)
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
		if(adjustAmounts<=0)
			return componentsRequirements;
		if(componentsRequirements == null)
			return null;
		final List<AbilityComponent> newSet = new XVector<AbilityComponent>();
		for(final AbilityComponent A : componentsRequirements)
		{
			final AbilityComponent newA = (AbilityComponent)A.copyOf();
			newA.setAmount(newA.getAmount() * adjustAmounts);
			newSet.add(newA);
		}
		return newSet;
	}

	public List<Object> getAbilityComponents(final MOB mob, final String componentID, final String doingWhat, final int autoGenerate, final int[] compData, final int adjustAmounts)
	{
		if(autoGenerate>0)
			return new LinkedList<Object>();

		final List<AbilityComponent> componentsRequirements=getNonStandardComponentRequirements(componentID, adjustAmounts);
		if(componentsRequirements!=null)
		{
			final List<Object> components=CMLib.ableComponents().componentCheck(mob,componentsRequirements, true);
			if(components!=null)
			{
				if(compData != null)
				{
					for(final Object o : components)
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
			{
				String str=CMLib.ableComponents().getAbilityComponentDesc(mob,componentsRequirements.get(r),r>0);
				str=CMStrings.replaceAll(str,L(" on the ground"), ""); // this is implied
				buf.append(str);
			}
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
				return CMStrings.replaceAll(
						CMLib.ableComponents().getAbilityComponentDesc(mob, componentsRequirements),
						L(" on the ground"),
						"");
		}
		return "?";
	}

	protected boolean mayILearnToCraft(final MOB mob, final Item I)
	{
		return mayICraft(mob,I);
	}

	protected boolean doLearnRecipe(final MOB mob, List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
		buildingI=getTargetItemFavorMOB(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(buildingI == null)
			return false;
		if(buildingI.owner() instanceof Room)
		{
			commonTell(mob,L("You need to pick that up first."));
			return false;
		}
		if((!mayILearnToCraft( mob, buildingI ))
		||(this.getBrand(buildingI).length()>0))
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
		for(final String codeLines : recipeHolder.getRecipeCodeLines())
		{
			final int x=codeLines.indexOf('\t');
			if(x >= 0)
			{
				final String name=this.replacePercent(codeLines.substring(0,x),"").trim();
				if(buildingI.Name().indexOf(name)>=0)
				{
					commonTell(mob,L("You appear to already have that recipe written down here."));
					return false;
				}
			}
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
