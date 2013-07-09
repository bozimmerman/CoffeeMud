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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class CommonSkill extends StdAbility
{
	public String ID() { return "CommonSkill"; }
	public String name(){ return "Common Skill";}
	private static final String[] triggerStrings = empty;
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "";}
	public static final Map<String,Integer[]> resourcesMap=new Hashtable<String,Integer[]>();
	protected static Item fakeFire=null;
	
	protected volatile Room activityRoom=null;
	protected boolean aborted=false;
	protected boolean helping=false;
	protected boolean bundling=false;
	public Ability helpingAbility=null;
	protected volatile int tickUp=0;
	protected String verb="working";
	protected String playSound=null;
	protected int yield=1;

	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected String displayText="(Doing something productive)";
	public String displayText(){return displayText;}

	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getCommonTrainCostFormula(ID()); }
	protected int iniPracticesToPractice(){return 1;}

	protected boolean allowedWhileMounted(){return true;}
	
	public int usageType(){return USAGE_MOVEMENT;}

	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public int classificationCode()	{	return Ability.ACODE_COMMON_SKILL; }
	protected boolean canBeDoneSittingDown() { return false; }
	protected int getActivityMessageType() { return canBeDoneSittingDown()?CMMsg.MSG_HANDS|CMMsg.MASK_SOUND:CMMsg.MSG_NOISYMOVEMENT; }

	public int abilityCode(){return yield;}
	public void setAbilityCode(int newCode){yield=newCode;}

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
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())
			||(mob.location()!=activityRoom)
			||(!CMLib.flags().aliveAwakeMobileUnbound(mob,true)))
			{
				aborted=true; 
				unInvoke(); 
				return false;
			}
			String sound=(playSound!=null)?CMLib.protocol().msp(playSound,10):"";
			if(tickDown==4)
				mob.location().show(mob,null,getActivityMessageType(),"<S-NAME> <S-IS-ARE> almost done "+verb+"."+sound);
			else
			if((tickUp%4)==0)
			{
				int total=tickUp+tickDown;
				int pct=(int)Math.round(CMath.div(tickUp,total)*100.0);
				mob.location().show(mob,null,null,getActivityMessageType(),"<S-NAME> continue(s) "+verb+" ("+pct+"% completed)."+sound,null,"<S-NAME> continue(s) "+verb+"."+sound);
			}
			if((helping)
			&&(helpingAbility!=null)
			&&(helpingAbility.affecting() instanceof MOB)
			&&(((MOB)helpingAbility.affecting()).isMine(helpingAbility)))
				helpingAbility.tick(helpingAbility.affecting(),tickID);
			if((mob.soulMate()==null)&&(mob.playerStats()!=null)&&(mob.location()!=null))
				mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}
		
		int preTickDown=tickDown;
		if(!super.tick(ticking,tickID))
			return false;
		tickUp+=(preTickDown-tickDown);
		return true;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				MOB mob=(MOB)affected;
				if(aborted)
					mob.location().show(mob,null,getActivityMessageType(),"<S-NAME> stop(s) "+verb+".");
				else
					mob.location().show(mob,null,getActivityMessageType(),"<S-NAME> <S-IS-ARE> done "+verb+".");
				helping=false;
				helpingAbility=null;
			}
		}
		super.unInvoke();
	}

	protected int getDuration(int baseTicks, MOB mob, int itemLevel, int minDuration)
	{
		int ticks=baseTicks;
		int level=mob.phyStats().level() - itemLevel;
		double pct=CMath.div(level,CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL))*.5;
		ticks-=(int)Math.round(CMath.mul(ticks, pct));
		
		double quickPct = getXTIMELevel(mob) * 0.05; 
		ticks-=(int)Math.round(CMath.mul(ticks, quickPct));
		if(ticks<minDuration) ticks=minDuration;
		return ticks;
	}
	
	protected int addedTickTime(MOB invokerMOB, int baseTickTime)
	{
		// common skills tend to SUBTRACT time -- not add to it!
		return 0;
	}
	
	protected String getBrand(MOB mob)
	{
		if(mob==null)
			return "This is the work of an anonymous craftsman.";
		else
			return "This is the work of "+mob.Name()+".";
	}

	protected void commonTell(MOB mob, Environmental target, Environmental tool, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			if(target!=null) str=CMStrings.replaceAll(str,"<T-NAME>",target.name());
			if(tool!=null)  str=CMStrings.replaceAll(str,"<O-NAME>",tool.name());
			CMLib.commands().postSay(mob,null,str,false,false);
		}
		else
			mob.tell(mob,target,tool,str);
	}

	protected void commonTell(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			CMLib.commands().postSay(mob,null,str,false,false);
		}
		else
			mob.tell(str);
	}

	protected void commonEmote(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
			mob.location().show(mob,null,getActivityMessageType()|CMMsg.MASK_ALWAYS,str);
		else
			mob.tell(mob,null,null,str);
	}

	protected int lookingFor(int material, Room fromHere)
	{
		Vector V=new Vector();
		V.addElement(Integer.valueOf(material));
		return lookingFor(V,fromHere);
	}

	protected int lookingFor(Vector materials, Room fromHere)
	{
		Vector possibilities=new Vector();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room room=fromHere.getRoomInDir(d);
			Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				int material=room.myResource();
				if(materials.contains(Integer.valueOf(material&RawMaterial.MATERIAL_MASK)))
				{possibilities.addElement(Integer.valueOf(d));}
			}
		}
		if(possibilities.size()==0)
			return -1;
		return ((Integer)(possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1)))).intValue();
	}

	public Item getRequiredFire(MOB mob,int autoGenerate)
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
			Item I2=mob.location().getItem(i);
			if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return null;
		}
		return fire;
	}
	
	public int[] usageCost(MOB mob, boolean ignoreClassOverride)
	{
		if(mob==null) 
			return super.usageCost(null, ignoreClassOverride);
		if(usageType()==Ability.USAGE_NADA) 
			return super.usageCost(mob, ignoreClassOverride);

		final int[][] abilityUsageCache=mob.getAbilityUsageCache(ID());
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
			int diff=CMLib.ableMapper().qualifyingClassLevel(mob,this)+super.getXLOWCOSTLevel(mob)-CMLib.ableMapper().qualifyingLevel(mob,this);
			Integer[] costOverrides=null;
			if(!ignoreClassOverride) 
				costOverrides=CMLib.ableMapper().getCostOverrides(mob,ID());
			if(diff>0)
			switch(diff)
			{
			case 1: consumed=20; break;
			case 2: consumed=16; break;
			case 3: consumed=13; break;
			case 4: consumed=11; break;
			case 5: consumed=8; break;
			default: consumed=5; break;
			}
			if(overrideMana()>=0) consumed=overrideMana();
			minimum=5;
			if((costOverrides!=null)&&(costOverrides[AbilityMapper.AbilityMapping.COST_MANA]!=null))
			{
				consumed=costOverrides[AbilityMapper.AbilityMapping.COST_MANA].intValue();
				if((consumed<minimum)&&(consumed>=0)) minimum=consumed;
			}
		}
		int[] usageCost=buildCostArray(mob,consumed,minimum);
		if(rebuildCache)
		{
			if(consumed > COST_PCT-1)
				abilityUsageCache[myCacheIndex]=new int[]{consumed,minimum};
			else
				abilityUsageCache[myCacheIndex]=usageCost;
		}
		return usageCost;
	}
	
	public int xlevel(MOB mob)
	{ 
		return mob.phyStats().level()+(2*getXLEVELLevel(mob));
	}
	
	public boolean confirmPossibleMaterialLocation(int resource, Room room)
	{
		if(room==null) return false;
		Integer I=Integer.valueOf(resource);
		boolean isMaterial=(resource&RawMaterial.RESOURCE_MASK)==0;
		int roomResourceType=room.myResource();
		if(((isMaterial&&(resource==(roomResourceType&RawMaterial.MATERIAL_MASK))))
		||(I.intValue()==roomResourceType))
			return true;
		List<Integer> resources=room.resourceChoices();
		if(resources!=null)
		for(int i=0;i<resources.size();i++)
			if(isMaterial&&(resource==(resources.get(i).intValue()&RawMaterial.MATERIAL_MASK)))
				return true;
			else
			if(resources.get(i).equals(I))
				return true;
		return false;
	}

	public Integer[] supportedResourcesMap()
	{
		String rscs=supportedResourceString().toUpperCase();
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
					int y=setMat.indexOf('-');
					List<String> restV=null;
					if(y>0)
					{
						restV=CMParms.parseAny(setMat.substring(y+1),"-", true);
						setMat=setMat.substring(0, y);
					}
					for(int j=0;j<RawMaterial.MATERIAL_DESCS.length;j++)
						if(RawMaterial.MATERIAL_DESCS[j].equalsIgnoreCase(setMat))
						{ 
							x=RawMaterial.MATERIAL_CODES[j];
							if((restV!=null)&&(restV.size()>0))
							{
								List<Integer> rscsV=new XVector<Integer>(RawMaterial.CODES.COMPOSE_RESOURCES(x));
								for(String sv : restV)
								{
									int code = RawMaterial.CODES.FIND_CaseSensitive(sv);
									if(code >=0)
										rscsV.remove(Integer.valueOf(code));
								}
								for(int codeDex=0;codeDex<rscsV.size()-1;codeDex++)
									finalSet.add(rscsV.get(codeDex));
								x=rscsV.get(rscsV.size()-1).intValue();
							}
							break;
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
	
	public boolean isMadeOfSupportedResource(Item I)
	{
		if(I==null) return false;
		for(Integer R : supportedResourcesMap())
		{
			if((R.intValue() & RawMaterial.MATERIAL_MASK)==0)
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
	
	public boolean canBeLearnedBy(MOB teacherM, MOB studentM)
	{
		if(!super.canBeLearnedBy(teacherM,studentM))
			return false;
		if(studentM==null) return true;
		CharClass C=studentM.charStats().getCurrentClass();
		if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
			return true;
		boolean crafting = ((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
		AbilityMapper.AbilityLimits remainders = CMLib.ableMapper().getCommonSkillRemainder(studentM, this);
		if(remainders.commonSkills<=0)
		{
			teacherM.tell(studentM.name()+" can not learn any more common skills.");
			studentM.tell("You have learned the maximum "+C.maxCommonSkills()+" common skills, and may not learn any more.");
			return false;
		}
		if(remainders.specificSkillLimit<=0)
		{
			teacherM.tell(studentM.name()+" can not learn any more " + (crafting?"":"non-") + "crafting common skills.");
			final int max = crafting ? C.maxCraftingSkills() : C.maxNonCraftingSkills();
			studentM.tell("You have learned the maximum "+max+""+(crafting?" ":" non-") + "crafting skills, and may not learn any more.");
			return false;
		}
		return true;
	}

	public void teach(MOB teacher, MOB student)
	{
		super.teach(teacher, student);
		if((student!=null)&&(student.fetchAbility(ID())!=null))
		{
			CharClass C=student.charStats().getCurrentClass();
			if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
				return;
			boolean crafting = ((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
			AbilityMapper.AbilityLimits remainders = CMLib.ableMapper().getCommonSkillRemainder(student, this);
			if(remainders.commonSkills<=0)
				student.tell(student.name()+" may not learn any more common skills.");
			else
			if(remainders.commonSkills<=Integer.MAX_VALUE/2)
				student.tell(student.name()+" may learn "+remainders.commonSkills+" more common skills.");
			if(remainders.specificSkillLimit<=0)
				student.tell(student.name()+" may not learn any more "+(crafting?"":"non-") +"crafting common skills.");
			else
			if(remainders.specificSkillLimit<=Integer.MAX_VALUE/2)
				student.tell(student.name()+" may learn "+remainders.specificSkillLimit+" more "+(crafting?"":"non-") +"crafting common skills.");
		}
	}
	
	public void bumpTickDown(long byThisMuch)
	{
		tickDown+=byThisMuch;
	}
	
	public void startTickDown(MOB invokerMOB, Physical affected, int tickTime)
	{
		super.startTickDown(invokerMOB, affected, tickTime);
		tickUp=0;
	}
	
	public boolean checkStop(MOB mob, Vector commands)
	{
		if((commands!=null)
		&&(commands.size()==1)
		&&(commands.get(0) instanceof String)
		&&(((String)commands.get(0)).equalsIgnoreCase("stop")))
		{
			Ability A=mob.fetchEffect(ID());
			if((A!=null)&&(!A.isNowAnAutoEffect())&&(A.canBeUninvoked()))
			{
				if(A instanceof CommonSkill)
					((CommonSkill)A).aborted=true;
				A.unInvoke();
				return true;
			}
			mob.tell("You are not doing that right now.");
		}
		return false;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		aborted=false;
		if(mob.isInCombat())
		{
			commonEmote(mob,"<S-NAME> <S-IS-ARE> in combat!");
			return false;
		}
		if((!allowedWhileMounted())&&(mob.riding()!=null))
		{
			commonEmote(mob,"You can't do that while "+mob.riding().stateString(mob)+" "+mob.riding().name()+".");
			return false;
		}
		
		if(!CMLib.flags().canBeSeenBy(mob.location(),mob))
		{
			commonTell(mob,"<S-NAME> can't see to do that!");
			return false;
		}
		if((CMLib.flags().isSitting(mob)&&(!canBeDoneSittingDown()))||CMLib.flags().isSleeping(mob))
		{
			commonTell(mob,"You need to stand up!");
			return false;
		}
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
			{
				if(A instanceof CommonSkill)
					((CommonSkill)A).aborted=true;
				A.unInvoke();
			}
		}
		isAnAutoEffect=false;

		// if you can't move, you can't do anything!
		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;
		int[] consumed=usageCost(mob,false);
		if(mob.curState().getMana()<consumed[Ability.USAGEINDEX_MANA])
		{
			if(mob.maxState().getMana()==consumed[Ability.USAGEINDEX_MANA])
				mob.tell("You must be at full mana to do that.");
			else
				mob.tell("You don't have enough mana to do that.");
			return false;
		}
		if(mob.curState().getMovement()<consumed[Ability.USAGEINDEX_MOVEMENT])
		{
			if(mob.maxState().getMovement()==consumed[Ability.USAGEINDEX_MOVEMENT])
				mob.tell("You must be at full movement to do that.");
			else
				mob.tell("You don't have enough movement to do that.  You are too tired.");
			return false;
		}
		if(mob.curState().getHitPoints()<consumed[Ability.USAGEINDEX_HITPOINTS])
		{
			if(mob.maxState().getHitPoints()==consumed[Ability.USAGEINDEX_HITPOINTS])
				mob.tell("You must be at full health to do that.");
			else
				mob.tell("You don't have enough hit points to do that.");
			return false;
		}
		if(!checkComponents(mob))
			return false;
		mob.curState().adjMana(-consumed[0],mob.maxState());
		mob.curState().adjMovement(-consumed[1],mob.maxState());
		mob.curState().adjHitPoints(-consumed[2],mob.maxState());
		activityRoom=mob.location();
		if(!bundling)
			helpProficiency(mob, 0);

		return true;
	}
}
