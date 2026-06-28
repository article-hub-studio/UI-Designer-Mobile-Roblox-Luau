package com.robloxui.designer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robloxui.designer.ui.screens.EditorScreen
import com.robloxui.designer.ui.theme.RobloxUIDesignerTheme
import com.robloxui.designer.ui.theme.StudioColors
import com.robloxui.designer.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RobloxUIDesignerTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioColors.Background
                ) {
                    val viewModel: EditorViewModel = viewModel()
                    EditorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
