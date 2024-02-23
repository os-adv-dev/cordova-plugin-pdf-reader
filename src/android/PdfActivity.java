package org.apache.cordova.pdfpluginmanager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import org.apache.cordova.PluginResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apache.cordova.pdfpluginmanager.PDFViewer.btnsList;
import static org.apache.cordova.pdfpluginmanager.PDFViewer.callbackContext;

public class PdfActivity extends AppCompatActivity
        implements OnPageChangeListener, OnLoadCompleteListener, ScrollEndListener {

  int pageNumber = 0;

  private Button btn1;

  private Button btn2;

  private Button btn3;

  private TextView warningText;
  private View downloadErrorView;

  private List<Button> ListBtnView = new ArrayList<Button>();

  private TextView headerTitle;

  private String title;
  private String subject;
  private String disabledDescription;

  private PdfUtils pdfUtils;

  private LinearLayout footerBar;

  private int btnId;

  private boolean btnPressed;
  private boolean btnBackPressed;
  private boolean hasReachedEndOfFile = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResourceByName("activity_pdf"));

    pdfUtils = PdfUtils.getInstance(this, PdfUtils.Mode.EXTERNAL);

    String pdf = null, urlString = null;
    File fileObj = null;
    if(getIntent().hasExtra("url")){
      urlString = getIntent().getStringExtra("url");
    }else if(getIntent().hasExtra("file_obj")){
      fileObj = (File)getIntent().getSerializableExtra("file_obj");
    } /*else {
      pdf = getIntent().getStringExtra("file");
    }*/

    title = getIntent().getStringExtra("title");
    subject = getIntent().getStringExtra("subject");
    disabledDescription = getIntent().getStringExtra("disabledDescription");


    warningText = (TextView) findViewById(getIdResourceByName("warning_disabled_btns"));
	downloadErrorView = findViewById(getIdResourceByName("pdf_view_download_error"));
	downloadErrorView.setVisibility(View.GONE);

    btn1 = (Button) findViewById(getIdResourceByName("btn1"));
    btn2 = (Button) findViewById(getIdResourceByName("btn2"));
    btn3 = (Button) findViewById(getIdResourceByName("btn3"));

    footerBar = (LinearLayout) findViewById(getIdResourceByName("footerBar"));

    ListBtnView.add(btn1);
    ListBtnView.add(btn2);
    ListBtnView.add(btn3);

    btnPressed = false;
    btnBackPressed = false;

    if(btnsList.size() == 0){
      footerBar.setVisibility(View.GONE);
    }else {
      if(disabledDescription != null){
        warningText.setText(disabledDescription);
        warningText.setVisibility(View.VISIBLE);
      }

      for (int i = 0; i < btnsList.size(); i++) {
        ListBtnView.get(i).setVisibility(View.VISIBLE);
        ListBtnView.get(i).setText(btnsList.get(i).getName());
        if (!btnsList.get(i).isDefaulf().equals("true")) {
          ListBtnView.get(i).setBackgroundResource(getDrawableResourceByName("btn_unchecked"));
        }else{
          ListBtnView.get(i).setTextColor(Color.WHITE);
        }

        //Every button begins enabled and we mark them false if needScrollToEnd
        ListBtnView.get(i).setEnabled(true);
        if (btnsList.get(i).isDisabledUntilEOF() != null && btnsList.get(i).isDisabledUntilEOF().equalsIgnoreCase("true")) {
          ListBtnView.get(i).setEnabled(false);
        }

        final int finalI = i;
        ListBtnView.get(i).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            btnId = finalI;
            btnBackPressed = false;
            btnPressed = true;
            finish();
          }
        });
      }
    }

    headerTitle = (TextView) findViewById(getIdResourceByName("WebViewHeaderTitle"));
    headerTitle.setText(title);

    if(urlString != null){

      ProgressBar loader = (ProgressBar)findViewById(getIdResourceByName("progressBarLoadingFile"));
      loader.setVisibility(View.GONE);
      loader.setVisibility(View.VISIBLE);



      pdfUtils.downloadAndSaveFile(this, title + ".pdf", urlString, new PdfUtils.IDownloadCallback() {
        @Override
        public void callback(File file) {
          initFor(file);
        }
      });


    }else if (fileObj != null) {
      final File file = fileObj;

      if (file == null)
        callbackContext.error("Error opening Pdf");

      initFor(file);
    }else{
      final File file = pdfUtils.saveFile(this, title + ".pdf", pdf);

      if (file == null)
        callbackContext.error("Error opening Pdf");

      initFor(file);
    }

  }

  @Override public void loadComplete(int nbPages) {
    //callbackContext.success();
  }

  @Override public void onPageChanged(int page, int pageCount) {
    if(hasReachedEndOfFile) return;

    if(page == pageCount-1){
      hasReachedEndOfFile = true;
      for (int i = 0; i < ListBtnView.size(); i++) {
        ListBtnView.get(i).setEnabled(true);
      }
    }
  }

  @Override
  public void onBottomOfScrollReached(){
    if(hasReachedEndOfFile) return;

    hasReachedEndOfFile = true;
    for (int i = 0; i < ListBtnView.size(); i++) {
      ListBtnView.get(i).setEnabled(true);
    }

  }

  @Override
  public void onBackPressed() {
    btnBackPressed = true;
    super.onBackPressed();
  }

  @Override
  protected void onStop() {
    if(btnPressed){ // normal button events
      deleteAllFiles();
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, String.valueOf(btnsList.get(btnId).getId())));
    }else if(btnBackPressed){ //exiting activity with backbutton
      deleteAllFiles();
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, PDFViewer.PLUGIN_RESPONSE_EXIT_BACKBUTTON));
    }/*else {  //activity went into background (unknown reasons for now)
        PluginResult result = new PluginResult(PluginResult.Status.OK, "-2");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }*/
    super.onStop();
  }

  private void deleteAllFiles(){
    try {
      PdfUtils.deleteCachedPdfs();
    } catch (Exception e) {}
  }

  public void setTypefaces(){
    Typeface font1 = Typeface.createFromAsset(
            this.getAssets(),
            "fonts/Barlow-Regular.ttf");
    headerTitle.setTypeface(font1);

    btn1.setTypeface(font1);
    btn2.setTypeface(font1);
    btn3.setTypeface(font1);
  }

  public int getIdResourceByName(String resName) {
    String packageName = getPackageName();
    int resId = getResources().getIdentifier(resName, "id", packageName);
    return resId;
  }

  public int getLayoutResourceByName(String resName) {
    String packageName = getPackageName();
    int resId = getResources().getIdentifier(resName, "layout", packageName);
    return resId;
  }

  public int getDrawableResourceByName(String resName) {
    String packageName = getPackageName();
    int resId = getResources().getIdentifier(resName, "drawable", packageName);
    return resId;
  }




  public void initFor(final File file){
    //hide loading
    ProgressBar loader = (ProgressBar)findViewById(getIdResourceByName("progressBarLoadingFile"));
    loader.setVisibility(View.GONE);

    PDFView pdfView = (PDFView) findViewById(getIdResourceByName("pdfView"));


    LinearLayout header =
            (LinearLayout) findViewById(getIdResourceByName("pdf_layout_header"));

    RelativeLayout headerBackButton = (RelativeLayout) findViewById(getIdResourceByName("faBackButton"));
    Button shareBtn = (Button) findViewById(getIdResourceByName("btn_share"));

    setTypefaces();

    headerBackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        btnBackPressed = true;
        finish();
      }
    });

    shareBtn.setVisibility(View.VISIBLE);
    shareBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_EMAIL, "");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(PdfActivity.this, getApplicationContext().getPackageName() + ".pdfprovider", file));
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        sharingIntent.setType("application/pdf");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
      }
    });

    header.setVisibility(View.VISIBLE);



	
    if (file == null || !file.exists()) {
		downloadErrorView.setVisibility(View.VISIBLE);
    }else{
		downloadErrorView.setVisibility(View.GONE);
   
		pdfView.fromFile(file)
            .defaultPage(pageNumber)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(new MyScrolHandler(this, this)) //DefaultScrollHandle
            .load();
	}

  }

}
