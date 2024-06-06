package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post


@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    @Embedded
    val attachment: AttachmentEntity?,
    val show: Boolean = true,
    val authorId: Long = 0,
) {
    fun toDto() =
        Post(
            id = id,
            author = author,
            authorAvatar = authorAvatar,
            content = content,
            published = published,
            likedByMe = likedByMe,
            likes = likes,
            attachment = attachment?.toDto(),
            authorId = authorId)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                likes = dto.likes,
                attachment = AttachmentEntity.fromDto(dto.attachment),
                authorId = dto.authorId
            )
    }
}

@Entity
data class AttachmentEntity(
    val url: String,
    val description: String,
    val type: AttachmentType
) {
    fun toDto() = Attachment(url, description, type)

    companion object {
        fun fromDto(dto: Attachment?): AttachmentEntity? {
            return if (dto != null) AttachmentEntity(dto.url, dto.description, dto.type) else null
        }
    }
}

fun List<PostEntity>.toDto() = map { it.toDto() }
fun List<Post>.toEntity(show: Boolean = true) = map { PostEntity.fromDto(it) }
    .map { it.copy(show = show) }