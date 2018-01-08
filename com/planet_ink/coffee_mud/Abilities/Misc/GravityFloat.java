package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip.ShipFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class GravityFloat extends StdAbility
{
	@Override
	public String ID()
	{
		return "GravityFloat";
	}

	private final static String	localizedName	= CMLib.lang().L("GravityFloat");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Floating)");
	
	private volatile boolean flyingAllowed = false;

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS | Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}
	
	private class PossiblyFloater implements Runnable
	{
		private final Physical P;
		private final boolean hasGrav;

		public PossiblyFloater(final Physical possFloater, final boolean hasGravity)
		{
			this.P = possFloater;
			this.hasGrav = hasGravity;
		}

		@Override
		public void run()
		{
			final boolean hasGravity = confirmGravity(P,hasGrav);
			final Ability gravA=P.fetchEffect("GravityFloat");
			if(hasGravity)
			{
				if((gravA!=null)&&(!gravA.isSavable()))
				{
					gravA.unInvoke();
					P.delEffect(gravA);
					P.recoverPhyStats();
				}
			}
			else
			{
				if(gravA==null)
				{
					Ability gravityA=(Ability)copyOf();
					if(gravityA != null)
					{
						final Room R=CMLib.map().roomLocation(P);
						if(R!=null)
						{
							final CMMsg msg=CMClass.getMsg(CMClass.getFactoryMOB("gravity", 1, R),P,gravityA,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,null);
							if(P.okMessage(P, msg))
							{
								P.executeMsg(P, msg);
								R.showHappens(CMMsg.MSG_OK_VISUAL, P,L("<S-NAME> start(s) floating around."));
								P.addNonUninvokableEffect(gravityA);
								gravityA.setSavable(false);
								P.recoverPhyStats();
							}
						}
					}
				}
			}
		}
	}
	
	private final Runnable checkStopFloating()
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				final Physical P = affected;
				if(P!=null)
				{
					if(confirmGravity(P, false))
					{
						unInvoke();
						P.delEffect(GravityFloat.this);
						P.recoverPhyStats();
					}
				}
			}
		};
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(affected == null)
			return false;
		
		if(tickID!=Tickable.TICKID_MOB)
			return true;

		checkStopFloating().run();
		return (affected != null);
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setWeight(1);
		affectableStats.addAmbiance(L("Floating"));
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
		affectableStats.addAmbiance("-FLYING");
	}
	
	public void showFailedToMove(final MOB mob)
	{
		if(mob != null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				switch(CMLib.dice().roll(1, 10, 0))
				{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
				default:
				case 10:
					R.show(mob, null,CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> flap(s) around trying to go somewhere, but make(s) no progress."));
					break;
				}
			}
		}
	}
	
	public void showFailedToTouch(final MOB mob, Physical P)
	{
		if(mob != null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				switch(CMLib.dice().roll(1, 10, 0))
				{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					R.show(mob, P,CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> flap(s) around reaching for <T-NAME>, but <S-HE-SHE> float(s) away from it."));
					break;
				case 6:
				case 7:
				case 8:
				case 9:
				default:
				case 10:
					R.show(mob, P,CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> flap(s) around trying reach for <T-NAME>, but <T-HE-SHE> float(s) away."));
					break;
				}
			}
		}
	}
	
	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if((affected instanceof MOB)
		&&(msg.source()==affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_QUIETMOVEMENT:
			case CMMsg.TYP_NOISYMOVEMENT:
			case CMMsg.TYP_HANDS:
				if(!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))
				{
					if((msg.target() instanceof Item)
					&&(!msg.source().isMine(msg.target()))
					&&(CMLib.dice().rollPercentage()>20)
					&&(msg.source().phyStats().isAmbiance(L("Floating"))))
					{
						showFailedToTouch(msg.source(),(Physical)msg.target());
						return false;
					}
					if((msg.target() instanceof MOB)
					&&(CMLib.dice().rollPercentage()>20)
					&&(msg.source().phyStats().isAmbiance(L("Floating"))))
					{
						showFailedToTouch(msg.source(),(Physical)msg.target());
						return false;
					}
				}
				break;
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_SIT:
			{
				if(msg.source().phyStats().isAmbiance(L("Floating")) 
				&& (!flyingAllowed)
				&&(!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))
				&&(msg.target()!=null))
				{
					if(CMLib.flags().isSwimming(affected))
					{
						if(CMLib.dice().rollPercentage()>60)
						{
							showFailedToMove(msg.source());
							return false;
						}
					}
					else
					if(CMLib.dice().rollPercentage()>20)
					{
						showFailedToMove(msg.source());
						return false;
					}
				}
				break;
			}
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		// IDEA: gravity legs should develop over time...this turns into a saved ability with a score?
		if((affected instanceof Item)
		&&(msg.target()==affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
				msg.addTrailerRunnable(checkStopFloating());
				break;
			}
		}
		else
		if((affected instanceof MOB)
		&&(msg.source()==affected))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_PUSH:
				if((msg.target() instanceof Item)
				&&(msg.source().location().isHere(msg.target())))
				{
					final Item pushedI=(Item)msg.target();
					if((pushedI instanceof Rideable)
					||(!CMLib.flags().isGettable(pushedI))
					||(pushedI.phyStats().weight()>msg.source().phyStats().weight()))
					{
						// it will work
					}
					else
					{
						break; // won't work.
					}
				}
				else
				{
					break; // won't work.
				}
				//$FALL-THROUGH$
			case CMMsg.TYP_THROW:
				if(msg.source().phyStats().isAmbiance(L("Floating")))
				{
					final String sourceMessage = CMStrings.removeColors(CMLib.english().stripPunctuation(msg.sourceMessage()));
					final List<String> words=CMParms.parseSpaces(sourceMessage, true);
					final Room R=msg.source().location();
					if(R==null)
						break;
					final boolean useShip =((R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip))?true:false;
					int floatDir = -1;
					for(int i=words.size()-1;(i>=0) && (floatDir<0);i--)
					{
						for(int dir : Directions.DISPLAY_CODES())
						{
							if(words.get(i).equals(CMLib.directions().getUpperDirectionName(dir, useShip)))
							{
								floatDir = Directions.getOpDirectionCode(dir);
								break;
							}
						}
					}
					if(floatDir >=0)
					{
						final Room newRoom=R.getRoomInDir(floatDir);
						final Exit E=R.getExitInDir(floatDir);
						if((newRoom!=null)&&(E!=null)&&(E.isOpen()))
						{
							try
							{
								flyingAllowed=true;
								CMLib.tracking().walk(msg.source(),floatDir,msg.source().isInCombat(),false,false,false);
							}
							finally
							{
								flyingAllowed=false;
							}
						}
					}
				}
				break;
			}
		}
	}
	
	protected boolean confirmGravity(final Physical P, boolean hasGravity)
	{
		if(P instanceof Item)
		{
			final Item I=(Item)P;
			if((I.container()!=null)
			||(!CMLib.flags().isGettable(I))
			||(I instanceof Rideable)
			||(I.owner() instanceof MOB)
			||(I instanceof TechComponent))
			{
				if(!hasGravity)
					hasGravity=true;
			}
		}
		else
		if(P instanceof MOB)
		{
			final MOB M=(MOB)P;
			if((M.riding() != null) 
			|| (CMLib.flags().isBound(M)))
			{
				if(!hasGravity)
					hasGravity=true;
			}
			final Area A=CMLib.map().areaLocation(M);
			if(A instanceof SpaceShip)
			{
				if(!((SpaceShip)A).getShipFlag(ShipFlag.NO_GRAVITY))
				{
					if(!hasGravity)
						hasGravity=true;
				}
			}
		}
		return hasGravity;
	}
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(givenTarget==null)
			return false;
		final Physical P = givenTarget;
		
		new PossiblyFloater(P, auto).run();
		
		return true;
	}
}
