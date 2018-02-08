package io.github.dkambersky.ktapp.util

import android.content.Context
import android.widget.Toast
import eu.livotov.labs.android.camview.ScannerLiveView

/**
 * Listener with basic functionality, enabling us to only define onCodeScanned if we want to.
 */
abstract class BaseScannerViewEventListener(internal val context: Context) : ScannerLiveView.ScannerViewEventListener {
    override fun onScannerStarted(scanner: ScannerLiveView) {
        Toast.makeText(context, "Scanner Started", Toast.LENGTH_SHORT).show()
    }

    override fun onScannerStopped(scanner: ScannerLiveView) {
        Toast.makeText(context, "Scanner Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onScannerError(err: Throwable) {
        Toast.makeText(context, "Scanner Error: " + err.message, Toast.LENGTH_SHORT).show()
    }

    abstract override fun onCodeScanned(data: String)
}