package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_BoneMoon extends Prayer
{
	public String ID() { return "Prayer_BoneMoon"; }
	public String name(){ return "Bone Moon";}
	public String displayText(){ return "(Bone Moon)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_BoneMoon();	}
	protected int level=1;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
		{
			super.unInvoke();
			return;
		}
		Room  R=(Room)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(R!=null))
		   R.showHappens(CMMsg.MSG_OK_VISUAL,"The bone moon over you fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			DeadBody B=null;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&(I instanceof DeadBody)
				&&(I.container()==null)
				&&(I.rawSecretIdentity().length()>0)
				&&(I.rawSecretIdentity().indexOf("/")>0))
				{
					B=(DeadBody)I;
					break;
				}
			}
			if(B!=null)
			{
				Prayer_AnimateSkeleton.makeSkeletonFrom(R,B,null,level);	
				B.destroy();
				level+=3;
			}
		}
		return super.tick(ticking,tickID);
	}

	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already under a bone moon.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The Bone Moon rises over <S-NAME>.");
				level=1;
				if((CoffeeUtensils.doesOwnThisProperty(mob,((Room)target)))
					||((mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),((Room)target)))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMClass.DBEngine().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for the Bone Moon, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}