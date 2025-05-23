var ANSITABLES = {
	defaultBackground : "black",
	defaultForeground : "white",
	colorCodes1 :
	[ // 30-37
		"black", // black
		"#993300", // red
		"green", // green
		"#999966", // brown
		"#000099", // blue
		"purple", // purple
		"darkcyan", // cyan
		"lightgrey"	// grey
	],
	colorCodes2 :
	[
		"gray", // dark grey
		"red", // light red
		"lightgreen", // light green
		"yellow", // yellow
		"blue", // light blue
		"violet", // light purple
		"cyan", // light cyan
		"white" // white
	],
	html256 :
	[
		"","","","","","","","","","",
		"","","","","","","#000000","#00005f","#000087","#0000af",
		"#0000d7","#0000ff","#005f00","#005f5f","#005f87","#005faf","#005fd7","#005fff","#008700","#00875f",
		"#008787","#0087af","#0087d7","#0087ff","#00af00","#00af5f","#00af87","#00afaf","#00afd7","#00afff",
		"#00d700","#00d75f","#00d787","#00d7af","#00d7d7","#00d7ff","#00ff00","#00ff5f","#00ff87","#00ffaf",
		"#00ffd7","#00ffff","#5f0000","#5f005f","#5f0087","#5f00af","#5f00d7","#5f00ff","#5f5f00","#5f5f5f",
		"#5f5f87","#5f5faf","#5f5fd7","#5f5fff","#5f8700","#5f875f","#5f8787","#5f87af","#5f87d7","#5f87ff",
		"#5faf00","#5faf5f","#5faf87","#5fafaf","#5fafd7","#5fafff","#5fd700","#5fd75f","#5fd787","#5fd7af",
		"#5fd7d7","#5fd7ff","#5fff00","#5fff5f","#5fff87","#5fffaf","#5fffd7","#5fffff","#870000","#87005f",
		"#870087","#8700af","#8700d7","#8700ff","#875f00","#875f5f","#875f87","#875faf","#875fd7","#875fff",
		"#878700","#87875f","#878787","#8787af","#8787d7","#8787ff","#87af00","#87af5f","#87af87","#87afaf",
		"#87afd7","#87afff","#87d700","#87d75f","#87d787","#87d7af","#87d7d7","#87d7ff","#87ff00","#87ff5f",
		"#87ff87","#87ffaf","#87ffd7","#87ffff","#af0000","#af005f","#af0087","#af00af","#af00d7","#af00ff",
		"#af5f00","#af5f5f","#af5f87","#af5faf","#af5fd7","#af5fff","#af8700","#af875f","#af8787","#af87af",
		"#af87d7","#af87ff","#afaf00","#afaf5f","#afaf87","#afafaf","#afafd7","#afafff","#afd700","#afd75f",
		"#afd787","#afd7af","#afd7d7","#afd7ff","#afff00","#afff5f","#afff87","#afffaf","#afffd7","#afffff",
		"#d70000","#d7005f","#d70087","#d700af","#d700d7","#d700ff","#d75f00","#d75f5f","#d75f87","#d75faf",
		"#d75fd7","#d75fff","#d78700","#d7875f","#d78787","#d787af","#d787d7","#d787ff","#d7af00","#d7af5f",
		"#d7af87","#d7afaf","#d7afd7","#d7afff","#d7d700","#d7d75f","#d7d787","#d7d7af","#d7d7d7","#d7d7ff",
		"#d7ff00","#d7ff5f","#d7ff87","#d7ffaf","#d7ffd7","#d7ffff","#ff0000","#ff005f","#ff0087","#ff00af",
		"#ff00d7","#ff00ff","#ff5f00","#ff5f5f","#ff5f87","#ff5faf","#ff5fd7","#ff5fff","#ff8700","#ff875f",
		"#ff8787","#ff87af","#ff87d7","#ff87ff","#ffaf00","#ffaf5f","#ffaf87","#ffafaf","#ffafd7","#ffafff",
		"#ffd700","#ffd75f","#ffd787","#ffd7af","#ffd7d7","#ffd7ff","#ffff00","#ffff5f","#ffff87","#ffffaf",
		"#ffffd7","#ffffff"
	],
	getColorCodeIndex : function(word)
	{
		if (word == null)
			word = ANSITABLES.defaultForeground;
		for (var i = 0; i < ANSITABLES.colorCodes1.length; i++)
		{
			if (word.toLowerCase() == ANSITABLES.colorCodes1[i].toLowerCase())
				return (40 + i);
		}
		for (var i = 0; i < ANSITABLES.colorCodes2.length; i++)
		{
			if (word.toLowerCase() == ANSITABLES.colorCodes2[i].toLowerCase())
				return (30 + i);
		}
		return 30;
	},
	getRelativeColorCodeIndex : function(word)
	{
		var x = ANSITABLES.getColorCodeIndex(word);
		if (x < 40)
			return x - 30;
		if (x > 50)
			return x % 10;
		return x - 40;
	}
};
	
