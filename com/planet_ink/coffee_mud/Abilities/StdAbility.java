package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class StdAbility implements Ability
{
	public String ID() { return "StdAbility"; }
	public String Name(){return name();}
	public String name(){ return "an ability";}
	public String description(){return "&";}
	public String displayText(){return "Affected list display for "+ID();}
	public String image(){return "";}
    public String rawImage(){return "";}
	public void setImage(String newImage){}
	public static final String[] empty={};
	public String[] triggerStrings(){return empty;}
	public int maxRange(){return adjustedMaxInvokerRange(0);}
	public int minRange(){return 0;}
    public double castingTime(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFABLETIME),100.0);}
    public double combatCastingTime(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMABLETIME),100.0);}
	public boolean putInCommandlist(){return true;}
	public boolean isAutoInvoked(){return false;}
	public boolean bubbleAffect(){return false;}
	public int getTicksBetweenCasts() { return 0;}
	public long getTimeOfNextCast(){ return 0;}
	public void setTimeOfNextCast(long absoluteTime){};
	protected int iniTrainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLTRAINCOST);}
	protected int iniPracticesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLPRACCOST);}
	public int trainsRequired(MOB mob)
	{
		if(mob!=null)
		{
			Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if((O!=null)&&(O[AbilityMapper.AbilityMapping.COST_TRAIN]!=null))
				return O[AbilityMapper.AbilityMapping.COST_TRAIN].intValue();
		}
		return iniTrainsRequired();
	}
	public int practicesRequired(MOB mob)
	{
		if(mob!=null)
		{
			Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if((O!=null)&&(O[AbilityMapper.AbilityMapping.COST_PRAC]!=null))
				return O[AbilityMapper.AbilityMapping.COST_PRAC].intValue();
		}
		return iniPracticesRequired();
	}
	public int practicesToPractice(MOB mob)
	{
		if(mob!=null)
		{
			Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if((O!=null)&&(O[AbilityMapper.AbilityMapping.COST_PRACPRAC]!=null))
				return O[AbilityMapper.AbilityMapping.COST_PRACPRAC].intValue();
		}
		return iniPracticesToPractice();
	}
	protected int iniPracticesToPractice(){return 1;}
	public void setTimeOfNextCast(MOB caster)
	{
		long newTime=(getTicksBetweenCasts()*Tickable.TIME_TICK);
		double mul=1.0;
		mul -= (0.05 * (double)getXLEVELLevel(caster));
		mul -= (0.1 * (double)getXTIMELevel(caster));
		newTime=Math.round(CMath.mul(newTime,mul));
		setTimeOfNextCast(System.currentTimeMillis() +newTime);
	}
	
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public long flags(){return 0;}
	protected short[] expertise=null;
	public int usageType(){return USAGE_MANA;}
	protected int overrideMana(){return -1;} //-1=normal, Integer.MAX_VALUE=all, Integer.MAX_VALUE-100
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int enchantQuality(){return abstractQuality();}
    public void initializeClass(){}

    protected int castingQuality(MOB mob, Environmental target, int abstractQuality)
    {
        if((target!=null)&&(target.fetchEffect(ID())!=null))
            return Ability.QUALITY_INDIFFERENT;
        if(isAutoInvoked()) return Ability.QUALITY_INDIFFERENT;
        if((mob!=null)&&(target!=null)&&(mob.getVictim()==target))
        {
            if((minRange()>0)&&(mob.rangeToTarget()<minRange()))
                return Ability.QUALITY_INDIFFERENT;
            if(mob.rangeToTarget()>maxRange())
                return Ability.QUALITY_INDIFFERENT;
            
        }
        switch(abstractQuality)
        {
        case Ability.QUALITY_BENEFICIAL_OTHERS:
            if(mob==target) return Ability.QUALITY_BENEFICIAL_SELF;
            return Ability.QUALITY_BENEFICIAL_OTHERS;
        case Ability.QUALITY_MALICIOUS:
            return Ability.QUALITY_MALICIOUS;
        case Ability.QUALITY_BENEFICIAL_SELF:
            if((target instanceof MOB)&&(mob!=target)) return Ability.QUALITY_INDIFFERENT;
            return Ability.QUALITY_BENEFICIAL_SELF;
        default:
            return Ability.QUALITY_INDIFFERENT;
        }
    }

    public int castingQuality(MOB mob, Environmental target)
	{
        return castingQuality(mob,target,abstractQuality());
	}

    protected synchronized int expertise(MOB mob, int code)
    {
    	if((mob!=null)&&(this.isNowAnAutoEffect()||(this.canBeUninvoked())))
    	{
	    	if(expertise!=null) return expertise[code];
			expertise=new short[ExpertiseLibrary.NUM_XFLAGS];
			for(int x=0;x<ExpertiseLibrary.NUM_XFLAGS;x++)
				expertise[x]=(short)CMLib.expertises().getApplicableExpertiseLevel(ID(),x,mob);
	    	return expertise[code];
    	}
    	expertise=null;
    	return 0;
    }
    
    public synchronized void clearExpertiseCache()
    {
    	expertise=null;
    }
    
    protected int getX1Level(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_X1);}
    protected int getX2Level(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_X2);}
    protected int getX3Level(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_X3);}
    protected int getX4Level(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_X4);}
    protected int getX5Level(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_X5);}
    protected int getXLEVELLevel(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_LEVEL);}
    protected int getXLOWCOSTLevel(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_LOWCOST);}
    protected int getXMAXRANGELevel(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_MAXRANGE);}
    protected int getXTIMELevel(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_TIME);}
    protected int getXPCOSTLevel(MOB mob){return expertise(mob,ExpertiseLibrary.XFLAG_XPCOST);}

    protected int getXPCOSTAdjustment(MOB mob, int xpLoss){
    	int xLevel=getXPCOSTLevel(mob);
    	if(xLevel<=0) return xpLoss;
    	return xpLoss-(int)Math.round(CMath.mul(xpLoss,CMath.mul(.05,xLevel)));
    }

    public int adjustedMaxInvokerRange(int max)
    {
        if(invoker==null) return max;
        int level=getXMAXRANGELevel(invoker);
        if(level<=0) return  max;
        return max+(int)Math.round(Math.ceil(CMath.mul(max,CMath.mul(level,0.2))));
    }



	/**
	 * Designates whether, when used as a property/effect, what sort of objects this
	 * ability can affect. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can affect
	 */
	protected int canAffectCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}
	/**
	 * Designates whether, when invoked as a skill, what sort of objects this
	 * ability can effectively target. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can target
	 */
	protected int canTargetCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}

	protected boolean isAnAutoEffect=false;
	protected int proficiency=0;
	protected boolean savable=true;
	public String miscText="";
	protected MOB invoker=null;
	protected Environmental affected=null;
	protected boolean canBeUninvoked=true;
	protected boolean unInvoked=false;
	protected int tickDown=-1;
	protected long lastCastHelp=0;

	public StdAbility()
	{
        super();
        CMClass.bumpCounter(this,CMClass.OBJECT_ABILITY);
	}
    protected void finalize(){ CMClass.unbumpCounter(this,CMClass.OBJECT_ABILITY); }

	public CMObject newInstance()
	{
		try
        {
			expertise=null;
			return (CMObject)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdAbility();
	}
	public int classificationCode(){ return Ability.ACODE_SKILL; }

	protected static final EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	public EnvStats envStats(){return envStats;}
	public EnvStats baseEnvStats(){return envStats;}


	public long expirationDate()
	{
	    return ((long)tickDown) * Tickable.TIME_TICK;
	}
	public void setExpirationDate(long time){
		if(time>System.currentTimeMillis())
			tickDown=(int)((time-System.currentTimeMillis())/Tickable.TIME_TICK);
	}
	public boolean isNowAnAutoEffect(){ return isAnAutoEffect; }
    public boolean savable(){ return savable;}
    public void setSavable(boolean truefalse)   { savable=truefalse; }
    protected boolean amDestroyed=false;
    public void destroy(){amDestroyed=true; affected=null; invoker=null; miscText=null; }
    public boolean amDestroyed(){return amDestroyed;}
	public void setName(String newName){}
	public void recoverEnvStats() {}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}
	public int abilityCode(){return 0;}
	public void setAbilityCode(int newCode){}
	public Vector externalFiles(){return null;}
    protected long minCastWaitTime(){return 0;}

	// ** For most abilities, the following stuff actually matters */
	public void setMiscText(String newMiscText)	{ miscText=newMiscText;}
	public String text(){ return miscText;}
	public int proficiency(){ return proficiency;}
	public void setProficiency(int newProficiency)
	{
		proficiency=newProficiency;
		if(proficiency>100) proficiency=100;
	}

	protected int addedTickTime(MOB invokerMOB, int baseTickTime)
	{
		return (int)Math.round(CMath.mul(baseTickTime,CMath.mul(getXTIMELevel(invokerMOB),0.20)));
	}

	public void startTickDown(MOB invokerMOB, Environmental affected, int tickTime)
	{
		if(invokerMOB!=null) invoker=invokerMOB;

		savable=false; // makes it so that the effect does not save!

        tickTime+=addedTickTime(invokerMOB,tickTime);
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
			CMLib.threads().startTickDown(this,Tickable.TICKID_MOB,1);
		}
		tickDown=tickTime;
	}

	protected boolean disregardsArmorCheck(MOB mob)
	{
		return ((mob==null)
				||(mob.isMonster())
				||(CMLib.ableMapper().qualifiesByLevel(mob,this)));
	}

	public int adjustedLevel(MOB caster, int asLevel)
	{
		if(caster==null) return 1;
		int lowestQualifyingLevel=CMLib.ableMapper().lowestQualifyingLevel(this.ID());
        int adjLevel=lowestQualifyingLevel;
        if(asLevel<=0)
        {
    		int qualifyingLevel=CMLib.ableMapper().qualifyingLevel(caster,this);
    		if((caster.isMonster())||(qualifyingLevel>=0))
    			adjLevel+=(CMLib.ableMapper().qualifyingClassLevel(caster,this)-qualifyingLevel);
    		else
    			adjLevel=caster.envStats().level()-lowestQualifyingLevel-25;
        }
        else
        	adjLevel=asLevel;
		if(adjLevel<lowestQualifyingLevel)
			adjLevel=lowestQualifyingLevel;
		if(adjLevel<1) return 1;
		return adjLevel+getXLEVELLevel(caster);
	}

	public int experienceLevels(MOB caster, int asLevel)
	{
		if(caster==null) return 1;
		int adjLevel=1;
		int qualifyingLevel=CMLib.ableMapper().qualifyingLevel(caster,this);
		int lowestQualifyingLevel=CMLib.ableMapper().lowestQualifyingLevel(this.ID());
		if(qualifyingLevel>=0)
		{
			int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(caster,this);
			if(qualClassLevel>=qualifyingLevel)
				adjLevel=(qualClassLevel-qualifyingLevel)+1;
			else
			if(caster.envStats().level()>=qualifyingLevel)
				adjLevel=(caster.envStats().level()-qualifyingLevel)+1;
			else
			if(caster.envStats().level()>=lowestQualifyingLevel)
				adjLevel=(caster.envStats().level()-lowestQualifyingLevel)+1;
		}
		else
		if(caster.envStats().level()>=lowestQualifyingLevel)
			adjLevel=(caster.envStats().level()-lowestQualifyingLevel)+1;
		if(asLevel>0) adjLevel=asLevel;
		if(adjLevel<1) return 1;
		return adjLevel+getXLEVELLevel(caster);
	}

	public boolean canTarget(int can_code){return CMath.bset(canTargetCode(),can_code);}
	public boolean canAffect(int can_code){return CMath.bset(canAffectCode(),can_code);}
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
		String targetName=CMParms.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(castingQuality(mob,mob.getVictim())==Ability.QUALITY_MALICIOUS)&&(mob.getVictim()!=null))
		   target=mob.getVictim();
		else
		if((targetName.length()==0)&&(castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF))
			target=mob;
		else
		if((targetName.length()==0)&&(abstractQuality()!=Ability.QUALITY_MALICIOUS))
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
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Wearable.FILTER_UNWORNONLY);
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
		||((givenTarget==null)&&(!CMLib.flags().canBeSeenBy(target,mob))&&((!CMLib.flags().canBeHeardBy(target,mob))||(!target.isInCombat()))))
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
									  int wornFilter)
	{ return getAnyTarget(mob,commands,givenTarget,wornFilter,false,false);}

	public Environmental getAnyTarget(MOB mob,
						  Vector commands,
						  Environmental givenTarget,
						  int wornFilter,
						  boolean checkOthersInventory)
	{ return getAnyTarget(mob,commands,givenTarget,wornFilter,checkOthersInventory,false);}

	public Environmental getAnyTarget(MOB mob,
									  Vector commands,
									  Environmental givenTarget,
									  int wornFilter,
									  boolean checkOthersInventory,
									  boolean alreadyAffOk)
	{
		String targetName=CMParms.combine(commands,0);
		Environmental target=null;
		if(givenTarget!=null)
			target=givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(castingQuality(mob,mob.getVictim())==Ability.QUALITY_MALICIOUS))
			target=mob.getVictim();
		else
		if(targetName.equalsIgnoreCase("self")||targetName.equalsIgnoreCase("me"))
		   target=mob;
		else
		if(mob.location()!=null)
		{
			target=mob.location().fetchFromRoomFavorMOBs(null,targetName, wornFilter);
			if(target==null)
				target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,targetName,wornFilter);
			if((target==null)
			&&(targetName.equalsIgnoreCase("room")
				||targetName.equalsIgnoreCase("here")
				||targetName.equalsIgnoreCase("place")))
				target=mob.location();
			if((target==null)&&(checkOthersInventory))
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					target=M.fetchInventory(null,targetName);
					if(target!=null)
					{
						switch(wornFilter)
						{
						case Wearable.FILTER_UNWORNONLY:
							if(!((Item)target).amWearingAt(Wearable.IN_INVENTORY))
								continue;
							break;
						case Wearable.FILTER_WORNONLY:
							if(((Item)target).amWearingAt(Wearable.IN_INVENTORY))
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
		   &&(!CMLib.flags().canBeSeenBy(target,mob))
		   &&((!CMLib.flags().canBeHeardBy(target,mob))
                   ||((target instanceof MOB)&&(!((MOB)target).isInCombat())))))
		{
			if(targetName.trim().length()==0)
				mob.tell("You don't see that here.");
			else
			if(!CMLib.flags().isSleeping(mob))
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

	protected static Item possibleContainer(MOB mob, Vector commands, boolean withStuff, int wornFilter)
	{
		if((commands==null)||(commands.size()<2))
			return null;

		String possibleContainerID=(String)commands.lastElement();
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornFilter);
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

	public Item getTarget(MOB mob, Room location, Environmental givenTarget, Vector commands, int wornFilter)
	{ return getTarget(mob,location,givenTarget,null,commands,wornFilter);}
	public Item getTarget(MOB mob, Room location, Environmental givenTarget, Item container, Vector commands, int wornFilter)
	{
		String targetName=CMParms.combine(commands,0);

		Environmental target=null;
		if((givenTarget!=null)&&(givenTarget instanceof Item))
			target=givenTarget;

		if((location!=null)&&(target==null)&&(targetName.length()>0))
			target=location.fetchFromRoomFavorItems(container,targetName,wornFilter);
		if((target==null)&&(targetName.length()>0))
		{
			if(location!=null)
				target=location.fetchFromMOBRoomFavorsItems(mob,container,targetName,wornFilter);
			else
            switch(wornFilter)
            {
            case Wearable.FILTER_ANY:
                target=mob.fetchInventory(container,targetName);
                break;
            case Wearable.FILTER_UNWORNONLY:
                target=mob.fetchCarried(container,targetName);
                break;
            case Wearable.FILTER_WORNONLY:
                target=mob.fetchWornItem(targetName);
                break;
            }
		}
		if(target!=null) targetName=target.name();

		if((target==null)
		||(!(target instanceof Item))
		||((givenTarget==null)&&(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			if(targetName.length()==0)
				mob.tell("You need to be more specific.");
			else
			if((target==null)||(target instanceof Item))
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see that here.");
				else
				if(!CMLib.flags().isSleeping(mob))
					mob.tell("You don't see anything called '"+targetName+"' here.");
			}
			else
				mob.tell(mob,target,null,"You can't do that to <T-NAMESELF>.");
			return null;
		}
		return (Item)target;
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	protected void cloneFix(Ability E){expertise=null;}
	public CMObject copyOf()
	{
		try
		{
			StdAbility E=(StdAbility)this.clone();
            CMClass.bumpCounter(E,CMClass.OBJECT_ABILITY);
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto)
	{
		if(auto)
		{
			isAnAutoEffect=true;
			setProficiency(100);
			return true;
		}

		if((mob!=null)&&CMSecurity.isAllowed(mob,mob.location(),"SUPERSKILL"))
		   return true;

		isAnAutoEffect=false;
		int pctChance=proficiency();
		if(pctChance>95) pctChance=95;
		if(pctChance<5) pctChance=5;

		if(adjustment>=0)
			pctChance+=adjustment;
		else
		if(CMLib.dice().rollPercentage()>(100+adjustment))
			return false;
		return (CMLib.dice().rollPercentage()<pctChance);
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

	protected int[] buildCostArray(MOB mob, int consumed, int minimum)
	{
		int[] usageCosts=new int[3];
		int costDown=0;
		if(consumed>2)
		{
			costDown=getXLOWCOSTLevel(mob);
			if(costDown>=consumed)
				costDown=consumed/2;
            minimum=minimum-costDown;
            if(minimum<5) minimum=5;
		}
		boolean useMana=CMath.bset(usageType(),Ability.USAGE_MANA);
		boolean useMoves=CMath.bset(usageType(),Ability.USAGE_MOVEMENT);
		boolean useHits=CMath.bset(usageType(),Ability.USAGE_HITPOINTS);
		int divider=1;
		if((useMana)&&(useMoves)&&(useHits)) divider=3;
		else
		if((useMana)&&(useMoves)&&(!useHits)) divider=2;
		else
		if((useMana)&&(!useMoves)&&(useHits)) divider=2;
		else
		if((!useMana)&&(useMoves)&&(useHits)) divider=2;

		if(useMana){
			if(consumed==Integer.MAX_VALUE)
			{
				usageCosts[0]=mob.maxState().getMana()-costDown;
				if(mob.baseState().getMana()>mob.maxState().getMana())
				    usageCosts[0]=mob.baseState().getMana()-costDown;
			}
			else
			if(consumed>(Integer.MAX_VALUE-100))
				usageCosts[0]=(int)Math.round(CMath.mul(mob.maxState().getMana(),CMath.div((Integer.MAX_VALUE-consumed),100.0)))-costDown;
			else
			{
				usageCosts[0]=(consumed-costDown)/divider;
				if(usageCosts[0]<minimum)	usageCosts[0]=minimum;
			}
		}
		if(useMoves){
			if(consumed==Integer.MAX_VALUE)
			{
				usageCosts[1]=mob.maxState().getMovement()-costDown;
				if(mob.baseState().getMovement()>mob.maxState().getMovement())
				    usageCosts[1]=mob.baseState().getMovement()-costDown;
			}
			else
			if(consumed>(Integer.MAX_VALUE-100))
				usageCosts[1]=(int)Math.round(CMath.mul(mob.maxState().getMovement(),CMath.div((Integer.MAX_VALUE-consumed),100.0)))-costDown;
			else
			{
				usageCosts[1]=(consumed-costDown)/divider;
				if(usageCosts[1]<minimum)	usageCosts[1]=minimum;
			}
		}
		if(useHits){
			if(consumed==Integer.MAX_VALUE)
			{
				usageCosts[2]=mob.maxState().getHitPoints()-costDown;
				if(mob.baseState().getHitPoints()>mob.maxState().getHitPoints())
				    usageCosts[2]=mob.baseState().getHitPoints()-costDown;
			}
			else
			if(consumed>(Integer.MAX_VALUE-100))
				usageCosts[2]=(int)Math.round(CMath.mul(mob.maxState().getHitPoints(),CMath.div((Integer.MAX_VALUE-consumed),100.0)))-costDown;
			else
			{
				usageCosts[2]=(consumed-costDown)/divider;
				if(usageCosts[2]<minimum)	usageCosts[2]=minimum;
			}
		}
		return usageCosts;
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

		int diff=0;
		int lowest=Integer.MAX_VALUE;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			CharClass C=mob.charStats().getMyClass(c);
			int qualifyingLevel=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,ID());
			int classLevel=mob.charStats().getClassLevel(C.ID());
			if((qualifyingLevel>=0)&&(classLevel>=qualifyingLevel))
			{
				diff+=(classLevel-qualifyingLevel);
				if(qualifyingLevel<lowest) lowest=qualifyingLevel;
			}
		}
		if(lowest==Integer.MAX_VALUE)
		{
			lowest=CMLib.ableMapper().lowestQualifyingLevel(ID());
			if(lowest<0) lowest=0;
		}

		Integer[] costOverrides=null;
		if(!ignoreClassOverride)
			costOverrides=CMLib.ableMapper().getCostOverrides(mob,ID());
		int consumed=CMProps.getMaxManaException(ID());
		if(consumed==Integer.MIN_VALUE) consumed=CMProps.getIntVar(CMProps.SYSTEMI_MANACOST);
		if(consumed<0) consumed=50+lowest;
		int minimum=CMProps.getMinManaException(ID());
		if(minimum==Integer.MIN_VALUE)
            minimum=CMProps.getIntVar(CMProps.SYSTEMI_MANAMINCOST);
		if(minimum<0){ minimum=lowest; if(minimum<5) minimum=5;}
		if(diff>0) consumed=consumed - (consumed /10 * diff);
		if(consumed<minimum)
			consumed=minimum;
		if(overrideMana()>=0) consumed=overrideMana();
		if((costOverrides!=null)&&(costOverrides[AbilityMapper.AbilityMapping.COST_MANA]!=null))
		{
			consumed=costOverrides[AbilityMapper.AbilityMapping.COST_MANA].intValue();
			if((consumed<minimum)&&(consumed>=0)) minimum=consumed;
		}
		return buildCostArray(mob,consumed,minimum);
	}

	public void helpProficiency(MOB mob)
	{
		if(mob==null) return;
		Ability A=mob.fetchAbility(ID());
		if((A==null)||(!A.savable())) return;

        if(!mob.isMonster()) CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_SKILLUSE);

		if((System.currentTimeMillis()
		-((StdAbility)A).lastCastHelp)<300000)
			return;

        if(!A.appropriateToMyFactions(mob))
            return;

        int maxProficiency = CMLib.ableMapper().getMaxProficiency(mob,true,ID()); 
		if(A.proficiency()< maxProficiency)
		{
			if(((int)Math.round(Math.sqrt(((double)mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)))*34.0*Math.random()))>=A.proficiency())
			{
			    int qualLevel=CMLib.ableMapper().qualifyingLevel(mob,A);
			    if((qualLevel<0)||(qualLevel>30)||(CMLib.dice().rollPercentage()<(int)Math.round(100.0*CMath.div(31-qualLevel,30+qualLevel))))
			    {
					// very important, since these can be autoinvoked affects (copies)!
					A.setProficiency(A.proficiency()+1);
					if((this!=A)&&(proficiency()<maxProficiency))
						setProficiency(proficiency()+1);
					if(CMath.bset(mob.getBitmap(),MOB.ATT_AUTOIMPROVE))
						mob.tell("You become better at "+A.name()+".");
					((StdAbility)A).lastCastHelp=System.currentTimeMillis();
			    }
			}
		}
		else
			A.setProficiency(maxProficiency);
	}

    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
    {
        return true;
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
		expertise=null;
        if((mob!=null)&&(getXMAXRANGELevel(mob)>0))
            invoker=mob;
		if((!auto)&&(mob!=null))
		{
			isAnAutoEffect=false;

			// if you can't move, you can't cast! Not even verbal!
			if(!CMLib.flags().aliveAwakeMobile(mob,false))
				return false;

			if((getTicksBetweenCasts()>0)
			&&(getTimeOfNextCast()>0)
			&&(System.currentTimeMillis()<getTimeOfNextCast())
			&&(mob.location()!=null)&&(mob.location().getArea()!=null))
			{
				TimeClock C=mob.location().getArea().getTimeObj();
				if(C!=null)
					mob.tell("You must wait "+C.deriveEllapsedTimeString(getTimeOfNextCast()-System.currentTimeMillis())+" before you can do that again.");
				return false;
			}
			
			if(CMath.bset(usageType(),Ability.USAGE_MOVEMENT)
			   &&(CMLib.flags().isBound(mob)))
			{
				mob.tell("You are bound!");
				return false;
			}

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

            if((minCastWaitTime()>0)&&(lastCastHelp>0))
            {
                if((System.currentTimeMillis()-lastCastHelp)<minCastWaitTime())
                {
                    if(minCastWaitTime()<=1000)
                        mob.tell("You need a second to recover before doing that again.");
                    else
                    if(minCastWaitTime()<=5000)
                        mob.tell("You need a few seconds to recover before doing that again.");
                    else
                        mob.tell("You need awhile to recover before doing that again.");
                    return false;
                }
            }
            if(!checkComponents(mob))
                return false;
            mob.curState().adjMana(-consumed[0],mob.maxState());
            mob.curState().adjMovement(-consumed[1],mob.maxState());
            mob.curState().adjHitPoints(-consumed[2],mob.maxState());
			helpProficiency(mob);
		}
		else
			isAnAutoEffect=true;
		return true;
	}

    public boolean checkComponents(MOB mob)
    {
        if((mob!=null)
        &&(mob.session()!=null)
        &&(mob.soulMate()==null)
        &&(!CMSecurity.isAllowed(mob,mob.location(),"COMPONENTS")))
        {
        	Vector<AbilityComponent> componentsRequirements=(Vector<AbilityComponent>)CMLib.ableMapper().getAbilityComponentMap().get(ID().toUpperCase());
            if(componentsRequirements!=null)
            {
                Vector components=CMLib.ableMapper().componentCheck(mob,componentsRequirements);
                if(components==null)
                {
                    mob.tell("You lack the necessary materials to use this "
                            +Ability.ACODE_DESCS[classificationCode()&Ability.ALL_ACODES].toLowerCase()
                            +", the requirements are: "
                            +CMLib.ableMapper().getAbilityComponentDesc(mob,ID())+".");
                    return false;
                }
                while(components.size()>0)
                {
                    int i=0;
                    boolean destroy=false;
                    for(;i<components.size();i++)
                        if(components.elementAt(i) instanceof Boolean)
                        { destroy=((Boolean)components.elementAt(i)).booleanValue(); break;}
                    while(i>=0)
                    {
                        if((destroy)&&(components.elementAt(0) instanceof Item))
                            ((Item)components.elementAt(0)).destroy();
                        components.removeElementAt(0);
                        i--;
                    }
                }
            }
        }
        return true;
    }

	public HashSet properTargets(MOB mob, Environmental givenTarget, boolean auto)
	{
		HashSet h=CMLib.combat().properTargets(this,mob,auto);
		if((givenTarget!=null)
        &&(givenTarget instanceof MOB)
        &&(CMLib.flags().isInTheGame(givenTarget,true)))
		{
			if(h==null) h=new HashSet();
			if(!h.contains(givenTarget))
				h.add(givenTarget);
		}
		return h;
	}

    public int getMaliciousTickdownTime(MOB mob, Environmental target, int tickAdjustmentFromStandard, int asLevel)
    {
        if(tickAdjustmentFromStandard<=0)
        {
            tickAdjustmentFromStandard=((int)Math.round(CMath.mul(adjustedLevel(mob,asLevel),1.3)))+25;
            if((target!=null)&&(asLevel<=0)&&(mob!=null)&&(!(target instanceof Room)))
                tickAdjustmentFromStandard=(int)Math.round(CMath.mul(tickAdjustmentFromStandard,CMath.div(mob.envStats().level(),target.envStats().level())));

            if((tickAdjustmentFromStandard>(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)))
            ||(mob instanceof Deity))
                tickAdjustmentFromStandard=(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY));

            if(tickAdjustmentFromStandard<2)
                tickAdjustmentFromStandard=2;
        }
        return tickAdjustmentFromStandard;
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
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.NO_EFFECT,additionAffectCheckCode,CMMsg.NO_EFFECT,null);
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
            tickAdjustmentFromStandard=getMaliciousTickdownTime(mob,target,tickAdjustmentFromStandard,asLevel);
			newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		}
		return ok;
	}

	public boolean beneficialWordsFizzle(MOB mob,
										Environmental target,
										String message)
	{
		// it didn't work, but tell everyone you tried.
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T"+message+"^?");
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
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,message);
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
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL|CMMsg.MASK_MALICIOUS,message);
		if(mob.location()==null) return false;
        CMLib.color().fixSourceFightColor(msg);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}


    public int getBeneficialTickdownTime(MOB mob, Environmental target, int tickAdjustmentFromStandard, int asLevel)
    {
        if(tickAdjustmentFromStandard<=0)
        {
            tickAdjustmentFromStandard=(adjustedLevel(mob,asLevel)*5)+60;
            if((tickAdjustmentFromStandard>(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)))
            ||(mob instanceof Deity))
                tickAdjustmentFromStandard=(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY));
            if(tickAdjustmentFromStandard<5)
                tickAdjustmentFromStandard=5;
        }
        return tickAdjustmentFromStandard;
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
            tickAdjustmentFromStandard=getBeneficialTickdownTime(mob,target,tickAdjustmentFromStandard,asLevel);
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
			A.setSavable(false);
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
			thatAbility.setSavable(false);
			mob.addEffect(thatAbility);
			return true;
		}
		return false;
	}
	public void makeNonUninvokable()
	{
		unInvoked=false;
		canBeUninvoked=false;
		savable=true;
	}

	public String accountForYourself(){return name();}
	public int getTickDownRemaining(){return tickDown;}
	public void setTickDownRemaining(int newTick){tickDown=newTick;}
	public long getTickStatus(){ return Tickable.STATUS_NOT;}

	public boolean canBeTaughtBy(MOB teacher, MOB student)
	{
		if(CMath.bset(teacher.getBitmap(),MOB.ATT_NOTEACH))
		{
			teacher.tell("You are refusing to teach right now.");
			student.tell(teacher.name()+" is refusing to teach right now.");
			return false;
		}
		if(CMLib.flags().isSleeping(teacher)||CMLib.flags().isSitting(teacher))
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
			int prof25=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), .25));
			if(yourAbility.proficiency()<prof25)
			{
				teacher.tell("You are not proficient enough to teach '"+name()+"'");
				student.tell(teacher.name()+" is not proficient enough to teach '"+name()+"'.");
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
		if(iniTrainsRequired()==1)
			returnable="1 train";
		else
		if(iniTrainsRequired()>1)
			returnable=iniTrainsRequired()+" trains";
		if((returnable.length()>0)&&(iniPracticesRequired()>0))
			returnable+=", ";
		if(iniPracticesRequired()==1)
			returnable+="1 practice";
		else
		if(iniPracticesRequired()>1)
			returnable+=iniPracticesRequired()+" practices";
		if(returnable.length()==0)
			return "free!";
		return returnable;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if((practicesRequired(student)>0)&&(student.getPractices()<practicesRequired(student)))
		{
			teacher.tell(student.name()+" does not have enough practice points to learn '"+name()+"'.");
			student.tell("You do not have enough practice points.");
			return false;
		}
		if((trainsRequired(student)>0)&&(student.getTrains()<trainsRequired(student)))
		{
			teacher.tell(student.name()+" does not have enough training sessions to learn '"+name()+"'.");
			student.tell("You do not have enough training sessions.");
			return false;
		}
		if((CMath.bset(student.getBitmap(),MOB.ATT_NOTEACH))
		&&((!student.isMonster())||(!student.willFollowOrdersOf(teacher))))
		{
			teacher.tell(student.name()+" is refusing training at this time.");
			student.tell("You are refusing training at this time.");
			return false;
		}
		int qLevel=CMLib.ableMapper().qualifyingLevel(student,this);
		if(qLevel<0)
		{
			teacher.tell(student.name()+" is not the right class to learn '"+name()+"'.");
			student.tell("You are not the right class to learn '"+name()+"'.");
			return false;
		}
		if((!student.charStats().getCurrentClass().leveless())
		&&(!CMLib.ableMapper().qualifiesByLevel(student,this))
		&&(!CMSecurity.isDisabled("LEVELS")))
		{
			teacher.tell(student.name()+" is not high enough level to learn '"+name()+"'.");
			student.tell("You are not high enough level to learn '"+name()+"'.");
			return false;
		}
		if(student.charStats().getStat(CharStats.STAT_INTELLIGENCE)<2)
		{
			teacher.tell(student.name()+" is too stupid to learn '"+name()+"'.");
			student.tell("You are too stupid to learn '"+name()+"'.");
			return false;
		}
		if(qLevel>(student.charStats().getStat(CharStats.STAT_INTELLIGENCE)+15))
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
			int prof25=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,teacherAbility.ID()), .25));
			if(teacherAbility.proficiency()<prof25)
			{
				teacher.tell("You aren't proficient enough to teach '"+name()+"'.");
				student.tell(teacher.name()+" isn't proficient enough to teach you '"+name()+"'.");
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

		if(CMLib.flags().isSleeping(student)||CMLib.flags().isSitting(student))
		{
			student.tell("You need to stand up and be alert to learn.");
		    teacher.tell(student.name()+" needs to stand up to be taught about that.");
		    return false;
		}

		String extraMask=CMLib.ableMapper().getApplicableMask(student,this);
		if((extraMask.length()>0)&&(!CMLib.masking().maskCheck(extraMask,student,true)))
		{
			String reason="requirements: "+CMLib.masking().maskDesc(extraMask);
			student.tell("You may not learn '"+name()+"' at this time due to the "+reason+".");
			teacher.tell(student.name()+" does not fit the '"+name()+"' "+reason+".");
			return false;
		}

		DVector prereqs=CMLib.ableMapper().getUnmetPreRequisites(student,this);
		if((prereqs!=null)&&(prereqs.size()>0))
		{
			String names=CMLib.ableMapper().formatPreRequisites(prereqs);
			student.tell("You must learn "+names+" before you can gain "+name()+".");
			teacher.tell(student.name()+" has net learned the pre-requisites to "+name()+" yet.");
			return false;
		}

		return true;
	}

	protected int verbalCastCode(MOB mob, Environmental target, boolean auto)
	{
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}

    protected int verbalCastMask(MOB mob,Environmental target, boolean auto)
    { return verbalCastCode(mob,target,auto)&CMMsg.MAJOR_MASK;}

	protected int somanticCastCode(MOB mob, Environmental target, boolean auto)
	{
		int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}
    protected int somanticCastMask(MOB mob,Environmental target, boolean auto)
    { return somanticCastCode(mob,target,auto)&CMMsg.MAJOR_MASK;}

	public boolean canBePracticedBy(MOB teacher, MOB student)
	{
		if((practicesToPractice(student)>0)&&(student.getPractices()<practicesToPractice(student)))
		{
			teacher.tell(student.name()+" does not have enough practices to practice '"+name()+"'.");
			student.tell("You do not have enough practices.");
			return false;
		}

		if(CMath.bset(teacher.getBitmap(),MOB.ATT_NOTEACH))
		{
			teacher.tell("You are refusing to teach right now.");
			student.tell(teacher.name()+" is refusing to teach right now.");
			return false;
		}
		if((CMath.bset(student.getBitmap(),MOB.ATT_NOTEACH))
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

		int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), .75));
		if(yourAbility.proficiency()>teacherAbility.proficiency())
		{
			teacher.tell("You aren't proficient enough to teach any more about '"+name()+"'.");
			student.tell(teacher.name()+" isn't proficient enough to teach any more about '"+name()+"'.");
			return false;
		}
		else
		if(yourAbility.proficiency()>prof75-1)
		{
			teacher.tell("You can't teach "+student.charStats().himher()+" any more about '"+name()+"'.");
			student.tell("You can't learn any more about '"+name()+"' except through dilligence.");
			return false;
		}

		int prof25=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,teacherAbility.ID()), .25));
		if(teacherAbility.proficiency()<prof25)
		{
			teacher.tell("You aren't proficient enough to teach '"+name()+"'.");
			student.tell(teacher.name()+" isn't proficient enough to teach you '"+name()+"'.");
			return false;
		}
		if(CMLib.flags().isSleeping(student)||CMLib.flags().isSitting(student))
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
		if((practicesRequired(student)>0)&&(student.getPractices()<practicesRequired(student)))
			return;
		if((trainsRequired(student)>0)&&(student.getTrains()<trainsRequired(student)))
			return;
		if(student.fetchAbility(ID())==null)
		{
			student.setPractices(student.getPractices()-practicesRequired(student));
			student.setTrains(student.getTrains()-trainsRequired(student));
			Ability newAbility=(Ability)newInstance();
			int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,newAbility.ID()), .75));
			newAbility.setProficiency((int)Math.round(CMath.mul(proficiency(),((CMath.div(teacher.charStats().getStat(CharStats.STAT_WISDOM)+student.charStats().getStat(CharStats.STAT_INTELLIGENCE),100.0))))));
			if(newAbility.proficiency()>prof75)
				newAbility.setProficiency(prof75);
			int defProficiency=CMLib.ableMapper().getDefaultProficiency(student.charStats().getCurrentClass().ID(),true,ID());
			if((defProficiency>0)&&(defProficiency>newAbility.proficiency()))
				newAbility.setProficiency(defProficiency);
			student.addAbility(newAbility);
			newAbility.autoInvocation(student);
		}
		student.recoverCharStats();
		student.recoverEnvStats();
		student.recoverMaxState();
	}

	public void practice(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice(student))
			return;

		Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), .75));
			if(yourAbility.proficiency()<prof75)
			{
				student.setPractices(student.getPractices()-practicesToPractice(student));
				yourAbility.setProficiency(yourAbility.proficiency()+(int)Math.round(25.0*(CMath.div(teacher.charStats().getStat(CharStats.STAT_WISDOM)+student.charStats().getStat(CharStats.STAT_INTELLIGENCE),36.0))));
				if(yourAbility.proficiency()>prof75)
					yourAbility.setProficiency(prof75);
			}
		}
	}
	public void makeLongLasting()
	{
		tickDown=Integer.MAX_VALUE;
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		return;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if((canBeUninvoked())
        &&(tickID==Tickable.TICKID_MOB)
        &&(tickDown!=Integer.MAX_VALUE))
		{
			if(tickDown<0)
				return !unInvoked;
			if((--tickDown)<=0)
			{
				tickDown=-1;
				unInvoke();
				return false;
			}
		}
		return true;
	}

    public boolean appropriateToMyFactions(MOB mob)
    {
        for(Enumeration e=mob.fetchFactions();e.hasMoreElements();)
        {
            String factionID=(String)e.nextElement();
            Faction F=CMLib.factions().getFaction(factionID);
            if((F!=null)&&F.hasUsage(this))
                return F.canUse(mob,this);
        }
        return true;
    }

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
    public void addScript(ScriptingEngine S){}
    public void delScript(ScriptingEngine S) {}
    public int numScripts(){return 0;}
    public ScriptingEngine fetchScript(int x){ return null;}
	public boolean isGeneric(){return false;}

	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
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
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
}
