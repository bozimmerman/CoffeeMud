package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdMOB implements MOB
{
	public String ID(){return "StdMOB";}
	protected String Username="";

	private String clanID=null;
	private int clanRole=0;

	protected CharStats baseCharStats=new DefaultCharStats();
	protected CharStats charStats=new DefaultCharStats();

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected PlayerStats playerStats=null;

	protected boolean amDead=false;
	protected Room location=null;
	protected Room lastLocation=null;
	protected Rideable riding=null;

	protected Session mySession=null;
	protected boolean pleaseDestroy=false;
	protected byte[] description=null;
	protected String displayText="";
	protected byte[] miscText=null;

	protected long tickStatus=Tickable.STATUS_NOT;

	/* instantiated item types word, contained, owned*/
	protected Vector inventory=new Vector();

	/* instantiated creature types listed as followers*/
	protected Vector followers=new Vector();

	/* All Ability codes, including languages*/
	protected Vector abilities=new Vector();

	/* instantiated affects on this user*/
	protected Vector affects=new Vector();

	protected Vector behaviors=new Vector();

	protected DVector commandQue=new DVector(2);

	// gained attributes
	protected int Experience=0;
	protected int ExpNextLevel=1000;
	protected int Practices=0;
	protected int Trains=0;
	protected long AgeHours=0;
	protected int Money=0;
	protected int attributesBitmap=MOB.ATT_NOTEACH;
	public long getAgeHours(){return AgeHours;}
	public int getPractices(){return Practices;}
	public int getExperience(){return Experience;}
	public int getExpNextLevel(){return ExpNextLevel;}
	public int getExpNeededLevel()
	{
		if(!isMonster())
		{
			if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)>0)
			&&(CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)<=baseEnvStats().level()))
				return Integer.MAX_VALUE;
		}
		if(ExpNextLevel<=getExperience())
			ExpNextLevel=getExperience()+1000;
		return ExpNextLevel-getExperience();
	}
	public int getTrains(){return Trains;}
	public int getMoney(){return Money;}
	public int getBitmap(){return attributesBitmap;}
	public void setAgeHours(long newVal){ AgeHours=newVal;}
	public void setExperience(int newVal){ Experience=newVal; }
	public void setExpNextLevel(int newVal){ ExpNextLevel=newVal;}
	public void setPractices(int newVal){ Practices=newVal;}
	public void setTrains(int newVal){ Trains=newVal;}
	public void setMoney(int newVal){ Money=newVal;}
	public void setBitmap(int newVal)
	{
		attributesBitmap=newVal;
		if(mySession!=null)
			mySession.setTermID(((Util.bset(attributesBitmap,MOB.ATT_ANSI))?1:0)+((Util.bset(attributesBitmap,MOB.ATT_SOUND))?2:0));
	}

	protected int minuteCounter=0;
	private long lastMoveTime=0;
	private int movesSinceTick=0;
	private int manaConsumeCounter=Dice.roll(1,10,0);

	// the core state values
	public CharState curState=new DefaultCharState();
	public CharState maxState=new DefaultCharState();
	public CharState baseState=new DefaultCharState();
	private long lastTickedDateTime=0;
	public long lastTickedDateTime(){return lastTickedDateTime;}

	// mental characteristics
	protected int Alignment=0;
	protected String WorshipCharID="";
	protected String LeigeID="";
	protected int WimpHitPoint=0;
	protected int QuestPoint=0;
	protected int DeityIndex=-1;
	public String getLeigeID(){return LeigeID;}
	public String getWorshipCharID(){return WorshipCharID;}
	public int getAlignment(){return Alignment;}
	public int getWimpHitPoint(){return WimpHitPoint;}
	public int getQuestPoint(){return QuestPoint;}
	public void setLeigeID(String newVal){LeigeID=newVal;}
	public void setAlignment(int newVal)
	{
		if(newVal<0) newVal=0;
		if(newVal>1000) newVal=1000;
		Alignment=newVal;
	}
	public void setWorshipCharID(String newVal){ WorshipCharID=newVal;}
	public void setWimpHitPoint(int newVal){ WimpHitPoint=newVal;}
	public void setQuestPoint(int newVal){ QuestPoint=newVal;}
	public Deity getMyDeity()
	{
		if(getWorshipCharID().length()==0) return null;
		Deity bob=CMMap.getDeity(getWorshipCharID());
		if(bob==null)
			setWorshipCharID("");
		return bob;
	}

	// location!
	protected Room StartRoom=null;
	public Room getStartRoom(){return StartRoom;}
	public void setStartRoom(Room newVal){StartRoom=newVal;}


	protected MOB victim=null;
	protected MOB amFollowing=null;
	protected MOB soulMate=null;
	private double speeder=0.0;
	protected int atRange=-1;
	private long peaceTime=0;
	public long peaceTime(){return peaceTime;}

	public String Name()
	{
		return Username;
	}
	public void setName(String newName){Username=newName;}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return Username;
	}
	public Environmental newInstance()
	{
		return new StdMOB();
	}
	public StdMOB(){}

	protected void cloneFix(MOB E)
	{

		affects=new Vector();
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();
		baseCharStats=E.baseCharStats().cloneCharStats();
		charStats=E.charStats().cloneCharStats();
		baseState=E.baseState().cloneCharState();
		curState=E.curState().cloneCharState();
		maxState=E.maxState().cloneCharState();

		pleaseDestroy=false;

		inventory=new Vector();
		followers=new Vector();
		abilities=new Vector();
		affects=new Vector();
		behaviors=new Vector();
		for(int i=0;i<E.inventorySize();i++)
		{
			Item I2=E.fetchInventory(i);
			if(I2!=null)
			{
				Item I=(Item)I2.copyOf();
				I.setOwner(this);
				inventory.addElement(I);
			}
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item I2=fetchInventory(i);
			if((I2!=null)
			&&(I2.container()!=null)
			&&(!isMine(I2.container())))
				for(int ii=0;ii<E.inventorySize();ii++)
					if((E.fetchInventory(ii)==I2.container())&&(ii<inventorySize()))
					{ I2.setContainer(fetchInventory(ii)); break;}
		}
		for(int i=0;i<E.numLearnedAbilities();i++)
		{
			Ability A2=E.fetchAbility(i);
			if(A2!=null)
				abilities.addElement(A2.copyOf());
		}
		for(int i=0;i<E.numEffects();i++)
		{
			Ability A=(Ability)E.fetchEffect(i);
			if((A!=null)&&(!A.canBeUninvoked()))
				addEffect((Ability)A.copyOf());
		}
		for(int i=0;i<E.numBehaviors();i++)
		{
			Behavior B=E.fetchBehavior(i);
			if(B!=null)
				behaviors.addElement((Behavior)B.copyOf());
		}

	}
	public Environmental copyOf()
	{
		try
		{
			StdMOB E=(StdMOB)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public boolean isGeneric(){return false;}
	public EnvStats envStats()
	{
		return envStats;
	}

	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		if(location()!=null)
			location().affectEnvStats(this,envStats);
		envStats().setWeight(envStats().weight()+(int)Math.round(Util.div(getMoney(),100.0)));
		if(riding()!=null) riding().affectEnvStats(this,envStats);
		if(getMyDeity()!=null) getMyDeity().affectEnvStats(this,envStats);
		if(charStats!=null)
		{
			charStats().getCurrentClass().affectEnvStats(this,envStats);
			charStats().getMyRace().affectEnvStats(this,envStats);
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
			{
				item.recoverEnvStats();
				item.affectEnvStats(this,envStats);
			}
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability effect=fetchEffect(a);
			if(effect!=null)
				effect.affectEnvStats(this,envStats);
		}
		/* the follower light exception*/
		if(!Sense.isLightSource(this))
		for(int f=0;f<numFollowers();f++)
			if(Sense.isLightSource(fetchFollower(f)))
				envStats.setDisposition(envStats().disposition()|EnvStats.IS_LIGHTSOURCE);
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public int baseWeight()
	{
		if(charStats().getMyRace()==baseCharStats().getMyRace())
			return baseEnvStats().weight();
		else
			return charStats().getMyRace().getMaxWeight();
	}
	public int maxCarry()
	{
		double str=new Integer(charStats().getStat(CharStats.STRENGTH)).doubleValue();
		double bodyWeight=new Integer(baseWeight()).doubleValue();
		return (int)Math.round(bodyWeight + ((str+10.0)*str*bodyWeight/150.0) + (str*5.0));
	}
	public int maxFollowers()
	{
		return ((int)Math.round(Util.div(charStats().getStat(CharStats.CHARISMA),4.0))+1);
	}
	public int totalFollowers()
	{
		int total=numFollowers();
		try{
			for(int i=0;i<total;i++)
				total+=fetchFollower(i).totalFollowers();
		}catch(Throwable t){}
		return total;
	}

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		baseCharStats.setClassLevel(baseCharStats.getCurrentClass(),baseEnvStats().level()-baseCharStats().combinedSubLevels());
		charStats=baseCharStats().cloneCharStats();

		if(riding()!=null) riding().affectCharStats(this,charStats);
		if(getMyDeity()!=null) getMyDeity().affectCharStats(this,charStats);
		for(int a=0;a<numEffects();a++)
		{
			Ability effect=fetchEffect(a);
			if(effect!=null)
				effect.affectCharStats(this,charStats);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
				item.affectCharStats(this,charStats);
		}
		if(location()!=null)
			location().affectCharStats(this,charStats);
		charStats.getCurrentClass().affectCharStats(this,charStats);
		charStats.getMyRace().affectCharStats(this,charStats);
	}
	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=newBaseCharStats.cloneCharStats();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((Sense.isLightSource(this))&&(affected instanceof Room))
		{
			if(Sense.isInDark(affected))
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public CharState curState(){return curState;}
	public CharState maxState(){return maxState;}
	public CharState baseState(){return baseState;}
	public PlayerStats playerStats()
	{
		if((playerStats==null)&&(soulMate!=null))
			return soulMate.playerStats();
		return playerStats;
	}
	public void setPlayerStats(PlayerStats newStats){playerStats=newStats;}
	public void setBaseState(CharState newState)
	{
		baseState=newState.cloneCharState();
		maxState=newState.cloneCharState();
	}
	public void resetToMaxState()
	{
		recoverMaxState();
		curState=maxState.cloneCharState();
	}
	public void recoverMaxState()
	{
		maxState=baseState.cloneCharState();
		if(charStats.getMyRace()!=null)	charStats.getMyRace().affectCharState(this,maxState);
		if(riding()!=null) riding().affectCharState(this,maxState);
		for(int a=0;a<numEffects();a++)
		{
			Ability effect=fetchEffect(a);
			if(effect!=null)
				effect.affectCharState(this,maxState);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
				item.affectCharState(this,maxState);
		}
		if(location()!=null)
			location().affectCharState(this,maxState);
	}

	public boolean amDead()
	{
		return amDead||pleaseDestroy;
	}


	public void destroy()
	{
		removeFromGame();
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
		while(numEffects()>0)
			delEffect(fetchEffect(0));
		while(numLearnedAbilities()>0)
			delAbility(fetchAbility(0));
		while(inventorySize()>0)
		{
			Item I=fetchInventory(0);
			I.destroy();
			delInventory(I);
		}
	}

	public void removeFromGame()
	{
		pleaseDestroy=true;
		if(location!=null)
		{
			location().delInhabitant(this);
			if(mySession!=null)
				location().show(this,null,CMMsg.MSG_OK_ACTION,"<S-NAME> vanish(es) in a puff of smoke.");
		}
		setFollowing(null);
		Vector oldFollowers=new Vector();
		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if(follower!=null)
			{
				if(follower.isMonster())
					oldFollowers.addElement(follower);
				follower.setFollowing(null);
				delFollower(follower);
			}
		}

		if(!isMonster())
		{
			for(int f=0;f<oldFollowers.size();f++)
			{
				MOB follower=(MOB)oldFollowers.elementAt(f);
				if(follower.location()!=null)
				{
					MOB newFol=(MOB)follower.copyOf();
					newFol.baseEnvStats().setRejuv(0);
					newFol.text();
					follower.killMeDead(false);
					addFollower(newFol);
				}
			}
			session().setKillFlag(true);
		}
	}

	public String getClanID(){return ((clanID==null)?"":clanID);}
	public void setClanID(String clan){clanID=clan;}
	public int getClanRole(){return clanRole;}
	public void setClanRole(int role){clanRole=role;}

	public void bringToLife(Room newLocation, boolean resetStats)
	{
		amDead=false;
		if((miscText!=null)&&(resetStats)&&(isGeneric()))
		{
			if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
				CoffeeMaker.resetGenMOB(this,Util.decompressString(miscText));
			else
				CoffeeMaker.resetGenMOB(this,new String(miscText));
		}
		if(getStartRoom()==null)
			setStartRoom(isMonster()?newLocation:CMMap.getStartRoom(this));
		setLocation(newLocation);
		if(location()==null)
		{
			setLocation(getStartRoom());
			if(location()==null)
			{
				Log.errOut("StdMOB",Username+" cannot get a location.");
				return;
			}
		}
		if(!location().isInhabitant(this))
			location().addInhabitant(this);
		pleaseDestroy=false;

		// will ensure no duplicate ticks, this obj, this id
		CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_MOB,1);
		if(tickStatus==Tickable.STATUS_NOT)
			tick(this,MudHost.TICK_MOB); // slap on the butt
		for(int a=0;a<numLearnedAbilities();a++)
		{
			Ability A=fetchAbility(a);
			if(A!=null)
				A.autoInvocation(this);
		}
		location().recoverRoomStats();
		if((!isGeneric())&&(resetStats))
			resetToMaxState();

		if(Sense.isSleeping(this))
			tell("(You are asleep)");
		else
			CommonMsgs.look(this,true);
	}

	public boolean isInCombat()
	{
		if(victim==null) return false;
		if((victim.location()==null)
		||(location()==null)
		||(victim.location()!=location())
		||(victim.amDead()))
		{
			if((victim instanceof StdMOB)
			&&(((StdMOB)victim).victim==this))
				victim.setVictim(null);
			setVictim(null);
			return false;
		}
		return true;
	}
	public boolean mayIFight(MOB mob)
	{
		if(mob==null) return false;
		if(location()==null) return false;
		if(mob.location()==null) return false;
		if(mob.amDead()) return false;
		if(mob.curState().getHitPoints()<=0) return false;
		if(amDead()) return false;
		if(curState().getHitPoints()<=0) return false;
		if(mob.isMonster()) return true;
		if(isMonster()){
			MOB fol=amFollowing();
			if((fol!=null)&&(!fol.isMonster()))
				return fol.mayIFight(mob);
			return true;
		}
		if((mob.soulMate()!=null)||(soulMate()!=null))
			return true;
		if(mob==this) return true;
		if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ALWAYS"))
			return true;
		if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("NEVER"))
			return false;
		if((getClanID().length()>0)&&(mob.getClanID().length()>0)
		&&(Clans.getClanRelations(getClanID(),mob.getClanID())==Clan.REL_WAR))
			return true;
		if(Util.bset(getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if((isASysOp(location()))
			||(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
		if(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if((mob.isASysOp(mob.location()))
			||(Util.bset(getBitmap(),MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
			return false;
	}
	public boolean mayPhysicallyAttack(MOB mob)
	{
		if((!mayIFight(mob))
		||(location()!=mob.location())
		||(!location().isInhabitant(this))
		||(!mob.location().isInhabitant(mob)))
		   return false;
		return true;
	}
	public int adjustedAttackBonus(MOB mob)
	{
		double att=new Integer(
				envStats().attackAdjustment()
				+((charStats().getStat(CharStats.STRENGTH)-9)*3)).doubleValue();
		if(curState().getHunger()<1) att=att*.9;
		if(curState().getThirst()<1) att=att*.9;
		if(curState().getFatigue()>CharState.FATIGUED_MILLIS) att=att*.8;
		if(mob==null)
			return (int)Math.round(att);
		else
			return ((envStats().level()-mob.envStats().level())*2)+(int)Math.round(att);
	}

	public int adjustedArmor()
	{
		double arm=new Integer(((charStats().getStat(CharStats.DEXTERITY)-9)*3)
							   +50).doubleValue();
		if((envStats().disposition()&EnvStats.IS_SLEEPING)>0) arm=0.0;
		if(arm>0.0)
		{
			if(curState().getHunger()<1) arm=arm*.85;
			if(curState().getThirst()<1) arm=arm*.85;
			if(curState().getFatigue()>CharState.FATIGUED_MILLIS) arm=arm*.85;
			if((envStats().disposition()&EnvStats.IS_SITTING)>0) arm=arm*.75;
		}
		return (int)Math.round(envStats().armor()-arm);
	}

	public int adjustedDamage(Weapon weapon, MOB target)
	{
		double damageAmount=0.0;
		if(target!=null)
		{
			if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
				damageAmount = new Integer(Dice.roll(1, weapon.envStats().damage(),1)).doubleValue();
			else
				damageAmount = new Integer(Dice.roll(1, envStats().damage(), (charStats().getStat(CharStats.STRENGTH) / 3)-2)).doubleValue();
			if(!Sense.canBeSeenBy(target,this)) damageAmount *=.5;
			if(Sense.isSleeping(target)) damageAmount *=1.5;
			else
			if(Sense.isSitting(target)) damageAmount *=1.2;
		}
		else
		if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
			damageAmount = new Integer(weapon.envStats().damage()+1).doubleValue();
		else
			damageAmount = new Integer(envStats().damage()+(charStats().getStat(CharStats.STRENGTH) / 3)-2).doubleValue();
		if(curState().getHunger() < 1) damageAmount *= .8;
		if(curState().getFatigue()>CharState.FATIGUED_MILLIS) damageAmount *=.8;
		if(curState().getThirst() < 1) damageAmount *= .9;
		if(damageAmount<1.0) damageAmount=1.0;
		return (int)Math.round(damageAmount);
	}

	public void setAtRange(int newRange){atRange=newRange;}
	public int rangeToTarget(){return atRange;}
	public int maxRange(){return maxRange(null);}
	public int minRange(){return maxRange(null);}
	public int maxRange(Environmental tool)
	{
		int max=0;
		if(tool!=null)
			max=tool.maxRange();
		if((location()!=null)&&(location().maxRange()<max))
			max=location().maxRange();
		return max;
	}
	public int minRange(Environmental tool)
	{
		if(tool!=null) return tool.minRange();
		return 0;
	}

	public void makePeace()
	{
		MOB myVictim=victim;
		setVictim(null);
		if(myVictim!=null)
		{
			MOB oldVictim=myVictim.getVictim();
			if(oldVictim==this)
				myVictim.makePeace();
		}
	}

	public MOB getVictim()
	{
		if(!isInCombat()) return null;
		return victim;
	}

	public void setVictim(MOB mob)
	{
		if(mob==null)
		{	
			setAtRange(-1);
			if(victim!=null)
				commandQue.clear();
		}
		if(victim==mob) return;
		if(mob==this) return;

		victim=mob;
		recoverEnvStats();
		recoverCharStats();
		recoverMaxState();
		if(mob!=null)
		{
			if((mob.location()==null)
			||(location()==null)
			||(mob.amDead())
			||(amDead())
			||(mob.location()!=location())
			||(!location().isInhabitant(this))
			||(!location().isInhabitant(mob)))
			{
				if(victim!=null)
					victim.setVictim(null);
				victim=null;
			}
			else
			{
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
		}
	}
	public DeadBody killMeDead(boolean createBody)
	{
		Room deathRoom=null;
		if(isMonster())
			deathRoom=location();
		else
			deathRoom=CMMap.getBodyRoom(this);
		if(location()!=null) location().delInhabitant(this);
		DeadBody Body=null;
		if(createBody)
			Body=charStats().getMyRace().getCorpse(this,deathRoom);
		amDead=true;
		makePeace();
		setRiding(null);
		commandQue.clear();
		for(int a=numEffects()-1;a>=0;a--)
		{
			Ability A=fetchEffect(a);
			if(A!=null) A.unInvoke();
		}
		setLocation(null);
		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if(follower!=null)
			{
				follower.setFollowing(null);
				delFollower(follower);
			}
		}
		setFollowing(null);
		if((!isMonster())&&(soulMate()==null))
			bringToLife(CMMap.getDeathRoom(this),true);
		if(deathRoom!=null)
		{
			if(Body!=null) Body.startTicker(deathRoom);
			deathRoom.recoverRoomStats();
		}
		return Body;
	}

	public Room location()
	{
		if(location==null) return lastLocation;
		return location;
	}
	public void setLocation(Room newRoom)
	{
		lastLocation=location;
		location=newRoom;
	}
	public Rideable riding(){return riding;}
	public void setRiding(Rideable ride)
	{
		if((ride!=null)&&(riding()!=null)&&(riding()==ride)&&(riding().amRiding(this)))
			return;
		if((riding()!=null)&&(riding().amRiding(this)))
			riding().delRider(this);
		riding=ride;
		if((riding()!=null)&&(!riding().amRiding(this)))
			riding().addRider(this);
	}
	public Session session()
	{
		return mySession;
	}
	public void setSession(Session newSession)
	{
		mySession=newSession;
		setBitmap(getBitmap());
	}
	public Weapon myNaturalWeapon()
	{
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return charStats().getMyRace().myNaturalWeapon();
		return (Weapon)CMClass.getWeapon("Natural");
	}

	public String displayText(MOB viewer)
	{
		if((displayText.length()==0)
		   ||(!name().equals(name()))
		   ||(Sense.isSleeping(this))
		   ||(Sense.isSitting(this))
		   ||(riding()!=null)
		   ||((this instanceof Rideable)&&(((Rideable)this).numRiders()>0))
		   ||(isInCombat()))
		{
			StringBuffer sendBack=null;
			sendBack=new StringBuffer(name());
			sendBack.append(" ");
			sendBack.append(Sense.dispositionString(this,Sense.flag_is));
			sendBack.append(" here");
			if(riding()!=null)
			{
				sendBack.append(" "+riding().stateString(this)+" ");
				if(riding()==viewer)
					sendBack.append("YOU");
				else
				if(!Sense.canBeSeenBy(riding(),viewer))
				{
					if(riding() instanceof Item)
						sendBack.append("something");
					else
						sendBack.append("someone");
				}
				else
					sendBack.append(riding().name());
			}
			else
			if((this instanceof Rideable)
			   &&(((Rideable)this).numRiders()>0)
			   &&(((Rideable)this).stateStringSubject(((Rideable)this).fetchRider(0)).length()>0))
			{
				Rideable me=(Rideable)this;
				String first=me.stateStringSubject(me.fetchRider(0));
				sendBack.append(" "+first+" ");
				for(int r=0;r<me.numRiders();r++)
				{
					Rider rider=me.fetchRider(r);
					if((rider!=null)&&(me.stateStringSubject(rider).equals(first)))
					{
						if(r>0)
						{
							sendBack.append(", ");
							if(r==me.numRiders()-1)
								sendBack.append("and ");
						}
						if(rider==viewer)
							sendBack.append("you");
						else
						if(!Sense.canBeSeenBy(riding(),viewer))
						{
							if(riding() instanceof Item)
								sendBack.append("something");
							else
								sendBack.append("someone");
						}
						else
							sendBack.append(rider.name());
					}

				}
			}
			if((isInCombat())&&(Sense.canMove(this))&&(!Sense.isSleeping(this)))
			{
				sendBack.append(" fighting ");
				if(getVictim()==viewer)
					sendBack.append("YOU");
				else
				if(!Sense.canBeSeenBy(getVictim(),viewer))
					sendBack.append("someone");
				else
					sendBack.append(getVictim().name());
			}
			sendBack.append(".");
			return sendBack.toString();
		}
		else
			return displayText;
	}

	public String displayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		if((description==null)||(description.length==0))
			return "";
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS))
			return Util.decompressString(description);
		else
			return new String(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS))
			description=Util.compressString(newDescription);
		else
			description=newDescription.getBytes();
	}
	public void setMiscText(String newText)
	{
		if(newText.length()==0)
			miscText=null;
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			miscText=Util.compressString(newText);
		else
			miscText=newText.getBytes();
	}
	public String text()
	{
		if((miscText==null)||(miscText.length==0))
			return "";
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			return Util.decompressString(miscText);
		else
			return new String(miscText);
	}

	public String healthText()
	{
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return charStats().getMyRace().healthText(this);
		return CommonStrings.standardMobCondition(this);
	}

	public void dequeCommand()
	{
		Vector returnable=null;
		synchronized(commandQue)
		{
			if(commandQue.size()>0)
			{
				Vector topCMD=(Vector)commandQue.elementAt(0,1);
				int topTick=((Integer)commandQue.elementAt(0,2)).intValue();
				commandQue.removeElementAt(0);
				if((--topTick)<2)
					returnable=topCMD;
				else
					commandQue.insertElementAt(0,topCMD,new Integer(topTick));
			}
		}
		if(returnable!=null)
			doCommand(EnglishParser.findCommand(this,returnable),returnable);
	} 

	public void doCommand(Vector commands)
	{
		Object O=EnglishParser.findCommand(this,commands);
		if(O!=null) doCommand(O,commands);
	}

	private void doCommand(Object O, Vector commands)
	{
		try
		{
			if(O instanceof Command)
				((Command)O).execute(this,commands);
			else
			if(O instanceof Social)
				((Social)O).invoke(this,commands,null,false);
			else
			if(O instanceof Ability)
				EnglishParser.evoke(this,commands);
			else
				tell("Wha? Huh?");
		}
		catch(Exception e)
		{
			Log.errOut("StdMOB",Util.toStringList(commands));
			Log.errOut("StdMOB",e);
			tell("Oops!");
		}
	}

	public void enqueCommand(Vector commands, int tickDelay)
	{
		if(commands==null) return;
		int tickDown=1;
		if(tickDelay<1)
		{
			Object O=EnglishParser.findCommand(this,commands);
			if(O==null){ tell("Huh?!"); return;}

			if(O instanceof Command)
				tickDown=((Command)O).ticksToExecute();
			else
			if(O instanceof Ability)
				tickDown=isInCombat()?((Ability)O).combatCastingTime():((Ability)O).castingTime();

			if(((!isInCombat())&&(tickDown<2))
			||(tickDown==0))
			{
				doCommand(O,commands);
				return;
			}
		}

		synchronized(commandQue)
		{
			if((tickDelay+1)>tickDown) tickDown=tickDelay+1;
			commandQue.addElement(commands,new Integer(tickDown));
		}
	}

	public void establishRange(MOB source, MOB target, Environmental tool)
	{
		// establish and enforce range
		if((source.rangeToTarget()<0))
		{
			if(source.riding()!=null)
			{
				if((target==riding())||(source.riding().amRiding(target)))
					source.setAtRange(0);
				else
				if((source.riding() instanceof MOB)
				   &&(((MOB)source.riding()).isInCombat())
				   &&(((MOB)source.riding()).getVictim()==target)
				   &&(((MOB)source.riding()).rangeToTarget()>=0)
				   &&(((MOB)source.riding()).rangeToTarget()<rangeToTarget()))
				{
					source.setAtRange(((MOB)source.riding()).rangeToTarget());
					recoverEnvStats();
					return;
				}
				else
				for(int r=0;r<source.riding().numRiders();r++)
				{
					Rider rider=source.riding().fetchRider(r);
					if(!(rider instanceof MOB)) continue;
					MOB otherMOB=(MOB)rider;
					if((otherMOB!=null)
					   &&(otherMOB!=this)
					   &&(otherMOB.isInCombat())
					   &&(otherMOB.getVictim()==target)
					   &&(otherMOB.rangeToTarget()>=0)
					   &&(otherMOB.rangeToTarget()<rangeToTarget()))
					{
						source.setAtRange(otherMOB.rangeToTarget());
						source.recoverEnvStats();
						return;
					}
				}
			}

			if(target.getVictim()==source)
			{
				if(target.rangeToTarget()>=0)
					source.setAtRange(target.rangeToTarget());
				else
					source.setAtRange(maxRange(tool));
			}
			else
				source.setAtRange(maxRange(tool));
			recoverEnvStats();
		}
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((getMyDeity()!=null)&&(!getMyDeity().okMessage(this,msg)))
			return false;

		if(charStats!=null)
		{
			if(!charStats().getCurrentClass().okMessage(this,msg))
				return false;
			if(!charStats().getMyRace().okMessage(this, msg))
				return false;
		}

		for(int i=0;i<numEffects();i++)
		{
			Ability aff=(Ability)fetchEffect(i);
			if((aff!=null)&&(!aff.okMessage(this,msg)))
				return false;
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item I=(Item)fetchInventory(i);
			if((I!=null)&&(!I.okMessage(this,msg)))
				return false;
		}

		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}

		MOB mob=msg.source();
		if((msg.sourceCode()!=CMMsg.NO_EFFECT)&&(msg.amISource(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(!isMonster())
			&&(CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)>0)
			&&(baseEnvStats().level()>CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)))
			{
				curState().setHitPoints(1);
				if((msg.tool()!=null)
				&&(msg.tool()!=this)
				&&(msg.tool() instanceof MOB))
				   ((MOB)msg.tool()).tell(name()+" is immortal, and can not die.");
				tell("You are immortal, and can not die.");
				return false;
			}

			if(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			{
				int srcMajor=msg.sourceMajor();

				if(amDead())
				{
					tell("You are DEAD!");
					return false;
				}

				if(Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
				{
					if((msg.target()!=this)&&(msg.target()!=null)&&(msg.target() instanceof MOB))
					{
						MOB target=(MOB)msg.target();
						if((amFollowing()!=null)&&(target==amFollowing()))
						{
							tell("You like "+amFollowing().charStats().himher()+" too much.");
							return false;
						}
						if((getLeigeID().length()>0)&&(target.Name().equals(getLeigeID())))
						{
							tell("You are serving '"+getLeigeID()+"'!");
							return false;
						}
						establishRange(this,(MOB)msg.target(),msg.tool());
					}
				}


				if(Util.bset(srcMajor,CMMsg.MASK_EYES))
				{
					if(Sense.isSleeping(this))
					{
						tell("Not while you are sleeping.");
						return false;
					}
					if(!(msg.target() instanceof Room))
						if(!Sense.canBeSeenBy(msg.target(),this))
						{
							if(msg.target() instanceof Item)
								tell("You don't see "+msg.target().name()+" here.");
							else
								tell("You can't see that!");
							return false;
						}
				}
				if(Util.bset(srcMajor,CMMsg.MASK_MOUTH))
				{
					if(!Sense.aliveAwakeMobile(this,false))
						return false;
					if(Util.bset(srcMajor,CMMsg.MASK_SOUND))
					{
						if((msg.tool()==null)
						||(!(msg.tool() instanceof Ability))
						||(!((Ability)msg.tool()).isNowAnAutoEffect()))
						{
							if(Sense.isSleeping(this))
							{
								tell("Not while you are sleeping.");
								return false;
							}
							if(!Sense.canSpeak(this))
							{
								tell("You can't make sounds!");
								return false;
							}
							if(Sense.isAnimalIntelligence(this))
							{
								tell("You aren't smart enough to speak.");
								return false;
							}
						}
					}
					else
					{
						if((!Sense.canBeSeenBy(msg.target(),this))
						&&(!(isMine(msg.target())&&(msg.target() instanceof Item))))
						{
							mob.tell("You don't see '"+msg.target().name()+"' here.");
							return false;
						}
						if(!Sense.canTaste(this))
						{
							tell("You can't eat or drink!");
							return false;
						}
					}
				}
				if(Util.bset(srcMajor,CMMsg.MASK_HANDS))
				{
					if((!Sense.canBeSeenBy(msg.target(),this))
					&&(!(isMine(msg.target())&&(msg.target() instanceof Item)))
					&&(!(isInCombat()&&(msg.target()==victim))))
					{
						mob.tell("You don't see '"+msg.target().name()+"' here.");
						return false;
					}
					if(!Sense.aliveAwakeMobile(this,false))
						return false;

					if((Sense.isSitting(this))
					&&(msg.sourceMinor()!=CMMsg.TYP_SITMOVE)
					&&(msg.targetCode()!=CMMsg.MSG_OK_VISUAL)
					&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
					&&((!CoffeeUtensils.reachableItem(this,msg.target()))
					||(!CoffeeUtensils.reachableItem(this,msg.tool()))))
					{
						tell("You need to stand up!");
						return false;
					}
				}

				if(Util.bset(srcMajor,CMMsg.MASK_MOVE))
				{
					boolean sitting=Sense.isSitting(this);
					if((sitting)
					&&((msg.sourceMinor()==CMMsg.TYP_LEAVE)
					||(msg.sourceMinor()==CMMsg.TYP_ENTER)))
						sitting=false;

					if(((Sense.isSleeping(this))||(sitting))
					&&(msg.sourceMinor()!=CMMsg.TYP_STAND)
					&&(msg.sourceMinor()!=CMMsg.TYP_SITMOVE)
					&&(msg.sourceMinor()!=CMMsg.TYP_SLEEP))
					{
						tell("You need to stand up!");
						if(msg.sourceMinor()!=CMMsg.TYP_WEAPONATTACK)
							return false;
					}
					if(!Sense.canMove(this))
					{
						tell("You can't move!");
						return false;
					}
				}

				// limb check
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_PULL:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_REMOVE:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_DROP:
				case CMMsg.TYP_THROW:
					if(getWearPositions(Item.ON_ARMS)==0)
					{
						tell("You need arms to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_DELICATE_HANDS_ACT:
					if((getWearPositions(Item.ON_HANDS)==0)
					&&(msg.othersMinor()!=CMMsg.NO_EFFECT))
					{
						tell("You need hands to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_JUSTICE:
					if((getWearPositions(Item.ON_HANDS)==0)
					&&(msg.target() instanceof Item))
					{
						tell("You need hands to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_GIVE:
				case CMMsg.TYP_HANDS:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_WRITE:
					if(getWearPositions(Item.ON_HANDS)==0)
					{
						tell("You need hands to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_DRINK:
					if(getWearPositions(Item.ON_HANDS)==0)
					{
						if((msg.target()!=null)
						&&(isMine(msg.target())))
						{
							tell("You need hands to do that.");
							return false;
						}
					}
					break;
				}

				// activity check
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_JUSTICE:
					if((msg.target()!=null)
					&&(isInCombat())
					&&(msg.target() instanceof Item))
					{
						tell("Not while you are fighting!");
						return false;
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if(isInCombat())
					{
						if((msg.target()!=null)
						&&((msg.target() instanceof Exit)||(msg.source().isMine(msg.target()))))
							break;
						tell("Not while you are fighting!");
						return false;
					}
					break;
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_DELICATE_HANDS_ACT:
				case CMMsg.TYP_LEAVE:
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_VALUE:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_READSOMETHING:
					if(isInCombat())
					{
						tell("Not while you are fighting!");
						return false;
					}
					break;
				case CMMsg.TYP_REBUKE:
					if((msg.target()==null)||(!(msg.target() instanceof Deity)))
					{
						if(msg.target()!=null)
						{
							if(!Sense.canBeHeardBy(this,msg.target()))
							{
								tell(msg.target().name()+" can't hear you!");
								return false;
							}
							else
							if((!((msg.target() instanceof MOB)
							&&(((MOB)msg.target()).getLeigeID().equals(Name()))))
							&&(!msg.target().Name().equals(getLeigeID())))
							{
								tell(msg.target().name()+" does not serve you, and you do not serve "+msg.target().name()+".");
								return false;
							}
						}
						else
						if(getLeigeID().length()==0)
						{
							tell("You aren't serving anyone!");
							return false;
						}
					}
					else
					if(getWorshipCharID().length()==0)
					{
						tell("You aren't worshipping anyone!");
						return false;
					}
					break;
				case CMMsg.TYP_SERVE:
					if(msg.target()==null) return false;
					if(msg.target()==this)
					{
						tell("You can't serve yourself!");
						return false;
					}
					if(msg.target() instanceof Deity)
						break;
					if(!Sense.canBeHeardBy(this,msg.target()))
					{
						tell(msg.target().name()+" can't hear you!");
						return false;
					}
					if(getLeigeID().length()>0)
					{
						tell("You are already serving '"+getLeigeID()+"'.");
						return false;
					}
					break;
				case CMMsg.TYP_CAST_SPELL:
					if(charStats().getStat(CharStats.INTELLIGENCE)<5)
					{
						tell("You aren't smart enough to do magic.");
						return false;
					}
					break;
				default:
					break;
				}
			}
		}

		if((msg.sourceCode()!=CMMsg.NO_EFFECT)
		&&(msg.amISource(this))
		&&(msg.target()!=null)
		&&(msg.target()!=this)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&(msg.target() instanceof MOB)
		&&(location()==((MOB)msg.target()).location()))
		{
			MOB target=(MOB)msg.target();
			// and now, the consequences of range
			if((location()!=null)
			   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			   &&(rangeToTarget()>maxRange(msg.tool())))
			{
				String newstr="<S-NAME> advance(s) at ";
				msg.modify(this,target,null,CMMsg.MSG_ADVANCE,newstr+target.name(),CMMsg.MSG_ADVANCE,newstr+"you",CMMsg.MSG_ADVANCE,newstr+target.name());
				boolean ok=location().okMessage(this,msg);
				if(ok) setAtRange(rangeToTarget()-1);
				if(victim!=null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverEnvStats();
				}
				recoverEnvStats();
				return ok;
			}
			else
			if(msg.targetMinor()==CMMsg.TYP_RETREAT)
			{
				if(curState().getMovement()<25)
				{
					tell("You are too tired.");
					return false;
				}
				if((location()!=null)
				   &&(rangeToTarget()>=location().maxRange()))
				{
					tell("You cannot retreat any further.");
					return false;
				}
				curState().adjMovement(-25,maxState());
				setAtRange(rangeToTarget()+1);
				if(victim!=null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverEnvStats();
				}
				recoverEnvStats();
			}
			else
			if(msg.tool()!=null)
			{
				int useRange=rangeToTarget();
				Environmental tool=msg.tool();
				if(getVictim()!=null)
				{
					if(getVictim()==target)
						useRange=rangeToTarget();
					else
					{
						if(target.getVictim()==this)
							useRange=target.rangeToTarget();
						else
							useRange=maxRange(tool);
					}
				}
				if((useRange>=0)&&(maxRange(tool)<useRange))
				{
					mob.tell("You are too far away from "+target.name()+" to use "+tool.name()+".");
					return false;
				}
				else
				if((useRange>=0)&&(minRange(tool)>useRange))
				{
					mob.tell("You are too close to "+target.name()+" to use "+tool.name()+".");
					if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
					&&(tool instanceof Weapon)
					&&(!((Weapon)tool).amWearingAt(Item.INVENTORY)))
						CommonMsgs.remove(this,(Weapon)msg.tool(),false);
					return false;
				}
			}
		}

		if((msg.targetCode()!=CMMsg.NO_EFFECT)&&(msg.amITarget(this)))
		{
			if((amDead())||(location()==null))
				return false;
			if(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			{
				if((msg.amISource(this))
				&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
				&&((msg.tool()==null)||(!(msg.tool() instanceof Ability))||(!((Ability)msg.tool()).isNowAnAutoEffect())))
				{
					mob.tell("You like yourself too much.");
					if(victim==this) victim=null;
					return false;
				}

				if(!mayIFight(mob))
				{
					mob.tell("You are not allowed to attack "+name()+".");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if((!isMonster())&&(!mob.isMonster())
				&&(soulMate()==null)
				&&(mob.soulMate()==null)
				&&(!isASysOp(location()))&&(!mob.isASysOp(mob.location()))
				&&(mob.envStats().level()>envStats().level()+CommonStrings.getPKillLevelDiff()))
				{
					mob.tell("That is not EVEN a fair fight.");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if(amFollowing()==mob) setFollowing(null);

				if(isInCombat())
				{
					if((rangeToTarget()>0)
					&&(getVictim()!=msg.source())
					&&(msg.source().getVictim()==this)
					&&(msg.source().rangeToTarget()==0))
					{
					    setVictim(msg.source());
						setAtRange(0);
					}
				}

				if((msg.targetMinor()!=CMMsg.TYP_WEAPONATTACK)&&(msg.value()<=0))
				{
					int chanceToFail=Integer.MIN_VALUE;
					int saveCode=-1;
					for(int c=0;c<CharStats.affectTypeMap.length;c++)
						if(msg.targetMinor()==CharStats.affectTypeMap[c])
						{	saveCode=c; chanceToFail=charStats().getSave(c); break;}
					if(chanceToFail>Integer.MIN_VALUE)
					{
						chanceToFail+=(envStats().level()-msg.source().envStats().level());
						if(chanceToFail<5)
							chanceToFail=5;
						else
						if(chanceToFail>95)
						   chanceToFail=95;

						if(Dice.rollPercentage()<chanceToFail)
						{
							CommonStrings.resistanceMsgs(msg,msg.source(),this);
							msg.setValue(msg.value()+1);
						}
					}
				}
			}

			if((rangeToTarget()>=0)&&(!isInCombat()))
				setAtRange(-1);

			switch(msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_DRINK:
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_THROW:
			case CMMsg.TYP_EAT:
			case CMMsg.TYP_FILL:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_UNLOCK:
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_DISMOUNT:
				mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
				return false;
			case CMMsg.TYP_GIVE:
				if(msg.tool()==null) return false;
				if(!(msg.tool() instanceof Item)) return false;
				if(getWearPositions(Item.ON_ARMS)==0)
				{
					msg.source().tell(name()+" is unable to accept that from you.");
					return false;
				}
				if(!Sense.canBeSeenBy(msg.tool(),this))
				{
					mob.tell(name()+" can't see what you are giving.");
					return false;
				}
				FullMsg msg2=new FullMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP,null);
				if(!location().okMessage(msg.source(),msg2))
					return false;
				if((msg.target()!=null)&&(msg.target() instanceof MOB))
				{
					msg2=new FullMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET,null);
					if(!location().okMessage(msg.target(),msg2))
					{
						mob.tell(msg.target().name()+" cannot seem to accept "+msg.tool().name()+".");
						return false;
					}
				}
				break;
			case CMMsg.TYP_FOLLOW:
				if(totalFollowers()+mob.totalFollowers()>=maxFollowers())
				{
					mob.tell(name()+" can't accept any more followers.");
					return false;
				}
                if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF)>0)
				&&(!isMonster())
				&&(!mob.isMonster())
				&&(!isASysOp(location()))
				&&(!mob.isASysOp(location())))
                {
					if(envStats.level() > (mob.envStats().level() + CommonStrings.getIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF)))
					{
						mob.tell(name() + " is too advanced for you.");
						return false;
					}
					if(envStats.level() < (mob.envStats().level() - CommonStrings.getIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF)))
					{
						mob.tell(name() + " is too inexperienced for you.");
						return false;
					}
                }
				break;
			}
		}
		return true;
	}

	public void tell(MOB source, Environmental target, Environmental tool, String msg)
	{
		if(mySession!=null)
			mySession.stdPrintln(source,target,tool,msg);

	}

	public void tell(String msg)
	{
		tell(this,this,null,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(getMyDeity()!=null)
		   getMyDeity().executeMsg(this,msg);

		if(charStats!=null)
		{
			charStats().getCurrentClass().executeMsg(this,msg);
			charStats().getMyRace().executeMsg(this,msg);
		}

		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)	B.executeMsg(this,msg);
		}

		MOB mob=msg.source();

		boolean asleep=Sense.isSleeping(this);
		boolean canseesrc=Sense.canBeSeenBy(msg.source(),this);
		boolean canhearsrc=Sense.canBeHeardBy(msg.source(),this);

		// first do special cases...
		if((msg.targetCode()!=CMMsg.NO_EFFECT)&&(msg.amITarget(this)))
		{
			// healing by itself is pure happy
			if(msg.targetMinor()==CMMsg.TYP_HEALING)
			{
				int amt=msg.value();
				if((amt>0)&&(!amDead))
					curState().adjHitPoints(amt,maxState());
			}
			else
			if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			{
				int dmg=msg.value();
				synchronized(this)
				{
					if((dmg>0)&&(!amDead))
					{
						if((!curState().adjHitPoints(-dmg,maxState()))
						&&(curState().getHitPoints()<1)
						&&(location()!=null))
							MUDFight.postDeath(msg.source(),this,msg);
						else
						if((curState().getHitPoints()<getWimpHitPoint())&&(isInCombat()))
							MUDFight.postPanic(this,msg);
					}
				}
			}
		}

		// now go on to source activities
		if((msg.sourceCode()!=CMMsg.NO_EFFECT)&&(msg.amISource(this)))
		{
			if(Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
				if((msg.target() instanceof MOB)&&(getVictim()!=msg.target()))
				{
					establishRange(this,(MOB)msg.target(),msg.tool());
					setVictim((MOB)msg.target());
				}

			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_PANIC:
				CommonMsgs.flee(this,"");
				break;
			case CMMsg.TYP_EXPCHANGE:
				{
					MOB victim=null;
					if(msg.target() instanceof MOB)
						victim=(MOB)msg.target();

					if(msg.value()>=0)
						charStats().getCurrentClass().gainExperience(this,
																	 victim,
																	 msg.targetMessage(),
																	 msg.value(),
																	 Util.s_bool(msg.othersMessage()));
					else
						charStats().getCurrentClass().loseExperience(this,-msg.value());
				}
				break;
			case CMMsg.TYP_DEATH:
				if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
					MUDFight.justDie((MOB)msg.tool(),this);
				else
					MUDFight.justDie(null,this);
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_REBUKE:
				if(((msg.target()==null)&&(getLeigeID().length()>0))
				||((msg.target()!=null)&&(msg.target().Name().equals(getLeigeID()))))
					setLeigeID("");
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_SERVE:
				if((msg.target()!=null)&&(!(msg.target() instanceof Deity)))
					setLeigeID(msg.target().Name());
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_EXAMINESOMETHING:
				if((Sense.canBeSeenBy(this,mob))&&(msg.amITarget(this)))
				{
					StringBuffer myDescription=new StringBuffer("");
					if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
						myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc : "+text()+"\n\r"+description()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().roomID())+"\n\r");
					if(!isMonster())
					{
						String levelStr=charStats().displayClassLevel(this,false);
						myDescription.append(name()+" the "+charStats().raceName()+" is a "+levelStr+".\n\r");
					}
					if(envStats().height()>0)
						myDescription.append(charStats().HeShe()+" is "+envStats().height()+" inches tall and weighs "+baseEnvStats().weight()+" pounds.\n\r");
					myDescription.append(healthText()+"\n\r\n\r");
					myDescription.append(description()+"\n\r\n\r");
					myDescription.append(charStats().HeShe()+" is wearing:\n\r"+CommonMsgs.getEquipment(msg.source(),this));
					tell(myDescription.toString());
				}
				break;
			case CMMsg.TYP_READSOMETHING:
				if((Sense.canBeSeenBy(this,mob))&&(msg.amITarget(this)))
					tell("There is nothing written on "+name());
				break;
			case CMMsg.TYP_SIT:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_SLEEP:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SLEEPING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_QUIT:
				if(mob.isInCombat())
				{
					CommonMsgs.flee(mob,"NOWHERE");
					mob.makePeace();
				}
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_STAND:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_RECALL:
				if((msg.target()!=null) && (msg.target() instanceof Room) && (location() != msg.target()))
				{
					tell(msg.source(),null,msg.tool(),msg.targetMessage());
					location().delInhabitant(this);
					((Room)msg.target()).addInhabitant(this);
					((Room)msg.target()).showOthers(mob,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of the Java Plain.");
					setLocation(((Room)msg.target()));
					recoverEnvStats();
					recoverCharStats();
					recoverMaxState();
					CommonMsgs.look(mob,true);
				}
				break;
			case CMMsg.TYP_FOLLOW:
				if((msg.target()!=null)&&(msg.target() instanceof MOB))
				{
					setFollowing((MOB)msg.target());
					tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_NOFOLLOW:
				setFollowing(null);
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_ENTER:
				lastMoveTime=System.currentTimeMillis();
				movesSinceTick++;
				break;
			default:
				// you pretty much always know what you are doing, if you can do it.
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			}
		}
		else
		if((msg.targetCode()!=CMMsg.NO_EFFECT)&&(msg.amITarget(this)))
		{
			int targetMajor=msg.targetMajor();

			// two special cases were already handled above
			if((msg.targetMinor()!=CMMsg.TYP_HEALING)&&(msg.targetMinor()!=CMMsg.TYP_DAMAGE))
			{
				// but there might still be a few more...
				if((Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
				&&(!amDead))
				{
					if((!isInCombat())&&(location().isInhabitant((MOB)msg.source())))
					{
						establishRange(this,msg.source(),msg.tool());
						setVictim(msg.source());
					}
					if(isInCombat())
					{
						if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
						{
							Weapon weapon=msg.source().myNaturalWeapon();
							if((msg.tool()!=null)&&(msg.tool() instanceof Weapon))
								weapon=(Weapon)msg.tool();
							if(weapon!=null)
							{
								boolean isHit=(Dice.normalizeAndRollLess(msg.source().adjustedAttackBonus(this)+(adjustedArmor()-50)));
								MUDFight.postWeaponDamage(msg.source(),this,weapon,isHit);
								msg.setValue(1);
							}
						}
						else
						if((msg.tool()!=null)
						&&(msg.tool() instanceof Weapon))
							MUDFight.postWeaponDamage(msg.source(),this,(Weapon)msg.tool(),true);
					}
					if(Sense.isSitting(this)||Sense.isSleeping(this))
						CommonMsgs.stand(this,true);

				}
				else
				if((msg.targetMinor()==CMMsg.TYP_GIVE)
				 &&(msg.tool()!=null)
				 &&(msg.tool() instanceof Item))
				{
					FullMsg msg2=new FullMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP,null);
					location().send(this,msg2);
					msg2=new FullMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET,null);
					location().send(this,msg2);
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
				&&(Sense.canBeSeenBy(this,mob)))
				{
					StringBuffer myDescription=new StringBuffer("");
					if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
						myDescription.append(Name()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc :'"+text()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().roomID())+"\n\r"+description()+"\n\r");
					if(!isMonster())
					{
						String levelStr=charStats().displayClassLevel(this,false);
						myDescription.append(name()+" the "+charStats().raceName()+" is a "+levelStr+".\n\r");
					}
					if(envStats().height()>0)
						myDescription.append(charStats().HeShe()+" is "+envStats().height()+" inches tall and weighs "+baseEnvStats().weight()+" pounds.\n\r");
					myDescription.append(healthText()+"\n\r\n\r");
					myDescription.append(description()+"\n\r\n\r");
					myDescription.append(charStats().HeShe()+" is wearing:\n\r"+CommonMsgs.getEquipment(msg.source(),this));
					mob.tell(myDescription.toString());
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_REBUKE)
				&&(msg.source().Name().equals(getLeigeID())))
					setLeigeID("");
				else
				if(Util.bset(targetMajor,CMMsg.MASK_CHANNEL))
				{
					if((playerStats()!=null)
					&&(!Util.isSet(playerStats().getChannelMask(),((msg.targetCode()-CMMsg.MASK_CHANNEL)-CMMsg.TYP_CHANNEL))))
						tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
				}
			}

			// now do the tells
			if((Util.bset(targetMajor,CMMsg.MASK_SOUND))
			&&(canhearsrc)&&(!asleep))
			{
				if((msg.targetMinor()==CMMsg.TYP_SPEAK)
				 &&(msg.source()!=null)
				 &&(playerStats()!=null))
					playerStats().setReplyTo(msg.source());
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			}
			else
			if((Util.bset(targetMajor,CMMsg.MASK_GENERAL))
			||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			||(msg.targetMinor()==CMMsg.TYP_HEALING))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if((Util.bset(targetMajor,CMMsg.MASK_EYES))
			&&((!asleep)&&(canseesrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if(((Util.bset(targetMajor,CMMsg.MASK_HANDS))
				||(Util.bset(targetMajor,CMMsg.MASK_MOVE))
				||((Util.bset(targetMajor,CMMsg.MASK_MOUTH))
				   &&(!Util.bset(targetMajor,CMMsg.MASK_SOUND))))
			&&(!asleep)&&((canhearsrc)||(canseesrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
		}
		else
		if((msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(!msg.amISource(this))
		&&(!msg.amITarget(this)))
		{
			int othersMajor=msg.othersMajor();
			int othersMinor=msg.othersMinor();

			if(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)&&(msg.target() instanceof MOB))
				fightingFollowers((MOB)msg.target(),msg.source());

			if((othersMinor==CMMsg.TYP_ENTER) // exceptions to movement
			||(othersMinor==CMMsg.TYP_FLEE)
			||(othersMinor==CMMsg.TYP_LEAVE))
			{
				if(((!asleep)||(msg.othersMinor()==CMMsg.TYP_ENTER))
				&&(Sense.canSenseMoving(msg.source(),this)))
					tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(Util.bset(othersMajor,CMMsg.MASK_CHANNEL))
			{
				if((playerStats()!=null)
				&&(!Util.isSet(playerStats().getChannelMask(),((msg.othersCode()-CMMsg.MASK_CHANNEL)-CMMsg.TYP_CHANNEL))))
					tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if((Util.bset(othersMajor,CMMsg.MASK_SOUND))
			&&(!asleep)
			&&(canhearsrc))
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			else
			if(((Util.bset(othersMajor,CMMsg.MASK_EYES))
			||(Util.bset(othersMajor,CMMsg.MASK_HANDS))
			||(Util.bset(othersMajor,CMMsg.MASK_GENERAL)))
			&&((!asleep)&&(canseesrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			else
			if(((Util.bset(othersMajor,CMMsg.MASK_MOVE))
				||((Util.bset(othersMajor,CMMsg.MASK_MOUTH))&&(!Util.bset(othersMajor,CMMsg.MASK_SOUND))))
			&&(!asleep)
			&&((canseesrc)||(canhearsrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());

			if((msg.othersMinor()==CMMsg.TYP_DEATH)&&(victim!=null))
			{
				MOB victim=this.victim;
				if(victim==msg.source())
					setVictim(null);
				else
				if((victim.getVictim()==null)||(victim.getVictim()==msg.source()))
				{
					if((amFollowing()!=null)&&(victim.amFollowing()!=null)&&(amFollowing()==victim.amFollowing()))
						setVictim(null);
					else
					{
						victim.setAtRange(-1);
						victim.setVictim(this);
					}
				}
			}
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item I=(Item)fetchInventory(i);
			if(I!=null)
				I.executeMsg(this,msg);
		}

		for(int i=0;i<numEffects();i++)
		{
			Ability A=(Ability)fetchEffect(i);
			if(A!=null)
				A.executeMsg(this,msg);
		}
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	public int movesSinceLastTick(){return movesSinceTick;}
	public long lastMovedDateTime(){return lastMoveTime;}
	public long getTickStatus(){return tickStatus;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(pleaseDestroy)
			return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==MudHost.TICK_MOB)
		{
			movesSinceTick=0;
			if(amDead)
			{
				tickStatus=Tickable.STATUS_DEAD;
				if(isMonster())
					if((envStats().rejuv()<Integer.MAX_VALUE)
					&&(baseEnvStats().rejuv()>0))
					{
						envStats().setRejuv(envStats().rejuv()-1);
						if(envStats().rejuv()<0)
						{
							bringToLife(getStartRoom(),true);
							location().showOthers(this,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
						}
					}
					else
					{
						tickStatus=Tickable.STATUS_END;
						if(soulMate()==null) destroy();
						tickStatus=Tickable.STATUS_NOT;
						lastTickedDateTime=System.currentTimeMillis();
						return false;
					}
				tickStatus=Tickable.STATUS_END;
			}
			else
			if(location()!=null)
			{
				tickStatus=Tickable.STATUS_ALIVE;
				curState().recoverTick(this,maxState);
				curState().expendEnergy(this,maxState,false);
				if((!Sense.canBreathe(this))&&(!Sense.isGolem(this)))
				{
					location().show(this,this,CMMsg.MSG_OK_VISUAL,("^Z<S-NAME> can't breathe!^.^?")+CommonStrings.msp("choke.wav",10));
					MUDFight.postDamage(this,this,null,(int)Math.round(Util.mul(Math.random(),baseEnvStats().level()+2)),CMMsg.MSG_OK_VISUAL,-1,null);
				}
				if(isInCombat())
				{
					tickStatus=Tickable.STATUS_FIGHT;
					peaceTime=0;
					Item weapon=fetchWieldedItem();

					if((Util.bset(getBitmap(),MOB.ATT_AUTODRAW))&&(weapon==null))
					{
						CommonMsgs.draw(this,false,true);
						weapon=fetchWieldedItem();
					}

					double curSpeed=Math.floor(speeder);
					speeder+=Sense.isSitting(this)?(envStats().speed()/2.0):envStats().speed();
					int numAttacks=(int)Math.round(Math.floor(speeder-curSpeed));
					if(Sense.aliveAwakeMobile(this,true))
					{
						for(int s=0;s<numAttacks;s++)
						{
							if((!amDead())
							&&(curState().getHitPoints()>0)
							&&(isInCombat())
							&&((s==0)||(!Sense.isSitting(this))))
							{
								if((weapon!=null)&&(weapon.amWearingAt(Item.INVENTORY)))
									weapon=this.fetchWieldedItem();
								if((!Util.bset(getBitmap(),MOB.ATT_AUTOMELEE)))
									MUDFight.postAttack(this,victim,weapon);
								else
								{
									boolean inminrange=(rangeToTarget()>=minRange(weapon));
									boolean inmaxrange=(rangeToTarget()<=maxRange(weapon));
									if((!inminrange)&&(curState().getMovement()>=25))
									{
										FullMsg msg=new FullMsg(this,victim,CMMsg.MSG_RETREAT,"<S-NAME> retreat(s) before <T-NAME>.");
										if(location().okMessage(this,msg))
											location().send(this,msg);
									}
									else
									if((weapon!=null)&&inminrange&&inmaxrange)
										MUDFight.postAttack(this,victim,weapon);
								}
							}
							else
								break;
						}

						if(Dice.rollPercentage()>(charStats().getStat(CharStats.CONSTITUTION)*4))
							curState().adjMovement(-1,maxState());
					}

					if(!isMonster())
					{
						MOB target=this.getVictim();
						if((target!=null)&&(!target.amDead())&&(Sense.canBeSeenBy(target,this)))
							session().print(target.healthText()+"\n\r\n\r");
					}
				}
				else
				{
					speeder=0.0;
					peaceTime+=MudHost.TICK_TIME;
					if(Util.bset(getBitmap(),MOB.ATT_AUTODRAW)
					&&(peaceTime>=SHEATH_TIME)
					&&(Sense.aliveAwakeMobile(this,true)))
						CommonMsgs.sheath(this,true);
				}
				dequeCommand();
				tickStatus=Tickable.STATUS_OTHER;
				if(!isMonster())
				{
					if(Sense.isSleeping(this))
						curState().adjFatigue(-CharState.REST_PER_TICK,maxState());
					else
					{
						curState().adjFatigue(MudHost.TICK_TIME,maxState());
				        if((curState().getFatigue()>CharState.FATIGUED_MILLIS)
						&&(!isMonster())
                     	&&(Dice.rollPercentage()==1))
                     	{
                        	Ability theYawns = CMClass.getAbility("Disease_Yawning");
                        	if(theYawns!=null) theYawns.invoke(this, this, true);
                     	}
					}
				}

				if((riding()!=null)&&(CoffeeUtensils.roomLocation(riding())!=location()))
					setRiding(null);
				if((!isMonster())&&(((++minuteCounter)*MudHost.TICK_TIME)>60000))
				{
					minuteCounter=0;
					setAgeHours(AgeHours+1);
					if(AgeHours>60000)
					{
						if(((AgeHours%120)==0)&&(Dice.rollPercentage()==1))
						{
							Ability A=CMClass.getAbility("Disease_Cancer");
							if((A!=null)&&(fetchEffect(A.ID())==null))
								A.invoke(this,this,true);
						}
						else
						if(((AgeHours%1200)==0)&&(Dice.rollPercentage()<25))
						{
							Ability A=CMClass.getAbility("Disease_Arthritis");
							if((A!=null)&&(fetchEffect(A.ID())==null))
								A.invoke(this,this,true);
						}
					}
				}
			}

			Vector expenseAffects=null;
			if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACONSUMETIME)>0)
			&&(CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACONSUMEAMT)>0)
			&&((--manaConsumeCounter)<=0))
			{
				expenseAffects=new Vector();
				manaConsumeCounter=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACONSUMETIME);
			}

			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					tickStatus=Tickable.STATUS_AFFECT+a;
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					else
					if((expenseAffects!=null)
					&&(!A.isAutoInvoked())
					&&(A.canBeUninvoked())
					&&(A.displayText().length()>0)
                    &&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
						||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
						||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
						||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
					&&(A.usageCost(this)[0]>0))
						expenseAffects.addElement(A);

					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}

            if((expenseAffects!=null)&&(expenseAffects.size()>0))
            {
                int basePrice=1;
                if(fetchEffect("Prop_MagicBurn1")!=null)
					basePrice=2;  // No way to make a prop that actually increases mana spent

				switch(CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACONSUMEAMT))
				{
				case -100: basePrice=basePrice*envStats().level(); break;
				case -200:
					{
						int total=0;
						for(int a1=0;a1<expenseAffects.size();a1++)
						{
							int lql=CMAble.lowestQualifyingLevel(((Ability)expenseAffects.elementAt(a1)).ID());
							if(lql>0)
								total+=lql;
							else
								total+=1;
						}
						basePrice=basePrice*(total/expenseAffects.size());
					}
					break;
				default:
					basePrice=basePrice*CommonStrings.getIntVar(CommonStrings.SYSTEMI_MANACONSUMEAMT);
					break;
				}

                // 1 per tick per level per msg.  +1 to the affects so that way it's about
                // 3 cost = 1 regen... :)
                int reallyEat=basePrice*(expenseAffects.size()+1);
                while(curState().getMana()<reallyEat)
                {
                    location().show(this,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> strength of will begins to crumble.");
                    //pick one and kill it
                    Ability A=(Ability)expenseAffects.elementAt(Dice.roll(1,expenseAffects.size(),-1));
                    A.unInvoke();
                    expenseAffects.remove(A);
                    reallyEat=basePrice*expenseAffects.size();
                }
                if(reallyEat>0)
                    curState().adjMana( -reallyEat, maxState());
            }

			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				if(B!=null) B.tick(ticking,tickID);
			}

			tickStatus=Tickable.STATUS_CLASS;
			charStats().getCurrentClass().tick(ticking,tickID);
			tickStatus=Tickable.STATUS_RACE;
			charStats().getMyRace().tick(ticking,tickID);
			tickStatus=Tickable.STATUS_END;
		}
		tickStatus=Tickable.STATUS_NOT;
		lastTickedDateTime=System.currentTimeMillis();
		return !pleaseDestroy;
	}

	public boolean isMonster(){	return (mySession==null);}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean isASysOp(Room of)
	{
		if(baseCharStats()==null) return false;
		if(baseCharStats().getClassLevel("Archon")>=0)
			return true;
		if(of==null) return false;
		if(of.getArea()==null) return false;
		if(of.getArea().amISubOp(Username))
			return true;
		return false;
	}

	public void confirmWearability()
	{
		Race R=charStats().getMyRace();
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if((item!=null)&&(!item.amWearingAt(Item.INVENTORY)))
			{
				long oldCode=item.rawWornCode();
				item.unWear();
				int msgCode=CMMsg.MSG_WEAR;
				if((oldCode&Item.WIELD)>0)
					msgCode=CMMsg.MSG_WIELD;
				else
				if((oldCode&Item.HELD)>0)
					msgCode=CMMsg.MSG_HOLD;
				FullMsg msg=new FullMsg(this,item,null,CMMsg.NO_EFFECT,null,msgCode,null,CMMsg.NO_EFFECT,null);
				if((R.okMessage(this,msg))&&(item.okMessage(item,msg)))
				   item.wearAt(oldCode);
			}
		}
	}

	public void addInventory(Item item)
	{
		item.setOwner(this);
		inventory.addElement(item);
		item.recoverEnvStats();
	}
	public void delInventory(Item item)
	{
		inventory.removeElement(item);
		item.recoverEnvStats();
	}
	public int inventorySize()
	{
		return inventory.size();
	}
	public Item fetchInventory(int index)
	{
		try
		{
			return (Item)inventory.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Item fetchInventory(String itemName)
	{
		Item item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_ANY,true);
		if(item==null) item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_ANY,false);
		return item;
	}
	public Item fetchInventory(Item goodLocation, String itemName)
	{
		Item item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_ANY,true);
		if(item==null) item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_ANY,false);
		return item;
	}
	public Item fetchCarried(Item goodLocation, String itemName)
	{
		Item item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_UNWORNONLY,true);
		if(item==null) item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,goodLocation,Item.WORN_REQ_UNWORNONLY,false);
		return item;
	}
	public Item fetchWornItem(String itemName)
	{
		Item item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_WORNONLY,true);
		if(item==null) item=(Item)EnglishParser.fetchAvailableItem(inventory,itemName,null,Item.WORN_REQ_WORNONLY,false);
		return item;
	}
	public void addFollower(MOB follower)
	{
		if((follower!=null)&&(!followers.contains(follower)))
		{
			followers.addElement(follower);
		}
	}

	public void delFollower(MOB follower)
	{
		if((follower!=null)&&(followers.contains(follower)))
		{
			followers.removeElement(follower);
		}
	}
	public int numFollowers()
	{
		return followers.size();
	}
	public MOB fetchFollower(int index)
	{
		try
		{
			return (MOB)followers.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public MOB fetchFollower(MOB thisOne)
	{
		if(followers.contains(thisOne))
			return thisOne;
		return null;
	}
	public MOB fetchFollower(String ID)
	{
		MOB mob=(MOB)EnglishParser.fetchEnvironmental(followers,ID,true);
		if (mob==null) mob=(MOB)EnglishParser.fetchEnvironmental(followers,ID,false);
		return mob;
	}
	public boolean willFollowOrdersOf(MOB mob)
	{
		if(mob.isASysOp(mob.location())
		||(amFollowing()==mob)
		||(getLeigeID().equals(mob.Name()))
		||(CoffeeUtensils.doesOwnThisProperty(mob,getStartRoom())))
			return true;
		if((getClanID().length()>0)&&(getClanID().equals(mob.getClanID())))
		{
			Clan C=Clans.getClan(getClanID());
			if((C!=null)
			&&(C.allowedToDoThis(mob,Clans.FUNC_CLANCANORDERUNDERLINGS)>=0)
			&&(mob.getClanRole()>getClanRole()))
				return true;
		}
		return false;
	}
	public MOB amFollowing()
	{
		if(amFollowing!=null)
		{
			if(amFollowing.fetchFollower(this)==null)
				amFollowing=null;
		}
		return amFollowing;
	}
	public void setFollowing(MOB mob)
	{
		if(mob==null)
		{
			if(amFollowing!=null)
			{
				if(amFollowing.fetchFollower(this)!=null)
					amFollowing.delFollower(this);
			}
		}
		else
		if(mob.fetchFollower(this)==null)
			mob.addFollower(this);
		amFollowing=mob;
	}
	private void addFollowers(MOB mob, Hashtable toThis)
	{
		if(toThis.get(mob)==null)
		   	toThis.put(mob,mob);

		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if((follower!=null)&&(toThis.get(follower)==null))
			{
				toThis.put(follower,follower);
				addFollowers(follower,toThis);
			}
		}
	}

	public Hashtable getRideBuddies(Hashtable list)
	{
		if(list==null) return list;
		if(list.get(this)==null) list.put(this,this);
		if(riding()!=null)
			riding().getRideBuddies(list);
		return list;
	}

	public Hashtable getGroupMembers(Hashtable list)
	{
		if(list==null) return list;
		if(list.get(this)==null) list.put(this,this);
		if(amFollowing()!=null)
			amFollowing().getGroupMembers(list);
		for(int f=0;f<numFollowers();f++)
		{
			MOB follower=fetchFollower(f);
			if((follower!=null)&&(list.get(follower)==null))
				follower.getGroupMembers(list);
		}
		return list;
	}

	public boolean isEligibleMonster()
	{
		if(!isMonster())
			return false;
		MOB followed=amFollowing();
		if(followed!=null)
			if(!followed.isMonster())
				return false;
		return true;

	}

	public MOB soulMate()
	{
		return soulMate;
	}

	public void setSoulMate(MOB mob)
	{
		soulMate=mob;
	}

	public void addAbility(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numLearnedAbilities();a++)
		{
			Ability A=fetchAbility(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		abilities.addElement(to);
	}
	public void delAbility(Ability to)
	{
		abilities.removeElement(to);
	}
	public int numLearnedAbilities()
	{
		return abilities.size();
	}
	public int numAbilities()
	{
		return abilities.size()+charStats().getMyRace().racialAbilities(this).size();
	}

	public Ability fetchAbility(int index)
	{
		try
		{
			if(index<abilities.size())
				return (Ability)abilities.elementAt(index);
			return (Ability)charStats().getMyRace().racialAbilities(this).elementAt(index-abilities.size());
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAbility(String ID)
	{
		Ability A=null;
		for(int a=0;a<numLearnedAbilities();a++)
		{
			A=fetchAbility(a);
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		for(int a=0;a<charStats().getMyRace().racialAbilities(this).size();a++)
		{
			A=(Ability)charStats().getMyRace().racialAbilities(this).elementAt(a);
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		A=(Ability)EnglishParser.fetchEnvironmental(abilities,ID,false);
		if(A==null) A=(Ability)EnglishParser.fetchEnvironmental(charStats().getMyRace().racialAbilities(this),ID,false);
		return A;
	}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numEffects()
	{
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		if(fetchBehavior(to.ID())!=null) return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}
	public int freeWearPositions(long wornCode)
	{
		int x=getWearPositions(wornCode);
		if(x<=0) return 0;
		x=x-numWearingHere(wornCode);
		if(x<=0) return 0;
		return x;
	}
	public int getWearPositions(long wornCode)
	{
		if((charStats().getMyRace().forbiddenWornBits()&wornCode)>0)
			return 0;
		if(wornCode==Item.FLOATING_NEARBY)
			return 6;
		int total;
		int add=0;
		boolean found=false;
		for(int i=0;i<Race.BODY_WEARGRID.length;i++)
		{
			if((Race.BODY_WEARGRID[i][0]>0)
			&&((Race.BODY_WEARGRID[i][0]&wornCode)==wornCode))
			{
				found=true;
				total=charStats().getBodyPart(i);
				if(Race.BODY_WEARGRID[i][1]<0)
				{
					if(total>0) return 0;
				}
				else
				if(total<1)
				{
					return 0;
				}
				else
				if(i==Race.BODY_HAND)
				{
					// casting is ok here since these are all originals
					// that fall below the int/long fall.
					if(wornCode>Integer.MAX_VALUE)
						add+=total;
					else
					switch((int)wornCode)
					{
					case (int)Item.ON_HANDS:
						if(total<2)
							add+=1;
						else
							add+=total/2;
						break;
					case (int)Item.WIELD:
					case (int)Item.ON_RIGHT_FINGER:
					case (int)Item.ON_RIGHT_WRIST:
						add+=1; break;
					case (int)Item.HELD:
					case (int)Item.ON_LEFT_FINGER:
					case (int)Item.ON_LEFT_WRIST:
						add+=total-1;
						break;
					default:
						add+=total; break;
					}
				}
				else
				{
					int num=total/((int)Race.BODY_WEARGRID[i][1]);
					if(num<1)
						add+=1;
					else
						add+=num;
				}
			}
		}
		if(!found) return 1;
		return add;
	}

	public int numWearingHere(long wornCode)
	{
		int num=0;
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				num++;
		}
		return num;
	}
	public Item fetchFirstWornItem(long wornCode)
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				return thisItem;
		}
		return null;
	}

	public Item fetchWieldedItem()
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(Item.WIELD)))
				return thisItem;
		}
		return null;
	}

	public boolean isMine(Environmental env)
	{
		if(env instanceof Item)
		{
			if(inventory.contains(env)) return true;
			return false;
		}
		else
		if(env instanceof MOB)
		{
			if(followers.contains(env)) return true;
			return false;
		}
		else
		if(env instanceof Ability)
		{
			if(abilities.contains(env)) return true;
			if(affects.contains(env)) return true;
			return false;
		}
		return false;
	}

	public void giveItem(Item thisContainer)
	{
		// caller is responsible for recovering any env
		// stat changes!
		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));

		// ensure its out of its previous place
		Environmental owner=location();
		if(thisContainer.owner()!=null)
		{
			owner=thisContainer.owner();
			if(thisContainer.owner() instanceof Room)
				((Room)thisContainer.owner()).delItem(thisContainer);
			else
			if(thisContainer.owner() instanceof MOB)
				((MOB)thisContainer.owner()).delInventory(thisContainer);
		}
		location().delItem(thisContainer);

		thisContainer.unWear();

		if(!isMine(thisContainer))
			addInventory(thisContainer);
		thisContainer.recoverEnvStats();

		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			if(owner instanceof Room)
			{
				Room R=(Room)owner;
				for(int i=0;i<R.numItems();i++)
				{
					Item thisItem=R.fetchItem(i);
					if((thisItem!=null)&&(thisItem.container()==thisContainer))
					{
						giveItem(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
			else
			if(owner instanceof MOB)
			{
				MOB M=(MOB)owner;
				for(int i=0;i<M.inventorySize();i++)
				{
					Item thisItem=M.fetchInventory(i);
					if((thisItem!=null)&&(thisItem.container()==thisContainer))
					{
						giveItem(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
		}while(!nothingDone);
	}
	private void fightingFollowers(MOB target, MOB source)
	{
		if((source==null)||(target==null)) return;
		if(source==target) return;
		if((target==this)||(source==this)) return;
		if((target.location()!=location())||(target.location()!=source.location()))
			return;
		if((Util.bset(getBitmap(),MOB.ATT_AUTOASSIST))) return;
		if(isInCombat()) return;

		if((amFollowing()==target)
		||(target.amFollowing()==this)
		||((target.amFollowing()!=null)&&(target.amFollowing()==this.amFollowing())))
			setVictim(source);//MUDFight.postAttack(this,source,fetchWieldedItem());
		else
		if((amFollowing()==source)
		||(source.amFollowing()==this)
		||((source.amFollowing()!=null)&&(source.amFollowing()==this.amFollowing())))
			setVictim(target);//MUDFight.postAttack(this,target,fetchWieldedItem());
	}

	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+baseEnvStats().level();
		case 2: return ""+baseEnvStats().ability();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: baseEnvStats().setLevel(Util.s_int(val)); break;
		case 2: baseEnvStats().setAbility(Util.s_int(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdMOB)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
