// URL base del backend (ajustá el puerto si tu backend usa otro)
const API_URL = "http://localhost:8080/api/usuarios/current";

document.addEventListener("DOMContentLoaded", () => {
    cargarPerfil();
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);
});

function cargarPerfil() {
    axios.get(API_URL, { withCredentials: true }) // mantiene la sesión
        .then(response => {
            const usuario = response.data;

            if (!usuario || !usuario.email) {
                alert("No hay sesión activa. Redirigiendo al login...");
                window.location.href = "/web/login.html";
                return;
            }

            document.getElementById("id").textContent = usuario.id;
            document.getElementById("nombre").textContent = usuario.nombre;
            document.getElementById("apellido").textContent = usuario.apellido;
            document.getElementById("email").textContent = usuario.email;
            document.getElementById("estado").textContent = usuario.estadoUsuario;
            document.getElementById("tipo").textContent = usuario.tipoUsuario;
            document.getElementById("fecha").textContent = usuario.dayRegistrer;
        })
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            alert("Error al cargar el perfil. Verificá tu sesión.");
            window.location.href = "/web/login.html";
        });
}

function cerrarSesion() {
    // Si usás Spring Security con sesión, bastaría con llamar a /logout
    axios.post("http://localhost:8080/logout", {}, { withCredentials: true })
        .then(() => {
            alert("Sesión cerrada correctamente.");
            window.location.href = "login.html";
        })
        .catch(() => {
            alert("Error al cerrar sesión.");
        });
}
