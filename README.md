# Image Service
RESTful Image Optimisation Web Service
Edit and access images using a web service

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
```
GET /images/001/100x100?grayscale=true&brightness=-10
```

Get image in 100 x 100, set it to grayscale and brightness = -10

```
GET /images/002?gaussian=20&duplicate=true
```
Get image with default size, apply gaussian effect and save it as new image
-> return location header instead of an image

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
