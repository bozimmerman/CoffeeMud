package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://falserealities.game-host.org</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Spell_BaseClanEq extends Spell 
{
	public String ID() { return "Spell_BaseClanEq"; }
	public String name(){return "Enchant Clan Equipment Base Model";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_BaseClanEq();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		Clan C=Clans.getClan(mob.getClanID());
		if(C==null)
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		if(mob.getClanRole()!=Clan.POS_ENCHANTER)
		{
			mob.tell("You must be assigned to enchanter to draw from the power of your "+C.typeName()+".");
			return false;
		}
		String ClanName=C.ID();
		String ClanType=C.typeName();

		// Invoking will be like:
		//   CAST [CLANEQSPELL] ITEM QUANTITY
		//   -2   -1            0    1
		if(commands.size()<1)
		{
			mob.tell("Enchant which spell onto what?");
			return false;
		}
		if(commands.size()<2)
		{
			mob.tell("Use how much clan enchantment power?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.elementAt(0),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
		    mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
		    return false;
		}
		// Add clan power check start
		int points=Util.s_int((String)commands.elementAt(1));
		if(points<=0)
		{
			mob.tell("You need to use at least 1 enchantment point.");
			return false;
		}
		long exp=points*CommonStrings.getIntVar(CommonStrings.SYSTEMI_CLANENCHCOST);
		if(C.getExp()<exp)
		{
			mob.tell("You need "+exp+" to do that, but your "+C.typeName()+" has only "+C.getExp()+" experience points.");
			return false;
		}

		// Add clan power check end

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto))
		    return false;

		boolean success=profficiencyCheck(0,auto);

		C.setExp(C.getExp()-exp);
		C.update();
		
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting intensely.^?");
			if (mob.location().okAffect(mob, msg)) 
			{
				mob.location().send(mob, msg);
				Ability A=CMClass.getAbility("Prop_ClanEquipment");
				StringBuffer str=new StringBuffer("");
				str.append(((String)commands.elementAt(2)).toUpperCase()); // Type of Enchantment
				str.append(" ");
				str.append(""+points);     // Power of Enchantment
				str.append(" ");
				str.append(ClanName);                          // Clan Name
				str.append(" ");
				str.append(ClanType);                          // Clan Type
				A.setMiscText(str.toString());
				target.addAffect(A);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting intensely, and looking very frustrated.");
		return success;
	}
}