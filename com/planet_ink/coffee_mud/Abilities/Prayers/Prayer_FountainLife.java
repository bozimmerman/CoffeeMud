package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_FountainLife extends Prayer
{
	public String ID() { return "Prayer_FountainLife"; }
	public String name(){ return "Fountain of Life";}
	public int quality(){ return INDIFFERENT;}
	public int holyQuality(){ return HOLY_GOOD;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Room SpringLocation=null;
	private Item littleSpring=null;
	public Environmental newInstance(){	return new Prayer_FountainLife();}
	
	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked())
			SpringLocation.showHappens(Affect.MSG_OK_VISUAL,"The fountain of life dries up.");
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
		if((!auto)&&(mob.curState().getMana()<mob.maxState().getMana()))
		{
			mob.tell("This prayer requires all of your mana.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		mob.curState().setMana(0);

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the fountain of life.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String itemID = "LifeFountain";

				Item newItem=(Item)CMClass.getMiscMagic(itemID);

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
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a fountain of life, but there is no answer.");

		// return whether it worked
		return success;
	}
}
