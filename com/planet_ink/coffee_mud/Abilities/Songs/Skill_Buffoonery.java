package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Skill_Buffoonery extends StdAbility
{
	public String ID() { return "Skill_Buffoonery"; }
	public String name(){ return "Buffoonery";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BUFFOONERY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Buffoonery();}

	
	private Vector getFreeWearingPositions(MOB target)
	{
		Vector V=new Vector();
		boolean[] pos=new boolean[Item.wornOrder.length];
		
		for(int i=0;i<pos.length;i++)
			if(target.amWearingSomethingHere(Item.wornOrder[i]))
				pos[i]=true;
		
		for(int i=0;i<pos.length;i++)
			if(!pos[i]) 
				V.addElement(new Long(Item.wornOrder[i]));
		return V;
	}
	
	private boolean freePosition(MOB target)
	{
		return getFreeWearingPositions(target).size()>0;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify a target, and what item to swap on the target!");
			return false;
		}
		Item I=mob.fetchInventory((String)commands.lastElement());
		if((I==null)||(!Sense.canBeSeenBy(I,mob)))
		{
			mob.tell("You don't seem to have '"+((String)commands.lastElement())+"'.");
			return false;
		}
		if(((I instanceof Armor)&&(I.baseEnvStats().armor()>1))
		||((I instanceof Weapon)&&(I.baseEnvStats().damage()>1)))
		{
			mob.tell(I.name()+" is not buffoonish enough!");
			return false;
		}
		commands.removeElementAt(commands.size()-1);
		
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Item targetItem=null;
		Vector V=new Vector();
		
		for(int i=0;i<target.inventorySize();i++)
		{
			Item I2=target.fetchInventory(i);
			if((!I2.amWearingAt(Item.INVENTORY))
			&&(((I2 instanceof Weapon)&&(I.baseEnvStats().damage()>1))
			   ||((I2 instanceof Armor)&&(I.baseEnvStats().armor()>1)))
			&&(I2.container()==null))
				V.addElement(I2);
		}
		if(V.size()>0)
			targetItem=(Item)V.elementAt(Dice.roll(1,V.size(),-1));
		else
		if(!freePosition(target))
		{
			mob.tell(target.name()+" has no free wearing positions!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(0,auto);
		if(levelDiff>0)
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?1:2));

		String str=null;
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,(Affect.MSG_DELICATE_HANDS_ACT|Affect.MASK_MALICIOUS)|(auto?Affect.MASK_GENERAL:0),auto?"":"<S-NAME> do(es) buffoonery to <T-NAMESELF>.");			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				long position=-1;
				if(targetItem!=null)
				{
					position=targetItem.rawWornCode();
					targetItem.unWear();
				}
				else
				{
					Vector free=getFreeWearingPositions(target);
					if(free.size()<1)
					{
						mob.tell(target.name()+" has no free wearing positions!");
						return false;
					}
					if((free.contains(new Long(Item.WIELD)))
					&&((I instanceof Weapon)||(!(I instanceof Armor))))
						position=Item.WIELD;
					else
						position=((Long)free.elementAt(Dice.roll(1,free.size(),-1))).longValue();
				}
				if(position>=0)
				{
					I.unWear();
					target.giveItem(I);
					I.wearAt(position);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) buffoonery on <T-NAMESELF>, but fail(s).");

		return success;
	}

}