package com.example.directlineapp

import android.Manifest
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
    val chatbot =
        DirectLineChatbot("hSNvCahpROY.hQTSFq26wnc31Oj4i6h4SrpRxCJq65g46Nf71eu8z1Q")

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
        setupViewAnimations()

        tts = TextToSpeech(applicationContext) {}
        Log.d("Fragment Attached", "onCreate:${callingActivity?.className}")
    }

    private fun setupViewAnimations() {

        binding.waveThree.addDefaultWaves(
            1,
            1
        ) // or call WaveView#addWaveData to add wave data as you like
        binding.waveThree.visibility = View.GONE

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

        if (tts.isSpeaking) {
            tts.stop()
            startVoiceRecording(uiOutput)

        } else {
            startVoiceRecording(uiOutput)
        }
    }

    private fun startVoiceRecording(uiOutput: SpeechRecognizerViewModel.ViewState?) {
        if (uiOutput != null) {
            if (uiOutput.isListening) {
                binding.waveThree.visibility = View.VISIBLE
                binding.waveThree.startAnimation()
                binding.floatingActionButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_stop_24
                    )
                )
            } else {
                binding.waveThree.visibility = View.GONE
                binding.floatingActionButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        android.R.drawable.ic_btn_speak_now
                    )
                )



                binding.editTextVoiceInput.text = uiOutput.spokenText
                sendMessage(binding.editTextVoiceInput.text as String)
                Log.d(TAG, "CHATBOT msg sent: ${uiOutput.spokenText}")
                uiOutput.spokenText = ""

            }
        }
    }


    private fun sendMessage(userMessage: String) {
        if (userMessage.isNotBlank() || userMessage.isNotEmpty() || userMessage != "") {
            chatbot.user = "Abhinav"
            chatbot.debug = true

            chatbot.start(callback = object : DirectLineChatbot.Callback {
                override fun onStarted() {

                    Log.d("CHATBOT", "onStarted: ")
                    chatbot.send(userMessage)
                }

                override fun onMessageReceived(message: String) {
                    binding.waveThree.visibility = View.GONE
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
    }

    private fun startTextToSpeech(message: String) {
        binding.waveThree.visibility = View.GONE
        tts = TextToSpeech(applicationContext) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setPitch(1.5F)
                if (message == "Hello and welcome!") {
                   // tts.speak("", TextToSpeech.QUEUE_ADD, null, "")

                } else {
                    tts.speak(message, TextToSpeech.QUEUE_ADD, null, "")

                }

            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(p0: String?) {
            }

            override fun onDone(p0: String?) {
                Log.d(TAG, "onDone:$p0")
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