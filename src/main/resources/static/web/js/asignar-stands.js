document.addEventListener("DOMContentLoaded", () => {
    cargarDatos();
});

let feriasActivas = [];
let todasLasFerias = [];
let todosLosStands = [];

async function cargarDatos() {
    try {
        // 1. Cargar todas las ferias (para nombres) y ferias activas (para dropdown)
        const [resFerias, resStands, resFeriasActivas] = await Promise.all([
            axios.get("/api/ferias"),
            axios.get("/api/stands"),
            axios.get("/api/ferias/activas")
        ]);

        todasLasFerias = resFerias.data;
        todosLosStands = resStands.data;
        feriasActivas = resFeriasActivas.data;

        // 2. Separar stands
        const standsSinAsignar = todosLosStands.filter(s => !s.feriaId);
        const standsAsignados = todosLosStands.filter(s => s.feriaId);

        // 3. Renderizar tablas
        renderStandsSinAsignar(standsSinAsignar);
        renderStandsAsignados(standsAsignados);

    } catch (error) {
        console.error("Error al cargar datos:", error);
        alert("No se pudieron cargar los datos de stands y ferias.");
    }
}

// Renderiza la tabla de stands sin feria asignada
function renderStandsSinAsignar(stands) {
    const tbody = document.getElementById("body-stands-sin-asignar");
    tbody.innerHTML = "";

    if (stands.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No hay stands pendientes de asignación.</td></tr>';
        return;
    }

    // Crear el HTML del dropdown de ferias activas
    const feriasOptions = feriasActivas.map(f =>
        `<option value="${f.id}">${f.nombre}</option>`
    ).join('');

    stands.forEach(stand => {
        const row = document.createElement("tr");
        const ferianteNombre = stand.feriante ? stand.feriante.nombreEmprendimiento : "Feriante no definido";

        row.innerHTML = `
            <td>${stand.nombre} (${ferianteNombre})</td>
            <td>
                <select id="select-feria-${stand.id}">
                    <option value="">Seleccionar feria...</option>
                    ${feriasOptions}
                </select>
            </td>
            <td>
                <button onclick="asignarStand(${stand.id})" style="background-color:#2ecc71; color:white;">Asignar</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Renderiza la tabla de stands que SÍ tienen feria
function renderStandsAsignados(stands) {
    const tbody = document.getElementById("body-stands-asignados");
    tbody.innerHTML = "";

    if (stands.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No hay stands asignados.</td></tr>';
        return;
    }

    stands.forEach(stand => {
        const row = document.createElement("tr");
        const ferianteNombre = stand.feriante ? stand.feriante.nombreEmprendimiento : "Feriante no definido";

        // Buscar el nombre de la feria usando el ID
        const feria = todasLasFerias.find(f => f.id === stand.feriaId);
        const feriaNombre = feria ? feria.nombre : `ID Feria: ${stand.feriaId}`;

        row.innerHTML = `
            <td>${stand.nombre} (${ferianteNombre})</td>
            <td>${feriaNombre}</td>
            <td>
                <button onclick="desasignarStand(${stand.id})" style="background-color:#e74c3c; color:white;">Quitar</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// --- Funciones de Acción ---

async function asignarStand(standId) {
    const select = document.getElementById(`select-feria-${standId}`);
    const feriaId = select.value;

    if (!feriaId) {
        alert("Por favor, selecciona una feria.");
        return;
    }

    try {
        await axios.patch(`/api/stands/${standId}/asignar-feria/${feriaId}`);
        alert("Stand asignado correctamente.");
        cargarDatos(); // Recargar las tablas
    } catch (error) {
        console.error("Error al asignar:", error);
        alert("Error al asignar el stand.");
    }
}

async function desasignarStand(standId) {
    if (!confirm("¿Seguro que deseas quitar este stand de su feria?")) {
        return;
    }

    try {
        await axios.patch(`/api/stands/${standId}/desasignar-feria`);
        alert("Stand desasignado correctamente.");
        cargarDatos(); // Recargar las tablas
    } catch (error) {
        console.error("Error al desasignar:", error);
        alert("Error al desasignar el stand.");
    }
}