package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String,
    val authorId: Long,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val ownedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment? = null
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem

data class TimeDescriptor(
    override val id: Long,
    val description: String
) : FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType {
    IMAGE
}