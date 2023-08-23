package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_RacialMount extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_RacialMount";
	}

	private final static String localizedName = CMLib.lang().L("Racial Mount");

	@Override
	public String name()
	{
		return localizedName;
	}


	private String desc = null;

	@Override
	public String displayText()
	{
		if(desc == null)
		{
			if(affected instanceof MOB)
			{
				final Pair<String, String> p = getMountInfo((MOB)affected);
				if (p != null)
					desc = p.first;
			}
		}
		if(desc == null)
			return L("(Affinity for a mount)");
		else
			return L("(Affinity for "+desc+" mounts)");
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		desc = null;
		super.setAffectedOne(P);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected String getRacialCategory(final String str)
	{
		for(final Enumeration<Race> r = CMClass.races();r.hasMoreElements();)
		{
			final Race R = r.nextElement();
			if(R.racialCategory().equalsIgnoreCase(str))
				return R.racialCategory();
		}
		return null;
	}

	final static Pair<String, String> defaultMount = new Pair<String,String>("Equine","Horse");

	protected Pair<String, String> getMountInfo(final MOB mob)
	{
		if(mob == null)
			return null;
		@SuppressWarnings("unchecked")
		Map<String,Pair<String,String>> mapH=(Map<String,Pair<String,String>>)Resources.getResource("PARSED_RECIPE: racialmounts.txt");
		if(mapH==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+"racialmounts.txt",null,CMFile.FLAG_LOGERRORS).text();
			mapH = new Hashtable<String,Pair<String,String>>();
			for(final String lstr :  Resources.getFileLineVector(str) )
			{
				if(lstr.startsWith("#"))
					continue;
				final List<String> V = CMParms.parseTabs(lstr, true);
				if(V.size()>=3)
				{
					final String key;
					final String posskey = V.get(0);
					if(posskey.equals("*"))
						key = "*";
					else
					{
						final Race sR = CMClass.getRace(posskey);
						if(sR != null)
							key = sR.ID();
						else
							key = getRacialCategory(posskey);
					}
					if(key == null)
						Log.errOut(ID(),"Unknown racial mount host key "+posskey);
					else
					{
						final String cat = getRacialCategory(V.get(1));
						final Race rR = CMClass.getRace(V.get(2));
						if (rR == null)
							Log.errOut(ID(),"Unknown racial mount race "+V.get(2));
						else
						if (cat == null)
							Log.errOut(ID(),"Unknown racial mount category "+V.get(1));
						else
							mapH.put(key, new Pair<String,String>(cat,rR.ID()));
					}
				}
			}
			Resources.submitResource("PARSED_RECIPE: racialmounts.txt",mapH);
		}
		final Race R = mob.baseCharStats().getMyRace();
		Pair<String,String> p = mapH.get(R.ID());
		if(p == null)
		{
			p = mapH.get(R.racialCategory());
			if(p == null)
			{
				p = mapH.get("*");
				if(p == null)
					p = defaultMount;
			}
		}
		return p;
	}

	protected boolean isMount(final MOB mob, final MOB P)
	{
		return (mob.riding()==P)
			&&(getMountInfo(mob).first.equals(P.baseCharStats().getMyRace().racialCategory()));
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source() instanceof Rideable)
		&&(msg.sourceMinor() == CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&(affected instanceof MOB)
		&&isMount((MOB)affected,msg.source()))
		{
			final MOB rider=(MOB)affected;
			final int mv = msg.source().curState().getMovement();
			final Room R = (Room)msg.target();
			if((mv<=msg.source().maxState().getMovement()-R.pointsPerMove())
			&&(proficiencyCheck(rider,0,false)))
			{
				final int gain = (int)Math.round(CMath.mul(R.pointsPerMove(),0.5 + CMath.div(super.getXLEVELLevel(rider), 10.0)));
				if(gain <= 0)
					msg.source().curState().adjMovement(1, msg.source().maxState());
				else
					msg.source().curState().adjMovement(gain, msg.source().maxState());
			}
		}
		super.executeMsg(myHost, msg);
	}
}
