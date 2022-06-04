package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2022 Bo Zimmerman

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
public class ResourceOverride extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "ResourceOverride";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS | Behavior.CAN_AREAS | CAN_MOBS;
	}

	private final List<String>	aids		= new Vector<String>();
	private final List<Integer>	rscs		= new Vector<Integer>();
	private final List<Item>	crscs		= new Vector<Item>();
	private final Set<Integer>	roomTypes	= new TreeSet<Integer>();
	private CompiledZMask		mobMask		= null;

	private volatile Pair<MOB,Ability> corpseWatcher = null;

	@Override
	public String accountForYourself()
	{
		return "resource overriding";
	}

	protected static Integer getRscTypeIfAny(final String which)
	{
		int code=-1;
		if(which.equalsIgnoreCase("none"))
			return Integer.valueOf(0);
		if(CMath.isInteger(which))
			code=CMath.s_int(which);
		if(code<0)
			code = RawMaterial.CODES.FIND_IgnoreCase(which);
		if(code<0)
		{
			final RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(which);
			if(m!=null)
				code=RawMaterial.CODES.COMPOSE_RESOURCES(m.mask()).get(0).intValue();
		}
		if(code<0)
			code = RawMaterial.CODES.FIND_StartsWith(which);
		if(code<0)
		{
			final RawMaterial.Material m=RawMaterial.Material.startsWith(which);
			if(m!=null)
				code=RawMaterial.CODES.COMPOSE_RESOURCES(m.mask()).get(0).intValue();
		}
		if((code<0)&&(which.endsWith("S")||which.endsWith("s"))&&(which.length()>1))
		{
			final Integer I=getRscTypeIfAny(which.substring(0,which.length()-1));
			if(I!=null)
				code=I.intValue();
			
		}
		if(code < 0)
			return null;
		return Integer.valueOf(code);
	}

	@Override
	public void setParms(final String newStr)
	{
		super.setParms(newStr);
		super.tickDown=1;
		rscs.clear();
		crscs.clear();
		roomTypes.clear();
		this.mobMask = null;
		final Vector<String> V=CMParms.parse(getParms());
		if(V.size()==0)
			return;
		for(int v=0;v<V.size();v++)
		{
			// first try for a real one
			final String which=V.elementAt(v).toUpperCase().trim();
			if(which.startsWith("MOBMASK"))
			{
				final String w=which.substring(7).trim();
				if(w.startsWith("="))
				{
					final String mask=(w.substring(1) + " " + CMParms.combineQuoted(V,v+1)).trim();
					this.mobMask = CMLib.masking().getPreCompiledMask(mask);
					break;
				}
			}
			if(which.startsWith("MIN")
			||which.startsWith("MAX")
			||which.startsWith("CHANCE"))
				continue;
			final Integer c = getRscTypeIfAny(which);
			if(c!=null)
			{
				if(!rscs.contains(c))
					rscs.add(c);
			}
			else
			{
				int code=-1;
				for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
				{
					if(which.equalsIgnoreCase(Room.DOMAIN_OUTDOOR_DESCS[i]))
					{
						code = i;
						break;
					}
				}
				if(code<0)
				{
					for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
					{
						if(which.equalsIgnoreCase(Room.DOMAIN_INDOORS_DESCS[i]))
						{
							code = Room.INDOORS | i;
							break;
						}
					}
				}
				if(code>=0)
					roomTypes.add(Integer.valueOf(code));
				else
				{
					String uwhich = which;
					int p=uwhich.indexOf('(');
					String rscName = null;
					if((p>0)&&(uwhich.endsWith(")")))
					{
						final String lwhich = V.elementAt(v).trim();
						rscName = lwhich.substring(0,p);
						uwhich = uwhich.substring(p+1,uwhich.length()-1).trim();
					}
					p=uwhich.indexOf(' ');
					Integer rscType = null;
					String subType = null;
					if(p>0)
					{
						rscType = ResourceOverride.getRscTypeIfAny(uwhich.substring(0,p).trim());
						if(rscType != null)
							subType = uwhich.substring(p+1).trim();
					}
					else
						rscType = ResourceOverride.getRscTypeIfAny(uwhich);
					if(rscType != null)
					{
						final Item rscPrototype = CMLib.materials().makeItemResource(rscType.intValue(), (subType == null)?"":subType );
						if(rscName != null)
						{
							rscPrototype.setName(rscName);
							rscPrototype.setDisplayText(rscName+" is here");
						}
						crscs.add(rscPrototype);
					}
					else
					{
						final Ability A=(p<0)?CMClass.findAbility(which):null;
						if(A!=null)
							this.aids.add(A.ID());
						else
							Log.errOut("Unknown ResourceOverride resource/room code '"+uwhich+"' in '"+newStr+"'");
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if(rscs.size()==0)
			return true;
		if(super.canAct(ticking, tickID))
		{
			switch(tickID)
			{
			case Tickable.TICKID_ROOM_BEHAVIOR:
				if(ticking instanceof Room)
				{
					final Room R=(Room)ticking;
					if(!rscs.contains(Integer.valueOf(R.myResource())))
						R.setResource(rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue());
				}
				break;
			case Tickable.TICKID_AREA:
				if(ticking instanceof Area)
				{
					final Area A=(Area)ticking;
					Room R=null;
					for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
					{
						R=e.nextElement();
						if((R!=null)
						&&((roomTypes.size()==0)||(roomTypes.contains(Integer.valueOf(R.domainType()))))
						&&(!rscs.contains(Integer.valueOf(R.myResource()))))
							R.setResource(rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue());
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.source()==host)
		&&(msg.sourceMinor()==CMMsg.TYP_BODYDROP)
		&&(msg.target() instanceof DeadBody)
		&&(canChance()))
			((DeadBody)msg.target()).addBehavior(this);
		else
		if((host instanceof DeadBody)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
		&&(msg.sourceMinor()!=CMMsg.TYP_ITEMGENERATED)
		&&((aids.size()==0)||((msg.tool() instanceof Ability)&&(aids.contains(msg.tool().ID())))))
		{
			final Pair<MOB, Ability> w;
			synchronized(this)
			{
				w = corpseWatcher;
			}
			if((w!=null)
			&&((w.first==msg.source())||(w.second==msg.tool())))
				corpseWatcher = null;
			if(msg.target()==host)
				corpseWatcher = new Pair<MOB,Ability>(msg.source(), (Ability)msg.tool());
		}
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;

		if((msg.sourceMinor()==CMMsg.TYP_ITEMGENERATED)
		&&(msg.target() instanceof RawMaterial)
		&&((aids.size()==0)||((msg.tool() instanceof Ability)&&(aids.contains(msg.tool().ID())))))
		{
			if(host instanceof DeadBody)
			{
				final Pair<MOB, Ability> corpseWatcher = this.corpseWatcher;
				if(((mobMask == null)||(CMLib.masking().maskCheck(mobMask, msg.source(), true)))
				&&(corpseWatcher!=null)
				&&(msg.source()==corpseWatcher.first)
				&&(msg.tool().ID().equals(corpseWatcher.second.ID())))
				{
					if(CMLib.dice().roll(1, crscs.size()+rscs.size(), 0)<=crscs.size())
						msg.setTarget((Physical)crscs.get(CMLib.dice().roll(1, crscs.size(), -1)).copyOf());
					else
						msg.setTarget(CMLib.materials().makeItemResource(rscs.get(CMLib.dice().roll(1, rscs.size(), -1)).intValue(),""));
					if(msg.target() instanceof RawMaterial)
						((Item)msg.target()).setSecretIdentity("");
				}
			}
			else
			if(((host instanceof Room)||(host instanceof Area))
			&&(crscs.size()>0))
			{
				if((mobMask == null)
				||(CMLib.masking().maskCheck(mobMask, msg.source(), true)))
				{
					if(CMLib.dice().roll(1, crscs.size()+rscs.size(), 0)<=crscs.size())
					{
						msg.setTarget((Physical)crscs.get(CMLib.dice().roll(1, crscs.size(), -1)).copyOf());
						if(msg.target() instanceof RawMaterial)
							((Item)msg.target()).setSecretIdentity("");
					}
				}
			}
		}
		return true;
	}
}
