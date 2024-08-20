import 'dart:async';
import 'dart:io';
import 'package:flutter/services.dart';
import 'package:add_to_gallery/src/copy_to_permenant_directory.dart';
import 'package:add_to_gallery/src/get_file_type.dart';

/// Save images and videos to the gallery
class AddToGallery {
  static const MethodChannel _channel = MethodChannel('add_to_gallery');

  /// Makes a COPY of the file and saves it to the gallery
  ///
  /// Returns the path of the new file in the gallery
  static Future<File> addToGallery({
    /// The original file to copy to the gallery
    required File originalFile,

    /// The new name of the file to save
    String? newFileName,

    /// Name of the album to save to, the album is created if necessary
    required String albumName,

    /// Should we delete the original file after saving?
    required bool deleteOriginalFile,
  }) async {
    // Is it an image or video?
    String filetype = getFileType(originalFile.path);
    // Copy the original file (which may be temporary) to a permenant directory
    File copiedFile = await copyToPermanentDirectory(
      originalFile: originalFile,
      prefix: filetype,
      newFileName: newFileName,
    );

    // Save to gallery // Modified so can save video
    String? methodResults;

    if (Platform.isIOS) {
      if (filetype == 'image') {
        methodResults = await _channel.invokeMethod(
          'addToGallery',
          <String, dynamic>{
            'type': filetype,
            'path': copiedFile.path,
            'album': albumName,
          },
        );
      } else if (filetype == 'video') {
        methodResults = await _channel.invokeMethod(
          "addVideoToGallery",
          <String, dynamic>{
            'type': filetype,
            'path': copiedFile.path,
            'album': albumName,
          },
        );
      }
    } else if (Platform.isAndroid) {
      if (filetype == 'image') {
        methodResults = await _channel.invokeMethod(
          'saveImage',
          {
            'filePath': copiedFile.path,
            'albumName': albumName,
          },
        );
      } else if (filetype == 'video') {
        methodResults = await _channel.invokeMethod(
          'saveVideo',
          {
            'filePath': copiedFile.path,
            'albumName': albumName,
          },
        );
      }
    }

    // Nothing? Probably Android, return the copied file
    if (methodResults == null) {
      return copiedFile;
    }
    File galleryFile = File(methodResults.toString());
    // If the operation created a NEW file, delete our copy
    if (galleryFile.path != copiedFile.path) {
      copiedFile.delete();
    }
    // Delete the original file?
    if (deleteOriginalFile) {
      originalFile.delete();
    }
    // Return the new file
    return galleryFile;
  }
}
