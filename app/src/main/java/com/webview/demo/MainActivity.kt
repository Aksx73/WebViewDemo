package com.webview.demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.webview.demo.databinding.ActivityMainBinding
import com.webview.demo.helper.Connectivity
import com.webview.demo.helper.PermissionHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(){

    private lateinit var binding : ActivityMainBinding

    companion object {
        const val URL_MISSION_100 = "https://g05f99af21721c3-bjsdev.adb.ap-mumbai-1.oraclecloudapps.com/ords/r/prachar/mission-100"
        const val URL_RWB = "https://g05f99af21721c3-bjsdev.adb.ap-mumbai-1.oraclecloudapps.com/ords/r/prachar/mission-100"
        const val FILE_PICKER_REQ_CODE = 19
    }

    private var webViewAudioEnabled = true
    private var webViewCameraEnabled = true
    private var fileChooserEnabled = true

    private var filePickerFileMessage: ValueCallback<Uri>? = null
    private var filePickerFilePath: ValueCallback<Array<Uri>>? = null
    private var filePickerCamMessage: String? = null
    private var FILE_TYPE = "*/*"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()
    }

    private fun initWebView(){

        binding.webview.settings.loadsImagesAutomatically = true
        binding.webview.settings.javaScriptEnabled = true
        // binding.webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        binding.webview.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        binding.webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webview.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.useWideViewPort = true
        binding.webview.isVerticalScrollBarEnabled = true
        binding.webview.isScrollbarFadingEnabled = true
        binding.webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.progressBar.setProgressCompat(10, true)
        binding.webview.webViewClient = WebViewClient()
        binding.webview.webChromeClient = MyWebViewChromeClient()

        binding.webview.loadUrl(URL_MISSION_100)

    }

 /*   inner class MyWebViewClient : WebViewClient(){
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
        }

    }*/

    inner class MyWebViewChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, progress: Int) {
            binding.progressBar.setProgressCompat(progress, true)
            if (progress == 100) {
                binding.progressBar.visibility = View.INVISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback) {
            PermissionHelper.CheckPermissions(this@MainActivity, object :
                PermissionHelper.CheckPermissionListener {
                    override fun onAllGranted(sync: Boolean) {
                        callback.invoke(origin, true, true)
                    }

                    override fun onPartlyGranted(permissionsDenied: List<String?>?, sync: Boolean) {
                        callback.invoke(origin, false, false)
                    }
                }, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            for (res in request.resources) {
                if (res == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                    if (!webViewAudioEnabled) {
                        request.deny()
                        return
                    }
                }
                if (res == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                    if (!webViewCameraEnabled) {
                        request.deny()
                        return
                    }
                }
            }

            PermissionHelper.CheckPermissions(this@MainActivity, object : PermissionHelper.CheckPermissionListener {
                    override fun onAllGranted(sync: Boolean) {
                        request.grant(request.resources)
                    }

                    override fun onPartlyGranted(permissionsDenied: List<String?>?, sync: Boolean) {
                        request.deny()
                    }
                }, *parsePermission(request.resources)
            )
        }

        //Handling input[type="file"] requests for android API 16+
        fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
           // handler.sendEmptyMessage(MSG_CLICK_ON_URL)
            if (!fileChooserEnabled) {
                uploadMsg.onReceiveValue(null)
                return
            }
            filePickerFileMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = acceptType
            startActivityForResult(
                Intent.createChooser(i, "File Chooser"), FILE_PICKER_REQ_CODE
            )
        }

        //Handling input[type="file"] requests for android API 21+
        override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
            if (!fileChooserEnabled) {
                filePathCallback.onReceiveValue(null)
                return true
            }
            filePickerFilePath?.onReceiveValue(null)
            filePickerFilePath = filePathCallback

            //Checking permission for storage and camera for writing and uploading images
            val perms = arrayOf(/*Manifest.permission.WRITE_EXTERNAL_STORAGE,*/ Manifest.permission.CAMERA)
            PermissionHelper.CheckPermissions(this@MainActivity, object : PermissionHelper.CheckPermissionListener {
                    override fun onAllGranted(sync: Boolean) {
                        val takePictureIntent: Intent = createCameraCaptureIntent(fileChooserParams.acceptTypes)!!
                        if (fileChooserParams.isCaptureEnabled && fileChooserParams.mode == FileChooserParams.MODE_OPEN) {
                            // capture="camera" and without multiple
                            startActivityForResult(takePictureIntent, FILE_PICKER_REQ_CODE)
                            return
                        }
                        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        contentSelectionIntent.type = FILE_TYPE
                        contentSelectionIntent.putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            fileChooserParams.acceptTypes
                        )
                        val intentArray: Array<Intent?> = arrayOf(takePictureIntent) ?: arrayOfNulls(0)
                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(
                            Intent.EXTRA_TITLE,
                           "File Chooser"
                        )
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        startActivityForResult(chooserIntent, FILE_PICKER_REQ_CODE)
                    }

                    override fun onPartlyGranted(permissionsDenied: List<String>, sync: Boolean) {
                        if (filePickerFilePath != null) {
                            filePickerFilePath!!.onReceiveValue(null)
                            filePickerFilePath = null
                        }
                    }
                }, *perms)
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_PICKER_REQ_CODE) {
                if (null == filePickerFilePath) {
                    return
                }
                if (intent == null || intent.dataString == null) {
                    if (filePickerCamMessage != null) {
                        results = arrayOf(Uri.parse(filePickerCamMessage))
                    }
                } else {
                    val dataString = intent.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
        }
        filePickerFilePath?.onReceiveValue(results)
        filePickerFilePath = null
    }

    private fun createCameraCaptureIntent(mimeTypes: Array<String>?): Intent? {
        var isVideo = false
        if (mimeTypes != null && mimeTypes.size == 1 && mimeTypes[0].startsWith("video")) {
            isVideo = true
        }

        var takePictureIntent: Intent? = Intent(if (isVideo) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent!!.resolveActivity(this@MainActivity.packageManager) != null) {
            var imageVideoFile: File? = null
            try {
                imageVideoFile = createImageOrVideo(isVideo)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            if (imageVideoFile != null) {
                filePickerCamMessage = "file:" + imageVideoFile.absolutePath
                val photoUri = FileProvider.getUriForFile(this, "$packageName.file_provider", imageVideoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            } else {
                takePictureIntent = null
            }
        }
        return takePictureIntent
    }

    @Throws(IOException::class)
    fun createImageOrVideo(isVideo: Boolean): File? {
        @SuppressLint("SimpleDateFormat")
        val file_name = SimpleDateFormat("yyyy_mm_ss").format(Date())
        val new_name = "file_" + file_name + "_"
        val sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(new_name, if (isVideo) ".mp4" else ".jpg", sd_directory)
    }




    fun parsePermission(resource: Array<String>): Array<String?> {
        val permissions: MutableList<String> = ArrayList()
        for (res in resource) {
            /*if (res == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                permissions.add(Manifest.permission.RECORD_AUDIO)
            }*/
            if (res == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                permissions.add(Manifest.permission.CAMERA)
            }
        }
        val result = arrayOfNulls<String>(permissions.size)
        for (i in permissions.indices) {
            result[i] = permissions[i]
        }
        return result
    }


    override fun onResume() {
        super.onResume()
        if (!Connectivity.isConnected(this)) {
            Snackbar.make(binding.webview, "No internet", Snackbar.LENGTH_LONG).show()
        }
    }

     override fun onDestroy() {
        binding.webview.clearCache(true)
        binding.webview.destroy()
        super.onDestroy()
    }

}