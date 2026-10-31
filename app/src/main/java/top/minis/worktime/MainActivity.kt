package top.minis.worktime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import top.minis.worktime.ui.WorkTimeApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val store = WorkStore(applicationContext)
        setContent {
            WorkTimeApp(store)
        }
    }
}
