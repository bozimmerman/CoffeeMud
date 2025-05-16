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
	if(c == null)
		return false;
	if(typeof c === 'number')
		return true;
	if ((typeof c === 'string')&&(c.length>0))
		return (!isNaN(c)) && (!isNaN(parseFloat(c)));
	return false;
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

function populateDivFromUrl(div, url) 
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
		}
	};
	xhr.onerror = function() { div.innerHTML = 'Failed'; };
	xhr.send();
}