package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Thief_Kamikaze extends ThiefSkill
{
	public String ID() { return "Thief_Kamikaze"; }
	public String name(){ return "Kamikaze";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"KAMIKAZE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I.container()==null))
				{
					Trap T=CoffeeUtensils.fetchMyTrap(I);
					if((T!=null)&&(T.isABomb()))
					{
						if(!I.amWearingAt(Item.INVENTORY))
							CommonMsgs.remove(mob,I,true);
						CommonMsgs.drop(mob,I,false,false);
						if(I.owner() instanceof Room)
						{
							Room R=(Room)I.owner();
							for(int i2=0;i2<R.numInhabitants();i2++)
							{
								MOB M=(MOB)R.fetchInhabitant(i2);
								if(M!=null)
									T.spring(M);
							}
							T.disable();
							T.unInvoke();
							I.destroy();
						}
					}
				}
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(!mob.amDead())&&(mob.location()!=null))
		{
			if(mob.amFollowing()!=null)
				CommonMsgs.follow(mob,null,false);
			CommonMsgs.stand(mob,true);
			if((mob.isMonster())&&(!Sense.isMobile(mob)))
				MUDTracker.wanderAway(mob,true,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify who your kamikaze bomber is, and which direction they should go.");
			return false;
		}
		String s=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.INTELLIGENCE)<3))
		{
			mob.tell("You can't talk "+target.name()+" into a kamikaze mission.");
			return false;
		}

		if((s.length()==0)||(Util.parse(s).size()==0))
		{
			mob.tell("Send "+target.charStats().himher()+" which direction?");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int amountRequired=(int)(Math.round((100-(mob.charStats().getStat(CharStats.CHARISMA)*2)))*target.envStats().level());

		if(mob.getMoney()<amountRequired)
		{
			if(mob.getMoney()<amountRequired)
				mob.tell(target.charStats().HeShe()+" requires "+amountRequired+" coins to do this.");
			return false;
		}

		Trap bombFound=null;
		for(int i=0;i<target.inventorySize();i++)
		{
			Item I=target.fetchInventory(i);
			if((I!=null)&&(I.container()==null))
			{
				Trap T=CoffeeUtensils.fetchMyTrap(I);
				if((T!=null)&&(T.isABomb()))
				{
					bombFound=T;
					break;
				}
			}
		}
		if(bombFound==null)
		{
			mob.tell(target.name()+" must have some bombs for this to work.");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> attempt(s) to convince <T-NAMESELF> to kamikaze "+s+", but no deal is reached.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> pay(s) <T-NAMESELF> to Kamikaze "+s+" for "+amountRequired+" coins.^?");
			mob.setMoney(mob.getMoney()-amountRequired);
			mob.recoverEnvStats();
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.setMoney(target.getMoney()+amountRequired);
				target.recoverEnvStats();
				beneficialAffect(mob,target,2);
				((Trap)bombFound).activateBomb();
				commands=new Vector();
				commands.addElement("GO");
				commands.addElement(s);
				target.enqueCommand(commands,0);
			}
		}
		return success;
	}

}
