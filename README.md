# App Cuenta Corriente — Android

Trabajo Práctico Integrador N.º 2 · Aplicaciones Móviles y Cloud Computing · UPSO · 2026

---

## 1. Presentación

**App Cuenta Corriente** es la versión Android de la app del TP N.º 1, para una vendedora de ropa que trabaja con cuenta corriente: registra a sus clientas, les entrega prendas **a prueba**, confirma lo que compran, anota los pagos y lleva el **saldo** de cada una. La app usa **la misma API y la misma base de datos** que el frontend web, desplegadas en Railway.

**Grupo 10 · Integrantes**

| Integrante | Módulo en la app Android |
|---|---|
| Maldonado, Gabriela | Login, sesión y base técnica (cliente de la API, token) |
| Pistan, Liliana | Clientas y catálogo de prendas |
| Rivero, Juliana | Entregas a prueba y confirmación de compra |
| Cantero, Tomás | Ficha de la clienta y pagos |
| Mion, Tadeo | Inicio, navegación, estilos, notificaciones push y video |

**Direcciones**

| Qué | URL |
|---|---|
| API REST (PHP) | https://reliable-spirit-production-e065.up.railway.app/api/ |
| Estado de la API | https://reliable-spirit-production-e065.up.railway.app/api/estado.php |
| Frontend web | https://reliable-spirit-production-e065.up.railway.app |
| Repositorio del TP N.º 1 (backend y web) | https://github.com/gabimaldonado/app_cuenta_corriente |
| Proveedor | Railway (PaaS) |

**Usuario de prueba:** `admin@cuentacorriente.com` · la contraseña se entrega en el envío del campus (no se publica en el repositorio).

---

## 2. Cómo correrla

1. En Android Studio: **File → New → Project from Version Control**, pegar la URL de este repositorio y **Clone**.
2. Esperar a que termine la sincronización de Gradle (la primera vez descarga las dependencias y tarda unos minutos).
3. Elegir un emulador o un teléfono y tocar **▶ Run**.

**Dónde va la URL base.** Hay una sola, en `app/src/main/java/com/example/appcuentacorriente/ApiCliente.java`, en la constante `URL_BASE`. Ya apunta a la API desplegada en Railway (por HTTPS), así que no hace falta cambiar nada para probar.

**Archivo de configuración.** _(PENDIENTE — Tadeo: `google-services.json` para las notificaciones push: dónde va (`app/`) y cómo se consigue desde la consola de Firebase.)_

**Versión de Android probada.** Emulador *Medium Phone* con **Android 15 (API 35)**. La app pide como mínimo Android 7 (`minSdk 24`) y se compila con `targetSdk 37`.

**APK de depuración.** **Build → Build App Bundle(s) / APK(s) → Build APK(s)**. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 3. Pantallas

| Activity | Qué muestra | Desde dónde se llega | Qué datos recibe |
|---|---|---|---|
| `LoginActivity` | Email, contraseña y botón Ingresar. Muestra «Cargando…» mientras espera y el mensaje de error de la API | Es la pantalla de inicio de la app (LAUNCHER). Si ya hay una sesión guardada, se saltea | — |
| `ClientesActivity` | Lista de clientas activas que trae la API, con búsqueda por nombre o DNI | Botón «Ver clientas» de Inicio | — |
| `AltaClienteActivity` | Formulario de alta de clienta (nombre, teléfono, DNI, dirección, ciudad, notas) | Botón «Nueva Clienta» de `ClientesActivity` | — · Devuelve `RESULT_OK` para que la lista se actualice |
| `ProductosActivity` | Catálogo de prendas con precio y stock, con filtro Todas / Con stock / Sin stock | Botón «Catálogo de prendas» de `ClientesActivity` | — |
| `NuevaEntregaActivity` | Elegir clienta y prendas con stock para llevar a prueba | _(PENDIENTE — Juliana)_ | _(PENDIENTE)_ |
| `ConfirmarEntregaActivity` | Marcar cada prenda como «Se queda» o «Devuelve» y confirmar | _(PENDIENTE — Juliana)_ | _(PENDIENTE)_ |
| `FichaClienteActivity` | _(PENDIENTE — Tomi)_ | Tocar una clienta en `ClientesActivity` | `id` y `nombre` de la clienta |
| `RegistrarPagoActivity` | _(PENDIENTE — Tomi)_ | _(PENDIENTE)_ | _(PENDIENTE)_ |
| `InicioActivity` | _(PENDIENTE — Tadeo)_ | Desde el login, después de ingresar | — |

