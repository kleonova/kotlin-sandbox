package lev.learn.sandbox.auth.service.repository

import lev.learn.sandbox.auth.service.model.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserRepository {
    fun findByUsername(username: String): User? = transaction {
        UsersTable
            .selectAll()
            .where { UsersTable.username eq username }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun save(user: User): User = transaction {
        val exists = UsersTable
            .select(UsersTable.id)
            .where { UsersTable.id eq user.id }
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

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id],
        username = this[UsersTable.username],
        email = this[UsersTable.email],
        firstName = this[UsersTable.firstName],
        lastName = this[UsersTable.lastName],
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt]
    )
}
