package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.ui.TimeBlockScreen
import com.example.ui.TimeBlockViewModel
import com.example.ui.TimeBlockViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room DB and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database)
        
        // Instantiate ViewModel
        val factory = TimeBlockViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[TimeBlockViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                TimeBlockScreen(viewModel = viewModel)
            }
        }
    }
}
