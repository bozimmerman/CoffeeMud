package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Arsonry extends ThiefSkill
{
	public String ID() { return "Thief_Arsonry"; }
	public String name(){ return "Arsonry";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"ARSON","ARSONRY"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Arsonry();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("What or which direction is that which would you like to set on fire?");
			return false;
		}
		String str=Util.combine(commands,0);
		int dir=Directions.getGoodDirectionCode(str);
		Room targetRoom=null;
		Environmental target=null;
		if(dir>=0)
		{
			Room room=mob.location().getRoomInDir(dir);
			if((room==null)||(mob.location().getExitInDir(dir)==null))
			{
				mob.tell("But there's nothing that way!");
				return false;
			}
			if(!mob.location().getExitInDir(dir).isOpen())
			{
				mob.tell("That way isn't open!");
				return false;
			}
			Vector choices=new Vector();
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.fetchItem(i);
				if((I!=null)
				&&(I.container()==null)
				&&(Sense.burnStatus(I)>0))
					choices.addElement(I);
			}
			if(choices.size()==0)
			{
				mob.tell("There's nothing that way you can burn!");
				return false;
			}
			target=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
			targetRoom=room;
		}
		else
		{
			Item item=getTarget(mob,mob.location(),givenTarget,null,commands,Item.WORN_REQ_UNWORNONLY);
			if(item==null) return false;
			target=item;
			targetRoom=mob.location();
		}
		boolean proceed=false;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(Sense.isOnFire(I))&&(Sense.canBeSeenBy(I,mob)))
			{ proceed=true; break;}
		}
		if(!proceed)
		{
			mob.tell("You need to have something in your inventory on fire, like a torch, to use this skill.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-levelDiff,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> commit(s) arsonry against <T-NAME>.");
			if((mob.location().okMessage(mob,msg))
			&&((targetRoom==mob.location())||(targetRoom.okMessage(mob,msg))))
			{
				mob.location().send(mob,msg);
				if(targetRoom!=mob.location()) targetRoom.send(mob,msg);
				Ability B=CMClass.getAbility("Burning");
				B.setProfficiency(Sense.burnStatus(target));
				B.invoke(mob,target,true);
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) arsonry against <T-NAME>, but fails.");
		return success;
	}

}