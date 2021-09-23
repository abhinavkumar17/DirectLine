package com.example.directlineapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.directline_chatbot_sdk.bo.Button
import com.example.directline_chatbot_sdk.bo.DirectLineChatbot
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chatbot = DirectLineChatbot("0yBj8sJVI-M.I5KNG4i8azR9TSnxKD7XPoX0JrKPV1QcyuN9qmINUCo")
        // chatbot.debug(true)
        //chatbot.user("David Fournier")


        chatbot.user = "Abhinav"
        chatbot.debug = true

        /* chatbot.start(callback = object : DirectLineChatbot.Callback
        {
            override fun onStarted()
            {

                Log.d("TAG", "onStarted: ")
                chatbot.send("safe zone")
            }

            override fun onMessageReceived(message: String, buttonList: List<Button>)
            {
                Log.d("CHATBOT", message)
            }

            override fun onError(ex: Exception?){
                Log.d("CHATBOT", ex.toString())
            }
        })
    }*/
    }

}
