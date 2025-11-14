/*
 * ====================================
 * ASIGNAR-STANDS.JS (Lógica corregida)
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
            
            // Asignamos los datos
            todasLasFerias = resFerias.data;
            todosLosStands = resStands.data; // Lista completa de stands

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
    // LÓGICA DE RENDERIZADO (CORREGIDA)
    // ========================================================

    function mostrarListas() {
        selectedFeriaId = parseInt(feriaSelect.value);
        if (!selectedFeriaId) {
            gestionContainer.classList.add("hidden");
            return;
        }

        disponiblesList.innerHTML = '<h3>Stands Disponibles</h3>';
        enFeriaList.innerHTML = '<h3>Stands en esta Feria</h3>';

        // ==============================================
        //  BLOQUE DE LÓGICA CORREGIDO
        // ==============================================
        
        // 1. Encontrar los IDs de los stands que YA están en la feria seleccionada.
        //    Buscamos la feria en la lista de ferias...
        const feriaSeleccionada = todasLasFerias.find(f => f.id === selectedFeriaId);
        //    ...y obtenemos los IDs de sus stands.
        const idsStandsEnFeria = feriaSeleccionada ? feriaSeleccionada.stands.map(s => s.id) : [];

        // 2. Filtrar la lista COMPLETA de stands (todosLosStands)
        todosLosStands.forEach(stand => {
            if (idsStandsEnFeria.includes(stand.id)) {
                // Si el ID está en la lista de la feria, va a la columna "Stands en esta Feria"
                renderStandItem(stand, enFeriaList);
            } else {
                // Si no, va a la columna "Stands Disponibles"
                renderStandItem(stand, disponiblesList);
            }
        });
        
        // ==============================================

        // Guardar estado original para la lógica de guardado
        originalStandsEnFeria = idsStandsEnFeria;

        gestionContainer.classList.remove("hidden");
    }

    function renderStandItem(stand, lista) {
        const ferianteNombre = stand.feriante ?
            stand.feriante.nombreEmprendimiento : "Sin feriante";
        const item = document.createElement("div");
        item.className = "stand-item";
        item.dataset.standId = stand.id;

        const isAvailableList = (lista.id === 'stands-disponibles');
        
        // Usamos las clases de global.css (btn-primary = azul, btn-logout = rojo)
        const btnClass = isAvailableList ? 'btn-primary' : 'btn-logout';
        const btnText = isAvailableList ? '+ Agregar' : 'Quitar';

        item.innerHTML = `
            <span>${stand.nombre} (${ferianteNombre})</span>
            <button class="btn ${btnClass} btn-accion">${btnText}</button>
        `;
        lista.appendChild(item);
    }


    // ========================================================
    // LÓGICA DE CLIC (Sin cambios)
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
    // LÓGICA DE GUARDADO (Sin cambios)
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
            promises.push(
                axios.patch(`/api/stands/${standId}/asignar-feria/${selectedFeriaId}`)
            );
        });
        standsToUnassign.forEach(standId => {
            promises.push(
                axios.patch(`/api/stands/${standId}/desasignar-feria`)
            );
        });

        if (promises.length === 0) {
            showToast("No se detectaron cambios.", "warning");
            return;
        }

        try {
            await Promise.all(promises);
            showToast(`Cambios guardados: ${standsToAssign.length} asignados, ${standsToUnassign.length} quitados.`, "success");

            // Recargamos el estado desde el servidor
            await cargarDatosIniciales();
            mostrarListas(); // Re-renderiza las listas
        } catch (error) {
            console.error("Error al guardar cambios:", error);
            showToast("Error al guardar los cambios.", "error");
        }
    }

    // ========================================================
    // FUNCIÓN DE TOAST 
    // ========================================================
    function showToast(message, type = "info") {
        let color;
        // CAMBIO: Colores actualizados a la paleta de la app
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
            duration: 2000,
            gravity: "top",
            position: "right",
            style: {
                background: color,
            },
            stopOnFocus: true,
        }).showToast();
    }

    // Iniciar la aplicación
    init();
});