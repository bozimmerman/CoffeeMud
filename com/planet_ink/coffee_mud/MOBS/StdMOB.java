package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.db.*;
public class StdMOB implements MOB
{
	protected String Username="";
	protected String Password="";
	protected Calendar LastDateTime=Calendar.getInstance();
	protected long channelMask;

	protected CharStats baseCharStats=new CharStats();
	protected CharStats charStats=new CharStats();

	protected Stats envStats=new Stats();
	protected Stats baseEnvStats=new Stats();

	protected boolean amDead=false;
	protected Room location=null;

	protected Session mySession=null;
	protected boolean pleaseDestroy=false;
	private boolean readSysopMsgs;

	protected String description="";
	protected String displayText="";
	protected String miscText="";

	/* instantiated item types word, contained, owned*/
	protected Vector inventory=new Vector();

	/* instantiated creature types listed as followers*/
	protected Vector followers=new Vector();

	/* All Ability codes, including languages*/
	protected Vector abilities=new Vector();

	/* instantiated affects on this user*/
	protected Vector affects=new Vector();

	protected Vector behaviors=new Vector();

	// gained attributes
	protected int Experience=0;
	protected int ExpNextLevel=1000;
	protected int Practices=0;
	protected int Trains=0;
	protected long AgeHours=0;
	protected int Money=0;
	public long getAgeHours(){return AgeHours;}
	public int getPractices(){return Practices;}
	public int getExperience(){return Experience;}
	public int getExpNextLevel(){return ExpNextLevel;}
	public int getExpNeededLevel()
	{
		if(ExpNextLevel<=getExperience())
			ExpNextLevel=getExperience()+1000;
		return ExpNextLevel-getExperience();
	}
	public int getTrains(){return Trains;}
	public int getMoney(){return Money;}
	public void setAgeHours(long newVal){ AgeHours=newVal;}
	public void setExperience(int newVal){ Experience=newVal; }
	public void setExpNextLevel(int newVal){ ExpNextLevel=newVal;}
	public void setPractices(int newVal){ Practices=newVal;}
	public void setTrains(int newVal){ Trains=newVal;}
	public void setMoney(int newVal){ Money=newVal;}

	protected int minuteCounter=0;

	// the core state values
	public CharState curState=new CharState();
	public CharState maxState=new CharState();
	public Calendar lastTickedDateTime=Calendar.getInstance();

	// mental characteristics
	protected int Alignment=0;
	protected String WorshipCharID="";
	protected int WimpHitPoint=0;
	protected int QuestPoint=0;
	public String getWorshipCharID(){return WorshipCharID;}
	public int getAlignment(){return Alignment;}
	public int getWimpHitPoint(){return WimpHitPoint;}
	public int getQuestPoint(){return QuestPoint;}
	public void setAlignment(int newVal) 
	{ 
		if(newVal<0) newVal=0;
		if(newVal>1000) newVal=1000;
		Alignment=newVal; 
	}
	public void setWorshipCharID(String newVal){ WorshipCharID=newVal;}
	public void setWimpHitPoint(int newVal){ WimpHitPoint=newVal;}
	public void setQuestPoint(int newVal){ QuestPoint=newVal;}

	// location!
	protected Room StartRoom=null;
	public Room getStartRoom(){return StartRoom;}
	public void setStartRoom(Room newVal){StartRoom=newVal;}


	protected MOB victim=null;
	protected MOB replyTo=null;
	protected MOB amFollowing=null;
	protected MOB soulMate=null;
	private double speeder=0.0;

	public String ID()
	{
		return Username;
	}
	public String name(){ return Username;}
	public void setName(String newName){Username=newName;}
	public String password()
	{
		return Password;
	}
	public Environmental newInstance()
	{
		return new StdMOB();
	}
	private void cloneFix(MOB E)
	{
		
		affects=new Vector();
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();
		
		baseCharStats=E.baseCharStats().cloneCharStats();
		charStats=E.charStats().cloneCharStats();
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
			Item I=(Item)E.fetchInventory(i).copyOf();
			I.setOwner(this);
			inventory.addElement(I);
		}
		for(int i=0;i<E.numAbilities();i++)
			abilities.addElement(E.fetchAbility(i).copyOf());
		for(int i=0;i<E.numBehaviors();i++)
			behaviors.addElement(E.fetchBehavior(i));

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
	public Stats envStats()
	{
		return envStats;
	}

	public boolean readSysopMsgs()
	{
		return readSysopMsgs;
	}
	public void toggleReadSysopMsgs()
	{
		readSysopMsgs=!readSysopMsgs;
		tell("Extended messages are now : "+(readSysopMsgs?"ON":"OFF"));
	}

