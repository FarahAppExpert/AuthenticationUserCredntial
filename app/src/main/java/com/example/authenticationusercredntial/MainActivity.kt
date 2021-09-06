package com.example.authenticationusercredntial

import android.app.KeyguardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*

class MainActivity : AppCompatActivity() {
    companion object
    {
        private var Key_name : String = "my_key"
        private var Secret_Byte = byteArrayOf(1, 2, 3, 4, 5, 6)
        private var Reqest_Code : Int = 1
        private var AuthenticationDuration : Int = 30
        private lateinit var keyguardManager : KeyguardManager

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        var authentication_button : Button = findViewById<View>(R.id.authentication_button) as Button

        if(!keyguardManager.isKeyguardSecure)
        {
            Toast.makeText(this, "Secure lock screen hasn't set up.\n"
                    + "Go to 'Settings -> Security -> Screenlock' to set up a lock screen", Toast.LENGTH_LONG).show()
            authentication_button.setEnabled(false)
            return
        }

        CreateKey()
        findViewById<View>(R.id.authentication_button).setOnClickListener(object : View.OnClickListener
        {
            override fun onClick(v: View?) {
               EncryptedAuthentication()
            }

        })
    }

    private fun EncryptedAuthentication() : Boolean
    {
       try
       {
           val keyStore : KeyStore = KeyStore.getInstance("AndroidKeyStore")
           keyStore.load(null)
           val secretKey =  keyStore.getKey(Key_name, null)
           val cipher : Cipher = Cipher.getInstance(
               KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
           cipher.init(Cipher.ENCRYPT_MODE, secretKey)
           cipher.doFinal(Secret_Byte)

           showAuthentication()
           return true
       }
       catch (exception : UserNotAuthenticatedException)
       {
           showAuthentication()
           return false
       }
        catch (exception : KeyPermanentlyInvalidatedException)
        {
            Toast.makeText(this, "Keys are invalidated after created. Retry the purchase\n"
                    + exception.message, Toast.LENGTH_LONG).show()
            return false
        }
        catch (exception : BadPaddingException)
        {
            throw RuntimeException(exception)
        }
        catch (exception : IllegalBlockSizeException)
        {
            throw RuntimeException(exception)
        }
        catch (exception : KeyStoreException)
        {
            throw RuntimeException(exception)
        }
        catch (exception : CertificateException)
        {
            throw RuntimeException(exception)
        }
        catch (exception : UnrecoverableKeyException)
        {
            throw RuntimeException(exception)
        }
        catch (exception : IOException)
        {
            throw RuntimeException(exception)
        }
       catch (exception : NoSuchPaddingException)
       {
           throw RuntimeException(exception)
       }
        catch (exception : NoSuchAlgorithmException)
        {
            throw RuntimeException(exception)
        }
        catch (exception : InvalidKeyException)
        {
            throw RuntimeException(exception)
        }
    }



    private fun CreateKey()
    {
        try {
            var keyStore : KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            var keyGenerator : KeyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    Key_name,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(AuthenticationDuration)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())
            keyGenerator.generateKey();
        }
        catch ( exception : NoSuchAlgorithmException) {
        throw RuntimeException("Failed to create a symmetric key", exception)
        }
        catch (exception : NoSuchProviderException)
        {
            throw RuntimeException("Failed to create a symmetric key", exception)
        }
        catch (exception : InvalidAlgorithmParameterException)
        {
            throw RuntimeException("Failed to create a symmetric key", exception)
        }
        catch (exception : KeyStoreException)
        {
            throw RuntimeException("Failed to create a symmetric key", exception)
        }
        catch (exception : CertificateException)
        {
            throw RuntimeException("Failed to create a symmetric key", exception)
        }
        catch (exception : IOException)
        {
            throw RuntimeException("Failed to create a symmetric key", exception)
        }
    }

    private fun showAuthentication() {

        val intent: Intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null)
        if (intent !== null)
        {
            startActivityForResult(intent, Reqest_Code)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Reqest_Code)
        {
            if (resultCode == RESULT_OK)
            {
                if (EncryptedAuthentication())
                {
                    showConfimration()
                    val intent : Intent =  Intent(this, Successful_Authentication :: class.java);
                    startActivity(intent);
                }

            }
            else
            {
              Toast.makeText(this, "Authentication failed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfimration() {
       findViewById<View>(R.id.confirmation).visibility
       findViewById<View>(R.id.authentication_button).isEnabled
    }

    private fun alreadyAuthenticated ()
    {
        var textView : TextView = findViewById(R.id.already_authenticated)
        textView.visibility
        textView.setText(resources.getQuantityString(
            R.plurals.already_authenticated, AuthenticationDuration, AuthenticationDuration))
        findViewById<View>(R.id.authentication_button).isEnabled

    }
}