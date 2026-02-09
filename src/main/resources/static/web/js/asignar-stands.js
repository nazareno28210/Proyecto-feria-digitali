/*
 * ====================================
 * ASIGNAR-STANDS.JS (Actualizado: Restricción por Stand Inactivo)
 * ====================================
 */

document.addEventListener("DOMContentLoaded", () => {
    // Referencias al DOM
    const feriaSelect = document.getElementById("feria-select");
    const gestionContainer = document.getElementById("gestion-stands");
    const disponiblesList = document.getElementById("stands-disponibles");
    const enFeriaList = document.getElementById("stands-en-feria");
    const guardarBtn = document.getElementById("btn-guardar");

    // Almacenes de datos
    let todasLasFerias = [];
    let todosLosStands = [];
    let originalStandsEnFeria = []; 
    let selectedFeriaId = null;

    // ========================================================
    // INICIALIZACIÓN
    // ========================================================

    async function init() {
        await cargarDatosIniciales();
        feriaSelect.addEventListener("change", mostrarListas);
        guardarBtn.addEventListener("click", guardarCambios);
        initClickListeners();
    }

    async function cargarDatosIniciales() {
        try {
            const [resFerias, resStands] = await Promise.all([
                axios.get("/api/ferias"),
                axios.get("/api/stands")
            ]);
            
            todasLasFerias = resFerias.data;
            todosLosStands = resStands.data; // Aquí ya viene el campo 'activo' del DTO

            // Poblar selector de ferias
            feriaSelect.innerHTML = '<option value="">Selecciona una feria...</option>';
            todasLasFerias.forEach(feria => {
                const estado = feria.estado === 'Activa' ? '🟢' : '🔴';
                feriaSelect.innerHTML += `
                    <option value="${feria.id}">${estado} ${feria.nombre} (${feria.estado})</option>
                `;
            });
        } catch (error) {
            console.error("Error fatal al cargar datos:", error);
            showToast("Error al cargar datos iniciales", "error");
        }
    }

    // ========================================================
    // LÓGICA DE RENDERIZADO (CON FILTRO DE ACTIVACIÓN)
    // ========================================================

    function mostrarListas() {
        selectedFeriaId = parseInt(feriaSelect.value);
        if (!selectedFeriaId) {
            gestionContainer.classList.add("hidden");
            return;
        }

        disponiblesList.innerHTML = '<h3>Stands Disponibles</h3>';
        enFeriaList.innerHTML = '<h3>Stands en esta Feria</h3>';

        const feriaSeleccionada = todasLasFerias.find(f => f.id === selectedFeriaId);
        const idsStandsEnFeria = feriaSeleccionada ? feriaSeleccionada.stands.map(s => s.id) : [];

        todosLosStands.forEach(stand => {
            if (idsStandsEnFeria.includes(stand.id)) {
                renderStandItem(stand, enFeriaList);
            } else {
                renderStandItem(stand, disponiblesList);
            }
        });
        
        originalStandsEnFeria = idsStandsEnFeria;
        gestionContainer.classList.remove("hidden");
    }

    function renderStandItem(stand, lista) {
        const ferianteNombre = stand.feriante ? stand.feriante.nombreEmprendimiento : "Sin feriante";
        const item = document.createElement("div");
        item.className = "stand-item";
        item.dataset.standId = stand.id;

        const isAvailableList = (lista.id === 'stands-disponibles');
        
        // 🟢 LÓGICA DE ESTADO: Verificar si el stand está desactivado por el feriante
        const estaDesactivado = !stand.activo;
        const statusBadge = estaDesactivado 
            ? '<span class="status-badge-mini closed">Cerrado</span>' 
            : '<span class="status-badge-mini open">Abierto</span>';

        // 🟢 RESTRICCIÓN: Si está desactivado y está en la lista de disponibles, deshabilitamos el botón
        const disabledAttr = (isAvailableList && estaDesactivado) ? 'disabled' : '';
        const titleAttr = (isAvailableList && estaDesactivado) ? 'title="El feriante desactivó su stand y no puede ser asignado"' : '';

        const btnClass = isAvailableList ? 'btn-primary' : 'btn-logout';
        const btnText = isAvailableList ? '+ Agregar' : 'Quitar';

        item.innerHTML = `
            <div class="stand-info">
                <strong>${stand.nombre}</strong>
                <small>${ferianteNombre}</small>
                ${statusBadge}
            </div>
            <button class="btn ${btnClass} btn-accion" ${disabledAttr} ${titleAttr}>
                ${btnText}
            </button>
        `;
        
        // Añadimos una clase visual a la fila si está desactivada
        if (estaDesactivado) item.classList.add("item-inactivo");
        
        lista.appendChild(item);
    }

    // ========================================================
    // LÓGICA DE CLIC
    // ========================================================

    function initClickListeners() {
        gestionContainer.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-accion')) {
                const item = e.target.closest('.stand-item');

                // Mover a la lista de "En Feria"
                if (e.target.classList.contains('btn-primary')) {
                    enFeriaList.appendChild(item); 
                    e.target.textContent = 'Quitar';
                    e.target.classList.remove('btn-primary');
                    e.target.classList.add('btn-logout'); 
                } 
                // Mover a la lista de "Disponibles"
                else if (e.target.classList.contains('btn-logout')) {
                    disponiblesList.appendChild(item); 
                    e.target.textContent = '+ Agregar';
                    e.target.classList.remove('btn-logout');
                    e.target.classList.add('btn-primary');
                }
            }
        });
    }

    // ========================================================
    // LÓGICA DE GUARDADO
    // ========================================================

    async function guardarCambios() {
        if (!selectedFeriaId) return;

        const finalStandIdsEnFeria = Array.from(enFeriaList.querySelectorAll(".stand-item"))
                                          .map(item => parseInt(item.dataset.standId));

        const standsToAssign = finalStandIdsEnFeria.filter(
            id => !originalStandsEnFeria.includes(id)
        );
        const standsToUnassign = originalStandsEnFeria.filter(
            id => !finalStandIdsEnFeria.includes(id)
        );

        const promises = [];
        standsToAssign.forEach(standId => {
            promises.push(axios.patch(`/api/stands/${standId}/asignar-feria/${selectedFeriaId}`));
        });
        standsToUnassign.forEach(standId => {
            promises.push(axios.patch(`/api/stands/${standId}/desasignar-feria`));
        });

        if (promises.length === 0) {
            showToast("No se detectaron cambios.", "warning");
            return;
        }

        try {
            await Promise.all(promises);
            showToast(`Cambios guardados correctamente.`, "success");
            await cargarDatosIniciales();
            mostrarListas(); 
        } catch (error) {
            // Si el backend lanza el error que configuramos por stand desactivado 
            const errorMsg = error.response?.data || "Error al guardar los cambios.";
            showToast(errorMsg, "error");
        }
    }

    function showToast(message, type = "info") {
        let color;
        switch (type) {
            case "success": color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; break;
            case "error": color = "linear-gradient(to right, #ef4444, #b91c1c)"; break;
            case "warning": color = "linear-gradient(to right, #3b82f6, #67e8f9)"; break;
            default: color = "linear-gradient(to right, #3b82f6, #67e8f9)";
        }
        Toastify({
            text: message,
            duration: 3000,
            gravity: "top",
            position: "right",
            style: { background: color },
            stopOnFocus: true,
        }).showToast();
    }

    init();
});