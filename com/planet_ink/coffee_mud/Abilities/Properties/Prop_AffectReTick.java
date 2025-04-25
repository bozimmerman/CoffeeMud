package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
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
   Copyright 2025-2025 Bo Zimmerman

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
public class Prop_AffectReTick extends Property
{
	@Override
	public String ID()
	{
		return "Prop_AffectReTick";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_MOBS|Ability.CAN_ROOMS|Ability.CAN_EXITS|Ability.CAN_AREAS;
	}

	protected final int CAN_OWNERS = (int)CMath.pow(2, Ability.CAN_DESCS.length);
	protected final int CAN_WEARERS = (int)CMath.pow(2, Ability.CAN_DESCS.length+1);
	protected final Map<String, CompiledFormula> abilityTickRules = new Hashtable<String, CompiledFormula>();
	protected final Map<Ability,Integer> alreadyProcessed = new ExpireHashMap<Ability,Integer>(30 * 60 * 1000);
	protected final Set<Affectable> alreadyScanned = new ExpireHashSet<Affectable>(5 * 60 * 1000);
	protected int scanCode = Ability.CAN_MOBS;
	protected CompiledZMask mask = null;
	protected boolean initialized = false;

	protected void retick(final Ability A)
	{
		if((A!=null)
		&&(A.canBeUninvoked())
		&&(!A.isNowAnAutoEffect())
		&&(!alreadyProcessed.containsKey(A)))
		{
			final CompiledFormula formula = abilityTickRules.get(A.ID());
			final int tickDown = CMath.s_int(A.getStat("TICKDOWN"));
			alreadyProcessed.put(A, Integer.valueOf(tickDown));
			final double[] vars = new double[] { tickDown, tickDown };
			final int newDown = (int)Math.round(CMath.parseMathExpression(formula, vars, tickDown));
			if(newDown < tickDown)
			{
				final Physical P = A.affecting();
				final int tickID;
				if(P instanceof Item)
					tickID = Tickable.TICKID_ITEM_BEHAVIOR;
				else
				if(P instanceof Room)
					tickID = Tickable.TICKID_ROOM_BEHAVIOR;
				else
				if(P instanceof Exit)
					tickID = Tickable.TICKID_EXIT_BEHAVIOR;
				else
					tickID = Tickable.TICKID_MOB;
				for(int i=0;i<tickDown - newDown;i++)
					A.tick(A.affecting(), tickID);
			}
			A.setStat("TICKDOWN", ""+newDown);
		}
	}

	protected void untick(final Ability A)
	{
		if((A!=null)
		&&(A.canBeUninvoked())
		&&(!A.isNowAnAutoEffect())
		&&(alreadyProcessed.containsKey(A)))
		{
			final Integer tickDown = alreadyProcessed.remove(A);
			if(tickDown != null)
				A.setStat("TICKDOWN",tickDown.toString());
		}
	}

	protected void untick(final Physical P)
	{
		if((P!=null)
		&&(alreadyScanned.contains(P)))
		{
			if(((mask==null)||CMLib.masking().maskCheck(mask, P, true))
			&&(P.numEffects()>0))
			{
				alreadyScanned.remove(P);
				if(abilityTickRules.size()>P.numEffects())
				{
					for(int i=0;i<P.numEffects();i++)
					{
						final Ability A = P.fetchEffect(i);
						if(abilityTickRules.containsKey(A.ID()))
							untick(A);
					}
				}
				else
				{
					for(final String key : abilityTickRules.keySet())
					{
						final Ability A = P.fetchEffect(key);
						untick(A);
					}
				}
			}
		}
	}

	protected void retick(final Physical P)
	{
		if((P!=null)
		&&(!alreadyScanned.contains(P)))
		{
			alreadyScanned.add(P);
			if(((mask==null)||CMLib.masking().maskCheck(mask, P, true))
			&&(P.numEffects()>0))
			{
				if(abilityTickRules.size()>P.numEffects())
				{
					for(int i=0;i<P.numEffects();i++)
					{
						final Ability A = P.fetchEffect(i);
						if(abilityTickRules.containsKey(A.ID()))
							retick(A);
					}
				}
				else
				{
					for(final String key : abilityTickRules.keySet())
					{
						final Ability A = P.fetchEffect(key);
						retick(A);
					}
				}
			}
		}
	}

