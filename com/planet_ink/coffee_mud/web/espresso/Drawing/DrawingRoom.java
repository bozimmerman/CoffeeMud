package com.planet_ink.coffee_mud.web.espresso.Drawing;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.awt.*;
import java.util.*;
import java.io.*;

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
 * <p>Company: http://thefactory.homedns.org</p>
 * @author not attributable
 * @version 1.0.0.0
 */
public class DrawingRoom implements Serializable {
  public int x = 0;
  public int y = 0;
  public int z = 0;
  public String roomID = "";
  public Color bgColor = null;
  public DrawingDir[] doors = new DrawingDir[Directions.NUM_DIRECTIONS];

  public DrawingRoom() { }

  // Data for speedy status bar updates in the client side
  public String LocaleTypeName = "";
  public String RoomName = "";
  public Integer MOBCount = new Integer(0);
  public Integer ItemCount = new Integer(0);
}
