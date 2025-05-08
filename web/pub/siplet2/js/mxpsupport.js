var MXPEntity = function(theName, theDefinition)
{
	this.name = theName;
	this.definition = theDefinition;
};

var MXPBIT = 
{
	OPEN: 1,
	COMMAND: 2,
	NEEDTEXT: 4,
	SPECIAL: 8,
	HTML: 16,
	NOTSUPPORTED: 32,
	EATTEXT: 64,
	DISABLED: 128
};

var MXPMODE = 
{
	LINE_OPEN: 0,
	LINE_SECURE: 1,
	LINE_LOCKED: 2,
	RESET: 3,
	TEMP_SECURE: 4,
	LOCK_OPEN: 5,
	LOCK_SECURE: 6,
	LOCK_LOCKED: 7,
	LINE_ROOMNAME: 10,
	LINE_ROOMDESC: 11,
	LINE_ROOMEXITS: 12,
	LINE_WELCOME: 19
};

var MXPElement = function(theName, theDefinition, theAttributes, theFlag, theBitmap, theUnsupported)
{
	this.name = theName;
	this.definition = theDefinition;
	this.attributes = theAttributes;
	this.flag = theFlag;
	this.bitmap = theBitmap;
	if(theUnsupported === undefined)
		this.unsupported = '';
	else
		this.unsupported = theUnsupported;
	this.parsedAttributes = null;
	this.userParms = [];
	this.basicElement = true;
	this.attributeValues = null;
	this.alternativeAttributes	= null;
	this.text = null;
	
	if (((this.bitmap & MXPBIT.COMMAND)==0) 
	&& (theDefinition.toUpperCase().indexOf("&TEXT;") >= 0))
		this.bitmap = this.bitmap | MXPBIT.NEEDTEXT;
	if((this.flag != null) && (this.flag.length > 0)) 
		this.bitmap = this.bitmap | MXPBIT.NEEDTEXT;
	
	this.copyOf = function() 
	{
		var E= new MXPElement(this.name, this.definition, this.attributes, this.flag, this.bitmap,this.unsupported);
		if(this.parsedAttributes != null)
			E.parsedAttributes = JSON.parse(JSON.stringify(this.parsedAttributes));
		if((this.userParms != null)&&(this.userParms.length>0))
			E.userParms = JSON.parse(JSON.stringify(this.userParms));
		if(this.attributeValues != null)
			E.attributeValues = JSON.parse(JSON.stringify(this.attributeValues));
		if(this.alternativeAttributes != null)
			E.alternativeAttributes = JSON.parse(JSON.stringify(this.alternativeAttributes));
		return E;
	};
	
	this.getAttributeValue = function(tag)
	{
		tag = tag.toUpperCase().trim();
		this.getParsedAttributes();
		if (tag in this.attributeValues)
			return this.attributeValues[tag];
		return null;
	};

	this.setAttributeValue = function(tag, value)
	{
		tag = tag.toUpperCase().trim();
		this.getParsedAttributes();
		if (tag in this.attributeValues)
			delete this.attributeValues[tag];
		if (value != null)
			this.attributeValues[tag] = value;
	};

	this.getParsedAttributes = function()
	{
		if (this.parsedAttributes != null)
			return this.parsedAttributes;
		this.parsedAttributes = [];
		this.attributeValues = {};
		this.alternativeAttributes = {};
		var buf = this.attributes.trim();
		var bit = '';
		var quotes = '\0';
		var i = -1;
		var lastC = ' ';
		var firstEqual = false;
		while ((++i) < buf.length)
		{
			switch (buf[i])
			{
			case '=':
				if ((!firstEqual) && (bit.length > 0))
				{
					var tag = bit.toUpperCase().trim();
					bit = '';
					this.parsedAttributes.push(tag);
					this.attributeValues[tag] = bit;
				}
				else
					bit += buf[i];
				firstEqual = true;
				break;
			case '\n':
			case '\r':
			case ' ':
			case '\t':
				if (quotes == '\0')
				{
					if ((!firstEqual) && (bit.length > 0))
						this.parsedAttributes.push(bit.toUpperCase().trim());
					bit = '';
					firstEqual = false;
				}
				else
					bit += buf[i];
				break;
			case '"':
			case '\'':
				if (lastC == '\\')
					bit += buf[i];
				else
				if ((lastC == '=')
				|| (quotes != '\0') 
				|| ((quotes == '\0') && ((lastC == ' ') || (lastC == '\t'))))
				{
					if ((quotes != '\0') && (quotes == buf[i]))
					{
						quotes = '\0';
						if ((!firstEqual) && (bit.length > 0))
							this.parsedAttributes.push(bit.toUpperCase().trim());
						bit = '';
						firstEqual = false;
					}
					else
					{
						if (quotes != '\0')
							bit += buf[i];
						else
							quotes = buf[i];
					}
				}
				else
					bit += buf[i];
				break;
			default:
				bit += buf[i];
				break;
			}
			lastC = buf[i];
		}
		if ((!firstEqual) && (bit.length > 0))
			this.parsedAttributes.push(bit.toUpperCase().trim());
		for (var p = this.parsedAttributes.length - 1; p >= 0; p--)
		{
			var PA = this.parsedAttributes[p];
			var VAL = this.attributeValues[PA];
			if ((VAL != null) && (VAL in this.parsedAttributes))
			{
				
				this.parsedAttributes.splice(p,1);
				delete this.attributeValues[PA];
				this.alternativeAttributes[PA] = VAL;
			}
		}
		return this.parsedAttributes;
	};
	
	this.getCloseTags = function(desc)
	{
		var buf = desc;
		var tags = [];
		var bit = null;
		var quotes = '\0';
		var i = -1;
		var lastC = ' ';
		while ((++i) < buf.length)
		{
			switch (buf[i])
			{
			case '<':
				if (this.quotes != '\0')
					bit = null;
				else
				if (bit != null)
					return tags;
				else
					bit = '';
				break;
			case '>':
				if ((this.quotes == '\0') && (bit != null) && (bit.length > 0))
					tags.push(bit);
				bit = null;
				break;
			case ' ':
			case '\t':
				if ((this.quotes == '\0') && (bit != null) && (bit.length > 0))
					tags.push(bit);
				bit = null;
				break;
			case '"':
			case '\'':
				if (lastC == '\\')
					bit = null;
				else
				if ((this.quotes != '\0') && (this.quotes == buf[i]))
					this.quotes = '\0';
				else
				if (this.quotes == '\0')
					this.quotes = buf[i];
				bit = null;
				break;
			default:
				if ((bit != null) && (isLetterOrDigit(buf[i])))
					bit += buf[i].toUpperCase();
				else
					bit = null;
				break;
			}
			lastC = buf[i];
		}
		return tags;
	};
	
	/**
	 * Does 2 things:
	 * 1. Parses user attributes into tag attributes.
	 * 2. Returns the internal definition complete with variables.
	 */
	this.getFoldedDefinition = function(text)
	{
		var attribsList = this.getParsedAttributes();
		if ("TEXT" in this.attributeValues)
			delete this.attributeValues["TEXT"];
		this.attributeValues["TEXT"] =  text;
		// user parms are just 'parts' of the form VAR="VAL"
		if ((this.userParms != null) && (this.userParms.length > 0))
		{
			var position = -1;
			var attribName = null;
			var userParm = null;
			for (var u = 0; u < this.userParms.length; u++)
			{
				userParm = this.userParms[u].toUpperCase().trim();
				var xx = userParm.indexOf('=');
				if ((xx > 0) 
				&& (userParm.substr(0, xx).trim() in this.alternativeAttributes))
				{
					var newKey = this.alternativeAttributes[userParm.substr(0, xx).trim()];
					var uu = this.userParms[u];
					xx = uu.indexOf('=');
					this.userParms[u] = newKey + uu.substr(xx);
					userParm = this.userParms[u].toUpperCase().trim();
				}
				var found = false;
				if (userParm != null)
				{
					for (var a = 0; a < attribsList.length; a++)
					{
						attribName = attribsList[a];
						if ((userParm.startsWith(attribName + "=")) || (attribName == userParm))
						{
							found = true;
							if (a > position)
								position = a;
							if (attribName != null)
							{
								if (attribName in this.attributeValues)
									delete this.attributeValues[attribName];
								this.attributeValues[attribName] = (userParm == attribName) 
									? "" : this.userParms[u].trim().substr(attribName.length + 1);
							}
							break;
						}
					}
				}
				if ((!found) && (position < (attribsList.length - 1)))
				{
					position++;
					attribName = attribsList[position];
					if (attribName in this.attributeValues)
						delete this.attributeValues[attribName];
					this.attributeValues[attribName] = this.userParms[u].trim();
				}
			}
		}
		return this.definition;
	};
};

