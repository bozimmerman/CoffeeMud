package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_Mold extends Chant
{
	public String ID() { return "Chant_Mold"; }
	public String name(){ return "Mold";}
	public String displayText(){return "(Mold)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_Mold();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Item)))
			return;
		Item item=(Item)affected;
		super.unInvoke();

		if(canBeUninvoked())
			item.destroy();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=this.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(((target instanceof Item)&&(!(target instanceof Food)))
		   ||(target instanceof Room)
		   ||(target instanceof Exit))
		{
			mob.tell("You can't cast this on "+target.name()+".");
			return false;
		}


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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if(target instanceof Item)
					{
						Ability A=CMClass.getAbility("Disease_Lockjaw");
						if(A!=null)
						{
							A.setInvoker(mob);
							target.addNonUninvokableAffect(A);
						}
						maliciousAffect(mob,target,(int)(Host.TICKS_PER_MUDDAY*3),-1);
					}
					else
					if(target instanceof MOB)
					for(int i=0;i<((MOB)target).inventorySize();i++)
					{
						Item I=((MOB)target).fetchInventory(i);
						if((I!=null)&&(I instanceof Food))
						{
							Ability A=CMClass.getAbility("Disease_Lockjaw");
							if(A!=null)
							{
								A.setInvoker(mob);
								I.addNonUninvokableAffect(A);
							}
							maliciousAffect(mob,I,(int)(Host.TICKS_PER_MUDDAY*3),-1);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
