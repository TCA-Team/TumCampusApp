package de.tum.`in`.tumcampusapp.api.app

import de.tum.`in`.tumcampusapp.api.backend.CampusApiProto
import de.tum.`in`.tumcampusapp.api.backend.CampusGrpc
import de.tum.`in`.tumcampusapp.api.backend.GetTopNewsRequest
import io.grpc.ManagedChannelBuilder

class BackendHelper {

    // Dummy example code
    public fun getBackendConnection() {
        var managedChannel = ManagedChannelBuilder.forAddress("https://www.google.com", 443).build()
        var blockingStub = CampusGrpc.newBlockingStub(managedChannel)

        var newsReq = GetTopNewsRequest.newBuilder().build()
        var topNews = blockingStub.getTopNews(newsReq)

    }
}