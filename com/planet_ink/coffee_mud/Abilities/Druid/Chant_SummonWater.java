package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonWater extends Chant
{
	private Room SpringLocation=null;
	private Item littleSpring=null;

	public Chant_SummonWater()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Water";
		baseEnvStats().setLevel(5);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_SummonWater();
	}

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		SpringLocation.show(invoker,null,Affect.MSG_OK_VISUAL,"The little spring dries up.");
		super.unInvoke();
		Item spring=littleSpring; // protects against uninvoke loops!
		littleSpring=null;
		spring.destroyThis();
		SpringLocation.recoverRoomStats();
		SpringLocation=null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
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
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) for water.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String itemID = "Spring";

				Item newItem=(Item)CMClass.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				newItem=(Item)newItem.newInstance();
				mob.location().addItem(newItem);
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
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