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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prop_CommonTwister extends Property
{
	@Override
	public String ID()
	{
		return "Prop_CommonTwister";
	}

	@Override
	public String name()
	{
		return "Common Twister";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_EXITS | Ability.CAN_ROOMS | Ability.CAN_AREAS | Ability.CAN_ITEMS | Ability.CAN_MOBS;
	}

	protected List<Triad<String, String, String>>	changes	= new Vector<Triad<String, String, String>>();

	@Override
	public String accountForYourself()
	{
		return "Twists around what the gathering common skills gives you.";
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		changes.clear();
		final List<String> V=CMParms.parseSemicolons(text,true);
		for(int v=0;v<V.size();v++)
		{
			final String s=V.get(v);
			final String skill=CMParms.getParmStr(s,"SKILL","");
			final String mask=CMParms.getParmStr(s,"MASK","");
			if((skill.length()>0)&&(mask.length()>0))
				changes.add(new Triad<String,String,String>(skill,mask,s));
		}

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.target()!=null)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
		&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Area)
		   ||((affected instanceof Item)&&(msg.source().isMine(affected)))
		   ||((affected instanceof MOB)&&(msg.source()==affected))))
		{
			final Vector<String> poss=new Vector<String>();
			final int randomResource=CMLib.dice().roll(1,RawMaterial.CODES.TOTAL()-1,0);
			if(text().length()==0)
			{
				final Item I=CMLib.materials().makeItemResource(randomResource);
				msg.target().setName(I.Name());
				msg.target().setDisplayText(I.displayText());
				if(msg.target() instanceof Item)
					((Item)msg.target()).setMaterial(I.material());
			}
			else
			for(int v=0;v<changes.size();v++)
			{
				if(changes.get(v).first.equals("*")
				||(changes.get(v).first.equalsIgnoreCase(msg.tool().ID())))
				{
					final String two=changes.get(v).second;
					if(two.equals("*")
					||(CMLib.english().containsString(msg.target().name(),two)))
						poss.addElement(changes.get(v).third);
				}
			}
			if(poss.size()==0)
				return true;
			final String var=poss.elementAt(CMLib.dice().roll(1,poss.size(),-1));
			final String newname=CMParms.getParmStr(var,"NAME","");
			final String newdisp=CMParms.getParmStr(var,"DISPLAY","");
			final String newmat=CMParms.getParmStr(var,"MATERIAL","");

			if(newname.length()>0)
			{
				if(newname.equals("*"))
				{
					final Item I=CMLib.materials().makeItemResource(randomResource);
					msg.target().setName(I.Name());
				}
				else
					msg.target().setName(newname);
			}
			if(newdisp.length()>0)
			{
				if(newdisp.equals("*"))
				{
					final Item I=CMLib.materials().makeItemResource(randomResource);
					msg.target().setDisplayText(I.displayText());
				}
				else
					msg.target().setDisplayText(newdisp);
			}
			if((newmat.length()>0)&&(msg.target() instanceof Item))
			{
				final String oldMatName=RawMaterial.CODES.NAME(((Item)msg.target()).material()).toLowerCase();
				int newMatCode=-1;
				if(newmat.equals("*"))
					newMatCode=randomResource;
				else
				{
					newMatCode=CMLib.materials().getResourceCode(newmat,false);
					if(newMatCode<0)
					{
						newMatCode=CMLib.materials().getMaterialCode(newmat,false);
						if(newMatCode>0)
							newMatCode=CMLib.materials().getRandomResourceOfMaterial(newMatCode);
					}
					if(newMatCode>=0)
					{
						((Item)msg.target()).setMaterial(newMatCode);
						final String newMatName=RawMaterial.CODES.NAME(newMatCode).toLowerCase();
						msg.target().setName(CMStrings.replaceAll(msg.target().name(),oldMatName,newMatName));
						msg.target().setDisplayText(CMStrings.replaceAll(msg.target().name(),oldMatName,newMatName));
						msg.target().setName(CMStrings.replaceAll(msg.target().name(),CMStrings.capitalizeAndLower(oldMatName),CMStrings.capitalizeAndLower(newMatName)));
						msg.target().setDisplayText(CMStrings.replaceAll(msg.target().name(),CMStrings.capitalizeAndLower(oldMatName),CMStrings.capitalizeAndLower(newMatName)));
						msg.target().setName(CMStrings.replaceAll(msg.target().name(),oldMatName.toUpperCase(),newMatName.toUpperCase()));
						msg.target().setDisplayText(CMStrings.replaceAll(msg.target().name(),oldMatName.toUpperCase(),newMatName.toUpperCase()));
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
