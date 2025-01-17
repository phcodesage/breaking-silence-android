package com.example.breaking_silence_team_dream

import android.os.Bundle
import android.webkit.*
import android.view.View
import android.view.ViewGroup
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.breaking_silence_team_dream.ui.theme.BreakingsilenceteamdreamTheme
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.content.pm.PackageManager
import android.os.Build

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request permissions at startup - simplified for Android 8+
        requestPermissions(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST
        )

        enableEdgeToEdge()
        setContent {
            BreakingsilenceteamdreamTheme {
                WebViewScreen(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            // Handle permission results if needed
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted
            }
        }
    }
}

@Composable
fun WebViewScreen(activity: ComponentActivity) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(!isNetworkAvailable(context)) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest) {
                            activity.runOnUiThread {
                                request.grant(arrayOf(
                                    PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                                    PermissionRequest.RESOURCE_AUDIO_CAPTURE
                                ))
                            }
                        }
                    }

                    settings.apply {
                        // Essential settings
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        
                        // WebRTC specific settings
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        databaseEnabled = true
                        allowContentAccess = true
                        allowFileAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        
                        // Media settings
                        mediaPlaybackRequiresUserGesture = false
                        
                        // Use desktop Chrome user agent for better compatibility
                        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
                        
                        // Enable hardware acceleration
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        
                        // Cache settings
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        
                        // Enable WebRTC
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    // Enable hardware acceleration at the view level
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    loadUrl("https://app.breakingsilence.website/")
                }
            }
        )

        if (isLoading) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.loading_screen, null).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            )
        }
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}