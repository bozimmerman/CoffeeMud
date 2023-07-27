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
import java.util.concurrent.atomic.AtomicInteger;

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
		return bubble;
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
		super.setMiscText(newMiscText);
		changes.clear();
		previous.clear();
		final int x = newMiscText.indexOf("<PREVIOUS>");
		String parms = newMiscText;
		String xml = "";
		if(x>0)
		{
			parms = newMiscText.substring(0,x);
			xml = newMiscText.substring(x);
		}
		changes.putAll(CMParms.parseEQParms(parms));
		if(changes.containsKey("TRIGGER"))
		{
			trigger = 0;
			final String[] chtrs  = changes.remove("TRIGGER").toUpperCase().trim().split("|");
			for(String chtr : chtrs)
			{
				chtr = chtr.trim();
				final String chtru = chtr+"_";
				final String uchtr = "_"+chtr;
				for(int i=0;i<TRIGGER_DESC.length;i++)
				{
					final String tds = TRIGGER_DESC[i];
					if(tds.equals(chtr) || tds.startsWith(chtru) || tds.endsWith(uchtr))
						trigger|=Math.round(CMath.pow(2,i));
				}
			}
			if(trigger==0)
				trigger = TRIGGER_ALWAYS;
		}
		if(changes.containsKey("IDENTITY"))
			identity = Integer.valueOf(CMath.s_int(changes.get("IDENTITY")));
		bubble=false;
		if(changes.containsKey("BUBBLE"))
			bubble = CMath.s_bool(changes.get("BUBBLE"));
		previous.clear();
		if(xml.length()>0)
		{
			final List<XMLLibrary.XMLTag> tags = CMLib.xml().parseAllXML(xml);
			for(final XMLLibrary.XMLTag tag : tags)
			{
				if((tag.getParmValue("STAT")!=null)
				&&(tag.getParmValue("ID")!=null))
				{
					final String changeStat = tag.getParmValue("STAT");
					final String changeValue = CMLib.xml().restoreAngleBrackets(tag.value());
					final Integer identifier = Integer.valueOf(CMath.s_int(tag.getParmValue("ID")));
					previous.add(new Quad<Integer,Modifiable,String,String>(identifier,null,changeStat,changeValue));
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		if(previous.size()>0)
			undoEffect(null, null);
		super.finalize();
	}

	protected Integer makeIdentity(final Physical affected)
	{
		if(affected == null)
			return null;
		if(affected instanceof MOB)
		{
			final MOB M = (MOB)affected;
			if(M.isPlayer())
				return Integer.valueOf(affected.Name().hashCode());
		}
		if(affected instanceof DBIdentifiable)
		{
			final DBIdentifiable id = (DBIdentifiable)affected;
			if(id.databaseID().length()>0)
				return Integer.valueOf(id.databaseID().hashCode());
		}
		return Integer.valueOf(affected.getClass().getName().hashCode());
	}

	protected Modifiable findModifiable(final Physical affected, final String changeStat)
	{
		Modifiable M = null;
		if(affected.isStat(changeStat))
			M = affected;
		else
		if(affected.basePhyStats().isStat(changeStat))
			M = affected.basePhyStats();
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).baseCharStats().isStat(changeStat)))
			M = ((MOB)affected).baseCharStats();
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).baseState().isStat(changeStat)))
			M = ((MOB)affected).baseState();
		return M;
	}

	protected Modifiable findLostModifiable(final Physical affected, final Integer identity, final String changeStat)
	{
		if(affected != null)
		{
			if(identity.equals(this.makeIdentity(affected)))
				return findModifiable(affected, changeStat);
		}
		if(this.affected != null)
		{
			if(identity.equals(this.makeIdentity(this.affected)))
				return findModifiable(this.affected, changeStat);
		}
		if(affected instanceof Item)
		{
			if(identity.equals(this.makeIdentity(((Item)affected).owner())))
				return findModifiable(((Item)affected).owner(), changeStat);
		}
		if(this.affected instanceof Item)
		{
			if(identity.equals(this.makeIdentity(((Item)affected).owner())))
				return findModifiable(((Item)affected).owner(), changeStat);
		}
		return null;
	}

	protected void undoEffect(final Integer identity, final Physical affected)
	{
		if(previous.size()>0)
		{
			synchronized(previous)
			{
				if(previous.size()>0)
				{
					for(final Iterator<Quad<Integer,Modifiable,String,String>> q = previous.iterator();q.hasNext();)
					{
						final Quad<Integer, Modifiable,String,String> Q = q.next();
						if((identity == null)||(Q.first.equals(identity)))
						{
							if(Q.second==null)
								Q.second = this.findLostModifiable(affected, Q.first, Q.third);
							if(Q.second != null)
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
		}
	}

	protected void addEffect(final Integer identity, final Physical affected)
	{
		if((changes.size()>0)
		&&(affected != null)
		&&(identity != null))
		{
			synchronized(previous)
			{
				for(final String changeStat : changes.keySet())
				{
					boolean found=false;
					final String value = changes.get(changeStat);
					for(final Iterator<Quad<Integer,Modifiable,String,String>> q = previous.iterator();q.hasNext();)
					{
						final Quad<Integer, Modifiable,String,String> Q = q.next();
						if(((identity == null)||(Q.first.equals(identity)))
						&&(Q.third.equals(changeStat)))
							found=true;
					}
					if(!found)
					{
						final Modifiable M = findModifiable(affected, changeStat);
						if(M != null)
						{
							final String oldValue = M.getStat(changeStat);
							previous.add(new Quad<Integer,Modifiable,String,String>(identity,M,changeStat,oldValue));
							M.setStat(changeStat, value);
							affected.recoverPhyStats();
							if(affected instanceof MOB)
							{
								((MOB)affected).recoverMaxState();
								((MOB)affected).recoverCharStats();
							}
						}
					}
				}
			}
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
		Boolean	reeval = null;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_REMOVE:
			if(!CMath.bset(trigger, TRIGGER_WEAR_WIELD)&&(msg.target()==affected))
				reeval = Boolean.FALSE;
			break;
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_HOLD:
			if(CMath.bset(trigger, TRIGGER_WEAR_WIELD)&&(msg.target()==affected))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_DROP:
			if(CMath.banyset(trigger, TRIGGER_WEAR_WIELD|TRIGGER_GET)&&(msg.target()==affected))
				reeval = Boolean.FALSE;
			else
			if(CMath.banyset(trigger, TRIGGER_DROP_PUTIN)&&(msg.target()==affected))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_GET:
			if(CMath.banyset(trigger, TRIGGER_GET)&&(msg.target()==affected))
				reeval = Boolean.TRUE;
			else
			if(CMath.banyset(trigger, TRIGGER_DROP_PUTIN|TRIGGER_PUT)&&(msg.target()==affected))
				reeval = Boolean.FALSE;
			break;
		case CMMsg.TYP_PUT:
			if(CMath.banyset(trigger, TRIGGER_PUT)
			&&((msg.target()==affected)||(msg.target()==affected)))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_MOUNT:
			if((CMath.bset(trigger, TRIGGER_MOUNT)&&(msg.target()==affected)))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_DISMOUNT:
			if((CMath.bset(trigger, TRIGGER_MOUNT)&&(msg.target()==affected)))
				reeval = Boolean.FALSE;
			break;
		case CMMsg.TYP_ENTER:
			if(CMath.bset(trigger, TRIGGER_ENTER))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_LEAVE:
			if(CMath.bset(trigger, TRIGGER_ENTER))
				reeval = Boolean.FALSE;
			break;
		case CMMsg.TYP_DAMAGE:
			if( CMath.bset(trigger, TRIGGER_BEING_HIT)&&(msg.target()==affected))
				reeval = Boolean.TRUE;
			else
			if( CMath.bset(trigger, TRIGGER_HITTING_WITH)&&(msg.tool()==affected))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_FILL:
		case CMMsg.TYP_DRINK:
		case CMMsg.TYP_EAT:
			if((CMath.bset(trigger, TRIGGER_USE)&&(msg.target()==affected)))
				reeval = Boolean.TRUE;
			break;
		case CMMsg.TYP_POUR:
			if((CMath.bset(trigger, TRIGGER_USE)&&(msg.tool()==affected)))
				reeval = Boolean.TRUE;
			break;
		default:
			break;
		}
		if(reeval != null)
		{
			final Modifiable mod = (bubble) ? msg.source() : affected;
			msg.addTrailerRunnable(null);
		}
	}

}
