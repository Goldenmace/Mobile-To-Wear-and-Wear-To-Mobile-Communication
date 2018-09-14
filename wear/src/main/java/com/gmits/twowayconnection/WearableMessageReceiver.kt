package com.gmits.twowayconnection

import android.annotation.SuppressLint
import android.util.Log

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

import de.greenrobot.event.EventBus

import android.content.ContentValues.TAG

@SuppressLint("Registered")
class WearableMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        super.onMessageReceived(messageEvent)

        if (messageEvent!!.path.equals("/send-to-wear", ignoreCase = true)) {
            val s = String(messageEvent.data)
            Log.d(TAG, "Message received " + messageEvent.path + "& data: " + s)
            EventBus.getDefault().post(StatusModelWear(s))
        }
    }

}