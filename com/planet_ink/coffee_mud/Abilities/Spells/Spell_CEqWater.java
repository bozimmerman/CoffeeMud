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

public class Spell_CEqWater extends Spell_BaseClanEq {
  public String ID() { return "Spell_CEqWater"; }
  public String name(){return "ClanEnchant Water";}
  public Environmental newInstance(){	return new Spell_CEqWater();}

  public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
  {
    commands.insertElementAt("Water",2);
    // All the work is done by the base model
    if(!super.invoke(mob,commands,givenTarget,auto))
        return false;
      return true;
  }
}
