package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Stability extends StdAbility
{
	public String ID() { return "Skill_Stability"; }
	public String name(){return "Stability";}
	public String displayText(){return "";}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Skill_Stability();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(affect.amITarget(affected))
		&&(((Ability)affect.tool()).quality()==Ability.MALICIOUS)
		&&(Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_MOVING))
		&&(profficiencyCheck(-40,false)))
		{
			Room roomS=null;
			Room roomD=null;
			if((affect.target()!=null)&&(affect.target() instanceof MOB))
				roomD=((MOB)affect.target()).location();
			if((affect.source()!=null)&&(affect.source().location()!=null))
				roomS=affect.source().location();
			if((affect.target()!=null)&&(affect.target() instanceof Room))
				roomD=(Room)affect.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.show((MOB)affected,null,affect.tool(),Affect.MSG_OK_VISUAL,"<S-NAME> remain(s) stable despite the <O-NAME>.");
			if(roomD!=null)
				roomS.show((MOB)affected,null,affect.tool(),Affect.MSG_OK_VISUAL,"<S-NAME> remain(s) stable despite the <O-NAME>.");
			helpProfficiency((MOB)affected);
			return false;
		}
		return true;
	}


}
