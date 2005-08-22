package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

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
public class Say extends StdCommand
{
	public Say(){}

	private String[] access={getScr("Say","saycmd1"),getScr("Say","saycmd2"),"`",getScr("Say","saycmd3"),getScr("Say","saycmd4")};
	public String[] getAccessWords(){return access;}

	private static final String[] impossibleTargets={
		getScr("Say","imptarget1"),
		getScr("Say","imptarget2"),
		getScr("Say","imptarget3"),
		getScr("Say","imptarget4"),
		getScr("Say","imptarget5"),
		getScr("Say","imptarget6"),
		getScr("Say","imptarget7"),
		getScr("Say","imptarget8"),
		getScr("Say","imptarget9"),
		getScr("Say","imptarget10"),
		getScr("Say","imptarget11"),
		getScr("Say","imptarget12"),
		getScr("Say","imptarget13"),
		getScr("Say","imptarget14")
	};

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String theWord=getScr("Say","theword1");
		boolean toFlag=false;
		if(((String)commands.elementAt(0)).equalsIgnoreCase(getScr("Say","saycmd2")))
			theWord=getScr("Say","theword2");
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase(getScr("Say","saycmd5")))
			theWord=getScr("Say","theword3");
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase(getScr("Say","saycmd4"))
		||((String)commands.elementAt(0)).equalsIgnoreCase(getScr("Say","saycmd4b")))
		{
			theWord=getScr("Say","theword1");
			toFlag=true;
		}

		if(commands.size()==1)
		{
			mob.tell(theWord+" "+getScr("Say","saywhat"));
			return false;
		}

		String whom="";
		Environmental target=null;
		if(commands.size()>2)
		{
			whom=((String)commands.elementAt(1)).toUpperCase();
			if(!toFlag)
				for(int i=0;i<impossibleTargets.length;i++)
					if(impossibleTargets[i].startsWith(whom))
					{ whom=""; break;}
			if(whom.length()>0)
			{
				target=mob.location().fetchFromRoomFavorMOBs(null,whom,Item.WORN_REQ_ANY);
				if((toFlag)&&(target==null))
				    target=mob.fetchInventory(null,whom);

				if((!toFlag)&&(target!=null))
				{
					if(!(target instanceof MOB))
						target=null;
					else
					if(target.name().toUpperCase().indexOf(whom.toUpperCase())<0)
						target=null;
					else
					if((!target.name().equalsIgnoreCase(whom))&&(whom.length()<4))
						target=null;
				}

				if((target!=null)&&(Sense.canBeSeenBy(target,mob)))
					commands.removeElementAt(1);
				else
					target=null;
			}
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=Util.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell(theWord+" "+getScr("Say","saywhat"));
			return false;
		}
		if(toFlag&&((target==null)||(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell(getScr("Say","saysee",whom));
			return false;
		}
		combinedCommands=CommonStrings.applyFilter(combinedCommands,CommonStrings.SYSTEM_SAYFILTER);

		FullMsg msg=null;
		if((!theWord.equalsIgnoreCase(getScr("Say","saycmd2")))&&(target!=null))
		    theWord+=getScr("Say","saysto");
		else
		    theWord+=getScr("Say","says");
		String fromSelf="^T^<SAY \""+((target!=null)?target.name():mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		String toTarget="^T^<SAY \""+mob.name()+"\"^><S-NAME> "+theWord.toLowerCase()+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		if(target==null)
			msg=new FullMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+mob.name()+"\"^><S-NAME> "+theWord.toLowerCase()+" '"+combinedCommands+"'^</SAY^>^?");
		else
			msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,fromSelf,toTarget,fromSelf);

		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(theWord.equalsIgnoreCase(getScr("Say","saycmd5")))
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().getRoomInDir(d);
					Exit E=mob.location().getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,getScr("Say","yell1")+" "+combinedCommands+"' "+Directions.getInDirectionName(Directions.getOpDirectionCode(d))+"^?");
						if(R.okMessage(mob,msg))
							R.sendOthers(mob,msg);
					}
				}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
