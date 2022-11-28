package com.koenig.accountmanager.crypto

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@RequiresApi(Build.VERSION_CODES.M)
class Encryption {

    fun encryptedKey (KEY_NAME: String, value: String, context: Context): String
    {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        // INITIALIZE CIPHER
        val  cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val iv = cipher.iv
        val encryption = cipher.doFinal(value.toByteArray())

        // SAVE THE KEY TEMPORARILY
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putString(KEY_NAME+"EN", Base64.encodeToString(encryption,Base64.DEFAULT))
        editor.putString(KEY_NAME+"IV", Base64.encodeToString(iv,Base64.DEFAULT))
        editor.apply()

        // RETURN THE ENCRYPTED STRING
        return Base64.encodeToString(encryption, Base64.NO_WRAP)
    }


    fun decryptKey (KEY_NAME: String, context: Context): String?{

        val sharedPrefs: SharedPreferences = context.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        // INITIALIZE CIPHER
        val cipher2 = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128,Base64.decode(sharedPrefs.getString(KEY_NAME+"IV",null),Base64.DEFAULT))
        cipher2.init(Cipher.DECRYPT_MODE, getKey(), spec)

        // DECODE THE INPUT
        val decodedData = cipher2.doFinal(Base64.decode(sharedPrefs.getString(KEY_NAME+"EN",null),Base64.DEFAULT))
        val unencryptedString = String(decodedData)

        // RETURN THE DECODED STRING
        return unencryptedString
    }


    // CREATE THE ANDROID KEYSTORE
    private fun createKeyStore(): KeyStore
    {
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore
    }


    // GET THE SECRET KEY
    fun getKey(): SecretKey
    {
        val keyStore = createKeyStore()

        if(!isKeyExists(keyStore))
        {
            createKey()
        }

        return keyStore.getKey("alies", null) as SecretKey
    }


    // CREATE THE SECRET KEY, IF NOT ALREADY DONE
    fun createKey(): SecretKey
    {
        val keyGenerator : KeyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore")

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "alies",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()


        keyGenerator.init(keyGenParameterSpec);
        val  secretKey: SecretKey = keyGenerator.generateKey()

        return secretKey
    }


    // CHECK WHETHER KEY ALREADY EXISTS OR NOT
    private fun isKeyExists(keyStore: KeyStore): Boolean
    {
        val aliases = keyStore.aliases()
        return "alies" == aliases.nextElement()
    }
}