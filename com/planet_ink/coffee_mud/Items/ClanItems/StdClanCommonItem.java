package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2020 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdClanCommonItem extends StdClanItem
{
	@Override
	public String ID()
	{
		return "StdClanCommonItem";
	}

	private static final Map<Area,PairList<MOB,List<Integer>>> needChart = new Hashtable<Area,PairList<MOB,List<Integer>>>();

	protected int 		workDown=0;
	protected boolean 	glows=false;

	public StdClanCommonItem()
	{
		super();

		setName("a clan workers item");
		basePhyStats.setWeight(1);
		setDisplayText("an workers item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setClanItemType(ClanItem.ClanItemType.GATHERITEM);
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	public boolean fireHere(final Room R)
	{
		for(int i=0;i<R.numItems();i++)
		{
			final Item I2=R.getItem(i);
			if((I2!=null)
			&&(I2.container()==null)
			&&(CMLib.flags().isOnFire(I2)))
				return true;
		}
		return false;
	}

	protected List<Item> resourceHere(final Room R, final int material)
	{
		final List<Item> here=new ArrayList<Item>();
		for(int i=0;i<R.numItems();i++)
		{
			final Item I2=R.getItem(i);
			if((I2!=null)
			&&(I2.container()==null)
			&&(I2 instanceof RawMaterial)
			&&(((I2.material()&RawMaterial.RESOURCE_MASK)==material)
				||(((I2.material())&RawMaterial.MATERIAL_MASK)==material))
			&&(!CMLib.flags().isEnchanted(I2)))
				here.add(I2);
		}
		return here;
	}

	protected List<Item> resourceHere(final MOB M, final int material)
	{
		final List<Item> here=new Vector<Item>();
		for(int i=0;i<M.numItems();i++)
		{
			final Item I2=M.getItem(i);
			if((I2!=null)
			&&(I2.container()==null)
			&&(I2 instanceof RawMaterial)
			&&(((I2.material()&RawMaterial.RESOURCE_MASK)==material)
				||(((I2.material())&RawMaterial.MATERIAL_MASK)==material))
			&&(!CMLib.flags().isEnchanted(I2)))
				here.add(I2);
		}
		return here;
	}

	public List<Item> resourceHere(final Room R, final List<Integer> materials)
	{
		final List<Item> allMat=new Vector<Item>();
		List<Item> V=null;
		for(int m=0;m<materials.size();m++)
		{
			V=resourceHere(R,materials.get(m).intValue());
			for(int v=0;v<V.size();v++)
				allMat.add(V.get(v));
			V.clear();
		}
		return allMat;
	}

	public List<Item> resourceHere(final MOB M, final List<Integer> materials)
	{
		final List<Item> allMat=new Vector<Item>();
		List<Item> V=null;
		for(int m=0;m<materials.size();m++)
		{
			V=resourceHere(M,materials.get(m).intValue());
			for(int v=0;v<V.size();v++)
				allMat.add(V.get(v));
			V.clear();
		}
		return allMat;
	}

	public List<Integer> enCode(final MOB M, String req)
	{
		req=req.toUpperCase();
		final List<Integer> V=new Vector<Integer>();
		for(final RawMaterial.Material m : RawMaterial.Material.values())
		{
			final int x=req.indexOf(m.desc());
			if(x<0)
				continue;
			if((x>0)&&Character.isLetter(req.charAt(x-1)))
				continue;
			if(((x+m.desc().length())<req.length())
			&&Character.isLetter(req.charAt((x+m.desc().length()))))
				continue;
			V.add(Integer.valueOf(m.mask()));
		}
		final RawMaterial.CODES codes = RawMaterial.CODES.instance();
		for(int s=0;s<codes.total();s++)
		{
			final String S=codes.name(s);
			final int x=req.indexOf(S);
			if(x<0)
				continue;
			if((x>0)&&Character.isLetter(req.charAt(x-1)))
				continue;
			if(((x+S.length())<req.length())
			&&Character.isLetter(req.charAt((x+S.length()))))
				continue;
			V.add(Integer.valueOf(codes.get(s)));
		}
		if((M.location()!=null)
		&&(V.contains(Integer.valueOf(RawMaterial.MATERIAL_METAL)))
		&&(resourceHere(M.location(),RawMaterial.MATERIAL_WOODEN).size()==0))
			V.add(Integer.valueOf(RawMaterial.MATERIAL_WOODEN));
		return V;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		if((glows)
		&&(affected instanceof MOB)
		&&(!super.amWearingAt(Wearable.IN_INVENTORY))
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).location().domainType()==Room.DOMAIN_INDOORS_CAVE)
		&&(((MOB)affected).getStartRoom()!=null)
		&&(((MOB)affected).getStartRoom().getArea()==((MOB)affected).location().getArea()))
			stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

	@Override
	public void setReadableText(final String newText)
	{
		super.setReadableText(newText);
		glows=(newText.equalsIgnoreCase("Mining"));
	}

	public boolean trackTo(final MOB M, final MOB M2)
	{
		final Ability A=CMClass.getAbility("Skill_Track");
		if(A!=null)
		{
			final Room R=M2.location();
			if((R!=null)&&(CMLib.flags().isInTheGame(M2,true)))
			{
				A.invoke(M,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\""),R,true,0);
				return true;
			}
		}
		return false;
	}

	public boolean trackTo(final MOB M, final Room R)
	{
		final Ability A=CMClass.getAbility("Skill_Track");
		if((A!=null)&&(R!=null))
		{
			A.invoke(M,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\""),R,true,0);
			return true;
		}
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_CLANITEM)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(readableText().length()>0)
		&&(((MOB)owner()).getClanRole(clanID())!=null)
		&&((--workDown)<=0)
		&&(!CMLib.flags().isATrackingMonster((MOB)owner()))
		&&(CMLib.flags().isInTheGame((MOB)owner,true))
		&&(!CMLib.flags().isAnimalIntelligence((MOB)owner())))
		{
			workDown=CMLib.dice().roll(1,7,0);
			final MOB M=(MOB)owner();
			if(M.fetchEffect(readableText())==null)
			{
				final Ability A=CMClass.getAbility(readableText());
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
				{
					A.setProficiency(100);
					boolean success=false;
					if(((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_CRAFTINGSKILL)
					&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_EPICUREAN)
					&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_BUILDINGSKILL)
					&&(CMLib.flags().isMobile(M)))
					{
						final PairList<MOB,List<Integer>> DV=needChart.get(M.location().getArea());
						if(DV!=null)
						{
							List<Integer> needs=null;
							MOB M2=null;
							boolean getToWork=false;
							if(A.ID().equalsIgnoreCase("FireBuilding"))
							{
								MOB possibleMOBToGoTo=null;
								for(int i=0;i<DV.size();i++)
								{
									try
									{
										final int rand=i;
										needs=DV.get(rand).second;
										M2=DV.get(rand).first;
									}
									catch (final Exception e)
									{
										continue;
									}
									if((needs!=null)
									&&(M2!=null)
									&&(needs.contains(Integer.valueOf(RawMaterial.MATERIAL_METAL)))
									&&(!fireHere(M2.location()))
									&&(resourceHere(M2.location(),RawMaterial.MATERIAL_WOODEN).size()>0))
									{
										if(M.location()==M2.location())
										{
											getToWork=true;
											break;
										}
										else
										if((possibleMOBToGoTo==null)||(CMLib.dice().roll(1,2,0)==1))
											possibleMOBToGoTo=M2;
									}
								}
								if((!getToWork)
								&&(possibleMOBToGoTo!=null)
								&&(trackTo(M,possibleMOBToGoTo)))
								{
									return true;
								}
							}
							List<Item> rsc=null;
							// if I have the stuff on hand.
							if(!getToWork)
							for(int i=DV.size()-1;i>=0;i--)
							{
								try
								{
									final int rand=i;
									needs=DV.get(rand).second;
									M2=DV.get(rand).first;
									if(!CMLib.flags().isInTheGame(M2,true))
									{
										DV.remove(i);
										continue;
									}
								}
								catch(final Exception e)
								{
									continue;
								}
								if((needs!=null)
								&&(M2!=null)
								&&(M.location()==M2.location()))
								{
									rsc=resourceHere(M,needs);
									if(rsc.size()>0)
									{
										for(int r=0;r<rsc.size();r++)
											CMLib.commands().postDrop(M,rsc.get(r),false,true,false);
										return true;
									}
								}
							}
							if(!getToWork)
							{
								for(int i=0;i<DV.size();i++)
								{
									try
									{
										final int rand=CMLib.dice().roll(1,DV.size(),-1);
										needs=DV.get(rand).second;
										M2=DV.get(rand).first;
									}
									catch(final Exception e)
									{
										continue;
									}
									if((needs!=null)
									&&(M2!=null)
									&&(M.location()!=M2.location()))
									{
										rsc=resourceHere(M,needs);
										if((rsc.size()>0)
										&&(trackTo(M,M2)))
											return true;
									}
								}
							}
							if(!getToWork)
							{
								for(int i=0;i<DV.size();i++)
								{
									try
									{
										final int rand=CMLib.dice().roll(1,DV.size(),-1);
										needs=DV.get(rand).second;
										M2=DV.get(rand).first;
									}
									catch(final Exception e)
									{
										continue;
									}
									if((needs!=null)
									&&(M2!=null)
									&&(M.location()!=M2.location()))
									{
										rsc=resourceHere(M.location(),needs);
										if(rsc.size()>0)
										{
											for(int r=0;r<rsc.size();r++)
												CMLib.commands().postGet(M,null,rsc.get(r),false);
											if(trackTo(M,M2))
												return true;
										}
									}
								}
							}
						}
					}

					if((M.location()!=null)
					&&(CMLib.flags().isAliveAwakeMobileUnbound(M,true))
					&&(!CMLib.flags().canBeSeenBy(M.location(),M)))
					{
						switch(CMLib.dice().roll(1,7,0))
						{
						case 1:
							CMLib.commands().postSay(M, null, L("I can't see a thing."));
							break;
						case 2:
							CMLib.commands().postSay(M, null, L("It's too dark to work."));
							break;
						case 3:
							CMLib.commands().postSay(M, null, L("How am I supposed to work in these conditions?"));
							break;
						case 4:
							CMLib.commands().postSay(M, null, L("Too dadgum dark."));
							break;
						case 5:
							CMLib.commands().postSay(M, null, L("Is anyone there?  I can't see!"));
							break;
						case 6:
							CMLib.commands().postSay(M, null, L("Someone turn on the lights so I can work!"));
							break;
						case 7:
							CMLib.commands().postSay(M, null, L("I could use some light, if you expect me to work."));
							break;
						}
					}

					if((M.numItems()>1)
					&&(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
						||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_EPICUREAN)
						||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL)))
					{
						Item I=null;
						int tries=0;
						while((I==null)&&((++tries)<20))
						{
							I=M.getRandomItem();
							if((I==null)
							||(I==this)
							||(I instanceof RawMaterial)
							||(!I.amWearingAt(Wearable.IN_INVENTORY)))
								I=null;
						}
						final Vector<String> V=new Vector<String>();
						if(I!=null)
							V.addElement(I.name());
						success=A.invoke(M,V,null,false,phyStats().level());
					}
					else
						success=A.invoke(M,new Vector<String>(),null,false,phyStats().level());
					if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
					||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_EPICUREAN))
					{
						PairList<MOB,List<Integer>> DV=needChart.get(M.location().getArea());
						if(!success)
						{
							if(DV==null)
							{
								DV=new PairSVector<MOB,List<Integer>>();
								needChart.put(M.location().getArea(),DV);
							}
							DV.removeElementFirst(M);
							final String req=A.accountForYourself();
							final int reqIndex=req.indexOf(':');
							if(reqIndex>0)
								DV.add(M,enCode(M,req.substring(reqIndex+1)));
							else
								DV.add(M,enCode(M,req));
						}
						else
						if(DV!=null)
							DV.removeElementFirst(M);
					}
				}
			}
		}
		return true;
	}
}
