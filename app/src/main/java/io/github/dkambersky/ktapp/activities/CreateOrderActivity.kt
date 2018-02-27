package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.data.DeliveryTarget
import khttp.async
import khttp.get
import kotlinx.android.synthetic.main.activity_create_order.*
import kotlinx.coroutines.experimental.launch

class CreateOrderActivity : BaseActivity() {
    private var targets = mutableListOf<DeliveryTarget>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        /* Disable send button initially */
        buttonSendOrder.isEnabled = false

        /* Listeners*/
        buttonSendOrder.setOnClickListener { println("Send pressed"); sendOrder() }
        editTextName.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) checkSendability() }
        editTextDescription.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) checkSendability() }
        spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                buttonSendOrder.isEnabled = false
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                checkSendability()
            }
        }
        spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                buttonSendOrder.isEnabled = false
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                checkSendability()
            }
        }

        // Asynchronously load available targets
        launch { initializeSpinners() }

    }


    /* Disable Send Order button on invalid input */
    private fun checkSendability() {
        println("checking sendability")
        buttonSendOrder.isEnabled =
                editTextDescription.text.toString() != "" &&
                editTextName.text.toString() != "" &&
                ((spinnerFrom.selectedItem as DeliveryTarget).id !=
                        (spinnerTo.selectedItem as DeliveryTarget).id)

        println(buttonSendOrder.isEnabled)
    }

    /* Fetch data about targets, populate dropowns */
    private fun initializeSpinners() {
        launch {
            /* Haha, what is a pull parser?
             * This downloads, parses and assigns the array in 85 characters total.
             */
            targets = jacksonObjectMapper().readValue(get(flobotApp.serverUrl + "/targets").text)

            val adapter = ArrayAdapter<DeliveryTarget>(
                    applicationContext,
                    android.R.layout.simple_spinner_dropdown_item,
                    targets

            )
            this@CreateOrderActivity.runOnUiThread({ spinnerFrom.adapter = adapter; adapter.notifyDataSetChanged() })

            /* A bit unclean
                TODO clean up
             */
            val adapter2 = ArrayAdapter<DeliveryTarget>(
                    applicationContext,
                    android.R.layout.simple_spinner_dropdown_item,
                    targets

            )
            this@CreateOrderActivity.runOnUiThread({ spinnerTo.adapter = adapter2; adapter2.notifyDataSetChanged() })

        }
    }

    private fun sendOrder() {

        launch {
            async.post(
                    flobotApp.serverUrl + "/deliveries",
//                    "http://httpbin.org/post", // Debugging bin
                    json = mapOf
                    (
                            "name" to editTextName.text.toString(),
                            "description" to editTextDescription.text.toString(),
                            "priority" to 0,
                            "from" to (spinnerFrom.selectedItem as DeliveryTarget).id,
                            "to" to (spinnerTo.selectedItem as DeliveryTarget).id
                    ),
                    timeout = 1.0
            )
            {
                println("Got response")
                if (statusCode == 200)
                    println("Order successfully created!")
                else
                    println("Error creating order!\n$statusCode $text")

            }
            /* This might be more important than it looks - async weirdness */
            println("Order should be sent")


        }
    }
}
