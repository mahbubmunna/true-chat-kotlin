package me.moonss.truechat.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import me.moonss.truechat.R
import me.moonss.truechat.models.ChatMessage
import me.moonss.truechat.models.User
import java.sql.Time

class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user.username

        recycler_view_chat_log.adapter = adapter

        //placeDummyData()
        listenForMessages()

        send_button_chat_log.setOnClickListener {

            performSendMessage(user)
        }

    }

    private fun listenForMessages() {
        val reference = FirebaseDatabase.getInstance().getReference("/messages")

        reference.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val message = p0.getValue(ChatMessage::class.java)

                if (message != null) {
                    Log.d(TAG, message.text)

                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(message.text))
                    } else {
                        adapter.add(ChatToItem(message.text))
                    }
                }


            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}



        })
    }

    private fun performSendMessage(user: User) {

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val id = reference.key
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.uid
        val message = edit_text_chat_log.text.toString()

        val chatMessage = ChatMessage(id!!, fromId!!, toId, message, System.currentTimeMillis())
        Log.d(TAG, "Send button clicked, retrieved message is: ${chatMessage.text}")

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Message saved with key: ${reference.key}")
            }

        //clearing the text field
        edit_text_chat_log.setText("")
    }

    companion object {
        private val TAG = ChatLogActivity::class.java.simpleName
    }

}

class ChatFromItem(val text: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from.text = text
    }
}

class ChatToItem(val text: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to.text = text
    }
}
