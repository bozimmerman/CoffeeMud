package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_Aura extends Paladin
{
	public String ID() { return "Paladin_Aura"; }
	public String name(){ return "Paladin`s Aura";}
	public Paladin_Aura()
	{
		super();
		paladinsGroup=new Vector();
	}
	public Environmental newInstance(){	return new Paladin_Aura();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		for(int i=paladinsGroup.size()-1;i>=0;i--)
		{
			try
			{
				MOB mob=(MOB)paladinsGroup.elementAt(i);
				if((mob.getAlignment()<350)
				&&(profficiencyCheck(0,false)))
				{
					int damage=(int)Math.round(Util.div(mob.envStats().level(),3.0));
					ExternalPlay.postDamage(invoker,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe aura around <S-NAME> <DAMAGE> <T-NAME>!^?");
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e)
			{
			}
		}
		return true;
	}

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
		   &&(profficiencyCheck(0,false))
		   &&(affect.target() instanceof MOB)
		   &&(affect.source()!=invoker))
		{
			if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(!Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_HOLY))
			&&(Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_UNHOLY)))
			{
				affect.source().location().show((MOB)affect.target(),null,Affect.MSG_OK_VISUAL,"The holy field around <S-NAME> protect(s) <S-HIM-HER> from the evil magic attack of "+affect.source().name()+".");
				return false;
			}
			if(((affect.targetMinor()==Affect.TYP_POISON)||(affect.targetMinor()==Affect.TYP_DISEASE))
			&&(profficiencyCheck(0,false)))
				return false;
		}
		return true;
	}
}