package com.gmits.twowayconnection

import android.annotation.SuppressLint
import android.util.Log

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

import android.content.ContentValues.TAG
import de.greenrobot.event.EventBus


@SuppressLint("Registered")
class WearListCallListenerService : WearableListenerService() {

    companion object {
        lateinit var msg: String
    }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        super.onMessageReceived(messageEvent)
        msg = String(messageEvent!!.data)
        Log.d(TAG, "Message received in mobile <= from path:" + messageEvent.path + "& data: " + msg)
        EventBus.getDefault().post(StatusModelMobile(msg))
    }

}
