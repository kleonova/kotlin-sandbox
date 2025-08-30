package lev.learn.sandbox.auth.service.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import lev.learn.sandbox.auth.service.repository.DbConstants.MAX_VARCHAR_LENGTH

object UsersTable : Table("users") {
    val id = uuid("id")
    val username = varchar("username", MAX_VARCHAR_LENGTH).uniqueIndex()
    val email = varchar("email", MAX_VARCHAR_LENGTH).nullable()
    val firstName = varchar("first_name", MAX_VARCHAR_LENGTH).nullable()
    val lastName = varchar("last_name", MAX_VARCHAR_LENGTH).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}
