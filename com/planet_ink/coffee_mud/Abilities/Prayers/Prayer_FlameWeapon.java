package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_FlameWeapon extends Prayer
{
	public String ID() { return "Prayer_FlameWeapon"; }
	public String name(){ return "Flame Weapon";}
	public String displayText(){return "(Enflamed)";}
	public int quality(){return Ability.INDIFFERENT;}
	public int holyQuality(){ return HOLY_EVIL;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_FlameWeapon();}
	private boolean notAgain=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+1);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affect.source().location()!=null)
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetCode()-Affect.MASK_HURT)>0)
		&&(affect.tool()==affected)
		&&(!notAgain)
		&&(affect.target() instanceof MOB)
		&&(!((MOB)affect.target()).amDead()))
		{
			notAgain=true;
			FullMsg msg=new FullMsg(affect.source(),(MOB)affect.target(),affected,Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_FIRE,Affect.MSG_NOISYMOVEMENT,null);
			if(affect.source().location().okAffect(affect.source(),msg))
			{
				affect.source().location().send(affect.source(), msg);
				if(!msg.wasModified())
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),Affect.MSG_OK_ACTION,"The flame around "+affected.displayName()+" "+CommonStrings.standardHitWord(Weapon.TYPE_BURNING,flameDamage)+" <T-NAME>!"));
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),null,Affect.NO_EFFECT,Affect.MASK_HURT+flameDamage,Affect.NO_EFFECT,null));
				}
			}
			notAgain=false;
		}
	}


	public void unInvoke()
	{
		Item destroyMe=null;
		// undo the affects of this spell
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof Item))
			{
				if(((((Weapon)affected).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
				&&((((Weapon)affected).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION))
				{
					if((((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
						((MOB)((Item)affected).owner()).tell("The flames around "+((Item)affected).displayName()+" consume it.");
					destroyMe=(Item)affected;
				}
				else
				if((((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
					((MOB)((Item)affected).owner()).tell("The flames around "+((Item)affected).displayName()+" fade.");
			}
		}
		super.unInvoke();
		if(destroyMe!=null)
			destroyMe.destroyThis();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Weapon))
		{
			mob.tell("You can only enflame weapons.");
			return false;
		}
		if(((Weapon)target).fetchAffect(this.ID())!=null)
		{
			mob.tell(target.displayName()+" is already enflamed.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> appear(s) surrounded by flames!":"^S<S-NAME> hold(s) <T-NAMESELF> and "+prayWord(mob)+".^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> is engulfed in flames!");
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");
		// return whether it worked
		return success;
	}
}