package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Masonry extends CraftingSkill
{
	public String ID() { return "Masonry"; }
	public String name(){ return "Masonry";}
	private static final String[] triggerStrings = {"MASONRY"};
	public String[] triggerStrings(){return triggerStrings;}

	private final static int BUILD_WALL=0;
	private final static int BUILD_ROOF=1;
	private final static int BUILD_ARCH=2;
	private final static int BUILD_DEMOLISH=3;
	private final static int BUILD_TITLE=4;
	private final static int BUILD_DESC=5;
	private final static int BUILD_MONUMENT=6;
	private final static int BUILD_WINDOW=7;
	private final static int BUILD_CRAWLWAY=8;
	private final static int BUILD_POOL=9;
	private final static int BUILD_PORTCULIS=10;

	private final static int DAT_NAME=0;
	private final static int DAT_WOOD=1;
	private final static int DAT_ROOF=2;
	private final static int DAT_REQDIR=3;
	private final static int DAT_REQNONULL=4;

	// name, wood, ok=0/roof=1/out=2, req direction=1, ok=0, ok=0, nonull=1, nullonly=2
	private final static String[][] data={
		{"Wall","250","1","1","0"},
		{"Roof","500","2","0","0"},
		{"Archway","200","0","1","0"},
		{"Demolish","0","0","1","0"},
		{"Title","0","0","0","0"},
		{"Description","0","0","0","0"},
		{"Druidic Monument","1000","2","0","0"},
		{"Window","100","1","1","1"},
		{"Crawlway","500","1","1","1"},
		{"Pool","700","2","0","0"},
		{"Portcullis","100","0","1","0"},
	};

	private Room room=null;
	private int dir=-1;
	private int doingCode=-1;
	private boolean messedUp=false;
	private int workingOn=-1;
	private static boolean mapped=false;
	private String designTitle="";
	private String designDescription="";

	public Masonry()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",5,ID(),false);}
	}

	public Exit generify(Exit E)
	{
		Exit E2=CMClass.getExit("GenExit");
		E2.setName(E.name());
		E2.setDisplayText(E.displayText());
		E2.setDescription(E.description());
		E2.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),E.hasALock(),E.isLocked(),E.defaultsLocked());
		E2.setBaseEnvStats(E.baseEnvStats().cloneStats());
		E2.setExitParams(E.doorName(),E.closeWord(),E.openWord(),E.closedText());
		E2.setKeyName(E.keyName());
		E2.setOpenDelayTicks(E.openDelayTicks());
		E2.setReadable(E.isReadable());
		E2.setReadableText(E.readableText());
		E2.setTemporaryDoorLink(E.temporaryDoorLink());
		E2.recoverEnvStats();
		return E2;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(!aborted)
				{
					if((messedUp)&&(room!=null))
					switch(doingCode)
					{
					case BUILD_ROOF:
						commonTell(mob,"You've ruined the frame and roof!");
						break;
					case BUILD_WALL:
						commonTell(mob,"You've ruined the wall!");
						break;
					case BUILD_ARCH:
						commonTell(mob,"You've ruined the archway!");
						break;
					case BUILD_PORTCULIS:
						commonTell(mob,"You've ruined the portcullis!");
						break;
					case BUILD_TITLE:
						commonTell(mob,"You've ruined the titling!");
						break;
					case BUILD_DESC:
						commonTell(mob,"You've ruined the describing!");
						break;
					case BUILD_MONUMENT:
						commonTell(mob,"You've ruined the druidic monument!");
						break;
					case BUILD_WINDOW:
						commonTell(mob,"You've ruined the window!");
						break;
					case BUILD_POOL:
						commonTell(mob,"You've ruined the pool!");
						break;
					case BUILD_CRAWLWAY:
						commonTell(mob,"You've ruined the crawlway!");
						break;
					case BUILD_DEMOLISH:
					default:
						commonTell(mob,"You've failed to demolish!");
						break;
					}
					else
					switch(doingCode)
					{
					case BUILD_ROOF:
					case BUILD_POOL:
						{
							Room R=null;
							if(doingCode==BUILD_POOL)
							{
								if((room.domainType()&Room.INDOORS)==Room.INDOORS)
									R=CMClass.getLocale("IndoorWaterSurface");
								else
									R=CMClass.getLocale("WaterSurface");
							}
							else
								R=CMClass.getLocale("StoneRoom");
							R.setRoomID(room.roomID());
							R.setDisplayText(room.displayText());
							R.setDescription(room.description());
							R.setArea(room.getArea());
							for(int a=room.numEffects()-1;a>=0;a--)
							{
								Ability A=room.fetchEffect(a);
								if(A!=null)
								{
									room.delEffect(A);
									R.addEffect(A);
								}
							}
							for(int i=room.numItems()-1;i>=0;i--)
							{
								Item I=room.fetchItem(i);
								if(I!=null)
								{
									room.delItem(I);
									R.addItem(I);
								}
							}
							for(int m=room.numInhabitants()-1;m>=0;m--)
							{
								MOB M=room.fetchInhabitant(m);
								if(M!=null){
									room.delInhabitant(M);
									R.addInhabitant(M);
									M.setLocation(R);
								}
							}
							CMClass.ThreadEngine().deleteTick(room,-1);
							CMMap.delRoom(room);
							CMMap.addRoom(R);
							for(int d=0;d<R.rawDoors().length;d++)
							{
								if((R.rawDoors()[d]==null)
								||(R.rawDoors()[d].roomID().length()>0))
									R.rawDoors()[d]=room.rawDoors()[d];
							}
							for(int d=0;d<R.rawExits().length;d++)
							{
								if((R.rawDoors()[d]==null)
								||(R.rawDoors()[d].roomID().length()>0))
									R.rawExits()[d]=room.rawExits()[d];
							}

							R.startItemRejuv();
							for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
							{
								Room R2=(Room)r.nextElement();
								for(int d=0;d<R2.rawDoors().length;d++)
									if(R2.rawDoors()[d]==room)
									{
										R2.rawDoors()[d]=R;
										if(R2 instanceof GridLocale)
											((GridLocale)R2).buildGrid();
									}
							}
							for(Enumeration e=CMMap.players();e.hasMoreElements();)
							{
								MOB M=(MOB)e.nextElement();
								if(M.getStartRoom()==room)
									M.setStartRoom(R);
								else
								if(M.location()==room)
									M.setLocation(R);
							}
							if(doingCode==BUILD_POOL)
							{
								Room R2=CMClass.getLocale("UnderWater");
								R2.setRoomID(CMMap.getOpenRoomID(R.getArea().ID()));
								R2.setDisplayText("Under the water");
								R2.setDescription("You are swimming around under the water.");
								R2.setArea(R.getArea());
								R2.rawDoors()[Directions.UP]=R;
								R2.rawExits()[Directions.UP]=CMClass.getExit("Open");
								R.rawDoors()[Directions.DOWN]=R2;
								R.rawExits()[Directions.DOWN]=CMClass.getExit("Open");
								LandTitle title=CoffeeUtensils.getLandTitle(R);
								if((title!=null)&&(CoffeeUtensils.getLandTitle(R2)==null))
								{
									LandTitle A2=(LandTitle)title.newInstance();
									A2.setLandPrice(((LandTitle)title).landPrice());
									R2.addNonUninvokableEffect((Ability)A2);
									break;
								}
								CMClass.DBEngine().DBCreateRoom(R2,R2.ID());
								CMClass.DBEngine().DBUpdateExits(R2);
							}

							R.getArea().clearMap();
							R.getArea().fillInAreaRoom(R);
							CMClass.DBEngine().DBUpdateRoom(R);
							CMClass.DBEngine().DBUpdateExits(R);
						}
						break;
					case BUILD_PORTCULIS:
						{
							Exit x=CMClass.getExit("GenExit");
							Exit x2=CMClass.getExit("GenExit");
							x.setName("an archway");
							x.setDescription("A portcullis lies this way.");
							x.setExitParams("portcullis","lower","raise","A portcullis blocks your way.");
							x.setDoorsNLocks(true,false,true,false,false,false);
							x2.setName("a portcullis");
							x2.setDescription("A portcullis lies this way.");
							x2.setExitParams("portcullis","lower","raise","A portcullis blocks your way.");
							x2.setDoorsNLocks(true,false,true,false,false,false);
							room.rawExits()[dir]=x;
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=x2;
								CMClass.DBEngine().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMClass.DBEngine().DBUpdateExits(room);
						}
						break;
					case BUILD_ARCH:
						{
							Exit x=CMClass.getExit("GenExit");
							Exit x2=CMClass.getExit("GenExit");
							x.setName("an archway");
							x.setDescription("A majestic archway towers above you.");
							x2.setName("an archway");
							x2.setDescription("A majestic archway towers above you.");
							room.rawExits()[dir]=x;
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=x2;
								CMClass.DBEngine().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMClass.DBEngine().DBUpdateExits(room);
						}
						break;
					case BUILD_WALL:
						{
							room.rawExits()[dir]=null;
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=null;
								CMClass.DBEngine().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMClass.DBEngine().DBUpdateExits(room);
						}
						break;
					case BUILD_TITLE:
						{
							room.setDisplayText(designTitle);
							CMClass.DBEngine().DBUpdateRoom(room);
						}
						break;
					case BUILD_DESC:
						{
							if(workingOn>=0)
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.rawExits()[workingOn]==E))
								{
									E=generify(E);
									room.rawExits()[workingOn]=E;
								}
								E.setDescription(designDescription);
								CMClass.DBEngine().DBUpdateExits(room);
							}
							else
							{
								room.setDescription(designDescription);
								CMClass.DBEngine().DBUpdateRoom(room);
							}
						}
						break;
					case BUILD_MONUMENT:
						{
							Item I=CMClass.getItem("DruidicMonument");
							room.addItem(I);
							I.setDispossessionTime(0);
						}
						break;
					case BUILD_CRAWLWAY:
						{
							if((workingOn>=0)&&(room.getExitInDir(workingOn)!=null))
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.rawExits()[workingOn]==E))
								{
									E=generify(E);
									room.rawExits()[workingOn]=E;
								}
								Ability A=CMClass.getAbility("Prop_Crawlspace");
								if(A!=null) E.addNonUninvokableEffect(A);
								CMClass.DBEngine().DBUpdateExits(room);
							}
						}
						break;
					case BUILD_WINDOW:
						{
							if((workingOn>=0)&&(room.getExitInDir(workingOn)!=null))
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.rawExits()[workingOn]==E))
								{
									E=generify(E);
									room.rawExits()[workingOn]=E;
								}
								Room R2=room.getRoomInDir(workingOn);
								if(R2!=null)
								{
									Ability A=CMClass.getAbility("Prop_RoomView");
									if(A!=null)
									{
										A.setMiscText(CMMap.getExtendedRoomID(R2));
										E.addNonUninvokableEffect(A);
									}
								}
								CMClass.DBEngine().DBUpdateExits(room);
							}
						}
						break;
					case BUILD_DEMOLISH:
					default:
						{
							if(dir<0)
							{
								Room R2=room.rawDoors()[Directions.DOWN];
								if(((room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
								   &&(R2!=null)
								   &&((R2.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)||(R2.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
								{
									if(R2.rawDoors()[Directions.UP]==room)
									{
										R2.rawDoors()[Directions.UP]=null;
										R2.rawExits()[Directions.UP]=null;
									}
									CoffeeUtensils.obliterateRoom(R2);
									room.rawDoors()[Directions.DOWN]=null;
									room.rawExits()[Directions.DOWN]=null;
								}
								Room R=CMClass.getLocale("Plains");
								R.setRoomID(room.roomID());
								R.setDisplayText(room.displayText());
								R.setDescription(room.description());
								R.setArea(room.getArea());
								for(int a=room.numEffects()-1;a>=0;a--)
								{
									Ability A=room.fetchEffect(a);
									if(A!=null){
										room.delEffect(A);
										R.addEffect(A);
									}
								}
								for(int i=room.numItems()-1;i>=0;i--)
								{
									Item I=room.fetchItem(i);
									if(I!=null){
										room.delItem(I);
										R.addItem(I);
									}
								}
								for(int m=room.numInhabitants()-1;m>=0;m--)
								{
									MOB M=room.fetchInhabitant(m);
									if(M!=null){
										room.delInhabitant(M);
										R.addInhabitant(M);
										M.setLocation(R);
									}
								}
								CMClass.ThreadEngine().deleteTick(room,-1);
								CMMap.delRoom(room);
								CMMap.addRoom(R);
								for(int d=0;d<R.rawDoors().length;d++)
									R.rawDoors()[d]=room.rawDoors()[d];
								for(int d=0;d<R.rawExits().length;d++)
									R.rawExits()[d]=room.rawExits()[d];
								R.startItemRejuv();
								for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
								{
									Room R3=(Room)r.nextElement();
									for(int d=0;d<R3.rawDoors().length;d++)
										if(R3.rawDoors()[d]==room)
										{
											R3.rawDoors()[d]=R;
											if(R3 instanceof GridLocale)
												((GridLocale)R3).buildGrid();
										}
								}
								R.getArea().clearMap();
								R.getArea().fillInAreaRoom(R);
								CMClass.DBEngine().DBUpdateRoom(R);
								CMClass.DBEngine().DBUpdateExits(R);
							}
							else
							{
								room.rawExits()[dir]=CMClass.getExit("Open");
								if(room.rawDoors()[dir]!=null)
								{
									room.rawDoors()[dir].rawExits()[Directions.getOpDirectionCode(dir)]=CMClass.getExit("Open");
									CMClass.DBEngine().DBUpdateExits(room.rawDoors()[dir]);
								}
								CMClass.DBEngine().DBUpdateExits(room);
							}
						}
						break;
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"What kind of masonry, where? Try Masonry list.");
			return false;
		}
		String str=(String)commands.elementAt(0);
		if(("LIST").startsWith(str.toUpperCase()))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Stone required\n\r");
			for(int r=0;r<data.length;r++)
			{
				if((r!=BUILD_MONUMENT)||(mob.charStats().getCurrentClass().baseClass().equals("Druid")))
					buf.append(Util.padRight(data[r][DAT_NAME],20)+" "+data[r][DAT_WOOD]+"\n\r");
			}
			commonTell(mob,buf.toString());
			return true;
		}

		designTitle="";
		designDescription="";
		String startStr=null;
		int completion=15;
		workingOn=-1;
		doingCode=-1;
		dir=-1;

		room=null;
		messedUp=false;

		String firstWord=(String)commands.firstElement();
		helpingAbility=null;

		if(firstWord.equalsIgnoreCase("help"))
		{
			messedUp=!profficiencyCheck(mob,0,auto);
			completion=25;
			commands.removeElementAt(0);
			MOB targetMOB=getTarget(mob,commands,givenTarget,false,true);
			if(targetMOB==null) return false;
			if(targetMOB==mob)
			{
				commonTell(mob,"You can not do that.");
				return false;
			}
			helpingAbility=(CommonSkill)targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTell(mob,targetMOB.Name()+" is not building anything.");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
			{
				helpingAbility=null;
				return false;
			}
			helping=true;
			verb="helping "+targetMOB.name()+" with "+helpingAbility.verb;
			startStr="<S-NAME> start(s) "+verb;
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,startStr+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,completion);
			}
			return true;
		}
		for(int r=0;r<data.length;r++)
		{
			if((r!=BUILD_MONUMENT)||(mob.charStats().getCurrentClass().baseClass().equals("Druid")))
				if(data[r][DAT_NAME].toUpperCase().startsWith(firstWord.toUpperCase()))
					doingCode=r;
		}
		if(doingCode<0)
		{
			commonTell(mob,"'"+firstWord+"' is not a valid masonry project.  Try LIST.");
			return false;
		}
		String dirName=(String)commands.lastElement();
		dir=Directions.getGoodDirectionCode(dirName);
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("roof")))
		   dir=-1;
		else
		if(((dir<0)||(dir>3))
		&&(Util.s_int(data[doingCode][DAT_REQDIR])==1))
		{
			commonTell(mob,"A valid direction in which to build must also be specified.");
			return false;
		}

		if(data[doingCode][DAT_REQNONULL].equals("1")
		&&(dir>=0)
		&&(mob.location().getExitInDir(dir)==null))
		{
			commonTell(mob,"There is a wall that way that needs to be demolished first.");
			return false;
		}


		int woodRequired=Util.s_int(data[doingCode][DAT_WOOD]);
		if(((mob.location().domainType()&Room.INDOORS)==0)
		&&(data[doingCode][DAT_ROOF].equals("1")))
		{
			commonTell(mob,"That can only be built after a roof, which includes the frame.");
			return false;
		}
		else
		if(((mob.location().domainType()&Room.INDOORS)>0)
		   &&(data[doingCode][DAT_ROOF].equals("2")))
		{
			commonTell(mob,"That can only be built outdoors!");
			return false;
		}

		if(doingCode==BUILD_TITLE)
		{
			String title=Util.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,"A title must be specified.");
				return false;
			}
			for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.displayText().equalsIgnoreCase(title))
				{
					commonTell(mob,"That title has already been taken.  Choose another.");
					return false;
				}
			}
			designTitle=title;
		}
		else
		if(doingCode==BUILD_DESC)
		{
			if(commands.size()<3)
			{
				commonTell(mob,"You must specify an exit direction or the word room, followed by a description for it.");
				return false;
			}
			if(Directions.getGoodDirectionCode((String)commands.elementAt(1))>=0)
			{
				dir=Directions.getGoodDirectionCode((String)commands.elementAt(1));
				if(mob.location().getExitInDir(dir)==null)
				{
					commonTell(mob,"There is no exit "+Directions.getInDirectionName(dir)+" to describe.");
					return false;
				}
				workingOn=dir;
				commands.removeElementAt(1);
			}
			else
			if(!((String)commands.elementAt(1)).equalsIgnoreCase("room"))
			{
				commonTell(mob,"'"+((String)commands.elementAt(1))+"' is neither the word room, nor an exit direction.");
				return false;
			}
			else
				commands.removeElementAt(1);
			designDescription=Util.combine(commands,1);
		}
		else
		if((doingCode==BUILD_WINDOW)||(doingCode==BUILD_CRAWLWAY))
			workingOn=dir;

		int foundWood=0;
		int[][] idata=null;
		if(doingCode==BUILD_PORTCULIS)
		{
			int[] pm={EnvResource.MATERIAL_METAL,EnvResource.MATERIAL_MITHRIL};
			idata=fetchFoundResourceData(mob,
			 							woodRequired,"metal",pm,
			 							0,null,null,
			 							false,
			 							0);
			if(idata==null) return false;
			woodRequired=idata[0][FOUND_AMT];
		}
		else
		{
			int[] pm={EnvResource.MATERIAL_ROCK};
			idata=fetchFoundResourceData(mob,
										woodRequired,"stone",pm,
										0,null,null,
										false,
										0);
			if(idata==null) return false;
			woodRequired=idata[0][FOUND_AMT];
		}

		boolean canBuild=CoffeeUtensils.doesOwnThisProperty(mob,mob.location());
		if(!canBuild)
		{
			if((dir>=0)
			&&((data[doingCode][DAT_REQDIR].equals("1")||(workingOn==dir))))
			{
				Room R=mob.location().getRoomInDir(dir);
				if((R!=null)
				&&(CoffeeUtensils.doesOwnThisProperty(mob,R)))
					canBuild=true;
			}
		}
		if(!canBuild)
		{
			commonTell(mob,"You'll need the permission of the owner to do that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		room=mob.location();
		if(woodRequired>0)
			destroyResources(mob.location(),woodRequired,idata[0][FOUND_CODE],0,null,0);

		switch(doingCode)
		{
		case BUILD_ROOF:
			verb="building a frame and roof";
			break;
		case BUILD_POOL:
			verb="building a pool";
			break;
		case BUILD_WALL:
			verb="building the "+Directions.getDirectionName(dir)+" wall";
			break;
		case BUILD_ARCH:
			verb="building the "+Directions.getDirectionName(dir)+" archway";
			break;
		case BUILD_PORTCULIS:
			verb="building the "+Directions.getDirectionName(dir)+" portcullis";
			break;
		case BUILD_TITLE:
			verb="giving this place a title";
			break;
		case BUILD_DESC:
			verb="giving this place a description";
			break;
		case BUILD_MONUMENT:
			verb="building a druidic monument";
			break;
		case BUILD_WINDOW:
			verb="building a window "+Directions.getDirectionName(dir);
			break;
		case BUILD_CRAWLWAY:
			verb="building a crawlway "+Directions.getDirectionName(dir);
			break;
		case BUILD_DEMOLISH:
		default:
			if(dir<0)
			{
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						verb="demolishing the pool";
				else
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
				{
					commonTell(mob,null,null,"You must demolish a pool from above.");
					return false;
				}
				else
				if(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE)
				{
					commonTell(mob,null,null,"There are no stone constructs to demolish here!");
					return false;
				}
				else
					verb="demolishing the roof";
			}
			else
				verb="demolishing the "+Directions.getDirectionName(dir)+" wall";
			break;
		}
		messedUp=!profficiencyCheck(mob,0,auto);
		startStr="<S-NAME> start(s) "+verb;
		if(completion<15) completion=15;

		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,startStr+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
