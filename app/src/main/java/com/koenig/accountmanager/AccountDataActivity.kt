package com.koenig.accountmanager

import android.content.ContentValues
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koenig.accountmanager.adapters.DatabaseAdapter
import com.koenig.accountmanager.models.AccountDataModel

class AccountDataActivity : AppCompatActivity() {

    // CONTENT URI FOR ACCOUNT DB
    val Content_DB_URI = Uri.parse("content://${DBContentProvider.PROVIDER_NAME}/db")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_data)

        // RECYCLER VIEW
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        // TEXT NO DATA
        val textNoData: TextView = findViewById(R.id.textNoData)

        // TEXT HEADER
        val textHeader: TextView = findViewById(R.id.textHeaderAcc)

        // INPUT LAYOUT
        val inputLayout: LinearLayout = findViewById(R.id.LayoutNewRowInput)

        // TEXT NEW ACCOUNT NAME
        val textNewAccountName: TextView = findViewById(R.id.textAccount)

        // TEXT NEW PASSWORD
        val textNewPassword: TextView = findViewById(R.id.textPassword)

        // SAVE NEW ROW BUTTON
        val buttonSaveNewRow: Button = findViewById(R.id.buttonSaveNewRow)
        buttonSaveNewRow.setOnClickListener {

            // INSERT ROW INTO DATABASE
            if(textNewAccountName.text.isNullOrBlank() && textNewPassword.text.isNullOrBlank())
            {
                // VALUES AREN'T VALID
                Toast.makeText(this, "Enter all values", Toast.LENGTH_LONG).show()
            }
            else
            {
                // VALUES ARE VALID
                insertNewRow(textNewAccountName.text.toString(), textNewPassword.text.toString())
            }

            // INPUT LAYOUT => INVISIBLE
            inputLayout.visibility = View.GONE

            // CHANGE HEADER TEXT
            textHeader.text = "All your accounts"

            // SETUP THE RECYCLERVIEW
            setUpData(recyclerView, textNoData)
        }

        // ADD ROW BUTTON
        val buttonAddRow: Button = findViewById(R.id.buttonAddRow)
        buttonAddRow.setOnClickListener {

            // INPUT LAYOUT => VISIBLE
            inputLayout.visibility = View.VISIBLE

            // RECYCLERVIEW => INVISIBLE
            recyclerView.visibility = View.GONE

            // NODATATEXT => INVISIBLE
            textNoData.visibility = View.GONE

            // CHANGE HEADER TEXT
            textHeader.text = "Add new Row"

        }

        // SET UP THE DATABASE
        setUpData(recyclerView, textNoData)
    }

    private fun insertNewRow(newAccountName: String, newPassword: String) {

        val values = ContentValues()

        values.put(DBContentProvider.accountName, newAccountName)
        values.put(DBContentProvider.password, newPassword)

        contentResolver.insert(Content_DB_URI, values)
    }

    private fun setUpData(recyclerView: RecyclerView, textNoData: TextView) {

        val cursor = contentResolver.query(Content_DB_URI, null, null, null, null)
        val accountDataList: ArrayList<AccountDataModel> = ArrayList()

        with(cursor)
        {
            while (this!!.moveToNext())
            {
                val id = getLong(getColumnIndexOrThrow(DBContentProvider.id))
                val accountName = getString(getColumnIndexOrThrow(DBContentProvider.accountName))
                val password = getString(getColumnIndexOrThrow(DBContentProvider.password))
                val accountDataModel = AccountDataModel(id, accountName, password)
                accountDataList.add(accountDataModel)
            }
        }

        if(cursor!!.count > 0)
        {
            // HAS DATA ROWS
            textNoData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = DatabaseAdapter(accountDataList)
        }
        else
        {
            // NO ROWS
            textNoData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }
}