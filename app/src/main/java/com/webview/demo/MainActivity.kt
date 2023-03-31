package com.webview.demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.*
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
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


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var doubleBackToExitPressedOnce = false

    companion object {
        const val URL_MISSION_100 =
            "https://g05f99af21721c3-bjsdev.adb.ap-mumbai-1.oraclecloudapps.com/ords/r/prachar/mission-100"
        const val URL_RWB =
            "https://g05f99af21721c3-bjsdev.adb.ap-mumbai-1.oraclecloudapps.com/ords/r/prachar/mission-100"
        const val FILE_PICKER_REQ_CODE = 19
        const val VIDEO_DURATION = 30
    }

    private var webViewAudioEnabled = true
    private var webViewCameraEnabled = true
    private var fileChooserEnabled = true

    private var filePickerFileMessage: ValueCallback<Uri>? = null
    private var filePickerFilePath: ValueCallback<Array<Uri>>? = null
    private var filePickerCamMessage: String? = null
    private var FILE_TYPE = "*/*"

    var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var permissions_33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.CAMERA
    )

    fun storagePermissions(): Array<String> {
        val p: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions_33
        } else {
            permissions
        }
        return p
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()

    }

    private fun initWebView() {

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

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback
        ) {
            PermissionHelper.CheckPermissions(
                this@MainActivity,
                object :
                    PermissionHelper.CheckPermissionListener {
                    override fun onAllGranted(sync: Boolean) {
                        callback.invoke(origin, true, true)
                    }

                    override fun onPartlyGranted(permissionsDenied: List<String?>?, sync: Boolean) {
                        callback.invoke(origin, false, false)
                    }
                },
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            for (res in request.resources) {
                /* if (res == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                     if (!webViewAudioEnabled) {
                         request.deny()
                         return
                     }
                 }*/
                if (res == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                    if (!webViewCameraEnabled) {
                        request.deny()
                        return
                    }
                }
            }

            PermissionHelper.CheckPermissions(
                this@MainActivity, object : PermissionHelper.CheckPermissionListener {
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
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (!fileChooserEnabled) {
                filePathCallback.onReceiveValue(null)
                return true
            }
            filePickerFilePath?.onReceiveValue(null)
            filePickerFilePath = filePathCallback

            //Checking permission for storage and camera for writing and uploading images
            PermissionHelper.CheckPermissions(
                this@MainActivity,
                object : PermissionHelper.CheckPermissionListener {
                    override fun onAllGranted(sync: Boolean) {
                        val takePictureIntent: Intent =
                            createCameraCaptureIntent(fileChooserParams.acceptTypes)!!
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
                        val intentArray: Array<Intent?> =
                            arrayOf(takePictureIntent) ?: arrayOfNulls(0)
                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser")
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        startActivityForResult(chooserIntent, FILE_PICKER_REQ_CODE)
                    }

                    override fun onPartlyGranted(permissionsDenied: List<String>, sync: Boolean) {
                        if (filePickerFilePath != null) {
                            filePickerFilePath!!.onReceiveValue(null)
                            filePickerFilePath = null
                        }
                    }
                },
                *storagePermissions()
            )
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
        if (mimeTypes != null && mimeTypes.isNotEmpty() && mimeTypes[0].startsWith("video")) {
            isVideo = true
        }

        var takePictureIntent: Intent? =
            Intent(if (isVideo) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE)

        if (isVideo) takePictureIntent?.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_DURATION);

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
        val sd_directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.webview.canGoBack()) {
                        binding.webview.goBack()
                    } else {
                        if (doubleBackToExitPressedOnce) {
                            super.onBackPressed()
                        }
                        this.doubleBackToExitPressedOnce = true
                        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onResume() {
        super.onResume()
        if (!Connectivity.isConnected(this)) {
            Snackbar.make(binding.webview, "No internet", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
       // binding.webview.clearCache(true)
       // binding.webview.destroy()
        super.onDestroy()

        //todo clear media created cache
    }

}