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

public class RoomList extends StdEspressoCommand {
  public String Name = "RoomList";
  public String ID() { return name(); }
  public String name() { return Name; }

  public RoomList() {
  }

  public Object run(Vector param, EspressoServer server) {
    if (super.isAuthenticated(param, server)) {
      Vector rooms=new Vector();
      // param element 1 should be the area name
      Area A=CMMap.getArea((String)param.elementAt(1));
      for (Enumeration r = A.getMap(); r.hasMoreElements(); )
      {
        Room R = (Room) r.nextElement();
        if (R.roomID().length() > 0)
        {
          rooms.addElement(R.roomID());
        }
      }
      if(rooms.size()>0)
        return rooms;
      else
        return null;
    }
    return null;
  }
}