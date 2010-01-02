package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class CommonSkill extends StdAbility
{
	public String ID() { return "CommonSkill"; }
	public String name(){ return "Common Skill";}
	private static final String[] triggerStrings = empty;
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "";}

	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected String displayText="(Doing something productive)";
	public String displayText(){return displayText;}

	protected int iniTrainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_COMMONTRAINCOST);}
	protected int iniPracticesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_COMMONPRACCOST);}
	protected int iniPracticesToPractice(){return 1;}

	protected boolean allowedWhileMounted(){return true;}
	
	protected Room activityRoom=null;
	protected boolean aborted=false;
	protected boolean helping=false;
    protected boolean bundling=false;
	public Ability helpingAbility=null;
	protected int tickUp=0;
	protected String verb="working";
    protected String playSound=null;
	public int usageType(){return USAGE_MOVEMENT;}

	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public int classificationCode()	{	return Ability.ACODE_COMMON_SKILL; }

	protected int yield=1;
	public int abilityCode(){return yield;}
	public void setAbilityCode(int newCode){yield=newCode;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
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
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())||(mob.location()!=activityRoom)||(!CMLib.flags().aliveAwakeMobileUnbound(mob,true)))
			{aborted=true; unInvoke(); return false;}
            String sound=(playSound!=null)?CMProps.msp(playSound,10):"";
			if(tickDown==4)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+verb+"."+sound);
			else
			if((tickUp%4)==0)
			{
				int total=tickUp+tickDown;
				int pct=(int)Math.round(CMath.div(tickUp,total)*100.0);
				mob.location().show(mob,null,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> continue(s) "+verb+" ("+pct+"% completed)."+sound,null,"<S-NAME> continue(s) "+verb+"."+sound);
			}
			if((helping)
			&&(helpingAbility!=null)
			&&(helpingAbility.affecting() instanceof MOB)
			&&(((MOB)helpingAbility.affecting()).isMine(helpingAbility)))
				helpingAbility.tick(helpingAbility.affecting(),tickID);
			if((mob.soulMate()==null)&&(mob.playerStats()!=null)&&(mob.location()!=null))
			    mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}
		tickUp++;
		return super.tick(ticking,tickID);
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
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> stop(s) "+verb+".");
				else
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> done "+verb+".");
				helping=false;
				helpingAbility=null;
			}
		}
		super.unInvoke();
	}

	protected int getDuration(int baseTicks, MOB mob, int itemLevel, int minDuration)
	{
		int ticks=baseTicks;
		int level=mob.envStats().level() - itemLevel;
		double pct=CMath.div(level,CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL))*.5;
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
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_ALWAYS,str);
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
		if(autoGenerate>0) return CMClass.getItem("StdItem");
		Item fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
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
		{
			int[] usage=new int[3];
			usage[0]=overrideMana();
			usage[1]=overrideMana();
			usage[2]=overrideMana();
			return usage;
		}
		if(usageType()==Ability.USAGE_NADA) return new int[3];

		int consumed=25;
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
		int minimum=5;
		if((costOverrides!=null)&&(costOverrides[AbilityMapper.AbilityMapping.COST_MANA]!=null))
		{
			consumed=costOverrides[AbilityMapper.AbilityMapping.COST_MANA].intValue();
			if((consumed<minimum)&&(consumed>=0)) minimum=consumed;
		}
		return buildCostArray(mob,consumed,minimum);
	}
	
	public int xlevel(MOB mob)
	{ 
		return mob.envStats().level()+(2*getXLEVELLevel(mob));
	}
    
	public boolean confirmPossibleMaterialLocation(int resource, Room location)
	{
		if(location==null) return false;
		Integer I=Integer.valueOf(resource);
		boolean isMaterial=(resource&RawMaterial.RESOURCE_MASK)==0;
		int roomResourceType=location.myResource();
		if(((isMaterial&&(resource==(roomResourceType&RawMaterial.MATERIAL_MASK))))
		||(I.intValue()==roomResourceType))
			return true;
		Vector resources=location.resourceChoices();
		if(resources!=null)
		for(int i=0;i<resources.size();i++)
			if(isMaterial&&(resource==(((Integer)resources.elementAt(i)).intValue()&RawMaterial.MATERIAL_MASK)))
				return true;
			else
			if(resources.elementAt(i).equals(I))
				return true;
		return false;
	}

	private int[] remainingLearnsFromClassMax(MOB student)
	{
		if(student==null) return null;
		CharClass C=student.charStats().getCurrentClass();
		boolean crafting = ((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
		int[] remaining = {C.maxCommonSkills(), crafting?C.maxCraftingSkills():C.maxNonCraftingSkills()};
		if(remaining[0]==0) remaining[0]=Integer.MAX_VALUE;
		if(remaining[1]==0) remaining[1]=Integer.MAX_VALUE;
		DVector culturalAbilitiesDV = student.baseCharStats().getMyRace().culturalAbilities();
		HashSet culturalAbilities=new HashSet();
		for(int i=0;i<culturalAbilitiesDV.size();i++)
			culturalAbilities.add(culturalAbilitiesDV.elementAt(i, 1).toString().toLowerCase());
		for(int a=0;a<student.numLearnedAbilities();a++)
		{
			Ability A=student.fetchAbility(a);
			if(A instanceof CommonSkill)
			{
				if((CMLib.ableMapper().getQualifyingLevel(C.ID(), false, A.ID())>=0)||(culturalAbilities.contains(A.ID().toLowerCase())))
					continue;
				remaining[0]--;
				if(crafting == ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL))
					remaining[1]--;
			}
		}
		return remaining;
	}
	
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		CharClass C=student.charStats().getCurrentClass();
		if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
			return true;
		boolean crafting = ((classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
		int[] remaining = remainingLearnsFromClassMax(student);
		if(remaining[0]<=0)
		{
			teacher.tell(student.name()+" can not learn any more common skills.");
			student.tell("You may only learn " + C.maxCommonSkills() + " common skills.");
			return false;
		}
		if(remaining[1]<=0)
		{
			teacher.tell(student.name()+" can not learn any more " + (crafting?"":"non-") + "crafting common skills.");
			student.tell("You may only learn " + (crafting?""+C.maxCraftingSkills()+" ":C.maxNonCraftingSkills()+" non-") + "crafting common skills.");
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
			int[] remaining = remainingLearnsFromClassMax(student);
			if(remaining[0]<=0)
				student.tell(student.name()+" may not learn any more common skills.");
			else
			if(remaining[0]<Integer.MAX_VALUE/2)
				student.tell(student.name()+" may learn "+remaining[0]+" more common skills.");
			if(remaining[1]<=0)
				student.tell(student.name()+" may not learn any more "+(crafting?"":"non-") +"crafting common skills.");
			else
			if(remaining[1]<Integer.MAX_VALUE/2)
				student.tell(student.name()+" may learn "+remaining[1]+" more "+(crafting?"":"non-") +"crafting common skills.");
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
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
		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			commonTell(mob,"You need to stand up!");
			return false;
		}
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
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
    		helpProficiency(mob);

		return true;
	}
}
