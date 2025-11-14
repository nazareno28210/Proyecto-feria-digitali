/*
 * ====================================
 * JS de Gestión de Ferias (Sin 'confirm')
 * ====================================
 */

// 1. Función Toastify (con paleta de colores)
function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }
  Toastify({
    text: message,
    duration: 4000,
    gravity: "top", 
    position: "right", 
    style: {
        background: color,
    },
    stopOnFocus: true,
  }).showToast();
}


document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("form-feria");
    const tbody = document.querySelector("#tabla-ferias tbody");

    const API_BASE_URL = "http://localhost:8080/api/ferias";

    // Crear feria
    form.addEventListener("submit", async e => {
        e.preventDefault();

        // Validaciones de frontend (cliente)
        const fechaInicio = document.getElementById("fechaInicio").value;
        const fechaFinal = document.getElementById("fechaFinal").value;

        const hoy = new Date();
        const hoyFormateado = hoy.getFullYear() + '-' +
                           String(hoy.getMonth() + 1).padStart(2, '0') + '-' +
                           String(hoy.getDate()).padStart(2, '0');

        if (fechaInicio < hoyFormateado) {
            showToast("La fecha de inicio no puede ser anterior al día de hoy.", "warning");
            return;
        }
        if (fechaFinal < fechaInicio) {
            showToast("La fecha final no puede ser anterior a la fecha de inicio.", "warning");
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
            if (err.response && err.response.data) {
                 showToast(err.response.data, "error");
            } else {
                showToast("Error al crear la feria", "error");
            }
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
                        <button onclick="darBaja(${f.id})">Baja</button>
                        <button onclick="eliminar(${f.id})">Eliminar</button>
                    `;
                } else {
                    acciones = `
                        <button onclick="activar(${f.id})">Activar</button>
                        <button onclick="eliminar(${f.id})">Eliminar</button>
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
            const msg = err.response ? err.response.data : "Error al cargar las ferias";
            showToast(msg, "error");
        }
    }

    cargarFerias();

    // Funciones de acciones
    
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
        // El código de borrado se ejecuta inmediatamente
        try {
            await axios.delete(`${API_BASE_URL}/${id}`);
            showToast("Feria eliminada", "success");
            cargarFerias();
        } catch (err) {
            const msg = err.response ? `Error: ${err.response.data}` : "Error al eliminar";
            showToast(msg, "error");
        }
    }
});