package com.planet_ink.coffee_mud.web.espresso.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://falserealities.game-host.org</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class HelpData extends StdEspressoCommand {
  public String Name = "HelpData";
  public String ID() { return name(); }
  public String name() { return Name; }

  public HelpData() {
  }

  public Object run(Vector param, EspressoServer server) {
    if (super.isAuthenticated(param, server)) {
        return getHelp(safelyGetStr(param,1));
    }
    return null;
  }
}
