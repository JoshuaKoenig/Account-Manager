package com.koenig.accountmanager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.koenig.accountmanager.crypto.Encryption

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    // THE MASTER TABLE URI
    var MASTER_CONTENT_URI = Uri.parse("content://${DBContentProvider.PROVIDER_NAME}/master")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)

        // WHEN NO MASTER PW DEFINED THEN CALL THE CREATE MASTER ACTIVITY
        if (!sharedPreferences.getBoolean(getString(R.string.has_master_pw_key), false))
        {
            val intent = Intent(this, CreateMasterActivity::class.java)
            startActivity(intent)
        }

        // THE LOGIN BUTTON
        val loginButton: Button = findViewById(R.id.buttonLogin)

        // THE PW INPUT
        val passwordInput: EditText = findViewById(R.id.textPassword)

        // VALIDATION OF THE PW
        loginButton.setOnClickListener {
            //validatePassword_Insecure(passwordInput.text.toString())
            validatePassword_Secure(passwordInput.text.toString())
        }
    }


    private fun validatePassword_Insecure(enteredPassword: String)
    {
        val selection = "masterPassword = '${enteredPassword}'"
        val cursor = contentResolver.query(MASTER_CONTENT_URI, null, selection, null, null, null)

        if (cursor!!.count > 0)
        {
            // CORRECT PW WAS ENTERED
            val intent = Intent(this, AccountDataActivity::class.java)
            startActivity(intent)
        }
        else
        {
            // WRONG PW WAS ENTERED
            Toast.makeText(this, "Wrong Password", Toast.LENGTH_LONG).show()
        }
    }


    private fun validatePassword_Secure(enteredPassword: String)
    {
        val encryption = Encryption()

        val cursor = contentResolver.query(MASTER_CONTENT_URI, null, null, null, null, null)
        var masterpw: String = ""

        with(cursor)
        {
            while (this!!.moveToNext())
            {
                masterpw = getString(getColumnIndexOrThrow(DBContentProvider.masterPassword))
            }
        }

        // DECRYPT THE MASTER PASSWORD
        val decryptedMasterPassword = encryption.decryptKey("MASTER_PASSWORD", this)

        if (enteredPassword == decryptedMasterPassword.toString())
        {
            // CORRECT PW WAS ENTERED
            val intent = Intent(this, AccountDataActivity::class.java)
            startActivity(intent)
        }
        else
        {
            // WRONG PW WAS ENTERED
            Toast.makeText(this, "Wrong Password", Toast.LENGTH_LONG).show()
        }
    }
}