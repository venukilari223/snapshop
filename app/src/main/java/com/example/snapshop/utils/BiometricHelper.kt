package com.example.snapshop.utils
import android.content.Context

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    /**
     * Show the biometric authentication prompt.
     *
     * @param onSuccess Callback when authentication succeeds.
     * @param onError Callback when an error or failure occurs.
     * @param onNotAvailable Callback when biometric authentication is not available.
     */
    fun showAuthenticationPrompt(
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onNotAvailable: () -> Unit
    ) {
        if (!isAuthenticationAvailable(activity)) {
            onNotAvailable()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL) {
                        onNotAvailable()
                    } else {
                        onError(errorCode, errString)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError(-1, "Authentication failed. Please try again.")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Your Identity")
            .setSubtitle("Authentication required to place order")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Check if biometric authentication is available and ready to use.
     *
     * @param context Application or activity context.
     * @return `true` if authentication is available, `false` otherwise.
     */
    fun isAuthenticationAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
}
