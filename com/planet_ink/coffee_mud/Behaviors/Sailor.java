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
		return "Aggressive";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
	}

	public Sailor()
	{
		// TODO Auto-generated constructor stub
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
		combatMover = CMParms.getParmBool(newParms, "FIGHTMOVER", true);
		combatTech = CMParms.getParmBool(newParms, "FIGHTTECH", true);
		aggressive = CMParms.getParmBool(newParms, "AGGRO", true);
		loyalShipArea	= null;
		loyalShipItem	= null;
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
				//targetShipDist = -1;
			}
			else
			if(msg.source().riding() == this.loyalShipItem)
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
				A.setProficiency((tickWait+1) * 4 * 3);
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
				tickWait = ((Physical)ticking).phyStats().level() / 4;
			else
				tickWait = 10000;
		}
		
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			if((ticking instanceof MOB)
			&&(!CMLib.flags().canFreelyBehaveNormal(ticking)))
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
				if((loyalShipArea!=null)
				&&(mobRoom.getArea() != loyalShipArea))
					return true;
				
				if(loyalShipItem==null)
					return true;
				
				if((targetShipItem != null)
				&&(CMLib.map().roomLocation(targetShipItem)!=CMLib.map().roomLocation(loyalShipItem)))
				{
					combatIsOver=true;
					targetShipItem = null;
					//stop combat signal
				}
				
				if(targetShipItem != null)
				{
					if(combatTech)
					{
						if(loyalShipItem.subjectToWearAndTear()
						&&(CMLib.dice().rollPercentage() >= loyalShipItem.usesRemaining())
						&&(CMLib.dice().rollPercentage()<50)
						&&(tryMend(mob)))
							return true;
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
										mob.enqueCommand(new XVector<String>("GET","ALL",I.Name()), 0, 0);
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
							&&(((AmmunitionWeapon)I).ammunitionRemaining() >= ((AmmunitionWeapon)I).ammunitionCapacity()))
							{
								int aimPt = CMLib.dice().roll(1, targetSpeed, -1);
								mob.enqueCommand(new XVector<String>("AIM",mobRoom.getContextName(I),""+aimPt), 0, 0);
							}
						}
						
						if(CMLib.flags().isMobile(mob))
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
						||CMLib.law().doesOwnThisProperty(mob, (PrivateProperty)loyalShipItem)))
					{
						int ourSpeed = CMLib.tracking().getSailingShipSpeed(loyalShipItem);
						if(ourSpeed == 0)
							ourSpeed = 1;
						XVector<String> course=new XVector<String>("COURSE");
						String lastDir = CMLib.directions().getDirectionName(CMLib.dice().roll(1, 4, -1));
						while(ourSpeed > 0)
						{
							if(ourSpeed == 1)
							{
								if(CMLib.dice().rollPercentage()<40)
									course.add(CMLib.directions().getDirectionName(CMLib.dice().roll(1, 4, -1)));
								else
									course.add(lastDir);
							}
							else
								course.add(lastDir);
							ourSpeed--;
						}
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
						for(Enumeration<Item> i=mobRoom.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if((I!=null)&&(I.ID().endsWith("Grapples")))
								mob.enqueCommand(new XVector<String>("GET",mobRoom.getContextName(I)), 0, 0);
						}
					}
				}
			}
		}
		return true;
	}
}
