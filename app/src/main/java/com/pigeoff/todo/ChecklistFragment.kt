package com.pigeoff.todo

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.todo.db.RmDB
import com.pigeoff.todo.db.RmTask
import kotlinx.android.synthetic.main.activity_main.view.*

class ChecklistFragment(private var metroId: Int, private var db: RmDB): Fragment() {

    private var tasks: List<RmTask>? = null
    var mListChangeListener: OnListChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.activity_main, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //INITIATE DATABASE AND ITEMS
        tasks = db.tasksDAO().getAllTasks(metroId)

        // BINDING VIEWS
        val titre: TextView = requireView().textView
        val recyclerView = view.recyclerView
        val addBtn: TextView = view.textAddNote
        val clearBtn: TextView = view.btnClear
        val helpBtn: TextView = view.btnHelp
        val leftLine = view.leftLine
        val metroIndic = view.metroIndic
        val ligneIndic = view.ligneIndic

        // INIT THEME
        val metro = MetroTheme.MetroBuilder(metroId)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), metro.color)
        if (metro.textColor == R.color.textColorBlack)
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        else
            requireActivity().window.decorView.systemUiVisibility = 0
        leftLine.setBackgroundColor(Color.parseColor(metro.colorString))
        ligneIndic.setTextColor(Color.parseColor(metro.textColorString))
        ligneIndic.setBackgroundResource(metro.drawable)
        ligneIndic.setText(metro.id.toString())

        //INITIATE LIST TITLE
        val ligneTitle = LignesSettings().getTitle(requireContext(), metroId)
        if (ligneTitle != "@null") titre.text = ligneTitle else titre.text = getString(R.string.app_name)

        titre.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val bttmLayout = layoutInflater.inflate(R.layout.layout_sheet_title, null)
            val editTitre = bttmLayout.findViewById<EditText>(R.id.editTextTask)
            val ligneTitle = LignesSettings().getTitle(requireContext(), metroId)
            if (ligneTitle != "@null")
                editTitre.setText(ligneTitle)
            else
                editTitre.setText("")

            dialog.setContentView(bttmLayout)
            dialog.show()

            showKeyboard(editTitre, dialog)



            bttmLayout.findViewById<Button>(R.id.metroIndic).setOnClickListener {
                val lignesSettings = LignesSettings()
                lignesSettings.setTitle(requireContext(), metroId, editTitre.text.toString())
                titre.setText(editTitre.text.toString())
                dialog.dismiss()
            }

            editTitre.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val lignesSettings = LignesSettings()
                    lignesSettings.setTitle(requireContext(), metroId, editTitre.text.toString())
                    titre.setText(editTitre.text.toString())
                    dialog.dismiss()
                }
                false
            }
        }


        //INITIATE RECYCLERVIEW
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = MainAdapter(requireContext(), tasks, metro, db)

        addBtn.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val bttmLayout = layoutInflater.inflate(R.layout.layout_sheet_add, null)
            val editTitre = bttmLayout.findViewById<EditText>(R.id.editTextTask)
            val editDetails = bttmLayout.findViewById<EditText>(R.id.editTextDetails)

            dialog.setContentView(bttmLayout)
            dialog.show()

            showKeyboard(editTitre, dialog)


            bttmLayout.findViewById<Button>(R.id.metroIndic).setOnClickListener {
                val task = RmTask()
                if (editTitre.text.toString().isNotEmpty()) {
                    if (editDetails.text.toString().isNotEmpty()) {
                        task.details = editDetails.text.toString()
                    }
                    task.titre = editTitre.text.toString()
                    task.ligne = metroId
                    db!!.tasksDAO().newNote(task)
                    updateData(recyclerView)
                }
                dialog.dismiss()
            }

            val addEditorAction = TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val task = RmTask()
                    if (editTitre.text.toString().isNotEmpty()) {
                        if (editDetails.text.toString().isNotEmpty()) {
                            task.details = editDetails.text.toString()
                        }
                        task.titre = editTitre.text.toString()
                        task.ligne = metroId
                        db!!.tasksDAO().newNote(task)
                        updateData(recyclerView)
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
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(this.getString(R.string.confirm_message2))
                .setNegativeButton(this.getString(R.string.confirm_cancel), DialogInterface.OnClickListener { dialog, which ->
                    //Nothing to do
                })
                .setPositiveButton(this.getString(R.string.confirme_ok), DialogInterface.OnClickListener { dialog, which ->
                    db!!.tasksDAO().clearTasks()
                    updateData(recyclerView)
                })
                .show()
        }
        helpBtn.setOnClickListener {
            val intent = Intent(requireContext(), HelpActivity::class.java)
            startActivity(intent)
        }

        val themeClickListener = View.OnClickListener {
            val lignesDialog = BottomSheetDialog(requireContext())
            val lignesLayout = LinearLayout(requireContext())
            val lignesRecycler = RecyclerView(requireContext())
            val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            lignesRecycler.layoutManager = LinearLayoutManager(requireContext())
            lignesRecycler.layoutParams = params
            lignesRecycler.adapter = LignesAdapter(requireContext(), this, lignesDialog)
            lignesLayout.addView(lignesRecycler)
            lignesDialog.setContentView(lignesLayout)
            lignesDialog.show()
        }
        ligneIndic.setOnClickListener(themeClickListener)
        metroIndic.setOnClickListener(themeClickListener)

    }

    fun configTheme(metro: MetroTheme.MetroBuilder, recyclerView: RecyclerView?) {}

    fun updateData(recyclerView: RecyclerView?) {
        val currentAdapter = recyclerView?.adapter as MainAdapter
        tasks = db.tasksDAO().getAllTasks(metroId)
        currentAdapter.updateData(tasks!!)
    }

    fun showKeyboard(ettext: EditText, dialog: BottomSheetDialog) {
        ettext.requestFocus()
        ettext.postDelayed(Runnable {
            val keyboard =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(ettext, 0)
            keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
            , 100)
    }


    interface OnListChangeListener {
        fun onListChangeListener(metroid: Int)
    }

    fun setOnListChangeListener(listener: OnListChangeListener) {
        this.mListChangeListener = listener
    }
}
