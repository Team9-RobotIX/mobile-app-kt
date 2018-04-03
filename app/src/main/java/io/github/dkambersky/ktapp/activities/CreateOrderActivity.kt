package io.github.dkambersky.ktapp.activities

import android.R.layout.simple_spinner_dropdown_item
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dkambersky.ktapp.R
import io.github.dkambersky.ktapp.data.DeliveryTarget
import khttp.async
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


        editTextName.afterTextChanged { checkSendability() }
        editTextDescription.afterTextChanged { checkSendability() }

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
        println("Checking sendability")


        /* Return early if missing targets */
        if (spinnerFrom.selectedItem == null || spinnerTo.selectedItem == null) {
            buttonSendOrder.isEnabled = false
            return
        }


        buttonSendOrder.isEnabled =
                editTextName.text.toString() != "" &&
                ((spinnerFrom.selectedItem as DeliveryTarget).id !=
                        (spinnerTo.selectedItem as DeliveryTarget).id)

        println(buttonSendOrder.isEnabled)
    }

    /* Fetch data about targets, populate drop-downs */
    private fun initializeSpinners() {
        launch {
            /* Haha, what is a pull parser?
             * This downloads, parses and assigns the array in 85 characters total.
             */
            val targetsData = getOrSnack(flobotApp.serverUrl + "/targets")
            if (targetsData != null)
                targets = jacksonObjectMapper().readValue(targetsData.text)

            val adapter = ArrayAdapter<DeliveryTarget>(applicationContext, simple_spinner_dropdown_item, targets)
            this@CreateOrderActivity.runOnUiThread({ spinnerFrom.adapter = adapter; adapter.notifyDataSetChanged() })

            val adapter2 = ArrayAdapter<DeliveryTarget>(applicationContext, simple_spinner_dropdown_item, targets)
            this@CreateOrderActivity.runOnUiThread({ spinnerTo.adapter = adapter2; adapter2.notifyDataSetChanged() })


            val usersData = getOrSnack(flobotApp.serverUrl + "/users")
            val users = mutableListOf<String>()

            if (usersData?.jsonArray != null) {
                for (i in 0 until (usersData.jsonArray.length())) {
                    /* Android boilerplate >.> */
                    users.add((usersData.jsonArray.getJSONObject(i).getString("username")))

                }
            }

            val adapter3 = ArrayAdapter<String>(applicationContext, simple_spinner_dropdown_item, users)
            this@CreateOrderActivity.runOnUiThread({ spinnerUser.adapter = adapter3; adapter3.notifyDataSetChanged() })


        }
    }

    private fun sendOrder() {

        launch {
            async.post(
//                    "http://httpbin.org/post", // Debugging bin
                    flobotApp.serverUrl + "/deliveries",
                    json = mapOf
                    (
                            "name" to editTextName.text.toString(),
                            "description" to editTextDescription.text.toString(),
                            "priority" to 0,
                            "from" to (spinnerFrom.selectedItem as DeliveryTarget).id,
                            "to" to (spinnerTo.selectedItem as DeliveryTarget).id,
                            "sender" to flobotApp.auth.name,
                            "receiver" to spinnerUser.selectedItem as String
                    ),
                    timeout = 1.0,
                    headers = mapOf("Authorization" to "Bearer ${flobotApp.auth.token!!}")
            )
            {
                println("Got response")
                if (statusCode == 200) {
                    println("Order successfully created! $jsonObject")
                    transition(TrackOrderActivity::class.java,
                            "order" to text)
                } else
                    println("Error creating order!\n$statusCode $text")

            }
            /* This might be more important than it looks - async weirdness */
            println("Order should be sent")

        }
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }


}
