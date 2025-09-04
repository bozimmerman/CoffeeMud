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
   Copyright 2004-2025 Bo Zimmerman

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

	protected TriadList<String, String, String>	changes	= new TriadVector<String, String, String>();
	protected Set<String> skillMask = new HashSet<String>();

	@Override
	public String accountForYourself()
	{
		return "Twists around what the gathering common skills gives you.";
	}

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		changes.clear();
		skillMask.clear();
		final List<String> V=CMParms.parseSemicolons(text,true);
		if(V.size()==0 && text.trim().length()==0)
			skillMask.add("*");
		else
		for(int v=0;v<V.size();v++)
		{
			final String s=V.get(v);
			final String skill=CMParms.getParmStr(s,"SKILL","");
			final String mask=CMParms.getParmStr(s,"MASK","");
			if((skill.length()>0)&&(mask.length()>0))
			{
				skillMask.add(skill.toUpperCase());
				changes.add(skill,mask,s);
			}
		}

	}

	protected boolean commonTwist(final Ability skill, final Map<String,String> itemParms, final boolean overrideId)
	{
		final List<String> poss=new ArrayList<String>();
		final int randomResource=CMLib.dice().roll(1,RawMaterial.CODES.TOTAL()-1,0);
		if(text().length()==0)
		{
			final Item I=CMLib.materials().makeItemResource(randomResource);
			itemParms.put("NAME",I.Name());
			itemParms.put("DISPLAYTEXT",I.displayText());
			itemParms.put("MATERIAL",""+I.material());
			itemParms.put("SECRET","");
			itemParms.put("SUBTYPE","");
		}
		else
		{
			final String name = itemParms.get("NAME");
			for(int v=0;v<changes.size();v++)
			{
				if(changes.get(v).first.equals("*")
				||overrideId
				||(changes.get(v).first.equalsIgnoreCase(skill.ID())))
				{
					final String two=changes.get(v).second;
					if(two.equals("*")
					||(CMLib.english().containsString(name,two)))
						poss.add(changes.get(v).third);
				}
			}
		}
		if(poss.size()==0)
			return true;
		final String var=poss.get(CMLib.dice().roll(1,poss.size(),-1));
		final String newname=CMParms.getParmStr(var,"NAME","");
		final String newdisp=CMParms.getParmStr(var,"DISPLAY","");
		final String newmat=CMParms.getParmStr(var,"MATERIAL","");
		final String newsub=CMParms.getParmStr(var,"SUBTYPE",null);

		if(newname.length()>0)
		{
			if(newname.equals("*"))
			{
				final Item I=CMLib.materials().makeItemResource(randomResource);
				itemParms.put("NAME",I.Name());
			}
			else
				itemParms.put("NAME",newname);
		}
		if(newdisp.length()>0)
		{
			if(newdisp.equals("*"))
			{
				final Item I=CMLib.materials().makeItemResource(randomResource);
				itemParms.put("DISPLAY",I.displayText());
			}
			else
				itemParms.put("DISPLAY",newdisp);
		}
		if(newsub != null)
			itemParms.put("SUBTYPE",newsub);
		if(newmat.length()>0)
		{
			final int oldMatType = CMath.s_int(itemParms.get("MATERIAL"));
			final String oldMatName=RawMaterial.CODES.NAME(oldMatType).toLowerCase();
			int newMatCode=-1;
			if(newmat.equals("*"))
				newMatCode=randomResource;
			else
			{
				newMatCode=CMLib.materials().findResourceCode(newmat,false);
				if(newMatCode<0)
				{
					newMatCode=CMLib.materials().findMaterialCode(newmat,false);
					if(newMatCode>0)
						newMatCode=CMLib.materials().getRandomResourceOfMaterial(newMatCode);
				}
			}
			if(newMatCode>=0)
			{
				itemParms.put("MATERIAL",""+newMatCode);
				final String newMatName=RawMaterial.CODES.NAME(newMatCode).toLowerCase();
				itemParms.put("NAME",CMStrings.replaceAll(itemParms.get("NAME"),oldMatName,newMatName));
				itemParms.put("DISPLAYTEXT",CMStrings.replaceAll(itemParms.get("DISPLAYTEXT"),oldMatName,newMatName));
				itemParms.put("NAME",CMStrings.replaceAll(itemParms.get("NAME"),CMStrings.capitalizeAndLower(oldMatName),CMStrings.capitalizeAndLower(newMatName)));
				itemParms.put("DISPLAYTEXT",CMStrings.replaceAll(itemParms.get("DISPLAYTEXT"),CMStrings.capitalizeAndLower(oldMatName),CMStrings.capitalizeAndLower(newMatName)));
				itemParms.put("NAME",CMStrings.replaceAll(itemParms.get("NAME"),oldMatName.toUpperCase(),newMatName.toUpperCase()));
				itemParms.put("DISPLAYTEXT",CMStrings.replaceAll(itemParms.get("DISPLAYTEXT"),oldMatName.toUpperCase(),newMatName.toUpperCase()));
				itemParms.put("SECRET","");
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		//CMMsg.MSG_HANDS | CMMsg.MASK_SOUND : CMMsg.MSG_NOISYMOVEMENT

		if((affected!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
		&&(skillMask.contains("*")||skillMask.contains(msg.tool().ID().toUpperCase())))
		{
			if((msg.target() instanceof Room)
			&&(msg.tool().ID().equals("Speculate"))
			&&(msg.targetMessage()!=null))
			{
				final int matCode = RawMaterial.CODES.FIND_IgnoreCase(msg.targetMessage());
				final Map<String,String> itemParms = new TreeMap<String,String>();
				itemParms.put("NAME","a pound of "+msg.targetMessage());
				itemParms.put("DISPLAYTEXT","a pound of "+msg.targetMessage()+" is here");
				itemParms.put("MATERIAL",""+matCode);
				itemParms.put("SECRET","");
				itemParms.put("SUBTYPE","");
				if(this.commonTwist((Ability)msg.tool(), itemParms, true))
				{
					if(itemParms.get("SUBTYPE").length()>0)
						msg.setTargetMessage(itemParms.get("SUBTYPE"));
					else
					{
						final int newMat = CMath.s_int(itemParms.get("MATERIAL"));
						if(newMat != matCode)
							msg.setTargetMessage(RawMaterial.CODES.NAME(newMat));
					}
				}
			}
			else
			if((msg.target() instanceof Item)
			&&(((msg.sourceMinor()==CMMsg.TYP_ITEMGENERATED)&&(!((Item)msg.target()).phyStats().isAmbiance("-"+ID())))
				||(((msg.sourceCode()==CMMsg.MSG_NOISYMOVEMENT)||(msg.sourceCode()==(CMMsg.MSG_HANDS | CMMsg.MASK_SOUND)))
					&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_GATHERINGSKILL)))
			&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Area)
			   ||((affected instanceof Item)&&(msg.source().isMine(affected)))
			   ||((affected instanceof MOB)&&(msg.source()==affected))))
			{
				final Item I = (Item)msg.target();
				final Map<String,String> itemParms = new TreeMap<String,String>();
				itemParms.put("NAME",I.Name());
				itemParms.put("DISPLAYTEXT",I.displayText());
				itemParms.put("MATERIAL",""+I.material());
				itemParms.put("SECRET",I.rawSecretIdentity());
				itemParms.put("SUBTYPE",(I instanceof RawMaterial)?((RawMaterial)I).getSubType():"");
				if(this.commonTwist((Ability)msg.tool(), itemParms, false))
				{
					I.setName(itemParms.get("NAME"));
					I.setDisplayText(itemParms.get("DISPLAYTEXT"));
					I.setMaterial(CMath.s_int(itemParms.get("MATERIAL")));
					if(I instanceof RawMaterial)
					{
						I.setSecretIdentity(itemParms.get("SECRET"));
						((RawMaterial)I).setSubType(itemParms.get("SUBTYPE"));
					}
					I.phyStats().addAmbiance("-"+ID());
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
