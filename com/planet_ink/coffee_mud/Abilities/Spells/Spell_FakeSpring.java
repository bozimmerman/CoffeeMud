package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FakeSpring extends Spell
{
	Room SpringLocation=null;
	Drink littleSpring=null;
	public Spell_FakeSpring()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fake Spring";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=Ability.CAN_ITEMS;
		canTargetCode=0;
		
		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FakeSpring();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		super.unInvoke();
		if(canBeUninvoked)
		{
			Item spring=(Item)littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroyThis();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(littleSpring))
		{
			if(affect.targetMinor()==Affect.TYP_DRINK)
			{
				affect.source().tell("You have drunk all you can.");
				return false;
			}
		}
		else
		if((affect.tool()!=null)&&(affect.tool()==littleSpring)&&(affect.target()!=null)&&(affect.target() instanceof Drink))
		{
			if(affect.targetMinor()==Affect.TYP_FILL)
			{
				affect.source().tell(affect.target().name()+" is full.");
				return false;
			}
		}
		return super.okAffect(affect);
	
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> invoke(s) a spell dramatically.");
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
				
				Drink W=(Drink)CMClass.getItem("GenWater");
				W.setName(newItem.name());
				W.setDisplayText(newItem.displayText());
				W.setDescription(newItem.description());
				W.baseEnvStats().setWeight(newItem.baseEnvStats().weight());
				((Item)W).setGettable(false);
				W.setThirstQuenched(0);
				W.recoverEnvStats();
				mob.location().addItem((Item)W);
				mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=W;
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
