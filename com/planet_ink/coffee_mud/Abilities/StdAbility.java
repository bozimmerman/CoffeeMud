package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class StdAbility extends Scriptable implements Ability, Cloneable
{
	public String ID() { return "StdAbility"; }
	public String Name(){return name();}
	public String name(){ return "an ability";}
	public String description(){return "&";}
	public String displayText(){return "Affected list display for "+ID();}
	public String image(){return "";}
	public void setImage(String newImage){}
	public static final String[] empty={};
	public String[] triggerStrings(){return empty;}
	public int maxRange(){return 0;}
	public int minRange(){return 0;}
	public int castingTime(){return 1;}
	public int combatCastingTime(){return 1;}
	public boolean putInCommandlist(){return true;}
	public boolean isAutoInvoked(){return false;}
	public boolean bubbleAffect(){return false;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST);}
	protected int practicesToPractice(){return 1;}
	public long flags(){return 0;}
	public int usageType(){return USAGE_MANA;}
	protected int overrideMana(){return -1;} //-1=normal, Integer.MAX_VALUE=all, Integer.MAX_VALUE-100
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}

	protected boolean isAnAutoEffect=false;
	protected int profficiency=0;
	protected boolean borrowed=false;
	public String miscText="";
	protected MOB invoker=null;
	protected Environmental affected=null;
	protected boolean canBeUninvoked=true;
	protected boolean unInvoked=false;
	protected int tickDown=-1;
	protected long lastProfHelp=0;

	public StdAbility()
	{
	}

	public Environmental newInstance()
	{
		try{
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdAbility();
	}
	public int classificationCode(){ return Ability.SKILL; }

	protected static final EnvStats envStats=new DefaultEnvStats();
	public EnvStats envStats(){return envStats;}
	public EnvStats baseEnvStats(){return envStats;}

	public boolean isNowAnAutoEffect(){ return isAnAutoEffect; }
	public boolean isBorrowed(Environmental toMe){ return borrowed;	}
	public void setBorrowed(Environmental toMe, boolean truefalse) { borrowed=truefalse; }
	public void setName(String newName){}
	public void recoverEnvStats() {}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}
	public int abilityCode(){return 0;}
	public void setAbilityCode(int newCode){}
	public Vector externalFiles(){return null;}

	// ** For most abilities, the following stuff actually matters */
	public void setMiscText(String newMiscText)	{ miscText=newMiscText;}
	public String text(){ return miscText;}
	public int profficiency(){ return profficiency;}
	public void setProfficiency(int newProfficiency)
	{
		profficiency=newProfficiency;
		if(profficiency>100) profficiency=100;
	}

	public void startTickDown(MOB invokerMOB, Environmental affected, int tickTime)
	{
		if(invokerMOB!=null) invoker=invokerMOB;

		borrowed=true; // makes it so that the effect does not save!

		if(invoker()!=null)
			for(int c=0;c<invoker().charStats().numClasses();c++)
				tickTime=invoker().charStats().getMyClass(c).classDurationModifier(invoker(),this,tickTime);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob.location()==null) return;
			if(affected.fetchEffect(ID())==null) 
				affected.addEffect(this);
			mob.location().recoverRoomStats();
			if(invoker()!=affected)
				for(int c=0;c<mob.charStats().numClasses();c++)
					tickTime=mob.charStats().getMyClass(c).classDurationModifier(mob,this,tickTime);
		}
		else
		{
			if(affected.fetchEffect(this.ID())==null)
				affected.addEffect(this);

			if(affected instanceof Room)
				((Room)affected).recoverRoomStats();
			else
				affected.recoverEnvStats();
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_MOB,1);
		}
		tickDown=tickTime;
	}

	protected boolean disregardsArmorCheck(MOB mob)
	{
		return ((mob==null)
				||(mob.isMonster())
				||(CMAble.qualifiesByLevel(mob,this)));
	}
	
	
	public int adjustedLevel(MOB caster, int asLevel)
	{
		if(caster==null) return 1;
		int lowestQualifyingLevel=CMAble.lowestQualifyingLevel(this.ID());
		int adjLevel=lowestQualifyingLevel;
		int qualifyingLevel=CMAble.qualifyingLevel(caster,this);
		if((caster.isMonster())||(qualifyingLevel>=0))
			adjLevel+=(CMAble.qualifyingClassLevel(caster,this)-qualifyingLevel);
		else
			adjLevel=caster.envStats().level()-lowestQualifyingLevel-25;
		if(asLevel>0) adjLevel=asLevel;
		if(adjLevel<lowestQualifyingLevel)
			adjLevel=lowestQualifyingLevel;
		if(adjLevel<1) return 1;
		return adjLevel;
	}

	public boolean canAffect(Environmental E)
	{
		if((E==null)&&(canAffectCode()==0)) return true;
		if(E==null) return false;
		if((E instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0)) return true;
		if((E instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0)) return true;
		if((E instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0)) return true;
		if((E instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0)) return true;
		if((E instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}

	public boolean canTarget(Environmental E)
	{
		if((E==null)&&(canTargetCode()==0)) return true;
		if(E==null) return false;
		if((E instanceof MOB)&&((canTargetCode()&Ability.CAN_MOBS)>0)) return true;
		if((E instanceof Item)&&((canTargetCode()&Ability.CAN_ITEMS)>0)) return true;
		if((E instanceof Room)&&((canTargetCode()&Ability.CAN_ROOMS)>0)) return true;
		if((E instanceof Area)&&((canTargetCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}

	public MOB getTarget(MOB mob, Vector commands, Environmental givenTarget)
	{ return getTarget(mob,commands,givenTarget,false,false);	}

	public MOB getTarget(MOB mob, Vector commands, Environmental givenTarget, boolean quiet, boolean alreadyAffOk)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality()==Ability.MALICIOUS)&&(mob.getVictim()!=null))
		   target=mob.getVictim();
		else
		if((targetName.length()==0)&&(quality()!=Ability.MALICIOUS))
			target=mob;
		else
		if(targetName.equalsIgnoreCase("self")||targetName.equalsIgnoreCase("me"))
		   target=mob;
		else
		if((targetName.length()>0)&&(mob.location()!=null))
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)
		||((givenTarget==null)&&(!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see anyone called '"+targetName+"' here.");
			}
			return null;
		}

		if((!alreadyAffOk)&&(!isAutoInvoked())&&(target.fetchEffect(this.ID())!=null))
		{
			if((givenTarget==null)&&(!quiet))
			{
				if(target==mob)
					mob.tell("You are already affected by "+name()+".");
				else
					mob.tell(target,null,null,"<S-NAME> is already affected by "+name()+".");
			}
			return null;
		}
		return target;
	}

	
	public Environmental getAnyTarget(MOB mob, 
									  Vector commands, 
									  Environmental givenTarget, 
									  int wornReqCode)
	{ return getAnyTarget(mob,commands,givenTarget,wornReqCode,false,false);}
	
	public Environmental getAnyTarget(MOB mob, 
						  Vector commands, 
						  Environmental givenTarget, 
						  int wornReqCode,
						  boolean checkOthersInventory)
	{ return getAnyTarget(mob,commands,givenTarget,wornReqCode,checkOthersInventory,false);}
	
	public Environmental getAnyTarget(MOB mob, 
									  Vector commands, 
									  Environmental givenTarget, 
									  int wornReqCode,
									  boolean checkOthersInventory,
									  boolean alreadyAffOk)
	{
		String targetName=Util.combine(commands,0);
		Environmental target=null;
		if(givenTarget!=null)
			target=givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality()==Ability.MALICIOUS)&&(mob.getVictim()!=null))
			target=mob.getVictim();
		else
		if(targetName.equalsIgnoreCase("self")||targetName.equalsIgnoreCase("me"))
		   target=mob;
		else
		if(mob.location()!=null)
		{
			target=mob.location().fetchFromRoomFavorMOBs(null,targetName, wornReqCode);
			if(target==null)
				target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,targetName,wornReqCode);
			if((target==null)
			&&(targetName.equalsIgnoreCase("room")
				||targetName.equalsIgnoreCase("here")
				||targetName.equalsIgnoreCase("place")))
				target=mob.location();
			if((target==null)&&(checkOthersInventory))
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					target=M.fetchInventory(targetName);
					if(target!=null)
					{
						switch(wornReqCode)
						{
						case Item.WORN_REQ_UNWORNONLY:
							if(!((Item)target).amWearingAt(Item.INVENTORY))
								continue;
							break;
						case Item.WORN_REQ_WORNONLY:
							if(((Item)target).amWearingAt(Item.INVENTORY))
								continue;
							break;
						}
						break;
					}
				}
		}
		if(target!=null) targetName=target.name();
		
		if((target==null)
		||((givenTarget==null)
		   &&(!Sense.canBeSeenBy(target,mob))
		   &&((!Sense.canBeHeardBy(target,mob))||((target instanceof MOB)&&(!((MOB)target).isInCombat())))))
		{
			if(targetName.trim().length()==0)
				mob.tell("You don't see that here.");
			else
			if(!Sense.isSleeping(mob))
				mob.tell("You don't see '"+targetName+"' here.");
			return null;
		}

		if((!alreadyAffOk)&&(target.fetchEffect(this.ID())!=null))
		{
			if(givenTarget==null)
			{
				if(target==mob)
					mob.tell("You are already affected by "+name()+".");
				else
					mob.tell(mob,target,null,"<T-NAME> is already affected by "+name()+".");
			}
			return null;
		}
		return target;
	}

	protected static Item possibleContainer(MOB mob, Vector commands, boolean withStuff, int wornReqCode)
	{
		if((commands==null)||(commands.size()<2))
			return null;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornReqCode);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang) instanceof Container)
		&&((!withStuff)||(((Container)thisThang).getContents().size()>0)))
		{
			commands.removeElementAt(commands.size()-1);
			return (Item)thisThang;
		}
		return null;
	}

	public Item getTarget(MOB mob, Room location, Environmental givenTarget, Vector commands, int wornReqCode)
	{ return getTarget(mob,location,givenTarget,null,commands,wornReqCode);}
	public Item getTarget(MOB mob, Room location, Environmental givenTarget, Item container, Vector commands, int wornReqCode)
	{
		String targetName=Util.combine(commands,0);

		Environmental target=null;
		if((givenTarget!=null)&&(givenTarget instanceof Item))
			target=givenTarget;

		if(location!=null)
			target=location.fetchFromRoomFavorItems(container,targetName,wornReqCode);
		if(target==null)
		{
			if(location!=null)
				target=location.fetchFromMOBRoomFavorsItems(mob,container,targetName,wornReqCode);
			else
				target=mob.fetchCarried(container,targetName);
		}
		if(target!=null) targetName=target.name();
		
		if((target==null)
		||(!(target instanceof Item))
		||((target!=null)
		   &&((givenTarget==null)&&(!Sense.canBeSeenBy(target,mob)))))
		{
			if(targetName.length()==0)
				mob.tell("You need to be more specific.");
			else
			if((target==null)||(target instanceof Item))
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see that here.");
				else
				if(!Sense.isSleeping(mob))
					mob.tell("You don't see anything called '"+targetName+"' here.");
			}
			else
				mob.tell(mob,target,null,"You can't do that to <T-NAMESELF>.");
			return null;
		}
		return (Item)target;
	}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}


	protected void cloneFix(Ability E){}
	public Environmental copyOf()
	{
		try
		{
			StdAbility E=(StdAbility)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public boolean profficiencyCheck(MOB mob, int adjustment, boolean auto)
	{
		
		if(auto)
		{
			isAnAutoEffect=true;
			setProfficiency(100);
			return true;
		}
		
		if((mob!=null)&&CMSecurity.isAllowed(mob,mob.location(),"SUPERSKILL"))
		   return true;

		isAnAutoEffect=false;
		int pctChance=profficiency();
		if(pctChance>95) pctChance=95;
		if(pctChance<5) pctChance=5;

		if(adjustment>=0)
			pctChance+=adjustment;
		else
		if(Dice.rollPercentage()>(100+adjustment))
			return false;
		return (Dice.rollPercentage()<pctChance);
	}

	public Environmental affecting()
	{
		return affected;
	}
	public void setAffectedOne(Environmental being)
	{
		affected=being;
	}

	public void unInvoke()
	{
		unInvoked=true;

		if(affected==null) return;
		Environmental being=affected;

		if(canBeUninvoked())
		{
			being.delEffect(this);
			if(being instanceof Room)
				((Room)being).recoverRoomStats();
			else
			if(being instanceof MOB)
			{
				if(((MOB)being).location()!=null)
					((MOB)being).location().recoverRoomStats();
				else
				{
					being.recoverEnvStats();
					((MOB)being).recoverCharStats();
					((MOB)being).recoverMaxState();
				}
			}
			else
				being.recoverEnvStats();
		}
	}

	public boolean canBeUninvoked()
	{
		return canBeUninvoked;
	}

	public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public MOB invoker()
	{
		return invoker;
	}
	public void setInvoker(MOB mob){invoker=mob;}

	protected int[] buildCostArray(MOB mob, int consumed)
	{
		int[] usageCosts=new int[3];
		boolean useMana=Util.bset(usageType(),Ability.USAGE_MANA);
		boolean useMoves=Util.bset(usageType(),Ability.USAGE_MOVEMENT);
		boolean useHits=Util.bset(usageType(),Ability.USAGE_HITPOINTS);
		int divider=1;
		if((useMana)&&(useMoves)&&(useHits)) divider=3;
		else
		if((useMana)&&(useMoves)&&(!useHits)) divider=2;
		else
		if((useMana)&&(!useMoves)&&(useHits)) divider=2;
		else
		if((!useMana)&&(useMoves)&&(useHits)) divider=2;

		if(useMana){
			usageCosts[0]=consumed/divider;
			if(usageCosts[0]<5)	usageCosts[0]=5;
			if(consumed==Integer.MAX_VALUE)
			{
				usageCosts[0]=mob.maxState().getMana();
				if(mob.baseState().getMana()>mob.maxState().getMana())
				    usageCosts[0]=mob.baseState().getMana();
			}
			else
			if(consumed>(Integer.MAX_VALUE-100))
				usageCosts[0]=(int)Math.round(Util.mul(mob.maxState().getMana(),Util.div((Integer.MAX_VALUE-consumed),100.0)));
		}
		if(useMoves){
			usageCosts[1]=consumed/divider;
			if(usageCosts[1]<5)	usageCosts[1]=5;
			if(consumed==Integer.MAX_VALUE)
			{
				usageCosts[1]=mob.maxState().getMovement();
				if(mob.baseState().getMovement()>mob.maxState().getMovement())
				    usageCosts[1]=mob.baseState().getMovement();
			}
			else
			if(consumed>(Integer.MAX_VALUE-100))
				usageCosts[0]=(int)Math.round(Util.mul(mob.maxState().getMovement(),Util.div((Integer.MAX_VALUE-consumed),100.0)));
		}
		if(useHits){
			usageCosts[2]=consumed/divider;
			if(usageCosts[2]<5)	usageCosts[2]=5;
			if(consumed==Integer.MAX_VALUE)
			{
				usageCosts[2]=mob.maxState().getHitPoints();
				if(mob.baseState().getHitPoints()>mob.maxState().getHitPoints())
				    usageCosts[2]=mob.baseState().getHitPoints();
			}
			else
			if(consumed>(Integer.MAX_VALUE-100))
				usageCosts[0]=(int)Math.round(Util.mul(mob.maxState().getHitPoints(),Util.div((Integer.MAX_VALUE-consumed),100.0)));
		}
		return usageCosts;
	}

	public int[] usageCost(MOB mob)
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

		int diff=0;
		int lowest=Integer.MAX_VALUE;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			CharClass C=mob.charStats().getMyClass(c);
			int qualifyingLevel=CMAble.getQualifyingLevel(C.ID(),true,ID());
			int classLevel=mob.charStats().getClassLevel(C.ID());
			if((qualifyingLevel>=0)&&(classLevel>=qualifyingLevel))
			{
				diff+=(classLevel-qualifyingLevel);
				if(qualifyingLevel<lowest) lowest=qualifyingLevel;
			}
		}
		if(lowest==Integer.MAX_VALUE)
		{
			lowest=CMAble.lowestQualifyingLevel(ID());
			if(lowest<0) lowest=0;
		}

		int consumed=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACOST);
		if(consumed<0) consumed=50+lowest;
		int minimum=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANAMINCOST);
		if(minimum<0){ minimum=lowest; if(minimum<5) minimum=5;}
		if(diff>0) consumed=consumed - (consumed /10 * diff);
		if(consumed<minimum) consumed=minimum;
		if(overrideMana()>=0) consumed=overrideMana();
		return buildCostArray(mob,consumed);
	}

	public void helpProfficiency(MOB mob)
	{
		if(mob==null) return;
		Ability A=mob.fetchAbility(ID());
		if((A==null)||(A.isBorrowed(mob))) return;

		if((System.currentTimeMillis()
		-((StdAbility)A).lastProfHelp)<60000)
			return;

		if(A.profficiency()<100)
		{
			if(((int)Math.round(Math.sqrt(new Integer(mob.charStats().getStat(CharStats.INTELLIGENCE)).doubleValue())*34.0*Math.random()))>=A.profficiency())
			{
			    int qualLevel=CMAble.qualifyingLevel(mob,A);
			    if((qualLevel<0)||(qualLevel>30)||(Dice.rollPercentage()<(int)Math.round(100.0*Util.div(31-qualLevel,30+qualLevel))))
			    {
					// very important, since these can be autoinvoked affects (copies)!
					A.setProfficiency(A.profficiency()+1);
					if((this!=A)&&(profficiency()<100))
						setProfficiency(profficiency()+1);
					if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOIMPROVE))
						mob.tell("You become better at "+A.name()+".");
					((StdAbility)A).lastProfHelp=System.currentTimeMillis();
			    }
			}
		}
		else
			A.setProfficiency(100);
	}

	public boolean invoke(MOB mob, Environmental target, boolean auto, int asLevel)
	{
		Vector V=new Vector();
		if(target!=null)
			V.addElement(target.name());
		return invoke(mob,V,target,auto,asLevel);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		if(!auto)
		{
			isAnAutoEffect=false;

			// if you can't move, you can't cast! Not even verbal!
			if(!Sense.aliveAwakeMobile(mob,false))
				return false;
			
			if(Util.bset(usageType(),Ability.USAGE_MOVEMENT)
			   &&(Sense.isBound(mob)))
			{
				mob.tell("You are bound!");
				return false;
			}

			int[] consumed=usageCost(mob);
			if(mob.curState().getMana()<consumed[0])
			{
				if(mob.maxState().getMana()==consumed[0])
					mob.tell("You must be at full mana to do that.");
				else
					mob.tell("You don't have enough mana to do that.");
				return false;
			}
			mob.curState().adjMana(-consumed[0],mob.maxState());
			if(mob.curState().getMovement()<consumed[1])
			{
				if(mob.maxState().getMovement()==consumed[1])
					mob.tell("You must be at full movement to do that.");
				else
					mob.tell("You don't have enough movement to do that.  You are too tired.");
				return false;
			}
			mob.curState().adjMovement(-consumed[1],mob.maxState());
			if(mob.curState().getHitPoints()<consumed[2])
			{
				if(mob.maxState().getHitPoints()==consumed[2])
					mob.tell("You must be at full health to do that.");
				else
					mob.tell("You don't have enough hit points to do that.");
				return false;
			}
			mob.curState().adjHitPoints(-consumed[2],mob.maxState());
			helpProfficiency(mob);
		}
		else
			isAnAutoEffect=true;
		return true;
	}

	public HashSet properTargets(MOB mob, Environmental givenTarget, boolean auto)
	{
		HashSet h=MUDFight.properTargets(this,mob,auto);
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
		{
			if(h==null) h=new HashSet();
			if(!h.contains(givenTarget))
				h.add(givenTarget);
		}
		return h;
	}


	public boolean maliciousAffect(MOB mob,
								   Environmental target,
								   int asLevel,
								   int tickAdjustmentFromStandard,
								   int additionAffectCheckCode)
	{
		boolean ok=true;
		if(mob.location()==null) return false;
		if(additionAffectCheckCode>=0)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.NO_EFFECT,additionAffectCheckCode,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				ok=(msg.value()<=0);
			}
			else
				ok=false;
		}
		if(ok)
		{
			invoker=mob;
			Ability newOne=(Ability)copyOf();
			((StdAbility)newOne).canBeUninvoked=true;
			if(tickAdjustmentFromStandard<=0)
			{
				tickAdjustmentFromStandard=(adjustedLevel(mob,asLevel)*2)+25;
				if((target!=null)&&(asLevel<=0)&&(mob!=null))
					tickAdjustmentFromStandard=(int)Math.round(Util.mul(tickAdjustmentFromStandard,Util.div(mob.envStats().level(),target.envStats().level())));

				if(tickAdjustmentFromStandard>(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)))
					tickAdjustmentFromStandard=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY));

				if(tickAdjustmentFromStandard<2)
					tickAdjustmentFromStandard=2;
			}

			newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		}
		return ok;
	}

	public boolean beneficialWordsFizzle(MOB mob,
										Environmental target,
										String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T"+message+"^?");
		if(mob.location()==null) return false;
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}

	public boolean beneficialVisualFizzle(MOB mob,
										  Environmental target,
										  String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,message);
		if(mob.location()==null) return false;
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}

	public boolean maliciousFizzle(MOB mob,
								   Environmental target,
									String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL|CMMsg.MASK_MALICIOUS,message);
		if(mob.location()==null) return false;
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}


	public boolean beneficialAffect(MOB mob,
								   Environmental target,
								   int asLevel,
								   int tickAdjustmentFromStandard)
	{
		boolean ok=true;
		if(ok)
		{
			invoker=mob;
			Ability newOne=(Ability)this.copyOf();
			((StdAbility)newOne).canBeUninvoked=true;

			if(tickAdjustmentFromStandard<=0)
			{
				tickAdjustmentFromStandard=(adjustedLevel(mob,asLevel)*7)+60;
				if(tickAdjustmentFromStandard>(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)))
					tickAdjustmentFromStandard=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY));
				if(tickAdjustmentFromStandard<5)
					tickAdjustmentFromStandard=5;
			}

			newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		}
		return ok;
	}

	public void spreadImmunity(MOB mob)
	{
	    if((mob==null)||(mob.fetchEffect(ID())!=null))
	        return;
		Ability A=mob.fetchEffect("TemporaryImmunity");
		if(A==null)
		{
			A=CMClass.getAbility("TemporaryImmunity");
			A.setBorrowed(mob,true);
			A.makeLongLasting();
			mob.addEffect(A);
			A.makeLongLasting();
		}
		A.setMiscText("+"+ID());
	}
	
	public boolean autoInvocation(MOB mob)
	{
		if(isAutoInvoked())
		{
			Ability thisAbility=mob.fetchEffect(ID());
			if(thisAbility!=null) return false;
			Ability thatAbility=(Ability)copyOf();
			((StdAbility)thatAbility).canBeUninvoked=true;
			thatAbility.setBorrowed(mob,true);
			mob.addEffect(thatAbility);
			return true;
		}
		return false;
	}
	public void makeNonUninvokable()
	{
		unInvoked=false;
		canBeUninvoked=false;
		borrowed=false;
	}

	public String accountForYourself(){return name();}
	public int getTickDownRemaining(){return tickDown;}
	public void setTickDownRemaining(int newTick){tickDown=newTick;}
	public long getTickStatus(){ return Tickable.STATUS_NOT;}

	public boolean canBeTaughtBy(MOB teacher, MOB student)
	{
		if(Util.bset(teacher.getBitmap(),MOB.ATT_NOTEACH))
		{
			teacher.tell("You are refusing to teach right now.");
			student.tell(teacher.name()+" is refusing to teach right now.");
			return false;
		}
		if(Sense.isSleeping(teacher)||Sense.isSitting(teacher))
		{
		    teacher.tell("You need to stand up to teach.");
		    student.tell(teacher.name()+" needs to stand up to teach.");
		    return false;
		}
		if(teacher.isInCombat())
		{
		    student.tell(teacher.name()+" seems busy right now.");
		    teacher.tell("Not while you are fighting!");
		    return false;
		}
		Ability yourAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<25)
			{
				teacher.tell("You are not profficient enough to teach '"+name()+"'");
				student.tell(teacher.name()+" is not profficient enough to teach '"+name()+"'.");
				return false;
			}
			return true;
		}
		teacher.tell("You don't know '"+name()+"'.");
		student.tell(teacher.name()+" doesn't know '"+name()+"'.");
		return false;
	}

	protected boolean ableOk(MOB mob, MOB target, CMMsg msg)
	{
		if((mob==null)||(mob.location()==null))
			return false;

		if((target==null)
		||(target.location()==null)
		||(target.location()==mob.location()))
			return mob.location().okMessage(mob,msg);

		boolean ok=mob.location().okMessage(mob,msg);
		if(!ok) return false;
		return target.okMessage(mob,msg);
	}

	protected void ableSend(MOB mob, MOB target, CMMsg msg)
	{
		if((mob==null)||(mob.location()==null))
			return;

		if((target==null)
		||(target.location()==null)
		||(target.location()==mob.location()))
		{
			mob.location().send(mob,msg);
			return;
		}

		mob.location().send(mob,msg);
		target.executeMsg(mob,msg);
	}


	public String requirements()
	{
		String returnable="";
		if(trainsRequired()==1)
			returnable="1 train";
		else
		if(trainsRequired()>1)
			returnable=trainsRequired()+" trains";
		if((returnable.length()>0)&&(practicesRequired()>0))
			returnable+=", ";
		if(practicesRequired()==1)
			returnable+="1 practice";
		else
		if(practicesRequired()>1)
			returnable+=practicesRequired()+" practices";
		if(returnable.length()==0)
			return "free!";
		else
			return returnable;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesRequired())
		{
			teacher.tell(student.name()+" does not have enough practice points to learn '"+name()+"'.");
			student.tell("You do not have enough practice points.");
			return false;
		}
		if(student.getTrains()<trainsRequired())
		{
			teacher.tell(student.name()+" does not have enough training sessions to learn '"+name()+"'.");
			student.tell("You do not have enough training sessions.");
			return false;
		}
		if((Util.bset(student.getBitmap(),MOB.ATT_NOTEACH))
		&&((!student.isMonster())||(!student.willFollowOrdersOf(teacher))))
		{
			teacher.tell(student.name()+" is refusing training at this time.");
			student.tell("You are refusing training at this time.");
			return false;
		}
		int qLevel=CMAble.qualifyingLevel(student,this);
		if(qLevel<0)
		{
			teacher.tell(student.name()+" is not the right class to learn '"+name()+"'.");
			student.tell("You are not the right class to learn '"+name()+"'.");
			return false;
		}
		if((!student.charStats().getCurrentClass().leveless())
		&&(!CMAble.qualifiesByLevel(student,this))
		&&(!CMSecurity.isDisabled("LEVELS")))
		{
			teacher.tell(student.name()+" is not high enough level to learn '"+name()+"'.");
			student.tell("You are not high enough level to learn '"+name()+"'.");
			return false;
		}
		if(student.charStats().getStat(CharStats.INTELLIGENCE)<2)
		{
			teacher.tell(student.name()+" is too stupid to learn '"+name()+"'.");
			student.tell("You are too stupid to learn '"+name()+"'.");
			return false;
		}
		if(qLevel>(student.charStats().getStat(CharStats.INTELLIGENCE)+15))
		{
			teacher.tell(student.name()+" is not smart enough to learn '"+name()+"'.");
			student.tell("You are not of high enough intelligence to learn '"+name()+"'.");
			return false;
		}
		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			teacher.tell(student.name()+" already knows '"+name()+"'.");
			student.tell("You already know '"+name()+"'.");
			return false;
		}

		if(teacherAbility!=null)
		{
			if(teacherAbility.profficiency()<25)
			{
				teacher.tell("You aren't profficient enough to teach '"+name()+"'.");
				student.tell(teacher.name()+" isn't profficient enough to teach you '"+name()+"'.");
				return false;
			}
		}
		else
		{
			student.tell(teacher.name()+" does not know anything about that.");
			teacher.tell("You don't know that.");
			return false;
		}
		if(student.isInCombat())
		{
		    teacher.tell(student.name()+" seems busy right now.");
		    student.tell("Not while you are fighting!");
		    return false;
		}

		if(Sense.isSleeping(student)||Sense.isSitting(student))
		{
			student.tell("You need to stand up and be alert to learn.");
		    teacher.tell(student.name()+" needs to stand up to be taught about that.");
		    return false;
		}
		return true;
	}

	public boolean canBePracticedBy(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice())
		{
			teacher.tell(student.name()+" does not have enough practices to practice '"+name()+"'.");
			student.tell("You do not have enough practices.");
			return false;
		}

		if(Util.bset(teacher.getBitmap(),MOB.ATT_NOTEACH))
		{
			teacher.tell("You are refusing to teach right now.");
			student.tell(teacher.name()+" is refusing to teach right now.");
			return false;
		}
		if((Util.bset(student.getBitmap(),MOB.ATT_NOTEACH))
		&&((!student.isMonster())||(!student.willFollowOrdersOf(teacher))))
		{
			teacher.tell(student.name()+" is refusing training at this time.");
			student.tell("You are refusing training at this time.");
			return false;
		}

		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if(yourAbility==null)
		{
			teacher.tell(student.name()+" has not gained '"+name()+"' yet.");
			student.tell("You havn't gained '"+name()+"' yet.");
			return false;
		}

		if(teacherAbility==null)
		{
			student.tell(teacher.name()+" does not know anything about '"+name()+"'.");
			teacher.tell("You don't know '"+name()+"'.");
			return false;
		}

		if(yourAbility.profficiency()>teacherAbility.profficiency())
		{
			teacher.tell("You aren't profficient enough to teach any more about '"+name()+"'.");
			student.tell(teacher.name()+" isn't profficient enough to teach any more about '"+name()+"'.");
			return false;
		}
		else
		if(yourAbility.profficiency()>74)
		{
			teacher.tell("You can't teach "+student.charStats().himher()+" any more about '"+name()+"'.");
			student.tell("You can't learn any more about '"+name()+"' except through dilligence.");
			return false;
		}

		if(teacherAbility.profficiency()<25)
		{
			teacher.tell("You aren't profficient enough to teach '"+name()+"'.");
			student.tell(teacher.name()+" isn't profficient enough to teach you '"+name()+"'.");
			return false;
		}
		if(Sense.isSleeping(student)||Sense.isSitting(student))
		{
			student.tell("You need to stand up to practice.");
		    teacher.tell(student.name()+" needs to stand up to practice that.");
		    return false;
		}
		if(student.isInCombat())
		{
		    teacher.tell(student.name()+" seems busy right now.");
		    student.tell("Not while you are fighting!");
		    return false;
		}

		return true;
	}


	public void teach(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesRequired())
			return;
		if(student.getTrains()<trainsRequired())
			return;
		if(student.fetchAbility(ID())==null)
		{
			student.setPractices(student.getPractices()-practicesRequired());
			student.setTrains(student.getTrains()-trainsRequired());
			Ability newAbility=(Ability)newInstance();
			newAbility.setProfficiency((int)Math.round(Util.mul(profficiency(),((Util.div(teacher.charStats().getStat(CharStats.WISDOM)+student.charStats().getStat(CharStats.INTELLIGENCE),100.0))))));
			if(newAbility.profficiency()>75)
				newAbility.setProfficiency(75);
			student.addAbility(newAbility);
			newAbility.autoInvocation(student);
		}
	}

	public void practice(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice())
			return;

		Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<75)
			{
				student.setPractices(student.getPractices()-practicesToPractice());
				yourAbility.setProfficiency(yourAbility.profficiency()+(int)Math.round(25.0*(Util.div(teacher.charStats().getStat(CharStats.WISDOM)+student.charStats().getStat(CharStats.INTELLIGENCE),36.0))));
				if(yourAbility.profficiency()>75)
					yourAbility.setProfficiency(75);
			}
		}
	}
	public void makeLongLasting()
	{
		tickDown=Integer.MAX_VALUE;
	}


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		return;
	}

	/** this method is used to tell the system whether
	 * a PENDING message may take place
	 */
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		return true;
	}

	/**
	 * this method allows any environmental object
	 * to behave according to a timed response.  by
	 * default, it will never be called unless the
	 * object uses the ServiceEngine to setup service.
	 * The tickID allows granularity with the type
	 * of service being requested.
	 */
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if((tickID==MudHost.TICK_MOB)
		&&(tickDown!=Integer.MAX_VALUE)
		&&(canBeUninvoked()))
		{
			if(tickDown<0)
				return !unInvoked;
			else
			{
				tickDown-=1;
				if(tickDown<=0)
				{
					tickDown=-1;
					unInvoke();
					return false;
				}
			}
		}
		return true;
	}
	public boolean appropriateToMyAlignment(int alignment){return true;}

	public void addEffect(Ability to){}
	public void addNonUninvokableEffect(Ability to){}
	public void delEffect(Ability to){}
	public int numEffects(){ return 0;}
	public Ability fetchEffect(int index){return null;}
	public Ability fetchEffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
	public boolean isGeneric(){return false;}

	private static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setMiscText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdAbility)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
