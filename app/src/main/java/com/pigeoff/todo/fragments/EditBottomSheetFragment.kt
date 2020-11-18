package com.pigeoff.todo.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.todo.R
import com.pigeoff.todo.db.RmDB
import com.pigeoff.todo.db.RmTask
import com.pigeoff.todo.util.LignesSettings
import com.pigeoff.todo.util.MetroTheme
import com.pigeoff.todo.util.UtilVariables
import kotlinx.android.synthetic.main.layout_sheet_add.view.*


class EditBottomSheetFragment(action: Int, metro: MetroTheme.MetroBuilder?, db: RmDB, task: RmTask?, adapterPosition: Int?) : BottomSheetDialogFragment() {

    private val variables = UtilVariables()

    private val action = action
    private val metro = metro
    private val db = db
    private val task = task
    private val adapterPosition = adapterPosition

    private lateinit var editTextTask: EditText
    private lateinit var editTextDetails: EditText
    private lateinit var btnAddDetails: TextView
    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button

    var mTaskSaveListener: OnTaskSaveListener? = null
    var mListTitleChangeListener: OnListTitleChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_sheet_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editTextTask = view.editTextTask
        editTextDetails = view.editTextDetails
        btnAddDetails = view.btnAddDetails
        saveBtn = view.metroIndic
        deleteBtn = view.metroIndicDelete
    }

    override fun onResume() {
        super.onResume()
        if (action == variables.EDIT_ACTION_EDIT) {
            if (task != null) {
                editTextTask.setSelection(editTextTask.text.toString().length)
                editTextTask.setText(task.titre)
                editTextDetails.setText(task.details)
                if (task.details.isNotEmpty()) {
                    editTextDetails.visibility = View.VISIBLE
                }
            }
        }
        else if (action == variables.EDIT_ACTION_EDIT_TITLE) {
            btnAddDetails.visibility = View.GONE
            deleteBtn.visibility = View.GONE
            editTextTask.hint = requireContext().getString(R.string.indicator_title_edit)

            if (metro != null) {
                val ligneTitle = LignesSettings().getTitle(requireContext(), metro.id)
                if (ligneTitle != "@null") editTextTask.setText(ligneTitle)
                else editTextTask.setText("")

                editTextTask.requestFocus()
                editTextTask.setSelection(editTextTask.text.toString().length)
            }
        }
        else {
            editTextTask.requestFocus()
            editTextTask.setSelection(editTextTask.text.toString().length)
            deleteBtn.visibility = View.GONE
        }

        btnAddDetails.setOnClickListener {
            editTextDetails.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener { onSaveAction(action, adapterPosition) }

        editTextTask.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSaveAction(action, adapterPosition)
            }
            true
        }

        deleteBtn.setOnClickListener {
            onSaveAction(variables.EDIT_ACTION_DELETE, adapterPosition)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog

        if (action == variables.EDIT_ACTION_POST || action == variables.EDIT_ACTION_EDIT_TITLE) {
            dialog = BottomSheetDialog(requireActivity(), R.style.DialogStyleKeyboard)
        }
        else {
            dialog = BottomSheetDialog(requireActivity(), R.style.DialogStyleNoKeyboard)
        }

        dialog.setOnShowListener {
            val bottomSheet: FrameLayout? =  dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).setState(BottomSheetBehavior.STATE_EXPANDED)
        }

        return dialog
    }

    fun onSaveAction(action: Int, position: Int?) {
        if (action == variables.EDIT_ACTION_POST) {
            val newTask = newTask()
            mTaskSaveListener?.onTaskSaveListener(variables.EDIT_ACTION_POST, newTask, null)
            this.dismiss()
            this.onDestroy()
        }
        else if (action == variables.EDIT_ACTION_EDIT) {
            if (task != null) {
                if (updateTask(task)) {
                    if (position != null)
                        mTaskSaveListener?.onTaskSaveListener(variables.EDIT_ACTION_EDIT, task, position)
                    else
                        mTaskSaveListener?.onTaskSaveListener(variables.EDIT_ACTION_EDIT, task, null)
                }
            }
            this.dismiss()
            this.onDestroy()
        }
        else if (action == variables.EDIT_ACTION_DELETE) {
            if (task != null) {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(requireContext().getString(R.string.confirm_message))
                        .setNegativeButton(requireContext().getString(R.string.confirm_cancel)) { dialog, which ->

                        }
                        .setPositiveButton(requireContext().getString(R.string.confirme_ok)) { dialog, which ->
                            if (deleteTask(task)) {
                                if (position != null)
                                    mTaskSaveListener?.onTaskSaveListener(
                                        variables.EDIT_ACTION_DELETE,
                                        task,
                                        position
                                    )
                                else
                                    mTaskSaveListener?.onTaskSaveListener(
                                        variables.EDIT_ACTION_DELETE,
                                        task,
                                        null
                                    )
                                this.dismiss()
                                this.onDestroy()
                            }
                        }
                        .show()
            }
        }
        else if (action == variables.EDIT_ACTION_EDIT_TITLE) {
            if (metro != null) {
                val title = editTextTask.text.toString()
                val lignesSettings = LignesSettings()
                lignesSettings.setTitle(requireContext(), metro.id, title)
                mListTitleChangeListener?.onTaskDeleteListener(title)
            }
            this.dismiss()
            this.onDestroy()
        }
    }

    fun updateTask(task: RmTask) : Boolean {
        if (editTextTask.text.isNotEmpty()) {
            task.titre = editTextTask.text.toString()
            task.details = editTextDetails.text.toString()
            db.tasksDAO().updateNote(task)
            return true
        }
        else {
            return false
        }
    }

    fun newTask() : RmTask? {
        val task = RmTask()
        if (metro != null) {
            if (editTextTask.text.isNotEmpty()) {
                task.ligne = metro.id
                task.titre = editTextTask.text.toString()
                task.details = editTextDetails.text.toString()
                db.tasksDAO().newNote(task)
                return task
            }
            else {
                return null
            }
        }
        else {
            return null
        }
    }

    fun deleteTask(task: RmTask?) : Boolean {
        if (task != null) {
            db.tasksDAO().deleteNote(task)
            return true
        }
        else {
            return false
        }
    }

    interface OnTaskSaveListener {
        fun onTaskSaveListener(action: Int, task: RmTask?, adapterPosition: Int?)
    }

    interface OnListTitleChangeListener {
        fun onTaskDeleteListener(title: String)
    }

    fun setOnTaskSaveListener(listener: OnTaskSaveListener) {
        this.mTaskSaveListener = listener
    }

    fun setListTitleChangeListener(listener: OnListTitleChangeListener) {
        this.mListTitleChangeListener = listener
    }
}