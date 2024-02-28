package com.example.deleteapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deleteapp.databinding.ItemAppBinding

class AppsAdapter(private val context: Context,
                  val apps: MutableList<AppData>,
                  private val onAppUninstallRequested: (String) -> Unit) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        with(holder.binding) {
            tvAppName.text = app.label
            ivAppIcon.setImageDrawable(app.icon)
            tvSize.text = app.size
            tvDate.text = app.installTime

            // アイテムの背景色を選択状態に応じて変更　選択時:グレーアウト　非選択時:白
            root.setBackgroundColor(if (app.isSelected) Color.LTGRAY else Color.WHITE)

            // アイテムクリック時の処理
            root.setOnClickListener {
                app.isSelected = !app.isSelected
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = apps.size

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
