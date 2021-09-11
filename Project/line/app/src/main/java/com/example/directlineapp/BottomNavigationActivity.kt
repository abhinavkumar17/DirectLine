package com.example.directlineapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.directline_chatbot_sdk.bo.DirectLineChatbot
import com.example.directlineapp.databinding.ActivityBootomNavigationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBootomNavigationBinding

    private lateinit var speechRecognizerViewModel: SpeechRecognizerViewModel
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val TAG = "BottomNavigationActivity"

    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    lateinit var tts: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBootomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            setupSpeechViewModel()

        }
        setupNavigationActions()

        setUpOnClickListeners()

        tts = TextToSpeech(applicationContext) {}
        Log.d("Fragment Attached", "onCreate:${callingActivity?.className}")
    }

    private fun setupSpeechViewModel() {
        speechRecognizerViewModel =
            ViewModelProvider(this).get(SpeechRecognizerViewModel::class.java)
        speechRecognizerViewModel.getViewState().observe(
            this,
            androidx.lifecycle.Observer { viewState ->
                render(viewState)
            })

    }


    private fun setupNavigationActions() {


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_bootom_navigation)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


    private fun setUpOnClickListeners() {
        binding.floatingActionButton.apply {
            setOnClickListener(micClickListener)
        }


    }

    private val micClickListener = View.OnClickListener {
        if (!speechRecognizerViewModel.permissionToRecordAudio) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return@OnClickListener
        }

        if (speechRecognizerViewModel.isListening) {
            speechRecognizerViewModel.stopListening()
        } else {
            speechRecognizerViewModel.startListening()
        }
    }


    private fun getCurrentFragment(): String? {
        return supportFragmentManager.fragments.last().tag
    }

    private fun render(uiOutput: SpeechRecognizerViewModel.ViewState?) {
        if (uiOutput == null) return

        if (uiOutput.isListening) {
            binding.editTextVoiceInput.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.editTextVoiceInput.visibility = View.VISIBLE
                    }
                })
            binding.floatingActionButton.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_baseline_stop_24
                )
            )
        } else {
            binding.floatingActionButton.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    android.R.drawable.ic_btn_speak_now
                )
            )
            if (binding.editTextVoiceInput.text != "") {
                Log.d(TAG, "msg sent: ${binding.editTextVoiceInput.text as String}")
                sendMessage(binding.editTextVoiceInput.text as String)
                binding.editTextVoiceInput.text = ""
                uiOutput.spokenText = ""
            } else {
                if (uiOutput.spokenText != ("")) {
                    Log.d(TAG, "msg speak: ${uiOutput.spokenText}")
                    binding.editTextVoiceInput.text = uiOutput.spokenText
                }
            }
        }

    }


    private fun sendMessage(userMessage: String) {
        if (userMessage.isNotBlank() || userMessage.isNotEmpty() || userMessage != "") {
            val chatbot =
                DirectLineChatbot("0yBj8sJVI-M.I5KNG4i8azR9TSnxKD7XPoX0JrKPV1QcyuN9qmINUCo")


            chatbot.user = "Abhinav"
            chatbot.debug = true

            if (userMessage != "") {
                chatbot.start(callback = object : DirectLineChatbot.Callback {
                    override fun onStarted() {

                        Log.d("TAG", "onStarted: ")
                        chatbot.send(userMessage)
                    }

                    override fun onMessageReceived(message: String) {
                        Log.d("CHATBOT", message)
                        if (tts.isSpeaking) {
                            tts.stop()
                            startTextToSpeech(message)
                            binding.editTextVoiceInput.text = ""
                        } else {
                            startTextToSpeech(message)
                            binding.editTextVoiceInput.text = ""
                        }

                    }

                    override fun onError(ex: Exception?) {
                        Log.d("CHATBOT Error", ex.toString())
                    }
                })


            }
            /*  speechRecognizerViewModel.getBotResponseFromRepository(userMessage)!!
                  .observeOnce(this) { botResponse ->
                      Log.d(TAG, "msg response: $botResponse")


                  }*/

        }
    }

    private fun startTextToSpeech(message: String) {
        tts = TextToSpeech(applicationContext) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setPitch(1.5F)
                tts.speak(message, TextToSpeech.QUEUE_ADD, null, "")

            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(p0: String?) {
            }

            override fun onDone(p0: String?) {
                Handler(Looper.getMainLooper()).post {
                    if (!tts.isSpeaking && !speechRecognizerViewModel.isListening) {
                        speechRecognizerViewModel.startListening()
                    } else {
                        tts.stop()
                        speechRecognizerViewModel.startListening()
                    }
                }
            }

            override fun onError(p0: String?) {
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            speechRecognizerViewModel.permissionToRecordAudio =
                grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

        if (speechRecognizerViewModel.permissionToRecordAudio) {
            binding.floatingActionButton.performClick()
        }
    }

}


private fun <T> LiveData<T>.observeOnce(
    bottomNavigationActivity: BottomNavigationActivity,
    observer: Observer<T>
) {
    observe(bottomNavigationActivity, object : Observer<T> {
        override fun onChanged(t: T) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}