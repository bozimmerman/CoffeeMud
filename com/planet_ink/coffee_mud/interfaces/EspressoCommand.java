package com.planet_ink.coffee_mud.interfaces;
import com.planet_ink.coffee_mud.web.espresso.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://thefactory.homedns.org</p>
 * @author not attributable
 * @version 1.0.0.0
 */

public interface EspressoCommand extends Comparable {

  public String ID();
  public String name();
  public Object run(Vector param, EspressoServer server);
  public String getHelp(String helpTopic);
  public String getHelp(String helpTopic, MOB mob);
}
