package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Items.Basic.StdJournal;
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
public class ArchonJournal extends StdJournal implements ArchonOnly
{
	public String ID(){	return "ArchonJournal";}
	public ArchonJournal()
	{
		super();

		setName("");
		setDisplayText("the Archon Journal");
		setDescription("a fabulous tome of wisdom has been left here");
		secretIdentity="The Archon's Journal.  Just READ me.";
		baseGoldValue=20000;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_GET:
			if(mob.charStats().getClassLevel("Archon")<0)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,name()+" flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		}
		return true;
	}

}
