package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    //выбирает PostEntity со статусом "показывать"
    @Query("SELECT * FROM PostEntity WHERE shown = 1 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    //меняет статус на "показывать"
    @Query("UPDATE PostEntity SET shown = 1 WHERE shown = 0")
    suspend fun showAll()

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getById(id: Long): PostEntity

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("""
        UPDATE PostEntity SET
        content = :content,
        likedByMe = :likedByMe,
        likes = :likes
        WHERE id = :id
        """)
    suspend fun updateContentById(id: Long, content: String, likedByMe: Boolean, likes: Int)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post)
        else updateContentById(post.id, post.content, post.likedByMe, post.likes)

    suspend fun saveOld(post: PostEntity) = insert(post)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long)

    @Query("UPDATE PostEntity SET id = :id WHERE id = 0")
    suspend fun updatePostId(id: Long)

    @Query("SELECT * FROM PostEntity ORDER BY ID DESC LIMIT 1")
    suspend fun selectLast(): PostEntity

    @Query("DELETE FROM PostEntity")
    suspend fun clear()

}
