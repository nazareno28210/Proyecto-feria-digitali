/*
 * ====================================
 * JS de Gestión de Ferias (CRUD Completo)
 * ====================================
 */

function showToast(message, type = "info") {
    let color;
    switch (type) {
        case "success": color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; break;
        case "error": color = "linear-gradient(to right, #ef4444, #b91c1c)"; break;
        case "warning": color = "linear-gradient(to right, #f59e0b, #d97706)"; break;
        default: color = "linear-gradient(to right, #3b82f6, #67e8f9)";
    }
    Toastify({
        text: message,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: { background: color },
    }).showToast();
}

document.addEventListener("DOMContentLoaded", () => {
    const formCrear = document.getElementById("form-feria");
    const tbody = document.querySelector("#tabla-ferias tbody");
    const API_BASE_URL = "http://localhost:8080/api/ferias";

    // 1. Cargar ferias (El Admin ve todas las no eliminadas)
    async function cargarFerias() {
        try {
            const res = await axios.get(API_BASE_URL);
            tbody.innerHTML = "";

            res.data.forEach(f => {
                const row = document.createElement("tr");
                if (f.estado === "Inactiva") row.classList.add("fila-inactiva");

                row.innerHTML = `
                    <td>${f.nombre}</td>
                    <td>${f.lugar}</td>
                    <td>${f.fechaInicio} / ${f.fechaFinal}</td>
                    <td><span class="badge-${f.estado.toLowerCase()}">${f.estado}</span></td>
                    <td>
                        <button class="btn-editar" onclick='abrirModalEditar(${JSON.stringify(f)})'>
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="${f.estado === 'Activa' ? 'btn-baja' : 'btn-activar'}" 
                                onclick="${f.estado === 'Activa' ? 'darBaja' : 'activar'}(${f.id})">
                            ${f.estado === 'Activa' ? 'Desactivar' : 'Activar'}
                        </button>
                        <button class="btn-eliminar" onclick="eliminar(${f.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } catch (err) {
            showToast("Error al cargar ferias", "error");
        }
    }

    // 2. Crear feria
    formCrear.addEventListener("submit", async e => {
        e.preventDefault();
        const feria = {
            nombre: document.getElementById("nombre").value,
            lugar: document.getElementById("lugar").value,
            fechaInicio: document.getElementById("fechaInicio").value,
            fechaFinal: document.getElementById("fechaFinal").value,
            imagenUrl: document.getElementById("imagenUrl").value,
            descripcion: document.getElementById("descripcion").value
        };

        try {
            await axios.post(API_BASE_URL, feria);
            showToast("Feria creada correctamente", "success");
            formCrear.reset();
            cargarFerias();
        } catch (err) {
            showToast(err.response?.data || "Error al crear", "error");
        }
    });

    // 3. Funciones de Estado y Borrado Lógico
    window.activar = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/${id}/activar`);
            showToast("Feria activada", "success");
            cargarFerias();
        } catch (err) { showToast("Error al activar", "error"); }
    };

    window.darBaja = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/${id}/baja`);
            showToast("Feria desactivada", "warning");
            cargarFerias();
        } catch (err) { showToast("Error al desactivar", "error"); }
    };

    window.eliminar = async (id) => {
        if (!confirm("¿Deseas eliminar esta feria? No aparecerá más en el sistema.")) return;
        try {
            await axios.put(`${API_BASE_URL}/${id}/eliminar`); // Borrado lógico
            showToast("Feria eliminada", "success");
            cargarFerias();
        } catch (err) { showToast("Error al eliminar", "error"); }
    };

    // 4. Lógica del Modal de Edición
    window.abrirModalEditar = (feria) => {
        document.getElementById("edit-id").value = feria.id;
        document.getElementById("edit-nombre").value = feria.nombre;
        document.getElementById("edit-lugar").value = feria.lugar;
        document.getElementById("edit-fechaInicio").value = feria.fechaInicio;
        document.getElementById("edit-fechaFinal").value = feria.fechaFinal;
        document.getElementById("edit-imagenUrl").value = feria.imagenUrl || "";
        document.getElementById("edit-descripcion").value = feria.descripcion;
        document.getElementById("modal-editar").style.display = "block";
    };

    window.cerrarModal = () => {
        document.getElementById("modal-editar").style.display = "none";
    };

    document.getElementById("form-editar").addEventListener("submit", async (e) => {
        e.preventDefault();
        const id = document.getElementById("edit-id").value;
        const feriaEditada = {
            nombre: document.getElementById("edit-nombre").value,
            lugar: document.getElementById("edit-lugar").value,
            fechaInicio: document.getElementById("edit-fechaInicio").value,
            fechaFinal: document.getElementById("edit-fechaFinal").value,
            imagenUrl: document.getElementById("edit-imagenUrl").value,
            descripcion: document.getElementById("edit-descripcion").value
        };

        try {
            await axios.put(`${API_BASE_URL}/${id}`, feriaEditada); // Actualizar
            showToast("Feria actualizada correctamente", "success");
            cerrarModal();
            cargarFerias();
        } catch (err) { showToast("Error al actualizar", "error"); }
    });

    cargarFerias();
});