	public Stats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		if(location()!=null)
			location().affectEnvStats(this,envStats);
		envStats().setWeight(envStats().weight()+(int)Math.round(Util.div(getMoney(),10.0)));
		if(charStats!=null)
		{
			if(charStats().getMyClass()!=null)
				charStats().getMyClass().affectEnvStats(this,envStats);
			if(charStats().getMyRace()!=null)
				charStats().getMyRace().affectEnvStats(this,envStats);
		}
		for(int i=0;i<inventory.size();i++)
		{
			Item item=(Item)inventory.elementAt(i);
			item.affectEnvStats(this,envStats);
		}
		for(int a=0;a<affects.size();a++)
		{
			Ability affect=(Ability)affects.elementAt(a);
			affect.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(Stats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public Calendar lastDateTime(){return LastDateTime;}
	public void setUserInfo(String newUsername,
							String newPassword,
							Calendar newCalendar)
	{
		Username=newUsername;
		Password=newPassword;
		LastDateTime=newCalendar.getInstance();
	}

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		charStats=baseCharStats().cloneCharStats();
		for(int a=0;a<affects.size();a++)
		{
			Ability affect=(Ability)affects.elementAt(a);
			affect.affectCharStats(this,charStats);
		}
		for(int i=0;i<inventory.size();i++)
		{
			Item item=(Item)inventory.elementAt(i);
			item.affectCharStats(this,charStats);
		}
		if(location()!=null)
			location().affectCharStats(this,charStats);
		if(charStats().getMyClass()!=null)
			charStats().getMyClass().affectCharStats(this,charStats);
		if(charStats().getMyRace()!=null)
			charStats().getMyRace().affectCharStats(this,charStats);
	}
	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=newBaseCharStats.cloneCharStats();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		if(Sense.isLight(this))
		{
			if((affected instanceof Room)&&(Sense.isInDark(affected)))
				affectableStats.setDisposition(affectableStats.disposition()-Sense.IS_DARK);
		}
	}

	public CharState curState(){return curState;}
	public CharState maxState(){return maxState;}
	public void setMaxState(CharState newState)
	{	maxState=newState.cloneCharState(); }
	public void recoverMaxState()
	{	curState=maxState.cloneCharState(); }

	public void setChannelMask(long newMask)
	{
		channelMask=newMask;
	}
	public long getChannelMask()
	{
		return channelMask;
	}

	public boolean amDead()
	{
		return amDead||pleaseDestroy;
	}

