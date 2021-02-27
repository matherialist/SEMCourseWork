package com.holeaf.api.model

data class Role(
    val id: Int,
    val name: String,
    val chatEveryone: Boolean,
    val chatCourier: Boolean,
    val chatSupplier: Boolean,
    val chatManager: Boolean,
    val chatClient: Boolean,
    val chatSeller: Boolean,
    val mapClient: Boolean,
    val mapSeller: Boolean,
    val mapManager: Boolean,
    val mapCourier: Boolean,
    val storeClient: Boolean,
    val storeSeller: Boolean,
    val supplies: Boolean,
    val orders: Boolean,
    val telemetry: Boolean,
    val stats: Boolean,
    val keys: Boolean,
    val users: Boolean
)