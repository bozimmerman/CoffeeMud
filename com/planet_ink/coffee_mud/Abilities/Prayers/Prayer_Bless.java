package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Bless extends Prayer
{
	public String ID() { return "Prayer_Bless"; }
	public String name(){ return "Bless";}
	public String displayText(){ return "(Blessed)";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_BLESSING;}
	public Environmental newInstance(){	return new Prayer_Bless();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOOD);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()-10);
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+1);
	}



	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(canBeUninvoked())
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
				((MOB)((Item)affected).owner()).tell("The blessing on "+((Item)affected).name()+" fades.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your aura of blessing fades.");
		super.unInvoke();
	}

	public static Item getSomething(MOB mob, boolean cursedOnly)
	{
		Vector good=new Vector();
		Vector great=new Vector();
		Item target=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((!cursedOnly)||(isCursed(I)))
				if(I.amWearingAt(Item.INVENTORY))
					good.addElement(I);
				else
					great.addElement(I);
		}
		if(great.size()>0)
			target=(Item)great.elementAt(Dice.roll(1,great.size(),-1));
		else
		if(good.size()>0)
			target=(Item)good.elementAt(Dice.roll(1,good.size(),-1));
		return target;
	}

	public static void endLowerBlessings(Environmental target, int level)
	{
		Vector V=Sense.flaggedAffects(target,Ability.FLAG_BLESSING);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(CMAble.lowestQualifyingLevel(A.ID())<level)
				A.unInvoke();
		}
	}
	public static void endLowerCurses(Environmental target, int level)
	{
		Vector V=Sense.flaggedAffects(target,Ability.FLAG_CURSE);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(CMAble.lowestQualifyingLevel(A.ID())<=level)
				A.unInvoke();
		}
	}

	public static boolean isCursed(Item item)
	{
		if(!Sense.isRemovable(item))
			return true;
		if(!Sense.isDroppable(item))
			return true;
		return Sense.flaggedAffects(item,Ability.FLAG_CURSE).size()>0;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"<T-NAME> appear(s) blessed!":"^S<S-NAME> bless(es) <T-NAMESELF>"+inTheNameOf(mob)+".^?")+CommonStrings.msp("bless.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=getSomething(target,true);
				while(I!=null)
				{
					FullMsg msg2=new FullMsg(target,I,null,CMMsg.MASK_GENERAL|CMMsg.MSG_DROP,"<S-NAME> release(s) <T-NAME>.");
					target.location().send(target,msg2);
					endLowerCurses(I,CMAble.lowestQualifyingLevel(ID()));
					I.recoverEnvStats();
					I=getSomething(target,true);
				}
				endLowerCurses(target,CMAble.lowestQualifyingLevel(ID()));
				beneficialAffect(mob,target,0);
				target.recoverEnvStats();
				target.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for blessings, but nothing happens.");
		// return whether it worked
		return success;
	}
}
