function isLetter(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return ((((c>=97)&&(c<=122))
			||((c>=65)&&(c<=90))));
}

function isLowerCase(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return (c>=97)&&(c<=122);
}

function isUpperCase(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return (c>=65)&&(c<=90);
}

function isLetterOrDigit(c)
{
	return isLetter(c) || isDigit(c);
}

function isDigit(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.charCodeAt(0);
	return (c>=48)&&(c<=90);
}

function isNumber(c)
{
	if((c == null)||(c === undefined))
		return false;
	if(typeof c === 'number')
		return true;
	if ((typeof c === 'string')&&(c.length>0))
		return (!isNaN(c)) && (!isNaN(parseFloat(c)));
	return false;
}

function isJsonObject(variable) {
    return variable !== null 
    	&& typeof variable === 'object' 
    	&& !Array.isArray(variable) 
    	&& Object.prototype.toString.call(variable) === '[object Object]';
}

function stripHtmlTags(htmlString) 
{
	var tempDiv = document.createElement('div');
	tempDiv.innerHTML = htmlString;
	return tempDiv.textContent || tempDiv.innerText || '';
}

function setSelectByValue(select, value) 
{
	for (let i = 0; i < select.options.length; i++) 
	{
		if (select.options[i].value == value) 
		{
			select.selectedIndex = i;
			break;
		}
	}
}

function isValidExpression(exp) {
	try 
	{
		if (exp.trim().length === 0)
			return false;
		var depth = 0;
		var inString = false;
		var stringChar = null;
		var inComment = false;
		for (var i = 0; i < exp.length; i++) 
		{
			if (inComment) {
				inComment =  (exp[i] !== '\n');
				continue;
			}
			if (inString) {
				inString =  ! (exp[i] === stringChar && exp[i - 1] !== '\\');
				continue;
			}
			if (exp[i] === '"' || exp[i] === "'") 
			{
				inString = true;
				stringChar = exp[i];
			} 
			else 
			if (exp[i] === '/' && exp[i + 1] === '/')
				inComment = true;
			else 
			if (exp[i] === '{')
				depth++;
			else 
			if (exp[i] === '}') 
			{
				depth--;
				if (depth < 0)
					return false;
			}
		}
		if (inString)
			return false;
		if (depth !== 0)
			return false;
		if (exp.trim().startsWith('}'))
			return false;
		new Function(exp);
		return true;
	} 
	catch (e) 
	{
		return false;
	}
}

function In(element,inlist)
{
	if(Array.isArray(inlist))
		return inlist.indexOf(element)>=0;
	return element in inlist;
}

function getRadioValue(radio)
{
	for(var i=0;i<radio.length;i++)
		if(radio[i].checked)
			return radio[i].value;
	return '';
}

function setRadioValue(radio, value)
{
	for(var i=0;i<radio.length;i++)
		radio[i].checked = radio[i].value == value;
}

function escapeHTML(s)
{
	return s
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;');
}

function brCount(s)
{
	var matches = s.match(/<br\s*\/?>/gi) || [];
	return matches.length;
}

function butt(t,js)
{
	return "<span style=\"display: inline-block; background-image: url('images/lilButt.gif'); background-position: center;"
		+"background-repeat: no-repeat; background-size: 100% 100%; padding: 5px 10px; white-space: nowrap; cursor: pointer; line-height: 10px;\"" 
		+"onclick=\""+js+"\"><font face=\"Arial\" color=\"purple\" size=\"-2\"><b>"+t+"</b></font></span>";
}

function extractUnclosedFontTags(span, htmlBuffer) {
	if(!htmlBuffer)
		return '';
	var w = '';
	var x = htmlBuffer.lastIndexOf("<FONT");
	var y = htmlBuffer.lastIndexOf("</FONT");
	if(y<x)
		w += htmlBuffer.substr(x,htmlBuffer.indexOf('>',x)-x+1);
	var x1 = htmlBuffer.lastIndexOf("<A ");
	var y1 = htmlBuffer.lastIndexOf("</A>");
	if(y1<x1)
	{
		if(x>=0)
		{
			if(x<x1)
				w += htmlBuffer.substr(x1,htmlBuffer.indexOf('>',x1)-x1+1);
			else
				w = htmlBuffer.substr(x1,htmlBuffer.indexOf('>',x1)-x1+1) + w;
		}
		else
			w = htmlBuffer.substr(x1,htmlBuffer.indexOf('>',x1)-x1+1);
		
	}
	return w;
}

function populateDivFromUrl(div, url, callback) 
{
	var xhr = new XMLHttpRequest();
	xhr.open('GET', url, true);
	xhr.onreadystatechange = function() {
		if((xhr.readyState === 4)&&(xhr.status === 200)) 
		{
			var txt = xhr.responseText; // Populate div
			var x = txt.indexOf('${');
			while(x>=0) {
				var y = x+2;
				var depth = 0;
				while((y<txt.length) && ((txt[y]!='}')||(depth>0)))
				{
					var c = txt[y++];
					if(c=='{')
						depth++;
					else
					if(c=='}')
						depth--;
				}
				if((y>x)&&(y<txt.length))
				{
					var js = txt.substr(x+2,y-(x+2));
					txt = txt.substr(0,x) + eval(js) + txt.substr(y+1);
				}
				x = txt.indexOf('${',x+1);
			}
			div.innerHTML = txt;
			if(callback !== undefined && callback)
				callback();
		}
	};
	xhr.onerror = function() { div.innerHTML = 'Failed'; };
	xhr.send();
}
