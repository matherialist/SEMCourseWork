data class AuthUser(val login: String, val password: String)

data class NewUser(val login: String, val password: String, val email: String)

data class ResetPasswordUser(val email: String)

data class PlaceInfo(val id:Int, val user: Int, val longitude: Float, val latitude: Float)

data class UserInformation(val id:Int, val username:String, var email:String, var role:Int)