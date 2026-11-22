package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

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
 * A web-path macro for validating misc-text parameters for an ability.
 * Post (or get) two fields: ABILITY (the ability ID) and PARMS (the
 * parameter text to be validated).  The response body is empty when the
 * parameters are valid, and otherwise contains guidance explaining the
 * first mismatch.
 *
 * @author Bo Zimmerman
 */
public class AbilityParmsCheck extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityParmsCheck";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final boolean helpMode = httpReq.getUrlParameter("HELP") != null;
		final boolean editorMode = httpReq.getUrlParameter("EDITOR") != null;
		httpResp.setMimeType(((helpMode || editorMode) ? MIMEType.All.html : MIMEType.All.txt).getType());
		final String abilityID = httpReq.getUrlParameter("ABILITY");
		if((abilityID == null) || (abilityID.trim().length() == 0))
			return "no ability or behavior was named";
		String text = httpReq.getUrlParameter("PARMS");
		if(text == null)
			text = "";
		final String id = abilityID.trim();
		String name = null;
		String format = null;
		try
		{
			Ability A = CMClass.getAbility(id);
			if(A != null)
			{
				name = A.Name();
				format = A.miscTextFormat();
			}
			else
			{
				final Behavior B = CMClass.getBehavior(id);
				if(B == null)
					return "no ability or behavior named '" + id + "'";
				name = B.name();
				format = B.parmsFormat();
			}
			if(format == null)
				format = "";
			format = format.trim();
			if(format.equalsIgnoreCase(CMParms.FORMAT_UNDEFINED))
				format = "";
			if(helpMode)
				return this.helpEntry(id, name, format);
			if(editorMode)
				return this.editorEntry(httpReq, id, name, format, text, httpReq.getUrlParameter("TEXTONLY") != null);
			// nothing was declared to check against, so anything passes
			if(format.length() == 0)
				return "";
			final String isInvalid = CMLib.ableParms().testParmsFormat(format, text.trim());
			if(isInvalid != null)
				return isInvalid;
			return "";
		}
		catch(final Exception e)
		{
			Log.errOut("AbilityParmsCheck", "Error checking parms for '"+id+"': "+e.getMessage());
			return "parms could not be checked: " + e.getMessage();
		}
	}
	/**
	 * Wraps a parms format string for display, preferring to break between
	 * the groups of a format (at the slashes) rather than at the spaces, so
	 * that the arguments of a single placeholder are never split apart.
	 *
	 * @param s the format string to wrap
	 * @param limit the maximum line length
	 * @return the wrapped and html-escaped format string
	 */
	public static String wrapFormat(final String s, final int limit)
	{
		if(s == null)
			return "";
		final StringBuilder str = new StringBuilder(CMStrings.replaceAll(CMStrings.replaceAll(s, "&", "&amp;"), "<", "&lt;"));
		int slashAt = -1;
		int spaceAt = -1;
		int lineStart = 0;
		int x = 0;
		while(x < str.length())
		{
			final char c = str.charAt(x);
			if(x > lineStart)
			{
				if(c == '/')
					slashAt = x;
				else
				if(c == ' ')
					spaceAt = x;
			}
			x++;
			if((x - lineStart >= limit)&&((slashAt > lineStart)||(spaceAt > lineStart)))
			{
				if(slashAt > lineStart)
				{
					str.insert(slashAt, "<BR>");
					lineStart = slashAt + 4;
				}
				else
				{
					str.replace(spaceAt, spaceAt+1, "<BR>");
					lineStart = spaceAt + 4;
				}
				x = lineStart;
				slashAt = -1;
				spaceAt = -1;
			}
		}
		return str.toString();
	}

	/**
	 * Renders the parms of an ability or behavior as editors, along with the parms
	 * they currently spell out and what the validator says about them.
	 *
	 * @param httpReq the http request being answered
	 * @param id the id of the ability or behavior
	 * @param name the name of the ability or behavior
	 * @param format the parms format of the ability or behavior
	 * @param text the parms the box holds now
	 * @param bodyOnly true to answer with the parms text and error alone
	 * @return the html for the editor
	 */
	protected String editorEntry(final HTTPRequest httpReq, final String id, final String name, final String format, final String text, final boolean bodyOnly)
	{
		try
		{
			final String parms = CMLib.ableParms().parmsEditorText(httpReq, format, text);
			final String err = (format.length() == 0) ? null : CMLib.ableParms().testParmsFormat(format, parms);
			if(bodyOnly)
				return "TEXT:" + escape(parms) + "\n" + escape(err);
			final StringBuilder str = new StringBuilder("");
			str.append("<INPUT TYPE=HIDDEN ID=PEDITORTEXT VALUE=\"").append(escape(parms)).append("\">");
			str.append("<DIV ID=PARMSEDITORMSG>");
			if(err == null)
				str.append("<FONT COLOR=#B0FFB0>valid parameters</FONT>");
			else
				str.append("<FONT COLOR=#FFC0CB>").append(escape(err)).append("</FONT>");
			str.append("</DIV>");
			if(format.length() == 0)
				str.append("<I>Nothing was declared for ").append(escape(name)).append(", so anything may be written here.</I>");
			else
				str.append(CMLib.ableParms().parmsEditorHtml(httpReq, format, text));
			return this.clearWebMacros(str.toString());
		}
		catch(final CMException e)
		{
			return "<B>" + escape(id) + " has a malformed format: " + escape(e.getMessage()) + "</B>";
		}
	}

	/**
	 * Escapes a value for safe use in html text and attribute values.
	 *
	 * @param str the value to escape
	 * @return the escaped value
	 */
	protected static String escape(final String str)
	{
		if(str == null)
			return "";
		String s = CMStrings.replaceAll(str, "&", "&amp;");
		s = CMStrings.replaceAll(s, "\"", "&quot;");
		s = CMStrings.replaceAll(s, "<", "&lt;");
		return CMStrings.replaceAll(s, ">", "&gt;");
	}

	protected String helpEntry(final String id, final String name, final String format)
	{
		final StringBuilder str = new StringBuilder("");
		str.append("<B>").append(name).append("</B> (").append(id).append(")<BR>");
		str.append("<B>Format:</B> ");
		if(format.length() == 0)
			str.append("none declared - there is nothing to check against");
		else
			str.append(wrapFormat(format, 60)); // allow for the prefix above
		str.append("<BR><BR>");
		final String help = CMLib.help().getHelpText(id, null, true);
		if((help == null) || (help.length() == 0))
			str.append("<I>No help text is available for ").append(id).append(".</I>");
		else
			str.append(this.helpHelp(help, 78));
		return this.clearWebMacros(str.toString());
	}
	

	/** The title given to the misctext/parms boxes that open an editor on double-click. */
	public static final String PARMS_EDITOR_TITLE="double-click for editor";

	/**
	 * Builds the little '?' link that opens the help for whatever ability or behavior
	 * is currently selected in the given form field.
	 *
	 * @param fieldName the name of the select field to get the help topic from
	 * @return the html for the help link
	 */
	public static String helpLink(final String fieldName)
	{
		return "<A HREF=\"javascript:OpenHelpFor('"+fieldName+"')\" TITLE=\"help\"><FONT COLOR=YELLOW>?</FONT></A>";
	}

	/**
	 * The extra attributes that make a misctext/parms box announce, and respond to,
	 * a double-click request for an editor.
	 *
	 * @param fieldName the name of the select field holding the ability or behavior
	 * @return the html attributes to add to the parms text field
	 */
	public static String parmsEditorAttrs(final String fieldName)
	{
		return " TITLE=\""+PARMS_EDITOR_TITLE+"\" ONDBLCLICK=\"OpenParmsEditor(this,'"+fieldName+"');\"";
	}
}
