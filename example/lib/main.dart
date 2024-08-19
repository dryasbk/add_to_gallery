import 'dart:io';
// import 'dart:typed_data';
import 'package:add_to_gallery/add_to_gallery.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

final String _albumName = 'Add to Gallery';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
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
                  style: Theme.of(context).textTheme.titleMedium,
                ),
              ),
            ),
            SaveAsset(assetPath: 'assets/local-image-1.jpg'),
            SaveAsset(assetPath: 'assets/local-image-2.jpg'),
            SaveImage(),
            SaveNewImageName(newImageName: 'newImageName'),
            SaveNewVideoName(
              newVideoName: 'NewVideoName',
            ),
            LastGalleryImage(),
          ],
        ),
      ),
    );
  }
}

class LastGalleryImage extends StatelessWidget {
  const LastGalleryImage({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _getGalleryPath(),
      builder: (BuildContext context, AsyncSnapshot<String?> snapshot) {
        String? galleryPath = snapshot.hasData ? snapshot.data : null;
        if (galleryPath == null) {
          return Text(
            'When you restart the app, the last saved item will render here',
          );
        } else {
          return Image.file(
            File(galleryPath),
            height: 100,
          );
        }
      },
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
            File localFile = await _copyAssetLocally(assetPath);
            // iOS
            if (!await Permission.photos.request().isGranted) {
              throw ('Permission Required');
            }
            // Android (v9 and below)
            if (!await Permission.storage.request().isGranted) {
              throw ('Permission Required');
            }
            File file = await AddToGallery.addToGallery(
              originalFile: localFile,
              albumName: _albumName,
              deleteOriginalFile: true,
            );
            await _saveGalleryPath(file.path);
            await _showAlertMessage(context, file.path);
          } on PlatformException catch (e) {
            await _showError(context, 'Error: ${e.message}');
          } catch (e) {
            await _showError(context, 'Error: ${e.toString()}');
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
            final XFile? image =
                await ImagePicker().pickImage(source: ImageSource.camera);
            if (image != null) {
              File cameraFile = File(image.path);
              // iOS
              if (!await Permission.photos.request().isGranted) {
                throw ('Permission Required');
              }
              // Android (v9 and below)
              if (!await Permission.storage.request().isGranted) {
                throw ('Permission Required');
              }
              File file = await AddToGallery.addToGallery(
                originalFile: cameraFile,
                albumName: _albumName,
                deleteOriginalFile: true,
              );
              await _saveGalleryPath(file.path);
              await _showAlertMessage(context, file.path);
            }
          } on PlatformException catch (e) {
            await _showError(context, 'Error: ${e.message}');
          } catch (e) {
            await _showError(context, 'Error: ${e.toString()}');
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

class SaveNewImageName extends StatelessWidget {
  const SaveNewImageName({Key? key, required this.newImageName})
      : super(key: key);
  final String newImageName;
  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: () async {
          try {
            final XFile? image =
                await ImagePicker().pickImage(source: ImageSource.camera);
            if (image != null) {
              File cameraFile = File(image.path);
              // iOS
              if (!await Permission.photos.request().isGranted) {
                throw ('Permission Required');
              }
              // Android (v9 and below)
              if (!await Permission.storage.request().isGranted) {
                throw ('Permission Required');
              }
              File file = await AddToGallery.addToGallery(
                originalFile: cameraFile,
                newFileName: newImageName,
                albumName: _albumName,
                deleteOriginalFile: true,
              );
              await _saveGalleryPath(file.path);
              await _showAlertMessage(context, file.path);
            }
          } on PlatformException catch (e) {
            await _showError(context, 'Error: ${e.message}');
          } catch (e) {
            await _showError(context, 'Error: ${e.toString()}');
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
//

class SaveNewVideoName extends StatelessWidget {
  const SaveNewVideoName({Key? key, required this.newVideoName})
      : super(key: key);
  final String newVideoName;
  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: () async {
          try {
            final XFile? image =
                await ImagePicker().pickVideo(source: ImageSource.gallery);
            if (image != null) {
              File cameraFile = File(image.path);
              // iOS
              if (!await Permission.photos.request().isGranted) {
                throw ('Permission Required');
              }
              // Android (v9 and below)
              if (!await Permission.storage.request().isGranted) {
                throw ('Permission Required');
              }
              File file = await AddToGallery.addToGallery(
                originalFile: cameraFile,
                newFileName: newVideoName,
                albumName: _albumName,
                deleteOriginalFile: true,
              );
              await _saveGalleryPath(file.path);
              await _showAlertMessage(context, file.path);
            }
          } on PlatformException catch (e) {
            await _showError(context, 'Error: ${e.message}');
          } catch (e) {
            await _showError(context, 'Error: ${e.toString()}');
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

//
Future<void> _showAlertMessage(
  BuildContext context,
  String path,
) async {
  await showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: Text('Saved to Gallery'),
        content: Image.file(
          File(path),
          height: 200,
        ),
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

Future<void> _showError(
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

Future<void> _saveGalleryPath(
  String path,
) async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.setString('galleryPath', path);
}

Future<String?> _getGalleryPath() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  return prefs.getString('galleryPath');
}
