package com.planet_ink.coffee_mud.commands.base;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

// requires nothing to load
public class Social
{
	public String Social_name;
	public String You_see;
	public String Third_party_sees;
	public String Target_sees;
	public String See_when_no_target;
	public int sourceCode=Affect.MSG_OK_ACTION;
	public int othersCode=Affect.MSG_OK_ACTION;
	public int targetCode=Affect.MSG_OK_ACTION;
}
