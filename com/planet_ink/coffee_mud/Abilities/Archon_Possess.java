package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;

public class Archon_Possess extends ArchonSkill
{
	public Archon_Possess()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Possess";

		triggerStrings.addElement("POSSESS");
		triggerStrings.addElement("POSS");
	}

	public Environmental newInstance()
	{
		return new Archon_Possess();
	}
	public boolean invoke(MOB mob, Vector commands)
	{
		String MOBname=CommandProcessor.combine(commands,0);
		
		MOB target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			for(int m=0;m<MUD.map.size();m++)
			{
				Room room=(Room)MUD.map.elementAt(m);
				MOB mob2=room.fetchInhabitant(MOBname);
				if((mob2!=null)&&(mob2.isMonster()))
				{
					target=mob2;
					break;
				}
			}
		}
		if(target==null)
		{
			mob.tell("You can't possess '"+MOBname+"' right now.");
			return false;
		}
		
		if(!profficiencyCheck(0))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> get(s) a far away look, but loses concentration.");
			return false;
		}
		
		mob.location().showOthers(mob,null,Affect.VISUAL_ONLY,"<S-NAME> get(s) a far away look, then seem(s) to fall limp.");
		
		Session s=mob.session();
		s.mob=target;
		target.setSession(s);
		target.setSoulMate(mob);
		mob.setSession(null);
		BasicSenses.look(target,null,true);
		target.tell("Your spirit has changed bodies...");
		return true;
	}
	
	public static void dispossess(MOB mob)
	{
		if(mob.soulMate()==null)
		{
			mob.tell("Huh?");
			return;
		}
		Session s=mob.session();
		s.mob=mob.soulMate();
		mob.soulMate().setSession(s);
		mob.setSession(null);
		mob.soulMate().tell("Your spirit has returned to your body...\n\r\n\r");
		BasicSenses.look(mob.soulMate(),null,true);
		mob.setSoulMate(null);
	}
}
