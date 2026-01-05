-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 05-01-2026 a las 11:36:54
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `calendario_app`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--
CREATE TABLE `clientes` (
  `id_cliente` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellidos` varchar(150) NOT NULL,
  `creado_el` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_el` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id_cliente`, `nombre`, `apellidos`, `creado_el`, `actualizado_el`) VALUES
(1, 'Sergio', 'Aracil Ortuño', '2025-12-10 09:29:01', '2025-12-10 09:29:01'),
(7, 'Antonio', 'Gonzalez', '2025-12-10 11:19:32', '2025-12-10 11:19:32');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `etiquetas`
--

CREATE TABLE `etiquetas` (
  `id_etiqueta` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `color` char(7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `etiquetas_usuarios`
--

CREATE TABLE `etiquetas_usuarios` (
  `id_etiqueta` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos`
--

CREATE TABLE `eventos` (
  `id_evento` int(11) NOT NULL,
  `id_creador` int(11) DEFAULT NULL,
  `titulo` varchar(255) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_inicio` datetime NOT NULL,
  `fecha_fin` datetime NOT NULL,
  `ubicacion` varchar(255) DEFAULT NULL,
  `estado` enum('finalizado','en progreso','pendiente') NOT NULL DEFAULT 'pendiente',
  `creado_el` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_el` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos_usuarios`
--

CREATE TABLE `eventos_usuarios` (
  `id_usuario` int(11) NOT NULL,
  `id_evento` int(11) NOT NULL,
  `estado` enum('aceptado','pendiente','rechazado') NOT NULL DEFAULT 'pendiente',
  `notificado` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `festivos`
--

CREATE TABLE `festivos` (
  `id_festivo` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `mes` int(11) NOT NULL COMMENT 'Mes del festivo (1-12)',
  `dia` int(11) NOT NULL COMMENT 'Día del festivo (1-31)',
  `tipo` enum('nacional','religioso','regional') DEFAULT 'nacional',
  `descripcion` text DEFAULT NULL,
  `es_fijo` tinyint(1) DEFAULT 1 COMMENT 'TRUE si es fecha fija, FALSE si es móvil (ej: Semana Santa)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `festivos`
--

INSERT INTO `festivos` (`id_festivo`, `nombre`, `mes`, `dia`, `tipo`, `descripcion`, `es_fijo`) VALUES
(1, 'Año Nuevo', 1, 1, 'nacional', 'Año Nuevo', 1),
(2, 'Epifanía del Señor', 1, 6, 'religioso', 'Día de Reyes', 1),
(3, 'San José', 3, 19, 'regional', 'San José (Comunidad Valenciana, Murcia, etc.)', 1),
(4, 'Día del Trabajador', 5, 1, 'nacional', 'Fiesta del Trabajo', 1),
(5, 'San Juan', 6, 24, 'religioso', 'Natividad de San Juan Bautista', 1),
(6, 'Santiago Apóstol', 7, 25, 'religioso', 'Santiago Apóstol, Patrón de España', 1),
(7, 'Asunción de la Virgen', 8, 15, 'religioso', 'Asunción de la Virgen', 1),
(8, 'Fiesta Nacional de España', 10, 12, 'nacional', 'Día de la Hispanidad', 1),
(9, 'Todos los Santos', 11, 1, 'religioso', 'Día de Todos los Santos', 1),
(10, 'Día de la Constitución', 12, 6, 'nacional', 'Día de la Constitución Española', 1),
(11, 'Inmaculada Concepción', 12, 8, 'religioso', 'Inmaculada Concepción', 1),
(12, 'Navidad', 12, 25, 'religioso', 'Navidad', 1),
(13, 'San Esteban', 12, 26, 'regional', 'San Esteban (Cataluña, Baleares)', 1),
(14, 'Jueves Santo 2024', 3, 28, 'religioso', 'Jueves Santo', 0),
(15, 'Viernes Santo 2024', 3, 29, 'religioso', 'Viernes Santo', 0),
(16, 'Lunes de Pascua 2024', 4, 1, 'regional', 'Lunes de Pascua (Cataluña, Valencia, Baleares, etc.)', 0),
(17, 'Jueves Santo 2025', 4, 17, 'religioso', 'Jueves Santo', 0),
(18, 'Viernes Santo 2025', 4, 18, 'religioso', 'Viernes Santo', 0),
(19, 'Lunes de Pascua 2025', 4, 21, 'regional', 'Lunes de Pascua (Cataluña, Valencia, Baleares, etc.)', 0),
(20, 'Jueves Santo 2026', 4, 2, 'religioso', 'Jueves Santo', 0),
(21, 'Viernes Santo 2026', 4, 3, 'religioso', 'Viernes Santo', 0),
(22, 'Lunes de Pascua 2026', 4, 6, 'regional', 'Lunes de Pascua (Cataluña, Valencia, Baleares, etc.)', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `grupos`
--

CREATE TABLE `grupos` (
  `id_grupo` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `creado_el` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_el` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `grupos_usuarios`
--

CREATE TABLE `grupos_usuarios` (
  `id_grupo` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `rol` enum('admin','miembro') NOT NULL DEFAULT 'miembro',
  `fecha_union` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `id_cliente` int(11) NOT NULL,
  `correo` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` enum('admin','usuario') NOT NULL DEFAULT 'usuario'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `id_cliente`, `correo`, `password_hash`, `rol`) VALUES
(1, 1, 'sergioaracilortuno@gmail.com', 'sostenibilidad', 'usuario'),
(2, 7, 'antonio@gmail.com', '1234', 'usuario');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id_cliente`);

--
-- Indices de la tabla `etiquetas`
--
ALTER TABLE `etiquetas`
  ADD PRIMARY KEY (`id_etiqueta`);

--
-- Indices de la tabla `etiquetas_usuarios`
--
ALTER TABLE `etiquetas_usuarios`
  ADD PRIMARY KEY (`id_etiqueta`,`id_usuario`),
  ADD KEY `fk_usuario` (`id_usuario`);

--
-- Indices de la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD PRIMARY KEY (`id_evento`),
  ADD KEY `fk_eventos_creador` (`id_creador`),
  ADD KEY `idx_eventos_fechas` (`fecha_inicio`,`fecha_fin`);

--
-- Indices de la tabla `eventos_usuarios`
--
ALTER TABLE `eventos_usuarios`
  ADD PRIMARY KEY (`id_usuario`,`id_evento`),
  ADD KEY `idx_eventos_usuarios_evento` (`id_evento`);

--
-- Indices de la tabla `festivos`
--
ALTER TABLE `festivos`
  ADD PRIMARY KEY (`id_festivo`),
  ADD KEY `idx_festivos_mes_dia` (`mes`,`dia`);

--
-- Indices de la tabla `grupos`
--
ALTER TABLE `grupos`
  ADD PRIMARY KEY (`id_grupo`);

--
-- Indices de la tabla `grupos_usuarios`
--
ALTER TABLE `grupos_usuarios`
  ADD PRIMARY KEY (`id_grupo`,`id_usuario`),
  ADD KEY `idx_grupos_usuarios_usuario` (`id_usuario`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `correo` (`correo`),
  ADD KEY `fk_usuarios_clientes` (`id_cliente`),
  ADD KEY `idx_usuarios_correo` (`correo`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id_cliente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `etiquetas`
--
ALTER TABLE `etiquetas`
  MODIFY `id_etiqueta` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `id_evento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `festivos`
--
ALTER TABLE `festivos`
  MODIFY `id_festivo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT de la tabla `grupos`
--
ALTER TABLE `grupos`
  MODIFY `id_grupo` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `etiquetas_usuarios`
--
ALTER TABLE `etiquetas_usuarios`
  ADD CONSTRAINT `fk_etiqueta` FOREIGN KEY (`id_etiqueta`) REFERENCES `etiquetas` (`id_etiqueta`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD CONSTRAINT `fk_eventos_creador` FOREIGN KEY (`id_creador`) REFERENCES `usuarios` (`id_usuario`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `eventos_usuarios`
--
ALTER TABLE `eventos_usuarios`
  ADD CONSTRAINT `fk_eu_evento` FOREIGN KEY (`id_evento`) REFERENCES `eventos` (`id_evento`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_eu_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `grupos_usuarios`
--
ALTER TABLE `grupos_usuarios`
  ADD CONSTRAINT `fk_gu_grupo` FOREIGN KEY (`id_grupo`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_gu_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `fk_usuarios_clientes` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
