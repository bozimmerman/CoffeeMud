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

public class AreaList extends StdEspressoCommand {
  public String Name = "AreaList";
  public String ID() { return name(); }
  public String name() { return Name; }

  public AreaList() {
  }

  public Object run(Vector param, EspressoServer server) {
    if(super.isAuthenticated(param,server))
    {
      // We pass back a vector of areas.  First element is just the MUD
      Vector areaV = new Vector();
      MOB mob = server.getMOB(auth(param));
      if(mob==null)
        return null;
      for (Enumeration a = CMMap.areas(); a.hasMoreElements(); ) {
        Area A = (Area) a.nextElement();
        areaV.addElement(A.Name());
      }
      return areaV;
    }
    else
    {
      return null;
    }
  }
}