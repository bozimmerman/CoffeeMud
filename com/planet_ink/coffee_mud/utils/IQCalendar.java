package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.text.*;

public class IQCalendar extends GregorianCalendar
{

	public final static long MILI_SECOND=1000;
	public final static long MILI_MINUTE=MILI_SECOND*60;
	public final static long MILI_HOUR=MILI_MINUTE*60;
	public final static long MILI_DAY=MILI_HOUR*24;
	
	
	public IQCalendar(Calendar C)
	{
		super();
		this.setTime(C.getTime());
		this.setTimeZone(C.getTimeZone());
	}
	
	public IQCalendar(long time)
	{
		super();
		this.setTimeInMillis(time);
	}
	
	public IQCalendar()
	{
		super();
	}
	
	/**
	 * Returns the numeric representation of the month
	 * 
  	 * <br><br><b>Usage:</b> Month2MM("January");
	 * @param Month The month name
	 * @return String The number of the month as a string
	 */	
	public static String month2MM(String Month)
	{
		if (Month.equals("January"))
			return "01";
		if (Month.equals("February"))
			return "02";
		if (Month.equals("March"))
			return "03";
		if (Month.equals("April"))
			return "04";
		if (Month.equals("May"))
			return "05";
		if (Month.equals("June"))
			return "06";
		if (Month.equals("July"))
			return "07";
		if (Month.equals("August"))
			return "08";
		if (Month.equals("September"))
			return "09";
		if (Month.equals("October"))
			return "10";
		if (Month.equals("November"))
			return "11";
		if (Month.equals("December"))
			return "12";
		
		return "01";
	}
	
	

	/**
	 * Return the name of the month, given a number
	 * 
  	 * <br><br><b>Usage:</b> String Mnth=ReturnMonthName(m,GiveShort).charStats();
	 * @param Number Month number to convert
	 * @param GiveShort Give abbreviation if true
	 * @return String Month name
	 */
	public static String getMonthName(int Number, boolean GiveShort)
	{
		if(Number>12)Number=Number%12;
		
		if(!GiveShort)
			switch(Number)
			{
				case 1: return "January";
				case 2: return "February";
				case 3: return "March";
				case 4: return "April";
				case 5: return "May";
				case 6: return "June";
				case 7: return "July";
				case 8: return "August";
				case 9: return "September";
				case 10: return "October";
				case 11: return "November";
				case 12: return "December";
			}
		else
			switch(Number)
			{
				case 1: return "Jan";
				case 2: return "Feb";
				case 3: return "Mar";
				case 4: return "Apr";
				case 5: return "May";
				case 6: return "Jun";
				case 7: return "Jul";
				case 8: return "Aug";
				case 9: return "Sep";
				case 10: return "Oct";
				case 11: return "Nov";
				case 12: return "Dec";
			}
			
		return "";
	}
	
	
	/**
	 * Converts a string of some form into a IQCalendar object.
	 * 
  	 * <br><br><b>Usage:</b> IQCalendar.S2Date(GetRes(Results,"StartDateTime"));
	 * @param TheDate The string to retrieve from
	 * @return IQCalendar IQCalendar object
	 */
	public static long string2Millis(String TheDate)
	{
		IQCalendar C=string2Date(TheDate);
		if(C!=null) return C.getTimeInMillis();
		return 0;
	}
	
	/**
	 * Converts a string of some form into a IQCalendar object.
	 * 
  	 * <br><br><b>Usage:</b> IQCalendar.S2Date(GetRes(Results,"StartDateTime"));
	 * @param TheDate The string to retrieve from
	 * @return IQCalendar IQCalendar object
	 */
	public static IQCalendar string2Date(String TheDate)
	{
		IQCalendar D=IQCalendar.getIQInstance();

		if(TheDate==null)
			return D;
		if(TheDate.trim().length()==0)
			return D;

		DateFormat fmt=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);

		// for those stupid SQLServer date formats, clean them up!
		if((TheDate.indexOf(".")==19)||(TheDate.indexOf("-")==4))
		{
			int monthdiv=TheDate.indexOf("-",5);
			if(monthdiv>4)
			{
				String oldDate=TheDate;
				TheDate=oldDate.substring(5,monthdiv).trim()+"/"+oldDate.substring(monthdiv+1,monthdiv+3).trim()+"/"+oldDate.substring(0,4);
			
				int divider=oldDate.indexOf(":",monthdiv+2);
				if(divider>monthdiv)
				{
					int HH=Util.s_int(oldDate.substring(divider-2,divider).trim());
					int MM=Util.s_int(oldDate.substring(divider+1,divider+3).trim());
					String AP="AM";
					if(oldDate.indexOf("PM")>divider)
						AP="PM";
					else
					if(oldDate.indexOf("AM")<divider)
					{
						if(HH==0) HH=12;
						else
						if(HH>=12)
						{
							AP="PM";
							if(HH>12) HH=HH-12;
						}
					}
					TheDate=TheDate+" "+Integer.toString(HH)+":"+Integer.toString(MM)+" "+AP;
				}
				else
					TheDate=TheDate+" 12:00 PM";
			}
		}

