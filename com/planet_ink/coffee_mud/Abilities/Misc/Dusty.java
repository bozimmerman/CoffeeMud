package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Dusty extends StdAbility
{
	@Override
	public String ID()
	{
		return "Dusty";
	}

	private final static String localizedName = CMLib.lang().L("Dusty");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Dusty)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS | CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	protected int  dustLevel = 1;
	protected long dustTime = System.currentTimeMillis();
	protected long lastInterval = CMProps.getMillisPerMudHour() * 6 * 20;
	protected long nextChange = dustTime + lastInterval;

	@Override
	public String text()
	{
		return "TIME="+dustTime+" LEVEL="+dustLevel+" INTERVAL="+(lastInterval / CMProps.getMillisPerMudHour());
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		dustLevel = CMParms.getParmInt(newMiscText, "LEVEL", 0);
		dustTime = CMParms.getParmLong(newMiscText, "TIME", System.currentTimeMillis());
		lastInterval = CMParms.getParmLong(newMiscText, "INTERVAL",  6 * 20);
		lastInterval *= CMProps.getMillisPerMudHour();
		nextChange = dustTime + lastInterval;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Item)
		{
			final Item I=(Item)affected;
			if(I.owner() instanceof MOB)
			{
				I.delEffect(this);
				return;
			}
			if(System.currentTimeMillis() > nextChange)
			{
				dustTime = System.currentTimeMillis();
				lastInterval *= 2;
				dustLevel += 1;
				nextChange = dustTime + lastInterval;
			}
			if(I.owner() instanceof Room)
			{
				switch(dustLevel)
				{
				case 0:
				{
					affectableStats.addAmbiance(L("a bit dusty"));
					break;
				}
				case 1:
				{
					affectableStats.addAmbiance(L("dusty"));
					break;
				}
				case 2:
				{
					affectableStats.setName(L("a dusty " + CMLib.english().removeArticleLead(affected.name())));
					break;
				}
				case 3:
				{
					affectableStats.setName(affected.name()+L(", covered in dust"));
					break;
				}
				case 4:
				{
					affectableStats.setName(affected.name()+L(", covered in a thick layer of dust"));
					break;
				}
				case 5:
				{
					affectableStats.setName(L("Something under a dense layer of dust"));
					break;
				}
				default:
				{
					affectableStats.setName(L("A lump of thick dust"));
					break;
				}
				}
			}
		}
	}


	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final Room R=msg.source().location();
		if(R!=null)
		{
			final Area A=R.getArea();
			if(A!=null)
			{
				final int weather = A.getClimateObj().weatherType(R);
				switch(weather)
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_RAIN:
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_SNOW:
				case Climate.WEATHER_HAIL:
				case Climate.WEATHER_SLEET:
					if(affected != null)
					{
						affected.delEffect(this);
						affected=null;
					}
					break;
				case Climate.WEATHER_DUSTSTORM:
					// let puddle maker leave dust behind
					if(affected != null)
					{
						affected.delEffect(this);
						affected=null;
					}
					break;
				case Climate.WEATHER_CLEAR:
				case Climate.WEATHER_CLOUDY:
				case Climate.WEATHER_WINDY:
				case Climate.WEATHER_HEAT_WAVE:
				case Climate.WEATHER_DROUGHT:
				case Climate.WEATHER_WINTER_COLD:
					break;
				}
			}
		}

		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null))
		{
			final MOB mob=msg.source();
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			if("DUST".startsWith(word)
			||"CLEAN".startsWith(word))
			{
				if(R != null)
				{
					cmds.remove(0);
					final String rest=CMParms.combine(cmds,0);
					if(rest.equalsIgnoreCase("all")
					|| (rest.equalsIgnoreCase(""))
					|| (rest.equalsIgnoreCase("room"))
					|| (rest.equalsIgnoreCase("here")))
					{
						if(R.show(msg.source(), null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> clean(s) and dust(s).")))
						{
							for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
							{
								final Item I=i.nextElement();
								if((I!=null)
								&&(I.container()==null)
								&&(CMLib.flags().isGettable(I)))
								{
									final Dusty D=(Dusty)I.fetchEffect(ID());
									if(D!=null)
									{
										D.dustLevel--;
										if(D.dustLevel < 1)
											I.delEffect(D);
										I.recoverPhyStats();
									}
								}
							}
						}
					}
					else
					{
						final Item I=R.findItem(null, rest);
						if((I==null)
						||(!CMLib.flags().canBeSeenBy(I, mob)))
						{
							msg.setSourceMessage(L("You don't see '@x1' here.",rest));
							return true;
						}
						final Dusty D=(Dusty)I.fetchEffect(ID());
						if(D==null)
						{
							msg.setSourceMessage(L("@x1 doesn't seem very dirty.",I.name(mob)));
							return true;
						}
						if(R.show(msg.source(), I, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> clean(s) and dust(s) <T-NAME>.")))
						{
							I.delEffect(D);
						}
					}
					return false;
				}
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(msg.target()==affected)
		{
			if(msg.targetMinor()==CMMsg.TYP_GET)
			{
				final int choice=CMLib.dice().roll(1, 7, -1);
				switch(choice)
				{
				case 0:
					msg.addTrailerMsg(CMClass.getMsg(msg.source(), msg.target(), null, CMMsg.MASK_ALWAYS|CMMsg.MSG_HANDS, L("<S-NAME> clean(s) the dust off of <T-NAME>.")));
					break;
				case 1:
					msg.addTrailerMsg(CMClass.getMsg(msg.source(), msg.target(), null, CMMsg.MASK_ALWAYS|CMMsg.MSG_HANDS, L("<S-NAME> blow(s) the dust off of <T-NAME>.")));
					break;
				case 2:
					msg.addTrailerMsg(CMClass.getMsg(msg.source(), msg.target(), null, CMMsg.MASK_ALWAYS|CMMsg.MSG_HANDS, L("<S-NAME> wipe(s) the dust off of <T-NAME>.")));
					break;
				case 3:
					msg.addTrailerMsg(CMClass.getMsg(msg.source(), msg.target(), null, CMMsg.MASK_ALWAYS|CMMsg.MSG_HANDS, L("<S-NAME> dust(s) off <T-NAME>.")));
					break;
				case 5:
					break;
				case 6:
					break;
				case 7:
					break;
				}
				if(affected != null)
				{
					affected.delEffect(this);
					affected=null;
				}
			}
			else
			if(msg.targetMinor()==CMMsg.TYP_DROP)
			{
				if(affected != null)
				{
					affected.delEffect(this);
					affected=null;
				}
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}
}
