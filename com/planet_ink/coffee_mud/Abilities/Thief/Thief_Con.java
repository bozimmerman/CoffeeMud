package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Con extends ThiefSkill
{
	public String ID() { return "Thief_Con"; }
	public String name(){ return "Con";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"CON"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Con();}
	private MOB lastChecked=null;

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Con whom into doing what?");
			return false;
		}
		Vector V=new Vector();
		V.addElement(commands.elementAt(0));
		MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null) return false;

		commands.removeElementAt(0);

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.INTELLIGENCE)<3))
		{
			mob.tell("You can't con "+target.displayName()+".");
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("Con "+target.charStats().himher()+" into doing what?");
			return false;
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't con someone to follow.");
			return false;
		}

		int oldProfficiency=profficiency();

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=(mob.envStats().level()-target.envStats().level())*10;
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck((mob.charStats().getStat(CharStats.CHARISMA)*2)+levelDiff,auto);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,"^T<S-NAME> attempt(s) to con <T-NAMESELF> into '"+Util.combine(commands,0)+"', but is unsuccesssful.^?");
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,"^T<S-NAME> con(s) <T-NAMESELF> into '"+Util.combine(commands,0)+"'.^?");
			mob.recoverEnvStats();
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				try
				{
					ExternalPlay.doCommand(target,commands);
				}
				catch(Exception e)
				{
					mob.tell(target.charStats().HeShe()+" smiles, saying '"+e.getMessage()+"'.");
				}
			}
			target.recoverEnvStats();
		}
		if(target==lastChecked)
			setProfficiency(oldProfficiency);
		lastChecked=target;
		return success;
	}

}
