package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_FreezeMetal extends Prayer
{
	public String ID() { return "Prayer_FreezeMetal"; }
	public String name(){return "Freeze Metal";}
	public String displayText(){return "(Frozen)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_FreezeMetal();}
	public int holyQuality(){ return HOLY_NEUTRAL;}

	private Vector affectedItems=new Vector();

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;
		if((msg.target()==null)
		   ||(!(msg.target() instanceof Item)))
			return true;
		MOB mob=(MOB)affected;
		if(!mob.isMine(msg.target())) return true;
		Item I=(Item)msg.target();
		if(msg.targetMinor()==Affect.TYP_GET)
		{
			if(I.amWearingAt(Item.INVENTORY))
				msg.source().tell(affected.name()+" is too cold!");
			else
				msg.source().tell(affected.name()+" is frozen stuck!");
			return false;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Host.MOB_TICK) return true;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		if(invoker==null)
			return true;

		MOB mob=(MOB)affected;

		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item!=null)
			   &&(!item.amWearingAt(Item.INVENTORY))
			   &&((item.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
			   &&(item.container()==null)
			   &&(!mob.amDead()))
			{
				int damage=Dice.roll(1,3,1);
				if(item.subjectToWearAndTear())
					item.setUsesRemaining(item.usesRemaining()-1);
				ExternalPlay.postDamage(invoker,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_COLD,Weapon.TYPE_BURSTING,item.name()+" <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
		{
			super.unInvoke();
			return;
		}

		if(canBeUninvoked())
		if(affected instanceof MOB)
		{
			for(int i=0;i<affectedItems.size();i++)
			{
				Item I=(Item)affectedItems.elementAt(i);
				Ability A=I.fetchAffect(this.ID());
				while(A!=null)
				{
					I.delAffect(A);
					A=I.fetchAffect(this.ID());
				}

			}
		}
		super.unInvoke();
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+".^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
					success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");

		// return whether it worked
		return success;
	}
}