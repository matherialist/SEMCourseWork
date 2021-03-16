package com.holeaf.mobile.ui.login

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
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.holeaf.mobile.R
import com.holeaf.mobile.ui.recovery.RecoveryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 * Use the [RecoveryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecoveryFragment : Fragment() {
    val recoveryViewModel: RecoveryViewModel  by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val recoveryButton = view.findViewById<Button>(R.id.recovery)
        val loadingProgressBar = view.findViewById<ProgressBar>(R.id.loading)

        recoveryViewModel.recoveryFormState.observe(viewLifecycleOwner,
            Observer { recoveryFormState ->
                if (recoveryFormState == null) {
                    return@Observer
                }
                recoveryButton.isEnabled = recoveryFormState.isDataValid
                recoveryFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
            })

        recoveryViewModel.recoveryResult.observe(viewLifecycleOwner,
            Observer { recoveryResult ->
                recoveryResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                recoveryResult.error?.let {
                    showRecoveryFailed(it)
                }
                recoveryResult.success?.let {
                    recoveryCompleted()
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
                recoveryViewModel.recoveryDataChanged(
                    emailEditText.text.toString()
                )
            }
        }
        emailEditText.addTextChangedListener(afterTextChangedListener)

        recoveryButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            recoveryViewModel.recovery(
                emailEditText.text.toString()
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recovery, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         */
        @JvmStatic
        fun newInstance() =
            RecoveryFragment()
    }

    private fun recoveryCompleted() {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, R.string.password_sent_to_email, Toast.LENGTH_LONG).show()
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun showRecoveryFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}