// PhoneAdapter.kt
package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhoneAdapter(
    private var list: List<BlockedPhone>,
    private val onDelete: (BlockedPhone) -> Unit
) : RecyclerView.Adapter<PhoneAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvPhone: TextView = view.findViewById(R.id.tvPhoneNumber)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = list[pos]
        h.tvPhone.text = item.number
        h.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size

    fun update(newList: List<BlockedPhone>) {
        list = newList
        notifyDataSetChanged()
    }
}