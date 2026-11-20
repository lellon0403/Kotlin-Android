package com.androidbook.bookmarket.mypage

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SearchView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.androidbook.bookmarket.R
import java.time.LocalDate

class MemoActivity : AppCompatActivity() {

    private val db = DatabaseHelperMemo(this)
    private var selectedPos = -1
    private lateinit var listView: ListView
    private lateinit var adapter: SimpleAdapter
    private lateinit var memoList: ArrayList<HashMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.memo)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listView = findViewById(R.id.memo_list)
        loadMemos()

        listView.setOnItemClickListener { _, _, position, _ -> selectedPos = position }

        findViewById<Button>(R.id.btnInsert).setOnClickListener { showInsertDialog() }
        findViewById<Button>(R.id.btnUpdate).setOnClickListener { showUpdateDialog() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { confirmDelete() }
    }

    private fun createAdapter(list: ArrayList<HashMap<String, String>>) = SimpleAdapter(
        this,
        list,
        R.layout.activity_memo_item,
        arrayOf("date", "title", "content"),
        intArrayOf(R.id.tvDate, R.id.tvTitle, R.id.tvContent)
    )

    private fun loadMemos() {
        memoList = db.readData()
        adapter = createAdapter(memoList)
        if (memoList.isNotEmpty()) listView.adapter = adapter
    }

    private fun hasSelection(): Boolean = selectedPos in 0 until adapter.count

    private fun showInsertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_memo_dialog, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).show()

        dialogView.findViewById<Button>(R.id.insert).setOnClickListener {
            val title = dialogView.findViewById<EditText>(R.id.title).text.toString()
            val content = dialogView.findViewById<EditText>(R.id.content).text.toString()
            db.insertData(LocalDate.now().toString(), title, content)
            dialog.dismiss()
            loadMemos()
        }
        dialogView.findViewById<Button>(R.id.cancel).setOnClickListener { dialog.dismiss() }
    }

    private fun showUpdateDialog() {
        if (adapter.count == 0) return
        if (!hasSelection()) {
            Toast.makeText(applicationContext, "항목을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.activity_memo_dialog, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.title)
        val contentEdit = dialogView.findViewById<EditText>(R.id.content)
        val saveButton = dialogView.findViewById<Button>(R.id.insert)
        val selected = memoList[selectedPos]

        saveButton.text = "수정"
        titleEdit.setText(selected["title"].toString())
        contentEdit.setText(selected["content"].toString())

        val dialog = AlertDialog.Builder(this).setView(dialogView).show()

        saveButton.setOnClickListener {
            val id = memoList[selectedPos]["id"].toString().toInt()
            db.updateData(LocalDate.now().toString(), titleEdit.text.toString(), contentEdit.text.toString(), id)
            dialog.dismiss()
            memoList.clear()
            loadMemos()
        }
        dialogView.findViewById<Button>(R.id.cancel).setOnClickListener { dialog.dismiss() }
    }

    private fun confirmDelete() {
        if (selectedPos <= -1) {
            Toast.makeText(applicationContext, "항목을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val listener = DialogInterface.OnClickListener { dialogInterface, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (hasSelection()) {
                        val id = memoList[selectedPos]["id"].toString().toInt()
                        db.deleteData(id)
                        memoList.removeAt(selectedPos)
                        adapter.notifyDataSetChanged()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> dialogInterface.cancel()
            }
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("메모장 삭제")
            .setMessage("선택한 메모를 삭제하겠습니까?")
            .setIcon(R.drawable.icon_cart)
            .setPositiveButton("예", listener)
            .setNegativeButton("아니요", listener)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.queryHint = "Search View Hint"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                memoList.clear()
                memoList = db.searchReadData(query.toString())
                adapter = createAdapter(memoList)
                listView.adapter = adapter
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
        searchView.setOnCloseListener {
            loadMemos()
            true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}
