package asaa.bachelor.bleconnector.bt

import java.util.*

enum class CommonCharacteristics(val officialUUID: String) {
    DeviceName("0x2A00"),
    Appearance("0x2A01"),
    PeripheralPrivacyFlag("0x2A02"),
    ReconnectionAddress("0x2A03"),
    PeripheralPreferredConnectionParameters("0x2A04"),
    ServiceChanged("0x2A05"),
    AlertLevel("0x2A06"),
    TxPowerLevel("0x2A07"),
    DateTime("0x2A08"),
    DayofWeek("0x2A09"),
    DayDateTime("0x2A0A"),
    ExactTime256("0x2A0C"),
    DSTOffset("0x2A0D"),
    TimeZone("0x2A0E"),
    LocalTimeInformation("0x2A0F"),
    TimewithDST("0x2A11"),
    TimeAccuracy("0x2A12"),
    TimeSource("0x2A13"),
    ReferenceTimeInformation("0x2A14"),
    TimeUpdateControlPoint("0x2A16"),
    TimeUpdateState("0x2A17"),
    GlucoseMeasurement("0x2A18"),
    BatteryLevel("0x2A19"),
    TemperatureMeasurement("0x2A1C"),
    TemperatureType("0x2A1D"),
    IntermediateTemperature("0x2A1E"),
    MeasurementInterval("0x2A21"),
    BootKeyboardInputReport("0x2A22"),
    SystemID("0x2A23"),
    ModelNumberString("0x2A24"),
    SerialNumberString("0x2A25"),
    FirmwareRevisionString("0x2A26"),
    HardwareRevisionString("0x2A27"),
    SoftwareRevisionString("0x2A28"),
    ManufacturerNameString("0x2A29"),
    RegulatoryCertificationDataList("0x2A2A"),
    CurrentTime("0x2A2B"),
    ScanRefresh("0x2A31"),
    BootKeyboardOutputReport("0x2A32"),
    BootMouseInputReport("0x2A33"),
    GlucoseMeasurementContext("0x2A34"),
    BloodPressureMeasurement("0x2A35"),
    IntermediateCuffPressure("0x2A36"),
    HeartRateMeasurement("0x2A37"),
    BodySensorLocation("0x2A38"),
    HeartRateControlPoint("0x2A39"),
    AlertStatus("0x2A3F"),
    RingerControlPoint("0x2A40"),
    RingerSetting("0x2A41"),
    AlertCategoryIDBitMask("0x2A42"),
    AlertCategoryID("0x2A43"),
    AlertNotificationControlPoint("0x2A44"),
    UnreadAlertStatus("0x2A45"),
    NewAlert("0x2A46"),
    SupportedNewAlertCategory("0x2A47"),
    SupportedUnreadAlertCategory("0x2A48"),
    BloodPressureFeature("0x2A49"),
    HIDInformation("0x2A4A"),
    ReportMap("0x2A4B"),
    HIDControlPoint("0x2A4C"),
    Report("0x2A4D"),
    ProtocolMode("0x2A4E"),
    ScanIntervalWindow("0x2A4F"),
    PnPID("0x2A50"),
    GlucoseFeature("0x2A51"),
    RecordAccessControlPoint("0x2A52"),
    RSCMeasurement("0x2A53"),
    RSCFeature("0x2A54"),
    SCControlPoint("0x2A55"),
    Aggregate("0x2A5A"),
    CSCMeasurement("0x2A5B"),
    CSCFeature("0x2A5C"),
    SensorLocation("0x2A5D"),
    PLXSpotCheckMeasurement("0x2A5E"),
    PLXContinuousMeasurement("0x2A5F"),
    PLXFeatures("0x2A60"),
    CyclingPowerMeasurement("0x2A63"),
    CyclingPowerVector("0x2A64"),
    CyclingPowerFeature("0x2A65"),
    CyclingPowerControlPoint("0x2A66"),
    LocationandSpeed("0x2A67"),
    Navigation("0x2A68"),
    PositionQuality("0x2A69"),
    LNFeature("0x2A6A"),
    LNControlPoint("0x2A6B"),
    Elevation("0x2A6C"),
    Pressure("0x2A6D"),
    Temperature("0x2A6E"),
    Humidity("0x2A6F"),
    TrueWindSpeed("0x2A70"),
    TrueWindDirection("0x2A71"),
    ApparentWindSpeed("0x2A72"),
    ApparentWindDirection("0x2A73"),
    GustFactor("0x2A74"),
    PollenConcentration("0x2A75"),
    UVIndex("0x2A76"),
    Irradiance("0x2A77"),
    Rainfall("0x2A78"),
    WindChill("0x2A79"),
    HeatIndex("0x2A7A"),
    DewPoint("0x2A7B"),
    DescriptorValueChanged("0x2A7D"),
    AerobicHeartRateLowerLimit("0x2A7E"),
    AerobicThreshold("0x2A7F"),
    Age("0x2A80"),
    AnaerobicHeartRateLowerLimit("0x2A81"),
    AnaerobicHeartRateUpperLimit("0x2A82"),
    AnaerobicThreshold("0x2A83"),
    AerobicHeartRateUpperLimit("0x2A84"),
    DateofBirth("0x2A85"),
    DateofThresholdAssessment("0x2A86"),
    EmailAddress("0x2A87"),
    FatBurnHeartRateLowerLimit("0x2A88"),
    FatBurnHeartRateUpperLimit("0x2A89"),
    FirstName("0x2A8A"),
    FiveZoneHeartRateLimits("0x2A8B"),
    Gender("0x2A8C"),
    HeartRateMax("0x2A8D"),
    Height("0x2A8E"),
    HipCircumference("0x2A8F"),
    LastName("0x2A90"),
    MaximumRecommendedHeartRate("0x2A91"),
    RestingHeartRate("0x2A92"),
    SportTypeforAerobicandAnaerobicThresholds("0x2A93"),
    ThreeZoneHeartRateLimits("0x2A94"),
    TwoZoneHeartRateLimits("0x2A95"),
    VO2Max("0x2A96"),
    WaistCircumference("0x2A97"),
    Weight("0x2A98"),
    DatabaseChangeIncrement("0x2A99"),
    UserIndex("0x2A9A"),
    BodyCompositionFeature("0x2A9B"),
    BodyCompositionMeasurement("0x2A9C"),
    WeightMeasurement("0x2A9D"),
    WeightScaleFeature("0x2A9E"),
    UserControlPoint("0x2A9F"),
    MagneticFluxDensity2D("0x2AA0"),
    MagneticFluxDensity3D("0x2AA1"),
    Language("0x2AA2"),
    BarometricPressureTrend("0x2AA3"),
    BondManagementControlPoint("0x2AA4"),
    BondManagementFeature("0x2AA5"),
    CentralAddressResolution("0x2AA6"),
    CGMMeasurement("0x2AA7"),
    CGMFeature("0x2AA8"),
    CGMStatus("0x2AA9"),
    CGMSessionStartTime("0x2AAA"),
    CGMSessionRunTime("0x2AAB"),
    CGMSpecificOpsControlPoint("0x2AAC"),
    IndoorPositioningConfiguration("0x2AAD"),
    Latitude("0x2AAE"),
    Longitude("0x2AAF"),
    LocalNorthCoordinate("0x2AB0"),
    LocalEastCoordinate("0x2AB1"),
    FloorNumber("0x2AB2"),
    Altitude("0x2AB3"),
    Uncertainty("0x2AB4"),
    LocationName("0x2AB5"),
    URI("0x2AB6"),
    HTTPHeaders("0x2AB7"),
    HTTPStatusCode("0x2AB8"),
    HTTPEntityBody("0x2AB9"),
    HTTPControlPoint("0x2ABA"),
    HTTPSSecurity("0x2ABB"),
    TDSControlPoint("0x2ABC"),
    OTSFeature("0x2ABD"),
    objectname("0x2ABE"),
    objecttype("0x2ABF"),
    objectsize("0x2AC0"),
    objectfirstcreated("0x2AC1"),
    objectlastmodified("0x2AC2"),
    objectID("0x2AC3"),
    objectproperties("0x2AC4"),
    objectactioncontrolpoint("0x2AC5"),
    objectlistcontrolpoint("0x2AC6"),
    objectlistfilter("0x2AC7"),
    objectchanged("0x2AC8"),
    ResolvablePrivateAddressOnly("0x2AC9"),
    Unspecified("0x2ACA"),
    DirectoryListing("0x2ACB"),
    FitnessMachineFeature("0x2ACC"),
    TreadmillData("0x2ACD"),
    CrossTrainerData("0x2ACE"),
    StepClimberData("0x2ACF"),
    StairClimberData("0x2AD0"),
    RowerData("0x2AD1"),
    IndoorBikeData("0x2AD2"),
    TrainingStatus("0x2AD3"),
    SupportedSpeedRange("0x2AD4"),
    SupportedInclinationRange("0x2AD5"),
    SupportedResistanceLevelRange("0x2AD6"),
    SupportedHeartRateRange("0x2AD7"),
    SupportedPowerRange("0x2AD8"),
    FitnessMachineControlPoint("0x2AD9"),
    FitnessMachineStatus("0x2ADA"),
    MeshProvisioningDataIn("0x2ADB"),
    MeshProvisioningDataOut("0X2ADC"),
    MeshProxyDataIn("0X2ADD"),
    MeshProxyDataOut("0X2ADE"),
    AverageCurrent("0X2AE0"),
    AverageVoltage("0X2AE1"),
    Boolean("0X2AE2"),
    ChromaticDistanceFromPlanckian("0X2AE3"),
    ChromaticityCoordinates("0X2AE4"),
    ChromaticityinCCTAndDuvValues("0X2AE5"),
    ChromaticityTolerance("0X2AE6"),
    ColorRenderingIndex("0X2AE7"),
    Coefficient("0X2AE8"),
    CorrelatedColorTemperature("0X2AE9"),
    Count16("0X2AEA"),
    Count24("0X2AEB"),
    CountryCode("0X2AEC"),
    DateUTC("0X2AED"),
    ElectricCurrent("0X2AEE"),
    ElectricCurrentRange("0X2AEF"),
    ElectricCurrentSpecification("0X2AF0"),
    ElectricCurrentStatistics("0X2AF1"),
    Energy("0X2AF2"),
    EnergyInAPeriodOfDay("0X2AF3"),
    EventStatistics("0X2AF4"),
    FixedString16("0X2AF5"),
    FixedString24("0X2AF6"),
    FixedString36("0X2AF7"),
    FixedString8("0X2AF8"),
    GenericLevel("0X2AF9"),
    GlobalTradeItemNumber("0X2AFA"),
    Illuminance("0X2AFB"),
    LuminousEfficacy("0X2AFC"),
    LuminousEnergy("0X2AFD"),
    LuminousExposure("0X2AFE"),
    LuminousFlux("0X2AFF"),
    LuminousFluxRange("0x2B00"),
    LuminousIntensity("0x2B01"),
    MassFlow("0x2B02"),
    PerceivedLightness("0x2B03"),
    Percentage8("0x2B04"),
    Power("0x2B05"),
    PowerSpecification("0x2B06"),
    RelativeRuntimeInACurrentRange("0x2B07"),
    RelativeRuntimeInAGenericLevelRange("0x2B08"),
    RelativeValueInAVoltageRange("0x2B09"),
    RelativeValueInAnIlluminanceRange("0x2B0A"),
    RelativeValueInAPeriodOfDay("0x2B0B"),
    RelativeValueInATemperatureRange("0x2B0C"),
    Temperature8("0x2B0D"),
    Temperature8InAPeriodOfDay("0x2B0E"),
    Temperature8Statistics("0x2B0F"),
    TemperatureRange("0x2B10"),
    TemperatureStatistics("0x2B11"),
    TimeDecihour8("0x2B12"),
    TimeExponential8("0x2B13"),
    TimeHour24("0x2B14"),
    TimeMillisecond24("0x2B15"),
    TimeSecond16("0x2B16"),
    TimeSecond8("0x2B17"),
    Voltage("0x2B18"),
    VoltageSpecification("0x2B19"),
    VoltageStatistics("0x2B1A"),
    VolumeFlow("0x2B1B"),
    ChromaticityCoordinate("0x2B1C"),
    RCFeature("0x2B1D"),
    RCSettings("0x2B1E"),
    ReconnectionConfigurationControlPoint("0x2B1F"),
    IDDStatusChanged("0x2B20"),
    IDDStatus("0x2B21"),
    IDDAnnunciationStatus("0x2B22"),
    IDDFeatures("0x2B23"),
    IDDStatusReaderControlPoint("0x2B24"),
    IDDCommandControlPoint("0x2B25"),
    IDDCommandData("0x2B26"),
    IDDRecordAccessControlPoint("0x2B27"),
    IDDHistoryData("0x2B28"),
    ClientSupportedFeatures("0x2B29"),
    DatabaseHash("0x2B2A"),
    BSSControlPoint("0x2B2B"),
    BSSResponse("0x2B2C"),
    EmergencyID("0x2B2D"),
    EmergencyText("0x2B2E"),
    EnhancedBloodPressureMeasurement("0x2B34"),
    EnhancedIntermediateCuffPressure("0x2B35"),
    BloodPressureRecord("0x2B36"),
    BREDRHandoverData("0x2B38"),
    BluetoothSIGData("0x2B39"),
    ServerSupportedFeatures("0x2B3A"),
    PhysicalActivityMonitorFeatures("0x2B3B"),
    GeneralActivityInstantaneousData("0x2B3C"),
    GeneralActivitySummaryData("0x2B3D"),
    CardioRespiratoryActivityInstantaneousData("0x2B3E"),
    CardioRespiratoryActivitySummaryData("0x2B3F"),
    StepCounterActivitySummaryData("0x2B40"),
    SleepActivityInstantaneousData("0x2B41"),
    SleepActivitySummaryData("0x2B42"),
    PhysicalActivityMonitorControlPoint("0x2B43"),
    CurrentSession("0x2B44"),
    Session("0x2B45"),
    PreferredUnits("0x2B46"),
    HighResolutionHeight("0x2B47"),
    MiddleName("0x2B48"),
    StrideLength("0x2B49"),
    Handedness("0x2B4A"),
    DeviceWearingPosition("0x2B4B"),
    FourZoneHeartRateLimits("0x2B4C"),
    HighIntensityExerciseThreshold("0x2B4D"),
    ActivityGoal("0x2B4E"),
    SedentaryIntervalNotification("0x2B4F"),
    CaloricIntake("0x2B50"),
    AudioInputState("0x2B77"),
    GainSettingsAttribute("0x2B78"),
    AudioInputType("0x2B79"),
    AudioInputStatus("0x2B7A"),
    AudioInputControlPoint("0x2B7B"),
    AudioInputDescription("0x2B7C"),
    VolumeState("0x2B7D"),
    VolumeControlPoint("0x2B7E"),
    VolumeFlags("0x2B7F"),
    OffsetState("0x2B80"),
    AudioLocation("0x2B81"),
    VolumeOffsetControlPoint("0x2B82"),
    AudioOutputDescription("0x2B83"),
    DeviceTimeFeature("0x2B8E"),
    DeviceTimeParameters("0x2B8F"),
    DeviceTime("0x2B90"),
    DeviceTimeControlPoint("0x2B91"),
    TimeChangeLogData("0x2B92"),
    ConstantToneExtensionEnable("0x2BAD"),
    AdvertisingConstantToneExtensionMinimumLength("0x2BAE"),
    AdvertisingConstantToneExtensionMinimumTransmitCount("0x2BAF"),
    AdvertisingConstantToneExtensionTransmitDuration("0x2BB0"),
    AdvertisingConstantToneExtensionInterval("0x2BB1"),
    AdvertisingConstantToneExtensionPHY("0x2BB2"),
    Mute("0x2BC3");

    val longUUID = ("0000" + officialUUID.takeLast(4) + "-0000-1000-8000-00805F9B34FB").toLowerCase()
    val uuid = UUID.fromString(longUUID)

    companion object {
        private val map = CommonCharacteristics.values().associateBy(CommonCharacteristics::longUUID)
        fun mapIfExists(uuid: String): CommonCharacteristics? = map[uuid.toLowerCase()]

    }
}