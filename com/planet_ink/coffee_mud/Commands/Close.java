package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Close extends StdCommand
{
	public Close(){}
	
	private String[] access={"CLOSE","CLOS","CLO","CL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatToClose=Util.combine(commands,1);
		if(whatToClose.length()==0)
		{
			mob.tell(getScr("Movement","closeerr1"));
			return false;
		}
		Environmental closeThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToClose);
		if(dirCode>=0)
			closeThis=mob.location().getExitInDir(dirCode);
		if(closeThis==null)
			closeThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatToClose,Item.WORN_REQ_ANY);

		if((closeThis==null)||(!Sense.canBeSeenBy(closeThis,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",whatToClose));
			return false;
		}
		String closeWord=((closeThis==null)||(!(closeThis instanceof Exit)))?getScr("Movement","scloseword"):((Exit)closeThis).closeWord();
		FullMsg msg=new FullMsg(mob,closeThis,null,CMMsg.MSG_CLOSE,getScr("Movement","scloses",closeWord)+CommonStrings.msp("dooropen.wav",10));
		if(closeThis instanceof Exit)
		{
			boolean open=((Exit)closeThis).isOpen();
			if((mob.location().okMessage(msg.source(),msg))
			&&(open))
			{
				mob.location().send(msg.source(),msg);
				if(dirCode<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					if(mob.location().getExitInDir(d)==closeThis)
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
					&&(!opE.isOpen())
					&&(!((Exit)closeThis).isOpen()))
					   opR.showHappens(CMMsg.MSG_OK_ACTION,getScr("Movement","aftercloses",opE.name(),Directions.getInDirectionName(opCode)));
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
