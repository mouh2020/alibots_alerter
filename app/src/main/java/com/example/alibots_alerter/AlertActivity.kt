package com.example.alibots_alerter

import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import kotlin.concurrent.thread


class AlertActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        val loadingText = findViewById<TextView>(R.id.textView)
        val listView = findViewById<ListView>(R.id.users_list_view)
        val sendBtn = findViewById<Button>(R.id.send_btn)
        val excel_file_path = intent.getStringExtra("excel_file_path")
        val bot_token = intent.getStringExtra("bot_token")
        val messageToSend = intent.getStringExtra("messageToSend")

        // Check if the required data is missing
        if (excel_file_path == null || bot_token.isNullOrEmpty() || messageToSend.isNullOrEmpty()) {
            Toast.makeText(this, "Missing required data (Excel file, bot token, or message)", Toast.LENGTH_LONG).show()
            return
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val user = parent.getItemAtPosition(position) as User
            Toast.makeText(this, "Clicked on: ${user.firstname}", Toast.LENGTH_SHORT).show()
        }

        sendBtn.setOnClickListener {
            val adapter = listView.adapter as UserAdapter
            val users = adapter.users.filter { it.sent }  // Filter the users who have sent true
            sendMessagesToUsers(bot_token, users, messageToSend)
        }

        // Load users from the Excel file in a separate thread
        thread {
            try {
                val inputStream: InputStream =
                    this.contentResolver.openInputStream(excel_file_path.toUri()) ?: return@thread
                val workbook = WorkbookFactory.create(inputStream)
                val sheet: Sheet = workbook.getSheetAt(0)
                val rowIterator = sheet.iterator()
                val users_list = arrayListOf<User>()

                // Read rows and create user objects
                while (rowIterator.hasNext()) {
                    val row = rowIterator.next()
                    row.getCell(2).cellType = CellType.STRING
                    if (row.rowNum == 0) {
                        val id = "Account ID"
                        val firstname = "Name"
                        val user_to_add = User(id, firstname, false)
                        users_list.add(user_to_add)
                        continue
                    }
                    val id = row.getCell(2).toString()
                    val firstname = row.getCell(0).toString()
                    val user_to_add = User(id, firstname, false)
                    users_list.add(user_to_add)
                }

                runOnUiThread {
                    // Hide progress bar and loading text
                    progressBar.visibility = View.INVISIBLE
                    loadingText.visibility = View.INVISIBLE
                    sendBtn.visibility = View.VISIBLE

                    // Populate the ListView
                    listView.adapter = UserAdapter(this, users_list)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.INVISIBLE
                    loadingText.visibility = View.INVISIBLE
                    Toast.makeText(this, "Failed to load file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendMessagesToUsers(token: String, users: List<User>, message: String) {
        // Initialize the bot
        val bot = TelegramBot(token)
        CoroutineScope(Dispatchers.IO).launch {
            for (user in users) {
                try {
                    // Prepare the message request for the user
                    val sendMessageRequest = SendMessage(user.id.toInt(), message)
                    val response = bot.execute(sendMessageRequest)

                    // Switch to Main thread to update the UI
                    withContext(Dispatchers.Main) {
                        if (response.isOk) {
                            Toast.makeText(this@AlertActivity, "Message sent successfully to: ${user.firstname}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this@AlertActivity,
                                "Failed to send message: ${response.description()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AlertActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
