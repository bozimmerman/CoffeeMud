import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Songcraft extends Skill_Spellcraft
{
	public String ID() { return "Skill_Songcraft"; }
	public String name(){ return "Songcraft";}
	public Environmental newInstance(){	return new Skill_Songcraft();}
	public int craftType(){return Ability.SONG;}
}
