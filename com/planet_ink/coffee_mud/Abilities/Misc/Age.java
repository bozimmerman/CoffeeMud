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

	public final static String happyBabyEmoter="min=1 max=500 chance=10;cries for no reason.;loves its mommy.;loves its daddy.;smiles.;makes a spit bubble.;wiggles its toes.;chews on their finger.;holds up a finger.;stretches its little body.";
	public final static String otherBabyEmoter="min=1 max=5 chance=10;wants its mommy.;wants its daddy.;cries.;doesnt like you.;cries for its mommy.;cries for its daddy.";
	public final static String downBabyEmoter="min=1 max=2 chance=50;wants its mommy.;wants its daddy.;cries.;cries;cries.";
	
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
						babe.setLiegeID(following.Name());
					babe.recoverCharStats();
					babe.recoverEnvStats();
					babe.recoverMaxState();
					Age A=(Age)babe.fetchEffect(ID());
					if(A!=null) A.setMiscText(text());
					Ability STAT=babe.fetchEffect("Prop_StatTrainer");
					if(STAT!=null)
						STAT.setMiscText("CHA=10 CON=7 DEX=3 INT=3 STR=2 WIS=2");
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
		if((affected instanceof MOB)
		&&(((MOB)affected).amFollowing()!=null)
		&&(((MOB)affected).amFollowing().playerStats()!=null)
		&&(!((MOB)affected).amFollowing().isMonster())
		&&(((MOB)affected).location().isInhabitant((MOB)affected))
		&&(((MOB)affected).location().isInhabitant(((MOB)affected).amFollowing())))
		{
			if((ellapsed>(long)(60*day))
			&&(((MOB)affected).fetchBehavior("MudChat")==null))
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
					babe.baseCharStats().setStat(CharStats.WISDOM,6);
					babe.baseEnvStats().setHeight(babe.baseEnvStats().height()*5);
					babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()*5);
					babe.baseState().setHitPoints(4);
					babe.baseState().setMana(25);
					babe.baseState().setMovement(50);
					Behavior B=CMClass.getBehavior("MudChat");
					if(B!=null)
						babe.addBehavior(B);
					else
						babe.delEffect(this);
					babe.recoverCharStats();
					babe.recoverEnvStats();
					babe.recoverMaxState();
					babe.text();
				}
			}
			else
			if((ellapsed>(long)(90*day))
			&&(((MOB)affected).fetchBehavior("MudChat")!=null))
			{
				Room R=CoffeeUtensils.roomLocation(affected);
				if(R!=null)
				{
					MOB babe=(MOB)affected;
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMMap.getLoadPlayer(babe.getLiegeID());
					if(liege==null) liege=babe.amFollowing();
					MOB newMan=CMClass.getMOB("StdMOB");
					newMan.setAgeHours(babe.getAgeHours());
					newMan.setAlignment(babe.getAlignment());
					newMan.setBaseCharStats(babe.baseCharStats());
					newMan.setBaseEnvStats(babe.baseEnvStats());
					newMan.baseEnvStats().setLevel(1);
					newMan.setBitmap(babe.getBitmap());
					newMan.setClanID(babe.getClanID());
					newMan.setDescription(babe.description());
					newMan.setDisplayText(babe.displayText());
					newMan.setExperience(babe.getExperience());
					newMan.setExpNextLevel(babe.getExpNextLevel());
					newMan.setFollowing(null);
					newMan.setLiegeID(babe.getLiegeID());
					newMan.setLocation(babe.location());
					newMan.setMoney(babe.getMoney());
					newMan.setName(babe.Name());
					newMan.setPlayerStats(new DefaultPlayerStats()); // ***TODO
					newMan.setPractices(babe.getPractices());
					newMan.setQuestPoint(babe.getQuestPoint());
					newMan.setStartRoom(babe.getStartRoom());
					newMan.setTrains(babe.getTrains());
					newMan.setWimpHitPoint(babe.getWimpHitPoint());
					newMan.setWorshipCharID(babe.getWorshipCharID());
					newMan.playerStats().setPassword(liege.playerStats().password());
					newMan.playerStats().setEmail(liege.playerStats().getEmail());
					newMan.playerStats().setUpdated(System.currentTimeMillis());
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					newMan.baseCharStats().getMyRace().setHeightWeight(newMan.baseEnvStats(),(char)newMan.baseCharStats().getStat(CharStats.GENDER));
					newMan.baseState().setHitPoints(20);
					newMan.baseState().setMana(100);
					newMan.baseState().setMovement(100);
					newMan.baseCharStats().getMyRace().reRoll(newMan,newMan.baseCharStats());
					newMan.baseCharStats().getMyRace().startRacing(newMan,false);
					newMan.baseCharStats().setMyClasses(";Apprentice");
					newMan.baseCharStats().setMyLevels(";1");
					newMan.baseCharStats().getCurrentClass().startCharacter(newMan,false,false);
					CoffeeUtensils.outfit(newMan,newMan.baseCharStats().getMyRace().outfit());
					CoffeeUtensils.outfit(newMan,newMan.baseCharStats().getCurrentClass().outfit());
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						newMan.baseCharStats().setStat(i,newMan.baseCharStats().getStat(i)+1);
					for(int i=CharStats.MAX_STRENGTH_ADJ;i<CharStats.MAX_STRENGTH_ADJ+CharStats.NUM_BASE_STATS;i++)
						newMan.baseCharStats().setStat(i,newMan.baseCharStats().getStat(i)+1);
					newMan.recoverCharStats();
					newMan.recoverEnvStats();
					newMan.recoverMaxState();
					newMan.resetToMaxState();
					CMClass.DBEngine().DBCreateCharacter(newMan);
					if(CMMap.getPlayer(newMan.Name())==null)
						CMMap.addPlayer(newMan);

					newMan.playerStats().setLastIP(liege.session().getAddress());
					Log.sysOut("Age","Created user: "+newMan.Name());
					for(int s=0;s<Sessions.size();s++)
					{
						Session S=Sessions.elementAt(s);
						if((S!=null)
						&&(S.mob()!=null)
						&&(Util.bset(S.mob().getBitmap(),MOB.ATT_AUTONOTIFY))
						&&(S.mob().playerStats()!=null)
						&&((S.mob().playerStats().getFriends().containsKey(newMan.Name())||S.mob().playerStats().getFriends().containsKey("All"))))
							S.mob().tell("^X"+newMan.Name()+" has just been created.^.^?");
					}
					CommonMsgs.channel("WIZINFO","",newMan.Name()+" has just been created.",true);
					if(liege!=babe.amFollowing())
						babe.amFollowing().tell(newMan.Name()+" has just grown up! "+Util.capitalize(newMan.baseCharStats().hisher())+" password is the same as "+liege.Name()+"'s.");
					liege.tell(newMan.Name()+" has just grown up! "+Util.capitalize(newMan.baseCharStats().hisher())+" password is the same as "+liege.Name()+"'s.");
					newMan.removeFromGame();
					babe.destroy();
				}
			}
		}
		norecurse=false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof Item)&&((msg.target()==affected)||(msg.tool()==affected)))
		{
			Behavior B=affected.fetchBehavior("Emoter");
			if(B==null)
			{
				B=CMClass.getBehavior("Emoter");
				if(B!=null)
					affected.addBehavior(B);
			}
			if(((Item)affected).owner() instanceof Room)
			{ if(!B.getParms().equalsIgnoreCase(downBabyEmoter)) B.setParms(downBabyEmoter);}
			else
			{
				if(affected.description().toUpperCase().indexOf(((Item)affected).owner().name().toUpperCase())<=0)
				{ if(!B.getParms().equalsIgnoreCase(otherBabyEmoter)) B.setParms(otherBabyEmoter);}
				else
				{ if(!B.getParms().equalsIgnoreCase(happyBabyEmoter)) B.setParms(happyBabyEmoter);}
			}
		}
		doThang();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		doThang();
	}
}
