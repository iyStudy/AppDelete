package com.example.deleteapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

//class AppsAdapter(private val context: Context, private var apps: MutableList<AppData>) :
//    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {
class AppsAdapter(private val context: Context,
                  val apps: MutableList<AppData>,
                  private val onAppUninstallRequested: (String) -> Unit) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {



    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView = view.findViewById(R.id.appNameTextView)
        var imageView: ImageView = view.findViewById(R.id.appIconImageView)
        var tvSize:TextView = view.findViewById(R.id.tvSize)
        var tvDate:TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_unused_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.textView.text = app.label
        holder.imageView.setImageDrawable(app.icon)
        holder.tvSize.text = app.size
        holder.tvDate.text = app.installTime



        // アイテムの背景色を選択状態に応じて変更
        holder.itemView.setBackgroundColor(if (app.isSelected) Color.LTGRAY else Color.WHITE)

        // アイテムクリック時の処理を更新
        holder.itemView.setOnClickListener {
            // 選択状態をトグル
            app.isSelected = !app.isSelected
            // 背景色を更新
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    fun updateApps(newApps: List<AppData>) {
        apps.clear()
        apps.addAll(newApps)
        notifyDataSetChanged()
    }

    fun sortAppsByName() {
        apps.sortBy { it.label }
        notifyDataSetChanged()
    }

    fun sortAppsByDate() {
        apps.sortByDescending { it.installTime }
        notifyDataSetChanged()
    }
    fun sortAppsBySize() {
        apps.sortByDescending { it.sizeBytes }
        notifyDataSetChanged()
    }


}

