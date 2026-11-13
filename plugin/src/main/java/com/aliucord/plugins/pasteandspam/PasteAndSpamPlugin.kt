package com.aliucord.plugins.pasteandspam

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.lytefast.flexinput.fragment.FlexInputFragment
import kotlin.concurrent.thread
import kotlin.random.Random

@AliucordPlugin
class PasteAndSpamPlugin : Plugin() {
    @Volatile private var isSpamming = false
    private var spamThread: Thread? = null
    private var customPrefix = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun start(context: Context) {
        patcher.after<FlexInputFragment>("onViewCreated", View::class.java) { param ->
            val view = param.args[0] as View
            val input = view.findViewById<EditText>(com.discord.R.id.chat_input_edit_text) ?: return@after
            val parent = input.parent as? ViewGroup ?: return@after

            // Nút Dán (Paste)
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

            // Nút Spam
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

    @SuppressLint("ClickableViewAccessibility")
    private fun showSpamMenu(context: Context, chatInput: EditText) {
        val dialog = AlertDialog.Builder(context).create()
        dialog.setTitle("Cấu hình Auto Spam")

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // Thanh kéo di chuyển menu
        val dragBar = TextView(context).apply {
            text = "≡ Kéo để di chuyển ≡"
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.LTGRAY)
            setPadding(0, 20, 0, 20)
        }
        
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(layout)

        // Dòng 1: Delay
        val delayInput = EditText(context).apply {
            hint = "1. Delay (ms) - vd: 1000"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(delayInput)

        // Dòng 2: Tag
        val tagLayout = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val tagCheck = CheckBox(context)
        val tagInput = EditText(context).apply { hint = "2. ID/Tên cần tag" }
        tagLayout.addView(tagCheck)
        tagLayout.addView(tagInput)
        layout.addView(tagLayout)

        // Dòng 2.5: Tùy chọn vị trí Tag
        val tagPosGroup = RadioGroup(context).apply { orientation = RadioGroup.HORIZONTAL }
        val tagPosOptions = arrayOf("Đầu", "Giữa", "Cuối", "Random")
        for (i in tagPosOptions.indices) {
            val rb = RadioButton(context).apply { text = tagPosOptions[i]; id = i }
            tagPosGroup.addView(rb)
        }
        tagPosGroup.check(0)
        layout.addView(TextView(context).apply { text = "2.5 Vị trí Tag:" })
        layout.addView(tagPosGroup)

        // Dòng 3: Vị trí gửi chữ
        val msgPosGroup = RadioGroup(context)
        val msgPosOptions = arrayOf("Đầu - Cuối", "Cuối - Đầu", "Random", "Full (CopyPaste)", "Cả 2 đầu", "Cả 3")
        for (i in msgPosOptions.indices) {
            val rb = RadioButton(context).apply { text = msgPosOptions[i]; id = i }
            msgPosGroup.addView(rb)
        }
        msgPosGroup.check(0)
        layout.addView(TextView(context).apply { text = "3. Vị trí gửi chữ:" })
        layout.addView(msgPosGroup)

        // Dòng 5: Nguồn chữ (File hoặc Nhập tay)
        val msgSourceLayout = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val fileBtn = Button(context).apply { text = "Nhập file .txt" }
        val manualBtn = Button(context).apply { text = "Nhập tay" }
        msgSourceLayout.addView(fileBtn)
        msgSourceLayout.addView(manualBtn)
        
        val msgList = mutableListOf<String>()
        fileBtn.setOnClickListener {
            msgList.clear()
            msgList.addAll(listOf("> # Nội dung 1", "> Nội dung 2", "# Nội dung 3")) // Mock data
            Toast.makeText(context, "Đã tải file (3 dòng)", Toast.LENGTH_SHORT).show()
        }
        manualBtn.setOnClickListener {
            val inputDialog = AlertDialog.Builder(context)
            inputDialog.setTitle("Nhập nội dung (mỗi dòng 1 câu)")
            val editText = EditText(context)
            editText.setMinLines(5)
            inputDialog.setView(editText)
            inputDialog.setPositiveButton("Lưu") { _, _ ->
                msgList.clear()
                msgList.addAll(editText.text.toString().split("\n").filter { it.isNotBlank() })
            }
            inputDialog.show()
        }
        layout.addView(TextView(context).apply { text = "5. Nguồn chữ:" })
        layout.addView(msgSourceLayout)

        // Loop
        val loopCheck = CheckBox(context).apply { text = "Loop (Lặp lại khi hết)" }
        layout.addView(loopCheck)

        // Prefix thủ công
        val prefixBtn = Button(context).apply { text = "Nhập Prefix thủ công" }
        val prefixLayout = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val prefixCheck = CheckBox(context).apply { text = "Bật Prefix thủ công" }
        prefixLayout.addView(prefixBtn)
        prefixLayout.addView(prefixCheck)
        layout.addView(prefixLayout)

        prefixBtn.setOnClickListener {
            val inputDialog = AlertDialog.Builder(context)
            inputDialog.setTitle("Nhập Prefix")
            val editText = EditText(context)
            editText.setText(customPrefix)
            inputDialog.setView(editText)
            inputDialog.setPositiveButton("Lưu") { _, _ ->
                customPrefix = editText.text.toString()
            }
            inputDialog.show()
        }

        // Nút Start / Stop
        val btnLayout = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val startBtn = Button(context).apply { text = "START" }
        val stopBtn = Button(context).apply { text = "STOP" }
        btnLayout.addView(startBtn)
        btnLayout.addView(stopBtn)
        layout.addView(btnLayout)

        rootLayout.addView(dragBar)
        rootLayout.addView(scrollView)

        // Logic di chuyển menu
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        dragBar.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = dialog.window?.attributes?.x ?: 0
                    initialY = dialog.window?.attributes?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val params = dialog.window?.attributes
                    params?.x = initialX + (event.rawX - initialTouchX).toInt()
                    params?.y = initialY + (event.rawY - initialTouchY).toInt()
                    dialog.window?.attributes = params
                    true
                }
                else -> false
            }
        }

