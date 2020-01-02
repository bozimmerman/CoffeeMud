package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.ThinAbility;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class ExtraData extends ThinAbility
{
	@Override
	public String ID()
	{
		return "ExtraData";
	}

	private final static String localizedName = CMLib.lang().L("Extra Data");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public boolean canAffect(final int can_code)
	{
		return true;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	protected Map<String,String> data = new Hashtable<String,String>();

	@Override
	public String text()
	{
		return CMLib.xml().toXML(this.data);
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText.trim().length()>0)
			this.data=CMLib.xml().fromXML(newMiscText);
		else
			this.data.clear();
		CODES=BASE_CODES;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	BASE_CODES	= { "CLASS", "TEXT" };

	protected volatile String[] CODES = BASE_CODES;

	@Override
	public String[] getStatCodes()
	{
		if(CODES == BASE_CODES)
		{
			synchronized(this)
			{
				if(CODES == BASE_CODES)
				{
					CODES = Arrays.copyOf(BASE_CODES, BASE_CODES.length + data.size());
					int index=BASE_CODES.length;
					for(final String keyStr : data.keySet())
						CODES[index++] = keyStr;
				}
			}
		}
		return CODES;
	}

	@Override
	public String getStat(final String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return text();
		default:
		{
			if(code == null)
				return "";
			synchronized(this.data)
			{
				if(!data.containsKey(code.toUpperCase().trim()))
				{
					this.data.put(code.toUpperCase().trim(), "");
					CODES=BASE_CODES;
				}
				return this.data.get(code.toUpperCase().trim());
			}
		}
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setMiscText(val);
			break;
		default:
		{
			if(code == null)
				return;
			synchronized(this.data)
			{
				if(!this.data.containsKey(code.toUpperCase().trim()))
					CODES=BASE_CODES;
				if(val == null)
					this.data.remove(code.toUpperCase().trim());
				else
					this.data.put(code.toUpperCase().trim(), val);
			}
		}
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof ThinAbility))
			return false;
		final String[] CODES=this.getStatCodes();
		for(int i=0;i<CODES.length;i++)
		{
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}

	private void cloneFix(final ExtraData E)
	{
		this.data.clear();
		this.data.putAll(E.data);
		CODES=BASE_CODES;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final ExtraData E=(ExtraData)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.ABILITY);//removed for mem & perf
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
}
