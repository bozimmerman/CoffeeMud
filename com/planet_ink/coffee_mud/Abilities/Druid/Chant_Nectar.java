package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Nectar extends Chant
{
	public String ID() { return "Chant_Nectar"; }
	public String name(){ return "Nectar";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_Nectar();	}
	public Vector drank=null;
	private int lastNum=-1;

	public void unInvoke()
	{
		if((affected==null)
		||(!(affected instanceof Item))
		||(((Item)affected).owner()==null)
		||(!(((Item)affected).owner() instanceof Room)))
			super.unInvoke();
		else
		{
			Item littleSpring=(Item)affected;
			Room SpringLocation=CoffeeUtensils.roomLocation(littleSpring);
			if(canBeUninvoked())
				SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,littleSpring.name()+" dries up.");
			super.unInvoke();
			if(canBeUninvoked())
			{
				Item spring=littleSpring; // protects against uninvoke loops!
				spring.destroy();
				SpringLocation.recoverRoomStats();
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(!(affected instanceof Item)) return false;
		Item littleSpring=(Item)affected;
		Room R=CoffeeUtensils.roomLocation(affected);
		if(R==null) return false;
		if(lastNum!=R.numInhabitants())
		{
			lastNum=R.numInhabitants();
			return true;
		}
		if(lastNum<1) return true;
		MOB M=R.fetchInhabitant(Dice.roll(1,lastNum,-1));
		if(M==null) return true;
		if(drank==null) drank=new Vector();
		if(drank.contains(M)) return true;
		drank.addElement(M);
		if(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_MIND))
		{
			Vector commands=new Vector();
			commands.addElement("DRINK");
			commands.addElement(littleSpring.name()+"$");
			M.enqueCommand(commands,0);
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected!=null)
		if(msg.amITarget(affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				{
					MOB M=(MOB)msg.source();
					int hp=Dice.roll(1,M.charStats().getStat(CharStats.CONSTITUTION),0);
					MUDFight.postHealing(M,M,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,hp,null);
					int mana=Dice.roll(1,((M.charStats().getStat(CharStats.WISDOM)+M.charStats().getStat(CharStats.INTELLIGENCE))/2),0);
					M.curState().adjMana(mana,M.maxState());
					int move=Dice.roll(1,((M.charStats().getStat(CharStats.WISDOM)+M.charStats().getStat(CharStats.INTELLIGENCE))/2),0);
					M.curState().adjMovement(move,M.maxState());
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!auto)
		{
			if((mob.location().domainType()&Room.INDOORS)>0)
			{
				mob.tell("You must be outdoors for this chant to work.");
				return false;
			}
			if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			{
				mob.tell("This magic will not work here.");
				return false;
			}
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) for nectar.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=(Item)CMClass.getItem("Spring");
				newItem.setName("an enormous flower");
				newItem.setDisplayText("an enormous flower is dripping with nectar");
				newItem.setDescription("The closer you look, the more illusive the flower becomes.  There must be druid magic at work here!");
				Ability A=CMClass.getAbility("Poison_Liquor");
				if(A!=null) newItem.addNonUninvokableEffect(A);

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				drank=new Vector();
				lastNum=-1;
				beneficialAffect(mob,newItem,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for nectar, but nothing happens.");

		// return whether it worked
		return success;
	}
}