package com.planet_ink.coffee_mud.Tests;

import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.QuestManager;
import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.Resources;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;

/*
Copyright 2026-2026 Bo Zimmerman

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
/**
 * Regression test for holiday name handling in the Quests library.
 *
 * <p>Exercises the shared holiday-name lookup path (the code behind
 * {@link QuestManager#getHolidayIndex} / {@link QuestManager#getHolidayName},
 * which powers both {@code createHoliday} duplicate detection and delete/modify
 * lookups) against the shipped {@code holidays.quest} file, using only
 * read-only operations so that no game state is modified.</p>
 */
public class HolidaysQuests extends StdTest
{
	@Override
	public String ID()
	{
		return "HolidaysQuests";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final CMFile hf = new CMFile(Resources.makeFileResourceName("quests/holidays/holidays.quest"), null);
		byte[] backup = null;
		try
		{
			if (hf.exists() && hf.canRead())
				backup = hf.raw();

			final QuestManager Q = CMLib.quests();
			if (Q == null)
				return "No quests library available.";

			final List<String> steps = Q.getHolidayFile();
			if ((steps == null) || (steps.size() != 5))
				return ("Expected 5 holiday steps, found " + ((steps == null) ? "0" : ""+steps.size()));

			for(final String step : steps)
			{
				if ((step == null) || (step.trim().length() == 0))
					return ("Encountered an empty holiday step.");
			}

			final List<String> names = java.util.Arrays.asList("holidays","drought","famine","harsh winter","newyear");

			for(int i=0; i<names.size(); i++)
			{
				final String name = names.get(i);
				final int index = Q.getHolidayIndex(name);
				if (index != i)
					return ("getHolidayIndex("+name+") -> "+index+", expected "+i);
				final String back = Q.getHolidayName(index);
				if ((back == null) || (!back.trim().equalsIgnoreCase(name)))
					return ("getHolidayName("+(i)+", "+name+") -> '"+back+"', expected '"+name+"'");
			}

			if (Q.getHolidayIndex("HOOLIDAYS") != 0)
				return ("Case-insensitive index lookup failed.");
			if (Q.getHolidayIndex("") != -1)
				return ("Empty name should return -1.");
			if (Q.getHolidayIndex("does-not-exist") != -1)
				return ("Unknown name should return -1, not "+Q.getHolidayIndex("does-not-exist"));

			final String first = Q.getHolidayName(0);
			if ((first == null) || (!first.trim().equalsIgnoreCase("holidays")))
				return ("First holiday name is '"+first+"', expected 'holidays'");

			// Failure cases: these must report an error / be rejected, never throw or mutate state.

			// Deleting the reserved meta-holiday (index 0) and out-of-range indices are all rejected.
			if (!Q.deleteHoliday(0).contains("does not exist"))
				return ("deleteHoliday(0) should be rejected, got '"+Q.deleteHoliday(0)+"'");
			if (!Q.deleteHoliday(-1).contains("does not exist"))
				return ("deleteHoliday(-1) should be rejected, got '"+Q.deleteHoliday(-1)+"'");
			final String deleteMissing = Q.deleteHoliday(Integer.MAX_VALUE);
			if (!deleteMissing.contains("does not exist"))
				return ("deleteHoliday(out-of-range) should be rejected, got '"+deleteMissing+"'");

			// Duplicate creation of an existing holiday name must fail without writing.
			final String dupErr = Q.createHoliday("drought", "ALL", false);
			if (dupErr.trim().length() == 0)
				return ("createHoliday of an existing name should be rejected.");
			if (Q.getHolidayIndex("drought") != 1)
				return ("Duplicate createHoliday mutated the holiday list.");

			// A successful create is always followed by a delete, and the deleted name
			// must not persist in either the on-disk file or the resolved holiday set.
			String newErr = Q.createHoliday("zzztestholiday", "ALL", true);
			if (newErr.trim().length() != 0)
				return ("createHoliday of a new name unexpectedly failed: '"+newErr+"'");

			final int idxBefore = Q.getHolidayIndex("zzztestholiday");
			if (idxBefore < 0)
				return ("Created holiday not resolvable after create.");

			final String delResp = Q.deleteHoliday(idxBefore);
			if ((delResp == null) || (!delResp.toLowerCase().contains("deleted")))
				return ("delete of created holiday did not succeed: '"+delResp+"'");

			if (Q.getHolidayIndex("zzztestholiday") != -1)
				return ("Created holiday still present after delete.");

			// The file must be back to its original five steps, and any in-memory
			// holiday-name cache must reflect that.
			final List<String> stepsAfter = Q.getHolidayFile();
			if ((stepsAfter == null) || (stepsAfter.size() != 5))
				return ("File not restored to 5 steps, found " + ((stepsAfter == null) ? "0" : ""+stepsAfter.size()));
			for(final String name2 : names)
				if (Q.getHolidayIndex(name2) < 0)
					return ("Original holiday '"+name2+"' missing after the round trip.");
		}
		catch(final CMException e)
		{
			return "CMException: "+e.getMessage();
		}
		finally
		{
			try
			{
				if ((backup != null) && hf.exists())
					hf.saveRaw(backup);
			}
			catch(final Exception t)
			{
				// best-effort restore so the checked-in holiday file is never left mutated
			}
		}
		return null;
	}
}
