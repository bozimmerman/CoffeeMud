package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Util;
import com.planet_ink.coffee_mud.utils.Sense;
public class DefaultCharState implements Cloneable, CharState
{
	protected int HitPoints=10;
	protected int Mana=100;
	protected int Movement=50;
	protected int Hunger=1000;
	protected int Thirst=500;

	protected int botherCycle=0;
	protected int ticksHungry=0;
	protected int ticksThirsty=0;

	protected int annoyanceTicker=ANNOYANCE_DEFAULT_TICKS;

	public int getHitPoints(){return HitPoints;}
	public void setHitPoints(int newVal){HitPoints=newVal;}
	public boolean adjHitPoints(int byThisMuch, CharState max)
	{
		HitPoints+=byThisMuch;
		if(HitPoints<1)
		{
			HitPoints=0;
			return false;
		}
		if(HitPoints>max.getHitPoints())
		{
			HitPoints=max.getHitPoints();
			return false;
		}
		return true;
	}
	public int getHunger(){return Hunger;}
	public void setHunger(int newVal){Hunger=newVal; if(Hunger>0)ticksHungry=0;}
	public boolean adjHunger(int byThisMuch, CharState max)
	{
		Hunger+=byThisMuch;
		if(Hunger<0)
		{
			Hunger=0;
			return false;
		}
		if(Hunger>0) ticksHungry=0;
		if(Hunger>1000)
		{
			Hunger=1000;
			return false;
		}
		return true;
	}
	public int getThirst(){return Thirst;}
	public void setThirst(int newVal){Thirst=newVal; if(Thirst>0) ticksThirsty=0;}
	public boolean adjThirst(int byThisMuch, CharState max)
	{
		Thirst+=byThisMuch;
		if(Thirst<0)
		{
			Thirst=0;
			return false;
		}
		if(Thirst>0) ticksThirsty=0;
		if(Thirst>500)
		{
			Thirst=500;
			return false;
		}
		return true;
	}
	public int getMana(){return Mana;}
	public void setMana(int newVal){ Mana=newVal;}
	public boolean adjMana(int byThisMuch, CharState max)
	{
		Mana+=byThisMuch;
		if(Mana<0)
		{
			Mana=0;
			return false;
		}
		if(Mana>max.getMana())
		{
			Mana=max.getMana();
			return false;
		}
		return true;
	}
	public int getMovement(){return Movement;}
	public void setMovement(int newVal){ Movement=newVal;}
	public boolean adjMovement(int byThisMuch, CharState max)
	{
		Movement+=byThisMuch;
		if(Movement<0)
		{
			Movement=0;
			return false;
		}
		if(Movement>max.getMovement())
		{
			Movement=max.getMovement();
			return false;
		}
		return true;
	}

	public void adjState(MOB mob, CharState maxState)
	{
		if(++botherCycle<ADJUST_FACTOR)
			return;

		botherCycle=0;

		double con=new Integer(mob.charStats().getStat(CharStats.CONSTITUTION)).doubleValue();
		double man=new Integer((mob.charStats().getStat(CharStats.INTELLIGENCE)+mob.charStats().getStat(CharStats.WISDOM))).doubleValue();
		double str=new Integer(mob.charStats().getStat(CharStats.STRENGTH)).doubleValue();
		int lvl=mob.envStats().level();
		int lvlby2=(int)Math.round(Util.div(lvl,2.0));

		if(Sense.isSleeping(mob))
		{
			adjHitPoints((int)Math.round(con*.2)+1,maxState);
			adjMana((int)Math.round((man*.2)+lvl),maxState);
			adjMovement((int)Math.round(str),maxState);
		}
		else
		if((Sense.isSitting(mob))||(mob.riding()!=null))
		{
			adjHitPoints((int)Math.round(con*.1)+1,maxState);
			adjMana((int)Math.round((man*.1)+lvlby2),maxState);
			adjMovement((int)Math.round(str*.5),maxState);
		}
		else
		if(Sense.isFlying(mob))
		{
			adjHitPoints((int)Math.round(con*.1)+1,maxState);
			adjMana((int)Math.round(man*.1)+lvlby2,maxState);
			adjMovement((int)Math.round(str*.4),maxState);
		}
		else
		if(Sense.isSwimming(mob))
		{
			adjHitPoints((int)Math.round(con*.05)+1,maxState);
			adjMana((int)Math.round(man*.1)+lvlby2,maxState);
			adjMovement((int)Math.round(str*.2)+1,maxState);
		}
		else
		if((!mob.isInCombat())&&(!Sense.isClimbing(mob)))
		{
			adjHitPoints((int)Math.round((con*.05)+.05),maxState);
			adjMana((int)Math.round(man*.1)+lvlby2,maxState);
			adjMovement((int)Math.round(str*.3)+1,maxState);
		}
	}

	public void expendEnergy(MOB mob, CharState maxState, boolean expendMovement)
	{
		if((!mob.isMonster())&&(mob.location()!=null))
		{
			if(expendMovement)
				adjMovement(-mob.location().thirstPerRound(mob),maxState);
			if(!Sense.isSleeping(mob))
			{
				adjThirst(-mob.location().thirstPerRound(mob),maxState);
				adjHunger(-1,maxState);
			}
			boolean thirsty=(getThirst()<=0);
			boolean hungry=(getHunger()<=0);
			if((hungry||thirsty)&&(!expendMovement))
			{
				if(thirsty)ticksThirsty++;
				if(hungry)ticksHungry++;
			
				if((ticksThirsty>this.DEATH_THIRST_TICKS)
				||(ticksHungry>this.DEATH_HUNGER_TICKS))
				{
					if(thirsty)
						mob.tell("YOU ARE DYING OF THIRST!");
					if(hungry)
						mob.tell("YOU ARE DYING OF HUNGER!");
					ExternalPlay.die(null,mob);
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


	// create a new one of these
	public CharState cloneCharState()
	{
		try
		{
			return (CharState) this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultCharState();
		}
	}
}
