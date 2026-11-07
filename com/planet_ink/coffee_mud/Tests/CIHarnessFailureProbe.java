package com.planet_ink.coffee_mud.Tests;

import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/** Temporary test-branch probe. Remove after verifying a red CI run and its report. */
public class CIHarnessFailureProbe extends StdTest
{
	@Override
	public String ID()
	{
		return "CIHarnessFailureProbe";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"party"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		return "Intentional CI failure probe. This test must make the Actions job fail.";
	}
}
