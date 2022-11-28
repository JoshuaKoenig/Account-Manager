package com.koenig.accountmanager

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.koenig.accountmanager.crypto.Encryption

class CreateMasterActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_master)

        // SET PASSWORD BUTTON
        val setMasterPwButton: Button = findViewById(R.id.buttonLogin)

        // PASSWORD INPUT
        val passwordInput: EditText = findViewById(R.id.textPassword)

        // SET THE MASTER PW WHEN INPUT FIELD IS NOT EMPTY
        setMasterPwButton.setOnClickListener {

            if (!passwordInput.text.isNullOrBlank())
            {
                setMasterPassword(passwordInput.text.toString())
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setMasterPassword(password: String)
    {
        val encryption = Encryption()

        // SHARED PREFERENCES
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // ENCRYPT THE MASTER PASSWORD
        val encryptedMasterPassword = encryption.encryptedKey("MASTER_PASSWORD", password ,this)

        // SET HAS MASTER PW
        editor.putBoolean(getString(R.string.has_master_pw_key), true)

        // SET MASTER PW
        editor.putString(getString(R.string.master_pw_key), encryptedMasterPassword)

        // APPLY CHANGES
        editor.apply()
        
        // INSERT MASTER DATA IN DB
        insertMasterPwIntoTable(encryptedMasterPassword)

        // START ACCOUNT DATA ACTIVITY
        val intent = Intent(this, AccountDataActivity::class.java)
        startActivity(intent)
    }


    private fun insertMasterPwIntoTable(password: String)
    {
        val values = ContentValues()

        values.put(DBContentProvider.masterPassword, password)

        // INSERT MASTER PW INTO MASTER TABLE
        contentResolver.insert(DBContentProvider.MASTER_CONTENT_URI, values)
    }
}