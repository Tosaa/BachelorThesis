#include "util.cpp"
#include <Arduino.h>
#include <BLE2902.h>
#include <BLEAdvertising.h>
#include <BLECharacteristic.h>
#include <BLEDescriptor.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <GeneralUtils.h>

// #include <esp_gap_ble_api.h>
// CONSTANTS
#define SERVICE_UUID_MAIN "26cb2f28-a4ba-49fc-856a-d57fe4d3dada"
#define CHARACTERISTIC_UUID_READ "fbdc45f2-8337-467b-8019-e7db05355215"
#define CHARACTERISTIC_UUID_WIRTE_COMMAND "8c65f73d-ddab-4dd9-a2e0-f5a10ce7e252"
#define CHARACTERISTIC_UUID_WIRTE_SIZE "f3410822-00c0-4dd3-a60b-3cd6124fd323"
#define CHARACTERISTIC_UUID_NOTIFY "1ee1d0fc-6f3c-4c6a-ac1c-c54d2a97f932"
#define CHARACTERISTIC_UUID_INDICATE "83157f66-7c91-431e-a037-7c2b9e594ef6"
#define CHARACTERISTIC_UUID_CONN_INTERVAL "46ac40cc-7eaa-41a9-9964-956a984fd9c3"

BLEServer *mainServer;
BLEService *mainService;
BLECharacteristic *characteristicRead;
BLECharacteristic *characteristicWriteCommand;
BLECharacteristic *characteristicWriteValueSize;
BLECharacteristic *characteristicConnectionInterval;
BLECharacteristic *characteristicNotify;
BLE2902 *descriptorNotify;
BLECharacteristic *characteristicIndicate;
BLE2902 *descriptorIndicate;
esp_gatt_if_t m_gatts_if;
// store mac address of client
// retrieve BLEAddress object via BLEAddress(clientAddressSTring)
std::string clientAddressString;

String bigText;
String bigText2;

class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *mainServer, esp_ble_gatts_cb_param_t *params) {
    clientAddressString = BLEAddress(params->connect.remote_bda).toString();
    BLEAddress addr = BLEAddress(clientAddressString);
    Serial.println(String("Connection started: ") + addr.toString().c_str());

    // uint16_t test = 500;
    // mainServer->updateConnParams(*clientAddress->getNative(), test, test, 0x0000, 0x000A);
  }

  void onDisconnect(BLEServer *mainServer) {
    Serial.println("Connection ended");
    descriptorIndicate->setIndications(false);
    descriptorNotify->setNotifications(false);
    mainServer->startAdvertising();
  }
};

class ConnectionIntervalCallback : public BLECharacteristicCallbacks {

  void onWrite(BLECharacteristic *pCharacteristic) {
    BLEAddress clientAddress = BLEAddress(clientAddressString);

    // decode value
    String rawData = pCharacteristic->getValue().c_str();
    Serial.println("connection interval raw: " + rawData);
    uint16_t newInterval = *(uint16_t *)pCharacteristic->getData();

    // fix values
    uint16_t latency = 0x0000;
    uint16_t timeout = 0x01FA;
    Serial.printf("update connection parameters for %s: ", clientAddress.toString().c_str());
    Serial.printf("%d - %d, %d, %d \n", newInterval, newInterval, latency, timeout);
    mainServer->updateConnParams(*clientAddress.getNative(), newInterval, newInterval, latency, timeout);
  }
};

class DataSizeCallback : public BLECharacteristicCallbacks {

  void onWrite(BLECharacteristic *pCharacteristic) {

    String rawData = pCharacteristic->getValue().c_str();
    Serial.println("data size raw: " + rawData);
    uint16_t newSize = *(uint16_t *)pCharacteristic->getData();
    Serial.println("data size: " + String(newSize));
    String standardOutput = "Data Size " + String(newSize);
    if (newSize >= standardOutput.length()) {
      String content = "";
      for (int i = standardOutput.length(); i < newSize; i++) {
        content += (char)('a' + (i % 26));
      }
      bigText = standardOutput + content;
      Serial.println("text updated: " + bigText);
      characteristicRead->setValue(bigText.c_str());
      characteristicIndicate->setValue(bigText.c_str());
      characteristicNotify->setValue(bigText.c_str());
    }
  }
};

class CommandCallback : public BLECharacteristicCallbacks {

