var argscheck = require('cordova/argscheck'),
               exec = require('cordova/exec');

var pdfreader = {

    openPdf: function(title,url,buttonsArray,successCallback, errorCallback, subject, description){
        exec(successCallback, errorCallback, "PDFViewer", "openPdf", [title,url,buttonsArray, subject, description]);
    },

	closePdf: function(successCallback, errorCallback) {
		exec(successCallback, errorCallback, "PDFViewer", "closePdf", []);
	}
};

module.exports = pdfreader;

