package com.my.kizzy.rpc

import com.my.kizzy.repository.KizzyRepository

sealed class RpcImage {
    abstract suspend fun resolveImage(repository: KizzyRepository): String?

    class ExternalImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: KizzyRepository): String? {
            return repository.getImage(image)
        }
    }
}
