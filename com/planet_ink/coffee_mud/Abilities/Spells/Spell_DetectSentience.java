package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectSentience extends Spell
{
	public String ID() { return "Spell_DetectSentience"; }
	public String name(){return "Detect Sentience";}
	public int quality(){ return INDIFFERENT;}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) softly to <S-HIM-HERSELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer lines=new StringBuffer("^x");
				lines.append(Util.padRight("Name",17)+"| ");
				lines.append(Util.padRight("Location",17)+"^.^N\n\r");
				for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().getStat(CharStats.INTELLIGENCE)>=2))
						{
							lines.append("^!"+Util.padRight(M.name(),17)+"^?| ");
							lines.append(R.roomTitle());
							lines.append("\n\r");
						}
					}
				}
				mob.tell(lines.toString()+"^.");
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) to <S-HIM-HERSELF>, but the spell fizzles.");

		return success;
	}
}
