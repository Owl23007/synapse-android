package top.contins.synapse.network.model

import com.google.gson.annotations.SerializedName

data class UserSelfProfileResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("signature") val signature: String?,
    @SerializedName("avatarImage") val avatarImage: String?,
    @SerializedName("backgroundImage") val backgroundImage: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("createTime") val createTime: String?,
    @SerializedName("updateTime") val updateTime: String?
)
