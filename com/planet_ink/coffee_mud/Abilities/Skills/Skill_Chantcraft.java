package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Chantcraft extends Skill_Spellcraft
{
	public String ID() { return "Skill_Chantcraft"; }
	public String name(){ return "Chantcraft";}
	public Environmental newInstance(){	return new Skill_Chantcraft();}
	public int craftType(){return Ability.CHANT;}
}
