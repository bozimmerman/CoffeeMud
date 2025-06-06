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
   Copyright 2005-2025 Bo Zimmerman

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
public class Prop_Smell extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Smell";
	}

	@Override
	public String name()
	{
		return "A Smell";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;
	}

	protected TriadList<String,Integer,Integer> smells=null;
	protected final static int FLAG_EMOTE=512;
	protected final static int FLAG_BROADCAST=1024;
	protected boolean lastWasBroadcast=false;

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public void setMiscText(final String newStr)
	{
		if(newStr.startsWith("+"))
		{
			if(text().indexOf(newStr.substring(1).trim())>=0)
				return;
			super.setMiscText(text()+";"+newStr.substring(1).trim());
			smells=null;
		}
		else
		if(newStr.startsWith("-"))
		{
			final int x=text().indexOf(newStr.substring(1).trim());
			if(x>=0)
			{
				final int len=newStr.substring(1).trim().length();
				super.setMiscText(text().substring(0,x)+text().substring(x+len));
				smells=null;
			}
			else
				return;
		}
		else
		{
			super.setMiscText(newStr);
			smells=null;
		}
	}

	public TriadList<String,Integer,Integer> getSmells()
	{
		if(smells!=null)
			return smells;
		final List<String> allsmells=CMParms.parseSemicolons(text(),true);
		smells=new TriadVector<String,Integer,Integer>(3);
		for(int i=0;i<allsmells.size();i++)
		{
			final String smell=allsmells.get(i);
			if(smell.length()>0)
			{
				int pct=100;
				int ticks=-1;
				final List<String> parsedSmell=CMParms.parse(smell);
				for(int ii=parsedSmell.size()-1;ii>=0;ii--)
				{
					final String s=parsedSmell.get(ii).toUpperCase();
					if(s.startsWith("TICKS="))
					{
						ticks=CMath.s_int(s.substring(6).trim());
						parsedSmell.remove(ii);
					}
					if(s.startsWith("CHANCE="))
					{
						pct=(pct&(FLAG_BROADCAST+FLAG_EMOTE))+CMath.s_int(s.substring(5).trim());
						parsedSmell.remove(ii);
					}
					if(s.equals("EMOTE"))
					{
						pct=pct|FLAG_EMOTE;
						parsedSmell.remove(ii);
					}
					if(s.equals("BROADCAST"))
					{
						pct=pct|FLAG_BROADCAST;
						parsedSmell.remove(ii);
					}
				}
				final String finalSmell=CMParms.combine(parsedSmell,0).trim();
				if(finalSmell.length()>0)
					smells.add(finalSmell,Integer.valueOf(pct),Integer.valueOf(ticks));
			}
		}
		return smells;
	}

	public String selectSmell(final boolean emoteOnly)
	{
		lastWasBroadcast=false;
		getSmells();
		if((smells!=null)&&(smells.size()>0))
		{
			int total=0;
			for(int i=0;i<smells.size();i++)
			{
				final int pct=smells.get(i).second.intValue();
				if((!emoteOnly)||(CMath.bset(pct,FLAG_EMOTE)))
					total+=pct&511;
			}
			if(total==0)
				return "";
			int draw=CMLib.dice().roll(1,total,0);
			for(int i=0;i<smells.size();i++)
			{
				final int pct=smells.get(i).second.intValue();
				if((!emoteOnly)||(CMath.bset(pct,FLAG_EMOTE)))
				{
					draw-=pct&511;
					if(draw<=0)
					{
						lastWasBroadcast=CMath.bset(pct,FLAG_BROADCAST);
						return smells.get(i).first;
					}
				}
			}
		}
		return "";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,selectSmell(false));
	}

	public void emoteHere(final Room room, final MOB emoter, final String str)
	{
		final CMMsg msg=CMClass.getMsg(emoter,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_AROMA,str);
		if(room.okMessage(emoter,msg))
		{
			if(CMLib.flags().canSmell(emoter))
				room.send(emoter, msg);
			else
				room.sendOthers(emoter, msg);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(CMLib.dice().rollPercentage()<=20))
		{
			final String emote=selectSmell(true);
			if((emote!=null)&&(emote.length()>0))
			{
				final Room room=CMLib.map().roomLocation(affected);
				if(room!=null)
				{
					emoteHere(room,(MOB)affected,emote);
					if(lastWasBroadcast)
					{
						final MOB emoter=CMClass.getFactoryMOB();
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							final Room R=room.getRoomInDir(d);
							final Exit E=room.getExitInDir(d);
							if((R!=null)&&(E!=null)&&(E.isOpen()))
							{
								emoter.setLocation(R);
								final int opDir=R.getReverseDir(d);
								final String inDir=CMLib.directions().getInDirectionName(opDir, CMLib.flags().getDirType(R));
								emoter.setName(L("something @x1",inDir));
								emoteHere(R,emoter,emote);
							}
						}
						emoter.destroy();
					}
				}
			}
			final TriadList<String,Integer,Integer> sm=getSmells();
			boolean redo=false;
			for(int i=sm.size()-1;i>=0;i--)
			{
				if(sm.get(i).third.intValue()>0)
				{
					final Integer I=Integer.valueOf(smells.get(i).third.intValue()-1);
					if(I.intValue()>0)
					{
						final String smell=sm.get(i).first;
						final Integer pct=sm.get(i).second;
						sm.add(smell,pct,I);
					}
					sm.remove(i);
					if(I.intValue()<=0)
						redo=true;
				}
			}
			if(redo)
			{
				final StringBuffer newText=new StringBuffer("");
				for(int i=0;i<sm.size();i++)
				{
					final String smell=sm.get(i).first;
					final Integer pct=sm.get(i).second;
					final Integer ticks=sm.get(i).third;
					if(ticks.intValue()>0)
						newText.append("TICKS="+ticks+" ");
					if(CMath.bset(pct.intValue(),FLAG_EMOTE))
						newText.append("EMOTE ");
					if(CMath.bset(pct.intValue(),FLAG_BROADCAST))
						newText.append("BROADCAST ");
					if((pct.intValue()&511)!=100)
						newText.append("CHANCE="+(pct.intValue()&511)+" ");
					newText.append(smell+";");
				}
				if(newText.length()==0)
					affected.delEffect(this);
				else
					setMiscText(newText.toString());
			}
		}
		return super.tick(ticking,tickID);
	}
}

