package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCorpse extends Corpse
{
	public String ID(){	return "GenCorpse";}
	public Environmental newInstance(){	return new Corpse();}
}
