package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = posts.isEmpty()
)

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)
