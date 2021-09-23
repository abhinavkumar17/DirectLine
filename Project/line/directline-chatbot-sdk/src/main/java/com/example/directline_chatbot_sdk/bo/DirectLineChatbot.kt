package com.example.directline_chatbot_sdk.bo

import android.util.Log
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.net.URI

class DirectLineChatbot(val secret: String)
{

    interface Callback
    {

        /**
         * Gets called when a successful connection has been made with the chatbot
         */
        fun onStarted()

        /**
         * Gets called every time a message *from the bot* has been received
         * @message: the text message from the chatbot
         */
        //fun onMessageReceived(message: String, buttonList: List<Button>?)
        fun onMessageReceived(messageReceived: MessageReceivedNew)

        fun onError(ex: Exception?)
    }

    companion object
    {
        private const val TAG = "WEB SOCKET"

        private val GSON = Gson()
    }

    /**
     * The user name as sent to the chatbot
     */
    var user: String = "Me"

    /**
     * If turned to true, will display verbose logs
     */
    var debug: Boolean = false

    private var webSocket: WebSocketClient? = null

    private var conversationId: String? = null

    private var callback: Callback? = null

    private var header: String = "Bearer ${secret}"

    private var id = Id(user)

    private var started = false

    /**
     * Sends asynchronously a text message to the chatbot.
     */
    fun send(message: String)
    {
        conversationId?.let {
            val messageObj = Message("message", id, message)
            WebService.api.send(messageObj, it, header).enqueue(object : retrofit2.Callback<Id>
            {
                override fun onResponse(call: Call<Id>?, response: Response<Id>?)
                {
                    response?.body()?.let { _ ->
                        log("MESSAGE \"${message}\" SENT SUCCESSFULLY")
                    }
                }

                override fun onFailure(call: Call<Id>?, t: Throwable?)
                {
                    t?.printStackTrace()
                }
            })
        }
            ?: if (started)
                throw IllegalStateException("The DirectLineChatbot has not finished its initialization yet. Please wait for onStarted() to be triggered.")
            else
                throw IllegalStateException("The DirectLineChatbot must be initialized first. Call start().")
    }

    /**
     * Starts asynchronously a WebSocket connection with the Microsoft Web App Bot.
     * When started, the onStarted() method from the @callback will be called.
     */
    fun start(callback: Callback)
    {
        this.callback = callback
        this.started = true
        WebService.api.startConversation(header).enqueue(object : retrofit2.Callback<StartConversation>
        {
            override fun onResponse(call: Call<StartConversation>?, response: Response<StartConversation>?)
            {
                response?.body()?.let { body ->
                    body.streamUrl.let { streamUrl ->
                        log(streamUrl)
                        startWebSocket(streamUrl)
                    }
                    conversationId = body.conversationId
                }
            }

            override fun onFailure(call: Call<StartConversation>?, t: Throwable?)
            {
                t?.printStackTrace()
            }
        })
    }

    /**
     * Closes the WebSocket of the Web App Bot.
     * Any further call to send() method will throw an exception
     */
    fun stop()
    {
        conversationId = null;
        webSocket?.close();
    }

    private fun log(log: String)
    {
        if (debug)
        {
            Log.d(TAG, log)
        }
    }

    private fun startWebSocket(streamUrl: String)
    {
        webSocket = object : WebSocketClient(URI.create(streamUrl))
        {
            override fun onOpen(handshakedata: ServerHandshake?)
            {
                log("OPEN")
                callback?.onStarted()
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean)
            {
                log("CLOSE")
            }

            override fun onMessage(message: String?)
            {
                log("MESSAGE RECEIVED : $message")
                val messageReceived = GSON.fromJson(message, MessageReceivedNew::class.java)

                messageReceived?.watermark?.let {
                    //val buttonList = messageReceived.activities[0].attachments[0].content.buttons
                   // callback?.onMessageReceived(messageReceived.activities[0].text,buttonList)
                   callback?.onMessageReceived(messageReceived)
                }
            }

            override fun onError(ex: Exception?)
            {
                ex?.message?.let { Log.e(TAG, it) }
                callback?.onError(ex)
            }
        }
        webSocket?.connect()//  for difference between web socket and http refer https://developerinsider.co/difference-between-http-and-http-2-0-websocket/
    }
}