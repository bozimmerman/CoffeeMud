package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Merchant extends CommonSkill
{
	public String ID() { return "Merchant"; }
	public String name(){ return "Marketeering";}
	private static final String[] triggerStrings = {"MARKET"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String foundShortName="";
	private static boolean mapped=false;
	public Merchant()
	{
		super();
		displayText="";

		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Archon",1,ID(),false);}
	}
	public Environmental newInstance(){	Merchant M=new Merchant(); M.setMiscText(text()); return M;}
	

}
