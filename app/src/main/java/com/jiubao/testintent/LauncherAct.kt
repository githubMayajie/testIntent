package com.jiubao.testintent

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_launcher.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.data
import android.R.attr.data
import android.annotation.TargetApi
import android.content.*
import android.database.Cursor
import android.provider.DocumentsContract
import com.jiubao.testlayout.LayoutRootAct


/**
 * 功能描述:
 * 作者： Administrator 时间： 2018/4/11.
 * 版本:
 */
class LauncherAct:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        button.apply {
            text = "this is a launcher btn"
            setOnClickListener {
//                val intent = ComIntent.createAlarm("message",10,10)
//                val intent = ComIntent.showAllArarm()
//                val intent = ComIntent.addCalendar("title","location", Calendar.getInstance().apply {
//                    set(2015, 6, 23, 7, 30)
//                }, Calendar.getInstance().apply {
//                    set(2015, 6, 23, 10, 30)
//                })
//                addPermission()
//                val intent = ComIntent.getCapture(Uri.fromFile(File("1111.jpeg")),"1111.jpeg")
//
//                val intent = Intent(Intent.ACTION_PICK).apply {
//                    type = ContactsContract.Contacts.CONTENT_TYPE
//                }
//                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                    type = "image/*"
//                }
                val intent = Intent(this@LauncherAct,LayoutRootAct::class.java)
                if(intent.resolveActivity(packageManager) != null){
//                    startActivityForResult(intent,REQUEST_IMAGE_GET)
                    startActivity(intent)
                }

            }
        }
    }




    private var currentImgPath :String? = null

    fun createImageFile():File{
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName,".jpg",storageDir).apply {
            currentImgPath = this.absolutePath
        }
    }

    fun addPermission() {
//        MPermissions.requestPermissions(this, REQUEST_CODE_ALBUM, "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS");
        performCodeWithPermission("请求获取权限", object : PermissionCallback {
            override fun hasPermission() {
//                val takePicIntent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
                val takePicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                val takePicIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                if(takePicIntent.resolveActivity(packageManager) != null){
                    val filePhoto = createImageFile()
                    takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(this@LauncherAct,
                                    "$packageName.FileProvider",
                                    filePhoto))
                    startActivityForResult(takePicIntent,REQUEST_TAKE_PHOTO)
                }
            }

            override fun noPermission() {
                Toast.makeText(this@LauncherAct, "申请权限失败", Toast.LENGTH_SHORT).show()
            }
        },"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
    }

    fun addPicToGallery(){
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
            val f = File(currentImgPath)
            val contentUri = Uri.fromFile(f)
            data = contentUri
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO ){
                val file = File(currentImgPath)
                if(file.exists()){
//                addPicToGallery()
                    var bmOpt = BitmapFactory.Options()
                    bmOpt.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(currentImgPath,bmOpt)
                    val photoW = bmOpt.outWidth
                    val photoH = bmOpt.outHeight
                    val scaleFactor = Math.min(photoW / 300, photoH / 500)
                    bmOpt.inJustDecodeBounds = false
                    bmOpt.inSampleSize = scaleFactor
                    bmOpt.inPurgeable = true
                    imageView.setImageBitmap(BitmapFactory.decodeFile(currentImgPath,bmOpt))
                }
            }else if(requestCode == REQUEST_SELECT_CONSTACT){
                val contactUri = data?.getData()
                if(contactUri != null){

                }
            }else if(requestCode == REQUEST_IMAGE_GET){
                val fullPhotoUri = data?.getData()
                val path = readImagePathFromUri(this,fullPhotoUri!!,contentResolver)
                val file = File(path)
                if(file.exists()){
//                addPicToGallery()
                    var bmOpt = BitmapFactory.Options()
                    bmOpt.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(path,bmOpt)
                    val photoW = bmOpt.outWidth
                    val photoH = bmOpt.outHeight
                    val scaleFactor = Math.min(photoW / 300, photoH / 500)
                    bmOpt.inJustDecodeBounds = false
                    bmOpt.inSampleSize = scaleFactor
                    bmOpt.inPurgeable = true
                    imageView.setImageBitmap(BitmapFactory.decodeFile(path,bmOpt))
                }
//                imageView.setImageURI(fullPhotoUri)
            }

        } else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 0x99
        const val REQUEST_TAKE_PHOTO = 0x1
        const val REQUEST_SELECT_CONSTACT = 0x2
        const val REQUEST_IMAGE_GET = 0x3
    }

    interface PermissionCallback{
        fun hasPermission()
        fun noPermission()
    }

    var permissionCallback:PermissionCallback? = null;
    fun performCodeWithPermission(permissionDes:String?,callback: PermissionCallback?,vararg permissions:String){
        if(permissionDes == null || permissions.isEmpty()){
            return
        }
        permissionCallback = callback
        if((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || checkPermissionGranted(permissions)){
            if(permissionCallback != null){
                permissionCallback?.hasPermission()
                permissionCallback = null
            }
        }else{
            requestPermission(permissionDes, REQUEST_PERMISSION_CODE,permissions)
        }
    }


    fun checkPermissionGranted(permissions:Array<out String>):Boolean{
        for(permission in permissions){
            if(!selfPermissionGranted(permission)){
                return false
            }
        }
        return true
    }

    fun selfPermissionGranted(permission: String):Boolean{
        var result = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(android.R.attr.targetSdkVersion >= Build.VERSION_CODES.M){
                result = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }else{
                result =  PermissionChecker.checkSelfPermission(this@LauncherAct,permission) ==
                        PermissionChecker.PERMISSION_GRANTED;
            }
        }
        return result
    }


    fun requestPermission(permissionDes:String,requestCode: Int,permissions:Array<out String>){
        if(shouldShowRequestPermissionRationale(permissions)){
            //如果用户之前拒绝过此权限，再提示一次准备授权相关权限
            AlertDialog.Builder(this).setTitle("提示")
                    .setMessage(permissionDes)
                    .setPositiveButton("确认", DialogInterface.OnClickListener{ dialog, which->
                        ActivityCompat.requestPermissions(this@LauncherAct,permissions,requestCode)
                    }).show()
        }else{
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this@LauncherAct,permissions,requestCode)
        }
    }

    fun shouldShowRequestPermissionRationale(permissions: Array<out String>):Boolean{
        for (permission in permissions){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
                return true
            }
        }
        return false
    }

    fun verifyPermissions(grantResults:IntArray):Boolean{
        if(grantResults.isEmpty()){
            return false
        }
        for(result in grantResults){
            if(result != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(verifyPermissions(grantResults)){
                permissionCallback?.hasPermission()
            }else{
                permissionCallback?.noPermission()
            }
            permissionCallback = null
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    fun readImagePathFromUri(context: Context, uri: Uri, cr: ContentResolver): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getPath(context, uri)
        } else {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = cr.query(uri,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            return picturePath
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                      selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun getSDCardRootPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

}