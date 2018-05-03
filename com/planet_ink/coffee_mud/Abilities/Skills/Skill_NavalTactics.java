package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_NavalTactics extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_NavalTactics";
	}

	private final static String	localizedName	= CMLib.lang().L("Naval Tactics");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	private static final String[]	triggerStrings	= I(new String[] { "NAVALTACTICS", "NTACTICS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected int				setDistance	= -1;
	protected Tactic			tactic		= Tactic.FOLLOW;
	protected volatile boolean	wait		= false;

	private enum Tactic
	{
		FOLLOW,
		APPROACH,
		RETREAT,
		FLEE
	}
	
	protected boolean isGoodShipDir(Room shipR, int dir)
	{
		final Room R=shipR.getRoomInDir(dir);
		final Exit E=shipR.getExitInDir(dir);
		if((R!=null)
		&&(CMLib.flags().isWateryRoom(R))
		&&(E!=null)
		&&(E.isOpen()))
			return true;
		return false;
	}
	
	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		
		if((affected!=null)
		&&(msg.source().riding()==affected)
		&&(msg.source().Name().equals(affected.Name()))
		&&((msg.sourceMinor()==CMMsg.TYP_ACTIVATE)
			||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			||(msg.sourceMinor()==CMMsg.TYP_ATTACKMISS)))
			wait=false;
		return true;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		
		{
			final Physical affected=this.affected;
			if(!(affected instanceof SailingShip))
				return false;
			if(wait)
				return true;
			final MOB mob=invoker();
			final SailingShip shipItem=(SailingShip)affected;
			final Room R=mob.location();
			if((R!=null)
			&&(tactic!=null)
			&&(R.getArea() instanceof BoardableShip)
			&&(shipItem.getShipArea()==R.getArea())
			&&(shipItem.isInCombat())
			&&(shipItem.getCombatant() instanceof SailingShip)
			&&((CMLib.law().doesHavePriviledgesHere(mob, R))
				||(CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.CMDMOBS)))
			&&(R.roomID().length()>0)
			&&((R.domainType()&Room.INDOORS)==0))
			{
				final SailingShip targetShip=(SailingShip)shipItem.getCombatant();
				// establish desired distance
				switch(tactic)
				{
				case APPROACH:
					setDistance = 0;
					break;
				case FLEE:
					setDistance = R.maxRange() + 1;
					break;
				case FOLLOW:
				{
					if(setDistance < 0)
						setDistance = shipItem.rangeToTarget();
					break;
				}
				case RETREAT:
					if(setDistance < 0)
					{
						final Area otherArea=targetShip.getShipArea();
						int highestRange=0;
						for(final Enumeration<Room> r=otherArea.getProperMap();r.hasMoreElements();)
						{
							final Room oR=r.nextElement();
							if(oR.numItems()>0)
							{
								for(final Enumeration<Item> i=oR.items();i.hasMoreElements();)
								{
									final Item I=i.nextElement();
									if(CMLib.combat().isAShipSiegeWeapon(I))
									{
										int range=I.maxRange();
										if(range > highestRange)
											highestRange=range;
									}
								}
							}
						}
						setDistance=highestRange+1;
					}
					break;
				default:
					return false;
				}
				if(shipItem.rangeToTarget() == setDistance)
					return true;
				int mySpeed = shipItem.getShipSpeed();
				int directionToTarget=shipItem.getDirectionToTarget();
				List<Integer> newCourse = new ArrayList<Integer>();
				int myMoves=mySpeed;
				int direction=directionToTarget;
				switch(tactic)
				{
				case APPROACH:
					if(myMoves > shipItem.rangeToTarget())
						myMoves=shipItem.rangeToTarget();
					if(direction != shipItem.getDirectionFacing())
						newCourse.add(Integer.valueOf(direction));
					else
					for(int m=0;m<myMoves;m++)
						newCourse.add(Integer.valueOf(direction));
					break;
				case FLEE:
				{
					Room shipR=CMLib.map().roomLocation(shipItem);
					if(shipR!=null)
					{
						direction=Directions.getOpDirectionCode(directionToTarget);
						if(direction<0)
							direction=Directions.NORTH;
						if(!isGoodShipDir(shipR,direction))
						{
							final List<Integer> goodDirs = new ArrayList<Integer>();
							for(int dir : Directions.CODES())
							{
								if(isGoodShipDir(shipR,dir))
									goodDirs.add(Integer.valueOf(dir));
							}
							final Integer dirI=Integer.valueOf(directionToTarget);
							if(goodDirs.contains(dirI)
							&&(goodDirs.size()>1))
								goodDirs.remove(dirI);
							if(goodDirs.size()>0)
								direction=goodDirs.get(0).intValue();
						}
						if(direction != shipItem.getDirectionFacing())
							newCourse.add(Integer.valueOf(direction));
						else
						for(int m=0;m<myMoves;m++)
							newCourse.add(Integer.valueOf(direction));
					}
					break;
				}
				case FOLLOW:
				{
					int movesToGo=0;
					if(shipItem.rangeToTarget() > setDistance)
						movesToGo=shipItem.rangeToTarget() - setDistance;
					else
					if(shipItem.rangeToTarget() < setDistance)
					{
						direction=Directions.getOpDirectionCode(direction);
						movesToGo=setDistance - shipItem.rangeToTarget();
					}
					else
						return true;
					if(myMoves > movesToGo)
						myMoves = movesToGo;
					if(direction != shipItem.getDirectionFacing())
						newCourse.add(Integer.valueOf(direction));
					else
					for(int m=0;m<myMoves;m++)
						newCourse.add(Integer.valueOf(direction));
					break;
				}
				case RETREAT:
					if(shipItem.rangeToTarget() >= setDistance)
						return true;
					int movesToGo=setDistance - shipItem.rangeToTarget();
					if(myMoves > movesToGo)
						myMoves = movesToGo;
					direction=Directions.getOpDirectionCode(direction);
					if(direction != shipItem.getDirectionFacing())
						newCourse.add(Integer.valueOf(direction));
					else
					for(int m=0;m<myMoves;m++)
						newCourse.add(Integer.valueOf(direction));
					break;
				default:
					break;
				
				}
				if(newCourse.size()>0)
				{
					final List<String> courseCmd=new ArrayList<String>();
					courseCmd.add("COURSE");
					for(Integer I : newCourse)
						courseCmd.add(CMLib.directions().getDirectionName(I.intValue()));
					if(shipItem.isAnchorDown())
						mob.enqueCommand(new XVector<String>("RAISE","ANCHOR"), 0, 0);
					mob.enqueCommand(courseCmd, 0, 0);
					wait=true;
				}
				return true;
			}
			if(this.canBeUninvoked())
			{
				this.unInvoke();
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void unInvoke()
	{
		final MOB invoker=this.invoker();
		super.unInvoke();
		if((invoker!=null)&&(this.unInvoked))
			invoker.tell(L("Your ship is no longer following naval tactics."));
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			this.tactic=(Tactic)CMath.s_valueOf(Tactic.class, newMiscText.toUpperCase().trim());
			wait=false;
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof BoardableShip))
		||(!(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip)))
		{
			mob.tell(L("You must be on a sailing ship."));
			return false;
		}
		final BoardableShip myShip=(BoardableShip)R.getArea();
		final SailingShip myShipItem=(SailingShip)myShip.getShipItem();
		final Area myShipArea=myShip.getShipArea();
		final Room myShipRoom = CMLib.map().roomLocation(myShipItem);
		if((myShipItem==null)
		||(myShipArea==null)
		||(myShipRoom==null)
		||(!(myShipItem.owner() instanceof Room)))
		{
			mob.tell(L("You must be on your sailing ship."));
			return false;
		}
		
		if((R.domainType()&Room.INDOORS)!=0)
		{
			mob.tell(L("You must be on the deck of a ship."));
			return false;
		}
		
		if((!CMLib.law().doesHavePriviledgesHere(mob, R))
		&&(!CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.CMDMOBS)))
		{
			mob.tell(L("You must be on the deck of a ship that you have privileges on."));
			return false;
		}
		
		final Skill_NavalTactics A=(Skill_NavalTactics)myShipItem.fetchEffect(ID());
		if((commands.size()==0)&&(A!=null))
		{
			A.unInvoke();
			return false;
		}
		
		if((commands.size()==0)
		||(CMath.s_valueOf(Tactic.class, commands.get(0).toUpperCase())==null))
		{
			mob.tell(L("You need to specify a tactic, such as FOLLOW, APPROACH, RETREAT, or FLEE."));
			return false;
		}
		Tactic tactic = (Tactic)CMath.s_valueOf(Tactic.class, commands.get(0).toUpperCase());
		
		final PhysicalAgent targetShip=myShipItem.getCombatant();
		if((!myShipItem.isInCombat())
		||(!(targetShip instanceof SailingShip))
		||(!myShipRoom.isHere(targetShip)))
		{
			mob.tell(L("You must be in combat with another large sailing ship to use that tactic."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String str=auto?"":L("^S<S-NAME> adjust(s) <S-HIS-HER> naval tactics.^?");
			final CMMsg msg=CMClass.getMsg(mob,myShipItem,this,CMMsg.MSG_QUIETMOVEMENT,str,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_QUIETMOVEMENT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				Skill_NavalTactics tacticA;
				if(A!=null)
					tacticA=A;
				else
					tacticA=(Skill_NavalTactics)super.beneficialAffect(mob, myShipItem, asLevel, 0);
				if(tacticA!=null)
				{
					tacticA.setDistance=-1;
					tacticA.setMiscText(tactic.toString());
					tacticA.tick(myShipItem, Tickable.TICKID_SPELL_AFFECT); // first tick is important
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,myShipItem,L("<S-NAME> attempt(s) to adjust naval tactics, but fail(s)."));

		// return whether it worked
		return success;
	}
}
