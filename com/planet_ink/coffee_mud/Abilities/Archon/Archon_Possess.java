package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;

public class Archon_Possess extends ArchonSkill
{
	public Archon_Possess()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Possess";

		baseEnvStats().setLevel(1);
		addQualifyingClass("Archon",1);
		triggerStrings.addElement("POSSESS");
		triggerStrings.addElement("POSS");
		quality=Ability.MALICIOUS;
	}

	public Environmental newInstance()
	{
		return new Archon_Possess();
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String MOBname=Util.combine(commands,0);

		MOB target=getTarget(mob,commands,givenTarget);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room room=(Room)CMMap.map.elementAt(m);
				MOB mob2=room.fetchInhabitant(MOBname);
				if((mob2!=null)&&(mob2.isMonster()))
				{
					target=mob2;
					break;
				}
			}
		}
		if((target==null)||(!target.isMonster()))
		{
			mob.tell("You can't possess '"+MOBname+"' right now.");
			return false;
		}

		if(!profficiencyCheck(0,auto))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> get(s) a far away look, but lose(s) concentration.");
			return false;
		}

		mob.location().showOthers(mob,null,Affect.MSG_OK_VISUAL,auto?"<S-NAME> fall(s) limp.":"<S-NAME> get(s) a far away look, then seem(s) to fall limp.");

		Session s=mob.session();
		s.setMob(target);
		target.setSession(s);
		target.setSoulMate(mob);
		mob.setSession(null);
		ExternalPlay.look(target,null,true);
		target.tell("^HYour spirit has changed bodies...");
		return true;
	}

	public void dispossess(MOB mob)
	{
		if(mob.soulMate()==null)
		{
			mob.tell("Huh?");
			return;
		}
		Session s=mob.session();
		s.setMob(mob.soulMate());
		mob.soulMate().setSession(s);
		mob.setSession(null);
		mob.soulMate().tell("^HYour spirit has returned to your body...\n\r\n\r^N");
		ExternalPlay.look(mob.soulMate(),null,true);
		mob.setSoulMate(null);
	}

	public void unInvoke()
	{
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		dispossess((MOB)affected);
		affected=null;

		super.unInvoke();
	}
}
