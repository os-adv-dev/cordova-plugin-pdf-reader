package org.apache.cordova.pdfpluginmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by sergiocagica on 19/09/16.
 */

public class PdfUtils{

  /** The Constant MIME_TYPE_PDF. */
  private static final String MIME_TYPE_PDF = "application/pdf";

  /** The Constant TAG. */
  private static final String TAG = "PDFControl";

  /** The cache dir. */
  public static File cacheDir;

  private static boolean existsPdf = false;

  private static PdfUtils INSTANCE;

  public enum Mode {
    INTERNAL, EXTERNAL
  }

  private static Mode mMode;

  public static PdfUtils getInstance(Context context, Mode mode) {
    if (INSTANCE == null) {
      INSTANCE = new PdfUtils(context, mode);
    }
    return INSTANCE;
  }

  public PdfUtils(Context context, Mode mode) {
    mMode = mode;
    initFolders(context, mode);
  }

  /**
   * Show pdf.
   *
   * @param fileName the file name
   * @param byteArrayString the byte array string
   */
  public void showPDF(Context mContext, String fileName, String byteArrayString) {

    File file = saveFile(mContext, fileName, byteArrayString);

    boolean canDisplayPdf = canDisplayPdf(mContext);
    switch (mMode) {
      case EXTERNAL:
        if (canDisplayPdf && file != null) {
          openPDF(mContext, file);
        } else {
          Toast.makeText(mContext,
              "Não existe nenhuma aplicação no dispositivo que permita a leitura de PDFs.",
              Toast.LENGTH_LONG).show();
        }
        break;

      case INTERNAL:
        openPDF(mContext, fileName);
        break;

      default:
        break;
    }
  }

