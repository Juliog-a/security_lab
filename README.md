# Sistema de Seguridad para Laboratorios con Sensores de Temperatura y Relés

Este repositorio contiene el código fuente y la documentación de un sistema de seguridad distribuido para laboratorios, diseñado para monitorear la temperatura y humedad mediante el sensor DHT11 y activar relés en caso de condiciones anómalas. El sistema utiliza el protocolo MQTT para la comunicación entre los diferentes dispositivos, y Mosquitto como broker MQTT, permitiendo la gestión eficiente y en tiempo real de múltiples puntos de control.

## Características principales
- **Monitoreo de Temperatura**: Los sensores distribuidos miden la temperatura en diferentes puntos del laboratorio, pudiendose escalar a distintos laboratorios mediante una clave primaria que en este caso es la ID de grupo. 
- **Activación de Relés**: Si la temperatura excede un umbral predefinido, se activan automáticamente los relés para disparar sistemas de alarma o ventilación.
- **Sistema Distribuido**: Cada nodo del sistema puede operar de manera independiente, comunicándose a través de la red mediante MQTT.
- **Seguridad y Fiabilidad**: El uso de MQTT y Mosquitto garantiza una comunicación segura y fiable entre los dispositivos, lo que es crucial para aplicaciones en entornos críticos como los laboratorios.

## Tecnologías utilizadas
- **Sensores de Temperatura**: DHT11.
- **Microcontroladores**: ESP32.
- **MQTT**: Protocolo de comunicación ligera para IoT.
- **Mosquitto**: Broker MQTT para la gestión de mensajes.
- **Lenguajes de programación**: Java y C++.

## Instrucciones de uso
1. Configura y despliega los sensores de temperatura en los puntos críticos del laboratorio.
2. Instala y configura el broker Mosquitto.
3. Configura los umbrales de temperatura y las acciones asociadas (activación de relés).
4. Despliega y prueba el sistema de comunicación entre nodos utilizando MQTT.
5. La placa usa Wifi, necesitaras estar conectado a una red, eso se consigue en el archivo de firmware cambiando los valores en la sección RED.
---

Este proyecto está diseñado para proporcionar un entorno seguro en laboratorios, facilitando el monitoreo y control de temperatura de manera automática y distribuida.
