package com.holeaf.mobile.ui.chat

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.Role
import com.holeaf.mobile.data.chat.ChatAdapter
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.ui.login.decodePhoto
import kotlinx.android.synthetic.main.fragment_chat.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChatFragment : Fragment() {

    val viewModel: ChatViewModel by viewModel()
    val loggedUserInfo: LoggedUserInfo by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    private lateinit var chatAdapter: ChatAdapter

    private fun subscribeOnAddMessage() {
        viewModel.notifyNewMessageInsertedLiveData.observe(viewLifecycleOwner, {
            chatAdapter.data = viewModel.messagesData.value.orEmpty().toMutableList()
            chatAdapter.notifyItemInserted(it)
            messages.scrollToPosition(chatAdapter.itemCount - 1)
        })
    }


    fun send(newMessage: EditText) {
        val text = newMessage.text
        if (text.trim().isNotBlank()) {
            viewModel.sendMessage(text.toString())
            newMessage.text.clear()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val messages = view.findViewById<RecyclerView>(R.id.messages)
        val newMessage = view.findViewById<EditText>(R.id.newMessage)
        val sendMessage = view.findViewById<ImageButton>(R.id.sendMessage)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val username = view.findViewById<TextView>(R.id.username)

        viewModel.chatId = arguments?.getInt("id")
        viewModel.chatName = arguments?.getString("name")
        val myAvatar = arguments?.getString("avatar").orEmpty()
        val chatAvatar = arguments?.getString("chatAvatar").orEmpty()
        val client = arguments?.getBoolean("client", false) ?: false
        val courier = arguments?.getBoolean("courier", false) ?: false


        val role = Role(
            0,
            "stub",
            false,
            false,
            false,
            false,
            false,
            false,
            client,
            false,
            false,
            courier,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        )

        val myAvatarBitmap = decodePhoto(myAvatar, loggedUserInfo.role, requireContext())
        val chatAvatarBitmap = decodePhoto(chatAvatar, role, requireContext())

        chatAdapter = ChatAdapter(mutableListOf(), myAvatarBitmap, chatAvatarBitmap)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            .apply {
                stackFromEnd = true
                isSmoothScrollbarEnabled = true
            }
        viewModel.messagesData.observe(viewLifecycleOwner) {
            chatAdapter.data = viewModel.messagesData.value.orEmpty().toMutableList()
            chatAdapter.notifyDataSetChanged()
            layoutManager.smoothScrollToPosition(
                messages,
                null,
                viewModel.messagesData.value.orEmpty().size
            )
        }
        subscribeOnAddMessage()
        if (Build.VERSION.SDK_INT >= 11) {
            messages.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                messages.postDelayed({
                    messages.smoothScrollToPosition(
                        chatAdapter.itemCount
                    )
                }, 100)
            }
        }

        newMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send(newMessage)
            }
            false
        }
        sendMessage.setOnClickListener {
            val inputMethodManager =
                requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            send(newMessage)
        }
        newMessage.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean {
                if (p2?.action == KeyEvent.ACTION_DOWN && p1 == KeyEvent.KEYCODE_ENTER) {
                    send(newMessage)
                    return true
                }
                return false
            }
        })
        messages.layoutManager = layoutManager
        messages.adapter = chatAdapter
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val msgCount = chatAdapter.getItemCount()
                val lastVisiblePosition =
                    layoutManager.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= msgCount - 1 &&
                    lastVisiblePosition == positionStart - 1
                ) {
                    messages.scrollToPosition(positionStart)
                } else {
                    messages.scrollToPosition(chatAdapter.itemCount)
                }
            }
        })

        viewModel.loadMessages(chatAdapter) {
            layoutManager.smoothScrollToPosition(messages, null, chatAdapter.itemCount)
        }
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace<ChatListFragment>(
                R.id.fragment_container
            ).addToBackStack(null).commit()
        }

        username.text = viewModel.chatName
    }

    companion object {
        fun newInstance(
            id: Int,
            name: String,
            chatAvatar: String,
            myAvatar: String,
            role: Role
        ): ChatFragment {
            val fragment = ChatFragment()
            val arguments = Bundle()

            arguments.putInt("id", id)
            arguments.putString("name", name)
            arguments.putString("avatar", myAvatar)
            arguments.putString("chatAvatar", chatAvatar)
            arguments.putBoolean("courier", role.mapCourier)
            arguments.putBoolean("client", role.mapClient)
            fragment.arguments = arguments
            return fragment
        }
    }
}