const API_URL = "http://localhost:8080/api/ferias";
let feriasGlobal = [];

async function cargarFerias() {
  try {
    const response = await fetch(API_URL);
    feriasGlobal = await response.json();
    mostrarFerias(feriasGlobal);
  } catch (error) {
    console.error("Error al cargar las ferias:", error);
  }
}

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

// 🔹 Filtrado por nombre
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
