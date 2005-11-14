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
public class As extends StdCommand
{
	public As(){}

	private String[] access={getScr("As","cmd")};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell(getScr("As","error"));
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		if((!CMSecurity.isAllowed(mob,mob.location(),getScr("As","cmd")))||(mob.isMonster()))
		{
			mob.tell(getScr("As","notp"));
			return false;
		}
		Session mySession=mob.session();
		MOB M=CMMap.getLoadPlayer(cmd);
		if(M==null)
			M=mob.location().fetchInhabitant(cmd);
		if(M==null)
		{
		    try
		    {
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					M=R.fetchInhabitant(cmd);
					if(M!=null) break;
				}
		    }
		    catch(NoSuchElementException e){}
		}
		if(M==null)
		{
			mob.tell(getScr("As","noname"));
			return false;
		}
		if(M.soulMate()!=null)
		{
		    mob.tell(M.Name()+" "+getScr("As","possessed"));
		    return false;
		}
		if((CMSecurity.isASysOp(M))&&(!CMSecurity.isASysOp(mob)))
		{
		    mob.tell(getScr("As","notp",M.Name()));
		    return false;
		}
        if(!M.isMonster())
        {
            if(!CMSecurity.isAllowedEverywhere(mob,"ORDER"))
            {
                mob.tell(getScr("As","notpl",M.Name()));
                return false;
            }
        }
		Session oldSession=M.session();
		Room oldRoom=M.location();
		boolean inside=(oldRoom!=null)?oldRoom.isInhabitant(M):false;
		boolean dead=M.amDead();
		int myBitmap=mob.getBitmap();
		int oldBitmap=M.getBitmap();
		M.setSession(mySession);
		mySession.setMob(M);
		M.setSoulMate(mob);
        mySession.initTelnetMode(oldBitmap);
		if(((String)commands.firstElement()).equalsIgnoreCase(getScr("As","here"))
		   ||((String)commands.firstElement()).equalsIgnoreCase("."))
		{
		    if((M.location()!=mob.location())&&(!mob.location().isInhabitant(M)))
				mob.location().bringMobHere(M,false);
			commands.removeElementAt(0);
		}
		if(dead) M.bringToLife();
		if((M.location()==null)&&(oldRoom==null)&&(mob.location()!=null))
		{
		    inside=false;
			mob.location().bringMobHere(M,false);
		}
		M.doCommand(commands);
		if(M.playerStats()!=null) M.playerStats().setUpdated(0);
		if((oldRoom!=null)&&(inside)&&(!oldRoom.isInhabitant(M)))
			oldRoom.bringMobHere(M,false);
		else
		if((oldRoom==null)||(!inside))
		{
			if(M.location()!=null)
				M.location().delInhabitant(M);
			M.setLocation(oldRoom);
		}
		M.setSoulMate(null);
		M.setSession(oldSession);
		mySession.setMob(mob);
        mySession.initTelnetMode(myBitmap);
		if(dead) M.removeFromGame();
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"AS");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