var MXP = function(sipwin)
{
	this.defaultMode = MXPMODE.LINE_OPEN; // actually changes!
	this.mode = 0;
	this.eatTextUntilEOLN = false;
	this.eatNextEOLN = false;
	this.eatAllEOLN = false;
	this.openElements = [];
	this.gauges = [];
	this.elements = {
		"B": new MXPElement("B", "<B>", "", "", MXPBIT.HTML),
		"BOLD": new MXPElement("BOLD", "<B>", "", "", MXPBIT.HTML),
		"STRONG": new MXPElement("STRONG", "<B>", "", "", MXPBIT.HTML),
		"U": new MXPElement("U", "<U>", "", "", MXPBIT.HTML),
		"UNDERLINE": new MXPElement("UNDERLINE", "<U>", "", "", MXPBIT.HTML),
		"I": new MXPElement("I", "<I>", "", "", MXPBIT.HTML),
		"ITALIC": new MXPElement("ITALIC", "<I>", "", "", MXPBIT.HTML),
		"S": new MXPElement("S", "<S>", "", "", MXPBIT.HTML),
		"STRIKEOUT": new MXPElement("STRIKEOUT", "<S>", "", "", MXPBIT.HTML),
		"EM": new MXPElement("EM", "<I>", "", "", MXPBIT.HTML),
		"H1": new MXPElement("H1", "<H1>", "", "", MXPBIT.HTML),
		"H2": new MXPElement("H2", "<H2>", "", "", MXPBIT.HTML),
		"H3": new MXPElement("H3", "<H3>", "", "", MXPBIT.HTML),
		"H4": new MXPElement("H4", "<H4>", "", "", MXPBIT.HTML),
		"H5": new MXPElement("H5", "<H5>", "", "", MXPBIT.HTML),
		"H6": new MXPElement("H6", "<H6>", "", "", MXPBIT.HTML),
		"HR": new MXPElement("HR", "<HR>", "", "", MXPBIT.HTML | MXPBIT.COMMAND),
		"SMALL": new MXPElement("SMALL", "<SMALL>", "", "", MXPBIT.HTML),
		"TT": new MXPElement("TT", "<PRE>", "", "", MXPBIT.HTML),
		"BR": new MXPElement("BR", "<BR>", "", "", MXPBIT.HTML | MXPBIT.COMMAND),
		"SBR": new MXPElement("SBR", "&nbsp;", "", "", MXPBIT.HTML | MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"P": new MXPElement("P", "", "", "", MXPBIT.HTML | MXPBIT.SPECIAL), // special
		"C": new MXPElement("C", "<FONT COLOR=&fore; BACK=&back;>", "FORE BACK", "", 0),
		"COLOR": new MXPElement("COLOR", "<FONT COLOR=&fore; BACK=&back;>", "FORE BACK", "", 0),
		"HIGH": new MXPElement("HIGH", "", "", "", MXPBIT.NOTSUPPORTED),
		"H": new MXPElement("H", "", "", "", MXPBIT.NOTSUPPORTED),
		"FONT": new MXPElement("FONT", "<FONT STYLE=\"color: &color;;background-color: &back;;font-family: &face;;font-size: &size;;\">", "FACE SIZE COLOR BACK STYLE", "", MXPBIT.SPECIAL|MXPBIT.HTML),
		"NOBR": new MXPElement("NOBR", "", "", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
		"A": new MXPElement("A", "<A STYLE=\"&lcc;\" ONMOUSEOVER=\"&onmouseover;\" ONCLICK=\"&onclick;\" HREF=\"&href;\" TITLE=\"&hint;\">",
				"HREF HINT EXPIRE TITLE=HINT STYLE ONMOUSEOUT ONMOUSEOVER ONCLICK", "", MXPBIT.HTML, "EXPIRE"),
		"SEND": new MXPElement("SEND", "<A STYLE=\"&lcc;\" HREF=\"&href;\" ONMOUSEOUT=\"delayhidemenu();\" ONCLICK=\"&onclick;\" TITLE=\"&hint;\">", "HREF HINT PROMPT EXPIRE STYLE", "", MXPBIT.SPECIAL,
				"EXPIRE"), // special done
		"EXPIRE": new MXPElement("EXPIRE", "", "NAME", "", MXPBIT.NOTSUPPORTED),
		"VERSION": new MXPElement("VERSION", "", "", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
		"SUPPORT": new MXPElement("SUPPORT", "", "", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
		"GAUGE": new MXPElement("GAUGE", "", "ENTITY MAX CAPTION COLOR", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
		"STAT": new MXPElement("STAT", "", "ENTITY MAX CAPTION", "", MXPBIT.SPECIAL | MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"FRAME": new MXPElement("FRAME", "", "NAME ACTION TITLE INTERNAL ALIGN LEFT TOP WIDTH HEIGHT SCROLLING FLOATING", "", MXPBIT.SPECIAL | MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"DEST": new MXPElement("DEST", "", "NAME", "", MXPBIT.SPECIAL | MXPBIT.NOTSUPPORTED),
		"DESTINATION": new MXPElement("DESTINATION", "", "NAME", "", MXPBIT.SPECIAL | MXPBIT.NOTSUPPORTED),
		"RELOCATE": new MXPElement("RELOCATE", "", "URL PORT", "", MXPBIT.SPECIAL | MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"USER": new MXPElement("USER", "", "", "", MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"PASSWORD": new MXPElement("PASSWORD", "", "", "", MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"IMAGE": new MXPElement("IMAGE", "<IMG SRC=\"&url;&fname;\" HEIGHT=&h; WIDTH=&w; ALIGN=&align;>", "FNAME URL T H W HSPACE VSPACE ALIGN ISMAP", "", MXPBIT.COMMAND, "HSPACE VSPACE ISMAP"),
		"IMG": new MXPElement("IMG", "<IMG SRC=\"&src;\" HEIGHT=&height; WIDTH=&width; ALIGN=&align;>", "SRC HEIGHT=70 WIDTH=70 ALIGN", "", MXPBIT.COMMAND),
		"FILTER": new MXPElement("FILTER", "", "SRC DEST NAME", "", MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"SCRIPT": new MXPElement("SCRIPT", "", "", "", MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
		"ENTITY": new MXPElement("ENTITY", "", "NAME VALUE DESC PRIVATE PUBLISH DELETE ADD", "", MXPBIT.SPECIAL | MXPBIT.COMMAND, "PRIVATE PUBLISH ADD"), // special
		"EN": new MXPElement("EN", "", "NAME VALUE DESC PRIVATE PUBLISH DELETE ADD", "", MXPBIT.SPECIAL | MXPBIT.COMMAND, "PRIVATE PUBLISH ADD"), // special
		"TAG": new MXPElement("TAG", "", "INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE", "", MXPBIT.SPECIAL | MXPBIT.COMMAND, "WINDOWNAME"),
		"VAR": new MXPElement("VAR", "", "NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE", "", MXPBIT.SPECIAL, "PRIVATE PUBLISH ADD REMOVE"), // special
		"V": new MXPElement("V", "", "NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE", "", MXPBIT.SPECIAL, "PRIVATE PUBLISH ADD REMOVE"), // special
		"ELEMENT": new MXPElement("ELEMENT", "", "NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
		"EL": new MXPElement("EL", "", "NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
		"ATTLIST": new MXPElement("ATTLIST", "", "NAME ATT", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
		"AT": new MXPElement("AT", "", "NAME ATT", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
		"SOUND": new MXPElement("SOUND", "", "FNAME V=100 L=1 P=50 T U", "", MXPBIT.COMMAND|MXPBIT.SPECIAL),
		"MUSIC": new MXPElement("MUSIC", "", "FNAME V=100 L=1 P=50 T U", "", MXPBIT.COMMAND|MXPBIT.SPECIAL)
		// -------------------------------------------------------------------------
		
	};
	this.tags = {};
	this.entities = {
		"nbsp": new MXPEntity("nbsp", "&nbsp;"),
		"lt": new MXPEntity("lt", "&lt;"),
		"gt": new MXPEntity("gt", "&gt;"),
		"quot": new MXPEntity("quot", "&quot;"),
		"amp": new MXPEntity("amp", "&amp;")
	};
	
	this.partial = null;
	this.partQuote = '\0';
	this.partialBit ='';
	this.partLastC=' ';
	this.parts = [];
	this.textProcessor = null;

	this.active = function() { return sipwin.MXPsupport };

	this.cancelProcessing = function() {
		var s = this.partial;
		this.partial = null;
		if(s.length==0)
			return '';
		// set resume parsing markers
		if(s[0]=='&')
			return '&amp;\0'+s.substr(1); 
		else
			return '&lt;\0'+s.substr(1);
	};

	this.process = function(c) {
		if(!this.active())
			return null;
		if((typeof c == 'object') && (c.length))
			return this.processAnsiEscape(c);
		if(this.eatTextUntilEOLN)
		{
			this.partial = null;
			if((c=='\n')||(c=='\r'))
				return null;
			return '';
		}
		if(this.partial == null)
		{
			if(c == '<')
			{
				this.startParsing(c);
				return '';
			}
			else
			if(this.textProcessor != null)
			{
				if(this.textProcessor.text.length < 4096)
					this.textProcessor.text += c;
				return '';
			}
			else
			if(c=='&')
			{
				this.startParsing(c);
				return '';
			}
			return null;
		}
		else
		if(this.partial[0]=='&')
		{
			this.partial += c;
			if(this.partial.length > 64)
				return this.cancelProcessing();
			switch(c)
			{
			case '#':
				if(this.partial.length != 2)
					return this.cancelProcessing();
				break;
			case ';':
				{
					if(this.partial.length>2)
					{
						var tag = this.partial.substr(1,this.partial.length-2);
						return this.processEntity(tag,null,true);
					}
					break;
				}
			default:
				if(isDigit(c))
				{
					if((!this.partial.startsWith('&#'))||(this.partial.length==2))
						return this.cancelProcessing();
				}
				else
				if(isLetter(c)||(c=='_'))
				{
					if(this.partial.startsWith('&#'))
						return this.cancelProcessing();
				}
				else
					return this.cancelProcessing();
				break;
			}
			return '';
		}
		else
		{
			this.partial += c;
			if(this.partial.startsWith('<!--'))
			{
				if(this.partial.endsWith('-->')
				&& (this.partial.length > 6)
				&& (this.partial.length < 4096))
					this.cancelProcessing();
				return '';
			}
			if('<!--'.startsWith(this.partial))
				return '';
			if(this.partial.length > 512)
				return this.cancelProcessing();
			if ((this.mode == MXPMODE.LINE_LOCKED) || (this.mode == MXPMODE.LOCK_LOCKED))
				return this.cancelProcessing();
			switch (c)
			{
			case '\n':
			case '\r':
				return this.cancelProcessing();
			case ' ':
			case '\t':
				if (this.partQuote == '\0')
				{
					if (this.partialBit.length > 0)
						this.parts.push(this.partialBit);
					this.partialBit = '';
				}
				else
					this.partialBit += c;
				break;
			case '"':
			case '\'':
				if (this.partLastC == '\\')
				{
					if ((this.partQuote != '\0') || (this.partialBit.length > 0))
						this.partialBit += c;
					else
						return this.cancelProcessing();
				}
				else
				if ((this.partLastC == '=') 
				|| (this.partQuote != '\0') 
				|| ((this.partQuote == '\0') && ((this.partLastC == ' ') || (this.partLastC == '\t'))))
				{
					if ((this.partQuote != '\0') && (this.partQuote == c))
					{
						this.partQuote = '\0';
						this.parts.push(this.partialBit);
						this.partialBit='';
					}
					else
					if (this.partQuote != '\0')
						this.partialBit += c;
					else
						this.partQuote = c;
				}
				else
					this.partialBit += c;
				break;
			case '<':
				if (this.partQuote != '\0')
					this.partialBit += c;
				else
				{
					var s = this.cancelProcessing();
					this.startParsing(c);
					return s;
				}
				break;
			case '>':
				if (this.partQuote != '\0')
					this.partialBit += c;
				else
				{
					if (this.partialBit.length > 0)
						this.parts.push(this.partialBit);
					this.partialBit = '';
					return this.processTag();
				}
				break;
			case '!':
				if ((this.partQuote != '\0')
				|| ((this.parts.length > 0) && (this.parts[0] in this.elements)))
					this.partialBit += c;
				else
				if((this.partialBit.length == 0) && (this.parts.length == 0))
					this.partialBit += c;
				else
					return this.cancelProcessing();
				break;
			case '/':
				if ((this.partQuote != '\0')
				|| ((this.parts.length > 0) && (this.parts[0] in this.elements)))
					this.partialBit += c;
				else
				if((this.partialBit.length == 0) && (this.parts.length == 0))
					this.partialBit += c;
				else
				if(this.partialBit.length >0)
				{
					if(this.parts.length == 0)
						this.partialBit = this.partialBit.toUpperCase().trim();
					this.parts.push(this.partialBit);
					this.parts.push('/');
					this.partialBit='';
				}
				else
					return this.cancelProcessing();
				break;
			default:
				if ((this.partQuote != '\0') 
				|| (isLetter(c)) 
				|| (this.partialBit.length > 0)
				|| ((this.parts.length > 0) && (this.parts[0] in this.elements)))
					this.partialBit += c;
				else
					return this.cancelProcessing();
				break;
			}
			this.partLastC = c;
			return '';
		}
	};

	this.cancelTextProcessing = function()
	{
		if(this.textProcessor != null)
		{
			var nextProcessor = null;
			// go back in the open elements tree to find another text eating candidate
			for(var i=0; i<this.openElements.length;i++)
			{
				var E = this.openElements[i];
				if(E == this.textProcessor)
					break;
				if ((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT) 
					nextProcessor = E;
			}
			this.textProcessor = nextProcessor;
		}
	};

	this.startParsing = function(c)
	{
		this.partial = c;
		this.partQuote = '\0';
		this.parts = [];
		this.partialBit = '';
		this.partLastC = ' ';
	};

	this.processAnsiEscape = function(dat)
	{
		if(dat.length < 2)
			return '';
		if("zZ".indexOf(String.fromCharCode(dat[dat.length-1]))>=0)
		{
			var code=0;
			for(var i=0;i<dat.length-1;i++)
				code = (code * 10) + dat[i] - 48;
			if (code < 0)
				return '';
			else
			if (code < 20)
			{
				this.mode = code;
				return this.executeMode();
			}
			else
			if (code < 100)
			{
				var tag = this.tags[code];
				if ((tag != null) 
				&& ((tag.bitmap & MXPBIT.DISABLED)==0))
				{
					if ((tag.bitmap & MXPBIT.EATTEXT)==MXPBIT.EATTEXT)
						eatTextUntilEOLN = true;
					var definition = tag.getFoldedDefinition("");
					if(definition==null)
						return null;
					if(definition.length == 0)
						return '';
					return '\0' + definition;
				}
			}
		}
		return '';
	};

	this.elementIndexOf = function(tag)
	{
		for (var x = this.openElements.length - 1; x >= 0; x--)
		{
			E = this.openElements[x];
			if (E.name == tag)
				return x;
		}
		return -1;
	}

	this.processTag = function()
	{
		var oldString = this.partial;
		var abort = this.cancelProcessing();
		if(this.parts.length == 0)
			return ''; // this is an error of some sort
		var tag = this.parts[0].toUpperCase().trim();
		this.parts.splice(0,1); // lose the tag name
		var endTag = tag.startsWith("/");
		if (endTag)
			tag = tag.substr(1).trim();
		var selfCloser = false;
		if((this.parts.length>0) && (this.parts[this.parts.length-1] == '/'))
		{
			this.parts.splice(this.parts.length-1,1); // lose the self closer
			selfCloser = true;
		}
		if(tag.startsWith("!"))
			tag = tag.substr(1).trim();
		if ((tag.length == 0) || (!(tag in this.elements)))
			return abort;
		var E = this.elements[tag];
		var text = "";
		if (endTag)
		{
			var foundAt = this.elementIndexOf(tag);
			if (foundAt < 0)
				return oldString; // closed an unopened tag!
			else
			{
				E=this.openElements[foundAt];
				this.openElements.splice(foundAt,1);
			}
			var close = this.getCloseTag(E);
			// anyway...
			if((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT)
				text = E.text;
			if ((E.bitmap & MXPBIT.HTML)==MXPBIT.HTML) 
			{
				if ((E.bitmap & MXPBIT.SPECIAL)==MXPBIT.SPECIAL) 
					this.doSpecialProcessing(E, true);
				return oldString; // this is an end html, so why not just return it?
			}
			if(this.textProcessor == E)
				this.cancelTextProcessing();
		}
		else
		{
			E = E.copyOf();
			E.userParms = this.parts;
			if((!selfCloser)
			//&& ((E.bitmap & MXPBIT.HTML)==0) // this caused problems, I need to know why.
			&& (((E.bitmap & MXPBIT.COMMAND)==0)
			|| ((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT)))
			{
				var foundAt = this.elementIndexOf(tag);
				if (foundAt >= 0)
				{
					var oldE = this.openElements[foundAt];
					if((oldE.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT)
						text += oldE.text;
					this.openElements.splice(foundAt,1);
				}
				E.lastBackground = sipwin.ansi.lastBackground;
				E.lastForeground = sipwin.ansi.lastForeground;
				this.openElements.push(E);
				if ((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT) 
				{
					this.textProcessor = E;
					E.text = '';
					return text == null ? '' : text;
				}
			}
		}
		
		var definition = E.getFoldedDefinition(this.stripBadHTMLTags(text.replaceAll("&nbsp;", " ")));
		if ((endTag || selfCloser) 
		&& ((E.bitmap & MXPBIT.COMMAND)==0)
		&& (E.flag != null)
		&& (E.flag.length > 0))
		{
			var f = E.flag.trim();
			if (f.toUpperCase().startsWith("SET "))
				f = f.substr(4).trim();
			this.modifyEntity(f, text);
		}

		if ((E.bitmap & MXPBIT.SPECIAL)==MXPBIT.SPECIAL) 
			this.doSpecialProcessing(E, endTag || selfCloser);

		definition = this.processAnyEntities(definition, E);
		if (endTag)
		{
			var endHtml = '';
			var close = this.makeCloseTag(definition.trim());
			if((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT)
				endHtml = definition + text + close;
			else
			if(((E.bitmap & MXPBIT.HTML)==MXPBIT.HTML)
			||(definition.length>0))
				endHtml = text + close;
			else
				endHtml = text;
			definition = endHtml;
		}
		if(definition.length > 8192) // safety fallout
			return '';
		if(this.textProcessor != null)
		{
			this.textProcessor.text += definition;
			return '';
		}
		if((definition=='')
		||(oldString.toLowerCase() == definition.toLowerCase())
		||(E.name==this.getFirstTag(definition))) // this line added for FONT exception
			return definition;
		if(E.lastForeground)
			sipwin.ansi.lastForeground = E.lastForeground;
		if(E.lastBackground)
			sipwin.ansi.lastBackground = E.lastBackground;
		return '\0'+definition;
	};
	
	this.getFirstTag = function(s)
	{
		if (!s.startsWith("<"))
			return "";
		var x = s.indexOf('>');
		var y = s.indexOf(' ');
		if((x<0)&&(y<0))
			return '';
		if(y<0)
			return s.substr(1, x-1).toUpperCase();
		else
		if(x<0)
			return s.substr(1, y-1).toUpperCase();
		else
		if(x<y)
			return s.substr(1, x-1).toUpperCase();
		else
			return s.substr(1, y-1).toUpperCase();
	};
	
	this.makeCloseTag = function(s)
	{
		var x = s.indexOf('><');
		var post = '';
		if(x>0)
		{
			post = this.makeCloseTag(s.substr(x+1));
			s=s.substr(0,x+1);
		}
		s = this.getFirstTag(s);
		if(s.length == 0)
			return s;
		return '</' + s + '>' + post;
	};
	
	this.processAnyEntities = function(buf, currentElement)
	{
		var i = -1;
		while (++i < buf.length)
			if(buf[i] == '&')
			{
				if ((this.mode == MXP.MODE_LINE_LOCKED) || (this.mode == MXP.MODE_LOCK_LOCKED))
					continue;
				var convertIt = false;
				var oldI = i;
				var content = '';
				if ((i < buf.length - 3) 
				&& (buf[i + 1] == '#') 
				&& (isDigit(buf[i + 2])))
				{
					i++; // skip to the hash, the next line will skip to the digit
					while ((++i) < buf.length)
					{
						if (buf[i] == ';')
						{
							convertIt = false;
							break;
						}
						else
						if (!isDigit(buf[i]))
						{
							convertIt = true;
							break;
						}
					}
				}
				else
				{
					while ((++i) < buf.length)
					{
						if (buf[i] == ';')
						{
							convertIt = false;
							break;
						}
						else
						if (!isLetterOrDigit(buf[i]))
						{
							convertIt = true;
							break;
						}
						else
						if ((!isLetter(buf[i])) && (content.length == 0))
						{
							convertIt = true;
							break;
						}
						content += buf[i];
						if (content.length > 20)
							break;
					}
				}
				if ((i >= buf.length) && (content.length > 0) && ((buf.length - i) < 10))
					return buf;
				if ((convertIt) || (content.length == 0) || (buf[i] != ';'))
					continue; // dont adjust i, just continue
				var tag = content.trim();
				var val = this.getEntityValue(tag, currentElement);
				var oldValue = buf.substr(oldI, i + 1 - oldI);
				buf = buf.substr(0,oldI) + buf.substr(i+1);
				i = oldI;
				if (val != null)
				{
					if ((currentElement != null) 
					&& (currentElement.name.toUpperCase() == "FONT"))
					{
						if (tag.toUpperCase() == "COLOR")
							sipwin.ansi.lastForeground = val;
						else
						if (tag.toUpperCase() == "BACK")
							sipwin.ansi.lastBackground = val;
					}
					buf = buf.substr(0,oldI) + val + buf.substr(oldI);
					if ((val.toUpperCase()== oldValue.toUpperCase()) 
					|| (currentElement != null))
						i += val.length;
				}
				i +=-1;
			}
		return buf;
	};

	this.stripBadHTMLTags = function(s)
	{
		var buf = s;
		var quotes = [];
		var i = -1;
		var start = -1;
		var bit = null;
		var lastTag = null;
		while ((++i) < buf.length)
		{
			switch (buf[i])
			{
			case '<':
				if (quotes.length > 0)
					break;
				bit = '';
				lastTag = null;
				start = i;
				break;
			case '>':
				if (bit != null)
					lastTag = bit;
				if ((quotes.length == 0) 
				&& (start >= 0) 
				&& (i - start > 0) 
				&& (lastTag != null) 
				&& (lastTag.trim().toUpperCase() == "FONT"))
				{
					var distance = (i - start) + 1;
					buf = buf.substr(0,start) + buf.substr(i+1);
					i = i - distance;
				}
				bit = null;
				lastTag = null;
				start = -1;
				break;
			case ' ':
				if (bit != null)
				{
					lastTag = bit;
					bit = null;
				}
				break;
			case '"':
			case '\'':
				if (start < 0)
					break;
				if ((quotes.length > 0) 
				&& (quotes[quotes.length-1] == buf[i]))
					quotes.splice(quotes.length - 1);
				else
					quotes.push(buf[i]);
				break;
			default:
				if (bit != null)
					bit += buf[i];
				break;
			}
		}
		return buf;
	};

	this.modifyEntity =function(name, value)
	{
		name = name.toLowerCase();
		var X = (name in this.entities) ? this.entities[name] : null;
		if (X == null)
		{
			X = new MXPEntity(name, value);
			this.entities[name] = X;
		}
		else
		{
			if (X.definition.toLowerCase() == value.toLowerCase())
				return;
			X.definition =value;
		}
		var gauge = null;
		for (var g = 0; g < this.gauges.length; g++)
		{
			gauge = this.gauges[g];
			if ((gauge[0].toLowerCase() == name) || (gauge[1].toLowerCase() == name))
			{
				var initEntity = this.getEntityValue(gauge[0], null);
				var initValue = 0;
				if (isNumber(initEntity))
					initValue = Number(initEntity);
				var maxEntity = this.getEntityValue(gauge[1], null);
				var maxValue = 100;
				if (isNumber(maxEntity)>0)
					maxValue = Number(maxEntity);
				if (maxValue < initValue)
					maxValue = (initValue <= 0) ? 100 : initValue;
				if (initValue > 0)
					initValue = Math.round(100.0 * (initValue / maxValue));
				sipwin.modifyGauge(gauge[0],initValue,maxValue);
			}
		}
	};
	
	this.getEntityValue = function(tag, E)
	{
		var val = null;
		if (tag.toLowerCase() == "lcc")
			val = "color: " + sipwin.ansi.lastForeground + "; background-color: " + sipwin.ansi.lastBackground;
		if ((val == null) && (E != null))
			val = E.getAttributeValue(tag);
		if ((val == null) && (E == null))
		{
			for (var x = this.openElements.length - 1; x >= 0; x--)
			{
				E = this.openElements[x];
				val = E.getAttributeValue(tag);
				if (val != null)
					break;
			}
		}
		if (val == null)
		{
			var N = this.entities[tag];
			if (N == null)
				N = this.entities[tag.toLowerCase()];
			if (N == null)
				N = this.entities[tag.toUpperCase()];
			if (N != null)
				val = N.definition;
		}
		return val;
	};
	
	this.processEntity = function(tag, currentE)
	{
		var old = this.partial;
		var abort = this.cancelProcessing();
		if ((this.mode == MXPMODE.LINE_LOCKED) || (this.mode == MXPMODE.LOCK_LOCKED))
			return '';
		var val = this.getEntityValue(tag, currentE);
		if (val != null)
		{
			if ((currentE != null) && (currentE.name.toUpperCase() == "FONT"))
			{
				if (tag.toUpperCase() == "COLOR")
					sipwin.ansi.lastForeground = val;
				else
				if (tag.toUpperCase() == "BACK")
					sipwin.ansi.lastBackground = val;
			}
			return val;
		}
		return old;
	};
	
	this.doSpecialProcessing = function(E, isEndTag)
	{
		var tagName = E.name.toUpperCase();
		if (tagName == "FONT")
		{
			var style = E.getAttributeValue("STYLE");
			var color = E.getAttributeValue("COLOR");
			var back = E.getAttributeValue("BACK");
			var face = E.getAttributeValue("FACE");
			var size = E.getAttributeValue("SIZE");
			if ((style != null) 
			&& (color == null) && (back == null) && (face == null) && (size == null))
			{
				var s = null;
				var v = null;
				while (style.length > 0)
				{
					var x = style.indexOf(';');
					if (x >= 0)
					{
						s = style.substr(0, x).trim();
						style = style.substr(x + 1).trim();
					}
					else
					{
						s = style.trim();
						style = "";
					}
					var y = s.indexOf(':');
					if (y >= 0)
					{
						v = s.substr(y + 1);
						s = s.substr(0, y);
						var ls = s.toLowerCase();
						if (ls == "color")
							E.setAttributeValue("COLOR", v);
						else
						if (ls == "background-color")
							E.setAttributeValue("BACK", v);
						else
						if (ls == "font-size")
							E.setAttributeValue("SIZE", v);
						else
						if (ls == "font-family")
							E.setAttributeValue("FACE", v);
					}
				}
				E.setAttributeValue("STYLE", null);
			}
			
		}
		else
		if (tagName == "NOBR")
			this.eatNextEOLN = true;
		else
		if (tagName == "P")
		{
			if (isEndTag)
			{
				this.eatAllEOLN = false;
				this.eatNextEOLN = false;
			}
			else
			{
				this.eatAllEOLN = true;
				this.eatNextEOLN = true;
			}
		}
		else
		if (tagName == "SEND")
		{
			var prompt = E.getAttributeValue("PROMPT");
			if ((prompt != null) && (prompt.length > 0))
				return;
			if (prompt == null)
				prompt = "true";
			else
				prompt = "false";
			E.setAttributeValue("PROMPT", prompt);
			var href = E.getAttributeValue("HREF");
			var hint = E.getAttributeValue("HINT");
			if ((href == null) || (href.trim().length == 0))
				href = "if(window.alert) alert('Nothing done.');";
			if ((hint == null) || (hint.trim().length == 0))
				hint = "Click here!";
			hint = hint.replaceAll(new RegExp("RIGHT-CLICK","gi"), "click");
			hint = hint.replaceAll(new RegExp("RIGHT-MOUSE","gi"), "click mouse");
			E.setAttributeValue("ONCLICK", "");
			E.setAttributeValue("HREF", "");
			E.setAttributeValue("HINT", "");
			var hrefV = href.split('|').filter(str => str.trim() != '');
			var hintV = hint.split('|').filter(str => str.trim() != '');
			if (hrefV.length == 1)
			{
				href = hrefV[0].replaceAll("'", "\\'");
				E.setAttributeValue("HREF", "javascript:addToPrompt('" + href + "'," + prompt + ")");
				if (hintV.length > 1)
					hint = hintV[0];
				E.setAttributeValue("HINT", hint);
			}
			else
			if (hintV.length > hrefV.length)
			{
				E.setAttributeValue("HINT", hintV[0]);
				hintV.splice(0,1);
				E.setAttributeValue("HREF", "javascript:goDefault(0);");
				var newHint = '';
				for (var i = 0; i < hintV.length; i++)
				{
					newHint += hintV[i];
					if (i < (hintV.length - 1))
						newHint += "|";
				}
				href = href.replaceAll("'", "\\'");
				hint = newHint.replaceAll("'", "\\'");
				var func = "contextmenu(this, event, '"+href+"','"+hint+"',"+prompt+");";
				E.setAttributeValue("ONCLICK", "return "+func);
			}
			else
			{
				E.setAttributeValue("HINT", "Click to open menu");
				E.setAttributeValue("HREF", "javascript:goDefault(0);");
				href = href.replaceAll("'", "\\'");
				hint = newHint.replaceAll("'", "\\'");
				var func = "contextmenu(this, event, '"+href+"','"+hint+"',"+prompt+");";
				E.setAttributeValue("ONCLICK", "return "+func);
			}
		}
		else
		if ((tagName == "ELEMENT") || (tagName == "EL"))
		{
			var name = E.getAttributeValue("NAME");
			var definition = E.getAttributeValue("DEFINITION");
			var attributes = E.getAttributeValue("ATT");
			var tag = E.getAttributeValue("TAG");
			var flags = E.getAttributeValue("FLAG");
			var open = E.getAttributeValue("OPEN");
			var delflag = E.getAttributeValue("DELETE");
			var emptyFlag = E.getAttributeValue("EMPTY");
			if (name == null)
				return;
			if ((delflag != null) && (name in this.elements))
			{
				E = this.elements[name];
				if ((E.bitmap & MXPBIT.OPEN) == MXPBIT.OPEN)
					delete this.elements[name];
				return;
			}
			if (definition == null)
				definition = "";
			if (attributes == null)
				attributes = "";
			var bitmap = 0;
			if (open != null)
				bitmap |= MXPBIT.OPEN;
			if (emptyFlag != null)
				bitmap |= MXPBIT.COMMAND;
			var L = new MXPElement(name.toUpperCase().trim(), definition, attributes, flags, bitmap);
			L.basicElement = false;
			if(L.name in this.elements)
				delete this.elements[L.name];
			this.elements[L.name]= L;
			if (isNumber(tag)
			&& (Number(tag) > 19) && (Number(tag) < 100))
			{
				var tagNum = Number(tag);
				if (tagNum in this.tags)
					delete this.tags[tagNum];
				tags[tagNum] = L;
			}
			return;
		}
		else
		if ((tagName == "ENTITY") || (tagName == "EN"))
		{
			var name = E.getAttributeValue("NAME");
			var value = E.getAttributeValue("VALUE");
			// var desc=E.getAttributeValue("DESC");
			// var PRIVATE=E.getAttributeValue("PRIVATE");
			// var PUBLISH=E.getAttributeValue("PUBLISH");
			var delflag = E.getAttributeValue("DELETE");
			var removeFlag = E.getAttributeValue("REMOVE");
			var addFlag = E.getAttributeValue("ADD");
			if ((name == null) || (name.length == 0))
				return;
			if (delflag != null)
			{
				if(name in this.entities)
					delete this.entities[name];
				return;
			}
			if (removeFlag != null)
			{
				// whatever a string list is (| separated things) this removes
				// it
			}
			else
			if (addFlag != null)
			{
				// whatever a string list is (| separated things) this adds it?
				// it
			}
			else
				this.modifyEntity(name, value);
			return;
		}
		else
		if ((tagName == "SOUND") || (tagName == "MUSIC"))
		{
			var name = E.getAttributeValue("FNAME");
			var url = E.getAttributeValue("U");
			var repeats = E.getAttributeValue("L");
			repeats = (repeats.length > 0) ? Number(repeats) : 0;
			var priority = E.getAttributeValue("P");
			priority = (priority.length > 0) ? Number(priority) : 50;
			var volume = E.getAttributeValue("V");
			volume = (volume.length > 0) ? Number(volume) : 50;
			sipwin.msp.PlaySound(name,url,repeats,volume,priority);
			return;
		}
		else
		if (((tagName== "VAR") || (tagName == "V")) && (isEndTag))
		{
			var name = E.getAttributeValue("NAME");
			// var PRIVATE=E.getAttributeValue("PRIVATE");
			// var PUBLISH=E.getAttributeValue("PUBLISH");
			var delflag = E.getAttributeValue("DELETE");
			var removeFlag = E.getAttributeValue("REMOVE");
			var value = E.getAttributeValue("TEXT");
			if (value == null)
				value = "";
			var addFlag = E.getAttributeValue("ADD");
			if ((name == null) || (name.length == 0))
				return;
			if (delflag != null)
			{
				if(name in this.entities)
					delete this.entities[name];
				return;
			}
			if (removeFlag != null)
			{
				// whatever a string list is (| separated things) this removes
				// it
			}
			else
			if (addFlag != null)
			{
				// whatever a string list is (| separated things) this adds
				// it?
			}
			else
				this.modifyEntity(name, value);
			return;
		}
		else
		if (tagName == "VERSION")
		{
			sipwin.wsocket.send("\033[1z<VERSION MXP=1.0 STYLE=1.0 CLIENT=Siplet VERSION=" 
				+ sipwin.siplet.VERSION_MAJOR + " REGISTERED=NO>\n");
		}
		else
		if (tagName =="GAUGE")
		{
			var gaugeEntity = E.getAttributeValue("ENTITY");
			var max = E.getAttributeValue("MAX");
			if ((gaugeEntity == null) || (max == null))
				return '';
			gaugeEntity = gaugeEntity.toLowerCase();
			max = max.toLowerCase();
			var caption = E.getAttributeValue("CAPTION");
			if (caption == null)
				caption = "";
			var color = E.getAttributeValue("COLOR");
			if (color == null)
				color = "WHITE";
			var initEntity = this.getEntityValue(gaugeEntity, null);
			var initValue = 0;
			if (isNumber(initEntity))
				initValue = Number(initEntity);
			var maxEntity = this.getEntityValue(max, null);
			var maxValue = 100;
			if (isNumber(maxEntity))
				maxValue = Number(maxEntity);
			if (maxValue < initValue)
				maxValue = (initValue <= 0) ? 100 : initValue;
			if (initValue > 0)
				initValue = Math.round(100.0 * (initValue / maxValue));
			sipwin.createGauge(gaugeEntity,caption,color,initValue,maxValue);
			var gauge = [ gaugeEntity, max ];
			this.gauges.push(gauge);
		}
		else
		if ((tagName == "DEST")
		|| (tagName == "DESTINATION"))
		{
			var name = E.getAttributeValue("NAME");
			if(name==null)
				name="";
			//TODO: not yet implemented, because frame is not
			//jscriptBuffer.append("retarget('" + NAME + "');");
			return;
		}
		else
		if ((tagName =="ATTLIST") || (tagName =="ATT"))
		{
			var name = E.getAttributeValue("NAME");
			var value = E.getAttributeValue("ATT");
			if ((name == null) || (value == null))
				return '';
			var E2 = this.elements[name.toUpperCase().trim()];
			if (E2 == null)
				return;
			E2.setAttributes(value);
		}
		else
		if (tagName =="SUPPORT")
		{
			var supportResponse = '';
			var V = E.userParms;
			if ((V == null) || (V.length == 0))
			{
				for (var e in this.elements)
				{
					var E2 = this.elements[e];
					if (!E2.basicElement)
						continue;
					var unsupportedParms = E2.unsupported.split(' ').filter(item => item);
					if ((E2.bitmap & MXPBIT.NOTSUPPORTED) == MXPBIT.NOTSUPPORTED)
						supportResponse += (" -" + E2.name);
					else
					{
						supportResponse += (" +" + E2.name);
						if (unsupportedParms.length > 0)
						{
							for (var x = 0; x < unsupportedParms.length; x++)
								supportResponse += (" -" + E2.name + "." + unsupportedParms[x]);
						}
					}
				}
			}
			else
			{
				for (var v = 0; v < V.length; v++)
				{
					var request = V[v].trim().toUpperCase();
					if (request.startsWith("\""))
						request = request.substr(1).trim();
					if (request.endsWith("\""))
						request = request.substr(0, request.length - 1).trim();
					if (request.startsWith("\'"))
						request = request.substr(1).trim();
					if (request.endsWith("\'"))
						request = request.substr(0, request.length - 1).trim();
					var x = request.indexOf('.');
					var tag = request;
					var parm = "";
					if (x > 0)
					{
						tag = request.substr(0, x).trim();
						parm = request.substr(x + 1).trim();
					}
					var elem = this.elements[tag];
					if ((elem == null) 
					|| ((elem.bitmap & MXPBIT.NOTSUPPORTED) == MXPBIT.NOTSUPPORTED))
					{
						if ((parm.length > 0) && (parm != "*"))
							supportResponse += (" -" + tag + "." + parm);
						else
							supportResponse += (" -" + tag);
						continue;
					}
					if (parm.length == 0)
					{
						supportResponse += (" +" + tag);
						continue;
					}
					var unsupportedParms = elem.unsupported.split(' ').filter(item => item);
					var allAttributes = elem.getParsedAttributes();
					if (parm == "*")
					{
						for (var a = 0; a < allAttributes.length; a++)
						{
							var att = allAttributes[a];
							if (!(att in unsupportedParms))
								supportResponse += (" +" + tag + "." + att);
						}
						continue;
					}
					if ((parm in unsupportedParms) || (!(parm in allAttributes)))
						supportResponse += (" -" + tag + "." + parm);
					else
						supportResponse += (" +" + tag + "." + parm);
				}
			}
			sipwin.wsocket.send("\033[1z<SUPPORTS" + supportResponse + ">\n");
		}
		else
		if (tagName == "TAG")
		{
			this.elements["TAG"] = new MXPElement("TAG", "", "INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE", "", MXPBIT.SPECIAL | MXPBIT.COMMAND);
			// String window=E.getAttributeValue("WINDOWNAME");
			var index = E.getAttributeValue("INDEX");
			if (!isNumber(index))
				return;
			var number = Number(index);
			if ((number < 20) || (number > 99))
				return;
			var foreColor = E.getAttributeValue("FORE");
			if (foreColor == null)
				foreColor = "";
			var backColor = E.getAttributeValue("BACK");
			if (backColor == null)
				backColor = "";
			var gag = E.getAttributeValue("GAG");
			var enable = E.getAttributeValue("ENABLE");
			var disable = E.getAttributeValue("DISABLE");
			var parms = '';
			if ((foreColor.length > 0) || (backColor.length > 0))
			{
				parms += "<FONT ";
				if (foreColor.length > 0)
					parms += " COLOR=" + foreColor;
				if (backColor.length > 0)
					parms += " BACK=" + backColor;
				parms += ">";
			}
			var L = this.tags[number];
			if (L == null)
				return;
			var newBitmap = L.bitmap;
			if (gag != null)
				newBitmap |= MXPBIT.EATTEXT;
			else
			if (disable != null)
				newBitmap |= MXPBIT.DISABLED;
			else
			if (((L.bitmap & MXPBIT.DISABLED) == MXPBIT.DISABLED) && (enable != null))
				newBitmap -= MXPBIT.DISABLED;
			L.bitmap = newBitmap;
			if (parms.length > 0)
			{
				var definition = this.stripBadHTMLTags(L.definition);
				L.definition = definition + parms;
			}
			return;
		}
	};
	
	this.executeMode = function()
	{
		switch (this.mode)
		{
		case MXPMODE.RESET:
			this.defaultMode = MXPMODE.LINE_OPEN;
			this.mode = this.defaultMode;
			return this.closeAllTags();
		case MXPMODE.LOCK_OPEN:
		case MXPMODE.LOCK_SECURE:
		case MXPMODE.LOCK_LOCKED:
			this.defaultMode = this.mode;
			break;
		}
		return '';
	};
	
	this.eolDetected = function()
	{
		if(!this.active())
			return null;
		if ((this.mode == MXPMODE.LINE_LOCKED) || (this.mode == MXPMODE.LOCK_LOCKED))
			return null;
		var eatEOL = this.eatNextEOLN;
		this.eatNextEOLN = this.eatAllEOLN;
		if (this.eatTextUntilEOLN)
		{
			this.eatTextUntilEOLN = false;
			eatEOL = true;
		}
		switch (this.mode)
		{
		case MXPMODE.LINE_OPEN:
		{
			var ret = this.closeAllTags();
			ret += this.executeMode();
			return ret + '<BR>';
		}
		case MXPMODE.LINE_SECURE:
		case MXPMODE.LINE_LOCKED:
		case MXPMODE.TEMP_SECURE:
		{
			var ret = this.closeAllTags();
			ret += this.executeMode();
			return ret;
		}
		}
		if(eatEOL)
			return '';
		return '<BR>';
	};

	this.getCloseTag = function(E)
	{
		var endTags = E.getCloseTags(E.definition);
		var newEnd = '';
		for (var e = endTags.length - 1; e >= 0; e--)
		{
			if (endTags[e].toUpperCase().trim() in this.elements)
				newEnd += "</" + endTags[e].toUpperCase().trim();
		}
		return newEnd;
	};

	// does not close Secure tags -- they are never ever closed
	this.closeAllTags = function()
	{
		var s = '';
		for (var x = this.openElements.length - 1; x >= 0; x--)
		{
			var E = this.openElements[x];
			if ((E.bitmap & MXPBIT.OPEN) == MXPBIT.OPEN)
			{
				var close = this.getCloseTag(E);
				if (close.length > 0)
					s += close + ">";
				this.openElements.splice(x,1);
			}
		}
		if(this.textProcessor != null)
		{
			s = this.textProcessor.text + s;
			this.cancelTextProcessing();
		}
		return s;
	};

};