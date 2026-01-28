document.getElementById("form-password")
    .addEventListener("submit", cambiarPassword);

function cambiarPassword(e) {
    e.preventDefault();

    const actual = actual.value;
    const nueva = nueva.value;
    const repetir = repetir.value;

    if (nueva !== repetir) {
        toast("Las contraseñas no coinciden", "error");
        return;
    }

    axios.put("http://localhost:8080/api/password/cambiar", {
        passwordActual: actual,
        passwordNueva: nueva
    }, { withCredentials: true })
        .then(() => {
            toast("Contraseña actualizada", "success");
            setTimeout(() => window.location.href = "/web/usuario-perfil.html", 1500);
        })
        .catch(() => {
            toast("Error al cambiar la contraseña", "error");
        });
}

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
