package asaa.bachelor.bleconnector.bt.common

import java.util.*

enum class CommonServices(val officialUUID: String) {
    GenericAccess("0x1800"),
    GenericAttribute("0x1801"),
    ImmediateAlert("0x1802"),
    LinkLoss("0x1803"),
    TxPower("0x1804"),
    CurrentTime("0x1805"),
    ReferenceTimeUpdate("0x1806"),
    NextDSTChange("0x1807"),
    Glucose("0x1808"),
    HealthThermometer("0x1809"),
    DeviceInformation("0x180A"),
    HeartRate("0x180D"),
    PhoneAlertStatus("0x180E"),
    Battery("0x180F"),
    BloodPressure("0x1810"),
    AlertNotification("0x1811"),
    HumanInterfaceDevice("0x1812"),
    ScanParameters("0x1813"),
    RunningSpeedandCadence("0x1814"),
    AutomationIO("0x1815"),
    CyclingSpeedandCadence("0x1816"),
    CyclingPower("0x1818"),
    LocationandNavigation("0x1819"),
    EnvironmentalSensing("0x181A"),
    BodyComposition("0x181B"),
    UserData("0x181C"),
    WeightScale("0x181D"),
    BondManagement("0x181E"),
    ContinuousGlucoseMonitoring("0x181F"),
    InternetProtocolSupport("0x1820"),
    IndoorPositioning("0x1821"),
    PulseOximeter("0x1822"),
    HTTPProxy("0x1823"),
    TransportDiscovery("0x1824"),
    ObjectTransfer("0x1825"),
    FitnessMachine("0x1826"),
    MeshProvisioning("0x1827"),
    MeshProxy("0x1828"),
    ReconnectionConfiguration("0x1829"),
    InsulinDelivery("0x183A"),
    BinarySensor("0x183B"),
    EmergencyConfiguration("0x183C"),
    PhysicalActivityMonitor("0x183E"),
    AudioInputControl("0x1843"),
    VolumeControl("0x1844"),
    VolumeOffsetControl("0x1845"),
    DeviceTime("0x1847"),
    ConstantToneExtension("0x184A"),
    MicrophoneControl("0x184D");

    val longUUID = ("0000" + officialUUID.takeLast(4) + "-0000-1000-8000-00805F9B34FB").toLowerCase()
    val uuid = UUID.fromString(longUUID)

    companion object {
        private val map = CommonServices.values().associateBy(CommonServices::longUUID)
        fun mapIfExists(uuid: String): CommonServices? = map[uuid.toLowerCase()]

    }
}