package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Trap_RoomPit extends StdTrap
{
	public String ID() { return "Trap_RoomPit"; }
	public String name(){ return "pit trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 1;}
	public String requiresToSet(){return "";}
	protected Vector pit=null;

	public void unInvoke()
	{
		if((pit!=null)
		&&(canBeUninvoked())
		&&(pit.size()>1))
		{
			Room R1=(Room)pit.firstElement();
			Room R2=(Room)pit.lastElement();
			while(R1.numInhabitants()>0)
			{
				MOB M=R1.fetchInhabitant(0);
				if(M!=null){
					M.killMeDead(false);
					R1.delInhabitant(M);
				}
			}
			while(R2.numInhabitants()>0)
			{
				MOB M=R2.fetchInhabitant(0);
				if(M!=null){
					M.killMeDead(false);
					R2.delInhabitant(M);
				}
			}
			Room R=R2.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.getRoomInDir(Directions.DOWN)==R2))
			{
				R.rawDoors()[Directions.DOWN]=null;
				R.setRawExit(Directions.DOWN,null);
			}
			R2.rawDoors()[Directions.UP]=null;
			R2.setRawExit(Directions.UP,null);
			R2.rawDoors()[Directions.DOWN]=null;
			R2.setRawExit(Directions.DOWN,null);
			R1.rawDoors()[Directions.UP]=null;
			R1.setRawExit(Directions.UP,null);
			pit=null;
            R1.destroy();
            R2.destroy();
			super.unInvoke();
		}
		else
		{
			pit=null;
			super.unInvoke();
		}
	}

    public boolean canSetTrapOn(MOB mob, Environmental E)
    {
        if(!super.canSetTrapOn(mob,E)) return false;
        if(E instanceof Room)
        {
            if(((Room)E).getRoomInDir(Directions.DOWN)!=null)
            {
                mob.tell("The flooring here won't support a pit.");
                return false;
            }
        }
        return true;
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if((tickID==Tickable.TICKID_TRAP_DESTRUCTION)
		&&(canBeUninvoked())
		&&(pit!=null)
		&&(pit.size()>1)
		&&(((((Room)pit.firstElement()).numPCInhabitants()>0)
			||(((Room)pit.lastElement()).numPCInhabitants()>0))))
			return true;
		return super.tick(ticking,tickID);
	}

	protected synchronized void makePit(MOB target)
	{
		if((pit==null)||(pit.size()<2))
		{
			Vector V=new Vector();
			Room myPitUp=CMClass.getLocale("ClimbableSurface");
			myPitUp.setRoomID("");
			myPitUp.setArea(target.location().getArea());
			myPitUp.baseEnvStats().setDisposition(myPitUp.baseEnvStats().disposition()|EnvStats.IS_DARK);
			myPitUp.setDisplayText("Inside a dark pit");
			myPitUp.setDescription("The walls here are slick and tall.  The trap door has already closed.");
			myPitUp.recoverEnvStats();

			Room myPit=CMClass.getLocale("StdRoom");
			myPit.setRoomID("");
			myPit.setArea(target.location().getArea());
			myPit.baseEnvStats().setDisposition(myPit.baseEnvStats().disposition()|EnvStats.IS_DARK);
			myPit.setDisplayText("Inside a dark pit");
			myPit.setDescription("The walls here are slick and tall.  You can barely see the closed trap door well above you.");
			myPit.setRawExit(Directions.UP,CMClass.getExit("StdOpenDoorway"));
			myPit.rawDoors()[Directions.UP]=myPitUp;
			myPitUp.setRawExit(Directions.DOWN,CMClass.getExit("StdOpenDoorway"));
			myPitUp.rawDoors()[Directions.DOWN]=myPit;
			myPitUp.recoverEnvStats();
			V.addElement(myPit);
			V.addElement(myPitUp);
			pit=V;
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		boolean unSpring=false;
		if((!sprung)
		&&(affected instanceof Room)
		&&(msg.amITarget(affected))
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!msg.source().isMine(affected)))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Exit))
		{
			Room room=(Room)affected;
			if((room.getExitInDir(Directions.DOWN)==msg.tool())
			||(room.getReverseExit(Directions.DOWN)==msg.tool()))
			{
				unSpring=true;
				sprung=true;
			}
		}
		super.executeMsg(myHost,msg);
		if(unSpring) sprung=false;
	}
	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
			CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
		}
		CMLib.commands().postLook(target,true);
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null)&&(!CMLib.flags().isInFlight(target)))
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet()).contains(target))
			||(target==invoker())
			||(CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) falling into a pit!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> fall(s) into a pit!"))
			{
				super.spring(target);
				makePit(target);
				((Room)pit.lastElement()).setRawExit(Directions.UP,CMClass.getExit("StdClosedDoorway"));
				((Room)pit.lastElement()).rawDoors()[Directions.UP]=target.location();
				if((target.location().getRoomInDir(Directions.DOWN)==null)
				&&(target.location().getExitInDir(Directions.DOWN)==null))
				{
					target.location().setRawExit(Directions.DOWN,CMClass.getExit("StdClosedDoorway"));
					target.location().rawDoors()[Directions.DOWN]=((Room)pit.lastElement());
				}
				((Room)pit.firstElement()).bringMobHere(target,false);
				finishSpringing(target);
			}
		}
	}
}
