package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_HeatMetal extends Spell
{
	public String ID() { return "Spell_HeatMetal"; }
	public String name(){return "Heat Metal";}
	public String displayText(){return "(Heated)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_HeatMetal();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	public long flags(){return Ability.FLAG_HEATING;}

	private Vector affectedItems=new Vector();

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;
		if(!msg.amITarget(affected)) return true;

		if(Util.bset(msg.targetMajor(),CMMsg.MASK_HANDS))
		{
			msg.source().tell(affected.name()+" is too hot!");
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
		if(tickID!=Host.TICK_MOB) return true;
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
				int damage=Dice.roll(1,6,1);
				ExternalPlay.postDamage(invoker,mob,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURSTING,item.name()+" <DAMAGE> <T-NAME>!");
				if(Dice.rollPercentage()<mob.charStats().getStat(CharStats.STRENGTH))
				{
					ExternalPlay.drop(mob,item,false,false);
					if(!mob.isMine(item))
					{
						item.addEffect((Ability)this.copyOf());
						affectedItems.addElement(item);
						break;
					}
				}
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
				Ability A=I.fetchEffect(this.ID());
				while(A!=null)
				{
					I.delEffect(A);
					A=I.fetchEffect(this.ID());
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
					success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> invoke(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}