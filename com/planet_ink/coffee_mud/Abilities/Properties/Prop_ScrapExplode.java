package com.planet_ink.coffee_mud.Abilities.Properties;
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

public class Prop_ScrapExplode extends Property {

	public String ID() { return "Prop_ScrapExplode"; }
	public String name() { return "Scrap Explode"; }
	protected int canAffectCode() { return Ability.CAN_ITEMS; }
	public Environmental newInstance() {	return new Prop_ScrapExplode();}

	public void executeMsg(Environmental myHost, CMMsg affect)
	{
	    super.executeMsg(myHost, affect);
	    if((affect.target()!=null)&&(affect.target().equals(affected))
	       &&(affect.tool()!=null)&&(affect.tool().ID().equals("Scrapping"))) 
		{
			Item item=(Item)affect.target();
			MOB mob = (MOB)affect.source();
			Room room = mob.location();
			int damage = 3 * item.envStats().weight();
			if (mob != null) 
			{
				MUDFight.postDamage(mob, mob, item, damage*2, CMMsg.NO_EFFECT, Weapon.TYPE_BURSTING,
				        "Scrapping " + item.Name() + " causes an explosion, ***RIPPING*** ("+(damage*2)+") through <T-NAME>!!!");
				Hashtable theBadGuys=mob.getGroupMembers(new Hashtable());
				for(Enumeration e=theBadGuys.elements();e.hasMoreElements();) 
				{
					MOB inhab=(MOB)e.nextElement();
					if (mob == inhab)continue;
					MUDFight.postDamage(inhab, inhab, item, damage, CMMsg.NO_EFFECT, Weapon.TYPE_BURSTING,
					        replaceDamageTag("Fragments from " + item.Name() + " <DAMAGE> <T-NAME>!", damage, Weapon.TYPE_BURSTING));
				}
			}
			item.destroy();
			room.recoverRoomStats();
	    }
	}
	private static String replaceDamageTag(String str, int damage, int damageType)
	{
		if (str == null)return null;
		int replace = str.indexOf("<DAMAGE>");
		if (replace >= 0) 
		{
			if (!CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
			    return str.substring(0, replace) + CommonStrings.standardHitWord(damageType, damage) + str.substring(replace + 8);
			else
			    return str.substring(0, replace) + CommonStrings.standardHitWord(damageType, damage) + " (" + damage + ")" + str.substring(replace + 8);
		}
		return str;
	}
}
