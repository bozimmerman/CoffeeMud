package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class WebHelper
{
	// makes a simple error page if no error.cmvp exists
	//  does NOT imbed macros just inserts directly into page -
	//   this means no dependancies on pre-existing macros
	public static byte[] makeErrorPage(String s1, String s2)
	{
		StringBuffer s = new StringBuffer("<html><head><title>");
		s.append(s1);
		s.append("</title></head><body><p><h1>");
		s.append(s1);
		s.append("</h1>");

		if (s2 != null)
		{
			s.append(s2);
			s.append("<br>");
		}

		s.append("<br><hr><i>");
		s.append(HTTPserver.ServerVersionString);
		s.append("</i></body></html>");

		return s.toString().getBytes();
	}
}