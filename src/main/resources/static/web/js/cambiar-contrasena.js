// cambiar-contrasena.js — usa mostrarNotificacion global (notificaciones.js)

document.getElementById("form-password").addEventListener("submit", cambiarPassword);

function cambiarPassword(e) {
    e.preventDefault();

    const actualInput = document.getElementById("actual");
    const nuevaInput = document.getElementById("nueva");
    const repetirInput = document.getElementById("repetir");

    const actual = actualInput.value;
    const nueva = nuevaInput.value;
    const repetir = repetirInput.value;

    if (nueva !== repetir) {
        mostrarNotificacion("Las nuevas contrasenas no coinciden.", "error");
        return;
    }

    axios.post("/api/password/cambiar", {
        passwordActual: actual,
        passwordNueva: nueva
    }, { withCredentials: true })
        .then(() => {
            mostrarNotificacion("Contrasena actualizada con exito.", "success");

            axios.get("/api/usuarios/current", { withCredentials: true })
                .then(res => {
                    const usuario = res.data;
                    setTimeout(() => {
                        if (usuario.tipoUsuario === "ADMINISTRADOR") {
                            window.location.href = "/web/admin/dashboard.html";
                        } else if (usuario.tipoUsuario === "FERIANTE") {
                            window.location.href = "/web/feriante/perfil.html";
                        } else {
                            window.location.href = "/web/usuario-perfil.html";
                        }
                    }, 1500);
                })
                .catch(() => {
                    setTimeout(() => window.location.href = "/web/login.html", 1500);
                });
        })
        .catch((error) => {
            const errorMsg = error.response?.data || "Error al cambiar la contrasena.";
            mostrarNotificacion(errorMsg, "error");
        });
}