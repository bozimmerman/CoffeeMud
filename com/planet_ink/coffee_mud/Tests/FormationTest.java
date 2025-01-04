package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
public class FormationTest extends StdTest
{
	@Override
	public String ID()
	{
		return "FormationTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		// who attacks who, who is targeting whom immediately, who is targeting whom eventually
		final int[][] tests = new int[][] {
			{0,  0,-1, -1,0, 0,-1},
			{0,  1,-1, -1,0, 0,-1, -1,0, 1,-1},
			{0,  2,-1, -1,0, 0,-1, -1,0, 2,-1},
			{0,  3,-1, -1,0, 0,-1, -1,0, 3,-1},
			{1,  0,-1, -1,0, 0,-1},
			{1,  1,-1, -1,0, 0,-1, -1,0, 1,-1},
			{1,  2,-1, -1,0, 0,-1, -1,0, 2,-1},
			{1,  3,-1, -1,0, 0,-1, -1,0, 3,-1},
			{2,  0,-1, -1,0, 0,-1},
			{2,  1,-1, -1,0, 0,-1, -1,0, 1,-1},
			{2,  2,-1, -1,0, 0,-1, -1,0, 2,-1},
			{2,  3,-1, -1,0, 0,-1, -1,0, 3,-1}
		};
		for(int tnum=0;tnum<tests.length;tnum++)
		{
			final int[] test = tests[tnum];
			final Area tempA=CMClass.getAreaType("StdArea");
			tempA.setName("TempArea");
			final Room R1=CMClass.getLocale("Plains");
			R1.setRoomID("TempArea#1");
			R1.setArea(tempA);
			MOB leaderM = null;
			MOB enemyM = null;
			final List<MOB> grp = new ArrayList<MOB>(); // you can randomize this if you want.
			try
			{
				for(int i=0;i<5;i++)
				{
					final MOB M = CMClass.getMOB("StdMOB");
					M.baseCharStats().setMyRace(CMClass.getRace("Human"));
					M.setName(L("Player"+i));
					M.basePhyStats().setLevel(1);
					M.recoverCharStats();
					M.recoverPhyStats();
					M.bringToLife(R1,true);
					final Weapon W = CMClass.getWeapon("StdWeapon");
					M.addItem(W);
					W.setRawWornCode(Wearable.WORN_WIELD);
					W.setRanges(test[0], test[0]);
					M.setAttribute(Attrib.AUTOMELEE, true);
					grp.add(M);
					if(leaderM == null )
						leaderM = M;
					else
					{
						M.setFollowing(leaderM);
						leaderM.addFollower(M, i);
					}
				}
				{
					enemyM = CMClass.getMOB("StdMOB");
					enemyM.baseCharStats().setMyRace(CMClass.getRace("Human"));
					enemyM.setName(L("Enemy"));
					enemyM.basePhyStats().setLevel(1);
					enemyM.recoverCharStats();
					enemyM.recoverPhyStats();
					enemyM.bringToLife(R1,true);
					enemyM.setAttribute(Attrib.AUTOMELEE, true);
				}
				final int start = 1;
				final MOB attacker = (test[start+0] >= 0)?grp.get(test[start+0]):enemyM;
				final MOB defender = (test[start+1] >= 0)?grp.get(test[start+1]):enemyM;
				CMLib.combat().postAttack(attacker,defender,attacker.fetchWieldedItem());
				final MOB chk1 = (test[start+2] >= 0)?grp.get(test[start+2]):enemyM;
				final MOB chkV = (test[start+3] >= 0)?grp.get(test[start+3]):enemyM;
				if(chk1.getVictim()!=chkV)
					return "Fail(test#"+tnum+".0): "+((chk1.getVictim()==null)?"null":chk1.getVictim().name());
				for(int i=0;i<grp.size();i++)
				{
					if(grp.get(i).isInCombat() && grp.get(i).rangeToTarget()!=test[0]+i)
						return "Fail(test#"+tnum+".X"+i+"): "+grp.get(i).name()+"=="+grp.get(i).rangeToTarget();
				}
				enemyM.tick(enemyM, Tickable.TICKID_MOB);
				for(final MOB M : grp)
					M.tick(M, Tickable.TICKID_MOB);
				for(int i=start+4;i<test.length;i+=2)
				{
					final MOB chk2 = (test[i] >= 0)?grp.get(test[i]):enemyM;
					final MOB chkV2 = (test[i+1] >= 0)?grp.get(test[i+1]):enemyM;
					if(chk2.getVictim()!=chkV2)
						return "Fail(test#"+tnum+".Y"+i+"): "+((chk2.getVictim()==null)?"null":chk2.getVictim().name());
				}
				for(int i=0;i<grp.size();i++)
				{
					if(grp.get(i).isInCombat() && grp.get(i).rangeToTarget()!=test[0]+i)
						return "Fail(test#"+tnum+".Z"+i+"): "+grp.get(i).getVictim().name()+"=="+grp.get(i).rangeToTarget();
				}
			}
			finally
			{
				if(enemyM != null)
					enemyM.destroy();
				for(final MOB M : grp)
					M.destroy();
				R1.destroy();
				tempA.destroy();
			}
		}
		return null;
	}
}
