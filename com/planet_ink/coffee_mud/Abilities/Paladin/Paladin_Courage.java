package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Prayers.Prayer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_Courage extends Paladin
{
	public String ID() { return "Paladin_Courage"; }
	public String name(){ return "Paladin`s Courage";}
	public Paladin_Courage()
	{
		super();
		paladinsGroup=new Vector();
	}
	public Environmental newInstance(){	return new Paladin_Courage();}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if((invoker==null)||(invoker.getAlignment()<650))
			return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((affect.target()!=null)
		   &&(paladinsGroup.contains(affect.target()))
		   &&(!paladinsGroup.contains(affect.source()))
		   &&(affect.target() instanceof MOB)
		   &&(affect.source()!=invoker))
		{
			if((invoker.getAlignment()>650)
			&&(profficiencyCheck(0,false))
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability))
			{
				String str1=affect.tool().ID().toUpperCase();
				if((str1.indexOf("SPOOK")>=0)
				||(str1.indexOf("NIGHTMARE")>=0)
				||(str1.indexOf("FEAR")>=0))
				{
					MOB mob=(MOB)affect.target();
					mob.location().showSource(mob,null,Affect.MSG_OK_VISUAL,"Your courage protects you from the "+affect.tool().name()+" attack.");
					mob.location().showOthers(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME>'s courage protects <S-HIM-HER> from the "+affect.tool().name()+" attack.");
					return false;
				}
			}
		}
		return true;
	}
}
