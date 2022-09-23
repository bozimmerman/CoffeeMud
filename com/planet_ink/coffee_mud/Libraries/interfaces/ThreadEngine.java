package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2004-2022 Bo Zimmerman

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
public interface ThreadEngine extends CMLibrary, Runnable
{
	// tick related
	public TickClient startTickDown(Tickable E, int tickID, long tickTimeMs, int numTicks);
	public TickClient startTickDown(Tickable E, int tickID, int numTicks);
	public boolean isTicking(Tickable E, int tickID);
	public long getTimeMsToNextTick(Tickable E, int tickID);

	public Iterator<TickableGroup> tickGroups();
	public long getTickGroupPeriod(final Tickable E, final int tickID);

	public boolean deleteTick(Tickable E, int tickID);
	public boolean deleteAllTicks(Tickable E);

	public boolean setTickPending(Tickable E, int tickID);
	public void suspendTicking(Tickable E, int tickID);
	public void resumeTicking(Tickable E, int tickID);
	public void suspendResumeRecurse(CMObject O, boolean skipEmbeddedAreas, boolean suspend);
	public boolean isSuspended(Tickable E, int tickID);
	public void suspendAll(CMRunnable[] exceptRs);
	public void resumeAll();
	public boolean isAllSuspended();

	public void scheduleRunnable(Runnable R, long ellapsedMs);
	public void executeRunnable(Runnable R);
	public void executeRunnable(String threadGroupName, Runnable R);
	public void executeRunnable(final char threadGroupId, final Runnable R);

	public void tickAllTickers(Room here);
	public void rejuv(Room here, int tickID);
	public void clearDebri(Room room, int taskCode);

	public String getTickInfoReport(String which);
	public String getSystemReport(String itemCode);
	public String getTickStatusSummary(Tickable obj);
	public List<Tickable> getNamedTickingObjects(String name);
	public List<TickClient> findTickClient(final String name, final boolean exactOnly);
	public Runnable findRunnableByThread(final Thread thread);
	public void dumpDebugStack(final String ID, Thread theThread);
}
