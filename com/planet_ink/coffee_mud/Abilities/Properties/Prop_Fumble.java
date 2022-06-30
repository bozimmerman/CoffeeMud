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
import com.planet_ink.coffee_mud.Libraries.interfaces.CMMiscUtils.ItemState;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class Prop_Fumble extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Fumble";
	}

	@Override
	public String name()
	{
		return "Causing skill failures";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_AREAS|Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself()
	{
		return "Difficult to use.";
	}

	@Override
	public long flags()
	{
		return 0;
	}

	protected final static String	DEF_MSG_STR="<S-NAME> fail(s) <S-HIS-HER> @x1 on <T-NAME> due to @x2";

	protected int			chance		= 0;
	protected ItemState		itemState	= ItemState.WORN;
	protected CompiledZMask	mobMask		= null;
	protected int[]			domains		= new int[0];
	protected int[]			ableTypes	= new int[0];
	protected long			flags		= 0;
	protected String		msgStr		= DEF_MSG_STR;
	protected boolean		privateM	= true;

	protected final Set<String>		ids			= new HashSet<String>();

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		final String chance=CMParms.getParmStr(newMiscText, "CHANCE", "100%");
		this.chance = (int)Math.round(CMath.s_pct(chance) * 100.0);
		final String item = CMParms.getParmStr(newMiscText, "ITEM", "WORN").toUpperCase();
		itemState = ItemState.WORN;
		if(item.equals("WORN")||item.equals("WEAR"))
			itemState = ItemState.WORN;
		else
		if(item.equals("HAVE")||item.equals("HAS")||item.startsWith("INV"))
			itemState = ItemState.HAVE_ANY;
		else
		if(item.equals("HERE")||item.equals("ROOM"))
			itemState = ItemState.ROOM_ANY;
		else
		if(item.equals("PRESENT"))
			itemState = ItemState.PRESENT_ANY;
		else
		{
			Log.errOut(ID()+" on "+((affected==null)?"null":affected.Name())+" @"+
						CMLib.map().roomLocation(affected)+" has bad item state: "+item);
		}
		final String mask=CMParms.getParmStr(newMiscText, "MASK", "");
		mobMask = null;
		if(mask.length()>0)
			mobMask = CMLib.masking().getPreCompiledMask(mask);

		this.msgStr=CMParms.getParmStr(newMiscText, "MSG", DEF_MSG_STR);
		this.privateM=CMParms.getParmBool(newMiscText, "PRIVATE", true);

		final String idStr=CMParms.getParmStr(newMiscText, "AID", "").toUpperCase();
		this.ids.clear();
		this.ids.addAll(CMParms.parseCommas(idStr, true));

		final String typStr = CMParms.getParmStr(newMiscText, "ATYPE", "ALL").toUpperCase();
		this.ableTypes = new int[0];
		if(typStr.length()>0)
		{
			for(final String s : CMParms.parseCommas(typStr, true))
			{
				if(s.equals("ALL"))
				{
					this.ableTypes = new int[0];
					break;
				}
				int x=CMParms.indexOf(Ability.ACODE_DESCS_, s);
				if(x<0)
					x=CMParms.indexOf(Ability.ACODE_DESCS, s);
				if(x<0)
					Log.errOut(ID()+" on "+((affected==null)?"null":affected.Name())+" @"+
							CMLib.map().roomLocation(affected)+" has bad atype: "+s);
				else
				{
					this.ableTypes = Arrays.copyOf(this.ableTypes, this.ableTypes.length+1);
					this.ableTypes[this.ableTypes.length-1]=x;
				}
			}
		}

		final String domStr = CMParms.getParmStr(newMiscText, "ADOMAIN", "").toUpperCase();
		this.domains = new int[0];
		if(typStr.length()>0)
		{
			for(final String s : CMParms.parseCommas(domStr, true))
			{
				int x=CMParms.indexOf(Ability.DOMAIN_DESCS, s);
				if(x<0)
					x=CMParms.indexOfIgnoreCase(Ability.DOMAIN_VERBS, s);
				if(x<0)
					Log.errOut(ID()+" on "+((affected==null)?"null":affected.Name())+" @"+
							CMLib.map().roomLocation(affected)+" has bad domain: "+s);
				else
				{
					this.domains = Arrays.copyOf(this.domains, this.domains.length+1);
					this.domains[this.domains.length-1]=x<<5;
				}
			}
		}

		final String flagStr = CMParms.getParmStr(newMiscText, "AFLAG", "").toUpperCase();
		this.flags = 0;
		if(typStr.length()>0)
		{
			for(final String s : CMParms.parseCommas(flagStr, true))
			{
				final int x=CMParms.indexOf(Ability.FLAG_DESCS, s);
				if(x<0)
					Log.errOut(ID()+" on "+((affected==null)?"null":affected.Name())+" @"+
							CMLib.map().roomLocation(affected)+" has bad flag: "+s);
				else
					this.flags |= Math.round(Math.pow(2, x));
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool() instanceof Ability)
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&&(CMLib.dice().rollPercentage()<=this.chance)
		&&(msg.source().isMine(msg.tool())))
		{
			if((affected instanceof Item)
			&&(!CMLib.utensils().isItemInState(msg.source().location(), msg.source(), itemState, (Item)affected)))
				return super.okMessage(myHost,msg);
			if((mobMask != null)
			&&(!CMLib.masking().maskCheck(mobMask, msg.source(), true)))
				return super.okMessage(myHost,msg);
			final Ability A=(Ability)msg.tool();
			if((ableTypes.length>0)
			&&(!CMParms.contains(ableTypes, A.classificationCode()&Ability.ALL_ACODES)))
				return super.okMessage(myHost,msg);

			if(((domains.length>0)&&CMParms.contains(ableTypes, A.classificationCode()&Ability.ALL_DOMAINS))
			||(ids.contains(A.ID().toUpperCase()))
			||(ids.contains(A.name().toUpperCase()))
			||((flags!=0)&&((flags&A.flags())>0)))
			{
				if(privateM)
					msg.source().tell(msg.source(),msg.target(),null,L(msgStr,A.name(),affected.name(msg.source())));
				else
					msg.source().location().show(msg.source(), msg.target(), null, CMMsg.MSG_OK_ACTION,
							L(msgStr,A.name(),affected.name(msg.source())));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