var ANSISTACK = function()
{
	this.reset = function()
	{
		this.blinkOn = false;
		this.fontOn = false;
		this.boldOn = false;
		this.underlineOn = false;
		this.italicsOn = false;
		this.lastBackground = null;
		this.lastForeground = null;
	};
	this.reset();
		
	this.blinkOff = function()
	{
		if (this.blinkOn)
		{
			this.blinkOn = false;
			return "</BLINK>";
		}
		return "";
	};
	
	this.underlineOff = function()
	{
		if (this.underlineOn)
		{
			this.underlineOn = false;
			return "</U>";
		}
		return "";
	};
	
	this.fontOff = function()
	{
		if (this.fontOn)
		{
			this.lastBackground = ANSITABLES.defaultBackground;
			// why does this fix anything?!
			//this.lastForeground = ANSITABLES.defaultForeground;
			this.fontOn = false;
			return "</FONT>";
		}
		return "";
	};
	
	this.italicsOff = function()
	{
		if (this.italicsOn)
		{
			this.italicsOn = false;
			return "</I>";
		}
		return "";
	};
	
	this.styleSheet = function()
	{
		return ('color:'+this.lastForeground+';')
		+(this.italicsOn?'font-style: italic;':'')
		+(this.underlineOn?'text-decoration: underline;':'')
		+(this.blinkOn?'animation: blinker 1s linear infinite;':'')
		+((this.lastBackground)?('background-color:'+this.lastBackground+';'):'');
	}

	this.process = function(dat)
	{
		if(dat.length < 2)
			return "";
		var bits = [""];
		for(var i=0;i<dat.length;i++)
		{
			var c = dat[i];
			if(c == 59) // ';'
				bits.push("");
			else
			if(((c >= 65) && (c <= 90))
			||((c >= 97) && (c <= 122)))
				bits.push(String.fromCharCode(c));
			else
			if((c == 48)&&(bits[bits.length-1].length > 0))
				bits[bits.length-1] += String.fromCharCode(c);
			else
			if((c >= 48) && (c <= 57))
				bits[bits.length-1] += String.fromCharCode(c);
		}
		var html = '';
		if(bits[bits.length-1] == 'm')
		{
			bits.pop(); // removes the 'm'
			var background = null;
			var foreground = null;
			for(var i=0;i<bits.length;i++)
			{
				var bit = bits[i];
				var code = (bit.length==0) ? 0 : Number(bit);
				switch (code)
				{
				case 0:
					if (i == (bits.length - 1))
					{
						html += this.blinkOff();
						html += this.underlineOff();
						html += this.fontOff();
						html += this.italicsOff();
					}
					this.boldOn = false;
					break;
				case 1:
					this.boldOn = true;
					if ((bits.length == 1) && (this.lastForeground != null))
						foreground = ANSITABLES.colorCodes2[ANSITABLES.getRelativeColorCodeIndex(this.lastForeground)];
					break;
				case 4:
				{
					if (!this.underlineOn)
					{
						this.underlineOn = true;
						html += "<U>";
					}
					break;
				}
				case 5:
				{
					if (!this.blinkOn)
					{
						this.blinkOn = true;
						html += "<BLINK>";
					}
					break;
				}
				case 6:
				{
					if (!this.italicsOn)
					{
						this.italicsOn = true;
						html += "<I>";
					}
					break;
				}
				case 7:
				{
					//TODO: this is reverse on, and requires a weird color reversal
					// from whatever the previous colors were.
					// do it later
					break;
				}
				case 8:
				{
					background = ANSITABLES.defaultBackground;
					foreground = ANSITABLES.defaultBackground;
					break;
				}
				case 22:
					html += this.blinkOff();
					html += this.underlineOff();
					html += this.fontOff();
					html += this.italicsOff();
					break;
				case 24:
					html += this.underlineOff();
					break;
				case 25:
					html += this.blinkOff();
					break;
				case 26:
					html += this.italicsOff();
					break;
				case 30:
				case 31:
				case 32:
				case 33:
				case 34:
				case 35:
				case 36:
				case 37:
					foreground = this.boldOn ? 
						ANSITABLES.colorCodes2[code - 30] : 
						ANSITABLES.colorCodes1[code - 30];
					break;
				case 39:
					foreground = ANSITABLES.defaultForeground;
					break;
				case 40:
				case 41:
				case 42:
				case 43:
				case 44:
				case 45:
				case 46:
				case 47:
					background = ANSITABLES.colorCodes1[code - 40];
					break;
				case 49:
					background = ANSITABLES.defaultForeground;
					break;
				case 38: // foreground ansi-256
					if((i<bits.length-2)
					&&(Number(bits[i+1])==5))
					{
						var cd = (bits[i+2].length==0) ? 0 : Number(bits[i+2]);
						if((cd >=0)
						&& (cd < ANSITABLES.html256.length)
						&&(ANSITABLES.html256[cd].length>0))
							foreground = ANSITABLES.html256[cd];
						i=bits.length-1;
					}
					break;
				case 48: // background ansi 256
					if((i<bits.length-2)
					&&(Number(bits[i+1])==5))
					{
						var cd = (bits[i+2].length==0) ? 0 : Number(bits[i+2]);
						if((cd >=0)
						&& (cd < ANSITABLES.html256.length)
						&&(ANSITABLES.html256[cd].length>0))
							background = ANSITABLES.html256[cd];
						i=bits.length-1;
					}
					break;
				}
				if ((background != null) || (foreground != null))
				{
					if (this.lastBackground == null)
						this.lastBackground = ANSITABLES.defaultBackground;
					if (this.lastForeground == null)
						this.lastForeground = ANSITABLES.defaultForeground;
					if (background == null)
						background = this.lastBackground;
					if (foreground == null)
						foreground = this.lastForeground;

					if ((this.lastBackground != background) 
					|| (this.lastForeground != foreground))
					{
						html += this.fontOff();
						this.lastBackground = background;
						this.lastForeground = foreground;
						this.fontOn = true;
						html += '<FONT STYLE="' + this.styleSheet() + '">'; 
					}
				}
			}
		}
		return html;
	}
}
