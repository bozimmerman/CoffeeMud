package com.planet_ink.coffee_mud.Libraries.editors;

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

import java.util.List;

import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.ParmType;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.core.CMLib;

public class EditorXml extends AbilityParmEditorImpl
{
	public EditorXml()
	{
		super("XML",CMLib.lang().L("XML"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if((oldVal == null)||(oldVal.trim().length()==0))
			return false;
		final List<XMLTag> pieces = CMLib.xml().parseAllXML(oldVal);
		if((pieces == null)||(pieces.size()==0))
			return false;
		for(final XMLTag piece : pieces)
		{
			if(!allPiecesClosed(piece))
				return false;
		}
		return true;
	}

	protected boolean allPiecesClosed(final XMLTag piece)
	{
		if(piece.outerEndIndex() < piece.outerStartIndex())
			return false;
		for(final XMLTag inner : piece.contents())
		{
			if(!allPiecesClosed(inner))
				return false;
		}
		return true;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "<FOO/>";
	}

	@Override
	public String defaultValue()
	{
		return "<FOO/>";
	}
}
