package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Spell_BaseClanEq extends Spell
{
	public String ID() { return "Spell_BaseClanEq"; }
	public String name(){return "Enchant Clan Equipment Base Model";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	protected String type="";
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(student!=null)
		{
			for(int a=0;a<student.numAbilities();a++)
			{
				Ability A=student.fetchAbility(a);
				if((A!=null)&&(A instanceof Spell_BaseClanEq))
				{
					teacher.tell(student.name()+" already knows '"+A.name()+"', and may not learn another clan enchantment.");
					student.tell("You may only learn a single clan enchantment.");
					return false;
				}
			}
		}
		return super.canBeLearnedBy(teacher,student);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(type.length()==0) return false;
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
		if(C.allowedToDoThis(mob,Clan.FUNC_CLANENCHANT)!=1)
		{
			mob.tell("You are not authorized to draw from the power of your "+C.typeName()+".");
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
		    mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
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
		if((C.getExp()<exp)||(exp<0))
		{
			mob.tell("You need "+exp+" to do that, but your "+C.typeName()+" has only "+C.getExp()+" experience points.");
			return false;
		}

		// Add clan power check end
		if(target.fetchEffect("Prop_ClanEquipment")!=null)
		{
			mob.tell(target.name()+" is already clan enchanted.");
			return false;
		}

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		    return false;

		boolean success=profficiencyCheck(mob,0,auto);

		C.setExp(C.getExp()-exp);
		C.update();

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting intensely.^?");
			if (mob.location().okMessage(mob, msg))
			{
				mob.location().send(mob, msg);
				Ability A=CMClass.getAbility("Prop_ClanEquipment");
				StringBuffer str=new StringBuffer("");
				str.append(type); // Type of Enchantment
				str.append(" ");
				str.append(""+points);     // Power of Enchantment
				str.append(" \"");
				str.append(ClanName);                          // Clan Name
				str.append("\" \"");
				str.append(ClanType);                          // Clan Type
				str.append("\"");
				A.setMiscText(str.toString());
				target.addEffect(A);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting intensely, and looking very frustrated.");
		return success;
	}
}
