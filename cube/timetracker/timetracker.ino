#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_DRV2605.h>
#include <WiFi.h>
#include <WebSocketsServer.h>

// === WLAN-Konfiguration ===
const char* HOME_SSID     = "HOME_SSID";
const char* HOME_PASSWORD = "HOME_PASSWORD";

const char* AP_SSID     = "WuerfelTracker";
const char* AP_PASSWORD = "wuerfel1234";

const unsigned long WIFI_TIMEOUT_MS = 15000;

// === Sensor, Haptik & WebSocket ===
Adafruit_MPU6050 mpu;
Adafruit_DRV2605 drv;
WebSocketsServer webSocket(81);

bool drvReady = false;
bool apMode = false;

// === Face-Erkennung ===
const float FACE_THRESHOLD = 7.0;
const unsigned long STABLE_TIME_MS = 500;
const unsigned long HEARTBEAT_MS = 5000;

// Welcher Effekt wird bei Seitenwechsel abgespielt?
// 1 = Strong Click, gute Default-Wahl.
// Andere Favoriten: 10 (Double Click), 24 (Sharp Tick), 47 (Buzz)
const uint8_t FACE_CHANGE_EFFECT = 1;

int currentFace = 0;
int candidateFace = 0;
unsigned long candidateSince = 0;
unsigned long lastHeartbeat = 0;

int detectFace(float x, float y, float z) {
  if (z >  FACE_THRESHOLD) return 1;
  if (z < -FACE_THRESHOLD) return 2;
  if (x >  FACE_THRESHOLD) return 3;
  if (x < -FACE_THRESHOLD) return 4;
  if (y >  FACE_THRESHOLD) return 5;
  if (y < -FACE_THRESHOLD) return 6;
  return 0;
}

// === Haptisches Feedback ausloesen ===
void playHapticFeedback(uint8_t effect) {
  if (!drvReady) return;  // kein DRV2605L -> stumm weiter
  drv.setWaveform(0, effect);  // Slot 0: gewuenschter Effekt
  drv.setWaveform(1, 0);       // Slot 1: Ende der Sequenz
  drv.go();
}

// === WLAN-Setup: erst Station versuchen, dann AP ===
void setupWiFi() {
  Serial.print("Versuche Home-WLAN: ");
  Serial.println(HOME_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(HOME_SSID, HOME_PASSWORD);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED &&
         (millis() - start) < WIFI_TIMEOUT_MS) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    apMode = false;
    Serial.println("MODUS: Station (Home-WLAN)");
    Serial.print("IP-Adresse: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("Home-WLAN nicht erreichbar - starte eigenen Access Point.");
    WiFi.disconnect();
    WiFi.mode(WIFI_AP);
    bool ok = WiFi.softAP(AP_SSID, AP_PASSWORD);
    if (!ok) {
      Serial.println("AP konnte nicht gestartet werden!");
      while (1) delay(10);
    }
    apMode = true;
    Serial.println("MODUS: Access Point");
    Serial.print("WLAN-Name: ");
    Serial.println(AP_SSID);
    Serial.print("Passwort:  ");
    Serial.println(AP_PASSWORD);
    Serial.print("IP-Adresse: ");
    Serial.println(WiFi.softAPIP());
  }

  Serial.print("WebSocket-URL: ws://");
  Serial.print(apMode ? WiFi.softAPIP() : WiFi.localIP());
  Serial.println(":81");
}

// === WebSocket-Handler ===
void onWebSocketEvent(uint8_t clientNum, WStype_t type, uint8_t* payload, size_t length) {
  switch (type) {
    case WStype_CONNECTED: {
      IPAddress ip = webSocket.remoteIP(clientNum);
      Serial.print("Client ");
      Serial.print(clientNum);
      Serial.print(" verbunden von ");
      Serial.println(ip);
      String msg = "{\"type\":\"hello\",\"face\":" + String(currentFace) +
                   ",\"mode\":\"" + (apMode ? "ap" : "station") +
                   "\",\"haptic\":" + (drvReady ? "true" : "false") + "}";
      webSocket.sendTXT(clientNum, msg);
      break;
    }
    case WStype_DISCONNECTED:
      Serial.print("Client ");
      Serial.print(clientNum);
      Serial.println(" getrennt.");
      break;
    case WStype_TEXT:
      Serial.print("Nachricht von Client: ");
      Serial.println((char*)payload);
      break;
    default:
      break;
  }
}

void broadcastFaceChange(int newFace) {
  String msg = "{\"type\":\"face_change\",\"face\":" + String(newFace) +
               ",\"timestamp\":" + String(millis()) + "}";
  webSocket.broadcastTXT(msg);
  Serial.print("Gesendet: ");
  Serial.println(msg);
}

void broadcastHeartbeat() {
  String msg = "{\"type\":\"heartbeat\",\"face\":" + String(currentFace) +
               ",\"timestamp\":" + String(millis()) +
               ",\"clients\":" + String(webSocket.connectedClients()) + "}";
  webSocket.broadcastTXT(msg);
}

void setup() {
  Serial.begin(115200);
  delay(2000);
  Serial.println("\n=== Wuerfel-Tracker startet ===");

  Wire.begin(21, 22);

  // --- MPU-6050 (Pflicht) ---
  if (!mpu.begin()) {
    Serial.println("MPU6050 nicht gefunden!");
    while (1) delay(10);
  }
  mpu.setAccelerometerRange(MPU6050_RANGE_2_G);
  mpu.setGyroRange(MPU6050_RANGE_250_DEG);
  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
  Serial.println("MPU6050 bereit.");

  // --- DRV2605L (optional) ---
  if (drv.begin()) {
    drv.selectLibrary(1);
    drv.setMode(DRV2605_MODE_INTTRIG);
    drvReady = true;
    Serial.println("DRV2605L bereit (Adresse 0x5A).");
  } else {
    Serial.println("DRV2605L NICHT gefunden - weiter ohne Vibration.");
  }

  setupWiFi();

  webSocket.begin();
  webSocket.onEvent(onWebSocketEvent);
  Serial.println("WebSocket-Server laeuft auf Port 81.\n");
}

void loop() {
  webSocket.loop();

  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  int detected = detectFace(a.acceleration.x,
                            a.acceleration.y,
                            a.acceleration.z);

  unsigned long now = millis();

  if (detected != candidateFace) {
    candidateFace = detected;
    candidateSince = now;
  } else if (detected != currentFace &&
             detected != 0 &&
             (now - candidateSince) >= STABLE_TIME_MS) {
    currentFace = detected;
    Serial.print(">>> Neue Seite: ");
    Serial.println(currentFace);

    broadcastFaceChange(currentFace);
    playHapticFeedback(FACE_CHANGE_EFFECT);
  }

  if (now - lastHeartbeat >= HEARTBEAT_MS) {
    lastHeartbeat = now;
    broadcastHeartbeat();
  }

  delay(20);
}