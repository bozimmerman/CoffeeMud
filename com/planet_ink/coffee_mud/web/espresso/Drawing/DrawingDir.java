package com.planet_ink.coffee_mud.web.espresso.Drawing;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;
import java.util.*;
import java.io.*;

public class DrawingDir implements Serializable {
  public String room="";
  public boolean hasADoor=false;
  public boolean positionedAlready=false;
  public boolean exitNull=false;
}
