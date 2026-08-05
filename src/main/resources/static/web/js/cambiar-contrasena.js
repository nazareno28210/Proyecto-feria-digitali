// cambiar-contrasena.js — usa mostrarNotificacion global (notificaciones.js)

document.addEventListener("DOMContentLoaded", () => {
    const formPassword = document.getElementById("form-password");
    if (formPassword) {
        formPassword.addEventListener("submit", cambiarPassword);
    }
});

function switchMethod(method) {
    const tabManual = document.getElementById("tab-manual");
    const tabEmail = document.getElementById("tab-email");
    const containerManual = document.getElementById("method-manual-container");
    const containerEmail = document.getElementById("method-email-container");

    if (!tabManual || !tabEmail || !containerManual || !containerEmail) return;

    if (method === 'manual') {
        tabManual.classList.add("active");
        tabEmail.classList.remove("active");
        containerManual.classList.remove("hidden");
        containerEmail.classList.add("hidden");
    } else {
        tabEmail.classList.add("active");
        tabManual.classList.remove("active");
        containerEmail.classList.remove("hidden");
        containerManual.classList.add("hidden");
    }
}

function cambiarPassword(e) {
    e.preventDefault();

    const actualInput = document.getElementById("actual");
    const nuevaInput = document.getElementById("nueva");
    const repetirInput = document.getElementById("repetir");

    const actual = actualInput.value;
    const nueva = nuevaInput.value;
    const repetir = repetirInput.value;

    if (nueva.length < 6) {
        mostrarNotificacion("La nueva contraseña debe tener al menos 6 caracteres.", "error");
        return;
    }

    if (nueva !== repetir) {
        mostrarNotificacion("Las nuevas contraseñas no coinciden.", "error");
        return;
    }

    const btnSubmit = document.getElementById("btn-submit-manual");
    if (btnSubmit) btnSubmit.disabled = true;

    axios.post("/api/password/cambiar", {
        passwordActual: actual,
        passwordNueva: nueva
    }, { withCredentials: true })
        .then(() => {
            mostrarNotificacion("¡Contraseña actualizada con éxito!", "success");

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
            const errorMsg = error.response?.data || "Error al cambiar la contraseña.";
            mostrarNotificacion(errorMsg, "error");
        })
        .finally(() => {
            if (btnSubmit) btnSubmit.disabled = false;
        });
}

async function solicitarLinkPorCorreo() {
    const btn = document.getElementById("btn-enviar-correo");
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `<i class="bi bi-hourglass-split"></i> Enviando...`;
    }

    try {
        // Intentar obtener el usuario actual registrado
        const resUser = await axios.get("/api/usuarios/current", { withCredentials: true });
        const email = resUser.data?.email;

        if (email) {
            await axios.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
            mostrarNotificacion(`Se envió un enlace de restablecimiento a ${email}. Revisa tu correo.`, "success");
        } else {
            window.location.href = "/web/reset-password.html";
        }
    } catch (e) {
        console.warn("Usuario no autenticado o error:", e);
        window.location.href = "/web/reset-password.html";
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `<i class="bi bi-send-fill"></i> Enviar Enlace a mi Correo`;
        }
    }
}