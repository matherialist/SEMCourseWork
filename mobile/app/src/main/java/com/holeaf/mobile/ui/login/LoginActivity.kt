package com.holeaf.mobile.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.holeaf.mobile.R
import org.koin.androidx.fragment.android.replace
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.core.KoinExperimentalAPI

class LoginActivity : AppCompatActivity() {
    @KoinExperimentalAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupKoinFragmentFactory()
        setContentView(R.layout.activity_login)

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace<LoginFragment>(
                R.id.fragment_container,
            ).commit()
        }
    }

}
