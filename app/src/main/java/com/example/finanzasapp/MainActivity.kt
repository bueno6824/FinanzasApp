package com.example.finanzasapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finanzasapp.util.NotificationWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val solicitarPermisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {
                programarRecordatorio()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        aplicarTemaGuardado()

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        configurarNavegacion()

        if (savedInstanceState == null) {
            configurarNotificaciones()
        }
    }

    private fun aplicarTemaGuardado() {
        when (obtenerTemaDeSharedPrefs()) {
            0 ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )

            1 ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            else ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
        }
    }

    private fun configurarNavegacion() {
        val navHostFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.nav_host_fragment
                ) as NavHostFragment

        val navController =
            navHostFragment.navController

        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottomNav
            )

        bottomNav.setupWithNavController(
            navController
        )
    }

    private fun configurarNotificaciones() {
        NotificationWorker.crearCanal(
            context = this
        )

        // Actualiza el recordatorio anterior de 3 horas
        // para que funcione cada 24 horas.
        programarRecordatorio()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            val permisoConcedido =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!permisoConcedido) {
                solicitarPermisoNotificaciones.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun programarRecordatorio() {
        val request =
            PeriodicWorkRequestBuilder<NotificationWorker>(
                24,
                TimeUnit.HOURS
            ).build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "RecordatorioDiario",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun guardarPreferenciaTema(
        mode: Int
    ) {
        val sharedPreferences =
            getSharedPreferences(
                "Settings",
                Context.MODE_PRIVATE
            )

        sharedPreferences
            .edit()
            .putInt(
                "theme_mode",
                mode
            )
            .apply()
    }

    private fun obtenerTemaDeSharedPrefs(): Int {
        val sharedPreferences =
            getSharedPreferences(
                "Settings",
                Context.MODE_PRIVATE
            )

        return sharedPreferences.getInt(
            "theme_mode",
            2
        )
    }
}