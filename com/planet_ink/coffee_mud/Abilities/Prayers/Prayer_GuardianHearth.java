package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_GuardianHearth extends Prayer
{
	public String ID() { return "Prayer_GuardianHearth"; }
	public String name(){return "Guardian Hearth";}
	public String displayText(){return "(Guardian Hearth)";}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected static HashSet prots=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.okMessage(myHost,msg);

		if(prots==null)
		{
			prots=new HashSet();
			for(int i=0;i<CharStats.affectTypeMap.length;i++)
				if(CharStats.affectTypeMap[i]>=0)
				   prots.add(new Integer(CharStats.affectTypeMap[i]));
		}
		Room R=(Room)affected;
		if(((msg.tool() instanceof Trap)
		||(prots.contains(new Integer(msg.sourceMinor())))
		||(prots.contains(new Integer(msg.targetMinor()))))
		   &&(msg.target() instanceof MOB))
		{
			HashSet H=((MOB)msg.target()).getGroupMembers(new HashSet());
			for(Iterator e=H.iterator();e.hasNext();)
				if(CoffeeUtensils.doesHavePriviledgesHere((MOB)e.next(),R))
				{
					R.show(((MOB)msg.target()),null,this,CMMsg.MSG_OK_VISUAL,"The guardian hearth protect(s) <S-NAME>!");
					break;
				}
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already a guarded hearth.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to guard this place.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if((target instanceof Room)
				&&(CoffeeUtensils.doesOwnThisProperty(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMClass.DBEngine().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to guard this place, but <S-IS-ARE> not answered.");

		return success;
	}
}