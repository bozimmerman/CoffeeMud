package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Knock extends StdCommand
{
	public Knock(){}
	
	private String[] access={"KNOCK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<=1)
		{
			mob.tell(getScr("Movement","knockerr1"));
			return false;
		}
		String knockWhat=Util.combine(commands,1).toUpperCase();
		int dir=MUDTracker.findExitDir(mob,mob.location(),knockWhat);
		if(dir<0)
		{
			Environmental getThis=mob.location().fetchFromMOBRoomItemExit(mob,null,knockWhat,Item.WORN_REQ_UNWORNONLY);
			if(getThis==null)
			{
				mob.tell(getScr("Movement","youdontsee",knockWhat.toLowerCase()));
				return false;
			}
			FullMsg msg=new FullMsg(mob,getThis,null,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,getScr("Movement","knockmsg")+CommonStrings.msp("knock.wav",50));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);

		}
		else
		{
			Exit E=mob.location().getExitInDir(dir);
			if(E==null)
			{
				mob.tell(getScr("Movement","knockerr1"));
				return false;
			}
			if(!E.hasADoor())
			{
				mob.tell(getScr("Movement","knockerr2",E.name()));
				return false;
			}
			FullMsg msg=new FullMsg(mob,E,null,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,getScr("Movement","knockmsg")+CommonStrings.msp("knock.wav",50));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				E=mob.location().getPairedExit(dir);
				Room R=mob.location().getRoomInDir(dir);
				if((R!=null)&&(E!=null)&&(E.hasADoor())
				&&(R.showOthers(mob,E,null,CMMsg.MSG_KNOCK,getScr("Movement","knockmsg2")+CommonStrings.msp("knock.wav",50)))
				&&((R.domainType()&Room.INDOORS)==Room.INDOORS))
				{
					Vector V=new Vector();
					V.addElement(mob.location());
					MUDTracker.getRadiantRooms(R,V,true,false,false,null,5);
					V.removeElement(mob.location());
					for(int v=0;v<V.size();v++)
					{
						Room R2=(Room)V.elementAt(v);
						int dir2=MUDTracker.radiatesFromDir(R2,V);
						if((dir2>=0)&&((R2.domainType()&Room.INDOORS)==Room.INDOORS))
						{
							Room R3=R2.getRoomInDir(dir2);
							if(((R3!=null)&&(R3.domainType()&Room.INDOORS)==Room.INDOORS))
								R2.showHappens(CMMsg.MASK_SOUND|CMMsg.TYP_KNOCK,getScr("Movement","knockmsg3",Directions.getInDirectionName(dir2))+CommonStrings.msp("knock.wav",50));
						}
					}
				}
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
