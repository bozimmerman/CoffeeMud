package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Age extends StdAbility
{
	public String ID() { return "Age"; }
	public String name(){ return "Age";}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Age();}
	public boolean putInCommandlist(){return false;}
	public int classificationCode(){return Ability.PROPERTY;}
	public String accountForYourself(){return displayText();}
	public String displayText()
	{
		long start=Util.s_long(text());
		long days=((System.currentTimeMillis()-start)/MudHost.TICK_TIME)/MudHost.TICKS_PER_MUDDAY; // down to days;
		long months=days/30;
		if(days<1)
			return "(<1 day old)";
		else
		if(months<1)
			return "("+days+" day(s) old)";
		else
			return "("+months+" month(s) old)";
	}
	private boolean norecurse=false;

	private void doThang()
	{
		if(affected==null) return;
		if(text().length()==0) return;
		long l=Util.s_long(text());
		if(l==0) return;
		if(norecurse) return;
		norecurse=true;

		long day=60000; // one minute
		day*=(long)60; // one hour
		day*=(long)24; // on day
		long ellapsed=(System.currentTimeMillis()-(long)l);
		if(affected instanceof Item)
		{
			if(ellapsed>(long)(30*day))
			{
				Room R=CoffeeUtensils.roomLocation(affected);
				if((R!=null)&&(affected instanceof CagedAnimal))
				{
					Item I=(Item)affected;
					MOB following=null;
					if(I.owner() instanceof MOB)
						following=((MOB)I.owner());
					CagedAnimal C=(CagedAnimal)affected;
					MOB babe=C.unCageMe();
					babe.baseCharStats().setStat(CharStats.CHARISMA,10);
					babe.baseCharStats().setStat(CharStats.CONSTITUTION,7);
					babe.baseCharStats().setStat(CharStats.DEXTERITY,3);
					babe.baseCharStats().setStat(CharStats.INTELLIGENCE,3);
					babe.baseCharStats().setStat(CharStats.STRENGTH,2);
					babe.baseCharStats().setStat(CharStats.WISDOM,2);
					babe.baseEnvStats().setHeight(babe.baseEnvStats().height()*2);
					babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()*2);
					babe.baseState().setHitPoints(2);
					babe.baseState().setMana(10);
					babe.baseState().setMovement(20);
					if(following!=null)
						babe.setLeigeID(following.Name());
					babe.recoverCharStats();
					babe.recoverEnvStats();
					babe.recoverMaxState();
					Age A=(Age)babe.fetchEffect(ID());
					if(A!=null) A.setMiscText(text());
					babe.text();
					babe.bringToLife(R,true);
					babe.setMoney(0);
					babe.setFollowing(following);
					R.show(babe,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> JUST TOOK <S-HIS-HER> FIRST STEPS!!!");
					((Item)affected).destroy();
				}
			}
		}
		else
		if(affected instanceof MOB)
		{
			if(ellapsed>(long)(60*day))
			{
				Room R=CoffeeUtensils.roomLocation(affected);
				if(R!=null)
				{
					MOB babe=(MOB)affected;
					babe.baseCharStats().setStat(CharStats.CHARISMA,10);
					babe.baseCharStats().setStat(CharStats.CONSTITUTION,10);
					babe.baseCharStats().setStat(CharStats.DEXTERITY,5);
					babe.baseCharStats().setStat(CharStats.INTELLIGENCE,6);
					babe.baseCharStats().setStat(CharStats.STRENGTH,6);
					babe.baseCharStats().setStat(CharStats.WISDOM,4);
					babe.baseEnvStats().setHeight(babe.baseEnvStats().height()*5);
					babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()*5);
					babe.baseState().setHitPoints(4);
					babe.baseState().setMana(25);
					babe.baseState().setMovement(50);
					Behavior B=CMClass.getBehavior("MudChat");
					babe.addBehavior(B);
					babe.recoverCharStats();
					babe.recoverEnvStats();
					babe.recoverMaxState();
					babe.text();
					babe.delEffect(this);
				}
			}
		}
		norecurse=false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		doThang();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		doThang();
	}
}
