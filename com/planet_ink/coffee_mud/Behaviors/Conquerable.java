package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.util.*;


public class Conquerable extends Arrest
{
	public String ID(){return "Conquerable";}
	public Behavior newInstance(){ return new Conquerable();}
	protected boolean defaultModifiableNames(){return false;}
	public String getParms(){return "custom";}
}
