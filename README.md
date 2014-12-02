# Image Service
RESTful image manipulation service

## Read First !!
This application requires [OpenCV](http://opencv.org) compiled on your machine.<br>
I have a cookbook for Linux [HERE](https://github.com/mapfap/ImageService/wiki/OpenCV-Cookbook).<br>
For Windows please see the [Official Website](http://opencv.org)<br>
NOTE:
* You have to change path to your native library in Main class.
* Please use OpenCV 2.4.10. If you have different version, you have to change lib file in pom.xml

## What's it for?
* Mobile App: Smartphones don't have enough memory to quickly process images, and it drains the battery.  Image Editor Service offloads this work to a remote service.
* Studio: Use only one workstation to process all work.
* Web App: can easily request different variations of the same image (such as thumbnail) just by changing the image URL. No need for image manipulation in the web app.

## Use Cases
* Store image on server.
* Show image.
* Edit image of given URL (external links are permitted).
* Delete image from server.

## Operations ##
* Resize
* Set brightness
* Set saturation
* Convert to grayscale
* Apply distortion effect

## Example Request
```js
GET images/e23f2186b?width=100&height=100&grayscale=true&brightness=-2
```
Get image in 100 x 100, set it to grayscale and brightness = -2

## Client
Very simple web browser client [here](https://github.com/mapfap/ImageService-SimpleClient)

## API Specification
[see](https://github.com/mapfap/ImageService/wiki/API-Specification)

## Software Design
[see](https://github.com/mapfap/ImageService/wiki/Software-Design)

## Dependencies
* Jersey
* Jetty
* Apache Commons
* Apache Derby
* EclipseLink JPA

Note: use maven to install all packages except
* [OpenCV 2.4.10](http://opencv.org), required native library (need to compile its source)

## Security Consideration
* What if user send very large file
* What if user repeatedly requests  1x1, 1x2, 1x3, ..., 1x10000000 ~ This would really hurts the server.
* What if user try to get all images by keep scanning through increasing ID /images/1, /images/2, ..., /images/1000
  ```
    Fixed: Use hashed ID instead of serialized ID.
  ```
  
## Supported Formats
* Windows bitmaps - *.bmp, *.dib
* JPEG files - *.jpeg, *.jpg, *.jpe
* JPEF 2000 files - *.jp2
* Portable Network Graphics - *.png
* Portable image format - *.pbm, *.pgm, *.ppm
* Sun raster - *.sr, *.ras
* TIFF files - *.tiff, *tif
