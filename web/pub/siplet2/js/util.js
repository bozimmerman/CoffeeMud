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