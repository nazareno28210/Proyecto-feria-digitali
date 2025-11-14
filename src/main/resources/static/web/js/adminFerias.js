document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("form-feria");
    const tbody = document.querySelector("#tabla-ferias tbody");

    // Crear feria
    form.addEventListener("submit", async e => {
        e.preventDefault();
        const feria = {
            nombre: document.getElementById("nombre").value,
            lugar: document.getElementById("lugar").value,
            fechaInicio: document.getElementById("fechaInicio").value,
            fechaFinal: document.getElementById("fechaFinal").value,
            descripcion: document.getElementById("descripcion").value
        };

        try {
            await axios.post("/api/ferias", feria);
            alert("Feria creada correctamente");
            form.reset();
            cargarFerias();
        } catch (err) {
            alert("Error al crear la feria");
        }
    });

    // Cargar ferias
async function cargarFerias() {
    const res = await axios.get("/api/ferias");
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
  }

    cargarFerias();

    window.activar = async (id) => {
    await axios.patch(`/api/ferias/${id}/activar`);
    cargarFerias();
    };

    window.darBaja = async (id) => {
        await axios.patch(`/api/ferias/${id}/baja`);
        cargarFerias();
    }

    window.eliminar = async (id) => {
        if (confirm("¿Eliminar esta feria?")) {
            await axios.delete(`/api/ferias/${id}`);
            cargarFerias();
        }
    }

    
});
