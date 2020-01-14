package com.planet_ink.coffee_mud.Items.Basic;

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
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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
public class GenPiano extends GenRideable implements MusicalInstrument
{
	@Override
	public String ID()
	{
		return "GenPiano";
	}

	private InstrumentType type = InstrumentType.PIANOS;

	public GenPiano()
	{
		super();
		setName("a generic piano");
		setDisplayText("a generic piano sits here.");
		setDescription("");
		baseGoldValue = 1015;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		basePhyStats().setWeight(2000);
		rideBasis = Rideable.RIDEABLE_SIT;
		riderCapacity = 2;
		setMaterial(RawMaterial.RESOURCE_OAK);
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, false);
		super.recoverPhyStats();
	}

	@Override
	public InstrumentType getInstrumentType()
	{
		return type;
	}

	@Override
	public String getInstrumentTypeName()
	{
		return type.name();
	}

	@Override
	public void setReadableText(final String text)
	{
		super.setReadableText(text);
		if(CMath.isInteger(text))
			setInstrumentType(CMath.s_int(text));
	}

	@Override
	public void setInstrumentType(final int typeOrdinal)
	{
		if(typeOrdinal < InstrumentType.values().length)
			type = InstrumentType.values()[typeOrdinal];
	}

	@Override
	public void setInstrumentType(final InstrumentType newType)
	{
		if(newType != null)
			type = newType;
	}

	@Override
	public void setInstrumentType(final String newType)
	{
		if(newType != null)
		{
			final InstrumentType typeEnum = (InstrumentType)CMath.s_valueOf(InstrumentType.class, newType.toUpperCase().trim());
			if(typeEnum != null)
				type = typeEnum;
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((affected instanceof Rider)
		&&(((Rider)affected).riding()==this))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_UNHELPFUL);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(this.numRiders()>0))
		{
			final Rider R=this.fetchRider(0);
			if((R instanceof MOB)
			&&((((MOB)R).numFollowers()>0)||(((MOB)R).numFollowers()>0)))
			{
				final MOB riderM=(MOB)R;
				if(msg.amISource(riderM))
				{
					if((msg.target() instanceof MOB)
					&&(CMLib.flags().matchedAffects(riderM, (Physical)msg.target(),-1,-1,-1).size()>0))
					{
						final MOB target=(MOB)msg.target();
						if(target.getVictim()==msg.source())
							target.setVictim(null);
						if(msg.source().getVictim()==target)
							msg.source().setVictim(null);
						msg.source().tell(L("Not while you are playing!"));
						return false;
					}
				}
				else
				if(msg.amITarget(riderM)
				&&(CMLib.flags().matchedAffects(riderM, (Physical)msg.target(),-1,-1,-1).size()>0))
				{
					final Set<MOB> riderG=riderM.getGroupMembers(new HashSet<MOB>());
					riderG.remove(riderM);
					if(riderG.size()>0)
					{
						final MOB target=riderM;
						final int x=CMLib.dice().roll(1, riderG.size(), -1);
						MOB newTarget=null;
						int i=0;
						final Iterator<MOB> r=riderG.iterator();
						while((r.hasNext())&&(i<=x))
						{
							newTarget=r.next();
							i++;
						}
						if(newTarget != null)
						{
							if(target.getVictim()==msg.source())
								target.setVictim(null);
							if(msg.source().getVictim()==target)
								msg.source().setVictim(newTarget);
							msg.setTarget(newTarget);
						}
					}
					return false;
				}
				else
				{
					final Set<MOB> riderG=riderM.getGroupMembers(new HashSet<MOB>());
					if(riderG.contains(msg.target())||riderG.contains(msg.source()))
					{
						if(riderM.getVictim()!=null)
							riderM.makePeace(false);
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	private final static String[] MYCODES={"INSTTYPE"};

	@Override
	public String getStat(final String code)
	{
		if(super.getCodeNum(code)>=0)
			return super.getStat(code);
		switch(getCodeNum(code))
		{
		case 0:
			return this.getInstrumentTypeName();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(super.getCodeNum(code)>=0)
			super.setStat(code, val);
		else
		switch(getCodeNum(code))
		{
		case 0:
		{
			this.setInstrumentType(val);
			break;
		}
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return super.getCodeNum(code);
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenPiano.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(super.getStatCodes());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenPiano))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
