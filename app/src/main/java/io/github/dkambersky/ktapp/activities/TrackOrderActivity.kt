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
import khttp.delete
import khttp.get
import khttp.patch
import kotlinx.android.synthetic.main.activity_track_order.*
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import java.util.*

class TrackOrderActivity : BaseActivity() {

    /* Delay in milliseconds between updates */
    private val pollingDelay = 400L

    /* The order we're tracking */
    lateinit var order: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)


        /* Register listeners */
        b_verify.setOnClickListener { initializeScanner() }
        b_confirmLoad.setOnClickListener { confirmPackageLoaded() }
        b_confirmRetrieve.setOnClickListener { confirmPackageRetrieved() }
        b_cancel.setOnClickListener { cancelDelivery() }

        /* Hide situational UI elements */
        toggleVisibility(b_verify, b_confirmLoad, b_confirmRetrieve, scanview, visible = false)


        updateOrderState(JSONObject(intent.extras["order"] as String))

        /* Schedule polling updates */
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                launch {
                    val orderResponse = get("${flobotApp.serverUrl}/delivery/${order.getInt("id")}")
                    if (orderResponse.statusCode == 200)
                        updateOrderState(orderResponse.jsonObject)
                }
            }
        }, pollingDelay, pollingDelay)

    }

    private fun updateOrderState(order: JSONObject) {
        runOnUiThread {
            val needsUpdate = !this::order.isInitialized || this.order.getString("state") != order.getString("state")
            this.order = order

            if (needsUpdate)
                switchState(order.optString("state"))

            try {
                track_title_num.text = order.getInt("id").toString()
            } catch (e: Exception) {
                System.err.println("Error! ${e.localizedMessage}")
                e.printStackTrace()
                Toast.makeText(this, "Delivery completed!", Toast.LENGTH_LONG).show()
                transition(MainActivity::class.java)
            }
        }
    }


    private fun switchState(state: String) {
        track_state.text = state
        println("Switching state to $state")

        when (state) {
            "AWAITING_AUTHENTICATION_SENDER" -> {
                /* If user is the sender, display QR code challenge */
                if (flobotApp.auth.name == order.getString("sender")) {

                    toggleVisibility(trackInteractiveParts, b_verify, visible = true)
                }
            }
            "AWAITING_AUTHENTICATION_RECEIVER" -> {
                if (flobotApp.auth.name == order.getString("receiver")) {
                    /* Display QR code challenge */
                    toggleVisibility(trackInteractiveParts, b_verify, visible = true)
                }
            }
            "AWAITING_PACKAGE_LOAD" -> {
                if (flobotApp.auth.name == order.getString("sender")) {
                    /* Verification passed - hide verification UI */
                    toggleVisibility(scanview, visible = false)

                    /* Allow user to indicate package has been loaded */
                    toggleVisibility(trackInteractiveParts, b_confirmLoad, visible = true)
                }
            }
            "AWAITING_PACKAGE_RETRIEVAL" -> {
                if (flobotApp.auth.name == order.getString("receiver")) {
                    /* Verification passed - hide verification UI */
                    toggleVisibility(scanview, visible = false)

                    /* Allow user to indicate package has been loaded */
                    toggleVisibility(trackInteractiveParts, b_confirmRetrieve, visible = true)
                }
            }


            else -> {
                /* Not in an interactive state - just track the order. */
                toggleVisibility(trackInteractiveParts, visible = false)
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

                    val url = "${flobotApp.serverUrl}/${order.getString("robot")}/verify"
                    println("Sending verification to $url")
                    async.post(
                            url,
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
        toggleVisibility(b_verify, visible = false)
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

    private fun cancelDelivery() {
        launch {
            val resp = delete("${flobotApp.serverUrl}/delivery/${order.getInt("id")}",
                    timeout = 1.0,
                    headers = mapOf("Authorization" to "Bearer ${flobotApp.auth.token}")
            )
            println("Confirmation status: ${resp.statusCode}, text: ${resp.text}")
            if (resp.statusCode == 200)
                transition(MainActivity::class.java)
        }
    }


}
