package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
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
   Copyright 2003-2020 Bo Zimmerman

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
public class CommonSpeaker extends StdBehavior
{
	@Override
	public String ID()
	{
		return "CommonSpeaker";
	}

	@Override
	public String accountForYourself()
	{
		return language+" speaking";
	}

	protected int		tickTocker	= 1;
	protected int		tickTock	= 0;
	protected String	language	= "Common";
	protected Language	lang		= null;
	protected String	prevLang	= null;

	@Override
	public CMObject copyOf()
	{
		final CommonSpeaker cp = (CommonSpeaker)super.copyOf();
		cp.lang=null;
		cp.prevLang=null;
		return cp;
	}

	@Override
	public void setParms(final String parameters)
	{
		lang=null;
		prevLang=null;
		super.setParms(parameters);
		if(parameters.trim().length()>0)
			language=parameters;
		else
			language="Common";
		tickTocker=1;
		tickTock=0;
	}

	@Override
	public void endBehavior(final PhysicalAgent forMe)
	{
		if(lang != null)
		{
			if(!lang.isSavable())
			{
				if(forMe instanceof MOB)
					((MOB)forMe).delAbility(lang);
				final Ability langE=forMe.fetchEffect(lang.ID());
				if((langE!=null)
				&&(!langE.isSavable()))
				{
					langE.unInvoke();
					forMe.delEffect(langE);
				}
			}
		}
		if(prevLang != null)
		{
			final Language tempL=CMLib.utensils().getLanguageSpoken(forMe);
			if((prevLang.length()==0)
			||(prevLang.equalsIgnoreCase("Common")))
			{
				if((tempL!=null)
				&&(!tempL.ID().equalsIgnoreCase("Common")))
					tempL.setBeingSpoken(tempL.ID(), false);
			}
			else
			if((tempL==null)
			||(tempL.ID().equalsIgnoreCase("Common"))
			||(!tempL.ID().equalsIgnoreCase(prevLang)))
			{

				if(tempL!=null)
					tempL.setBeingSpoken(tempL.ID(), false);
				final Language goodL=(Language)forMe.fetchEffect(prevLang);
				if(goodL!=null)
					goodL.setBeingSpoken(goodL.ID(), true);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(--tickTock>0)
			return true;
		if((++tickTocker)==100)
			tickTocker=99;
		tickTock=tickTocker;
		if(!(ticking instanceof Physical))
			return true;

		if(lang == null)
		{
			final Physical mob=(Physical)ticking;
			final Ability L=CMClass.getAbilityPrototype(language);
			if((L==null)||(!(L instanceof Language)))
			{
				if(!CMSecurity.isDisabled(DisFlag.LANGUAGES))
				{
					Log.errOut("CommonSpeaker on "+ticking.name()+" in "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(mob))
							+" has unknown language '"+language+"'");
				}
				return false;
			}
			if(prevLang == null)
			{
				final Language spokenL=CMLib.utensils().getLanguageSpoken(mob);
				if(spokenL != null)
					prevLang=spokenL.ID();
				else
					prevLang="";
			}
			if(mob instanceof MOB)
			{
				final Ability A=((MOB)mob).fetchAbility(L.ID());
				if(A==null)
				{
					lang=(Language)CMClass.getAbility(L.ID());
					lang.setProficiency(100);
					lang.setSavable(false);
					((MOB)mob).addAbility(lang);
					lang.autoInvocation((MOB)ticking, false);
				}
				else
				if(A.isSavable() || (((MOB)mob).isRacialAbility(A.ID())))
					lang=(Language)A;
				else
				if(!A.isSavable())
					lang=(Language)A;
				else
				{
					tickTock=Integer.MAX_VALUE;
					Log.debugOut("CommonSpeaker on "+ticking.name()+" in "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(mob))
							+" disabled due to temp language conflict.");
					return true; // no idea what to do here
				}
			}
			else
			{
				lang=(Language)CMClass.getAbility(L.ID());
				lang.setProficiency(100);
				lang.setSavable(false);
			}
		}


		if((ticking instanceof MOB)&&(!((MOB)ticking).isMine(lang)))
			lang=null;
		else
		{
			final Language spoken=(Language)((Physical)ticking).fetchEffect(lang.ID());
			if((spoken==null)
			||(!spoken.beingSpoken(lang.ID())))
			{
				if(ticking instanceof MOB)
				{
					lang.autoInvocation((MOB)ticking, false);
					lang.invoke((MOB)ticking,null,false,0);
				}
				else
				{
					final Language langE=(Language)lang.copyOf();
					langE.setBeingSpoken(lang.ID(), true);
					langE.setSavable(false);
					((Physical)ticking).addNonUninvokableEffect(langE);
				}
			}
		}
		return true;
	}
}
