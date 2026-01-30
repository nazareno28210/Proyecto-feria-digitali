document.getElementById("form-password").addEventListener("submit", cambiarPassword);

function cambiarPassword(e) {
    e.preventDefault();

    // Obtener los elementos del DOM
    const actualInput = document.getElementById("actual");
    const nuevaInput = document.getElementById("nueva");
    const repetirInput = document.getElementById("repetir");

    const actual = actualInput.value;
    const nueva = nuevaInput.value;
    const repetir = repetirInput.value;

    // Validación básica de coincidencia en el cliente
    if (nueva !== repetir) {
        toast("Las nuevas contraseñas no coinciden", "error");
        return;
    }

    // Petición para cambiar la contraseña
    axios.post("http://localhost:8080/api/password/cambiar", {
        passwordActual: actual,
        passwordNueva: nueva
    }, { withCredentials: true })
        .then(() => {
            toast("Contraseña actualizada con éxito", "success");
            
            // 🟢 LÓGICA DE REDIRECCIÓN DINÁMICA 🟢
            // Consultamos quién es el usuario actual para saber a dónde mandarlo
            axios.get("http://localhost:8080/api/usuarios/current", { withCredentials: true })
                .then(res => {
                    const usuario = res.data;
                    
                    setTimeout(() => {
                        
                        if (usuario.tipoUsuario === "ADMINISTRADOR") {
                            window.location.href = "/web/admin/dashboard.html"
                        }
                        else if (usuario.tipoUsuario === "FERIANTE") {
                            window.location.href = "/web/feriante/perfil.html";
                            
                        } else  {
                            window.location.href = "/web/usuario-perfil.html";
                        } 
                    }, 1500);
                })
                .catch(() => {
                    // En caso de error al verificar el tipo, por seguridad mandamos al login
                    setTimeout(() => window.location.href = "/web/login.html", 1500);
                });
        })
        .catch((error) => {
            // Manejo de errores (contraseña actual incorrecta, requisitos de seguridad, etc.)
            const errorMsg = error.response?.data || "Error al cambiar la contraseña";
            toast(errorMsg, "error");
        });
}

// Función auxiliar para las notificaciones
function toast(msg, tipo) {
    Toastify({
        text: msg,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: tipo === "success"
                ? "linear-gradient(to right, #2ecc71, #27ae60)"
                : "linear-gradient(to right, #e74c3c, #c0392b)"
        }
    }).showToast();
}