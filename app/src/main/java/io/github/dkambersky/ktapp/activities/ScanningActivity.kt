package io.github.dkambersky.ktapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.util.BaseScannerViewEventListener
import khttp.async
import kotlinx.android.synthetic.main.activity_scanning.*
import kotlinx.coroutines.experimental.launch


class ScanningActivity : BaseActivity() {
    var nextLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)

        /* Check camera permissions */
        ensurePermissions()

        /* Set up scanner */
        camview.startScanner()

        camview.scannerViewEventListener = object : BaseScannerViewEventListener(this) {
            override fun onCodeScanned(data: String) {

                /* On successful scan, show text to user, until we have an auth mechanism */
                Toast.makeText(context, data, Toast.LENGTH_SHORT).show()

                /* Debug - flip auth bit on server */
                if (data == "${flobotApp.serverUrl}/lock" ||
                        data == "http://18.219.63.23/development/lock")
                    launch {
                        async.post(
                                flobotApp.serverUrl + "/lock",
                                json = mapOf("locked" to nextLocked),
                                timeout = 1.0
                        )
                        {
                            println("Got response")
                            if (statusCode == 200)
                                println("Lock switch succeeded!")
                            else
                                println("Error switching lock!\n$statusCode $text")

                        }
                        /* This might be more important than it looks - async weirdness */
                        println("Lock should be switched")
                        nextLocked = !nextLocked


                    }


            }
        }
    }

    private fun ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 50)
    }

}
