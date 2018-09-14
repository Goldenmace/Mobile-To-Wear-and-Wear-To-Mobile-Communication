package com.gmits.twowayconnection

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.WatchViewStub
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import de.greenrobot.event.EventBus
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivity : WearableActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    lateinit var ivPlay: ImageView
    lateinit var lottieAnimationView: LottieAnimationView
    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        setAmbientEnabled()

        val stub = findViewById<WatchViewStub>(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener {
            ivPlay = findViewById(R.id.ivPlay)
            lottieAnimationView = findViewById(R.id.lottieAnimationView)
            ivPlay.tag = "PLAY"

            ivPlay.setOnClickListener {
                if (ivPlay.tag == "PLAY") {
                    ivPlay.tag = "PAUSE"
                    ivPlay.setImageResource(R.drawable.play)
                    lottieAnimationView.pauseAnimation()
                } else {
                    ivPlay.tag = "PLAY"
                    ivPlay.setImageResource(R.drawable.pause)
                    lottieAnimationView.playAnimation()

                }

                resolveNode(ivPlay.tag as String)

            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
        EventBus.getDefault().register(this)
    }

    override fun onConnected(p0: Bundle?) {
    }

    private fun resolveNode(tag: String) {
        StartWearableActivityTask(tag).execute()
    }


    @SuppressLint("StaticFieldLeak")
    inner class StartWearableActivityTask internal constructor(internal var text: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg args: Void): Boolean? {

            val flag: Boolean
            val nodes = getAllNodes()
            if (nodes.isEmpty()) {
                flag = false
            } else {
                flag = true
                for (node in nodes) {
                    sendStringMessageToWear(node, text)
                }
            }
            return flag
        }

    }

    fun getAllNodes(): Collection<String> {

        val results = HashSet<String>()
        val nodeListTask = Wearable.getNodeClient(applicationContext).connectedNodes
        try {
            val nodes = Tasks.await(nodeListTask)

            for (node in nodes) {
                results.add(node.id)
            }
        } catch (exception: ExecutionException) {

        } catch (exception: InterruptedException) {
        }

        return results
    }

    fun sendStringMessageToWear(node: String, message: String) {

        val sendMessageTask = Wearable
                .getMessageClient(applicationContext)
                .sendMessage(node, "/send-to-mobile", message.toByteArray())
        try {
            val result = Tasks.await(sendMessageTask)
        } catch (exception: ExecutionException) {

        } catch (exception: InterruptedException) {
        }

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun onEvent(event: StatusModelWear) {
        runOnUiThread {
            if (event.status == "PLAY") {
                ivPlay.tag = "PAUSE"
                ivPlay.setImageResource(R.drawable.play)
                lottieAnimationView.pauseAnimation()
            } else {
                ivPlay.tag = "PLAY"
                ivPlay.setImageResource(R.drawable.pause)
                lottieAnimationView.playAnimation()

            }
        }
    }
}
