package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Cogniportive extends Spell
{
	public String ID() { return "Spell_Cogniportive"; }
	public String name(){return "Cogniportive";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Cogniportive();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public String establishHome(MOB mob, Item me)
	{
		if(me instanceof LandTitle)
			return ((LandTitle)me).landRoomID();
		else
		{
			// check mobs worn items first!
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(Sense.canAccess(mob,R))
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)
						&&(M.isMonster())
						&&(!(M instanceof ShopKeeper))
						&&(M.fetchInventory(me.Name())!=null)
						&&(!M.fetchInventory(me.Name()).amWearingAt(Item.INVENTORY)))
							return CMMap.getExtendedRoomID(M.getStartRoom());
					}
				}
			}
			// check shopkeepers second!
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(Sense.canAccess(mob,R))
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)&&(CoffeeUtensils.getShopKeeper(M)!=null))
						{
							ShopKeeper S=CoffeeUtensils.getShopKeeper(M);
							if(S.doIHaveThisInStock(me.Name(),null))
								return CMMap.getExtendedRoomID(M.getStartRoom());
						}
					}
				}
			}
			// check mobs inventory items third!
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(Sense.canAccess(mob,R))
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)
						&&(M.isMonster())
						&&(!(M instanceof ShopKeeper))
						&&(M.fetchInventory(me.Name())!=null)
						&&(M.fetchInventory(me.Name()).amWearingAt(Item.INVENTORY)))
							return CMMap.getExtendedRoomID(M.getStartRoom());
					}
				}
			}
			// check room stuff last
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((Sense.canAccess(mob,R))&&(R.fetchItem(null,me.Name())!=null))
				   return CMMap.getExtendedRoomID(R);
			}
		}
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
			Environmental target=null;
			if((mob.location()!=null))
				target=afftarget;
			Room home=CMMap.getRoom(text());
			if(home==null)
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Strange fizzled sparks fly from "+me.name()+".");
			else
			{
				Hashtable h=properTargets(mob,null,false);
				if(h==null) return;

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					FullMsg enterMsg=new FullMsg(follower,home,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
					if(thisRoom.okMessage(follower,leaveMsg)&&home.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							ExternalPlay.flee(follower,"NOWHERE");
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						home.bringMobHere(follower,false);
						home.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						ExternalPlay.look(follower,null,true);
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
			if(msg.sourceMinor()==CMMsg.TYP_SPEAK)
			{
				String msgStr=msg.sourceMessage();
				int x=msgStr.indexOf("'");
				if(x<0) break;
				msgStr=msgStr.substring(x+1);
				x=msgStr.lastIndexOf("'");
				if(x<0) break;
				msgStr=msgStr.substring(0,x);
				Vector V=Util.parse(msgStr);
				if(V.size()<2) break;
				String str=(String)V.firstElement();
				if(!str.equalsIgnoreCase("HOME")) break;
				str=Util.combine(V,1);
				if(CoffeeUtensils.containsString(affected.name(),str)
				||CoffeeUtensils.containsString(affected.displayText(),str))
					msg.addTrailerMsg(new FullMsg(msg.source(),affected,msg.target(),msg.NO_EFFECT,null,CMMsg.MASK_GENERAL|CMMsg.TYP_WAND_USE,msg.sourceMessage(),msg.NO_EFFECT,null));
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null)
		{
			String str=Util.combine(commands,0).toUpperCase();
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

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> glow(s) softly!");
				beneficialAffect(mob,target,1000);
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