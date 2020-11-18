package com.pigeoff.todo.adapters

import android.content.Context
import android.content.DialogInterface
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.todo.MainActivity
import com.pigeoff.todo.util.MetroTheme
import com.pigeoff.todo.R
import com.pigeoff.todo.db.RmDB
import com.pigeoff.todo.db.RmTask
import com.pigeoff.todo.fragments.ChecklistFragment
import com.pigeoff.todo.fragments.EditBottomSheetFragment
import com.pigeoff.todo.util.UtilVariables
import kotlinx.android.synthetic.main.recycler_list.view.*
import java.util.*


class MainAdapter(
    private val context: Context,
    private val notesList: MutableList<RmTask>?,
    private var metro: MetroTheme.MetroBuilder,
    private val db: RmDB) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val variables = UtilVariables()

    var allNotes = notesList
    var dotColor = metro.drawable

    /* CE QUI SE LANCE EN PREMIER : initilisise la classe ViewHolder qui sert de classe principal */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.recycler_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return allNotes!!.count()
    }

    /* Action quand un holder est cliqué */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val note = allNotes?.get(position)
        holder.textTitle.text = note?.titre

        if (note?.done == 1) {
            holder.imageDot.setImageDrawable(context.getDrawable(dotColor))
            holder.textTitle.setTextColor(ContextCompat.getColor(context,
                R.color.textColorBlackDisabled
            ))
            holder.textTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.plus(Paint.ANTI_ALIAS_FLAG)
        }
        else {
            holder.imageDot.setImageDrawable(context.getDrawable(R.drawable.point_correspondance))
            holder.textTitle.setTextColor(ContextCompat.getColor(context,
                R.color.textColorBlack
            ))
            holder.textTitle.paintFlags = Paint.ANTI_ALIAS_FLAG
        }

        holder.imageDot.setOnClickListener {
            if (note?.done == 1) {
                holder.imageDot.setImageDrawable(context.getDrawable(R.drawable.point_correspondance))
                holder.textTitle.setTextColor(ContextCompat.getColor(context,
                    R.color.textColorBlack
                ))
                holder.textTitle.paintFlags = Paint.ANTI_ALIAS_FLAG
                note.done = 0
                db.tasksDAO().updateNote(note)
            }
            else {
                holder.imageDot.setImageDrawable(context.getDrawable(dotColor))
                holder.textTitle.setTextColor(ContextCompat.getColor(context,
                    R.color.textColorBlackDisabled
                ))
                holder.textTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.plus(Paint.ANTI_ALIAS_FLAG)
                note?.done = 1
                db.tasksDAO().updateNote(note)
            }
        }

        holder.linearList.setOnClickListener {
            val editBottomSheetFragment = EditBottomSheetFragment(
                variables.EDIT_ACTION_EDIT,
                null,
                db,
                note,
                allNotes!!.indexOf(note))
            val localContext = context as MainActivity
            editBottomSheetFragment.show(localContext.supportFragmentManager, "MODAL")
            editBottomSheetFragment.setOnTaskSaveListener(object : EditBottomSheetFragment.OnTaskSaveListener {
                override fun onTaskSaveListener(action: Int, task: RmTask?, adapterPosition: Int?) {
                    if (action == variables.EDIT_ACTION_EDIT) {
                        val newTasks = db.tasksDAO().getAllTasks(metro.id)
                        updateData(newTasks)
                    }
                    else if (action == variables.EDIT_ACTION_DELETE) {
                        if (task != null) deleteTask(task, allNotes!!.indexOf(note))
                    }
                }
            })
        }
    }


    fun updateData(data: MutableList<RmTask>) {
        allNotes = mutableListOf()
        allNotes = data
        notifyDataSetChanged()
        notifyItemRangeChanged(0, allNotes!!.count())
    }


    fun updateItemsRange(original: Int, target: Int) {
        Collections.swap(allNotes, original, target)
        notifyItemMoved(original, target)

        //Get IDs
        val taskOriginalId = allNotes!!.get(original).id
        val taskTargetId = allNotes!!.get(target).id

        //Assign new IDs
        allNotes!!.get(original).id = taskTargetId
        allNotes!!.get(target).id = taskOriginalId

        //Update database
        db.tasksDAO().updateNote(allNotes!!.get(original))
        db.tasksDAO().updateNote(allNotes!!.get(target))

    }

    fun deleteTask(task: RmTask, position: Int) {
        val taskId = task.id
        var toRmove = RmTask()
        db.tasksDAO().deleteNote(task)
        for (i in allNotes!!) {
            if (i.id == taskId)
                toRmove = i
        }
        notifyItemRemoved(position)
        allNotes!!.remove(toRmove)
        notifyItemRangeChanged(0, notesList!!.count())
    }

    fun deleteTasksDone() {
        val toRmove = arrayListOf<RmTask>()
        val newNotes = mutableListOf<RmTask>()
        for (task in allNotes!!) {
            if (task.done == 1) {
                toRmove.add(task)
                notifyItemRemoved(allNotes!!.indexOf(task))
                db.tasksDAO().deleteNote(task)
            }
            else {
                newNotes.add(task)
            }
        }
        allNotes = newNotes
        notifyItemRangeChanged(0, allNotes!!.count())
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle = view.textViewTitle
        val imageDot = view.imageViewDotAdapter
        val linearList = view.linearList
    }

    /*
    PLACARD À CODES :

    => Clique long sur la puce permet de supprimer la tâche :

    holder.imageDot.setOnLongClickListener {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.confirm_message))
            .setNegativeButton(context.getString(R.string.confirm_cancel)) { dialog, which ->
                //Nothing to do
            }
            .setPositiveButton(context.getString(R.string.confirme_ok)) { dialog, which ->
                deleteTask(note!!, allNotes!!.indexOf(note))
            }
            .show()

        true
    }

     */

}