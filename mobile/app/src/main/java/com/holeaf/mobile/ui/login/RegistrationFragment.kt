package com.holeaf.mobile.ui.registration

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.holeaf.mobile.R
import com.holeaf.mobile.ui.MainActivity
import com.holeaf.mobile.ui.login.LoggedInUserView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrationFragment : Fragment() {

    val registrationViewModel: RegistrationViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment RegistrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = RegistrationFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val password2EditText = view.findViewById<EditText>(R.id.password_confirm)
        val registrationButton = view.findViewById<Button>(R.id.register)
        val loadingProgressBar = view.findViewById<ProgressBar>(R.id.loading)

        registrationViewModel.registrationFormState.observe(viewLifecycleOwner,
                Observer { registrationFormState ->
                    if (registrationFormState == null) {
                        return@Observer
                    }
                    registrationButton.isEnabled = registrationFormState.isDataValid
                    registrationFormState.usernameError?.let {
                        usernameEditText.error = getString(it)
                    }
                    registrationFormState.passwordError?.let {
                        passwordEditText.error = getString(it)
                    }
                    registrationFormState.emailError?.let {
                        emailEditText.error = getString(it)
                    }
                    registrationFormState.password2Error?.let {
                        password2EditText.error = getString(it)
                    }
                })

        registrationViewModel.registrationResult.observe(viewLifecycleOwner,
                Observer { registrationResult ->
                    registrationResult ?: return@Observer
                    loadingProgressBar.visibility = View.GONE
                    registrationResult.error?.let {
                        showRegistrationFailed(it)
                    }
                    registrationResult.success?.let {
                        loginAfterRegister(it)
                    }
                })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                registrationViewModel.registrationDataChanged(
                        usernameEditText.text.toString(),
                        passwordEditText.text.toString(),
                        password2EditText.text.toString(),
                        emailEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        password2EditText.addTextChangedListener(afterTextChangedListener)
        emailEditText.addTextChangedListener(afterTextChangedListener)

        registrationButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            registrationViewModel.register(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    emailEditText.text.toString()
            )
        }
    }

    private fun loginAfterRegister(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName

        // TODO : initiate successful logged in experience
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)

        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showRegistrationFailed(errorString: Exception) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString.message, Toast.LENGTH_LONG).show()
    }
}
