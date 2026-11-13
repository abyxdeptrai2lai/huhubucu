package com.aliucord.plugins.pasteandspam

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.lytefast.flexinput.fragment.FlexInputFragment
import kotlin.concurrent.thread
import kotlin.random.Random

@AliucordPlugin
class PasteAndSpamPlugin : Plugin() {
    private var isSpamming = false
    private var spamThread: Thread? = null

    override fun start(context: Context) {
        patcher.after<FlexInputFragment>("onViewCreated", View::class.java) { param ->
            val view = param.args[0] as View
            val input = view.findViewById<EditText>(com.discord.R.id.chat_input_edit_text) ?: return@after
            val parent = input.parent as? ViewGroup ?: return@after

            val pasteBtn = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_edit)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setOnClickListener {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    if (cb.hasPrimaryClip()) {
                        val text = cb.primaryClip?.getItemAt(0)?.text
                        if (text != null) {
                            val start = input.selectionStart.coerceAtLeast(0)
                            input.text.insert(start, text)
                        }
                    }
                }
            }
            parent.addView(pasteBtn, 0)

            val spamBtn = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_send)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setOnClickListener { showSpamMenu(context, input) }
            }
            parent.addView(spamBtn, 0)
        }
    }

    override fun stop(context: Context) {
        isSpamming = false
        spamThread?.interrupt()
    }

    private fun showSpamMenu(context: Context, chatInput: EditText) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Cấu hình Auto Spam")

        val scroll = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        scroll.addView(layout)

        val delayInput = EditText(context).apply {
            hint = "Delay (ms) - vd: 1000"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(delayInput)

        val tagCheck = CheckBox(context).apply { text = "Tag người dùng" }
        layout.addView(tagCheck)

        val tagInput = EditText(context).apply { hint = "Nhập ID/Tên cần tag" }
        layout.addView(tagInput)

        val fileBtn = Button(context).apply { text = "Nhập file .txt chứa nội dung" }
        val fileContent = arrayOf("")
        fileBtn.setOnClickListener {
            fileContent[0] = "Dòng 1\nDòng 2\nDòng 3\nDòng 4"
            fileBtn.text = "Đã tải file (4 dòng)"
        }
        layout.addView(fileBtn)

        val radioGroup = RadioGroup(context)
        val modes = arrayOf("Đầu - Đuôi", "Cuối - Đầu", "Random", "Full (CopyPaste)")
        for (i in modes.indices) {
            val rb = RadioButton(context).apply {
                text = modes[i]
                id = i
                if (i == 0) isChecked = true
            }
            radioGroup.addView(rb)
        }
        layout.addView(radioGroup)

        val btnLayout = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val startBtn = Button(context).apply { text = "START" }
        val stopBtn = Button(context).apply { text = "STOP" }
        btnLayout.addView(startBtn)
        btnLayout.addView(stopBtn)
        layout.addView(btnLayout)

        builder.setView(scroll)
        val dialog = builder.create()

        startBtn.setOnClickListener {
            if (isSpamming) return@setOnClickListener
            try {
                val delay = delayInput.text.toString().toLong()
                val doTag = tagCheck.isChecked
                val tagId = tagInput.text.toString().trim()
                val modeId = radioGroup.checkedRadioButtonId

                if (fileContent[0].isEmpty()) {
                    Toast.makeText(context, "Chưa nhập file!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                isSpamming = true
                spamThread = thread {
                    val lines = fileContent[0].split("\n")
                    var index = 0

                    while (isSpamming) {
                        var message = ""
                        when (modeId) {
                            2 -> message = lines[Random.nextInt(lines.size)]
                            3 -> message = lines.joinToString("\n")
                            else -> {
                                if (index >= lines.size) index = 0
                                message = lines[index++]
                            }
                        }

                        if (modeId == 0) message = "$message\n"
                        if (modeId == 1) message = "\n$message"

                        if (doTag && tagId.isNotEmpty()) {
                            val prefixes = arrayOf("> #", ">", "#")
                            val prefix = prefixes[Random.nextInt(prefixes.size)]
                            message = "$prefix$tagId\n$message"
                        }

                        val finalMsg = message
                        (context as android.app.Activity).runOnUiThread {
                            chatInput.setText(finalMsg)
                            chatInput.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_SEND)
                        }
                        Thread.sleep(delay)
                    }
                }
                Toast.makeText(context, "Đã bắt đầu spam!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        stopBtn.setOnClickListener {
            isSpamming = false
            spamThread?.interrupt()
            Toast.makeText(context, "Đã dừng spam!", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
}
