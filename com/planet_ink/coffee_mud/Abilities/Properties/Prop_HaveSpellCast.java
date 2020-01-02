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
   Copyright 2001-2020 Bo Zimmerman

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
public class Prop_HaveSpellCast extends Prop_SpellAdder
{
	@Override
	public String ID()
	{
		return "Prop_HaveSpellCast";
	}

	@Override
	public String name()
	{
		return "Casting spells when owned";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected Item	myItem	= null;

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_GET;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_CASTER;
	}

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Casts ", " on the owner.");
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		if(P==null)
		{
			if((lastMOB instanceof MOB)
			&&(((MOB)lastMOB).location()!=null))
				removeMyAffectsFromLastMOB();
		}
		super.setAffectedOne(P);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		if(processing)
			return;
		try
		{
			processing=true;
			if(host instanceof Item)
			{
				myItem=(Item)host;

				if((lastMOB instanceof MOB)
				&&((myItem.owner()!=lastMOB)||(myItem.amDestroyed()))
				&&(((MOB)lastMOB).location()!=null))
					removeMyAffectsFromLastMOB();

				if((lastMOB==null)
				&&(myItem.owner() instanceof MOB)
				&&(((MOB)myItem.owner()).location()!=null))
					addMeIfNeccessary(myItem.owner(),myItem.owner(),true,0,maxTicks);
			}
		}
		finally
		{
			processing=false;
		}
	}

	@Override
	public String getStat(final String code)
	{
		if(code == null)
			return "";
		if(code.equalsIgnoreCase("STAT-LEVEL"))
		{
			int level = 0;
			for(final Pair<Ability,Integer> p : this.getMySpellsV())
			{
				final Ability A=p.first;
				if(A!=null)
				{
					int mul=1;
					if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
						mul=-1;
					level += ((mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID())));
				}
			}
			return ""+level;
		}
		else
		if(code.toUpperCase().startsWith("STAT-"))
			return "";
		return super.getStat(code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(code!=null)
		{
			if(code.equalsIgnoreCase("STAT-LEVEL"))
			{

			}
			else
			if(code.equalsIgnoreCase("TONEDOWN"))
			{
				setStat("TONEDOWN-MISC",val);
			}
			else
			if((code.equalsIgnoreCase("TONEDOWN-ARMOR"))
			||(code.equalsIgnoreCase("TONEDOWN-WEAPON"))
			||(code.equalsIgnoreCase("TONEDOWN-MISC")))
			{
				/*
				final double pct=CMath.s_pct(val);
				final String s=text();
				int plusminus=s.indexOf('+');
				int minus=s.indexOf('-');
				if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
					plusminus=minus;
				while(plusminus>=0)
				{
					minus=s.indexOf('-',plusminus+1);
					plusminus=s.indexOf('+',plusminus+1);
					if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
						plusminus=minus;
				}
				setMiscText(s);
				*/
			}
		}
		else
			super.setStat(code, val);
	}
}
