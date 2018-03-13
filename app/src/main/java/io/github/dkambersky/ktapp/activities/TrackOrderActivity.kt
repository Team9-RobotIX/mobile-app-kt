package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import io.github.dkambersky.ktapp.R
import khttp.get
import kotlinx.android.synthetic.main.activity_track_order.*
import kotlinx.coroutines.experimental.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class TrackOrderActivity : BaseActivity() {

    /* Delay in milliseconds between updates */
    private val POLLING_DELAY = 500L

    /* The order we're tracking */
    lateinit var order: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)

        updateOrderState(JSONArray(intent.extras["order"] as String).getJSONObject(0))

        /* Schedule polling updates */
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                launch {
                    updateOrderState(get("${flobotApp.serverUrl}/delivery/${order.getInt("id")}").jsonObject)
                }
            }
        }, POLLING_DELAY, POLLING_DELAY)

    }

    fun updateOrderState(order: JSONObject) {
        runOnUiThread {
            this.order = order
            track_title_num.text = order.getInt("id").toString()
            track_state.text = order.getString("state")
            println("Updated order state.")
        }

    }

}
