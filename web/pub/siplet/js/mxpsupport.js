var MXPBIT = 
{
	OPEN: 1,
	COMMAND: 2,
	NEEDTEXT: 4,
	SPECIAL: 8,
	HTML: 16,
	NOTSUPPORTED: 32,
	EATTEXT: 64,
	DISABLED: 128,
	NOOVERRIDE: 256
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
	this.debug = false;
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

	this.sameAs = function(x)
	{
		return this.name == x.name
			&& this.definition == x.definition
			&& this.attributes == x.attributes
			&& this.flag == x.flag
			&& this.bitmap == x.bitmap;
	}
	
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
			if ((VAL != null) && In(VAL, this.parsedAttributes))
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

window.defElements = {
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
	"BLINK": new MXPElement("BLINK", "<FONT STYLE=\"animation: blinker 0.75s steps(2) infinite;\">", "", "", MXPBIT.HTML),
	"TT": new MXPElement("TT", "<PRE>", "", "", MXPBIT.HTML),
	"BR": new MXPElement("BR", "<BR>", "", "", MXPBIT.HTML | MXPBIT.COMMAND),
	"SBR": new MXPElement("SBR", "&nbsp;", "", "", MXPBIT.HTML | MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
	"P": new MXPElement("P", "", "", "", MXPBIT.HTML | MXPBIT.SPECIAL), // special
	"C": new MXPElement("C", "<FONT COLOR=&fore; BACK=&back;>", "FORE BACK", "", 0),
	"COLOR": new MXPElement("COLOR", "<FONT COLOR=&fore; BACK=&back;>", "FORE BACK", "", 0),
	"HIGH": new MXPElement("HIGH", "", "", "", MXPBIT.NOTSUPPORTED),
	"H": new MXPElement("H", "", "", "", MXPBIT.NOTSUPPORTED),
	"FONT": new MXPElement("FONT", "<FONT STYLE=\"color: &color;;background-color: &back;;font-family: &face;;font-size: &size;;\">", 
			"FACE SIZE COLOR BACK STYLE", "", MXPBIT.SPECIAL|MXPBIT.HTML),
	"NOBR": new MXPElement("NOBR", "", "", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
	"A": new MXPElement("A", "<A NAME=\"&name;\" STYLE=\"&lcc;\" ONMOUSEOVER=\"&onmouseover;\" ONCLICK=\"&onclick;\" HREF=\"&href;\" TITLE=\"&hint;\" TARGET=\"&target;\">",
			"HREF HINT NAME TITLE=HINT STYLE TARGET ONMOUSEOUT ONMOUSEOVER ONCLICK", "", MXPBIT.HTML|MXPBIT.SPECIAL, ""),
	"SEND": new MXPElement("SEND", "<A NAME=\"&expire;\" STYLE=\"&lcc;\" HREF=\"&href;\" ONMOUSEOUT=\"ContextDelayHide();\" ONCLICK=\"&onclick;\" TITLE=\"&hint;\">", 
			"HREF HINT PROMPT EXPIRE STYLE", "", MXPBIT.SPECIAL, ""), // special done
	"EXPIRE": new MXPElement("EXPIRE", "", "NAME", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"VERSION": new MXPElement("VERSION", "", "", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
	"SUPPORT": new MXPElement("SUPPORT", "", "", "", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
	"GAUGE": new MXPElement("GAUGE", "", "ENTITY MAX CAPTION COLOR", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"STAT": new MXPElement("STAT", "", "ENTITY MAX CAPTION", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"FRAME": new MXPElement("FRAME", "", "NAME ACTION TITLE INTERNAL ALIGN LEFT TOP WIDTH HEIGHT SCROLLING FLOATING IMAGE IMGOP DOCK", 
			"", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"DEST": new MXPElement("DEST", "", "NAME EOF", "", MXPBIT.SPECIAL),
	"DESTINATION": new MXPElement("DESTINATION", "", "NAME EOF", "", MXPBIT.SPECIAL),
	"RELOCATE": new MXPElement("RELOCATE", "", "URL PORT QUIET", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"USER": new MXPElement("USER", "", "", "", MXPBIT.COMMAND | MXPBIT.SPECIAL),
	"PASSWORD": new MXPElement("PASSWORD", "", "", "", MXPBIT.COMMAND | MXPBIT.SPECIAL),
	"IMAGE": new MXPElement("IMAGE", "<IMG SRC=\"&url;&fname;\" HEIGHT=&h; WIDTH=&w; ALIGN=&align;>", 
			"FNAME URL T H W HSPACE VSPACE ALIGN ISMAP", "", MXPBIT.COMMAND, "HSPACE VSPACE ISMAP"),
	"IMG": new MXPElement("IMG", "<IMG SRC=\"&src;\" HEIGHT=&height; WIDTH=&width; ALIGN=&align; STYLE=&style;>", 
			"SRC HEIGHT=70 WIDTH=70 ALIGN STYLE", "", MXPBIT.COMMAND),
	"FILTER": new MXPElement("FILTER", "", "SRC DEST NAME", "", MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
	"SCRIPT": new MXPElement("SCRIPT", "", "", "", MXPBIT.COMMAND | MXPBIT.NOTSUPPORTED),
	"ENTITY": new MXPElement("ENTITY", "", "NAME VALUE DESC PRIVATE PUBLISH DELETE ADD", 
			"", MXPBIT.SPECIAL | MXPBIT.COMMAND, "PRIVATE PUBLISH ADD"), // special
	"EN": new MXPElement("EN", "", "NAME VALUE DESC PRIVATE PUBLISH DELETE ADD", 
			"", MXPBIT.SPECIAL | MXPBIT.COMMAND, "PRIVATE PUBLISH ADD"), // special
	"TAG": new MXPElement("TAG", "", "INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE", 
			"", MXPBIT.SPECIAL | MXPBIT.COMMAND, "WINDOWNAME"),
	"VAR": new MXPElement("VAR", "", "NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE", 
			"", MXPBIT.SPECIAL, "PRIVATE PUBLISH ADD REMOVE"), // special
	"V": new MXPElement("V", "", "NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE", 
			"", MXPBIT.SPECIAL, "PRIVATE PUBLISH ADD REMOVE"), // special
	"ELEMENT": new MXPElement("ELEMENT", "", "NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY", 
			"", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
	"EL": new MXPElement("EL", "", "NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY", 
			"", MXPBIT.SPECIAL | MXPBIT.COMMAND), // special
	"ATTLIST": new MXPElement("ATTLIST", "", "NAME ATT", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"AT": new MXPElement("AT", "", "NAME ATT", "", MXPBIT.SPECIAL | MXPBIT.COMMAND),
	"SOUND": new MXPElement("SOUND", "", "FNAME V=100 L=1 P=50 T U", "", MXPBIT.COMMAND|MXPBIT.SPECIAL),
	"MUSIC": new MXPElement("MUSIC", "", "FNAME V=100 L=1 P=50 T U", "", MXPBIT.COMMAND|MXPBIT.SPECIAL),
	"INPUT": new MXPElement("INPUT", "<INPUT NAME=&name; &checked; TYPE=&type; value=\"&value;\" onchange=\"SipWin(this).setEntity('&name;',this.value);SipWin(this).dispatchEvent('&name;');\" onclick=\"SipWin(this).dispatchEvent('&name;');\">", 
		"NAME TYPE VALUE CHECKED", "", MXPBIT.COMMAND)
	// -------------------------------------------------------------------------
};

var MXP = function(sipwin)
{
	this.defBitmap = 0;
	this.reset = function()
	{
		this.elements = [];
		for(var k in window.defElements)
			this.elements[k] = window.defElements[k];
		this.entities = GetGlobalEntities();
		if(sipwin.pb && sipwin.pb.entities)
			for(var k in sipwin.pb.entities)
				this.entities[k] = sipwin.pb.entities[k];
		this.dests = [];
		this.defaultMode = MXPMODE.LINE_OPEN; // actually changes!
		this.mode = 0;
		this.eatTextUntilEOLN = false;
		this.eatNextEOLN = false;
		this.eatAllEOLN = false;
		this.openElements = [];
		this.tags = {};
		this.frames = {};
		this.partial = null;
		this.partQuote = '\0';
		this.partialBit ='';
		this.partLastC=' ';
		this.parts = [];
		this.textProcessor = null;
	};
	this.reset();

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
	
	this.flush = function() {
		var s = this.partial;
		this.partial = null;
		return s == null ? '' : s;
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
				if(this.textProcessor.text.length < 8192)
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
				&& (this.partial.length < 8192))
					this.cancelProcessing();
				return '';
			}
			if('<!--'.startsWith(this.partial))
				return '';
			if(this.partial.length > 2048)
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
					s = s.replaceAll('\0',''); // otherwise its endless
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
		if(dat.length < 3)
			return '';
		var type = dat[0];
		if(type != 91) // only working [ right now]
			return "";
		if("zZ".indexOf(String.fromCharCode(dat[dat.length-1]))>=0)
		{
			var code=0;
			for(var i=1;i<dat.length-1;i++)
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
		if(this.debug)
			console.log('mxpp:' + oldString);
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
		{
			if(this.debug)
				console.log('mxpr:' + abort);
			return abort;
		}
		var E = this.elements[tag];
		var text = "";
		if (endTag)
		{
			var foundAt = this.elementIndexOf(tag);
			if (foundAt < 0)
			{
				if(this.debug)
					console.log('mxpr:' + oldString);
				return oldString; // closed an unopened tag!
			}
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
				if(this.debug)
					console.log('mxpr:' + oldString);
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
				E.lastBackground = sipwin.ansi.background;
				E.lastForeground = sipwin.ansi.foreground;
				E.rawText = oldString;
				this.openElements.push(E);
				if ((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT) 
				{
					this.textProcessor = E;
					E.text = '';
					if(this.debug)
						console.log('mxpr:' + text);
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
		if(tag === 'FONT')
		{
			// i hate this
			var x = definition.indexOf(';font-family: ;font-size: ;"');
			if(x>0)
				definition = definition.substr(0,x) + definition.substr(x+27);
		}
		if (endTag)
		{
			var endHtml = '';
			var close = this.makeCloseTag(definition.trim());
			if((E.bitmap & MXPBIT.NEEDTEXT)==MXPBIT.NEEDTEXT)
			{
				if((E.bitmap & MXPBIT.EATTEXT)==MXPBIT.EATTEXT)
					endHtml = '';
				else
					endHtml = definition + text + close;
			}
			else
			if(((E.bitmap & MXPBIT.HTML)==MXPBIT.HTML)
			||(definition.length>0))
				endHtml = text + close;
			else
				endHtml = text;
			definition = endHtml;
		}
		if(this.textProcessor != null)
		{
			if(definition.length > 8192) // safety fallout
			{
				if(this.debug)
					console.log('mxpr:');
				return '';
			}
			this.textProcessor.text += definition;
			return '';
		}
		if((definition=='')
		||(oldString.toLowerCase() == definition.toLowerCase())
		||(E.name==this.getFirstTag(definition))) // this line added for FONT exception
		{
			if(this.debug)
				console.log('mxpr:' + definition);
			return definition;
		}
		sipwin.ansi.setColors(E.lastForeground, E.lastBackground);
		if(this.debug)
			console.log('mxpr:\\0' + definition);
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
		var pre = '';
		if(x>0)
		{
			pre = this.makeCloseTag(s.substr(x+1));
			s=s.substr(0,x+1);
		}
		s = this.getFirstTag(s);
		if(s.length == 0)
			return pre + s;
		return pre + '</' + s + '>';
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
				if(val != null)
				{
					val = val.replaceAll('\n\r',' ')
							.replaceAll('\r\n',' ')
							.replaceAll('\r',' ')
							.replaceAll('\n','');
				}
				var oldValue = buf.substr(oldI, i + 1 - oldI);
				buf = buf.substr(0,oldI) + buf.substr(i+1);
				i = oldI;
				if (val != null)
				{
					if ((currentElement != null) 
					&& (currentElement.name.toUpperCase() == "FONT"))
					{
						if (tag.toUpperCase() == "COLOR")
							sipwin.ansi.setColors(val, null);
						else
						if (tag.toUpperCase() == "BACK")
							sipwin.ansi.setColors(null, val);
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
		if (name in this.entities)
		{
			var X =  this.entities[name];
			if (X.toLowerCase() == value.toLowerCase())
				return;
			this.entities[name] = value;
		}
		else
			this.entities[name] = value;
		var gauge = null;
		for (var g = 0; g < sipwin.gauges.length; g++)
		{
			gauge = sipwin.gauges[g];
			if ((gauge.lentity == name) || (name == gauge.lmaxEntity))
			{
				var initValue = this.getEntityValue(gauge.entity, null);
				if (isNumber(initValue))
					initValue = Number(initValue);
				var maxValue = null;
				if(gauge.maxEntity != null)
				{
					maxValue = this.getEntityValue(gauge.maxEntity, null);
					if (isNumber(maxValue))
						maxValue = Number(maxValue);
					if(gauge.bar)
					{
						if (maxValue < initValue)
							maxValue = (initValue <= 0) ? 100 : initValue;
						if (initValue > 0)
							initValue = Math.round(100.0 * (initValue / maxValue));
					}
				}
				gauge.value = initValue;
				gauge.max = maxValue;
			}
			sipwin.modifyGauges();
		}
	};
	
	this.getEntityValue = function(tag, E)
	{
		var val = null;
		if (tag.toLowerCase() == "lcc")
			val = "color: " + sipwin.ansi.foreground + "; background-color: " + sipwin.ansi.background;
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
				val = N;
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
					sipwin.ansi.setColors(val, null);
				else
				if (tag.toUpperCase() == "BACK")
					sipwin.ansi.setColors(null, val);
			}
			return val;
		}
		return old;
	};

	this.getFrameMap = function()
	{
		var framechoices = Object.assign({}, this.frames);
		if(this.dests.length == 1) 
			framechoices['_previous'] = sipwin.topContainer.firstChild;
		else
		if(this.dests.length > 1) 
			framechoices['_previous'] = this.dests[this.dests.length-2];
		if(sipwin.topContainer)
			framechoices['_top'] = sipwin.topContainer.firstChild;
		return framechoices;
	};

	this.switchFrameTab = function(tab, container) 
	{
		container.sprops.tabs.forEach(function(t) 
		{
			t.content.style.display = 'none';
			t.button.style.backgroundColor = '';
		});
		tab.content.style.display = 'block';
		tab.button.style.backgroundColor = 'lightgray';
		container.sprops.activeTab = tab;
	};
	
	this.closeFrame = function(name)
	{
		var framechoices = this.getFrameMap();
		var aligns = ["LEFT","RIGHT","TOP","BOTTOM"];
		if((name != null) && (name in framechoices))
		{
			var frame = framechoices[name];
			var container = frame;
			var isTabContent = false;
			var tabbedContainer = null;
			var contentArea = null;
			var tabBar = null;
			var sprops = container.sprops;
			if (frame.parentNode 
			&& frame.parentNode.parentNode 
			&& frame.parentNode.parentNode.sprops 
			&& frame.parentNode.parentNode.sprops.tabbed) 
			{
				isTabContent = true;
				tabbedContainer = frame.parentNode.parentNode;
				contentArea = frame.parentNode;
				tabBar = tabbedContainer.children[1];
				sprops = tabbedContainer.sprops;
				if(!sprops) 
					return false; // Not a valid tabbed frame
				
			}
			else
			if(!container.sprops) 
				return; // Not a valid frame
			sipwin.dispatchEvent({type:'closeframe',data: name});
			if(isTabContent) 
			{
				var tabs = sprops.tabs;
				var tabIndex = tabs.findIndex(t => t.content === frame);
				if (tabIndex < 0) 
					return;
				var tab = tabs[tabIndex];
				var wasActive = sprops.activeTab === tab;
				sipwin.cleanDiv(frame);
				contentArea.removeChild(frame);
				tabBar.removeChild(tab.button);
				tabs.splice(tabIndex, 1);
				if(wasActive && (tabs.length > 0))
					this.switchFrameTab(tabs[0], tabbedContainer);
				if(tabs.length === 1) 
				{
					var remaining = tabs[0];
					tabbedContainer.removeChild(tabBar);
					tabbedContainer.removeChild(contentArea);
					var restoredTop = remaining.title ? '20px' : '0px';
					remaining.content.style.position = 'absolute';
					remaining.content.style.top = restoredTop;
					remaining.content.style.left = '0px';
					remaining.content.style.width = '100%';
					remaining.content.style.height = `calc(100% - ${restoredTop})`;
					tabbedContainer.appendChild(remaining.content);
					if (remaining.title) 
					{
						var titleBar = document.createElement('div');
						titleBar.style.position = 'absolute';
						titleBar.style.top = '0px';
						titleBar.style.left = '0px';
						titleBar.style.width = '100%';
						titleBar.style.height = '20px';
						titleBar.style.backgroundColor = 'white';
						titleBar.style.color = 'black';
						titleBar.innerHTML = `&nbsp;${remaining.title}`;
						tabbedContainer.appendChild(titleBar);
					}
					delete sprops.tabbed;
					delete sprops.tabs;
					delete sprops.activeTab;
					delete sprops.tabPos;
					delete sprops.tabDirection;
					delete this.frames[name]; 
					if(remaining.name)
						this.frames[remaining.name] = tabbedContainer;
					if(tabbedContainer.parentNode === sipwin.topWindow)
						sipwin.resizeTermWindow();
					return;
				}
				delete this.frames[name];
				if(tabbedContainer.parentNode === sipwin.topWindow) 
					sipwin.resizeTermWindow();
			}
			else
			if(sprops && sprops.tabbed) 
			{
				// Close entire tabbed container: Clean all tabs
				var tabsCopy = [...sprops.tabs];
				contentArea = frame.children[0];
				tabBar = frame.children[1];
				tabsCopy.forEach(tab => 
				{
					sipwin.cleanDiv(tab.content);
					contentArea.removeChild(tab.content);
					tabBar.removeChild(tab.button);
					if (tab.name) 
						delete this.frames[tab.name];
				});
				frame.removeChild(contentArea);
				frame.removeChild(tabBar);
				delete sprops.tabbed;
				delete sprops.tabs;
				delete sprops.activeTab;
				delete sprops.tabPos;
				delete sprops.tabDirection;
			}
			
			if(sprops.internal != null)
			{
				var parentFrame = frame.parentNode;
				var privilegedFrame = parentFrame.childNodes[0];
				var peerFrames = [];
				for(var i=2;i<parentFrame.childNodes.length;i++)
				{
					var otherFrame = parentFrame.childNodes[i];
					if(otherFrame.sprops)
					{
						if(sprops.align == otherFrame.sprops.align)
							peerFrames.push(otherFrame);
					}
				}
				peerFrames.push(privilegedFrame);
				var peerDex = peerFrames.indexOf(frame);
				var alignx = (sprops.align)?aligns.indexOf(sprops.align.toUpperCase().trim()):-1;
				var shiftw = sprops.width;
				var shifth = sprops.height;
				switch(alignx)
				{
				case 0: // scooch all left
					for(var i=peerDex+1;i<peerFrames.length;i++)
					{
						var tmp = peerFrames[i].style.left; 
						peerFrames[i].style.left = subDim(tmp, shiftw);
					}
					privilegedFrame.style.width = addDim(privilegedFrame.style.width, shiftw);
					break;
				case 1: //right
					for(var i=peerDex+1;i<peerFrames.length-1;i++)
					{
						var tmp = peerFrames[i].style.left; 
						peerFrames[i].style.left = addDim(tmp, shiftw);
					}
					privilegedFrame.style.width = addDim(privilegedFrame.style.width, shiftw);
					break;
				case 2: // top
					for(var i=peerDex+1;i<peerFrames.length;i++)
					{
						var tmp = peerFrames[i].style.top; 
						peerFrames[i].style.top = subDim(tmp, shifth);
					}
					privilegedFrame.style.height = addDim(privilegedFrame.style.height, shifth);
					break;
				case 3: //bottom
					for(var i=peerDex+1;i<peerFrames.length-1;i++)
					{
						var tmp = peerFrames[i].style.top; 
						peerFrames[i].style.top = addDim(tmp, shifth);
					}
					privilegedFrame.style.height = addDim(privilegedFrame.style.height, shifth);
					break;
				}
				for(var k in this.frames)
				{
					if((this.frames[k] != frame)
					&&(frame.contains(this.frames[k])))
						delete this.frames[k];
				}
				sipwin.cleanDiv(frame);
				parentFrame.removeChild(frame);
				delete this.frames[name];
				if(parentFrame.parentNode == sipwin.topWindow)
					sipwin.resizeTermWindow();
			}
			else
			{
				sipwin.cleanDiv(frame);
				delete this.frames[name];
				sipwin.topWindow.removeChild(frame);
			}
			return true;
		}
		return false;
	};
		
	this.setFrameAriaAttributes = function(element)
	{
		element.setAttribute('role', 'log');
		element.setAttribute('aria-live', 'polite');
		element.setAttribute('aria-atomic', 'false');
		element.setAttribute('aria-busy', 'false');
	};

	this.applyFrameScrollingBehavior = function(element, scrolling)
	{
		element.style.overflowX = 'hidden';
		element.style.overflowY = 'hidden';
		if (scrolling && scrolling.toLowerCase() === 'yes') 
		{
			element.style.overflowY = 'auto';
			element.style.overflowX = 'auto';
		} 
		else
		if (scrolling && scrolling.toLowerCase() === 'x')
			element.style.overflowX = 'auto';
		else
		{
			if (scrolling && scrolling.toLowerCase() === 'y')
				element.style.overflowY = 'auto';
			element.style.overflowWrap = 'break-word';
			element.style.wordWrap = 'break-word';
			element.style.whiteSpace = 'pre-wrap';
		}
	};

	this.applyFrameImageBackground = function(element, image, imgop)
	{
		if (!image) 
			return;
		element.style.backgroundImage = 'url("' + image + '")';
		element.style.backgroundSize = 'cover';
		element.style.backgroundPosition = 'center';
		element.style.backgroundRepeat = 'no-repeat';
		if (imgop && imgop.trim()) 
		{
			var opacity = imgop.trim();
			if (!opacity.startsWith('.') && !opacity.startsWith('0.'))
				opacity = parseFloat(opacity) / 100;
			element.style.opacity = opacity;
		}
		updateMediaImagesInSpan(sipwin.sipfs, element);
	}

	this.createFrameTitleBar = function(sprops, name, isFloating)
	{
		var titleBar = document.createElement('div');
		if (sprops.title && sprops.title.trim().length > 0) 
		{
			titleBar.style.cssText = "position:absolute;left:0%;height:20px;width:100%;";
			titleBar.style.backgroundColor = 'white';
			titleBar.style.color = 'black';
			titleBar.innerHTML = '&nbsp;' + sprops.title;
			if (isFloating && sprops.floating && sprops.floating.toLowerCase() === 'close') 
			{
				titleBar.innerHTML += '<IMG alt="Close" style="float: right; width: 16px; height: 16px;" '
					+ 'onkeydown="if(event.key === \'Enter\' || event.key === \' \') { event.preventDefault(); this.click(); }" '
					+ 'ONCLICK="window.currWin.displayText(\'<FRAME NAME=' + name + ' ACTION=CLOSE>\');" '
					+ ' tabindex="0" SRC="images/close.gif">';
			}
			return { titleBar: titleBar, hasTitle: true, titleHeight: '20px' };
		}
		else
		{
			titleBar.style.cssText = "position:absolute;top:0px;left:0px;height:0px;width:0px;";
			return { titleBar: titleBar, hasTitle: false, titleHeight: '0px' };
		}
	}

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
			color = E.getAttributeValue("COLOR");
			back = E.getAttributeValue("BACK");
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
		if (tagName == "EXPIRE")
		{
			var name = E.getAttributeValue("NAME");
			if ((name == null) || (name.trim().length == 0))
			{
				var all = sipwin.topWindow.querySelectorAll('a');;
				for(var i=0;i<all.length;i++)
					all[i].outerHTML = all[i].innerHTML;
			}
			else
			{
				var all = sipwin.topWindow.querySelectorAll('a[name="'+name+'"]');
				for(var i=0;i<all.length;i++)
					all[i].outerHTML = all[i].innerHTML;
			}
		}
		else
		if (tagName == "RELOCATE")
		{
			var host = E.getAttributeValue("URL");
			var port = E.getAttributeValue("PORT");
			if(!host || !port)
				return;
			var quiet = E.getAttributeValue("QUIET");
			if (quiet != null)
				E.bitmap = MXPBIT.EATTEXT|MXPBIT.NEEDTEXT;
			sipwin.closeSocket();
			sipwin.connect('ws://'+host+':'+port);
		}
		else
		if (tagName == "A")
		{
			var href = E.getAttributeValue("HREF");
			if(href)
			{
				href = href.toLowerCase().trim();
				if(href == '#')
				{}
				else
				if(href.startsWith('http://')||href.startsWith('https://'))
				{
					E.setAttributeValue("TARGET","_blank");	
				}
				else
				if(href.startsWith('javascript:')
				&&(isValidJavaScriptLine(href.substr(11).trim())))
				{
					// valid javascript call, do nothing
				}
				else
					E.setAttributeValue('HREF','javascript:window.alert(\'Link disabled for security reasons.\');');
			}
			var omo = E.getAttributeValue("ONMOUSEOVER");
			if(omo)
			{
				omo = omo.toLowerCase().trim();
				if(isValidJavaScriptLine(omo))
				{
					// valid javascript call, do nothing
				}
				else
					E.setAttributeValue('ONMOUSEOVER','');
			}
			var onclk = E.getAttributeValue("ONCLICK");
			if(onclk)
			{
				onclk = onclk.toLowerCase().trim();
				if(isValidJavaScriptLine(onclk))
				{
					// valid javascript call, do nothing
				}
				else
					E.setAttributeValue('ONCLICK','');
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
				href = hrefV[0];
				if(href.startsWith('win.'))
					cmd = "window.currWin." + href.substr(4);
				else
					cmd = "addToPrompt('" + href.replaceAll("'", "\\'") + "'," + prompt + ")";
				E.setAttributeValue("HREF", "javascript:"+cmd);
				if (hintV.length > 1)
					hint = hintV[0];
				E.setAttributeValue("HINT", hint);
			}
			else
			if (hintV.length > hrefV.length)
			{
				E.setAttributeValue("HINT", hintV[0]);
				hintV.splice(0,1);
				E.setAttributeValue("HREF", "#");
				var newHint = '';
				for (var i = 0; i < hintV.length; i++)
				{
					newHint += hintV[i];
					if (i < (hintV.length - 1))
						newHint += "|";
				}
				href = href.replaceAll("'", "\\'");
				hint = newHint.replaceAll("'", "\\'");
				var func = "MXPContextMenu(this, event, '"+href+"','"+hint+"',"+prompt+");";
				E.setAttributeValue("ONCLICK", "return "+func);
			}
			else
			{
				E.setAttributeValue("HINT", "Click to open menu");
				E.setAttributeValue("HREF", "#");
				href = href.replaceAll("'", "\\'");
				hint = newHint.replaceAll("'", "\\'");
				var func = "MXPContextMenu(this, event, '"+href+"','"+hint+"',"+prompt+");";
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
			var uname = name.toUpperCase().trim();
			if((uname in this.elements) && (this.defBitmap == 0))
			{
				if((this.elements[uname].bitmap&MXPBIT.NOOVERRIDE)>0)
					return;
			}
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
			var bitmap = this.defBitmap;
			if (open != null)
				bitmap |= MXPBIT.OPEN;
			if (emptyFlag != null)
				bitmap |= MXPBIT.COMMAND;
			var L = new MXPElement(name.toUpperCase().trim(), definition, attributes, flags, bitmap);
			L.basicElement = false;
			if(L.name in this.elements)
				delete this.elements[L.name];
			this.elements[L.name]= L;
			L.tag = tag;
			if (isNumber(tag)
			&& (Number(tag) > 19) && (Number(tag) < 100))
			{
				var tagNum = Number(tag);
				if (tagNum in this.tags)
					delete this.tags[tagNum];
				this.tags[tagNum] = L;
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
				// it, but since i've no idea what a string list is used for...
			}
			else
			if (addFlag != null)
			{
				// whatever a string list is (| separated things) this adds it?
				// it, but since i've no idea what a string list is used for...
			}
			else
				this.modifyEntity(name, value);
			return;
		}
		else
		if (tagName=="FRAME")
		{
			var aligns = ["LEFT","RIGHT","TOP","BOTTOM"];
			var name = E.getAttributeValue("NAME");
			var action = E.getAttributeValue("ACTION") || ''; // open,close,redirect
			var framechoices = this.getFrameMap();
			if(!name)
			{
				console.error('No frame name.');
				return; // can't open an anonymous frame
			}
			if("CLOSE" == action.toUpperCase())
			{
				this.closeFrame(name);
				return;
			}
			if("REDIRECT" == action.toUpperCase())
			{
				if(name in framechoices)
				{
					var error = new SipSkip('');
					var newFrame = framechoices[name];
					error.call = function() {
						sipwin.flushWindow();
						sipwin.window = newFrame;
					};
					throw error;
				}
				console.error('Cant re-direct to non-existant frame \''+name+'\'.');
				return; // can't re-open a non-existant frame
			}
			
			if(("OPEN" != action.toUpperCase()) && ("MODIFY" != action.toUpperCase()))
			{
				console.error('Unknown action \''+action+'\'.');
				return; // can't do random things
			}
			var modifyFrame = null;
			if(name in framechoices)
			{
				if("OPEN" == action.toUpperCase())
				{
					console.error('Duplicate frame name \''+name+'\'.');
					return; // can't open an existing frame
				}
				modifyFrame = framechoices[name];
			}
			else
			if("MODIFY" == action.toUpperCase())
			{
				console.error('Cant re-open a non-existant frame \''+name+'\'.');
				return; // can't re-open a non-existant frame
			}
			var sprops = {
				"name": name,
				"action": action,
				"title": E.getAttributeValue("TITLE"),
				"dock": E.getAttributeValue("DOCK"),
				"internal": E.getAttributeValue("INTERNAL"),
				"align": E.getAttributeValue("ALIGN"), // internal only: left,right,bottom,top
				"left": fixDivSizeSpec(E.getAttributeValue("LEFT")), // ignored if internal is specified
				"top": fixDivSizeSpec(E.getAttributeValue("TOP")), // ignored if internal is specified 
				"width": fixDivSizeSpec(E.getAttributeValue("WIDTH")) || '50%',
				"height": fixDivSizeSpec(E.getAttributeValue("HEIGHT")) || '50%',
				"scrolling": E.getAttributeValue("SCROLLING"),
				"floating": E.getAttributeValue("FLOATING"), // otherwise, close on click-away
				"image": E.getAttributeValue("IMAGE") || '',
				"imgop": E.getAttributeValue("IMGOP") || ''
			};

			// start the opening process!

			/**
			 * Open tabbed frame:
			 */
			if((sprops.dock != null) && (sprops.dock.trim().length>0))
			{
				var tabPos = 'above';
				var tabFrame = sprops.dock;
				var d = sprops.dock.indexOf(' ');
				if(d > 0) 
				{
					tabPos = sprops.dock.substr(0, d).trim().toLowerCase();
					tabFrame = sprops.dock.substr(d + 1).trim();
				}
				if(!(tabFrame in framechoices))
				{
					console.error('Invalid dock target.');
					return; // Invalid dock target, so nowhere to dock to
				}
				var peerContainer = framechoices[tabFrame];
				if(peerContainer.parentNode 
				&& peerContainer.parentNode.parentNode 
				&& peerContainer.parentNode.parentNode.sprops 
				&& peerContainer.parentNode.parentNode.sprops.tabbed)
					peerContainer = peerContainer.parentNode.parentNode;
				if(!peerContainer.sprops) 
				{
					console.error('Malformed dock target.');
					return; // Invalid sprops
				}
				var isTabbed = !!peerContainer.sprops.tabbed;
				var tabBar, contentArea;
				var tabDirection = (tabPos === 'left' || tabPos === 'right') ? 'vertical' : 'horizontal';
				var tabBarWidth = (tabDirection === 'vertical') ? '150px' : '100%'; // Adjustable for vertical
				var tabBarHeight = (tabDirection === 'vertical') ? '100%' : '20px';
				var tabBarTop = (tabPos === 'below') ? 'calc(100% - 20px)' : '0px';
				var tabBarLeft = (tabPos === 'right') ? 'calc(100% - 150px)' : '0px';
				var contentTop = (tabPos === 'below') ? '0px' : ((tabDirection === 'horizontal') ? '20px' : '0px');
				var contentLeft = (tabPos === 'right') ? '0px' : ((tabDirection === 'vertical') ? '150px' : '0px');
				var contentWidth = (tabDirection === 'vertical') ? 'calc(100% - 150px)' : '100%';
				var contentHeight = (tabPos === 'below') ? 'calc(100% - 20px)' : ((tabDirection === 'horizontal') ? 'calc(100% - 20px)' : '100%');
				var createTabButton = function(label, clickFunc, tabName) 
				{
					var btn = document.createElement('span');
					btn.style.padding = '2px 10px';
					btn.style.border = '1px solid black';
					btn.style.margin = '2px';
					btn.style.cursor = 'pointer';
					btn.style.display = (tabDirection === 'vertical') ? 'block' : 'inline-block';
					btn.innerHTML = label;
					btn.onclick = clickFunc;
					return btn;
				};

				if (!isTabbed) 
				{
					var originalContent = peerContainer.children[0];
					var originalTitleBar = (peerContainer.children.length > 1) ? peerContainer.children[1] : null;
					var titleFromBar = originalTitleBar ? originalTitleBar.innerHTML.replace(/&nbsp;/, '').trim() : '';
					var originalTitle = titleFromBar || peerContainer.sprops.title || peerContainer.sprops.name || 'Main';
					var originalName = peerContainer.sprops.name;

					if (originalTitleBar)
						peerContainer.removeChild(originalTitleBar);

					tabBar = document.createElement('div');
					tabBar.style.position = 'absolute';
					tabBar.style.top = tabBarTop;
					tabBar.style.left = tabBarLeft;
					tabBar.style.width = tabBarWidth;
					tabBar.style.height = tabBarHeight;
					tabBar.style.backgroundColor = 'white';
					tabBar.style.color = 'black';
					tabBar.style.overflow = 'auto';
					if (tabDirection === 'horizontal')
					{
						tabBar.style.overflowX = 'auto';
						tabBar.style.overflowY = 'hidden';
						tabBar.style.whiteSpace = 'nowrap';
					}
					else
						tabBar.style.overflow = 'auto';
					contentArea = document.createElement('div');
					contentArea.style.position = 'absolute';
					contentArea.style.top = contentTop;
					contentArea.style.left = contentLeft;
					contentArea.style.width = contentWidth;
					contentArea.style.height = contentHeight;
					contentArea.style.overflow = 'hidden';
					this.setFrameAriaAttributes(contentArea);

					peerContainer.removeChild(originalContent);
					contentArea.appendChild(originalContent);
					originalContent.style.position = 'absolute';
					originalContent.style.top = '0px';
					originalContent.style.left = '0px';
					originalContent.style.width = '100%';
					originalContent.style.height = '100%';

					peerContainer.appendChild(contentArea);
					peerContainer.appendChild(tabBar);

					var originalTab =
					{
						name: originalName,
						title: originalTitle,
						content: originalContent,
						button: createTabButton(originalTitle, () => this.switchFrameTab(originalTab, peerContainer), originalName)
					};
					tabBar.appendChild(originalTab.button);
					peerContainer.sprops.tabbed = true;
					peerContainer.sprops.tabs = [originalTab];
					peerContainer.sprops.tabPos = tabPos;
					peerContainer.sprops.tabDirection = tabDirection;
					if(originalName)
						this.frames[originalName] = originalContent;
					this.switchFrameTab(originalTab, peerContainer);
				}
				else
				{
					contentArea = peerContainer.children[0];
					tabBar = peerContainer.children[1];
					tabPos = peerContainer.sprops.tabPos;
					tabDirection = peerContainer.sprops.tabDirection;
				}
				var newContent = document.createElement('div');
				newContent.style.position = 'absolute';
				newContent.style.top = '0px';
				newContent.style.left = '0px';
				newContent.style.width = '100%';
				newContent.style.height = '100%';
				newContent.style.backgroundColor = 'black';
				newContent.style.color = 'white';
				this.setFrameAriaAttributes(newContent);
				newContent.style.border = '1px solid white';
				newContent.style.overflowY = (sprops.scrolling && sprops.scrolling.toLowerCase() === 'yes' || sprops.scrolling === 'y') ? 'auto' : 'hidden';
				newContent.style.overflowX = (sprops.scrolling && sprops.scrolling.toLowerCase() === 'yes' || sprops.scrolling === 'x') ? 'auto' : 'hidden';
				if(!sprops.scrolling || sprops.scrolling.toLowerCase() !== 'yes')
				{
					newContent.style.overflowWrap = 'break-word';
					newContent.style.wordWrap = 'break-word';
					newContent.style.whiteSpace = 'pre-wrap';
				}
				this.applyFrameImageBackground(newContent, sprops.image, sprops.imgop);
				contentArea.appendChild(newContent);
				newContent.style.display = 'none';
				var newTab = 
				{
					name: name,
					title: sprops.title || name,
					content: newContent,
					button: createTabButton(sprops.title || name, () => this.switchFrameTab(newTab, peerContainer), name)
				};
				tabBar.appendChild(newTab.button);
				peerContainer.sprops.tabs.push(newTab);
				this.frames[name] = newContent;
				if (action.toUpperCase() === 'REDIRECT') 
					sipwin.window = newContent;
				this.switchFrameTab(newTab, peerContainer);
				if (peerContainer.parentNode === sipwin.topWindow) 
					sipwin.resizeTermWindow();
				if("OPEN" == action.toUpperCase())
					sipwin.dispatchEvent({type:'openframe',data: name});
				return;
			}
			
			/**
			 * Open internal frame:
			 */
			if(sprops.internal != null)
			{
				var alignx = (!sprops.align)?-1:aligns.indexOf(sprops.align.toUpperCase().trim());
				if(alignx < 0)
				{
					console.error('Bad internal alignment \''+sprops.align+'\' dock target.');
					return; // Invalid align
				}
				var siblingDiv = sipwin.window;
				var containerDiv = sipwin.window.parentNode; // has the titlebar in it and window and so forth
				var isInTabbed = (containerDiv.parentNode && containerDiv.parentNode.sprops && containerDiv.parentNode.sprops.tabbed);
				if(isInTabbed) 
				{
					var tabContent = sipwin.window;
					tabContent.style.position = 'relative';
					var wrapper = document.createElement('div');
					wrapper.style.position = 'absolute';
					wrapper.style.top = '0px';
					wrapper.style.left = '0px';
					wrapper.style.width = '100%';
					wrapper.style.height = '100%';
					while (tabContent.firstChild)
						wrapper.appendChild(tabContent.firstChild);
					tabContent.appendChild(wrapper);
					siblingDiv = wrapper;
					sipwin.window = wrapper;
					containerDiv = tabContent;
				}
				var newContainerDiv = document.createElement('div');
				newContainerDiv.style.cssText = containerDiv.style.cssText;
				newContainerDiv.style.display = 'block';
				containerDiv.appendChild(newContainerDiv);
				var newContentWindow = document.createElement('div');
				newContentWindow.style.cssText = sipwin.window.style.cssText;
				newContentWindow.style.display = '';
				newContentWindow.style.left = '0%';
				newContentWindow.style.top = '0%';
				newContentWindow.style.width = '100%';
				newContentWindow.style.height = '100%';
				newContentWindow.style.border = "1px solid white";
				newContentWindow.style.boxSizing = "border-box";
				this.setFrameAriaAttributes(newContentWindow);
				this.applyFrameImageBackground(newContentWindow, sprops.image, sprops.imgop);
				var isFullWidth = sprops.width.endsWith('%') && parseFloat(sprops.width) === 100;
				var isFullHeight = sprops.height.endsWith('%') && parseFloat(sprops.height) === 100;
				if ((alignx === 0 || alignx === 1) && isFullWidth) 
				{
					newContainerDiv.style.left = siblingDiv.style.left;
					newContainerDiv.style.width = siblingDiv.style.width;
					siblingDiv.style.width = '0px';
					newContainerDiv.style.top = siblingDiv.style.top;
					newContainerDiv.style.height = siblingDiv.style.height;
				}
				else
				if ((alignx === 2 || alignx === 3) && isFullHeight) 
				{
					newContainerDiv.style.top = siblingDiv.style.top;
					newContainerDiv.style.height = siblingDiv.style.height;
					siblingDiv.style.height = '0px';
					newContainerDiv.style.left = siblingDiv.style.left;
					newContainerDiv.style.width = siblingDiv.style.width;
				}
				else
				switch(alignx)
				{
				case 0: // left
					if (sprops.width === '100%') 
					{
						var occupied = '0px';
						var priorLeft = containerDiv.querySelector('[sprops][style*="align: left"], [sprops][style*="align: LEFT"]');
						if (priorLeft) 
							occupied = priorLeft.style.width || '0px';
						containerDiv.style.setProperty('--occupied-left', occupied);
						newContainerDiv.style.left = 'var(--occupied-left)';
						newContainerDiv.style.top = siblingDiv.style.top;
						newContainerDiv.style.height = siblingDiv.style.height;
						newContainerDiv.style.width = 'calc(100% - var(--occupied-left))';
						siblingDiv.style.width = 'var(--occupied-left)';
					}
					else 
					{
						newContainerDiv.style.left = siblingDiv.style.left;
						newContainerDiv.style.top = siblingDiv.style.top;
						newContainerDiv.style.height = siblingDiv.style.height;
						newContainerDiv.style.width = sprops.width;
						siblingDiv.style.left = addDim(siblingDiv.style.left, sprops.width);
						siblingDiv.style.width = subDim(siblingDiv.style.width, sprops.width);
					}
					break;
				case 1: // right
					if (sprops.width === '100%') 
					{
						var occupied = '0px';
						var priorRight = containerDiv.querySelector('[sprops][style*="align: right"], [sprops][style*="align: RIGHT"]');
						if (priorRight) 
							occupied = priorRight.style.width || '0px';
						containerDiv.style.setProperty('--occupied-right', occupied);
						newContainerDiv.style.right = 'var(--occupied-right)';
						newContainerDiv.style.top = siblingDiv.style.top;
						newContainerDiv.style.height = siblingDiv.style.height;
						newContainerDiv.style.width = 'calc(100% - var(--occupied-right))';
						siblingDiv.style.width = 'var(--occupied-right)';
					}
					else 
					{
						newContainerDiv.style.left = subDim(addDim(siblingDiv.style.left, siblingDiv.style.width), sprops.width);
						newContainerDiv.style.top = siblingDiv.style.top;
						newContainerDiv.style.height = siblingDiv.style.height;
						newContainerDiv.style.width = sprops.width;
						siblingDiv.style.width = subDim(siblingDiv.style.width, sprops.width);
					}
					break;
				case 2: // top
					if (sprops.height === '100%') 
					{
						var occupied = '0px';
						var priorTop = containerDiv.querySelector('[sprops][style*="align: top"], [sprops][style*="align: TOP"]');
						if (priorTop) 
							occupied = priorTop.style.height || '0px';
						containerDiv.style.setProperty('--occupied-bottom', occupied);
						newContainerDiv.style.bottom = 'var(--occupied-bottom)';
						newContainerDiv.style.left = siblingDiv.style.left;
						newContainerDiv.style.width = siblingDiv.style.width;
						newContainerDiv.style.height = 'calc(100% - var(--occupied-bottom))';
						siblingDiv.style.height = 'var(--occupied-bottom)';
					}
					else 
					{
						newContainerDiv.style.top = siblingDiv.style.top;
						newContainerDiv.style.left = siblingDiv.style.left;
						newContainerDiv.style.width = siblingDiv.style.width;
						newContainerDiv.style.height = sprops.height;
						siblingDiv.style.top = addDim(siblingDiv.style.top, sprops.height);
						siblingDiv.style.height = subDim(siblingDiv.style.height, sprops.height);
					}
					break;
				case 3: // bottom
					if (sprops.height === '100%') 
					{
						var occupied = '0px';
						var priorBottom = containerDiv.querySelector('[sprops][style*="align: bottom"], [sprops][style*="align: BOTTOM"]');
						if (priorBottom) 
							occupied = priorBottom.style.height || '0px';
						containerDiv.style.setProperty('--occupied-top', occupied);
						newContainerDiv.style.top = 'var(--occupied-top)';
						newContainerDiv.style.left = siblingDiv.style.left;
						newContainerDiv.style.width = siblingDiv.style.width;
						newContainerDiv.style.height = 'calc(100% - var(--occupied-top))';
						siblingDiv.style.height = 'var(--occupied-top)';
					}
					else 
					{
						newContainerDiv.style.top = subDim(addDim(siblingDiv.style.top, siblingDiv.style.height), sprops.height);
						newContainerDiv.style.left = siblingDiv.style.left;
						newContainerDiv.style.width = siblingDiv.style.width;
						newContainerDiv.style.height = sprops.height;
						siblingDiv.style.height = subDim(siblingDiv.style.height, sprops.height);
					}
					break;
				}
				newContainerDiv.appendChild(newContentWindow); // dont do until left/width/top/heigh
				var ents = [newContainerDiv,newContentWindow];
				for(var w =0; w<ents.length;w++)
				{
					var ww = ents[w];
					ww.style.overflowX = 'hidden';
					ww.style.overflowY = 'hidden';
				}
				this.applyFrameScrollingBehavior(newContentWindow, sprops.scrolling);
				var titleBarInfo = this.createFrameTitleBar(sprops, name, false);
				var titleBar = titleBarInfo.titleBar;
				if(titleBarInfo.hasTitle)
				{
					titleBar.style.top = newContentWindow.style.top;
					newContentWindow.style.top = titleBarInfo.titleHeight;
					newContentWindow.style.height = 'calc(100% - 20px)';
				}
				newContainerDiv.append(titleBar);
				if(action.toUpperCase() =='REDIRECT')
					sipwin.window = newContentWindow;
				newContainerDiv.sprops = sprops;
				this.frames[name] = newContainerDiv;
				if(containerDiv == sipwin.topWindow.firstChild)
					sipwin.resizeTermWindow();
				if("OPEN" == action.toUpperCase())
					sipwin.dispatchEvent({type:'openframe',data: name});
				return;
			}

			/**
			 * Open floating frame:
			 */
			if((sprops.top != null)
			&&(sprops.left != null)
			&&(sprops.width != null)
			&&(sprops.height != null))
			{
				var newTopWindow = document.createElement('div');
				if(window.sipcounter === undefined) 
					window.sipcounter=1;
				newTopWindow.id = "WIN" + (window.sipcounter++);
				newTopWindow.style.cssText = "position:absolute;top:"+sprops.top+";left:"+sprops.left+";height:"+sprops.height+";width:"+sprops.width+";";
				newTopWindow.style.cssText += "border-style:solid;border-width:5px;border-color:white;";
				newTopWindow.style.backgroundColor = 'darkgray';
				newTopWindow.style.color = 'black';
				var contentTop = '0px';
				var titleBarInfo = this.createFrameTitleBar(sprops, name, true);
				var titleBar = titleBarInfo.titleBar;
				if(titleBarInfo.hasTitle)
				{
					contentTop = titleBarInfo.titleHeight;
					MakeDraggable(newTopWindow, titleBar);
				}
				else
					MakeDraggable(newTopWindow);
				var contentWindow = document.createElement('div');
				contentWindow.style.cssText = "position:absolute;top:"+contentTop+";left:0%;height:calc(100% - "+contentTop+");width:100%;";
				contentWindow.style.backgroundColor = 'black';
				contentWindow.style.color = 'white';
				contentWindow.style.top = contentTop;
				newTopWindow.sprops = sprops;
				contentWindow.style.overflowY = 'hidden';
				contentWindow.style.overflowX = 'hidden';
				this.applyFrameScrollingBehavior(contentWindow, sprops.scrolling);
				if(sprops.floating == null)
				{
					contentWindow.onclick=function() {
						sipwin.topWindow.removeChild(newTopWindow);
						delete this.frames[name];
					}
				}
				newTopWindow.appendChild(contentWindow);
				newTopWindow.appendChild(titleBar);
				if(action.toUpperCase() =='REDIRECT')
					sipwin.window = contentWindow;
				sipwin.topWindow.appendChild(newTopWindow);
				this.frames[name] = newTopWindow;
				if("OPEN" == action.toUpperCase())
					sipwin.dispatchEvent({type:'openframe',data: name});
				return;
			}
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
				// it, but since i've no idea what a string list is used for...
			}
			else
			if (addFlag != null)
			{
				// whatever a string list is (| separated things) this adds
				// it, but since i've no idea what a string list is used for...
			}
			else
				this.modifyEntity(name, value);
			return;
		}
		else
		if (tagName == "VERSION")
		{
			sipwin.wsocket.send("\x1b[1z<VERSION MXP=1.0 STYLE=1.0 CLIENT=Siplet VERSION=" 
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
			sipwin.createGauge(gaugeEntity,max,true,caption,color,initValue,maxValue);
		}
		else
		if (tagName =="STAT")
		{
			var gaugeEntity = E.getAttributeValue("ENTITY");
			var max = E.getAttributeValue("MAX");
			if (gaugeEntity == null)
				return '';
			gaugeEntity = gaugeEntity.toLowerCase();
			if(max != null)
				max = max.toLowerCase();
			var caption = E.getAttributeValue("CAPTION");
			if (caption == null)
				caption = "";
			var initEntity = this.getEntityValue(gaugeEntity, null);
			var initValue = 0;
			if (isNumber(initEntity))
				initValue = Number(initEntity);
			var maxEntityValue = null;
			if(max != null)
			{
				maxEntityValue = this.getEntityValue(max, null);
				if (isNumber(maxEntityValue))
					maxEntityValue = Number(maxEntityValue);
			}
			sipwin.createGauge(gaugeEntity,max,false,caption,color,initValue,maxEntityValue);
		}
		else
		if (tagName == "USER")
		{
			if(sipwin.pb && sipwin.pb.user)
				sipwin.submitInput(sipwin.pb.user);
		}
		else
		if (tagName == "PASSWORD")
		{
			if(sipwin.pb && sipwin.pb.password)
				sipwin.submitHidden(sipwin.pb.password);
		}
		else
		if ((tagName == "DEST")
		|| (tagName == "DESTINATION"))
		{
			var name = E.getAttributeValue("NAME");
			var eof = E.getAttributeValue("EOF");
			var dx = E.getAttributeValue("X");
			var dy = E.getAttributeValue("Y");
			var framechoices = this.getFrameMap();
			if((name != null) && (name in framechoices))
			{
				if(isEndTag)
				{
					var error = new SipSkip('');
					var newFrame = framechoices[name];
					var dests = this.dests;
					error.call = function() 
					{
						//sipwin.htmlBuffer += "<BR>";
						sipwin.flushWindow();
						if(dests.length > 0)
							sipwin.window = dests.pop(); // the text window
						else
							sipwin.window = sipwin.topWindow.firstChild.firstChild;
						setTimeout(function()
						{
							DisplayFakeInput(null);
						},1);
					};
					throw error;
				}
				else
				{
					var frame = framechoices[name];
					if(frame.sprops && frame.firstChild)
						frame = frame.firstChild;
					this.dests.push(sipwin.window);
					var error = new SipSkip('');
					if(eof != null)
					{
						sipwin.cleanDiv(frame);
						frame.innerHTML = '';
					}
					error.call = function() 
					{
						sipwin.flushWindow();
						sipwin.window = frame;
					};
					throw error;
				}
			}
			//TODO: this also supports positioning the cursor.. how the heck is that possible here?
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
							if (!In(att, unsupportedParms))
								supportResponse += (" +" + tag + "." + att);
						}
						continue;
					}
					if (In(parm, unsupportedParms) || (!In(parm, allAttributes)))
						supportResponse += (" -" + tag + "." + parm);
					else
						supportResponse += (" +" + tag + "." + parm);
				}
			}
			sipwin.wsocket.send("\x1b[1z<SUPPORTS" + supportResponse + ">\n");
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