	public void destroy()
	{
		pleaseDestroy=true;
		if(location!=null)
		{
			location().delInhabitant(this);
			location().show(this,null,Affect.VISUAL_WNOISE,"<S-NAME> vanish(es) in a puff of smoke.");
		}
		setFollowing(null);
		if((!isMonster())&&(numFollowers()>0))
			MOBloader.DBUpdateFollowers(this);

		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if((follower.amFollowing()==this)&&(follower.isMonster()))
				follower.destroy();
			delFollower(follower);
		}
		if(!isMonster())
			session().killFlag=true;
		LastDateTime=Calendar.getInstance();
	}

	public MOB replyTo()
	{
		return replyTo;
	}
	public void bringToLife(Room newLocation)
	{
		if(getStartRoom()==null)
			setStartRoom((Room)MUD.getRoom("START"));
		if(getStartRoom()==null)
			setStartRoom((Room)MUD.map.elementAt(0));
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
		{
			location().addInhabitant(this);
			location().showOthers(this,null,Affect.VISUAL_WNOISE,"<S-NAME> appears!");
		}
		pleaseDestroy=false;
		
		// will ensure no duplicate ticks, this obj, this id
		ServiceEngine.startTickDown(this,ServiceEngine.MOB_TICK,1);

		for(int a=0;a<numAbilities();a++)
			fetchAbility(a).autoInvocation(this);

		location().recoverRoomStats();
		BasicSenses.look(this,null,true);
	}

	public void raiseFromDead()
	{
		amDead=false;
		recoverMaxState();
		bringToLife(getStartRoom());
	}

	public boolean isInCombat()
	{
		if(victim==null) return false;
		if((victim.location()!=location())||(victim.amDead()))
		{
			victim=null;
			return false;
		}
		return true;
	}

	public void makePeace()
	{
		MOB myVictim=victim;
		victim=null;
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
		if(victim==mob) return;

		victim=mob;
		recoverEnvStats();
		recoverCharStats();
		if(mob!=null)
		{
			mob.recoverCharStats();
			mob.recoverEnvStats();
		}

	}
	public void kill()
	{
		amDead=true;
		victim=null;

		int a=0;
		while(a<affects.size())
		{
			Ability A=(Ability)affects.elementAt(a);
			int s=affects.size();
			A.unInvoke();
			if(affects.size()==s)
				a++;
		}
		if(isMonster())
			setLocation(null);
	}

	public Room location()
	{
		return location;
	}
	public void setLocation(Room newRoom)
	{
		location=newRoom;
	}
	public Session session()
	{
		return mySession;
	}
	public void setSession(Session newSession)
	{
		mySession=newSession;
	}

	public String displayText()
	{
		String sendBack=displayText;
		if((displayText.length()==0)||(Sense.isSleeping(this))||(Sense.isSitting(this))||(isInCombat()))
		{
			sendBack=name()+" "+Sense.dispositionString(this,Sense.flag_is)+" here";
			if(isInCombat())
				sendBack+=" fighting "+getVictim().name();
			sendBack+=".";
		}
		return sendBack;
	}
	public String rawDisplayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		return description;
	}
	public void setDescription(String newDescription)
	{
		description=newDescription;
	}
	public void setMiscText(String newText)
	{
		miscText=newText;
	}
	public String text()
	{
		return miscText;
	}

	public boolean isCorrectPass(String possiblePassword)
	{
		return Password.equals(possiblePassword);
	}

	public boolean okAffect(Affect affect)
	{
		for(int i=0;i<affects.size();i++)
			if(!((Ability)fetchAffect(i)).okAffect(affect))
				return false;

		for(int i=0;i<inventorySize();i++)
			if(!((Item)fetchInventory(i)).okAffect(affect))
				return false;

		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			if(!B.okAffect(this,affect))
				return false;
		}


		MOB mob=affect.source();
		if(affect.amISource(this))
		{
			if(charStats!=null)
			{
				if(charStats().getMyClass()!=null)
					if(!charStats().getMyClass().okAffect(affect))
						return false;
				if(charStats().getMyRace()!=null)
					if(!charStats().getMyRace().okAffect(affect))
						return false;
			}

			switch(affect.sourceType())
			{
			case Affect.VISUAL:
				if(Sense.isSleeping(this))
				{
					tell("Not while you are sleeping.");
					return false;
				}
				switch(affect.sourceCode())
				{
				case Affect.VISUAL_LOOK:
				case Affect.VISUAL_READ:
					if(!Sense.canBeSeenBy(affect.target(),this))
					{
						tell("You can't see that!");
						return false;
					}
					break;
				default:
					break;
				}
				break;
			case Affect.SOUND:
				if(Sense.isSleeping(this))
				{
					tell("Not while you are sleeping.");
					return false;
				}
				if((!Sense.canSpeak(this))
				   &&((affect.sourceCode()==Affect.SOUND_WORDS)||(affect.sourceCode()==Affect.SOUND_MAGIC)))
				{
					tell("You can't make sounds!");
					return false;
				}
				if((affect.sourceCode()==Affect.SOUND_MAGIC)
				&&(charStats().getIntelligence()<7))
				{
					tell("You aren't smart enough to do magic.");
					return false;
				}
				if((affect.sourceCode()==Affect.SOUND_WORDS)
				&&(charStats().getIntelligence()<2))
				{
					tell("You aren't smart enough to speak.");
					return false;
				}
				break;
			case Affect.AIR:
				break;
			case Affect.TASTE:
				if(!Sense.canPerformAction(this))
					return false;
				if(!Sense.canBeSeenBy(affect.target(),this))
				{
					mob.tell("You don't see that here.");
					return false;
				}
				if(!Sense.canTaste(this))
				{
					tell("You can't eat or drink!");
					return false;
				}
				break;
			case Affect.HANDS:
				if((Sense.isSleeping(this))||(Sense.isSitting(this)))
				{
					tell("You must stand up to do that.");
					return false;
				}
				if(!Sense.canBeSeenBy(affect.target(),this))
				{
					mob.tell("You don't see that here.");
					return false;
				}
				if(!Sense.canPerformAction(this))
					return false;
				break;
			case Affect.STRIKE:
				if(affect.target()==this) return true;
				if((Sense.isSleeping(this))||(Sense.isSitting(this)))
				{
					tell("You must stand up to do that.");
					return false;
				}
				if((amFollowing()!=null)&&(affect.target()==amFollowing()))
				{
					tell("You like "+amFollowing().charStats().himher()+" too much.");
					return false;
				}
				if((!Sense.canPerformAction(this))&&(affect.sourceCode()!=Affect.STRIKE_MIND))
					return false;
				break;
			case Affect.MOVE:
				switch(affect.sourceCode())
				{
				case Affect.MOVE_SIT:
					if(mob.isInCombat())
					{
						tell("Not while you are fighting!");
						return false;
					}
					if(Sense.isSitting(this))
					{
						tell("You are already sitting!");
						return false;
					}
					if(!Sense.canMove(this))
					{
						tell("You can't move!");
						return false;
					}
					break;
				case Affect.MOVE_SLEEP:
					if(mob.isInCombat())
					{
						tell("Not while you are fighting!");
						return false;
					}
					if(Sense.isSleeping(this))
					{
						tell("You are already asleep!");
						return false;
					}
					if(!Sense.canPerformAction(this))
						return false;
					break;
				case Affect.MOVE_STAND:
					if((!Sense.isSitting(this))&&(!Sense.isSleeping(this)))
					{
						tell("You are already standing!");
						return false;
					}
					if(!Sense.canMove(this))
					{
						tell("You can't move!");
						return false;
					}
					break;
				case Affect.MOVE_FLEE:
					if(!Sense.canPerformAction(this))
						return false;
					break;
				case Affect.MOVE_ENTER:
				case Affect.MOVE_LEAVE:
				case Affect.MOVE_GENERAL:
					if(mob.isInCombat())
					{
						tell("Not while you are fighting!");
						return false;
					}
					if((Sense.isSleeping(this))||(Sense.isSitting(this)))
					{
						tell("You must stand up to do that.");
						return false;
					}
					if(!Sense.canPerformAction(this))
						return false;
					break;
				default:
					break;
				}
				break;
			}
		}

		if(affect.amITarget(this))
		{
			switch(affect.targetType())
			{
			case Affect.VISUAL:
				return true;
			case Affect.SOUND:
				return true;
			case Affect.AIR:
				return true;
			case Affect.TASTE:
				if(!Sense.canBeSeenBy(this,affect.source()))
				{
					mob.tell("You don't see "+charStats().himher()+".");
					return false;
				}
				return true;
			case Affect.HANDS:
				switch(affect.targetCode())
				{
				case Affect.HANDS_GENERAL:
					return true;
				case Affect.HANDS_RECALL:
					return true;
				case Affect.HANDS_GIVE:
					if(affect.tool()==null) return false;
					if(!(affect.tool() instanceof Item)) return false;
					if(!Sense.canBeSeenBy(affect.tool(),this))
					{
						mob.tell(name()+" can't see what you are giving.");
						return false;
					}
					return true;
				default:
					break;
				}
				break;
			case Affect.STRIKE:
				if(affect.source()==this)
					return true;
				if((!this.isMonster())
				&&(!affect.source().isMonster())
				&&(affect.source().envStats().level()>(this.envStats().level()-10)))
				{
					mob.tell("Player killing is highly discouraged.");
					return false;
				}
				if(this.amFollowing()==mob)
					setFollowing(null);
				if((!isInCombat())&&(isMonster()))
					victim=(MOB)affect.source();
				return true;
			case Affect.GENERAL:
				return true;
			case Affect.NO_EFFECT:
				return true;
			}
			mob.tell("You can't do that.");
			return false;
		}
		return true;
	}

	public void tell(MOB source, Environmental target, String msg)
	{
		if(mySession!=null)
			mySession.stdPrintln(source,target,msg);

	}

	public void tell(String msg)
	{
		tell(this,this,msg);
	}

	public void affect(Affect affect)
	{

		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			B.affect(this,affect);
		}

		MOB mob=affect.source();
		if(affect.amISource(this))
		{
			if(charStats!=null)
			{
				if(charStats().getMyClass()!=null)
					charStats().getMyClass().affect(affect);
				if(charStats().getMyRace()!=null)
					charStats().getMyRace().affect(affect);
			}
			if(affect.sourceType()==Affect.STRIKE)
				if(affect.target() instanceof MOB)
					if(getVictim()!=affect.target())
						victim=(MOB)affect.target();
			switch(affect.sourceCode())
			{
			case Affect.VISUAL_LOOK:
				if((Sense.canBeSeenBy(this,mob))&&(affect.amITarget(this)))
				{
					StringBuffer myDescription=new StringBuffer("");
					if(mob.readSysopMsgs())
						myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc : "+text()+"\n\r"+description()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().ID())+"\n\r");
					if(!isMonster())
						myDescription.append(name()+" the "+charStats().getMyRace().name()+" is a level "+envStats().level()+" "+charStats().getMyClass().name()+".\n\r");
					myDescription.append(name()+" "+TheFight.mobCondition(this)+"\n\r\n\r");
					myDescription.append(description()+"\n\r\n\r");
					myDescription.append(charStats().HeShe()+" is wearing:\n\r"+Scoring.getEquipment(affect.source(),this));
					mob.tell(myDescription.toString());
				}
				break;
			case Affect.VISUAL_READ:
				if((Sense.canBeSeenBy(this,mob))&&(affect.amITarget(this)))
					mob.tell("There is nothing written on "+name());
				break;
			case Affect.NO_EFFECT:
				return;
			case Affect.MOVE_SIT:
			{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-Sense.IS_SLEEPING-Sense.IS_SNEAKING-Sense.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|Sense.IS_SITTING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				tell(affect.source(),affect.target(),affect.sourceMessage());
			}
			break;
			case Affect.MOVE_SLEEP:
			{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-Sense.IS_SLEEPING-Sense.IS_SNEAKING-Sense.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|Sense.IS_SLEEPING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				tell(affect.source(),affect.target(),affect.sourceMessage());
			}
			break;
			case Affect.MOVE_STAND:
			{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-Sense.IS_SLEEPING-Sense.IS_SNEAKING-Sense.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				tell(affect.source(),affect.target(),affect.sourceMessage());
			}
			break;
			case Affect.HANDS_RECALL:
				if((affect.target()!=null) && (affect.target() instanceof Room) && (location() != affect.target()))
				{
					tell(affect.source(),null,affect.targetMessage());
					location().delInhabitant(this);
					((Room)affect.target()).addInhabitant(this);
					((Room)affect.target()).showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> appears out of the Java Plain.");
					setLocation(((Room)affect.target()));
					affect.source().recoverEnvStats();
					affect.source().recoverCharStats();
					BasicSenses.look(mob,new Vector(),true);
				}
			break;
			default:
				tell(affect.source(),affect.target(),affect.sourceMessage());
				break;
			}
		}
		else
		if(affect.amITarget(this))
		{
			switch(affect.targetType())
			{
			case Affect.STRIKE:
				if((!isInCombat())&&(location().isInhabitant((MOB)affect.source())))
					victim=(MOB)affect.source();
				switch(affect.targetCode())
				{
				case Affect.STRIKE_HANDS:
					if((isInCombat())&&(!amDead))
					{
						Item weapon=new Natural();
						if((affect.tool()!=null)&&(affect.tool() instanceof Item))
							weapon=(Item)affect.tool();
						TheFight.doAttack(affect.source(),this,weapon);
					}
					break;
				default:
				{
					tell(affect.source(),affect.target(),affect.targetMessage());

					int chanceToFail=((this.envStats().level()-mob.envStats().level())*5);
					switch(affect.targetCode())
					{
					case Affect.STRIKE_MAGIC:
						chanceToFail+=charStats().getIntelligence();
						break;
					case Affect.STRIKE_MIND:
						chanceToFail+=(charStats().getWisdom()+charStats().getIntelligence()+charStats().getCharisma());
						break;
					case Affect.STRIKE_POISON:
						chanceToFail+=(charStats().getConstitution()*2);
						break;
					case Affect.STRIKE_GAS:
						if(!Sense.canSmell(this))
							chanceToFail+=100;
						else
							chanceToFail+=(int)Math.round(Util.div((charStats().getConstitution()+charStats().getDexterity()),2.0));
						break;
					case Affect.STRIKE_COLD:
					case Affect.STRIKE_ELECTRIC:
					case Affect.STRIKE_FIRE:
					case Affect.STRIKE_WATER:
						chanceToFail+=(int)Math.round(Util.div((charStats().getConstitution()+charStats().getDexterity()),2.0));
						break;
					default:
						chanceToFail=0;
						break;
					}

					if(chanceToFail>0)
					{
						if(chanceToFail<5)
							chanceToFail=5;
						else
						if(chanceToFail>95)
						   chanceToFail=95;
						if(Dice.rollPercentage()<chanceToFail)
						{
							String tool=null;
							String endPart=" from <S-NAME>.";
							if(affect.tool()!=null)
							{
								if(affect.tool() instanceof Trap)
									endPart=".";
								else
							    if(affect.tool() instanceof Ability)
									tool=((Ability)affect.tool()).name();
							}
							switch(affect.targetCode())
							{
							case Affect.STRIKE_MAGIC:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> resist(s) the "+((tool==null)?"magical attack":tool)+endPart);
								break;
							case Affect.STRIKE_MIND:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> shake(s) off the "+((tool==null)?"mental attack":tool)+endPart);
								break;
							case Affect.STRIKE_GAS:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> avoid(s) the "+((tool==null)?"noxious fumes":tool)+endPart);
								break;
							case Affect.STRIKE_COLD:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> shake(s) off the "+((tool==null)?"cold blast":tool)+endPart);
								break;
							case Affect.STRIKE_ELECTRIC:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> shake(s) off the "+((tool==null)?"electrical attack":tool)+endPart);
								break;
							case Affect.STRIKE_FIRE:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> avoid(s) the "+((tool==null)?"blast of heat":tool)+endPart);
								break;
							case Affect.STRIKE_WATER:
								mob.location().show(mob,this,Affect.VISUAL_WNOISE,"<T-NAME> dodge(s) the "+((tool==null)?"wet blast":tool)+endPart);
								break;
							}
							affect.tagModified(true);
						}
					}
					if((affect.tool()!=null)&&(affect.tool() instanceof Item))
						((Item)affect.tool()).strike(affect.source(),this,true);
				}
				break;
				}
				Movement.standIfNecessary(this);
				fightingFollowers(victim);
				break;
			case Affect.HANDS:
				switch(affect.targetCode())
				{
				case Affect.HANDS_GIVE:
					if((affect.tool()!=null)&&(affect.tool() instanceof Item))
					{
						FullMsg msg=new FullMsg(affect.source(),affect.tool(),null,Affect.HANDS_DROP,Affect.HANDS_DROP,Affect.NO_EFFECT,null);
						if(location().okAffect(msg))
						{
							location().send(this,msg);
							msg=new FullMsg((MOB)affect.target(),affect.tool(),null,Affect.HANDS_GET,Affect.HANDS_GET,Affect.NO_EFFECT,null);
							if(location().okAffect(msg))
							{
								location().send(this,msg);
								tell(affect.source(),affect.target(),affect.targetMessage());
							}
						}
						return;
					}
					break;
				default:
					break;
				}
				break;
			case Affect.VISUAL:
				switch(affect.targetCode())
				{
				case Affect.VISUAL_LOOK:
					if(Sense.canBeSeenBy(location(),this))
						tell(affect.source(),affect.target(),affect.targetMessage());
					if(Sense.canBeSeenBy(this,mob))
					{
						StringBuffer myDescription=new StringBuffer("");
						if(mob.readSysopMsgs())
							myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc :'"+text()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().ID())+"\n\r"+description()+"\n\r");
						if(!isMonster())
							myDescription.append(name()+" the "+charStats().getMyRace().name()+" is a level "+envStats().level()+" "+charStats().getMyClass().name()+".\n\r");
						myDescription.append(name()+" "+TheFight.mobCondition(this)+"\n\r\n\r");
						myDescription.append(description()+"\n\r\n\r");
						myDescription.append(charStats().HeShe()+" is wearing:\n\r"+Scoring.getEquipment(affect.source(),this));
						mob.tell(myDescription.toString());
					}
					break;
				case Affect.VISUAL_READ:
					if(Sense.canBeSeenBy(location(),this))
						tell(affect.source(),affect.target(),affect.targetMessage());
					if((Sense.canBeSeenBy(this,mob))&&(affect.amITarget(this)))
						mob.tell("There is nothing written on "+name());
					break;
				case Affect.VISUAL_ONLY:
					if((!Sense.isSleeping(this))&&(Sense.canBeSeenBy(affect.source(),this)))
						tell(affect.source(),affect.target(),affect.targetMessage());
					break;
				default:
					if((!Sense.isSleeping(this))&&((Sense.canBeSeenBy(location(),this)||(Sense.canHear(this)))))
						tell(affect.source(),affect.target(),affect.targetMessage());
					break;
				}
				break;
			case Affect.SOUND:
				if((Sense.canBeHeardBy(affect.source(),this))&&(!Sense.isSleeping(this)))
				{
					if(affect.targetCode()==Affect.SOUND_WORDS)
						replyTo=affect.source();
					tell(affect.source(),affect.target(),affect.targetMessage());
				}
				break;
			case Affect.MOVE:
				if((!Sense.isSleeping(this))&&((Sense.canBeSeenBy(location(),this)||Sense.canHear(this))))
					tell(affect.source(),affect.target(),affect.targetMessage());
				break;
			case Affect.AIR:
				if(Sense.canSmell(this))
					tell(affect.source(),affect.target(),affect.targetMessage());
				break;
			case Affect.NO_EFFECT:
				return;
			default:
				tell(affect.source(),affect.target(),affect.targetMessage());
				break;
			}
		}
		else
		{
			switch(affect.othersType())
			{
			case Affect.VISUAL:
				if((Sense.canBeSeenBy(affect.source(),this))&&(affect.othersCode()==Affect.VISUAL_ONLY)&&(!Sense.isSleeping(this)))
					tell(affect.source(),affect.target(),affect.targetMessage());
				else
				if((Sense.canBeSeenBy(affect.source(),this)||Sense.canHear(this))&&(!Sense.isSleeping(this)))
					tell(affect.source(),affect.target(),affect.othersMessage());
				break;
			case Affect.SOUND:
				if((Sense.canBeHeardBy(affect.source(),this))&&(!Sense.isSleeping(this)))
					tell(affect.source(),affect.target(),affect.othersMessage());
				break;
			case Affect.AIR:
				if(Sense.canSmell(this))
					tell(affect.source(),affect.target(),affect.othersMessage());
				break;
			case Affect.NO_EFFECT:
				return;
			case Affect.MOVE:
				if((Sense.canBeSeenBy(mob,this)||Sense.canBeHeardBy(mob,this)))
					tell(affect.source(),affect.target(),affect.othersMessage());
				break;
			default:
				tell(affect.source(),affect.target(),affect.othersMessage());
				break;
			}
		}

		for(int i=0;i<inventorySize();i++)
			((Item)fetchInventory(i)).affect(affect);

		for(int i=0;i<numAffects();i++)
			((Ability)fetchAffect(i)).affect(affect);
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public boolean tick(int tickID)
	{
		if(pleaseDestroy)
			return false;
		
		if(tickID==ServiceEngine.MOB_TICK)
		{
			if(amDead)
			{
				if(isMonster())
					if(envStats().rejuv()<Integer.MAX_VALUE)
					{
						envStats().setRejuv(envStats().rejuv()-1);
						if(envStats().rejuv()<0)
						{
							raiseFromDead();
						}
					}
					else
					{
						destroy();
						return false;
					}
			}
			else
			if(location()!=null)
			{
				curState().adjState(this,maxState);
				curState().expendEnergy(this,maxState,false);
				if(location()!=null)
				{
					if(!Sense.canBreathe(this))
					{
						this.location().show(this,this,Affect.VISUAL_WNOISE,"<S-NAME> can't breathe!");
						curState().adjHitPoints(-(int)Math.round(Math.random()*6.0),maxState());
					}
					if(isInCombat())
					{
						Item weapon=this.fetchWieldedItem();
						double curSpeed=Math.floor(speeder);
						speeder+=envStats().speed();
						int numAttacks=(int)Math.round(Math.floor(speeder-curSpeed));
						for(int s=0;s<numAttacks;s++)
							TheFight.postAttack(this,victim,weapon);
						curState().expendEnergy(this,maxState,true);
						if(!isMonster())
						{
							if(session().ondeckCmd==null)
							{
								Vector CMDS=session().deque();
								if(CMDS!=null)
									session().ondeckCmd=CMDS;
							}
							MOB target=this.getVictim();
							if((target!=null)&&(!target.amDead())&&(Sense.canBeSeenBy(target,this)))
								session().print(target.name()+" "+TheFight.mobCondition(target)+"\n\r\n\r");
						}
						if(weapon==null)
							pleaseWieldSomething();
					}
					else
					{
						speeder=0.0;
						if((!isMonster())&&(session().ondeckCmd==null))
						{
							Vector CMDS=session().deque();
							if(CMDS!=null)
								session().ondeckCmd=CMDS;
						}
					}
				}

				if((!isMonster())&&(((++minuteCounter)*MUD.TICK_TIME)>60000))
				{
					minuteCounter=0;
					setAgeHours(AgeHours+1);
				}
			}

			int a=0;
			while(a<affects.size())
			{
				Ability A=(Ability)affects.elementAt(a);
				int s=affects.size();
				if(!A.tick(tickID))
					A.unInvoke();
				if(affects.size()==s)
					a++;
			}

			for(int b=0;b<behaviors.size();b++)
			{
				Behavior B=(Behavior)behaviors.elementAt(b);
				B.tick(this,tickID);
			}
		}
		lastTickedDateTime=Calendar.getInstance();
		return !pleaseDestroy;
	}

	public boolean isMonster()
	{
		if(mySession==null)
		   return true;
		return false;
	}

	public boolean isASysOp()
	{
		if(isMonster()) return false;
		if(baseCharStats()==null) return false;
		if(baseCharStats().getMyClass()==null) return false;
		if(this.baseCharStats().getMyClass() instanceof Archon)
			return true;
		return false;
	}

	public void addInventory(Item item)
	{
		item.setOwner(this);
		inventory.addElement(item);
	}
	public void delInventory(Item item)
	{
		inventory.removeElement(item);
	}
	public int inventorySize()
	{
		return inventory.size();
	}
	public Item fetchInventory(int index)
	{
		if(index<inventorySize())
			return (Item)inventory.elementAt(index);
		return null;
	}
	public Item fetchInventory(String itemName)
	{
		return (Item)Util.fetchAvailableItem(inventory,itemName,null,false,false);
	}
	public Item fetchCarried(Item goodLocation, String itemName)
	{
		return (Item)Util.fetchAvailableItem(inventory,itemName,goodLocation,false,true);
	}
	public Item fetchWornItem(String itemName)
	{
		return (Item)Util.fetchAvailableItem(inventory,itemName,null,true,false);
	}
	public void addFollower(MOB follower)
	{
		if(follower!=null)
			followers.addElement(follower);
	}

	public void delFollower(MOB follower)
	{
		if(follower!=null)
			followers.removeElement(follower);
	}
	public int numFollowers()
	{
		return followers.size();
	}
	public MOB fetchFollower(int index)
	{
		if(index<numFollowers())
			return (MOB)followers.elementAt(index);
		return null;
	}
	public MOB fetchFollower(MOB thisOne)
	{
		for(int f=0;f<followers.size();f++)
			if(followers.elementAt(f)==thisOne)
				return thisOne;
		return null;
	}
	public MOB fetchFollower(String ID)
	{
		return (MOB)Util.fetchEnvironmental(followers,ID);
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
		for(int i=0;i<abilities.size();i++)
			if(((Ability)abilities.elementAt(i)).ID().equals(to.ID()))
				return;
		int qualifyingLevel=to.qualifyingLevel(this);
		if((qualifyingLevel>=0)&&(qualifyingLevel!=to.envStats().level()))
		{
			to=(Ability)to.copyOf();
			to.baseEnvStats().setLevel(qualifyingLevel);
			to.recoverEnvStats();
		}
		abilities.addElement(to);
	}
	public void delAbility(Ability to)
	{
		abilities.removeElement(to);
	}
	public int numAbilities()
	{
		return abilities.size();
	}
	public Ability fetchAbility(int index)
	{
		if(index <numAbilities())
			return (Ability)abilities.elementAt(index);
		return null;
	}
	public Ability fetchAbility(String ID)
	{
		for(int i=0;i<abilities.size();i++)
			if(((Ability)abilities.elementAt(i)).ID().equals(ID))
				return (Ability)abilities.elementAt(i);
		return (Ability)Util.fetchEnvironmental(abilities,ID);
	}

	public void addAffect(Ability to)
	{
		if(to==null) return;
		for(int i=0;i<affects.size();i++)
			if(((Ability)affects.elementAt(i)).ID().equals(to.ID()))
				return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delAffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numAffects()
	{
		return affects.size();
	}
	public Ability fetchAffect(int index)
	{
		if(index <numAffects())
			return (Ability)affects.elementAt(index);
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<affects.size();a++)
			if(((Ability)affects.elementAt(a)).ID().equals(ID))
			   return (Ability)affects.elementAt(a);
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int i=0;i<behaviors.size();i++)
			if(((Behavior)behaviors.elementAt(i)).ID().equals(to.ID()))
				return;
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
		if(index <numBehaviors())
			return (Behavior)behaviors.elementAt(index);
		return null;
	}

	public boolean amWearingSomethingHere(long wornCode)
	{
		for(int i=0;i<inventory.size();i++)
		{
			Item thisItem=(Item)inventory.elementAt(i);
			if(thisItem.amWearingAt(wornCode))
				return true;
		}
		return false;
	}
	public Item fetchWornItem(long wornCode)
	{
		for(int i=0;i<inventory.size();i++)
		{
			Item thisItem=(Item)inventory.elementAt(i);
			if(thisItem.amWearingAt(wornCode))
				return thisItem;
		}
		return null;
	}

	public Item fetchWieldedItem()
	{
		for(int i=0;i<inventory.size();i++)
		{
			Item thisItem=(Item)inventory.elementAt(i);
			if(thisItem.amWearingAt(Item.WIELD))
				return thisItem;
		}
		return null;
	}


	private void pleaseWieldSomething()
	{
		if(fetchWieldedItem()!=null)
			return;

		for(int i=0;i<inventory.size();i++)
		{
			Item thisItem=(Item)inventory.elementAt(i);
			if(thisItem.canBeWornAt(Item.WIELD))
			{
				thisItem.wear(Item.WIELD);
				return;
			}
		}
	}


	public boolean isMine(Environmental env)
	{
		if(env instanceof Item)
		{
			for(int i=0;i<inventory.size();i++)
				if(inventory.elementAt(i)==env)
					return true;
			return false;
		}
		else
		if(env instanceof MOB)
		{
			for(int i=0;i<followers.size();i++)
				if(followers.elementAt(i)==env)
					return true;
			return false;
		}
		else
		if(env instanceof Ability)
		{
			for(int i=0;i<abilities.size();i++)
				if(abilities.elementAt(i)==env)
					return true;
			return false;
		}
		return false;
	}

	private void fightingFollowers(MOB victim)
	{
		if(victim==null) return;
		for(int f=0;f<followers.size();f++)
		{
			MOB follower=(MOB)followers.elementAt(f);
			if(follower.amFollowing()==this)
				if(follower.getVictim()!=victim)
					TheFight.postAttack(follower,victim,follower.fetchWieldedItem());
		}
	}
}
