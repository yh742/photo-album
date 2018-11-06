# photo-album

## What is this?

An app to upload photos with OAuth authentication. The album can process the image by turning it into x-ray gradiation or by converting the image to ascii.

## Video Demo

The video is hosted at: https://drive.google.com/file/d/1aWh_29O0aEsbICTo4rr_cmdoezAWuTux/view?usp=sharing

## TestCases

### TestCase 1: login as guest user and upload picture
Expected result: upload failure toast messages, guest users can't upload

Steps:

* User starts application
* User selects "Authentication" button
* User selects "LOGIN AS GUEST" button
* User selects "BROWSE" button
* User selects photo
* User selects "UPLOAD" button
* User enters image description
* Toast should show "You don't have permission to upload"

### TestCase 2: login as user and upload picture
Expected result: upload success toast message, actual users can upload

Steps:
* User starts application
* User selects "Authentication" button
* User selects "LOGIN WITH EMAIL" button
* User types in email "yh742@cornell.edu"
* User types in password
* User presses "SIGNIN" button
* User selects "BROWSE" button
* User selects photo
* User selects "UPLOAD" button
* User enters image description
* Toast should show "Image uploaded"
