package com.planet_ink.coffee_mud.web;

import java.io.*;
import java.util.*;


// simple wrapper for returned file from FileGrabber
//  contains a File and a state integer
// yes yes should be using wrapper functions, but it's
//  so simple I don't see the point

// if state != OK, file is not guaranteed to be non-null

public class GrabbedFile
{
	public File file;
	public int state;
	
	public static final int STATE_OK = 0;
	public static final int STATE_IS_DIRECTORY = 1;	// file will be valid - not an error
	public static final int STATE_BAD_FILENAME = 2;
	public static final int STATE_NOT_FOUND = 3;
	public static final int STATE_INTERNAL_ERROR = 4;
	public static final int STATE_SECURITY_VIOLATION = 5;
	
}