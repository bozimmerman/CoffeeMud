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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2012 Bo Zimmerman

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
	public String ID() { return "CraftingSkill"; }
	public String name(){ return "Crafting Skill";}
	public int classificationCode(){return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CRAFTINGSKILL;}
	public String accountForYourself(){return name()+" requires: "+supportedResourceString();}
	protected Item building=null;
	protected Recipe recipeHolder = null;
	protected boolean fireRequired=true;
	protected enum CraftingActivity { CRAFTING, MENDING, LEARNING, REFITTING }
	protected CraftingActivity activity = CraftingActivity.CRAFTING;
	protected boolean messedUp=false;

	// common recipe definition indexes
	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;

	public CraftingSkill(){super();}

	public String parametersFile(){ return "";}
	
	public double getItemWeightMultiplier(boolean bundling)
	{
		return 1.0;
	}
	
	public int getStandardWeight(int baseWoodRequired, boolean bundling)
	{
		int newWeight=(int)Math.round( (double)baseWoodRequired * this.getItemWeightMultiplier( bundling ));
		if((baseWoodRequired>0) && (newWeight<=0))
			return 1;
		return newWeight;
	}
	
	protected String replacePercent(String thisStr, String withThis)
	{
		if(withThis.length()==0)
		{
			int x=thisStr.indexOf("% ");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf(" %");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf('%');
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		else
		{
			int x=thisStr.indexOf('%');
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		return thisStr;
	}

	protected void messedUpCrafting(MOB mob)
	{
		if(building!=null)
		{
			if(building.usesRemaining()<100)
			{
				if(building.usesRemaining()>90)
					building.setUsesRemaining(building.usesRemaining()+1);
				else
				if(building.usesRemaining()>80)
					building.setUsesRemaining(building.usesRemaining()+3);
				else
				if(building.usesRemaining()>70)
					building.setUsesRemaining(building.usesRemaining()+5);
				else
				if(building.usesRemaining()>60)
					building.setUsesRemaining(building.usesRemaining()+7);
				else
					building.setUsesRemaining(building.usesRemaining()+10);
			}
			commonEmote(mob,"<S-NAME> mess(es) up mending "+building.name()+".");
		}
		
	}

	protected long getContainerType(final String s)
	{
		if(s.length()==0) return 0;
		if(CMath.isInteger(s))
			return CMath.s_int(s);
		long ret=0;
		final String[] allTypes=s.split("|");
		for(final String splitS : allTypes)
		{
			int bit=CMParms.indexOf(Container.CONTAIN_DESCS, splitS.toUpperCase().trim());
			if(bit>0) 
				return 0;
			else
				ret = ret | CMath.pow(2,(bit-1));
		}
		return ret;
	}

	protected List<List<String>> addRecipes(MOB mob, List<List<String>> recipes)
	{
		if(mob==null) return recipes;
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
				if(!clonedYet){ recipes=new XVector<List<String>>(recipes); clonedYet=true;}
				StringBuffer allRecipeLines=new StringBuffer("");
				if(((Recipe)I).getRecipeCodeLines().length>0)
				{
					for(String recipeLine : ((Recipe)I).getRecipeCodeLines())
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
							while(V2.size()<lastRecipeV.size()) V2.add("");
							while(V2.size()>lastRecipeV.size()) V2.remove(V2.size()-1);
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

	protected int adjustWoodRequired(int woodRequired, MOB mob) 
	{
		int newWoodRequired=woodRequired-(int)Math.round((0.05*(double)woodRequired*(double)getXPCOSTLevel(mob)));
		if(newWoodRequired<=0)
			if(woodRequired > 0)
				newWoodRequired=1;
			else
				newWoodRequired=0;
		return newWoodRequired;
	}

	protected void dropAWinner(MOB mob, Item building)
	{
		Room R=mob.location();
		if(R==null)
			commonTell(mob,"You are NOWHERE?!");
		else
		if(building==null)
			commonTell(mob,"You have built NOTHING?!!");
		else
		{
			R.addItem(building,ItemPossessor.Expire.Player_Drop);
			R.recoverRoomStats();
			boolean foundIt=false;
			for(int r=0;r<R.numItems();r++)
				if(R.getItem(r)==building)
					foundIt=true;
			if(!foundIt)
			{
				commonTell(mob,"You have won the common-skill-failure LOTTERY! Congratulations!");
				CMLib.leveler().postExperience(mob, null, null,50,false);
			}
		}
	}

	protected void addSpells(Physical P, String spells)
	{
		if(spells.length()==0) return;
		if(spells.equalsIgnoreCase("bundle")) return;
		List<Ability> V=CMLib.ableParms().getCodedSpells(spells);
		for(int v=0;v<V.size();v++)
			P.addNonUninvokableEffect((Ability)V.get(v));
	}

	protected void setWearLocation(Item I, String wearLocation, int hardnessMultiplier)
	{
		short[] layerAtt = null;
		short[] layers = null;
		if(I instanceof Armor) {
			layerAtt = new short[1];
			layers = new short[1];
			long[] wornLoc = new long[1];
			boolean[] logicalAnd = new boolean[1];
			double[] hardBonus=new double[]{(double)hardnessMultiplier};
			CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,wearLocation);
			if(I instanceof Armor) {
				Armor armor = (Armor)I;
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
		List<List<String>> V=new Vector<List<String>>();
		if(str==null) return V;
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
					if(V2.size()>longestList) longestList=V2.size();
					if(V2 instanceof Vector) ((Vector)V2).trimToSize();
					V.add(V2);
					V2=new Vector();
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
			if(V2.size()>longestList) longestList=V2.size();
			V.add(V2);
		}
		for(int v=0;v<V.size();v++)
		{
			V2=(List)V.get(v);
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
			StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,true).text();
			V=loadList(str);
			if((V.size()==0)&&(!ID().equals("GenCraftSkill")))
				Log.errOut(ID(),"Recipes not found!");
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}

	protected static final int FOUND_CODE=0;
	protected static final int FOUND_AMT=1;
	protected int fixResourceRequirement(int resource, int amt)
	{
		if(amt<=0) return amt;
		switch(resource)
		{
		case RawMaterial.RESOURCE_MITHRIL:
			amt=amt/2;
			break;
		case RawMaterial.RESOURCE_ADAMANTITE:
			amt=amt/3;
			break;
		case RawMaterial.RESOURCE_BALSA:
			amt=amt/2;
			break;
		case RawMaterial.RESOURCE_IRONWOOD:
			amt=amt*2;
			break;
		}
		if(amt<=0) amt=1;
		return amt;
	}

	public List<List<String>> fetchRecipes()
	{
		return loadRecipes();
	}

	protected List<List<String>> loadRecipes(){ return new Vector();}

	protected int[][] fetchFoundResourceData(MOB mob,
											 int req1Required,
											 String req1Desc, int[] req1,
											 int req2Required,
											 String req2Desc, int[] req2,
											 boolean bundle,
											 int autoGeneration,
											 DVector eduMods)
	{
		int[][] data=new int[2][2];
		if((req1Desc!=null)&&(req1Desc.length()==0)) req1Desc=null;
		if((req2Desc!=null)&&(req2Desc.length()==0)) req2Desc=null;

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
			for(int i=0;i<req1.length;i++)
			{
				if((req1[i]&RawMaterial.RESOURCE_MASK)==0)
					firstWood=CMLib.materials().findMostOfMaterial(mob.location(),req1[i]);
				else
					firstWood=CMLib.materials().findFirstResource(mob.location(),req1[i]);
				
				if(firstWood!=null) break;
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
			for(int i=0;i<req2.length;i++)
			{
				if((req2[i]&RawMaterial.RESOURCE_MASK)==0)
					firstOther=CMLib.materials().findMostOfMaterial(mob.location(),req2[i]);
				else
					firstOther=CMLib.materials().findFirstResource(mob.location(),req2[i]);
				if(firstOther!=null) break;
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
					commonTell(mob,"There is no "+req1Desc.toLowerCase()+" here to make anything from!  It might need to be put down first.");
				return null;
			}
			if(!bundle) req1Required=fixResourceRequirement(data[0][FOUND_CODE],req1Required);
		}
		if(req2Required>0)
		{
			if(req2Desc != null)
				if(((req2!=null)&&(data[1][FOUND_AMT]==0))
				||((req2==null)&&(req2Desc.length()>0)&&(data[1][FOUND_AMT]==0)))
				{
					if(req2Desc.equalsIgnoreCase("PRECIOUS"))
						commonTell(mob,"You need some sort of precious stones to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
					else
						commonTell(mob,"You need some "+req2Desc.toLowerCase()+" to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
					return null;
				}
			if(!bundle) req2Required=fixResourceRequirement(data[1][FOUND_CODE],req2Required);
		}

		if(req1Required>data[0][FOUND_AMT])
		{
			commonTell(mob,"You need "+req1Required+" pounds of "+RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase()+" to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
			return null;
		}
		data[0][FOUND_AMT]=req1Required;
		if((req2Required>0)&&(req2Required>data[1][FOUND_AMT]))
		{
			commonTell(mob,"You need "+req2Required+" pounds of "+RawMaterial.CODES.NAME(data[1][FOUND_CODE]).toLowerCase()+" to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
			return null;
		}
		data[1][FOUND_AMT]=req2Required;
		return data;
	}

	protected void randomRecipeFix(MOB mob, List<List<String>> recipes, Vector commands, int autoGeneration)
	{
		if(((mob.isMonster()&&(!CMLib.flags().isAnimalIntelligence(mob)))||(autoGeneration>0))
		&&(commands.size()==0)
		&&(recipes!=null)
		&&(recipes.size()>0))
		{
			int tries=0;
			int maxtries=100;
			while((++tries)<maxtries)
			{
				List<String> randomRecipe=recipes.get(CMLib.dice().roll(1,recipes.size(),-1));
				boolean proceed=true;
				if((randomRecipe.size()>1))
				{
					int levelIndex=-1;
					for(int i=1;i<randomRecipe.size();i++)
					{
						if(CMath.isInteger((String)randomRecipe.get(i)))
						{
							levelIndex=i;
							break;
						}
					}
					if((levelIndex>0)
					&&(xlevel(mob)<CMath.s_int((String)randomRecipe.get(levelIndex))))
						proceed=false;
				}
				if((proceed)||(tries==(maxtries-1)))
				{
					commands.addElement(randomRecipe.get(RCP_FINALNAME));
					break;
				}
			}
		}
	}

	public ItemKeyPair craftAnyItem(int material)
	{
		return craftItem(null,material);
	}

	public ItemKeyPair craftItem(String recipeName, int material)
	{
		Item building=null;
		DoorKey key=null;
		int tries=0;
		MOB mob=CMLib.map().getFactoryMOBInAnyRoom();
		mob.basePhyStats().setLevel(Integer.MAX_VALUE/2);
		mob.basePhyStats().setSensesMask(mob.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
		mob.recoverPhyStats();
		while(((building==null)||(building.name().endsWith(" bundle")))&&(((++tries)<100)))
		{
			Vector V=new Vector();
			V.addElement(Integer.valueOf(material));
			if(recipeName!=null) V.addElement(recipeName);
			invoke(mob,V,null,true,-1);
			if((V.size()>0)&&(V.lastElement() instanceof Item))
			{
				if((V.size()>1)&&((V.elementAt(V.size()-2) instanceof DoorKey)))
					key=(DoorKey)V.elementAt(V.size()-2);
				else
					key=null;
				building=(Item)V.lastElement();
			}
			else
				building=null;
		}
		mob.destroy();
		if(building==null) return null;
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

	public List<ItemKeyPair> craftAllItemSets(int material)
	{
		List<ItemKeyPair> allItems=new Vector<ItemKeyPair>();
		List<List<String>> recipes=fetchRecipes();
		Item built=null;
		HashSet<String> usedNames=new HashSet<String>();
		ItemKeyPair pair=null;
		String s=null;
		for(int r=0;r<recipes.size();r++)
		{
			s=(String)((recipes.get(r)).get(RCP_FINALNAME));
			s=replacePercent((String)((recipes.get(r)).get(RCP_FINALNAME)),"").trim();
			pair=craftItem(s,material);
			if(pair==null) continue;
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

	public ItemKeyPair craftItem(String recipeName)
	{
		List<Integer> rscs=myResources();
		if(rscs.size()==0) rscs=new XVector(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
		int material=((Integer)rscs.get(CMLib.dice().roll(1,rscs.size(),-1))).intValue();
		return craftItem(recipeName,material);
	}

	public List<ItemKeyPair> craftAllItemSets()
	{
		List<Integer> rscs=myResources();
		List<ItemKeyPair> allItems=new Vector<ItemKeyPair>();
		List<ItemKeyPair> pairs=null;
		if(rscs.size()==0) rscs=new XVector(Integer.valueOf(RawMaterial.RESOURCE_WOOD));
		for(int r=0;r<rscs.size();r++)
		{
			pairs=craftAllItemSets(((Integer)rscs.get(r)).intValue());
			if((pairs==null)||(pairs.size()==0)) continue;
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
		List<List<String>> matches=new Vector();
		if(recipeName.length()==0) return matches;
		for(int r=0;r<recipes.size();r++)
		{
			List<String> V=recipes.get(r);
			if(V.size()>0)
			{
				String item=(String)V.get(RCP_FINALNAME);
				if(replacePercent(item,"").equalsIgnoreCase(recipeName))
					matches.add(V);
			}
		}
		if(matches.size()>0) return matches;
		for(int r=0;r<recipes.size();r++)
		{
			List<String> V=recipes.get(r);
			if(V.size()>0)
			{
				String item=(String)V.get(RCP_FINALNAME);
				if((replacePercent(item,"").toUpperCase().indexOf(recipeName.toUpperCase())>=0))
					matches.add(V);
			}
		}
		if(matches.size()>0) return matches;
		if(beLoose)
		{
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=(String)V.get(RCP_FINALNAME);
					if((recipeName.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
						matches.add(V);
				}
			}
			if(matches.size()>0) return matches;
			String lastWord=(String)CMParms.parse(recipeName).lastElement();
			if(lastWord.length()>1)
				for(int r=0;r<recipes.size();r++)
				{
					List<String> V=recipes.get(r);
					if(V.size()>0)
					{
						String item=(String)V.get(RCP_FINALNAME);
						if((replacePercent(item,"").toUpperCase().indexOf(lastWord.toUpperCase())>=0)
						||(lastWord.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
							matches.add(V);
					}
				}
		}
		return matches;
	}

	protected Vector getAllMendable(MOB mob, Environmental from, Item contained)
	{
		Vector V=new Vector();
		if(from==null) return V;
		if(from instanceof Room)
		{
			Room R=(Room)from;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.getItem(i);
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
			MOB M=(MOB)from;
			for(int i=0;i<M.numItems();i++)
			{
				Item I=M.getItem(i);
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
				V.addElement(from);
		}
		return V;
	}

	public boolean publicScan(MOB mob, Vector commands)
	{
		String rest=CMParms.combine(commands,1);
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
				commonTell(mob,"You don't see anyone called '"+rest+"' here.");
				return false;
			}
		}
		Vector allStuff=getAllMendable(mob,scanning,null);
		if(allStuff.size()==0)
		{
			if(mob==scanning)
				commonTell(mob,"You don't seem to have anything that needs mending with "+name()+".");
			else
				commonTell(mob,"You don't see anything on "+scanning.name()+" that needs mending with "+name()+".");
			return false;
		}
		StringBuffer buf=new StringBuffer("");
		if(scanning==mob)
			buf.append("The following items could use some "+name()+":\n\r");
		else
			buf.append("The following items on "+scanning.name()+" could use some "+name()+":\n\r");
		for(int i=0;i<allStuff.size();i++)
		{
			Item I=(Item)allStuff.elementAt(i);
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
		return (int)Math.round(((double)((double)(1.0+(double)crafterM.phyStats().level()-(double)I.phyStats().level())
			   /(double)crafterM.phyStats().level())/2.0+0.5)
			*((double)proficiency()/100.0)*((double)proficiency()/100.0)*100.0);
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
		ItemCraftor C=(ItemCraftor)this;
		if(!C.supportsDeconstruction())
			return false;
		if(!C.mayICraft(I))
			return false;
		List<String> existingRecipes=new XVector<String>(R.getRecipeCodeLines());
		if(R.getTotalRecipePages() <=existingRecipes.size())
			return false;
		try
		{
    		existingRecipes.add(CMLib.ableParms().makeRecipeFromItem(C, I));
    		R.setRecipeCodeLines(existingRecipes.toArray(new String[0]));
    		R.setCommonSkillID( ID() );
		}
		catch(CMException cme)
		{
			Log.errOut("CraftingSkill",cme.getMessage());
			return false;
		}
		return true;
	}

	protected boolean mayBeCrafted(final Item I)
	{
		if(I==null) return false;
		if(I instanceof ArchonOnly) return false;
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
			if(I.fetchBehavior( i ) instanceof ScriptingEngine)
				return false;
		if(I.numScripts()>0)
			return false;
		if(CMLib.flags().flaggedBehaviors(I, Behavior.FLAG_POTENTIALLYAUTODEATHING).size()>0)
			return false;
		Item I2=CMClass.getItem(I.ID());
		if(CMLib.flags().isGettable(I)!=CMLib.flags().isGettable(I2))
			return false;
		if(CMLib.flags().isRemovable(I)!=CMLib.flags().isRemovable(I2))
			return false;
		return true;
	}

	public boolean isANativeItem(String name)
	{
		name=CMLib.english().stripPunctuation(name);
		List<String> nameV=CMParms.parse(name.toUpperCase());
		List<List<String>> recipes = this.loadRecipes();
		if(nameV.size()==0) return false;
		TreeSet<String> allExpertiseWords=(TreeSet<String>)Resources.getResource("CRAFTING_SKILL_EXPERTISE_WORDS");
		if(allExpertiseWords == null)
		{
			if (this instanceof EnhancedCraftingSkill)
			{
    			List<ExpertiseLibrary.ExpertiseDefinition> V = ((EnhancedCraftingSkill)this).getAllThisSkillsDefinitions();
    			allExpertiseWords = new TreeSet<String>();
    			for(final ExpertiseLibrary.ExpertiseDefinition def : V )
    			{
    				if(def.data != null)
    					for(String s : def.data)
    						allExpertiseWords.add(s.toUpperCase());
    			}
    			Resources.submitResource("CRAFTING_SKILL_EXPERTISE_WORDS", allExpertiseWords);
			}
			else
				allExpertiseWords=new TreeSet<String>();
		}
		for(List<String> recipe : recipes)
		{
			if(recipe.size() <= RCP_FINALNAME)
				continue;
			String thisOnesName = recipe.get(RCP_FINALNAME);
			List<String> thisOneNameV=CMParms.parse(thisOnesName.toUpperCase());
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
		weapon.setWeaponType(Weapon.TYPE_BASHING);
		for(int cl=0;cl<Weapon.TYPE_DESCS.length;cl++)
		{
			if(weaponClass.equalsIgnoreCase(Weapon.TYPE_DESCS[cl]))
			{
				weapon.setWeaponType(cl);
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
			weapon.setWeaponType(Weapon.TYPE_SLASHING);
			break;
		case Weapon.CLASS_SWORD:
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED:
		case Weapon.CLASS_POLEARM:
		case Weapon.CLASS_RANGED:
		case Weapon.CLASS_THROWN:
			weapon.setWeaponType(Weapon.TYPE_PIERCING);
			break;
		case Weapon.CLASS_FLAILED:
			weapon.setWeaponType(flailedType);
			break;
		case Weapon.CLASS_NATURAL:
			weapon.setWeaponType(naturalType);
			break;
		case Weapon.CLASS_BLUNT:
		case Weapon.CLASS_HAMMER:
		case Weapon.CLASS_STAFF:
			weapon.setWeaponType(Weapon.TYPE_BASHING);
			break;
		}
	}

	protected void setRideBasis(Rideable rideable, String type)
	{
		if(type.equalsIgnoreCase("CHAIR"))
			rideable.setRideBasis(Rideable.RIDEABLE_SIT);
		else
		if(type.equalsIgnoreCase("TABLE"))
			rideable.setRideBasis(Rideable.RIDEABLE_TABLE);
		else
		if(type.equalsIgnoreCase("LADDER"))
			rideable.setRideBasis(Rideable.RIDEABLE_LADDER);
		else
		if(type.equalsIgnoreCase("ENTER"))
			rideable.setRideBasis(Rideable.RIDEABLE_ENTERIN);
		else
		if(type.equalsIgnoreCase("BED"))
			rideable.setRideBasis(Rideable.RIDEABLE_SLEEP);
	}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(E==null) return false;
		if(!(E instanceof Item))
		{
			if(!quiet)
				commonTell(mob,"You can't mend "+E.name()+".");
			return false;
		}
		Item IE=(Item)E;
		if(!IE.subjectToWearAndTear())
		{
			if(!quiet)
				commonTell(mob,"You can't mend "+IE.name()+".");
			return false;
		}
		if(IE.usesRemaining()>=100)
		{
			if(!quiet)
				commonTell(mob,IE.name()+" is in good condition already.");
			return false;
		}
		return true;
	}

	public List<Object> getAbilityComponents(MOB mob, String componentID, String doingWhat, int autoGenerate)
	{
		if(autoGenerate>0) return new LinkedList<Object>();
		
		final List<AbilityComponent> componentsRequirements;
		if(componentID.trim().startsWith("("))
		{
			Map<String, List<AbilityComponent>> H=new TreeMap<String, List<AbilityComponent>>();
			String error=CMLib.ableMapper().addAbilityComponent("ID="+componentID, H);
			if(error!=null)
				Log.errOut(ID(),"Error parsing custom component: "+componentID);
			componentsRequirements=H.get("ID");
		}
		else
			componentsRequirements=(Vector<AbilityComponent>)CMLib.ableMapper().getAbilityComponentMap().get(componentID.toUpperCase());
		if(componentsRequirements!=null)
		{
			List<Object> components=CMLib.ableMapper().componentCheck(mob,componentsRequirements);
			if(components!=null)
			{
				return components;
			}
			StringBuffer buf=new StringBuffer("");
			for(int r=0;r<componentsRequirements.size();r++)
				buf.append(CMLib.ableMapper().getAbilityComponentDesc(mob,componentsRequirements,r));
			mob.tell("You lack the necessary materials to "
					+doingWhat.toLowerCase()
					+", the requirements are: "
					+buf.toString()+".");
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
		final String woodStr = (String)recipe.get(RCP_WOOD);
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
				Map<String, List<AbilityComponent>> H=new TreeMap<String, List<AbilityComponent>>();
				String error=CMLib.ableMapper().addAbilityComponent("ID="+ID, H);
				if(error!=null)
					return "Error parsing custom component: "+woodStr;
				componentsRequirements=H.get("ID");
			}
			else
				componentsRequirements=(Vector<AbilityComponent>)CMLib.ableMapper().getAbilityComponentMap().get(ID);
			if(componentsRequirements!=null)
				return CMLib.ableMapper().getAbilityComponentDesc(mob, componentsRequirements);
		}
		return "?";
	}
	
	protected boolean doLearnRecipe(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		recipeHolder=null;
		if((!(this instanceof ItemCraftor))||(!((ItemCraftor)this).supportsDeconstruction()))
		{
			commonTell(mob,"You don't know how to learn new recipes with this skill.");
			return false;
		}
		commands=new XVector(commands);
		commands.remove(0);
		if(commands.size()<1)
		{
			commonTell(mob,"You've failed to specify which item to deconstruct and learn.");
			return false;
		}
		building=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(building == null)
			return false;
		if(building.owner() instanceof Room)
		{
			commonTell(mob,"You need to pick that up first.");
			return false;
		}
		if(!mayICraft( mob, building ))
		{
			commonTell(mob,"You can't learn anything about "+building.name()+" with "+name()+".");
			return false;
		}
		if(!building.amWearingAt( Item.IN_INVENTORY ))
		{
			commonTell(mob,"You need to remove "+building.name()+" first.");
			return false;
		}
		if((building instanceof Container)&&(((Container)building).getContents().size()>0))
		{
			commonTell(mob,"You need to empty "+building.name()+" first.");
			return false;
		}
		recipeHolder=null;
		for(int i=0;i<mob.numItems();i++)
		{
			Item I=mob.getItem( i );
			if((I instanceof Recipe)&&(I.container()==null))
			{
				Recipe R=(Recipe)I;
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
			commonTell(mob,"You need to have either a blank recipe page or book, or one already containing recipes for "+name()+" that has blank pages.");
			return false;
		}
		activity = CraftingActivity.LEARNING;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		displayText="You are deconstructing "+building.name();
		verb="deconstructing "+building.name();
		messedUp=!proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) deconstructing and studying <T-NAMESELF>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			int duration = getDuration(10+building.phyStats().level(),mob,building.phyStats().level(),10);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