        startBtn.setOnClickListener {
            if (isSpamming) return@setOnClickListener
            if (msgList.isEmpty()) {
                Toast.makeText(context, "Chưa có nội dung!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val delay = delayInput.text.toString().toLongOrNull() ?: 1000L
            val doTag = tagCheck.isChecked
            val tagId = tagInput.text.toString().trim()
            val tagPos = tagPosGroup.checkedRadioButtonId
            val msgPos = msgPosGroup.checkedRadioButtonId
            val isLoop = loopCheck.isChecked
            val doPrefix = prefixCheck.isChecked

            isSpamming = true
            spamThread = thread {
                var index = 0
                while (isSpamming) {
                    var msg = if (isLoop) {
                        if (index >= msgList.size) index = 0
                        msgList[index++]
                    } else {
                        if (index >= msgList.size) break
                        msgList[index++]
                    }

                    // Xử lý vị trí chữ
                    msg = when (msgPos) {
                        0 -> "$msg\n"
                        1 -> "\n$msg"
                        2 -> if (Random.nextBoolean()) "$msg\n" else "\n$msg"
                        3 -> msg
                        4 -> "\n$msg\n"
                        5 -> "\n\n$msg\n\n"
                        else -> msg
                    }

                    // Xử lý Tag & Tự động phát hiện Prefix trong câu
                    if (doTag && tagId.isNotEmpty()) {
                        val mentionId = if (tagId.startsWith("<@")) tagId else "<@$tagId>"
                        val prefixesToCheck = arrayOf("> # ", "> ", "# ")
                        var prefixFound = ""
                        var contentWithoutPrefix = msg

                        for (p in prefixesToCheck) {
                            if (msg.startsWith(p)) {
                                prefixFound = p
                                contentWithoutPrefix = msg.substring(p.length)
                                break
                            }
                        }

                        msg = if (prefixFound.isNotEmpty()) {
                            "$prefixFound$mentionId $contentWithoutPrefix"
                        } else {
                            when (tagPos) {
                                0 -> "$mentionId\n$msg"
                                1 -> {
                                    val lines = msg.split("\n")
                                    if (lines.size > 1) {
                                        val mid = lines.size / 2
                                        lines.toMutableList().apply { add(mid, mentionId) }.joinToString("\n")
                                    } else {
                                        "$mentionId\n$msg"
                                    }
                                }
                                2 -> "$msg\n$mentionId"
                                3 -> if (Random.nextBoolean()) "$mentionId\n$msg" else "$msg\n$mentionId"
                                else -> msg
                            }
                        }
                    }

                    if (doPrefix && customPrefix.isNotEmpty()) {
                        msg = "$customPrefix $msg"
                    }

                    val finalMsg = msg
                    (context as android.app.Activity).runOnUiThread {
                        chatInput.setText(finalMsg)
                        chatInput.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_SEND)
                    }
                    Thread.sleep(delay)
                }
                isSpamming = false
            }
            Toast.makeText(context, "Đã bắt đầu spam!", Toast.LENGTH_SHORT).show()
        }

        stopBtn.setOnClickListener {
            isSpamming = false
            spamThread?.interrupt()
            Toast.makeText(context, "Đã dừng spam!", Toast.LENGTH_SHORT).show()
        }

        dialog.setView(rootLayout)
        dialog.show()
        
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
