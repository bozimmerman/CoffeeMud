package com.planet_ink.coffee_mud.web.espresso.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;
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

public class LocaleData extends StdEspressoCommand {
  public String Name = "LocaleData";
  public String ID() { return name(); }
  public String name() { return Name; }
  Vector params=null;

  public LocaleData() {
  }

  public Object run(Vector param, EspressoServer server) {
    if (super.isAuthenticated(param, server)) {
      params=param;
      // Incoming Data:
      // Return Data: Vector
      String command = safelyGetStr(param,1);
      if(command.length()!=0)
        if(command.equalsIgnoreCase("list"))
          return localeList();
        if(command.equalsIgnoreCase("help"))
          return localeHelp(safelyGetStr(param,2));
    }
    return null;
  }

  public Vector localeList()
  {
    Object[] sorted=(Object[])Resources.getResource("ESPRESSO-LOCALES");
    if (sorted == null) {
      Vector sortMe = new Vector();
      for (Enumeration l = CMClass.locales(); l.hasMoreElements(); )
        sortMe.addElement(CMClass.className(l.nextElement()));
      sorted = (Object[]) (new TreeSet(sortMe)).toArray();
      Resources.submitResource("ESPRESSO-LOCALES", sorted);
    }
    return new Vector(Arrays.asList(sorted));
  }

  public String localeHelp(String localeName)
  {
    if(!(localeList().contains(localeName)))
      return "";
    StringBuffer help=new StringBuffer();
    Room R = (Room)CMClass.getClass(localeName);
    // Show Name, Domain Type, Condition Type, Move Modifier, and Resource Defaults
    help.append("Locale Name       : "+R.ID()+"\n\r");
    if(R.domainType()>=Room.INDOORS)
      help.append("Domain Type       : "+Room.indoorDomainDescs[R.domainType()-Room.INDOORS]+" (INDOOR)\n\r");
    else
      help.append("Domain Type       : "+Room.outdoorDomainDescs[R.domainType()]+" (OUTDOOR)\n\r");
//    help.append("Condition Type    : "+Room.conditionDescs[R.domainConditions()]+"\n\r");
    help.append("Move Modifier     : "+R.envStats().weight()+"\n\r");
    StringBuffer resList = new StringBuffer();
    if (R.resourceChoices() != null) {
      for (int i = 0; i < R.resourceChoices().size(); i++) {
        Integer res = (Integer) R.resourceChoices().elementAt(i);
        if (resList.length() > 0)
          resList.append(", ");
        resList.append(EnvResource.RESOURCE_DESCS[res.intValue() &
                       EnvResource.RESOURCE_MASK]);
      }
    }
    else
      resList.append("None");
    help.append("Default Resources : "+resList.toString()+"\n\r");
    // finally, add any help data from the files
    help.append("-------------------------\n\r");
    help.append(super.getHelp(localeName));
    return help.toString();
  }
}
