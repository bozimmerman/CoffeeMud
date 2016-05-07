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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


public class Sailor extends StdBehavior
{

	@Override
	public String ID()
	{
		return "Sailor";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
	}

	public Sailor()
	{
	}

	protected volatile int	tickDown		= -1;
	protected int			tickWait		= -1;
	protected BoardableShip	loyalShipArea	= null;
	protected Item			loyalShipItem	= null;
	protected Rideable		targetShipItem	= null;
	protected boolean		combatIsOver	= false;
	protected boolean		peaceMover		= false;
	protected boolean		combatMover		= true;
	protected boolean		combatTech		= true;
	protected boolean		aggressive		= false;
	protected boolean		aggrMobs		= false;
	protected boolean		areaOnly		= true;
	protected boolean		wimpy			= true;
	//protected int			targetShipDist	= -1;
	
	@Override
	public String accountForYourself()
	{
		if(getParms().trim().length()>0)
			return "aggression against "+CMLib.masking().maskDesc(getParms(),true).toLowerCase();
		else
			return "aggressiveness";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait = CMParms.getParmInt(newParms, "TICKDELAY", -1);
		peaceMover = CMParms.getParmBool(newParms, "PEACEMOVER", false);
		areaOnly = CMParms.getParmBool(newParms, "AREAONLY", true);
		combatMover = CMParms.getParmBool(newParms, "FIGHTMOVER", true);
		combatTech = CMParms.getParmBool(newParms, "FIGHTTECH", true);
		aggressive = CMParms.getParmBool(newParms, "AGGRO", false);
		aggrMobs = CMParms.getParmBool(newParms, "AGGROMOBS", false);
		wimpy = CMParms.getParmBool(newParms, "WIMPY", true);
		loyalShipArea	= null;
		loyalShipItem	= null;
	}
	
	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting, msg))
			return false;
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ENTER:
			if((this.loyalShipItem!=null)
			&&(msg.source().riding() == this.loyalShipItem)
			&&(msg.target() instanceof Room)
			&&(msg.source().isMonster())
			&&(msg.source().Name().equals(this.loyalShipItem.Name()))
			)
			{
				final Area shipA=CMLib.map().areaLocation(loyalShipItem);
				Room targetR=(Room)msg.target();
				if((areaOnly) && (shipA != targetR.getArea()))
					return false;
			}
			break;
		}
		return true;
	}
	
	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ADVANCE:
			if((msg.target() instanceof Rideable)
			&&(msg.target() instanceof Item)
			&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
			&&(msg.source().riding() == loyalShipItem))
			{
				targetShipItem = (Rideable)msg.target();
			}
			else
			if((msg.target()  == loyalShipItem)
			&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
			&&(msg.source().riding() != loyalShipItem))
			{
				targetShipItem = (Rideable)msg.target();
			}
			break;
		case CMMsg.TYP_ENTER:
			if((msg.source().riding() == this.loyalShipItem)
			&&(msg.source().isMonster())
			&&(msg.source().Name().equals(this.loyalShipItem.Name())))
			{
				
			}
			break;
		}
	}

	public boolean tryMend(MOB mob)
	{
		if(CMLib.flags().domainAffects(mob, Ability.ACODE_COMMON_SKILL).size()==0)
		{
			Ability A=CMClass.getAbility("Shipwright");
			if((A!=null)
			&&((A.proficiency()==0)||(A.proficiency()==100)))
			{
				A.setProficiency((mob.phyStats().level()+1) * 4 * 3);
				if(A.proficiency() >= 100)
					A.setProficiency(99);
			}
			Ability mend=mob.fetchAbility("Shipwright");
			if(mend != null)
			{
				mob.enqueCommand(new XVector<String>("SHIPWRIGHT","MEND",loyalShipItem.Name()), 0, 0);
				return true;
			}
		}
		return false;
	}
	
	protected boolean amInTrouble()
	{
		return (wimpy
		&&(loyalShipItem!=null)
		&&(this.targetShipItem!=null)
		&&(combatMover)
		&&(((Item)targetShipItem).subjectToWearAndTear())
		&&(loyalShipItem.subjectToWearAndTear())
		&&((((Item)targetShipItem).usesRemaining() - loyalShipItem.usesRemaining()) > 33));
	}

	protected boolean isGoodShipDir(Room shipR, int dir)
	{
		final Room R=shipR.getRoomInDir(dir);
		final Exit E=shipR.getExitInDir(dir);
		if((R!=null)
		&&(CMLib.flags().isWateryRoom(R))
		&&(E!=null)
		&&(E.isOpen())
		&&((!areaOnly)||(CMLib.map().areaLocation(shipR)==CMLib.map().areaLocation(R))))
			return true;
		return false;
	}
	
	protected int getEscapeRoute(int directionToTarget)
	{
		if(directionToTarget < 0)
			return directionToTarget;
		Room shipR=CMLib.map().roomLocation(loyalShipItem);
		if(shipR!=null)
		{
			int opDir=Directions.getOpDirectionCode(directionToTarget);
			if(isGoodShipDir(shipR,opDir))
				return opDir;
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
				return goodDirs.get(0).intValue();
		}
		return -1;
	}
	
	protected boolean canMoveShip()
	{
		return ((loyalShipItem!=null)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBILITY))
		&&(loyalShipItem.owner() instanceof Room)
		&&(((Room)loyalShipItem.owner()).getMobility())
		&&(!CMLib.tracking().isAnAdminHere((Room)loyalShipItem.owner(), true)));
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID!=Tickable.TICKID_MOB)
		||(!(ticking instanceof MOB)))
			return true;
		if(tickWait < 0)
		{
			if(ticking instanceof Physical)
			{
				tickWait = (30 - ((Physical)ticking).phyStats().level()) / 4;
				if(tickWait < 0)
					tickWait=0;
			}
			else
				tickWait = 10000;
		}
		
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			if((ticking instanceof MOB)
			&&(CMLib.flags().canFreelyBehaveNormal(ticking)))
			{
				final MOB mob=(MOB)ticking;
				final Room mobRoom=mob.location();
				if(mobRoom==null)
					return true;
				if((loyalShipItem==null)
				&&(mobRoom.getArea() instanceof BoardableShip))
				{
					loyalShipArea = (BoardableShip)mobRoom.getArea();
					loyalShipItem = loyalShipArea.getShipItem();
				}
				
				if(loyalShipItem==null)
					return true;
				
				if(mobRoom.getArea() != loyalShipArea)
					return true;
				
				if((targetShipItem!=null) 
				&&CMLib.map().roomLocation(targetShipItem)!=CMLib.map().roomLocation(loyalShipItem))
				{
					combatIsOver=true;
					targetShipItem = null;
					//stop combat signal
				}
				
				if(((combatMover && (targetShipItem != null)) || peaceMover))
				{
					if(CMath.s_bool(loyalShipItem.getStat("ANCHORDOWN"))
					&&(canMoveShip()))
					{
						mob.enqueCommand(new XVector<String>("RAISE","ANCHOR"), 0, 0);
						return true;
					}
				}
				
				if((loyalShipItem.owner() instanceof Room)
				&&(loyalShipItem.owner() instanceof GridLocale)
				&&(((Room)loyalShipItem.owner()).getGridParent()==null)
				&&(canMoveShip()))
					((GridLocale)loyalShipItem.owner()).getRandomGridChild().moveItemTo(loyalShipItem);
				
				if(targetShipItem != null)
				{
					int distanceToTarget=CMath.s_int(loyalShipItem.getStat("DISTANCETOTARGET"));
					if(loyalShipItem.subjectToWearAndTear()
					&&(CMLib.dice().rollPercentage() >= loyalShipItem.usesRemaining())
					&&(CMLib.dice().rollPercentage()<50)
					&&(tryMend(mob)))
						return true;
					if(combatTech)
					{
						for(Enumeration<Item> i=mobRoom.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if(CMLib.combat().isAShipSiegeWeapon(I)
							&&(I instanceof AmmunitionWeapon)
							&&(((AmmunitionWeapon)I).ammunitionCapacity() > 0)
							&&(((AmmunitionWeapon)I).ammunitionRemaining() < ((AmmunitionWeapon)I).ammunitionCapacity()))
							{
								// found one to load!
								for(Enumeration<Item> i2=mob.items();i2.hasMoreElements();)
								{
									Item I2=i2.nextElement();
									if((I2 instanceof Ammunition)
									&&(I2.container()==null)
									&&(((Ammunition)I2).ammunitionType().equals(((AmmunitionWeapon)I).ammunitionType())))
									{
										mob.enqueCommand(new XVector<String>("LOAD",I2.Name(),mobRoom.getContextName(I)), 0, 0);
										return true;
									}
								}
								for(Enumeration<Item> i2=mobRoom.items();i2.hasMoreElements();)
								{
									Item I2=i2.nextElement();
									if((I2 instanceof Ammunition)
									&&(I2.container()==null)
									&&(((Ammunition)I2).ammunitionType().equals(((AmmunitionWeapon)I).ammunitionType())))
									{
										mob.enqueCommand(new XVector<String>("GET","ALL",I2.Name()), 0, 0);
										return true;
									}
								}
							}
						}
						
						int targetSpeed = CMLib.tracking().getSailingShipSpeed((Item)targetShipItem);
						if(targetSpeed == 0)
							targetSpeed = 1;
						for(Enumeration<Item> i=mobRoom.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if(CMLib.combat().isAShipSiegeWeapon(I)
							&&(I instanceof AmmunitionWeapon)
							&&(((AmmunitionWeapon)I).ammunitionRemaining() >= ((AmmunitionWeapon)I).ammunitionCapacity())
							&&(distanceToTarget >= ((AmmunitionWeapon)I).minRange())
							&&(distanceToTarget <= ((AmmunitionWeapon)I).maxRange()))
							{
								int aimPt = CMLib.dice().roll(1, targetSpeed, -1);
								mob.enqueCommand(new XVector<String>("AIM",mobRoom.getContextName(I),""+aimPt), 0, 0);
							}
						}
						
						if(CMLib.flags().isMobile(mob) && (distanceToTarget==0))
						{
							for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
							{
								Item I=i.nextElement();
								if((I!=null)
								&&(I.ID().endsWith("Grapples"))
								&&(I.container()==null))
									mob.enqueCommand(new XVector<String>("HOLD",I.Name()), 0, 0);
							}
							final Item heldI=mob.fetchHeldItem();
							if((heldI != null)&&(heldI.ID().endsWith("Grapples")))
								mob.enqueCommand(new XVector<String>("THROW",heldI.Name(),this.targetShipItem.Name()), 0, 0);
							for(Enumeration<Item> i=mobRoom.items();i.hasMoreElements();)
							{
								Item I=i.nextElement();
								if((I!=null)
								&&(I.ID().endsWith("Grapples"))
								&&(I instanceof Exit)
								&&(((Exit)I).lastRoomUsedFrom(mobRoom)!=null))
									mob.enqueCommand(new XVector<String>("ENTER",mobRoom.getContextName(I)), 0, 0);
							}
						}
					}
					if((combatMover)
					&&((!(loyalShipItem instanceof PrivateProperty))
						||(((PrivateProperty)loyalShipItem).getOwnerName().length()==0)
						||CMLib.law().doesOwnThisProperty(mob, (PrivateProperty)loyalShipItem))
					&&(canMoveShip())
					)
					{
						int ourSpeed = CMLib.tracking().getSailingShipSpeed(loyalShipItem);
						if(ourSpeed == 0)
							ourSpeed = 1;
						ourSpeed=CMLib.dice().roll(1, ourSpeed, 0);
						XVector<String> course=new XVector<String>("COURSE");
						String lastDir=loyalShipItem.getStat("DIRECTIONFACING");
						if((lastDir==null)||(lastDir.length()==0))
							lastDir = CMLib.directions().getDirectionName(CMLib.dice().roll(1, 4, -1));
						String directionToTarget = loyalShipItem.getStat("DIRECTIONTOTARGET");
						if(this.amInTrouble() && (ourSpeed >0))
						{
							final int dirToTarget=CMLib.directions().getDirectionCode(directionToTarget);
							final int escapeDir = this.getEscapeRoute(dirToTarget);
							if(escapeDir >=0)
							{
								for(int i=0;i<ourSpeed;i++)
								{
									course.add(CMLib.directions().getDirectionName(escapeDir));
								}
								ourSpeed = 0;
							}
						}
						while(ourSpeed > 0)
						{
							if(ourSpeed == 1)
							{
								if((distanceToTarget > 5)&&(directionToTarget.length()>0))
									course.add(directionToTarget);
								else
								if((CMLib.dice().rollPercentage()<30)&&(directionToTarget.length()>0))
									course.add(directionToTarget);
								else
									course.add(lastDir);
							}
							else
								course.add(lastDir);
							ourSpeed--;
						}
						mob.enqueCommand(course, 0, 0);
					}
				}
				else
				{
					if(loyalShipItem.subjectToWearAndTear()
					&&(loyalShipItem.usesRemaining() < 100))
					{
						tryMend(mob);
					}
					if(combatIsOver && CMLib.flags().isMobile(mob))
					{
						combatIsOver = false;
						if(combatMover && (!peaceMover))
						{
							mob.enqueCommand(new XVector<String>("COURSE"), 0, 0);
							mob.enqueCommand(new XVector<String>("LOWER","ANCHOR"), 0, 0);
						}
						for(Enumeration<Item> i=mobRoom.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if((I!=null)&&(I.ID().endsWith("Grapples")))
								mob.enqueCommand(new XVector<String>("GET",mobRoom.getContextName(I)), 0, 0);
						}
					}
					final Room shipRoom=CMLib.map().roomLocation(loyalShipItem);
					if((aggressive)&&(shipRoom!=null))
					{
						for(Enumeration<Item> i=shipRoom.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if((I!=null)
							&&(I.container()==null)
							&&(I!=loyalShipItem)
							&&((I instanceof BoardableShip)
								||((I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER))))
							{
								boolean hasPlayer = false;
								if(!aggrMobs)
								{
									if(I instanceof Rideable)
									{
										for(Enumeration<Rider> r=((Rideable)I).riders();r.hasMoreElements();)
										{
											Rider R=r.nextElement();
											if((R instanceof MOB)&&(((MOB)R).isPlayer()))
												hasPlayer=true;
										}
									}
									if(I instanceof BoardableShip)
									{
										Area shipArea=((BoardableShip)I).getShipArea();
										if(shipArea!=null)
										{
											for(Enumeration<Room> r=shipArea.getProperMap();r.hasMoreElements();)
											{
												Room R=r.nextElement();
												if(R!=null)
												{
													for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
													{
														final MOB M=m.nextElement();
														if((M!=null)&&(M.isPlayer()))
															hasPlayer=true;
													}
												}
											}
										}
									}
								}
								if(aggrMobs || hasPlayer)
								{
									mob.enqueCommand(new XVector<String>("TARGET",mobRoom.getContextName(I)), 0, 0);
								}
							}
						}
					}
					if((peaceMover)
					&&((!(loyalShipItem instanceof PrivateProperty))
						||(((PrivateProperty)loyalShipItem).getOwnerName().length()==0)
						||CMLib.law().doesOwnThisProperty(mob, (PrivateProperty)loyalShipItem))
					&&(canMoveShip())
					)
					{
						int ourSpeed = CMLib.tracking().getSailingShipSpeed(loyalShipItem);
						if(ourSpeed == 0)
							ourSpeed = 1;
						ourSpeed=CMLib.dice().roll(1, ourSpeed, 0);
						XVector<String> course=new XVector<String>("COURSE");
						String lastDir = CMLib.directions().getDirectionName(CMLib.dice().roll(1, 4, -1));
						while(ourSpeed > 0)
						{
							if(ourSpeed == 1)
							{
								if(CMLib.dice().rollPercentage()<30)
									course.add(CMLib.directions().getDirectionName(CMLib.dice().roll(1, 4, -1)));
								else
									course.add(lastDir);
							}
							else
								course.add(lastDir);
							ourSpeed--;
						}
						mob.enqueCommand(course, 0, 0);
					}
				}
			}
		}
		return true;
	}
}
