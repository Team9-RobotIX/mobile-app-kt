package io.github.dkambersky.ktapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View.GONE
import android.widget.Toast
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.util.BaseScannerViewEventListener
import khttp.async
import khttp.get
import khttp.patch
import kotlinx.android.synthetic.main.activity_track_order.*
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import java.util.*

class TrackOrderActivity : BaseActivity() {

    /* Delay in milliseconds between updates */
    private val pollingDelay = 500L

    /* The order we're tracking */
    lateinit var order: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)


        /* Register listeners */
        b_verify.setOnClickListener { initializeScanner() }
        b_confirmLoad.setOnClickListener { confirmPackageLoaded() }
        b_confirmRetrieve.setOnClickListener { confirmPackageRetrieved() }

        /* Hide situational UI elements */
        toggleVisibility(b_verify)
        toggleVisibility(b_confirmLoad)
        toggleVisibility(b_confirmRetrieve)
        toggleVisibility(scanview)


        updateOrderState(JSONObject(intent.extras["order"] as String))

        /* Schedule polling updates */
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                launch {
                    updateOrderState(get("${flobotApp.serverUrl}/delivery/${order.getInt("id")}").jsonObject)
                }
            }
        }, pollingDelay, pollingDelay)

    }

    private fun updateOrderState(order: JSONObject) {
        runOnUiThread {
            switchState(order.optString("state"))
            this.order = order
            try {
                track_title_num.text = order.getInt("id").toString()
            } catch (e: Exception) {
                Toast.makeText(this, "Delivery completed!", Toast.LENGTH_LONG).show()
                transition(MainActivity::class.java)
            }
        }
    }


    private fun switchState(state: String) {
        if (state == "") return

        /* Handle first run */
        if (!this::order.isInitialized) {
            track_state.text = state
        }

        /* Don't switch if already in a state */
        if (this::order.isInitialized && state == order.getString("state"))
            return

        track_state.text = state

        when (state) {
            "AWAITING_AUTHENTICATION_SENDER", "AWAITING_AUTHENTICATION_RECEIVER" -> {
                /* Display QR code challenge */
                toggleVisibility(b_verify)
            }
            "AWAITING_PACKAGE_LOAD" -> {
                if (flobotApp.auth.name == order.getString("sender")) {
                    /* Verification passed - hide verification UI */
                    toggleVisibility(scanview)

                    /* Allow user to indicate package has been loaded */
                    toggleVisibility(b_confirmLoad)
                }
            }
            "AWAITING_PACKAGE_RETRIEVAL" -> {
                if (flobotApp.auth.name == order.getString("receiver")) {
                    /* Verification passed - hide verification UI */
                    toggleVisibility(scanview)

                    /* Allow user to indicate package has been loaded */
                    toggleVisibility(b_confirmRetrieve)
                }
            }


            "PACKAGE_LOAD_COMPLETE", "MOVING_TO_DESTINATION" -> {
                /* The sender's work is done - hide the functional parts,
                   just track delivery's state. */
                toggleVisibility(trackInteractiveParts)
            }

            else -> {
                trackInteractiveParts.visibility = GONE
            }

        }

    }

    private fun initializeScanner() {
        /* Ensure we have camera permissions */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 50)

        /* Set up scanner */
        scanview.startScanner()

        /* Define scanner behavior */
        scanview.scannerViewEventListener = object : BaseScannerViewEventListener(this) {
            override fun onCodeScanned(data: String) {
                Toast.makeText(this@TrackOrderActivity, "Scanned $data", Toast.LENGTH_SHORT).show()
                launch {
                    async.post(
                            flobotApp.serverUrl + "/verify",
                            json = mapOf("token" to data),
                            timeout = 1.0,
                            headers = mapOf("Authorization" to "Bearer ${flobotApp.auth.token}")
                    )
                    {
                        if (statusCode == 200)
                            println("Verification completed!")
                        else
                            println("Error verifying!\n$statusCode $text")
                    }
                    /* This might be more important than it looks - async weirdness */
                    println("Verification complete")

                }


            }
        }

        /* Finally, show scanner */
        toggleVisibility(scanview)
        toggleVisibility(b_verify)
    }

    private fun confirmPackageLoaded() {
        launch {
            val resp = patch("${flobotApp.serverUrl}/delivery/${order.getInt("id")}",
                    json = mapOf("state" to "PACKAGE_LOAD_COMPLETE"),
                    timeout = 1.0,
                    headers = mapOf("Authorization" to "Bearer ${flobotApp.auth.token}")
            )
            println("Confirmation status: ${resp.statusCode}, text: ${resp.text}")
        }
    }

    private fun confirmPackageRetrieved() {
        launch {
            val resp = patch("${flobotApp.serverUrl}/delivery/${order.getInt("id")}",
                    json = mapOf("state" to "PACKAGE_RETRIEVAL_COMPLETE"),
                    timeout = 1.0,
                    headers = mapOf("Authorization" to "Bearer ${flobotApp.auth.token}")
            )
            println("Confirmation status: ${resp.statusCode}, text: ${resp.text}")
        }
    }


}
