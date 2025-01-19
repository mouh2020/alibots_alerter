package com.example.alibots_alerter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preview)
        val previewBtn = findViewById<Button>(R.id.preview_btn)
        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<Button>(R.id.back_btn)
        val token = findViewById<EditText>(R.id.token)
        val chat_id = findViewById<EditText>(R.id.chat_id)
        val messageToSend = findViewById<EditText>(R.id.message)
        val excel_path = intent.getStringExtra("excel_path")
        previewBtn.setOnClickListener {
            val bot = TelegramBot(token.text.toString())
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val message = SendMessage(chat_id.text.toString().toInt(), messageToSend.text.toString())
                    val response = bot.execute(message)

                    // Switch to Main thread to update the UI
                    withContext(Dispatchers.Main) {
                        if (response.isOk) {
                            Toast.makeText(this@PreviewActivity, "Message sent successfully to:"+chat_id.text, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this@PreviewActivity,
                                "Failed to send message: ${response.description()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PreviewActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        nextBtn.setOnClickListener {
            val intent = Intent(this, AlertActivity::class.java)
            intent.putExtra("excel_file_path",excel_path.toString())
            intent.putExtra("bot_token",token.text.toString())
            intent.putExtra("messageToSend",messageToSend.text.toString())
            startActivity(intent)
        }
        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}