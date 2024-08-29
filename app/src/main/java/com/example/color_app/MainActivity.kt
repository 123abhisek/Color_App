package com.example.color_app

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color_app.ui.theme.Color_AppTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var colorDao: ColorDao
    private lateinit var colorDatabase: ColorDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        colorDatabase = ColorDatabase.getDatabase(this)
        colorDao = colorDatabase.colorDao()

        setContent {
            Color_AppTheme {
                ColorAppScreen(colorDao)
            }
        }
    }
}

data class ColorItem(
    val hex_color: String = "",
    @com.google.firebase.database.PropertyName("Date")
    val date: String = "",
    val sync : Boolean = false
)

@SuppressLint("Range")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorAppScreen(colorDao: ColorDao) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var syncRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    var colorList by remember { mutableStateOf<List<ColorItem>>(emptyList()) }
    var syncCount by remember { mutableStateOf(0) }
    var isInternetAvailable by remember { mutableStateOf(isInternetAvailable(context)) }

    LaunchedEffect(Unit){

        val colorsFromRoom = withContext(Dispatchers.IO){
            colorDao.getAllColors().map {
                ColorItem(hex_color = it.hexColor, date = it.date, sync = it.sync)
            }
        }

        colorList = colorsFromRoom

        if (isInternetAvailable) {
            var database: DatabaseReference =
                FirebaseDatabase.getInstance().reference.child("Colors")
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val colors = mutableListOf<ColorItem>()
                    var count = 0
                    for (colorSnapshot in snapshot.children) {
                        val color = colorSnapshot.getValue(ColorItem::class.java)
                        if (color != null) {
                            colors.add(color)
                            if (color.sync == false) {
                                count++
                            }
                        }
                    }
                    colorList = colors
                    syncCount = count
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Color App")
                },
                actions =  {
                    Button(
                        onClick = {
                            if (isInternetAvailable) {
                                updateAllSyncToTrue(syncRef, context, colorDao)
                            }else{
                                Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB6B9FF),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "$syncCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Sync",
                            modifier = Modifier.size(25.dp),
                            tint = Color(0xFF5659A4)
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF5659A4),
                    titleContentColor = Color.White
                )
            )

        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ){
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 columns
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(colorList.size) { index ->
                        var colorItem = colorList[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // Maintain a square shape for cards
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(colorItem.hex_color)))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Hex color at the top left
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = colorItem.hex_color,
                                        color = Color.White,
                                        style = TextStyle(
                                            fontSize = 16.sp
                                        ),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Horizontal line below the hex color text
                                    Divider(
                                        color = Color.White,
                                        thickness = 1.dp,
                                        modifier = Modifier.width(80.dp)
                                    )
                                }

                                // Date at the bottom right
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Created at",
                                        color = Color.White,
                                        style = TextStyle(
                                            fontSize = 14.sp
                                        ),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = colorItem.date,
                                        color = Color.White,
                                        style = TextStyle(
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (showDialog){
                    ColorInputDialog(
                        onDismiss = {showDialog=false},
                        onSave = { hexColorInput ->
                            saveColorToFirebase(hexColorInput, context)
                            showDialog=false
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true},
                containerColor = Color(0xFFB6B9FF),
                contentColor = Color(0xFF5659A4),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(20.dp,0.dp,20.dp,0.dp)
                ){
                    Text(
                        text = "Add Color",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    )
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
}

fun updateAllSyncToTrue(
    database: DatabaseReference,
    context: Context,
    colorDao: ColorDao
) {
    database.child("Colors").get().addOnSuccessListener { snapshot ->
        val colorList = mutableListOf<ColorItem>() // To store the colors for RoomDB

        for (colorSnapshot in snapshot.children) {
            val color = colorSnapshot.getValue(ColorItem::class.java)
            if (color != null) {
                colorList.add(color)
                colorSnapshot.ref.child("sync").setValue(true) // Set sync to true in Firebase
            }
        }

        // Drop the table and reinsert all data in a background thread
        Thread {
            // Clear the RoomDB table first
            colorDao.clearTable()

            // Insert the updated data into the RoomDB table
            colorDao.insertAll(colorList.map {
                ColorEntity(hexColor = it.hex_color, date = it.date, sync = true)
            })
        }.start()

        Toast.makeText(context, "All data has been synced and saved to RoomDB...", Toast.LENGTH_SHORT).show()

    }.addOnFailureListener {
        Toast.makeText(context, "Failed to update data", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ColorInputDialog(onDismiss:()->Unit, onSave: (String) -> Unit){
    var hexColor by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Add New Color")},
        text = {
            Column {
                Text("Enter Hexa Color" )
                BasicTextField(
                    value = hexColor,
                    onValueChange = {hexColor=it},
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    keyboardActions = KeyboardActions.Default
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {

                    if (hexColor.length == 7 || hexColor.length == 9) {
                        onSave(hexColor)
                    }
                    else{
                        Toast.makeText(context, "Hexa value is not in correct format",Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
               Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

fun getCurrentDate():String{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentDate = LocalDate.now()
        currentDate.format(formatter)
    }else{
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()
        formatter.format(currentDate)
    }
}

fun saveColorToFirebase( hexColor: String, context:Context){
    val database : DatabaseReference = FirebaseDatabase.getInstance().reference;
    val colorRef = database.child("Colors").push()

    val colorData = mapOf(
        "hex_color" to hexColor,
        "Date" to getCurrentDate(),
        "sync" to false
    )

    colorRef.setValue(colorData).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Toast.makeText(context, "Color saved successfully", Toast.LENGTH_SHORT).show()
        } else {
            // Print the exception details for debugging
            Toast.makeText(context, "Color failed to save: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            task.exception?.printStackTrace()
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewColorAppScreen(){
    lateinit var colorDao: ColorDao
    Color_AppTheme {
        ColorAppScreen(colorDao)
    }
}
