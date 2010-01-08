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
import java.io.IOException;

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
public class Restring extends StdCommand
{
	public Restring(){}

	private String[] access={"RESTRING"};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String allWord=CMParms.combine(commands,1);
		int x=allWord.indexOf("@");
		MOB srchMob=mob;
		Item srchContainer=null;
		Room srchRoom=mob.location();
		if(x>0)
		{
			String rest=allWord.substring(x+1).trim();
			allWord=allWord.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					Item I = srchRoom.fetchItem(null, rest);
					if(I instanceof Container)
						srchContainer=(Container)I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
				}
				else
				{
					srchMob=M;
					srchRoom=null;
				}
			}
		}
		Environmental thang=null;
		if((srchMob!=null)&&(srchRoom!=null))
			thang=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,srchContainer,allWord,Wearable.FILTER_ANY);
		else
		if(srchMob!=null)
			thang=srchMob.fetchInventory(allWord);
		else
		if(srchRoom!=null)
			thang=srchRoom.fetchFromRoomFavorItems(srchContainer,allWord,Wearable.FILTER_ANY);
		if((thang!=null)&&(thang instanceof Item))
		{
			if(!thang.isGeneric())
				mob.tell(thang.name()+" can not be restrung.");
			else
			{
				int showFlag=-1;
				if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
					showFlag=-999;
				boolean ok=false;
				while(!ok)
				{
					int showNumber=0;
                    CMLib.genEd().genName(mob,thang,++showNumber,showFlag);
                    CMLib.genEd().genDisplayText(mob,thang,++showNumber,showFlag);
                    CMLib.genEd().genDescription(mob,thang,++showNumber,showFlag);
					if(showFlag<-900){ ok=true; break;}
					if(showFlag>0){ showFlag=-1; continue;}
					showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
					if(showFlag<=0)
					{
						showFlag=-1;
						ok=true;
					}
				}
			}
			thang.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
		}
		else
			mob.tell("'"+allWord+"' can not be restrung.");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD")
		     ||CMSecurity.isAllowed(mob,mob.location(),"RESTRING");
	}

	
}
