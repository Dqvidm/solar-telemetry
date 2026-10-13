import time
import math
import random
import requests

API_ENDPOINT = "http://localhost:8080/api/telemetry"
PANEL_ID = "PV-ARRAY-STRING-01"
TRANSMISSION_INTERVAL_SEC = 2.0

VOC_NOMINAL = 49.5      # Open-circuit voltage (V)
ISC_NOMINAL = 10.2      # Short-circuit current (A)
TEMP_COEFF_V = -0.0035  # Voltage thermal degradation coefficient (-0.35%/°C)

def simulate_pv_reading(step_counter):
    time_factor = (step_counter % 360) * (math.pi / 180.0)
    irradiance_factor = max(0.05, math.sin(time_factor))

    temperature = 25.0 + (irradiance_factor * 35.0) + random.uniform(-0.4, 0.4)

    # Temperature-adjusted voltage calculation
    temp_delta = temperature - 25.0
    voltage = (VOC_NOMINAL * (1.0 + TEMP_COEFF_V * temp_delta)) * (0.88 + random.uniform(-0.02, 0.02))

    current = (ISC_NOMINAL * irradiance_factor) * (0.95 + random.uniform(-0.03, 0.03))

    return {
        "panelId": PANEL_ID,
        "voltage": round(max(0.0, voltage), 2),
        "current": round(max(0.0, current), 2),
        "temperature": round(temperature, 2)
    }

def transmit_telemetry(payload):
    try:
        response = requests.post(API_ENDPOINT, json=payload, timeout=3.0)
        power = round(payload['voltage'] * payload['current'], 1)

        if response.status_code in (200, 201):
            body = response.json()
            alerts = body.get("alerts", [])
            alert_tag = f" | ALERTS ({len(alerts)}): {alerts}" if alerts else ""
            print(f"[TX OK] {payload['panelId']} | V: {payload['voltage']:5.2f} V | I: {payload['current']:5.2f} A | P: {power:6.1f} W | T: {payload['temperature']:5.2f} °C{alert_tag}")
        else:
            print(f"[TX REJECTED] Server responded with HTTP {response.status_code}: {response.text}")
    except requests.exceptions.ConnectionError:
        print("[TX ERROR] Connection refused. Ensure Spring Boot is running on port 8080.")
    except requests.exceptions.Timeout:
        print("[TX ERROR] Request timed out.")
    except Exception as ex:
        print(f"[TX ERROR] Unexpected failure: {ex}")

def main():
    print("=====================================================")
    print(" Photovoltaic Telemetry Edge Node Generator")
    print(f" Target Endpoint: {API_ENDPOINT}")
    print(f" Node Identifier: {PANEL_ID}")
    print(f" Transmission Interval: {TRANSMISSION_INTERVAL_SEC}s")
    print("=====================================================\n")

    TOTAL_PACKETS = 10
    step = 45

    try:
        for packet_num in range(1, TOTAL_PACKETS + 1):
            print(f"[{packet_num}/{TOTAL_PACKETS}] ", end="")
            packet = simulate_pv_reading(step)
            transmit_telemetry(packet)
            step += 1
            time.sleep(TRANSMISSION_INTERVAL_SEC)
        print("\nBatch transmission complete. Process terminated.")
    except KeyboardInterrupt:
        print("\nTransmission interrupted by user.")

if __name__ == "__main__":
    main()