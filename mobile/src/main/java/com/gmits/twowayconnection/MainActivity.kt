package com.gmits.twowayconnection

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import de.greenrobot.event.EventBus
import java.util.HashSet
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    lateinit var ivPlay: ImageView
    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        ivPlay = findViewById(R.id.ivPlay)
        ivPlay.tag == "PLAY"
        ivPlay.setOnClickListener {
            if (ivPlay.tag == "PLAY") {
                ivPlay.tag = "PAUSE"
                ivPlay.setImageResource(R.drawable.pause)
            } else {
                ivPlay.tag = "PLAY"
                ivPlay.setImageResource(R.drawable.play)
            }
            resolveNode(ivPlay.tag as String)
        }
    }

    fun onEvent(event: StatusModelMobile) {
        runOnUiThread {
            if (event.status == "PLAY") {
                ivPlay.tag = "PAUSE"
                ivPlay.setImageResource(R.drawable.pause)
            } else {
                ivPlay.tag = "PLAY"
                ivPlay.setImageResource(R.drawable.play)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
        EventBus.getDefault().register(this)
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
                .sendMessage(node, "/send-to-wear", message.toByteArray())
        try {
            val result = Tasks.await(sendMessageTask)
        } catch (exception: ExecutionException) {

        } catch (exception: InterruptedException) {
        }

    }



    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }
}
