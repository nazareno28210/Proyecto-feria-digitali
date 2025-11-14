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
    let originalStandsEnFeria = []; // IDs de stands originalmente en la feria
    let selectedFeriaId = null;

    // ========================================================
    // INICIALIZACIÓN
    // ========================================================

    async function init() {
        await cargarDatosIniciales();
        feriaSelect.addEventListener("change", mostrarListas);
        guardarBtn.addEventListener("click", guardarCambios);

        // 🔴 CAMBIO: Ya no se llama a initDragAndDrop()
        // 🟢 CAMBIO: Se inicializan los listeners de clic
        initClickListeners();
    }

    async function cargarDatosIniciales() {
        try {
            const [resFerias, resStands] = await Promise.all([
                axios.get("/api/ferias"),
                axios.get("/api/stands")
            ]);

            todasLasFerias = resFerias.data;
            todosLosStands = resStands.data;

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
    // LÓGICA DE RENDERIZADO (AL CAMBIAR FERIA)
    // ========================================================

    function mostrarListas() {
        selectedFeriaId = parseInt(feriaSelect.value);

        if (!selectedFeriaId) {
            gestionContainer.classList.add("hidden");
            return;
        }

        // Limpiar listas
        disponiblesList.innerHTML = '<h3>Stands Disponibles</h3>';
        enFeriaList.innerHTML = '<h3>Stands en esta Feria</h3>';

        // Filtrar y renderizar stands
        const standsDisponibles = todosLosStands.filter(s => !s.feriaId);
        const standsEnFeria = todosLosStands.filter(s => s.feriaId === selectedFeriaId);

        // Guardar estado original para la lógica de guardado
        originalStandsEnFeria = standsEnFeria.map(s => s.id);

        standsDisponibles.forEach(stand => renderStandItem(stand, disponiblesList));
        standsEnFeria.forEach(stand => renderStandItem(stand, enFeriaList));

        gestionContainer.classList.remove("hidden");
    }

    // 🟢 CAMBIO: renderStandItem ahora crea botones en lugar de items "arrastrables"
    function renderStandItem(stand, lista) {
        const ferianteNombre = stand.feriante ? stand.feriante.nombreEmprendimiento : "Sin feriante";
        const item = document.createElement("div");
        item.className = "stand-item";
        item.dataset.standId = stand.id;

        const isAvailableList = (lista.id === 'stands-disponibles');

        // Usamos las clases de global.css
        const btnClass = isAvailableList ? 'btn-primary' : 'btn-logout';
        const btnText = isAvailableList ? '+ Agregar' : 'Quitar';

        item.innerHTML = `
            <span>${stand.nombre} (${ferianteNombre})</span>
            <button class="btn ${btnClass} btn-accion">${btnText}</button>
        `;
        lista.appendChild(item);
    }


    // ========================================================
    // 🟢 NUEVA LÓGICA DE CLIC (Reemplaza Drag & Drop)
    // ========================================================

    function initClickListeners() {
        // Usamos delegación de eventos en el contenedor principal
        gestionContainer.addEventListener('click', (e) => {

            // Si hacen clic en un botón de acción
            if (e.target.classList.contains('btn-accion')) {
                const item = e.target.closest('.stand-item');

                // Mover a la lista de "En Feria"
                if (e.target.classList.contains('btn-primary')) {
                    enFeriaList.appendChild(item); // Mueve el item en la UI
                    e.target.textContent = 'Quitar';
                    e.target.classList.remove('btn-primary');
                    e.target.classList.add('btn-logout'); // Clase roja

                // Mover a la lista de "Disponibles"
                } else if (e.target.classList.contains('btn-logout')) {
                    disponiblesList.appendChild(item); // Mueve el item en la UI
                    e.target.textContent = '+ Agregar';
                    e.target.classList.remove('btn-logout');
                    e.target.classList.add('btn-primary'); // Clase verde
                }
            }
        });
    }

    // ========================================================
    // LÓGICA DE GUARDADO (¡Esta función no necesita cambios!)
    // ========================================================

    async function guardarCambios() {
        if (!selectedFeriaId) return;

        // 1. Obtener el estado final desde la UI (lee las listas tal como están)
        const finalStandIdsEnFeria = Array.from(enFeriaList.querySelectorAll(".stand-item"))
                                          .map(item => parseInt(item.dataset.standId));

        // 2. Calcular los cambios (Deltas)
        const standsToAssign = finalStandIdsEnFeria.filter(
            id => !originalStandsEnFeria.includes(id)
        );

        const standsToUnassign = originalStandsEnFeria.filter(
            id => !finalStandIdsEnFeria.includes(id)
        );

        // 3. Crear promesas para todas las llamadas API
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

        // 4. Ejecutar todas las promesas
        try {
            await Promise.all(promises);
            showToast(`Cambios guardados: ${standsToAssign.length} asignados, ${standsToUnassign.length} quitados.`, "success");

            // 5. Recargar el estado desde el servidor
            await cargarDatosIniciales();
            // Renderizar la vista con los nuevos datos
            mostrarListas();

        } catch (error) {
            console.error("Error al guardar cambios:", error);
            showToast("Error al guardar los cambios.", "error");
        }
    }

    // ========================================================
    // FUNCIÓN DE TOAST (COPIADA DE login.js)
    // ========================================================
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
            default:
                color = "linear-gradient(to right, #f7971e, #ffd200)";
        }

        Toastify({
            text: message,
            duration: 4000,
            gravity: "top",
            position: "right",
            backgroundColor: color,
            stopOnFocus: true,
        }).showToast();
    }

    // Iniciar la aplicación
    init();
});