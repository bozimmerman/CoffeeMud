package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonWater extends Chant
{
	public String ID() { return "Chant_SummonWater"; }
	public String name(){ return "Summon Water";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Room SpringLocation=null;
	private Item littleSpring=null;
	public Environmental newInstance(){	return new Chant_SummonWater();	}

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked())
			SpringLocation.showHappens(Affect.MSG_OK_VISUAL,"The little spring dries up.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item spring=littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroyThis();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) for water.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				String itemID = "Spring";

				Item newItem=(Item)CMClass.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				mob.location().addItem(newItem);
				mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for water, but nothing happens.");

		// return whether it worked
		return success;
	}
}