package de.tum.`in`.tumcampusapp.component.ui.chat


import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for chat messages
 */

class ChatMessageViewModel(private val localRepository: ChatMessageLocalRepository,
                           private val remoteRepository: ChatMessageRemoteRepository,
                           private val compositeDisposable: CompositeDisposable) : ViewModel() {

    interface DataLoadInterface {
        fun onDataLoaded()
    }

    /**
     * Returns a flowable that emits a list of chat messages from the local repository
     */

    fun markAsRead(room: Int) =
            localRepository.markAsRead(room)

    fun deleteOldEntries() =
            localRepository.deleteOldEntries()

    fun addToUnsent(message: ChatMessage) =
            localRepository.addToUnsent(message)

    fun getAll(room: Int): List<ChatMessage> =
            localRepository.getAllChatMessagesList(room)

    fun getUnsent(): List<ChatMessage> =
            localRepository.getUnsent()

    fun removeUnsent(chatMessage: ChatMessage) =
            localRepository.removeUnsent(chatMessage)

    fun getOlderMessages(roomId: Int, messageId: Long, verification: TUMCabeVerification, callback: DataLoadInterface?): Boolean =
            compositeDisposable.add(
                    remoteRepository
                            .getMessages(roomId, messageId, verification)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(Schedulers.io())
                            .subscribe({ t ->
                                t.forEach { localRepository.replaceMessage(it) }
                                callback?.onDataLoaded()
                            }, { t -> Utils.logwithTag("ChatMessageViewModel", t.message) })
            )

    fun getNewMessages(roomId: Int, verification: TUMCabeVerification, callback: DataLoadInterface?): Boolean =
            compositeDisposable.add(
                    remoteRepository.getNewMessages(roomId, verification)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(Schedulers.io())
                            .subscribe({ t ->
                                t.forEach { localRepository.replaceMessage(it) }
                                callback?.onDataLoaded()
                            }, { t -> Utils.logwithTag("ChatMessageViewModel", t.message) })
            )

    fun sendMessage(roomId: Int, chatMessage: ChatMessage, context: Context): Boolean {
        val broadcastManager = LocalBroadcastManager.getInstance(context)
        return compositeDisposable.add(
                remoteRepository.sendMessage(roomId, chatMessage)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.io())
                        .subscribe({ message ->
                            message.sendingStatus = ChatMessage.STATUS_SENT
                            localRepository.replaceMessage(message)
                            localRepository.removeUnsent(chatMessage)

                            // Send broadcast to eventually open ChatActivity
                            val intent = Intent(Const.CHAT_BROADCAST_NAME).apply {
                                val fcmChat = FcmChat(message.room, message.member.id, 0)
                                putExtra(Const.FCM_CHAT, fcmChat)
                            }
                            broadcastManager.sendBroadcast(intent)
                        }, { t ->
                            Utils.logwithTag("ChatMessageViewModel", t.message)
                            chatMessage.sendingStatus = ChatMessage.STATUS_ERROR
                            localRepository.replaceMessage(chatMessage)
                            val intent = Intent(Const.CHAT_BROADCAST_NAME)
                            broadcastManager.sendBroadcast(intent)
                        })
        )
    }
}