  void onWrite(BLECharacteristic *pCharacteristic) {
    // decode value
    std::string receivedData = pCharacteristic->getValue().c_str();
    receivedData = GeneralUtils::trim(receivedData);
    receivedData = GeneralUtils::toLower(receivedData);
    Serial.printf("raw Command: %s\n", receivedData.c_str());
    if (receivedData == "read") {
      int valueSize = characteristicNotify->getValue().length();

      int mtuSize = mainServer->getPeerMTU(mainServer->getConnId()) - 3;
      Serial.printf("current value size:%d, MTU Size:%d\n", valueSize, mtuSize);
      // This only works for notify because it does not need any confirmation
      // For Indicate we need to work with the esp gatts callback.
      // It is possible ! But it is a lot of
      for (int offset = 0; offset < valueSize; offset += mtuSize) {
        uint8_t *value = characteristicNotify->getData() + offset;
        uint16_t valueLength = mtuSize;
        if (valueSize - offset < mtuSize) {
          valueLength = valueSize - offset;
        }
        Serial.printf("notify:%s\n", String((char *)value).substring(0, valueLength).c_str());

        esp_err_t errRc = esp_ble_gatts_send_indicate(m_gatts_if, mainServer->getConnId(), characteristicNotify->getHandle(), valueLength, value, false);
        if (errRc != ESP_OK) {
          Serial.println("could not send notify");
        }
      }

      characteristicIndicate->indicate();
      characteristicNotify->notify(false);
    }
  }
};

class CharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onRead(BLECharacteristic *pCharacteristic) { Serial.println(pCharacteristic->toString().c_str() + String(": onRead")); }

  void onWrite(BLECharacteristic *pCharacteristic) { Serial.println(pCharacteristic->toString().c_str() + String(": onWrite ") + pCharacteristic->getValue().c_str()); }
  /*
    void onNotify(BLECharacteristic *pCharacteristic) { Serial.println(pCharacteristic->toString().c_str() + String(" ") + pCharacteristic->getValue().c_str()); }

    void onStatus(BLECharacteristic *pCharacteristic, Status s, uint32_t code) { Serial.println(pCharacteristic->toString().c_str() + String(": onStatus ") + s + String(" ") + code); }
    */
};

class DescriptorCallbacks : public BLEDescriptorCallbacks {};

static void esp_ble_gap_callback(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param) {
  if (event == ESP_GAP_BLE_UPDATE_CONN_PARAMS_EVT) {
    Serial.println("\tconnection update:");
    Serial.printf("\t\taddress:%s\n", BLEAddress(param->update_conn_params.bda).toString().c_str());
    Serial.printf("\t\tstatus:%s\n", Util::convertBLEStatus(param->update_conn_params.status));
    Serial.printf("\t\tmin int:%d\n", param->update_conn_params.min_int);
    Serial.printf("\t\tmax int:%d\n", param->update_conn_params.max_int);
    Serial.printf("\t\tlatency:%d\n", param->update_conn_params.latency);
    Serial.printf("\t\tconnection interval:%d\n", param->update_conn_params.conn_int);
    Serial.printf("\t\ttimeout:%d\n", param->update_conn_params.timeout);
  }
  return;
};

// set this callback propably overrides the original callback which is needed to perform tasks based on the event
// esp_ble_gatts_register_callback(esp_ble_gatts_callback);
// Using Device::setCustomGattsHandler does not override the original callback
static void esp_ble_gatts_callback(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param) {
  unsigned long timeNow = millis();
  Serial.print(Util::millisToTime(timeNow) + " ");
  Serial.println(Util::convertBLEServerEvent(event).c_str());
  if (event == ESP_GATTS_READ_EVT) {
    Serial.printf("\thandle:%d\n", param->read.handle);
    Serial.printf("\toffset:%d\n", param->read.offset);
  }
  if (event == ESP_GATTS_WRITE_EVT) {
    Serial.printf("\thandle:%d\n", param->write.handle);
    Serial.printf("\tvalue:%s\n", String((char *)param->write.value));
  }
  if (event == ESP_GATTS_CONF_EVT) {
    Serial.printf("\thandle:%d\n", param->conf.handle);
  }
  // save gatts_if if it is not saved yet
  m_gatts_if = gatts_if;
  return;
};

// ESP_GATTS_REG_EVT

