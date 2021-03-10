package asaa.bachelor.bleconnector.bt.common

enum class CustomCharacteristic(val uuid: String) {
    READ_CHARACTERISTIC("fbdc45f2-8337-467b-8019-e7db05355215"),
    WRITE_CHARACTERISTIC("f847b552-f7dc-42e9-9dad-8452d7ad7681"),
    WRITE_WO_RESPONSE_CHARACTERISTIC("59bf9b12-ba39-4983-89bc-e541c2091535"),
    NOTIFY_CHARACTERISTIC("1ee1d0fc-6f3c-4c6a-ac1c-c54d2a97f932"),
    INDICATE_CHARACTERISTIC("83157f66-7c91-431e-a037-7c2b9e594ef6");

    companion object {
        private val map = CustomCharacteristic.values().associateBy(CustomCharacteristic::uuid)
        fun mapIfExists(uuid: String): CustomCharacteristic? = map[uuid.toLowerCase()]
    }
}

enum class CustomService(val uuid: String) {
    CUSTOM_SERVICE_1("26cb2f28-a4ba-49fc-856a-d57fe4d3dada");

    companion object {
        private val map = CustomService.values().associateBy(CustomService::uuid)
        fun mapIfExists(uuid: String): CustomService? = map[uuid.toLowerCase()]
    }
}

/*
26cb2f28-a4ba-49fc-856a-d57fe4d3dada SERVICE
fbdc45f2-8337-467b-8019-e7db05355215 READ
f847b552-f7dc-42e9-9dad-8452d7ad7681 WRITE
59bf9b12-ba39-4983-89bc-e541c2091535 WRITE_WO_RESPONSE
1ee1d0fc-6f3c-4c6a-ac1c-c54d2a97f932 NOTIFY
83157f66-7c91-431e-a037-7c2b9e594ef6 INDICATE
46ac40cc-7eaa-41a9-9964-956a984fd9c3 OPEN
ab191949-a8c0-438b-81ce-90b97f1858a8 OPEN
8c65f73d-ddab-4dd9-a2e0-f5a10ce7e252 OPEN
f3410822-00c0-4dd3-a60b-3cd6124fd323 OPEN
* */