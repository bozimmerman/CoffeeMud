package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Unlock extends StdCommand
{
	public Unlock(){}

	private String[] access={"UNLOCK","UNL","UN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatTounlock=Util.combine(commands,1);
		if(whatTounlock.length()==0)
		{
			mob.tell(getScr("Movement","unlockerr1"));
			return false;
		}
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatTounlock,Item.WORN_REQ_ANY);

		if((unlockThis==null)||(!Sense.canBeSeenBy(unlockThis,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",whatTounlock));
			return false;
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,CMMsg.MSG_UNLOCK,getScr("Movement","sunlocks")+CommonStrings.msp("doorunlock.wav",10));
		if(unlockThis instanceof Exit)
		{
			boolean locked=((Exit)unlockThis).isLocked();
			if((mob.location().okMessage(msg.source(),msg))
			&&(locked))
			{
				mob.location().send(msg.source(),msg);
				if(dirCode<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					if(mob.location().getExitInDir(d)==unlockThis)
					{dirCode=d; break;}

				if((dirCode>=0)&&(mob.location().getRoomInDir(dirCode)!=null))
				{
					Room opR=mob.location().getRoomInDir(dirCode);
					Exit opE=mob.location().getPairedExit(dirCode);
					if(opE!=null)
					{
						FullMsg altMsg=new FullMsg(msg.source(),opE,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
						opE.executeMsg(msg.source(),altMsg);
					}
					int opCode=Directions.getOpDirectionCode(dirCode);
					if((opE!=null)
					&&(!opE.isLocked())
					&&(!((Exit)unlockThis).isLocked()))
					   opR.showHappens(CMMsg.MSG_OK_ACTION,getScr("Movement","afterunlocks",opE.name(),Directions.getInDirectionName(opCode)));
				}
			}
		}
		else
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
