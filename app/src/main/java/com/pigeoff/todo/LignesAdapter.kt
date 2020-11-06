package com.pigeoff.todo

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pigeoff.todo.db.RmDB
import kotlinx.android.synthetic.main.recycler_lines.view.*

class LignesAdapter(private val context: Context, private val parentFragment: Fragment, private val sheet: BottomSheetDialog) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var lignes: MutableList<Int> = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)

    val VIEW_TOP = 0
    val VIEW_NORMAL = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_NORMAL)
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_lines, parent, false))
        else {
            return TopViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_lines_top, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TOP
        }
        else {
            return VIEW_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return lignes.count()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.getItemViewType()) {
            VIEW_TOP -> {
                holder as TopViewHolder
            }
            VIEW_NORMAL -> {
                holder as ViewHolder
                val metro = MetroTheme.MetroBuilder(lignes.get(position))
                val ligneNum = holder.ligneNum
                val ligneTitle = holder.ligneTitle
                val ligneContainer = holder.ligneContainer

                ligneTitle.setTextColor(Color.parseColor("#999999"))
                ligneNum.background = context.getDrawable(metro.drawable)
                ligneNum.setTextColor(Color.parseColor(metro.textColorString))
                ligneNum.setText(metro.id.toString())

                val title = getTitle(context, metro.id)
                if (title != "@null") {
                    ligneTitle.setTextColor(Color.parseColor("#111111"))
                    ligneTitle.setText(getTitle(context, metro.id))
                }

                ligneContainer.setOnClickListener {
                    parentFragment as ChecklistFragment
                    //parentFragment.metroId = metro.id
                    //parentFragment.configTheme(metro, parentFragment.recyclerView)
                    parentFragment.mListChangeListener?.onListChangeListener(metro.id)
                    sheet.dismiss()
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ligneNum = view.ligneIndic2
        val ligneTitle = view.ligneTitle
        val ligneContainer = view.ligneContainer
    }

    class TopViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    fun setTitle(context: Context, numero: Int, title: String) {
        val prefStr = "l"+numero.toString()
        val pref: SharedPreferences = context.getSharedPreferences("lignes", 0)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString(prefStr, title)
        editor.apply()
    }

    fun getTitle(context: Context, numero: Int) : String {
        val prefStr = "l"+numero.toString()
        val pref: SharedPreferences = context.getSharedPreferences("lignes", 0)
        val title = pref.getString(prefStr, "@null")
        return if (!title.isNullOrEmpty()) title else "@null"
    }
}