	protected void process(final MOB M, final boolean domobs, final boolean doitems)
	{
		if(M != null)
		{
			final boolean noReItemMob = alreadyScanned.contains(M);
			if(domobs)
				retick(M);
			if(doitems && (!noReItemMob))
			{
				for(int i=0;i<M.numItems();i++)
				{
					final Item I = M.getItem(i);
					if(I != null)
						retick(I);
				}
			}
		}
	}

	protected void process(final Room R, final boolean domobs, final boolean doitems)
	{
		if(CMath.bset(scanCode, CAN_OWNERS))
		{
			if(affected instanceof Item)
				retick(((Item)affected).owner());
			else
				retick(affected);
		}
		if(CMath.bset(scanCode, Ability.CAN_ROOMS))
			retick(R);
		if(CMath.bset(scanCode, Ability.CAN_EXITS))
		{
			for(int d=0; d<Directions.NUM_DIRECTIONS(); d++)
			{
				final Exit X = R.getExitInDir(d);
				if(X != null)
					retick(X);
			}
		}
		if(doitems)
		{
			for(int i=0;i<R.numItems();i++)
			{
				final Item I = R.getItem(i);
				if(I != null)
					retick(I);
			}
		}
		if(domobs || doitems)
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M = R.fetchInhabitant(m);
				process(M, domobs, doitems);
			}
		}
	}

	protected Environmental mightApplier(final Environmental E)
	{
		if((E==affected)
		&&(CMath.bset(scanCode, CAN_OWNERS)))
			return E;
		if(E instanceof Area)
		{
			if(CMath.bset(scanCode, Ability.CAN_AREAS))
				return E;
		}
		else
		if(E instanceof Room)
		{
			if(CMath.bset(scanCode, Ability.CAN_ROOMS))
				return E;
		}
		else
		if(E instanceof MOB)
		{
			if(CMath.bset(scanCode, Ability.CAN_MOBS))
				return E;
			if((affected instanceof Item)
			&&(((Item)affected).owner()==E))
			{
				if(CMath.bset(scanCode, CAN_OWNERS))
					return E;
				if((CMath.bset(scanCode, CAN_WEARERS))
				&&(((Item)affected).amBeingWornProperly()))
					return E;
			}
		}
		else
		if(E instanceof Item)
		{
			if(CMath.bset(scanCode, Ability.CAN_ITEMS))
				return E;
		}
		return null;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.tool() instanceof Ability)
		&&(abilityTickRules.containsKey(msg.tool().ID()))
		&&(!alreadyProcessed.containsKey(msg.tool())))
		{
			msg.addTrailerRunnable(new Runnable() {
				final String ID = msg.tool().ID();
				final Environmental src = mightApplier(msg.source());
				final Environmental target = mightApplier(msg.target());
				@Override
				public void run()
				{
					CMLib.threads().scheduleRunnable(new Runnable() {
						@Override
						public void run()
						{
							Ability A;
							if(src instanceof Affectable)
							{
								A = ((Affectable)src).fetchEffect(ID);
								if((A != null)
								&&((mask==null)||CMLib.masking().maskCheck(mask, src, true)))
									retick(A);
							}
							if(target instanceof Affectable)
							{
								A = ((Affectable)target).fetchEffect(ID);
								if((A != null)
								&&((mask==null)||CMLib.masking().maskCheck(mask, target, true)))
									retick(A);
							}
						}
					}, 800);
				}
			});
		}
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&(!CMath.banyset(scanCode, CAN_OWNERS|CAN_WEARERS)))
		{
			final boolean domobs = CMath.bset(scanCode, Ability.CAN_MOBS);
			final boolean doitems = CMath.bset(scanCode, Ability.CAN_ITEMS);
			process(msg.source(), domobs, doitems);
			process((Room)msg.target(), domobs, doitems);
		}
		else
		if((msg.target() == affected)
		&&(affected instanceof Item)
		&&(CMath.banyset(scanCode, CAN_OWNERS|CAN_WEARERS)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			{
				if(CMath.bset(scanCode, CAN_OWNERS))
					retick(((Item)affected).owner());
				else
				if(((Item)affected).amBeingWornProperly())
					retick(((Item)affected).owner());
				break;
			}
			case CMMsg.TYP_DROP:
			{
				untick(((Item)affected).owner());
				break;
			}
			case CMMsg.TYP_WEAR:
			{
				if(CMath.bset(scanCode, CAN_WEARERS))
					retick(msg.source());
				break;
			}
			case CMMsg.TYP_REMOVE:
			{
				if(CMath.bset(scanCode, CAN_WEARERS))
					untick(msg.source());
				break;
			}
			default:
				break;
			}
		}
		else
		if(!initialized)
		{
			synchronized(this)
			{
				if(!initialized)
				{
					initialized = true;
					if(CMath.banyset(scanCode, CAN_OWNERS|CAN_WEARERS))
					{
						if(affected instanceof Item)
						{
							if(scanCode==CAN_WEARERS)
							{
								if(((Item)affected).amBeingWornProperly())
									retick(((Item)affected).owner());
							}
							else
								retick(((Item)affected).owner());
						}
						else
							retick(affected);
					}
					else
					{
						final boolean domobs = CMath.bset(scanCode, Ability.CAN_MOBS);
						final boolean doitems = CMath.bset(scanCode, Ability.CAN_ITEMS);
						if(affected instanceof Area)
						{
							for(final Enumeration<Room> r= ((Area)affected).getFilledCompleteMap();r.hasMoreElements();)
							{
								final Room R = r.nextElement();
								if(R!=null)
									process(R, domobs, doitems);
							}
						}
						else
						{
							final Room R = CMLib.map().roomLocation(this);
							if(R!=null)
								process(R, domobs, doitems);
						}
					}
				}
			}
		}
		super.executeMsg(host, msg);
	}

	@Override
	public void setMiscText(final String parms)
	{
		super.setMiscText(parms);
		final Map<String,String> parMap = CMParms.parseEQParms(parms);
		scanCode = Ability.CAN_MOBS;
		if(parMap.containsKey("SCAN"))
		{
			final String scan = parMap.remove("SCAN").toUpperCase().trim();
			scanCode = 0;
			for(final String sc : CMParms.parseCommas(scan,true))
			{
				if(sc.equals("OWNER")||sc.equals("OWNERS"))
					scanCode |= CAN_OWNERS;
				else
				{
					int dex = CMParms.indexOf(Ability.CAN_DESCS, sc);
					if(dex < 0)
						dex = CMParms.indexOf(Ability.CAN_DESCS, sc+"S");
					if(dex < 0)
						Log.errOut(ID(), "Unknown scan code: "+sc+" in "+parms);
					else
						scanCode |= CMath.pow(2, dex);
				}
			}
		}
		mask = null;
		if(parMap.containsKey("SCANMASK"))
		{
			final String scanMask = parMap.remove("SCANMASK").trim();
			if(scanMask.length()==0)
				mask = null;
			else
				mask = CMLib.masking().getPreCompiledMask(scanMask);
		}
		abilityTickRules.clear();
		for(final String key : parMap.keySet())
		{
			final Ability A = CMClass.findAbility(key);
			if(A == null)
				Log.errOut(ID(), "Unknown ability: "+key+" in "+parms);
			else
			{
				final String formula = parMap.get(key);
				if(formula.indexOf("@x1")<0)
					Log.errOut(ID(), "Bad formula for ability: "+key+" in "+parms);
				else
				{
					try
					{
						abilityTickRules.put(A.ID(), CMath.compileMathExpression(formula));
					}
					catch(final ArithmeticException e)
					{
						Log.errOut(ID(), "Bad formula for ability: "+key+" in "+parms+": "+e.getMessage());
					}
				}
			}
		}
	}
}