  /**
   * Save file.
   *
   * @param fileName the file name
   * @param byteArrayString the byte array string
   * @return the file
   */
  public File saveFile(Context mContext, String fileName, String byteArrayString) {
    File file = null;
    byte[] byteArray = convert(byteArrayString);
    if (byteArray == null) return null;
    switch (mMode) {
      case EXTERNAL:
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
          Toast.makeText(mContext, "Cartão SD não encontrado.", Toast.LENGTH_SHORT).show();
        } else {
          try {
            file = new File(cacheDir, fileName);
            OutputStream out = new FileOutputStream(file);
            out.write(byteArray);
            out.close();
            existsPdf = true;
          } catch (IOException e) {
            Log.e("PDF", "Falhou o processo.");
          }
        }
        break;

      case INTERNAL:
        try {
          FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
          fos.write(byteArray);
          fos.close();
          file = returnFile(fileName);
          existsPdf = true;
        } catch (FileNotFoundException e) {
          Log.e("PDF", "Não foi encontrado o ficheiro " + fileName);
        } catch (IOException e) {
          Log.e("PDF", "IOException", e);
        }
        break;

      default:
        break;
    }
    return file;
  }



  /**
   * Download and Save file.
   *
   */
  public void downloadAndSaveFile(Context mContext, String fileName, String url, IDownloadCallback callback) {
    new DownloadTask(mContext, fileName, callback).execute(url);
  }



  /**
   * Convert String to byte array.
   *
   * @param bytesArrayInAString the bytes array in a string
   * @return the byte[]
   */
  @SuppressLint("NewApi") public static byte[] convert(String bytesArrayInAString) {
    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    byte[] byteArray = null;

    if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO) {
      /** Using android.util.Base64 (since API 8). */
      try {
        byteArray = Base64.decode(bytesArrayInAString, Base64.DEFAULT);
      } catch (IllegalArgumentException e) {
        return null;
      }
    }

    return byteArray;
  }

  /**
   * Delete all the pdf files into TEMP directory.
   */
  public static void deleteCachedPdfs() {
    if (!existsPdf) return;
    String[] results = cacheDir.list();
    int counter = 0;
    for (String fileName : results) {
      try {
        File cacheFile = new File(cacheDir, fileName);
        if (cacheFile.exists()) {
          cacheFile.delete();
          counter++;
        }
      } catch (Exception ex) {
        Log.e(TAG, "Exception deleting file " + fileName + ": " + ex.getMessage());
      }
    }

    Log.i(TAG, "Deleted " + counter + " files from cache diretory.");
  }

  /**
   * Check file existence.
   *
   * @param fileName the file name
   * @return true, if successful
   */
  public static boolean checkFileExistence(String fileName) {
    String[] results = cacheDir.list();
    for (String file : results) {
      try {
        File cacheFile = new File(cacheDir, file);
        if (cacheFile.exists() && file.equals(fileName)) {
          return true;
        }
      } catch (Exception ex) {
        Log.e(TAG, "Exception on file info" + file + ": " + ex.getMessage());
      }
    }

    return false;
  }

  /**
   * Return file.
   *
   * @param fileName the file name
   * @return the file
   */
  public static File returnFile(String fileName) {
    File[] listFiles = cacheDir.listFiles();
    for (File file : listFiles) {
      if (file.exists() && file.getName().equals(fileName)) {
        return file;
      }
    }

    return null;
  }

  /**
   * Inits the cache folder.
   *
   * @param mode the mode
   */
  private void initFolders(Context mContext, Mode mode) {
    /** Init physical storage. */

    File[] sandboxStorage = mContext.getExternalFilesDirs("PDF/temp/");
    if(sandboxStorage.length != 0 ) {
      cacheDir = sandboxStorage[0];
    }else{
        //fail safe, just in case
        cacheDir = new File(Environment.getExternalStorageDirectory(), "PDF/temp/");
    }


    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
      Log.i(TAG, "Non existent dirs, now created.");
    }
  }

  /**
   * Check if the supplied context can render PDF files via some installed application that reacts
   * to a intent with
   * the pdf mime type and viewing action.
   *
   * @param context the context
   * @return true, if successful
   */
  private boolean canDisplayPdf(Context context) {
    PackageManager packageManager = context.getPackageManager();
    Intent testIntent = new Intent(Intent.ACTION_VIEW);
    testIntent.setType(MIME_TYPE_PDF);
    if (packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY).size()
        > 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Opens pdf Intent to read file..
   *
   * @param file the file
   */
  private boolean openPDF(Context mContext, File file) {
    return callIntent(mContext, file);
  }

  /**
   * Open pdf.
   *
   * @param fileName the file name
   */
  public boolean openPDF(Context mContext, String fileName) {
    File file = new File(cacheDir, fileName);
    return callIntent(mContext, file);
  }

  /**
   * Call intent.
   *
   * @param file the file
   */
  private boolean callIntent(Context mContext, File file) {
    PackageManager packageManager = mContext.getPackageManager();
    Intent testIntent = new Intent(Intent.ACTION_VIEW);
    testIntent.setType(MIME_TYPE_PDF);
    List<ResolveInfo> list =
        packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
    if (list.size() > 0 && file.isFile()) {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_VIEW);
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, MIME_TYPE_PDF);

      mContext.startActivity(intent);
      return true;
    } else {
      return false;
    }
  }






  public interface IDownloadCallback{
    void callback(File file);
  }

  private class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private String fileName;
    private File file = null;
    private IDownloadCallback callback;

    public DownloadTask(Context context, String fileName, IDownloadCallback callback) {
      this.context = context;
      this.fileName = fileName;
      this.callback = callback;
    }

    @Override
    protected String doInBackground(String... sUrl) {
      InputStream input;
      HttpURLConnection connection = null;
      try {
        URL url = new URL(sUrl[0]);
        connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        // expect HTTP 200 OK, so we don't mistakenly save error report
        // instead of the file
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
          return "Server returned HTTP " + connection.getResponseCode()
                  + " " + connection.getResponseMessage();
        }

        // this will be useful to display download percentage
        // might be -1: server did not report the length
        int fileLength = connection.getContentLength();

        // download the file
        input = connection.getInputStream();

        switch (mMode) {
          case EXTERNAL:
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
              Toast.makeText(context, "Cartão SD não encontrado.", Toast.LENGTH_SHORT).show();
            } else {
              try {
                file = new File(cacheDir, fileName);
                OutputStream out = new FileOutputStream(file);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                  // allow canceling with back button
                  if (isCancelled()) {
                    input.close();
                    return null;
                  }
                  total += count;
                  // publishing the progress....
                  if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                  out.write(data, 0, count);
                }
                existsPdf = true;
              } catch (IOException e) {
                Log.e("PDF", "Falhou o processo.");
              }
            }
            break;

          case INTERNAL:
            try {
              FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
              byte data[] = new byte[4096];
              long total = 0;
              int count;
              while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                  input.close();
                  return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                  publishProgress((int) (total * 100 / fileLength));
                fos.write(data, 0, count);
              }
              file = returnFile(fileName);
              existsPdf = true;
            } catch (FileNotFoundException e) {
              Log.e("PDF", "Não foi encontrado o ficheiro " + fileName);
            } catch (IOException e) {
              Log.e("PDF", "IOException", e);
            }
            break;

          default:
            break;
        }

      } catch (Exception e) {
        return e.toString();
      } finally {

        if (connection != null)
          connection.disconnect();
      }
      return null;
    }


    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);

      callback.callback(file);
    }
  }



}
