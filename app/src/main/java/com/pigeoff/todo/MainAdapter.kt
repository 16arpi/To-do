package com.pigeoff.todo

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.todo.db.RmDB
import com.pigeoff.todo.db.RmTask
import kotlinx.android.synthetic.main.recycler_list.view.*


class MainAdapter(private val context: Context, private val notesList: List<RmTask>?, private var metro: MetroTheme.MetroBuilder, private val db: RmDB) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var allNotes = notesList
    var dotColor = metro.drawable

    /* CE QUI SE LANCE EN PREMIER : initilisise la classe ViewHolder qui sert de classe principal */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_list, parent, false))
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
            holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.textColorBlackDisabled))
            holder.textTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.plus(Paint.ANTI_ALIAS_FLAG)
        }
        else {
            holder.imageDot.setImageDrawable(context.getDrawable(R.drawable.point_correspondance))
            holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.textColorBlack))
            holder.textTitle.paintFlags = Paint.ANTI_ALIAS_FLAG
        }

        holder.imageDot.setOnClickListener {
            if (note?.done == 1) {
                holder.imageDot.setImageDrawable(context.getDrawable(R.drawable.point_correspondance))
                holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.textColorBlack))
                holder.textTitle.paintFlags = Paint.ANTI_ALIAS_FLAG
                note?.done = 0
                db.tasksDAO().updateNote(note)
            }
            else {
                holder.imageDot.setImageDrawable(context.getDrawable(dotColor))
                holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.textColorBlackDisabled))
                holder.textTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.plus(Paint.ANTI_ALIAS_FLAG)
                note?.done = 1
                db.tasksDAO().updateNote(note)
            }
        }

        holder.imageDot.setOnLongClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.confirm_message))
                .setNegativeButton(context.getString(R.string.confirm_cancel), DialogInterface.OnClickListener { dialog, which ->
                    //Nothing to do
                })
                .setPositiveButton(context.getString(R.string.confirme_ok), DialogInterface.OnClickListener { dialog, which ->
                    db.tasksDAO().deleteNote(note)
                    val newTasks = db.tasksDAO().getAllTasks(metro.id)
                    updateData(newTasks)
                })
                .show()

            true
        }

        holder.linearList.setOnClickListener {
            val bttmDialog = BottomSheetDialog(context)
            val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val bttmLayout = inflater.inflate(R.layout.layout_sheet_add, null)
            val editTitre = bttmLayout.findViewById<EditText>(R.id.editTextTask)
            editTitre.setText(note!!.titre)
            val btnDelete = bttmLayout.findViewById<Button>(R.id.metroIndicDelete)
            btnDelete.visibility = View.VISIBLE
            val editDetails = bttmLayout.findViewById<EditText>(R.id.editTextDetails)
            if (!note!!.details.isNullOrEmpty()) editDetails.visibility = View.VISIBLE; editDetails.setText(note.details)
            bttmDialog.setContentView(bttmLayout)
            bttmDialog.show()

            bttmLayout.findViewById<Button>(R.id.metroIndic).setOnClickListener {
                if (!editTitre.text.toString().isNullOrEmpty()) {
                    if (!editDetails.text.toString().isNullOrEmpty()) {
                        note!!.details = editDetails.text.toString()
                    }
                    note!!.titre = editTitre.text.toString()
                    db!!.tasksDAO().updateNote(note)

                    val newTasks = db.tasksDAO().getAllTasks(metro.id)
                    updateData(newTasks)
                }
                bttmDialog.dismiss()
            }

            val actionListener = TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!editTitre.text.toString().isNullOrEmpty()) {
                        if (!editDetails.text.toString().isNullOrEmpty()) {
                            note!!.details = editDetails.text.toString()
                        }
                        note!!.titre = editTitre.text.toString()
                        db!!.tasksDAO().updateNote(note)

                        val newTasks = db.tasksDAO().getAllTasks(metro.id)
                        updateData(newTasks)
                    }
                    bttmDialog.dismiss()
                }
                false
            }

            editTitre.setOnEditorActionListener(actionListener)
            editDetails.setOnEditorActionListener(actionListener)

            bttmLayout.findViewById<TextView>(R.id.btnAddDetails).setOnClickListener {
                editDetails.visibility = View.VISIBLE
            }

            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.confirm_message))
                    .setNegativeButton(context.getString(R.string.confirm_cancel), DialogInterface.OnClickListener { dialog, which ->
                        //Nothing to do
                    })
                    .setPositiveButton(context.getString(R.string.confirme_ok), DialogInterface.OnClickListener { dialog, which ->
                        bttmDialog.dismiss()
                        db.tasksDAO().deleteNote(note)
                        val newTasks = db.tasksDAO().getAllTasks(metro.id)
                        updateData(newTasks)
                    })
                    .show()
            }

            true
        }
    }

    fun updateData(data: List<RmTask>) {
        allNotes = listOf()
        allNotes = data
        notifyDataSetChanged()
    }



    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle = view.textViewTitle
        val imageDot = view.imageViewDotAdapter
        val linearList = view.linearList
    }

}