		// If it has no time, give it one!
		if((TheDate.indexOf(":")<0)
		&&(TheDate.indexOf("AM")<0)
		&&(TheDate.indexOf("PM")<0))
			TheDate=TheDate+" 5:00 PM";

		try
		{	
			fmt.parse(TheDate);
			D=new IQCalendar(fmt.getCalendar());
		}
		catch(ParseException e)
		{ }
		return D;
	}
	
	
	/**
	 * Returns the regular Hours given the hours in the 
	 * international format (military time)
	 * 
  	 * <br><br><b>Usage:</b> ConvertHour(GetIn(req, "ENDHR"))
	 * @param TheHour Hours in military format
	 * @return String Hours in regular format
	 **/
	public static String convertHour(String TheHour)
	{
		int IntHour =  Util.s_int(TheHour);
		if (IntHour > 12)
		{
			IntHour = IntHour-12;
		}
		else
			if (IntHour == 0)
				IntHour = 12;
		
		TheHour = Integer.toString(IntHour);
		return TheHour;
	}
	
	/**
	 * Returns the AMPM stamp given the international Hours portion the Time
	 * 
  	 * <br><br><b>Usage:</b> getAMPM(GetIn(req, "ENDHR"))
	 * @param TheHour Hours in military format
	 * @return String AM or PM stamp
	 **/
	public static String getAMPM(String TheHour)
	{
		String Stamp;
		
		int IntHour =  Util.s_int(TheHour);
		if (IntHour >= 12)
			Stamp = "PM";
		else 
			Stamp = "AM";
		return Stamp;
	}
	
	/**
	 * Get the zone id given the timezone string
	 * 
  	 * <br><br><b>Usage:</b> GetTheZoneID(MeetTZ.getRawOffset())+"\n";
	 * @param theRawOffset The time zone's raw offset to convert
	 * @return String The time zone ID
	 */
	public static String getTheIntZoneID(int theRawOffset)
	{
		if (theRawOffset == 0)			// GMT 0
			return "GMT";
		if (theRawOffset == 3600000)	// GMT 1
			return "CET";
		if (theRawOffset == 7200000)	// GMT 2
			return "CAT";
		if (theRawOffset == 10800000)	// GMT 3
			return "EAT";
		if (theRawOffset == 12600000)	// GMT 3.5
			return "MET";
		if (theRawOffset == 14400000)	// GMT 4
			return "NET";
		if (theRawOffset == 18000000)	// GMT 5
			return "PLT";
		if (theRawOffset == 19800000)	// GMT 5.5
			return "IST";
		if (theRawOffset == 21600000)	// GMT 6
			return "BST";
		if (theRawOffset == 25200000)	// GMT 7
			return "VST";
		if (theRawOffset == 28800000)	// GMT 8
			return "CTT";
		if (theRawOffset == 32400000)	// GMT 9
			return "JST";
		if (theRawOffset == 34200000)	// GMT 9.5
			return "ACT";
		if (theRawOffset == 36000000)	// GMT 10
			return "AET";
		if (theRawOffset == 39600000)	// GMT 11
			return "SST";
		if (theRawOffset == 43200000)	// GMT 12
			return "NST";
		if (theRawOffset == -39600000)	// GMT -11
			return "MIT";
		if (theRawOffset == -36000000)	// GMT -10
			return "HST";
		if (theRawOffset == -32400000)	// GMT -9
			return "AST";
		if (theRawOffset == -28800000)	// GMT -8
			return "PST";
		if (theRawOffset == -25200000)	// GMT -7
			return "MST";
		if (theRawOffset == -21600000)	// GMT -6
			return "CST";
		if (theRawOffset == -18000000)	// GMT -5
			return "EST";
		if (theRawOffset == -14400000)	// GMT -4
			return "ADT";
		if (theRawOffset == -12600000)	// GMT -3.5
			return "CNT";
		if (theRawOffset == -10800000)	// GMT -3
			return "AGT";
		if (theRawOffset == -7200000)	// GMT -2
			return "BET";
		if (theRawOffset == -3600000)	// GMT -1
			return "EET";
		
		return "GMT";
				
	}
	
	/**
	 *  Returns the time zone of the given ID
	 * 
  	 * <br><br><b>Usage:</b> MEETZN = GetTheTimeZone(ID);
	 * @param theID	The ID of the abbreviated time zone.
	 * @return String The time zone name
	 */
	public static String getTheTimeZone(String theID)
	{
		if (theID.equalsIgnoreCase("CET"))
			return "Europe/Paris";
		if (theID.equalsIgnoreCase("ADT"))
			return "America/Halifax";
		if (theID.equalsIgnoreCase("BET"))
			return "Atlantic/South_Georgia";
		if (theID.equalsIgnoreCase("EET"))
			return "Atlantic/Azores";
		if (theID.equalsIgnoreCase("CAT"))
			return "Europe/Athens";
		if (theID.equalsIgnoreCase("EAT"))
			return "Asia/Riyadh";
		
		return theID;
	}
	
	
	/**
	 * Returns the month for a given date
	 * 
  	 * <br><br><b>Usage:</b> String ENDMM=d2MMString();
	 * @param NA
	 * @return String The month name
	 **/
	public String d2MMString()
	{
		switch (this.get(IQCalendar.MONTH)+1)
		{
			case 1: 
				return "January";
			case 2: 
				return "February";
			case 3: 
				return "March";
			case 4: 
				return "April";
			case 5: 
				return "May";
			case 6: 
				return "June";
			case 7: 
				return "July";
			case 8: 
				return "August";
			case 9: 
				return "September";
			case 10: 
				return "October";
			case 11: 
				return "November";
			case 12: 
				return "December";
			default:
				return "January";
		}
	}

	/**
	 * Returns the DD portion of a given date
	 * 
  	 * <br><br><b>Usage:</b> String ENDDD=d2DDString();
	 * @param NA
	 * @return String The day
	 **/
	public String d2DDString()
	{
		String Day=Integer.toString(this.get(IQCalendar.DAY_OF_MONTH)).trim();
		if (Day.length()==1)
			Day = "0" + Day;
		return Day;
	}

	/**
	* Returns the YYYY portion of a given date
	* Returns the DD portion of a given date
	* 
  	* <br><br><b>Usage:</b> String ENDYYYY=d2YYYYString();
	* @param NA
	* @return String The year
	**/
	public String d2YYYYString()
	{
		String Year=Integer.toString(this.get(IQCalendar.YEAR)).trim();
		if (Year.length()==2)
			Year = "20" + Year;
		return Year;
	}

	/**
	* Returns the Hours portion of a given Time
	* 
  	* <br><br><b>Usage:</b> String ENDHR=T2HRString();
	* @param INTERNATIONALTIME whether international time is used
	* @return String The hour
	**/
	public String t2HRString(boolean INTERNATIONALTIME)
	{
		int IntHour = this.get(IQCalendar.HOUR);
		if (INTERNATIONALTIME)
		{
			if (this.get(IQCalendar.AM_PM) == IQCalendar.PM)
				IntHour = IntHour + 12;
			if ((this.get(IQCalendar.AM_PM) == IQCalendar.AM) &&
				(IntHour==12))
				IntHour = 0;
		}
		else
		{
			if (IntHour==0)
				IntHour=12;
		}
				
		String StrHour=Integer.toString(IntHour);
		if (StrHour.length()==1)
			StrHour = "0" + StrHour;
		return StrHour;
	}
	

	/**
	* Returns the Minutes portion of a given Time
	* 
  	* <br><br><b>Usage:</b> String ENDMIN=T2MINString();
	* @param NA
	* @return String The minutes
	**/
	public String t2MINString()
	{
		int IntMin = this.get(IQCalendar.MINUTE);
		int remainder = IntMin % 5;
		if (remainder != 0)
		{
			if (remainder >= 3)
			{
				IntMin = IntMin + (5 - remainder);
				if (IntMin == 60)
					IntMin = 55;
			}
			else
				IntMin = IntMin - remainder;
		}
		String StrMin=Integer.toString(IntMin);
		if (StrMin.length()==1)
			StrMin = "0" + StrMin;		
		return StrMin;
	}
	
	/**
	 *  Returns the time zone of the server
	 * 
  	 * <br><br><b>Usage:</b> MEETZN = T2ZoneString();
	 * @param NA
	 * @return String The time zone
	 */
	public String t2ZoneString()
	{
		TimeZone CurrentZone;
		CurrentZone = this.getTimeZone();
		String theID = CurrentZone.getID();
		theID = getTheIntZoneID(CurrentZone.getRawOffset());

		return	theID; 
	}
		
	/**
	 * Returns the Minutes portion of a given Time
	 * 
  	 * <br><br><b>Usage:</b> String ST_AMPM=T2_AMPMString();
	 * @param NA
	 * @return String AM or PM stamp
	 **/
	public String t2_AMPMString()
	{
		String AMPM;
	
		if (this.get(IQCalendar.AM_PM)==IQCalendar.PM)
			AMPM="PM";
		else
			AMPM="AM";
			
		return AMPM;
	}

	/**
	 * Converts a given date into a string of form:
	 * International month dd/month name/yyyy
	 * non-international month name/dd/yyyy
	 * International time hh:mm am/pm timezone
	 * non-international time hh:mm timezone
	 * 
  	 * <br><br><b>Usage:</b> d2InternationalDate()+"."
	 * @param NA
	 * @return String International date format
	 */
	public String d2InternationalDate(boolean INTERNATIONALDATE, boolean INTERNATIONALTIME)
	{
		String MONTH = d2MMString();
		String DAY = d2DDString();
		String YEAR = d2YYYYString();
		
		String HOUR = t2HRString(INTERNATIONALTIME);	
		String MINUTE = t2MINString();
		String AMPM = t2_AMPMString();
		String TIMEZONE = t2ZoneString();
		
		String TheStrDateTime = "";
		if (INTERNATIONALDATE)
		{
			TheStrDateTime = DAY + " " + MONTH + ", " + YEAR + " ";
		}
		else
		{
			TheStrDateTime = MONTH + " " + DAY + ", " + YEAR + " ";
		}
		
		if (INTERNATIONALTIME)
		{
			TheStrDateTime = TheStrDateTime + HOUR + ":" + MINUTE  + " " + TIMEZONE;
		}
		else
		{
			TheStrDateTime = TheStrDateTime + HOUR + ":" + MINUTE + " " + AMPM + " " + TIMEZONE;
		}
		return TheStrDateTime;
	
	}
	
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY HH:MM AP
	 * 
  	 * <br><br><b>Usage:</b> d2String()
	 * @param NA
	 * @return String Formatted date/time
	 */
	public static String d2String(long time)
	{
		return new IQCalendar(time).d2String();
	}
	
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY HH:MM AP
	 * 
  	 * <br><br><b>Usage:</b> d2String()
	 * @param NA
	 * @return String Formatted date/time
	 */
	public String d2String()
	{
		String MINUTE=Integer.toString(this.get(IQCalendar.MINUTE)).trim();
		if(MINUTE.length()==1)
			MINUTE="0"+MINUTE;
		String AMPM="AM";
		if(this.get(IQCalendar.AM_PM)==IQCalendar.PM)
			AMPM="PM";
		int Hour=this.get(IQCalendar.HOUR);
		if(Hour==0) Hour=12;
		String Year=Integer.toString(this.get(IQCalendar.YEAR));
		if(Year.length()<4)
		{
			if(Year.length()<2)
				Year=("0"+Year);
			if(Year.length()<2)
				Year=("0"+Year);
			int Yr=Util.s_int(Year);
			if(Yr<50)Year="20"+Year;
			else Year="19"+Year;
		}
		return (this.get(IQCalendar.MONTH)+1)+"/"+this.get(IQCalendar.DATE)+"/"+Year+" "+Hour+":"+MINUTE+" "+AMPM;
	}

	
	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY HH:MM AP
	 * 
  	 * <br><br><b>Usage:</b> d2SString()
	 * @param NA
	 * @return String Formatted date/time
	 */
	public String d2SString()
	{
		String StrDate=d2String();
		if(StrDate.length()<3) return StrDate;
		return (StrDate.substring(0,StrDate.length()-3)+":"+this.get(IQCalendar.SECOND)+" "+StrDate.substring(StrDate.length()-2));
	}

	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YYYY
	 * 
  	 * <br><br><b>Usage:</b> d2DString()
	 * @param NA
	 * @return String Formatted date
	 */
	public String d2DString()
	{
		String T=d2String();
		if(T.indexOf(" ")>0) T=T.substring(0,T.indexOf(" "));
		return T.trim();
	}

	/**
	 * Converts a given date into a string of form:
	 * MM/DD/YY
	 * 
  	 * <br><br><b>Usage:</b> d2D2String()
	 * @param NA
	 * @return String Formatted date
	 */
	public String d2D2String()
	{
		String T=d2DString();
		int x=T.lastIndexOf("/");
		T=T.substring(0,x+1)+T.substring(x+3);
		return T.trim();
	}
	
	public static IQCalendar getIQInstance()
	{
		return new IQCalendar();
	}
}
