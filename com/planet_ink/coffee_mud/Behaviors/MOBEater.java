package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MOBEater extends ActiveTicker
{
	private Room Stomach = null;
	private int swallowDown=5;
	private int digestDown=4;
	private Room lastKnownLocation=null;

	public MOBEater()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=10; maxTicks=30; chance=25;
		tickReset();
		canImproveCode=Behavior.CAN_MOBS;
	}

	public Behavior newInstance()
	{
		return new MOBEater();
	}

	public void startBehavior(Environmental forMe)
	{
		Stomach = CMClass.getLocale("StdRoom");
		if((forMe!=null)&&(forMe instanceof MOB))
		{
			lastKnownLocation=((MOB)forMe).location();
			if(lastKnownLocation!=null)
				Stomach.setArea(lastKnownLocation.getArea());
			Stomach.setName("The Stomach of "+forMe.name());
			Stomach.setDescription("You are in the stomach of "+forMe.name()+".  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.");
		}
	}

	public void kill()
	{
		if(lastKnownLocation==null) return;
		if(Stomach==null) return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach.numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if(TastyMorsel!=null)
				lastKnownLocation.bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		int itemCount = Stomach.numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			Item PartiallyDigestedItem = Stomach.fetchItem(y);
			if (PartiallyDigestedItem!=null)
			{
				lastKnownLocation.addItemRefuse(PartiallyDigestedItem,Item.REFUSE_PLAYER_DROP);
				Stomach.delItem(PartiallyDigestedItem);
			}
		}
		if((morselCount>0)||(itemCount>0))
		{
			lastKnownLocation.recoverRoomStats();
		}
		lastKnownLocation=null;
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;

		MOB mob=(MOB)ticking;
		if((canAct(ticking,tickID))&&(ticking instanceof MOB)&&(((MOB)ticking).isInCombat()))
		{
			if(mob.location()!=null)
				lastKnownLocation=mob.location();
			if((!mob.amDead())&&(tickID==Host.MOB_TICK))
			{
				if((--swallowDown)<=0)
				{
					swallowDown=2;
					digestTastyMorsels(mob);
				}
				if((--digestDown)<=0)
				{
					digestDown=4;
					trySwallowWhole(mob);
				}
			}
		}
		if(mob.amDead())
			kill();
	}
	protected boolean trySwallowWhole(MOB mob)
	{
		if(Stomach==null) return true;
		if (Sense.aliveAwakeMobile(mob,true)&&
			(Sense.canHear(mob)||Sense.canSee(mob)||Sense.canSmell(mob)))
		{
			MOB TastyMorsel = mob.getVictim();
			if(TastyMorsel==null) return true;
			if (TastyMorsel.envStats().weight()<(mob.envStats().weight()/2))
			{
				// ===== if it is less than three so roll for it
				// ===== check the result
				if (Dice.rollPercentage()<5)
				{
					// ===== The player has been eaten.
					// ===== move the tasty morsel to the stomach
					FullMsg EatMsg=new FullMsg(mob,
											   TastyMorsel,
											   null,
											   Affect.MSG_OK_ACTION,
											   "<S-NAME> swallow(es) <T-NAMESELF> WHOLE!");
					if(mob.location().okAffect(EatMsg))
					{
						mob.location().send(TastyMorsel,EatMsg);
						Stomach.bringMobHere(TastyMorsel,false);
						FullMsg enterMsg=new FullMsg(TastyMorsel,Stomach,null,Affect.MSG_ENTER,Stomach.description(),Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> Slides down the gullet into the stomach!");
						Stomach.send(TastyMorsel,enterMsg);
					}
				}
			}
		}
		return true;
	}

	protected boolean digestTastyMorsels(MOB mob)
	{
		if(Stomach==null) return true;
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach.numInhabitants();
		for (int x=0;x<morselCount;x++)
		{
			// ===== get a tasty morsel
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if (TastyMorsel != null)
			{
				FullMsg DigestMsg=new FullMsg(mob,
										   TastyMorsel,
										   null,
										   Affect.MSK_MALICIOUS_MOVE|Affect.TYP_ACID,
										   "<S-NAME> Digests <T-NAMESELF>!!");
				// no OKaffectS, since the dragon is not in his own stomach.
				Stomach.send(mob,DigestMsg);
				int damage=(int)Math.round(Util.div(TastyMorsel.curState().getHitPoints(),2));
				ExternalPlay.postDamage(mob,TastyMorsel,null,damage,Affect.ACT_GENERAL|Affect.TYP_ACID,Weapon.TYPE_BURNING,"The stomach acid <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}
}