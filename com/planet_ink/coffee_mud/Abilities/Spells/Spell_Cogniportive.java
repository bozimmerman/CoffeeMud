package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2007 Bo Zimmerman

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
public class Spell_Cogniportive extends Spell
{
	public String ID() { return "Spell_Cogniportive"; }
	public String name(){return "Cogniportive";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public String establishHome(MOB mob, Item me)
	{
		if(me instanceof LandTitle)
			return ((Room)((LandTitle)me).getPropertyRooms().firstElement()).roomID();
		// check mobs worn items first!
	    try
	    {
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)
						&&(M.isMonster())
						&&(!(M instanceof ShopKeeper))
						&&(M.fetchInventory(me.Name())!=null)
						&&(!M.fetchInventory(me.Name()).amWearingAt(Item.IN_INVENTORY))
                        &&(CMLib.law().getLandTitle(R)==null))
							return CMLib.map().getExtendedRoomID(M.getStartRoom());
					}
				}
			}
	    }catch(NoSuchElementException nse){}
	    try
	    {
			// check shopkeepers second!
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)&&(CMLib.coffeeShops().getShopKeeper(M)!=null))
						{
							ShopKeeper S=CMLib.coffeeShops().getShopKeeper(M);
							if((S.getShop().doIHaveThisInStock(me.Name(),null,S.whatIsSold(),M.getStartRoom()))
                            &&(CMLib.law().getLandTitle(R)==null))
								return CMLib.map().getExtendedRoomID(M.getStartRoom());
						}
					}
				}
			}
	    }catch(NoSuchElementException nse){}
	    try
	    {
			// check mobs inventory items third!
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)
						&&(M.isMonster())
						&&(!(M instanceof ShopKeeper))
						&&(M.fetchInventory(me.Name())!=null)
						&&(M.fetchInventory(me.Name()).amWearingAt(Item.IN_INVENTORY))
                        &&(CMLib.law().getLandTitle(R)==null))
							return CMLib.map().getExtendedRoomID(M.getStartRoom());
					}
				}
			}
	    }catch(NoSuchElementException nse){}
	    try
	    {
			// check room stuff last
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((CMLib.flags().canAccess(mob,R))
                &&(R.fetchItem(null,me.Name())!=null)
                &&(CMLib.law().getLandTitle(R)==null))
				   return CMLib.map().getExtendedRoomID(R);
			}
	    }catch(NoSuchElementException nse){}
		return "";
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   Item me)
	{
		if((mob!=null)
		   &&(mob.isMine(me))
		   &&(mob.location()!=null)
		   &&(me!=null))
		{
			if(text().length()==0)
				setMiscText(establishHome(mob,me));
			Room home=CMLib.map().getRoom(text());
			if((home==null)||(!CMLib.flags().canAccess(mob,home)))
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Strange fizzled sparks fly from "+me.name()+".");
			else
			{
				HashSet h=properTargets(mob,null,false);
				if(h==null) return;

				Room thisRoom=mob.location();
				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();
					CMMsg enterMsg=CMClass.getMsg(follower,home,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke.");
					CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
					if(thisRoom.isInhabitant(follower)
                    &&thisRoom.okMessage(follower,leaveMsg)
                    &&(!home.isInhabitant(follower))
                    &&home.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CMLib.commands().postFlee(follower,("NOWHERE"));
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						home.bringMobHere(follower,false);
						home.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						CMLib.commands().postLook(follower,true);
					}
				}
			}
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();

		if(affected instanceof Item)
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(affected))
				waveIfAble(mob,msg.tool(),(Item)affected);
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.sourceMessage()!=null))
			{
				String msgStr=CMStrings.getSayFromMessage(msg.sourceMessage());
				if(msgStr!=null)
				{
					Vector V=CMParms.parse(msgStr);
					if((V.size()>=2)
					&&(((String)V.firstElement()).equalsIgnoreCase("HOME")))
					{
						String str=CMParms.combine(V,1);
						if((str.length()>0)
						&&((CMLib.english().containsString(affected.name(),str)
								||CMLib.english().containsString(affected.displayText(),str))))
							msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,msg.sourceMessage(),CMMsg.NO_EFFECT,null));
					}
				}
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORNREQ_ANY);
		if(target==null)
		{
			String str=CMParms.combine(commands,0).toUpperCase();
			if(str.equals("MONEY")||str.equals("GOLD")||str.equals("COINS"))
				mob.tell("You can't cast this spell on coins!");
			return false;
		}

		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			mob.tell(target.name()+" is already cogniportive!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> glow(s) softly!");
				beneficialAffect(mob,target,asLevel,1000);
				A=target.fetchEffect(ID());
				if(A!=null)
					A.setMiscText(((Spell_Cogniportive)A).establishHome(mob,target));
				target.recoverEnvStats();
				mob.recoverEnvStats();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}
