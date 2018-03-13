package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.dkambersky.ktapp.R
import kotlinx.android.synthetic.main.activity_track_order.*
import org.json.JSONArray

class TrackOrderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)

        val order = JSONArray(intent.extras["order"] as String).getJSONObject(0)

        track_title_num.text = order.getInt("id").toString()
        track_state.text = order.getString("state")
    }
}
