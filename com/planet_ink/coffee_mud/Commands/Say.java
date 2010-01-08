package com.planet_ink.coffee_mud.Commands;
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
public class Say extends StdCommand
{
	public Say(){}

	private String[] access={"SAY",
                             "ASK",
                             "`",
                             "SA",
                             "SAYTO"};
	public String[] getAccessWords(){return access;}

    protected static final String[] impossibleTargets={
		"HERE",
		"THERE",
		"IS",
		"JUST",
		"A",
		"AN",
		"TO",
		"THE",
		"SOME",
		"SITS",
		"RESTS",
		"LEFT",
		"HAS",
		"BEEN"
	};

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String theWord="Say";
		boolean toFlag=false;
		if(((String)commands.elementAt(0)).equalsIgnoreCase("ASK"))
			theWord="Ask";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("YELL"))
			theWord="Yell";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("SAYTO")
		||((String)commands.elementAt(0)).equalsIgnoreCase("SAYT"))
		{
			theWord="Say";
			toFlag=true;
		}

        Room R=mob.location();
		if((commands.size()==1)||(R==null))
		{
			mob.tell(theWord+" what?");
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
				target=R.fetchFromRoomFavorMOBs(null,whom,Wearable.FILTER_ANY);
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

				if((target!=null)&&(CMLib.flags().canBeSeenBy(target,mob)))
					commands.removeElementAt(1);
				else
					target=null;
			}
		}
		String combinedCommands=CMParms.combineWithQuotes(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell(theWord+"  what?");
			return false;
		}
		if(toFlag&&((target==null)||(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			mob.tell("you don't see "+whom+" here to speak to.");
			return false;
		}
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.SYSTEM_SAYFILTER);

		CMMsg msg=null;
		if((!theWord.equalsIgnoreCase("ASK"))&&(target!=null))
		    theWord+="(s) to";
		else
		    theWord+="(s)";
		String fromSelf="^T^<SAY \""+((target!=null)?target.name():mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		String toTarget="^T^<SAY \""+mob.name()+"\"^><S-NAME> "+theWord.toLowerCase()+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		if(target==null)
			msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+mob.name()+"\"^><S-NAME> "+theWord.toLowerCase()+" '"+combinedCommands+"'^</SAY^>^?");
		else
			msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,fromSelf,toTarget,fromSelf);
		
        
        
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(theWord.toUpperCase().startsWith("YELL"))
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R2=R.getRoomInDir(d);
					Exit E2=R.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
					{
						Environmental tool=msg.tool();
						msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,"^TYou hear someone yell '"+combinedCommands+"' "+Directions.getInDirectionName(Directions.getOpDirectionCode(d))+"^?");
						if((R2.okMessage(mob,msg))
						&&((tool==null)||(tool.okMessage(mob,msg))))
						{
							R2.sendOthers(mob,msg);
						}
					}
				}
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
