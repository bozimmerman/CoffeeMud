package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.database.DBInterface;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


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
public class DefaultCharState implements CharState
{
    public String ID(){return "DefaultCharState";}
    protected int[] states={10,100,50,1000,500};
	protected long Fatigue=0;

	protected int botherCycle=0;
	protected int ticksHungry=0;
	protected int ticksThirsty=0;

	protected int annoyanceTicker=ANNOYANCE_DEFAULT_TICKS;

	public DefaultCharState(){}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCharState();}}
    public void initializeClass(){}
    public void setAllValues(int def)
	{
    	for(int i=0;i<states.length;i++)
    		states[i]=def;
		Fatigue=def;
	}

    public void copyInto(CharState intoState)
    {
    	if(intoState instanceof DefaultCharState)
    	{
    		for(int i=0;i<states.length;i++)
    			((DefaultCharState)intoState).states[i]=states[i];
    		((DefaultCharState)intoState).Fatigue=Fatigue;
    	}
    	else
    	for(int i=0;i<getStatCodes().length;i++)
    		intoState.setStat(getStatCodes()[i],getStat(getStatCodes()[i]));
    }

	public int getHitPoints(){return states[STAT_HITPOINTS];}
	public void setHitPoints(int newVal){states[STAT_HITPOINTS]=newVal;}
	public boolean adjHitPoints(int byThisMuch, CharState max)
	{
		states[STAT_HITPOINTS]+=byThisMuch;
		if(states[STAT_HITPOINTS]<1)
		{
			states[STAT_HITPOINTS]=0;
			return false;
		}
		if(states[STAT_HITPOINTS]>max.getHitPoints())
		{
			states[STAT_HITPOINTS]=max.getHitPoints();
			return false;
		}
		return true;
	}
	public long getFatigue(){return Fatigue;}
	public void setFatigue(long newVal){Fatigue=newVal;}
	public boolean adjFatigue(long byThisMuch, CharState max)
	{
		Fatigue+=byThisMuch;
		if(Fatigue<1)
		{
			Fatigue=0;
			return false;
		}
		return true;
	}
	public int getHunger(){return states[STAT_HUNGER];}
	public void setHunger(int newVal){states[STAT_HUNGER]=newVal; if(states[STAT_HUNGER]>0)ticksHungry=0;}
	public boolean adjHunger(int byThisMuch, int max)
	{
	    if((byThisMuch>0)&&(states[STAT_HUNGER]==Integer.MAX_VALUE))
	        return false;
	    states[STAT_HUNGER]+=byThisMuch;
		if(states[STAT_HUNGER]<0)
		{
			states[STAT_HUNGER]=0;
			return false;
		}
		if(states[STAT_HUNGER]>0) ticksHungry=0;
		if(states[STAT_HUNGER]>max)
		{
			states[STAT_HUNGER]=max;
			return false;
		}
		return true;
	}
	public int maxHunger(int baseWeight)
	{
	    long factor=baseWeight/250;
	    if(factor==0) factor=1;
	    factor*=getHunger();
	    if(factor>Integer.MAX_VALUE)
	        return Integer.MAX_VALUE;
        return (int)factor;
	}
	public int getThirst(){return states[STAT_THIRST];}
	public void setThirst(int newVal){states[STAT_THIRST]=newVal; if(states[STAT_THIRST]>0) ticksThirsty=0;}
	public boolean adjThirst(int byThisMuch, int max)
	{
	    if((byThisMuch>0)&&(states[STAT_THIRST]==Integer.MAX_VALUE))
	        return false;
	    states[STAT_THIRST]+=byThisMuch;
		if(states[STAT_THIRST]<0)
		{
			states[STAT_THIRST]=0;
			return false;
		}
		if(states[STAT_THIRST]>0) ticksThirsty=0;
		if(states[STAT_THIRST]>max)
		{
			states[STAT_THIRST]=max;
			return false;
		}
		return true;
	}
	public int maxThirst(int baseWeight)
	{
	    long factor=baseWeight/250;
	    if(factor==0) factor=1;
	    factor*=getThirst();
	    if(factor>Integer.MAX_VALUE)
	        return Integer.MAX_VALUE;
        return (int)factor;
	}

	public String getCombatStats(){return "H"+states[STAT_HITPOINTS]+":M"+states[STAT_MANA]+":V"+states[STAT_MOVE]+":F"+Fatigue;}
	public int getMana(){return states[STAT_MANA];}
	public void setMana(int newVal){ states[STAT_MANA]=newVal;}
	public boolean adjMana(int byThisMuch, CharState max)
	{
		states[STAT_MANA]+=byThisMuch;
		if(states[STAT_MANA]<0)
		{
			states[STAT_MANA]=0;
			return false;
		}
		if(states[STAT_MANA]>max.getMana())
		{
			states[STAT_MANA]=max.getMana();
			return false;
		}
		return true;
	}
	public int getMovement(){return states[STAT_MOVE];}
	public void setMovement(int newVal){ states[STAT_MOVE]=newVal;}
	public boolean adjMovement(int byThisMuch, CharState max)
	{
		states[STAT_MOVE]+=byThisMuch;
		if(states[STAT_MOVE]<0)
		{
			states[STAT_MOVE]=0;
			return false;
		}
		if(states[STAT_MOVE]>max.getMovement())
		{
			states[STAT_MOVE]=max.getMovement();
			return false;
		}
		return true;
	}

	public void recoverTick(MOB mob, CharState maxState)
	{
		if(++botherCycle<ADJUST_FACTOR)
			return;

		botherCycle=0;
		CharStats charStats=mob.charStats();
		double con=(double)charStats.getStat(CharStats.STAT_CONSTITUTION);
		double man=(double)((charStats.getStat(CharStats.STAT_INTELLIGENCE)+charStats.getStat(CharStats.STAT_WISDOM)));
		double str=(double)charStats.getStat(CharStats.STAT_STRENGTH);
		if(getHunger()<1)
		{
			con=con*.50;
			man=man*.50;
			str=str*.50;
		}
		if(getThirst()<1)
		{
			con=con*.50;
			man=man*.50;
			str=str*.50;
		}
		if(getFatigue()>FATIGUED_MILLIS)
			man=man*.5;

		double lvl=(double)mob.envStats().level();
		double lvlby1p5=CMath.div(lvl,1.5);
		//double lvlby2=CMath.div(lvl,2.0);
		//double lvlby3=CMath.div(lvl,3.0);

		double hpGain=(con>1.0)?((con/50.0)*lvlby1p5)+(con/4.5)+2.0:1.0;
		double manaGain=(man>2.0)?((man/90.0)*lvlby1p5)+(man/4.5)+2.0:1.0;
		double moveGain=(str>1.0)?((str/50.0)*lvl)+(str/3.0)+5.0:1.0;

		if(CMLib.flags().isSleeping(mob))
		{
			hpGain+=(hpGain/2.0);
			manaGain+=(manaGain/2.0);
			moveGain+=(moveGain/2.0);
			if((mob.riding()!=null)&&(mob.riding() instanceof Item))
			{
				hpGain+=(hpGain/8.0);
				manaGain+=(manaGain/8.0);
				moveGain+=(moveGain/8.0);
			}
		}
		else
		if((CMLib.flags().isSitting(mob))||(mob.riding()!=null))
		{
			hpGain+=(hpGain/4.0);
			manaGain+=(manaGain/4.0);
			moveGain+=(moveGain/4.0);
			if((mob.riding()!=null)&&(mob.riding() instanceof Item))
			{
				hpGain+=(hpGain/8.0);
				manaGain+=(manaGain/8.0);
				moveGain+=(moveGain/8.0);
			}
		}
		else
		{
			if(CMLib.flags().isFlying(mob))
				moveGain+=(moveGain/8.0);
			else
			if(CMLib.flags().isSwimming(mob))
			{
				hpGain-=(hpGain/2.0);
				manaGain-=(manaGain/4.0);
				moveGain-=(moveGain/2.0);
			}
		}

		if((!mob.isInCombat())
		&&(!CMLib.flags().isClimbing(mob)))
		{
			if((hpGain>0)&&(!CMLib.flags().isGolem(mob)))
				adjHitPoints((int)Math.round(hpGain),maxState);
			if(manaGain>0)
				adjMana((int)Math.round(manaGain),maxState);
			if(moveGain>0)
				adjMovement((int)Math.round(moveGain),maxState);
		}
	}

	public void expendEnergy(MOB mob, CharState maxState, boolean expendMovement)
	{
		if(mob.location()!=null)
		{
			if(expendMovement)
			{
				int move=-mob.location().pointsPerMove(mob);
				if(mob.envStats().weight()>mob.maxCarry())
					move+=(int)Math.round(CMath.mul(move,10.0*CMath.div(mob.envStats().weight()-mob.maxCarry(),mob.maxCarry())));
				adjMovement(move,maxState);
			}
			if((!CMLib.flags().isSleeping(mob))
			&&(!CMSecurity.isAllowed(mob,mob.location(),"IMMORT")))
			{
				int factor=mob.baseWeight()/500;
				if(factor<1) factor=1;
				if(!CMSecurity.isDisabled("THIRST"))
					adjThirst(-(mob.location().thirstPerRound(mob)*factor),maxState.maxThirst(mob.baseWeight()));
				if(!CMSecurity.isDisabled("HUNGER"))
					adjHunger(-factor,maxState.maxHunger(mob.baseWeight()));
			}
			boolean thirsty=(getThirst()<=0);
			boolean hungry=(getHunger()<=0);
			if((hungry||thirsty)&&(!expendMovement))
			{
				if(thirsty)ticksThirsty++;
				if(hungry)ticksHungry++;

				if((ticksThirsty>DEATH_THIRST_TICKS)
				||(ticksHungry>DEATH_HUNGER_TICKS))
				{
					if(thirsty)
						mob.tell("YOU ARE DYING OF THIRST!");
					if(hungry)
						mob.tell("YOU ARE DYING OF HUNGER!");
					CMLib.combat().postDeath(null,mob,null);
				}
				else
				if(ticksThirsty>DEATH_THIRST_TICKS-30)
					mob.tell("You are dehydrated, and near death.  DRINK SOMETHING!");
				else
				if(ticksHungry>DEATH_HUNGER_TICKS-30)
					mob.tell("You are starved, and near death.  EAT SOMETHING!");
				else
				if((--annoyanceTicker)<=0)
				{
					annoyanceTicker=ANNOYANCE_DEFAULT_TICKS;

					if(thirsty)
					{
						if(ticksThirsty>((DEATH_THIRST_TICKS/2)+(DEATH_THIRST_TICKS/4)))
							mob.tell("You are dehydrated! Drink something!");
						else
						if(ticksThirsty>(DEATH_THIRST_TICKS/2))
							mob.tell("You are parched! Drink something!");
						else
							mob.tell("You are thirsty.");
					}
					if(hungry)
					{
						if(ticksHungry>((DEATH_HUNGER_TICKS/2)+(DEATH_HUNGER_TICKS/4)))
							mob.tell("You are starved! Eat something!");
						else
						if(ticksHungry>(DEATH_HUNGER_TICKS/2))
							mob.tell("You are famished! Eat something!");
						else
							mob.tell("You are hungry.");
					}
				}
			}
		}
	}

	private final static String[] CODES={
		"HITS","MANA","MOVE",
		"HUNGER","THIRST","FATIGUE"};
    public int getSaveStatIndex(){return getStatCodes().length;}
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(CharState E){
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
			   return false;
		return true;
	}

	public void setStat(String code, String val)
	{
		switch(getCodeNum(code)){
		case 0: setHitPoints(CMath.s_parseIntExpression(val)); break;
		case 1: setMana(CMath.s_parseIntExpression(val)); break;
		case 2: setMovement(CMath.s_parseIntExpression(val)); break;
		case 3: setHunger(CMath.s_parseIntExpression(val)); break;
		case 4: setThirst(CMath.s_parseIntExpression(val)); break;
		case 5: setFatigue(CMath.s_parseIntExpression(val)); break;
		}
	}
	public String getStat(String code)
	{
		switch(getCodeNum(code)){
		case 0: return ""+getHitPoints();
		case 1: return ""+getMana();
		case 2: return ""+getMovement();
		case 3: return ""+getHunger();
		case 4: return ""+getThirst();
		case 5: return ""+getFatigue();
		default: return "";
		}
	}

	// create a new one of these
    public CMObject copyOf()
	{
		try
		{
            DefaultCharState E=(DefaultCharState)this.clone();
            E.states=(int[])E.states.clone();
            return E;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultCharState();
		}
	}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
