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
	
	// here are the codes for interacting with this behavior
	// see Law.java for info
	public boolean modifyBehavior(Environmental hostObj, 
								  MOB mob, 
								  Object O)
	{
		if((mob!=null)
		&&(mob.location()!=null)
		&&(hostObj!=null)
		&&(hostObj instanceof Area))
		{
			Law laws=getLaws((Area)hostObj);
			Integer I=null;
			Vector V=null;
			if(O instanceof Integer)
				I=(Integer)O;
			else
			if(O instanceof Vector)
			{
				V=(Vector)O;
				if(V.size()==0) 
					return false;
				I=(Integer)V.firstElement();
			}
			else
				return false;
			switch(I.intValue())
			{
			default:
				break;
			}
		}
		return super.modifyBehavior(hostObj,mob,O);
	}
	
	public boolean isAnyKindOfOfficer(Law laws, MOB M)
	{
		return super.isAnyKindOfOfficer(laws,M);
	}
	
	public boolean isTheJudge(Law laws, MOB M)
	{
		return super.isTheJudge(laws,M);
	}
	
	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		return super.isAnUltimateAuthorityHere(M,laws);
	}
	
	protected boolean theLawIsEnabled(Law laws)
	{
		return true;
	}
}
