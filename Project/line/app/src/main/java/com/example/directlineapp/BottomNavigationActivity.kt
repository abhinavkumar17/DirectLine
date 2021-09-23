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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBootomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationActions()

        setUpOnClickListeners()

        Log.d("Fragment Attached", "onCreate:${callingActivity?.className}")
    }



    private fun setupNavigationActions() {


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_bootom_navigation)

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
       CustomBottomSheetDialogFragment.newInstance("What is sync up pets","Welcome").show(supportFragmentManager,"TAG")
    }



}