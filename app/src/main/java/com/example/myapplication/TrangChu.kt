package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrangChu : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhoneAdapter
    private val blockedList = mutableListOf<BlockedPhone>()
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trang_chu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ánh xạ view
        recyclerView = findViewById(R.id.recyclerView)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val tvEmpty = findViewById<View>(R.id.tvEmpty)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PhoneAdapter(blockedList) { phone ->
            // Xóa số khi bấm thùng rác
            blockedList.remove(phone)
            saveList()
            adapter.update(blockedList)
            updateEmptyView(tvEmpty)
            Toast.makeText(this, "Đã xóa ${phone.number}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // Load dữ liệu đã lưu
        loadList()

        // Nút thêm số
        btnAdd.setOnClickListener {
            val number = etPhone.text.toString().trim()
            if (number.length >= 7 && blockedList.none { it.number == number }) {
                val newPhone = BlockedPhone(nextId++, number)
                blockedList.add(newPhone)
                saveList()
                adapter.update(blockedList)
                etPhone.text?.clear()
                updateEmptyView(tvEmpty)
                Toast.makeText(this, "Đã thêm $number", Toast.LENGTH_SHORT).show()
            } else if (blockedList.any { it.number == number }) {
                Toast.makeText(this, "Số này đã có trong danh sách!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tìm kiếm realtime
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    blockedList
                } else {
                    blockedList.filter { it.number.contains(query) }
                }
                adapter.update(filtered)
                tvEmpty.visibility = if (filtered.isEmpty() && blockedList.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadList() {
        val prefs = getSharedPreferences("blocked_prefs", MODE_PRIVATE)
        val json = prefs.getString("blocked_numbers", null) ?: return

        val type = object : TypeToken<MutableList<BlockedPhone>>() {}.type
        val list: MutableList<BlockedPhone> = Gson().fromJson(json, type) ?: mutableListOf()
        blockedList.clear()
        blockedList.addAll(list)

        // Tính lại ID tiếp theo
        nextId = if (list.isEmpty()) 1 else list.maxOf { it.id } + 1

        adapter.update(blockedList)
        updateEmptyView(findViewById(R.id.tvEmpty))
    }

    private fun saveList() {
        val prefs = getSharedPreferences("blocked_prefs", MODE_PRIVATE)
        val json = Gson().toJson(blockedList)
        prefs.edit().putString("blocked_numbers", json).apply()
    }

    private fun updateEmptyView(tvEmpty: View) {
        tvEmpty.visibility = if (blockedList.isEmpty()) View.VISIBLE else View.GONE
    }
}