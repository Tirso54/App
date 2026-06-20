# Política de Privacidad de TimeBlock Pro

**Última actualización:** 20 de junio de 2026

Esta política de privacidad describe cómo TimeBlock Pro ("la aplicación", "nosotros") trata la información cuando usas la aplicación.

## 1. Resumen

TimeBlock Pro es una aplicación de planificación semanal de tareas (Time Blocking) con una función de optimización automática mediante inteligencia artificial. La aplicación está diseñada para almacenar tus datos **localmente en tu dispositivo**, y solo envía la información estrictamente necesaria a un servicio externo (Google Gemini) para generar sugerencias de reorganización de tu horario.

No recopilamos, vendemos ni compartimos tu información con fines publicitarios. No solicitamos tu nombre, correo electrónico, ni ningún dato de identificación personal para usar la aplicación.

## 2. Qué datos se almacenan y dónde

| Dato | Dónde se guarda | Quién lo ve |
|---|---|---|
| Tareas que creas (título, duración, categoría, prioridad, día, estado completado) | Base de datos local en tu dispositivo (Room/SQLite) | Solo tú, en tu dispositivo |
| Estas mismas tareas, en el momento de pedir una optimización con IA | Se envían temporalmente a la API de Google Gemini para procesarse | Google, según su propia política de privacidad (ver sección 4) |

Los datos de tus tareas **no se envían a ningún servidor propio de TimeBlock Pro**, porque la aplicación no tiene servidor propio: toda la lógica corre en tu dispositivo, salvo la llamada directa a Gemini descrita abajo.

## 3. La función de optimización con IA

TimeBlock Pro incluye una función central de optimización automática de horarios mediante inteligencia artificial (Google Gemini). Esta función es parte integral de la aplicación.

Cuando usas esta función:
- La lista de tareas que tienes en ese momento (títulos, duraciones, categorías, prioridades y días) se envía a la API de Gemini de Google para que sugiera una redistribución más equilibrada.
- La aplicación usa una clave de API proporcionada por el desarrollador de TimeBlock Pro, compartida entre todos los usuarios de la app. No se te pide que introduzcas ninguna clave propia.
- La respuesta de la IA (la nueva distribución sugerida) se recibe y se aplica localmente; no se almacena en ningún servidor intermedio nuestro.

Si la llamada a Gemini falla (por ejemplo, sin conexión a internet), la aplicación usa un optimizador local incorporado que no requiere conexión ni envía ningún dato fuera del dispositivo.

## 4. Servicios de terceros

La única integración con un servicio externo es **Google Gemini API** (generativelanguage.googleapis.com), operada por Google. El tratamiento que Google hace de los datos enviados a su API está sujeto a la política de privacidad de Google:

https://policies.google.com/privacy

Te recomendamos revisar también los términos específicos de la API de Gemini:

https://ai.google.dev/gemini-api/terms

## 5. Copias de seguridad

TimeBlock Pro excluye explícitamente la base de datos de tareas de los sistemas de copia de seguridad automática de Android (backup en la nube y transferencia entre dispositivos). Esto significa que tus tareas permanecen únicamente en el dispositivo donde las creaste, y no se incluyen en backups de Google ni se transfieren automáticamente a otro dispositivo.

## 6. Permisos que solicita la aplicación

TimeBlock Pro solicita únicamente el permiso de **Internet**, necesario exclusivamente para comunicarse con la API de Gemini al usar la función de optimización. La aplicación no solicita acceso a contactos, ubicación, cámara, micrófono, almacenamiento externo, ni ningún otro permiso sensible.

## 7. Datos de menores

TimeBlock Pro no está dirigida específicamente a menores de edad y no recopila intencionadamente datos personales identificables de ningún usuario, independientemente de su edad, salvo el contenido de las tareas que el propio usuario decide introducir.

## 8. Tus derechos y control sobre tus datos

Como todos los datos de tus tareas se almacenan localmente en tu dispositivo:
- Puedes eliminar todos tus datos desinstalando la aplicación o limpiando sus datos desde los ajustes de Android (Ajustes → Aplicaciones → TimeBlock Pro → Almacenamiento → Borrar datos).
- No existe una cuenta de usuario ni un servidor central donde se almacenen tus datos, por lo que no es necesario solicitarnos su eliminación.

## 9. Cambios en esta política

Podemos actualizar esta política de privacidad si cambian las funciones de la aplicación. Cualquier cambio relevante se reflejará en este documento, indicando la fecha de la última actualización en la parte superior.

## 10. Contacto

Si tienes preguntas sobre esta política de privacidad, puedes contactar a través del repositorio del proyecto en GitHub:

https://github.com/Tirso54/App
