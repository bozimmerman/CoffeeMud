package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_BlessedHearth extends Prayer
{
	public String ID() { return "Prayer_BlessedHearth"; }
	public String name(){return "Blessed Hearth";}
	public String displayText(){return "(Blessed Hearth)";}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Prayer_BlessedHearth();}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.okMessage(myHost,msg);

		Room R=(Room)affected;
		if(((msg.sourceMinor()==CMMsg.TYP_UNDEAD)||(msg.targetMinor()==CMMsg.TYP_UNDEAD))
		&&(msg.target() instanceof MOB))
		{
			HashSet H=((MOB)msg.target()).getGroupMembers(new HashSet());
			for(Iterator e=H.iterator();e.hasNext();)
				if(CoffeeUtensils.doesHavePriviledgesHere((MOB)e.next(),R))
				{
					R.show(msg.source(),null,this,CMMsg.MSG_OK_VISUAL,"The blessed powers block the unholy magic from <S-NAMESELF>.");
					return false;
				}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() instanceof MOB))
		{
			HashSet H=((MOB)msg.target()).getGroupMembers(new HashSet());
			for(Iterator e=H.iterator();e.hasNext();)
				if(CoffeeUtensils.doesHavePriviledgesHere((MOB)e.next(),R))
				{
					msg.setValue(msg.value()/10);
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
			mob.tell("This place is already a blessed hearth.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to fill this place with blessedness.^?");
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
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to fill this place with blessedness, but <S-IS-ARE> not answered.");

		return success;
	}
}