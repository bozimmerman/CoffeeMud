package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FakeSpring extends Spell
{
	public String ID() { return "Spell_FakeSpring"; }
	public String name(){return "Fake Spring";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_FakeSpring();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item spring=(Item)affected; // protects against uninvoke loops!
			Room SpringLocation=CoffeeUtensils.roomLocation(spring);
			spring.destroy();
			SpringLocation.recoverRoomStats();
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(affected))
		{
			if(affect.targetMinor()==Affect.TYP_DRINK)
			{
				if(affect.othersMessage()!=null)
					affect.source().location().show(affect.source(),affect.target(),affect.tool(),Affect.MSG_QUIETMOVEMENT,affect.othersMessage());
				affect.source().tell("You have drunk all you can.");
				return false;
			}
		}
		else
		if((affect.tool()!=null)&&(affect.tool()==affected)&&(affect.target()!=null)&&(affect.target() instanceof Drink))
		{
			if(affect.targetMinor()==Affect.TYP_FILL)
			{
				affect.source().tell(affect.target().name()+" is full.");
				return false;
			}
		}
		return super.okAffect(myHost,affect);

	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell dramatically.^?");
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

				Drink W=(Drink)CMClass.getItem("GenWater");
				W.setName(newItem.Name());
				W.setDisplayText(newItem.displayText());
				W.setDescription(newItem.description());
				W.baseEnvStats().setWeight(newItem.baseEnvStats().weight());
				((Item)W).setGettable(false);
				W.setThirstQuenched(0);
				W.recoverEnvStats();
				mob.location().addItem((Item)W);
				mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				if((ExternalPlay.doesOwnThisProperty(mob,mob.location()))
				||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),mob.location()))))
				{
					Ability A=(Ability)copyOf();
					A.setInvoker(mob);
					W.addNonUninvokableAffect(A);
				}
				else
					beneficialAffect(mob,W,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically attempt(s) to invoke a spell, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
