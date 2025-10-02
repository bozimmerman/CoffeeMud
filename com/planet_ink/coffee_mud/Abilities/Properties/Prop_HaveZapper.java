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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMaskEntry;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class Prop_HaveZapper extends Property implements TriggeredAffect, Deity.DeityWorshipper
{
	@Override
	public String ID()
	{
		return "Prop_HaveZapper";
	}

	@Override
	public String name()
	{
		return "Restrictions to ownership";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected final static String[] lawAmbi=new String[] {"#LAW"};
	protected final static String[] chaosAmbi=new String[] {"#CHAOS"};

	protected boolean	actual		= false;
	protected boolean	contents	= false;
	protected int		percent		= 100;
	protected String	msgStr		= "";
	protected String	deityName	= "";
	protected int		bonus		= 0;
	protected String[]	bonusAmbi	= null;
	protected String[]	event		= new String[0];

	protected MaskingLibrary.CompiledZMask mask=null;
	protected String maskStr = "";

	@Override
	public String getWorshipCharID()
	{
		if(mask==null)
			return "";
		return deityName;
	}

	@Override
	public void setWorshipCharID(final String newVal)
	{
	}

	@Override
	public void setDeityName(final String newDeityName)
	{
	}

	@Override
	public String deityName()
	{
		return getWorshipCharID();
	}

	@Override
	public Deity getMyDeity()
	{
		if (getWorshipCharID().length() == 0)
			return null;
		return CMLib.map().getDeity(getWorshipCharID());
	}

	protected String defaultMessage()
	{
		return "<O-NAME> flashes and flies out of <S-HIS-HER> hands!";
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ZAPPER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_GET;
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		actual=false;
		contents=false;
		final String txtUpp=text.toUpperCase()+" ";
		if(txtUpp.startsWith("CONTENT ACTUAL ") || txtUpp.startsWith("ACTUAL CONTENT "))
		{
			actual=true;
			contents=true;
			text=text.substring(15).trim();
		}
		else
		if(txtUpp.startsWith("ACTUAL "))
		{
			actual=true;
			text=text.substring(7);
		}
		else
		if(txtUpp.startsWith("CONTENT "))
		{
			contents=true;
			text=text.substring(8);
		}
		percent=100;
		int x=text.indexOf('%');
		if(x>0)
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(text.charAt(x)))
					tot+=CMath.s_int(""+text.charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			percent=tot;
		}
		deityName="";
		msgStr=CMParms.getParmStr(text,"MESSAGE",defaultMessage());
		event=new String[0];
		final String eventStr=CMParms.getParmStr(text,"EVENT","");
		if(eventStr.trim().length()>0)
		{
			event = CMParms.parseCommas(eventStr, true).toArray(event);
			if(event.length>0)
				event[0] = event[0].toUpperCase().trim();
		}
		mask=null;
		bonus=0;
		bonusAmbi=null;
		maskStr = "";
		if(msgStr.trim().length()>0)
		{
			maskStr = CMLib.masking().separateZapperMask(text);
			mask=CMLib.masking().getPreCompiledMask(maskStr);
		}
		if(mask != null)
		{
			MaskingLibrary.ZapperKey key=MaskingLibrary.ZapperKey._DEITY;
			for(final CompiledZMaskEntry[] entries : mask.entries())
			{
				for(final CompiledZMaskEntry entry : entries)
				{
					switch(entry.maskType())
					{
					case _OR:
						key=(key==MaskingLibrary.ZapperKey._DEITY)?MaskingLibrary.ZapperKey.DEITY:MaskingLibrary.ZapperKey._DEITY;
						break;
					case _DEITY:
					case DEITY:
						if(entry.maskType()==key)
						{
							for(final Object o : entry.parms())
							{
								if((o instanceof String)
								&&(!"ANY".equalsIgnoreCase((String)o)))
									deityName=(String)o;
							}
						}
						break;
					case FACTION:
					case _FACTION:
						if((entry.maskType()==MaskingLibrary.ZapperKey._FACTION)
						||((entry.maskType()==MaskingLibrary.ZapperKey.FACTION)
							&&(key==MaskingLibrary.ZapperKey.DEITY)))
						{
							if(CMParms.contains(entry.parms(),"EVIL"))
								bonus=PhyStats.IS_EVIL;
							else
							if(CMParms.contains(entry.parms(),"GOOD"))
								bonus=PhyStats.IS_GOOD;
							if(CMParms.contains(entry.parms(),"LAW"))
								bonusAmbi=lawAmbi;
							else
							if(CMParms.contains(entry.parms(),"CHAOS"))
								bonusAmbi=chaosAmbi;
						}
						else
						if((entry.maskType()==MaskingLibrary.ZapperKey.FACTION)
						||((entry.maskType()==MaskingLibrary.ZapperKey._FACTION)
							&&(key==MaskingLibrary.ZapperKey.DEITY)))
						{
							if(CMParms.contains(entry.parms(),"EVIL"))
								bonus=PhyStats.IS_GOOD;
							else
							if(CMParms.contains(entry.parms(),"GOOD"))
								bonus=PhyStats.IS_EVIL;
							if(CMParms.contains(entry.parms(),"LAW"))
								bonusAmbi=chaosAmbi;
							else
							if(CMParms.contains(entry.parms(),"CHAOS"))
								bonusAmbi=lawAmbi;
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}

	@Override
	public String accountForYourself()
	{
		return "Ownership restricted as follows: "+CMLib.masking().maskDesc(maskStr);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(bonus != 0)
			affectableStats.setDisposition(affectableStats.disposition()|bonus);
		if(bonusAmbi != null)
		{
			for(final String str : bonusAmbi)
				affectableStats.addAmbiance(str);
		}
	}

	protected boolean executeEvent(final CMMsg msg)
	{
		if((this.event==null)||(this.event.length==0)||("ZAP".startsWith(event[0])))
			return false; // normal zap (cancel message)
		if("ACCUSE".startsWith(event[0]))
		{
			final LegalBehavior B =CMLib.law().getLegalBehavior(msg.source().location());
			final Area A = CMLib.law().getLegalObject(msg.source().location());
			if((A!=null)&&(B!=null))
			{
				B.accuse(A, msg.source(), null, Arrays.copyOfRange(event, 1, event.length));
				return true;
			}
		}
		return false; // normal zap (cancel message)
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return false;

		final MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(affected)
		||((msg.tool()==affected)&&(msg.target() instanceof Container)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_HOLD:
				break;
			case CMMsg.TYP_WEAR:
				break;
			case CMMsg.TYP_WIELD:
				break;
			case CMMsg.TYP_GET:
				if((!CMLib.masking().maskCheck(mask,mob,actual))
				&&(CMLib.dice().rollPercentage()<=percent)
				&&((!(affected instanceof Container))||(!(msg.tool() instanceof Item)))
				)
				{
					mob.location().show(mob,null,affected,CMMsg.MSG_OK_ACTION,msgStr);
					return executeEvent(msg);
				}
				break;
			case CMMsg.TYP_EAT:
			case CMMsg.TYP_DRINK:
				if((!CMLib.masking().maskCheck(mask,mob,actual))
				&&(CMLib.dice().rollPercentage()<=percent))
				{
					mob.location().show(mob,null,affected,CMMsg.MSG_OK_ACTION,msgStr);
					return executeEvent(msg);
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public String getStat(final String code)
	{
		if(code == null)
			return "";
		if(code.equalsIgnoreCase("STAT-LEVEL"))
		{
			int level = 0;
			if((mask != null)
			&&(!mask.empty())
			&&(mask.entries()!=null)
			&&(mask.entries().length>0))
			{
				for(final CompiledZMaskEntry[] entries : this.mask.entries())
				{
					for(final CompiledZMaskEntry entry : entries)
					{
						int lvlAdj = 0;
						switch(entry.maskType())
						{
						case _PLAYER:
						case _NPC:
							lvlAdj -=5;
							break;
						case _ALIGNMENT:
							lvlAdj -= (9-entry.parms().length);
							break;
						case ALIGNMENT:
							lvlAdj -= entry.parms().length;
							break;
						case _RACECAT:
						case _RACE:
							lvlAdj -=9;
							break;
						case RACECAT:
						case RACE:
							lvlAdj -= entry.parms().length;
							break;
						case _BASECLASS:
							lvlAdj -= (9-entry.parms().length);
							break;
						case BASECLASS:
							lvlAdj -= entry.parms().length;
							break;
						case _ANYCLASS:
						case _ANYCLASSLEVEL:
						case _CLASS:
						case _CLANLEVEL:
							lvlAdj -= 9;
							break;
						case ANYCLASS:
						case ANYCLASSLEVEL:
						case CLANLEVEL:
						case CLASS:
							lvlAdj -= entry.parms().length;
							break;
						case _GENDER:
							lvlAdj -= (9-(entry.parms().length*3));
							break;
						case GENDER:
							lvlAdj -= (entry.parms().length*3);
							break;
						case TATTOO:
						case _TATTOO:
						case TAG:
						case _TAG:
						case _FACTION:
						case FACTION:
							lvlAdj -= 9;
							break;
						default:
							break;
						}
						if(lvlAdj<-9)
							lvlAdj=-9;
						level += lvlAdj;
					}
				}
			}
			if(level > 0)
				level = -1;
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
			else
			if(code.equalsIgnoreCase("TONEUP"))
			{
				setStat("TONEUP-MISC",val);
			}
			else
			if((code.equalsIgnoreCase("TONEUP-ARMOR"))
			||(code.equalsIgnoreCase("TONEUP-WEAPON"))
			||(code.equalsIgnoreCase("TONEUP-MISC")))
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
