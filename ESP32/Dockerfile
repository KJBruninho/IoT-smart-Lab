FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates bash python3 xz-utils git \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://raw.githubusercontent.com/arduino/arduino-cli/master/install.sh | sh \
    && mv bin/arduino-cli /usr/local/bin/arduino-cli \
    && rm -rf bin

RUN arduino-cli config init \
    && arduino-cli config add board_manager.additional_urls https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json \
    && arduino-cli core update-index \
    && arduino-cli core install esp32:esp32

# Libraries used by the firmware. Adjust versions if needed.
RUN arduino-cli lib install PubSubClient OneWire DallasTemperature ArduinoJson

COPY build-esp32.sh /opt/build-esp32.sh
RUN chmod +x /opt/build-esp32.sh

WORKDIR /workspace

CMD ["bash", "/opt/build-esp32.sh"]
