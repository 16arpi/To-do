package com.pigeoff.todo.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.todo.*
import com.pigeoff.todo.adapters.LignesAdapter
import com.pigeoff.todo.adapters.MainAdapter
import com.pigeoff.todo.db.RmDB
import com.pigeoff.todo.db.RmTask
import com.pigeoff.todo.util.LignesSettings
import com.pigeoff.todo.util.MetroTheme
import com.pigeoff.todo.util.TaskDragDropCallback
import com.pigeoff.todo.util.UtilVariables
import kotlinx.android.synthetic.main.activity_main.view.*


class ChecklistFragment(private var metroId: Int, private var db: RmDB): Fragment() {

    private val variables = UtilVariables()
    private var tasks: MutableList<RmTask>? = null
    var mListChangeListener: OnListChangeListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

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
        val clearBtn: ImageButton = view.btnClear
        val helpBtn: ImageButton = view.btnHelp
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
        if (ligneTitle != "@null") titre.text = ligneTitle else titre.text = getString(
            R.string.app_name
        )

        titre.setOnClickListener {
            val editBottomSheetFragment = EditBottomSheetFragment(
                variables.EDIT_ACTION_EDIT_TITLE,
                metro,
                db,
                null,
                null)
            val localContext = context as MainActivity
            editBottomSheetFragment.show(localContext.supportFragmentManager, "MODAL")
            editBottomSheetFragment.setListTitleChangeListener(object : EditBottomSheetFragment.OnListTitleChangeListener {
                override fun onTaskDeleteListener(title: String) {
                    if (title.isNotEmpty())
                        titre.setText(title)
                    else
                        titre.setText(requireContext().getString(R.string.app_name))
                }
            })
        }

        //INITIATE RECYCLERVIEW
        val adapter =
            MainAdapter(
                requireContext(),
                tasks,
                metro,
                db
            )
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        //INITATIVE DRAG AND DROP THING
        val taskTouchHelper = ItemTouchHelper(TaskDragDropCallback(adapter))
        taskTouchHelper.attachToRecyclerView(recyclerView)

        addBtn.setOnClickListener {
            val editBottomSheetFragment = EditBottomSheetFragment(
                variables.EDIT_ACTION_POST,
                metro,
                db,
                null,
                null)
            val localContext = context as MainActivity
            editBottomSheetFragment.show(localContext.supportFragmentManager, "MODAL")
            editBottomSheetFragment.setOnTaskSaveListener(object : EditBottomSheetFragment.OnTaskSaveListener {
                override fun onTaskSaveListener(action: Int, task: RmTask?, adapterPosition: Int?) {
                    if (action == variables.EDIT_ACTION_POST) {
                        updateData(recyclerView)
                    }
                }
            })
        }

        val anchor = view.findViewById<View>(R.id.anchorMenu)

        helpBtn.setOnClickListener {
            val popupMenu = PopupMenu(context, anchor, Gravity.END)
            popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_main_about -> {
                        val intent = Intent(requireContext(), HelpActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

        clearBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(this.getString(R.string.confirm_message2))
                .setNegativeButton(this.getString(R.string.confirm_cancel)) { dialog, which ->
                    //Nothing to do
                }
                .setPositiveButton(this.getString(R.string.confirme_ok)) { dialog, which ->
                    adapter.deleteTasksDone()
                }
                .show()
        }

        val themeClickListener = View.OnClickListener {
            val lignesDialog = BottomSheetDialog(requireContext())
            val lignesLayout = LinearLayout(requireContext())
            val lignesRecycler = RecyclerView(requireContext())
            val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            lignesRecycler.layoutManager = LinearLayoutManager(requireContext())
            lignesRecycler.layoutParams = params
            lignesRecycler.adapter = LignesAdapter(
                requireContext(),
                this,
                lignesDialog
            )
            lignesLayout.addView(lignesRecycler)
            lignesDialog.setContentView(lignesLayout)
            lignesDialog.show()
        }
        ligneIndic.setOnClickListener(themeClickListener)
        metroIndic.setOnClickListener(themeClickListener)

    }

    fun updateData(recyclerView: RecyclerView?) {
        val currentAdapter = recyclerView?.adapter as MainAdapter
        tasks = db.tasksDAO().getAllTasks(metroId)
        currentAdapter.updateData(tasks!!)
    }

    fun showKeyboard(ettext: EditText) {
        ettext.requestFocus()
        ettext.postDelayed({
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

    /*
    PLACARD DE CODES :

    val dialog = BottomSheetDialog(requireContext())
            val bttmLayout = layoutInflater.inflate(R.layout.layout_sheet_title, null)
            val editTitre = bttmLayout.findViewById<EditText>(R.id.editTextTask)
            val ligneTitle = LignesSettings()
                .getTitle(requireContext(), metroId)
            if (ligneTitle != "@null")
                editTitre.setText(ligneTitle)
            else
                editTitre.setText("")

            dialog.setContentView(bttmLayout)
            dialog.show()

            showKeyboard(editTitre)

            bttmLayout.findViewById<Button>(R.id.metroIndic).setOnClickListener {
                val lignesSettings = LignesSettings()
                lignesSettings.setTitle(requireContext(), metroId, editTitre.text.toString())
                val newTitle = editTitre.text.toString()
                if (newTitle.isNotEmpty())
                    titre.setText(newTitle)
                else
                    titre.setText(requireContext().getString(R.string.app_name))
                dialog.dismiss()
            }

            editTitre.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val lignesSettings = LignesSettings()
                    lignesSettings.setTitle(requireContext(), metroId, editTitre.text.toString())
                    val newTitle = editTitre.text.toString()
                    if (newTitle.isNotEmpty())
                        titre.setText(newTitle)
                    else
                        titre.setText(requireContext().getString(R.string.app_name))
                    dialog.dismiss()
                }
                false
            }
     */
}
