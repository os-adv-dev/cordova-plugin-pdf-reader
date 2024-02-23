module.exports = function(ctx) {
    var fs = require('fs'),
        path = require('path'),
        xml = require('cordova-common').xmlHelpers;

    var manifestPath = path.join(ctx.opts.projectRoot, 'platforms/android/app/src/main/AndroidManifest.xml');
    var doc = xml.parseElementtreeSync(manifestPath);
    if (doc.getroot().tag !== 'manifest') {
        throw new Error(manifestPath + ' has incorrect root node name (expected "manifest")');
    }

    //adds the tools namespace to the root node
    //doc.getroot().attrib['xmlns:tools'] = 'http://schemas.android.com/tools';
    //add tools:replace in the application node
    doc.getroot().find('./application').attrib['android:requestLegacyExternalStorage'] = 'true';

    //write the manifest file
    fs.writeFileSync(manifestPath, doc.write({indent: 4}), 'utf-8');

};