package org.apache.cordova.pdfpluginmanager;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Button;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PDFViewer extends CordovaPlugin {
  
  private int READ_EXTERNAL = 0;

  public static final String PLUGIN_RESPONSE_EXIT_BACKBUTTON = "-1";
  public static final String PLUGIN_RESPONSE_PERMISSIONS_DENIED = "-3";
  public static final String PLUGIN_RESPONSE_PERMISSIONS_GRANTED = "-4";

  public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;
  public static final String WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

  public static CallbackContext callbackContext;

  public static PluginResult result;

  public static List<BtnObject> btnsList = new ArrayList<BtnObject>();

  @Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
          throws JSONException {
    PDFViewer.callbackContext = callbackContext;
    String url = "";
    String title = "";
    String subject = null;
    String disabledDescription = null;
    JSONArray btnsArray = null;

    if ("openPdf".equals(action)) {
      try {
        title = args.getString(0);
        url = args.getString(1);
        btnsArray = args.getJSONArray(2);
        if(args.length() > 3){
          subject = args.getString(3);
          if(subject != null && subject.equals("null")){
            subject = null;
          }

          disabledDescription = args.getString(4);
          if(disabledDescription != null && disabledDescription.equals("null")){
            disabledDescription = null;
          }
        }
        btnsList.clear();

        if(url.equals("") || url==null || title.equals("") || title==null || btnsArray.length()>3){
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
          return true;
        }

        for (int j = 0; j < btnsArray.length(); j++) {
          JSONObject jsonobject = btnsArray.getJSONObject(j);
          int id = jsonobject.getInt("id");
          String name = jsonobject.getString("name");
          String isDefault = jsonobject.getString("isDefault");
          String isDisabledUntilEOF = "false";
          if(jsonobject.has("needScrollToEnd")) {
            isDisabledUntilEOF = jsonobject.getString("needScrollToEnd");
          }

          BtnObject button = new BtnObject(id,name,isDefault,isDisabledUntilEOF);
          btnsList.add(button);
        }

      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
      }
      try {
        this.openPdf(url, title, subject, disabledDescription);
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Failed to open pdf"));
      }
      return true;
    }
    if ("closePdf".equals(action)) {
      try {
        PdfUtils.deleteCachedPdfs();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
      } catch (ActivityNotFoundException e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
      }
      return true;
    }
    return false;  // Returning false results in a "MethodNotFound" error.
  }

  private void openPdf(String url, String title, String subject, String disabledDescription) {
    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && (!PermissionHelper.hasPermission(this, READ) || !PermissionHelper.hasPermission(this, WRITE))) {
      PermissionHelper.requestPermissions(this, READ_EXTERNAL, new String[] {READ, WRITE});
    } else {
      Intent intent = new Intent(cordova.getActivity(), PdfActivity.class);

      if(URLUtil.isValidUrl(url)){
        intent.putExtra("url", url);
      }else{
        PdfUtils pdfUtils = PdfUtils.getInstance(this.cordova.getActivity(), PdfUtils.Mode.EXTERNAL);
        final File file = pdfUtils.saveFile(this.cordova.getActivity(), title + ".pdf", url);

        intent.putExtra("file_obj", file);

      }

      intent.putExtra("title", title);
      intent.putExtra("subject", (subject != null)? subject: title);
      intent.putExtra("disabledDescription", disabledDescription);
      cordova.getActivity().startActivity(intent);
    }
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] requestResults){
    for (int index = 0; index < permissions.length; index++) {
      if(requestResults[index] != PackageManager.PERMISSION_GRANTED){
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PLUGIN_RESPONSE_PERMISSIONS_DENIED));
        return;
      }
    }
    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, PLUGIN_RESPONSE_PERMISSIONS_GRANTED));
  }
}