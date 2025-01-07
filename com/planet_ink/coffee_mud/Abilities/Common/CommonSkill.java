package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Items.Basic.GenDrink;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.CostDef;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class CommonSkill extends StdAbility
{
	@Override
	public String ID()
	{
		return "CommonSkill";
	}

	private final static String localizedName = CMLib.lang().L("Common Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = empty;

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	public String supportedResourceString()
	{
		return "";
	}

	public static final Map<String, Integer[]>	resourcesMap	= new Hashtable<String, Integer[]>();
	protected static Item						fakeFire		= null;
	protected static final List<String>			uninvokeEmpties	= new ReadOnlyList<String>(new ArrayList<String>(0));

	protected volatile Room	activityRoom	= null;
	protected boolean		aborted			= false;
	protected boolean		helping			= false;
	protected boolean		bundling		= false;
	public Ability			helpingAbility	= null;
	protected volatile int	tickUp			= 0;
	protected String		verb			= L("working");
	protected String		playSound		= null;
	protected int			bonusYield		= 0;
	protected volatile int	lastBaseDuration= 0;

	protected int baseYield()
	{
		return 1;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected String displayText = L("(Doing something productive)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected CostDef getRawTrainingCost()
	{
		return CMProps.getCommonSkillGainCost(ID());
	}

	@Override
	protected int iniPracticesToPractice()
	{
		return 1;
	}

	protected boolean allowedWhileMounted()
	{
		return true;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected boolean allowedInTheDark()
	{
		return false;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected List<String> getUninvokeException()
	{
		return uninvokeEmpties;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL;
	}

	protected boolean canBeDoneSittingDown()
	{
		return false;
	}

	protected int getActivityMessageType()
	{
		return canBeDoneSittingDown() ? CMMsg.MSG_HANDS | CMMsg.MASK_SOUND : CMMsg.MSG_NOISYMOVEMENT;
	}

	protected int getCompletedActivityMessageType()
	{
		int activityCode = CMMsg.MASK_HANDS | CMMsg.MASK_SOUND;
		if(!canBeDoneSittingDown())
			activityCode |= CMMsg.MASK_MOVE;
		return activityCode | CMMsg.TYP_ITEMSGENERATED;
	}

	protected String getAlmostDoneMessage()
	{
		final String sound=(playSound!=null)?CMLib.protocol().msp(playSound,10):"";
		return L("<S-NAME> <S-IS-ARE> almost done @x1.@x2",verb,sound);
	}

	public void setTickUp(final int up)
	{
		this.tickUp = up;
	}

	protected String getYouContinueMessage()
	{
		final int total=tickUp+tickDown;
		final int pct=(int)Math.round(CMath.div(tickUp,total)*100.0);
		final String sound=(playSound!=null)?CMLib.protocol().msp(playSound,10):"";
		return L("<S-NAME> continue(s) @x1 (@x2% completed).@x3",verb,""+pct,sound);
	}

	protected String getOthersContinueMessage()
	{
		final String sound=(playSound!=null)?CMLib.protocol().msp(playSound,10):"";
		return L("<S-NAME> continue(s) @x1.@x2",verb,sound);
	}

	@Override
	public int abilityCode()
	{
		return bonusYield;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		bonusYield = newCode;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((myHost instanceof MOB)&&(myHost == this.affected)&&(((MOB)myHost).location()!=null))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource((MOB)myHost))))
			{
				aborted=true;
				unInvoke();
			}
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if((mob.isInCombat())
			||(R!=activityRoom)
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true)))
			{
				aborted=true;
				unInvoke();
				return false;
			}
			if(((!allowedWhileMounted())&&(mob.riding()!=null))
			||((!allowedInTheDark())&&(!CMLib.flags().canBeSeenBy(mob.location(),mob)))
			||((!canBeDoneSittingDown())&&(CMLib.flags().isSitting(mob))))
			{
				aborted=true;
				unInvoke();
				return false;
			}

			if(tickDown==4)
			{
				if(!R.show(mob,null,getActivityMessageType(),getAlmostDoneMessage()))
				{
					aborted=true;
					unInvoke();
					return false;
				}
			}
			else
			if((tickUp%4)==0)
			{
				if(!R.show(mob,null,null,getActivityMessageType(),this.getYouContinueMessage(),null,this.getOthersContinueMessage()))
				{
					aborted=true;
					unInvoke();
					return false;
				}
			}
			if((helping)
			&&(helpingAbility!=null)
			&&(helpingAbility.affecting() instanceof MOB)
			&&(((MOB)helpingAbility.affecting()).isMine(helpingAbility)))
				helpingAbility.tick(helpingAbility.affecting(),tickID);
			if((mob.soulMate()==null)
			&&(mob.playerStats()!=null)
			&&(R!=null)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE)))
				mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}

		final int preTickDown=tickDown;
		if(!super.tick(ticking,tickID))
			return false;
		tickUp+=(preTickDown-tickDown);
		return true;
	}

	// so we can override it on a skill-by-skill basis
	protected List<List<String>> loadList(final StringBuffer str)
	{
		return CMLib.utensils().loadRecipeList(str.toString(), true);
	}

	@SuppressWarnings("unchecked")
	protected List<List<String>> loadRecipes(final String filename)
	{
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			final List<List<String>> frecipes = new ArrayList<List<String>>();
			for(final CMFile F : CMFile.getExistingExtendedFiles(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS))
			{
				final StringBuffer str = F.text();
				frecipes.addAll(loadList(str));
			}
			V=new ReadOnlyList<List<String>>(frecipes);
			if((V.size()==0)
			&&(!ID().equals("GenCraftSkill"))
			&&(!ID().endsWith("Costuming")))
				Log.errOut(ID(),"Recipes not found!");
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked() && (!super.unInvoked))
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				final MOB mob=(MOB)affected;
				final String verb = this.verb;
				if((verb != null)
				&&(verb.length()>0))
				{
					if(aborted)
						mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> stop(s) @x1.",verb));
					else
						mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> <S-IS-ARE> done @x1.",verb));
				}
				helping=false;
				helpingAbility=null;
			}
		}
		super.unInvoke();
	}

	protected int getDuration(final int baseTicks, final MOB mob, final int itemLevel, final int minDuration)
	{
		int ticks=baseTicks;
		final int level=mob.phyStats().level() - itemLevel;
		final double pct=CMath.div(level,CMProps.get(mob.session()).getInt(CMProps.Int.LASTPLAYERLEVEL))*.5;
		ticks-=(int)Math.round(CMath.mul(ticks, pct));

		lastBaseDuration=ticks;
		if(lastBaseDuration<minDuration)
			lastBaseDuration=minDuration;

		final double quickPct = getXTIMELevel(mob) * 0.05;
		ticks-=(int)Math.round(CMath.mul(ticks, quickPct));
		if(ticks<minDuration)
			ticks=minDuration;
		return ticks;
	}

	@Override
	protected int addedTickTime(final MOB invokerMOB, final int baseTickTime)
	{
		// common skills tend to SUBTRACT time -- not add to it!
		return 0;
	}

	protected void setBrand(final MOB mob, final Item buildingI)
	{
		if(buildingI instanceof RawMaterial)
			buildingI.setSecretIdentity(buildingI.name());
		else
			buildingI.setSecretIdentity(getBrand(mob));
	}

	protected String getBrand(final Item buildingI)
	{
		if(buildingI != null)
		{
			final Ability A=buildingI.fetchEffect("Copyright");
			if((A!=null)&&(A.text().length()>0))
				return A.text();
			final int x=buildingI.rawSecretIdentity().indexOf(ItemCraftor.CRAFTING_BRAND_STR_PREFIX);
			if(x>=0)
			{
				final int y=buildingI.rawSecretIdentity().indexOf('.',x+ItemCraftor.CRAFTING_BRAND_STR_PREFIX.length());
				if(y>=0)
				{
					return buildingI.rawSecretIdentity().substring(x,y);
				}
			}
		}
		return "";
	}

	protected String getBrand(final MOB mob)
	{
		if(mob==null)
			return L(ItemCraftor.CRAFTING_BRAND_STR_ANON);
		else
			return L(ItemCraftor.CRAFTING_BRAND_STR_NAME,mob.Name());
	}

	protected void commonEmote(final MOB mob, final String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
			mob.location().show(mob,null,getActivityMessageType()|CMMsg.MASK_ALWAYS,str);
		else
			mob.tell(mob,null,null,str);
	}

	protected boolean dropAWinner(final MOB mob, final Item buildingI)
	{
		return dropAWinner(mob,mob.location(),buildingI);
	}

	/**
	 * Produce a constructed OR gathered thing.
	 * @param mob CAN BE NULL!!! the dropper
	 * @param R the room to drop it in
	 * @param buildingI the item to drop
	 * @return true if it dropped
	 */
	protected boolean dropAWinner(MOB mob, final Room R, final Item buildingI)
	{
		if(R==null)
			commonTelL(mob,"You are NOWHERE?!");
		else
		if(buildingI==null)
			commonTelL(mob,"You have built NOTHING?!!");
		else
		if(mob == null)
		{
			mob=CMClass.getFactoryMOB(R.name(),buildingI.phyStats().level(),R);
			try
			{
				final CMMsg msg=CMClass.getMsg(mob,buildingI,this,CMMsg.TYP_ITEMGENERATED|CMMsg.MASK_ALWAYS,null);
				if(R.okMessage(mob,msg))
				{
					final Item I=(Item)msg.target();
					R.addItem(I,ItemPossessor.Expire.Resource);
					R.recoverRoomStats();
					R.send(mob,msg);
					return R.isContent(I);
				}
			}
			finally
			{
				mob.destroy();
			}
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,buildingI,this,CMMsg.TYP_ITEMGENERATED|CMMsg.MASK_ALWAYS,null);
			if(R.okMessage(mob,msg))
			{
				final Item I=(Item)msg.target();
				R.addItem(I,ItemPossessor.Expire.Resource);
				R.recoverRoomStats();
				mob.location().send(mob,msg);

				if(!R.isContent(I))
				{
					commonTelL(mob,"You have won the common-skill-failure LOTTERY! Congratulations!");
					CMLib.leveler().postExperience(mob, "ABILITY:"+ID(), null,null,50, false);
				}
				else
					return true;
			}
		}
		return false;
	}

	protected int lookingForMat(final List<Integer> materials, final Room fromHere)
	{
		final List<Integer> possibilities=new ArrayList<Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room room=fromHere.getRoomInDir(d);
			final Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				final int material=room.myResource();
				if(materials.contains(Integer.valueOf(material&RawMaterial.MATERIAL_MASK)))
					possibilities.add(Integer.valueOf(d));
			}
		}
		if(possibilities.size()==0)
			return -1;
		return (possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1))).intValue();
	}

	protected int lookingForMat(final int material, final Room fromHere)
	{
		final List<Integer> V=new ArrayList<Integer>(1);
		V.add(Integer.valueOf(material));
		return lookingForMat(V,fromHere);
	}

	protected int lookingForRsc(final List<Integer> materials, final Room fromHere)
	{
		final List<Integer> possibilities=new ArrayList<Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room room=fromHere.getRoomInDir(d);
			final Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				final int material=room.myResource();
				if(materials.contains(Integer.valueOf(material)))
					possibilities.add(Integer.valueOf(d));
			}
		}
		if(possibilities.size()==0)
			return -1;
		return (possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1))).intValue();
	}

	protected int lookingForRsc(final int material, final Room fromHere)
	{
		final List<Integer> V=new ArrayList<Integer>(1);
		V.add(Integer.valueOf(material));
		return lookingForRsc(V,fromHere);
	}

	public Item getRequiredFire(final MOB mob,final int autoGenerate)
	{
		if((autoGenerate>0)
		||((this instanceof CraftingSkill)&&(!((CraftingSkill)this).fireRequired)))
		{
			if(fakeFire != null)
				return fakeFire;
			fakeFire =CMClass.getBasicItem( "StdItem" );
			fakeFire.basePhyStats().setDisposition( fakeFire.basePhyStats().disposition() | PhyStats.IS_GLOWING | PhyStats.IS_LIGHTSOURCE );
			fakeFire.addNonUninvokableEffect( CMClass.getAbility( "Burning" ) );
			fakeFire.recoverPhyStats();
			return fakeFire;
		}
		Item fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I2=mob.location().getItem(i);
			if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTelL(mob,"A fire will need to be built first.");
			return null;
		}
		return fire;
	}

	@Override
	public int[] usageCost(final MOB mob, final boolean ignoreClassOverride)
	{
		final Map<String,int[]> overrideCache=getHardOverrideManaCache();
		if(mob==null)
		{
			if(overrideCache.containsKey(ID()))
				return overrideCache.get(ID());

		}
		if(usageType()==Ability.USAGE_NADA)
			return super.usageCost(mob, ignoreClassOverride);

		final int[][] abilityUsageCache=(mob==null)?new int[Ability.CACHEINDEX_TOTAL][]:mob.getAbilityUsageCache(ID());
		final int myCacheIndex=ignoreClassOverride?Ability.CACHEINDEX_CLASSLESS:Ability.CACHEINDEX_NORMAL;
		final int[] myCache=abilityUsageCache[myCacheIndex];
		final boolean rebuildCache=(myCache==null);
		int consumed;
		int minimum;
		if(!rebuildCache && (myCache!=null ))
		{
			if(myCache.length==3)
				return myCache;
			consumed=myCache[0];
			minimum=myCache[1];
		}
		else
		{
			consumed=25;
			final int lvl;
			final int lowest;
			if(mob != null)
			{
				lvl=CMLib.ableMapper().qualifyingClassLevel(mob,this)+super.getXLOWCOSTLevel(mob);
				lowest=CMLib.ableMapper().qualifyingLevel(mob,this);
			}
			else
			{
				lvl=CMLib.ableMapper().lowestQualifyingLevel(ID());
				lowest=lvl;
			}
			final int diff=lvl-lowest;
			Integer[] costOverrides=null;
			if((!ignoreClassOverride)&&(mob!=null))
				costOverrides=CMLib.ableMapper().getCostOverrides(mob,ID());
			if(diff>0)
			{
				if(diff > 60)
					consumed=5;
				else
				if(diff > 2)
					consumed = consumed - diff/3;
			}
			final int maxOverride;
			final Object maxOverrideCost = CMProps.getManaCostExceptionObject(ID());
			if(maxOverrideCost instanceof Integer)
				maxOverride=((Integer)maxOverrideCost).intValue();
			else
			if(maxOverrideCost instanceof CMath.CompiledFormula)
			{
				final double[] vars = new double[] {
					lowest,
					(mob==null)?lvl:mob.phyStats().level(),
					consumed,
					(mob==null)?lvl:adjustedLevel(mob,0)
				};
				maxOverride=(int)CMath.parseMathExpression((CMath.CompiledFormula)maxOverrideCost, vars, 0.0);
			}
			else
				maxOverride=-1;
			if((maxOverride<0)&&(maxOverride>-9999))
				consumed=consumed+(lowest*CMath.abs(maxOverride));
			else
			if(consumed > maxOverride)
				consumed=maxOverride;
			final int minOverride=CMProps.getMinManaException(ID());
			if(minOverride!=Integer.MIN_VALUE)
			{
				if(minOverride<0)
				{
					int actualMinimum=5;
					final int exception = CMProps.getMinManaException("_DEFAULT");
					if(exception > 0 )
						actualMinimum = exception;
					consumed=(lowest<actualMinimum)?actualMinimum:lowest;
				}
				else
				if(consumed<minOverride)
					consumed=minOverride;
			}
			if(overrideMana()>=0)
				consumed=overrideMana();
			minimum=5;
			if((costOverrides!=null)&&(costOverrides[AbilityMapper.AbilCostType.MANA.ordinal()]!=null))
			{
				consumed=costOverrides[AbilityMapper.AbilCostType.MANA.ordinal()].intValue();
				if((consumed<minimum)&&(consumed>=0))
					minimum=consumed;
			}
		}
		final int[] usageCost=buildCostArray(mob,consumed,minimum);
		if(rebuildCache)
		{
			if(consumed > COST_PCT-1)
				abilityUsageCache[myCacheIndex]=new int[]{consumed,minimum};
			else
				abilityUsageCache[myCacheIndex]=usageCost;
		}
		if(mob == null)
			overrideCache.put(ID(), usageCost);
		return usageCost;
	}

	public int xlevel(final MOB mob)
	{
		return mob.phyStats().level()+(2*getXLEVELLevel(mob));
	}

	public boolean confirmPossibleMaterialLocation(final int resource, final Room room)
	{
		if(room==null)
			return false;
		final Integer I=Integer.valueOf(resource);
		final boolean isMaterial=(resource&RawMaterial.RESOURCE_MASK)==0;
		final int roomResourceType=room.myResource();
		if(((isMaterial&&(resource==(roomResourceType&RawMaterial.MATERIAL_MASK))))
		||(I.intValue()==roomResourceType))
			return true;
		final List<Integer> resources=room.resourceChoices();
		if(resources!=null)
		{
			for(int i=0;i<resources.size();i++)
			{
				if(isMaterial&&(resource==(resources.get(i).intValue()&RawMaterial.MATERIAL_MASK)))
					return true;
				else
				if(resources.get(i).equals(I))
					return true;
			}
		}
		return false;
	}

	public Integer[] supportedResourcesMap()
	{
		final String rscs=supportedResourceString().toUpperCase();
		if(resourcesMap.containsKey(rscs))
		{
			return resourcesMap.get(rscs);
		}
		else
		{
			final List<String> set=CMParms.parseAny(supportedResourceString(),"|",true);
			final List<Integer> finalSet=new ArrayList<Integer>();
			for(int i=0;i<set.size();i++)
			{
				int x=-1;
				String setMat=set.get(i);
				if(setMat.startsWith("_"))
					x=RawMaterial.CODES.FIND_IgnoreCase(setMat.substring(1));
				else
				{
					final int y=setMat.indexOf('-');
					List<String> restV=null;
					if(y>0)
					{
						restV=CMParms.parseAny(setMat.substring(y+1),"-", true);
						setMat=setMat.substring(0, y);
					}
					final RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(setMat);
					if(m!=null)
					{
						x=m.mask();
						if((restV!=null)&&(restV.size()>0))
						{
							final List<Integer> rscsV=new XVector<Integer>(RawMaterial.CODES.COMPOSE_RESOURCES(x));
							for(final String sv : restV)
							{
								final int code = RawMaterial.CODES.FIND_CaseSensitive(sv);
								if(code >=0)
									rscsV.remove(Integer.valueOf(code));
							}
							for(int codeDex=0;codeDex<rscsV.size()-1;codeDex++)
								finalSet.add(rscsV.get(codeDex));
							x=rscsV.get(rscsV.size()-1).intValue();
						}
					}
				}
				if(x<0)
					x=RawMaterial.CODES.FIND_IgnoreCase(setMat);
				if(x>=0)
					finalSet.add(Integer.valueOf(x));
			}
			final Integer[] finalArray=finalSet.toArray(new Integer[0]);
			resourcesMap.put(rscs, finalArray);
			return finalArray;
		}
	}

	public boolean isMadeOfSupportedResource(final Item I)
	{
		if(I==null)
			return false;
		for(final Integer R : supportedResourcesMap())
		{
			// basically check if lower 8 bits are 0, therefore its a raw mat
			if((R.intValue() & RawMaterial.MATERIAL_MASK)==R.intValue())
			{
				if((I.material()& RawMaterial.MATERIAL_MASK)==R.intValue())
					return true;
			}
			else
			if(I.material()==R.intValue())
				return true;
		}
		return false;
	}

	@Override
	public boolean canBeLearnedBy(final MOB teacherM, final MOB studentM)
	{
		if(!super.canBeLearnedBy(teacherM,studentM))
			return false;
		if(studentM==null)
			return true;
		final CharClass C=studentM.charStats().getCurrentClass();
		if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
			return true;
		final boolean crafting = ((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
								||((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL);
		final AbilityComponents.AbilityLimits remainders = CMLib.ableComponents().getSpecialSkillRemainder(studentM, this);
		if(remainders.commonSkills()<=0)
		{
			if(teacherM != null)
				teacherM.tell(L("@x1 can not learn any more common skills.",studentM.name(teacherM)));
			studentM.tell(L("You have learned the maximum @x1 common skills, and may not learn any more.",""+remainders.maxCommonSkills()));
			return false;
		}
		if(remainders.specificSkillLimit()<=0)
		{
			if(teacherM != null)
			{
				if(crafting)
					teacherM.tell(L("@x1 can not learn any more crafting common skills.",studentM.name(teacherM)));
				else
					teacherM.tell(L("@x1 can not learn any more non-crafting common skills.",studentM.name(teacherM)));
			}
			final int max = crafting ? remainders.maxCraftingSkills() : remainders.maxNonCraftingSkills();
			if(crafting)
				studentM.tell(L("You have learned the maximum @x1 crafting skills, and may not learn any more.",""+max));
			else
				studentM.tell(L("You have learned the maximum @x1 non-crafting skills, and may not learn any more.",""+max));
			return false;
		}
		return true;
	}

	@Override
	public void teach(final MOB teacher, final MOB student)
	{
		super.teach(teacher, student);
		if((student!=null)&&(student.fetchAbility(ID())!=null))
		{
			final CharClass C=student.charStats().getCurrentClass();
			if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
				return;
			final boolean crafting = ((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
									||((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL);
			final AbilityComponents.AbilityLimits remainders = CMLib.ableComponents().getSpecialSkillRemainder(student, this);
			if(remainders.commonSkills()<=0)
				student.tell(L("@x1 may not learn any more common skills.",student.name()));
			else
			if(remainders.commonSkills()<=Integer.MAX_VALUE/2)
				student.tell(L("@x1 may learn @x2 more common skills.",student.name(),""+remainders.commonSkills()));
			if(remainders.specificSkillLimit()<=0)
				student.tell(L("@x1 may not learn any more @x2crafting common skills.",student.name(),(crafting?"":"non-")));
			else
			if(remainders.specificSkillLimit()<=Integer.MAX_VALUE/2)
				student.tell(L("@x1 may learn @x2 more @x3crafting common skills.",student.name(),""+remainders.specificSkillLimit(),(crafting?"":"non-")));
		}
	}

	public void bumpTickDown(final long byThisMuch)
	{
		tickDown+=byThisMuch;
		if(byThisMuch > 0)
			this.lastBaseDuration+=byThisMuch;
	}

	@Override
	public void startTickDown(final MOB invokerMOB, final Physical affected, final int tickTime)
	{
		super.startTickDown(invokerMOB, affected, tickTime);
		tickUp=0;
	}

	public boolean checkStop(final MOB mob, final List<String> commands)
	{
		if((commands!=null)
		&&(commands.size()==1)
		&&(commands.get(0).equalsIgnoreCase("stop")))
		{
			final Ability A=mob.fetchEffect(ID());
			if((A!=null)&&(!A.isNowAnAutoEffect())&&(A.canBeUninvoked()))
			{
				if(A instanceof CommonSkill)
					((CommonSkill)A).aborted=true;
				A.unInvoke();
				return true;
			}
			mob.tell(L("You are not doing that right now."));
		}
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if("abort".equalsIgnoreCase(newMiscText))
		{
			this.aborted=true;
			this.tickDown=1;
			this.tick(affected, Tickable.TICKID_MOB);
		}
		else
			super.setMiscText(newMiscText);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		aborted=false;
		if(!auto)
		{
			if(mob.isInCombat())
			{
				commonEmote(mob,L("<S-NAME> <S-IS-ARE> in combat!"));
				return false;
			}
			if((!allowedWhileMounted())
			&&(mob.riding()!=null))
			{
				commonEmote(mob,L("You can't do that while @x1 @x2.",mob.riding().stateString(mob),mob.riding().name()));
				return false;
			}

			if((!allowedInTheDark())
			&&(!CMLib.flags().canBeSeenBy(mob.location(),mob)))
			{
				commonTelL(mob,"<S-NAME> can't see to do that!");
				return false;
			}
			if((CMLib.flags().isSitting(mob)&&(!canBeDoneSittingDown()))
			||CMLib.flags().isSleeping(mob))
			{
				commonTelL(mob,"You need to stand up!");
				return false;
			}
			for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)||(A.ID().equalsIgnoreCase("AstroEngineering")))
				&&(!A.isNowAnAutoEffect())
				&&(!getUninvokeException().contains(A.ID())))
				{
					if(A instanceof CommonSkill)
						((CommonSkill)A).aborted=true;
					A.unInvoke();
				}
			}
			// if you can't move, you can't do anything!
			if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
				return false;
		}
		isAnAutoEffect=false;

		final int[] consumed=usageCost(mob,false);
		if(mob.curState().getMana()<consumed[Ability.USAGEINDEX_MANA])
		{
			if(mob.maxState().getMana()==consumed[Ability.USAGEINDEX_MANA])
				mob.tell(L("You must be at full mana to do that."));
			else
				mob.tell(L("You don't have enough mana to do that."));
			return false;
		}
		if(mob.curState().getMovement()<consumed[Ability.USAGEINDEX_MOVEMENT])
		{
			if(mob.maxState().getMovement()==consumed[Ability.USAGEINDEX_MOVEMENT])
				mob.tell(L("You must be at full movement to do that."));
			else
				mob.tell(L("You don't have enough movement to do that.  You are too tired."));
			return false;
		}
		if(mob.curState().getHitPoints()<consumed[Ability.USAGEINDEX_HITPOINTS])
		{
			if(mob.maxState().getHitPoints()==consumed[Ability.USAGEINDEX_HITPOINTS])
				mob.tell(L("You must be at full health to do that."));
			else
				mob.tell(L("You don't have enough hit points to do that."));
			return false;
		}
		if(!checkComponents(mob))
			return false;
		mob.curState().adjMana(-consumed[0],mob.maxState());
		mob.curState().adjMovement(-consumed[1],mob.maxState());
		mob.curState().adjHitPoints(-consumed[2],mob.maxState());
		setAbilityCode(0);
		activityRoom=mob.location();
		if((!bundling)&&(!auto))
			helpProficiency(mob, 0);

		return true;
	}

	private final static String[] MYCODES={"TICKUP","PCTREMAIN","NAME"};

	@Override
	public String getStat(final String code)
	{
		switch(getMyCodeNum(code))
		{
		case 0:
			return "" + tickUp;
		case 1:
		{
			final int tot= tickUp +tickDown;
			if((tot > 0)
			&&(affected != null))
				return CMath.toPct(CMath.div(Math.round(CMath.div(tickUp, tot)*100.0),100.0));
			return "";
		}
		case 2:
			return name();
		default:
			return super.getStat(code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getMyCodeNum(code))
		{
		case 0:
			tickUp=CMath.s_int(val);
			break;
		case 1:
			break;
		case 2:
			break;
		default:
			super.setStat(code,  val);
			break;
		}
	}

	protected int getMyCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(CommonSkill.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(super.getStatCodes());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
}