_(PENDIENTE — agregar una o dos capturas de pantalla.)_

---

## 4. Endpoints que usa la app

La app usa **la misma API del TP N.º 1, sin cambios**: no se agregó ni se modificó ningún endpoint para Android. Todos los pedidos (salvo el login) viajan con el encabezado `Authorization: Bearer <token>`.

| Método | Ruta | Para qué lo usa la app | Pantalla |
|---|---|---|---|
| POST | `/api/login.php` | Ingresar: recibe el token y el nombre del usuario | `LoginActivity` |
| DELETE | `/api/sesion.php` | Cerrar sesión (borra el token en el servidor) | `InicioActivity` (botón Salir) |
| GET | `/api/clientes.php?buscar=` | Listar y buscar clientas | `ClientesActivity` |
| POST | `/api/clientes.php` | Dar de alta una clienta | `AltaClienteActivity` |
| GET | `/api/productos.php?stock=` | Listar el catálogo, con filtro por stock | `ProductosActivity` |

_(PENDIENTE — Juliana, Tomi y Tadeo: agregar los de entregas, ficha, pagos y resumen cuando estén conectados.)_

---

## 5. Servicio cloud

_(PENDIENTE — Tadeo: completar cuando esté funcionando.)_

**Cuál y por qué.** La app usa **Firebase Cloud Messaging (FCM)** para las notificaciones push. El ingreso se hace con el login propio de nuestra API (`login.php` y su token, igual que en el frontend web) y no con Firebase Authentication: así la app y la web comparten los mismos usuarios y la API no necesita cambios.

**Qué datos salen y hacia dónde.** _(PENDIENTE — por ejemplo: el token de registro de FCM de la instalación, hacia los servidores de Google/Firebase; ningún dato de las clientas.)_

---

## 6. Decisiones de diseño

### Base técnica, login y sesión

- **Java con vistas XML (Empty Views Activity).** Es lo que se vio en la materia. Cada pantalla es una Activity con su layout en `res/layout` y todos los textos en `res/values/strings.xml`.
- **Retrofit + Gson para consumir la API.** Retrofit arma los pedidos HTTP a partir de la interfaz `CuentaCorrienteApi`, donde cada integrante agrega sus endpoints en su sección; Gson convierte el JSON en objetos Java. Los pedidos se hacen con `enqueue`, en segundo plano, para que la pantalla no se congele.
- **Un solo cliente de la API.** `ApiCliente` crea Retrofit una sola vez y guarda la **única** URL base de la app. Todas las pantallas lo usan con `ApiCliente.get(this)`.
- **El token se agrega solo.** Un interceptor de OkHttp, dentro de `ApiCliente`, le pone a cada pedido el encabezado `Authorization: Bearer <token>`, así ninguna pantalla tiene que acordarse de hacerlo.
- **La sesión se guarda en el teléfono.** `Sesion` guarda el token y el nombre en `SharedPreferences` (el equivalente al `localStorage` de la web). Si al abrir la app ya hay un token, el login se saltea.
- **El login no queda en la pila.** Al ingresar bien, el login abre la pantalla principal y hace `finish()`: con Atrás no se vuelve a la pantalla de ingreso.
- **Manejo de errores.**
  - Mientras espera la respuesta, el botón se desactiva y dice «Cargando…»: no se puede enviar dos veces.
  - Si la API responde con error (400, 401, 404, 422), se muestra el mensaje que manda la API en su JSON `{"error": "..."}`, con `ApiCliente.mensajeDeError()`.
  - Si no hay respuesta (modo avión o servidor caído), se muestra «No se pudo conectar con el servidor» y la app no se cierra.
  - Si un pedido responde **401** (token vencido o inválido), `Sesion.vencida()` borra la sesión, avisa y vuelve al login sin dejar pantallas viejas en la pila.
- **Girar la pantalla no pierde datos.** El mensaje del login se guarda en `onSaveInstanceState` y se recupera al recrearse la pantalla; los campos de texto con id los conserva Android solo.

_(PENDIENTE — Liliana, Juliana, Tomi y Tadeo: un párrafo por decisión de su módulo.)_

---

## 7. Limitaciones conocidas

- **El token se guarda sin cifrar** en `SharedPreferences`. Para una app real convendría usar almacenamiento cifrado.
- **Salir sin conexión:** si no hay Internet o el token ya venció, el botón Salir avisa que no pudo cerrar la sesión y no vuelve al login.

_(PENDIENTE — cada integrante agrega lo que no llegó a hacer o funciona a medias.)_
