package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class Prop_OpenPassword extends Property
{
	@Override
	public String ID()
	{
		return "Prop_OpenPassword";
	}

	@Override
	public String name()
	{
		return "Opening Password";
	}

	private String password = "";
	private String languageID = "";

	@Override
	public void setMiscText(String newMiscText)
	{
		int x=newMiscText.indexOf(';');
		if(x>0)
		{
			password=newMiscText.substring(x+1).trim();
			languageID=newMiscText.substring(0,x);
			if(!(CMClass.getAbility(languageID) instanceof Language))
			{
				if(affected != null)
					Log.errOut("Prop_OpenPassword on "+affected.Name()+" has invalid languageID: "+affected.Name());
			}
		}
		else
			password=newMiscText;
		super.setMiscText(newMiscText);
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS | Ability.CAN_EXITS;
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(affected!=null)
		&&(msg.sourceMessage()!=null)
		&&((msg.sourceMajor()&CMMsg.MASK_MAGIC)==0)
		&&((languageID.trim().length()==0)
			||((msg.tool()!=null)&&(msg.tool().ID().equalsIgnoreCase(languageID)))))
		{
			final int start=msg.sourceMessage().indexOf("\'");
			final int end=msg.sourceMessage().lastIndexOf("\'");
			if((start>0)&&(end>start))
			{
				final String str=msg.sourceMessage().substring(start+1,end).trim();
				final MOB mob=msg.source();
				if(str.equalsIgnoreCase(password)
				&&(password.length()>0)
				&&(mob.location()!=null))
				{
					final Room R=mob.location();
					if(affected instanceof Exit)
					{
						final Exit E=(Exit)affected;
						if(!E.isOpen())
						{
							int dirCode=-1;
							for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
							{
								if(R.getExitInDir(d)==E)
								{
									dirCode = d;
									break;
								}
							}
							if(dirCode>=0)
							{
								CMMsg msg2=CMClass.getMsg(mob,E,null,CMMsg.MSG_UNLOCK,null);
								CMLib.utensils().roomAffectFully(msg2,R,dirCode);
								msg2=CMClass.getMsg(mob,E,null,CMMsg.MSG_OPEN,L("<T-NAME> opens."));
								CMLib.utensils().roomAffectFully(msg2,R,dirCode);
							}
						}
					}
					else
					if(affected instanceof Container)
					{
						CMMsg msg2=CMClass.getMsg(mob,affected,null,CMMsg.MSG_UNLOCK,null);
						affected.executeMsg(mob,msg2);
						msg2=CMClass.getMsg(mob,affected,null,CMMsg.MSG_OPEN,L("<T-NAME> opens."));
						affected.executeMsg(mob,msg2);
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
}
