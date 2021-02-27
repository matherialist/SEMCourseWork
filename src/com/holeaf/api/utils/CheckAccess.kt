//package com.holeaf.api.utils
//
//import com.holeaf.api.Roles
//import com.holeaf.api.Users
//import io.ktor.application.*
//import io.ktor.auth.*
//import io.ktor.http.*
//import io.ktor.response.*
//import io.ktor.util.pipeline.*
//import kotlinx.coroutines.launch
//import org.jetbrains.exposed.sql.ResultRow
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.select
//import org.jetbrains.exposed.sql.transactions.transaction
//
//suspend fun PipelineContext<Unit, ApplicationCall>.holeafApplication.validationInterface.checkAccess(
//    call: ApplicationCall,
//    check: (role: ResultRow?) -> Boolean,
//    code: suspend () -> Unit
//) {
//    val principal = call.principal<UserIdPrincipal>()!!
//    if (transaction {
//            val users = Users.select { Users.username eq principal.name }
//            val user = users.single()
//            val roleId = user[Users.roleId]
//            val role = Roles.select { Roles.id eq roleId }.singleOrNull()
//            print(role)
//            if (!check(role)) {
//                launch {
//                    call.respond(HttpStatusCode.Forbidden)
//                }
//                return@transaction false
//            }
//            return@transaction true
//        }) {
//        code()
//    }
//}
