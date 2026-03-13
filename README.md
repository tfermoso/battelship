# Batalla Naval Online (Spring Boot)

Aplicación web multijugador del clásico juego **Battleship / Hundir la
flota** desarrollada con **Spring Boot**, **Thymeleaf**, **JPA**,
**Spring Security** y **WebSocket**.

Permite a dos jugadores crear partidas, colocar sus barcos y disparar
por turnos viendo los resultados en tiempo real.

------------------------------------------------------------------------

# Tecnologías utilizadas

Backend - Java - Spring Boot - Spring MVC - Spring Security - Spring
Data JPA - WebSocket (STOMP + SockJS)

Frontend - Thymeleaf - HTML5 - CSS3 - JavaScript

Base de datos - MySQL / MariaDB - Hibernate (JPA)

------------------------------------------------------------------------

# Funcionalidades principales

## Autenticación de usuarios

-   Registro de usuarios
-   Login seguro con Spring Security
-   Contraseñas cifradas con BCrypt

## Gestión de partidas

Los jugadores pueden:

-   Crear partidas
-   Unirse a partidas existentes
-   Ver partidas en:
    -   espera
    -   preparación
    -   en juego
    -   finalizadas

## Preparación de flota

Cada jugador debe colocar:

-   1 barco grande (4 celdas)
-   2 barcos medianos (2 celdas)
-   3 barcos pequeños (1 celda)

El tablero es de **10x10**.

Cuando ambos jugadores están listos la partida comienza automáticamente.

## Sistema de turnos

Los jugadores disparan por turnos.

Resultados posibles:

-   WATER → Agua
-   HIT → Tocado
-   SUNK → Hundido

Cuando un jugador pierde todos sus barcos la partida termina.

## Tiempo real con WebSocket

Se utiliza **WebSocket con STOMP** para enviar eventos a los jugadores:

Eventos enviados:

-   PLAYER_READY
-   GAME_START
-   SHOT

Esto permite:

-   actualizar estado de jugadores
-   mostrar disparos del rival
-   actualizar turno automáticamente

------------------------------------------------------------------------

# Arquitectura del proyecto

Estructura principal:

    controller/
        AuthController
        GameController
        BoardController

    service/
        GameService
        BoardService
        UserService

    repository/
        GameRepository
        BoardRepository
        ShipRepository
        ShipPositionRepository
        ShotRepository

    model/
        Game
        Board
        Ship
        ShipPosition
        Shot
        User

    dto/
        CreateGameDto
        RegisterUserDto
        ShotDto
        ShipPlacementRequestDto

    websocket/
        GameWebSocketService

------------------------------------------------------------------------

# Flujo del juego

1.  Usuario se registra o inicia sesión
2.  Accede al lobby de partidas
3.  Crea una partida o se une a una existente
4.  Ambos jugadores colocan su flota
5.  La partida comienza automáticamente
6.  Los jugadores disparan por turnos
7.  Cuando todos los barcos de un jugador están hundidos termina la
    partida

------------------------------------------------------------------------

# Ejecución del proyecto

## Ejecutar desde IDE

Simplemente ejecutar:

    BatallanavalApplication

## Compilar a JAR

    mvn clean package

Se generará:

    target/batallanaval.jar

## Ejecutar el JAR

    java -jar batallanaval.jar

------------------------------------------------------------------------

# Configuración externa

Se puede usar un `application.properties` externo en la misma carpeta
que el jar.

Ejemplo:

    server.port=9090

    spring.datasource.url=jdbc:mysql://localhost:3306/batallanaval
    spring.datasource.username=root
    spring.datasource.password=root

Ejecutar:

    java -jar batallanaval.jar

------------------------------------------------------------------------

# Puerto personalizado

También se puede iniciar la aplicación en otro puerto:

    java -jar batallanaval.jar --server.port=9090

------------------------------------------------------------------------

# Mejoras futuras

Posibles ampliaciones:

-   Chat entre jugadores
-   Ranking de jugadores
-   Historial de partidas
-   IA para jugar contra el ordenador
-   Animaciones en disparos
-   Estadísticas de juego

------------------------------------------------------------------------

# Autor

Proyecto desarrollado como ejemplo educativo para demostrar:

-   Arquitectura Spring Boot
-   Juegos por turnos
-   WebSockets en tiempo real
-   Integración frontend + backend
