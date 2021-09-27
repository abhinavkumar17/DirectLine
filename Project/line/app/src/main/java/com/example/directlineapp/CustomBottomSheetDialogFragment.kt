package com.example.directlineapp

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.directline_chatbot_sdk.bo.DirectLineChatbot
import com.example.directline_chatbot_sdk.bo.MessageReceivedNew
import kotlinx.android.synthetic.main.bot_message_box.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.directline_chatbot_sdk.bo.Button as Button1
import android.speech.tts.Voice





class CustomBottomSheetDialogFragment : DialogFragment(), DialogInterface.OnDismissListener {

    private val TAG = "/ testCustomBottomSheetDialogFragment"
    val chatbot =
        DirectLineChatbot("hSNvCahpROY.hQTSFq26wnc31Oj4i6h4SrpRxCJq65g46Nf71eu8z1Q")
    lateinit var tts: TextToSpeech

    companion object {
        const val TAG = "SimpleDialog"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_SUBTITLE = "KEY_SUBTITLE"
        private var frameLayout: FrameLayout? = null
        private val USER = 0
        private val BOT = 1
        val date = Date(System.currentTimeMillis())
        fun newInstance(title: String, subTitle: String): CustomBottomSheetDialogFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_SUBTITLE, subTitle)
            val fragment = CustomBottomSheetDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
        setupClickListeners(view)
        setupViewAnimations()
        setupTextWatcher(view)
        tts = TextToSpeech(requireContext()) {}
    }

    private fun setupTextWatcher(view: View) {
        view.edittext_chatbox.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(cs: CharSequence, s: Int, b: Int, c: Int) {
                if (tts.isSpeaking) {
                    tts.stop()
                }
                Log.i("Key:", cs.toString())
            }

            override fun afterTextChanged(editable: Editable) {
            }

            override fun beforeTextChanged(cs: CharSequence, i: Int, j: Int, k: Int) {}
        })
    }

    private fun setupViewAnimations() {
        wave_three.addDefaultWaves(
            2,
            1
        ) // or call WaveView#addWaveData to add wave data as you like
        wave_three.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        /*   dialog?.window?.setLayout(
               1000,
               1500
           )*/
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }
        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)
        dialog!!.setCancelable(false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupView(view: View) {
        view.textView_Title.text = arguments?.getString(KEY_TITLE)
        view.textView_SubTitle.text = arguments?.getString(KEY_SUBTITLE)
        if (dialog?.isShowing == false) {
            initChat(arguments?.getString(KEY_TITLE), date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupClickListeners(view: View) {
        view.send_button.setOnClickListener {
            if (send_button.isEnabled && send_button_text.isEnabled) {
                if (tts.isSpeaking) {
                    tts.stop()
                    startRecording()
                } else {
                    startRecording()
                }

            }
        }
        send_button_text.setOnClickListener {
            if (send_button.isEnabled && send_button_text.isEnabled) {
                if (tts.isSpeaking) {
                    tts.stop()
                    sendTextMessage(edittext_chatbox.text.toString())
                } else {
                    sendTextMessage(edittext_chatbox.text.toString())
                }
            }
        }
        view.imageView_arrow_up.setOnClickListener {
            if (view.imageView_arrow_up.isEnabled) {
                if (tts.isSpeaking) {
                    tts.stop()
                    dismiss()
                } else {
                    dismiss()
                }
            }
        }
        view.chatScrollView.post { view.chatScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun startRecording() {
        checkAudioPermission()
        // changing the color of mic icon, which
        // indicates that it is currently listening
        view?.send_button?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_stop_24
            )
        )
        view?.send_button?.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.mic_enabled_color
            )
        )
        wave_three.visibility = View.VISIBLE
        wave_three.startAnimation()
        startSpeechToText()
    }


    private fun checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // M = 23
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    "android.permission.RECORD_AUDIO"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:com.example.directlineapp")
                )
                startActivity(intent)
                Toast.makeText(requireContext(), "Allow Microphone Permission", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initChat(msg: String?, date: Date) {
        sendTextMessage(userMessage = msg.toString())
    }

    private fun sendTextMessage(userMessage: String) {

        chatbot.user = "Abhinav"
        chatbot.debug = true
        chatbot.start(callback = object : DirectLineChatbot.Callback {
            override fun onStarted() {
                Log.d("CHATBOT", "onStarted: ")

                if (userMessage.trim().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter your query",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    chatbot.send(userMessage)
                    val message = UserMessage()
                    message.UserMessage("User", userMessage)
                    activity?.runOnUiThread(Runnable {
                        //on main thread
                        showTextView(userMessage, USER, date.toString())
                    })

                }
            }

            override fun onMessageReceived(messageReceived: MessageReceivedNew) {
                val message = messageReceived.activities[0].text
                var buttonList:List<Button1> = listOf()
               if(messageReceived.activities[0].attachments !=null && messageReceived.activities[0].attachments.isNotEmpty())
               {
                    buttonList = messageReceived.activities[0].attachments[0].content.buttons
               }

                if (message.isNotEmpty()) {
                    if (tts.isSpeaking) {
                        tts.stop()
                        val botMessage = "Sorry didn't understand"
                        addBotMessage(botMessage, buttonList)
                    } else {
                        addBotMessage(message, buttonList)
                    }
                }
            }

            override fun onError(ex: Exception?) {
                Log.d("CHATBOT Error", ex.toString())
                activity?.runOnUiThread(Runnable {
                    //on main thread
                    Toast.makeText(
                        requireContext(),
                        "Please enter your query",
                        Toast.LENGTH_SHORT
                    ).show()
                })

            }
        })

    }

    private fun updateButtonList(buttonList: List<Button1>) {
        // creating the button
        for (i in buttonList) {

            val myButton = Button(requireContext())
            myButton.text = i.title
            frameLayout = getBotLayout()

            //val ll = view?.findViewById<View>(R.id.bu) as LinearLayout
            /*    val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )*/

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            lp.bottomMargin = 15
            lp.leftMargin = 5
            lp.rightMargin = 5
            lp.topMargin = 5
            lp.gravity = View.TEXT_ALIGNMENT_CENTER
            myButton.setBackgroundColor(Color.LTGRAY)
            myButton.setTextColor(Color.BLACK)
            myButton.background = resources.getDrawable(R.drawable.button_background, null)
            myButton.setOnClickListener {
                /*Toast.makeText(requireContext(), "you selected ${i.title}", Toast.LENGTH_LONG)
                    .show()*/
                sendTextMessage(userMessage = i.title)
                edittext_chatbox.text = i.title.toEditable()
            }
            button_layout?.addView(myButton, lp)


        }

    }

    private fun sendVoiceMessage(userMessage: String) {
        chatbot.user = "Abhinav"
        chatbot.debug = true
        chatbot.start(callback = object : DirectLineChatbot.Callback {
            override fun onStarted() {
                Log.d("CHATBOT", "onStarted: ")
                chatbot.send(userMessage)
            }

            override fun onMessageReceived(messageReceived: MessageReceivedNew) {

                val message = messageReceived.activities[0].text

                var buttonList:List<Button1> = listOf()

                if(messageReceived.activities[0].attachments !=null && messageReceived.activities[0].attachments.isNotEmpty())
                {
                    buttonList = messageReceived.activities[0].attachments[0].content.buttons
                }

                if (message.isNotEmpty()) {
                    if (tts.isSpeaking) {
                        tts.stop()
                        val botMessage = "Sorry didn't understand"
                        startTextToSpeech(botMessage,buttonList)
                        edittext_chatbox.text = "".toEditable()
                    } else {
                        startTextToSpeech(message, buttonList)
                        edittext_chatbox.text = "".toEditable()
                    }
                } else {
                    startTextToSpeech(message, buttonList)
                    edittext_chatbox.text = "".toEditable()
                }
            }

            override fun onError(ex: Exception?) {
                var buttonList:List<Button1> = listOf()
                Log.d("CHATBOT Error", ex.toString())
                val botMessage = "Check your network connection"
                showTextView(botMessage, BOT, date.toString())
                startTextToSpeech(botMessage, buttonList)
            }
        })

    }

    private fun addBotMessage(
        botMessage: String,
        buttonList: List<com.example.directline_chatbot_sdk.bo.Button>
    ) {
        if (botMessage == "Hello and welcome!") {
            // tts.speak("", TextToSpeech.QUEUE_ADD, null, "")
        } else {
            activity?.runOnUiThread(Runnable {

                showTextView(botMessage, BOT, date.toString())
                if (buttonList.isNotEmpty()) {
                    updateButtonList(buttonList)
                }
                else{
                    Log.d(TAG, "addBotMessage: list empty")
                }
            })
            edittext_chatbox.text = "".toEditable()

        }
    }

    private fun startTextToSpeech(
        message: String,
        buttonList: List<com.example.directline_chatbot_sdk.bo.Button>
    ) {
        wave_three.visibility = View.GONE
        tts = TextToSpeech(requireContext()) {
            if (it == TextToSpeech.SUCCESS) {
                val voiceobj = Voice(
                    "en-us-x-sfg male_2-local", Locale.US, 1, 1, true,null)
                tts.voice = voiceobj
                tts.language = Locale.US
                tts.voice
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
                activity?.runOnUiThread {
                    //on main thread
                    if (buttonList.isNotEmpty()) {
                        updateButtonList(buttonList)
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    startRecording()
                    Toast.makeText(requireContext(), "Finished speaking.", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onError(p0: String?) {
            }
        })

    }

    private fun startSpeechToText() {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle?) {
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray?) {}
            override fun onEndOfSpeech() {
                // changing the color of our mic icon to
                // gray to indicate it is not listening
                view?.send_button?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_mic
                    )
                )
                view?.send_button?.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mic_enabled_color
                    )
                ) // #FF6D6A6A
                wave_three.visibility = View.GONE
            }

            override fun onError(i: Int) {}

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResults(bundle: Bundle) {
                val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (result != null) {

                    if (result[0].toString().contains("close")) {
                        view?.chat_layout?.removeAllViews()
                        dismiss()
                    } else {
                        sendVoiceMessage(result[0].toString())
                    }

                }
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle?) {
            }
        })
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun showTextView(message: String, type: Int, date: String) {
        frameLayout = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout()
            }
            else -> {
                getBotLayout()
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        view?.chat_layout?.addView(frameLayout)
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_msg)
        messageTextView?.text = message
        frameLayout?.requestFocus()
        view?.edittext_chatbox?.text = "".toEditable()
        val currentDateTime = Date(System.currentTimeMillis())
        val dateNew = Date(date)
        val dateFormat = SimpleDateFormat("dd-MM-yy", Locale.ENGLISH)
        val currentDate = dateFormat.format(currentDateTime)
        val providedDate = dateFormat.format(dateNew)
        var time = ""
        if (currentDate.equals(providedDate)) {
            val timeFormat = SimpleDateFormat(
                "hh:mm aa",
                Locale.ENGLISH
            )
            time = timeFormat.format(dateNew)
        } else {
            val dateTimeFormat = SimpleDateFormat(
                "dd-MM-yy hh:mm aa",
                Locale.ENGLISH
            )
            time = dateTimeFormat.format(dateNew)
        }
        val timeTextView = frameLayout?.findViewById<TextView>(R.id.message_time)
        timeTextView?.text = time

    }

    private fun getUserLayout(): FrameLayout? {
        val inflater: LayoutInflater = LayoutInflater.from(requireContext())
        return inflater.inflate(R.layout.user_message_box, null) as FrameLayout?
    }

    private fun getBotLayout(): FrameLayout? {
        val inflater: LayoutInflater = LayoutInflater.from(requireContext())
        view?.send_button?.isEnabled = true
        view?.send_button_text?.isEnabled = true
        view?.imageView_arrow_up?.visibility = View.VISIBLE
        view?.imageView_arrow_up?.isEnabled = true
        return inflater.inflate(R.layout.bot_message_box, null) as FrameLayout?
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onPause() {
        super.onPause()
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        view?.chat_layout?.removeAllViews()
    }

}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)