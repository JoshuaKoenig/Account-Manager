package com.koenig.accountmanager.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.koenig.accountmanager.crypto.Encryption
import com.koenig.accountmanager.databinding.ItemAccountDataBinding
import com.koenig.accountmanager.models.AccountDataModel

class DatabaseAdapter constructor(private var context: Context, private var encryption: Encryption, private var accountDataList: ArrayList<AccountDataModel>): RecyclerView.Adapter<DatabaseAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatabaseAdapter.MainHolder {
        val binding = ItemAccountDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: DatabaseAdapter.MainHolder, position: Int) {
        val accountDataRow = accountDataList[holder.adapterPosition]
        holder.bind(accountDataRow)
    }

    override fun getItemCount(): Int = accountDataList.size

    inner class MainHolder(val binding: ItemAccountDataBinding): RecyclerView.ViewHolder(binding.root)
    {
        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(accountDataRow: AccountDataModel)
        {
            binding.root.tag = accountDataRow

            // GET THE KEY FOR DECRYPTION
            val id = accountDataRow.Id
            val key = "_KEY_${id}"

            // DECRYPT THE PASSWORD
            val decryptedPassword = encryption.decryptKey(key, context)

            binding.accountData = accountDataRow

            // DISPLAY THE DECRYPTED PASSWORD AT RUNTIME
            binding.accountData!!.password = decryptedPassword.toString()

            binding.executePendingBindings()
        }
    }
}