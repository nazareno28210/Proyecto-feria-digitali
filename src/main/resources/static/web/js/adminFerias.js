document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("form-feria");
    const tbody = document.querySelector("#tabla-ferias tbody");

    // 🟢 URL base de la API (como en tus otros archivos) 🟢
    const API_BASE_URL = "http://localhost:8080/api/ferias";

    // Crear feria
    form.addEventListener("submit", async e => {
        e.preventDefault();

        // 🟢 INICIO DE VALIDACIÓN 🟢
        const nombre = document.getElementById("nombre").value;
        const lugar = document.getElementById("lugar").value;
        const fechaInicio = document.getElementById("fechaInicio").value;
        const fechaFinal = document.getElementById("fechaFinal").value;
        const descripcion = document.getElementById("descripcion").value;

        // 1. Obtener fecha de hoy en formato YYYY-MM-DD
        // (new Date() se ajusta a la zona horaria local)
        const hoy = new Date();
        const hoyFormateado = hoy.getFullYear() + '-' +
                           String(hoy.getMonth() + 1).padStart(2, '0') + '-' +
                           String(hoy.getDate()).padStart(2, '0');

        // 2. Validar fecha de inicio
        if (fechaInicio < hoyFormateado) {
            showToast("La fecha de inicio no puede ser anterior al día de hoy.", "error");
            return;
        }

        // 3. Validar fecha final
        if (fechaFinal < fechaInicio) {
            showToast("La fecha final no puede ser anterior a la fecha de inicio.", "error");
            return;
        }
        // 🟢 FIN DE VALIDACIÓN 🟢

        const feria = {
            nombre: nombre,
            lugar: lugar,
            fechaInicio: fechaInicio,
            fechaFinal: fechaFinal,
            descripcion: descripcion
        };

        try {
            // 🟢 URL ACTUALIZADA 🟢
            await axios.post(API_BASE_URL, feria);

            // 🟢 TOAST AÑADIDO 🟢
            showToast("Feria creada correctamente", "success");
            form.reset();
            cargarFerias();
        } catch (err) {
            // 🟢 TOAST AÑADIDO 🟢
            showToast("Error al crear la feria", "error");
        }
    });

    // Cargar ferias
    async function cargarFerias() {
        try {
            // 🟢 URL ACTUALIZADA 🟢
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
            showToast("Error al cargar las ferias", "error");
        }
    }

    cargarFerias();

    // 🟢 FUNCIONES ACTUALIZADAS CON TOASTS Y TRY-CATCH 🟢

    window.activar = async (id) => {
        try {
            // 🟢 URL ACTUALIZADA 🟢
            await axios.patch(`${API_BASE_URL}/${id}/activar`);
            showToast("Feria activada", "success");
            cargarFerias();
        } catch (err) {
            showToast("Error al activar", "error");
        }
    };

    window.darBaja = async (id) => {
        try {
            // 🟢 URL ACTUALIZADA 🟢
            await axios.patch(`${API_BASE_URL}/${id}/baja`);
            showToast("Feria dada de baja", "success");
            cargarFerias();
        } catch (err) {
            showToast("Error al dar de baja", "error");
        }
    }

    window.eliminar = async (id) => {
        if (confirm("¿Eliminar esta feria? Esta acción no se puede deshacer.")) {
            try {
                // 🟢 URL ACTUALIZADA 🟢
                await axios.delete(`${API_BASE_URL}/${id}`);
                showToast("Feria eliminada", "success");
                cargarFerias();
            } catch (err) {
                showToast("Error al eliminar", "error");
            }
        }
    }
});

// 🟢 FUNCIÓN TOAST AÑADIDA (copiada de login.js) 🟢
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