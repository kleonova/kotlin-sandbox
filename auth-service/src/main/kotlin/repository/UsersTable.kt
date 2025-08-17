package lev.learn.sandbox.auth.service.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 255).nullable()
    val firstName = varchar("first_name", 255).nullable()
    val lastName = varchar("last_name", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}