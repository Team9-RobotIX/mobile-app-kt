package io.github.dkambersky.ktapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.util.BaseScannerViewEventListener
import kotlinx.android.synthetic.main.activity_scanning.*
import kotlinx.coroutines.experimental.launch


class ScanningActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)

        /* Check camera permissions */
        ensurePermissions()

        /* Set up scanner */
        camview.startScanner()

        camview.setHudVisible(false)

        camview.scannerViewEventListener = object : BaseScannerViewEventListener(this) {
            override fun onCodeScanned(data: String) {

                /* On successful scan, show text to user, until we have an auth mechanism */
                Toast.makeText(context, data, Toast.LENGTH_SHORT).show()

                /* Debug - flip auth bit on server */
                if (data == "${flobotApp.serverUrl}/lock")
                    launch { readFromServer("/lock") }
            }
        }
    }

    private fun ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 50)
    }

}
