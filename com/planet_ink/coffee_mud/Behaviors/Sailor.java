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

public class Sailor extends StdBehavior
{

	@Override
	public String ID()
	{
		return "Sailor";
	}

	public Sailor()
	{
	}

	protected volatile int	tickDown		= -1;
	protected int			tickWait		= -1;
	protected int			tickBonus		= 0;
	protected BoardableShip	loyalShipArea	= null;
	protected Item			loyalShipItem	= null;
	protected Rideable		targetShipItem	= null;
	protected boolean		combatIsOver	= false;
	protected boolean		peaceMover		= false;
	protected boolean		combatMover		= true;
	protected boolean		combatTech		= true;
	protected boolean		boarder			= false;
	protected boolean		defender		= false;
	protected boolean		aggressive		= false;
	protected boolean		aggrMobs		= false;
	protected boolean		aggrLvlChk		= false;
	protected CompiledZMask aggrMask		= null;
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
	public long flags()
	{
		if(boarder)
			return Behavior.FLAG_MOBILITY | Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
		else
		if(defender)
			return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
		else
			return 0;
	}

	public Item getShip(final MOB M)
	{
		if(M.getStartRoom() instanceof BoardableShip)
		{
			final Item hisShip=((BoardableShip)M.getStartRoom()).getShipItem();
			if(hisShip != null)
				return hisShip;
		}
		if(M.amFollowing()!=null)
		{
			final Item folship = getShip(M.amFollowing());
			if(folship != null)
				return folship;
		}
		if(M.isPlayer())
		{
			final Room R=M.location();
			if((R!=null)&&(R.getArea() instanceof BoardableShip))
			{
				final Item shipI=((BoardableShip)R.getArea()).getShipItem();
				if((shipI!=null)&&(CMLib.law().doesHaveWeakPriviledgesHere(M, M.location())))
					return shipI;
				final Room shipR=CMLib.map().roomLocation(shipI);
				if(shipR!=null)
				{
					for(Enumeration<Item> i=shipR.items();i.hasMoreElements();)
					{
						Item I=i.nextElement();
						if((I instanceof BoardableShip)&&(I!=shipI))
						{
							final Item otherShipI=((BoardableShip)I).getShipItem();
							final Area otherShipA=((BoardableShip)I).getShipArea();
							if((otherShipI!=null)
							&&(otherShipA!=null)
							&&(CMLib.law().doesHaveWeakPriviledgesHere(M, otherShipA.getRandomProperRoom())))
								return otherShipI;
						}
					}
				}
			}
		}
		return null;
	}

	public boolean amOnMyShip(final MOB M)
	{
		final Room R=M.location();
		if((R!=null)&&(R.getArea() instanceof BoardableShip))
			return (((BoardableShip)R.getArea()).getShipItem() == loyalShipItem);
		return false;
	}

	public boolean isMyShipInCombat(final MOB M)
	{
		if(loyalShipItem instanceof SailingShip)
		{
			return ((SailingShip)loyalShipItem).isInCombat();
		}
		return false;
	}

