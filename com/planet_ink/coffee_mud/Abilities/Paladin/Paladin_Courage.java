package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((invoker==null)||(invoker.getAlignment()<650))
			return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((msg.target()!=null)
		   &&(paladinsGroup.contains(msg.target()))
		   &&(!paladinsGroup.contains(msg.source()))
		   &&(msg.target() instanceof MOB)
		   &&(msg.source()!=invoker))
		{
			if((invoker.getAlignment()>650)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&((invoker==null)||(invoker.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
			{
				String str1=msg.tool().ID().toUpperCase();
				if((str1.indexOf("SPOOK")>=0)
				||(str1.indexOf("NIGHTMARE")>=0)
				||(str1.indexOf("FEAR")>=0))
				{
					MOB mob=(MOB)msg.target();
					mob.location().showSource(mob,null,CMMsg.MSG_OK_VISUAL,"Your courage protects you from the "+msg.tool().name()+" attack.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME>'s courage protects <S-HIM-HER> from the "+msg.tool().name()+" attack.");
					return false;
				}
			}
		}
		return true;
	}
}
