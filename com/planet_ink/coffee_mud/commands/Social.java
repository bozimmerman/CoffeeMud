package com.planet_ink.coffee_mud.commands;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.interfaces.*;

// requires nothing to load
public class Social
{
	public String Social_name;
	public String You_see;
	public String Third_party_sees;
	public String Target_sees;
	public String See_when_no_target;
	public int sourceCode=Affect.GENERAL;
	public int othersCode=Affect.GENERAL;
	public int targetCode=Affect.GENERAL;
}
