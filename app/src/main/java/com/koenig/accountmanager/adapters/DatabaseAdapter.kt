package com.koenig.accountmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.koenig.accountmanager.databinding.ItemAccountDataBinding
import com.koenig.accountmanager.models.AccountDataModel

class DatabaseAdapter constructor(private var accountDataList: ArrayList<AccountDataModel>): RecyclerView.Adapter<DatabaseAdapter.MainHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatabaseAdapter.MainHolder {
        val binding = ItemAccountDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: DatabaseAdapter.MainHolder, position: Int) {
        val accountDataRow = accountDataList[holder.adapterPosition]
        holder.bind(accountDataRow)
    }

    override fun getItemCount(): Int = accountDataList.size

    inner class MainHolder(val binding: ItemAccountDataBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(accountDataRow: AccountDataModel)
        {
            binding.root.tag = accountDataRow
            binding.accountData = accountDataRow
            binding.executePendingBindings()
        }
    }
}