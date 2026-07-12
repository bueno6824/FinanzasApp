package com.example.finanzasapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        // Aplicar el tema guardado antes del super.onCreate
        val temaGuardado = obtenerTemaDeSharedPrefs() // Recupera el Int guardado (0, 1 o 2)
        when(temaGuardado) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        // Esto vincula el menú con el controlador y maneja la limpieza de la pila automáticamente
        bottomNav.setupWithNavController(navController)
    }

    // Agrega esto en tu MainActivity.kt (fuera del onCreate)

    fun guardarPreferenciaTema(mode: Int) {
        val sharedPreferences = getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("theme_mode", mode).apply()
    }

    private fun obtenerTemaDeSharedPrefs(): Int {
        val sharedPreferences = getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
        // Por defecto devolvemos 2 (Defecto del sistema)
        return sharedPreferences.getInt("theme_mode", 2)
    }



}
