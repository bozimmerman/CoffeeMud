package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Mike Rundell

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
public class MOBEater extends ActiveTicker
{
	public String ID(){return "MOBEater";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private Room Stomach = null;
	private int digestDown=4;
	private Room lastKnownLocation=null;

	public MOBEater()
	{
		minTicks=4; maxTicks=8; chance=50;
		tickReset();
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
			Stomach.addNonUninvokableEffect(CMClass.getAbility("Prop_NoRecall"));
		}
	}

	public void kill()
	{
		if(lastKnownLocation==null) return;
		if(Stomach==null) return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		Vector these=new Vector();
		for (int x=0;x<Stomach.numInhabitants();x++)
		{
			// ===== get the tasty morsels
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if(TastyMorsel!=null)
				these.addElement(TastyMorsel);
		}

		// =====move the inventory of the stomach to the room
		for (int y=0;y<Stomach.numItems();y++)
		{
			Item PartiallyDigestedItem = Stomach.fetchItem(y);
			if((PartiallyDigestedItem!=null)&&(PartiallyDigestedItem.container()==null))
				these.addElement(PartiallyDigestedItem);
		}
		for(int i=0;i<these.size();i++)
		{
			if(these.elementAt(i) instanceof Item)
				lastKnownLocation.bringItemHere((Item)these.elementAt(i),Item.REFUSE_PLAYER_DROP);
			else
			if(these.elementAt(i) instanceof MOB)
				lastKnownLocation.bringMobHere((MOB)these.elementAt(i),false);
		}
		Stomach.recoverEnvStats();
		lastKnownLocation.recoverRoomStats();
		lastKnownLocation=null;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) return true;
		if(!(ticking instanceof MOB)) return true;

		MOB mob=(MOB)ticking;
		if(mob.location()!=null)
			lastKnownLocation=mob.location();

		if((--digestDown)<=0)
		{
			digestDown=4;
			digestTastyMorsels(mob);
		}

		if((canAct(ticking,tickID))
		&&(((MOB)ticking).isInCombat())
		&&(!mob.amDead()))
			trySwallowWhole(mob);
		return true;
	}
	public void executeMsg(Environmental mob, CMMsg msg)
	{
		if((mob instanceof MOB)
		&&(msg.amISource((MOB)mob))
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			kill();
		super.executeMsg(mob,msg);
	}
	protected boolean trySwallowWhole(MOB mob)
	{
		if(Stomach==null) return true;
		if (Sense.aliveAwakeMobile(mob,true)
			&&(mob.rangeToTarget()==0)
			&&(Sense.canHear(mob)||Sense.canSee(mob)||Sense.canSmell(mob)))
		{
			MOB TastyMorsel = mob.getVictim();
			if(TastyMorsel==null) return true;
			if (TastyMorsel.envStats().weight()<(mob.envStats().weight()/2))
			{
				// ===== The player has been eaten.
				// ===== move the tasty morsel to the stomach
				FullMsg EatMsg=new FullMsg(mob,
										   TastyMorsel,
										   null,
										   CMMsg.MSG_OK_ACTION,
										   "<S-NAME> swallow(es) <T-NAMESELF> WHOLE!");
				if(mob.location().okMessage(TastyMorsel,EatMsg))
				{
					mob.location().send(TastyMorsel,EatMsg);
					Stomach.bringMobHere(TastyMorsel,false);
					FullMsg enterMsg=new FullMsg(TastyMorsel,Stomach,null,CMMsg.MSG_ENTER,Stomach.description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> slide(s) down the gullet into the stomach!");
					Stomach.send(TastyMorsel,enterMsg);
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
										   CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,
										   "<S-NAME> digest(s) <T-NAMESELF>!!");
				// no OKaffectS, since the dragon is not in his own stomach.
				Stomach.send(mob,DigestMsg);
				int damage=(int)Math.round(Util.div(TastyMorsel.curState().getHitPoints(),2));
				if(damage<(TastyMorsel.envStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()+1;
				MUDFight.postDamage(mob,TastyMorsel,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The stomach acid <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}
}
