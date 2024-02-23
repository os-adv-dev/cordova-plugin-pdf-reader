<h1>cordova-plugin-pdf-reader</h1>
<p>This plugin allows you to open a pdf in a native modal. The model has the following structure: header ( tite name, back button, share button ) and Footer (action buttons). You can define the header title (String), the pdf you want to show (base64 or url), and the number of button in the footer bar ( 0 to 3 buttons ).</p>
<h2>API Reference</h2>
<h3>PdfReader</h3>
<pre>PdfReader.openPdf: function(title,url,buttonsArray,successCallback, errorCallback){
		exec(successCallback, errorCallback, "PDFViewer", "openPdf", [title,url,buttonsArray,subject,description]);
	}


    <strong>title</strong> -&gt; String
    <strong>url</strong> -&gt; base64
    <strong>buttonsArray</strong> -&gt; JsonArray
    	|
        |
        |------&gt; 0 buttons -&gt; buttonsArray = [ ]
        |------&gt; 1 button -&gt; buttonsArray = [{"id":1,"name":"btnName","isDefault":"true"}]
        |------&gt; 2 buttons -&gt; buttonsArray = [{"id":1,"name":"btnName","isDefault":"true"},
        				      {"id":2,"name":"btnName2","isDefault":"false", "needScrollToEnd":"true"}]

        <strong>id</strong> -&gt; int (unique)
        <strong>name</strong> -&gt; String
        <strong>isDefault</strong> -&gt; "true"/"false" ("true" if button backgroung blue | "false" if button backgroung white</pre>
        <strong>needScrollToEnd</strong> -&gt; If button is active only if scroll reaches bottom
    <strong>subject</strong> -&gt; email subject for sharing
    <strong>description</strong> -&gt; Description that appears before buttons
<p><strong>Example:</strong></p>
<pre>var onSuccess = function (res) {

            console.info(res);

        };

        var onError = function (err) {

            console.error(err);

        };

PdfReader.openPdf("Title Name","iVBORw0KGgoAAAANSUhEUgAAACQAAAAkCAYAAADhAJiYAAAAmUlEQVR42u3X3QqAIAwFYKGg1/auF+rnzbo1hQIZImo6j7HBuf9Q2aYyxiikKAEJSEDh6CcQIAd5S/cG+RhXp83UCxTCLL1OiGKOGphSUDNMCYhi9pqYXFBzTA6IYrYWmFQQGyYFRDGXzep15i/JBlFM7RofBHdlkI+atQcN3RhZ5tgvhivk+gG5oMVQsyz56N8g+bkKSECx3F93twfcz7kPAAAAAElFTkSuQmCC",[{"id":1,"name":"btn1","isDefault":"true"},{"id":2,"name":"btn2","isDefault":"false"}],onSuccess, onError,"Email subject", "Description before buttons.");
 </pre>
<p><strong>Expected Callbacks:</strong></p>
<pre>	onSuccess:
    		res = -1 -&gt; When back Button is pressed.
           	res = x -&gt; When Button with id x is pressed.
            res = y -&gt; When Button with id y is pressed.
            res = z -&gt; When Button with id z is pressed.
  </pre>
