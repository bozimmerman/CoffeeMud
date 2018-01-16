package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.AnimalHusbandry;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2018 Bo Zimmerman

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

public class Prop_MoveRestrictor extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_MoveRestrictor";
	}

	@Override
	public String name()
	{
		return "Moving restrictor";
	}

	protected String		message			= L("<S-NAME> can`t go that way.");
	protected boolean		publicMsg		= false;
	protected int[]			noDomains		= new int[0];
	protected int[]			onlyDomains		= new int[0];
	protected String		restrictKeyword	= "";
	protected String		restrictMobs	= "";
	protected String		restrictItems	= "";
	protected Set<String>	noLocaleIDs		= new TreeSet<String>();
	protected Set<String>	onlyLocaleIDs	= new TreeSet<String>();

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		message				= L("<S-NAME> can`t go that way.");
		publicMsg			= CMParms.getParmBool(newText, "PUBLIC", false);
		final List<Integer> lst = new ArrayList<Integer>();
		lst.clear();
		for(final String locals : CMParms.parseCommas(CMParms.getParmStr(newText, "NODOMAINS", ""), true))
		{
			Integer I=Room.DOMAIN_INDOOR_MAP.get(locals.toUpperCase().trim());
			if(I!=null)
				lst.add(I);
			I=Room.DOMAIN_OUTDOOR_MAP.get(locals.toUpperCase().trim());
			if(I!=null)
				lst.add(I);
		}
		noDomains	= new int[lst.size()];
		for(int i=0;i<lst.size();i++)
			noDomains[i]=lst.get(i).intValue();
		lst.clear();
		for(final String locals : CMParms.parseCommas(CMParms.getParmStr(newText, "ONLYDOMAINS", ""), true))
		{
			Integer I=Room.DOMAIN_INDOOR_MAP.get(locals.toUpperCase().trim());
			if(I!=null)
				lst.add(I);
			I=Room.DOMAIN_OUTDOOR_MAP.get(locals.toUpperCase().trim());
			if(I!=null)
				lst.add(I);
		}
		onlyDomains			= new int[lst.size()];
		for(int i=0;i<lst.size();i++)
			onlyDomains[i]=lst.get(i).intValue();
		restrictKeyword = CMParms.getParmStr(newText, "SEARCH", "");
		restrictMobs = CMParms.getParmStr(newText, "MSEARCH", "");
		restrictItems = CMParms.getParmStr(newText, "ISEARCH", "");
		List<String> lst2= CMParms.parseCommas(CMParms.getParmStr(newText, "NOLOCALES", ""), true);
		noLocaleIDs.clear();
		for(String s : lst2)
		{
			final Room R=CMClass.getLocalePrototype(s);
			if(s!=null)
				noLocaleIDs.add(R.ID());
		}
		lst2= CMParms.parseCommas(CMParms.getParmStr(newText, "ONLYLOCALES", ""), true);
		onlyLocaleIDs.clear();
		for(String s : lst2)
		{
			final Room R=CMClass.getLocalePrototype(s);
			if(s!=null)
				onlyLocaleIDs.add(R.ID());
		}
		
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&((msg.source()==affected)||(msg.source().riding() == affected))
		)
		{
			final Room R=(Room)msg.target();
			if(CMParms.contains(noDomains, R.domainType())
			||((onlyDomains.length>0)
				&&(!CMParms.contains(onlyDomains, R.domainType())))
			||((restrictKeyword.length()>0)
				&&(CMLib.english().containsString(R.displayText(msg.source()), restrictKeyword)
					||CMLib.english().containsString(R.description(msg.source()), restrictKeyword)))
			||((restrictMobs.length()>0)
				&&(R.fetchInhabitant(restrictMobs)!=null))
			||((restrictItems.length()>0)
					&&(R.fetchInhabitant(restrictItems)!=null))
			||noLocaleIDs.contains(R.ID())
			||((onlyLocaleIDs.size()>0)
				&&(!onlyLocaleIDs.contains(R.ID())))
			)
			{
				if(publicMsg)
					R.show(msg.source(), null, CMMsg.MSG_OK_ACTION, message);
				else
					msg.source().tell(message);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
