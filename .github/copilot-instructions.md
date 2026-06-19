# Instrucciones para GitHub Copilot

Este repositorio contiene una librería Java desarrollada con:

- Java 21
- Maven
- Arquitectura modular orientada a reutilización
- Sin dependencias de bases de datos
- Código destinado a ser utilizado por otros proyectos

## Normas de desarrollo

- Utilizar exclusivamente características compatibles con Java 21.
- Seguir principios SOLID.
- Favorecer composición frente a herencia cuando sea posible.
- Evitar código duplicado.
- Mantener bajo acoplamiento y alta cohesión.
- Utilizar nombres descriptivos en inglés para clases, métodos y variables.
- Generar código limpio, legible y mantenible.
- Evitar comentarios innecesarios; el código debe ser autoexplicativo.
- Cuando sea útil, añadir JavaDoc en APIs públicas.
- No introducir nuevas dependencias Maven sin justificar su necesidad.
- Mantener compatibilidad con sistemas Linux y Windows.
- Siempre trabajar sobre una rama específica para cada cambio o tarea.

## Testing

- Generar pruebas unitarias con JUnit 5.
- Utilizar Mockito cuando sea necesario.
- Mantener las pruebas independientes y repetibles.
- Cubrir casos normales, límites y errores.
- Priorizar la cobertura de la lógica de negocio.

## Maven

- Respetar la estructura estándar Maven.
- Mantener actualizado el pom.xml.
- Evitar configuraciones innecesariamente complejas.
- Utilizar versiones estables y ampliamente soportadas.

## Al modificar código existente

Antes de realizar cambios:

1. Analizar el contexto completo de la clase y del módulo.
2. Mantener el estilo existente del proyecto.
3. Evitar cambios incompatibles con versiones anteriores.
4. Proponer refactorizaciones solo cuando aporten una mejora clara.
5. Actualizar pruebas afectadas por los cambios.

## Al generar nuevas funcionalidades

- Diseñar primero la API pública.
- Explicar brevemente el diseño propuesto.
- Generar código completo y compilable.
- Incluir pruebas unitarias.
- Considerar manejo de errores y validaciones.
- Considerar rendimiento y concurrencia cuando aplique.

## Respuestas esperadas

Cuando se solicite una nueva funcionalidad:

1. Explicar brevemente el enfoque.
2. Generar el código completo.
3. Generar las pruebas unitarias.
4. Indicar posibles mejoras futuras.

Cuando se solicite una corrección:

1. Identificar la causa probable.
2. Proponer la solución más simple.
3. Mantener compatibilidad con el código existente.
4. Generar las pruebas necesarias para evitar regresiones.
