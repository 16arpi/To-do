package com.pigeoff.todo

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.pigeoff.todo.db.RmDB
import com.pigeoff.todo.db.RmTask


class MainActivity : AppCompatActivity() {

    var metroId: Int = 1
    var db: RmDB? = null
    var tasks: List<RmTask>? = null
    var recyclerView: RecyclerView? = null
    var adapter: MainAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // INIT THEME
        val pref: SharedPreferences = this.getSharedPreferences("theme", 0)
        metroId = pref.getInt("metroid", 1)
        val metro = MetroTheme.MetroBuilder(metroId)

        db = Room.databaseBuilder(
            applicationContext,
            RmDB::class.java, "notes"
        ).allowMainThreadQueries().build()

        if (metroId == 1) {
            tasks = db!!.tasksDAO().getAllTasks()
        }
        else {
            tasks = db!!.tasksDAO().getAllTasks(metroId)
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        configTheme(metro, recyclerView)

        // BINDING VIEWS
        recyclerView = findViewById(R.id.recyclerView)
        val addBtn: TextView = findViewById(R.id.textAddNote)
        val clearBtn: TextView = findViewById(R.id.btnClear)
        val helpBtn: TextView = findViewById(R.id.btnHelp)

        addBtn.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val bttmLayout = layoutInflater.inflate(R.layout.layout_sheet_add, null)
            val editTitre = bttmLayout.findViewById<EditText>(R.id.editTextTask)
            val editDetails = bttmLayout.findViewById<EditText>(R.id.editTextDetails)
            dialog.setContentView(bttmLayout)
            dialog.show()

            showKeyboard(editTitre, dialog)


            bttmLayout.findViewById<Button>(R.id.metroIndic).setOnClickListener {
                val task = RmTask()
                if (!editTitre.text.toString().isNullOrEmpty()) {
                    if (!editDetails.text.toString().isNullOrEmpty()) {
                        task.details = editDetails.text.toString()
                    }
                    task.titre = editTitre.text.toString()
                    task.ligne = metroId
                    db!!.tasksDAO().newNote(task)
                    updateData()
                }
                dialog.dismiss()
            }

            val addEditorAction = TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val task = RmTask()
                    if (!editTitre.text.toString().isNullOrEmpty()) {
                        if (!editDetails.text.toString().isNullOrEmpty()) {
                            task.details = editDetails.text.toString()
                        }
                        task.titre = editTitre.text.toString()
                        task.ligne = metroId
                        db!!.tasksDAO().newNote(task)
                        updateData()
                    }
                    dialog.dismiss()
                }
                false
            }

            editTitre.setOnEditorActionListener(addEditorAction)
            editDetails.setOnEditorActionListener(addEditorAction)

            bttmLayout.findViewById<TextView>(R.id.btnAddDetails).setOnClickListener {
                editDetails.visibility = View.VISIBLE
            }

        }

        clearBtn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(this.getString(R.string.confirm_message2))
                .setNegativeButton(this.getString(R.string.confirm_cancel), DialogInterface.OnClickListener { dialog, which ->
                    //Nothing to do
                })
                .setPositiveButton(this.getString(R.string.confirme_ok), DialogInterface.OnClickListener { dialog, which ->
                    db!!.tasksDAO().clearTasks()
                    updateData()
                })
                .show()
        }

        helpBtn.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

    }

    fun configTheme(metro: MetroTheme.MetroBuilder, recyclerView: RecyclerView?) {
        val leftLine = findViewById<View>(R.id.leftLine)
        val metroIndic = findViewById<TextView>(R.id.metroIndic)
        val ligneIndic = findViewById<TextView>(R.id.ligneIndic)

        this.window.statusBarColor = ContextCompat.getColor(this, metro.color)
        if (metro.textColor == R.color.textColorBlack)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        else
            getWindow().getDecorView().setSystemUiVisibility(0)

        leftLine.setBackgroundColor(Color.parseColor(metro.colorString))
        ligneIndic.setTextColor(Color.parseColor(metro.textColorString))
        ligneIndic.setBackgroundResource(metro.drawable)
        ligneIndic.setText(metro.id.toString())

        val titre: TextView = findViewById(R.id.textView)
        val ligneTitle = LignesSettings().getTitle(this, metroId)
        if (ligneTitle != "@null")
            titre.text = ligneTitle
        else
            titre.text = getString(R.string.app_name)

        tasks = db!!.tasksDAO().getAllTasks(metroId)
        recyclerView?.adapter = MainAdapter(this, tasks, metro, db!!)

        val themeClickListener = View.OnClickListener {
            val lignesDialog = BottomSheetDialog(this)
            val lignesLayout = LinearLayout(this)
            var lignesRecycler = RecyclerView(this)
            //val lignesLayout = layoutInflater.inflate(R.layout.layout_sheet_line, null)

            lignesRecycler.layoutManager = LinearLayoutManager(this)
            var params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lignesRecycler.layoutParams = params
            lignesRecycler.adapter = LignesAdapter(this, lignesDialog)
            lignesLayout.addView(lignesRecycler)


            lignesDialog.setContentView(lignesLayout)
            lignesDialog.show()
        }

        ligneIndic.setOnClickListener(themeClickListener)
        metroIndic.setOnClickListener(themeClickListener)

        titre.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val bttmLayout = layoutInflater.inflate(R.layout.layout_sheet_title, null)
            val editTitre = bttmLayout.findViewById<EditText>(R.id.editTextTask)
            val ligneTitle = LignesSettings().getTitle(this, metroId)
            if (ligneTitle != "@null")
                editTitre.setText(ligneTitle)
            else
                editTitre.setText("")

            dialog.setContentView(bttmLayout)
            dialog.show()

            showKeyboard(editTitre, dialog)



            bttmLayout.findViewById<Button>(R.id.metroIndic).setOnClickListener {
                val lignesSettings = LignesSettings()
                lignesSettings.setTitle(this, metroId, editTitre.text.toString())
                configTheme(metro, recyclerView)
                dialog.dismiss()
            }

            editTitre.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val lignesSettings = LignesSettings()
                    lignesSettings.setTitle(this, metroId, editTitre.text.toString())
                    configTheme(metro, recyclerView)
                    dialog.dismiss()
                }
                false
            }
        }
    }

    fun updateData() {
        val currentAdapter = recyclerView?.adapter as MainAdapter
        tasks = db!!.tasksDAO().getAllTasks(metroId)
        currentAdapter.updateData(tasks!!)
    }

    fun showKeyboard(ettext: EditText, dialog: BottomSheetDialog) {
        ettext.requestFocus()
        ettext.postDelayed(Runnable {
            val keyboard =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(ettext, 0)
            keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
            , 100)
    }
}
