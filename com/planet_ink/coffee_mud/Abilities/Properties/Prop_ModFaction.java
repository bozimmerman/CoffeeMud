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
   Copyright 2011-2018 Bo Zimmerman

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
public class Prop_ModFaction extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ModFaction";
	}

	@Override
	public String name()
	{
		return "Modifying Faction Gained";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_AREAS | Ability.CAN_ROOMS;
	}

	protected String	operationFormula	= "";
	protected String	factionID			= "";
	protected boolean	reactions			= false;
	protected boolean	gainonly			= false;
	protected boolean	lossonly			= false;

	protected CMath.CompiledFormula	operation			= null;
	protected MaskingLibrary.CompiledZMask			mask				= null;
	
	@Override
	public String accountForYourself()
	{
		final Faction F=(factionID.length()>0)?CMLib.factions().getFaction(factionID):null;
		final String gainOrLoss=(gainonly)?"gained ":lossonly?"lost ":"";
		final String factionName;
		if(factionID.length()==0)
			factionName = "any faction";
		else
		if (reactions)
			factionName = "certain factions";
		else
		if(F==null)
			factionName = "some faction";
		else
			factionName = F.name();
		return "Modifies "+gainOrLoss+"faction with "+factionName+": "+operationFormula;
	}

	public int translateAmount(int amount, String val)
	{
		if(amount<0)
			amount=-amount;
		if(val.endsWith("%"))
			return (int)Math.round(CMath.mul(amount,CMath.div(CMath.s_int(val.substring(0,val.length()-1)),100)));
		return CMath.s_int(val);
	}

	public String translateNumber(String val)
	{
		if(val.endsWith("%"))
			return "@x1 * (" + val.substring(0,val.length()-1) + " / 100)";
		return Integer.toString(CMath.s_int(val));
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		operation = null;
		factionID = "";
		mask=null;
		gainonly=false;
		lossonly=false;
		reactions=false;
		String s=newText.trim();
		int x=s.indexOf(';');
		if(x>=0)
		{
			mask=CMLib.masking().getPreCompiledMask(s.substring(x+1).trim());
			s=s.substring(0,x).trim();
		}
		x=s.indexOf(':');
		if(x>=0)
		{
			factionID=s.substring(0,x).trim();
			if(factionID.startsWith("+"))
			{
				gainonly=true;
				factionID=factionID.substring(1).trim();
			}
			else
			if(factionID.startsWith("-"))
			{
				lossonly=true;
				factionID=factionID.substring(1).trim();
			}
			s=s.substring(x+1).trim();
		}
		if(factionID.trim().equalsIgnoreCase("REACTION"))
		{
			factionID="";
			reactions=true;
		}

		operationFormula="Amount "+s;
		if(s.startsWith("="))
			operation = CMath.compileMathExpression(translateNumber(s.substring(1)).trim());
		else
		if(s.startsWith("+"))
			operation = CMath.compileMathExpression("@x1 + "+translateNumber(s.substring(1)).trim());
		else
		if(s.startsWith("-"))
			operation = CMath.compileMathExpression("@x1 - "+translateNumber(s.substring(1)).trim());
		else
		if(s.startsWith("*"))
			operation = CMath.compileMathExpression("@x1 * "+translateNumber(s.substring(1)).trim());
		else
		if(s.startsWith("/"))
			operation = CMath.compileMathExpression("@x1 / "+translateNumber(s.substring(1)).trim());
		else
		if(s.startsWith("(")&&(s.endsWith(")")))
		{
			operationFormula="Amount ="+s;
			operation = CMath.compileMathExpression(s);
		}
		else
			operation = CMath.compileMathExpression(translateNumber(s.trim()));
		operationFormula=CMStrings.replaceAll(operationFormula, "@x1", "Amount");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_FACTIONCHANGE)
		&&(operation != null)
		&&(((msg.target()==affected)&&(affected instanceof MOB))
		   ||((affected instanceof Item)
			   &&(msg.source()==((Item)affected).owner())
			   &&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY)))
		   ||(affected instanceof Room)
		   ||(affected instanceof Area))
		&&(msg.value()!=Integer.MAX_VALUE)
		&&(msg.value()!=Integer.MIN_VALUE)
		&&((!gainonly)||(msg.value()>0))
		&&((!lossonly)||(msg.value()<0))
		&&((factionID.length()==0)||(msg.othersMessage().equalsIgnoreCase(factionID)))
		)
		{
			if(reactions)
			{
				final Faction F=CMLib.factions().getFaction(msg.othersMessage());
				if((F==null)||(!F.reactions().hasMoreElements()))
					return super.okMessage(myHost,msg);
			}
			if(mask!=null)
			{
				if(affected instanceof Item)
				{
					if((msg.target()==null)||(!(msg.target() instanceof MOB))||(!CMLib.masking().maskCheck(mask,msg.target(),true)))
						return super.okMessage(myHost,msg);
				}
				else
				if(!CMLib.masking().maskCheck(mask,msg.source(),true))
					return super.okMessage(myHost,msg);
			}
			msg.setValue((int)Math.round(CMath.parseMathExpression(operation, new double[]{msg.value()}, 0.0)));
		}
		return super.okMessage(myHost,msg);
	}
}
