-- =========================================================
-- SCRIPT DE INICIALIZACIÓN DE DATOS (Feria Digital)
-- Ejecutar en MySQL Workbench / DBeaver si la BD está vacía
-- =========================================================

-- Insertar Categorías principales
INSERT IGNORE INTO categoria_producto (id, nombre, descripcion) VALUES
(1, 'Artesanías', 'Productos hechos a mano, tejido, madera, cerámica y vidrio'),
(2, 'Gastronomía', 'Comidas, repostería, chocolates, panificados y bebidas artesanales'),
(3, 'Indumentaria', 'Ropa artesanal, estampados, sublimación y diseño independiente'),
(4, 'Vivero', 'Plantas de interior, exterior, macetas artesanales y suculentas'),
(5, 'Accesorios', 'Marroquinería, joyería, bijouterie y complementos'),
(6, 'Ilustración y Papelería', 'Cuadernos artesanales, stickers, láminas e impresiones');

-- Insertar Usuario Administrador por defecto (Contraseña: 123)
-- Hash BCrypt para '123': .7C/.5p7i2uR...
-- Podés crear administradores desde el registro o ajustando la tabla de usuarios
