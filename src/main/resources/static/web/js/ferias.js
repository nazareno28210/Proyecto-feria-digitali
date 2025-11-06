const API_URL = "http://localhost:8080/api/ferias";
let feriasGlobal = [];

async function cargarFerias() {
  try {
    // 1. CAMBIO: Usamos axios.get() en lugar de fetch()
    const response = await axios.get(API_URL);

    // 2. CAMBIO: Axios entrega los datos JSON directamente en la propiedad 'data'
    feriasGlobal = response.data;

    mostrarFerias(feriasGlobal);
  } catch (error) {
    console.error("Error al cargar las ferias:", error);
    // Aquí podrías añadir un mensaje de error en el HTML
  }
}

// El resto de tu código sigue exactamente igual
function mostrarFerias(lista) {
  const container = document.getElementById("ferias-container");
  container.innerHTML = "";

  lista.forEach((feria) => {
    const card = document.createElement("div");
    card.classList.add("card");

    card.innerHTML = `
      <div class="card-content">
        <h2>${feria.nombre}</h2>
        <p><strong>Lugar:</strong> ${feria.lugar}</p>
        <p><strong>Fecha inicio:</strong> ${feria.fechaInicio}</p>
        <p><strong>Fecha fin:</strong> ${feria.fechaFin ?? "Sin definir"}</p>
        <p>${feria.descripcion ?? ""}</p>
      </div>
      <button onclick="verDetalles(${feria.id})">Ver detalles</button>
    `;

    container.appendChild(card);
  });
}

function verDetalles(id) {
  window.location.href = `feria_detalle.html?id=${id}`;
}

// 🔹 Filtrado por nombre (esto tampoco cambia)
document.addEventListener("DOMContentLoaded", () => {
  cargarFerias();

  const inputBusqueda = document.getElementById("busqueda");
  inputBusqueda.addEventListener("input", () => {
    const texto = inputBusqueda.value.toLowerCase();
    const filtradas = feriasGlobal.filter((f) =>
      f.nombre.toLowerCase().includes(texto)
    );
    mostrarFerias(filtradas);
  });
});