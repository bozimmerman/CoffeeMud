package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Skill_Cage extends StdAbility
{
	public String ID() { return "Skill_Cage"; }
	public String name(){ return "Cage";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"CAGE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item cage=null;
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(commands.size()>0)
			{
				String last=(String)commands.lastElement();
				Item I=mob.location().fetchItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.removeElement(last);
				}
			}
		}

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((!auto)&&(!(CMSecurity.isAllowed(mob,mob.location(),"ORDER")&&target.willFollowOrdersOf(mob))))
		{
			boolean ok=false;
			if((target.isMonster())
			&&(Sense.isAnimalIntelligence(target)))
			{
				if(Sense.isSleeping(target)
				||(!Sense.canMove(target))
				||((target.amFollowing()==mob))
				||(Sense.isBoundOrHeld(target)))
					ok=true;
			}
			if(!ok)
			{
				mob.tell(target.name()+" won't seem to let you.");
				return false;
			}

			if(cage==null)
			{
				mob.tell("Cage "+target.name()+" where?");
				return false;
			}

			if(mob.isInCombat())
			{
				mob.tell("Not while you are fighting!");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		CagedAnimal caged=(CagedAnimal)CMClass.getItem("GenCaged");
		if((success)&&(caged.cageMe(target)))
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((cage!=null)&&(cage.owner()!=null))
				{
					if(cage.owner() instanceof MOB)
						((MOB)cage.owner()).addInventory((Item)caged);
					else
					if(cage.owner() instanceof Room)
						((Room)cage.owner()).addItem((Item)caged);
				}
				else
					mob.addInventory((Item)caged);
				FullMsg putMsg=new FullMsg(mob,cage,(Item)caged,CMMsg.MSG_PUT,"<S-NAME> cage(s) <O-NAME> in <T-NAME>.");
				if(mob.location().okMessage(mob,putMsg))
				{
					mob.location().send(mob,putMsg);
					target.killMeDead(false);
				}
				else
					((Item)caged).destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to cage <T-NAME> and fail(s).");


		// return whether it worked
		return success;
	}
}