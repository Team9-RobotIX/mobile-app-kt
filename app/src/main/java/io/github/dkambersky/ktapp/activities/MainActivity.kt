package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.data.Delivery
import khttp.get
import khttp.post
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import java.util.*


class MainActivity : BaseActivity() {

    /* Delay in milliseconds between updates */
    private val pollingDelay = 400L

    private val updatePausePeriod = 1000L

    private var deliveries = listOf<Delivery>()
    private var deliveriesShow = listOf<String>()

    var updatesPaused = false

    var updateTask: TimerTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        buttonLogin.setOnClickListener {
            toggleVisibility(login_form)
            toggleVisibility(deliveryPane, visible = login_form.visibility == View.GONE)
        }
        email_sign_in_button.setOnClickListener {
            toggleVisibility(login_form)

            if (!flobotApp.auth.loggedIn)
                trySigningIn()


            /* After login is done, try toggling */
            launch {
                Thread.sleep(pollingDelay)
                runOnUiThread { toggleVisibility(deliveryPane, visible = login_form.visibility == View.GONE) }
            }

        }
        buttonCreateOrder.setOnClickListener { transition(CreateOrderActivity::class.java) }


        /* Disable creating order and scanning w/o login */
        buttonCreateOrder.isEnabled = false

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

    private fun trySigningIn() {
        /* Hide keyboard - android APIs for this are a bloody mess */
        hideKeyboardFrom(name)
        hideKeyboardFrom(password)

        launch {
            val uName = name.text.toString()
            val response = post(flobotApp.serverUrl + "/login",
                    json = mapOf(
                            "username" to uName,
                            "password" to password.text.toString()
                    )
            )

            when (response.statusCode) {
                200 -> {
                    /* Extract bearer token */
                    val token = response.jsonObject.get("bearer") as String

                    /* Save login data */
                    flobotApp.auth.name = uName
                    flobotApp.auth.loggedIn = true
                    flobotApp.auth.token = token

                    /* Inform the user */
                    showSnackbar("Logged in successfully!", Snackbar.LENGTH_LONG)
                    
                    /* Enable user-dependent actions */
                    this@MainActivity.runOnUiThread {
                        buttonCreateOrder.isEnabled = true
                    }
                }
                401 -> {
                    showSnackbar("Wrong username or password")
                }

            }
            this@MainActivity.runOnUiThread({ authStatusText.text = flobotApp.auth.loggedInFriendlyText() })

        }
    }


    private fun updateDeliveryList() {
        if (updatesPaused) return

        var list = mutableListOf<Delivery>()
        try {
            list = jacksonObjectMapper().readValue(get("${flobotApp.serverUrl}/deliveries").text)
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
                    val resp = get("${flobotApp.serverUrl}/delivery/$orderId")
                    transition(TrackOrderActivity::class.java, "order" to resp.text)
                }


            }
        }
    }

}


