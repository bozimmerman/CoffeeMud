package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_TremorSense extends Chant
{
	public String ID() { return "Chant_TremorSense"; }
	public String name(){return "Tremor Sense";}
	public String displayText(){return "(Tremor Sense)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	public Environmental newInstance(){	return new Chant_TremorSense();}
	private Vector rooms=new Vector();

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> tremor sense fades.");
		for(int r=0;r<rooms.size();r++)
		{
			Room R=(Room)rooms.elementAt(r);
			Ability A=R.fetchEffect(ID());
			if((A!=null)&&(A.invoker()==mob))
				A.unInvoke();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected==null) return;
		if(affected instanceof MOB)
		{
			if(msg.amISource((MOB)affected)
			&&((msg.sourceMinor()==CMMsg.TYP_STAND)
			   ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)))
				unInvoke();
		}
		else
		if(affected instanceof Room)
		{
			if((msg.target()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!Sense.isInFlight(msg.source()))
			&&(invoker!=null)
			&&(invoker.location()!=null))
			{
				if(invoker.location()==affected)
					invoker.tell("You feel footsteps around you.");
				else
				{
					int dir=MUDTracker.radiatesFromDir((Room)affected,rooms);
					if(dir>=0)
						invoker.tell("You feel footsteps "+Directions.getInDirectionName(dir));
				}
			}
			else
			if((msg.tool() instanceof Ability)
			&&((msg.tool().ID().equals("Prayer_Tremor"))
				||(msg.tool().ID().endsWith("_Earthquake"))))
			{
				if(invoker.location()==affected)
					invoker.tell("You feel a ferocious rumble.");
				else
				{
					int dir=MUDTracker.radiatesFromDir((Room)affected,rooms);
					if(dir>=0)
						invoker.tell("You feel a ferocious rumble "+Directions.getInDirectionName(dir));
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already sensing tremors.");
			return false;
		}

		if((!Sense.isSitting(mob))||(mob.riding()!=null))
		{
			mob.tell("You must be sitting on the ground for this chant to work.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"":"^S<S-NAME> chant(s) to <S-HIM-HERSELF>.  ")+"<T-NAME> gain(s) a sense of the earth!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				rooms.clear();
				MUDTracker.getRadiantRooms(mob.location(),rooms,false,false,true,null,5);
				for(int r=0;r<rooms.size();r++)
				{
					Room R=(Room)rooms.elementAt(r);
					if((R!=mob.location())
					&&(R.domainType()!=Room.DOMAIN_INDOORS_AIR)
					&&(R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
					&&(R.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
						beneficialAffect(mob,R,0);
				}
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing happens.");


		// return whether it worked
		return success;
	}
}