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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/*
   Copyright 2017-2024 Bo Zimmerman

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
public class Prop_WearOverride extends Property
{
	@Override
	public String ID()
	{
		return "Prop_WearOverride";
	}

	@Override
	public String name()
	{
		return "Wearable Unzapper";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected CompiledZMask		mask		= null;
	protected String			maskDesc	= "";
	protected boolean			forceDress	= false;
	protected boolean			contentsOk	= false;
	protected long				locMaskAdj	= Integer.MAX_VALUE;
	protected volatile boolean	activated	= false;
	protected volatile MOB		lastMob		= null;

	public String accountForYourself()
	{
		if(affected != null)
			return "Allows "+affected.Name()+" to be worn by: "+maskDesc;
		else
			return "Allows an item to be worn by "+maskDesc;
	}

	@Override
	public void setMiscText(final String newText)
	{
		maskDesc = "";
		mask = null;
		forceDress = false;
		if(newText.length()>0)
		{
			final List<String> ps = CMParms.parse(newText);
			for(int p=ps.size()-1;p>=0;p--)
			{
				if(ps.get(p).equalsIgnoreCase("DRESS"))
				{
					forceDress = true;
					ps.remove(p);
				}
				else
				if(ps.get(p).equalsIgnoreCase("CONTENTS"))
				{
					contentsOk = true;
					ps.remove(p);
				}
			}
			final String maskStr = CMParms.combineQuoted(ps, 0);
			mask=CMLib.masking().getPreCompiledMask(maskStr);
			maskDesc=CMLib.masking().maskDesc(maskStr,true);
		}
		super.setMiscText(newText);
	}

	@Override
	public boolean bubbleAffect()
	{
		return activated;
	}

	@Override
	public void affectCharStats(final MOB affectMOB, final CharStats affectableStats)
	{
		if(this.activated)
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap() & this.locMaskAdj);
	}

	protected boolean dress(final Room R, final MOB mob, final Environmental target, final Item I)
	{
		final Command C=CMClass.getCommand("Dress");
		if(C!=null)
		{
			final String targetName = R.getContextName(target);
			final String toolName = "$"+I.name()+"$";
			try
			{
				C.execute(mob, new XVector<String>("Dress",targetName,toolName), 0);
				return true;
			}
			catch (final IOException e)
			{
			}
		}
		return false;
	}

	protected boolean undress(final Room R, final MOB mob, final Environmental target, final Item I)
	{
		final Command C=CMClass.getCommand("Undress");
		if(C!=null)
		{
			final String targetName = R.getContextName(target);
			final String toolName = "$"+I.name()+"$";
			try
			{
				C.execute(mob, new XVector<String>("Undress",targetName,toolName), 0);
				return true;
			}
			catch (final IOException e)
			{
			}
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((contentsOk)
		&&(affected instanceof Container)
		&&((msg.source().riding()==null)||(msg.source().riding()==((Container)affected).owner())))
		{
			final Container itemC=(Container)affected;
			if((msg.target() instanceof MOB)
			&&(itemC!=null)
			&&(itemC.owner() == msg.target())
			&&(itemC.rawWornCode()!=Item.IN_INVENTORY)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(CMLib.flags().canBeSeenBy(msg.target(),msg.source())))
			{
				msg.addTrailerRunnable(new Runnable() {
					final MOB mob=msg.source();
					final Container C=itemC;
					final int srcCode = msg.targetCode();
					@Override
					public void run()
					{
						final CMMsg msg2=CMClass.getMsg(mob, C, srcCode, null);
						CMLib.commands().handleBeingLookedAt(msg2);
					}
				});
			}
		}
		super.executeMsg(host,msg);
	}
	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_WEAR)
		&&(affected instanceof Item))
		{
			if(msg.target() == affected)
			{
				if((mask != null)
				&&(CMLib.masking().maskCheck(mask, msg.source(), true)))
				{
					activated=true;
					this.locMaskAdj=~((Item)msg.target()).rawProperLocationBitmap();
					msg.source().recoverCharStats();
					msg.source().recoverCharStats();
					msg.addTrailerRunnable(new Runnable()
					{
						final MOB mob=msg.source();
						@Override
						public void run()
						{
							activated = false;
							mob.recoverCharStats();
							mob.recoverCharStats();
						}
					});
					this.lastMob = msg.source();
				}
				else
				{
					msg.source().tell(L("That won't fit on the likes of you."));
					return false;
				}
			}
			else
			if(lastMob == msg.source())
			{
				activated = false;
				lastMob = null;
				msg.source().recoverCharStats();
				msg.source().recoverCharStats();
			}
		}
		else
		if((forceDress||contentsOk)
		&&(affected instanceof Container)
		&&((msg.source().riding()==null)||(msg.source().riding()==((Container)affected).owner())))
		{
			final Item contI = (Item)affected;
			if(forceDress
			&&(msg.tool()==contI)
			&&(msg.targetMinor()==CMMsg.TYP_PUT)
			&&(msg.target() instanceof MOB))
			{
				final Room R=CMLib.map().roomLocation(msg.target());
				if(dress(R,msg.source(),msg.target(),contI))
					return false;
			}
			else
			if(contentsOk
			&&(msg.targetMinor()==CMMsg.TYP_PUT)
			&&(msg.tool()!=contI)
			&&(msg.target() instanceof MOB)
			&&(contI!=null)
			&&(contI.rawWornCode()!=Item.IN_INVENTORY)
			&&(msg.target()==contI.owner()))
			{
				final MOB ownM=(MOB)msg.target();
				final long rawWornCode=contI.rawWornCode();
				try
				{
					msg.source().moveItemTo(contI);
					final Command C=CMClass.getCommand("Put");
					if(C!=null)
					{
						try
						{
							final List<String> V = new XVector<String>("PUT",msg.tool().Name(),contI.Name());
							C.execute(msg.source(), V, 0);
							return false;
						}
						catch (final IOException e)
						{
						}
					}
				}
				finally
				{
					if(contI.owner() == msg.source())
					{
						ownM.moveItemTo(contI);
						contI.setRawWornCode(rawWornCode);
					}
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
			{
				if(forceDress
				&&(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'D'))
				{
					final List<String> parsedFail = CMParms.parse(msg.targetMessage());
					if(parsedFail.size()<3)
						return true;
					final String cmd=parsedFail.get(0).toUpperCase();
					if(!("DRESS".startsWith(cmd)))
						return true;
					final Room R=CMLib.map().roomLocation(msg.source());
					if(R==null)
						return true;
					final String what=parsedFail.get(parsedFail.size()-1);
					parsedFail.remove(what);
					final MOB mob=msg.source();
					final Item item=mob.findItem(null,what);
					if(item == contI)
					{
						final String whom=CMParms.combine(parsedFail,1);
						final MOB target=R.fetchInhabitant(whom);
						if(target != null)
						{
							if((!target.willFollowOrdersOf(mob))
							&&(!CMLib.flags().isBoundOrHeld(target)))
							{
								final MOB oldFollowing=target.amFollowing();
								try
								{
									target.setFollowing(mob);
									if(dress(R,mob,target,item))
										return false;
								}
								finally
								{
									target.setFollowing(oldFollowing);
								}
							}
						}
					}
					return true;
				}
				else
				if(forceDress
				&&(contI!=null)
				&&(contI.rawWornCode()!=Item.IN_INVENTORY)
				&&(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'U'))
				{
					final List<String> parsedFail = CMParms.parse(msg.targetMessage());
					if(parsedFail.size()<3)
						return true;
					final String cmd=parsedFail.get(0).toUpperCase();
					if(!("UNDRESS".startsWith(cmd)))
						return true;
					final Room R=CMLib.map().roomLocation(msg.source());
					if(R==null)
						return true;
					final String what=parsedFail.get(parsedFail.size()-1);
					parsedFail.remove(what);
					final MOB mob=msg.source();
					final Item item=mob.findItem(null,what);
					if(item == contI)
					{
						final String whom=CMParms.combine(parsedFail,1);
						final MOB target=R.fetchInhabitant(whom);
						if(target != null)
						{
							if((!target.willFollowOrdersOf(mob))
							&&(!CMLib.flags().isBoundOrHeld(target)))
							{
								final MOB oldFollowing=target.amFollowing();
								try
								{
									target.setFollowing(mob);
									if(dress(R,mob,target,item))
										return false;
								}
								finally
								{
									target.setFollowing(oldFollowing);
								}
							}
						}
					}
					return true;
				}
				else
				if(contentsOk
				&&(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'G')
				&&(contI!=null)
				&&(contI.rawWornCode()!=Item.IN_INVENTORY)
				&&(contI.owner() instanceof MOB)
				&&(contI.owner() != msg.source()))
				{
					final List<String> parsedFail = CMParms.parse(msg.targetMessage());
					if(parsedFail.size()<3)
						return true;
					final String cmd=parsedFail.get(0).toUpperCase();
					if(!("GET".startsWith(cmd)))
						return true;
					final MOB ownM = (MOB)contI.owner();
					final Room R=msg.source().location();
					int fromWhomDex=-1;
					int lastWhomDex=-1;
					for(int i=parsedFail.size()-2;i>=2;i--)
					{
						if("FROM".startsWith(parsedFail.get(i).toUpperCase()))
						{
							lastWhomDex=i;
							fromWhomDex=i+1;
							break;
						}
					}
					if(fromWhomDex<0)
					{
						fromWhomDex=parsedFail.size()-1;
						lastWhomDex=fromWhomDex;
					}
					if((fromWhomDex<0)||(R==null)||(ownM==null)||(msg.source().isMine(contI)))
						return true;
					if(R.fetchInhabitant(CMParms.combine(parsedFail,fromWhomDex))!=(MOB)contI.owner())
						return true;
					final String getWhat = CMParms.combine(parsedFail,1,lastWhomDex);
					if(ownM.fetchItem(contI, Wearable.FILTER_ANY, getWhat)!=null)
					{
						final long rawWornCode=contI.rawWornCode();
						try
						{
							msg.source().moveItemTo(contI);
							final Command C=CMClass.getCommand("Get");
							if(C!=null)
							{
								while(parsedFail.size()>fromWhomDex)
									parsedFail.remove(parsedFail.size()-1);
								parsedFail.add(contI.Name());
								try
								{
									C.execute(msg.source(), parsedFail, 0);
									return false;
								}
								catch (final IOException e)
								{
								}
							}
						}
						finally
						{
							ownM.moveItemTo(contI);
							contI.setRawWornCode(rawWornCode);
						}
					}
					else
					{
						msg.source().tell(L("You don't see @x1 in @x2 on @x3.",getWhat,contI.name(msg.source()),ownM.name(msg.source())));
						return false;
					}
					return true;
				}
				else
				if(contentsOk
				&&(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'P')
				&&(contI!=null)
				&&(contI.rawWornCode()!=Item.IN_INVENTORY)
				&&(contI.owner() instanceof MOB))
				{
					final List<String> parsedFail = CMParms.parse(msg.targetMessage());
					if(parsedFail.size()<3)
						return true;
					final String cmd=parsedFail.get(0).toUpperCase();
					if(!("PUT".startsWith(cmd)))
						return true;
					final MOB ownM = (MOB)contI.owner();
					final Room R=msg.source().location();
					int intoWhomDex=-1;
					for(int i=parsedFail.size()-2;i>=2;i--)
					{
						if("INTO".startsWith(parsedFail.get(i).toUpperCase()))
						{
							intoWhomDex=i+1;
							break;
						}
					}
					if(intoWhomDex<0)
						intoWhomDex=parsedFail.size()-1;
					if((intoWhomDex<0)||(R==null)||(ownM==null)||(msg.source().isMine(contI)))
						return true;
					if(R.fetchInhabitant(CMParms.combine(parsedFail,intoWhomDex))!=(MOB)contI.owner())
						return true;
					final long rawWornCode=contI.rawWornCode();
					try
					{
						msg.source().moveItemTo(contI);
						final Command C=CMClass.getCommand("Put");
						if(C!=null)
						{
							while(parsedFail.size()>=intoWhomDex)
								parsedFail.remove(parsedFail.size()-1);
							parsedFail.add(contI.Name());
							try
							{
								C.execute(msg.source(), parsedFail, 0);
								return false;
							}
							catch (final IOException e)
							{
							}
						}
					}
					finally
					{
						if(contI.owner() == msg.source())
						{
							ownM.moveItemTo(contI);
							contI.setRawWornCode(rawWornCode);
						}
					}
					return true;
				}
			}
		}
		return super.okMessage(host, msg);
	}
}