void setup() {
  Serial.begin(115200);
  delay(4000);
  Serial.println("Start BLE Server");
  bigText = String("Hello World");
  BLEDevice::init("ESP32-T");

  mainServer = BLEDevice::createServer();
  mainService = mainServer->createService(BLEUUID(SERVICE_UUID_MAIN), 20);
  Serial.printf("mainService: %s\n", SERVICE_UUID_MAIN);
  Serial.println(mainService->toString().c_str());
  descriptorNotify = new BLE2902();
  descriptorIndicate = new BLE2902();

  characteristicRead = mainService->createCharacteristic(CHARACTERISTIC_UUID_READ, BLECharacteristic::PROPERTY_READ);
  Serial.printf("read 1: %s \n", CHARACTERISTIC_UUID_READ);
  Serial.println(characteristicRead->toString().c_str());

  characteristicWriteCommand = mainService->createCharacteristic(CHARACTERISTIC_UUID_WIRTE_COMMAND, BLECharacteristic::PROPERTY_WRITE);
  Serial.printf("write:%s \n", CHARACTERISTIC_UUID_WIRTE_COMMAND);
  Serial.println(characteristicWriteCommand->toString().c_str());

  characteristicWriteValueSize = mainService->createCharacteristic(CHARACTERISTIC_UUID_WIRTE_SIZE, BLECharacteristic::PROPERTY_WRITE);
  Serial.printf("write:%s \n", CHARACTERISTIC_UUID_WIRTE_SIZE);
  Serial.println(characteristicWriteValueSize->toString().c_str());

  characteristicNotify = mainService->createCharacteristic(CHARACTERISTIC_UUID_NOTIFY, BLECharacteristic::PROPERTY_NOTIFY);
  characteristicNotify->addDescriptor(descriptorNotify);
  Serial.printf("notify:%s \n", CHARACTERISTIC_UUID_NOTIFY);
  Serial.println(characteristicNotify->toString().c_str());

  characteristicConnectionInterval = mainService->createCharacteristic(CHARACTERISTIC_UUID_CONN_INTERVAL, BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_READ);
  Serial.printf("connection interval:%s \n", CHARACTERISTIC_UUID_CONN_INTERVAL);
  Serial.println(characteristicConnectionInterval->toString().c_str());

  characteristicIndicate = mainService->createCharacteristic(CHARACTERISTIC_UUID_INDICATE, BLECharacteristic::PROPERTY_INDICATE);
  characteristicIndicate->addDescriptor(descriptorIndicate);
  Serial.printf("indicate:%s \n", CHARACTERISTIC_UUID_INDICATE);
  Serial.println(characteristicIndicate->toString().c_str());

  mainServer->setCallbacks(new ServerCallbacks());
  characteristicRead->setValue(bigText.c_str());
  characteristicRead->setCallbacks(new CharacteristicCallbacks());
  characteristicNotify->setValue(bigText.c_str());
  characteristicNotify->setCallbacks(new CharacteristicCallbacks());
  characteristicWriteValueSize->setCallbacks(new DataSizeCallback());
  characteristicWriteCommand->setCallbacks(new CommandCallback());
  characteristicIndicate->setValue(bigText.c_str());
  characteristicIndicate->setCallbacks(new CharacteristicCallbacks());
  characteristicConnectionInterval->setCallbacks(new ConnectionIntervalCallback());

  // esp_ble_gap_register_callback(gap_ble_callback);

  delay(500);
  mainService->start();
  esp_ble_adv_channel_t channel_map = ADV_CHNL_37;
  BLEAdvertising *advertising = BLEDevice::getAdvertising();
  advertising->setMinInterval(0x0022);
  advertising->addServiceUUID(SERVICE_UUID_MAIN);
  advertising->setScanResponse(true);
  advertising->setMinPreferred(0x06);
  advertising->setMinPreferred(0x12);
  advertising->setAdvertisementChannelMap(channel_map);

  BLEDevice::startAdvertising();
  BLEDevice::setCustomGattsHandler(esp_ble_gatts_callback);
  BLEDevice::setCustomGapHandler(esp_ble_gap_callback);
  // esp_ble_gatts_register_callback(esp_ble_gatts_callback);
}

void loop() {
  unsigned long timeNow = millis();

  Serial.println("Round " + Util::millisToTime(timeNow));
  // time between send notify and indicate (default 3 sec: 3 * 1000)
  delay(5000);
  // characteristicNotify->setValue(Util::millisToTime(timeNow).c_str());
  // characteristicNotify->notify();
  // characteristicIndicate->setValue(String(timeNow).c_str());
  // characteristicIndicate->indicate();
}
