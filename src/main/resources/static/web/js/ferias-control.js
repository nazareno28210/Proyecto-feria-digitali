document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("form-feria");
    const tbody = document.querySelector("#tabla-ferias tbody");

    // URL base de la API
    const API_BASE_URL = "http://localhost:8080/api/ferias";

    // Crear feria
    form.addEventListener("submit", async e => {
        e.preventDefault();

        // Las validaciones de frontend (cliente) se mantienen
        const fechaInicio = document.getElementById("fechaInicio").value;
        const fechaFinal = document.getElementById("fechaFinal").value;

        const hoy = new Date();
        const hoyFormateado = hoy.getFullYear() + '-' +
                           String(hoy.getMonth() + 1).padStart(2, '0') + '-' +
                           String(hoy.getDate()).padStart(2, '0');

        if (fechaInicio < hoyFormateado) {
            showToast("La fecha de inicio no puede ser anterior al día de hoy.", "error");
            return;
        }
        if (fechaFinal < fechaInicio) {
            showToast("La fecha final no puede ser anterior a la fecha de inicio.", "error");
            return;
        }

        const feria = {
            nombre: document.getElementById("nombre").value,
            lugar: document.getElementById("lugar").value,
            fechaInicio: fechaInicio,
            fechaFinal: fechaFinal,
            descripcion: document.getElementById("descripcion").value
        };

        try {
            await axios.post(API_BASE_URL, feria);

            showToast("Feria creada correctamente", "success");
            form.reset();
            cargarFerias();

        } catch (err) {
            // ==================================================
            // 🟢 CAMBIO CLAVE: Leer el error del backend 🟢
            // ==================================================
            if (err.response && err.response.data) {
                // Muestra el error específico de la validación del backend
                showToast(err.response.data, "error");
            } else {
                // Error genérico si no hay respuesta (ej: red caída)
                showToast("Error al crear la feria", "error");
            }
            // ==================================================
        }
    });

    // Cargar ferias
    async function cargarFerias() {
        try {
            const res = await axios.get(API_BASE_URL);
            tbody.innerHTML = "";

            res.data.forEach(f => {
                const row = document.createElement("tr");

                let acciones = "";
                if (f.estado === "Activa") {
                    acciones = `
                        <button onclick="darBaja(${f.id})" style="background-color:#e67e22;color:white;">Baja</button>
                        <button onclick="eliminar(${f.id})" style="background-color:#e74c3c;color:white;">Eliminar</button>
                    `;
                } else {
                    acciones = `
                        <button onclick="activar(${f.id})" style="background-color:#2ecc71;color:white;">Activar</button>
                        <button onclick="eliminar(${f.id})" style="background-color:#e74c3c;color:white;">Eliminar</button>
                    `;
                }

                row.innerHTML = `
                    <td>${f.nombre}</td>
                    <td>${f.lugar}</td>
                    <td>${f.fechaInicio} → ${f.fechaFinal}</td>
                    <td>${f.estado}</td>
                    <td>${acciones}</td>
                `;
                tbody.appendChild(row);
            });
        } catch (err) {
            // 🟢 CAMBIO: Manejo de error mejorado
            const msg = err.response ? err.response.data : "Error al cargar las ferias";
            showToast(msg, "error");
        }
    }

    cargarFerias();

    // 🟢 FUNCIONES ACTUALIZADAS CON MANEJO DE ERRORES MEJORADO 🟢

    window.activar = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/${id}/activar`);
            showToast("Feria activada", "success");
            cargarFerias();
        } catch (err) {
            const msg = err.response ? err.response.data : "Error al activar";
            showToast(msg, "error");
        }
    };

    window.darBaja = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/${id}/baja`);
            showToast("Feria dada de baja", "success");
            cargarFerias();
        } catch (err) {
            const msg = err.response ? err.response.data : "Error al dar de baja";
            showToast(msg, "error");
        }
    }

    window.eliminar = async (id) => {
        if (confirm("¿Eliminar esta feria? Esta acción no se puede deshacer.")) {
            try {
                await axios.delete(`${API_BASE_URL}/${id}`);
                showToast("Feria eliminada", "success");
                cargarFerias();
            } catch (err) {
                // Fíjate que tu captura de pantalla mostraba un error 404
                // Este código ahora SÍ mostraría ese error 404 si ocurre.
                const msg = err.response ? `Error: ${err.response.data}` : "Error al eliminar";
                showToast(msg, "error");
            }
        }
    }
});

// Función Toast (sin cambios)
function showToast(message, type = "info") {
    let color;
    switch (type) {
        case "success":
            color = "linear-gradient(to right, #00b09b, #96c93d)";
            break;
        case "error":
            color = "linear-gradient(to right, #ff5f6d, #ffc371)";
            break;
        case "warning":
            color = "linear-gradient(to right, #f7971e, #ffd200)";
            break;
        default:
            color = "linear-gradient(to right, #2193b0, #6dd5ed)";
    }

    Toastify({
        text: message,
        duration: 4000,
        gravity: "top", // top or bottom
        position: "right", // left, center or right
        backgroundColor: color,
        stopOnFocus: true,
    }).showToast();
}