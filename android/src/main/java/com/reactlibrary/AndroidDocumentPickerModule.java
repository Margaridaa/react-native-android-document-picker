// AndroidDocumentPickerModule.java

package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.content.Intent;

import static android.app.Activity.RESULT_OK;
import static android.app.Activity.RESULT_CANCELED;

import org.json.JSONException;
import org.json.JSONObject;

public class AndroidDocumentPickerModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;
    private ArrayList<Uri> selectedUris = new ArrayList<>();
    private int selectedNumberOfFiles = 20;

    public AndroidDocumentPickerModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        context.addActivityEventListener(new ActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode == 9900 && resultCode == RESULT_OK) {
                    if (null != data) { // checking empty selection
                        if (null != data.getClipData()) { // checking multiple selection or not
                            selectedNumberOfFiles = data.getClipData().getItemCount();
                            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                Uri d = data.getClipData().getItemAt(i).getUri();
                                selectedUris.add(d);
                            }
                        } else {
                            Uri d = data.getData();
                            selectedNumberOfFiles = 1;
                            selectedUris.add(d);
                        }
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    selectedNumberOfFiles = -1;
                }
            }

            @Override
            public void onNewIntent(Intent intent) {

            }
        });
    }

    @Override
    public String getName() {
        return "AndroidDocumentPicker";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @ReactMethod(isBlockingSynchronousMethod = true)
    public void openDocument(ReadableMap options, Callback successCallback, Callback failureCallback) {
        ReadableArray fileTypes = null;
        Boolean multipleFiles = null;
        try {
            fileTypes = options.getArray("fileTypes");
        } catch (Exception e) {
        }
        try {
            multipleFiles = options.getBoolean("multipleFiles");
        } catch (Exception e) {
        }

        ArrayList<String> arrFileTypes = toArrayList(fileTypes);
        String[] mimeTypes = arrFileTypes.toArray(new String[0]);

        // Creating the intent
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("*/*");

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multipleFiles);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

        // starting activity for ACTION_OPEN_DOCUMENT
        this.getReactApplicationContext().startActivityForResult(Intent.createChooser(intent, "Choose"), 9900, null);

        // wait for selectedUris
        while (selectedUris.size() < selectedNumberOfFiles) { }

        if (selectedUris.size() > 0) {
            WritableArray writableArray = convertToWritableArray(returnFileInfoObject());
            selectedNumberOfFiles = 20;
            successCallback.invoke(writableArray);
        }
        else {
            selectedNumberOfFiles = 20;
            failureCallback.invoke("Error");
        }
    }

    public ArrayList<Object> returnFileInfoObject() {
        String fileName = "";
        String fileSize = "";
        String fileType = "";
        ArrayList<Object> fileObjects = new ArrayList<Object>();

        if (selectedUris.size() > 0) {
            for (Uri selectedUri : selectedUris) {
                JSONObject jsonObject = new JSONObject();
                if (selectedUri.getScheme().equals("content")) {
                    try {
                        Cursor cursor = reactContext.getContentResolver().query(selectedUri, null, null, null, null);

                        if (cursor.moveToFirst()) {
                            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                            fileType = getMimeType(fileName);

                            jsonObject.put("fileName", fileName);
                            jsonObject.put("fileSize", fileSize);
                            jsonObject.put("fileType", fileType);
                            jsonObject.put("fileUri", selectedUri);

                            fileObjects.add(jsonObject);
                        }
                        cursor.close();
                    } catch (IllegalArgumentException | JSONException ignored) {
                    }
                }
            }
        }
        selectedUris.clear();
        return fileObjects;
    }

    public static String getMimeType(String url) {
        String extension = URLConnection.guessContentTypeFromName(url);
        return extension;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public WritableArray convertToWritableArray(ArrayList<Object> arrayList) {
        List<String> objectFiles = new ArrayList<String>();
        for (Object objFile : arrayList) {
            objectFiles.add(objFile.toString());
        }
        WritableArray writableArray = new WritableNativeArray();
        for (String objFile : objectFiles) {
            String objFileString = objFile.toString().replaceAll("\\\\", "");
            writableArray.pushString(objFileString);
        }
        return writableArray;
    }
    private static ArrayList<String> toArrayList(ReadableArray array) {
        ArrayList<String> arrayList = new ArrayList<>(array.size());
        for (int i = 0, size = array.size(); i < size; i++) {
            switch (array.getType(i)) {
                case String:
                    arrayList.add(array.getString(i));
                    break;
                default:
            }
        }
        return arrayList;
    }
}
