#include <Arduino.h>
#include <BLEServer.h>
#include <string>

class Util {
public:
  static String convertBLEStatus(esp_ble_key_mask_t value) {
    if (value == ESP_BT_STATUS_SUCCESS) {
      return "SUCCESS";
    }
    if (value == ESP_BT_STATUS_FAIL) {
      return "FAIL";
    }
    if (value == ESP_BT_STATUS_NOT_READY) {
      return "NOT_READY";
    }
    if (value == ESP_BT_STATUS_NOMEM) {
      return "NOMEM";
    }
    if (value == ESP_BT_STATUS_BUSY) {
      return "BUSY";
    }
    if (value == ESP_BT_STATUS_DONE) {
      return "DONE";
    }
    if (value == ESP_BT_STATUS_UNSUPPORTED) {
      return "UNSUPPORTED";
    }
    if (value == ESP_BT_STATUS_PARM_INVALID) {
      return "PARM_INVALID";
    }
    if (value == ESP_BT_STATUS_UNHANDLED) {
      return "UNHANDLED";
    }
    if (value == ESP_BT_STATUS_AUTH_FAILURE) {
      return "AUTH_FAILURE";
    }
    if (value == ESP_BT_STATUS_RMT_DEV_DOWN) {
      return "RMT_DEV_DOWN";
    }
    if (value == ESP_BT_STATUS_AUTH_REJECTED) {
      return "AUTH_REJECTED";
    }
    if (value == ESP_BT_STATUS_INVALID_STATIC_RAND_ADDR) {
      return "INVALID_STATIC_RAND_ADDR";
    }
    if (value == ESP_BT_STATUS_PENDING) {
      return "PENDING";
    }
    if (value == ESP_BT_STATUS_UNACCEPT_CONN_INTERVAL) {
      return "UNACCEPT_CONN_INTERVAL";
    }
    if (value == ESP_BT_STATUS_PARAM_OUT_OF_RANGE) {
      return "PARAM_OUT_OF_RANGE";
    }
    if (value == ESP_BT_STATUS_TIMEOUT) {
      return "TIMEOUT";
    }
    if (value == ESP_BT_STATUS_PEER_LE_DATA_LEN_UNSUPPORTED) {
      return "PEER_LE_DATA_LEN_UNSUPPORTED";
    }
    if (value == ESP_BT_STATUS_CONTROL_LE_DATA_LEN_UNSUPPORTED) {
      return "CONTROL_LE_DATA_LEN_UNSUPPORTED";
    }
    if (value == ESP_BT_STATUS_ERR_ILLEGAL_PARAMETER_FMT) {
      return "ERR_ILLEGAL_PARAMETER_FMT";
    }
    if (value == ESP_BT_STATUS_MEMORY_FULL) {
      return "MEMORY_FULL";
    }
    return "unknown";
  }

  static String convertBLEServerEvent(esp_gatts_cb_event_t value) {
    if (value == ESP_GATTS_REG_EVT) {
      return "REG_EVT";
    }
    if (value == ESP_GATTS_READ_EVT) {
      return "READ_EVT";
    }
    if (value == ESP_GATTS_WRITE_EVT) {
      return "WRITE_EVT";
    }
    if (value == ESP_GATTS_EXEC_WRITE_EVT) {
      return "EXEC_WRITE_EVT";
    }
    if (value == ESP_GATTS_MTU_EVT) {
      return "MTU_EVT";
    }
    if (value == ESP_GATTS_CONF_EVT) {
      return "CONF_EVT";
    }
    if (value == ESP_GATTS_UNREG_EVT) {
      return "UNREG_EVT";
    }
    if (value == ESP_GATTS_CREATE_EVT) {
      return "CREATE_EVT";
    }
    if (value == ESP_GATTS_ADD_INCL_SRVC_EVT) {
      return "ADD_INCL_SRVC_EVT";
    }
    if (value == ESP_GATTS_ADD_CHAR_EVT) {
      return "ADD_CHAR_EVT";
    }
    if (value == ESP_GATTS_ADD_CHAR_DESCR_EVT) {
      return "ADD_CHAR_DESCR_EVT";
    }
    if (value == ESP_GATTS_DELETE_EVT) {
      return "DELETE_EVT";
    }
    if (value == ESP_GATTS_START_EVT) {
      return "START_EVT";
    }
    if (value == ESP_GATTS_STOP_EVT) {
      return "STOP_EVT";
    }
    if (value == ESP_GATTS_CONNECT_EVT) {
      return "CONNECT_EVT";
    }
    if (value == ESP_GATTS_DISCONNECT_EVT) {
      return "DISCONNECT_EVT";
    }
    if (value == ESP_GATTS_OPEN_EVT) {
      return "OPEN_EVT";
    }
    if (value == ESP_GATTS_CANCEL_OPEN_EVT) {
      return "CANCEL_OPEN_EVT";
    }
    if (value == ESP_GATTS_CLOSE_EVT) {
      return "CLOSE_EVT";
    }
    if (value == ESP_GATTS_LISTEN_EVT) {
      return "LISTEN_EVT";
    }
    if (value == ESP_GATTS_CONGEST_EVT) {
      return "CONGEST_EVT";
    }
    if (value == ESP_GATTS_RESPONSE_EVT) {
      return "RESPONSE_EVT";
    }
    if (value == ESP_GATTS_CREAT_ATTR_TAB_EVT) {
      return "CREAT_ATTR_TAB_EVT";
    }
    if (value == ESP_GATTS_SET_ATTR_VAL_EVT) {
      return "SET_ATTR_VAL_EVT";
    }
    if (value == ESP_GATTS_SEND_SERVICE_CHANGE_EVT) {
      return "SEND_SERVICE_CHANGE_EVT";
    }
    return "unknown";
  }

  static String millisToTime(unsigned long millis) {
    unsigned int seconds = millis / 1000 % 60;
    unsigned int minutes = millis / 1000 / 60 % 60;
    unsigned int hour = millis / 1000 / 60 / 60 % 24;
    unsigned int day = millis / 1000 / 60 / 60 / 24 % 365;
    return String(day) + ":" + String(hour) + ":" + String(minutes) + ":" + String(seconds);
  }
};
