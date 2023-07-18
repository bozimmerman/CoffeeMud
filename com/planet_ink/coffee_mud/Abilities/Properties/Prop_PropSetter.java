package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Properties.Prop_HaveAdjuster.ItemSetDef;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.BribeGateGuard;
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
public class Prop_PropSetter extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_PropSetter";
	}

	@Override
	public String name()
	{
		return "Changing properties/attributes";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_AREAS|Ability.CAN_ROOMS|Ability.CAN_MOBS|Ability.CAN_EXITS;
	}

	@Override
	public String accountForYourself()
	{
		return "Different, somehow.";
	}

	@Override
	public long flags()
	{
		return 0;
	}

	protected SLinkedList<Quad<Integer,Modifiable,String,String>> previous =
			new SLinkedList<Quad<Integer,Modifiable,String,String>>();

	protected Map<String,String> changes = new SHashtable<String,String>();
	protected int				 trigger = TRIGGER_ALWAYS;
	protected Integer			 identity=Integer.valueOf(0);
	protected boolean			 bubble	 = false;

	@Override
	public CMObject copyOf()
	{
		final Prop_PropSetter obj=(Prop_PropSetter)super.copyOf();
		obj.changes = new SHashtable<String,String>();
		obj.previous= new SLinkedList<Quad<Integer,Modifiable,String,String>>();
		obj.identity = Integer.valueOf(0);
		return obj;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if((identity==null)||(identity.intValue() == 0))
			identity = Integer.valueOf(P.hashCode());
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		changes.clear();
		previous.clear();
		changes.putAll(CMParms.parseEQParms(newMiscText));
		if(changes.containsKey("TRIGGER"))
		{
			trigger = 0;
			final String chtr  = changes.remove("TRIGGER").toUpperCase().trim();
			final String chtru = chtr+"_";
			final String uchtr = "_"+chtr;
			for(int i=0;i<TRIGGER_DESC.length;i++)
			{
				final String tds = TRIGGER_DESC[i];
				if(tds.equals(chtr) || tds.startsWith(chtru) || tds.endsWith(uchtr))
					trigger|=Math.round(CMath.pow(2,i));
			}
			if(trigger==0)
				trigger = TRIGGER_ALWAYS;
		}
		if(changes.containsKey("IDENTITY"))
			identity = Integer.valueOf(CMath.s_int(changes.get("IDENTITY")));
		bubble=false;
		if(changes.containsKey("BUBBLE"))
			bubble = CMath.s_bool(changes.get("BUBBLE"));
		if(changes.containsKey("SETTEDFLAG") && CMath.s_bool(changes.get("SETTEDFLAG")))
		{
			//TODO: move all the changes to "previous", restore modifiable object
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		if(previous.size()>0)
			undoEffect(null);
		super.finalize();
	}

	protected void undoEffect(final Integer identity)
	{
		if(previous.size()>0)
		{
			for(final Iterator<Quad<Integer,Modifiable,String,String>> q = previous.iterator();q.hasNext();)
			{
				final Quad<Integer, Modifiable,String,String> Q = q.next();
				if((identity == null)||(Q.first.equals(identity)))
				{
					Q.second.setStat(Q.third, Q.fourth);
					q.remove();
				}
			}
			if(identity == null)
				previous.clear();
			if((previous.size()==0)&&(affected != null))
				affected.delEffect(this);
		}
	}

	/**
	 * This method returns a mask of TRIGGER_* constants denoting what triggers the properties
	 * @see TriggeredAffect#TRIGGER_ALWAYS
	 *
	 * @return  a mask of TRIGGER_* constants denoting what triggers the properties
	 */
	public int triggerMask()
	{
		return trigger;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		boolean	 reeval  = false;
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_REMOVE:
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_HOLD:
			reeval = (CMath.bset(trigger, TRIGGER_WEAR_WIELD)&&(msg.target()==affected));
			break;
		case CMMsg.TYP_DROP:
		case CMMsg.TYP_GET:
			reeval = CMath.banyset(trigger, TRIGGER_WEAR_WIELD|TRIGGER_GET|TRIGGER_DROP_PUTIN)&&(msg.target()==affected);
			break;
		case CMMsg.TYP_PUT:
			reeval = (CMath.banyset(trigger, TRIGGER_PUT|TRIGGER_GET)
					&&(msg.target()==affected));
			break;
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_DISMOUNT:
			reeval = (CMath.bset(trigger, TRIGGER_PUT)
					&&(msg.target()==affected));
			break;
		case CMMsg.TYP_ENTER:
		case CMMsg.TYP_LEAVE:
			reeval = CMath.bset(trigger, TRIGGER_ENTER);
			break;
		case CMMsg.TYP_DAMAGE:
			if( CMath.bset(trigger, TRIGGER_BEING_HIT)&&(msg.target()==affected))
				reeval = true;
			else
			if( CMath.bset(trigger, TRIGGER_HITTING_WITH)&&(msg.source()==affected))
				reeval = true;
			break;
		case CMMsg.TYP_FILL:
			reeval = (CMath.bset(trigger, TRIGGER_USE)
					&&(msg.target()==affected));
			break;
		case CMMsg.TYP_DRINK:
			reeval = (CMath.bset(trigger, TRIGGER_USE)
					&&(msg.target()==affected));
			break;
		case CMMsg.TYP_POUR:
			reeval = (CMath.bset(trigger, TRIGGER_USE)
					&&(msg.tool()==affected));
			break;
		case CMMsg.TYP_EAT:
			reeval = (CMath.bset(trigger, TRIGGER_USE)
					&&(msg.target()==affected));
			break;
		}
		if(reeval)
		{
			msg.addTrailerRunnable(null);
		}
	}

}
