package top.contins.synapse.domain.usecase.chat

import top.contins.synapse.domain.model.chat.Message
import top.contins.synapse.domain.repository.chat.ChatRepository
import javax.inject.Inject

class SaveMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: Message) {
        repository.saveMessage(message)
    }
}
