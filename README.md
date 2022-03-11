# react-native-android-document-picker

## Getting started

`$ npm install react-native-android-document-picker --save`

### Mostly automatic installation

`$ react-native link react-native-android-document-picker`

## Usage
```javascript
import AndroidDocumentPicker from 'react-native-android-document-picker';
```

### openDocument
```javascript
AndroidDocumentPicker.openDocument({
    multipleFiles: boolean,
    fileTypes: object,
  },
  successCallback: Function,
  failureCallback: Function
);
```

#### openDocument usage
```javascript
const handleChoosePhotoAndroid = async () => {
    let newFiles = [...files];

        await AndroidDocumentPicker.openDocument({multipleFiles: true, fileTypes: ["image/*"]}, (array) => {
          array.forEach((el) => {
            const doc = JSON.parse(el);
          
            console.log("doc:", doc);
            // {"fileName": "some_pdf_file.pdf", 
            // "fileSize": "450110", 
            // "fileType": "application/pdf", 
            // "fileUri": "content://com.android.providers.downloads.documents/document/1058"}
            
            newFiles.push({
            fileName: doc.fileName,
            uri: doc.fileUri,
            type: doc.fileType,
            size: doc.fileSize
            });
          });

          setFiles(newFiles);
        }, 
        (error) => {
            console.log('error', error);
        });
```

#### To-do
- [ ] clean up iOS related folders
- [ ] improve documentation
