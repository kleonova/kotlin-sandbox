package lev.learn.sandbox.auth.service.repository

import lev.learn.sandbox.auth.service.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {
    fun findByUsername(username: String): User? = transaction {
        UsersTable
            .select { UsersTable.username eq username }
            .map(::toUser)
            .singleOrNull()
    }

    fun save(user: User): User = transaction {
        val exists = UsersTable.slice(UsersTable.id)
            .select { UsersTable.username eq user.username }
            .empty().not()

        if (!exists) {
            UsersTable.insert {
                it[id] = user.id
                it[username] = user.username
                it[email] = user.email
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[createdAt] = user.createdAt
                it[updatedAt] = user.updatedAt
            }
        } else {
            UsersTable.update({ UsersTable.username eq user.username }) {
                it[email] = user.email
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[updatedAt] = user.updatedAt
            }
        }
        user
    }

    private fun toUser(row: ResultRow): User = User(
        id = row[UsersTable.id],
        username = row[UsersTable.username],
        email = row[UsersTable.email],
        firstName = row[UsersTable.firstName],
        lastName = row[UsersTable.lastName],
        createdAt = row[UsersTable.createdAt],
        updatedAt = row[UsersTable.updatedAt]
    )
}