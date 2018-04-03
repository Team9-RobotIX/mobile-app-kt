package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.data.Delivery
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import java.util.*


class MainActivity : BaseActivity() {

    /* Delay in milliseconds between updates */
    private val pollingDelay = 400L

    private val updatePausePeriod = 1000L

    private var deliveries = listOf<Delivery>()
    private var deliveriesShow = listOf<String>()

    private var updatesPaused = false

    private var updateTask: TimerTask? = null


    override fun onResume() {
        super.onResume()
        flobotApp.loadUrl()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        flobotApp.loadUrl()
//        launch { Thread.sleep(1000); transition(SettingsActivity::class.java) }

        buttonCreateOrder.setOnClickListener { transition(CreateOrderActivity::class.java) }

        authStatusText.text = flobotApp.auth.loggedInFriendlyText()


        /* Pause updating if user touches carousel - not to yank state away when interacting */
        val listViewTimer = Timer()
        listViewOrders.setOnTouchListener { _, _ ->
            updatesPaused = true
            updateTask?.cancel()

            updateTask = object : TimerTask() {
                override fun run() {
                    updatesPaused = false
                }
            }
            listViewTimer.schedule(updateTask, updatePausePeriod)
            false
        }


        /* Schedule polling updates */
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                launch { updateDeliveryList() }
            }
        }, pollingDelay, pollingDelay)


    }


    private fun updateDeliveryList() {
        if (updatesPaused) return

        var list = mutableListOf<Delivery>()
        try {
            val response = getOrSnack("${flobotApp.serverUrl}/deliveries")
            if (response != null) {
                list = jacksonObjectMapper().readValue(response.text)
            }
        } catch (e: Exception) {
            println("Something broke in parsing: ${e.printStackTrace()}")
        }


        runOnUiThread {
            deliveries = list.filter { it.sender == flobotApp.auth.name || it.receiver == flobotApp.auth.name }.toMutableList()
            deliveriesShow = deliveries.map { it.prettyPrint(flobotApp.auth.name!!) }

            deliveries.forEach {
                println("Delivery from ${it.sender} to ${it.receiver}: ${it.name}")
                listViewOrders.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deliveriesShow)
            }


            listViewOrders.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
                val delivery = (listViewOrders.adapter.getItem(position) as String)

                val orderId = delivery
                        .substringBefore(' ')
                        .substring(1)

                /* Asynchronously transition to tracking state*/
                launch {
                    val resp = getOrSnack("${flobotApp.serverUrl}/delivery/$orderId")
                    if (resp != null)
                        transition(TrackOrderActivity::class.java, "order" to resp.text)
                }

            }
        }
    }

}