	@Override
	public boolean grantsAggressivenessTo(final MOB M)
	{
		if(boarder || defender)
		{
			final Item hisShipI=getShip(M);
			if((hisShipI == this.loyalShipItem)||(this.loyalShipItem==null)||(hisShipI==null))
				return false;
			if(this.loyalShipItem instanceof SailingShip)
			{
				final SailingShip myShip=(SailingShip)this.loyalShipItem;
				final ItemTicker I1=(ItemTicker)myShip.fetchEffect("ItemRejuv");
				final ItemTicker I2=(ItemTicker)hisShipI.fetchEffect("ItemRejuv");
				if((I1!=null)
				&&(I2!=null)
				&&(I1.properLocation()==I2.properLocation()))
					return false;
				final PhysicalAgent myCombatTarget=myShip.getCombatant();
				if((myCombatTarget != null)
				&&(myCombatTarget == hisShipI))
					return true;
			}
			if(hisShipI instanceof SailingShip)
			{
				final PhysicalAgent hisCombatTarget=((SailingShip)hisShipI).getCombatant();
				if((hisCombatTarget != null)
				&&(hisCombatTarget == this.loyalShipItem))
					return true;
			}
			if(M.amFollowing()!=null)
				return grantsAggressivenessTo(M.amFollowing());
		}
		return false;
	}
	
	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait = CMParms.getParmInt(newParms, "TICKDELAY", -1);
		tickBonus = CMParms.getParmInt(newParms, "TICKBONUS", 0);
		peaceMover = CMParms.getParmBool(newParms, "PEACEMOVER", false);
		areaOnly = CMParms.getParmBool(newParms, "AREAONLY", true);
		combatMover = CMParms.getParmBool(newParms, "FIGHTMOVER", true);
		combatTech = CMParms.getParmBool(newParms, "FIGHTTECH", true);
		boarder = CMParms.getParmBool(newParms, "BOARDER", false);
		defender = CMParms.getParmBool(newParms, "DEFENDER", false);
		aggressive = CMParms.getParmBool(newParms, "AGGRO", false);
		aggrMobs = CMParms.getParmBool(newParms, "AGGROMOBS", false);
		aggrLvlChk = CMParms.getParmBool(newParms, "AGGROLEVELCHECK", false);
		aggrMask = CMLib.masking().maskCompile(CMParms.getParmStr(newParms, "AGGROMASK", ""));
		wimpy = CMParms.getParmBool(newParms, "WIMPY", true);
		loyalShipArea	= null;
		loyalShipItem	= null;
	}
	
	@Override
	public CMObject copyOf()
	{
		Sailor S=(Sailor)super.copyOf();
		S.loyalShipArea=null;
		S.loyalShipItem=null;
		return S;
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
		if(this.loyalShipItem!=null)
		{
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
				if((msg.source()!=null)
				&&(msg.source().riding() == this.loyalShipItem)
				&&(msg.source().isMonster())
				&&(msg.source().Name().equals(this.loyalShipItem.Name())))
				{
					
				}
				break;
			}
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
	
	public boolean tryTrawl(MOB mob)
	{
		if(CMLib.flags().domainAffects(mob, Ability.ACODE_COMMON_SKILL).size()==0)
		{
			Ability A=CMClass.getAbility("Trawling");
			if((A!=null)
			&&((A.proficiency()==0)||(A.proficiency()==100)))
			{
				A.setProficiency((mob.phyStats().level()+1) * 4 * 3);
				if(A.proficiency() >= 100)
					A.setProficiency(99);
			}
			Ability mend=mob.fetchAbility("Trawling");
			if(mend != null)
			{
				mob.enqueCommand(new XVector<String>("TRAWL"), 0, 0);
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
				tickWait = (30 - tickBonus - ((Physical)ticking).phyStats().level() / 4);
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
					if((mob.isMonster())
					&&(mob.amFollowing()==null)
					&&(!CMLib.flags().isAnimalIntelligence(mob)))
					{
						if(mob.getStartRoom()==null)
							mob.setStartRoom(mob.location());
						else
						if((mob.getStartRoom().getArea()!=loyalShipArea)
						&&(mob.getStartRoom().roomID().length()>0)
						&&(mob.isSavable()))
						{
							final MOB newM = (MOB) mob.copyOf();
							newM.basePhyStats().setRejuv(PhyStats.NO_REJUV);
							newM.phyStats().setRejuv(PhyStats.NO_REJUV);
							newM.setStartRoom(mob.location());
							mob.location().addInhabitant(newM);
							mob.delBehavior(this);
							newM.delBehavior(newM.fetchBehavior(ID()));
							this.tickDown=0;
							newM.addBehavior(this);
							newM.text();
							mob.killMeDead(false);
							return true;
						}
					}
				}

				if(loyalShipItem==null)
					return true;

				boolean amIInCombat = mob.isInCombat();
				if((defender || boarder)&&(!amIInCombat))
				{
					if(this.isMyShipInCombat(mob))
					{
						if(mobRoom.numInhabitants()>1)
						{
							for(Enumeration<MOB> m=mobRoom.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if((M!=null)
								&&(M!=mob)
								&&(mob.mayPhysicallyAttack(M))
								&&(grantsAggressivenessTo(M))
								&&((!aggrLvlChk)||(mob.phyStats().level()<(M.phyStats().level()+5)))
								&&(CMLib.masking().maskCheck(aggrMask,M,false)))
								{
									if(CMLib.combat().postAttack(mob, M, mob.fetchWieldedItem()))
									{
										amIInCombat=true;
										break;
									}
								}
							}
						}

						if(boarder && (!amIInCombat) && (!this.amOnMyShip(mob)))
						{
							CMLib.tracking().beMobile(mob, true, false, false, false, null, null);
						}
					}
					else
					if(!amOnMyShip(mob))
					{
						if((mob.getStartRoom() != null)&&(mob.getStartRoom().getArea() instanceof BoardableShip))
						{
							CMLib.tracking().wanderAway(mob, false, true);
						}
					}
				}
				
				if(mobRoom.getArea() != loyalShipArea)
					return true;

				if((targetShipItem!=null) 
				&&CMLib.map().roomLocation(targetShipItem)!=CMLib.map().roomLocation(loyalShipItem))
				{
					combatIsOver=true;
					targetShipItem = null;
					//stop combat signal
				}

				if((loyalShipItem instanceof SailingShip) 
				&& (((combatMover && (targetShipItem != null)) || peaceMover)))
				{
					if((((SailingShip)loyalShipItem).isAnchorDown())
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

				if((targetShipItem != null)&&(loyalShipItem instanceof SailingShip))
				{
					final SailingShip sailShip=(SailingShip)loyalShipItem;
					int distanceToTarget= sailShip.rangeToTarget();
					if(loyalShipItem.subjectToWearAndTear()
					&&(CMLib.dice().rollPercentage() >= loyalShipItem.usesRemaining())
					&&(CMLib.dice().rollPercentage()<50)
					&&(tryMend(mob)))
						return true;
					if(combatTech)
					{
						boolean roomHasWeapons = false;
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
										mob.enqueCommand(new XVector<String>("GET",""+((AmmunitionWeapon)I).ammunitionCapacity(),I2.Name()), 0, 0);
										return true;
									}
								}
							}
						}
						
						int targetSpeed = (targetShipItem instanceof SailingShip)?((SailingShip)targetShipItem).getShipSpeed():1;
						if(targetSpeed == 0)
							targetSpeed = 1;
						final PairList<Weapon,int[]> aimings = sailShip.getSiegeWeaponAimings();
						for(Enumeration<Item> i=mobRoom.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if(CMLib.combat().isAShipSiegeWeapon(I))
							{
								roomHasWeapons = true;
								if((I instanceof AmmunitionWeapon)
								&&(!aimings.containsFirst((Weapon)I))
								&&(((AmmunitionWeapon)I).ammunitionRemaining() >= ((AmmunitionWeapon)I).ammunitionCapacity())
								&&(distanceToTarget >= ((AmmunitionWeapon)I).minRange())
								&&(distanceToTarget <= ((AmmunitionWeapon)I).maxRange()))
								{
									int aimPt = CMLib.dice().roll(1, targetSpeed, -1);
									mob.enqueCommand(new XVector<String>("AIM",mobRoom.getContextName(I),""+aimPt), 0, 0);
									return true;
								}
							}
						}
						
						boolean isSecondFiddle=false;
						if(roomHasWeapons)
						{
							for(Enumeration<MOB> m=mobRoom.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if(M==mob)
									break;
								if(M!=null)
								{
									final Sailor S=(Sailor)M.fetchBehavior(ID());
									if((S!=null)&&(S.combatTech)&&(S.loyalShipItem==loyalShipItem))
									{
										isSecondFiddle=true;
										break;
									}
								}
							}
						}
						if(isSecondFiddle || (!roomHasWeapons))
						{
							final List<Integer> choices=new ArrayList<Integer>(1);
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								final Room nextR=mobRoom.getRoomInDir(d);
								final Exit nextE=mobRoom.getExitInDir(d);
								if((nextR!=null)&&(nextE!=null)&&(nextE.isOpen()))
								{
									for(Enumeration<Item> i=nextR.items();i.hasMoreElements();)
									{
										Item I=i.nextElement();
										if(CMLib.combat().isAShipSiegeWeapon(I))
										{
											choices.add(Integer.valueOf(d));
										}
									}
								}
							}
							if(choices.size()>0)
								CMLib.tracking().walk(mob, choices.get(CMLib.dice().roll(1, choices.size(), -1)).intValue(), false, false);
							else
							if(!roomHasWeapons)
							{
								CMLib.tracking().beMobile(mob, true, false, false, false, null, null);
							}
						}
					}
					if(defender && ((mobRoom.domainType() & Room.INDOORS) != 0))
					{
						CMLib.tracking().beMobile(mob, true, false, false, false, null, null);
					}
					
					if((boarder || CMLib.flags().isMobile(mob)))
					{
						if (distanceToTarget==0)
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
						
						if(boarder)
						{
							CMLib.tracking().beMobile(mob, true, false, false, false, null, null);
						}
					}
					if((combatMover)
					&&((!(loyalShipItem instanceof PrivateProperty))
						||(((PrivateProperty)loyalShipItem).getOwnerName().length()==0)
						||CMLib.law().doesOwnThisProperty(mob, (PrivateProperty)loyalShipItem))
					&&(loyalShipItem instanceof SailingShip)
					&&(canMoveShip())
					)
					{
						int ourSpeed = ((SailingShip)loyalShipItem).getShipSpeed();
						if(ourSpeed == 0)
							ourSpeed = 1;
						ourSpeed=CMLib.dice().roll(1, ourSpeed, 0);
						XVector<String> course=new XVector<String>("COURSE");
						int lastDirI = ((SailingShip)loyalShipItem).getDirectionFacing();
						String lastDir=(lastDirI>=0)?CMLib.directions().getDirectionName(lastDirI):"";
						if((lastDir==null)||(lastDir.length()==0))
							lastDir = CMLib.directions().getDirectionName(CMLib.dice().roll(1, 4, -1));
						final int directionToTarget = ((SailingShip)loyalShipItem).getDirectionToTarget();
						if(this.amInTrouble() && (ourSpeed >0))
						{
							final int escapeDir = this.getEscapeRoute(directionToTarget);
							if(escapeDir >=0)
							{
								if((ourSpeed>1)&&(((SailingShip)loyalShipItem).getDirectionFacing() != escapeDir))
									ourSpeed=1;
								for(int i=0;i<ourSpeed;i++)
									course.add(CMLib.directions().getDirectionName(escapeDir));
								ourSpeed = 0;
							}
						}
						while(ourSpeed > 0)
						{
							if(ourSpeed == 1)
							{
								if((distanceToTarget > 5)&&(directionToTarget>=0))
									course.add(CMLib.directions().getDirectionName(directionToTarget));
								else
								if((CMLib.dice().rollPercentage()<30)&&(directionToTarget>=0))
									course.add(CMLib.directions().getDirectionName(directionToTarget));
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
					tryTrawl(mob);
					if(combatIsOver && (boarder || CMLib.flags().isMobile(mob)))
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
								||((I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER)))
							&&(CMLib.flags().canBeSeenBy(I, mob)))
							{
								final LinkedList<MOB> elligible=new LinkedList<MOB>();
								if(I instanceof Rideable)
								{
									for(Enumeration<Rider> r=((Rideable)I).riders();r.hasMoreElements();)
									{
										Rider R=r.nextElement();
										if((R instanceof MOB)
										&&((aggrMobs) || (((MOB)R).isPlayer())))
											elligible.add((MOB)R);
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
													if((M!=null)
													&&((aggrMobs) || M.isPlayer()))
														elligible.add(M);
												}
											}
										}
									}
								}
								if(elligible.size()>0)
								{
									MOB captaiM = null;
									for(final MOB M : elligible)
									{
										if((M.isPlayer())
										&&(CMLib.law().doesHaveWeakPriviledgesHere(M, M.location())))
											captaiM=M;
									}
									if(captaiM == null)
									{
										MOB[] Ms=new MOB[5];
										for(final MOB M : elligible)
										{
											if(!M.isPlayer())
											{
												final Sailor oS=(Sailor)M.fetchBehavior(ID());
												if(oS!=null)
												{
													if(oS.combatMover)
														Ms[0]=M;
													if(oS.peaceMover)
														Ms[1]=M;
													if(oS.combatTech)
														Ms[2]=M;
													if(oS.boarder)
														Ms[3]=M;
													if(oS.defender)
														Ms[4]=M;
												}
											}
										}
										for(int x=0;x<Ms.length;x++)
										{
											if(Ms[x]!=null)
												captaiM=Ms[x];
										}
									}
									if(captaiM == null)
									{
										for(final MOB M : elligible)
										{
											if((!M.isPlayer())
											&&(CMLib.law().doesHaveWeakPriviledgesHere(M, M.location())))
												captaiM=M;
										}
									}
									if(captaiM==null)
									{
										for(final MOB M : elligible)
										{
											if(M.isPlayer())
												captaiM=M;
										}
									}
									if(captaiM==null)
										captaiM=elligible.get(0);
									if((captaiM!=null)
									&&((!aggrLvlChk)||(mob.phyStats().level()<(captaiM.phyStats().level()+5)))
									&&(CMLib.masking().maskCheck(aggrMask,captaiM,false)))
										mob.enqueCommand(new XVector<String>("TARGET",mobRoom.getContextName(I)), 0, 0);
								}
							}
						}
					}
					if((peaceMover)
					&&((!(loyalShipItem instanceof PrivateProperty))
						||(((PrivateProperty)loyalShipItem).getOwnerName().length()==0)
						||CMLib.law().doesOwnThisProperty(mob, (PrivateProperty)loyalShipItem))
					&&(loyalShipItem instanceof SailingShip)
					&&(canMoveShip())
					)
					{
						int ourSpeed = ((SailingShip)loyalShipItem).getShipSpeed();
						if(ourSpeed == 0)
							ourSpeed = 1;
						ourSpeed=CMLib.dice().roll(1, ourSpeed, 0);
						XVector<String> course=new XVector<String>("COURSE");
						Room curRoom=CMLib.map().roomLocation(loyalShipItem);
						Integer lastDir = Integer.valueOf(CMLib.dice().roll(1, 4, -1));
						int tries=99;
						while((ourSpeed > 0)&&(--tries>0) && (curRoom!=null))
						{
							Integer nextDir=lastDir;
							if(ourSpeed == 1)
							{
								if(CMLib.dice().rollPercentage()<30)
									nextDir=Integer.valueOf(CMLib.dice().roll(1, 4, -1));
							}
							if(nextDir != null)
							{
								final Room nextRoom=curRoom.getRoomInDir(nextDir.intValue());
								if((nextRoom == null)
								||((areaOnly)&&(nextRoom.getArea()!=curRoom.getArea())))
									nextDir = null;
							}
							if(nextDir != null)
							{
								course.add(CMLib.directions().getDirectionName(nextDir.intValue()));
								curRoom=curRoom.getRoomInDir(nextDir.intValue());
								ourSpeed--;
							}
							if((tries<20)&&(ourSpeed>1))
								ourSpeed=1;
						}
						mob.enqueCommand(course, 0, 0);
					}
				}
			}
		}
		return true;
	}
}
