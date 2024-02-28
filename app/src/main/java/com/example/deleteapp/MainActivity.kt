package com.example.deleteapp

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: AppsAdapter
    private lateinit var uninstallLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 権限の確認
        requestUsageStatsPermission()

        setupUninstallLauncher()

        val appList = getAllUserInstalledApps(this).toMutableList()
        adapter = AppsAdapter(this, appList) { packageName ->
            startAppUninstall(packageName)
        }
        displayApps(appList)


        // deleteボタンのクリックリスナー設定
        findViewById<FloatingActionButton>(R.id.fabDelete).setOnClickListener {
            // 選択されたアプリのパッケージ名を取得
            val selectedApps = adapter.apps.filter { it.isSelected }.map { it.packageName }
            // 選択されたアプリを一括でアンインストール
            selectedApps.forEach { packageName ->
                startAppUninstall(packageName)
            }
        }
    }

    // アンインストールのロジック（ActivityResultLauncherを使うなど）
    private fun uninstallApp(packageName: String) {
        // アンインストールロジックの実装

    }

    private fun setupUninstallLauncher() {
        uninstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // アンインストールが成功したらリストを更新
                updateAppList()
            }
        }
    }

    // アプリのアンインストールを開始するメソッド
    fun startAppUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        uninstallLauncher.launch(intent)
    }

    // アプリ情報を表示するメソッド
    private fun displayApps(appInfos: MutableList<AppData>) {
        val recyclerView = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }
    private fun updateAppList() {
        val updatedList = getAllUserInstalledApps(this).toMutableList()
        adapter.updateApps(updatedList) // adapter の updateApps メソッドを呼び出す
    }


    fun getAllUserInstalledApps(context: Context): List<AppData> {
        val pm = context.packageManager
        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

        return installedApps.map { appInfo ->
            val storageStats = storageStatsManager.queryStatsForUid(appInfo.storageUuid, appInfo.uid)
            val appSize = Formatter.formatFileSize(context, storageStats.appBytes)
            val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
            val installTime = dateFormat.format(Date(packageInfo.firstInstallTime))
            AppData(
                label = appInfo.loadLabel(pm).toString(),
                icon = appInfo.loadIcon(pm),
                packageName = appInfo.packageName,
                size = appSize,  // アプリのサイズを設定
                sizeBytes = storageStats.appBytes,  // バイト単位のサイズを設定
                installTime = installTime // インストール日時を設定
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                adapter.sortAppsByName()
                true
            }
            R.id.action_sort_by_date -> {
                adapter.sortAppsByDate()
                true
            }
            R.id.action_sort_by_size -> {
                // サイズで並び替える処理
                adapter.sortAppsBySize()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            })
        }
    }

}
