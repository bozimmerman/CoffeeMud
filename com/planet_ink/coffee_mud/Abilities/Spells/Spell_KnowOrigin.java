package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_KnowOrigin extends Spell
{
	public String ID() { return "Spell_KnowOrigin"; }
	public String name(){return "Know Origin";}
	public int quality(){return INDIFFERENT;};
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_KnowOrigin();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public Room origin(Environmental meThang)
	{
		if(meThang instanceof LandTitle)
			return CMMap.getRoom(((LandTitle)meThang).landRoomID());
		else
		if(meThang instanceof MOB)
			return ((MOB)meThang).getStartRoom();
		else
		if(meThang instanceof Item)
		{
			Item me=(Item)meThang;
			// check mobs worn items first!
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R!=null)
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)
						&&(M.isMonster())
						&&(!(M instanceof ShopKeeper))
						&&(M.fetchInventory(me.name())!=null)
						&&(!M.fetchInventory(me.name()).amWearingAt(Item.INVENTORY)))
							return M.getStartRoom();
					}
				}
			}
			// check shopkeepers second!
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R!=null)
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)&&(M instanceof ShopKeeper))
						{
							ShopKeeper S=(ShopKeeper)M;
							if(S.doIHaveThisInStock(me.name(),null))
								return M.getStartRoom();
						}
					}
				}
			}
			// check mobs inventory items third!
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R!=null)
				{
					for(int s=0;s<R.numInhabitants();s++)
					{
						MOB M=R.fetchInhabitant(s);
						if((M!=null)
						&&(M.isMonster())
						&&(!(M instanceof ShopKeeper))
						&&(M.fetchInventory(me.name())!=null)
						&&(M.fetchInventory(me.name()).amWearingAt(Item.INVENTORY)))
							return M.getStartRoom();
					}
				}
			}
			// check room stuff last
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)&&(R.fetchItem(null,me.name())!=null))
				   return R;
			}
		}
		return null;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Room R=origin(target);
		boolean success=profficiencyCheck(0,auto);
		if((success)&&(R!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> encant(s), divining the origin of <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.tell(target.name()+" seems to come from '"+R.displayText()+"'.");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to divine something, but fail(s).");

		return success;
	}
}