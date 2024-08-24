# Sistema de Seguridad para Laboratorios con Sensores de Temperatura y Relés

Este repositorio contiene el código fuente y la documentación de un sistema de seguridad distribuido para laboratorios, diseñado para monitorear la temperatura y humedad mediante el sensor DHT11 y activar relés en caso de condiciones anómalas. El sistema utiliza el protocolo MQTT para la comunicación entre los diferentes dispositivos, y Mosquitto como broker MQTT, permitiendo la gestión eficiente y en tiempo real de múltiples puntos de control.

## Características principales
- **Monitoreo de Temperatura**: Los sensores distribuidos miden la temperatura en diferentes puntos del laboratorio, pudiéndose escalar a distintos laboratorios mediante una clave primaria, que en este caso es la ID de grupo.
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
5. La placa usa Wi-Fi, necesitarás estar conectado a una red; esto se consigue en el archivo de firmware cambiando los valores en la sección RED.

---

## Estructura del Proyecto

Este proyecto se divide en dos partes principales: **Backend-API** y **Firmware**.

### Backend-API

El **Backend-API** es el componente central que gestiona la lógica del sistema y proporciona la interfaz de comunicación entre los dispositivos (sensores y relés) y los usuarios. Sus funciones principales incluyen:

- **Recepción y Procesamiento de Datos**: Recibe los datos de temperatura enviados por los sensores a través de MQTT, los procesa y almacena para su análisis y monitoreo en tiempo real.
- **Gestión de Umbrales y Eventos**: Define y gestiona los umbrales de temperatura. Cuando se detectan condiciones anómalas, la API desencadena las acciones necesarias, como la activación de relés o el envío de notificaciones.
- **Interfaz para el Usuario**: Proporciona endpoints RESTful que permiten a los usuarios interactuar con el sistema, configurando umbrales, visualizando datos históricos, y controlando manualmente los dispositivos si es necesario.
- **Comunicación con el Firmware**: Envía comandos al firmware que controla los dispositivos de campo (sensores y relés), asegurando que las acciones se ejecuten correctamente.

### Firmware
El Firmware de este proyecto parte de una plantilla proporcionada por la Universidad de Sevilla.
El **Firmware** es el software que se ejecuta directamente en los dispositivos de campo, en este caso en la ESP32, lo que incluye:

- **Captura de Datos de Sensores**: Lee los valores de temperatura de los sensores conectados y los envía periódicamente al Backend-API a través de MQTT.
- **Recepción de Comandos**: Escucha y ejecuta los comandos enviados desde el Backend-API, como la activación de relés o el ajuste de parámetros operativos.
- **Control de Dispositivos**: Controla directamente los relés y otros dispositivos electrónicos en función de las órdenes recibidas o según la lógica interna (por ejemplo, en caso de pérdida de conexión con el Backend-API, puede operar en modo seguro).
- **Comunicación con el Backend-API**: Utiliza MQTT para comunicarse con el Backend-API, enviando datos y recibiendo comandos en tiempo real.

---

Este proyecto está diseñado para proporcionar un entorno seguro en laboratorios, facilitando el monitoreo y control de temperatura de manera automática y distribuida.

