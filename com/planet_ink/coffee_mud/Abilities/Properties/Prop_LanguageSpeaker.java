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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class Prop_LanguageSpeaker extends Property
{
	@Override
	public String ID()
	{
		return "Prop_LanguageSpeaker";
	}

	@Override
	public String name()
	{
		return "Forces language speaking";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	protected boolean doPlayers=false;
	protected boolean noMobs=false;
	protected boolean homeOnly=false;
	protected MaskingLibrary.CompiledZMask mobMask = null;
	protected Language lang = null;
	protected String langStr = "";

	private final Room homeRoom = null;
	private final Area homeArea = null;
	private CMClass.CMObjectType affectedType = CMClass.CMObjectType.AREA;

	@Override
	public void setMiscText(String txt)
	{

		doPlayers=CMParms.getParmBool(txt,"PLAYERS",false);
		noMobs=CMParms.getParmBool(txt,"NOMOBS",false);
		homeOnly=CMParms.getParmBool(txt,"HOMEONLY",false);
		langStr=CMParms.getParmStr(txt,"LANGUAGE","").trim();
		final int x=txt.indexOf(';');
		mobMask=null;
		if((x>=0)&&(txt.substring(x+1).trim().length()>0))
			mobMask=CMLib.masking().getPreCompiledMask(txt.substring(x+1).trim());
		lang=null;
		super.setMiscText(txt);
	}

	@Override
	public void setAffectedOne(Physical P)
	{
		affectedType = CMClass.getType(P);
		super.setAffectedOne(P);
	}

	public Language getLanguage()
	{
		if((lang == null)&&(langStr.trim().length()>0))
		{
			lang=(Language)CMClass.getAbility(langStr.trim());
			langStr="";
		}
		return lang;
	}

	@Override
	public String accountForYourself()
	{
		return "Forces speaking the language: "+((lang!=null)?lang.name():"?");
	}

	public void startSpeaking(MOB mob)
	{
		final Room mobHomeRoom=mob.getStartRoom();
		final Area mobHomeArea=((mobHomeRoom==null)?null:mobHomeRoom.getArea());
		if(((lang!=null)||(langStr.length()>0))
		&&(doPlayers || mob.isMonster())
		&&((!noMobs) || (!mob.isMonster()))
		&&((!homeOnly) || (mobHomeRoom == homeRoom))
		&&((!homeOnly) || (mobHomeArea == homeArea))
		&&(mob.fetchEffect(langStr)==null)
		&&((mobMask==null) || CMLib.masking().maskCheck(mobMask,mob,true)))
		{
			if(lang == null)
				lang = getLanguage();
			if(lang == null)
			{
				lang=(Language)CMClass.getAbility("Common");
				Log.errOut("Prop_LanguageSpeaker","Unknown language "+langStr);
			}
			if(lang != null)
			{
				switch(affectedType)
				{
				case AREA:
					lang=(Language)lang.copyOf();
					break;
				case LOCALE:
					lang=(Language)lang.copyOf();
					break;
				case MOB:
					break;
				case EXIT:
					lang=(Language)lang.copyOf();
					break;
				default: // item
					break;
				}
				mob.addNonUninvokableEffect(lang);
				lang.setSavable(false);
				lang.invoke(mob,mob,false,0);
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected!=null)
			switch(affectedType)
			{
			case AREA:
			{
				if(msg.targetMinor()==CMMsg.TYP_ENTER)
					startSpeaking(msg.source());
				else
				if(msg.sourceMinor()==CMMsg.TYP_LIFE)
					startSpeaking(msg.source());
				break;
			}
			case LOCALE:
			{
				if((msg.target() == affected)
				&&(msg.targetMinor()==CMMsg.TYP_ENTER))
					startSpeaking(msg.source());
				else
				if(msg.sourceMinor()==CMMsg.TYP_LIFE)
					startSpeaking(msg.source());
				break;
			}
			case MOB:
			{
				if(lang==null)
					startSpeaking((MOB)affected);
				break;
			}
			case EXIT:
			{
				if((msg.targetMinor()==CMMsg.TYP_ENTER)
				&&(msg.tool()==affected))
					startSpeaking(msg.source());
				break;
			}
			default: // item
			{
				if((msg.target() == affected)
				&&(msg.targetMinor()==CMMsg.TYP_GET)
				&&((lang==null)||(lang.affecting()!=msg.source())))
				{
					if((lang!=null)&&(lang.affecting()!=null))
						lang.affecting().delEffect(lang);
					startSpeaking(msg.source());
				}
				else
				if((msg.target() == affected)
				&&(msg.targetMinor()==CMMsg.TYP_DROP)
				&&(lang!=null)
				&&(lang.affecting()!=null))
				{
					lang.affecting().delEffect(lang);
					lang.setAffectedOne(null);
				}
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
