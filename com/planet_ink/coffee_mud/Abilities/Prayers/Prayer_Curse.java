package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Curse extends Prayer
{
	public String ID() { return "Prayer_Curse"; }
	public String name(){ return "Curse";}
	public String displayText(){ return "(Cursed)";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_Curse();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_EVIL);
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()+10);
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()-1);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(canBeUninvoked())
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
				((MOB)((Item)affected).owner()).tell("The curse on "+((Item)affected).name()+" is lifted.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("The curse is lifted.");
		super.unInvoke();
	}

	public static Item getSomething(MOB mob, boolean blessedOnly)
	{
		Vector good=new Vector();
		Vector great=new Vector();
		Item target=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((!blessedOnly)||(isBlessed(I)))
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

	public static boolean isBlessed(Item item)
	{
		if(item.fetchAffect("Prayer_Bless")!=null)
			return true;
		if(item.fetchAffect("Prayer_HolyAura")!=null)
			return true;
		if(item.fetchAffect("Prayer_BlessItem")!=null)
			return true;
		if(item.fetchAffect("Prayer_HolyWord")!=null)
			return true;
		return false;
	}

	public static void endIt(Environmental target, int level)
	{
		for(int a=target.numAffects()-1;a>=0;a--)
		{
			Ability A=target.fetchAffect(a);
			if(A!=null)
			{
				if(A instanceof Prayer_Bless)
					A.unInvoke();
				if(A instanceof Prayer_BlessItem)
					A.unInvoke();
				if(A instanceof Prayer_Sanctuary)
					A.unInvoke();
				if((A instanceof Prayer_HolyAura)&&(level>0))
					A.unInvoke();
				if((A instanceof Prayer_HolyWord)&&(level>1))
					A.unInvoke();
				if((A instanceof Prayer_UnholyWord)&&(level!=2))
					A.unInvoke();
				if((A instanceof Prayer_Curse)&&(level!=0))
					A.unInvoke();
				if((A instanceof Prayer_CurseItem)&&(level!=0))
					A.unInvoke();
				if((A instanceof Prayer_GreatCurse)&&(level!=1))
					A.unInvoke();
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"<T-NAME> is cursed!":"^S<S-NAME> curse(s) <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					Item I=getSomething(mob,true);
					if(I!=null)
					{
						endIt(I,0);
						I.recoverEnvStats();
					}
					endIt(target,0);
					success=maliciousAffect(mob,target,0,-1);
					target.recoverEnvStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
