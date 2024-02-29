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
import com.example.deleteapp.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var adapter: AppsAdapter
    private lateinit var uninstallLauncher: ActivityResultLauncher<Intent>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 権限の確認
        requestUsageStatsPermission()
        // アンインストールランチャーの準備
        setupUninstallLauncher()
        // インストール済みアプリ情報の取得
        val appList = getAllUserInstalledApps(this).toMutableList()
        adapter = AppsAdapter(this, appList) { packageName ->
            startAppUninstall(packageName)
        }

        binding.appsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.appsRecyclerView.adapter = adapter


        // deleteボタンのクリックリスナー設定
        binding.fabDelete.setOnClickListener {
            // 選択されたアプリのパッケージ名を取得
            val selectedApps = adapter.apps.filter { it.isSelected }.map { it.packageName }
            // 選択されたアプリを一括でアンインストール
            selectedApps.forEach { packageName ->
                // Androidの仕様上、アプリ毎に削除処理を行う
                startAppUninstall(packageName)
            }
        }
    }

    /**
     * アンインストール用のActivityResultLauncherをセットアップ
     */
    private fun setupUninstallLauncher() {
        uninstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // アンインストールが成功したらリストを更新
                updateAppList()
            }
        }
    }

    /**
     * 指定されたパッケージ名のアプリをアンインストール
     */
    fun startAppUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        uninstallLauncher.launch(intent)
    }

    /**
     * アプリリストを更新
     */
    private fun updateAppList() {
        val updatedList = getAllUserInstalledApps(this).toMutableList()
        adapter.updateApps(updatedList) // adapter の updateApps メソッドを呼び出す
    }


    /**
     * インストール済みのアプリを取得
     *
     * @param context コンテキスト。
     * @return インストール済みのAppDataオブジェクトのリスト。
     */
    fun getAllUserInstalledApps(context: Context): List<AppData> {
        val pm = context.packageManager
        val currentPackageName = context.packageName  // 現在のアプリのパッケージ名を取得
        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName != currentPackageName }  // 自身のアプリを除外

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

        return installedApps.mapNotNull { appInfo ->
            try {
                val storageStats = storageStatsManager.queryStatsForUid(appInfo.storageUuid, appInfo.uid)
                val appSize = Formatter.formatFileSize(context, storageStats.appBytes)
                val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
                val installTime = dateFormat.format(Date(packageInfo.firstInstallTime))
                AppData(
                    label = appInfo.loadLabel(pm).toString(),
                    icon = appInfo.loadIcon(pm),
                    packageName = appInfo.packageName,
                    size = appSize,
                    sizeBytes = storageStats.appBytes,
                    installTime = installTime
                )
            } catch (e: Exception) {
                Log.e("AppList", "Error getting app details", e)
                null
            }
        }
    }



    /**
     * オプションメニューを作成
     *
     * @param menu メニュー。
     * @return メニューの作成に成功した場合はtrue。
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * オプションアイテムが選択されたときに呼び出されます。
     *
     * @param item 選択されたメニューアイテム。
     * @return アクションが消費された場合はtrue。
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                // 名前で並び替え
                adapter.sortAppsByName()
                true
            }
            R.id.action_sort_by_date -> {
                // 日付で並び替え
                adapter.sortAppsByDate()
                true
            }
            R.id.action_sort_by_size -> {
                // サイズで並び替え
                adapter.sortAppsBySize()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * PACKAGE_USAGE_STATSの権限があるか確認します。
     *
     * @param context コンテキスト。
     * @return 権限があればtrue。
     */
    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * PACKAGE_USAGE_STATSの権限を要求
     */
    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            })
        }
    }

}
