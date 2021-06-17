import 'dart:io';
import 'dart:typed_data';
import 'package:path/path.dart';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:add_to_gallery/add_to_gallery.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

final String _albumName = 'Add to Gallery';

double textSize = 20;

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String firstButtonText = 'Take photo';
  String secondButtonText = 'Record video';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        body: ListView(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Center(
                child: Text(
                  'Add to Gallery',
                  style: Theme.of(context).textTheme.subtitle1,
                ),
              ),
            ),
            SaveAsset(assetPath: 'assets/local-image-1.jpg'),
            SaveAsset(assetPath: 'assets/local-image-2.jpg'),
            SaveImage(),
          ],
        ),
      ),
    );
  }
}

class SaveAsset extends StatelessWidget {
  final String assetPath;

  const SaveAsset({
    Key? key,
    required this.assetPath,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: () async {
          try {
            File file = await _copyAssetLocally(assetPath);
            String path = await AddToGallery.addToGallery(
              originalFile: file,
              albumName: _albumName,
              deleteOriginalFile: false,
            );
            String message =
                'Added to Gallery\n\nOriginal: ${file.path}\n\nGallery: $path';
            await _showAlertMessage(context, message);
          } on PlatformException catch (e) {
            await _showAlertMessage(context, 'Error: ${e.message}');
          } catch (e) {
            await _showAlertMessage(context, 'Error: ${e.toString()}');
          }
        },
        child: Padding(
          padding: EdgeInsets.all(8.0),
          child: Column(
            children: <Widget>[
              Image.asset(
                assetPath,
                height: 100,
              ),
              Text('Save Local Asset'),
            ],
          ),
        ),
      ),
    );
  }
}

class SaveImage extends StatelessWidget {
  const SaveImage({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: () async {
          try {
            PickedFile? image =
                await ImagePicker().getImage(source: ImageSource.camera);
            if (image != null) {
              File file = File(image.path);
              String path = await AddToGallery.addToGallery(
                originalFile: file,
                albumName: _albumName,
                deleteOriginalFile: false,
              );
              String message =
                  'Added to Gallery\n\nOriginal: ${file.path}\n\nGallery: $path';
              await _showAlertMessage(context, message);
            }
          } on PlatformException catch (e) {
            await _showAlertMessage(context, 'Error: ${e.message}');
          } catch (e) {
            await _showAlertMessage(context, 'Error: ${e.toString()}');
          }
        },
        child: Padding(
          padding: EdgeInsets.all(8.0),
          child: Column(
            children: <Widget>[
              Icon(Icons.camera_alt),
              Text('Take Photo'),
            ],
          ),
        ),
      ),
    );
  }
}

Future<void> _showAlertMessage(
  BuildContext context,
  String message,
) async {
  await showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: Text('Saved to Gallery'),
        content: Text(message),
        actions: <Widget>[
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: Text('OK'),
          ),
        ],
      );
    },
  );
}

Future<File> _copyAssetLocally(
  String path,
) async {
  ByteData byteData = await rootBundle.load(path);
  File file = await _getBlankFileForAsset(
    path: path,
    prefix: 'assets',
  );
  await file.writeAsBytes(
    byteData.buffer.asUint8List(
      byteData.offsetInBytes,
      byteData.lengthInBytes,
    ),
  );
  return file;
}

Future<File> _getBlankFileForAsset({
  required String path,
  required String prefix,
}) async {
  String fileExt = extension(path);
  int now = DateTime.now().millisecondsSinceEpoch;
  String fileName = '$prefix-$now$fileExt';
  Directory directory = await getTemporaryDirectory();
  return File('${directory.path}/$fileName');
}
