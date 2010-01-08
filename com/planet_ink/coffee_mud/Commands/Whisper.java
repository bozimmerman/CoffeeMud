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
public class Whisper extends StdCommand
{
	public Whisper(){}

	private String[] access={"WHISPER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()==1)
		{
			mob.tell("Whisper what?");
			return false;
		}
		Environmental target=null;
		Room R = mob.location();
		if(commands.size()>2)
		{
			String possibleTarget=(String)commands.elementAt(1);
			target=R.fetchFromRoomFavorMOBs(null,possibleTarget,Wearable.FILTER_ANY);
			if((target!=null)&&(!target.name().equalsIgnoreCase(possibleTarget))&&(possibleTarget.length()<4))
			   target=null;
			if((target!=null)
			&&(CMLib.flags().canBeSeenBy(target,mob))
			&&((!(target instanceof Rider))
			   ||(((Rider)target).riding()==mob.riding())))
				commands.removeElementAt(1);
			else
				target=null;
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell("Whisper what?");
			return false;
		}

		CMMsg msg=null;
		if(target==null)
		{
			Rideable riddenR=mob.riding();
			if(riddenR==null)
			{
				msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) to <S-HIM-HERSELF> '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
                                              CMMsg.NO_EFFECT,null,
                                              CMMsg.MSG_QUIETMOVEMENT,"^T<S-NAME> whisper(s) to <S-HIM-HERSELF>.^?"+CMProps.msp("whisper.wav",40));
				if(R.okMessage(mob,msg))
					R.send(mob,msg);
			}
			else
			{
				msg=CMClass.getMsg(mob,riddenR,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around <T-NAMESELF> '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
								CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around <T-NAMESELF> '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
								CMMsg.NO_EFFECT,null);
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					Vector<Environmental> targets = new Vector<Environmental>();
					for(int i=0;i<R.numInhabitants();i++)
						targets.addElement(R.fetchInhabitant(i));
					for(Enumeration<Environmental> e = targets.elements();e.hasMoreElements();)
					{
						Environmental E=e.nextElement();
						if(E!=null)
						{
							if( (E instanceof MOB) && (riddenR != null) && riddenR.amRiding((MOB)E))
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+riddenR.name()+" '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
												CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+riddenR.name()+" '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
												CMMsg.NO_EFFECT,null);
							else
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+riddenR.name()+" '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
												CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) something around "+riddenR.name()+".^?"+CMProps.msp("whisper.wav",40),
												CMMsg.NO_EFFECT,null);
							if(R.okMessage(mob,msg))
								R.sendOthers(mob,msg);
						}
					}
				}
			}
		}
		else
		{
			msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T^<WHISPER \""+target.name()+"\"^><S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'.^</WHISPER^>^?"+CMProps.msp("whisper.wav",40)
										   ,CMMsg.MSG_SPEAK,"^T^<WHISPER \""+target.name()+"\"^><S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'^</WHISPER^>.^?"+CMProps.msp("whisper.wav",40)
										   ,CMMsg.MSG_QUIETMOVEMENT,"^T<S-NAME> whisper(s) something to <T-NAMESELF>.^</WHISPER^>^?"+CMProps.msp("whisper.wav",40));
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
