package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Chantcraft extends Skill_Songcraft
{
	public String ID() { return "Skill_Chantcraft"; }
	public String name(){ return "Chantcraft";}
	public int craftType(){return Ability.CHANT;}
}
