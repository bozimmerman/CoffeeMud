package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class CombatAbilities extends StdBehavior
{
	public String ID(){return "CombatAbilities";}


	public int combatMode=0;

	public final static int COMBAT_RANDOM=0;
	public final static int COMBAT_DEFENSIVE=1;
	public final static int COMBAT_OFFENSIVE=2;
	public final static int COMBAT_MIXEDOFFENSIVE=3;
	public final static int COMBAT_MIXEDDEFENSIVE=4;
	public final static String[] names={
		"RANDOM",
		"DEFENSIVE",
		"OFFENSIVE",
		"MIXEDOFFENSIVE",
		"MIXEDDEFENSIVE"
	};


	protected String getParmsMinusCombatMode()
	{
		Vector V=Util.parse(getParms());
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=((String)V.elementAt(v)).toUpperCase();
			for(int i=0;i<names.length;i++)
				if(names[i].startsWith(s))
				{
					combatMode=i;
					V.removeElementAt(v);
				}
		}
		return Util.combine(V,0);
	}

	protected void newCharacter(MOB mob)
	{
		Vector oldAbilities=new Vector();
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)
			{
				if(A.profficiency()<50)	A.setProfficiency(50);
				oldAbilities.addElement(A);
			}
		}
		mob.charStats().getCurrentClass().startCharacter(mob,true,false);
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability newOne=mob.fetchAbility(a);
			if((newOne!=null)&&(!oldAbilities.contains(newOne)))
			{
				if(!CMAble.qualifiesByLevel(mob,newOne))
				{
					mob.delAbility(newOne);
					mob.delEffect(mob.fetchEffect(newOne.ID()));
					a=a-1;
				}
				else
					newOne.setProfficiency(100);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) return true;
		if(tickID!=MudHost.TICK_MOB)
		{
			Log.errOut("CombatAbilities",ticking.name()+" wants to fight?!");
			return true;
		}
		MOB mob=(MOB)ticking;

		if(!canActAtAll(mob)) return true;
		if(!mob.isInCombat()) return true;
		MOB victim=mob.getVictim();
		if(victim==null) return true;

		// insures we only try this once!
		for(int b=0;b<mob.numBehaviors();b++)
		{
			Behavior B=mob.fetchBehavior(b);
			if((B==null)||(B==this))
				break;
			else
			if(B instanceof CombatAbilities)
				return true;
		}

		int tries=0;
		Ability tryThisOne=null;

		while((tryThisOne==null)&&(tries<100)&&((mob.numAbilities())>0))
		{
			tryThisOne=mob.fetchAbility(Dice.roll(1,mob.numAbilities(),-1));

			if((tryThisOne==null)
			||(tryThisOne.isAutoInvoked())
			||(tryThisOne.triggerStrings()==null)
			||(tryThisOne.triggerStrings().length==0)
			||((tryThisOne.quality()!=Ability.MALICIOUS)
				&&(tryThisOne.quality()!=Ability.BENEFICIAL_SELF)
				&&(tryThisOne.quality()!=tryThisOne.BENEFICIAL_OTHERS))
			||(victim.fetchEffect(tryThisOne.ID())!=null))
				tryThisOne=null;
			else
			if(tryThisOne.quality()==Ability.MALICIOUS)
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
					break;
				case COMBAT_DEFENSIVE:
					if(Dice.rollPercentage()>5)
						tryThisOne=null;
					break;
				case COMBAT_OFFENSIVE:
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(Dice.rollPercentage()>75)
						tryThisOne=null;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(Dice.rollPercentage()>25)
						tryThisOne=null;
					break;
				}
			}
			else
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
					break;
				case COMBAT_DEFENSIVE:
					break;
				case COMBAT_OFFENSIVE:
					if(Dice.rollPercentage()>5)
						tryThisOne=null;
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(Dice.rollPercentage()>25)
						tryThisOne=null;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(Dice.rollPercentage()>75)
						tryThisOne=null;
					break;
				}
			}

			tries++;
		}


		boolean wandThis=true;
		if(tryThisOne!=null)
		{
			if(Util.bset(tryThisOne.usageType(),Ability.USAGE_MANA))
			{
				if((Math.random()>Util.div(mob.curState().getMana(), mob.maxState().getMana()))
                ||(mob.curState().getMana() < tryThisOne.usageCost(mob)[0]))
				{
                   if((Dice.rollPercentage()>30)
				   ||(CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACONSUMETIME)<=0)
				   ||((mob.amFollowing()!=null)&&(!mob.amFollowing().isMonster())))
                        return true;
                   else
					   mob.curState().adjMana(tryThisOne.usageCost(mob)[0],mob.maxState());
				}
				mob.curState().adjMana(5,mob.maxState());
			}
			if(Util.bset(tryThisOne.usageType(),Ability.USAGE_MOVEMENT))
			{
				if((Math.random()>Util.div(mob.curState().getMovement(),mob.maxState().getMovement()))
				||(mob.curState().getMovement()<tryThisOne.usageCost(mob)[1]))
					return true;
				mob.curState().adjMovement(5,mob.maxState());
			}
			if(Util.bset(tryThisOne.usageType(),Ability.USAGE_HITPOINTS))
			{
				if((Math.random()>Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))
				   ||(mob.curState().getHitPoints()<tryThisOne.usageCost(mob)[2]))
					return true;
			}

			if(tryThisOne.quality()!=Ability.MALICIOUS)
				victim=mob;

			tryThisOne.setProfficiency(Dice.roll(1,70,mob.baseEnvStats().level()));
			Vector V=new Vector();
			V.addElement(victim.name());
			if(tryThisOne.invoke(mob,V,victim,false))
				wandThis=false;
		}
		if((wandThis)
		&&(victim.location()!=null)
		&&(!victim.amDead())
		&&(Dice.rollPercentage()<25)
		&&(mob.fetchAbility("Skill_WandUse")!=null))
		{
			Ability A=mob.fetchAbility("Skill_WandUse");
			if(A!=null) A.setProfficiency(100);
			Item myWand=null;
			Item backupWand=null;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I instanceof Wand))
				{
					if(!I.amWearingAt(Item.INVENTORY))
						myWand=I;
					else
						backupWand=I;
				}
			}
			if((myWand==null)&&(backupWand!=null)&&(backupWand.canWear(mob,Item.HELD)))
			{
				Vector V=new Vector();
				V.addElement("hold");
				V.addElement(backupWand.name());
				mob.doCommand(V);
			}
			else
			if(myWand!=null)
			{
				A=((Wand)myWand).getSpell();
				if((A!=null)
				&&((A.quality()==Ability.MALICIOUS)
				||(A.quality()==Ability.BENEFICIAL_SELF)
				||(A.quality()==Ability.BENEFICIAL_OTHERS)))
				{
					if(A.quality()!=Ability.MALICIOUS)
						victim=mob;
					else
					if(mob.getVictim()!=null)
						victim=mob.getVictim();
					else
						victim=null;
					if(victim!=null)
					{
						Vector V=new Vector();
						V.addElement("sayto");
						V.addElement(victim.name());
						V.addElement(((Wand)myWand).magicWord());
						mob.doCommand(V);
					}
				}
			}
		}
		return true;
	}
}
