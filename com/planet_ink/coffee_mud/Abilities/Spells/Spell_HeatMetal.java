package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_HeatMetal extends Spell
{
	private Vector affectedItems=new Vector();

	public Spell_HeatMetal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Heat Metal";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="";

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(11);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_HeatMetal();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_ALTERATION;
	}

	public boolean okAffect(Affect msg)
	{
		if(!super.okAffect(msg)) return false;
		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;
		if(!msg.amITarget(affected)) return true;

		if(Util.bset(msg.targetMajor(),Affect.AFF_TOUCHED))
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

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
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
			if((item!=null)&&(!item.amWearingAt(Item.INVENTORY))&&(item.material()==Item.METAL)&&(item.location()==null)&&(!mob.amDead()))
			{
				int damage=Dice.roll(1,6,1);
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,item.name()+" "+ExternalPlay.standardHitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
				ExternalPlay.postDamage(invoker,mob,this,damage);
				if(Dice.rollPercentage()<mob.charStats().getStrength())
				{
					ExternalPlay.drop(mob,item);
					if(!mob.isMine(item))
					{
						item.addAffect((Ability)this.copyOf());
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> invoke(s) a spell upon <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
					success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}