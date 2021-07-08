package asaa.bachelor.bleconnector.bt.common

import java.util.*

enum class CommonDescriptors(val officialUUID: String) {
    CharacteristicExtendedProperties("0x2900"),
    CharacteristicUserDescription("0x2901"),
    ClientCharacteristicConfiguration("0x2902"),
    ServerCharacteristicConfiguration("0x2903"),
    CharacteristicPresentationFormat("0x2904"),
    CharacteristicAggregateFormat("0x2905"),
    ValidRange("0x2906"),
    ExternalReportReference("0x2907"),
    ReportReference("0x2908"),
    NumberofDigitals("0x2909"),
    ValueTriggerSetting("0x290A"),
    EnvironmentalSensingConfiguration("0x290B"),
    EnvironmentalSensingMeasurement("0x290C"),
    EnvironmentalSensingTriggerSetting("0x290D"),
    TimeTriggerSetting("0x290E"),
    CompleteBREDRTransportBlockData("0x290F");

    val longUUID = ("0000" + officialUUID.takeLast(4) + "-0000-1000-8000-00805F9B34FB").toLowerCase()
    val uuid = UUID.fromString(longUUID)

    companion object {
        private val map = CommonDescriptors.values().associateBy(CommonDescriptors::longUUID)
        fun mapIfExists(uuid: String): CommonDescriptors? = map[uuid.toLowerCase()]

    }
}